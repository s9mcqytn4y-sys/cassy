package id.azureenterprise.cassy.desktop

import id.azureenterprise.cassy.kernel.domain.DailySummary
import id.azureenterprise.cassy.kernel.domain.IssueSeverity
import id.azureenterprise.cassy.kernel.domain.OperationalIssue
import id.azureenterprise.cassy.kernel.domain.ShiftSummary
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.bufferedWriter
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText

class DesktopReportingExporter(
    private val clock: Clock,
    private val exportRootProvider: () -> Path = ::defaultExportRoot
) {
    fun export(bundle: OperationalReportBundle): ReportingExportResult {
        val exportedAt = clock.now()
        val exportRoot = exportRootProvider().createDirectories()
        val folderName = buildExportFolderName(bundle, exportedAt)
        val exportDirectory = exportRoot.resolve(folderName).createDirectories()

        writeDailySummaryCsv(exportDirectory.resolve("daily-summary.csv"), bundle, exportedAt)
        writeShiftSummaryCsv(exportDirectory.resolve("shift-summary.csv"), bundle.shiftSummary)
        writeIssueCsv(exportDirectory.resolve("operational-issues.csv"), bundle.dailySummary.issues)
        writeHtmlOverview(exportDirectory.resolve("README.html"), bundle, exportedAt)

        return ReportingExportResult(
            exportDirectory = exportDirectory,
            exportedAt = exportedAt,
            generatedFiles = listOf(
                exportDirectory.resolve("daily-summary.csv"),
                exportDirectory.resolve("shift-summary.csv"),
                exportDirectory.resolve("operational-issues.csv"),
                exportDirectory.resolve("README.html")
            ),
            ruleNote = RULE_NOTE
        )
    }

    private fun writeDailySummaryCsv(path: Path, bundle: OperationalReportBundle, exportedAt: Instant) {
        path.bufferedWriter(StandardCharsets.UTF_8).use { writer ->
            CSVPrinter(writer, csvFormat()).use { csv ->
                csv.printRecord(
                    "exported_at",
                    "store_name",
                    "terminal_name",
                    "operator_name",
                    "business_day_id",
                    "date_label",
                    "status",
                    "opened_at",
                    "closed_at",
                    "total_sales",
                    "transaction_count",
                    "cash_sales_total",
                    "non_cash_sales_total",
                    "net_cash_movement",
                    "shift_count",
                    "open_shift_count",
                    "pending_approval_count",
                    "sync_level",
                    "sync_pending_count",
                    "sync_failed_count",
                    "sync_last_success_at",
                    "sync_last_error_message",
                    "has_operational_issues",
                    "export_rule"
                )
                csv.printRecord(
                    exportedAt.toString(),
                    bundle.shell.storeName.orEmpty(),
                    bundle.shell.terminalName.orEmpty(),
                    bundle.shell.operatorName.orEmpty(),
                    bundle.dailySummary.businessDayId,
                    bundle.dailySummary.dateLabel,
                    bundle.dailySummary.status,
                    bundle.dailySummary.openedAt.toString(),
                    bundle.dailySummary.closedAt?.toString().orEmpty(),
                    bundle.dailySummary.totalSales.toCurrencyString(),
                    bundle.dailySummary.transactionCount,
                    bundle.dailySummary.cashSalesTotal.toCurrencyString(),
                    bundle.dailySummary.nonCashSalesTotal.toCurrencyString(),
                    bundle.dailySummary.netCashMovement.toCurrencyString(),
                    bundle.dailySummary.shiftCount,
                    bundle.dailySummary.openShiftCount,
                    bundle.dailySummary.pendingApprovalCount,
                    bundle.dailySummary.syncStatus.level.name,
                    bundle.dailySummary.syncStatus.pendingCount,
                    bundle.dailySummary.syncStatus.failedCount,
                    bundle.dailySummary.syncStatus.lastSyncAt?.toString().orEmpty(),
                    bundle.dailySummary.syncStatus.lastErrorMessage.orEmpty(),
                    bundle.dailySummary.hasOperationalIssues,
                    RULE_NOTE
                )
            }
        }
    }

    private fun writeShiftSummaryCsv(path: Path, shiftSummary: ShiftSummary?) {
        path.bufferedWriter(StandardCharsets.UTF_8).use { writer ->
            CSVPrinter(writer, csvFormat()).use { csv ->
                csv.printRecord(
                    "shift_id",
                    "business_day_id",
                    "status",
                    "opened_at",
                    "closed_at",
                    "operator_name",
                    "opening_cash",
                    "closing_cash",
                    "expected_cash",
                    "variance",
                    "sales_total",
                    "cash_sales_total",
                    "non_cash_sales_total",
                    "cash_in_total",
                    "cash_out_total",
                    "safe_drop_total",
                    "pending_transaction_count",
                    "has_unresolved_issues"
                )
                if (shiftSummary == null) {
                    csv.printRecord("", "", "NO_SHIFT_CONTEXT", "", "", "", "", "", "", "", "", "", "", "", "", "", 0, false)
                } else {
                    csv.printRecord(
                        shiftSummary.shiftId,
                        shiftSummary.businessDayId,
                        shiftSummary.status,
                        shiftSummary.openedAt.toString(),
                        shiftSummary.closedAt?.toString().orEmpty(),
                        shiftSummary.operatorName,
                        shiftSummary.openingCash.toCurrencyString(),
                        shiftSummary.closingCash?.toCurrencyString().orEmpty(),
                        shiftSummary.expectedCash.toCurrencyString(),
                        shiftSummary.variance?.toCurrencyString().orEmpty(),
                        shiftSummary.salesTotal.toCurrencyString(),
                        shiftSummary.cashSalesTotal.toCurrencyString(),
                        shiftSummary.nonCashSalesTotal.toCurrencyString(),
                        shiftSummary.cashInTotal.toCurrencyString(),
                        shiftSummary.cashOutTotal.toCurrencyString(),
                        shiftSummary.safeDropTotal.toCurrencyString(),
                        shiftSummary.pendingTransactionCount,
                        shiftSummary.hasUnresolvedIssues
                    )
                }
            }
        }
    }

    private fun writeIssueCsv(path: Path, issues: List<OperationalIssue>) {
        path.bufferedWriter(StandardCharsets.UTF_8).use { writer ->
            CSVPrinter(writer, csvFormat()).use { csv ->
                csv.printRecord(
                    "severity",
                    "type",
                    "label",
                    "description",
                    "status",
                    "actor",
                    "timestamp",
                    "reason_code",
                    "source_id"
                )
                issues
                    .sortedByDescending { issueSeverityRank(it.severity) }
                    .forEach { issue ->
                        csv.printRecord(
                            issue.severity.name,
                            issue.type.name,
                            issue.label,
                            issue.description,
                            issue.status.orEmpty(),
                            issue.actor.orEmpty(),
                            issue.timestamp?.toString().orEmpty(),
                            issue.reasonCode.orEmpty(),
                            issue.sourceId.orEmpty()
                        )
                    }
            }
        }
    }

    private fun writeHtmlOverview(path: Path, bundle: OperationalReportBundle, exportedAt: Instant) {
        val shiftSummary = bundle.shiftSummary
        val issuesMarkup = if (bundle.dailySummary.issues.isEmpty()) {
            "<li>Tidak ada issue operasional terbuka.</li>"
        } else {
            bundle.dailySummary.issues
                .sortedByDescending { issueSeverityRank(it.severity) }
                .joinToString(separator = "") { issue ->
                    "<li><strong>${issue.severity.name}</strong> - ${escape(issue.label)}: ${escape(issue.description)}</li>"
                }
        }

        path.writeText(
            """
            <!doctype html>
            <html lang="id">
            <head>
              <meta charset="utf-8" />
              <title>Cassy Operational Export</title>
              <style>
                body { font-family: Segoe UI, Arial, sans-serif; margin: 32px; color: #17212b; background: #f3f6f8; }
                h1, h2 { margin-bottom: 8px; }
                .meta { color: #51606d; margin-bottom: 24px; }
                .grid { display: grid; grid-template-columns: repeat(2, minmax(260px, 1fr)); gap: 16px; margin-bottom: 24px; }
                .card { background: white; border-radius: 16px; padding: 18px; box-shadow: 0 10px 24px rgba(10, 30, 45, 0.08); }
                .label { font-size: 12px; text-transform: uppercase; color: #6b7a88; }
                .value { font-size: 24px; font-weight: 700; margin-top: 4px; }
                ul { padding-left: 20px; }
                code { background: #e6edf2; padding: 2px 6px; border-radius: 6px; }
              </style>
            </head>
            <body>
              <h1>Operational Export Cassy</h1>
              <p class="meta">Diekspor ${escape(exportedAt.toString())} | Rule: ${escape(RULE_NOTE)}</p>
              <div class="grid">
                <section class="card">
                  <div class="label">Store / Terminal</div>
                  <div class="value">${escape(bundle.shell.storeName.orEmpty())} / ${escape(bundle.shell.terminalName.orEmpty())}</div>
                </section>
                <section class="card">
                  <div class="label">Business Day</div>
                  <div class="value">${escape(bundle.dailySummary.dateLabel)}</div>
                </section>
                <section class="card">
                  <div class="label">Total Sales</div>
                  <div class="value">${bundle.dailySummary.totalSales.toCurrencyString()}</div>
                </section>
                <section class="card">
                  <div class="label">Sync State</div>
                  <div class="value">${bundle.dailySummary.syncStatus.level.name} / pending ${bundle.dailySummary.syncStatus.pendingCount} / failed ${bundle.dailySummary.syncStatus.failedCount}</div>
                </section>
              </div>
              <section class="card">
                <h2>Shift Summary</h2>
                <p>${escape(shiftSummary?.shiftId ?: "Tidak ada shift relevan pada snapshot ini")}</p>
                <p>Operator: ${escape(shiftSummary?.operatorName.orEmpty())}</p>
                <p>Expected cash: <code>${shiftSummary?.expectedCash?.toCurrencyString().orEmpty()}</code></p>
                <p>Variance: <code>${shiftSummary?.variance?.toCurrencyString().orEmpty()}</code></p>
              </section>
              <section class="card" style="margin-top: 16px;">
                <h2>Issue Operasional</h2>
                <ul>$issuesMarkup</ul>
              </section>
            </body>
            </html>
            """.trimIndent(),
            StandardCharsets.UTF_8
        )
    }

    private fun csvFormat(): CSVFormat = CSVFormat.DEFAULT.builder()
        .setHeader()
        .setSkipHeaderRecord(false)
        .build()

    private fun buildExportFolderName(bundle: OperationalReportBundle, exportedAt: Instant): String {
        val timestamp = exportedAt.toString()
            .replace(":", "")
            .replace("-", "")
            .replace("T", "-")
            .substringBefore(".")
        return listOf(
            "cassy-report",
            timestamp,
            sanitizeSegment(bundle.shell.storeName.orEmpty().ifBlank { "store" }),
            sanitizeSegment(bundle.shell.terminalName.orEmpty().ifBlank { "terminal" })
        ).joinToString("-")
    }

    private fun issueSeverityRank(severity: IssueSeverity): Int = when (severity) {
        IssueSeverity.CRITICAL -> 3
        IssueSeverity.WARNING -> 2
        IssueSeverity.INFO -> 1
    }

    private fun sanitizeSegment(value: String): String {
        return value
            .lowercase()
            .replace(Regex("[^a-z0-9]+"), "-")
            .trim('-')
            .ifEmpty { "na" }
    }

    private fun escape(value: String): String {
        return value
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
    }

    private fun Double.toCurrencyString(): String = toLong().toString()

    private companion object {
        const val RULE_NOTE = "Snapshot lokal Cassy; accuracy > visuals; dipakai untuk review operasional POS, bukan ledger HQ."

        fun defaultExportRoot(): Path {
            val home = Paths.get(System.getProperty("user.home"))
            val documents = home.resolve("Documents")
            val base = if (Files.isDirectory(documents)) documents else home
            return base.resolve("Cassy").resolve("exports")
        }
    }
}

data class OperationalReportBundle(
    val shell: DesktopShellState,
    val dailySummary: DailySummary,
    val shiftSummary: ShiftSummary?,
    val exportedBy: String?
)

data class ReportingExportResult(
    val exportDirectory: Path,
    val exportedAt: Instant,
    val generatedFiles: List<Path>,
    val ruleNote: String
)
