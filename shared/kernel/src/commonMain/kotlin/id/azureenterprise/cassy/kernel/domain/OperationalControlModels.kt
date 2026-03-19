package id.azureenterprise.cassy.kernel.domain

enum class OperationStatus {
    READY,
    BLOCKED,
    REQUIRES_APPROVAL,
    COMPLETED,
    UNAVAILABLE
}

enum class OperationType {
    OPEN_BUSINESS_DAY,
    START_SHIFT,
    VOID_SALE
}

enum class OperationBlockerCode {
    TERMINAL_NOT_BOUND,
    ACCESS_NOT_ACTIVE,
    CAPABILITY_DENIED,
    BUSINESS_DAY_REQUIRED,
    SHIFT_ALREADY_ACTIVE,
    INVALID_OPENING_CASH,
    OPENING_CASH_OUT_OF_POLICY,
    REASON_REQUIRED,
    VOID_NOT_READY
}

data class OperationDecision(
    val type: OperationType,
    val status: OperationStatus,
    val title: String,
    val message: String,
    val blockerCode: OperationBlockerCode? = null,
    val actionLabel: String? = null,
    val requiresReason: Boolean = false
)

data class OpeningCashPolicy(
    val maxCashierOpeningCash: Double = 500_000.0,
    val hardLimitOpeningCash: Double = 5_000_000.0
)

data class ShiftStartPolicyAssessment(
    val decision: OperationDecision,
    val requiresApproval: Boolean,
    val approvalWillBeApplied: Boolean
)

data class OperationalControlSnapshot(
    val headline: String,
    val primaryAction: OperationType?,
    val canAccessSalesHome: Boolean,
    val salesHomeBlocker: String?,
    val businessDayId: String?,
    val shiftId: String?,
    val decisions: List<OperationDecision>
)

sealed interface StartShiftExecutionResult {
    data class Started(
        val shift: Shift,
        val approvalApplied: Boolean
    ) : StartShiftExecutionResult

    data class ApprovalRequired(
        val decision: OperationDecision
    ) : StartShiftExecutionResult

    data class Blocked(
        val decision: OperationDecision
    ) : StartShiftExecutionResult
}
