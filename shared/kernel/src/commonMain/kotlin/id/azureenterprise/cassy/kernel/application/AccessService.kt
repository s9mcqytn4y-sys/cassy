package id.azureenterprise.cassy.kernel.application

import id.azureenterprise.cassy.kernel.data.KernelRepository
import id.azureenterprise.cassy.kernel.domain.AccessContext
import id.azureenterprise.cassy.kernel.domain.AccessSession
import id.azureenterprise.cassy.kernel.domain.BootstrapStoreRequest
import id.azureenterprise.cassy.kernel.domain.BootstrapStoreField
import id.azureenterprise.cassy.kernel.domain.BootstrapStoreFieldIssue
import id.azureenterprise.cassy.kernel.domain.BootstrapStoreValidationResult
import id.azureenterprise.cassy.kernel.domain.LoginResult
import id.azureenterprise.cassy.kernel.domain.OperatorAccount
import id.azureenterprise.cassy.kernel.domain.OperatorRole
import id.azureenterprise.cassy.kernel.domain.PinHasher
import id.azureenterprise.cassy.kernel.domain.TerminalBinding
import id.azureenterprise.cassy.kernel.domain.supports
import id.azureenterprise.cassy.kernel.domain.AccessCapability
import id.azureenterprise.cassy.kernel.domain.IdGenerator
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

class AccessService(
    private val kernelRepository: KernelRepository,
    private val pinHasher: PinHasher,
    private val clock: Clock
) {
    fun validateBootstrapRequest(request: BootstrapStoreRequest): BootstrapStoreValidationResult {
        val normalizedRequest = BootstrapStoreRequest(
            storeName = normalizeSingleLine(request.storeName),
            terminalName = normalizeSingleLine(request.terminalName),
            cashierName = normalizeSingleLine(request.cashierName),
            cashierPin = request.cashierPin.filter(Char::isDigit).take(6),
            supervisorName = normalizeSingleLine(request.supervisorName),
            supervisorPin = request.supervisorPin.filter(Char::isDigit).take(6),
            cashierAvatarPath = request.cashierAvatarPath?.trim()?.takeIf { it.isNotEmpty() },
            supervisorAvatarPath = request.supervisorAvatarPath?.trim()?.takeIf { it.isNotEmpty() }
        )

        val issues = buildList {
            validateName(
                value = normalizedRequest.storeName,
                field = BootstrapStoreField.STORE_NAME,
                emptyMessage = "Nama toko wajib diisi",
                maxLength = MAX_STORE_NAME_LENGTH
            ).let(::addAll)
            validateName(
                value = normalizedRequest.terminalName,
                field = BootstrapStoreField.TERMINAL_NAME,
                emptyMessage = "Nama terminal wajib diisi",
                maxLength = MAX_TERMINAL_NAME_LENGTH
            ).let(::addAll)
            validateName(
                value = normalizedRequest.cashierName,
                field = BootstrapStoreField.CASHIER_NAME,
                emptyMessage = "Nama kasir wajib diisi",
                maxLength = MAX_OPERATOR_NAME_LENGTH
            ).let(::addAll)
            validateName(
                value = normalizedRequest.supervisorName,
                field = BootstrapStoreField.SUPERVISOR_NAME,
                emptyMessage = "Nama supervisor wajib diisi",
                maxLength = MAX_OPERATOR_NAME_LENGTH
            ).let(::addAll)
            validatePin(normalizedRequest.cashierPin)?.let {
                add(BootstrapStoreFieldIssue(BootstrapStoreField.CASHIER_PIN, it))
            }
            validatePin(normalizedRequest.supervisorPin)?.let {
                add(BootstrapStoreFieldIssue(BootstrapStoreField.SUPERVISOR_PIN, it))
            }
        }

        return BootstrapStoreValidationResult(
            normalizedRequest = normalizedRequest,
            issues = issues
        )
    }

    suspend fun restoreContext(): AccessContext {
        kernelRepository.ensureOperationalMetadataDefaults()
        val binding = kernelRepository.getTerminalBinding()
        val operators = kernelRepository.listActiveOperators()
        val activeSession = kernelRepository.getActiveAccessSession()
        val activeOperator = activeSession?.let { kernelRepository.getOperatorById(it.operatorId) }
        return AccessContext(
            terminalBinding = binding,
            operators = operators,
            activeSession = activeSession,
            activeOperator = activeOperator
        )
    }

    suspend fun needsBootstrap(): Boolean {
        val context = restoreContext()
        return context.terminalBinding == null || context.operators.isEmpty()
    }

    suspend fun bootstrapStore(request: BootstrapStoreRequest): Result<TerminalBinding> {
        val validation = validateBootstrapRequest(request)
        if (!validation.isValid) {
            return Result.failure(IllegalArgumentException(validation.issues.first().message))
        }

        if (!needsBootstrap()) {
            return Result.failure(IllegalStateException("Store sudah di-bootstrap"))
        }

        val normalized = validation.normalizedRequest
        val boundAt = clock.now()
        val binding = TerminalBinding(
            storeId = IdGenerator.nextId("store"),
            storeName = normalized.storeName,
            terminalId = IdGenerator.nextId("terminal"),
            terminalName = normalized.terminalName,
            boundAt = boundAt
        )
        kernelRepository.upsertTerminalBinding(binding)

        seedOperator(
            employeeCode = "cashier",
            displayName = normalized.cashierName,
            role = OperatorRole.CASHIER,
            pin = normalized.cashierPin,
            avatarPath = normalized.cashierAvatarPath
        )
        seedOperator(
            employeeCode = "supervisor",
            displayName = normalized.supervisorName,
            role = OperatorRole.SUPERVISOR,
            pin = normalized.supervisorPin,
            avatarPath = normalized.supervisorAvatarPath
        )

        kernelRepository.insertAudit(
            id = IdGenerator.nextId("audit"),
            message = "Bootstrap store ${binding.storeName} pada terminal ${binding.terminalName}",
            level = "INFO"
        )

        return Result.success(binding)
    }

    suspend fun login(operatorId: String, pin: String): LoginResult {
        val binding = kernelRepository.getTerminalBinding() ?: return LoginResult.NotBound
        return when (val authResult = authenticateOperator(operatorId, pin)) {
            is PinAuthResult.Success -> {
                val operator = authResult.operator
                val now = authResult.at
                kernelRepository.clearActiveAccessSessions()
                kernelRepository.updateOperatorAccessState(
                    operatorId = operator.id,
                    failedAttempts = 0,
                    lockedUntil = null,
                    lastLoginAt = now
                )
                val session = AccessSession(
                    id = IdGenerator.nextId("session"),
                    operatorId = operator.id,
                    terminalId = binding.terminalId,
                    storeId = binding.storeId,
                    authMode = "PIN_LOCAL",
                    status = "ACTIVE",
                    createdAt = now,
                    updatedAt = now
                )
                kernelRepository.createAccessSession(session)
                kernelRepository.insertAudit(
                    id = IdGenerator.nextId("audit"),
                    message = "Login berhasil untuk ${operator.displayName}",
                    level = "INFO"
                )
                LoginResult.Success(
                    session = session,
                    operator = operator.copy(failedAttempts = 0, lockedUntil = null, lastLoginAt = now)
                )
            }
            PinAuthResult.OperatorNotFound -> LoginResult.OperatorNotFound
            is PinAuthResult.WrongPin -> LoginResult.WrongPin(
                failedAttempts = authResult.failedAttempts,
                remainingBeforeLock = authResult.remainingBeforeLock
            )
            is PinAuthResult.Locked -> LoginResult.Locked(authResult.lockedUntil)
        }
    }

    suspend fun verifyStepUp(
        operatorId: String,
        pin: String,
        capability: AccessCapability
    ): Result<OperatorAccount> {
        val binding = kernelRepository.getTerminalBinding()
            ?: return Result.failure(IllegalStateException("Terminal belum terikat ke store"))
        return when (val authResult = authenticateOperator(operatorId, pin)) {
            is PinAuthResult.Success -> {
                val operator = authResult.operator
                if (!operator.role.supports(capability)) {
                    Result.failure(IllegalStateException("Akses ${operator.role.name} tidak diizinkan untuk $capability"))
                } else {
                    kernelRepository.insertAudit(
                        id = IdGenerator.nextId("audit"),
                        message = "Step-up auth berhasil untuk ${operator.displayName} pada terminal ${binding.terminalName}",
                        level = "INFO"
                    )
                    Result.success(operator)
                }
            }
            PinAuthResult.OperatorNotFound -> Result.failure(IllegalStateException("Operator step-up tidak ditemukan"))
            is PinAuthResult.WrongPin -> Result.failure(
                IllegalStateException("PIN salah. Sisa percobaan sebelum lock: ${authResult.remainingBeforeLock}")
            )
            is PinAuthResult.Locked -> Result.failure(
                IllegalStateException("Akses operator terkunci sampai ${authResult.lockedUntil ?: "-"}")
            )
        }
    }

    private suspend fun authenticateOperator(operatorId: String, pin: String): PinAuthResult {
        val operator = kernelRepository.getOperatorById(operatorId) ?: return PinAuthResult.OperatorNotFound
        val now = clock.now()
        if (operator.lockedUntil?.let { it > now } == true) {
            return PinAuthResult.Locked(operator.lockedUntil)
        }

        val hash = pinHasher.hash(pin, operator.pinSalt)
        if (hash != operator.pinHash) {
            val failedAttempts = operator.failedAttempts + 1
            val lockedUntil = if (failedAttempts >= MAX_FAILED_ATTEMPTS) {
                Instant.fromEpochMilliseconds(now.toEpochMilliseconds() + LOCKOUT_DURATION_MS)
            } else {
                null
            }

            kernelRepository.updateOperatorAccessState(
                operatorId = operator.id,
                failedAttempts = if (lockedUntil != null) 0 else failedAttempts,
                lockedUntil = lockedUntil,
                lastLoginAt = operator.lastLoginAt
            )
            kernelRepository.insertAudit(
                id = IdGenerator.nextId("audit"),
                message = "PIN salah untuk ${operator.displayName}",
                level = "WARN"
            )
            return if (lockedUntil != null) {
                PinAuthResult.Locked(lockedUntil)
            } else {
                PinAuthResult.WrongPin(
                    failedAttempts = failedAttempts,
                    remainingBeforeLock = MAX_FAILED_ATTEMPTS - failedAttempts
                )
            }
        }

        return PinAuthResult.Success(
            operator = operator,
            at = now
        )
    }

    suspend fun logout() {
        kernelRepository.clearActiveAccessSessions()
        kernelRepository.insertAudit(
            id = IdGenerator.nextId("audit"),
            message = "Session terminal diakhiri",
            level = "INFO"
        )
    }

    suspend fun requireCapability(capability: AccessCapability): Result<OperatorAccount> {
        val session = kernelRepository.getActiveAccessSession()
            ?: return Result.failure(IllegalStateException("Akses belum aktif"))
        val operator = kernelRepository.getOperatorById(session.operatorId)
            ?: return Result.failure(IllegalStateException("Operator aktif tidak ditemukan"))
        if (!operator.role.supports(capability)) {
            return Result.failure(IllegalStateException("Akses ${operator.role.name} tidak diizinkan untuk $capability"))
        }
        return Result.success(operator)
    }

    private suspend fun seedOperator(
        employeeCode: String,
        displayName: String,
        role: OperatorRole,
        pin: String,
        avatarPath: String?
    ) {
        val salt = IdGenerator.nextId("salt")
        kernelRepository.upsertOperator(
            OperatorAccount(
                id = IdGenerator.nextId("operator"),
                employeeCode = employeeCode,
                displayName = displayName,
                role = role,
                avatarPath = avatarPath,
                pinHash = pinHasher.hash(pin, salt),
                pinSalt = salt,
                failedAttempts = 0,
                lockedUntil = null,
                isActive = true,
                lastLoginAt = null
            )
        )
    }

    private fun validatePin(pin: String): String? {
        return when {
            pin.length != 6 -> "PIN harus 6 digit"
            pin.any { !it.isDigit() } -> "PIN harus numerik"
            else -> null
        }
    }

    private fun validateName(
        value: String,
        field: BootstrapStoreField,
        emptyMessage: String,
        maxLength: Int
    ): List<BootstrapStoreFieldIssue> {
        return buildList {
            when {
                value.isBlank() -> add(BootstrapStoreFieldIssue(field, emptyMessage))
                value.length > maxLength -> add(BootstrapStoreFieldIssue(field, "Maksimal $maxLength karakter"))
            }
        }
    }

    private fun normalizeSingleLine(value: String): String {
        return value.trim()
            .replace(WHITESPACE_REGEX, " ")
    }

    private companion object {
        val WHITESPACE_REGEX = Regex("\\s+")
        const val MAX_STORE_NAME_LENGTH = 120
        const val MAX_TERMINAL_NAME_LENGTH = 40
        const val MAX_OPERATOR_NAME_LENGTH = 80
        const val MAX_FAILED_ATTEMPTS = 3
        const val LOCKOUT_DURATION_MS = 5 * 60 * 1000L
    }
}

private sealed interface PinAuthResult {
    data class Success(
        val operator: OperatorAccount,
        val at: Instant
    ) : PinAuthResult

    data class WrongPin(
        val failedAttempts: Int,
        val remainingBeforeLock: Int
    ) : PinAuthResult

    data class Locked(
        val lockedUntil: Instant?
    ) : PinAuthResult

    data object OperatorNotFound : PinAuthResult
}
