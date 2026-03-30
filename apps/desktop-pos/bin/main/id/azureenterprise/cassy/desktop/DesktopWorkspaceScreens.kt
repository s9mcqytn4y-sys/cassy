package id.azureenterprise.cassy.desktop

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.AssistChip
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
import androidx.compose.ui.text.style.TextOverflow
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
        DesktopWorkspace.System -> SystemWorkspace(state, onSync)
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
    WorkspacePage("Guided Operations", "Panel status operasional, readiness gate, dan tindakan aman berikutnya.") {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
            WorkspaceCard("Status Operasional", Modifier.weight(1.25f)) {
                OperationalDashboardCard(state.operations.dashboard)
            }
            WorkspaceCard("Ringkasan Sesi", Modifier.weight(0.95f)) {
                SummaryRow("Hari bisnis", humanizeBusinessDayLabel(state.operations.businessDayLabel))
                SummaryRow("Shift aktif", humanizeShiftLabel(state.operations.shiftLabel))
                SummaryRow("Operator", humanizeOperatorLabel(state.shell.operatorName, state.shell.roleLabel))
                SummaryRow("Langkah aman berikutnya", state.shell.nextActionLabel ?: "Tidak ada blocker utama")
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
            WorkspaceCard("Readiness Kasir", Modifier.weight(1f)) {
                SummaryRow("Printer", state.hardware.printer.label)
                Text(
                    state.hardware.printer.detailMessage ?: "Printer aman dipakai.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                SummaryRow("Scanner", state.hardware.scanner.label)
                Text(
                    state.hardware.scanner.detailMessage ?: "Scanner aman dipakai.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                SummaryRow("Laci kas", state.hardware.cashDrawer.label)
                Text(
                    state.hardware.cashDrawer.detailMessage ?: "Laci kas aman dipakai.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            WorkspaceCard("Gate Operasional", Modifier.weight(1f)) {
                Text(
                    state.operations.blockingMessage ?: "Tidak ada blocker. Kasir aman untuk lanjut.",
                    style = MaterialTheme.typography.bodyMedium
                )
                OutlinedButton(
                    onClick = { onSelectWorkspace(DesktopWorkspace.Operations) },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Buka Panel Operasional") }
                Button(
                    onClick = { onSelectWorkspace(DesktopWorkspace.Cashier) },
                    enabled = state.operations.dashboard.canAccessSalesHome,
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Masuk Kasir") }
            }
        }
        if (!state.operations.dashboard.canAccessSalesHome) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                WorkspaceCard("1. Aktifkan Hari Bisnis", Modifier.weight(1f)) {
                    Text(
                        "Kasir belum bisa dipakai sebelum hari bisnis aktif. Langkah ini membuka konteks operasional harian.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(onClick = onOpenDay, enabled = state.operations.canOpenDay && !state.isBusy, modifier = Modifier.fillMaxWidth()) {
                        Text("Buka Hari Bisnis")
                    }
                }
                WorkspaceCard("2. Buka Shift Kasir", Modifier.weight(1f)) {
                    CassyCurrencyInput(
                        "Modal Awal Tunai",
                        state.operations.openingCashInput,
                        onOpeningCashChanged,
                        helperText = "Isi saldo awal laci kas sebelum transaksi pertama."
                    )
                    SemanticTextField(
                        "Catatan Supervisor",
                        state.operations.openingCashReason,
                        onOpeningCashReasonChanged,
                        singleLine = false,
                        helperText = "Wajib diisi bila nominal perlu approval."
                    )
                    Button(onClick = onStartShift, enabled = !state.isBusy, modifier = Modifier.fillMaxWidth()) { Text("Buka Shift") }
                }
            }
        }
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
    onReprintLastReceipt: () -> Unit,
    onCancelSale: () -> Unit
) {
    WorkspacePage(
        title = "Kasir",
        subtitle = "Alur kasir dipandu agar scan, review, member, pembayaran, dan struk selesai tanpa tebakan.",
        scrollable = false
    ) {
        if (!state.operations.dashboard.canAccessSalesHome) {
            WorkspaceCard("Kasir Belum Siap") { Text(state.operations.blockingMessage ?: "Kasir belum siap dipakai.") }
            return@WorkspacePage
        }
        val milestone = remember(state.catalog) { resolveCashierMilestone(state.catalog) }
        CashierMilestoneBar(milestone = milestone, catalog = state.catalog)
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxSize()) {
            Surface(
                modifier = Modifier.weight(1.15f).fillMaxHeight(),
                shape = RoundedCornerShape(12.dp),
                tonalElevation = 0.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
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
            WorkspaceCard("Keranjang Aktif", Modifier.weight(0.95f).fillMaxHeight()) {
                MemberStepCard(
                    state = state.catalog,
                    milestone = milestone,
                    onMemberNumberChanged = onMemberNumberChanged,
                    onMemberNameChanged = onMemberNameChanged,
                    onSkipMember = onSkipMember
                )
                HorizontalDivider()
                if (state.catalog.basket.items.isEmpty()) {
                    Text("Belum ada barang. Scan barcode atau cari SKU di panel kiri.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f, fill = false)) {
                        items(state.catalog.basket.items) { item -> CassyCartItemRow(item, onIncrement, onDecrement) }
                    }
                }
                HorizontalDivider()
                CassyMetricRow("Subtotal Belanja", "Rp ${state.catalog.basket.totals.subtotal.toInt()}")
                CassyMetricRow("Pajak", "Rp ${state.catalog.basket.totals.taxTotal.toInt()}")
                CassyMetricRow("Diskon", "Rp ${state.catalog.basket.totals.discountTotal.toInt()}")
                CassyMetricRow("Total Belanja", "Rp ${state.catalog.basket.totals.finalTotal.toInt()}", isHighlight = true)
                Button(
                    onClick = onConfirmCartReview,
                    enabled = state.catalog.basket.items.isNotEmpty() && !state.catalog.reviewConfirmed,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (state.catalog.reviewConfirmed) "Keranjang Sudah Dicek" else "Lanjut ke Member")
                }
            }
            WorkspaceCard("Pembayaran & Struk", Modifier.weight(0.9f).fillMaxHeight()) {
                DonationStepCard(
                    state = state.catalog,
                    milestone = milestone,
                    onDonationEnabledChanged = onDonationEnabledChanged,
                    onDonationAmountChanged = onDonationAmountChanged,
                    onSkipDonation = onSkipDonation
                )
                CassyCurrencyInput("Uang Diterima", state.catalog.cashReceivedInput, onCashReceivedChanged, helperText = "Masukkan uang dari pelanggan.")
                state.catalog.cashTenderQuote?.let { quote ->
                    SummaryRow(if (quote.isSufficient) "Kembalian" else "Kurang Bayar", "Rp ${if (quote.isSufficient) quote.changeAmount.toInt() else quote.shortageAmount.toInt()}")
                }
                Button(
                    onClick = onCheckoutCash,
                    enabled = state.catalog.basket.items.isNotEmpty() &&
                        state.catalog.reviewConfirmed &&
                        isMemberStepResolved(state.catalog) &&
                        state.catalog.cashTenderQuote?.isSufficient == true,
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Text("Selesaikan Transaksi")
                }
                HorizontalDivider()
                SummaryRow("Metode pembayaran", "Tunai")
                SummaryRow("Status struk", state.catalog.printState.detailMessage ?: "Belum ada struk final")
                ReceiptPreviewPanel(state.catalog)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(onClick = onCancelSale, enabled = state.catalog.basket.items.isNotEmpty(), modifier = Modifier.weight(1f)) { Text("Kosongkan") }
                    OutlinedButton(onClick = onPrintLastReceipt, enabled = state.catalog.lastFinalizedSaleId != null, modifier = Modifier.weight(1f)) { Text("Cetak Struk") }
                }
                OutlinedButton(onClick = onReprintLastReceipt, enabled = state.catalog.lastFinalizedSaleId != null, modifier = Modifier.fillMaxWidth()) { Text("Cetak Ulang") }
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
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        CashierMilestone.entries.forEachIndexed { index, step ->
            val state = milestoneVisualState(step, milestone, catalog)
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                color = when (state) {
                    MilestoneVisualState.Active -> MaterialTheme.colorScheme.primaryContainer
                    MilestoneVisualState.Done -> MaterialTheme.colorScheme.secondaryContainer
                    MilestoneVisualState.Pending -> MaterialTheme.colorScheme.surfaceVariant
                    MilestoneVisualState.Blocked -> MaterialTheme.colorScheme.surface
                },
                tonalElevation = 0.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("${index + 1}. ${step.title}", fontWeight = FontWeight.Bold, color = if (state == MilestoneVisualState.Blocked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface)
                    Text(step.shortDescription, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun MemberStepCard(
    state: DesktopCatalogState,
    milestone: CashierMilestone,
    onMemberNumberChanged: (String) -> Unit,
    onMemberNameChanged: (String) -> Unit,
    onSkipMember: () -> Unit
) {
    WorkspaceCard("Member", Modifier.fillMaxWidth()) {
        Text(
            if (state.memberSkipped) "Langkah member dilewati untuk transaksi ini."
            else "Tambahkan nomor atau nama member bila pelanggan memilikinya. Langkah ini opsional.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        SemanticTextField("Nomor Member", state.memberNumberInput, onMemberNumberChanged, helperText = "Opsional. Kosongkan bila tidak ada.", placeholder = "Contoh 081234567890")
        SemanticTextField("Nama Member", state.memberNameInput, onMemberNameChanged, helperText = "Opsional untuk pencatatan lokal cepat.", placeholder = "Nama pelanggan")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = onSkipMember, modifier = Modifier.weight(1f)) { Text("Lewati Member") }
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                color = if (milestone.ordinal >= CashierMilestone.Member.ordinal && isMemberStepResolved(state)) {
                    MaterialTheme.colorScheme.secondaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            ) {
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    Text(if (isMemberStepResolved(state)) "Member Siap" else "Isi atau Lewati", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun DonationStepCard(
    state: DesktopCatalogState,
    milestone: CashierMilestone,
    onDonationEnabledChanged: (Boolean) -> Unit,
    onDonationAmountChanged: (String) -> Unit,
    onSkipDonation: () -> Unit
) {
    WorkspaceCard("Donasi Opsional", Modifier.fillMaxWidth()) {
        Text(
            "Gunakan hanya bila toko memang sedang menjalankan program donasi. Langkah ini tidak wajib.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            FilterChip(
                selected = state.donationOffered,
                onClick = { onDonationEnabledChanged(true) },
                label = { Text("Tawarkan Donasi") }
            )
            FilterChip(
                selected = state.donationSkipped,
                onClick = onSkipDonation,
                label = { Text("Lewati Donasi") }
            )
        }
        if (state.donationOffered) {
            CassyCurrencyInput(
                label = "Nominal Donasi",
                value = state.donationAmountInput,
                onValueChange = onDonationAmountChanged,
                helperText = "Catatan lokal kasir. Belum mengubah total transaksi inti."
            )
        }
        if (milestone == CashierMilestone.Pembayaran || milestone == CashierMilestone.Selesai) {
            Text(
                if (state.donationOffered && state.donationAmountInput.isNotBlank()) "Donasi dicatat sebagai catatan operasional lokal."
                else "Bila tidak relevan, langsung lanjut ke pembayaran.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
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

@Composable
private fun ReceiptPreviewPanel(catalog: DesktopCatalogState) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        tonalElevation = 0.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("Preview Struk", fontWeight = FontWeight.SemiBold)
                AssistChip(
                    onClick = {},
                    enabled = false,
                    label = { Text(if (catalog.lastFinalizedSaleId != null) "Siap reprint" else "Menunggu transaksi") }
                )
            }
            Text(
                catalog.receiptPreview.localNumber ?: "Struk final akan tampil di sini setelah transaksi selesai.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                catalog.lastReceiptPreview ?: catalog.receiptPreview.availabilityMessage,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 10,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

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
    WorkspacePage("Riwayat Transaksi", "Daftar transaksi final terbaru untuk lookup dan review.") {
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
    WorkspacePage("Inventori", "Stock truth dan master data dipisah agar layar tidak sumpek dan operator tidak salah konteks.") {
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
    WorkspacePage("Operasional", "Task operasional berat dibagi ke sub-route agar mudah diakses, cepat dipahami, dan aman untuk laptop.") {
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
            SemanticTextField("Image Ref / File Hint", state.productImageRefInput, onProductImageRefChanged, helperText = "Bisa pakai imageUrl lama atau file di input_images.")
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
            CassyCurrencyInput("Nominal", state.operations.cashMovementAmountInput, onCashMovementAmountChanged, helperText = "Reason code tetap wajib.")
            SemanticTextField("Reason Code", state.operations.cashMovementReasonCode, onCashReasonCodeChanged)
            SemanticTextField("Catatan", state.operations.cashMovementReasonDetail, onCashReasonDetailChanged, singleLine = false)
            Button(onClick = onSubmitCashMovement, modifier = Modifier.fillMaxWidth()) { Text("Simpan Kontrol Kas") }
        }
        WorkspaceCard("Approval Queue", Modifier.weight(1f)) {
            val approvals = state.operations.pendingApprovals.filter {
                it.operationType.name == "CASH_IN" || it.operationType.name == "CASH_OUT" || it.operationType.name == "SAFE_DROP"
            }
            if (approvals.isEmpty()) {
                Text("Tidak ada approval cash control yang menunggu.", color = MaterialTheme.colorScheme.onSurfaceVariant)
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
        WorkspaceCard("Void Sale Review", Modifier.weight(1f)) {
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
            SummaryRow("Sale Ref", state.operations.voidSale.selectedLocalNumber ?: "-")
            SummaryRow("Metode", state.operations.voidSale.selectedPaymentMethod ?: "-")
            SummaryRow("Status", state.operations.voidSale.selectedSaleStatus ?: "-")
            SummaryRow("Kontrak Stok", state.operations.voidSale.inventoryImpactClassification)
            SemanticTextField("Reason Code", state.operations.voidSale.reasonCode, onVoidReasonCodeChanged)
            SemanticTextField("Catatan Void", state.operations.voidSale.reasonDetail, onVoidReasonDetailChanged, singleLine = false)
            SemanticTextField("Follow-up Stok", state.operations.voidSale.inventoryFollowUpNote, onVoidInventoryFollowUpChanged, singleLine = false)
            Button(onClick = onExecuteVoid, enabled = state.operations.voidSale.canExecute, modifier = Modifier.fillMaxWidth()) { Text("Eksekusi Void") }
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
        WorkspaceCard("Close Shift", Modifier.weight(1f)) {
            state.operations.closeShiftReview?.let { review ->
                SummaryRow("Expected Cash", "Rp ${review.expectedCash.toInt()}")
                SummaryRow("Variance", review.variance?.let { "Rp ${it.toInt()}" } ?: "-")
            }
            CassyCurrencyInput("Closing Cash", state.operations.closingCashInput, onClosingCashChanged, helperText = "Hitung fisik tunai dulu.")
            SemanticTextField("Reason Code", state.operations.closeShiftReasonCode, onCloseShiftReasonCodeChanged)
            SemanticTextField("Catatan", state.operations.closeShiftReasonDetail, onCloseShiftReasonDetailChanged, singleLine = false)
            Button(onClick = onCloseShift, modifier = Modifier.fillMaxWidth()) { Text("Eksekusi Tutup Shift") }
        }
        WorkspaceCard("Approval Close Shift", Modifier.weight(1f)) {
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
    WorkspaceCard("Close Day") {
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
        WorkspaceCard("Sync Center", Modifier.weight(1f)) {
            SummaryRow("Status", state.operations.reportingSummary?.syncStatus?.level?.name ?: "OFFLINE")
            SummaryRow("Pending", state.operations.reportingSummary?.syncStatus?.pendingCount?.toString() ?: "0")
            SummaryRow("Failed", state.operations.reportingSummary?.syncStatus?.failedCount?.toString() ?: "0")
            Button(onClick = onSync, modifier = Modifier.fillMaxWidth()) { Text("Replay Sync") }
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
private fun SystemWorkspace(state: DesktopAppState, onSync: () -> Unit) {
    WorkspacePage("Sistem", "Diagnostics, recovery, dan dev reset yang eksplisit.") {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
            WorkspaceCard("Recovery", Modifier.weight(1f)) {
                SummaryRow("Data Root", resolveDesktopDataRoot().absolutePath)
                SummaryRow("Printer", state.hardware.printer.label)
                SummaryRow("Scanner", state.hardware.scanner.label)
                Button(onClick = onSync) { Text("Replay Sync") }
            }
            WorkspaceCard("Dev Reset", Modifier.weight(1f)) {
                Text("Reset database hanya boleh jalan dengan dev flag eksplisit.", style = MaterialTheme.typography.bodySmall)
                Text(devResetCommandHint(), style = MaterialTheme.typography.bodySmall)
            }
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
        Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
        content()
    }
}

@Composable
private fun WorkspaceCard(title: String, modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
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

@Composable
private fun ApprovalRow(
    title: String,
    detail: String,
    label: String,
    onApprove: () -> Unit,
    onDeny: () -> Unit
) {
    Surface(shape = RoundedCornerShape(14.dp), tonalElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
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
