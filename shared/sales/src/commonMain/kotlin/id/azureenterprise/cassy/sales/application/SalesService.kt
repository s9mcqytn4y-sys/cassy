package id.azureenterprise.cassy.sales.application

import id.azureenterprise.cassy.inventory.application.InventoryService
import id.azureenterprise.cassy.inventory.application.SaleInventoryLine
import id.azureenterprise.cassy.masterdata.domain.Product
import id.azureenterprise.cassy.masterdata.domain.ProductLookupUseCase
import id.azureenterprise.cassy.masterdata.domain.ProductLookupResult
import id.azureenterprise.cassy.sales.data.SalesRepository
import id.azureenterprise.cassy.sales.domain.Basket
import id.azureenterprise.cassy.sales.domain.BasketItem
import id.azureenterprise.cassy.sales.domain.CompletedSaleReadback
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
    private val pricingEngine: PricingEngine,
    private val productLookupUseCase: ProductLookupUseCase,
    private val clock: Clock
) {
    private val json = Json
    private var idSequence: Long = 0
    private val _basket = MutableStateFlow(Basket())
    val basket: StateFlow<Basket> = _basket.asStateFlow()

    private var activeSaleId: String? = null

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

        val saleId = activeSaleId ?: nextId("sale")
        val paymentId = nextId("pay")
        val localNumber = "INV-${clock.now().toEpochMilliseconds()}"

        salesRepository.createPendingSale(
            saleId = saleId,
            paymentId = paymentId,
            localNumber = localNumber,
            shiftId = operationalContext.shiftId,
            terminalId = operationalContext.terminalId,
            basket = currentBasket,
            paymentMethod = normalizedPaymentMethod,
            paymentState = PaymentState.pending(
                detailCode = PaymentStatusDetailCode.AWAITING_FINALIZATION,
                detailMessage = "Checkout lokal sedang difinalkan"
            )
        )

        return try {
            inventoryService.recordSaleCompletion(
                saleId = saleId,
                terminalId = operationalContext.terminalId,
                lines = currentBasket.items.map {
                    SaleInventoryLine(
                        productId = it.product.id,
                        quantity = it.quantity
                    )
                }
            ).getOrThrow()

            val successfulPaymentState = PaymentState.success()
            val receiptSnapshot = ReceiptSnapshotDocument(
                saleId = saleId,
                localNumber = localNumber,
                storeName = operationalContext.storeName,
                shiftId = operationalContext.shiftId,
                terminalId = operationalContext.terminalId,
                terminalName = operationalContext.terminalName,
                finalizedAtEpochMs = clock.now().toEpochMilliseconds(),
                template = ReceiptTemplateSnapshot(),
                payment = ReceiptPaymentSnapshot(
                    method = normalizedPaymentMethod,
                    amount = currentBasket.totals.finalTotal,
                    state = successfulPaymentState
                ),
                totals = currentBasket.totals,
                items = currentBasket.items.map { item ->
                    ReceiptItemSnapshot(
                        productId = item.product.id,
                        productName = item.product.name,
                        quantity = item.quantity,
                        unitPrice = item.unitPrice,
                        lineTotal = item.totalPrice,
                        taxAmount = item.taxAmount,
                        discountAmount = item.discountAmount
                    )
                },
                footerLines = listOf("Terima kasih", "Simpan struk ini sebagai bukti pembayaran")
            )

            salesRepository.finalizeSale(
                saleId = saleId,
                paymentId = paymentId,
                receiptSnapshot = receiptSnapshot,
                paymentState = successfulPaymentState
            )
            recordFinalizationIntent(receiptSnapshot)

            val readback = salesRepository.getCompletedSaleReadback(saleId)
                ?: error("Completed sale readback tidak ditemukan setelah finalisasi")

            clearCart()
            activeSaleId = null

            Result.success(
                SaleCompletionResult(
                    saleId = saleId,
                    localNumber = localNumber,
                    readback = readback,
                    printState = ReceiptPrintState(
                        status = ReceiptPrintStatus.READY_FOR_PRINT,
                        detailMessage = "Final snapshot siap dipakai untuk print atau reprint"
                    )
                )
            )
        } catch (error: Throwable) {
            salesRepository.failCheckout(
                saleId = saleId,
                paymentId = paymentId,
                paymentState = PaymentState.failed(
                    detailCode = PaymentStatusDetailCode.TECHNICAL_FAILURE,
                    detailMessage = error.message ?: "Checkout gagal diselesaikan"
                )
            )
            Result.failure(error)
        }
    }

    suspend fun getCompletedSaleReadback(saleId: String): Result<CompletedSaleReadback> {
        return salesRepository.getCompletedSaleReadback(saleId)
            ?.let { Result.success(it) }
            ?: Result.failure(IllegalStateException("Snapshot final sale tidak ditemukan"))
    }

    suspend fun suspendSale(): Result<Unit> {
        val saleId = activeSaleId ?: return Result.failure(Exception("No active sale to suspend"))
        salesRepository.suspendSale(saleId)
        clearCart()
        activeSaleId = null
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

    private suspend fun recordFinalizationIntent(receiptSnapshot: ReceiptSnapshotDocument) {
        kernelPort.recordAudit(
            message = "Sale finalized ${receiptSnapshot.localNumber} via ${receiptSnapshot.payment.method} amount ${receiptSnapshot.totals.finalTotal}",
        )
        kernelPort.recordEvent(
            type = "SALE_FINALIZED",
            payload = json.encodeToString(receiptSnapshot)
        )
    }

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
}
