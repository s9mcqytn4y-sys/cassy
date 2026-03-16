package id.azureenterprise.cassy.desktop

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import id.azureenterprise.cassy.masterdata.domain.Product
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.compose.koinInject
import org.koin.core.context.GlobalContext
import java.io.File
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    val smokeMode = args.contains("--smoke-run")
    if (smokeMode) {
        runHeadlessSmoke()
        return
    }

    startDesktopKoin()
    println("Cassy desktop runtime Java ${System.getProperty("java.version")} | smokeMode=$smokeMode")

    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Cassy POS",
            icon = painterResource("logo/cassandra-logo-main-512.png")
        ) {
            CassyDesktopTheme {
                val controller: DesktopAppController = koinInject()
                val state by controller.state.collectAsState()
                val scope = rememberCoroutineScope()

                LaunchedEffect(Unit) {
                    controller.load()
                }

                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Row(modifier = Modifier.fillMaxSize()) {
                        CassySlimRail(
                            selectedStage = state.stage,
                            onReload = { scope.launch { controller.load() } },
                            onLogout = { scope.launch { controller.logout() } }
                        )

                        Column(modifier = Modifier.fillMaxSize()) {
                            CassyTopBar(state = state.shell)

                            Box(modifier = Modifier.fillMaxSize()) {
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
                                        onClosingDay = { scope.launch { controller.closeBusinessDay() } }
                                    )
                                    is DesktopStage.FatalError -> FatalStage(
                                        message = stage.message,
                                        onRetry = { scope.launch { controller.load() } }
                                    )
                                }

                                state.banner?.let { banner ->
                                    Box(modifier = Modifier.padding(16.dp).align(Alignment.BottomEnd)) {
                                        BannerCard(banner = banner, onDismiss = controller::dismissBanner)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun runHeadlessSmoke() {
    val smokeMarkerPath = System.getenv("CASSY_SMOKE_MARKER")
    startDesktopKoin()
    runBlocking {
        val controller = GlobalContext.get().get<DesktopAppController>()
        controller.load()
        delay(300)
        val stage = controller.state.value.stage
        if (stage is DesktopStage.FatalError) {
            smokeMarkerPath?.let { File(it).writeText("FAILED stage=${stage.message}") }
            exitProcess(1)
        } else {
            smokeMarkerPath?.let { File(it).writeText("OK stage=${stage::class.simpleName}") }
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
        title = "Gagal Memuat Data",
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
            Text("Pengaturan Awal Toko", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("Lengkapi data toko dan terminal sebelum memulai operasional.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        }
        item { FormField("Nama Toko", state.bootstrap.storeName) { onFieldChanged(BootstrapField.StoreName, it) } }
        item { FormField("Nama Terminal", state.bootstrap.terminalName) { onFieldChanged(BootstrapField.TerminalName, it) } }
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
private fun LoginStage(
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
private fun OpenDayStage(
    state: DesktopAppState,
    onOpenDay: () -> Unit,
    onLogout: () -> Unit
) {
    CenterPanel(
        title = "Hari Bisnis Belum Dibuka",
        subtitle = state.operations.blockingMessage ?: "Tekan tombol di bawah untuk membuka hari bisnis.",
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
private fun StartShiftStage(
    state: DesktopAppState,
    onOpeningCashChanged: (String) -> Unit,
    onStartShift: () -> Unit
) {
    CenterPanel(
        title = "Mulai Shift Baru",
        subtitle = "Masukkan saldo kas awal (Modal Awal) sebelum memulai transaksi.",
        action = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                CassyCurrencyInput(
                    label = "Modal Awal Tunai",
                    value = state.operations.openingCashInput,
                    onValueChange = onOpeningCashChanged,
                    helperText = "Uang tunai yang tersedia di laci kas saat ini."
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
    onClosingDay: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f).fillMaxHeight().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // PHASE 2: Optimized Search & Barcode Bar
            Surface(
                tonalElevation = 2.dp,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = state.catalog.searchQuery,
                        onValueChange = onSearchChanged,
                        placeholder = { Text("Cari Nama Produk...") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        shape = RoundedCornerShape(8.dp)
                    )
                    OutlinedTextField(
                        value = state.catalog.barcodeInput,
                        onValueChange = onBarcodeChanged,
                        placeholder = { Text("Barcode / SKU") },
                        modifier = Modifier.width(180.dp),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )
                    Button(
                        onClick = onScanBarcode,
                        modifier = Modifier.height(56.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Input")
                    }
                }
            }

            // PHASE 2: High Density Product List
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(state.catalog.products) { product ->
                    CassyDenseProductRow(product = product, onClick = { onAddProduct(product) })
                }
            }
        }

        // Cart Panel (Fixed Right)
        Surface(
            modifier = Modifier.width(380.dp).fillMaxHeight(),
            tonalElevation = 4.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Text("Ringkasan Pesanan", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                if (state.catalog.basket.items.isEmpty()) {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Belum ada barang", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                        }
                    }
                } else {
                    LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(state.catalog.basket.items) { item ->
                            CartItemRow(item, onIncrement, onDecrement)
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                MetricRow("Subtotal", "Rp ${state.catalog.basket.totals.subtotal.toInt()}")
                MetricRow("Pajak", "Rp ${state.catalog.basket.totals.taxTotal.toInt()}")
                MetricRow("Total", "Rp ${state.catalog.basket.totals.finalTotal.toInt()}", isHighlight = true)

                Spacer(modifier = Modifier.height(24.dp))

                CassyCurrencyInput(
                    label = "Uang Kas Akhir",
                    value = state.operations.closingCashInput,
                    onValueChange = onClosingCashChanged
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onEndShift, modifier = Modifier.weight(1f).height(48.dp)) { Text("Tutup Shift") }
                    Button(onClick = onClosingDay, modifier = Modifier.weight(1f).height(48.dp)) { Text("Tutup Hari") }
                }
            }
        }
    }
}

@Composable
private fun CartItemRow(
    item: id.azureenterprise.cassy.sales.domain.BasketItem,
    onIncrement: (Product) -> Unit,
    onDecrement: (Product, Double) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.product.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, maxLines = 1)
                Text("Rp ${item.unitPrice.toInt()}", style = MaterialTheme.typography.labelSmall)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    onClick = { onDecrement(item.product, item.quantity) },
                    modifier = Modifier.size(28.dp),
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) { Text("-", fontWeight = FontWeight.Bold) }
                }
                Text("${item.quantity.toInt()}", fontWeight = FontWeight.ExtraBold, modifier = Modifier.widthIn(min = 20.dp), textAlign = TextAlign.Center)
                Surface(
                    onClick = { onIncrement(item.product) },
                    modifier = Modifier.size(28.dp),
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) { Text("+", fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}

@Composable
private fun CenterPanel(title: String, subtitle: String, action: @Composable () -> Unit) {
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
            TextButton(onClick = onDismiss) { Text("OK") }
        }
    }
}

@Composable
private fun MetricRow(label: String, value: String, isHighlight: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = if (isHighlight) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium)
        Text(
            text = value,
            style = if (isHighlight) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.titleMedium,
            fontWeight = if (isHighlight) FontWeight.ExtraBold else FontWeight.Bold,
            color = if (isHighlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}
