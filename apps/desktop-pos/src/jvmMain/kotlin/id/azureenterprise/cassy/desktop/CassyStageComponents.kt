package id.azureenterprise.cassy.desktop

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import id.azureenterprise.cassy.sales.domain.SaleHistoryEntry
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
    onSelectAvatar: (BootstrapField) -> Unit,
    onClearAvatar: (BootstrapField) -> Unit,
    onBootstrap: () -> Unit
) {
    val readinessItems = listOf(
        BootstrapReadinessItem(
            title = "Nama toko",
            detail = "Nama yang muncul di struk dan laporan.",
            isReady = state.bootstrap.storeName.isNotBlank()
        ),
        BootstrapReadinessItem(
            title = "Nama terminal",
            detail = "Penanda perangkat kasir utama.",
            isReady = state.bootstrap.terminalName.isNotBlank()
        ),
        BootstrapReadinessItem(
            title = "Kasir awal",
            detail = "Nama kasir dan PIN 6 digit harus lengkap.",
            isReady = state.bootstrap.cashierName.isNotBlank() && state.bootstrap.cashierPin.length == 6
        ),
        BootstrapReadinessItem(
            title = "Supervisor awal",
            detail = "Nama supervisor dan PIN 6 digit harus lengkap.",
            isReady = state.bootstrap.supervisorName.isNotBlank() && state.bootstrap.supervisorPin.length == 6
        )
    )
    val completedCount = readinessItems.count { it.isReady }

    Box(modifier = Modifier.fillMaxSize().padding(horizontal = 28.dp, vertical = 24.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().widthIn(max = 1460.dp).align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.width(278.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Surface(
                    tonalElevation = 1.dp,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Pengaturan awal toko", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Text(
                            "Selesaikan data dasar ini sekali saja agar terminal bisa dipakai operasional harian tanpa kebingungan.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f)
                        ) {
                            Text(
                                "$completedCount dari ${readinessItems.size} langkah selesai",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                StageSectionCard(title = "Yang wajib selesai") {
                    readinessItems.forEach { item ->
                        BootstrapReadinessRow(item)
                    }
                }

                Surface(
                    tonalElevation = 0.dp,
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Setelah disimpan", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text("1. Layar akan pindah ke login operator lokal.", style = MaterialTheme.typography.bodySmall)
                        Text("2. Kasir dapat membuka hari bisnis saat mulai operasional.", style = MaterialTheme.typography.bodySmall)
                        Text("3. Kasir membuka shift setelah modal awal siap.", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Surface(
                modifier = Modifier.weight(1f),
                tonalElevation = 1.dp,
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(22.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("Isi data terminal utama", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text(
                                "Fokus dulu ke identitas toko, lalu operator awal. Tidak perlu memikirkan hari bisnis, shift, atau perangkat di tahap ini.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            ShortcutHintBar(
                                hints = listOf("Tab pindah field", "PIN 6 digit", "Enter simpan"),
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        StageSectionCard(
                            title = "Identitas toko dan terminal",
                            modifier = Modifier.weight(1f)
                        ) {
                            SemanticTextField(
                                label = "Nama Toko",
                                value = state.bootstrap.storeName,
                                onValueChange = { onFieldChanged(BootstrapField.StoreName, it) },
                                helperText = "Nama ini akan muncul di struk dan laporan harian.",
                                errorText = state.bootstrap.visibleError(BootstrapField.StoreName),
                                placeholder = "Contoh: Toko Berkah Jaya",
                                leadingIcon = Icons.Default.Storefront
                            )
                            SemanticTextField(
                                label = "Nama Terminal",
                                value = state.bootstrap.terminalName,
                                onValueChange = { onFieldChanged(BootstrapField.TerminalName, it) },
                                helperText = "Gunakan nama perangkat yang mudah dikenali, misalnya Kasir-01.",
                                errorText = state.bootstrap.visibleError(BootstrapField.TerminalName),
                                placeholder = "Contoh: Kasir-01",
                                leadingIcon = Icons.Default.PointOfSale
                            )
                        }

                        StageSectionCard(
                            title = "Preview identitas",
                            modifier = Modifier.width(296.dp)
                        ) {
                            BootstrapPreviewRow("Nama toko", state.bootstrap.storeName.ifBlank { "Belum diisi" })
                            BootstrapPreviewRow("Nama terminal", state.bootstrap.terminalName.ifBlank { "Belum diisi" })
                            BootstrapPreviewRow(
                                "Nama kasir",
                                state.bootstrap.cashierName.ifBlank { "Belum diisi" }
                            )
                            BootstrapPreviewRow(
                                "Nama supervisor",
                                state.bootstrap.supervisorName.ifBlank { "Belum diisi" }
                            )
                        }
                    }

                    StageSectionCard(title = "Operator awal") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            BootstrapRoleCard(
                                title = "Kasir",
                                detail = "Peran utama untuk transaksi, buka hari bisnis, buka shift, dan kontrol kas harian.",
                                avatarPath = state.bootstrap.cashierAvatarPath,
                                onSelectAvatar = { onSelectAvatar(BootstrapField.CashierAvatar) },
                                onClearAvatar = { onClearAvatar(BootstrapField.CashierAvatar) },
                                modifier = Modifier.weight(1f)
                            ) {
                                SemanticTextField(
                                    label = "Nama Kasir",
                                    value = state.bootstrap.cashierName,
                                    onValueChange = { onFieldChanged(BootstrapField.CashierName, it) },
                                    helperText = "Nama operator yang dipakai untuk transaksi harian.",
                                    errorText = state.bootstrap.visibleError(BootstrapField.CashierName),
                                    placeholder = "Contoh: Rani",
                                    leadingIcon = Icons.Default.Person
                                )
                                SemanticPinField(
                                    label = "PIN Kasir",
                                    value = state.bootstrap.cashierPin,
                                    onValueChange = { onFieldChanged(BootstrapField.CashierPin, it) },
                                    helperText = "Wajib 6 digit numerik.",
                                    errorText = state.bootstrap.visibleError(BootstrapField.CashierPin)
                                )
                            }

                            BootstrapRoleCard(
                                title = "Supervisor",
                                detail = "Memantau hasil, memberi approval penting, dan menangani pemulihan bila ada masalah.",
                                avatarPath = state.bootstrap.supervisorAvatarPath,
                                onSelectAvatar = { onSelectAvatar(BootstrapField.SupervisorAvatar) },
                                onClearAvatar = { onClearAvatar(BootstrapField.SupervisorAvatar) },
                                modifier = Modifier.weight(1f)
                            ) {
                                SemanticTextField(
                                    label = "Nama Supervisor",
                                    value = state.bootstrap.supervisorName,
                                    onValueChange = { onFieldChanged(BootstrapField.SupervisorName, it) },
                                    helperText = "Supervisor dipakai untuk approval penting dan pemulihan operasional.",
                                    errorText = state.bootstrap.visibleError(BootstrapField.SupervisorName),
                                    placeholder = "Contoh: Bayu",
                                    leadingIcon = Icons.Default.Badge
                                )
                                SemanticPinField(
                                    label = "PIN Supervisor",
                                    value = state.bootstrap.supervisorPin,
                                    onValueChange = { onFieldChanged(BootstrapField.SupervisorPin, it) },
                                    helperText = "Simpan hanya ke orang yang berwenang.",
                                    errorText = state.bootstrap.visibleError(BootstrapField.SupervisorPin),
                                    onImeAction = onBootstrap
                                )
                            }
                        }
                    }

                    Surface(
                        tonalElevation = 0.dp,
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("Catatan operasional", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text(
                                "Logo toko, alamat, telepon, dan catatan struk belum wajib di tahap ini. Fokus dulu agar terminal bisa dipakai dan operator bisa masuk.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Button(
                        onClick = onBootstrap,
                        enabled = !state.isBusy,
                        modifier = Modifier.fillMaxWidth().height(52.dp)
                    ) {
                        Text(if (state.isBusy) "Menyimpan..." else "Simpan dan lanjut ke login")
                    }
                }
            }
        }
    }
}

@Composable
fun LoginStage(
    state: DesktopAppState,
    onSelectOperator: (String) -> Unit,
    onPinChanged: (String) -> Unit,
    onLogin: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize().padding(horizontal = 36.dp, vertical = 28.dp)) {
        Surface(
            modifier = Modifier.fillMaxWidth().widthIn(max = 1120.dp).align(Alignment.TopCenter),
            tonalElevation = 1.dp,
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
                        Text("Masuk operator", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Text(
                            "Pilih operator yang aktif di terminal ini. Hak akses kasir dan supervisor dibedakan secara tegas.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    ShortcutHintBar(hints = listOf("Klik operator", "PIN 6 digit", "Enter masuk"))
                }

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                    state.login.operators.forEach { option ->
                        val selected = state.login.selectedOperatorId == option.id
                        LoginOperatorCard(
                            option = option,
                            selected = selected,
                            onClick = { onSelectOperator(option.id) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.64f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SemanticPinField(
                            label = "PIN Operator",
                            value = state.login.pin,
                            onValueChange = onPinChanged,
                            modifier = Modifier.fillMaxWidth(),
                            helperText = "PIN diverifikasi lokal di perangkat ini.",
                            errorText = state.login.feedback,
                            onImeAction = onLogin
                        )
                        Button(onClick = onLogin, enabled = !state.isBusy, modifier = Modifier.fillMaxWidth().height(50.dp)) {
                            Text(if (state.isBusy) "Memproses..." else "Masuk ke terminal")
                        }
                    }
                }
            }
        }
    }
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
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Ringkasan kesiapan", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
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
                        shape = RoundedCornerShape(10.dp),
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
                            supporting = "${summary.transactionCount} transaksi | void ${summary.voidedSaleCount} / Rp ${summary.voidedSalesTotal.toInt()}",
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
                        ReportingKeyValue("Void Tercatat", "${summary.voidedSaleCount} transaksi")
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoidSaleDialog(
    voidState: VoidSaleState,
    recentSales: List<SaleHistoryEntry>,
    onDismiss: () -> Unit,
    onSelectSale: (String) -> Unit,
    onReasonCodeChanged: (String) -> Unit,
    onReasonDetailChanged: (String) -> Unit,
    onInventoryFollowUpChanged: (String) -> Unit,
    onConfirm: () -> Unit,
    isBusy: Boolean = false
) {
    BasicAlertDialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            tonalElevation = 4.dp,
            modifier = Modifier.widthIn(min = 920.dp, max = 1080.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Void Penjualan", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Text(
                            "Jalur ini hanya untuk sale CASH yang sudah final. Refund kas dicatat eksplisit, stok tidak dibalik otomatis.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    ShortcutHintBar(hints = listOf("F7 Buka", "Review Refund", "Esc Tutup"))
                }

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                    StageSectionCard(title = "Pilih Transaksi", modifier = Modifier.weight(1f)) {
                        if (recentSales.isEmpty()) {
                            Text(
                                "Belum ada transaksi recent yang bisa direview.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxWidth().heightIn(min = 220.dp, max = 300.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(recentSales) { sale ->
                                    ElevatedCard(
                                        onClick = { onSelectSale(sale.saleId) },
                                        shape = RoundedCornerShape(14.dp),
                                        colors = if (voidState.selectedSaleId == sale.saleId) {
                                            CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                                        } else {
                                            CardDefaults.elevatedCardColors()
                                        }
                                    ) {
                                        Column(
                                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text("${sale.localNumber} | ${sale.paymentMethod}", fontWeight = FontWeight.Bold)
                                            Text(
                                                "Rp ${sale.finalAmount.toInt()} | ${sale.saleStatus.name} | ${sale.voidedAtEpochMs?.let { "voided" } ?: "normal"}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    StageSectionCard(title = "Assessment Void", modifier = Modifier.weight(1f)) {
                        ReportingKeyValue("Sale", voidState.selectedLocalNumber ?: "-")
                        ReportingKeyValue("Metode Bayar", voidState.selectedPaymentMethod ?: "-")
                        ReportingKeyValue("Status", voidState.selectedSaleStatus ?: "-")
                        ReportingKeyValue(
                            "Nominal",
                            voidState.selectedAmount?.let { "Rp ${it.toInt()}" } ?: "-"
                        )
                        ReportingKeyValue("Inventory", voidState.inventoryImpactClassification)
                        Text(
                            voidState.assessmentMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (voidState.canExecute) toneColor(UiTone.Warning) else MaterialTheme.colorScheme.error
                        )
                    }
                }

                StageSectionCard(title = "Alasan & Follow-up") {
                    ReasonOptionGroup(
                        title = "Alasan pembatalan",
                        options = voidState.reasonOptions,
                        selectedCode = voidState.reasonCode,
                        onSelected = onReasonCodeChanged
                    )
                    SemanticTextField(
                        label = "Catatan Void",
                        value = voidState.reasonDetail,
                        onValueChange = onReasonDetailChanged,
                        helperText = "Jelaskan konteks koreksi transaksi secara singkat dan bisa diaudit.",
                        placeholder = "Contoh: double input sebelum pelanggan pergi",
                        leadingIcon = Icons.Default.EditNote
                    )
                    SemanticTextField(
                        label = "Catatan Follow-up Stok",
                        value = voidState.inventoryFollowUpNote,
                        onValueChange = onInventoryFollowUpChanged,
                        helperText = "Tidak mengembalikan stok otomatis. Gunakan kolom ini untuk instruksi investigasi atau follow-up manual.",
                        placeholder = "Contoh: barang masih di meja kasir, cek fisik sebelum adjustment terpisah",
                        leadingIcon = Icons.Default.Inventory2,
                        singleLine = false
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    OutlinedButton(onClick = onDismiss) { Text("Tutup") }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = onConfirm,
                        enabled = !isBusy && voidState.canExecute && voidState.reasonCode.isNotBlank()
                    ) {
                        Text(if (isBusy) "Memproses..." else "Eksekusi Void")
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
fun OperationDecisionRow(decision: OperationDecision) {
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
                Text(humanizeOperationDecisionTitle(decision), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Text(humanizeDecisionMessage(decision.message), style = MaterialTheme.typography.bodySmall)
            }
            Text(
                text = decision.actionLabel?.replace("Business Day", "hari bisnis") ?: decision.type.toShortLabel(),
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
    val tone = banner.tone
    val icon = when (tone) {
        UiTone.Info -> Icons.Default.Info
        UiTone.Success -> Icons.Default.CheckCircle
        UiTone.Warning -> Icons.Default.Warning
        UiTone.Danger -> Icons.Default.Error
    }
    val title = when (tone) {
        UiTone.Info -> "Info"
        UiTone.Success -> "Berhasil"
        UiTone.Warning -> "Perhatian"
        UiTone.Danger -> "Masalah Operasional"
    }
    val message = banner.message.ifBlank {
        when (tone) {
            UiTone.Info -> "Ada pembaruan status, tetapi detail pesan tidak tersedia."
            UiTone.Success -> "Aksi selesai, tetapi detail pesan tidak tersedia."
            UiTone.Warning -> "Perlu perhatian operator, tetapi detail pesan tidak tersedia."
            UiTone.Danger -> "Terjadi masalah, tetapi detail pesan tidak tersedia."
        }
    }
    ElevatedCard(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = toneContainerColor(banner.tone)
        ),
        modifier = Modifier.widthIn(min = 320.dp, max = 440.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(icon, contentDescription = null, tint = toneContentColor(tone), modifier = Modifier.padding(top = 2.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, fontWeight = FontWeight.Bold, color = toneContentColor(tone))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 3,
                    color = toneContentColor(tone)
                )
            }
            IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Tutup", tint = toneContentColor(tone))
            }
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
        shape = RoundedCornerShape(12.dp),
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

private data class BootstrapReadinessItem(
    val title: String,
    val detail: String,
    val isReady: Boolean
)

@Composable
private fun BootstrapReadinessRow(item: BootstrapReadinessItem) {
    val icon = if (item.isReady) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked
    Surface(
        color = if (item.isReady) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f)
        },
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (item.isReady) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(item.title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Text(
                    item.detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun BootstrapRoleCard(
    title: String,
    detail: String,
    avatarPath: String?,
    onSelectAvatar: () -> Unit,
    onClearAvatar: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BootstrapAvatarPlaceholder(
                    title = title,
                    avatarPath = avatarPath
                )
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        detail,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = onSelectAvatar, modifier = Modifier.weight(1f)) { Text("Pilih foto") }
                OutlinedButton(
                    onClick = onClearAvatar,
                    enabled = avatarPath != null,
                    modifier = Modifier.weight(1f)
                ) { Text("Hapus foto") }
            }

            avatarPath?.let {
                Text(
                    "File lokal: ${java.io.File(it).name}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            content()
        }
    }
}

@Composable
private fun BootstrapAvatarPlaceholder(
    title: String,
    avatarPath: String?
) {
    ManagedAvatarPreview(
        imagePath = avatarPath,
        fallbackLabel = title,
        contentDescription = "Foto $title",
        size = 72.dp
    )
}

@Composable
private fun BootstrapPreviewRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun LoginOperatorCard(
    option: OperatorOption,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        onClick = onClick,
        colors = if (selected) {
            CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.76f))
        } else {
            CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.52f))
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BootstrapAvatarPlaceholder(
                    title = option.displayName.take(1).ifBlank { option.roleLabel.take(1) },
                    avatarPath = option.avatarPath
                )
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(option.displayName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text(
                        option.roleLabel.roleUiLabel(),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                option.capabilitySummary,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                if (selected) "Siap login di terminal ini" else "Pilih operator ini untuk melanjutkan",
                style = MaterialTheme.typography.bodySmall,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
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
    OperationType.OPEN_BUSINESS_DAY -> "Buka Hari"
    OperationType.START_SHIFT -> "Buka Shift"
    OperationType.CASH_IN -> "Uang Masuk"
    OperationType.CASH_OUT -> "Uang Keluar"
    OperationType.SAFE_DROP -> "Simpan Kas"
    OperationType.CLOSE_SHIFT -> "Tutup Shift"
    OperationType.CLOSE_BUSINESS_DAY -> "Tutup Hari"
    OperationType.VOID_SALE -> "Void"
    OperationType.STOCK_ADJUSTMENT -> "Adj. Stok"
    OperationType.RESOLVE_STOCK_DISCREPANCY -> "Selisih Stok"
}

private fun humanizeOperationDecisionTitle(decision: OperationDecision): String = when (decision.type) {
    OperationType.OPEN_BUSINESS_DAY -> "Buka hari bisnis"
    OperationType.START_SHIFT -> "Buka shift"
    OperationType.CASH_IN -> "Catat uang masuk"
    OperationType.CASH_OUT -> "Catat uang keluar"
    OperationType.SAFE_DROP -> "Simpan uang ke brankas"
    OperationType.CLOSE_SHIFT -> "Tutup shift"
    OperationType.CLOSE_BUSINESS_DAY -> "Tutup hari"
    OperationType.VOID_SALE -> "Batalkan transaksi final"
    OperationType.STOCK_ADJUSTMENT -> "Sesuaikan stok"
    OperationType.RESOLVE_STOCK_DISCREPANCY -> "Tindak lanjuti selisih stok"
}

private fun humanizeDecisionMessage(message: String): String {
    return message
        .replace("Business day", "Hari bisnis")
        .replace("business day", "hari bisnis")
        .replace("Close day", "Tutup hari")
        .replace("Cash In", "uang masuk")
        .replace("Cash Out", "uang keluar")
        .replace("Safe Drop", "simpan uang ke brankas")
        .replace("Reason Code", "alasan operasional")
}

private fun CashMovementType.toUiLabel(): String = when (this) {
    CashMovementType.CASH_IN -> "Uang Masuk"
    CashMovementType.CASH_OUT -> "Uang Keluar"
    CashMovementType.SAFE_DROP -> "Simpan Kas"
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

private fun BootstrapState.visibleError(field: BootstrapField): String? {
    return fieldErrors[field]?.takeIf { submitAttempted || field in touchedFields }
}

private fun String.roleUiLabel(): String = when (uppercase()) {
    "CASHIER" -> "Kasir"
    "SUPERVISOR" -> "Supervisor"
    "OWNER" -> "Owner"
    else -> this
}
