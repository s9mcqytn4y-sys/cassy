package id.azureenterprise.cassy.desktop

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
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
import kotlinx.coroutines.delay

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
    val isSuccess = catalog.lastFinalizedSaleId != null && catalog.basket.items.isEmpty()

    val barcodeFocusRequester = remember { FocusRequester() }
    val cashFocusRequester = remember { FocusRequester() }

    LaunchedEffect(isSuccess, catalog.basket.items.size) {
        if (!isSuccess) {
            delay(100)
            if (catalog.basket.items.isEmpty()) {
                barcodeFocusRequester.requestFocus()
            } else if (catalog.cashReceivedInput.isEmpty()) {
                // Focus cash input if items added but no cash yet
                // Or keep focus on barcode for scanning more items
                barcodeFocusRequester.requestFocus()
            }
        }
    }

    Row(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        // Main Area: Product Selection
        Box(modifier = Modifier.weight(1.3f).fillMaxHeight()) {
            if (!isSuccess) {
                CassyCatalogView(
                    state = catalog,
                    barcodeFocusRequester = barcodeFocusRequester,
                    onSearchChanged = onSearchChanged,
                    onBarcodeChanged = onBarcodeChanged,
                    onScanBarcode = onScanBarcode,
                    onAddProduct = onAddProduct
                )
            } else {
                CassyTransactionSuccessView(
                    state = catalog,
                    onPrint = onPrintLastReceipt,
                    onNewTransaction = onCancelSale
                )
            }
        }

        // Sidebar: Cart & Checkout (Centralized Control)
        CassyCartPanel(
            state = catalog,
            cashFocusRequester = cashFocusRequester,
            onCashReceivedChanged = onCashReceivedChanged,
            onIncrement = onIncrement,
            onDecrement = onDecrement,
            onCheckoutCash = onCheckoutCash,
            onCancelSale = onCancelSale,
            onMemberNumberChanged = onMemberNumberChanged,
            onMemberNameChanged = onMemberNameChanged,
            onDonationAmountChanged = onDonationAmountChanged,
            isLocked = isSuccess
        )
    }
}

@Composable
private fun CassyCatalogView(
    state: DesktopCatalogState,
    barcodeFocusRequester: FocusRequester,
    onSearchChanged: (String) -> Unit,
    onBarcodeChanged: (String) -> Unit,
    onScanBarcode: () -> Unit,
    onAddProduct: (Product) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 1.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Quick Search & Barcode Lane
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SemanticTextField(
                    label = "Cari Barang",
                    value = state.searchQuery,
                    onValueChange = onSearchChanged,
                    placeholder = "Nama atau SKU...",
                    modifier = Modifier.weight(1f),
                    leadingIcon = Icons.Default.Search,
                    imeAction = ImeAction.Search
                )
                SemanticTextField(
                    label = "Scan / Barcode",
                    value = state.barcodeInput,
                    onValueChange = onBarcodeChanged,
                    placeholder = "Scan sekarang...",
                    modifier = Modifier.weight(1f).focusRequester(barcodeFocusRequester),
                    leadingIcon = Icons.Default.QrCodeScanner,
                    imeAction = ImeAction.Done,
                    onImeAction = onScanBarcode
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(state.products) { product ->
                    CassyDenseProductRow(product = product, onClick = { onAddProduct(product) })
                }
            }
        }
    }
}

@Composable
private fun CassyCartPanel(
    state: DesktopCatalogState,
    cashFocusRequester: FocusRequester,
    onCashReceivedChanged: (String) -> Unit,
    onIncrement: (Product) -> Unit,
    onDecrement: (Product, Double) -> Unit,
    onCheckoutCash: () -> Unit,
    onCancelSale: () -> Unit,
    onMemberNumberChanged: (String) -> Unit,
    onMemberNameChanged: (String) -> Unit,
    onDonationAmountChanged: (String) -> Unit,
    isLocked: Boolean
) {
    Surface(
        modifier = Modifier.width(420.dp).fillMaxHeight(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Checkout Lane", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)

            // Items List (Compact)
            Box(modifier = Modifier.weight(1f)) {
                if (state.basket.items.isEmpty()) {
                    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("Keranjang Kosong", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        items(state.basket.items) { item ->
                            CassyCartItemRow(item, onIncrement, onDecrement)
                        }
                    }
                }
            }

            // Optional Fast-Actions (Member & Donation) - Inline & Compact
            if (!isLocked && state.basket.items.isNotEmpty()) {
                CassyInlineOptionalSteps(
                    state = state,
                    onMemberNumberChanged = onMemberNumberChanged,
                    onMemberNameChanged = onMemberNameChanged,
                    onDonationAmountChanged = onDonationAmountChanged
                )
            }

            // Totals and Payment
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        CassyMetricRow("Total Tagihan", state.basket.totals.finalTotal.toRupiah(), isHighlight = true)
                    }
                }

                if (!isLocked && state.basket.items.isNotEmpty()) {
                    CassyCurrencyInput(
                        label = "Bayar Tunai (F12)",
                        value = state.cashReceivedInput,
                        onValueChange = onCashReceivedChanged,
                        modifier = Modifier.focusRequester(cashFocusRequester),
                        onImeAction = onCheckoutCash
                    )

                    state.cashTenderQuote?.let { quote ->
                        val color = if (quote.isSufficient) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(if (quote.isSufficient) "KEMBALIAN" else "KURANG", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = color)
                            Text(if (quote.isSufficient) quote.changeAmount.toRupiah() else quote.shortageAmount.toRupiah(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = color)
                        }
                    }

                    Button(
                        onClick = onCheckoutCash,
                        enabled = state.cashTenderQuote?.isSufficient == true,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("SELESAI PEMBAYARAN", fontWeight = FontWeight.ExtraBold)
                    }
                }
            }

            if (!isLocked && state.basket.items.isNotEmpty()) {
                TextButton(onClick = onCancelSale, modifier = Modifier.fillMaxWidth()) {
                    Text("Kosongkan Keranjang", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
private fun CassyInlineOptionalSteps(
    state: DesktopCatalogState,
    onMemberNumberChanged: (String) -> Unit,
    onMemberNameChanged: (String) -> Unit,
    onDonationAmountChanged: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (state.memberNumberInput.isNotBlank()) "Member: ${state.memberNumberInput}" else "Member & Donasi",
                style = MaterialTheme.typography.labelMedium,
                color = if (state.memberNumberInput.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            IconButton(onClick = { expanded = !expanded }, modifier = Modifier.size(24.dp)) {
                Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = null)
            }
        }

        AnimatedVisibility(visible = expanded) {
            Column(modifier = Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SemanticTextField(
                    label = "Nomor Member",
                    value = state.memberNumberInput,
                    onValueChange = onMemberNumberChanged,
                    placeholder = "08...",
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.fillMaxWidth()
                )
                CassyCurrencyInput(
                    label = "Donasi",
                    value = state.donationAmountInput,
                    onValueChange = onDonationAmountChanged
                )
            }
        }
    }
}

@Composable
private fun CassyTransactionSuccessView(
    state: DesktopCatalogState,
    onPrint: () -> Unit,
    onNewTransaction: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            color = Color(0xFFE8F5E9),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.size(120.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color(0xFF4CAF50))
            }
        }
        Spacer(Modifier.height(24.dp))
        Text("Transaksi Berhasil!", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
        Text("Nota ${state.receiptPreview.localNumber ?: "-"} selesai.", color = MaterialTheme.colorScheme.onSurfaceVariant)

        state.cashTenderQuote?.let { quote ->
            if (quote.changeAmount > 0) {
                Spacer(Modifier.height(16.dp))
                Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(8.dp)) {
                    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("KEMBALIAN", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        Text(quote.changeAmount.toRupiah(), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                    }
                }
            }
        }

        Spacer(Modifier.height(48.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedButton(onClick = onPrint, modifier = Modifier.height(56.dp).width(180.dp)) {
                Icon(Icons.Default.Print, contentDescription = null)
                Spacer(Modifier.width(12.dp))
                Text("Cetak Struk")
            }
            Button(onClick = onNewTransaction, modifier = Modifier.height(56.dp).width(180.dp)) {
                Text("Transaksi Baru")
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
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(item.product.name, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("${item.product.price.toRupiah()} x ${item.quantity.toInt()}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { onDecrement(item.product, item.quantity) }, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(16.dp))
            }
            Text("${item.quantity.toInt()}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 4.dp))
            IconButton(onClick = { onIncrement(item.product) }, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun CassyMetricRow(label: String, value: String, isHighlight: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            text = value,
            style = if (isHighlight) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.bodyMedium,
            fontWeight = if (isHighlight) FontWeight.Black else FontWeight.Bold,
            color = if (isHighlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}
