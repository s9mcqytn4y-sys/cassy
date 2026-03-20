package id.azureenterprise.cassy.kernel.application

import id.azureenterprise.cassy.kernel.data.KernelRepository
import id.azureenterprise.cassy.kernel.data.OutboxRepository
import id.azureenterprise.cassy.kernel.domain.DailySummary
import id.azureenterprise.cassy.kernel.domain.ShiftSummary
import id.azureenterprise.cassy.kernel.domain.SyncLevel
import id.azureenterprise.cassy.kernel.domain.SyncStatus
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.hours

/**
 * ReportingQueryFacade (R5 Hardened Foundation)
 *
 * Provides a truthful, explicit boundary for reporting data.
 * Does not own SQL directly; delegates to KernelRepository and OperationalSalesPort.
 * Hardens date boundaries and unavailable metrics.
 */
class ReportingQueryFacade(
    private val kernelRepository: KernelRepository,
    private val outboxRepository: OutboxRepository,
    private val salesPort: OperationalSalesPort,
    private val clock: Clock,
    private val timeZone: TimeZone = TimeZone.currentSystemDefault()
) {
    private companion object {
        const val LAST_SYNC_SUCCESS_KEY = "sync.last_success_timestamp"
    }

    /**
     * R5-R02 (Sync Visibility) Hardening:
     * Derives visibility from Outbox (pending) and Metadata (last success).
     */
    suspend fun getSyncStatus(): SyncStatus {
        val pendingEvents = outboxRepository.getPendingEvents()
        val now = clock.now()

        val lastSyncTs = kernelRepository.getMetadata(LAST_SYNC_SUCCESS_KEY)?.toLongOrNull()
        val lastSyncAt = lastSyncTs?.let { Instant.fromEpochMilliseconds(it) }

        if (pendingEvents.isEmpty()) {
            return SyncStatus(
                level = SyncLevel.HEALTHY,
                pendingCount = 0,
                oldestPendingAt = null,
                lastSyncAt = lastSyncAt
            )
        }

        val oldestTimestamp = pendingEvents.minOf { it.timestamp }
        val oldestInstant = Instant.fromEpochMilliseconds(oldestTimestamp)
        val age = now - oldestInstant

        val level = when {
            age > 1.hours -> SyncLevel.STALLED
            age > 5.minutes -> SyncLevel.DELAYED
            else -> SyncLevel.PENDING
        }

        return SyncStatus(
            level = level,
            pendingCount = pendingEvents.size,
            oldestPendingAt = oldestInstant,
            lastSyncAt = lastSyncAt,
            message = if (level == SyncLevel.STALLED) "Sync tertunda lebih dari 1 jam" else null
        )
    }

    /**
     * R5-R01 (Reporting Foundation):
     * Derives daily summary by aggregating shifts and using sales source-of-truth.
     */
    suspend fun getDailySummary(businessDayId: String): DailySummary? {
        val businessDay = kernelRepository.getBusinessDayById(businessDayId) ?: return null

        val localDateTime = businessDay.openedAt.toLocalDateTime(timeZone)
        val dateLabel = "${localDateTime.year}-${localDateTime.monthNumber.toString().padStart(2, '0')}-${localDateTime.dayOfMonth.toString().padStart(2, '0')}"

        val shifts = kernelRepository.listShiftsByBusinessDay(businessDayId)
        val shiftIds = shifts.map { it.id }

        val salesSummary = salesPort.getMultiShiftSalesSummary(shiftIds)
        val cashTotals = kernelRepository.getCashMovementTotalsByMultiShift(shiftIds)

        val pendingApprovals = kernelRepository.countPendingApprovalRequestsByBusinessDay(businessDayId)
        val openShiftsCount = shifts.count { it.status == "OPEN" }

        val netCashMovement = cashTotals.cashInTotal - cashTotals.cashOutTotal - cashTotals.safeDropTotal

        return DailySummary(
            businessDayId = businessDayId,
            dateLabel = dateLabel,
            status = businessDay.status,
            openedAt = businessDay.openedAt,
            closedAt = businessDay.closedAt,
            totalSales = salesSummary.completedCashSalesTotal + salesSummary.completedNonCashSalesTotal,
            transactionCount = salesSummary.completedSaleCount,
            cashSalesTotal = salesSummary.completedCashSalesTotal,
            nonCashSalesTotal = salesSummary.completedNonCashSalesTotal,
            netCashMovement = netCashMovement,
            shiftCount = shifts.size,
            openShiftCount = openShiftsCount,
            pendingApprovalCount = pendingApprovals.toInt(),
            syncStatus = getSyncStatus(),
            hasOperationalIssues = openShiftsCount > 0 || pendingApprovals > 0
        )
    }

    /**
     * R5-R01 (Reporting Foundation):
     * Derives shift summary with explicit variance and issue signals.
     */
    suspend fun getShiftSummary(shiftId: String): ShiftSummary? {
        val shift = kernelRepository.getShiftById(shiftId) ?: return null
        val salesSummary = salesPort.getShiftSalesSummary(shiftId)
        val cashTotals = kernelRepository.getCashMovementTotalsByShift(shiftId)

        val expectedCash = shift.openingCash +
            cashTotals.cashInTotal +
            salesSummary.completedCashSalesTotal -
            cashTotals.cashOutTotal -
            cashTotals.safeDropTotal

        return ShiftSummary(
            shiftId = shiftId,
            businessDayId = shift.businessDayId,
            status = shift.status,
            openedAt = shift.openedAt,
            closedAt = shift.closedAt,
            operatorName = shift.openedBy,
            openingCash = shift.openingCash,
            closingCash = shift.closingCash,
            expectedCash = expectedCash,
            variance = shift.closingCash?.minus(expectedCash),
            salesTotal = salesSummary.completedCashSalesTotal + salesSummary.completedNonCashSalesTotal,
            cashSalesTotal = salesSummary.completedCashSalesTotal,
            nonCashSalesTotal = salesSummary.completedNonCashSalesTotal,
            cashInTotal = cashTotals.cashInTotal,
            cashOutTotal = cashTotals.cashOutTotal,
            safeDropTotal = cashTotals.safeDropTotal,
            pendingTransactionCount = salesSummary.pendingTransactions.size,
            hasUnresolvedIssues = salesSummary.pendingTransactions.isNotEmpty() || shift.status == "OPEN"
        )
    }
}
