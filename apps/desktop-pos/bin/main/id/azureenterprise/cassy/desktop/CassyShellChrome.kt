package id.azureenterprise.cassy.desktop

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.azureenterprise.cassy.kernel.domain.SyncLevel
import id.azureenterprise.cassy.kernel.domain.SyncStatus

data class StatusBadgeModel(
    val label: String,
    val status: String,
    val detail: String? = null,
    val tone: UiTone
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CassyOperationalTopBar(
    state: DesktopShellState,
    hardware: CashierHardwareSnapshot,
    syncStatus: SyncStatus? = null,
    onOpenCommand: () -> Unit = {}
) {
    val runtimeChannel = remember { System.getProperty("cassy.runtime.channel", "unknown") }
    val releaseVersion = remember { System.getProperty("cassy.release.version", "dev") }
    val buildLabel = remember(runtimeChannel, releaseVersion) {
        if (runtimeChannel == "packaged-release-candidate") "Rilis $releaseVersion" else "Versi $releaseVersion"
    }

    Surface(
        tonalElevation = 0.dp,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = state.workspaceTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${state.storeName ?: "Toko belum aktif"} | ${state.terminalName ?: "Terminal belum ditetapkan"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = buildLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(
                modifier = Modifier.widthIn(min = 560.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatusBadge(businessDayBadge(state.dayStatus))
                    StatusBadge(shiftBadge(state.shiftStatus))
                    StatusBadge(syncStatusBadge(syncStatus))
                    nextActionBadge(state.nextActionLabel)?.let { badge -> StatusBadge(badge) }
                }
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    deviceStatusBadges(hardware).forEach { badge -> StatusBadge(badge) }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = humanizeOperatorLabel(state.operatorName, state.roleLabel),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    AssistChip(
                        onClick = onOpenCommand,
                        label = { Text("Cari Aksi") }
                    )
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
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        FlowRow(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CompactStatusText("Hari bisnis", humanizeBusinessDayLabel(operations.businessDayLabel))
            CompactStatusText("Shift aktif", humanizeShiftLabel(operations.shiftLabel))
            CompactStatusText("Printer", hardware.printer.label)
            CompactStatusText("Scanner", hardware.scanner.label)
            shell.nextActionLabel?.let { next ->
                CompactStatusText("Langkah aman berikutnya", next)
            }
        }
    }
}

@Composable
fun StatusBadge(model: StatusBadgeModel) {
    Surface(
        color = toneContainerColor(model.tone),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, toneColor(model.tone).copy(alpha = 0.28f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = model.label,
                style = MaterialTheme.typography.labelSmall,
                color = toneContentColor(model.tone)
            )
            Text(
                text = model.status,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = toneContentColor(model.tone)
            )
            model.detail?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelSmall,
                    color = toneContentColor(model.tone)
                )
            }
        }
    }
}

@Composable
private fun CompactStatusText(label: String, value: String) {
    Text(
        text = "$label: $value",
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

private fun businessDayBadge(dayStatus: String): StatusBadgeModel = when (dayStatus.uppercase()) {
    "OPEN" -> StatusBadgeModel("Hari bisnis", "Aktif", "Operasional harian sudah dibuka.", UiTone.Success)
    else -> StatusBadgeModel("Hari bisnis", "Belum aktif", "Buka hari bisnis sebelum transaksi dimulai.", UiTone.Warning)
}

private fun shiftBadge(shiftStatus: String): StatusBadgeModel = when (shiftStatus.uppercase()) {
    "OPEN", "ACTIVE" -> StatusBadgeModel("Shift", "Kasir siap", "Shift aktif. Aman untuk lanjut transaksi.", UiTone.Success)
    else -> StatusBadgeModel("Shift", "Belum dibuka", "Buka shift dan isi modal awal sebelum checkout.", UiTone.Warning)
}

private fun syncStatusBadge(syncStatus: SyncStatus?): StatusBadgeModel {
    if (syncStatus == null) {
        return StatusBadgeModel(
            label = "Sinkronisasi",
            status = "Mode lokal",
            detail = "Operasional tetap aman. Replay sync saat koneksi siap.",
            tone = UiTone.Info
        )
    }
    return when (syncStatus.level) {
        SyncLevel.HEALTHY -> StatusBadgeModel("Sinkronisasi", "Normal", "Antrean sinkron sehat.", UiTone.Success)
        SyncLevel.PENDING -> StatusBadgeModel("Sinkronisasi", "Menunggu ${syncStatus.pendingCount}", "Data masih antre, kasir tetap bisa lanjut.", UiTone.Info)
        SyncLevel.DELAYED -> StatusBadgeModel("Sinkronisasi", "Tertunda ${syncStatus.pendingCount}", "Replay saat ritme kasir sudah aman.", UiTone.Warning)
        SyncLevel.STALLED -> StatusBadgeModel("Sinkronisasi", "Butuh tindakan", "Antrean macet. Tinjau Sync Center.", UiTone.Danger)
        SyncLevel.ERROR -> StatusBadgeModel("Sinkronisasi", "Gagal ${syncStatus.failedCount}", "Periksa error lalu replay sinkronisasi.", UiTone.Danger)
    }
}

private fun nextActionBadge(nextActionLabel: String?): StatusBadgeModel? {
    if (nextActionLabel.isNullOrBlank()) return null
    return StatusBadgeModel(
        label = "Tindakan prioritas",
        status = nextActionLabel,
        detail = "Langkah paling aman untuk melanjutkan operasional.",
        tone = UiTone.Info
    )
}

private fun deviceStatusBadges(hardware: CashierHardwareSnapshot): List<StatusBadgeModel> = listOf(
    hardware.printer.toBadge(
        label = "Printer",
        readyDetail = "Struk bisa langsung dicetak.",
        fallbackDetail = "Jika belum siap, transaksi tetap lanjut dan struk bisa dicetak ulang nanti."
    ),
    hardware.scanner.toBadge(
        label = "Scanner",
        readyDetail = "Pemindaian barcode siap dipakai.",
        fallbackDetail = "Jika gagal baca, kasir bisa cari barang manual lewat SKU atau nama."
    ),
    hardware.cashDrawer.toBadge(
        label = "Laci kas",
        readyDetail = "Alur tunai normal.",
        fallbackDetail = "Jika bermasalah, transaksi tunai tetap lanjut dengan catatan audit manual."
    )
)

private fun HardwareDeviceState.toBadge(
    label: String,
    readyDetail: String,
    fallbackDetail: String
): StatusBadgeModel = when (status) {
    HardwareDeviceStatus.READY -> StatusBadgeModel(label, this.label.ifBlank { "Siap" }, readyDetail, UiTone.Success)
    HardwareDeviceStatus.UNKNOWN -> StatusBadgeModel(label, this.label.ifBlank { "Belum diperiksa" }, detailMessage ?: fallbackDetail, UiTone.Warning)
    HardwareDeviceStatus.WARNING -> StatusBadgeModel(label, this.label.ifBlank { "Perlu perhatian" }, detailMessage ?: fallbackDetail, UiTone.Warning)
    HardwareDeviceStatus.UNAVAILABLE -> StatusBadgeModel(label, this.label.ifBlank { "Tidak tersedia" }, detailMessage ?: fallbackDetail, UiTone.Danger)
}
