package id.azureenterprise.cassy.desktop

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import id.azureenterprise.cassy.masterdata.domain.Product
import id.azureenterprise.cassy.sales.domain.BasketItem

/**
 * CassyCatalogView: High-density product listing area.
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
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    shape = RoundedCornerShape(8.dp)
                )
                OutlinedTextField(
                    value = state.barcodeInput,
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

        state.lookupFeedback?.let { feedback ->
            Surface(
                color = toneColor(feedback.tone).copy(alpha = 0.08f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = toneColor(feedback.tone)
                    )
                    Text(
                        text = feedback.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(state.products) { product ->
                CassyDenseProductRow(product = product, onClick = { onAddProduct(product) })
            }
        }
    }
}

/**
 * CassyCartPanel: Fixed right panel for order summary and checkout triggers.
 */
@Composable
fun CassyCartPanel(
    state: DesktopCatalogState,
    operations: OperationsState,
    onCashReceivedChanged: (String) -> Unit,
    onIncrement: (Product) -> Unit,
    onDecrement: (Product, Double) -> Unit,
    onCheckoutCash: () -> Unit,
    onPrintLastReceipt: () -> Unit,
    onReprintLastReceipt: () -> Unit,
    onCancelSale: () -> Unit,
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
            Text("Ringkasan Pesanan", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            OperationalDashboardCard(operations.dashboard)
            Spacer(modifier = Modifier.height(16.dp))

            if (state.basket.items.isEmpty()) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.ShoppingCart,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Belum ada barang", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                    }
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.basket.items) { item ->
                        CassyCartItemRow(item, onIncrement, onDecrement)
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            CassyMetricRow("Subtotal", "Rp ${state.basket.totals.subtotal.toInt()}")
            CassyMetricRow("Pajak", "Rp ${state.basket.totals.taxTotal.toInt()}")
            CassyMetricRow("Total", "Rp ${state.basket.totals.finalTotal.toInt()}", isHighlight = true)

            Spacer(modifier = Modifier.height(24.dp))

            CassyCurrencyInput(
                label = "Uang Diterima",
                value = state.cashReceivedInput,
                onValueChange = onCashReceivedChanged,
                helperText = "Masukkan uang dari pelanggan. Kembalian dihitung otomatis."
            )

            Spacer(modifier = Modifier.height(12.dp))

            state.cashTenderQuote?.let { quote ->
                val isSufficient = quote.isSufficient
                Surface(
                    color = if (isSufficient) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = if (isSufficient) "Kembalian pelanggan" else "Uang masih kurang",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Rp ${if (isSufficient) quote.changeAmount.toInt() else quote.shortageAmount.toInt()}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            Button(
                onClick = onCheckoutCash,
                enabled = state.basket.items.isNotEmpty() && state.cashTenderQuote?.isSufficient == true,
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Text("Selesaikan Tunai")
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = onCancelSale,
                enabled = state.basket.items.isNotEmpty(),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text("Batalkan Pesanan")
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (state.lastFinalizedSaleId != null) {
                Button(
                    onClick = onPrintLastReceipt,
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cetak Struk")
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onReprintLastReceipt,
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text("Print Ulang Struk")
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            Surface(
                tonalElevation = 1.dp,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Status Print", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Text(
                        text = state.printState.detailMessage ?: "Belum ada status print",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                tonalElevation = 1.dp,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Preview Struk Final", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    Text(
                        text = state.receiptPreview.localNumber ?: "Belum ada struk final",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = state.receiptPreview.content ?: state.receiptPreview.availabilityMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                        maxLines = 12
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (operations.pendingApprovals.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "${operations.pendingApprovals.size} approval operasional menunggu review",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onCashControl, modifier = Modifier.weight(1f).height(48.dp)) { Text("Kontrol Kas") }
                OutlinedButton(onClick = onEndShift, modifier = Modifier.weight(1f).height(48.dp)) { Text("Tutup Shift") }
                Button(onClick = onClosingDay, modifier = Modifier.weight(1f).height(48.dp)) { Text("Tutup Hari") }
            }

            if (state.recentSales.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Riwayat Final", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                state.recentSales.take(3).forEach { entry ->
                    Text(
                        text = "${entry.localNumber} | ${entry.paymentMethod} | Rp ${entry.finalAmount.toInt()}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
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
fun CassyMetricRow(label: String, value: String, isHighlight: Boolean = false) {
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
