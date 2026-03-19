package id.azureenterprise.cassy.desktop

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import id.azureenterprise.cassy.kernel.domain.OperationDecision
import id.azureenterprise.cassy.kernel.domain.OperationStatus
import id.azureenterprise.cassy.kernel.domain.OperationType
import id.azureenterprise.cassy.kernel.domain.OperationalControlSnapshot

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
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Pengaturan Awal Toko", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("Terminal adalah perangkat komputer ini yang akan digunakan untuk transaksi.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        }
        item {
            FormField(
                label = "Nama Toko",
                value = state.bootstrap.storeName,
                helperText = "Nama bisnis Anda (misal: Toko Berkah Jaya)"
            ) { onFieldChanged(BootstrapField.StoreName, it) }
        }
        item {
            FormField(
                label = "ID Terminal / Kasir",
                value = state.bootstrap.terminalName,
                helperText = "Nama unik komputer ini (misal: Kasir-01)"
            ) { onFieldChanged(BootstrapField.TerminalName, it) }
        }
        item { FormField("Nama Kasir", state.bootstrap.cashierName) { onFieldChanged(BootstrapField.CashierName, it) } }
        item { FormField("PIN Kasir (6 digit)", state.bootstrap.cashierPin, masked = true) { onFieldChanged(BootstrapField.CashierPin, it) } }
        item { FormField("Nama Supervisor", state.bootstrap.supervisorName) { onFieldChanged(BootstrapField.SupervisorName, it) } }
        item { FormField("PIN Supervisor (6 digit)", state.bootstrap.supervisorPin, masked = true) { onFieldChanged(BootstrapField.SupervisorPin, it) } }
        item {
            Button(onClick = onBootstrap, enabled = !state.isBusy, modifier = Modifier.fillMaxWidth().height(48.dp)) {
                Text(if (state.isBusy) "Menyimpan..." else "Simpan Pengaturan")
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
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Pilih Operator", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            state.login.operators.forEach { option ->
                val selected = state.login.selectedOperatorId == option.id
                ElevatedCard(
                    modifier = Modifier.width(200.dp),
                    shape = RoundedCornerShape(16.dp),
                    onClick = { onSelectOperator(option.id) },
                    colors = if (selected) CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer) else CardDefaults.elevatedCardColors()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(option.displayName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text(option.roleLabel, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
        OutlinedTextField(
            value = state.login.pin,
            onValueChange = onPinChanged,
            label = { Text("PIN Operator") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.width(240.dp),
            singleLine = true
        )
        state.login.feedback?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Button(onClick = onLogin, enabled = !state.isBusy, modifier = Modifier.width(240.dp).height(48.dp)) {
            Text(if (state.isBusy) "Memproses..." else "Masuk")
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
        action = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OperationalDashboardCard(state.operations.dashboard)
                ShortcutNominalRow(
                    values = listOf("100000", "200000", "500000", "1000000"),
                    onShortcutSelected = onShortcutSelected
                )
                CassyCurrencyInput(
                    label = "Modal Awal Tunai",
                    value = state.operations.openingCashInput,
                    onValueChange = onOpeningCashChanged,
                    helperText = "Jumlah uang tunai yang ada di laci kas saat ini.",
                    onImeAction = onStartShift
                )
                FormField(
                    label = "Alasan / Catatan Operasional",
                    value = state.operations.openingCashReason,
                    helperText = "Wajib diisi bila opening cash di luar kebijakan."
                ) { onOpeningCashReasonChanged(it) }
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
fun CenterPanel(title: String, subtitle: String, action: @Composable () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        ElevatedCard(modifier = Modifier.width(480.dp), shape = RoundedCornerShape(24.dp)) {
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
    Column(modifier = modifier) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            singleLine = true,
            visualTransformation = if (masked) PasswordVisualTransformation() else VisualTransformation.None
        )
        if (helperText != null) {
            Text(
                text = helperText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
            )
        }
    }
}

@Composable
fun BannerCard(banner: UiBanner, onDismiss: () -> Unit) {
    ElevatedCard(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = when(banner.tone) {
                UiTone.Danger -> MaterialTheme.colorScheme.errorContainer
                UiTone.Warning -> Color(0xFFFFF3E0)
                else -> MaterialTheme.colorScheme.secondaryContainer
            }
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

private fun OperationType.toShortLabel(): String = when (this) {
    OperationType.OPEN_BUSINESS_DAY -> "Open Day"
    OperationType.START_SHIFT -> "Start Shift"
    OperationType.VOID_SALE -> "Void"
}

private fun String.toShortcutLabel(): String = when (this) {
    "100000" -> "Rp 100rb"
    "200000" -> "Rp 200rb"
    "500000" -> "Rp 500rb"
    "1000000" -> "Rp 1jt"
    else -> "Rp $this"
}
