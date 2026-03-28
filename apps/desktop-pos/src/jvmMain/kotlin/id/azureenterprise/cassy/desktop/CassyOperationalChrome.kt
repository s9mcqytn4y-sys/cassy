package id.azureenterprise.cassy.desktop

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SpaceDashboard
import androidx.compose.material3.AssistChip
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    NavigationRail(
        modifier = Modifier.width(if (expanded) 112.dp else 76.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        header = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 10.dp)
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
                    modifier = Modifier.width(if (expanded) 48.dp else 40.dp)
                )
                if (expanded) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Cassy POS",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    ) {
        if (stage == DesktopStage.Workspace) {
            state.availableWorkspaces.forEach { workspace ->
                RailItemV2(
                    selected = workspace == selectedWorkspace,
                    icon = workspace.toOperationalIcon(),
                    label = workspace.shortLabel,
                    expanded = expanded,
                    onClick = { onSelectWorkspace(workspace) }
                )
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
            SyncLevel.PENDING -> "Menunggu kirim"
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
    val syncDetail = syncStatus?.let {
        when (it.level) {
            SyncLevel.HEALTHY -> "Antrian sinkron sehat."
            SyncLevel.PENDING -> "Perubahan lokal menunggu dikirim."
            SyncLevel.DELAYED -> "Operasional tetap aman, kirim ulang nanti."
            SyncLevel.STALLED -> "Perlu cek ulang sinkronisasi."
            SyncLevel.ERROR -> "Lanjutkan lokal dulu, lalu ulang sinkronisasi."
        }
    } ?: "Data lokal tetap aman di perangkat ini."
    Surface(
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface
    ) {
        when (stage) {
            DesktopStage.Bootstrap -> StageTopBar(
                title = "Pengaturan awal toko",
                subtitle = "Lengkapi identitas toko dan operator awal dulu. Setelah itu baru masuk dan mulai operasional.",
                statusTitle = "Belum siap transaksi",
                statusDetail = "Setup awal belum selesai. Hari bisnis dan shift belum perlu dibuka sekarang.",
                statusTone = UiTone.Warning,
                releaseVersion = releaseVersion
            )
            DesktopStage.Login -> StageTopBar(
                title = "Masuk operator",
                subtitle = "Pilih operator dan masukkan PIN lokal untuk melanjutkan ke terminal.",
                statusTitle = "Menunggu login operator",
                statusDetail = "Hak akses baru terlihat setelah operator aktif.",
                statusTone = UiTone.Info,
                releaseVersion = releaseVersion
            )
            DesktopStage.Loading -> StageTopBar(
                title = "Menyiapkan Cassy POS",
                subtitle = "Aplikasi sedang memuat konteks toko, operator, dan status operasional lokal.",
                statusTitle = "Memuat data lokal",
                statusDetail = "Tunggu sebentar sampai aplikasi siap dipakai.",
                statusTone = UiTone.Info,
                releaseVersion = releaseVersion
            )
            is DesktopStage.FatalError -> StageTopBar(
                title = "Masalah saat memuat aplikasi",
                subtitle = "Aplikasi belum bisa dipakai sampai masalah pemuatan selesai.",
                statusTitle = "Perlu tindakan",
                statusDetail = "Coba muat ulang. Jika tetap gagal, cek data lokal dan log aplikasi.",
                statusTone = UiTone.Danger,
                releaseVersion = releaseVersion
            )
            DesktopStage.Workspace -> {
                val readinessTitle: String
                val readinessDetail: String
                val readinessTone: UiTone
                when {
                    state.dayStatus != "OPEN" -> {
                        readinessTitle = "Hari bisnis belum dibuka"
                        readinessDetail = "Transaksi belum bisa dimulai. Buka hari bisnis terlebih dahulu."
                        readinessTone = UiTone.Danger
                    }
                    state.shiftStatus != "OPEN" -> {
                        readinessTitle = "Shift belum dibuka"
                        readinessDetail = "Isi modal awal lalu buka shift sebelum kasir dipakai."
                        readinessTone = UiTone.Warning
                    }
                    else -> {
                        readinessTitle = "Kasir siap dipakai"
                        readinessDetail = state.nextActionLabel?.let { "Langkah aman berikutnya: $it." }
                            ?: "Tidak ada blocker utama pada terminal ini."
                        readinessTone = UiTone.Success
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.widthIn(min = 180.dp, max = 240.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(state.workspaceTitle, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(
                            state.storeName ?: "Outlet belum aktif",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            state.terminalName ?: "Perangkat kasir belum terhubung",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    ChromeStatusPanel(
                        title = readinessTitle,
                        detail = readinessDetail,
                        tone = readinessTone,
                        modifier = Modifier.weight(1f)
                    )

                    Column(
                        modifier = Modifier.widthIn(min = 200.dp, max = 240.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            humanizeOperatorLabel(state.operatorName, state.roleLabel),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "Versi $releaseVersion",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "Sinkronisasi: $syncLabel",
                            style = MaterialTheme.typography.labelSmall,
                            color = toneContentColor(syncTone)
                        )
                        Text(
                            primaryHardwareSummary(hardware),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.End
                        )
                        AssistChip(onClick = onOpenCommand, label = { Text("Aksi") })
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
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
                "Sinkron ${bottomStripSyncLabel(operations.reportingSummary?.syncStatus)} · ${shell.nextActionLabel ?: "Pantau status utama di atas"}",
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
private fun StageTopBar(
        title: String,
        subtitle: String,
    statusTitle: String,
    statusDetail: String,
    statusTone: UiTone,
    releaseVersion: String
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        ChromeStatusPanel(
            title = statusTitle,
            detail = statusDetail,
            tone = statusTone,
            modifier = Modifier.widthIn(min = 260.dp, max = 320.dp)
        )

        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                "Versi $releaseVersion",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "Cepat di kasir. Rapi di operasional.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ChromeStatusPanel(
    title: String,
    detail: String,
    tone: UiTone,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = toneContainerColor(tone)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = toneContentColor(tone)
            )
            Text(
                detail,
                style = MaterialTheme.typography.bodySmall,
                color = toneContentColor(tone)
            )
        }
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
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Cassy $releaseVersion",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "${shell.storeName ?: "Outlet lokal"} | ${shell.terminalName ?: "Perangkat lokal"} | Ctrl+/ bantuan",
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

private fun bottomStripSyncLabel(status: SyncStatus?): String = when (status?.level) {
    SyncLevel.HEALTHY -> "Sehat"
    SyncLevel.PENDING -> "Menunggu"
    SyncLevel.DELAYED -> "Terlambat"
    SyncLevel.STALLED -> "Macet"
    SyncLevel.ERROR -> "Perlu tindakan"
    null -> "Lokal"
}

private fun DesktopWorkspace.toOperationalIcon(): ImageVector = when (this) {
    DesktopWorkspace.Dashboard -> Icons.Default.SpaceDashboard
    DesktopWorkspace.Cashier -> Icons.Default.PointOfSale
    DesktopWorkspace.History -> Icons.Default.History
    DesktopWorkspace.Inventory -> Icons.Default.Inventory2
    DesktopWorkspace.Operations -> Icons.Default.AdminPanelSettings
    DesktopWorkspace.Reporting -> Icons.Default.Assessment
    DesktopWorkspace.System -> Icons.Default.Settings
}
