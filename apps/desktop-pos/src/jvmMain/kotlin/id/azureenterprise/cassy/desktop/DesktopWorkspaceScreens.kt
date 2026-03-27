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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.azureenterprise.cassy.kernel.domain.CashMovementType
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
    onIncrement: (Product) -> Unit,
    onDecrement: (Product, Double) -> Unit,
    onCheckoutCash: () -> Unit,
    onPrintLastReceipt: () -> Unit,
    onReprintLastReceipt: () -> Unit,
    onCancelSale: () -> Unit,
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
            onIncrement = onIncrement,
            onDecrement = onDecrement,
            onCheckoutCash = onCheckoutCash,
            onPrintLastReceipt = onPrintLastReceipt,
            onReprintLastReceipt = onReprintLastReceipt,
            onCancelSale = onCancelSale
        )
        DesktopWorkspace.History -> HistoryWorkspace(state.catalog.recentSales)
        DesktopWorkspace.Inventory -> InventoryWorkspace(
            state = state.inventory,
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
            onDeferDiscrepancy = onDeferInventoryDiscrepancy
        )
        DesktopWorkspace.Operations -> OperationsWorkspace(
            state = state,
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
    WorkspacePage("Guided Operations Dashboard", "Entry point setelah login. Fokus ke status dan next action.") {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
            WorkspaceCard("Status Operasional", Modifier.weight(1.2f)) {
                OperationalDashboardCard(state.operations.dashboard)
            }
            WorkspaceCard("Context", Modifier.weight(0.8f)) {
                SummaryRow("Hari Bisnis", humanizeBusinessDayLabel(state.operations.businessDayLabel))
                SummaryRow("Shift", humanizeShiftLabel(state.operations.shiftLabel))
                SummaryRow("Operator", humanizeOperatorLabel(state.shell.operatorName, state.shell.roleLabel))
                SummaryRow("Tindakan", state.shell.nextActionLabel ?: "Tidak ada blocker utama")
            }
        }
        if (!state.operations.dashboard.canAccessSalesHome) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                WorkspaceCard("Buka Hari", Modifier.weight(1f)) {
                    Text(state.operations.blockingMessage ?: "Hari bisnis belum aktif.")
                    Button(onClick = onOpenDay, enabled = state.operations.canOpenDay && !state.isBusy) { Text("Buka Hari Bisnis") }
                }
                WorkspaceCard("Buka Shift", Modifier.weight(1f)) {
                    CassyCurrencyInput("Modal Awal Tunai", state.operations.openingCashInput, onOpeningCashChanged, helperText = "Saldo awal laci kas.")
                    SemanticTextField("Catatan Approval", state.operations.openingCashReason, onOpeningCashReasonChanged, singleLine = false)
                    Button(onClick = onStartShift, enabled = !state.isBusy) { Text("Buka Kasir") }
                }
            }
        } else {
            WorkspaceCard("Lanjutkan Operasional") {
                Text("Kasir siap. Gunakan workspace khusus agar task kompleks tidak lagi hidup di modal besar.")
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = { onSelectWorkspace(DesktopWorkspace.Cashier) }) { Text("Masuk Kasir") }
                    OutlinedButton(onClick = { onSelectWorkspace(DesktopWorkspace.Operations) }) { Text("Buka Operasional") }
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
    onIncrement: (Product) -> Unit,
    onDecrement: (Product, Double) -> Unit,
    onCheckoutCash: () -> Unit,
    onPrintLastReceipt: () -> Unit,
    onReprintLastReceipt: () -> Unit,
    onCancelSale: () -> Unit
) {
    WorkspacePage("Kasir", "Split checkout workspace: lookup, cart, payment/receipt.") {
        if (!state.operations.dashboard.canAccessSalesHome) {
            WorkspaceCard("Kasir Belum Siap") { Text(state.operations.blockingMessage ?: "Kasir belum siap dipakai.") }
            return@WorkspacePage
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxSize()) {
            Surface(modifier = Modifier.weight(1f).fillMaxHeight(), shape = RoundedCornerShape(20.dp), tonalElevation = 1.dp) {
                CassyCatalogView(state.catalog, onSearchChanged, onBarcodeChanged, onScanBarcode, onAddProduct)
            }
            WorkspaceCard("Cart Aktif", Modifier.weight(0.9f).fillMaxHeight()) {
                if (state.catalog.basket.items.isEmpty()) {
                    Text("Belum ada item. Scan barcode atau cari SKU di panel kiri.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f, fill = false)) {
                        items(state.catalog.basket.items) { item -> CassyCartItemRow(item, onIncrement, onDecrement) }
                    }
                }
                HorizontalDivider()
                CassyMetricRow("Subtotal", "Rp ${state.catalog.basket.totals.subtotal.toInt()}")
                CassyMetricRow("Pajak", "Rp ${state.catalog.basket.totals.taxTotal.toInt()}")
                CassyMetricRow("Total Akhir", "Rp ${state.catalog.basket.totals.finalTotal.toInt()}", isHighlight = true)
            }
            WorkspaceCard("Checkout & Struk", Modifier.weight(0.8f).fillMaxHeight()) {
                CassyCurrencyInput("Bayar Tunai", state.catalog.cashReceivedInput, onCashReceivedChanged, helperText = "Nominal pelanggan.")
                state.catalog.cashTenderQuote?.let { quote ->
                    SummaryRow(if (quote.isSufficient) "Kembalian" else "Kurang", "Rp ${if (quote.isSufficient) quote.changeAmount.toInt() else quote.shortageAmount.toInt()}")
                }
                Button(onClick = onCheckoutCash, enabled = state.catalog.basket.items.isNotEmpty() && state.catalog.cashTenderQuote?.isSufficient == true, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                    Text("Finalisasi Transaksi")
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(onClick = onCancelSale, enabled = state.catalog.basket.items.isNotEmpty(), modifier = Modifier.weight(1f)) { Text("Kosongkan") }
                    OutlinedButton(onClick = onPrintLastReceipt, enabled = state.catalog.lastFinalizedSaleId != null, modifier = Modifier.weight(1f)) { Text("Cetak") }
                }
                OutlinedButton(onClick = onReprintLastReceipt, enabled = state.catalog.lastFinalizedSaleId != null, modifier = Modifier.fillMaxWidth()) { Text("Cetak Ulang") }
                SummaryRow("Status Struk", state.catalog.printState.detailMessage ?: "-")
            }
        }
    }
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
    onDeferDiscrepancy: (String) -> Unit
) {
    WorkspacePage("Inventori", "Current state, discrepancy queue, approval queue, dan image readiness.") {
        InventoryTruthDialogContent(
            state = state,
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
    }
}

@Composable
private fun OperationsWorkspace(
    state: DesktopAppState,
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
    WorkspacePage("Operasional", "Task kompleks dipindah ke workspace penuh, bukan modal besar.") {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
            WorkspaceCard("Kontrol Kas", Modifier.weight(1f)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CashMovementType.entries.forEach { type ->
                        OutlinedButton(onClick = { onCashMovementTypeSelected(type) }) { Text(type.name.replace('_', ' ')) }
                    }
                }
                CassyCurrencyInput("Nominal", state.operations.cashMovementAmountInput, onCashMovementAmountChanged, helperText = "Reason code tetap wajib.")
                SemanticTextField("Reason Code", state.operations.cashMovementReasonCode, onCashReasonCodeChanged)
                SemanticTextField("Catatan", state.operations.cashMovementReasonDetail, onCashReasonDetailChanged, singleLine = false)
                Button(onClick = onSubmitCashMovement) { Text("Simpan Kontrol Kas") }
                state.operations.pendingApprovals.forEach { approval ->
                    ApprovalRow(
                        title = approval.title,
                        detail = approval.detail,
                        label = humanizeApprovalLabel(approval.id),
                        onApprove = { onApproveCashMovement(approval.id) },
                        onDeny = { onDenyCashMovement(approval.id) }
                    )
                }
            }
            WorkspaceCard("Void Sale Review", Modifier.weight(1f)) {
                Text(state.operations.voidSale.assessmentMessage, style = MaterialTheme.typography.bodySmall)
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.height(180.dp)) {
                    items(state.catalog.recentSales) { sale ->
                        OutlinedButton(onClick = { onSelectVoidSale(sale.saleId) }, modifier = Modifier.fillMaxWidth()) {
                            Text(humanizeSaleReference(sale))
                        }
                    }
                }
                SemanticTextField("Reason Code", state.operations.voidSale.reasonCode, onVoidReasonCodeChanged)
                SemanticTextField("Catatan Void", state.operations.voidSale.reasonDetail, onVoidReasonDetailChanged, singleLine = false)
                SemanticTextField("Follow-up Stok", state.operations.voidSale.inventoryFollowUpNote, onVoidInventoryFollowUpChanged, singleLine = false)
                Button(onClick = onExecuteVoid, enabled = state.operations.voidSale.canExecute) { Text("Eksekusi Void") }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
            WorkspaceCard("Close Shift", Modifier.weight(1f)) {
                state.operations.closeShiftReview?.let { review ->
                    SummaryRow("Expected Cash", "Rp ${review.expectedCash.toInt()}")
                    SummaryRow("Variance", review.variance?.let { "Rp ${it.toInt()}" } ?: "-")
                }
                CassyCurrencyInput("Closing Cash", state.operations.closingCashInput, onClosingCashChanged, helperText = "Hitung fisik tunai dulu.")
                SemanticTextField("Reason Code", state.operations.closeShiftReasonCode, onCloseShiftReasonCodeChanged)
                SemanticTextField("Catatan", state.operations.closeShiftReasonDetail, onCloseShiftReasonDetailChanged, singleLine = false)
                Button(onClick = onCloseShift) { Text("Eksekusi Tutup Shift") }
                state.operations.pendingApprovals.forEach { approval ->
                    if (approval.operationType.name == "CLOSE_SHIFT") {
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
            WorkspaceCard("Close Day / Sync", Modifier.weight(1f)) {
                SummaryRow("Hari Bisnis", humanizeBusinessDayLabel(state.operations.businessDayLabel))
                SummaryRow("Shift", humanizeShiftLabel(state.operations.shiftLabel))
                SummaryRow("Pending Approval", state.operations.dashboard.pendingApprovalCount.toString())
                Button(onClick = onCloseBusinessDay) { Text("Tutup Hari") }
                OutlinedButton(onClick = onSync) { Text("Replay Sync") }
                SummaryRow("Printer", state.hardware.printer.label)
                SummaryRow("Scanner", state.hardware.scanner.label)
            }
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
    Column(modifier = Modifier.fillMaxSize().padding(20.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
        content()
    }
}

@Composable
private fun WorkspaceCard(title: String, modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Surface(modifier = modifier, shape = RoundedCornerShape(20.dp), tonalElevation = 1.dp) {
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
