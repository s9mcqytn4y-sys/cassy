package id.azureenterprise.cassy.desktop

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import id.azureenterprise.cassy.kernel.domain.OperatorRole
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.compose.koinInject
import org.koin.core.context.GlobalContext
import java.io.File
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    val smokeMode = args.contains("--smoke-run")
    if (smokeMode) {
        runHeadlessSmoke()
        return
    }

    startDesktopKoin()
    println("Cassy desktop runtime Java ${System.getProperty("java.version")} | smokeMode=$smokeMode")

    application {
        // 1. SCREEN FIT HARDENING: Start Maximized
        val windowState = rememberWindowState(placement = WindowPlacement.Maximized)

        Window(
            onCloseRequest = ::exitApplication,
            title = "Cassy POS",
            state = windowState
        ) {
            CassyDesktopTheme {
                val controller: DesktopAppController = koinInject()
                val state by controller.state.collectAsState()
                val scope = rememberCoroutineScope()

                var showEndShiftDialog by remember { mutableStateOf(false) }
                var showCloseDayDialog by remember { mutableStateOf(false) }
                var showCashControlDialog by remember { mutableStateOf(false) }
                var showInventoryDialog by remember { mutableStateOf(false) }
                var showVoidDialog by remember { mutableStateOf(false) }
                var showReportingDialog by remember { mutableStateOf(false) }

                LaunchedEffect(Unit) {
                    controller.load()
                }

                // PHASE 3: Keyboard Shortcut Mapping (F1-F12 + Numpad Ergonomics)
                Box(
                    modifier = Modifier.fillMaxSize().onPreviewKeyEvent {
                        if (it.type == KeyEventType.KeyDown) {
                            when (it.key) {
                                Key.F1, Key.F5 -> { scope.launch { controller.replaySyncAndReload() }; true }
                                Key.F7 -> { showVoidDialog = true; true }
                                Key.F12 -> { showCloseDayDialog = true; true }
                                Key.F11 -> { showEndShiftDialog = true; true }
                                Key.F10 -> { showCashControlDialog = true; true }
                                Key.F9 -> { showInventoryDialog = true; true }
                                Key.F8 -> { showReportingDialog = true; true }
                                Key.E -> {
                                    if (it.isCtrlPressed && showReportingDialog) {
                                        scope.launch { controller.exportOperationalReport() }
                                        true
                                    } else {
                                        false
                                    }
                                }
                                Key.Escape -> {
                                    if (showVoidDialog) {
                                        showVoidDialog = false
                                        true
                                    } else if (showReportingDialog) {
                                        showReportingDialog = false
                                        true
                                    } else {
                                        false
                                    }
                                }
                                else -> false
                            }
                        } else false
                    }
                ) {
                    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                        Row(modifier = Modifier.fillMaxSize()) {
                            CassySlimRail(
                                selectedStage = state.stage,
                                onReload = { scope.launch { controller.replaySyncAndReload() } },
                                onLogout = { scope.launch { controller.logout() } }
                            )

                            Column(modifier = Modifier.fillMaxSize()) {
                                CassyTopBar(
                                    state = state.shell,
                                    hardware = state.hardware,
                                    syncStatus = state.operations.reportingSummary?.syncStatus,
                                    onShowReporting = { showReportingDialog = true }
                                )

                                Box(modifier = Modifier.fillMaxSize()) {
                                    when (val stage = state.stage) {
                                        DesktopStage.Loading -> LoadingStage()
                                        DesktopStage.Bootstrap -> BootstrapStage(
                                            state = state,
                                            onFieldChanged = controller::updateBootstrapField,
                                            onBootstrap = { scope.launch { controller.bootstrapStore() } }
                                        )
                                        DesktopStage.Login -> LoginStage(
                                            state = state,
                                            onSelectOperator = controller::selectOperator,
                                            onPinChanged = controller::updatePin,
                                            onLogin = { scope.launch { controller.login() } }
                                        )
                                        DesktopStage.OpenDay -> OpenDayStage(
                                            state = state,
                                            onOpenDay = { scope.launch { controller.openBusinessDay() } },
                                            onLogout = { scope.launch { controller.logout() } }
                                        )
                                        DesktopStage.StartShift -> StartShiftStage(
                                            state = state,
                                            onOpeningCashChanged = controller::updateOpeningCashInput,
                                            onOpeningCashReasonChanged = controller::updateOpeningCashReasonInput,
                                            onShortcutSelected = controller::updateOpeningCashInput,
                                            onStartShift = { scope.launch { controller.startShift() } }
                                        )
                                        DesktopStage.Catalog -> {
                                            Row(modifier = Modifier.fillMaxSize()) {
                                                CassyCatalogView(
                                                    state = state.catalog,
                                                    onSearchChanged = { v -> scope.launch { controller.updateCatalogQuery(v) } },
                                                    onBarcodeChanged = controller::updateBarcodeInput,
                                                    onScanBarcode = { scope.launch { controller.scanBarcodeOrSku() } },
                                                    onAddProduct = { p -> scope.launch { controller.addProduct(p) } },
                                                    modifier = Modifier.weight(1f)
                                                )
                                                CassyCartPanel(
                                                    state = state.catalog,
                                                    operations = state.operations,
                                                    inventory = state.inventory,
                                                    onCashReceivedChanged = controller::updateCashReceivedInput,
                                                    onIncrement = { p -> scope.launch { controller.incrementItem(p) } },
                                                    onDecrement = { p, q -> scope.launch { controller.decrementItem(p, q) } },
                                                    onCheckoutCash = { scope.launch { controller.checkoutCash() } },
                                                    onPrintLastReceipt = { scope.launch { controller.printLastReceipt() } },
                                                    onReprintLastReceipt = { scope.launch { controller.reprintLastReceipt() } },
                                                    onCancelSale = { scope.launch { controller.cancelCurrentSale() } },
                                                    onVoidSale = { showVoidDialog = true },
                                                    onShowReporting = { showReportingDialog = true },
                                                    onInventoryControl = { showInventoryDialog = true },
                                                    onCashControl = { showCashControlDialog = true },
                                                    onEndShift = { showEndShiftDialog = true },
                                                    onClosingDay = { showCloseDayDialog = true }
                                                )
                                            }
                                        }
                                        is DesktopStage.FatalError -> FatalStage(
                                            message = stage.message,
                                            onRetry = { scope.launch { controller.load() } }
                                        )
                                    }

                                    // 2. FEEDBACK HARDENING: Auto-close after 3s
                                    state.banner?.let { banner ->
                                        LaunchedEffect(banner) {
                                            delay(3000)
                                            controller.dismissBanner()
                                        }
                                        Box(modifier = Modifier.padding(16.dp).align(Alignment.BottomEnd)) {
                                            BannerCard(banner = banner, onDismiss = controller::dismissBanner)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 3. SAFETY GATES: Human-friendly warnings
                    if (showCashControlDialog) {
                        CashControlDialog(
                            state = state.operations,
                            onDismiss = { showCashControlDialog = false },
                            onTypeSelected = controller::updateCashMovementType,
                            onAmountChanged = controller::updateCashMovementAmountInput,
                            onReasonCodeChanged = controller::updateCashMovementReasonCode,
                            onReasonDetailChanged = controller::updateCashMovementReasonDetail,
                            onSubmit = {
                                showCashControlDialog = false
                                scope.launch { controller.submitCashMovement() }
                            },
                            onApprove = { id -> scope.launch { controller.approveCashMovement(id) } },
                            onDeny = { id -> scope.launch { controller.denyCashMovement(id) } }
                        )
                    }

                    if (showInventoryDialog) {
                        InventoryTruthDialog(
                            state = state.inventory,
                            onDismiss = { showInventoryDialog = false },
                            onSelectProduct = { productId ->
                                scope.launch { controller.selectInventoryProduct(productId) }
                            },
                            onCountQuantityChanged = controller::updateInventoryCountQuantityInput,
                            onSubmitCount = {
                                scope.launch { controller.submitInventoryCount() }
                            },
                            onAdjustmentDirectionChanged = controller::updateInventoryAdjustmentDirection,
                            onAdjustmentQuantityChanged = controller::updateInventoryAdjustmentQuantityInput,
                            onAdjustmentReasonCodeChanged = controller::updateInventoryAdjustmentReasonCode,
                            onAdjustmentReasonDetailChanged = controller::updateInventoryAdjustmentReasonDetail,
                            onApplyAdjustment = {
                                scope.launch { controller.applyInventoryAdjustment() }
                            },
                            onResolveDiscrepancy = { reviewId ->
                                scope.launch { controller.resolveInventoryDiscrepancy(reviewId) }
                            },
                            onMarkInvestigation = { reviewId ->
                                scope.launch { controller.markInventoryDiscrepancyInvestigation(reviewId) }
                            },
                            onApproveAction = { actionId ->
                                scope.launch { controller.approveInventoryAction(actionId) }
                            },
                            onDenyAction = { actionId ->
                                scope.launch { controller.denyInventoryAction(actionId) }
                            },
                            onDeferDiscrepancy = controller::deferInventoryDiscrepancy
                        )
                    }

                    if (showEndShiftDialog) {
                        CloseShiftWizardDialog(
                            state = state.operations,
                            onDismiss = { showEndShiftDialog = false },
                            onClosingCashChanged = controller::updateClosingCashInput,
                            onReasonCodeChanged = controller::updateCloseShiftReasonCode,
                            onReasonDetailChanged = controller::updateCloseShiftReasonDetail,
                            onSubmit = {
                                showEndShiftDialog = false
                                scope.launch { controller.endShift() }
                            },
                            onApprove = { id -> scope.launch { controller.approveCloseShift(id) } },
                            onDeny = { id -> scope.launch { controller.denyCloseShift(id) } }
                        )
                    }

                    if (showCloseDayDialog) {
                        CloseDayReviewDialog(
                            operations = state.operations,
                            onConfirm = {
                                showCloseDayDialog = false
                                scope.launch { controller.closeBusinessDay() }
                            },
                            onDismiss = { showCloseDayDialog = false }
                        )
                    }

                    if (showReportingDialog) {
                        ReportingSummaryDialog(
                            state = state.operations,
                            onDismiss = { showReportingDialog = false },
                            onExport = { scope.launch { controller.exportOperationalReport() } },
                            isBusy = state.isBusy
                        )
                    }

                    if (showVoidDialog) {
                        VoidSaleDialog(
                            voidState = state.operations.voidSale,
                            recentSales = state.catalog.recentSales,
                            onDismiss = { showVoidDialog = false },
                            onSelectSale = controller::selectVoidSale,
                            onReasonCodeChanged = controller::updateVoidReasonCode,
                            onReasonDetailChanged = controller::updateVoidReasonDetail,
                            onInventoryFollowUpChanged = controller::updateVoidInventoryFollowUpNote,
                            onConfirm = {
                                showVoidDialog = false
                                scope.launch { controller.executeVoidSale() }
                            },
                            isBusy = state.isBusy
                        )
                    }
                }
            }
        }
    }
}

private fun runHeadlessSmoke() {
    val smokeMarkerPath = System.getenv("CASSY_SMOKE_MARKER")
    val smokeScenario = System.getenv("CASSY_SMOKE_SCENARIO") ?: "basic"
    try {
        startDesktopKoin()
        runBlocking {
            val controller = GlobalContext.get().get<DesktopAppController>()
            val message = when (smokeScenario.lowercase()) {
                "beta" -> runBetaSmokeScenario(controller)
                else -> runBasicSmokeScenario(controller)
            }
            smokeMarkerPath?.let { File(it).writeText(message) }
            println(message)
        }
    } catch (error: Throwable) {
        val detail = error.message?.replace('\n', ' ')?.takeIf { it.isNotBlank() } ?: error::class.simpleName.orEmpty()
        val message = "CASSY_SMOKE_FAILED error=$detail"
        smokeMarkerPath?.let { File(it).writeText(message) }
        System.err.println(message)
        exitProcess(1)
    }
}

private suspend fun runBasicSmokeScenario(controller: DesktopAppController): String {
    controller.load()
    delay(300)
    val stage = controller.state.value.stage
    if (stage is DesktopStage.FatalError) {
        error(stage.message.replace('\n', ' '))
    }
    return "CASSY_SMOKE_OK scenario=basic stage=${stage::class.simpleName}"
}

private suspend fun runBetaSmokeScenario(controller: DesktopAppController): String {
    controller.load()
    delay(300)

    if (controller.state.value.stage == DesktopStage.Bootstrap) {
        controller.updateBootstrapField(BootstrapField.StoreName, "Cassy Beta Store")
        controller.updateBootstrapField(BootstrapField.TerminalName, "Kasir Beta 01")
        controller.updateBootstrapField(BootstrapField.CashierName, "Kasir Beta")
        controller.updateBootstrapField(BootstrapField.CashierPin, "123456")
        controller.updateBootstrapField(BootstrapField.SupervisorName, "Supervisor Beta")
        controller.updateBootstrapField(BootstrapField.SupervisorPin, "654321")
        controller.bootstrapStore()
        delay(400)
    }

    val operator = controller.state.value.login.operators.firstOrNull {
        it.roleLabel.contains(OperatorRole.SUPERVISOR.name, ignoreCase = true) ||
            it.roleLabel.contains("Supervisor", ignoreCase = true)
    } ?: controller.state.value.login.operators.firstOrNull()
        ?: error("Operator smoke tidak tersedia setelah bootstrap")

    controller.selectOperator(operator.id)
    controller.updatePin("654321")
    controller.login()
    delay(400)

    if (controller.state.value.stage == DesktopStage.OpenDay) {
        controller.openBusinessDay()
        delay(400)
    }

    if (controller.state.value.stage == DesktopStage.StartShift) {
        controller.updateOpeningCashInput("100000")
        controller.startShift()
        delay(400)
    }

    val stageAfterShift = controller.state.value.stage
    if (stageAfterShift is DesktopStage.FatalError) {
        error(stageAfterShift.message.replace('\n', ' '))
    }

    controller.updateBarcodeInput("8996001600033")
    controller.scanBarcodeOrSku()
    delay(200)

    controller.updateCashReceivedInput("1")
    controller.checkoutCash()
    delay(500)

    val recentSale = controller.state.value.catalog.recentSales.firstOrNull()
        ?: error("Recent sale smoke tidak tersedia setelah checkout")

    controller.selectVoidSale(recentSale.saleId)
    controller.updateVoidReasonCode("VOID_DUPLICATE_INPUT")
    controller.updateVoidReasonDetail("Beta smoke duplicate input")
    controller.updateVoidInventoryFollowUpNote("Verifikasi fisik item smoke beta")
    controller.executeVoidSale()
    delay(500)

    controller.exportOperationalReport()
    delay(400)

    val exportPath = controller.state.value.operations.reportingExportPath
        ?: error("Export reporting smoke beta tidak menghasilkan path")
    if (!File(exportPath).exists()) {
        error("Folder export reporting smoke beta tidak ditemukan: $exportPath")
    }

    val finalStage = controller.state.value.stage
    if (finalStage is DesktopStage.FatalError) {
        error(finalStage.message.replace('\n', ' '))
    }

    return buildString {
        append("CASSY_SMOKE_OK")
        append(" scenario=beta")
        append(" stage=${finalStage::class.simpleName}")
        append(" flow=bootstrap,login,open-day,start-shift,checkout,void,report-export")
        append(" export=${File(exportPath).name}")
    }
}
