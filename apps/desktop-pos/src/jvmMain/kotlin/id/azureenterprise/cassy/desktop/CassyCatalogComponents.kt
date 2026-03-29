package id.azureenterprise.cassy.desktop

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.azureenterprise.cassy.masterdata.domain.Product
import id.azureenterprise.cassy.sales.domain.BasketItem

/**
 * CassyCatalogScreen: Komponen UI untuk layar transaksi Kasir.
 * Menangani alur kerja lengkap dari scan hingga pembayaran.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CassyCatalogScreen(
    state: DesktopAppState,
    onSearchChanged: (String) -> Unit,
    onBarcodeChanged: (String) -> Unit,
    onScanBarcode: () -> Unit,
    onAddProduct: (Product) -> Unit,
    onCashReceivedChanged: (String) -> Unit,
    onConfirmCartReview: () -> Unit,
    onMemberNumberChanged: (String) -> Unit,
    onMemberNameChanged: (String) -> Unit,
    onSkipMember: () -> Unit,
    onDonationEnabledChanged: (Boolean) -> Unit,
    onDonationAmountChanged: (String) -> Unit,
    onSkipDonation: () -> Unit,
    onIncrement: (Product) -> Unit,
    onDecrement: (Product, Double) -> Unit,
    onCheckoutCash: () -> Unit,
    onPrintLastReceipt: () -> Unit,
    onCancelSale: () -> Unit
) {
    val catalog = state.catalog
    val milestone = remember(catalog) { resolveCashierMilestone(catalog) }

    Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        // Left Column: Main Workflow Area
        Column(
            modifier = Modifier.weight(1.3f).fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CashierMilestoneBar(milestone = milestone, catalog = catalog)

            Surface(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                tonalElevation = 1.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    when (milestone) {
                        CashierMilestone.ScanBarang, CashierMilestone.ReviewKeranjang -> {
                            CassyCatalogView(
                                state = catalog,
                                milestone = milestone,
                                onSearchChanged = onSearchChanged,
                                onBarcodeChanged = onBarcodeChanged,
                                onScanBarcode = onScanBarcode,
                                onAddProduct = onAddProduct,
                                onConfirmReview = onConfirmCartReview
                            )
                        }
                        CashierMilestone.Member -> {
                            CassyMemberForm(
                                state = catalog,
                                onNumberChanged = onMemberNumberChanged,
                                onNameChanged = onMemberNameChanged,
                                onSkip = onSkipMember
                            )
                        }
                        CashierMilestone.Donasi -> {
                            CassyDonationForm(
                                state = catalog,
                                onEnabledChanged = onDonationEnabledChanged,
                                onAmountChanged = onDonationAmountChanged,
                                onSkip = onSkipDonation
                            )
                        }
                        CashierMilestone.Pembayaran -> {
                            CassyPaymentPrompt(state = catalog)
                        }
                        CashierMilestone.Selesai -> {
                            CassyTransactionSuccessView(
                                state = catalog,
                                onPrint = onPrintLastReceipt
                            )
                        }
                    }
                }
            }
        }

        // Right Column: Cart Panel (Always Visible for reference)
        CassyCartPanel(
            state = catalog,
            onCashReceivedChanged = onCashReceivedChanged,
            onIncrement = onIncrement,
            onDecrement = onDecrement,
            onCheckoutCash = onCheckoutCash,
            onPrintLastReceipt = onPrintLastReceipt,
            onCancelSale = onCancelSale,
            isPaymentEnabled = milestone == CashierMilestone.Pembayaran
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CassyCatalogView(
    state: DesktopCatalogState,
    milestone: CashierMilestone,
    onSearchChanged: (String) -> Unit,
    onBarcodeChanged: (String) -> Unit,
    onScanBarcode: () -> Unit,
    onAddProduct: (Product) -> Unit,
    onConfirmReview: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        if (milestone == CashierMilestone.ScanBarang) {
            // Search & Barcode
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SemanticTextField(
                    label = "Cari Produk",
                    value = state.searchQuery,
                    onValueChange = onSearchChanged,
                    placeholder = "Ketik nama barang atau SKU...",
                    leadingIcon = Icons.Default.Search,
                    imeAction = ImeAction.Search
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Bottom) {
                    SemanticTextField(
                        label = "Scan Barcode",
                        value = state.barcodeInput,
                        onValueChange = onBarcodeChanged,
                        placeholder = "Scan atau input manual...",
                        modifier = Modifier.weight(1f),
                        leadingIcon = Icons.Default.QrCodeScanner,
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done,
                        onImeAction = onScanBarcode
                    )
                    Button(
                        onClick = onScanBarcode,
                        modifier = Modifier.height(56.dp).width(100.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Input")
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Result List
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(state.products) { product ->
                    CassyDenseProductRow(product = product, onClick = { onAddProduct(product) })
                }
            }
        } else {
            // Review Keranjang
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.ShoppingCartCheckout, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(24.dp))
                Text("Review Keranjang", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("Pastikan item dan jumlah sudah benar sebelum lanjut.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(32.dp))
                Button(
                    onClick = onConfirmReview,
                    modifier = Modifier.width(240.dp).height(56.dp)
                ) {
                    Text("Konfirmasi & Lanjut", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun CassyMemberForm(
    state: DesktopCatalogState,
    onNumberChanged: (String) -> Unit,
    onNameChanged: (String) -> Unit,
    onSkip: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(32.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.secondary)
        Spacer(Modifier.height(16.dp))
        Text("Data Member", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text("Opsional. Masukkan nomor member pelanggan bila ada.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(24.dp))

        Column(modifier = Modifier.width(400.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            SemanticTextField("Nomor Member", state.memberNumberInput, onNumberChanged, placeholder = "Contoh: 0812...", keyboardType = KeyboardType.Number)
            SemanticTextField("Nama Member (Opsional)", state.memberNameInput, onNameChanged, placeholder = "Nama pelanggan...")

            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedButton(onClick = onSkip, modifier = Modifier.weight(1f).height(48.dp)) {
                    Text("Lewati Langkah Ini")
                }
                Button(onClick = { /* Flow handles this via input detection */ }, enabled = state.memberNumberInput.isNotEmpty(), modifier = Modifier.weight(1f).height(48.dp)) {
                    Text("Gunakan Member")
                }
            }
        }
    }
}

@Composable
private fun CassyDonationForm(
    state: DesktopCatalogState,
    onEnabledChanged: (Boolean) -> Unit,
    onAmountChanged: (String) -> Unit,
    onSkip: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(32.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.VolunteerActivism, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color(0xFFE91E63))
        Spacer(Modifier.height(16.dp))
        Text("Tawarkan Donasi", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text("Tanyakan apakah pelanggan ingin menyumbangkan uang kembalian.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(24.dp))

        Column(modifier = Modifier.width(400.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            CassyCurrencyInput("Nominal Donasi", state.donationAmountInput, onAmountChanged)

            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedButton(onClick = onSkip, modifier = Modifier.weight(1f).height(48.dp)) {
                    Text("Tidak Donasi")
                }
                Button(onClick = { /* Flow handles this */ }, enabled = state.donationAmountInput.isNotEmpty(), modifier = Modifier.weight(1f).height(48.dp)) {
                    Text("Tambahkan Donasi")
                }
            }
        }
    }
}

@Composable
private fun CassyPaymentPrompt(state: DesktopCatalogState) {
    Column(modifier = Modifier.fillMaxSize().padding(32.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(24.dp))
        Text("Siap Terima Pembayaran", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Masukkan nominal tunai di panel kanan untuk menyelesaikan transaksi.", textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(Modifier.height(40.dp))
        Surface(color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f), shape = RoundedCornerShape(12.dp)) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("TOTAL TAGIHAN", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Text("Rp ${state.basket.totals.finalTotal.toInt()}", style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun CassyTransactionSuccessView(state: DesktopCatalogState, onPrint: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(32.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(80.dp), tint = Color(0xFF4CAF50))
        Spacer(Modifier.height(24.dp))
        Text("Transaksi Berhasil!", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
        Text("Nota ${state.receiptPreview.localNumber ?: "-"} telah tersimpan.", color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(Modifier.height(40.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedButton(onClick = onPrint, modifier = Modifier.height(56.dp)) {
                Icon(Icons.Default.Print, contentDescription = null)
                Spacer(Modifier.width(12.dp))
                Text("Cetak Struk")
            }
            Button(onClick = { /* Workflow handles next */ }, modifier = Modifier.height(56.dp)) {
                Text("Transaksi Baru")
            }
        }
    }
}

@Composable
private fun CassyCartPanel(
    state: DesktopCatalogState,
    onCashReceivedChanged: (String) -> Unit,
    onIncrement: (Product) -> Unit,
    onDecrement: (Product, Double) -> Unit,
    onCheckoutCash: () -> Unit,
    onPrintLastReceipt: () -> Unit,
    onCancelSale: () -> Unit,
    isPaymentEnabled: Boolean
) {
    Surface(
        modifier = Modifier.width(400.dp).fillMaxHeight(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Text("Keranjang", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)

            // Items List
            Box(modifier = Modifier.weight(1f)) {
                if (state.basket.items.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Belum ada item", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(state.basket.items) { item ->
                            CassyCartItemRow(item, onIncrement, onDecrement)
                        }
                    }
                }
            }

            // Totals
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    CassyMetricRow("Total Item", "${state.basket.items.sumOf { it.quantity.toInt() }} unit")
                    CassyMetricRow("Total Belanja", "Rp ${state.basket.totals.finalTotal.toInt()}", isHighlight = true)
                }
            }

            // Payment Input Area
            if (isPaymentEnabled) {
                CassyCurrencyInput(
                    label = "Bayar Tunai",
                    value = state.cashReceivedInput,
                    onValueChange = onCashReceivedChanged
                )

                state.cashTenderQuote?.let { quote ->
                    val color = if (quote.isSufficient) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.End) {
                        Text(if (quote.isSufficient) "KEMBALIAN" else "KURANG", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = color)
                        Text("Rp ${if (quote.isSufficient) quote.changeAmount.toInt() else quote.shortageAmount.toInt()}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = color)
                    }
                }

                Button(
                    onClick = onCheckoutCash,
                    enabled = state.basket.items.isNotEmpty() && state.cashTenderQuote?.isSufficient == true,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("FINALISASI TRANSAKSI", fontWeight = FontWeight.ExtraBold)
                }
            } else {
                Box(modifier = Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                    Text("Selesaikan langkah sebelumnya\nuntuk membuka pembayaran", textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            OutlinedButton(
                onClick = onCancelSale,
                enabled = state.basket.items.isNotEmpty(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Batalkan Transaksi")
            }
        }
    }
}

@Composable
private fun CashierMilestoneBar(milestone: CashierMilestone, catalog: DesktopCatalogState) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        for (step in CashierMilestone.entries) {
            val isActive = step == milestone
            val isDone = step.ordinal < milestone.ordinal
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                color = when {
                    isActive -> MaterialTheme.colorScheme.primaryContainer
                    isDone -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }
            ) {
                Box(modifier = Modifier.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                    Text(step.title, style = MaterialTheme.typography.labelMedium, fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal)
                }
            }
        }
    }
}

@Composable
fun CassyCartItemRow(
    item: BasketItem,
    onIncrement: (Product) -> Unit,
    onDecrement: (Product, Double) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(item.product.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("Rp ${item.product.price.toInt()} x ${item.quantity.toInt()}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IconButton(onClick = { onDecrement(item.product, item.quantity) }, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(18.dp))
            }
            Text("${item.quantity.toInt()}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Black)
            IconButton(onClick = { onIncrement(item.product) }, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
fun CassyMetricRow(label: String, value: String, isHighlight: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            text = value,
            style = if (isHighlight) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            fontWeight = if (isHighlight) FontWeight.ExtraBold else FontWeight.Bold,
            color = if (isHighlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun resolveCashierMilestone(catalog: DesktopCatalogState): CashierMilestone = when {
    catalog.lastFinalizedSaleId != null && catalog.basket.items.isEmpty() -> CashierMilestone.Selesai
    catalog.basket.items.isEmpty() -> CashierMilestone.ScanBarang
    !catalog.reviewConfirmed -> CashierMilestone.ReviewKeranjang
    !catalog.memberSkipped && catalog.memberNumberInput.isEmpty() -> CashierMilestone.Member
    !catalog.donationSkipped && catalog.donationAmountInput.isEmpty() -> CashierMilestone.Donasi
    else -> CashierMilestone.Pembayaran
}
