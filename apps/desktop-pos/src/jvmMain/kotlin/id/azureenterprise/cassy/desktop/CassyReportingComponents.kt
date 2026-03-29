package id.azureenterprise.cassy.desktop

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.azureenterprise.cassy.kernel.domain.IssueSeverity
import id.azureenterprise.cassy.kernel.domain.OperationalIssue
import id.azureenterprise.cassy.kernel.domain.OperationalIssueType
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * CassyReportingComponents: Root component for Reporting workspace.
 */
@Composable
fun CassyReportingComponents(
    state: OperationsState,
    onExportReport: () -> Unit,
    isBusy: Boolean
) {
    WorkspacePage("Laporan Operasional", "Analisa performa penjualan dan kesehatan terminal.") {
        val summary = state.reportingSummary
        if (summary == null) {
            WorkspaceCard("Ringkasan Belum Tersedia") {
                Text("Data hari bisnis aktif belum siap untuk dianalisa.")
            }
            return@WorkspacePage
        }

        Row(horizontalArrangement = Arrangement.spacedBy(20.dp), modifier = Modifier.fillMaxWidth()) {
            WorkspaceCard("Statistik Hari Ini", Modifier.weight(1f)) {
                SummaryRowV2("Transaksi Berhasil", "${summary.transactionCount}")
                SummaryRowV2("Void / Retur", "${summary.voidedSaleCount}")
                SummaryRowV2("Persetujuan Tunda", "${summary.pendingApprovalCount}")
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                SummaryRowV2("Estimasi Kas Akhir", "Rp ${summary.netCashMovement.toInt()}")
            }

            WorkspaceCard("Ekspor Data", Modifier.weight(1f)) {
                Text(
                    "Ekspor mengikuti snapshot lokal desktop. Data yang keluar harus dibaca sebagai operational truth terminal saat ini.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                SummaryRowV2("Format Output", "Bundle CSV")
                SummaryRowV2("Target Folder", state.reportingExportPath ?: "Default App Data")

                Button(
                    onClick = onExportReport,
                    enabled = !isBusy,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                ) {
                    if (isBusy) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(10.dp))
                        Text("Mengekspor...")
                    } else {
                        Text("Unduh Laporan Hari Ini")
                    }
                }
            }
        }

        WorkspaceCard("Antrian Isu Operasional") {
            OperationalIssueList(issues = summary.issues, modifier = Modifier.heightIn(max = 400.dp))
        }
    }
}

@Composable
fun OperationalIssueCard(
    issue: OperationalIssue,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when (issue.severity) {
        IssueSeverity.CRITICAL -> toneContainerColor(UiTone.Danger).copy(alpha = 0.2f)
        IssueSeverity.WARNING -> toneContainerColor(UiTone.Warning).copy(alpha = 0.2f)
        IssueSeverity.INFO -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    }

    val contentColor = when (issue.severity) {
        IssueSeverity.CRITICAL -> MaterialTheme.colorScheme.error
        IssueSeverity.WARNING -> toneColor(UiTone.Warning)
        IssueSeverity.INFO -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    val icon = when (issue.type) {
        OperationalIssueType.SYNC_STATUS -> Icons.Default.Sync
        OperationalIssueType.PENDING_APPROVAL -> Icons.Default.Lock
        OperationalIssueType.OPERATIONAL_BLOCKER -> Icons.Default.Block
        OperationalIssueType.DISCREPANCY -> Icons.Default.Difference
        OperationalIssueType.HARDWARE_UNAVAILABLE -> Icons.Default.PrintDisabled
        OperationalIssueType.STATE_UNAVAILABLE -> Icons.Default.ErrorOutline
        OperationalIssueType.PENDING_TRANSACTION -> Icons.Default.PendingActions
        OperationalIssueType.OPEN_WORK_UNIT -> Icons.AutoMirrored.Filled.EventNote
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        border = BorderStroke(1.dp, contentColor.copy(alpha = 0.15f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = issue.label,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    issue.status?.let {
                        Surface(
                            shape = CircleShape,
                            color = contentColor.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
                Text(
                    text = issue.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (issue.actor != null || issue.timestamp != null || issue.reasonCode != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        issue.actor?.let {
                            MetadataChip(icon = Icons.Default.Person, text = it)
                        }
                        issue.timestamp?.let {
                            MetadataChip(icon = Icons.Default.Schedule, text = formatTimestamp(it))
                        }
                        issue.reasonCode?.let {
                            MetadataChip(icon = Icons.Default.Tag, text = it)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetadataChip(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = text, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun formatTimestamp(instant: Instant): String {
    val lt = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    return "${lt.hour.toString().padStart(2, '0')}:${lt.minute.toString().padStart(2, '0')}"
}

@Composable
fun OperationalIssueList(
    issues: List<OperationalIssue>,
    modifier: Modifier = Modifier
) {
    if (issues.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF16A34A),
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Operasional Sehat",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Tidak ada masalah atau tindakan tertunda.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        val sortedIssues = remember(issues) {
            issues.sortedByDescending { it.severity }
        }
        LazyColumn(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(sortedIssues) { issue ->
                OperationalIssueCard(issue = issue)
            }
        }
    }
}
