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

                LaunchedEffect(Unit) {
                    controller.load()
                }

                // PHASE 3: Keyboard Shortcut Mapping (F1-F12 + Numpad Ergonomics)
                Box(
                    modifier = Modifier.fillMaxSize().onPreviewKeyEvent {
                        if (it.type == KeyEventType.KeyDown) {
                            when (it.key) {
                                Key.F1, Key.F5 -> { scope.launch { controller.load() }; true }
                                Key.F12 -> { showCloseDayDialog = true; true }
                                Key.F11 -> { showEndShiftDialog = true; true }
                                Key.F10 -> { showCashControlDialog = true; true }
                                Key.F9 -> { showInventoryDialog = true; true }
                                else -> false
                            }
                        } else false
                    }
                ) {
                    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                        Row(modifier = Modifier.fillMaxSize()) {
                            CassySlimRail(
                                selectedStage = state.stage,
                                onReload = { scope.launch { controller.load() } },
                                onLogout = { scope.launch { controller.logout() } }
                            )

                            Column(modifier = Modifier.fillMaxSize()) {
                                CassyTopBar(state = state.shell, hardware = state.hardware)

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
                }
            }
        }
    }
}

private fun runHeadlessSmoke() {
    val smokeMarkerPath = System.getenv("CASSY_SMOKE_MARKER")
    startDesktopKoin()
    runBlocking {
        val controller = GlobalContext.get().get<DesktopAppController>()
        controller.load()
        delay(300)
        val stage = controller.state.value.stage
        if (stage is DesktopStage.FatalError) {
            smokeMarkerPath?.let { File(it).writeText("FAILED stage=${stage.message}") }
            exitProcess(1)
        } else {
            smokeMarkerPath?.let { File(it).writeText("OK stage=${stage::class.simpleName}") }
        }
    }
}
