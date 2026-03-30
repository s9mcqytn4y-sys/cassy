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
    CASH_IN,
    CASH_OUT,
    SAFE_DROP,
    CLOSE_SHIFT,
    CLOSE_BUSINESS_DAY,
    VOID_SALE,
    STOCK_ADJUSTMENT,
    RESOLVE_STOCK_DISCREPANCY
}

enum class OperationBlockerCode {
    TERMINAL_NOT_BOUND,
    ACCESS_NOT_ACTIVE,
    CAPABILITY_DENIED,
    BUSINESS_DAY_REQUIRED,
    BUSINESS_DAY_NOT_ACTIVE,
    SHIFT_REQUIRED,
    SHIFT_ALREADY_ACTIVE,
    INVALID_OPENING_CASH,
    INVALID_CLOSING_CASH,
    INVALID_CASH_MOVEMENT_AMOUNT,
    OPENING_CASH_OUT_OF_POLICY,
    REASON_REQUIRED,
    REASON_CODE_REQUIRED,
    APPROVAL_PENDING,
    PENDING_TRANSACTION_PRESENT,
    SHIFT_VARIANCE_OUT_OF_TOLERANCE,
    OPEN_SHIFT_EXISTS,
    PENDING_APPROVAL_EXISTS,
    VOID_NOT_READY,
    VOID_PAYMENT_METHOD_UNSUPPORTED
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
    val pendingApprovalCount: Int,
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

enum class ReasonCategory {
    OPENING_CASH_EXCEPTION,
    CASH_IN,
    CASH_OUT,
    SAFE_DROP,
    SHIFT_CLOSE_VARIANCE,
    INVENTORY_ADJUSTMENT,
    VOID_SALE
}

enum class ApprovalStatus {
    REQUESTED,
    APPROVED,
    DENIED
}

enum class CashMovementType {
    CASH_IN,
    CASH_OUT,
    SAFE_DROP
}

data class ReasonCode(
    val code: String,
    val category: ReasonCategory,
    val title: String,
    val requiresApproval: Boolean,
    val isActive: Boolean,
    val sortOrder: Int
)

data class ApprovalRequest(
    val id: String,
    val operationType: OperationType,
    val entityId: String,
    val businessDayId: String,
    val shiftId: String?,
    val terminalId: String,
    val amount: Double?,
    val reasonCode: String,
    val reasonDetail: String?,
    val requestedBy: String,
    val approvedBy: String?,
    val status: ApprovalStatus,
    val requestedAtEpochMs: Long,
    val decidedAtEpochMs: Long?,
    val decisionNote: String?
)

data class CashMovement(
    val id: String,
    val businessDayId: String,
    val shiftId: String,
    val terminalId: String,
    val type: CashMovementType,
    val amount: Double,
    val reasonCode: String,
    val reasonDetail: String?,
    val approvalRequestId: String?,
    val performedBy: String,
    val createdAtEpochMs: Long
)

data class CashMovementTotals(
    val cashInTotal: Double = 0.0,
    val cashOutTotal: Double = 0.0,
    val safeDropTotal: Double = 0.0
)

data class CashMovementPolicy(
    val maxCashierCashIn: Double = 500_000.0,
    val maxCashierCashOut: Double = 500_000.0,
    val maxCashierSafeDrop: Double = 1_000_000.0,
    val hardLimitAmount: Double = 10_000_000.0
)

data class ShiftClosePolicy(
    val varianceTolerance: Double = 20_000.0,
    val approvalThresholdVariance: Double = 100_000.0,
    val hardLimitVariance: Double = 500_000.0
)

data class ShiftCloseReview(
    val shiftId: String,
    val businessDayId: String,
    val terminalId: String,
    val openingCash: Double,
    val cashSalesTotal: Double,
    val cashMovementTotals: CashMovementTotals,
    val expectedCash: Double,
    val actualCash: Double?,
    val variance: Double?,
    val pendingTransactions: List<PendingTransactionSummary>,
    val decision: OperationDecision
)

data class ShiftCloseReport(
    val id: String,
    val shiftId: String,
    val businessDayId: String,
    val terminalId: String,
    val openingCash: Double,
    val cashSalesTotal: Double,
    val cashInTotal: Double,
    val cashOutTotal: Double,
    val safeDropTotal: Double,
    val expectedCash: Double,
    val actualCash: Double,
    val variance: Double,
    val pendingTransactionCount: Int,
    val approvalRequestId: String?,
    val generatedBy: String,
    val generatedAtEpochMs: Long
)

sealed interface CashMovementExecutionResult {
    data class Recorded(
        val movement: CashMovement,
        val approvalApplied: Boolean
    ) : CashMovementExecutionResult

    data class ApprovalRequired(
        val decision: OperationDecision,
        val approvalRequestId: String
    ) : CashMovementExecutionResult

    data class Blocked(
        val decision: OperationDecision
    ) : CashMovementExecutionResult
}

sealed interface ShiftCloseExecutionResult {
    data class Closed(
        val shift: Shift,
        val report: ShiftCloseReport,
        val approvalApplied: Boolean
    ) : ShiftCloseExecutionResult

    data class ApprovalRequired(
        val decision: OperationDecision,
        val approvalRequestId: String
    ) : ShiftCloseExecutionResult

    data class Blocked(
        val decision: OperationDecision
    ) : ShiftCloseExecutionResult
}

data class PendingApprovalSummary(
    val id: String,
    val operationType: OperationType,
    val title: String,
    val detail: String,
    val amount: Double?
)

data class PendingTransactionSummary(
    val saleId: String,
    val localNumber: String,
    val amount: Double
)

data class ShiftSalesSummary(
    val completedCashSalesTotal: Double = 0.0,
    val completedNonCashSalesTotal: Double = 0.0,
    val completedSaleCount: Int = 0,
    val voidedSaleCount: Int = 0,
    val voidedSalesTotal: Double = 0.0,
    val pendingTransactions: List<PendingTransactionSummary> = emptyList()
)

data class VoidSalesSummary(
    val count: Int = 0,
    val totalAmount: Double = 0.0,
    val latestVoidedAtEpochMs: Long? = null
)
