package id.azureenterprise.cassy.desktop

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
    closingCashInput: String,
    onClosingCashChanged: (String) -> Unit,
    onIncrement: (Product) -> Unit,
    onDecrement: (Product, Double) -> Unit,
    onCheckoutCash: () -> Unit,
    onReprintLastReceipt: () -> Unit,
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

            Button(
                onClick = onCheckoutCash,
                enabled = state.basket.items.isNotEmpty(),
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Text("Bayar Tunai")
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (state.lastFinalizedSaleId != null) {
                OutlinedButton(
                    onClick = onReprintLastReceipt,
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text("Print Ulang Struk")
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            CassyCurrencyInput(
                label = "Uang Kas Akhir",
                value = closingCashInput,
                onValueChange = onClosingCashChanged
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onEndShift, modifier = Modifier.weight(1f).height(48.dp)) { Text("Tutup Shift") }
                Button(onClick = onClosingDay, modifier = Modifier.weight(1f).height(48.dp)) { Text("Tutup Hari") }
            }

            if (state.recentSales.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Riwayat Final", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(
                    state.recentSales.first().localNumber,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
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
