package id.azureenterprise.cassy.desktop

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
 * R5 Hardened Reporting Components.
 * Ensures truthful visibility of operational issues, blocks, and pending states.
 */

@Composable
fun OperationalIssueCard(
    issue: OperationalIssue,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when (issue.severity) {
        IssueSeverity.CRITICAL -> MaterialTheme.colorScheme.errorContainer
        IssueSeverity.WARNING -> Color(0xFFFEF3C7) // Amber 100
        IssueSeverity.INFO -> MaterialTheme.colorScheme.surfaceVariant
    }

    val contentColor = when (issue.severity) {
        IssueSeverity.CRITICAL -> MaterialTheme.colorScheme.onErrorContainer
        IssueSeverity.WARNING -> Color(0xFF92400E) // Amber 800
        IssueSeverity.INFO -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    // Using basic icons available in the standard Material set to avoid dependency issues
    val icon = when (issue.type) {
        OperationalIssueType.SYNC_STATUS -> Icons.Default.Refresh
        OperationalIssueType.PENDING_APPROVAL -> Icons.Default.Lock
        OperationalIssueType.OPERATIONAL_BLOCKER -> Icons.Default.Warning
        OperationalIssueType.DISCREPANCY -> Icons.Default.Info
        OperationalIssueType.HARDWARE_UNAVAILABLE -> Icons.Default.Build
        OperationalIssueType.STATE_UNAVAILABLE -> Icons.Default.Info // Fallback to Info
        OperationalIssueType.PENDING_TRANSACTION -> Icons.Default.ShoppingCart
        OperationalIssueType.OPEN_WORK_UNIT -> Icons.Default.DateRange
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor,
            contentColor = contentColor
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp).padding(top = 2.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
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
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = issue.description,
                    style = MaterialTheme.typography.bodySmall
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
                            MetadataChip(icon = Icons.Default.Notifications, text = formatTimestamp(it))
                        }
                        issue.reasonCode?.let {
                            MetadataChip(icon = Icons.Default.Info, text = it)
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
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(12.dp))
        Text(text = text, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium)
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
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val sortedIssues = issues.sortedByDescending { it.severity }
            items(sortedIssues) { issue ->
                OperationalIssueCard(issue = issue)
            }
        }
    }
}
