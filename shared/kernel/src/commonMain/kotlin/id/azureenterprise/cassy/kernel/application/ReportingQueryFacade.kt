package id.azureenterprise.cassy.kernel.application

import id.azureenterprise.cassy.kernel.data.KernelRepository
import id.azureenterprise.cassy.kernel.data.OutboxRepository
import id.azureenterprise.cassy.kernel.domain.*
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
 * Aggregates operational issues from multiple sources (Outbox, Approvals, Shifts, Hardware).
 */
class ReportingQueryFacade(
    private val kernelRepository: KernelRepository,
    private val outboxRepository: OutboxRepository,
    private val salesPort: OperationalSalesPort,
    private val hardwarePort: OperationalHardwarePort,
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
     * Aggregates issues for a Business Day.
     */
    suspend fun getDailySummary(businessDayId: String): DailySummary? {
        val businessDay = kernelRepository.getBusinessDayById(businessDayId) ?: return null

        val localDateTime = businessDay.openedAt.toLocalDateTime(timeZone)
        val dateLabel = "${localDateTime.year}-${localDateTime.monthNumber.toString().padStart(2, '0')}-${localDateTime.dayOfMonth.toString().padStart(2, '0')}"

        val shifts = kernelRepository.listShiftsByBusinessDay(businessDayId)
        val shiftIds = shifts.map { it.id }

        val salesSummary = salesPort.getMultiShiftSalesSummary(shiftIds)
        val cashTotals = kernelRepository.getCashMovementTotalsByMultiShift(shiftIds)

        val pendingApprovals = kernelRepository.listPendingApprovalRequests().filter { it.businessDayId == businessDayId }
        val openShiftsCount = shifts.count { it.status == "OPEN" }

        val netCashMovement = cashTotals.cashInTotal - cashTotals.cashOutTotal - cashTotals.safeDropTotal

        val issues = mutableListOf<OperationalIssue>()

        // 1. Open Shifts (Critical for day close)
        if (openShiftsCount > 0) {
            issues.add(OperationalIssue(
                type = OperationalIssueType.OPEN_WORK_UNIT,
                severity = IssueSeverity.CRITICAL,
                label = "Shift Terbuka",
                description = "Ada $openShiftsCount shift yang belum ditutup. Tutup semua shift sebelum tutup hari.",
                status = "OPEN"
            ))
        }

        // 2. Pending Approvals (Warning/Critical depending on type)
        pendingApprovals.forEach { approval ->
            issues.add(OperationalIssue(
                type = OperationalIssueType.PENDING_APPROVAL,
                severity = IssueSeverity.WARNING,
                label = "Persetujuan Tertunda: ${approval.operationType.name}",
                description = "Permintaan oleh ${approval.requestedBy} memerlukan tindakan supervisor.",
                sourceId = approval.id,
                actor = approval.requestedBy,
                timestamp = Instant.fromEpochMilliseconds(approval.requestedAtEpochMs),
                reasonCode = approval.reasonCode,
                status = approval.status.name
            ))
        }

        // 3. Sync Status
        val syncStatus = getSyncStatus()
        if (syncStatus.level != SyncLevel.HEALTHY) {
            val severity = when(syncStatus.level) {
                SyncLevel.STALLED -> IssueSeverity.WARNING
                SyncLevel.ERROR -> IssueSeverity.CRITICAL
                else -> IssueSeverity.INFO
            }
            issues.add(OperationalIssue(
                type = OperationalIssueType.SYNC_STATUS,
                severity = severity,
                label = "Sinkronisasi ${syncStatus.level.name}",
                description = syncStatus.message ?: "Ada ${syncStatus.pendingCount} data yang belum tersinkronisasi.",
                timestamp = syncStatus.oldestPendingAt,
                status = syncStatus.level.name
            ))
        }

        // 4. Hardware Issues
        issues.addAll(hardwarePort.getHardwareIssues())

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
            pendingApprovalCount = pendingApprovals.size,
            syncStatus = syncStatus,
            issues = issues,
            hasOperationalIssues = issues.any { it.severity != IssueSeverity.INFO }
        )
    }

    /**
     * Aggregates issues for a specific Shift.
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

        val issues = mutableListOf<OperationalIssue>()

        // 1. Pending Transactions (Critical for shift close)
        if (salesSummary.pendingTransactions.isNotEmpty()) {
            issues.add(OperationalIssue(
                type = OperationalIssueType.PENDING_TRANSACTION,
                severity = IssueSeverity.CRITICAL,
                label = "Transaksi Tertunda",
                description = "Ada ${salesSummary.pendingTransactions.size} transaksi yang belum diselesaikan (e.g. ${salesSummary.pendingTransactions.first().localNumber}).",
                status = "PENDING"
            ))
        }

        // 2. Cash Variance (Warning)
        val variance = shift.closingCash?.minus(expectedCash)
        if (variance != null && kotlin.math.abs(variance) > 1.0) {
            issues.add(OperationalIssue(
                type = OperationalIssueType.DISCREPANCY,
                severity = IssueSeverity.WARNING,
                label = "Selisih Kas",
                description = "Ditemukan selisih kas sebesar Rp ${variance.toInt()}.",
                sourceId = shiftId,
                actor = shift.closedBy,
                timestamp = shift.closedAt,
                status = "COMPLETED_WITH_VARIANCE"
            ))
        }

        // 3. Ongoing state
        if (shift.status == "OPEN") {
            issues.add(OperationalIssue(
                type = OperationalIssueType.OPEN_WORK_UNIT,
                severity = IssueSeverity.INFO,
                label = "Shift Masih Aktif",
                description = "Shift ini sedang berjalan oleh ${shift.openedBy}.",
                actor = shift.openedBy,
                timestamp = shift.openedAt,
                status = "OPEN"
            ))
        }

        // 4. Hardware Issues
        issues.addAll(hardwarePort.getHardwareIssues())

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
            variance = variance,
            salesTotal = salesSummary.completedCashSalesTotal + salesSummary.completedNonCashSalesTotal,
            cashSalesTotal = salesSummary.completedCashSalesTotal,
            nonCashSalesTotal = salesSummary.completedNonCashSalesTotal,
            cashInTotal = cashTotals.cashInTotal,
            cashOutTotal = cashTotals.cashOutTotal,
            safeDropTotal = cashTotals.safeDropTotal,
            pendingTransactionCount = salesSummary.pendingTransactions.size,
            issues = issues,
            hasUnresolvedIssues = issues.any { it.severity != IssueSeverity.INFO } || shift.status == "OPEN"
        )
    }
}
