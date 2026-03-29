package id.azureenterprise.cassy.desktop

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.azureenterprise.cassy.kernel.domain.SyncLevel
import id.azureenterprise.cassy.kernel.domain.SyncStatus

@Composable
fun CassyOperationalRail(
    state: DesktopShellState,
    selectedWorkspace: DesktopWorkspace,
    stage: DesktopStage,
    expanded: Boolean,
    onToggleExpanded: () -> Unit,
    onSelectWorkspace: (DesktopWorkspace) -> Unit,
    onLogout: () -> Unit,
    onReload: () -> Unit
) {
    val primaryWorkspaces = state.availableWorkspaces.filter { it in PRIMARY_WORKSPACES }
    val secondaryWorkspaces = state.availableWorkspaces.filter { it in SECONDARY_WORKSPACES }
    NavigationRail(
        modifier = Modifier.width(if (expanded) 112.dp else 76.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        header = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                IconButton(onClick = onToggleExpanded) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ChevronLeft else Icons.Default.Menu,
                        contentDescription = if (expanded) "Sembunyikan sidebar" else "Tampilkan sidebar"
                    )
                }
                Image(
                    painter = painterResource(CASSY_BRAND_ICON_RESOURCE),
                    contentDescription = "Logo Cassy",
                    modifier = Modifier.width(if (expanded) 44.dp else 38.dp)
                )
                if (expanded) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Cassy POS",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    ) {
        if (stage == DesktopStage.Workspace) {
            primaryWorkspaces.forEach { workspace ->
                RailItemV2(
                    selected = workspace == selectedWorkspace,
                    icon = workspace.toOperationalIcon(),
                    label = workspace.shortLabel,
                    expanded = expanded,
                    onClick = { onSelectWorkspace(workspace) }
                )
            }
            if (secondaryWorkspaces.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                secondaryWorkspaces.forEach { workspace ->
                    RailItemV2(
                        selected = workspace == selectedWorkspace,
                        icon = workspace.toOperationalIcon(),
                        label = workspace.shortLabel,
                        expanded = expanded,
                        onClick = { onSelectWorkspace(workspace) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        RailItemV2(
            selected = false,
            icon = Icons.Default.Refresh,
            label = "Sinkron",
            expanded = expanded,
            onClick = onReload
        )
        RailItemV2(
            selected = false,
            icon = Icons.AutoMirrored.Filled.ExitToApp,
            label = "Keluar",
            expanded = expanded,
            onClick = onLogout,
            tone = UiTone.Danger
        )
        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
private fun RailItemV2(
    selected: Boolean,
    icon: ImageVector,
    label: String,
    expanded: Boolean,
    onClick: () -> Unit,
    tone: UiTone = UiTone.Info
) {
    NavigationRailItem(
        selected = selected,
        onClick = onClick,
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (selected) {
                    MaterialTheme.colorScheme.primary
                } else if (tone == UiTone.Danger) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        },
        label = if (expanded) {
            {
                Text(
                    label,
                    fontSize = 11.sp,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                )
            }
        } else {
            null
        },
        colors = NavigationRailItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.primary,
            indicatorColor = MaterialTheme.colorScheme.primaryContainer
        )
    )
}

@Composable
fun CassyOperationalTopBar(
    stage: DesktopStage,
    state: DesktopShellState,
    hardware: CashierHardwareSnapshot,
    syncStatus: SyncStatus? = null,
    onOpenCommand: () -> Unit = {}
) {
    val releaseVersion = remember { System.getProperty("cassy.release.version", "dev") }
    val syncLabel = syncStatus?.let {
        when (it.level) {
            SyncLevel.HEALTHY -> "Normal"
            SyncLevel.PENDING -> "Menunggu"
            SyncLevel.DELAYED -> "Tertunda"
            SyncLevel.STALLED -> "Macet"
            SyncLevel.ERROR -> "Perlu tindakan"
        }
    } ?: "Lokal aman"
    val syncTone = syncStatus?.let {
        when (it.level) {
            SyncLevel.HEALTHY -> UiTone.Success
            SyncLevel.PENDING -> UiTone.Info
            SyncLevel.DELAYED -> UiTone.Warning
            SyncLevel.STALLED, SyncLevel.ERROR -> UiTone.Danger
        }
    } ?: UiTone.Info

    Surface(
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth().height(52.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        if (stage != DesktopStage.Workspace) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Menyiapkan Sesi...", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                Text("Versi $releaseVersion", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(text = state.workspaceTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)

                    // Breadcrumb Context
                    Text(
                        text = "${state.storeName ?: "Outlet"}  •  ${state.terminalName ?: "Terminal"}  •  ${state.operatorName ?: "Operator"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    StatusIndicator(label = "Sync", status = syncLabel, tone = syncTone)

                    val hardwareIssue = primaryHardwareIssueOrNull(hardware)
                    if (hardwareIssue != null) {
                        CompactChromeBadge(label = hardwareIssue, tone = UiTone.Warning)
                    }

                    Button(
                        onClick = onOpenCommand,
                        contentPadding = PaddingValues(horizontal = 12.dp),
                        modifier = Modifier.height(32.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.FlashOn, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Aksi Cepat", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

@Composable
fun CassyOperationalBottomStrip(
    shell: DesktopShellState,
    operations: OperationsState,
    hardware: CashierHardwareSnapshot
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Sync: ${bottomStripSyncLabel(operations.reportingSummary?.syncStatus)} · ${shell.nextActionLabel ?: "Sistem Nominal"}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                primaryHardwareSummary(hardware),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CompactChromeBadge(
    label: String,
    tone: UiTone
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = toneContainerColor(tone)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = toneContentColor(tone)
        )
    }
}

@Composable
fun CassyOperationalFooter(
    shell: DesktopShellState,
    modifier: Modifier = Modifier
) {
    val releaseVersion = remember { System.getProperty("cassy.release.version", "dev") }
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Cassy $releaseVersion",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "${shell.storeName ?: "Outlet"} | ${shell.terminalName ?: "Terminal"} | Ctrl+/ bantuan",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun primaryHardwareSummary(hardware: CashierHardwareSnapshot): String {
    val devices = listOf(
        "Printer" to hardware.printer,
        "Scanner" to hardware.scanner,
        "Laci kas" to hardware.cashDrawer
    )
    val issue = devices.firstOrNull { it.second.status != HardwareDeviceStatus.READY }
    return issue?.let { "${it.first}: ${it.second.label}" } ?: "Perangkat utama siap"
}

private fun primaryHardwareIssueOrNull(hardware: CashierHardwareSnapshot): String? {
    val summary = primaryHardwareSummary(hardware)
    return summary.takeUnless { it == "Perangkat utama siap" }
}

private val PRIMARY_WORKSPACES = setOf(
    DesktopWorkspace.Cashier,
    DesktopWorkspace.Dashboard,
    DesktopWorkspace.History,
    DesktopWorkspace.Inventory,
    DesktopWorkspace.Operations
)

private val SECONDARY_WORKSPACES = setOf(
    DesktopWorkspace.Reporting,
    DesktopWorkspace.System,
    DesktopWorkspace.Settings
)

private fun bottomStripSyncLabel(status: SyncStatus?): String = when (status?.level) {
    SyncLevel.HEALTHY -> "Sehat"
    SyncLevel.PENDING -> "Menunggu"
    SyncLevel.DELAYED -> "Terlambat"
    SyncLevel.STALLED -> "Macet"
    SyncLevel.ERROR -> "Bermasalah"
    null -> "Lokal"
}

private fun DesktopWorkspace.toOperationalIcon(): ImageVector = when (this) {
    DesktopWorkspace.Dashboard -> Icons.Default.SpaceDashboard
    DesktopWorkspace.Cashier -> Icons.Default.PointOfSale
    DesktopWorkspace.History -> Icons.Default.History
    DesktopWorkspace.Inventory -> Icons.Default.Inventory2
    DesktopWorkspace.Operations -> Icons.Default.AdminPanelSettings
    DesktopWorkspace.Reporting -> Icons.Default.Assessment
    DesktopWorkspace.System -> Icons.Default.Dns
    DesktopWorkspace.Settings -> Icons.Default.Settings
}
