package id.azureenterprise.cassy.desktop

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

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
        subtitle = state.operations.blockingMessage ?: "Tekan tombol di bawah untuk membuka operasional toko hari ini.",
        action = {
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
    )
}

@Composable
fun StartShiftStage(
    state: DesktopAppState,
    onOpeningCashChanged: (String) -> Unit,
    onStartShift: () -> Unit
) {
    CenterPanel(
        title = "Buka Kasir",
        subtitle = "Masukkan saldo kas awal (Modal Awal) sebelum memulai transaksi.",
        action = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                CassyCurrencyInput(
                    label = "Modal Awal Tunai",
                    value = state.operations.openingCashInput,
                    onValueChange = onOpeningCashChanged,
                    helperText = "Jumlah uang tunai yang ada di laci kas saat ini.",
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
