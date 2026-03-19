package id.azureenterprise.cassy.desktop

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.azureenterprise.cassy.inventory.domain.InventoryApprovalAction
import id.azureenterprise.cassy.inventory.domain.InventoryDiscrepancyReview
import id.azureenterprise.cassy.inventory.domain.InventoryDiscrepancyStatus
import id.azureenterprise.cassy.inventory.domain.StockLedgerEntry

@Composable
fun InventoryTruthDialog(
    state: InventoryPanelState,
    onDismiss: () -> Unit,
    onSelectProduct: (String) -> Unit,
    onCountQuantityChanged: (String) -> Unit,
    onSubmitCount: () -> Unit,
    onAdjustmentDirectionChanged: (InventoryAdjustmentDirection) -> Unit,
    onAdjustmentQuantityChanged: (String) -> Unit,
    onAdjustmentReasonCodeChanged: (String) -> Unit,
    onAdjustmentReasonDetailChanged: (String) -> Unit,
    onApplyAdjustment: () -> Unit,
    onResolveDiscrepancy: (String) -> Unit,
    onMarkInvestigation: (String) -> Unit,
    onApproveAction: (String) -> Unit,
    onDenyAction: (String) -> Unit,
    onDeferDiscrepancy: (String) -> Unit
) {
    val selectedProduct = state.availableProducts.firstOrNull { it.id == state.selectedProductId }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Inventory Truth Lite", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .width(920.dp)
                    .heightIn(max = 640.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    tonalElevation = 1.dp,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Current state vs explanation trail", fontWeight = FontWeight.Bold)
                        Text("inventory_balance adalah current state. stock_ledger_entry adalah explanation trail dan tidak dipakai sebagai cache balance.")
                        Text("Image I/O: ${state.imageIoStatus}")
                        Text("Folder: ${state.inputImagesFolder}")
                        Text("Image ref: ${state.selectedImageRef ?: "Belum ada file cocok / fallback hanya imageUrl"}")
                        Text("Approval: ${state.approvalLimitationNote}")
                        Text("Void contract: ${state.voidContractNote}")
                    }
                }

                if (state.availableProducts.isEmpty()) {
                    Text("Belum ada produk untuk diinspeksi.")
                } else {
                    Text("Pilih produk", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        state.availableProducts.chunked(3).forEach { rowProducts ->
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                rowProducts.forEach { product ->
                                    FilterChip(
                                        selected = state.selectedProductId == product.id,
                                        onClick = { onSelectProduct(product.id) },
                                        label = { Text("${product.name} (${product.sku})") }
                                    )
                                }
                            }
                        }
                    }
                }

                selectedProduct?.let { product ->
                    Surface(
                        tonalElevation = 1.dp,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Current state", fontWeight = FontWeight.Bold)
                            Text(product.name)
                            Text("SKU: ${product.sku}")
                            val balance = state.selectedReadback?.balance
                            if (balance != null) {
                                Text("Qty saat ini: ${balance.quantity}")
                                Text("Rotation policy: ${balance.rotationPolicy.name}")
                                Text("Last ledger: ${balance.lastLedgerEntryId ?: "-"}")
                            } else {
                                Text("Belum ada row inventory_balance final. Current state dibaca sebagai 0 sampai ada mutasi final.")
                            }
                        }
                    }
                }

                Surface(
                    tonalElevation = 1.dp,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Stock opname count", fontWeight = FontWeight.Bold)
                        InventoryQuantityInput(
                            label = "Counted qty",
                            value = state.countQuantityInput,
                            onValueChange = onCountQuantityChanged,
                            helperText = "Count menghasilkan discrepancy dulu. Tidak auto-adjust."
                        )
                        Button(onClick = onSubmitCount, modifier = Modifier.fillMaxWidth()) {
                            Text("Rekam Count")
                        }
                    }
                }

                Surface(
                    tonalElevation = 1.dp,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Lightweight adjustment", fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = state.adjustmentDirection == InventoryAdjustmentDirection.INCREASE,
                                onClick = { onAdjustmentDirectionChanged(InventoryAdjustmentDirection.INCREASE) },
                                label = { Text("Tambah") }
                            )
                            FilterChip(
                                selected = state.adjustmentDirection == InventoryAdjustmentDirection.DECREASE,
                                onClick = { onAdjustmentDirectionChanged(InventoryAdjustmentDirection.DECREASE) },
                                label = { Text("Kurangi") }
                            )
                        }
                        InventoryQuantityInput(
                            label = "Qty adjustment",
                            value = state.adjustmentQuantityInput,
                            onValueChange = onAdjustmentQuantityChanged,
                            helperText = "Mutasi final wajib punya reason code inventory."
                        )
                        InventoryReasonOptionGroup(
                            options = state.adjustmentReasonOptions,
                            selectedCode = state.adjustmentReasonCode,
                            onSelected = onAdjustmentReasonCodeChanged
                        )
                        InventoryFormField(
                            label = "Catatan adjustment / investigasi",
                            value = state.adjustmentReasonDetail,
                            onValueChange = onAdjustmentReasonDetailChanged
                        )
                        Text(
                            "Status operasi: Reason wajib durable. Jika reason/policy meminta approval, jalur yang shipped saat ini adalah LIGHT_PIN.",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Button(onClick = onApplyAdjustment, modifier = Modifier.fillMaxWidth()) {
                            Text("Simpan Adjustment")
                        }
                    }
                }

                Surface(
                    tonalElevation = 1.dp,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Discrepancy review queue", fontWeight = FontWeight.Bold)
                        if (state.unresolvedDiscrepancies.isEmpty()) {
                            Text("Belum ada discrepancy unresolved.")
                        } else {
                            state.unresolvedDiscrepancies.forEach { review ->
                                InventoryDiscrepancyRow(
                                    review = review,
                                    productLabel = state.availableProducts.firstOrNull { it.id == review.productId }?.name
                                        ?: review.productId,
                                    onResolve = onResolveDiscrepancy,
                                    onMarkInvestigation = onMarkInvestigation,
                                    onDefer = onDeferDiscrepancy
                                )
                            }
                        }
                    }
                }

                Surface(
                    tonalElevation = 1.dp,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Needs Approval", fontWeight = FontWeight.Bold)
                        if (state.pendingApprovalActions.isEmpty()) {
                            Text("Belum ada action inventory yang menunggu LIGHT_PIN.")
                        } else {
                            state.pendingApprovalActions.forEach { action ->
                                InventoryPendingApprovalRow(
                                    action = action,
                                    productLabel = state.availableProducts.firstOrNull { it.id == action.productId }?.name
                                        ?: action.productId,
                                    onApprove = onApproveAction,
                                    onDeny = onDenyAction
                                )
                            }
                        }
                    }
                }

                Surface(
                    tonalElevation = 1.dp,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Explanation trail", fontWeight = FontWeight.Bold)
                        val selectedReadback = state.selectedReadback
                        if (selectedReadback?.ledgerEntries.isNullOrEmpty()) {
                            Text("Belum ada stock_ledger_entry untuk produk ini.")
                        } else {
                            selectedReadback.ledgerEntries.forEach { entry ->
                                InventoryLedgerRow(entry)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            OutlinedButton(onClick = onDismiss) { Text("Tutup") }
        },
        dismissButton = {}
    )
}

@Composable
private fun InventoryLedgerRow(entry: StockLedgerEntry) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text("${entry.mutationType.name} | ${entry.quantityDelta}", fontWeight = FontWeight.Bold)
            Text("Source: ${entry.sourceType.name} / ${entry.sourceId}")
            Text("Line: ${entry.sourceLineId ?: "-"}")
            Text("Reason: ${entry.reasonCode ?: "-"} | Status: ${entry.status.name}")
        }
    }
}

@Composable
private fun InventoryDiscrepancyRow(
    review: InventoryDiscrepancyReview,
    productLabel: String,
    onResolve: (String) -> Unit,
    onMarkInvestigation: (String) -> Unit,
    onDefer: (String) -> Unit
) {
    Surface(
        color = when (review.status) {
            InventoryDiscrepancyStatus.PENDING_REVIEW -> MaterialTheme.colorScheme.errorContainer
            InventoryDiscrepancyStatus.INVESTIGATION_REQUIRED -> MaterialTheme.colorScheme.secondaryContainer
            else -> MaterialTheme.colorScheme.surfaceVariant
        },
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(productLabel, fontWeight = FontWeight.Bold)
            Text("Book ${review.bookQuantity} | Counted ${review.countedQuantity} | Variance ${review.varianceQuantity}")
            Text("Status: ${review.status.name} | Approval: ${review.approvalMode.name}")
            Text("Source: ${review.sourceType.name} / ${review.sourceId}")
            Text("Reason: ${review.reasonCode ?: "-"}")
            if (review.status == InventoryDiscrepancyStatus.PENDING_REVIEW) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { onResolve(review.id) }, modifier = Modifier.weight(1f)) {
                        Text("Approve")
                    }
                    OutlinedButton(onClick = { onMarkInvestigation(review.id) }, modifier = Modifier.weight(1f)) {
                        Text("Investigasi")
                    }
                    OutlinedButton(onClick = { onDefer(review.id) }, modifier = Modifier.weight(1f)) {
                        Text("Defer")
                    }
                }
            }
        }
    }
}

@Composable
private fun InventoryPendingApprovalRow(
    action: InventoryApprovalAction,
    productLabel: String,
    onApprove: (String) -> Unit,
    onDeny: (String) -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.tertiaryContainer,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(productLabel, fontWeight = FontWeight.Bold)
            Text("Status: Needs Approval | Mode: ${action.approvalMode.name}")
            Text("Action: ${action.actionType.name} | Delta: ${action.quantityDelta}")
            Text("Reason: ${action.reasonCode} | Requested by: ${action.requestedBy}")
            action.reasonDetail?.takeIf { it.isNotBlank() }?.let { Text("Catatan: $it") }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onApprove(action.id) }, modifier = Modifier.weight(1f)) {
                    Text("Approve")
                }
                OutlinedButton(onClick = { onDeny(action.id) }, modifier = Modifier.weight(1f)) {
                    Text("Deny")
                }
            }
        }
    }
}

@Composable
private fun InventoryReasonOptionGroup(
    options: List<ReasonOption>,
    selectedCode: String,
    onSelected: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Reason code", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        options.chunked(2).forEach { rowOptions ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowOptions.forEach { option ->
                    FilterChip(
                        selected = selectedCode == option.code,
                        onClick = { onSelected(option.code) },
                        label = { Text(option.title) }
                    )
                }
            }
        }
    }
}

@Composable
private fun InventoryFormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        androidx.compose.material3.OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            shape = RoundedCornerShape(12.dp)
        )
    }
}

@Composable
private fun InventoryQuantityInput(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    helperText: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )
        Text(
            text = helperText,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}
