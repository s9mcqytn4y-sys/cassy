package id.azureenterprise.cassy.desktop

import id.azureenterprise.cassy.kernel.application.AccessService
import id.azureenterprise.cassy.kernel.application.BusinessDayService
import id.azureenterprise.cassy.kernel.application.ShiftService
import id.azureenterprise.cassy.kernel.domain.AccessCapability
import id.azureenterprise.cassy.kernel.domain.BootstrapStoreRequest
import id.azureenterprise.cassy.kernel.domain.LoginResult
import id.azureenterprise.cassy.kernel.domain.supports
import id.azureenterprise.cassy.masterdata.data.ProductRepository
import id.azureenterprise.cassy.masterdata.domain.Product
import id.azureenterprise.cassy.masterdata.domain.ProductLookupResult
import id.azureenterprise.cassy.masterdata.domain.ProductLookupUseCase
import id.azureenterprise.cassy.sales.application.SalesService
import id.azureenterprise.cassy.sales.domain.Basket
import id.azureenterprise.cassy.sales.domain.SaleHistoryEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class DesktopAppController(
    private val accessService: AccessService,
    private val businessDayService: BusinessDayService,
    private val shiftService: ShiftService,
    private val productRepository: ProductRepository,
    private val productLookupUseCase: ProductLookupUseCase,
    private val salesService: SalesService
) {
    private val _state = MutableStateFlow(DesktopAppState())
    val state: StateFlow<DesktopAppState> = _state.asStateFlow()

    suspend fun load() {
        mutateBusy(true)
        runCatching { refreshStage() }
            .onFailure { error ->
                _state.update {
                    it.copy(
                        isBusy = false,
                        stage = DesktopStage.FatalError(error.message ?: "Gagal memuat state desktop"),
                        banner = UiBanner(UiTone.Danger, error.message ?: "Gagal memuat state desktop")
                    )
                }
            }
    }

    fun updateBootstrapField(field: BootstrapField, value: String) {
        _state.update {
            it.copy(
                bootstrap = when (field) {
                    BootstrapField.StoreName -> it.bootstrap.copy(storeName = value)
                    BootstrapField.TerminalName -> it.bootstrap.copy(terminalName = value)
                    BootstrapField.CashierName -> it.bootstrap.copy(cashierName = value)
                    BootstrapField.CashierPin -> it.bootstrap.copy(cashierPin = value.take(6))
                    BootstrapField.SupervisorName -> it.bootstrap.copy(supervisorName = value)
                    BootstrapField.SupervisorPin -> it.bootstrap.copy(supervisorPin = value.take(6))
                }
            )
        }
    }

    suspend fun bootstrapStore() {
        mutateBusy(true)
        val form = state.value.bootstrap
        val result = accessService.bootstrapStore(
            BootstrapStoreRequest(
                storeName = form.storeName,
                terminalName = form.terminalName,
                cashierName = form.cashierName,
                cashierPin = form.cashierPin,
                supervisorName = form.supervisorName,
                supervisorPin = form.supervisorPin
            )
        )
        refreshStage(
            result.fold(
                onSuccess = { UiBanner(UiTone.Success, "Bootstrap store selesai. Login dengan PIN operator.") },
                onFailure = { UiBanner(UiTone.Danger, it.message ?: "Bootstrap store gagal") }
            )
        )
    }

    fun selectOperator(operatorId: String) {
        _state.update {
            it.copy(login = it.login.copy(selectedOperatorId = operatorId, pin = "", feedback = null))
        }
    }

    fun updatePin(pin: String) {
        _state.update { it.copy(login = it.login.copy(pin = pin.take(6), feedback = null)) }
    }

    suspend fun login() {
        val operatorId = state.value.login.selectedOperatorId
            ?: return pushBanner(UiBanner(UiTone.Warning, "Pilih operator terlebih dahulu"))
        mutateBusy(true)
        when (val result = accessService.login(operatorId, state.value.login.pin)) {
            is LoginResult.Success -> refreshStage(UiBanner(UiTone.Success, "Login ${result.operator.displayName} berhasil"))
            is LoginResult.WrongPin -> {
                mutateBusy(false)
                _state.update {
                    it.copy(
                        login = it.login.copy(
                            feedback = "PIN salah. Sisa percobaan sebelum lock: ${result.remainingBeforeLock}"
                        ),
                        banner = UiBanner(UiTone.Warning, "PIN operator salah")
                    )
                }
            }
            is LoginResult.Locked -> {
                mutateBusy(false)
                _state.update {
                    it.copy(
                        login = it.login.copy(
                            feedback = "Akses terkunci sampai ${result.lockedUntil ?: "-"}"
                        ),
                        banner = UiBanner(UiTone.Danger, "Akses operator terkunci sementara")
                    )
                }
            }
            LoginResult.OperatorNotFound -> {
                mutateBusy(false)
                pushBanner(UiBanner(UiTone.Danger, "Operator tidak ditemukan"))
            }
            LoginResult.NotBound -> refreshStage(UiBanner(UiTone.Danger, "Terminal belum terikat ke store"))
        }
    }

    suspend fun logout() {
        mutateBusy(true)
        accessService.logout()
        refreshStage(UiBanner(UiTone.Info, "Session operator diakhiri"))
    }

    suspend fun openBusinessDay() {
        mutateBusy(true)
        val result = businessDayService.openNewDay()
        refreshStage(result.fold(
            onSuccess = { UiBanner(UiTone.Success, "Business day ${it.id} dibuka") },
            onFailure = { UiBanner(UiTone.Danger, it.message ?: "Open day gagal") }
        ))
    }

    suspend fun startShift() {
        mutateBusy(true)
        val openingCash = state.value.operations.openingCashInput.toDoubleOrNull()
        if (openingCash == null) {
            mutateBusy(false)
            return pushBanner(UiBanner(UiTone.Warning, "Opening cash harus berupa angka"))
        }
        val result = shiftService.startShift(openingCash)
        refreshStage(result.fold(
            onSuccess = { UiBanner(UiTone.Success, "Shift ${it.id} dimulai") },
            onFailure = { UiBanner(UiTone.Danger, it.message ?: "Start shift gagal") }
        ))
    }

    suspend fun endShift() {
        mutateBusy(true)
        val closingCash = state.value.operations.closingCashInput.toDoubleOrNull()
        if (closingCash == null) {
            mutateBusy(false)
            return pushBanner(UiBanner(UiTone.Warning, "Closing cash harus berupa angka"))
        }
        val result = shiftService.endShift(closingCash)
        refreshStage(result.fold(
            onSuccess = { UiBanner(UiTone.Success, "Shift ${it.id} ditutup") },
            onFailure = { UiBanner(UiTone.Danger, it.message ?: "Close shift gagal") }
        ))
    }

    suspend fun closeBusinessDay() {
        mutateBusy(true)
        val result = businessDayService.closeCurrentDay()
        refreshStage(result.fold(
            onSuccess = { UiBanner(UiTone.Success, "Business day ${it.id} ditutup") },
            onFailure = { UiBanner(UiTone.Danger, it.message ?: "Close day gagal") }
        ))
    }

    suspend fun updateCatalogQuery(query: String) {
        _state.update { it.copy(catalog = it.catalog.copy(searchQuery = query)) }
        refreshCatalog()
    }

    fun updateBarcodeInput(value: String) {
        _state.update { it.copy(catalog = it.catalog.copy(barcodeInput = value)) }
    }

    suspend fun scanBarcodeOrSku() {
        mutateBusy(true)
        val input = state.value.catalog.barcodeInput
        val banner = when (val result = productLookupUseCase.execute(input)) {
            is ProductLookupResult.FoundSingle -> {
                salesService.addProduct(result.product).fold(
                    onSuccess = { UiBanner(UiTone.Success, "${result.product.name} ditambahkan ke cart") },
                    onFailure = { UiBanner(UiTone.Danger, it.message ?: "Gagal menambah item") }
                )
            }
            ProductLookupResult.Collision -> UiBanner(UiTone.Warning, "Barcode bentrok. Gunakan pencarian manual.")
            ProductLookupResult.NotFound -> UiBanner(UiTone.Warning, "Barang tidak ditemukan untuk input: $input")
            ProductLookupResult.Unavailable -> UiBanner(UiTone.Warning, "Barang sedang tidak tersedia")
            is ProductLookupResult.InvalidInput -> UiBanner(UiTone.Warning, result.message)
        }
        _state.update { it.copy(catalog = it.catalog.copy(barcodeInput = "")) }
        refreshStage(banner)
    }

    suspend fun addProduct(product: Product) {
        mutateBusy(true)
        val result = salesService.addProduct(product)
        refreshStage(result.fold(
            onSuccess = { UiBanner(UiTone.Success, "${product.name} ditambahkan") },
            onFailure = { UiBanner(UiTone.Danger, it.message ?: "Gagal menambah item") }
        ))
    }

    suspend fun incrementItem(product: Product) {
        addProduct(product)
    }

    suspend fun decrementItem(product: Product, currentQuantity: Double) {
        mutateBusy(true)
        val result = salesService.setQuantity(product.id, currentQuantity - 1)
        refreshStage(result.fold(
            onSuccess = { UiBanner(UiTone.Info, "${product.name} diperbarui") },
            onFailure = { UiBanner(UiTone.Danger, it.message ?: "Gagal memperbarui item") }
        ))
    }

    suspend fun checkoutCash() {
        mutateBusy(true)
        val result = salesService.checkout("CASH")
        result.fold(
            onSuccess = { completion ->
                val saleId = completion.saleId
                val finalizedSale = completion.readback
                val receiptPreview = salesService.getReceiptForPrint(saleId).getOrNull()
                refreshStage(
                    UiBanner(UiTone.Success, "Transaksi ${finalizedSale.receiptSnapshot.localNumber} selesai")
                )
                _state.update {
                    it.copy(
                        catalog = it.catalog.copy(
                            lastFinalizedSaleId = saleId,
                            lastReceiptPreview = receiptPreview?.renderedContent,
                            recentSales = loadSaleHistory()
                        )
                    )
                }
            },
            onFailure = { error ->
                refreshStage(UiBanner(UiTone.Danger, error.message ?: "Finalisasi transaksi gagal"))
            }
        )
    }

    suspend fun reprintLastReceipt() {
        val saleId = state.value.catalog.lastFinalizedSaleId
            ?: return pushBanner(UiBanner(UiTone.Warning, "Belum ada struk final untuk dicetak ulang"))
        mutateBusy(true)
        val result = salesService.getReceiptForPrint(saleId, isReprint = true)
        result.fold(
            onSuccess = { receipt ->
                _state.update {
                    it.copy(
                        isBusy = false,
                        catalog = it.catalog.copy(lastReceiptPreview = receipt.renderedContent),
                        banner = UiBanner(UiTone.Info, "Struk final siap dicetak ulang")
                    )
                }
            },
            onFailure = { error ->
                pushBanner(UiBanner(UiTone.Danger, error.message ?: "Gagal memuat struk final"))
            }
        )
    }

    fun updateOpeningCashInput(value: String) {
        _state.update { it.copy(operations = it.operations.copy(openingCashInput = value)) }
    }

    fun updateClosingCashInput(value: String) {
        _state.update { it.copy(operations = it.operations.copy(closingCashInput = value)) }
    }

    fun dismissBanner() {
        _state.update { it.copy(banner = null) }
    }

    private suspend fun refreshStage(banner: UiBanner? = state.value.banner) {
        val context = accessService.restoreContext()
        val businessDay = businessDayService.getActiveBusinessDay()
        val activeShift = shiftService.getActiveShift()
        val activeOperator = context.activeOperator
        val operators = context.operators.map { OperatorOption(it.id, it.displayName, it.role.name) }
        val products = if (businessDay != null && activeShift != null) {
            loadProducts(state.value.catalog.searchQuery)
        } else {
            emptyList()
        }

        val stage = when {
            context.terminalBinding == null || context.operators.isEmpty() -> DesktopStage.Bootstrap
            context.activeSession == null -> DesktopStage.Login
            businessDay == null -> DesktopStage.OpenDay
            activeShift == null -> DesktopStage.StartShift
            else -> DesktopStage.Catalog
        }

        _state.update {
            it.copy(
                isBusy = false,
                stage = stage,
                shell = DesktopShellState(
                    storeName = context.terminalBinding?.storeName,
                    terminalName = context.terminalBinding?.terminalName,
                    operatorName = activeOperator?.displayName,
                    roleLabel = activeOperator?.role?.name,
                    dayStatus = businessDay?.status ?: "CLOSED",
                    shiftStatus = activeShift?.status ?: "LOCKED"
                ),
                login = it.login.copy(
                    operators = operators,
                    selectedOperatorId = it.login.selectedOperatorId ?: operators.firstOrNull()?.id,
                    pin = if (stage == DesktopStage.Login) it.login.pin else "",
                    feedback = if (stage == DesktopStage.Login) it.login.feedback else null
                ),
                operations = OperationsState(
                    canOpenDay = activeOperator?.role?.supports(AccessCapability.OPEN_DAY) == true,
                    blockingMessage = when {
                        context.activeSession == null -> "Login operator diperlukan"
                        businessDay == null && activeOperator?.role?.supports(AccessCapability.OPEN_DAY) != true ->
                            "Supervisor diperlukan untuk membuka business day"
                        else -> null
                    },
                    businessDayLabel = businessDay?.id,
                    shiftLabel = activeShift?.id,
                    openingCashInput = it.operations.openingCashInput,
                    closingCashInput = it.operations.closingCashInput
                ),
                catalog = it.catalog.copy(
                    products = products,
                    basket = salesService.basket.value,
                    recentSales = loadSaleHistory()
                ),
                banner = banner
            )
        }
    }

    private suspend fun refreshCatalog() {
        val products = loadProducts(state.value.catalog.searchQuery)
        _state.update { it.copy(catalog = it.catalog.copy(products = products)) }
    }

    private suspend fun loadProducts(query: String): List<Product> {
        return if (query.isBlank()) {
            productRepository.getAllProducts()
        } else {
            productRepository.searchProducts(query)
        }
    }

    private fun mutateBusy(value: Boolean) {
        _state.update { it.copy(isBusy = value) }
    }

    private fun pushBanner(banner: UiBanner) {
        _state.update { it.copy(isBusy = false, banner = banner) }
    }

    private suspend fun loadSaleHistory(): List<SaleHistoryEntry> {
        return salesService.getSaleHistory().getOrDefault(emptyList()).take(5)
    }
}

data class DesktopAppState(
    val stage: DesktopStage = DesktopStage.Loading,
    val shell: DesktopShellState = DesktopShellState(),
    val bootstrap: BootstrapState = BootstrapState(),
    val login: LoginState = LoginState(),
    val operations: OperationsState = OperationsState(),
    val catalog: DesktopCatalogState = DesktopCatalogState(),
    val banner: UiBanner? = null,
    val isBusy: Boolean = false
)

sealed interface DesktopStage {
    data object Loading : DesktopStage
    data object Bootstrap : DesktopStage
    data object Login : DesktopStage
    data object OpenDay : DesktopStage
    data object StartShift : DesktopStage
    data object Catalog : DesktopStage
    data class FatalError(val message: String) : DesktopStage
}

data class DesktopShellState(
    val storeName: String? = null,
    val terminalName: String? = null,
    val operatorName: String? = null,
    val roleLabel: String? = null,
    val dayStatus: String = "CLOSED",
    val shiftStatus: String = "LOCKED"
)

data class BootstrapState(
    val storeName: String = "",
    val terminalName: String = "",
    val cashierName: String = "",
    val cashierPin: String = "",
    val supervisorName: String = "",
    val supervisorPin: String = ""
)

enum class BootstrapField {
    StoreName,
    TerminalName,
    CashierName,
    CashierPin,
    SupervisorName,
    SupervisorPin
}

data class LoginState(
    val operators: List<OperatorOption> = emptyList(),
    val selectedOperatorId: String? = null,
    val pin: String = "",
    val feedback: String? = null
)

data class OperatorOption(
    val id: String,
    val displayName: String,
    val roleLabel: String
)

data class OperationsState(
    val canOpenDay: Boolean = false,
    val blockingMessage: String? = null,
    val businessDayLabel: String? = null,
    val shiftLabel: String? = null,
    val openingCashInput: String = "0.00",
    val closingCashInput: String = "0.00"
)

data class DesktopCatalogState(
    val searchQuery: String = "",
    val barcodeInput: String = "",
    val products: List<Product> = emptyList(),
    val basket: Basket = Basket(),
    val lastFinalizedSaleId: String? = null,
    val lastReceiptPreview: String? = null,
    val recentSales: List<SaleHistoryEntry> = emptyList()
)

enum class UiTone {
    Info,
    Success,
    Warning,
    Danger
}

data class UiBanner(
    val tone: UiTone,
    val message: String
)
