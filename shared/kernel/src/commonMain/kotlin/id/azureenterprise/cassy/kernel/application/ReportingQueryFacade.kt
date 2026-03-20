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
     * R5-R01 (Timezone) Hardening:
     * Uses explicit TimeZone for date label generation.
     */
    suspend fun getDailySummary(businessDayId: String): DailySummary? {
        val businessDay = kernelRepository.getBusinessDayById(businessDayId) ?: return null

        val localDateTime = businessDay.openedAt.toLocalDateTime(timeZone)
        val dateLabel = "${localDateTime.year}-${localDateTime.monthNumber.toString().padStart(2, '0')}-${localDateTime.dayOfMonth.toString().padStart(2, '0')}"

        val openShiftsCount = kernelRepository.countOpenShiftsByBusinessDay(businessDayId)
        val pendingApprovals = kernelRepository.countPendingApprovalRequestsByBusinessDay(businessDayId)

        // Note: totalSales and transactionCount implementation will be completed in Block 2 implementation
        // after OperationalSalesPort is fully mapped in the sales module.

        return DailySummary(
            businessDayId = businessDayId,
            dateLabel = dateLabel,
            openedAt = businessDay.openedAt,
            closedAt = businessDay.closedAt,
            totalSales = 0.0,
            transactionCount = 0,
            netCashMovement = 0.0,
            syncStatus = getSyncStatus(),
            hasOperationalIssues = openShiftsCount > 0 || pendingApprovals > 0
        )
    }

    suspend fun getShiftSummary(shiftId: String): ShiftSummary? {
        val shift = kernelRepository.getShiftById(shiftId) ?: return null
        val salesSummary = salesPort.getShiftSalesSummary(shiftId)
        val movementTotals = kernelRepository.getCashMovementTotalsByShift(shiftId)

        val expectedCash = shift.openingCash +
            movementTotals.cashInTotal +
            salesSummary.completedCashSalesTotal -
            movementTotals.cashOutTotal -
            movementTotals.safeDropTotal

        return ShiftSummary(
            shiftId = shiftId,
            openedAt = shift.openedAt,
            closedAt = shift.closedAt,
            operatorName = shift.openedBy,
            expectedCash = expectedCash,
            actualCash = shift.closingCash,
            variance = shift.closingCash?.minus(expectedCash),
            salesTotal = salesSummary.completedCashSalesTotal + salesSummary.completedNonCashSalesTotal,
            status = shift.status
        )
    }
}
