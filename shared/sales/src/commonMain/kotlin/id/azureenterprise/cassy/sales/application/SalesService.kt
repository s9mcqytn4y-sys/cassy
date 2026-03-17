package id.azureenterprise.cassy.sales.application

import id.azureenterprise.cassy.inventory.application.InventoryService
import id.azureenterprise.cassy.inventory.application.SaleInventoryLine
import id.azureenterprise.cassy.masterdata.domain.Product
import id.azureenterprise.cassy.masterdata.domain.ProductLookupUseCase
import id.azureenterprise.cassy.masterdata.domain.ProductLookupResult
import id.azureenterprise.cassy.sales.data.SalesRepository
import id.azureenterprise.cassy.sales.domain.Basket
import id.azureenterprise.cassy.sales.domain.BasketItem
import id.azureenterprise.cassy.sales.domain.CompleteSaleOutcome
import id.azureenterprise.cassy.sales.domain.CompletedSaleReadback
import id.azureenterprise.cassy.sales.domain.PendingSaleReadback
import id.azureenterprise.cassy.sales.domain.PaymentState
import id.azureenterprise.cassy.sales.domain.PaymentStatusDetailCode
import id.azureenterprise.cassy.sales.domain.PricingEngine
import id.azureenterprise.cassy.sales.domain.ReceiptPrintPayload
import id.azureenterprise.cassy.sales.domain.ReceiptPrintState
import id.azureenterprise.cassy.sales.domain.ReceiptPrintStatus
import id.azureenterprise.cassy.sales.domain.ReceiptItemSnapshot
import id.azureenterprise.cassy.sales.domain.ReceiptPaymentSnapshot
import id.azureenterprise.cassy.sales.domain.ReceiptSnapshotDocument
import id.azureenterprise.cassy.sales.domain.ReceiptTemplateSnapshot
import id.azureenterprise.cassy.sales.domain.SaleCompletionResult
import id.azureenterprise.cassy.sales.domain.SaleHistoryEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.Clock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class SalesService(
    private val salesRepository: SalesRepository,
    private val inventoryService: InventoryService,
    private val kernelPort: SalesKernelPort,
    private val paymentGatewayPort: PaymentGatewayPort,
    private val pricingEngine: PricingEngine,
    private val productLookupUseCase: ProductLookupUseCase,
    private val clock: Clock
) {
    private val json = Json
    private var idSequence: Long = 0
    private val _basket = MutableStateFlow(Basket())
    val basket: StateFlow<Basket> = _basket.asStateFlow()

    private var activeSession: ActiveSaleSession? = null

    suspend fun initialize() {
        salesRepository.getActiveBasket()?.let {
            _basket.value = it
        }
    }

    suspend fun addProductByLookup(input: String, quantity: Double = 1.0): Result<Basket> {
        val lookupResult = productLookupUseCase.execute(input)
        return when (lookupResult) {
            is ProductLookupResult.FoundSingle -> addProduct(lookupResult.product, quantity)
            is ProductLookupResult.NotFound -> Result.failure(Exception("Produk tidak ditemukan"))
            is ProductLookupResult.InvalidInput -> Result.failure(Exception(lookupResult.message))
            else -> Result.failure(Exception("Gagal mencari produk: hasil tidak unik atau tidak tersedia"))
        }
    }

    suspend fun addProduct(product: Product, quantity: Double = 1.0): Result<Basket> {
        requireOperationalContext().getOrElse { return Result.failure(it) }
        if (quantity <= 0) return Result.failure(IllegalArgumentException("Quantity harus lebih besar dari 0"))

        val currentItems = _basket.value.items.toMutableList()
        val existingIndex = currentItems.indexOfFirst { it.product.id == product.id }

        if (existingIndex >= 0) {
            val existingItem = currentItems[existingIndex]
            currentItems[existingIndex] = existingItem.copy(quantity = existingItem.quantity + quantity)
        } else {
            currentItems.add(pricingEngine.createBasketItem(product, quantity))
        }

        updateBasket(currentItems)
        return Result.success(_basket.value)
    }

    suspend fun setQuantity(productId: String, quantity: Double): Result<Basket> {
        requireOperationalContext().getOrElse { return Result.failure(it) }
        if (quantity < 0) return Result.failure(IllegalArgumentException("Quantity tidak boleh negatif"))

        val currentItems = _basket.value.items.toMutableList()
        val existingIndex = currentItems.indexOfFirst { it.product.id == productId }
        if (existingIndex < 0) return Result.failure(IllegalStateException("Item tidak ditemukan di cart"))

        if (quantity == 0.0) {
            currentItems.removeAt(existingIndex)
        } else {
            val existingItem = currentItems[existingIndex]
            currentItems[existingIndex] = existingItem.copy(quantity = quantity)
        }
        updateBasket(currentItems)
        return Result.success(_basket.value)
    }

    suspend fun removeProduct(productId: String): Result<Basket> {
        requireOperationalContext().getOrElse { return Result.failure(it) }
        val currentItems = _basket.value.items.filterNot { it.product.id == productId }
        updateBasket(currentItems)
        return Result.success(_basket.value)
    }

    private suspend fun updateBasket(items: List<BasketItem>) {
        val newTotals = pricingEngine.calculateTotals(items)
        val newBasket = Basket(items = items, totals = newTotals)
        _basket.value = newBasket
        salesRepository.saveActiveBasket(newBasket)
    }

    suspend fun checkout(paymentMethod: String): Result<SaleCompletionResult> {
        return completeSale(paymentMethod).mapCatching { outcome ->
            when (outcome) {
                is CompleteSaleOutcome.Completed -> outcome.result
                is CompleteSaleOutcome.Pending -> error(
                    outcome.paymentState.detailMessage ?: "Pembayaran masih pending"
                )
                is CompleteSaleOutcome.Rejected -> error(
                    outcome.paymentState.detailMessage ?: "Pembayaran ditolak"
                )
            }
        }
    }

    suspend fun completeSale(paymentMethod: String): Result<CompleteSaleOutcome> {
        val currentBasket = _basket.value
        if (currentBasket.items.isEmpty()) return Result.failure(Exception("Cart is empty"))
        if (currentBasket.totals.finalTotal <= 0.0) {
            return Result.failure(IllegalStateException("Total pembayaran harus lebih besar dari 0"))
        }

        val normalizedPaymentMethod = paymentMethod.trim().uppercase()
        if (normalizedPaymentMethod !in supportedPaymentMethods) {
            return Result.failure(IllegalArgumentException("Metode pembayaran tidak valid"))
        }

        val operationalContext = requireOperationalContext().getOrElse { return Result.failure(it) }
        val session = ensureActiveSession(
            paymentMethod = normalizedPaymentMethod,
            basket = currentBasket,
            operationalContext = operationalContext
        ).getOrElse { return Result.failure(it) }

        salesRepository.getCompletedSaleReadback(session.saleId)?.let { completed ->
            ensureFinalizationIntent(completed.receiptSnapshot)
            activeSession = null
            return Result.success(
                CompleteSaleOutcome.Completed(
                    result = completionResultFrom(completed),
                    replayed = true
                )
            )
        }

        return try {
            val gatewayResult = paymentGatewayPort.finalizePayment(
                PaymentGatewayRequest(
                    saleId = session.saleId,
                    paymentId = session.paymentId,
                    localNumber = session.localNumber,
                    paymentMethod = normalizedPaymentMethod,
                    amount = currentBasket.totals.finalTotal,
                    idempotencyKey = session.saleId,
                    isRetry = session.attemptCount > 1
                )
            )
            applyGatewayResult(
                saleId = session.saleId,
                paymentId = session.paymentId,
                localNumber = session.localNumber,
                paymentMethod = normalizedPaymentMethod,
                paymentState = gatewayResult.paymentState,
                providerReference = gatewayResult.providerReference
            )
        } catch (error: Throwable) {
            salesRepository.failCheckout(
                saleId = session.saleId,
                paymentId = session.paymentId,
                paymentState = PaymentState.failed(
                    detailCode = PaymentStatusDetailCode.TECHNICAL_FAILURE,
                    detailMessage = error.message ?: "Checkout gagal diselesaikan"
                )
            )
            activeSession = null
            Result.failure(error)
        }
    }

    suspend fun handlePaymentCallback(request: PaymentCallbackRequest): Result<CompleteSaleOutcome> {
        if (request.providerReference.isBlank()) {
            return Result.failure(IllegalArgumentException("Provider reference callback wajib ada"))
        }

        salesRepository.getCompletedSaleReadback(request.saleId)?.let { completed ->
            ensureFinalizationIntent(completed.receiptSnapshot)
            if (activeSession?.saleId == request.saleId) {
                activeSession = null
            }
            return Result.success(
                CompleteSaleOutcome.Completed(
                    result = completionResultFrom(completed),
                    replayed = true
                )
            )
        }

        val pendingSale = salesRepository.getPendingSaleReadback(request.saleId)
            ?: return Result.failure(IllegalStateException("Sale callback tidak ditemukan"))
        val payment = pendingSale.payment
            ?: return Result.failure(IllegalStateException("Payment callback tidak ditemukan"))

        return applyGatewayResult(
            saleId = pendingSale.sale.id,
            paymentId = payment.id,
            localNumber = pendingSale.sale.localNumber,
            paymentMethod = payment.method,
            paymentState = request.paymentState,
            providerReference = request.providerReference
        )
    }

    suspend fun getCompletedSaleReadback(saleId: String): Result<CompletedSaleReadback> {
        return salesRepository.getCompletedSaleReadback(saleId)
            ?.let { Result.success(it) }
            ?: Result.failure(IllegalStateException("Snapshot final sale tidak ditemukan"))
    }

    suspend fun suspendSale(): Result<Unit> {
        val saleId = activeSession?.saleId ?: return Result.failure(Exception("No active sale to suspend"))
        salesRepository.suspendSale(saleId)
        clearCart()
        activeSession = null
        return Result.success(Unit)
    }

    suspend fun clearCart(): Result<Basket> {
        _basket.value = Basket()
        salesRepository.clearActiveBasket()
        return Result.success(_basket.value)
    }

    private suspend fun requireOperationalContext(): Result<SalesOperationalContext> {
        return kernelPort.getOperationalContext()
            ?.let { Result.success(it) }
            ?: Result.failure(IllegalStateException("Konteks operasional belum valid"))
    }

    suspend fun getFinalizedSale(saleId: String): Result<CompletedSaleReadback> {
        return getCompletedSaleReadback(saleId)
    }

    suspend fun getSaleHistory(): Result<List<SaleHistoryEntry>> {
        return Result.success(salesRepository.getCompletedSales())
    }

    suspend fun getReceiptForPrint(saleId: String, isReprint: Boolean = false): Result<ReceiptPrintPayload> {
        val finalizedSale = salesRepository.getCompletedSaleReadback(saleId)
            ?: return Result.failure(IllegalStateException("Struk final tidak ditemukan"))
        val persistedReceipt = salesRepository.getPersistedReceiptSnapshot(saleId)
            ?: return Result.failure(IllegalStateException("Snapshot final receipt tidak ditemukan"))
        return Result.success(
            ReceiptPrintPayload(
                saleId = saleId,
                snapshot = finalizedSale.receiptSnapshot,
                renderedContent = renderReceipt(persistedReceipt.snapshot),
                templateId = persistedReceipt.snapshot.template.templateId,
                paperWidthMm = persistedReceipt.snapshot.template.paperWidthMm,
                printState = ReceiptPrintState(
                    status = ReceiptPrintStatus.READY_FOR_PRINT,
                    detailMessage = if (isReprint) {
                        "Snapshot final siap dicetak ulang"
                    } else {
                        "Snapshot final siap dicetak"
                    }
                ),
                isReprint = isReprint
            )
        )
    }

    private suspend fun ensureFinalizationIntent(receiptSnapshot: ReceiptSnapshotDocument) {
        kernelPort.recordAudit(
            auditId = "audit_sale_finalized_${receiptSnapshot.saleId}",
            message = "Sale finalized ${receiptSnapshot.localNumber} via ${receiptSnapshot.payment.method} amount ${receiptSnapshot.totals.finalTotal}",
        )
        kernelPort.recordEvent(
            eventId = "event_sale_finalized_${receiptSnapshot.saleId}",
            type = "SALE_FINALIZED",
            payload = json.encodeToString(receiptSnapshot)
        )
    }

    private suspend fun ensureActiveSession(
        paymentMethod: String,
        basket: Basket,
        operationalContext: SalesOperationalContext
    ): Result<ActiveSaleSession> {
        activeSession?.let { existing ->
            if (existing.paymentMethod != paymentMethod) {
                return Result.failure(
                    IllegalStateException("Masih ada finalisasi aktif dengan metode pembayaran berbeda")
                )
            }
            val retried = existing.copy(attemptCount = existing.attemptCount + 1)
            activeSession = retried
            return Result.success(retried)
        }

        val created = ActiveSaleSession(
            saleId = nextId("sale"),
            paymentId = nextId("pay"),
            localNumber = "INV-${clock.now().toEpochMilliseconds()}",
            paymentMethod = paymentMethod,
            attemptCount = 1
        )
        salesRepository.createPendingSale(
            saleId = created.saleId,
            paymentId = created.paymentId,
            localNumber = created.localNumber,
            shiftId = operationalContext.shiftId,
            terminalId = operationalContext.terminalId,
            basket = basket,
            paymentMethod = paymentMethod,
            paymentState = PaymentState.pending(
                detailCode = PaymentStatusDetailCode.AWAITING_FINALIZATION,
                detailMessage = "Checkout lokal sedang difinalkan"
            )
        )
        activeSession = created
        return Result.success(created)
    }

    private suspend fun applyGatewayResult(
        saleId: String,
        paymentId: String,
        localNumber: String,
        paymentMethod: String,
        paymentState: PaymentState,
        providerReference: String?
    ): Result<CompleteSaleOutcome> {
        salesRepository.getCompletedSaleReadback(saleId)?.let { completed ->
            ensureFinalizationIntent(completed.receiptSnapshot)
            if (activeSession?.saleId == saleId) {
                activeSession = null
            }
            return Result.success(
                CompleteSaleOutcome.Completed(
                    result = completionResultFrom(completed),
                    replayed = true
                )
            )
        }

        val pendingSale = salesRepository.getPendingSaleReadback(saleId)
            ?: return Result.failure(IllegalStateException("Sale pending tidak ditemukan"))

        return when {
            paymentState.canCompleteSale -> finalizeCompletedSale(
                pendingSale = pendingSale,
                paymentId = paymentId,
                paymentMethod = paymentMethod,
                paymentState = paymentState,
                providerReference = providerReference
            )
            paymentState.status == id.azureenterprise.cassy.sales.domain.PaymentStatus.PENDING -> {
                salesRepository.markPaymentPending(
                    saleId = saleId,
                    paymentId = paymentId,
                    paymentState = paymentState,
                    providerReference = providerReference
                )
                Result.success(
                    CompleteSaleOutcome.Pending(
                        saleId = saleId,
                        localNumber = localNumber,
                        paymentState = paymentState
                    )
                )
            }
            else -> {
                salesRepository.failCheckout(
                    saleId = saleId,
                    paymentId = paymentId,
                    paymentState = paymentState,
                    providerReference = providerReference
                )
                activeSession = null
                Result.success(
                    CompleteSaleOutcome.Rejected(
                        saleId = saleId,
                        localNumber = localNumber,
                        paymentState = paymentState
                    )
                )
            }
        }
    }

    private suspend fun finalizeCompletedSale(
        pendingSale: PendingSaleReadback,
        paymentId: String,
        paymentMethod: String,
        paymentState: PaymentState,
        providerReference: String?
    ): Result<CompleteSaleOutcome> {
        val contextualInfo = kernelPort.getOperationalContext()
        val receiptSnapshot = ReceiptSnapshotDocument(
            saleId = pendingSale.sale.id,
            localNumber = pendingSale.sale.localNumber,
            storeName = contextualInfo?.storeName ?: "",
            shiftId = pendingSale.sale.shiftId,
            terminalId = pendingSale.sale.terminalId,
            terminalName = contextualInfo?.terminalName,
            finalizedAtEpochMs = clock.now().toEpochMilliseconds(),
            template = ReceiptTemplateSnapshot(),
            payment = ReceiptPaymentSnapshot(
                method = paymentMethod,
                amount = pendingSale.sale.finalAmount,
                state = paymentState,
                providerReference = providerReference
            ),
            totals = basketTotalsFrom(pendingSale),
            items = pendingSale.items.map { item ->
                ReceiptItemSnapshot(
                    productId = item.productId,
                    productName = item.productName,
                    quantity = item.quantity,
                    unitPrice = item.unitPrice,
                    lineTotal = item.totalPrice,
                    taxAmount = item.taxAmount,
                    discountAmount = item.discountAmount
                )
            },
            footerLines = listOf("Terima kasih", "Simpan struk ini sebagai bukti pembayaran")
        )

        inventoryService.recordSaleCompletion(
            saleId = pendingSale.sale.id,
            terminalId = pendingSale.sale.terminalId,
            lines = pendingSale.items.map {
                SaleInventoryLine(
                    productId = it.productId,
                    quantity = it.quantity
                )
            }
        ).getOrThrow()

        salesRepository.finalizeSale(
            saleId = pendingSale.sale.id,
            paymentId = paymentId,
            receiptSnapshot = receiptSnapshot,
            paymentState = paymentState,
            providerReference = providerReference
        )
        ensureFinalizationIntent(receiptSnapshot)

        val readback = salesRepository.getCompletedSaleReadback(pendingSale.sale.id)
            ?: error("Completed sale readback tidak ditemukan setelah finalisasi")

        if (activeSession?.saleId == pendingSale.sale.id) {
            clearCart()
            activeSession = null
        }

        return Result.success(
            CompleteSaleOutcome.Completed(
                result = completionResultFrom(readback)
            )
        )
    }

    private fun completionResultFrom(readback: CompletedSaleReadback): SaleCompletionResult {
        return SaleCompletionResult(
            saleId = readback.sale.id,
            localNumber = readback.receiptSnapshot.localNumber,
            readback = readback,
            printState = ReceiptPrintState(
                status = ReceiptPrintStatus.READY_FOR_PRINT,
                detailMessage = "Final snapshot siap dipakai untuk print atau reprint"
            )
        )
    }

    private fun basketTotalsFrom(pendingSale: PendingSaleReadback) = id.azureenterprise.cassy.sales.domain.BasketTotals(
        subtotal = pendingSale.sale.totalAmount,
        taxTotal = pendingSale.sale.taxAmount,
        discountTotal = pendingSale.sale.discountAmount,
        finalTotal = pendingSale.sale.finalAmount
    )

    private fun renderReceipt(receiptSnapshot: ReceiptSnapshotDocument): String {
        val lines = buildList {
            add(receiptSnapshot.storeName.ifBlank { "Cassy POS" })
            add("No. Struk: ${receiptSnapshot.localNumber}")
            add("Terminal: ${receiptSnapshot.terminalName ?: receiptSnapshot.terminalId}")
            add("Waktu: ${receiptSnapshot.finalizedAtEpochMs}")
            add("Pembayaran: ${receiptSnapshot.payment.method} (${receiptSnapshot.payment.state.status})")
            add("")
            receiptSnapshot.items.forEach { item ->
                add("${item.productName} x${item.quantity} = ${item.lineTotal}")
            }
            add("")
            add("Subtotal: ${receiptSnapshot.totals.subtotal}")
            add("Pajak: ${receiptSnapshot.totals.taxTotal}")
            add("Diskon: ${receiptSnapshot.totals.discountTotal}")
            add("Total: ${receiptSnapshot.totals.finalTotal}")
            if (receiptSnapshot.footerLines.isNotEmpty()) {
                add("")
                receiptSnapshot.footerLines.forEach(::add)
            }
        }
        return lines.joinToString(separator = "\n")
    }

    companion object {
        private val supportedPaymentMethods = setOf("CASH", "CARD", "QRIS")
    }

    private fun nextId(prefix: String): String {
        idSequence += 1
        return "${prefix}_${clock.now().toEpochMilliseconds()}_$idSequence"
    }

    private data class ActiveSaleSession(
        val saleId: String,
        val paymentId: String,
        val localNumber: String,
        val paymentMethod: String,
        val attemptCount: Int
    )
}
