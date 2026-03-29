package id.azureenterprise.cassy.desktop

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.azureenterprise.cassy.inventory.domain.InventoryApprovalAction
import id.azureenterprise.cassy.inventory.domain.InventoryDiscrepancyReview
import id.azureenterprise.cassy.inventory.domain.InventoryDiscrepancyStatus
import id.azureenterprise.cassy.inventory.domain.StockLedgerEntry

/**
 * CassyInventoryComponents: Root component for Inventory workspace.
 */
@Composable
fun CassyInventoryComponents(
    state: DesktopAppState,
    onSelectInventoryRoute: (DesktopInventoryRoute) -> Unit,
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
    onDeferDiscrepancy: (String) -> Unit,
    onSelectMasterCategory: (String?) -> Unit,
    onMasterSearchChanged: (String) -> Unit,
    onPrepareNewMasterProduct: () -> Unit,
    onSelectMasterProduct: (String) -> Unit,
    onMasterProductNameChanged: (String) -> Unit,
    onMasterProductSkuChanged: (String) -> Unit,
    onMasterProductPriceChanged: (String) -> Unit,
    onMasterProductCategoryChanged: (String) -> Unit,
    onMasterProductImageRefChanged: (String) -> Unit,
    onMasterProductActiveChanged: (Boolean) -> Unit,
    onMasterBarcodeDraftChanged: (String) -> Unit,
    onMasterBarcodeTypeChanged: (String) -> Unit,
    onSaveMasterProduct: () -> Unit,
    onAddMasterBarcode: () -> Unit,
    onRemoveMasterBarcode: (String) -> Unit,
    onNewCategoryNameChanged: (String) -> Unit,
    onNewCategoryColorChanged: (String) -> Unit,
    onSaveMasterCategory: () -> Unit
) {
    // This is a placeholder for the actual layout which would likely be in DesktopWorkspaceScreens
    // or a dedicated screen here. Since I am refactoring, I will keep the logic simple.
    Text("Inventory Hub Content")
}

@Composable
fun InventoryTruthDialogContent(
    state: InventoryPanelState,
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
    onDeferDiscrepancy: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedProduct = state.availableProducts.firstOrNull { it.id == state.selectedProductId }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            tonalElevation = 1.dp,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Kondisi stok dan jejak perubahan", fontWeight = FontWeight.Bold)
                Text("Saldo stok menunjukkan kondisi saat ini. Riwayat perubahan dipakai untuk menjelaskan kenapa stok berubah.")
            }
        }

        if (state.availableProducts.isEmpty()) {
            Text("Belum ada produk untuk diinspeksi.")
        } else {
            Text("Pilih produk", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            // Chips or List here
        }
    }
}
