package id.azureenterprise.cassy.kernel.data

import id.azureenterprise.cassy.kernel.db.KernelDatabase
import id.azureenterprise.cassy.kernel.domain.AccessSession
import id.azureenterprise.cassy.kernel.domain.Shift
import id.azureenterprise.cassy.kernel.domain.OperatorAccount
import id.azureenterprise.cassy.kernel.domain.OperatorRole
import id.azureenterprise.cassy.kernel.domain.TerminalBinding
import id.azureenterprise.cassy.kernel.domain.BusinessDay
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

open class KernelRepository(
    private val database: KernelDatabase?,
    private val ioDispatcher: CoroutineContext,
    private val clock: Clock
) {
    private val queries = database?.kernelDatabaseQueries

    open suspend fun isBusinessDayOpen(): Boolean = withContext(ioDispatcher) {
        queries?.getActiveBusinessDay()?.executeAsOneOrNull() != null
    }

    open suspend fun getTerminalBinding(): TerminalBinding? = withContext(ioDispatcher) {
        queries?.getTerminalBinding()?.executeAsOneOrNull()?.let {
            TerminalBinding(
                storeId = it.storeId,
                storeName = it.storeName,
                terminalId = it.terminalId,
                terminalName = it.terminalName,
                boundAt = Instant.fromEpochMilliseconds(it.boundAt)
            )
        }
    }

    open suspend fun upsertTerminalBinding(binding: TerminalBinding) {
        withContext(ioDispatcher) {
            queries?.upsertTerminalBinding(
                binding.terminalId,
                binding.storeId,
                binding.storeName,
                binding.terminalName,
                binding.boundAt.toEpochMilliseconds()
            )
        }
    }

    open suspend fun listActiveOperators(): List<OperatorAccount> = withContext(ioDispatcher) {
        queries?.listActiveOperators()?.executeAsList()?.map { record ->
            OperatorAccount(
                id = record.id,
                employeeCode = record.employeeCode,
                displayName = record.displayName,
                role = OperatorRole.valueOf(record.role),
                pinHash = record.pinHash,
                pinSalt = record.pinSalt,
                failedAttempts = record.failedAttempts.toInt(),
                lockedUntil = record.lockedUntil?.let(Instant::fromEpochMilliseconds),
                isActive = record.isActive,
                lastLoginAt = record.lastLoginAt?.let(Instant::fromEpochMilliseconds)
            )
        } ?: emptyList()
    }

    open suspend fun getOperatorById(id: String): OperatorAccount? = withContext(ioDispatcher) {
        queries?.selectOperatorById(id)?.executeAsOneOrNull()?.let { record ->
            OperatorAccount(
                id = record.id,
                employeeCode = record.employeeCode,
                displayName = record.displayName,
                role = OperatorRole.valueOf(record.role),
                pinHash = record.pinHash,
                pinSalt = record.pinSalt,
                failedAttempts = record.failedAttempts.toInt(),
                lockedUntil = record.lockedUntil?.let(Instant::fromEpochMilliseconds),
                isActive = record.isActive,
                lastLoginAt = record.lastLoginAt?.let(Instant::fromEpochMilliseconds)
            )
        }
    }

    open suspend fun upsertOperator(operator: OperatorAccount) {
        withContext(ioDispatcher) {
            queries?.insertOperator(
                operator.id,
                operator.employeeCode,
                operator.displayName,
                operator.role.name,
                operator.pinHash,
                operator.pinSalt,
                operator.failedAttempts.toLong(),
                operator.lockedUntil?.toEpochMilliseconds(),
                operator.isActive,
                operator.lastLoginAt?.toEpochMilliseconds()
            )
        }
    }

    open suspend fun updateOperatorAccessState(
        operatorId: String,
        failedAttempts: Int,
        lockedUntil: Instant?,
        lastLoginAt: Instant?
    ) {
        withContext(ioDispatcher) {
            queries?.updateOperatorAccessState(
                failedAttempts.toLong(),
                lockedUntil?.toEpochMilliseconds(),
                lastLoginAt?.toEpochMilliseconds(),
                operatorId
            )
        }
    }

    open suspend fun getActiveAccessSession(): AccessSession? = withContext(ioDispatcher) {
        queries?.getActiveAccessSession()?.executeAsOneOrNull()?.let { record ->
            AccessSession(
                id = record.id,
                operatorId = record.operatorId,
                terminalId = record.terminalId,
                storeId = record.storeId,
                authMode = record.authMode,
                status = record.status,
                createdAt = Instant.fromEpochMilliseconds(record.createdAt),
                updatedAt = Instant.fromEpochMilliseconds(record.updatedAt)
            )
        }
    }

    open suspend fun createAccessSession(session: AccessSession) {
        withContext(ioDispatcher) {
            queries?.insertAccessSession(
                session.id,
                session.operatorId,
                session.terminalId,
                session.storeId,
                session.authMode,
                session.status,
                session.createdAt.toEpochMilliseconds(),
                session.updatedAt.toEpochMilliseconds()
            )
        }
    }

    open suspend fun clearActiveAccessSessions() {
        withContext(ioDispatcher) {
            queries?.clearActiveAccessSessions("TERMINATED", clock.now().toEpochMilliseconds())
        }
    }

    open suspend fun getActiveBusinessDay(): BusinessDay? = withContext(ioDispatcher) {
        queries?.getActiveBusinessDay()?.executeAsOneOrNull()?.let { record ->
            BusinessDay(
                id = record.id,
                openedAt = Instant.fromEpochMilliseconds(record.openedAt),
                closedAt = record.closedAt?.let(Instant::fromEpochMilliseconds),
                status = record.status
            )
        }
    }

    open suspend fun openBusinessDay(id: String): BusinessDay = withContext(ioDispatcher) {
        queries?.insertBusinessDay(id, clock.now().toEpochMilliseconds(), "OPEN")
        queries?.getBusinessDayById(id)?.executeAsOne()
            ?.let { record ->
                BusinessDay(
                    id = record.id,
                    openedAt = Instant.fromEpochMilliseconds(record.openedAt),
                    closedAt = record.closedAt?.let(Instant::fromEpochMilliseconds),
                    status = record.status
                )
            } ?: error("Failed to open day")
    }

    open suspend fun closeBusinessDay(id: String): BusinessDay = withContext(ioDispatcher) {
        val closedAt = clock.now().toEpochMilliseconds()
        queries?.closeBusinessDay(closedAt, id)
        queries?.getBusinessDayById(id)?.executeAsOne()?.let { record ->
            BusinessDay(
                id = record.id,
                openedAt = Instant.fromEpochMilliseconds(record.openedAt),
                closedAt = record.closedAt?.let(Instant::fromEpochMilliseconds),
                status = record.status
            )
        } ?: error("Failed to close day")
    }

    open suspend fun getActiveShift(terminalId: String): Shift? = withContext(ioDispatcher) {
        queries?.getActiveShift(terminalId)?.executeAsOneOrNull()?.let { record ->
            Shift(
                id = record.id,
                businessDayId = record.businessDayId,
                terminalId = record.terminalId,
                openedAt = Instant.fromEpochMilliseconds(record.openedAt),
                openingCash = record.openingCash,
                closedAt = record.closedAt?.let(Instant::fromEpochMilliseconds),
                closingCash = record.closingCash,
                openedBy = record.openedBy,
                closedBy = record.closedBy,
                status = record.status
            )
        }
    }

    open suspend fun openShift(
        id: String,
        businessDayId: String,
        terminalId: String,
        openingCash: Double,
        openedBy: String
    ): Shift = withContext(ioDispatcher) {
        val openedAt = clock.now().toEpochMilliseconds()
        queries?.insertShift(id, businessDayId, terminalId, openedAt, openingCash, openedBy, "OPEN")
        queries?.getShiftById(id)?.executeAsOne()?.let { record ->
            Shift(
                id = record.id,
                businessDayId = record.businessDayId,
                terminalId = record.terminalId,
                openedAt = Instant.fromEpochMilliseconds(record.openedAt),
                openingCash = record.openingCash,
                closedAt = record.closedAt?.let(Instant::fromEpochMilliseconds),
                closingCash = record.closingCash,
                openedBy = record.openedBy,
                closedBy = record.closedBy,
                status = record.status
            )
        } ?: error("Failed to open shift")
    }

    open suspend fun closeShift(id: String, closingCash: Double, closedBy: String): Shift = withContext(ioDispatcher) {
        val closedAt = clock.now().toEpochMilliseconds()
        queries?.closeShift(closedAt, closingCash, closedBy, id)
        queries?.getShiftById(id)?.executeAsOne()?.let { record ->
            Shift(
                id = record.id,
                businessDayId = record.businessDayId,
                terminalId = record.terminalId,
                openedAt = Instant.fromEpochMilliseconds(record.openedAt),
                openingCash = record.openingCash,
                closedAt = record.closedAt?.let(Instant::fromEpochMilliseconds),
                closingCash = record.closingCash,
                openedBy = record.openedBy,
                closedBy = record.closedBy,
                status = record.status
            )
        } ?: error("Failed to close shift")
    }

    open suspend fun insertAudit(id: String, message: String, level: String) {
        withContext(ioDispatcher) {
            queries?.insertAudit(id, clock.now().toEpochMilliseconds(), message, level)
        }
    }

    open suspend fun insertEvent(id: String, type: String, payload: String) {
        withContext(ioDispatcher) {
            queries?.insertEvent(id, clock.now().toEpochMilliseconds(), type, payload, "PENDING")
        }
    }
}
