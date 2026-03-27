package id.azureenterprise.cassy.desktop

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import id.azureenterprise.cassy.kernel.domain.CashMovementType
import id.azureenterprise.cassy.kernel.domain.OperationDecision
import id.azureenterprise.cassy.kernel.domain.OperationStatus
import id.azureenterprise.cassy.kernel.domain.OperationType
import id.azureenterprise.cassy.kernel.domain.OperationalControlSnapshot
import id.azureenterprise.cassy.kernel.domain.SyncLevel
import id.azureenterprise.cassy.kernel.domain.SyncStatus
import kotlinx.datetime.Instant

@Composable
fun LoadingStage() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
fun FatalStage(message: String, onRetry: () -> Unit) {
    CenterPanel(
        title = "Gagal Memuat Data",
        subtitle = message,
        action = {
            Button(onClick = onRetry) { Text("Coba Lagi") }
        }
    )
}

@Composable
fun BootstrapStage(
    state: DesktopAppState,
    onFieldChanged: (BootstrapField, String) -> Unit,
    onBootstrap: () -> Unit
) {
    CenterPanel(
        title = "Pengaturan Awal Toko",
        subtitle = "Desktop ini menjadi terminal utama transaksi. Isi identitas toko dan dua peran minimum agar POS langsung operasional.",
        contentWidth = 780.dp,
        action = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                ShortcutHintBar(hints = listOf("Tab Pindah Field", "Enter Simpan", "PIN 6 Digit"))
                StageSectionCard(title = "Identitas Terminal") {
                    SemanticTextField(
                        label = "Nama Toko",
                        value = state.bootstrap.storeName,
                        onValueChange = { onFieldChanged(BootstrapField.StoreName, it) },
                        helperText = "Nama yang muncul di struk dan reporting, misalnya Toko Berkah Jaya.",
                        placeholder = "Nama toko",
                        leadingIcon = Icons.Default.Storefront
                    )
                    SemanticTextField(
                        label = "Nama Terminal",
                        value = state.bootstrap.terminalName,
                        onValueChange = { onFieldChanged(BootstrapField.TerminalName, it) },
                        helperText = "Gunakan nama yang mudah dikenali saat review operasional, misalnya Kasir-01.",
                        placeholder = "Kasir-01",
                        leadingIcon = Icons.Default.PointOfSale
                    )
                }
                StageSectionCard(title = "Akses Operator Awal") {
                    SemanticTextField(
                        label = "Nama Kasir",
                        value = state.bootstrap.cashierName,
                        onValueChange = { onFieldChanged(BootstrapField.CashierName, it) },
                        helperText = "Kasir frontline untuk transaksi harian.",
                        placeholder = "Nama kasir",
                        leadingIcon = Icons.Default.Person
                    )
                    SemanticPinField(
                        label = "PIN Kasir",
                        value = state.bootstrap.cashierPin,
                        onValueChange = { onFieldChanged(BootstrapField.CashierPin, it) },
                        helperText = "Wajib 6 digit numerik."
                    )
                    SemanticTextField(
                        label = "Nama Supervisor",
                        value = state.bootstrap.supervisorName,
                        onValueChange = { onFieldChanged(BootstrapField.SupervisorName, it) },
                        helperText = "Supervisor dibutuhkan untuk open day dan approval operasional.",
                        placeholder = "Nama supervisor",
                        leadingIcon = Icons.Default.Badge
                    )
                    SemanticPinField(
                        label = "PIN Supervisor",
                        value = state.bootstrap.supervisorPin,
                        onValueChange = { onFieldChanged(BootstrapField.SupervisorPin, it) },
                        helperText = "Simpan hanya ke orang yang berwenang.",
                        onImeAction = onBootstrap
                    )
                }
                Button(onClick = onBootstrap, enabled = !state.isBusy, modifier = Modifier.fillMaxWidth().height(50.dp)) {
                    Text(if (state.isBusy) "Menyimpan..." else "Simpan Pengaturan Awal")
                }
            }
        }
    )
}

@Composable
fun LoginStage(
    state: DesktopAppState,
    onSelectOperator: (String) -> Unit,
    onPinChanged: (String) -> Unit,
    onLogin: () -> Unit
) {
    CenterPanel(
        title = "Pilih Operator",
        subtitle = "Masuk dengan PIN lokal. Pilih peran dulu agar hak akses dan blocker operasional langsung terlihat.",
        contentWidth = 760.dp,
        action = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(18.dp)) {
                ShortcutHintBar(hints = listOf("Klik Operator", "PIN 6 Digit", "Enter Masuk"))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    state.login.operators.forEach { option ->
                        val selected = state.login.selectedOperatorId == option.id
                        ElevatedCard(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(18.dp),
                            onClick = { onSelectOperator(option.id) },
                            colors = if (selected) {
                                CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                            } else {
                                CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            }
                        ) {
                            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(option.displayName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Text(option.roleLabel, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    if (selected) "Siap login" else "Pilih untuk mengaktifkan PIN",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (selected) toneColor(UiTone.Info) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                SemanticPinField(
                    label = "PIN Operator",
                    value = state.login.pin,
                    onValueChange = onPinChanged,
                    modifier = Modifier.fillMaxWidth(),
                    helperText = "PIN tidak dikirim ke backend. Validasi terjadi di local-first boundary desktop.",
                    onImeAction = onLogin
                )
                state.login.feedback?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
                Button(onClick = onLogin, enabled = !state.isBusy, modifier = Modifier.fillMaxWidth().height(50.dp)) {
                    Text(if (state.isBusy) "Memproses..." else "Masuk ke Terminal")
                }
            }
        }
    )
}

@Composable
fun OpenDayStage(
    state: DesktopAppState,
    onOpenDay: () -> Unit,
    onLogout: () -> Unit
) {
    CenterPanel(
        title = "Hari Bisnis Belum Dibuka",
        subtitle = state.operations.dashboard.headline,
        action = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OperationalDashboardCard(state.operations.dashboard)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (state.operations.canOpenDay) {
                        Button(onClick = onOpenDay, enabled = !state.isBusy, modifier = Modifier.height(48.dp)) {
                            Text(if (state.isBusy) "Memproses..." else "Buka Hari Bisnis")
                        }
                    } else {
                        OutlinedButton(onClick = onLogout, modifier = Modifier.height(48.dp)) { Text("Ganti Operator") }
                    }
                }
            }
        }
    )
}

@Composable
fun StartShiftStage(
    state: DesktopAppState,
    onOpeningCashChanged: (String) -> Unit,
    onOpeningCashReasonChanged: (String) -> Unit,
    onShortcutSelected: (String) -> Unit,
    onStartShift: () -> Unit
) {
    CenterPanel(
        title = "Buka Kasir",
        subtitle = state.operations.dashboard.headline,
        contentWidth = 640.dp,
        action = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                ShortcutHintBar(hints = listOf("Shortcut Nominal", "Enter Buka Kasir", "Review Bloker"))
                OperationalDashboardCard(state.operations.dashboard)
                ShortcutNominalRow(
                    values = listOf("100000", "200000", "500000", "1000000"),
                    onShortcutSelected = onShortcutSelected
                )
                CassyCurrencyInput(
                    label = "Modal Awal Tunai",
                    value = state.operations.openingCashInput,
                    onValueChange = onOpeningCashChanged,
                    helperText = "Isi saldo laci kas aktual sebelum transaksi pertama.",
                    onImeAction = onStartShift
                )
                SemanticTextField(
                    label = "Catatan Operasional",
                    value = state.operations.openingCashReason,
                    onValueChange = onOpeningCashReasonChanged,
                    helperText = "Wajib diisi bila opening cash di luar kebijakan normal.",
                    placeholder = "Contoh: butuh pecahan untuk jam sibuk",
                    leadingIcon = Icons.Default.EditNote,
                    imeAction = androidx.compose.ui.text.input.ImeAction.Done,
                    onImeAction = onStartShift
                )
                state.operations.blockingMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                Button(onClick = onStartShift, enabled = !state.isBusy, modifier = Modifier.fillMaxWidth().height(48.dp)) {
                    Text(if (state.isBusy) "Memulai..." else "Buka Kasir")
                }
            }
        }
    )
}

@Composable
fun OperationalDashboardCard(
    snapshot: OperationalControlSnapshot,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Control Tower", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(snapshot.headline, style = MaterialTheme.typography.bodyMedium)
            if (snapshot.pendingApprovalCount > 0) {
                Text(
                    text = "${snapshot.pendingApprovalCount} approval operasional menunggu keputusan.",
                    style = MaterialTheme.typography.bodySmall,
                    color = toneColor(UiTone.Warning)
                )
            }
            snapshot.salesHomeBlocker?.let {
                Text(
                    text = "Status kasir: $it",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                snapshot.decisions.forEach { decision ->
                    OperationDecisionRow(decision)
                }
            }
        }
    }
}

@Composable
fun CashControlDialog(
    state: OperationsState,
    onDismiss: () -> Unit,
    onTypeSelected: (CashMovementType) -> Unit,
    onAmountChanged: (String) -> Unit,
    onReasonCodeChanged: (String) -> Unit,
    onReasonDetailChanged: (String) -> Unit,
    onSubmit: () -> Unit,
    onApprove: (String) -> Unit,
    onDeny: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Kontrol Kas", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OperationalDashboardCard(state.dashboard)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CashMovementType.entries.forEach { type ->
                        FilterChip(
                            selected = state.cashMovementType == type,
                            onClick = { onTypeSelected(type) },
                            label = { Text(type.toUiLabel()) }
                        )
                    }
                }
                ShortcutNominalRow(
                    values = listOf("50000", "100000", "200000", "500000"),
                    onShortcutSelected = onAmountChanged
                )
                CassyCurrencyInput(
                    label = "Nominal",
                    value = state.cashMovementAmountInput,
                    onValueChange = onAmountChanged,
                    helperText = "Shift aktif wajib ada. Reason code harus valid."
                )
                ReasonOptionGroup(
                    title = "Pilih Alasan",
                    options = state.cashMovementReasonOptions,
                    selectedCode = state.cashMovementReasonCode,
                    onSelected = onReasonCodeChanged
                )
                FormField(
                    label = "Catatan",
                    value = state.cashMovementReasonDetail,
                    helperText = "Isi singkat konteks perpindahan kas."
                ) { onReasonDetailChanged(it) }
                if (state.pendingApprovals.isNotEmpty()) {
                    Text("Approval Menunggu", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    state.pendingApprovals.filter {
                        it.operationType == OperationType.CASH_IN ||
                            it.operationType == OperationType.CASH_OUT ||
                            it.operationType == OperationType.SAFE_DROP
                    }.forEach { approval ->
                        PendingApprovalRow(approval, onApprove, onDeny)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onSubmit) { Text("Simpan Kontrol Kas") }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Tutup") }
        }
    )
}

@Composable
fun CloseShiftWizardDialog(
    state: OperationsState,
    onDismiss: () -> Unit,
    onClosingCashChanged: (String) -> Unit,
    onReasonCodeChanged: (String) -> Unit,
    onReasonDetailChanged: (String) -> Unit,
    onSubmit: () -> Unit,
    onApprove: (String) -> Unit,
    onDeny: (String) -> Unit
) {
    val review = state.closeShiftReview
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Wizard Tutup Shift", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OperationalDashboardCard(state.dashboard)
                review?.let {
                    Surface(
                        tonalElevation = 1.dp,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Review Readiness", fontWeight = FontWeight.Bold)
                            Text(it.decision.message)
                            Text("Expected cash: Rp ${it.expectedCash.toInt()}")
                            Text("Cash sales: Rp ${it.cashSalesTotal.toInt()}")
                            Text("Cash in: Rp ${it.cashMovementTotals.cashInTotal.toInt()}")
                            Text("Cash out: Rp ${it.cashMovementTotals.cashOutTotal.toInt()}")
                            Text("Safe drop: Rp ${it.cashMovementTotals.safeDropTotal.toInt()}")
                            it.variance?.let { variance -> Text("Variance: Rp ${variance.toInt()}") }
                            if (it.pendingTransactions.isNotEmpty()) {
                                Text("Pending transaction:", fontWeight = FontWeight.Bold)
                                it.pendingTransactions.forEach { pending ->
                                    Text("${pending.localNumber} | Rp ${pending.amount.toInt()}")
                                }
                            }
                        }
                    }
                }
                CassyCurrencyInput(
                    label = "Closing Cash Aktual",
                    value = state.closingCashInput,
                    onValueChange = onClosingCashChanged,
                    helperText = "Hitung kas aktual dulu, baru review variance."
                )
                ReasonOptionGroup(
                    title = "Alasan Selisih",
                    options = state.closeShiftReasonOptions,
                    selectedCode = state.closeShiftReasonCode,
                    onSelected = onReasonCodeChanged
                )
                FormField(
                    label = "Catatan Selisih",
                    value = state.closeShiftReasonDetail,
                    helperText = "Wajib diisi bila variance perlu approval."
                ) { onReasonDetailChanged(it) }
                state.pendingApprovals.filter { it.operationType == OperationType.CLOSE_SHIFT }.forEach { approval ->
                    PendingApprovalRow(approval, onApprove, onDeny)
                }
            }
        },
        confirmButton = {
            Button(onClick = onSubmit) { Text("Eksekusi Tutup Shift") }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Kembali") }
        }
    )
}

@Composable
fun CloseDayReviewDialog(
    operations: OperationsState,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val closeDayDecision = operations.dashboard.decisions.firstOrNull { it.type == OperationType.CLOSE_BUSINESS_DAY }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Review Tutup Hari", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OperationalDashboardCard(operations.dashboard)
                closeDayDecision?.let {
                    Surface(
                        color = toneColor(
                            when (it.status) {
                                OperationStatus.READY -> UiTone.Success
                                OperationStatus.REQUIRES_APPROVAL -> UiTone.Warning
                                else -> UiTone.Danger
                            }
                        ).copy(alpha = 0.08f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(it.title, fontWeight = FontWeight.Bold)
                            Text(it.message)
                            Text("CTA: ${it.actionLabel ?: "Perbaiki blocker lalu review ulang"}")
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = closeDayDecision?.status == OperationStatus.READY
            ) { Text("Tutup Hari") }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Tinjau Lagi") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportingSummaryDialog(
    state: OperationsState,
    onDismiss: () -> Unit,
    onExport: () -> Unit,
    isBusy: Boolean = false
) {
    BasicAlertDialog(
        onDismissRequest = onDismiss,
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            tonalElevation = 4.dp,
            modifier = Modifier.widthIn(min = 920.dp, max = 1080.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Ringkasan Operasional", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Text(
                            "Snapshot lokal untuk owner/supervisor. Export menjaga truth terminal saat ini, bukan mirror backend.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    ShortcutHintBar(hints = listOf("F8 Buka", "Ctrl+E Export", "Esc Tutup"))
                }
                state.reportingSummary?.let { summary ->
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                        ReportingMetricTile(
                            title = "Penjualan Hari Ini",
                            primaryValue = "Rp ${summary.totalSales.toInt()}",
                            supporting = "${summary.transactionCount} transaksi | cash Rp ${summary.cashSalesTotal.toInt()}",
                            modifier = Modifier.weight(1f)
                        )
                        ReportingMetricTile(
                            title = "Kontrol Operasional",
                            primaryValue = "${summary.openShiftCount}/${summary.shiftCount} shift aktif",
                            supporting = "${summary.pendingApprovalCount} approval pending | net cash Rp ${summary.netCashMovement.toInt()}",
                            modifier = Modifier.weight(1f)
                        )
                        ReportingMetricTile(
                            title = "Status Sync",
                            primaryValue = summary.syncStatus.toUiLabel(),
                            supporting = "pending ${summary.syncStatus.pendingCount} | failed ${summary.syncStatus.failedCount}",
                            modifier = Modifier.weight(1f),
                            tone = when (summary.syncStatus.level) {
                                SyncLevel.HEALTHY -> UiTone.Success
                                SyncLevel.PENDING, SyncLevel.DELAYED -> UiTone.Warning
                                SyncLevel.STALLED, SyncLevel.ERROR -> UiTone.Danger
                            }
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                        StageSectionCard(title = "Readback Sync", modifier = Modifier.weight(1f)) {
                            ReportingKeyValue("Level", summary.syncStatus.toUiLabel())
                            ReportingKeyValue("Pending Event", summary.syncStatus.pendingCount.toString())
                            ReportingKeyValue("Failed Event", summary.syncStatus.failedCount.toString())
                            ReportingKeyValue("Sync Sukses Terakhir", summary.syncStatus.lastSyncAt?.toUiTimestamp() ?: "-")
                            ReportingKeyValue("Pending Tertua", summary.syncStatus.oldestPendingAt?.toUiTimestamp() ?: "-")
                            summary.syncStatus.lastErrorMessage?.let {
                                Text(
                                    text = "Error terakhir: $it",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (summary.syncStatus.level == SyncLevel.ERROR) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        StageSectionCard(title = "Shift Relevan", modifier = Modifier.weight(1f)) {
                            val shift = state.reportingShiftSummary
                            if (shift == null) {
                                Text(
                                    "Belum ada shift yang relevan untuk snapshot ini. Daily summary tetap bisa diekspor.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                ReportingKeyValue("Shift", shift.shiftId)
                                ReportingKeyValue("Operator", shift.operatorName)
                                ReportingKeyValue("Expected Cash", "Rp ${shift.expectedCash.toInt()}")
                                ReportingKeyValue("Variance", shift.variance?.let { "Rp ${it.toInt()}" } ?: "-")
                                ReportingKeyValue("Pending Transaksi", shift.pendingTransactionCount.toString())
                                ReportingKeyValue("Status", shift.status)
                            }
                        }
                    }

                    StageSectionCard(title = "Aturan Export & Output") {
                        Text(state.reportingExportRuleNote, style = MaterialTheme.typography.bodySmall)
                        ReportingKeyValue("Lokasi Terakhir", state.reportingExportPath ?: "-")
                        ReportingKeyValue("Diekspor Terakhir", state.reportingExportedAt?.toUiTimestamp() ?: "-")
                        Text(
                            "Bundle export berisi daily-summary.csv, shift-summary.csv, operational-issues.csv, dan README.html.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Text("Masalah & Tindakan", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    Box(modifier = Modifier.fillMaxWidth().heightIn(min = 240.dp, max = 360.dp)) {
                        OperationalIssueList(issues = summary.issues)
                    }
                } ?: Text("Data ringkasan tidak tersedia.")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss) { Text("Tutup") }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(onClick = onExport, enabled = !isBusy && state.reportingSummary != null) {
                        Text(if (isBusy) "Mengekspor..." else "Export Bundle CSV")
                    }
                }
            }
        }
    }
}

@Composable
private fun ReasonOptionGroup(
    title: String,
    options: List<ReasonOption>,
    selectedCode: String,
    onSelected: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        options.forEach { option ->
            FilterChip(
                selected = selectedCode == option.code,
                onClick = { onSelected(option.code) },
                label = { Text(option.title) }
            )
        }
    }
}

@Composable
private fun PendingApprovalRow(
    approval: id.azureenterprise.cassy.kernel.domain.PendingApprovalSummary,
    onApprove: (String) -> Unit,
    onDeny: (String) -> Unit
) {
    Surface(
        tonalElevation = 1.dp,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(approval.title, fontWeight = FontWeight.Bold)
            Text(approval.detail)
            approval.amount?.let { Text("Nominal: Rp ${it.toInt()}") }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { onDeny(approval.id) }, modifier = Modifier.weight(1f)) { Text("Tolak") }
                Button(onClick = { onApprove(approval.id) }, modifier = Modifier.weight(1f)) { Text("Setujui") }
            }
        }
    }
}

@Composable
private fun OperationDecisionRow(decision: OperationDecision) {
    val tone = when (decision.status) {
        OperationStatus.READY -> UiTone.Success
        OperationStatus.COMPLETED -> UiTone.Info
        OperationStatus.REQUIRES_APPROVAL -> UiTone.Warning
        OperationStatus.BLOCKED,
        OperationStatus.UNAVAILABLE -> UiTone.Danger
    }
    val icon = when (decision.status) {
        OperationStatus.READY,
        OperationStatus.COMPLETED -> Icons.Default.CheckCircle
        OperationStatus.REQUIRES_APPROVAL -> Icons.Default.Warning
        OperationStatus.BLOCKED,
        OperationStatus.UNAVAILABLE -> Icons.Default.Lock
    }
    Surface(
        color = toneColor(tone).copy(alpha = 0.08f),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OperationDecisionIcon(icon, tone)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(decision.title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Text(decision.message, style = MaterialTheme.typography.bodySmall)
            }
            Text(
                text = decision.actionLabel ?: decision.type.toShortLabel(),
                style = MaterialTheme.typography.labelSmall,
                color = toneColor(tone)
            )
        }
    }
}

@Composable
private fun OperationDecisionIcon(icon: ImageVector, tone: UiTone) {
    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = toneColor(tone)
    )
}

@Composable
private fun ShortcutNominalRow(
    values: List<String>,
    onShortcutSelected: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Shortcut nominal", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            values.take(2).forEach { value ->
                OutlinedButton(
                    onClick = { onShortcutSelected(value) },
                    modifier = Modifier.weight(1f).height(40.dp)
                ) {
                    Text(value.toShortcutLabel())
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            values.drop(2).forEach { value ->
                OutlinedButton(
                    onClick = { onShortcutSelected(value) },
                    modifier = Modifier.weight(1f).height(40.dp)
                ) {
                    Text(value.toShortcutLabel())
                }
            }
        }
    }
}

@Composable
fun CenterPanel(
    title: String,
    subtitle: String,
    contentWidth: androidx.compose.ui.unit.Dp = 480.dp,
    action: @Composable () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        ElevatedCard(modifier = Modifier.width(contentWidth), shape = RoundedCornerShape(24.dp)) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(32.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                Text(subtitle, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), textAlign = TextAlign.Center)
                action()
            }
        }
    }
}

@Composable
private fun FormField(
    label: String,
    value: String,
    modifier: Modifier = Modifier.fillMaxWidth(),
    masked: Boolean = false,
    helperText: String? = null,
    onValueChange: (String) -> Unit
) {
    SemanticTextField(
        label = label,
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        helperText = helperText,
        visualTransformation = if (masked) androidx.compose.ui.text.input.PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None
    )
}

@Composable
fun BannerCard(banner: UiBanner, onDismiss: () -> Unit) {
    ElevatedCard(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = toneContainerColor(banner.tone)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(banner.message, modifier = Modifier.weight(1f))
            IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = "Close") }
        }
    }
}

@Composable
private fun StageSectionCard(
    title: String,
    modifier: Modifier = Modifier.fillMaxWidth(),
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        tonalElevation = 1.dp,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                content()
            }
        )
    }
}

@Composable
private fun ReportingMetricTile(
    title: String,
    primaryValue: String,
    supporting: String,
    modifier: Modifier = Modifier,
    tone: UiTone = UiTone.Info
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = toneContainerColor(tone)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(title, style = MaterialTheme.typography.labelLarge, color = toneColor(tone))
            Text(primaryValue, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
            Text(supporting, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ReportingKeyValue(
    label: String,
    value: String
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.End)
    }
}

private fun OperationType.toShortLabel(): String = when (this) {
    OperationType.OPEN_BUSINESS_DAY -> "Open Day"
    OperationType.START_SHIFT -> "Start Shift"
    OperationType.CASH_IN -> "Cash In"
    OperationType.CASH_OUT -> "Cash Out"
    OperationType.SAFE_DROP -> "Safe Drop"
    OperationType.CLOSE_SHIFT -> "Close Shift"
    OperationType.CLOSE_BUSINESS_DAY -> "Close Day"
    OperationType.VOID_SALE -> "Void"
    OperationType.STOCK_ADJUSTMENT -> "Stock Adj"
    OperationType.RESOLVE_STOCK_DISCREPANCY -> "Resolve Diff"
}

private fun CashMovementType.toUiLabel(): String = when (this) {
    CashMovementType.CASH_IN -> "Cash In"
    CashMovementType.CASH_OUT -> "Cash Out"
    CashMovementType.SAFE_DROP -> "Safe Drop"
}

private fun String.toShortcutLabel(): String = when (this) {
    "100000" -> "Rp 100rb"
    "200000" -> "Rp 200rb"
    "500000" -> "Rp 500rb"
    "1000000" -> "Rp 1jt"
    else -> "Rp $this"
}

private fun SyncStatus.toUiLabel(): String = when (level) {
    SyncLevel.HEALTHY -> "Sehat"
    SyncLevel.PENDING -> "Menunggu"
    SyncLevel.DELAYED -> "Terlambat"
    SyncLevel.STALLED -> "Macet"
    SyncLevel.ERROR -> "Error"
}

private fun Instant.toUiTimestamp(): String = toString()
