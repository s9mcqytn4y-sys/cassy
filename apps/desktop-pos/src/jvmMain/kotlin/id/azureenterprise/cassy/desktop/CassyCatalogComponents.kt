package id.azureenterprise.cassy.desktop

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.azureenterprise.cassy.masterdata.domain.Product
import id.azureenterprise.cassy.sales.domain.BasketItem

/**
 * CassyCatalogView: High-density product listing area.
 * Hardened R5: Improved information hierarchy and input prominence.
 */
@Composable
fun CassyCatalogView(
    state: DesktopCatalogState,
    onSearchChanged: (String) -> Unit,
    onBarcodeChanged: (String) -> Unit,
    onScanBarcode: () -> Unit,
    onAddProduct: (Product) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Search & Barcode Header (Above the fold)
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
                    value = state.searchQuery,
                    onValueChange = onSearchChanged,
                    placeholder = { Text("Cari Nama Produk...") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    shape = RoundedCornerShape(8.dp)
                )
                OutlinedTextField(
                    value = state.barcodeInput,
                    onValueChange = onBarcodeChanged,
                    placeholder = { Text("Barcode / SKU") },
                    modifier = Modifier.width(200.dp),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )
                )
                Button(
                    onClick = onScanBarcode,
                    modifier = Modifier.height(56.dp),
                    shape = RoundedCornerShape(8.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Text("INPUT", fontWeight = FontWeight.Black)
                }
            }
        }

        // Feedback Strip
        state.lookupFeedback?.let { feedback ->
            Surface(
                color = toneColor(feedback.tone).copy(alpha = 0.12f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = toneColor(feedback.tone),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = feedback.message,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Product List with Sticky Header vibe
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "Katalog Produk",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
            )

            if (state.products.isEmpty() && state.searchQuery.isNotEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "Produk tidak ditemukan untuk \"${state.searchQuery}\"",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }
            } else {
                LazyColumn(
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
}

/**
 * CassyCartPanel: Fixed right panel for order summary and checkout triggers.
 * Hardened R5: Dense but readable, numeric alignment, primary action focus.
 */
@Composable
fun CassyCartPanel(
    state: DesktopCatalogState,
    operations: OperationsState,
    inventory: InventoryPanelState,
    onCashReceivedChanged: (String) -> Unit,
    onIncrement: (Product) -> Unit,
    onDecrement: (Product, Double) -> Unit,
    onCheckoutCash: () -> Unit,
    onPrintLastReceipt: () -> Unit,
    onReprintLastReceipt: () -> Unit,
    onCancelSale: () -> Unit,
    onInventoryControl: () -> Unit,
    onCashControl: () -> Unit,
    onEndShift: () -> Unit,
    onClosingDay: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.width(380.dp).fillMaxHeight(),
        tonalElevation = 4.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            // Header & Context
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Cart", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                if (state.basket.items.isNotEmpty()) {
                    Text(
                        "${state.basket.items.sumOf { it.quantity.toInt() }} item",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            OperationalDashboardCard(operations.dashboard)
            Spacer(modifier = Modifier.height(16.dp))

            // Basket Items
            Box(modifier = Modifier.weight(1f)) {
                if (state.basket.items.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.ShoppingCart,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "Belum ada transaksi",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(state.basket.items) { item ->
                            CassyCartItemRow(item, onIncrement, onDecrement)
                        }
                    }
                }
            }

            // Totals & Input
            Surface(
                tonalElevation = 1.dp,
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    CassyMetricRow("Subtotal", "Rp ${state.basket.totals.subtotal.toInt()}")
                    CassyMetricRow("Pajak", "Rp ${state.basket.totals.taxTotal.toInt()}")
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                    CassyMetricRow("Total Akhir", "Rp ${state.basket.totals.finalTotal.toInt()}", isHighlight = true)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            CassyCurrencyInput(
                label = "Bayar Tunai",
                value = state.cashReceivedInput,
                onValueChange = onCashReceivedChanged,
                helperText = "F10 untuk kontrol kas cepat"
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Change / Shortage Readback
            state.cashTenderQuote?.let { quote ->
                val isSufficient = quote.isSufficient
                Surface(
                    color = if (isSufficient) toneColor(UiTone.Success).copy(alpha = 0.1f) else toneColor(UiTone.Danger).copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp),
                    border = if (isSufficient) null else BorderStroke(1.dp, toneColor(UiTone.Danger).copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = if (isSufficient) "KEMBALIAN" else "KURANG BAYAR",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = if (isSufficient) toneColor(UiTone.Success) else toneColor(UiTone.Danger)
                        )
                        Text(
                            text = "Rp ${if (isSufficient) quote.changeAmount.toInt() else quote.shortageAmount.toInt()}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = if (isSufficient) toneColor(UiTone.Success) else toneColor(UiTone.Danger)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Primary Actions
            Button(
                onClick = onCheckoutCash,
                enabled = state.basket.items.isNotEmpty() && state.cashTenderQuote?.isSufficient == true,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("FINALISASI TRANSAKSI", fontWeight = FontWeight.Black)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onCancelSale,
                    enabled = state.basket.items.isNotEmpty(),
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Batal")
                }

                if (state.lastFinalizedSaleId != null) {
                    Button(
                        onClick = onPrintLastReceipt,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cetak")
                    }
                }
            }

            // Operational Controls (Bottom)
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SmallControlBtn("Inventori (F9)", onInventoryControl, Modifier.weight(1f))
                SmallControlBtn("Kas (F10)", onCashControl, Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SmallControlBtn("Tutup Shift (F11)", onEndShift, Modifier.weight(1f))
                SmallControlBtn("Tutup Hari (F12)", onClosingDay, Modifier.weight(1f), isPrimary = true)
            }
        }
    }
}

@Composable
private fun SmallControlBtn(label: String, onClick: () -> Unit, modifier: Modifier = Modifier, isPrimary: Boolean = false) {
    if (isPrimary) {
        FilledTonalButton(
            onClick = onClick,
            modifier = modifier.height(40.dp),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 8.dp)
        ) {
            Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier.height(40.dp),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 8.dp)
        ) {
            Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun CassyCartItemRow(
    item: BasketItem,
    onIncrement: (Product) -> Unit,
    onDecrement: (Product, Double) -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.product.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "Rp ${item.unitPrice.toInt()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(
                    onClick = { onDecrement(item.product, item.quantity) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Kurang", modifier = Modifier.size(16.dp))
                }

                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                ) {
                    Text(
                        text = "${item.quantity.toInt()}",
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                IconButton(
                    onClick = { onIncrement(item.product) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Tambah", modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
fun CassyMetricRow(label: String, value: String, isHighlight: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Text(
            label,
            style = if (isHighlight) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            fontWeight = if (isHighlight) FontWeight.Black else FontWeight.Medium
        )
        Text(
            text = value,
            style = if (isHighlight) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.titleMedium,
            fontWeight = if (isHighlight) FontWeight.Black else FontWeight.Bold,
            color = if (isHighlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            fontFamily = FontFamily.Monospace
        )
    }
}
