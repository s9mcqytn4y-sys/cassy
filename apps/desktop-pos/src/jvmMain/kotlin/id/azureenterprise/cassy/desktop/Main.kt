package id.azureenterprise.cassy.desktop

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import id.azureenterprise.cassy.masterdata.domain.Product
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import java.io.File
import kotlin.system.exitProcess

fun main(args: Array<String>) = application {
    val smokeMode = args.contains("--smoke-run")
    val smokeMarkerPath = System.getenv("CASSY_SMOKE_MARKER")
    startDesktopKoin()
    println("Cassy desktop runtime Java ${System.getProperty("java.version")} | smokeMode=$smokeMode")

    Window(
        onCloseRequest = ::exitApplication,
        title = "Cassy Foundation Desktop"
    ) {
        CassyDesktopTheme {
            val controller: DesktopAppController = koinInject()
            val state by controller.state.collectAsState()
            val scope = rememberCoroutineScope()

            LaunchedEffect(Unit) {
                controller.load()
            }

            LaunchedEffect(smokeMode, state.stage) {
                if (!smokeMode || state.stage == DesktopStage.Loading) return@LaunchedEffect
                delay(300)
                when (val stage = state.stage) {
                    is DesktopStage.FatalError -> {
                        smokeMarkerPath?.let { writeSmokeMarker(it, "FAILED stage=${stage.message}") }
                        System.err.println("CASSY_SMOKE_FAILED stage=${stage.message}")
                        exitApplication()
                        exitProcess(1)
                    }
                    else -> {
                        smokeMarkerPath?.let { writeSmokeMarker(it, "OK stage=${state.stage::class.simpleName}") }
                        println("CASSY_SMOKE_OK stage=${state.stage::class.simpleName}")
                        exitApplication()
                    }
                }
            }

            Surface(modifier = Modifier.fillMaxSize()) {
                Row(modifier = Modifier.fillMaxSize()) {
                    ShellRail(
                        state = state,
                        onReload = { scope.launch { controller.load() } },
                        onLogout = { scope.launch { controller.logout() } },
                        onDismissBanner = controller::dismissBanner
                    )
                    HorizontalDivider(modifier = Modifier.fillMaxHeight().width(1.dp))
                    when (val stage = state.stage) {
                        DesktopStage.Loading -> LoadingStage()
                        DesktopStage.Bootstrap -> BootstrapStage(
                            state = state,
                            onFieldChanged = controller::updateBootstrapField,
                            onBootstrap = { scope.launch { controller.bootstrapStore() } }
                        )
                        DesktopStage.Login -> LoginStage(
                            state = state,
                            onSelectOperator = controller::selectOperator,
                            onPinChanged = controller::updatePin,
                            onLogin = { scope.launch { controller.login() } }
                        )
                        DesktopStage.OpenDay -> OpenDayStage(
                            state = state,
                            onOpenDay = { scope.launch { controller.openBusinessDay() } },
                            onLogout = { scope.launch { controller.logout() } }
                        )
                        DesktopStage.StartShift -> StartShiftStage(
                            state = state,
                            onOpeningCashChanged = controller::updateOpeningCashInput,
                            onStartShift = { scope.launch { controller.startShift() } }
                        )
                        DesktopStage.Catalog -> CatalogStage(
                            state = state,
                            onSearchChanged = { value -> scope.launch { controller.updateCatalogQuery(value) } },
                            onBarcodeChanged = controller::updateBarcodeInput,
                            onScanBarcode = { scope.launch { controller.scanBarcodeOrSku() } },
                            onAddProduct = { product -> scope.launch { controller.addProduct(product) } },
                            onIncrement = { product -> scope.launch { controller.incrementItem(product) } },
                            onDecrement = { product, quantity -> scope.launch { controller.decrementItem(product, quantity) } },
                            onClosingCashChanged = controller::updateClosingCashInput,
                            onEndShift = { scope.launch { controller.endShift() } },
                            onCloseDay = { scope.launch { controller.closeBusinessDay() } }
                        )
                        is DesktopStage.FatalError -> FatalStage(
                            message = stage.message,
                            onRetry = { scope.launch { controller.load() } }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ShellRail(
    state: DesktopAppState,
    onReload: () -> Unit,
    onLogout: () -> Unit,
    onDismissBanner: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(280.dp)
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("CASSY", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Desktop-first retail operating core", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f))
        MetricBlock("Store", state.shell.storeName ?: "Belum terikat")
        MetricBlock("Terminal", state.shell.terminalName ?: "Belum terikat")
        MetricBlock("Operator", state.shell.operatorName ?: "Belum login")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatusChip("Day ${state.shell.dayStatus}", if (state.shell.dayStatus == "OPEN") UiTone.Success else UiTone.Warning)
            StatusChip("Shift ${state.shell.shiftStatus}", if (state.shell.shiftStatus == "OPEN") UiTone.Success else UiTone.Warning)
        }
        state.banner?.let { banner ->
            BannerCard(banner = banner, onDismiss = onDismissBanner)
        }
        Spacer(modifier = Modifier.weight(1f))
        OutlinedButton(onClick = onReload, modifier = Modifier.fillMaxWidth()) {
            Text("Refresh State")
        }
        TextButton(onClick = onLogout, modifier = Modifier.fillMaxWidth()) {
            Text("Logout")
        }
    }
}

@Composable
private fun LoadingStage() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun FatalStage(message: String, onRetry: () -> Unit) {
    CenterPanel(
        title = "State desktop gagal dimuat",
        subtitle = message,
        action = {
            Button(onClick = onRetry) { Text("Coba Lagi") }
        }
    )
}

@Composable
private fun BootstrapStage(
    state: DesktopAppState,
    onFieldChanged: (BootstrapField, String) -> Unit,
    onBootstrap: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Bootstrap Store", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("Setup gate minimum sebelum operator boleh masuk ke flow kasir.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f))
        }
        item { FormField("Nama Toko", state.bootstrap.storeName) { onFieldChanged(BootstrapField.StoreName, it) } }
        item { FormField("Nama Terminal", state.bootstrap.terminalName) { onFieldChanged(BootstrapField.TerminalName, it) } }
        item { FormField("Nama Kasir", state.bootstrap.cashierName) { onFieldChanged(BootstrapField.CashierName, it) } }
        item { FormField("PIN Kasir (6 digit)", state.bootstrap.cashierPin, masked = true) { onFieldChanged(BootstrapField.CashierPin, it) } }
        item { FormField("Nama Supervisor", state.bootstrap.supervisorName) { onFieldChanged(BootstrapField.SupervisorName, it) } }
        item { FormField("PIN Supervisor (6 digit)", state.bootstrap.supervisorPin, masked = true) { onFieldChanged(BootstrapField.SupervisorPin, it) } }
        item {
            Button(onClick = onBootstrap, enabled = !state.isBusy, modifier = Modifier.fillMaxWidth()) {
                Text(if (state.isBusy) "Menyimpan..." else "Simpan Bootstrap")
            }
        }
    }
}

@Composable
private fun LoginStage(
    state: DesktopAppState,
    onSelectOperator: (String) -> Unit,
    onPinChanged: (String) -> Unit,
    onLogin: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(28.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Text("Login / PIN", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Pilih operator lalu masukkan PIN lokal. Tidak ada bypass ke flow berikutnya.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            state.login.operators.forEach { option ->
                ElevatedCard(
                    modifier = Modifier.width(180.dp),
                    shape = RoundedCornerShape(18.dp),
                    onClick = { onSelectOperator(option.id) }
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(option.displayName, fontWeight = FontWeight.SemiBold)
                        StatusChip(option.roleLabel, UiTone.Info)
                        Text(if (state.login.selectedOperatorId == option.id) "Dipilih" else "Klik untuk memilih")
                    }
                }
            }
        }
        FormField("PIN Operator", state.login.pin, masked = true, onValueChange = onPinChanged)
        state.login.feedback?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Button(onClick = onLogin, enabled = !state.isBusy) {
            Text(if (state.isBusy) "Memverifikasi..." else "Masuk")
        }
    }
}

@Composable
private fun OpenDayStage(
    state: DesktopAppState,
    onOpenDay: () -> Unit,
    onLogout: () -> Unit
) {
    CenterPanel(
        title = "Business Day Belum Aktif",
        subtitle = state.operations.blockingMessage ?: "Supervisor dapat membuka business day untuk terminal ini.",
        action = {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (state.operations.canOpenDay) {
                    Button(onClick = onOpenDay, enabled = !state.isBusy) {
                        Text(if (state.isBusy) "Memproses..." else "Open Business Day")
                    }
                } else {
                    OutlinedButton(onClick = onLogout) { Text("Ganti Operator") }
                }
            }
        }
    )
}

@Composable
private fun StartShiftStage(
    state: DesktopAppState,
    onOpeningCashChanged: (String) -> Unit,
    onStartShift: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(28.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Text("Start Shift", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Opening cash harus tercatat sebelum catalog/cart dibuka.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f))
        FormField("Opening Cash", state.operations.openingCashInput, onValueChange = onOpeningCashChanged)
        state.operations.blockingMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Button(onClick = onStartShift, enabled = !state.isBusy) {
            Text(if (state.isBusy) "Memulai..." else "Mulai Shift")
        }
    }
}

@Composable
private fun CatalogStage(
    state: DesktopAppState,
    onSearchChanged: (String) -> Unit,
    onBarcodeChanged: (String) -> Unit,
    onScanBarcode: () -> Unit,
    onAddProduct: (Product) -> Unit,
    onIncrement: (Product) -> Unit,
    onDecrement: (Product, Double) -> Unit,
    onClosingCashChanged: (String) -> Unit,
    onEndShift: () -> Unit,
    onCloseDay: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Column(modifier = Modifier.weight(1.3f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Catalog + Cart Foundation", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FormField("Cari produk / SKU", state.catalog.searchQuery, modifier = Modifier.weight(1f), onValueChange = onSearchChanged)
                FormField("Barcode manual", state.catalog.barcodeInput, modifier = Modifier.weight(1f), onValueChange = onBarcodeChanged)
                Button(onClick = onScanBarcode, modifier = Modifier.align(Alignment.CenterVertically)) { Text("Scan") }
            }
            LazyVerticalGrid(
                columns = GridCells.Adaptive(180.dp),
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.catalog.products) { product ->
                    ElevatedCard(shape = RoundedCornerShape(18.dp), onClick = { onAddProduct(product) }) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(product.name, fontWeight = FontWeight.SemiBold)
                            Text(product.sku, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                            Text("Rp ${product.price}", style = MaterialTheme.typography.titleMedium)
                            OutlinedButton(onClick = { onAddProduct(product) }) { Text("Tambah") }
                        }
                    }
                }
            }
        }
        ElevatedCard(
            modifier = Modifier.weight(0.9f).fillMaxHeight(),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Cart", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                if (state.catalog.basket.items.isEmpty()) {
                    Text("Cart kosong. Tambahkan produk dari panel kiri.")
                } else {
                    LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(state.catalog.basket.items) { item ->
                            ElevatedCard {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(item.product.name, fontWeight = FontWeight.SemiBold)
                                        Text("Qty ${item.quantity} x Rp ${item.unitPrice}")
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                        OutlinedButton(onClick = { onDecrement(item.product, item.quantity) }) { Text("-") }
                                        Button(onClick = { onIncrement(item.product) }) { Text("+") }
                                    }
                                }
                            }
                        }
                    }
                }
            HorizontalDivider()
                MetricBlock("Subtotal", "Rp ${state.catalog.basket.totals.subtotal}")
                MetricBlock("Diskon", "Rp ${state.catalog.basket.totals.discountTotal}")
                MetricBlock("Pajak", "Rp ${state.catalog.basket.totals.taxTotal}")
                MetricBlock("Total", "Rp ${state.catalog.basket.totals.finalTotal}")
                StatusChip("Checkout M6 belum dibuka", UiTone.Warning)
            HorizontalDivider()
                FormField("Closing Cash", state.operations.closingCashInput, onValueChange = onClosingCashChanged)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = onEndShift, modifier = Modifier.weight(1f)) { Text("End Shift") }
                    Button(onClick = onCloseDay, modifier = Modifier.weight(1f)) { Text("Close Day") }
                }
            }
        }
    }
}

@Composable
private fun CenterPanel(title: String, subtitle: String, action: @Composable () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        ElevatedCard(modifier = Modifier.width(540.dp), shape = RoundedCornerShape(28.dp)) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(28.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text(subtitle, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f))
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
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        modifier = modifier,
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        visualTransformation = if (masked) PasswordVisualTransformation() else VisualTransformation.None
    )
}

@Composable
private fun BannerCard(banner: UiBanner, onDismiss: () -> Unit) {
    ElevatedCard(shape = RoundedCornerShape(18.dp)) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            StatusChip(banner.tone.name, banner.tone)
            Text(banner.message)
            TextButton(onClick = onDismiss) { Text("Dismiss") }
        }
    }
}

@Composable
private fun MetricBlock(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f))
        Text(value, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun StatusChip(label: String, tone: UiTone) {
    AssistChip(
        onClick = {},
        label = { Text(label) },
        leadingIcon = {
            Box(
                modifier = Modifier
                    .width(10.dp)
                    .height(10.dp)
                    .background(toneColor(tone), RoundedCornerShape(999.dp))
            )
        }
    )
}

private fun toneColor(tone: UiTone): Color = when (tone) {
    UiTone.Info -> Color(0xFF0E74AF)
    UiTone.Success -> Color(0xFF16A34A)
    UiTone.Warning -> Color(0xFFD97706)
    UiTone.Danger -> Color(0xFFDC2626)
}

private fun writeSmokeMarker(path: String, content: String) {
    File(path).apply {
        parentFile?.mkdirs()
        writeText(content)
    }
}
