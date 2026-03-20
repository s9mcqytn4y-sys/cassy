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
    val oldestPendingAt: Instant?,
    val lastSyncAt: Instant?,
    val message: String? = null
)

data class DailySummary(
    val businessDayId: String,
    val dateLabel: String, // e.g. "2026-03-19"
    val openedAt: Instant,
    val closedAt: Instant?,
    val totalSales: Double,
    val transactionCount: Int,
    val netCashMovement: Double,
    val syncStatus: SyncStatus,
    val hasOperationalIssues: Boolean
)

data class ShiftSummary(
    val shiftId: String,
    val openedAt: Instant,
    val closedAt: Instant?,
    val operatorName: String,
    val expectedCash: Double,
    val actualCash: Double?,
    val variance: Double?,
    val salesTotal: Double,
    val status: String
)
