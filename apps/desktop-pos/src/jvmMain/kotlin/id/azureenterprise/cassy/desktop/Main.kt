package id.azureenterprise.cassy.desktop

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
import java.io.RandomAccessFile
import java.nio.channels.FileLock
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    val smokeMode = args.contains("--smoke-run")
    val devResetMode = args.contains("--dev-reset-demo")
    if (smokeMode) {
        runHeadlessSmoke()
        return
    }
    if (devResetMode) {
        val deleted = resetDesktopDataForDevelopment()
        println("CASSY_DEV_RESET_OK files=${deleted.size} root=${resolveDesktopDataRoot().absolutePath}")
        return
    }

    startDesktopKoin()
    println("Cassy desktop runtime Java ${System.getProperty("java.version")} | smokeMode=$smokeMode")
    val appInstanceLock = DesktopAppInstanceLock.acquire()
    if (appInstanceLock == null) {
        System.err.println("Cassy desktop instance sudah aktif. Tutup window lama atau logout dari sesi sebelumnya.")
        exitProcess(0)
    }

    application {
        // 1. SCREEN FIT HARDENING: Start Maximized
        val windowState = rememberWindowState(placement = WindowPlacement.Maximized)

        Window(
            onCloseRequest = {
                appInstanceLock.close()
                exitApplication()
            },
            title = "Cassy POS",
            state = windowState
        ) {
            CassyDesktopTheme {
                val controller: DesktopAppController = koinInject()
                val state by controller.state.collectAsState()
                val scope = rememberCoroutineScope()
                var showCommandPalette by remember { mutableStateOf(false) }
                var showShortcutHelp by remember { mutableStateOf(false) }
                var railExpanded by rememberSaveable { mutableStateOf(false) }

                LaunchedEffect(Unit) {
                    controller.load()
                }

                Box(
                    modifier = Modifier.fillMaxSize().onPreviewKeyEvent {
                        if (it.type == KeyEventType.KeyDown) {
                            when (it.key) {
                                Key.F1, Key.F5 -> { scope.launch { controller.replaySyncAndReload() }; true }
                                Key.F7 -> {
                                    controller.selectWorkspace(DesktopWorkspace.Operations)
                                    controller.selectOperationsRoute(DesktopOperationsRoute.VoidSale)
                                    true
                                }
                                Key.F8 -> { controller.selectWorkspace(DesktopWorkspace.Reporting); true }
                                Key.F9 -> {
                                    controller.selectWorkspace(DesktopWorkspace.Inventory)
                                    controller.selectInventoryRoute(DesktopInventoryRoute.StockOverview)
                                    true
                                }
                                Key.F10 -> {
                                    controller.selectWorkspace(DesktopWorkspace.Operations)
                                    controller.selectOperationsRoute(DesktopOperationsRoute.CashControl)
                                    true
                                }
                                Key.F11 -> {
                                    controller.selectWorkspace(DesktopWorkspace.Operations)
                                    controller.selectOperationsRoute(DesktopOperationsRoute.CloseShift)
                                    true
                                }
                                Key.F12 -> {
                                    controller.selectWorkspace(DesktopWorkspace.Operations)
                                    controller.selectOperationsRoute(DesktopOperationsRoute.Diagnostics)
                                    true
                                }
                                Key.K -> {
                                    if (it.isCtrlPressed) {
                                        showCommandPalette = !showCommandPalette
                                        true
                                    } else false
                                }
                                Key.B -> {
                                    if (it.isCtrlPressed) {
                                        railExpanded = !railExpanded
                                        true
                                    } else false
                                }
                                Key.Slash -> {
                                    if (it.isCtrlPressed) {
                                        showShortcutHelp = !showShortcutHelp
                                        true
                                    } else false
                                }
                                Key.S -> {
                                    if (it.isCtrlPressed && it.isShiftPressed) {
                                        controller.selectWorkspace(DesktopWorkspace.Operations)
                                        controller.selectOperationsRoute(DesktopOperationsRoute.SyncCenter)
                                        true
                                    } else false
                                }
                                Key.R -> {
                                    if (it.isCtrlPressed && it.isShiftPressed) {
                                        controller.selectWorkspace(DesktopWorkspace.Reporting)
                                        true
                                    } else false
                                }
                                Key.I -> {
                                    if (it.isCtrlPressed && it.isShiftPressed) {
                                        controller.selectWorkspace(DesktopWorkspace.Inventory)
                                        controller.selectInventoryRoute(DesktopInventoryRoute.StockOverview)
                                        true
                                    } else false
                                }
                                Key.C -> {
                                    if (it.isCtrlPressed && it.isShiftPressed) {
                                        controller.selectWorkspace(DesktopWorkspace.Operations)
                                        controller.selectOperationsRoute(DesktopOperationsRoute.CashControl)
                                        true
                                    } else false
                                }
                                Key.H -> {
                                    if (it.isCtrlPressed && it.isShiftPressed) {
                                        controller.selectWorkspace(DesktopWorkspace.Dashboard)
                                        true
                                    } else false
                                }
                                Key.E -> {
                                    if (it.isCtrlPressed && state.activeWorkspace == DesktopWorkspace.Reporting) {
                                        scope.launch { controller.exportOperationalReport() }
                                        true
                                    } else {
                                        false
                                    }
                                }
                                Key.Escape -> {
                                    if (showCommandPalette) {
                                        showCommandPalette = false
                                        true
                                    } else if (showShortcutHelp) {
                                        showShortcutHelp = false
                                        true
                                    } else if (state.stepUpAuth.isVisible) {
                                        controller.dismissStepUp()
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
                        if (state.stage == DesktopStage.Workspace) {
                            Row(modifier = Modifier.fillMaxSize()) {
                                CassyOperationalRail(
                                    state = state.shell,
                                    selectedWorkspace = state.activeWorkspace,
                                    stage = state.stage,
                                    expanded = railExpanded,
                                    onToggleExpanded = { railExpanded = !railExpanded },
                                    onSelectWorkspace = controller::selectWorkspace,
                                    onReload = { scope.launch { controller.replaySyncAndReload() } },
                                    onLogout = { scope.launch { controller.logout() } }
                                )

                                Column(modifier = Modifier.fillMaxSize()) {
                                    CassyOperationalTopBar(
                                        stage = state.stage,
                                        state = state.shell,
                                        hardware = state.hardware,
                                        syncStatus = state.operations.reportingSummary?.syncStatus,
                                        onOpenCommand = { showCommandPalette = true }
                                    )

                                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                                        when (val stage = state.stage) {
                                            DesktopStage.Loading -> LoadingStage(state.loading)
                                            DesktopStage.Bootstrap -> BootstrapStage(
                                                state = state,
                                                mode = state.bootstrapMode,
                                                onFieldChanged = controller::updateBootstrapField,
                                                onStoreProfileFieldChanged = controller::updateStoreProfileField,
                                                onStoreProfileToggleChanged = controller::updateStoreProfileToggle,
                                                onSelectAvatar = controller::selectBootstrapAvatar,
                                                onClearAvatar = controller::clearBootstrapAvatar,
                                                onSelectStoreLogo = controller::selectStoreProfileLogo,
                                                onClearStoreLogo = controller::clearStoreProfileLogo,
                                                onBootstrap = { scope.launch { controller.bootstrapStore() } }
                                            )
                                            DesktopStage.Login -> LoginStage(
                                                state = state,
                                                onSelectOperator = controller::selectOperator,
                                                onPinChanged = controller::updatePin,
                                                onLogin = { scope.launch { controller.login() } }
                                            )
                                            DesktopStage.Workspace -> DesktopWorkspaceContent(
                                                state = state,
                                                onOpenDay = { scope.launch { controller.openBusinessDay() } },
                                                onOpeningCashChanged = controller::updateOpeningCashInput,
                                                onOpeningCashReasonChanged = controller::updateOpeningCashReasonInput,
                                                onStartShift = { scope.launch { controller.startShift() } },
                                                onSearchChanged = { v -> scope.launch { controller.updateCatalogQuery(v) } },
                                                onBarcodeChanged = controller::updateBarcodeInput,
                                                onScanBarcode = { scope.launch { controller.scanBarcodeOrSku() } },
                                                onAddProduct = { p -> scope.launch { controller.addProduct(p) } },
                                                onCashReceivedChanged = controller::updateCashReceivedInput,
                                                onConfirmCartReview = controller::confirmCartReview,
                                                onMemberNumberChanged = controller::updateMemberNumberInput,
                                                onMemberNameChanged = controller::updateMemberNameInput,
                                                onSkipMember = controller::skipMemberStep,
                                                onDonationEnabledChanged = controller::offerDonation,
                                                onDonationAmountChanged = controller::updateDonationAmountInput,
                                                onSkipDonation = controller::skipDonationStep,
                                                onIncrement = { p -> scope.launch { controller.incrementItem(p) } },
                                                onDecrement = { p, q -> scope.launch { controller.decrementItem(p, q) } },
                                                onCheckoutCash = { scope.launch { controller.checkoutCash() } },
                                                onPrintLastReceipt = { scope.launch { controller.printLastReceipt() } },
                                                onReprintLastReceipt = { scope.launch { controller.reprintLastReceipt() } },
                                                onCancelSale = { scope.launch { controller.cancelCurrentSale() } },
                                                onSelectInventoryRoute = controller::selectInventoryRoute,
                                                onSelectInventoryProduct = { productId -> scope.launch { controller.selectInventoryProduct(productId) } },
                                                onInventoryCountChanged = controller::updateInventoryCountQuantityInput,
                                                onSubmitInventoryCount = { scope.launch { controller.submitInventoryCount() } },
                                                onInventoryAdjustmentDirectionChanged = controller::updateInventoryAdjustmentDirection,
                                                onInventoryAdjustmentQuantityChanged = controller::updateInventoryAdjustmentQuantityInput,
                                                onInventoryAdjustmentReasonChanged = controller::updateInventoryAdjustmentReasonCode,
                                                onInventoryAdjustmentDetailChanged = controller::updateInventoryAdjustmentReasonDetail,
                                                onApplyInventoryAdjustment = { scope.launch { controller.applyInventoryAdjustment() } },
                                                onResolveInventoryDiscrepancy = { id -> scope.launch { controller.resolveInventoryDiscrepancy(id) } },
                                                onMarkInventoryInvestigation = { id -> scope.launch { controller.markInventoryDiscrepancyInvestigation(id) } },
                                                onApproveInventoryAction = { id -> scope.launch { controller.approveInventoryAction(id) } },
                                                onDenyInventoryAction = { id -> scope.launch { controller.denyInventoryAction(id) } },
                                                onDeferInventoryDiscrepancy = controller::deferInventoryDiscrepancy,
                                                onSelectMasterCategory = { categoryId -> scope.launch { controller.selectMasterCategory(categoryId) } },
                                                onMasterSearchChanged = { value -> scope.launch { controller.updateMasterDataSearchQuery(value) } },
                                                onPrepareNewMasterProduct = controller::prepareNewMasterProduct,
                                                onSelectMasterProduct = { id -> scope.launch { controller.selectMasterProduct(id) } },
                                                onMasterProductNameChanged = controller::updateMasterProductName,
                                                onMasterProductSkuChanged = controller::updateMasterProductSku,
                                                onMasterProductPriceChanged = controller::updateMasterProductPrice,
                                                onMasterProductCategoryChanged = controller::updateMasterProductCategory,
                                                onMasterProductImageRefChanged = controller::updateMasterProductImageRef,
                                                onMasterProductActiveChanged = controller::updateMasterProductActive,
                                                onMasterBarcodeDraftChanged = controller::updateMasterBarcodeDraft,
                                                onMasterBarcodeTypeChanged = controller::updateMasterBarcodeType,
                                                onSaveMasterProduct = { scope.launch { controller.saveMasterProduct() } },
                                                onAddMasterBarcode = { scope.launch { controller.addMasterBarcode() } },
                                                onRemoveMasterBarcode = { barcode -> scope.launch { controller.removeMasterBarcode(barcode) } },
                                                onNewCategoryNameChanged = controller::updateNewCategoryName,
                                                onNewCategoryColorChanged = controller::updateNewCategoryColor,
                                                onSaveMasterCategory = { scope.launch { controller.saveMasterCategory() } },
                                                onSelectOperationsRoute = controller::selectOperationsRoute,
                                                onCashMovementTypeSelected = controller::updateCashMovementType,
                                                onCashMovementAmountChanged = controller::updateCashMovementAmountInput,
                                                onCashReasonCodeChanged = controller::updateCashMovementReasonCode,
                                                onCashReasonDetailChanged = controller::updateCashMovementReasonDetail,
                                                onSubmitCashMovement = { scope.launch { controller.submitCashMovement() } },
                                                onApproveCashMovement = { id -> scope.launch { controller.approveCashMovement(id) } },
                                                onDenyCashMovement = { id -> scope.launch { controller.denyCashMovement(id) } },
                                                onSelectVoidSale = controller::selectVoidSale,
                                                onVoidReasonCodeChanged = controller::updateVoidReasonCode,
                                                onVoidReasonDetailChanged = controller::updateVoidReasonDetail,
                                                onVoidInventoryFollowUpChanged = controller::updateVoidInventoryFollowUpNote,
                                                onExecuteVoid = { scope.launch { controller.executeVoidSale() } },
                                                onClosingCashChanged = controller::updateClosingCashInput,
                                                onCloseShiftReasonCodeChanged = controller::updateCloseShiftReasonCode,
                                                onCloseShiftReasonDetailChanged = controller::updateCloseShiftReasonDetail,
                                                onCloseShift = { scope.launch { controller.endShift() } },
                                                onApproveCloseShift = { id -> scope.launch { controller.approveCloseShift(id) } },
                                                onDenyCloseShift = { id -> scope.launch { controller.denyCloseShift(id) } },
                                                onCloseBusinessDay = { scope.launch { controller.closeBusinessDay() } },
                                                onSync = { scope.launch { controller.replaySyncAndReload() } },
                                                onExportReport = { scope.launch { controller.exportOperationalReport() } },
                                                onStoreProfileFieldChanged = controller::updateStoreProfileField,
                                                onStoreProfileToggleChanged = controller::updateStoreProfileToggle,
                                                onSelectStoreLogo = controller::selectStoreProfileLogo,
                                                onClearStoreLogo = controller::clearStoreProfileLogo,
                                                onSaveStoreProfile = { scope.launch { controller.saveStoreProfile() } },
                                                onSelectWorkspace = controller::selectWorkspace
                                            )
                                            is DesktopStage.FatalError -> FatalStage(
                                                message = stage.message,
                                                onRetry = { scope.launch { controller.load() } }
                                            )
                                        }

                                        state.banner?.let { banner ->
                                            LaunchedEffect(banner) {
                                                when (banner.tone) {
                                                    UiTone.Info, UiTone.Success -> {
                                                        delay(2400)
                                                        controller.dismissBanner()
                                                    }
                                                    UiTone.Warning -> {
                                                        delay(4200)
                                                        controller.dismissBanner()
                                                    }
                                                    UiTone.Danger -> Unit
                                                }
                                            }
                                            Box(modifier = Modifier.padding(16.dp).align(Alignment.BottomEnd)) {
                                                BannerCard(banner = banner, onDismiss = controller::dismissBanner)
                                            }
                                        }
                                    }

                                    CassyOperationalFooter(shell = state.shell)
                                }
                            }
                        } else {
                            Box(modifier = Modifier.fillMaxSize()) {
                                when (val stage = state.stage) {
                                    DesktopStage.Loading -> LoadingStage(state.loading)
                                    DesktopStage.Bootstrap -> BootstrapStage(
                                        state = state,
                                        mode = state.bootstrapMode,
                                        onFieldChanged = controller::updateBootstrapField,
                                        onStoreProfileFieldChanged = controller::updateStoreProfileField,
                                        onStoreProfileToggleChanged = controller::updateStoreProfileToggle,
                                        onSelectAvatar = controller::selectBootstrapAvatar,
                                        onClearAvatar = controller::clearBootstrapAvatar,
                                        onSelectStoreLogo = controller::selectStoreProfileLogo,
                                        onClearStoreLogo = controller::clearStoreProfileLogo,
                                        onBootstrap = { scope.launch { controller.bootstrapStore() } }
                                    )
                                    DesktopStage.Login -> LoginStage(
                                        state = state,
                                        onSelectOperator = controller::selectOperator,
                                        onPinChanged = controller::updatePin,
                                        onLogin = { scope.launch { controller.login() } }
                                    )
                                    DesktopStage.Workspace -> Unit
                                    is DesktopStage.FatalError -> FatalStage(
                                        message = stage.message,
                                        onRetry = { scope.launch { controller.load() } }
                                    )
                                }

                                state.banner?.let { banner ->
                                    LaunchedEffect(banner) {
                                        when (banner.tone) {
                                            UiTone.Info, UiTone.Success -> {
                                                delay(2400)
                                                controller.dismissBanner()
                                            }
                                            UiTone.Warning -> {
                                                delay(4200)
                                                controller.dismissBanner()
                                            }
                                            UiTone.Danger -> Unit
                                        }
                                    }
                                    Box(modifier = Modifier.padding(16.dp).align(Alignment.BottomEnd)) {
                                        BannerCard(banner = banner, onDismiss = controller::dismissBanner)
                                    }
                                }
                            }
                        }
                    }
                    if (showCommandPalette) {
                        CassyCommandPalette(
                            availableWorkspaces = state.shell.availableWorkspaces,
                            onDismiss = { showCommandPalette = false },
                            onSelectWorkspace = {
                                controller.selectWorkspace(it)
                                showCommandPalette = false
                            }
                        )
                    }

                    if (showShortcutHelp) {
                        CassyShortcutHelpDialog(
                            onDismiss = { showShortcutHelp = false }
                        )
                    }

                    if (state.stepUpAuth.isVisible) {
                        CassyStepUpAuthDialog(
                            state = state.stepUpAuth,
                            onDismiss = controller::dismissStepUp,
                            onApproverChanged = controller::updateStepUpApprover,
                            onPinChanged = controller::updateStepUpPin,
                            onDecisionNoteChanged = controller::updateStepUpDecisionNote,
                            onConfirm = { scope.launch { controller.confirmStepUp() } }
                        )
                    }
                }
            }
        }
    }
}

private class DesktopAppInstanceLock private constructor(
    private val file: RandomAccessFile,
    private val lock: FileLock
) : AutoCloseable {
    override fun close() {
        runCatching { lock.release() }
        runCatching { file.close() }
    }

    companion object {
        fun acquire(): DesktopAppInstanceLock? {
            val lockFile = File(resolveDesktopDataRoot(), ".desktop-session.lock").apply {
                parentFile?.mkdirs()
            }
            val access = RandomAccessFile(lockFile, "rw")
            val fileLock = runCatching { access.channel.tryLock() }.getOrNull()
            return if (fileLock != null) {
                DesktopAppInstanceLock(access, fileLock)
            } else {
                runCatching { access.close() }
                null
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
        controller.updateStoreProfileField(StoreProfileUiField.BusinessName, "Cassy Beta Store")
        controller.updateStoreProfileField(StoreProfileUiField.StreetAddress, "Jl. Beta No. 10")
        controller.updateStoreProfileField(StoreProfileUiField.Neighborhood, "01/02")
        controller.updateStoreProfileField(StoreProfileUiField.Village, "Jayamukti")
        controller.updateStoreProfileField(StoreProfileUiField.District, "Lembang")
        controller.updateStoreProfileField(StoreProfileUiField.City, "Bandung Barat")
        controller.updateStoreProfileField(StoreProfileUiField.Province, "Jawa Barat")
        controller.updateStoreProfileField(StoreProfileUiField.PostalCode, "40391")
        controller.updateStoreProfileField(StoreProfileUiField.PhoneCountryCode, "+62")
        controller.updateStoreProfileField(StoreProfileUiField.PhoneNumber, "81234567890")
        controller.updateStoreProfileField(StoreProfileUiField.BusinessEmail, "beta@cassy.local")
        controller.updateStoreProfileField(StoreProfileUiField.LegalId, "NIB-BETA-001")
        controller.updateStoreProfileField(StoreProfileUiField.ReceiptNote, "Terima kasih sudah belanja di Cassy Beta")
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

    if (controller.state.value.stage == DesktopStage.Workspace) {
        controller.openBusinessDay()
        delay(400)
    }

    if (controller.state.value.stage == DesktopStage.Workspace) {
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
