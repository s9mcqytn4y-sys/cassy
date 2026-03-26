package id.azureenterprise.cassy.kernel.domain

import kotlinx.datetime.Instant

enum class SyncLevel {
    HEALTHY,    // All caught up
    PENDING,    // Some events waiting (normal)
    DELAYED,    // Oldest event > 5 mins
    STALLED,    // Oldest event > 1 hour
    ERROR       // Explicit sync failure
}

data class SyncStatus(
    val level: SyncLevel,
    val pendingCount: Int,
    val failedCount: Int,
    val oldestPendingAt: Instant?,
    val lastSyncAt: Instant?,
    val message: String? = null,
    val lastErrorMessage: String? = null
)

enum class IssueSeverity {
    INFO,       // Normal pending state
    WARNING,    // Discrepancy or delay that needs attention
    CRITICAL    // Blocked operation (e.g. cannot close day/shift)
}

/**
 * R5 Hardened Taxonomy for Operational Issues.
 */
enum class OperationalIssueType {
    SYNC_STATUS,
    PENDING_APPROVAL,
    OPERATIONAL_BLOCKER,
    DISCREPANCY,
    HARDWARE_UNAVAILABLE,
    STATE_UNAVAILABLE,
    PENDING_TRANSACTION,
    OPEN_WORK_UNIT // e.g. Open shift when closing day
}

data class OperationalIssue(
    val type: OperationalIssueType,
    val severity: IssueSeverity,
    val label: String,
    val description: String,
    val sourceId: String? = null,
    val actor: String? = null,
    val timestamp: Instant? = null,
    val reasonCode: String? = null,
    val status: String? = null
)

data class DailySummary(
    val businessDayId: String,
    val dateLabel: String, // e.g. "2026-03-19"
    val status: String, // OPEN, CLOSED
    val openedAt: Instant,
    val closedAt: Instant?,
    val totalSales: Double,
    val transactionCount: Int,
    val cashSalesTotal: Double,
    val nonCashSalesTotal: Double,
    val netCashMovement: Double, // cashIn - cashOut - safeDrop
    val shiftCount: Int,
    val openShiftCount: Int,
    val pendingApprovalCount: Int,
    val syncStatus: SyncStatus,
    val issues: List<OperationalIssue>,
    val hasOperationalIssues: Boolean
)

data class ShiftSummary(
    val shiftId: String,
    val businessDayId: String,
    val status: String, // OPEN, CLOSED
    val openedAt: Instant,
    val closedAt: Instant?,
    val operatorName: String,
    val openingCash: Double,
    val closingCash: Double?,
    val expectedCash: Double,
    val variance: Double?,
    val salesTotal: Double,
    val cashSalesTotal: Double,
    val nonCashSalesTotal: Double,
    val cashInTotal: Double,
    val cashOutTotal: Double,
    val safeDropTotal: Double,
    val pendingTransactionCount: Int,
    val issues: List<OperationalIssue>,
    val hasUnresolvedIssues: Boolean
)
