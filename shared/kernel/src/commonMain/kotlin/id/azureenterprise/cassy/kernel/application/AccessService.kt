package id.azureenterprise.cassy.kernel.application

import id.azureenterprise.cassy.kernel.data.KernelRepository
import id.azureenterprise.cassy.kernel.domain.AccessContext
import id.azureenterprise.cassy.kernel.domain.AccessSession
import id.azureenterprise.cassy.kernel.domain.BootstrapStoreRequest
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
    suspend fun restoreContext(): AccessContext {
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
        if (request.storeName.isBlank()) return Result.failure(IllegalArgumentException("Nama toko wajib diisi"))
        if (request.terminalName.isBlank()) return Result.failure(IllegalArgumentException("Nama terminal wajib diisi"))
        if (request.cashierName.isBlank()) return Result.failure(IllegalArgumentException("Nama kasir wajib diisi"))
        if (request.supervisorName.isBlank()) return Result.failure(IllegalArgumentException("Nama supervisor wajib diisi"))

        validatePin(request.cashierPin)?.let { return Result.failure(IllegalArgumentException(it)) }
        validatePin(request.supervisorPin)?.let { return Result.failure(IllegalArgumentException(it)) }

        if (!needsBootstrap()) {
            return Result.failure(IllegalStateException("Store sudah di-bootstrap"))
        }

        val boundAt = clock.now()
        val binding = TerminalBinding(
            storeId = IdGenerator.nextId("store"),
            storeName = request.storeName.trim(),
            terminalId = IdGenerator.nextId("terminal"),
            terminalName = request.terminalName.trim(),
            boundAt = boundAt
        )
        kernelRepository.upsertTerminalBinding(binding)

        seedOperator(
            employeeCode = "cashier",
            displayName = request.cashierName.trim(),
            role = OperatorRole.CASHIER,
            pin = request.cashierPin
        )
        seedOperator(
            employeeCode = "supervisor",
            displayName = request.supervisorName.trim(),
            role = OperatorRole.SUPERVISOR,
            pin = request.supervisorPin
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
        val operator = kernelRepository.getOperatorById(operatorId) ?: return LoginResult.OperatorNotFound
        val now = clock.now()
        if (operator.lockedUntil?.let { it > now } == true) {
            return LoginResult.Locked(operator.lockedUntil)
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
                LoginResult.Locked(lockedUntil)
            } else {
                LoginResult.WrongPin(
                    failedAttempts = failedAttempts,
                    remainingBeforeLock = MAX_FAILED_ATTEMPTS - failedAttempts
                )
            }
        }

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
        return LoginResult.Success(
            session = session,
            operator = operator.copy(failedAttempts = 0, lockedUntil = null, lastLoginAt = now)
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
        pin: String
    ) {
        val salt = IdGenerator.nextId("salt")
        kernelRepository.upsertOperator(
            OperatorAccount(
                id = IdGenerator.nextId("operator"),
                employeeCode = employeeCode,
                displayName = displayName,
                role = role,
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

    private companion object {
        const val MAX_FAILED_ATTEMPTS = 3
        const val LOCKOUT_DURATION_MS = 5 * 60 * 1000L
    }
}
