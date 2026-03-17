package id.azureenterprise.cassy.desktop

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
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
        Window(
            onCloseRequest = ::exitApplication,
            title = "Cassy POS",
            icon = painterResource("logo/cassandra-logo-main-512.png")
        ) {
            CassyDesktopTheme {
                val controller: DesktopAppController = koinInject()
                val state by controller.state.collectAsState()
                val scope = rememberCoroutineScope()

                var showEndShiftDialog by remember { mutableStateOf(false) }
                var showCloseDayDialog by remember { mutableStateOf(false) }

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
                                // Enter on Numpad for fast submission
                                Key.NumPadEnter -> {
                                    // Context-aware enter action could be added here
                                    false
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
                                onReload = { scope.launch { controller.load() } },
                                onLogout = { scope.launch { controller.logout() } }
                            )

                            Column(modifier = Modifier.fillMaxSize()) {
                                CassyTopBar(state = state.shell)

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
                                                    closingCashInput = state.operations.closingCashInput,
                                                    onClosingCashChanged = controller::updateClosingCashInput,
                                                    onIncrement = { p -> scope.launch { controller.incrementItem(p) } },
                                                    onDecrement = { p, q -> scope.launch { controller.decrementItem(p, q) } },
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

                                    state.banner?.let { banner ->
                                        Box(modifier = Modifier.padding(16.dp).align(Alignment.BottomEnd)) {
                                            BannerCard(banner = banner, onDismiss = controller::dismissBanner)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (showEndShiftDialog) {
                        CassySafetyDialog(
                            title = "Tutup Shift?",
                            message = "Anda akan menutup shift saat ini. Pastikan saldo kas akhir sudah diinput dengan benar.",
                            confirmLabel = "Tutup Shift",
                            onConfirm = {
                                showEndShiftDialog = false
                                scope.launch { controller.endShift() }
                            },
                            onDismiss = { showEndShiftDialog = false }
                        )
                    }

                    if (showCloseDayDialog) {
                        CassySafetyDialog(
                            title = "Tutup Hari Bisnis?",
                            message = "Tindakan ini akan mengakhiri semua operasional hari ini secara permanen. Lanjutkan?",
                            confirmLabel = "Tutup Hari",
                            tone = UiTone.Danger,
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
