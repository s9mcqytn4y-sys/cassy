package id.azureenterprise.cassy.desktop

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.azureenterprise.cassy.kernel.domain.CashMovementType
import id.azureenterprise.cassy.masterdata.data.ProductBarcodeRecord
import id.azureenterprise.cassy.masterdata.domain.Product
import id.azureenterprise.cassy.sales.domain.SaleHistoryEntry

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
    onSelectWorkspace: (DesktopWorkspace) -> Unit
) {
    when (state.activeWorkspace) {
        DesktopWorkspace.Dashboard -> DashboardWorkspace(
            state = state,
            onOpenDay = onOpenDay,
            onOpeningCashChanged = onOpeningCashChanged,
            onOpeningCashReasonChanged = onOpeningCashReasonChanged,
            onStartShift = onStartShift,
            onSelectWorkspace = onSelectWorkspace
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
            onReprintLastReceipt = onReprintLastReceipt,
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
            onSelectOperationsRoute = onSelectOperationsRoute,
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
            onCloseBusinessDay = onCloseBusinessDay,
            onSync = onSync
        )
        DesktopWorkspace.Reporting -> ReportingWorkspace(state.operations, onExportReport, state.isBusy)
        DesktopWorkspace.System -> SystemWorkspace(
            state = state,
            onSync = onSync,
            onStoreProfileFieldChanged = onStoreProfileFieldChanged,
            onStoreProfileToggleChanged = onStoreProfileToggleChanged,
            onSelectStoreLogo = onSelectStoreLogo,
            onClearStoreLogo = onClearStoreLogo,
            onSaveStoreProfile = onSaveStoreProfile
        )
    }
}

@Composable
private fun DashboardWorkspace(
    state: DesktopAppState,
    onOpenDay: () -> Unit,
    onOpeningCashChanged: (String) -> Unit,
    onOpeningCashReasonChanged: (String) -> Unit,
    onStartShift: () -> Unit,
    onSelectWorkspace: (DesktopWorkspace) -> Unit
) {
    WorkspacePage(DesktopLabels.Dashboard.title, DesktopLabels.Dashboard.subtitle) {
        DashboardMilestoneStrip(state)
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
            WorkspaceCard("Langkah berikutnya", Modifier.weight(1.15f)) {
                Text(state.operations.dashboard.headline)
                state.operations.blockingMessage?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                when {
                    state.operations.businessDayLabel == null -> {
                        Text(
                            "Kasir aktif boleh membuka hari bisnis. Supervisor fokus ke hasil, approval penting, dan pemulihan bila ada masalah.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(
                            onClick = onOpenDay,
                            enabled = state.operations.canOpenDay && !state.isBusy
                        ) { Text("Buka hari bisnis") }
                    }
                    state.operations.shiftLabel == null -> {
                        CassyCurrencyInput(
                            "Modal awal tunai",
                            state.operations.openingCashInput,
                            onOpeningCashChanged,
                            helperText = "Isi sesuai uang fisik di laci kas sebelum shift dibuka."
                        )
                        SemanticTextField(
                            "Catatan singkat",
                            state.operations.openingCashReason,
                            onOpeningCashReasonChanged,
                            singleLine = false,
                            placeholder = "Opsional"
                        )
                        Button(onClick = onStartShift, enabled = !state.isBusy) { Text("Buka shift") }
                    }
                    else -> {
                        Text(
                            "Terminal sudah siap dipakai. Buka kasir untuk transaksi atau operasional bila perlu kontrol lanjutan.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            if (DesktopWorkspace.Cashier in state.shell.availableWorkspaces) {
                                Button(onClick = { onSelectWorkspace(DesktopWorkspace.Cashier) }) { Text("Masuk kasir") }
                            }
                            OutlinedButton(onClick = { onSelectWorkspace(DesktopWorkspace.Operations) }) { Text("Buka operasional") }
                        }
                    }
                }
            }
            WorkspaceCard("Status terminal", Modifier.weight(0.85f)) {
                SummaryRow("Operator aktif", humanizeOperatorLabel(state.shell.operatorName, state.shell.roleLabel))
                SummaryRow("Hari bisnis", humanizeBusinessDayLabel(state.operations.businessDayLabel))
                SummaryRow("Shift", humanizeShiftLabel(state.operations.shiftLabel))
                SummaryRow("Tindak lanjut", state.shell.nextActionLabel ?: "Tidak ada blocker utama")
                SummaryRow("Approval tertunda", state.operations.dashboard.pendingApprovalCount.toString())
            }
        }
        val importantDecisions = state.operations.dashboard.decisions
            .filter {
                it.status != id.azureenterprise.cassy.kernel.domain.OperationStatus.COMPLETED &&
                    it.status != id.azureenterprise.cassy.kernel.domain.OperationStatus.READY
            }
            .take(3)
        WorkspaceCard("Perlu perhatian") {
            if (importantDecisions.isEmpty()) {
                Text(
                    DesktopLabels.Dashboard.issuesEmpty,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                importantDecisions.forEach { decision ->
                    OperationDecisionRow(decision)
                }
            }
        }
    }
}

@Composable
private fun DashboardMilestoneStrip(state: DesktopAppState) {
    val items = listOf(
        DashboardMilestoneItem(
            title = "Operator aktif",
            detail = state.shell.operatorName ?: "Login dulu",
            state = if (state.shell.operatorName != null) DashboardMilestoneState.Done else DashboardMilestoneState.Active
        ),
        DashboardMilestoneItem(
            title = "Hari bisnis",
            detail = if (state.operations.businessDayLabel == null) "Belum dibuka" else "Sudah aktif",
            state = when {
                state.shell.operatorName == null -> DashboardMilestoneState.Pending
                state.operations.businessDayLabel == null -> DashboardMilestoneState.Active
                else -> DashboardMilestoneState.Done
            }
        ),
        DashboardMilestoneItem(
            title = "Shift aktif",
            detail = if (state.operations.shiftLabel == null) "Belum dibuka" else "Sudah aktif",
            state = when {
                state.operations.businessDayLabel == null -> DashboardMilestoneState.Pending
                state.operations.shiftLabel == null -> DashboardMilestoneState.Active
                else -> DashboardMilestoneState.Done
            }
        ),
        DashboardMilestoneItem(
            title = "Kasir siap",
            detail = if (state.operations.dashboard.canAccessSalesHome) "Bisa transaksi" else "Menunggu langkah sebelumnya",
            state = when {
                state.operations.dashboard.canAccessSalesHome -> DashboardMilestoneState.Done
                state.operations.shiftLabel != null -> DashboardMilestoneState.Active
                else -> DashboardMilestoneState.Pending
            }
        )
    )
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        items.forEach { item ->
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                color = when (item.state) {
                    DashboardMilestoneState.Done -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                    DashboardMilestoneState.Active -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.52f)
                    DashboardMilestoneState.Pending -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f)
                }
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(item.title, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Text(
                        item.detail,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private data class DashboardMilestoneItem(
    val title: String,
    val detail: String,
    val state: DashboardMilestoneState
)

private enum class DashboardMilestoneState {
    Pending,
    Active,
    Done
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
    onReprintLastReceipt: () -> Unit,
    onCancelSale: () -> Unit
) {
    WorkspacePage(
        title = DesktopLabels.Cashier.title,
        subtitle = DesktopLabels.Cashier.subtitle,
        scrollable = false
    ) {
        if (!state.operations.dashboard.canAccessSalesHome) {
            WorkspaceCard(DesktopLabels.Cashier.unavailableTitle) {
                Text(state.operations.blockingMessage ?: DesktopLabels.Cashier.unavailableDetail)
            }
            return@WorkspacePage
        }
        val milestone = remember(state.catalog) { resolveCashierMilestone(state.catalog) }
        CashierMilestoneBar(milestone = milestone, catalog = state.catalog)
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.weight(1.35f).fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Surface(
                    modifier = Modifier.weight(0.58f).fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    tonalElevation = 1.dp
                ) {
                    CassyCatalogView(
                        state = state.catalog,
                        milestone = milestone,
                        onSearchChanged = onSearchChanged,
                        onBarcodeChanged = onBarcodeChanged,
                        onScanBarcode = onScanBarcode,
                        onAddProduct = onAddProduct
                    )
                }
                WorkspaceCard("Keranjang aktif", Modifier.weight(0.42f).fillMaxWidth()) {
                    if (state.catalog.basket.items.isEmpty()) {
                        Text(
                            DesktopLabels.Cashier.emptyBasket,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            items(state.catalog.basket.items) { item ->
                                CassyCartItemRow(item, onIncrement, onDecrement)
                            }
                        }
                    }
                    HorizontalDivider()
                    CassyMetricRow("Subtotal", "Rp ${state.catalog.basket.totals.subtotal.toInt()}")
                    CassyMetricRow("Pajak", "Rp ${state.catalog.basket.totals.taxTotal.toInt()}")
                    CassyMetricRow("Total", "Rp ${state.catalog.basket.totals.finalTotal.toInt()}", isHighlight = true)
                    Button(
                        onClick = onConfirmCartReview,
                        enabled = state.catalog.basket.items.isNotEmpty() && !state.catalog.reviewConfirmed,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            if (state.catalog.reviewConfirmed) {
                                DesktopLabels.Cashier.reviewedCta
                            } else {
                                DesktopLabels.Cashier.reviewCta
                            }
                        )
                    }
                }
            }
            Column(
                modifier = Modifier.weight(0.95f).fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                WorkspaceCard(DesktopLabels.Cashier.finalizationTitle, Modifier.weight(0.66f).fillMaxWidth()) {
                    Text(
                        DesktopLabels.Cashier.finalizationIntro,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    CashierFinalizationStatusPanel(state.catalog)
                    HorizontalDivider()
                    if (!state.catalog.reviewConfirmed) {
                        Text(
                            DesktopLabels.Cashier.reviewFirst,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        MemberInlineSection(
                            state = state.catalog,
                            onMemberNumberChanged = onMemberNumberChanged,
                            onMemberNameChanged = onMemberNameChanged,
                            onSkipMember = onSkipMember
                        )
                        HorizontalDivider()
                        DonationInlineSection(
                            state = state.catalog,
                            onDonationEnabledChanged = onDonationEnabledChanged,
                            onDonationAmountChanged = onDonationAmountChanged,
                            onSkipDonation = onSkipDonation
                        )
                    }
                    HorizontalDivider()
                    PaymentReadinessCard(state.catalog)
                    CassyCurrencyInput(
                        "Uang diterima",
                        state.catalog.cashReceivedInput,
                        onCashReceivedChanged,
                        helperText = DesktopLabels.Cashier.paymentCashHelper
                    )
                    paymentBlockingMessage(state.catalog)?.let { blockingMessage ->
                        Text(
                            blockingMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Button(
                        onClick = onCheckoutCash,
                        enabled = state.catalog.basket.items.isNotEmpty() &&
                            state.catalog.reviewConfirmed &&
                            isMemberStepResolved(state.catalog) &&
                            state.catalog.cashTenderQuote?.isSufficient == true,
                        modifier = Modifier.fillMaxWidth().height(52.dp)
                    ) {
                        Text(DesktopLabels.Cashier.paymentCta)
                    }
                    OutlinedButton(
                        onClick = onCancelSale,
                        enabled = state.catalog.basket.items.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(DesktopLabels.Cashier.clearSale) }
                }
                WorkspaceCard(DesktopLabels.Cashier.receiptTitle, Modifier.weight(0.34f).fillMaxWidth()) {
                    ReceiptStatePanel(
                        state = state.catalog,
                        onPrintLastReceipt = onPrintLastReceipt,
                        onReprintLastReceipt = onReprintLastReceipt
                    )
                }
            }
        }
    }
}

enum class CashierMilestone(
    val title: String,
    val shortDescription: String
) {
    ScanBarang("Scan Barang", "Scan barcode atau cari SKU"),
    ReviewKeranjang("Review Keranjang", "Cek item, jumlah, dan total"),
    Member("Member", "Input member atau lewati"),
    Pembayaran("Pembayaran", "Terima uang dan cek kembalian"),
    Selesai("Selesai & Struk", "Pastikan hasil transaksi jelas")
}

private enum class MilestoneVisualState { Active, Done, Pending, Blocked }

@Composable
private fun CashierMilestoneBar(
    milestone: CashierMilestone,
    catalog: DesktopCatalogState
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            CashierMilestone.entries.forEachIndexed { index, step ->
                val state = milestoneVisualState(step, milestone, catalog)
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    color = when (state) {
                        MilestoneVisualState.Active -> MaterialTheme.colorScheme.primaryContainer
                        MilestoneVisualState.Done -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.52f)
                        MilestoneVisualState.Pending -> MaterialTheme.colorScheme.surfaceVariant
                        MilestoneVisualState.Blocked -> MaterialTheme.colorScheme.surface
                    },
                    tonalElevation = if (state == MilestoneVisualState.Active) 1.dp else 0.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = cashierMilestoneMarker(index, state),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (state == MilestoneVisualState.Blocked) {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            } else {
                                MaterialTheme.colorScheme.primary
                            }
                        )
                        Text(
                            text = step.title,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (state == MilestoneVisualState.Blocked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
        Text(
            DesktopLabels.Cashier.milestoneHint(milestone),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun cashierMilestoneMarker(
    index: Int,
    state: MilestoneVisualState
): String = when (state) {
    MilestoneVisualState.Done -> "OK"
    MilestoneVisualState.Active -> "${index + 1}"
    MilestoneVisualState.Pending -> "${index + 1}"
    MilestoneVisualState.Blocked -> "!"
}

@Composable
private fun CashierFinalizationStatusPanel(catalog: DesktopCatalogState) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            SummaryRow("Keranjang", if (catalog.reviewConfirmed) "Sudah diperiksa" else "Perlu diperiksa")
            SummaryRow(
                DesktopLabels.Cashier.memberTitle,
                if (isMemberStepResolved(catalog)) "Siap lanjut" else "Opsional, belum dipilih"
            )
            SummaryRow(
                "Pembayaran",
                when {
                    catalog.cashTenderQuote?.isSufficient == true -> DesktopLabels.Cashier.paymentReady
                    else -> DesktopLabels.Cashier.paymentWaiting
                }
            )
        }
    }
}

@Composable
private fun PaymentReadinessCard(catalog: DesktopCatalogState) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        tonalElevation = 1.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            SummaryRow("Total dibayar", "Rp ${catalog.basket.totals.finalTotal.toInt()}")
            catalog.cashTenderQuote?.let { quote ->
                SummaryRow(
                    if (quote.isSufficient) "Kembalian" else "Kurang bayar",
                    "Rp ${if (quote.isSufficient) quote.changeAmount.toInt() else quote.shortageAmount.toInt()}"
                )
            }
        }
    }
}

private fun paymentBlockingMessage(catalog: DesktopCatalogState): String? = when {
    catalog.basket.items.isEmpty() -> "Tambahkan barang dulu sebelum pembayaran dibuka."
    !catalog.reviewConfirmed -> "Periksa keranjang dulu sebelum transaksi diselesaikan."
    !isMemberStepResolved(catalog) -> "Isi member atau lewati langkah ini dulu."
    catalog.cashTenderQuote?.isSufficient != true -> "Nominal pembayaran pelanggan masih belum cukup."
    else -> null
}

@Composable
private fun MemberInlineSection(
    state: DesktopCatalogState,
    onMemberNumberChanged: (String) -> Unit,
    onMemberNameChanged: (String) -> Unit,
    onSkipMember: () -> Unit
) {
    Text(DesktopLabels.Cashier.memberTitle, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
    Text(
        if (state.memberSkipped) DesktopLabels.Cashier.memberSkipped else DesktopLabels.Cashier.memberHelper,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    SemanticTextField(
        "Nomor Member",
        state.memberNumberInput,
        onMemberNumberChanged,
        helperText = DesktopLabels.Cashier.memberNumberHelper,
        placeholder = "Contoh 081234567890"
    )
    SemanticTextField(
        "Nama Member",
        state.memberNameInput,
        onMemberNameChanged,
        helperText = DesktopLabels.Cashier.memberNameHelper,
        placeholder = "Nama pelanggan"
    )
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(onClick = onSkipMember, modifier = Modifier.weight(1f)) { Text("Lewati") }
        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(10.dp),
            color = if (isMemberStepResolved(state)) {
                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ) {
            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text(if (isMemberStepResolved(state)) "Siap lanjut" else "Belum dipilih", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun DonationInlineSection(
    state: DesktopCatalogState,
    onDonationEnabledChanged: (Boolean) -> Unit,
    onDonationAmountChanged: (String) -> Unit,
    onSkipDonation: () -> Unit
) {
    Text(DesktopLabels.Cashier.donationTitle, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
    Text(
        DesktopLabels.Cashier.donationHelper,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        FilterChip(
            selected = state.donationOffered,
            onClick = { onDonationEnabledChanged(true) },
            label = { Text("Tawarkan") }
        )
        FilterChip(
            selected = state.donationSkipped,
            onClick = onSkipDonation,
            label = { Text("Lewati") }
        )
    }
    if (state.donationOffered) {
        CassyCurrencyInput(
            label = "Nominal Donasi",
            value = state.donationAmountInput,
            onValueChange = onDonationAmountChanged,
            helperText = DesktopLabels.Cashier.donationAmountHelper
        )
    }
}

@Composable
private fun ReceiptStatePanel(
    state: DesktopCatalogState,
    onPrintLastReceipt: () -> Unit,
    onReprintLastReceipt: () -> Unit
) {
    val hasReceipt = state.receiptPreview.content?.isNotBlank() == true
    Text(
        state.printState.detailMessage ?: if (hasReceipt) DesktopLabels.Cashier.receiptReady else DesktopLabels.Cashier.receiptPending,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(
            onClick = onPrintLastReceipt,
            enabled = state.lastFinalizedSaleId != null,
            modifier = Modifier.weight(1f)
        ) {
            Text(DesktopLabels.Cashier.printLastReceipt)
        }
        OutlinedButton(
            onClick = onReprintLastReceipt,
            enabled = state.lastFinalizedSaleId != null,
            modifier = Modifier.weight(1f)
        ) {
            Text(DesktopLabels.Cashier.reprintReceipt)
        }
    }
    if (hasReceipt) {
        ReceiptPreviewCard(state.receiptPreview, state.printState.detailMessage)
    } else {
        Text(
            DesktopLabels.Cashier.receiptPreviewPending,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun resolveCashierMilestone(catalog: DesktopCatalogState): CashierMilestone = when {
    catalog.lastFinalizedSaleId != null && catalog.basket.items.isEmpty() && catalog.receiptPreview.saleId == catalog.lastFinalizedSaleId ->
        CashierMilestone.Selesai
    catalog.basket.items.isEmpty() -> CashierMilestone.ScanBarang
    !catalog.reviewConfirmed -> CashierMilestone.ReviewKeranjang
    !isMemberStepResolved(catalog) -> CashierMilestone.Member
    else -> CashierMilestone.Pembayaran
}

private fun isMemberStepResolved(catalog: DesktopCatalogState): Boolean =
    catalog.memberSkipped || catalog.memberNumberInput.isNotBlank() || catalog.memberNameInput.isNotBlank()

private fun milestoneVisualState(
    step: CashierMilestone,
    active: CashierMilestone,
    catalog: DesktopCatalogState
): MilestoneVisualState = when {
    step == active -> MilestoneVisualState.Active
    step.ordinal < active.ordinal -> MilestoneVisualState.Done
    step == CashierMilestone.Member && !catalog.reviewConfirmed -> MilestoneVisualState.Blocked
    step == CashierMilestone.Pembayaran && (!catalog.reviewConfirmed || !isMemberStepResolved(catalog)) -> MilestoneVisualState.Blocked
    step == CashierMilestone.Selesai && catalog.lastFinalizedSaleId == null -> MilestoneVisualState.Pending
    else -> MilestoneVisualState.Pending
}

@Composable
private fun HistoryWorkspace(recentSales: List<SaleHistoryEntry>) {
    WorkspacePage("Riwayat transaksi", "Daftar transaksi final terbaru untuk dicari ulang dan direview.") {
        WorkspaceCard("Transaksi Terbaru") {
            if (recentSales.isEmpty()) {
                Text("Belum ada transaksi final.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(recentSales) { sale ->
                        Surface(shape = RoundedCornerShape(14.dp), tonalElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(humanizeSaleReference(sale), fontWeight = FontWeight.Bold)
                                Text("Rp ${sale.finalAmount.toInt()} • ${sale.paymentMethod} • ${sale.saleStatus.name}", color = MaterialTheme.colorScheme.onSurfaceVariant)
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
    WorkspacePage(DesktopLabels.Inventory.title, DesktopLabels.Inventory.subtitle) {
        RouteChips(
            routes = DesktopInventoryRoute.entries.map { route ->
                Triple(route.shortLabel, state.inventoryRoute == route, { onSelectInventoryRoute(route) })
            }
        )
        when (state.inventoryRoute) {
            DesktopInventoryRoute.StockOverview -> InventoryTruthDialogContent(
                state = state.inventory,
                onSelectProduct = onSelectProduct,
                onCountQuantityChanged = onCountQuantityChanged,
                onSubmitCount = onSubmitCount,
                onAdjustmentDirectionChanged = onAdjustmentDirectionChanged,
                onAdjustmentQuantityChanged = onAdjustmentQuantityChanged,
                onAdjustmentReasonCodeChanged = onAdjustmentReasonCodeChanged,
                onAdjustmentReasonDetailChanged = onAdjustmentReasonDetailChanged,
                onApplyAdjustment = onApplyAdjustment,
                onResolveDiscrepancy = onResolveDiscrepancy,
                onMarkInvestigation = onMarkInvestigation,
                onApproveAction = onApproveAction,
                onDenyAction = onDenyAction,
                onDeferDiscrepancy = onDeferDiscrepancy
            )

            DesktopInventoryRoute.MasterData -> MasterDataWorkspace(
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
    onSync: () -> Unit
) {
    WorkspacePage(DesktopLabels.Operations.title, DesktopLabels.Operations.subtitle) {
        RouteChips(
            routes = DesktopOperationsRoute.entries.map { route ->
                Triple(route.shortLabel, state.operationsRoute == route, { onSelectOperationsRoute(route) })
            }
        )
        when (state.operationsRoute) {
            DesktopOperationsRoute.CashControl -> CashControlRoute(
                state = state,
                onCashMovementTypeSelected = onCashMovementTypeSelected,
                onCashMovementAmountChanged = onCashMovementAmountChanged,
                onCashReasonCodeChanged = onCashReasonCodeChanged,
                onCashReasonDetailChanged = onCashReasonDetailChanged,
                onSubmitCashMovement = onSubmitCashMovement,
                onApproveCashMovement = onApproveCashMovement,
                onDenyCashMovement = onDenyCashMovement
            )

            DesktopOperationsRoute.VoidSale -> VoidSaleRoute(
                state = state,
                onSelectVoidSale = onSelectVoidSale,
                onVoidReasonCodeChanged = onVoidReasonCodeChanged,
                onVoidReasonDetailChanged = onVoidReasonDetailChanged,
                onVoidInventoryFollowUpChanged = onVoidInventoryFollowUpChanged,
                onExecuteVoid = onExecuteVoid
            )

            DesktopOperationsRoute.CloseShift -> CloseShiftRoute(
                state = state,
                onClosingCashChanged = onClosingCashChanged,
                onCloseShiftReasonCodeChanged = onCloseShiftReasonCodeChanged,
                onCloseShiftReasonDetailChanged = onCloseShiftReasonDetailChanged,
                onCloseShift = onCloseShift,
                onApproveCloseShift = onApproveCloseShift,
                onDenyCloseShift = onDenyCloseShift
            )

            DesktopOperationsRoute.CloseDay -> CloseDayRoute(
                state = state,
                onCloseBusinessDay = onCloseBusinessDay
            )

            DesktopOperationsRoute.SyncCenter -> SyncCenterRoute(
                state = state,
                onSync = onSync
            )

            DesktopOperationsRoute.Diagnostics -> DiagnosticsRoute(state = state)
        }
    }
}

@Composable
private fun MasterDataWorkspace(
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
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
        WorkspaceCard("Kategori", Modifier.weight(0.8f)) {
            Text(state.groupingHint, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            FilterChip(
                selected = state.selectedCategoryId == null,
                onClick = { onSelectCategory(null) },
                label = { Text("Semua Produk") }
            )
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.height(220.dp)) {
                items(state.categories) { category ->
                    FilterChip(
                        selected = state.selectedCategoryId == category.id,
                        onClick = { onSelectCategory(category.id) },
                        label = { Text("${category.name} (${category.productCount})") }
                    )
                }
            }
            HorizontalDivider()
            SemanticTextField("Kategori Baru", state.newCategoryName, onNewCategoryNameChanged)
            SemanticTextField("Warna Kategori", state.newCategoryColor, onNewCategoryColorChanged, helperText = "Contoh #1F7A8C")
            Button(onClick = onSaveCategory, modifier = Modifier.fillMaxWidth()) { Text("Tambah Kategori") }
        }
        WorkspaceCard("Produk", Modifier.weight(1f)) {
            SemanticTextField("Cari Produk / SKU", state.searchQuery, onSearchChanged, helperText = "Cari cepat untuk edit detail.")
            Button(onClick = onPrepareNewProduct, modifier = Modifier.fillMaxWidth()) { Text("Produk Baru") }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.height(320.dp)) {
                items(state.products) { product ->
                    Surface(shape = RoundedCornerShape(14.dp), tonalElevation = if (state.selectedProductId == product.id) 2.dp else 1.dp, modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
                                Text(product.name, fontWeight = FontWeight.SemiBold)
                                Text("SKU ${product.sku}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            OutlinedButton(onClick = { onSelectProduct(product.id) }) { Text("Pilih") }
                        }
                    }
                }
            }
        }
        WorkspaceCard("Detail Produk", Modifier.weight(1.2f)) {
            Text("Permudah operator: label jelas, SKU konsisten, barcode siap scan, dan image ref mudah ditelusuri.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            SemanticTextField("Nama Produk", state.productNameInput, onProductNameChanged)
            SemanticTextField("SKU", state.productSkuInput, onProductSkuChanged)
            CassyCurrencyInput("Harga Jual", state.productPriceInput, onProductPriceChanged)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Kategori", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.height(120.dp)) {
                    items(state.categories) { category ->
                        FilterChip(
                            selected = state.productCategoryId == category.id,
                            onClick = { onProductCategoryChanged(category.id) },
                            label = { Text(category.name) }
                        )
                    }
                }
            }
            SemanticTextField("Referensi gambar produk", state.productImageRefInput, onProductImageRefChanged, helperText = "Bisa pakai referensi lama atau file dari folder gambar lokal.")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Checkbox(checked = state.productIsActive, onCheckedChange = onProductActiveChanged)
                Text("Produk aktif untuk lookup dan penjualan")
            }
            Button(onClick = onSaveProduct, modifier = Modifier.fillMaxWidth()) { Text(if (state.selectedProductId == null) "Simpan Produk Baru" else "Simpan Perubahan Produk") }
            HorizontalDivider()
            Text("Barcode Management", fontWeight = FontWeight.Bold)
            SemanticTextField("Barcode", state.barcodeDraft, onBarcodeDraftChanged, helperText = "Paste hasil scan atau input manual.")
            SemanticTextField("Type", state.barcodeType, onBarcodeTypeChanged, helperText = "GLOBAL atau INTERNAL")
            Button(onClick = onAddBarcode, modifier = Modifier.fillMaxWidth()) { Text("Tambahkan Barcode") }
            if (state.barcodes.isEmpty()) {
                Text("Belum ada barcode. Tambahkan minimal satu barcode utama agar lookup siap scan.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                state.barcodes.forEach { barcode ->
                    BarcodeRow(barcode, onRemoveBarcode)
                }
            }
        }
    }
}

@Composable
private fun BarcodeRow(
    barcode: ProductBarcodeRecord,
    onRemoveBarcode: (String) -> Unit
) {
    Surface(shape = RoundedCornerShape(12.dp), tonalElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(barcode.barcode, fontWeight = FontWeight.SemiBold)
                Text(barcode.type, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            OutlinedButton(onClick = { onRemoveBarcode(barcode.barcode) }) { Text("Lepas") }
        }
    }
}

@Composable
private fun CashControlRoute(
    state: DesktopAppState,
    onCashMovementTypeSelected: (CashMovementType) -> Unit,
    onCashMovementAmountChanged: (String) -> Unit,
    onCashReasonCodeChanged: (String) -> Unit,
    onCashReasonDetailChanged: (String) -> Unit,
    onSubmitCashMovement: () -> Unit,
    onApproveCashMovement: (String) -> Unit,
    onDenyCashMovement: (String) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
        WorkspaceCard("Kontrol Kas", Modifier.weight(1f)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CashMovementType.entries.forEach { type ->
                    FilterChip(
                        selected = state.operations.cashMovementType == type,
                        onClick = { onCashMovementTypeSelected(type) },
                        label = { Text(type.name.replace('_', ' ')) }
                    )
                }
            }
            CassyCurrencyInput("Nominal", state.operations.cashMovementAmountInput, onCashMovementAmountChanged, helperText = "Alasan operasional tetap wajib.")
            SemanticTextField("Alasan Operasional", state.operations.cashMovementReasonCode, onCashReasonCodeChanged, helperText = "Pilih atau ketik alasan yang akan tercatat di audit.")
            SemanticTextField("Catatan", state.operations.cashMovementReasonDetail, onCashReasonDetailChanged, singleLine = false)
            Button(onClick = onSubmitCashMovement, modifier = Modifier.fillMaxWidth()) { Text("Simpan Kontrol Kas") }
        }
        WorkspaceCard("Menunggu Persetujuan", Modifier.weight(1f)) {
            val approvals = state.operations.pendingApprovals.filter {
                it.operationType.name == "CASH_IN" || it.operationType.name == "CASH_OUT" || it.operationType.name == "SAFE_DROP"
            }
            if (approvals.isEmpty()) {
                Text("Tidak ada kontrol kas yang menunggu persetujuan.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                approvals.forEach { approval ->
                    ApprovalRow(
                        title = approval.title,
                        detail = approval.detail,
                        label = humanizeApprovalLabel(approval.id),
                        onApprove = { onApproveCashMovement(approval.id) },
                        onDeny = { onDenyCashMovement(approval.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun VoidSaleRoute(
    state: DesktopAppState,
    onSelectVoidSale: (String) -> Unit,
    onVoidReasonCodeChanged: (String) -> Unit,
    onVoidReasonDetailChanged: (String) -> Unit,
    onVoidInventoryFollowUpChanged: (String) -> Unit,
    onExecuteVoid: () -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
        WorkspaceCard("Review Void Transaksi", Modifier.weight(1f)) {
            Text(state.operations.voidSale.assessmentMessage, style = MaterialTheme.typography.bodySmall)
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.height(220.dp)) {
                items(state.catalog.recentSales) { sale ->
                    OutlinedButton(onClick = { onSelectVoidSale(sale.saleId) }, modifier = Modifier.fillMaxWidth()) {
                        Text(humanizeSaleReference(sale))
                    }
                }
            }
        }
        WorkspaceCard("Eksekusi Void", Modifier.weight(1f)) {
            SummaryRow("Transaksi", state.operations.voidSale.selectedLocalNumber ?: "-")
            SummaryRow("Metode", state.operations.voidSale.selectedPaymentMethod ?: "-")
            SummaryRow("Status", state.operations.voidSale.selectedSaleStatus ?: "-")
            SummaryRow("Tindak lanjut stok", state.operations.voidSale.inventoryImpactClassification)
            SemanticTextField("Alasan Operasional", state.operations.voidSale.reasonCode, onVoidReasonCodeChanged, helperText = "Wajib diisi sebelum transaksi final dibatalkan.")
            SemanticTextField("Catatan Void", state.operations.voidSale.reasonDetail, onVoidReasonDetailChanged, singleLine = false)
            SemanticTextField("Follow-up Stok", state.operations.voidSale.inventoryFollowUpNote, onVoidInventoryFollowUpChanged, singleLine = false)
            Button(onClick = onExecuteVoid, enabled = state.operations.voidSale.canExecute, modifier = Modifier.fillMaxWidth()) { Text("Batalkan Transaksi Final") }
        }
    }
}

@Composable
private fun CloseShiftRoute(
    state: DesktopAppState,
    onClosingCashChanged: (String) -> Unit,
    onCloseShiftReasonCodeChanged: (String) -> Unit,
    onCloseShiftReasonDetailChanged: (String) -> Unit,
    onCloseShift: () -> Unit,
    onApproveCloseShift: (String) -> Unit,
    onDenyCloseShift: (String) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
        WorkspaceCard("Tutup Shift", Modifier.weight(1f)) {
            state.operations.closeShiftReview?.let { review ->
                SummaryRow("Expected Cash", "Rp ${review.expectedCash.toInt()}")
                SummaryRow("Variance", review.variance?.let { "Rp ${it.toInt()}" } ?: "-")
            }
            CassyCurrencyInput("Kas Akhir", state.operations.closingCashInput, onClosingCashChanged, helperText = "Hitung fisik tunai sebelum menutup shift.")
            SemanticTextField("Kode Alasan", state.operations.closeShiftReasonCode, onCloseShiftReasonCodeChanged)
            SemanticTextField("Catatan", state.operations.closeShiftReasonDetail, onCloseShiftReasonDetailChanged, singleLine = false)
            Button(onClick = onCloseShift, modifier = Modifier.fillMaxWidth()) { Text("Eksekusi Tutup Shift") }
        }
        WorkspaceCard("Approval Tutup Shift", Modifier.weight(1f)) {
            val approvals = state.operations.pendingApprovals.filter { it.operationType.name == "CLOSE_SHIFT" }
            if (approvals.isEmpty()) {
                Text("Tidak ada approval close shift yang menunggu.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                approvals.forEach { approval ->
                    ApprovalRow(
                        title = approval.title,
                        detail = approval.detail,
                        label = humanizeApprovalLabel(approval.id),
                        onApprove = { onApproveCloseShift(approval.id) },
                        onDeny = { onDenyCloseShift(approval.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CloseDayRoute(
    state: DesktopAppState,
    onCloseBusinessDay: () -> Unit
) {
    WorkspaceCard("Tutup Hari") {
        SummaryRow("Hari Bisnis", humanizeBusinessDayLabel(state.operations.businessDayLabel))
        SummaryRow("Shift", humanizeShiftLabel(state.operations.shiftLabel))
        SummaryRow("Pending Approval", state.operations.dashboard.pendingApprovalCount.toString())
        SummaryRow("Blocker", state.operations.blockingMessage ?: "Tidak ada blocker besar")
        Button(onClick = onCloseBusinessDay, modifier = Modifier.fillMaxWidth()) { Text("Tutup Hari") }
    }
}

@Composable
private fun SyncCenterRoute(
    state: DesktopAppState,
    onSync: () -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
        WorkspaceCard("Sinkronisasi", Modifier.weight(1f)) {
                SummaryRow("Status", state.operations.reportingSummary?.syncStatus?.level?.name ?: "LOKAL")
            SummaryRow("Pending", state.operations.reportingSummary?.syncStatus?.pendingCount?.toString() ?: "0")
            SummaryRow("Failed", state.operations.reportingSummary?.syncStatus?.failedCount?.toString() ?: "0")
            Button(onClick = onSync, modifier = Modifier.fillMaxWidth()) { Text("Ulangi Sinkronisasi") }
        }
        WorkspaceCard("Recovery", Modifier.weight(1f)) {
            Text("Sync Cassy tetap local-first. Queue harus explainable dan bisa dipulihkan dengan sengaja.", style = MaterialTheme.typography.bodySmall)
            Text(devResetCommandHint(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun DiagnosticsRoute(state: DesktopAppState) {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
        WorkspaceCard("Hardware", Modifier.weight(1f)) {
            SummaryRow("Printer", state.hardware.printer.label)
            SummaryRow("Scanner", state.hardware.scanner.label)
            SummaryRow("Cash Drawer", state.hardware.cashDrawer.label)
        }
        WorkspaceCard("Runtime", Modifier.weight(1f)) {
            SummaryRow("Data Root", resolveDesktopDataRoot().absolutePath)
            SummaryRow("Store", state.shell.storeName ?: "-")
            SummaryRow("Terminal", state.shell.terminalName ?: "-")
            SummaryRow("Operator", state.shell.operatorName ?: "-")
        }
    }
}

@Composable
private fun RouteChips(
    routes: List<Triple<String, Boolean, () -> Unit>>
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        routes.forEach { (label, selected, onClick) ->
            FilterChip(
                selected = selected,
                onClick = onClick,
                label = { Text(label) }
            )
        }
    }
}

@Composable
private fun ReportingWorkspace(state: OperationsState, onExportReport: () -> Unit, isBusy: Boolean) {
    WorkspacePage("Laporan", "Snapshot lokal terminal. Accuracy lebih penting daripada dekorasi.") {
        val summary = state.reportingSummary
        if (summary == null) {
            WorkspaceCard("Ringkasan Belum Ada") { Text("Hari bisnis aktif belum tersedia untuk reporting.") }
            return@WorkspacePage
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
            WorkspaceCard("Ringkasan Hari", Modifier.weight(1f)) {
                SummaryRow("Transaksi", summary.transactionCount.toString())
                SummaryRow("Void", summary.voidedSaleCount.toString())
                SummaryRow("Approval Pending", summary.pendingApprovalCount.toString())
                SummaryRow("Kas Netto", "Rp ${summary.netCashMovement.toInt()}")
            }
            WorkspaceCard("Export", Modifier.weight(1f)) {
                Text(state.reportingExportRuleNote, style = MaterialTheme.typography.bodySmall)
                SummaryRow("Folder", state.reportingExportPath ?: "-")
                Button(onClick = onExportReport, enabled = !isBusy) { Text(if (isBusy) "Mengekspor..." else "Export Bundle CSV") }
            }
        }
        WorkspaceCard("Issue Queue") {
            Box(modifier = Modifier.fillMaxWidth().height(280.dp)) {
                OperationalIssueList(summary.issues)
            }
        }
    }
}

@Composable
private fun SystemWorkspace(
    state: DesktopAppState,
    onSync: () -> Unit,
    onStoreProfileFieldChanged: (StoreProfileUiField, String) -> Unit,
    onStoreProfileToggleChanged: (StoreProfileToggleField, Boolean) -> Unit,
    onSelectStoreLogo: () -> Unit,
    onClearStoreLogo: () -> Unit,
    onSaveStoreProfile: () -> Unit
) {
    WorkspacePage("Pengaturan terminal", "Kelola identitas usaha, logo lokal, dan recovery desktop tanpa meninggalkan konteks kerja.") {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
            WorkspaceCard("Profil usaha", Modifier.weight(1.15f)) {
                StoreProfileEditor(
                    state = state.storeProfile,
                    isBusy = state.isBusy,
                    onFieldChanged = onStoreProfileFieldChanged,
                    onToggleChanged = onStoreProfileToggleChanged,
                    onSelectLogo = onSelectStoreLogo,
                    onClearLogo = onClearStoreLogo,
                    onSave = onSaveStoreProfile
                )
            }
            WorkspaceCard("Preview branding", Modifier.weight(0.85f)) {
                StoreProfilePreviewCard(
                    profile = state.storeProfile,
                    shell = state.shell
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
            WorkspaceCard("Recovery", Modifier.weight(1f)) {
                SummaryRow("Data Root", resolveDesktopDataRoot().absolutePath)
                SummaryRow("Printer", state.hardware.printer.label)
                SummaryRow("Scanner", state.hardware.scanner.label)
                SummaryRow("Laci Kas", state.hardware.cashDrawer.label)
                Button(onClick = onSync) { Text("Ulangi Sinkronisasi") }
            }
            WorkspaceCard("Reset Data Demo", Modifier.weight(1f)) {
                Text("Reset data hanya boleh dijalankan pada folder sandbox terpisah.", style = MaterialTheme.typography.bodySmall)
                Text(devResetCommandHint(), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun StoreProfileEditor(
    state: StoreProfileState,
    isBusy: Boolean,
    onFieldChanged: (StoreProfileUiField, String) -> Unit,
    onToggleChanged: (StoreProfileToggleField, Boolean) -> Unit,
    onSelectLogo: () -> Unit,
    onClearLogo: () -> Unit,
    onSave: () -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SemanticTextField(
                label = "Nama usaha",
                value = state.businessName,
                onValueChange = { onFieldChanged(StoreProfileUiField.BusinessName, it) },
                placeholder = "Contoh Toko Berkah Jaya",
                helperText = "Nama ini tampil di struk dan ringkasan operasional.",
                errorText = state.visibleError(StoreProfileUiField.BusinessName)
            )
            SemanticTextField(
                label = "Alamat jalan",
                value = state.streetAddress,
                onValueChange = { onFieldChanged(StoreProfileUiField.StreetAddress, it) },
                placeholder = "Contoh Jl. Jayagiri No. 10",
                helperText = "Isi alamat utama yang tampil di struk dan invoice.",
                errorText = state.visibleError(StoreProfileUiField.StreetAddress),
                singleLine = false
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                SemanticTextField(
                    label = "RT / RW",
                    value = state.neighborhood,
                    onValueChange = { onFieldChanged(StoreProfileUiField.Neighborhood, it) },
                    placeholder = "Contoh 02/05",
                    errorText = state.visibleError(StoreProfileUiField.Neighborhood),
                    modifier = Modifier.weight(0.55f)
                )
                SemanticTextField(
                    label = "Kelurahan / Desa",
                    value = state.village,
                    onValueChange = { onFieldChanged(StoreProfileUiField.Village, it) },
                    placeholder = "Contoh Jayagiri",
                    errorText = state.visibleError(StoreProfileUiField.Village),
                    modifier = Modifier.weight(1f)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                SemanticTextField(
                    label = "Kecamatan",
                    value = state.district,
                    onValueChange = { onFieldChanged(StoreProfileUiField.District, it) },
                    placeholder = "Contoh Lembang",
                    errorText = state.visibleError(StoreProfileUiField.District),
                    modifier = Modifier.weight(1f)
                )
                SemanticTextField(
                    label = "Kota / Kabupaten",
                    value = state.city,
                    onValueChange = { onFieldChanged(StoreProfileUiField.City, it) },
                    placeholder = "Contoh Bandung Barat",
                    errorText = state.visibleError(StoreProfileUiField.City),
                    modifier = Modifier.weight(1f)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                SemanticTextField(
                    label = "Provinsi",
                    value = state.province,
                    onValueChange = { onFieldChanged(StoreProfileUiField.Province, it) },
                    placeholder = "Contoh Jawa Barat",
                    errorText = state.visibleError(StoreProfileUiField.Province),
                    modifier = Modifier.weight(1f)
                )
                SemanticTextField(
                    label = "Kode pos",
                    value = state.postalCode,
                    onValueChange = { onFieldChanged(StoreProfileUiField.PostalCode, it) },
                    placeholder = "40391",
                    errorText = state.visibleError(StoreProfileUiField.PostalCode),
                    modifier = Modifier.width(140.dp)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                SemanticTextField(
                    label = "Kode negara",
                    value = state.phoneCountryCode,
                    onValueChange = { onFieldChanged(StoreProfileUiField.PhoneCountryCode, it) },
                    placeholder = "+62",
                    errorText = state.visibleError(StoreProfileUiField.PhoneCountryCode),
                    modifier = Modifier.width(120.dp)
                )
                SemanticTextField(
                    label = "Nomor telepon",
                    value = state.phoneNumber,
                    onValueChange = { onFieldChanged(StoreProfileUiField.PhoneNumber, it) },
                    placeholder = "81234567890",
                    helperText = "Nomor ini tampil di struk bila pelanggan perlu menghubungi toko.",
                    errorText = state.visibleError(StoreProfileUiField.PhoneNumber),
                    modifier = Modifier.weight(1f)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                SemanticTextField(
                    label = "Email usaha",
                    value = state.businessEmail,
                    onValueChange = { onFieldChanged(StoreProfileUiField.BusinessEmail, it) },
                    placeholder = "contoh@usaha.com",
                    helperText = "Opsional, dipakai untuk invoice atau dokumen usaha.",
                    errorText = state.visibleError(StoreProfileUiField.BusinessEmail),
                    modifier = Modifier.weight(1f)
                )
                SemanticTextField(
                    label = "NIB / NPWP / ID legal",
                    value = state.legalId,
                    onValueChange = { onFieldChanged(StoreProfileUiField.LegalId, it) },
                    placeholder = "Opsional",
                    helperText = "Simpan satu identitas legal yang paling sering dipakai.",
                    errorText = state.visibleError(StoreProfileUiField.LegalId),
                    modifier = Modifier.weight(1f)
                )
            }
            SemanticTextField(
                label = "Catatan struk",
                value = state.receiptNote,
                onValueChange = { onFieldChanged(StoreProfileUiField.ReceiptNote, it) },
                placeholder = "Contoh Terima kasih sudah berbelanja",
                helperText = "Dipakai sebagai footer struk. Maksimal 140 karakter.",
                errorText = state.visibleError(StoreProfileUiField.ReceiptNote),
                singleLine = false
            )
            Surface(
                shape = RoundedCornerShape(10.dp),
                tonalElevation = 0.dp,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Tampilan struk", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Tampilkan logo")
                        Checkbox(
                            checked = state.showLogoOnReceipt,
                            onCheckedChange = { onToggleChanged(StoreProfileToggleField.ShowLogoOnReceipt, it) }
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Tampilkan alamat")
                        Checkbox(
                            checked = state.showAddressOnReceipt,
                            onCheckedChange = { onToggleChanged(StoreProfileToggleField.ShowAddressOnReceipt, it) }
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Tampilkan telepon")
                        Checkbox(
                            checked = state.showPhoneOnReceipt,
                            onCheckedChange = { onToggleChanged(StoreProfileToggleField.ShowPhoneOnReceipt, it) }
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier.width(188.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Logo usaha", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            ManagedImagePreview(
                imagePath = state.logoPath,
                fallbackLabel = state.businessName.ifBlank { "Usaha" },
                contentDescription = "Logo usaha",
                modifier = Modifier.fillMaxWidth().height(172.dp)
            )
            Text(
                state.logoPath?.let { "File lokal: ${java.io.File(it).name}" } ?: "Belum ada logo lokal. Anda tetap bisa menyimpan profil usaha tanpa logo.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = onSelectLogo, modifier = Modifier.weight(1f)) { Text("Pilih") }
                OutlinedButton(
                    onClick = onClearLogo,
                    enabled = state.logoPath != null,
                    modifier = Modifier.weight(1f)
                ) { Text("Hapus") }
            }
            Button(onClick = onSave, enabled = !isBusy, modifier = Modifier.fillMaxWidth()) {
                Text(if (isBusy) "Menyimpan..." else "Simpan Profil")
            }
        }
    }
}

@Composable
private fun StoreProfilePreviewCard(
    profile: StoreProfileState,
    shell: DesktopShellState
) {
    ManagedImagePreview(
        imagePath = profile.logoPath,
        fallbackLabel = profile.businessName.ifBlank { shell.storeName ?: "Cassy" },
        contentDescription = "Preview logo usaha",
        modifier = Modifier.fillMaxWidth().height(132.dp)
    )
    SummaryRow("Nama usaha", profile.businessName.ifBlank { "Belum diisi" })
    SummaryRow("Alamat", profile.address.ifBlank { "Belum diisi" })
    SummaryRow(
        "Telepon",
        listOf(profile.phoneCountryCode, profile.phoneNumber)
            .joinToString(" ") { it.trim() }
            .trim()
            .ifBlank { "Belum diisi" }
    )
    SummaryRow("Email", profile.businessEmail.ifBlank { "Opsional" })
    SummaryRow("ID legal", profile.legalId.ifBlank { "Opsional" })
    SummaryRow("Catatan struk", profile.receiptNote.ifBlank { "Tidak ada catatan tambahan" })
    Surface(
        shape = RoundedCornerShape(10.dp),
        tonalElevation = 0.dp,
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(profile.businessName.ifBlank { shell.storeName ?: "Nama usaha" }, fontWeight = FontWeight.Bold)
            if (profile.showAddressOnReceipt) {
                Text(profile.address.ifBlank { "Alamat usaha akan muncul di sini." }, style = MaterialTheme.typography.bodySmall)
            }
            if (profile.showPhoneOnReceipt) {
                Text(
                    listOf(profile.phoneCountryCode, profile.phoneNumber).joinToString(" ") { it.trim() }.trim().ifBlank { "Nomor telepon usaha" },
                    style = MaterialTheme.typography.bodySmall
                )
            }
            if (profile.businessEmail.isNotBlank()) {
                Text(profile.businessEmail, style = MaterialTheme.typography.bodySmall)
            }
            if (profile.legalId.isNotBlank()) {
                Text("ID legal: ${profile.legalId}", style = MaterialTheme.typography.bodySmall)
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            Text("Preview struk", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
            Text(
                profile.receiptNote.ifBlank { "Terima kasih sudah berbelanja di toko kami." },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun WorkspacePage(title: String, subtitle: String, content: @Composable ColumnScope.() -> Unit) {
    WorkspacePage(title = title, subtitle = subtitle, scrollable = true, content = content)
}

@Composable
private fun WorkspacePage(
    title: String,
    subtitle: String,
    scrollable: Boolean,
    content: @Composable ColumnScope.() -> Unit
) {
    val modifier = Modifier.fillMaxSize().padding(20.dp).let { base ->
        if (scrollable) base.verticalScroll(rememberScrollState()) else base
    }
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
        content()
    }
}

@Composable
private fun WorkspaceCard(title: String, modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Surface(modifier = modifier, shape = RoundedCornerShape(10.dp), tonalElevation = 1.dp) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            content()
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontWeight = FontWeight.SemiBold)
    }
}

private fun StoreProfileState.visibleError(field: StoreProfileUiField): String? {
    return fieldErrors[field]?.takeIf { submitAttempted || field in touchedFields }
}

@Composable
private fun ReceiptPreviewCard(
    preview: ReceiptPreviewState,
    printMessage: String?
) {
    val content = preview.content
    if (content.isNullOrBlank()) {
        Text(
            preview.availabilityMessage,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        printMessage?.let {
            Text(
                it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    SummaryRow("No. transaksi", preview.localNumber ?: "-")
    printMessage?.let { SummaryRow("Status cetak", it) }
    Surface(
        shape = RoundedCornerShape(10.dp),
        tonalElevation = 0.dp,
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = content,
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun ApprovalRow(
    title: String,
    detail: String,
    label: String,
    onApprove: () -> Unit,
    onDeny: () -> Unit
) {
    Surface(shape = RoundedCornerShape(10.dp), tonalElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, fontWeight = FontWeight.Bold)
            Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            Text(detail, style = MaterialTheme.typography.bodySmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onDeny, modifier = Modifier.weight(1f)) { Text("Tolak") }
                Button(onClick = onApprove, modifier = Modifier.weight(1f)) { Text("Setujui") }
            }
        }
    }
}
