package id.azureenterprise.cassy.desktop

import id.azureenterprise.cassy.kernel.application.AccessService
import id.azureenterprise.cassy.kernel.application.BusinessDayService
import id.azureenterprise.cassy.kernel.application.CashControlService
import id.azureenterprise.cassy.kernel.application.OperationalControlService
import id.azureenterprise.cassy.kernel.application.ShiftService
import id.azureenterprise.cassy.kernel.application.ShiftClosingService
import id.azureenterprise.cassy.kernel.domain.AccessCapability
import id.azureenterprise.cassy.kernel.domain.BootstrapStoreRequest
import id.azureenterprise.cassy.kernel.domain.CashMovementExecutionResult
import id.azureenterprise.cassy.kernel.domain.CashMovementType
import id.azureenterprise.cassy.kernel.domain.LoginResult
import id.azureenterprise.cassy.kernel.domain.OperationType
import id.azureenterprise.cassy.kernel.domain.OperationalControlSnapshot
import id.azureenterprise.cassy.kernel.domain.PendingApprovalSummary
import id.azureenterprise.cassy.kernel.domain.ReasonCategory
import id.azureenterprise.cassy.kernel.domain.ShiftCloseExecutionResult
import id.azureenterprise.cassy.kernel.domain.ShiftCloseReview
import id.azureenterprise.cassy.kernel.domain.StartShiftExecutionResult
import id.azureenterprise.cassy.kernel.domain.supports
import id.azureenterprise.cassy.masterdata.data.ProductRepository
import id.azureenterprise.cassy.masterdata.domain.Product
import id.azureenterprise.cassy.masterdata.domain.ProductLookupResult
import id.azureenterprise.cassy.masterdata.domain.ProductLookupUseCase
import id.azureenterprise.cassy.sales.application.SalesService
import id.azureenterprise.cassy.sales.domain.Basket
import id.azureenterprise.cassy.sales.domain.CashTenderQuote
import id.azureenterprise.cassy.sales.domain.CompleteSaleOutcome
import id.azureenterprise.cassy.sales.domain.ReceiptPrintState
import id.azureenterprise.cassy.sales.domain.ReceiptPrintStatus
import id.azureenterprise.cassy.sales.domain.SaleHistoryEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class DesktopAppController(
    private val accessService: AccessService,
    private val businessDayService: BusinessDayService,
    private val shiftService: ShiftService,
    private val cashControlService: CashControlService,
    private val shiftClosingService: ShiftClosingService,
    private val operationalControlService: OperationalControlService,
    private val productRepository: ProductRepository,
    private val productLookupUseCase: ProductLookupUseCase,
    private val salesService: SalesService,
    private val hardwarePort: CashierHardwarePort
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
        when (
            val result = shiftService.submitStartShift(
                openingCash = openingCash,
                approvalReason = state.value.operations.openingCashReason
            )
        ) {
            is StartShiftExecutionResult.Started -> {
                val message = if (result.approvalApplied) {
                    "Shift ${result.shift.id} dimulai dengan approval opening cash"
                } else {
                    "Shift ${result.shift.id} dimulai"
                }
                refreshStage(UiBanner(UiTone.Success, message))
            }
            is StartShiftExecutionResult.ApprovalRequired -> {
                refreshStage(UiBanner(UiTone.Warning, result.decision.message))
            }
            is StartShiftExecutionResult.Blocked -> {
                refreshStage(UiBanner(UiTone.Danger, result.decision.message))
            }
        }
    }

    suspend fun endShift() {
        mutateBusy(true)
        val closingCash = state.value.operations.closingCashInput.toDoubleOrNull()
        if (closingCash == null) {
            mutateBusy(false)
            return pushBanner(UiBanner(UiTone.Warning, "Closing cash harus berupa angka"))
        }
        when (
            val result = shiftClosingService.closeShift(
                actualCash = closingCash,
                reasonCode = state.value.operations.closeShiftReasonCode,
                reasonDetail = state.value.operations.closeShiftReasonDetail
            )
        ) {
            is ShiftCloseExecutionResult.Closed -> refreshStage(
                UiBanner(
                    if (result.approvalApplied) UiTone.Warning else UiTone.Success,
                    "Shift ${result.shift.id} ditutup. Selisih kas Rp ${result.report.variance.toInt()}"
                )
            )
            is ShiftCloseExecutionResult.ApprovalRequired -> refreshStage(UiBanner(UiTone.Warning, result.decision.message))
            is ShiftCloseExecutionResult.Blocked -> refreshStage(UiBanner(UiTone.Danger, result.decision.message))
        }
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
                    onSuccess = {
                        _state.update {
                            it.copy(
                                catalog = it.catalog.copy(
                                    lookupFeedback = LookupFeedback(
                                        tone = UiTone.Success,
                                        message = "${result.product.name} ditambahkan. Scan berulang otomatis menambah qty."
                                    )
                                )
                            )
                        }
                        UiBanner(UiTone.Success, "${result.product.name} ditambahkan ke cart")
                    },
                    onFailure = { UiBanner(UiTone.Danger, it.message ?: "Gagal menambah item") }
                )
            }
            ProductLookupResult.Collision -> {
                _state.update {
                    it.copy(
                        catalog = it.catalog.copy(
                            lookupFeedback = LookupFeedback(
                                tone = UiTone.Warning,
                                message = "Kode bentrok. Pakai pencarian nama produk agar tidak salah scan."
                            )
                        )
                    )
                }
                UiBanner(UiTone.Warning, "Barcode bentrok. Gunakan pencarian manual.")
            }
            ProductLookupResult.NotFound -> {
                _state.update {
                    it.copy(
                        catalog = it.catalog.copy(
                            lookupFeedback = LookupFeedback(
                                tone = UiTone.Warning,
                                message = "Produk belum ditemukan. Cek barcode/SKU atau daftarkan lebih dulu."
                            )
                        )
                    )
                }
                UiBanner(UiTone.Warning, "Barang tidak ditemukan untuk input: $input")
            }
            ProductLookupResult.Unavailable -> {
                _state.update {
                    it.copy(
                        catalog = it.catalog.copy(
                            lookupFeedback = LookupFeedback(
                                tone = UiTone.Warning,
                                message = "Produk sedang tidak tersedia untuk dijual."
                            )
                        )
                    )
                }
                UiBanner(UiTone.Warning, "Barang sedang tidak tersedia")
            }
            is ProductLookupResult.InvalidInput -> {
                _state.update {
                    it.copy(
                        catalog = it.catalog.copy(
                            lookupFeedback = LookupFeedback(
                                tone = UiTone.Warning,
                                message = result.message
                            )
                        )
                    )
                }
                UiBanner(UiTone.Warning, result.message)
            }
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
        val tenderQuote = currentCashTenderQuote()
            ?: return pushBanner(UiBanner(UiTone.Warning, "Masukkan uang diterima pelanggan terlebih dahulu"))
        if (!tenderQuote.isSufficient) {
            return pushBanner(
                UiBanner(
                    UiTone.Warning,
                    "Uang pelanggan kurang Rp ${tenderQuote.shortageAmount.toInt()}"
                )
            )
        }
        val result = salesService.completeSale("CASH")
        result.fold(
            onSuccess = { outcome ->
                when (outcome) {
                    is CompleteSaleOutcome.Completed -> {
                        val saleId = outcome.result.saleId
                        val finalizedSale = outcome.result.readback
                        val receiptPreview = salesService.getReceiptForPrint(saleId).getOrThrow()
                        val hardwareResult = hardwarePort.handlePostFinalization(
                            paymentMethod = finalizedSale.receiptSnapshot.payment.method,
                            receiptPayload = receiptPreview
                        )
                        refreshStage(
                            UiBanner(
                                tone = if (hardwareResult.warningMessage == null) UiTone.Success else UiTone.Warning,
                                message = hardwareResult.warningMessage
                                    ?: "Transaksi ${finalizedSale.receiptSnapshot.localNumber} selesai. Kembalian Rp ${tenderQuote.changeAmount.toInt()}"
                            )
                        )
                        _state.update {
                            it.copy(
                                hardware = hardwareResult.snapshot,
                                catalog = it.catalog.copy(
                                    lastFinalizedSaleId = saleId,
                                    lastReceiptPreview = receiptPreview.renderedContent,
                                    recentSales = loadSaleHistory(),
                                    cashReceivedInput = "",
                                    cashTenderQuote = null,
                                    receiptPreview = ReceiptPreviewState(
                                        saleId = saleId,
                                        localNumber = finalizedSale.receiptSnapshot.localNumber,
                                        content = receiptPreview.renderedContent,
                                        availabilityMessage = "Preview struk final siap ditinjau"
                                    ),
                                    printState = receiptPreview.printState,
                                    lookupFeedback = null
                                )
                            )
                        }
                    }
                    is CompleteSaleOutcome.Pending -> {
                        refreshStage(
                            UiBanner(
                                UiTone.Warning,
                                outcome.paymentState.detailMessage ?: "Pembayaran masih pending"
                            )
                        )
                    }
                    is CompleteSaleOutcome.Rejected -> {
                        refreshStage(
                            UiBanner(
                                UiTone.Danger,
                                outcome.paymentState.detailMessage ?: "Pembayaran ditolak"
                            )
                        )
                    }
                }
            },
            onFailure = { error ->
                refreshStage(UiBanner(UiTone.Danger, error.message ?: "Finalisasi transaksi gagal"))
            }
        )
    }

    suspend fun printLastReceipt() {
        val saleId = state.value.catalog.lastFinalizedSaleId
            ?: return pushBanner(UiBanner(UiTone.Warning, "Belum ada struk final yang siap dicetak"))
        mutateBusy(true)
        _state.update {
            it.copy(
                catalog = it.catalog.copy(
                    printState = ReceiptPrintState(
                        status = ReceiptPrintStatus.NOT_REQUESTED,
                        detailMessage = "Mengirim struk ke printer..."
                    )
                )
            )
        }
        val result = salesService.getReceiptForPrint(saleId)
        result.fold(
            onSuccess = { receipt ->
                val printResult = hardwarePort.printReceipt(receipt)
                _state.update {
                    it.copy(
                        isBusy = false,
                        hardware = printResult.snapshot,
                        catalog = it.catalog.copy(
                            lastReceiptPreview = receipt.renderedContent,
                            receiptPreview = ReceiptPreviewState(
                                saleId = saleId,
                                localNumber = receipt.snapshot.localNumber,
                                content = receipt.renderedContent,
                                availabilityMessage = "Preview struk final siap ditinjau"
                            ),
                            printState = printResult.printState
                        ),
                        banner = UiBanner(
                            tone = if (printResult.printState.status == ReceiptPrintStatus.PRINTED) UiTone.Success else UiTone.Warning,
                            message = printResult.printState.detailMessage ?: "Status print berubah"
                        )
                    )
                }
            },
            onFailure = { error ->
                pushBanner(UiBanner(UiTone.Danger, error.message ?: "Gagal memuat struk final"))
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
                val printResult = hardwarePort.printReceipt(receipt)
                _state.update {
                    it.copy(
                        isBusy = false,
                        hardware = printResult.snapshot,
                        catalog = it.catalog.copy(
                            lastReceiptPreview = receipt.renderedContent,
                            receiptPreview = ReceiptPreviewState(
                                saleId = saleId,
                                localNumber = receipt.snapshot.localNumber,
                                content = receipt.renderedContent,
                                availabilityMessage = "Preview struk final hasil reprint siap ditinjau"
                            ),
                            printState = printResult.printState
                        ),
                        banner = UiBanner(
                            tone = if (printResult.printState.status == ReceiptPrintStatus.PRINTED) UiTone.Success else UiTone.Warning,
                            message = printResult.printState.detailMessage ?: "Struk final siap dicetak ulang"
                        )
                    )
                }
            },
            onFailure = { error ->
                pushBanner(UiBanner(UiTone.Danger, error.message ?: "Gagal memuat struk final"))
            }
        )
    }

    suspend fun cancelCurrentSale() {
        mutateBusy(true)
        val result = salesService.cancelActiveSale()
        refreshStage(
            result.fold(
                onSuccess = { UiBanner(UiTone.Info, "Pesanan draft dibatalkan. Struk final sebelumnya tetap aman.") },
                onFailure = { UiBanner(UiTone.Danger, it.message ?: "Gagal membatalkan pesanan") }
            )
        )
    }

    fun updateOpeningCashInput(value: String) {
        _state.update { it.copy(operations = it.operations.copy(openingCashInput = value)) }
    }

    fun updateClosingCashInput(value: String) {
        _state.update { it.copy(operations = it.operations.copy(closingCashInput = value)) }
    }

    fun updateOpeningCashReasonInput(value: String) {
        _state.update { it.copy(operations = it.operations.copy(openingCashReason = value)) }
    }

    fun updateCashMovementType(type: CashMovementType) {
        _state.update { current ->
            current.copy(
                operations = current.operations.copy(
                    cashMovementType = type,
                    cashMovementReasonCode = "",
                    cashMovementReasonDetail = ""
                )
            )
        }
    }

    fun updateCashMovementAmountInput(value: String) {
        _state.update { it.copy(operations = it.operations.copy(cashMovementAmountInput = value.filter(Char::isDigit))) }
    }

    fun updateCashMovementReasonCode(value: String) {
        _state.update { it.copy(operations = it.operations.copy(cashMovementReasonCode = value)) }
    }

    fun updateCashMovementReasonDetail(value: String) {
        _state.update { it.copy(operations = it.operations.copy(cashMovementReasonDetail = value)) }
    }

    suspend fun submitCashMovement() {
        mutateBusy(true)
        val amount = state.value.operations.cashMovementAmountInput.toDoubleOrNull()
        if (amount == null) {
            mutateBusy(false)
            return pushBanner(UiBanner(UiTone.Warning, "Nominal kontrol kas harus berupa angka"))
        }
        when (
            val result = cashControlService.submitMovement(
                type = state.value.operations.cashMovementType,
                amount = amount,
                reasonCode = state.value.operations.cashMovementReasonCode,
                reasonDetail = state.value.operations.cashMovementReasonDetail
            )
        ) {
            is CashMovementExecutionResult.Recorded -> refreshStage(
                UiBanner(
                    if (result.approvalApplied) UiTone.Warning else UiTone.Success,
                    "${result.movement.type.name.replace('_', ' ')} dicatat Rp ${result.movement.amount.toInt()}"
                )
            )
            is CashMovementExecutionResult.ApprovalRequired -> refreshStage(UiBanner(UiTone.Warning, result.decision.message))
            is CashMovementExecutionResult.Blocked -> refreshStage(UiBanner(UiTone.Danger, result.decision.message))
        }
    }

    suspend fun approveCashMovement(requestId: String) {
        mutateBusy(true)
        when (val result = cashControlService.approveCashMovement(requestId)) {
            is CashMovementExecutionResult.Recorded -> refreshStage(
                UiBanner(UiTone.Success, "${result.movement.type.name.replace('_', ' ')} disetujui dan dicatat")
            )
            is CashMovementExecutionResult.ApprovalRequired -> refreshStage(UiBanner(UiTone.Warning, result.decision.message))
            is CashMovementExecutionResult.Blocked -> refreshStage(UiBanner(UiTone.Danger, result.decision.message))
        }
    }

    suspend fun denyCashMovement(requestId: String) {
        mutateBusy(true)
        val denied = cashControlService.denyCashMovement(requestId, "Ditolak dari dashboard desktop")
        refreshStage(
            UiBanner(
                tone = if (denied != null) UiTone.Warning else UiTone.Danger,
                message = if (denied != null) "Approval ${denied.id} ditolak" else "Approval tidak bisa ditolak"
            )
        )
    }

    fun updateCloseShiftReasonCode(value: String) {
        _state.update { it.copy(operations = it.operations.copy(closeShiftReasonCode = value)) }
    }

    fun updateCloseShiftReasonDetail(value: String) {
        _state.update { it.copy(operations = it.operations.copy(closeShiftReasonDetail = value)) }
    }

    suspend fun approveCloseShift(requestId: String) {
        mutateBusy(true)
        when (val result = shiftClosingService.approveCloseShift(requestId)) {
            is ShiftCloseExecutionResult.Closed -> refreshStage(
                UiBanner(UiTone.Success, "Shift ${result.shift.id} ditutup lewat approval")
            )
            is ShiftCloseExecutionResult.ApprovalRequired -> refreshStage(UiBanner(UiTone.Warning, result.decision.message))
            is ShiftCloseExecutionResult.Blocked -> refreshStage(UiBanner(UiTone.Danger, result.decision.message))
        }
    }

    suspend fun denyCloseShift(requestId: String) {
        mutateBusy(true)
        val denied = shiftClosingService.denyCloseShift(requestId, "Ditolak dari wizard close shift")
        refreshStage(
            UiBanner(
                tone = if (denied) UiTone.Warning else UiTone.Danger,
                message = if (denied) "Approval close shift ditolak" else "Approval close shift tidak bisa ditolak"
            )
        )
    }

    fun updateCashReceivedInput(value: String) {
        val digitsOnly = value.filter { it.isDigit() }
        val receivedAmount = digitsOnly.toDoubleOrNull() ?: 0.0
        _state.update {
            it.copy(
                catalog = it.catalog.copy(
                    cashReceivedInput = digitsOnly,
                    cashTenderQuote = salesService.quoteCashTender(receivedAmount).getOrNull()
                )
            )
        }
    }

    fun dismissBanner() {
        _state.update { it.copy(banner = null) }
    }

    private suspend fun refreshStage(banner: UiBanner? = state.value.banner) {
        val context = accessService.restoreContext()
        val businessDay = businessDayService.getActiveBusinessDay()
        val activeShift = shiftService.getActiveShift()
        val operationalSnapshot = operationalControlService.buildSnapshot(
            openingCashInput = state.value.operations.openingCashInput,
            openingCashReason = state.value.operations.openingCashReason
        )
        val cashReasonOptions = cashControlService.listReasonCodes(
            state.value.operations.cashMovementType.toReasonCategory()
        ).map { ReasonOption(it.code, it.title) }
        val selectedCashReasonCode = state.value.operations.cashMovementReasonCode
            .takeIf { code -> cashReasonOptions.any { it.code == code } }
            ?: cashReasonOptions.firstOrNull()?.code.orEmpty()
        val closeShiftReasonOptions = shiftClosingService.listVarianceReasonCodes()
            .map { ReasonOption(it.code, it.title) }
        val selectedCloseShiftReasonCode = state.value.operations.closeShiftReasonCode
            .takeIf { code -> closeShiftReasonOptions.any { it.code == code } }
            ?: closeShiftReasonOptions.firstOrNull()?.code.orEmpty()
        val closeShiftReview = shiftClosingService.reviewCloseShift(
            state.value.operations.closingCashInput.toDoubleOrNull()
        )
        val pendingApprovals = cashControlService.listPendingApprovals() + shiftClosingService.listPendingApprovals()
        val activeOperator = context.activeOperator
        val operators = context.operators.map { OperatorOption(it.id, it.displayName, it.role.name) }
        val products = if (operationalSnapshot.canAccessSalesHome) {
            loadProducts(state.value.catalog.searchQuery)
        } else {
            emptyList()
        }

        val stage = when {
            context.terminalBinding == null || context.operators.isEmpty() -> DesktopStage.Bootstrap
            context.activeSession == null -> DesktopStage.Login
            businessDay == null -> DesktopStage.OpenDay
            !operationalSnapshot.canAccessSalesHome -> DesktopStage.StartShift
            else -> DesktopStage.Catalog
        }
        var resolvedBanner = banner
        if (stage == DesktopStage.Catalog) {
            val recoveredCount = salesService.recoverIncompleteFinalizations().getOrDefault(0)
            if (recoveredCount > 0) {
                resolvedBanner = UiBanner(
                    UiTone.Info,
                    "Finalisasi transaksi tertunda berhasil dipulihkan tanpa efek ganda"
                )
            }
        }
        val refreshedBasket = salesService.basket.value
        val refreshedCashQuote = salesService.quoteCashTender(
            state.value.catalog.cashReceivedInput.toDoubleOrNull() ?: 0.0
        ).getOrNull()

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
                    shiftStatus = activeShift?.status ?: "LOCKED",
                    nextActionLabel = operationalSnapshot.primaryAction?.toUiLabel()
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
                        else -> operationalSnapshot.salesHomeBlocker
                    },
                    businessDayLabel = operationalSnapshot.businessDayId,
                    shiftLabel = operationalSnapshot.shiftId,
                    openingCashInput = it.operations.openingCashInput,
                    closingCashInput = it.operations.closingCashInput,
                    openingCashReason = it.operations.openingCashReason,
                    cashMovementType = it.operations.cashMovementType,
                    cashMovementAmountInput = it.operations.cashMovementAmountInput,
                    cashMovementReasonCode = selectedCashReasonCode,
                    cashMovementReasonDetail = it.operations.cashMovementReasonDetail,
                    cashMovementReasonOptions = cashReasonOptions,
                    closeShiftReasonCode = selectedCloseShiftReasonCode,
                    closeShiftReasonDetail = it.operations.closeShiftReasonDetail,
                    closeShiftReasonOptions = closeShiftReasonOptions,
                    closeShiftReview = closeShiftReview,
                    pendingApprovals = pendingApprovals,
                    dashboard = operationalSnapshot
                ),
                catalog = it.catalog.copy(
                    products = products,
                    basket = refreshedBasket,
                    recentSales = loadSaleHistory(),
                    cashTenderQuote = refreshedCashQuote
                ),
                hardware = hardwarePort.getSnapshot(),
                banner = resolvedBanner
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

    private fun currentCashTenderQuote(): CashTenderQuote? {
        return state.value.catalog.cashTenderQuote
    }
}

data class DesktopAppState(
    val stage: DesktopStage = DesktopStage.Loading,
    val shell: DesktopShellState = DesktopShellState(),
    val bootstrap: BootstrapState = BootstrapState(),
    val login: LoginState = LoginState(),
    val operations: OperationsState = OperationsState(),
    val catalog: DesktopCatalogState = DesktopCatalogState(),
    val hardware: CashierHardwareSnapshot = CashierHardwareSnapshot(),
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
    val shiftStatus: String = "LOCKED",
    val nextActionLabel: String? = null
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
    val openingCashInput: String = "",
    val closingCashInput: String = "",
    val openingCashReason: String = "",
    val cashMovementType: CashMovementType = CashMovementType.CASH_IN,
    val cashMovementAmountInput: String = "",
    val cashMovementReasonCode: String = "",
    val cashMovementReasonDetail: String = "",
    val cashMovementReasonOptions: List<ReasonOption> = emptyList(),
    val closeShiftReasonCode: String = "",
    val closeShiftReasonDetail: String = "",
    val closeShiftReasonOptions: List<ReasonOption> = emptyList(),
    val closeShiftReview: ShiftCloseReview? = null,
    val pendingApprovals: List<PendingApprovalSummary> = emptyList(),
    val dashboard: OperationalControlSnapshot = OperationalControlSnapshot(
        headline = "Operasional belum siap.",
        primaryAction = null,
        canAccessSalesHome = false,
        salesHomeBlocker = "Login operator diperlukan.",
        businessDayId = null,
        shiftId = null,
        pendingApprovalCount = 0,
        decisions = emptyList()
    )
)

data class ReasonOption(
    val code: String,
    val title: String
)

data class DesktopCatalogState(
    val searchQuery: String = "",
    val barcodeInput: String = "",
    val products: List<Product> = emptyList(),
    val basket: Basket = Basket(),
    val cashReceivedInput: String = "",
    val cashTenderQuote: CashTenderQuote? = null,
    val lastFinalizedSaleId: String? = null,
    val lastReceiptPreview: String? = null,
    val recentSales: List<SaleHistoryEntry> = emptyList(),
    val receiptPreview: ReceiptPreviewState = ReceiptPreviewState(),
    val printState: ReceiptPrintState = ReceiptPrintState(
        status = ReceiptPrintStatus.NOT_REQUESTED,
        detailMessage = "Belum ada struk final"
    ),
    val lookupFeedback: LookupFeedback? = null
)

data class ReceiptPreviewState(
    val saleId: String? = null,
    val localNumber: String? = null,
    val content: String? = null,
    val availabilityMessage: String = "Preview struk final belum tersedia"
)

data class LookupFeedback(
    val tone: UiTone,
    val message: String
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

private fun OperationType.toUiLabel(): String = when (this) {
    OperationType.OPEN_BUSINESS_DAY -> "Buka Business Day"
    OperationType.START_SHIFT -> "Buka Shift"
    OperationType.CASH_IN -> "Cash In"
    OperationType.CASH_OUT -> "Cash Out"
    OperationType.SAFE_DROP -> "Safe Drop"
    OperationType.CLOSE_SHIFT -> "Tutup Shift"
    OperationType.CLOSE_BUSINESS_DAY -> "Tutup Hari"
    OperationType.VOID_SALE -> "Review Void"
}

private fun CashMovementType.toReasonCategory(): ReasonCategory = when (this) {
    CashMovementType.CASH_IN -> ReasonCategory.CASH_IN
    CashMovementType.CASH_OUT -> ReasonCategory.CASH_OUT
    CashMovementType.SAFE_DROP -> ReasonCategory.SAFE_DROP
}
