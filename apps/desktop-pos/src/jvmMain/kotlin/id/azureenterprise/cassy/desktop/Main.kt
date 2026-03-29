package id.azureenterprise.cassy.desktop

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import id.azureenterprise.cassy.kernel.domain.OperationType
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.KoinContext
import org.koin.compose.koinInject

fun main() {
    startDesktopKoin()
    application {
        val windowState = rememberWindowState()

        Window(
            onCloseRequest = ::exitApplication,
            title = "Cassy POS Desktop",
            state = windowState
        ) {
            KoinContext {
                CassyDesktopApp()
            }
        }
    }
}

@Composable
fun CassyDesktopApp() {
    val controller: DesktopAppController = koinInject()
    val state by controller.state.collectAsState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        controller.load()
    }

    CassyDesktopTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Row(modifier = Modifier.fillMaxSize()) {
                    var railExpanded by remember { mutableStateOf(false) }

                    if (state.stage == DesktopStage.Workspace) {
                        CassyOperationalRail(
                            state = state.shell,
                            selectedWorkspace = state.activeWorkspace,
                            stage = state.stage,
                            expanded = railExpanded,
                            onToggleExpanded = { railExpanded = !railExpanded },
                            onSelectWorkspace = controller::selectWorkspace,
                            onLogout = { scope.launch { controller.logout() } },
                            onReload = { scope.launch { controller.replaySyncAndReload() } }
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        CassyOperationalTopBar(
                            stage = state.stage,
                            state = state.shell,
                            hardware = state.hardware,
                            syncStatus = state.operations.reportingSummary?.syncStatus,
                            onOpenCommand = { /* TODO: Quick Actions Overlay */ }
                        )

                        Box(modifier = Modifier.weight(1f)) {
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
                                DesktopStage.Workspace -> {
                                    DesktopWorkspaceContent(
                                        state = state,
                                        onOpenDay = { scope.launch { controller.openBusinessDay() } },
                                        onOpeningCashChanged = controller::updateOpeningCashInput,
                                        onOpeningCashReasonChanged = controller::updateOpeningCashReasonInput,
                                        onStartShift = { scope.launch { controller.startShift() } },
                                        onSearchChanged = { scope.launch { controller.updateCatalogQuery(it) } },
                                        onBarcodeChanged = controller::updateBarcodeInput,
                                        onScanBarcode = { scope.launch { controller.scanBarcodeOrSku() } },
                                        onAddProduct = { scope.launch { controller.addProduct(it) } },
                                        onCashReceivedChanged = controller::updateCashReceivedInput,
                                        onConfirmCartReview = controller::confirmCartReview,
                                        onMemberNumberChanged = controller::updateMemberNumberInput,
                                        onMemberNameChanged = controller::updateMemberNameInput,
                                        onSkipMember = controller::skipMemberStep,
                                        onDonationEnabledChanged = controller::offerDonation,
                                        onDonationAmountChanged = controller::updateDonationAmountInput,
                                        onSkipDonation = controller::skipDonationStep,
                                        onIncrement = { scope.launch { controller.incrementItem(it) } },
                                        onDecrement = { product, qty -> scope.launch { controller.decrementItem(product, qty) } },
                                        onCheckoutCash = { scope.launch { controller.checkoutCash() } },
                                        onPrintLastReceipt = { scope.launch { controller.printLastReceipt() } },
                                        onReprintLastReceipt = { scope.launch { controller.reprintLastReceipt() } },
                                        onCancelSale = { scope.launch { controller.cancelCurrentSale() } },
                                        onSelectInventoryRoute = controller::selectInventoryRoute,
                                        onSelectInventoryProduct = { scope.launch { controller.selectInventoryProduct(it) } },
                                        onInventoryCountChanged = controller::updateInventoryCountQuantityInput,
                                        onSubmitInventoryCount = { scope.launch { controller.submitInventoryCount() } },
                                        onInventoryAdjustmentDirectionChanged = controller::updateInventoryAdjustmentDirection,
                                        onInventoryAdjustmentQuantityChanged = controller::updateInventoryAdjustmentQuantityInput,
                                        onInventoryAdjustmentReasonChanged = controller::updateInventoryAdjustmentReasonCode,
                                        onInventoryAdjustmentDetailChanged = controller::updateInventoryAdjustmentReasonDetail,
                                        onApplyInventoryAdjustment = { scope.launch { controller.applyInventoryAdjustment() } },
                                        onResolveInventoryDiscrepancy = { scope.launch { controller.resolveInventoryDiscrepancy(it) } },
                                        onMarkInventoryInvestigation = { scope.launch { controller.markInventoryDiscrepancyInvestigation(it) } },
                                        onApproveInventoryAction = { scope.launch { controller.approveInventoryAction(it) } },
                                        onDenyInventoryAction = { scope.launch { controller.denyInventoryAction(it) } },
                                        onDeferInventoryDiscrepancy = controller::deferInventoryDiscrepancy,
                                        onSelectMasterCategory = { scope.launch { controller.selectMasterCategory(it) } },
                                        onMasterSearchChanged = { scope.launch { controller.updateMasterDataSearchQuery(it) } },
                                        onPrepareNewMasterProduct = controller::prepareNewMasterProduct,
                                        onSelectMasterProduct = { scope.launch { controller.selectMasterProduct(it) } },
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
                                        onRemoveMasterBarcode = { scope.launch { controller.removeMasterBarcode(it) } },
                                        onNewCategoryNameChanged = controller::updateNewCategoryName,
                                        onNewCategoryColorChanged = controller::updateNewCategoryColor,
                                        onSaveMasterCategory = { scope.launch { controller.saveMasterCategory() } },
                                        onSelectOperationsRoute = controller::selectOperationsRoute,
                                        onCashMovementTypeSelected = controller::updateCashMovementType,
                                        onCashMovementAmountChanged = controller::updateCashMovementAmountInput,
                                        onCashReasonCodeChanged = controller::updateCashMovementReasonCode,
                                        onCashReasonDetailChanged = controller::updateCashMovementReasonDetail,
                                        onSubmitCashMovement = { scope.launch { controller.submitCashMovement() } },
                                        onApproveCashMovement = { scope.launch { controller.approveCashMovement(it) } },
                                        onDenyCashMovement = { scope.launch { controller.denyCashMovement(it) } },
                                        onSelectVoidSale = controller::selectVoidSale,
                                        onVoidReasonCodeChanged = controller::updateVoidReasonCode,
                                        onVoidReasonDetailChanged = controller::updateVoidReasonDetail,
                                        onVoidInventoryFollowUpChanged = controller::updateVoidInventoryFollowUpNote,
                                        onExecuteVoid = { scope.launch { controller.executeVoidSale() } },
                                        onClosingCashChanged = controller::updateClosingCashInput,
                                        onCloseShiftReasonCodeChanged = controller::updateCloseShiftReasonCode,
                                        onCloseShiftReasonDetailChanged = controller::updateCloseShiftReasonDetail,
                                        onCloseShift = { scope.launch { controller.endShift() } },
                                        onApproveCloseShift = { scope.launch { controller.approveCloseShift(it) } },
                                        onDenyCloseShift = { scope.launch { controller.denyCloseShift(it) } },
                                        onCloseBusinessDay = { scope.launch { controller.closeBusinessDay() } },
                                        onSync = { scope.launch { controller.replaySyncAndReload() } },
                                        onExportReport = { scope.launch { controller.exportOperationalReport() } },
                                        onStoreProfileFieldChanged = controller::updateStoreProfileField,
                                        onStoreProfileToggleChanged = controller::updateStoreProfileToggle,
                                        onSelectStoreLogo = controller::selectStoreProfileLogo,
                                        onClearStoreLogo = controller::clearStoreProfileLogo,
                                        onSaveStoreProfile = { scope.launch { controller.saveStoreProfile() } },
                                        onSelectWorkspace = controller::selectWorkspace,
                                        onRefreshDashboard = { scope.launch { controller.load() } },
                                        onApproveGeneric = { approval ->
                                            scope.launch {
                                                when (approval.type) {
                                                    OperationType.CASH_IN, OperationType.CASH_OUT, OperationType.SAFE_DROP ->
                                                        controller.approveCashMovement(approval.id)
                                                    OperationType.CLOSE_SHIFT -> controller.approveCloseShift(approval.id)
                                                    OperationType.STOCK_ADJUSTMENT, OperationType.RESOLVE_STOCK_DISCREPANCY ->
                                                        controller.approveInventoryAction(approval.id)
                                                    else -> {}
                                                }
                                            }
                                        },
                                        onDenyGeneric = { approval ->
                                            scope.launch {
                                                when (approval.type) {
                                                    OperationType.CASH_IN, OperationType.CASH_OUT, OperationType.SAFE_DROP ->
                                                        controller.denyCashMovement(approval.id)
                                                    OperationType.CLOSE_SHIFT -> controller.denyCloseShift(approval.id)
                                                    OperationType.STOCK_ADJUSTMENT, OperationType.RESOLVE_STOCK_DISCREPANCY ->
                                                        controller.denyInventoryAction(approval.id)
                                                    else -> {}
                                                }
                                            }
                                        }
                                    )
                                }
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
                                        UiTone.Danger -> {
                                            delay(6000)
                                            controller.dismissBanner()
                                        }
                                    }
                                }
                                Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.BottomCenter) {
                                    BannerCard(banner, onDismiss = controller::dismissBanner)
                                }
                            }

                            if (state.stepUpAuth.isVisible) {
                                CassyStepUpAuthDialog(
                                    state = state.stepUpAuth,
                                    onApproverChanged = controller::updateStepUpApprover,
                                    onPinChanged = controller::updateStepUpPin,
                                    onDecisionNoteChanged = controller::updateStepUpDecisionNote,
                                    onConfirm = { scope.launch { controller.confirmStepUp() } },
                                    onDismiss = controller::dismissStepUp
                                )
                            }

                            if (state.isBusy) {
                                Box(
                                    modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        }

                        if (state.stage == DesktopStage.Workspace) {
                            CassyOperationalFooter(state.shell)
                        }
                    }
                }
            }
        }
    }
}
