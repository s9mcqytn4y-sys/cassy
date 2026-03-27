package id.azureenterprise.cassy.desktop

import id.azureenterprise.cassy.kernel.domain.DailySummary
import id.azureenterprise.cassy.kernel.domain.IssueSeverity
import id.azureenterprise.cassy.kernel.domain.OperationalIssue
import id.azureenterprise.cassy.kernel.domain.OperationalIssueType
import id.azureenterprise.cassy.kernel.domain.ShiftSummary
import id.azureenterprise.cassy.kernel.domain.SyncLevel
import id.azureenterprise.cassy.kernel.domain.SyncStatus
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import java.nio.file.Files
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.test.Test
import kotlin.test.assertTrue

class DesktopReportingExporterTest {

    @Test
    fun `export creates csv bundle and html overview`() {
        val exportRoot = Files.createTempDirectory("cassy-reporting-export")
        val exporter = DesktopReportingExporter(Clock.System) { exportRoot }
        val now = Instant.parse("2026-03-27T06:30:00Z")
        val dailySummary = sampleDailySummary(now)
        val shiftSummary = sampleShiftSummary(now)

        val result = exporter.export(
            OperationalReportBundle(
                shell = DesktopShellState(
                    storeName = "Toko Uji",
                    terminalName = "Kasir-01",
                    operatorName = "Supervisor"
                ),
                dailySummary = dailySummary,
                shiftSummary = shiftSummary,
                exportedBy = "Supervisor"
            )
        )

        assertTrue(result.exportDirectory.resolve("daily-summary.csv").exists())
        assertTrue(result.exportDirectory.resolve("shift-summary.csv").exists())
        assertTrue(result.exportDirectory.resolve("operational-issues.csv").exists())
        assertTrue(result.exportDirectory.resolve("README.html").exists())
        assertTrue(result.exportDirectory.resolve("README.html").readText().contains("Operational Export Cassy"))
    }

    private fun sampleDailySummary(now: Instant): DailySummary {
        return DailySummary(
            businessDayId = "day-1",
            dateLabel = "2026-03-27",
            status = "OPEN",
            openedAt = now,
            closedAt = null,
            totalSales = 250000.0,
            transactionCount = 12,
            cashSalesTotal = 200000.0,
            nonCashSalesTotal = 50000.0,
            netCashMovement = 150000.0,
            shiftCount = 1,
            openShiftCount = 1,
            pendingApprovalCount = 1,
            syncStatus = SyncStatus(
                level = SyncLevel.ERROR,
                pendingCount = 3,
                failedCount = 1,
                oldestPendingAt = now,
                lastSyncAt = now,
                message = "Ada event gagal",
                lastErrorMessage = "Ada event gagal"
            ),
            issues = listOf(
                OperationalIssue(
                    type = OperationalIssueType.SYNC_STATUS,
                    severity = IssueSeverity.CRITICAL,
                    label = "Sync Error",
                    description = "Ada event gagal",
                    status = "ERROR"
                )
            ),
            hasOperationalIssues = true
        )
    }

    private fun sampleShiftSummary(now: Instant): ShiftSummary {
        return ShiftSummary(
            shiftId = "shift-1",
            businessDayId = "day-1",
            status = "OPEN",
            openedAt = now,
            closedAt = null,
            operatorName = "Supervisor",
            openingCash = 100000.0,
            closingCash = null,
            expectedCash = 300000.0,
            variance = null,
            salesTotal = 250000.0,
            cashSalesTotal = 200000.0,
            nonCashSalesTotal = 50000.0,
            cashInTotal = 50000.0,
            cashOutTotal = 0.0,
            safeDropTotal = 0.0,
            pendingTransactionCount = 0,
            issues = emptyList(),
            hasUnresolvedIssues = false
        )
    }
}
