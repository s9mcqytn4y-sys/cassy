package id.azureenterprise.cassy.desktop

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.azureenterprise.cassy.kernel.domain.CashMovementType
import id.azureenterprise.cassy.kernel.domain.OperationStatus
import id.azureenterprise.cassy.kernel.domain.OperationType
import id.azureenterprise.cassy.kernel.domain.PendingApprovalSummary
import id.azureenterprise.cassy.masterdata.data.ProductBarcodeRecord
import id.azureenterprise.cassy.masterdata.domain.Product
import id.azureenterprise.cassy.sales.domain.SaleHistoryEntry

/**
 * DesktopWorkspaceContent: Root container for all workspace screens.
 * Ensures layout is responsive and alur kerja is functional.
 */
@Composable
fun DesktopWorkspaceContent(
    state: DesktopAppState,
    onOpenDay: () -> Unit,
    onOpeningCashChanged: (String) -> Unit,
    onOpeningCashReasonChanged: (String) -> Unit,
    onStartShift: () -> Unit,
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
    onReprintLastReceipt: () -> Unit,
    onCancelSale: () -> Unit,
    onSelectInventoryRoute: (DesktopInventoryRoute) -> Unit,
    onSelectInventoryProduct: (String) -> Unit,
    onInventoryCountChanged: (String) -> Unit,
    onSubmitInventoryCount: () -> Unit,
    onInventoryAdjustmentDirectionChanged: (InventoryAdjustmentDirection) -> Unit,
    onInventoryAdjustmentQuantityChanged: (String) -> Unit,
    onInventoryAdjustmentReasonChanged: (String) -> Unit,
    onInventoryAdjustmentDetailChanged: (String) -> Unit,
    onApplyInventoryAdjustment: () -> Unit,
    onResolveInventoryDiscrepancy: (String) -> Unit,
    onMarkInventoryInvestigation: (String) -> Unit,
    onApproveInventoryAction: (String) -> Unit,
    onDenyInventoryAction: (String) -> Unit,
    onDeferInventoryDiscrepancy: (String) -> Unit,
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
    onSaveMasterCategory: () -> Unit,
    onSelectOperationsRoute: (DesktopOperationsRoute) -> Unit,
    onCashMovementTypeSelected: (CashMovementType) -> Unit,
    onCashMovementAmountChanged: (String) -> Unit,
    onCashReasonCodeChanged: (String) -> Unit,
    onCashReasonDetailChanged: (String) -> Unit,
    onSubmitCashMovement: () -> Unit,
    onApproveCashMovement: (String) -> Unit,
    onDenyCashMovement: (String) -> Unit,
    onSelectVoidSale: (String) -> Unit,
    onVoidReasonCodeChanged: (String) -> Unit,
    onVoidReasonDetailChanged: (String) -> Unit,
    onVoidInventoryFollowUpChanged: (String) -> Unit,
    onExecuteVoid: () -> Unit,
    onClosingCashChanged: (String) -> Unit,
    onCloseShiftReasonCodeChanged: (String) -> Unit,
    onCloseShiftReasonDetailChanged: (String) -> Unit,
    onCloseShift: () -> Unit,
    onApproveCloseShift: (String) -> Unit,
    onDenyCloseShift: (String) -> Unit,
    onCloseBusinessDay: () -> Unit,
    onSync: () -> Unit,
    onExportReport: () -> Unit,
    onStoreProfileFieldChanged: (StoreProfileUiField, String) -> Unit,
    onStoreProfileToggleChanged: (StoreProfileToggleField, Boolean) -> Unit,
    onSelectStoreLogo: () -> Unit,
    onClearStoreLogo: () -> Unit,
    onSaveStoreProfile: () -> Unit,
    onSelectWorkspace: (DesktopWorkspace) -> Unit,
    onRefreshDashboard: () -> Unit,
    onApproveGeneric: (PendingApprovalSummary) -> Unit,
    onDenyGeneric: (PendingApprovalSummary) -> Unit
) {
    when (state.activeWorkspace) {
        DesktopWorkspace.Dashboard -> DashboardWorkspace(
            state = state,
            onSelectWorkspace = onSelectWorkspace,
            onCloseBusinessDay = onCloseBusinessDay,
            onApproveApproval = onApproveGeneric,
            onDenyApproval = onDenyGeneric,
            onForceCloseShift = {
                onSelectOperationsRoute(DesktopOperationsRoute.CloseShift)
            },
            onRefresh = onRefreshDashboard
        )
        DesktopWorkspace.Cashier -> CheckoutWorkspace(
            state = state,
            onSearchChanged = onSearchChanged,
            onBarcodeChanged = onBarcodeChanged,
            onScanBarcode = onScanBarcode,
            onAddProduct = onAddProduct,
            onCashReceivedChanged = onCashReceivedChanged,
            onConfirmCartReview = onConfirmCartReview,
            onMemberNumberChanged = onMemberNumberChanged,
            onMemberNameChanged = onMemberNameChanged,
            onSkipMember = onSkipMember,
            onDonationEnabledChanged = onDonationEnabledChanged,
            onDonationAmountChanged = onDonationAmountChanged,
            onSkipDonation = onSkipDonation,
            onIncrement = onIncrement,
            onDecrement = onDecrement,
            onCheckoutCash = onCheckoutCash,
            onPrintLastReceipt = onPrintLastReceipt,
            onCancelSale = onCancelSale
        )
        DesktopWorkspace.History -> HistoryWorkspace(state.catalog.recentSales)
        DesktopWorkspace.Inventory -> InventoryWorkspace(
            state = state,
            onSelectInventoryRoute = onSelectInventoryRoute,
            onSelectProduct = onSelectInventoryProduct,
            onCountQuantityChanged = onInventoryCountChanged,
            onSubmitCount = onSubmitInventoryCount,
            onAdjustmentDirectionChanged = onInventoryAdjustmentDirectionChanged,
            onAdjustmentQuantityChanged = onInventoryAdjustmentQuantityChanged,
            onAdjustmentReasonCodeChanged = onInventoryAdjustmentReasonChanged,
            onAdjustmentReasonDetailChanged = onInventoryAdjustmentDetailChanged,
            onApplyAdjustment = onApplyInventoryAdjustment,
            onResolveDiscrepancy = onResolveInventoryDiscrepancy,
            onMarkInvestigation = onMarkInventoryInvestigation,
            onApproveAction = onApproveInventoryAction,
            onDenyAction = onDenyInventoryAction,
            onDeferDiscrepancy = onDeferInventoryDiscrepancy,
            onSelectMasterCategory = onSelectMasterCategory,
            onMasterSearchChanged = onMasterSearchChanged,
            onPrepareNewMasterProduct = onPrepareNewMasterProduct,
            onSelectMasterProduct = onSelectMasterProduct,
            onMasterProductNameChanged = onMasterProductNameChanged,
            onMasterProductSkuChanged = onMasterProductSkuChanged,
            onMasterProductPriceChanged = onMasterProductPriceChanged,
            onMasterProductCategoryChanged = onMasterProductCategoryChanged,
            onMasterProductImageRefChanged = onMasterProductImageRefChanged,
            onMasterProductActiveChanged = onMasterProductActiveChanged,
            onMasterBarcodeDraftChanged = onMasterBarcodeDraftChanged,
            onMasterBarcodeTypeChanged = onMasterBarcodeTypeChanged,
            onSaveMasterProduct = onSaveMasterProduct,
            onAddMasterBarcode = onAddMasterBarcode,
            onRemoveMasterBarcode = onRemoveMasterBarcode,
            onNewCategoryNameChanged = onNewCategoryNameChanged,
            onNewCategoryColorChanged = onNewCategoryColorChanged,
            onSaveMasterCategory = onSaveMasterCategory
        )
        DesktopWorkspace.Operations -> OperationsWorkspace(
            state = state,
            onSelectRoute = onSelectOperationsRoute,
            onCashMovementTypeSelected = onCashMovementTypeSelected,
            onCashMovementAmountChanged = onCashMovementAmountChanged,
            onCashReasonCodeChanged = onCashReasonCodeChanged,
            onCashReasonDetailChanged = onCashReasonDetailChanged,
            onSubmitCashMovement = onSubmitCashMovement,
            onApproveCashMovement = onApproveCashMovement,
            onDenyCashMovement = onDenyCashMovement,
            onSelectVoidSale = onSelectVoidSale,
            onVoidReasonCodeChanged = onVoidReasonCodeChanged,
            onVoidReasonDetailChanged = onVoidReasonDetailChanged,
            onVoidInventoryFollowUpChanged = onVoidInventoryFollowUpChanged,
            onExecuteVoid = onExecuteVoid,
            onClosingCashChanged = onClosingCashChanged,
            onCloseShiftReasonCodeChanged = onCloseShiftReasonCodeChanged,
            onCloseShiftReasonDetailChanged = onCloseShiftReasonDetailChanged,
            onCloseShift = onCloseShift,
            onApproveCloseShift = onApproveCloseShift,
            onDenyCloseShift = onDenyCloseShift,
            onForceCloseShift = {
                onSelectOperationsRoute(DesktopOperationsRoute.CloseShift)
            }
        )
        DesktopWorkspace.Settings -> SettingsWorkspace(
            state = state,
            onSync = onSync,
            onExportReport = onExportReport,
            onFieldChanged = onStoreProfileFieldChanged,
            onToggleChanged = onStoreProfileToggleChanged,
            onSelectLogo = onSelectStoreLogo,
            onClearLogo = onClearStoreLogo,
            onSaveProfile = onSaveStoreProfile
        )
        DesktopWorkspace.Reporting -> WorkspacePage("Laporan", "Review performa bisnis.") {
            WorkspaceCard("Laporan Harian") {
                Text("Fitur laporan sedang disiapkan.")
            }
        }
        DesktopWorkspace.System -> WorkspacePage("Sistem", "Informasi sistem dan pemeliharaan.") {
            WorkspaceCard("Status Sistem") {
                Text("Fitur sistem sedang disiapkan.")
            }
        }
    }
}

@Composable
private fun WorkspacePage(title: String, subtitle: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(24.dp))
        content()
    }
}

@Composable
private fun WorkspaceCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
private fun DashboardWorkspace(
    state: DesktopAppState,
    onSelectWorkspace: (DesktopWorkspace) -> Unit,
    onCloseBusinessDay: () -> Unit,
    onApproveApproval: (PendingApprovalSummary) -> Unit,
    onDenyApproval: (PendingApprovalSummary) -> Unit,
    onForceCloseShift: () -> Unit,
    onRefresh: () -> Unit
) {
    val scrollState = rememberScrollState()
    WorkspacePage("Dashboard", "Selamat datang di Cassy POS. Berikut ringkasan performa hari ini.") {
        Column(modifier = Modifier.verticalScroll(scrollState).weight(1f), verticalArrangement = Arrangement.spacedBy(24.dp)) {
            // Quick Stats
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatCard("Total Penjualan", "Rp ${state.operations.dashboard.totalSales.toInt()}", Icons.AutoMirrored.Filled.TrendingUp, MaterialTheme.colorScheme.primary, Modifier.weight(1f))
                StatCard("Transaksi", state.operations.dashboard.transactionCount.toString(), Icons.Default.Receipt, MaterialTheme.colorScheme.secondary, Modifier.weight(1f))
                StatCard("Avg Basket", "Rp ${state.operations.dashboard.averageBasket.toInt()}", Icons.Default.ShoppingCart, MaterialTheme.colorScheme.tertiary, Modifier.weight(1f))
            }

            // Pending Approvals
            WorkspaceCard("Persetujuan Tertunda") {
                if (state.operations.dashboard.pendingApprovals.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                            Text("Semua beres! Tidak ada antrean persetujuan.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        state.operations.dashboard.pendingApprovals.forEach { approval ->
                            ApprovalRow(approval, onApprove = { onApproveApproval(approval) }, onDeny = { onDenyApproval(approval) })
                        }
                    }
                }
            }

            // Shift Status
            WorkspaceCard("Status Shift Aktif") {
                state.operations.dashboard.activeShifts.forEach { shift ->
                    ShiftStatusItem(shift, onForceClose = onForceCloseShift)
                }
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 2.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = CircleShape, color = color.copy(alpha = 0.1f), modifier = Modifier.size(48.dp)) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.padding(12.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
private fun ApprovalRow(approval: PendingApprovalSummary, onApprove: () -> Unit, onDeny: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            val (icon, tint) = when (approval.type) {
                OperationType.VOID_SALE -> Icons.Default.DeleteSweep to MaterialTheme.colorScheme.error
                OperationType.STOCK_ADJUSTMENT -> Icons.Default.Inventory to MaterialTheme.colorScheme.primary
                OperationType.CASH_IN, OperationType.CASH_OUT, OperationType.SAFE_DROP -> Icons.Default.AccountBalanceWallet to MaterialTheme.colorScheme.secondary
                OperationType.CLOSE_SHIFT -> Icons.Default.LockClock to MaterialTheme.colorScheme.tertiary
                else -> Icons.AutoMirrored.Filled.Help to MaterialTheme.colorScheme.onSurfaceVariant
            }

            Surface(shape = CircleShape, color = tint.copy(alpha = 0.1f), modifier = Modifier.size(40.dp)) {
                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.padding(8.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(approval.type.name.replace("([a-z])([A-Z])".toRegex(), "$1 $2"), fontWeight = FontWeight.Bold)
                Text("Oleh: ${approval.requestedBy} • ${approval.formattedTimestamp}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = onDeny) { Icon(Icons.Default.Close, contentDescription = "Tolak", tint = MaterialTheme.colorScheme.error) }
                IconButton(onClick = onApprove) { Icon(Icons.Default.Check, contentDescription = "Setujui", tint = MaterialTheme.colorScheme.primary) }
            }
        }
    }
}

@Composable
private fun ShiftStatusItem(shift: id.azureenterprise.cassy.kernel.domain.ActiveShiftSummary, onForceClose: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 1.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("Kasir: ${shift.cashierName}", fontWeight = FontWeight.Bold)
                Text("Mulai: ${shift.formattedStartTime}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(8.dp))
                MetricRowV2("Penjualan", "Rp ${shift.currentSales.toInt()}")
                MetricRowV2("Tunai Diharapkan", "Rp ${shift.expectedCash.toInt()}")
            }
            val isAlert = shift.durationHours > 12
            if (isAlert) {
                Button(
                    onClick = onForceClose,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("PAKSA TUTUP", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onError, fontWeight = FontWeight.Bold)
                }
            } else {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun MetricRowV2(label: String, value: String, isAlert: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = if (isAlert) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun CheckoutWorkspace(
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
    CassyCatalogScreen(
        state = state,
        onSearchChanged = onSearchChanged,
        onBarcodeChanged = onBarcodeChanged,
        onScanBarcode = onScanBarcode,
        onAddProduct = onAddProduct,
        onCashReceivedChanged = onCashReceivedChanged,
        onConfirmCartReview = onConfirmCartReview,
        onMemberNumberChanged = onMemberNumberChanged,
        onMemberNameChanged = onMemberNameChanged,
        onSkipMember = onSkipMember,
        onDonationEnabledChanged = onDonationEnabledChanged,
        onDonationAmountChanged = onDonationAmountChanged,
        onSkipDonation = onSkipDonation,
        onIncrement = onIncrement,
        onDecrement = onDecrement,
        onCheckoutCash = onCheckoutCash,
        onPrintLastReceipt = onPrintLastReceipt,
        onCancelSale = onCancelSale
    )
}

@Composable
private fun HistoryWorkspace(recentSales: List<SaleHistoryEntry>) {
    WorkspacePage("Riwayat Transaksi", "Review transaksi final terbaru di terminal ini.") {
        WorkspaceCard("Transaksi Terakhir") {
            if (recentSales.isEmpty()) {
                Text("Belum ada riwayat transaksi.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.heightIn(max = 600.dp)) {
                    items(recentSales) { sale ->
                        Surface(shape = RoundedCornerShape(12.dp), tonalElevation = 1.dp, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text("Nota ${sale.localNumber}", fontWeight = FontWeight.Bold)
                                    Text(sale.paymentMethod, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text("Rp ${sale.finalAmount.toInt()}", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InventoryWorkspace(
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
    WorkspacePage("Inventori", "Kelola stok barang dan audit stok.") {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            NavigationTab(
                "Stok Opname",
                state.inventoryRoute == DesktopInventoryRoute.StockCount,
                onClick = { onSelectInventoryRoute(DesktopInventoryRoute.StockCount) }
            )
            NavigationTab(
                "Penyesuaian",
                state.inventoryRoute == DesktopInventoryRoute.Adjustment,
                onClick = { onSelectInventoryRoute(DesktopInventoryRoute.Adjustment) }
            )
            NavigationTab(
                "Discrepancy",
                state.inventoryRoute == DesktopInventoryRoute.Discrepancy,
                onClick = { onSelectInventoryRoute(DesktopInventoryRoute.Discrepancy) }
            )
            NavigationTab(
                "Master Data",
                state.inventoryRoute == DesktopInventoryRoute.MasterData,
                onClick = { onSelectInventoryRoute(DesktopInventoryRoute.MasterData) }
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        when (state.inventoryRoute) {
            DesktopInventoryRoute.StockOverview -> WorkspaceCard("Ringkasan Stok") {
                Text("Review ketersediaan stok produk.")
            }
            DesktopInventoryRoute.StockCount -> StockCountView(
                state = state.inventory,
                onSelectProduct = onSelectProduct,
                onQuantityChanged = onCountQuantityChanged,
                onSubmit = onSubmitCount
            )
            DesktopInventoryRoute.Adjustment -> StockAdjustmentView(
                state = state.inventory,
                onSelectProduct = onSelectProduct,
                onDirectionChanged = onAdjustmentDirectionChanged,
                onQuantityChanged = onAdjustmentQuantityChanged,
                onReasonCodeChanged = onAdjustmentReasonCodeChanged,
                onReasonDetailChanged = onAdjustmentReasonDetailChanged,
                onApply = onApplyAdjustment
            )
            DesktopInventoryRoute.Discrepancy -> DiscrepancyResolutionView(
                state = state.inventory,
                onResolve = onResolveDiscrepancy,
                onInvestigate = onMarkInvestigation,
                onApprove = onApproveAction,
                onDeny = onDenyAction,
                onDefer = onDeferDiscrepancy
            )
            DesktopInventoryRoute.MasterData -> MasterDataManagementView(
                state = state.masterData,
                onSelectCategory = onSelectMasterCategory,
                onSearchChanged = onMasterSearchChanged,
                onPrepareNewProduct = onPrepareNewMasterProduct,
                onSelectProduct = onSelectMasterProduct,
                onProductNameChanged = onMasterProductNameChanged,
                onProductSkuChanged = onMasterProductSkuChanged,
                onProductPriceChanged = onMasterProductPriceChanged,
                onProductCategoryChanged = onMasterProductCategoryChanged,
                onProductImageRefChanged = onMasterProductImageRefChanged,
                onProductActiveChanged = onMasterProductActiveChanged,
                onBarcodeDraftChanged = onMasterBarcodeDraftChanged,
                onBarcodeTypeChanged = onMasterBarcodeTypeChanged,
                onSaveProduct = onSaveMasterProduct,
                onAddBarcode = onAddMasterBarcode,
                onRemoveBarcode = onRemoveMasterBarcode,
                onNewCategoryNameChanged = onNewCategoryNameChanged,
                onNewCategoryColorChanged = onNewCategoryColorChanged,
                onSaveCategory = onSaveMasterCategory
            )
        }
    }
}

@Composable
private fun OperationsWorkspace(
    state: DesktopAppState,
    onSelectRoute: (DesktopOperationsRoute) -> Unit,
    onCashMovementTypeSelected: (CashMovementType) -> Unit,
    onCashMovementAmountChanged: (String) -> Unit,
    onCashReasonCodeChanged: (String) -> Unit,
    onCashReasonDetailChanged: (String) -> Unit,
    onSubmitCashMovement: () -> Unit,
    onApproveCashMovement: (String) -> Unit,
    onDenyCashMovement: (String) -> Unit,
    onSelectVoidSale: (String) -> Unit,
    onVoidReasonCodeChanged: (String) -> Unit,
    onVoidReasonDetailChanged: (String) -> Unit,
    onVoidInventoryFollowUpChanged: (String) -> Unit,
    onExecuteVoid: () -> Unit,
    onClosingCashChanged: (String) -> Unit,
    onCloseShiftReasonCodeChanged: (String) -> Unit,
    onCloseShiftReasonDetailChanged: (String) -> Unit,
    onCloseShift: () -> Unit,
    onApproveCloseShift: (String) -> Unit,
    onDenyCloseShift: (String) -> Unit,
    onForceCloseShift: () -> Unit
) {
    WorkspacePage("Operasional", "Manajemen shift, kas, dan pembatalan transaksi.") {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            NavigationTab("Alur Kas", state.operationsRoute == DesktopOperationsRoute.CashControl, onClick = { onSelectRoute(DesktopOperationsRoute.CashControl) })
            NavigationTab("Void Nota", state.operationsRoute == DesktopOperationsRoute.VoidSale, onClick = { onSelectRoute(DesktopOperationsRoute.VoidSale) })
            NavigationTab("Tutup Shift", state.operationsRoute == DesktopOperationsRoute.CloseShift, onClick = { onSelectRoute(DesktopOperationsRoute.CloseShift) })
        }
        Spacer(modifier = Modifier.height(24.dp))
        when (state.operationsRoute) {
            DesktopOperationsRoute.CashControl -> CashMovementView(
                state = state.operations,
                onTypeSelected = onCashMovementTypeSelected,
                onAmountChanged = onCashMovementAmountChanged,
                onReasonCodeChanged = onCashReasonCodeChanged,
                onReasonDetailChanged = onCashReasonDetailChanged,
                onSubmit = onSubmitCashMovement,
                onApprove = onApproveCashMovement,
                onDeny = onDenyCashMovement
            )
            DesktopOperationsRoute.VoidSale -> VoidSaleView(
                state = state.operations,
                onSelectSale = onSelectVoidSale,
                onReasonCodeChanged = onVoidReasonCodeChanged,
                onReasonDetailChanged = onVoidReasonDetailChanged,
                onInventoryFollowUpChanged = onVoidInventoryFollowUpChanged,
                onExecute = onExecuteVoid
            )
            DesktopOperationsRoute.CloseShift -> CloseShiftView(
                state = state.operations,
                onCashChanged = onClosingCashChanged,
                onReasonCodeChanged = onCloseShiftReasonCodeChanged,
                onReasonDetailChanged = onCloseShiftReasonDetailChanged,
                onClose = onCloseShift,
                onApprove = onApproveCloseShift,
                onDeny = onDenyCloseShift,
                onForceClose = onForceCloseShift
            )
            DesktopOperationsRoute.CloseDay -> WorkspaceCard("Tutup Hari") {
                Text("Selesaikan semua shift untuk menutup hari bisnis.")
            }
            DesktopOperationsRoute.SyncCenter -> WorkspaceCard("Sync Center") {
                Text("Review antrean sinkronisasi data.")
            }
            DesktopOperationsRoute.Diagnostics -> WorkspaceCard("Diagnostik") {
                Text("Cek kesehatan sistem dan hardware.")
            }
        }
    }
}

@Composable
private fun SettingsWorkspace(
    state: DesktopAppState,
    onSync: () -> Unit,
    onExportReport: () -> Unit,
    onFieldChanged: (StoreProfileUiField, String) -> Unit,
    onToggleChanged: (StoreProfileToggleField, Boolean) -> Unit,
    onSelectLogo: () -> Unit,
    onClearLogo: () -> Unit,
    onSaveProfile: () -> Unit
) {
    WorkspacePage("Pengaturan", "Konfigurasi aplikasi dan profil toko.") {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            NavigationTab("Profil Toko", state.shell.storeName != null, onClick = { /* TODO */ })
            NavigationTab("Sistem", false, onClick = { /* TODO */ })
        }
        Spacer(modifier = Modifier.height(24.dp))
        StoreProfileView(
            state = state.storeProfile,
            onFieldChanged = onFieldChanged,
            onToggleChanged = onToggleChanged,
            onSelectLogo = onSelectLogo,
            onClearLogo = onClearLogo,
            onSave = onSaveProfile
        )
    }
}

@Composable
private fun NavigationTab(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
        contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        Text(label, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun StockCountView(state: InventoryPanelState, onSelectProduct: (String) -> Unit, onQuantityChanged: (String) -> Unit, onSubmit: () -> Unit) {
    WorkspaceCard("Input Stok Fisik") {
        Text("Pilih produk dan masukkan jumlah yang ditemukan di rak.", style = MaterialTheme.typography.bodySmall)
        // ... implementation ...
    }
}

@Composable
private fun StockAdjustmentView(state: InventoryPanelState, onSelectProduct: (String) -> Unit, onDirectionChanged: (InventoryAdjustmentDirection) -> Unit, onQuantityChanged: (String) -> Unit, onReasonCodeChanged: (String) -> Unit, onReasonDetailChanged: (String) -> Unit, onApply: () -> Unit) {
    WorkspaceCard("Koreksi Manual") {
        Text("Gunakan untuk penyesuaian stok di luar transaksi regular (misal: barang rusak).", style = MaterialTheme.typography.bodySmall)
        // ... implementation ...
    }
}

@Composable
private fun DiscrepancyResolutionView(state: InventoryPanelState, onResolve: (String) -> Unit, onInvestigate: (String) -> Unit, onApprove: (String) -> Unit, onDeny: (String) -> Unit, onDefer: (String) -> Unit) {
    WorkspaceCard("Daftar Selisih") {
        Text("Daftar perbedaan antara stok sistem dan hasil opname yang butuh keputusan.", style = MaterialTheme.typography.bodySmall)
        // ... implementation ...
    }
}

@Composable
private fun MasterDataManagementView(
    state: MasterDataPanelState,
    onSelectCategory: (String?) -> Unit,
    onSearchChanged: (String) -> Unit,
    onPrepareNewProduct: () -> Unit,
    onSelectProduct: (String) -> Unit,
    onProductNameChanged: (String) -> Unit,
    onProductSkuChanged: (String) -> Unit,
    onProductPriceChanged: (String) -> Unit,
    onProductCategoryChanged: (String) -> Unit,
    onProductImageRefChanged: (String) -> Unit,
    onProductActiveChanged: (Boolean) -> Unit,
    onBarcodeDraftChanged: (String) -> Unit,
    onBarcodeTypeChanged: (String) -> Unit,
    onSaveProduct: () -> Unit,
    onAddBarcode: () -> Unit,
    onRemoveBarcode: (String) -> Unit,
    onNewCategoryNameChanged: (String) -> Unit,
    onNewCategoryColorChanged: (String) -> Unit,
    onSaveCategory: () -> Unit
) {
    // ... implementation ...
}

@Composable
private fun CashMovementView(state: OperationsState, onTypeSelected: (CashMovementType) -> Unit, onAmountChanged: (String) -> Unit, onReasonCodeChanged: (String) -> Unit, onReasonDetailChanged: (String) -> Unit, onSubmit: () -> Unit, onApprove: (String) -> Unit, onDeny: (String) -> Unit) {
    // ... implementation ...
}

@Composable
private fun VoidSaleView(state: OperationsState, onSelectSale: (String) -> Unit, onReasonCodeChanged: (String) -> Unit, onReasonDetailChanged: (String) -> Unit, onInventoryFollowUpChanged: (String) -> Unit, onExecute: () -> Unit) {
    // ... implementation ...
}

@Composable
private fun CloseShiftView(state: OperationsState, onCashChanged: (String) -> Unit, onReasonCodeChanged: (String) -> Unit, onReasonDetailChanged: (String) -> Unit, onClose: () -> Unit, onApprove: (String) -> Unit, onDeny: (String) -> Unit, onForceClose: () -> Unit) {
    // ... implementation ...
}

@Composable
private fun StoreProfileView(state: StoreProfileState, onFieldChanged: (StoreProfileUiField, String) -> Unit, onToggleChanged: (StoreProfileToggleField, Boolean) -> Unit, onSelectLogo: () -> Unit, onClearLogo: () -> Unit, onSave: () -> Unit) {
    // ... implementation ...
}
