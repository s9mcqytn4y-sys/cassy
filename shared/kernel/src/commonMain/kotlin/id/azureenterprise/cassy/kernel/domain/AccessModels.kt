package id.azureenterprise.cassy.kernel.domain

import kotlinx.datetime.Instant

enum class OperatorRole {
    CASHIER,
    SUPERVISOR,
    OWNER
}

enum class AccessCapability {
    OPEN_DAY,
    CLOSE_DAY,
    START_SHIFT,
    END_SHIFT,
    RECORD_CASH_MOVEMENT,
    RECORD_STOCK_COUNT,
    APPLY_STOCK_ADJUSTMENT,
    APPROVE_STOCK_ADJUSTMENT,
    APPROVE_OPENING_CASH_EXCEPTION,
    APPROVE_CASH_MOVEMENT_EXCEPTION,
    APPROVE_SHIFT_CLOSE_EXCEPTION,
    ACCESS_CATALOG
}

data class TerminalBinding(
    val storeId: String,
    val storeName: String,
    val terminalId: String,
    val terminalName: String,
    val boundAt: Instant
)

data class OperatorAccount(
    val id: String,
    val employeeCode: String,
    val displayName: String,
    val role: OperatorRole,
    val pinHash: String,
    val pinSalt: String,
    val failedAttempts: Int,
    val lockedUntil: Instant?,
    val isActive: Boolean,
    val lastLoginAt: Instant?
)

data class AccessSession(
    val id: String,
    val operatorId: String,
    val terminalId: String,
    val storeId: String,
    val authMode: String,
    val status: String,
    val createdAt: Instant,
    val updatedAt: Instant
)

data class AccessContext(
    val terminalBinding: TerminalBinding?,
    val operators: List<OperatorAccount>,
    val activeSession: AccessSession?,
    val activeOperator: OperatorAccount?
)

data class BootstrapStoreRequest(
    val storeName: String,
    val terminalName: String,
    val cashierName: String,
    val cashierPin: String,
    val supervisorName: String,
    val supervisorPin: String
)

sealed interface LoginResult {
    data class Success(
        val session: AccessSession,
        val operator: OperatorAccount
    ) : LoginResult

    data class WrongPin(
        val failedAttempts: Int,
        val remainingBeforeLock: Int
    ) : LoginResult

    data class Locked(
        val lockedUntil: Instant?
    ) : LoginResult

    data object OperatorNotFound : LoginResult

    data object NotBound : LoginResult
}

fun OperatorRole.supports(capability: AccessCapability): Boolean = when (this) {
    OperatorRole.CASHIER -> capability in setOf(
        AccessCapability.START_SHIFT,
        AccessCapability.END_SHIFT,
        AccessCapability.RECORD_CASH_MOVEMENT,
        AccessCapability.RECORD_STOCK_COUNT,
        AccessCapability.ACCESS_CATALOG
    )
    OperatorRole.SUPERVISOR,
    OperatorRole.OWNER -> true
}
