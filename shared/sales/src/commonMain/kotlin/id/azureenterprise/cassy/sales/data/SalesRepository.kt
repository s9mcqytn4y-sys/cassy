package id.azureenterprise.cassy.sales.data

import id.azureenterprise.cassy.sales.db.SalesDatabase
import id.azureenterprise.cassy.sales.domain.Basket
import id.azureenterprise.cassy.sales.domain.CompletedSaleReadback
import id.azureenterprise.cassy.sales.domain.FinalizationBundleState
import id.azureenterprise.cassy.sales.domain.FinalizationInventoryLine
import id.azureenterprise.cassy.sales.domain.Payment
import id.azureenterprise.cassy.sales.domain.PaymentState
import id.azureenterprise.cassy.sales.domain.PaymentStatus
import id.azureenterprise.cassy.sales.domain.PendingSaleReadback
import id.azureenterprise.cassy.sales.domain.PreparedSaleFinalizationBundle
import id.azureenterprise.cassy.sales.domain.PersistedSaleItem
import id.azureenterprise.cassy.sales.domain.PersistedReceiptSnapshot
import id.azureenterprise.cassy.sales.domain.ReceiptSnapshotDocument
import id.azureenterprise.cassy.sales.domain.SaleStatus
import id.azureenterprise.cassy.sales.domain.SaleHistoryEntry
import id.azureenterprise.cassy.kernel.domain.PendingTransactionSummary
import id.azureenterprise.cassy.kernel.domain.ShiftSalesSummary
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString

class SalesRepository(
    private val database: SalesDatabase,
    private val ioDispatcher: CoroutineContext,
    private val clock: Clock,
    private val json: Json = Json
) {
    private val queries = database.salesDatabaseQueries

    suspend fun createPendingSale(
        saleId: String,
        paymentId: String,
        localNumber: String,
        shiftId: String,
        terminalId: String,
        basket: Basket,
        paymentMethod: String,
        paymentState: PaymentState
    ) = withContext(ioDispatcher) {
        database.transaction {
            queries.insertSale(
                id = saleId,
                localNumber = localNumber,
                shiftId = shiftId,
                terminalId = terminalId,
                timestamp = clock.now().toEpochMilliseconds(),
                totalAmount = basket.totals.subtotal,
                taxAmount = basket.totals.taxTotal,
                discountAmount = basket.totals.discountTotal,
                finalAmount = basket.totals.finalTotal,
                status = SaleStatus.PENDING.name
            )

            basket.items.forEach { item ->
                queries.insertSaleItem(
                    id = "si_${saleId}_${item.product.id}",
                    saleId = saleId,
                    productId = item.product.id,
                    productName = item.product.name,
                    unitPrice = item.unitPrice,
                    quantity = item.quantity,
                    totalPrice = item.totalPrice,
                    taxAmount = item.taxAmount,
                    discountAmount = item.discountAmount
                )
            }

            queries.insertSalePayment(
                id = paymentId,
                saleId = saleId,
                method = paymentMethod,
                amount = basket.totals.finalTotal,
                status = paymentState.status.name,
                statusReasonCode = paymentState.detailCode?.name,
                statusDetailMessage = paymentState.detailMessage,
                providerReference = null,
                timestamp = clock.now().toEpochMilliseconds()
            )
        }
    }

    suspend fun finalizeSale(
        saleId: String,
        paymentId: String,
        receiptSnapshot: ReceiptSnapshotDocument,
        paymentState: PaymentState,
        providerReference: String? = null
    ) = withContext(ioDispatcher) {
        database.transaction {
            queries.updatePaymentStatus(
                status = paymentState.status.name,
                statusReasonCode = paymentState.detailCode?.name,
                statusDetailMessage = paymentState.detailMessage,
                providerReference = providerReference,
                id = paymentId
            )
            queries.updateSaleStatus(
                status = SaleStatus.COMPLETED.name,
                suspendedAt = null,
                id = saleId
            )
            queries.insertReceiptSnapshot(
                saleId = saleId,
                content = json.encodeToString(receiptSnapshot),
                snapshotVersion = receiptSnapshot.version.toLong(),
                templateId = receiptSnapshot.template.templateId,
                paperWidthMm = receiptSnapshot.template.paperWidthMm.toLong(),
                createdAt = clock.now().toEpochMilliseconds()
            )
        }
    }

    suspend fun prepareFinalizationBundle(bundle: PreparedSaleFinalizationBundle) = withContext(ioDispatcher) {
        queries.insertOrIgnoreFinalizationBundle(
            saleId = bundle.saleId,
            paymentId = bundle.paymentId,
            state = bundle.state.name,
            receiptContent = json.encodeToString(bundle.receiptSnapshot),
            paymentStatus = bundle.paymentState.status.name,
            paymentDetailCode = bundle.paymentState.detailCode?.name,
            paymentDetailMessage = bundle.paymentState.detailMessage,
            providerReference = bundle.providerReference,
            inventoryContent = json.encodeToString(bundle.inventoryLines),
            auditId = bundle.auditId,
            auditMessage = bundle.auditMessage,
            eventId = bundle.eventId,
            eventType = bundle.eventType,
            eventPayload = bundle.eventPayload,
            lastError = bundle.lastError,
            preparedAt = bundle.preparedAtEpochMs,
            updatedAt = bundle.updatedAtEpochMs
        )
    }

    suspend fun updateFinalizationBundleState(
        saleId: String,
        state: FinalizationBundleState,
        lastError: String? = null
    ) = withContext(ioDispatcher) {
        queries.updateFinalizationBundleState(
            state = state.name,
            lastError = lastError,
            updatedAt = clock.now().toEpochMilliseconds(),
            saleId = saleId
        )
    }

    suspend fun getFinalizationBundle(saleId: String): PreparedSaleFinalizationBundle? = withContext(ioDispatcher) {
        queries.getFinalizationBundle(saleId).executeAsOneOrNull()?.toBundle()
    }

    suspend fun getIncompleteFinalizationBundles(): List<PreparedSaleFinalizationBundle> = withContext(ioDispatcher) {
        queries.getIncompleteFinalizationBundles().executeAsList().map { it.toBundle() }
    }

    suspend fun commitPreparedFinalization(saleId: String) = withContext(ioDispatcher) {
        val bundle = queries.getFinalizationBundle(saleId).executeAsOneOrNull()
            ?: error("Finalization bundle tidak ditemukan untuk sale $saleId")
        database.transaction {
            queries.updatePaymentStatus(
                status = bundle.paymentStatus,
                statusReasonCode = bundle.paymentDetailCode,
                statusDetailMessage = bundle.paymentDetailMessage,
                providerReference = bundle.providerReference,
                id = bundle.paymentId
            )
            queries.updateSaleStatus(
                status = SaleStatus.COMPLETED.name,
                suspendedAt = null,
                id = saleId
            )
            val receiptSnapshot = json.decodeFromString<ReceiptSnapshotDocument>(bundle.receiptContent)
            queries.insertReceiptSnapshot(
                saleId = saleId,
                content = bundle.receiptContent,
                snapshotVersion = receiptSnapshot.version.toLong(),
                templateId = receiptSnapshot.template.templateId,
                paperWidthMm = receiptSnapshot.template.paperWidthMm.toLong(),
                createdAt = clock.now().toEpochMilliseconds()
            )
            queries.updateFinalizationBundleState(
                state = FinalizationBundleState.COMPLETED.name,
                lastError = null,
                updatedAt = clock.now().toEpochMilliseconds(),
                saleId = saleId
            )
        }
    }

    suspend fun markPaymentPending(
        saleId: String,
        paymentId: String,
        paymentState: PaymentState,
        providerReference: String? = null
    ) = withContext(ioDispatcher) {
        database.transaction {
            queries.updatePaymentStatus(
                status = paymentState.status.name,
                statusReasonCode = paymentState.detailCode?.name,
                statusDetailMessage = paymentState.detailMessage,
                providerReference = providerReference,
                id = paymentId
            )
            queries.updateSaleStatus(
                status = SaleStatus.PENDING.name,
                suspendedAt = null,
                id = saleId
            )
        }
    }

    suspend fun suspendSale(saleId: String) = withContext(ioDispatcher) {
        queries.updateSaleStatus(
            status = SaleStatus.SUSPENDED.name,
            suspendedAt = clock.now().toEpochMilliseconds(),
            id = saleId
        )
    }

    suspend fun failCheckout(
        saleId: String,
        paymentId: String,
        paymentState: PaymentState,
        providerReference: String? = null
    ) = withContext(ioDispatcher) {
        database.transaction {
            queries.updatePaymentStatus(
                status = paymentState.status.name,
                statusReasonCode = paymentState.detailCode?.name,
                statusDetailMessage = paymentState.detailMessage,
                providerReference = providerReference,
                id = paymentId
            )
            queries.updateSaleStatus(
                status = SaleStatus.CANCELLED.name,
                suspendedAt = null,
                id = saleId
            )
        }
    }

    suspend fun saveActiveBasket(basket: Basket) = withContext(ioDispatcher) {
        val content = json.encodeToString(basket)
        queries.insertOrUpdateActiveBasket(content, clock.now().toEpochMilliseconds())
    }

    suspend fun getActiveBasket(): Basket? = withContext(ioDispatcher) {
        queries.getActiveBasket().executeAsOneOrNull()?.let {
            json.decodeFromString<Basket>(it)
        }
    }

    suspend fun clearActiveBasket() = withContext(ioDispatcher) {
        queries.clearActiveBasket()
    }

    suspend fun getSaleById(saleId: String) = withContext(ioDispatcher) {
        queries.getSaleById(saleId).executeAsOneOrNull()?.toDomain()
    }

    suspend fun getPendingSaleReadback(saleId: String): PendingSaleReadback? = withContext(ioDispatcher) {
        val sale = queries.getSaleById(saleId).executeAsOneOrNull()?.toDomain() ?: return@withContext null
        val items = queries.getSaleItems(saleId).executeAsList().map {
            PersistedSaleItem(
                productId = it.productId,
                productName = it.productName.ifBlank { it.productId },
                quantity = it.quantity,
                unitPrice = it.unitPrice,
                totalPrice = it.totalPrice,
                taxAmount = it.taxAmount,
                discountAmount = it.discountAmount
            )
        }
        PendingSaleReadback(
            sale = sale,
            items = items,
            payment = getSalePayments(saleId).firstOrNull()
        )
    }

    suspend fun getCompletedSaleReadback(saleId: String): CompletedSaleReadback? = withContext(ioDispatcher) {
        val sale = queries.getSaleById(saleId).executeAsOneOrNull()?.toDomain() ?: return@withContext null
        val receipt = getPersistedReceiptSnapshot(saleId)?.snapshot ?: return@withContext null
        CompletedSaleReadback(sale = sale, receiptSnapshot = receipt)
    }

    suspend fun getPersistedReceiptSnapshot(saleId: String): PersistedReceiptSnapshot? = withContext(ioDispatcher) {
        queries.getReceiptSnapshot(saleId).executeAsOneOrNull()?.let {
            PersistedReceiptSnapshot(
                snapshot = json.decodeFromString(it.content),
                createdAtEpochMs = it.createdAt
            )
        }
    }

    suspend fun getSalePayments(saleId: String): List<Payment> = withContext(ioDispatcher) {
        queries.getSalePayments(saleId).executeAsList().map {
            Payment(
                id = it.id,
                saleId = it.saleId,
                method = it.method,
                amount = it.amount,
                state = PaymentState(
                    status = enumValueOf(it.status),
                    detailCode = it.statusReasonCode?.let(::enumValueOfOrNull),
                    detailMessage = it.statusDetailMessage
                ),
                providerReference = it.providerReference,
                timestamp = Instant.fromEpochMilliseconds(it.timestamp)
            )
        }
    }

    suspend fun getCompletedSales(): List<SaleHistoryEntry> = withContext(ioDispatcher) {
        queries.getCompletedSales().executeAsList().mapNotNull { sale ->
            val receipt = queries.getReceiptSnapshot(sale.id).executeAsOneOrNull()
                ?.content
                ?.let { json.decodeFromString<ReceiptSnapshotDocument>(it) }
                ?: return@mapNotNull null

            SaleHistoryEntry(
                saleId = sale.id,
                localNumber = sale.localNumber,
                terminalId = sale.terminalId,
                finalizedAtEpochMs = receipt.finalizedAtEpochMs,
                finalAmount = sale.finalAmount,
                paymentMethod = receipt.payment.method,
                paymentState = receipt.payment.state
            )
        }
    }

    suspend fun getShiftSalesSummary(shiftId: String): ShiftSalesSummary = withContext(ioDispatcher) {
        val pendingTransactions = queries.listPendingSalesByShift(shiftId).executeAsList().map {
            PendingTransactionSummary(
                saleId = it.id,
                localNumber = it.localNumber,
                amount = it.finalAmount
            )
        }
        val paymentRows = queries.getShiftPaymentMethodSummary(shiftId).executeAsList()
        val cashTotal = paymentRows.firstOrNull { it.method == "CASH" }?.totalAmount ?: 0.0
        val nonCashTotal = paymentRows.filterNot { it.method == "CASH" }.sumOf { it.totalAmount }
        val completedSaleCount = paymentRows.sumOf { it.paymentCount.toInt() }
        ShiftSalesSummary(
            completedCashSalesTotal = cashTotal,
            completedNonCashSalesTotal = nonCashTotal,
            completedSaleCount = completedSaleCount,
            pendingTransactions = pendingTransactions
        )
    }

    suspend fun getMultiShiftSalesSummary(shiftIds: List<String>): ShiftSalesSummary = withContext(ioDispatcher) {
        val pendingTransactions = queries.listPendingSalesByMultiShift(shiftIds).executeAsList().map {
            PendingTransactionSummary(
                saleId = it.id,
                localNumber = it.localNumber,
                amount = it.finalAmount
            )
        }
        val paymentRows = queries.getMultiShiftPaymentMethodSummary(shiftIds).executeAsList()
        val cashTotal = paymentRows.firstOrNull { it.method == "CASH" }?.totalAmount ?: 0.0
        val nonCashTotal = paymentRows.filterNot { it.method == "CASH" }.sumOf { it.totalAmount }
        val completedSaleCount = paymentRows.sumOf { it.paymentCount.toInt() }
        ShiftSalesSummary(
            completedCashSalesTotal = cashTotal,
            completedNonCashSalesTotal = nonCashTotal,
            completedSaleCount = completedSaleCount,
            pendingTransactions = pendingTransactions
        )
    }

    private inline fun <reified T : Enum<T>> enumValueOfOrNull(value: String): T? =
        runCatching { enumValueOf<T>(value) }.getOrNull()

    private fun id.azureenterprise.cassy.sales.db.FinalizationBundle.toBundle(): PreparedSaleFinalizationBundle {
        return PreparedSaleFinalizationBundle(
            saleId = saleId,
            paymentId = paymentId,
            state = FinalizationBundleState.valueOf(state),
            receiptSnapshot = json.decodeFromString(receiptContent),
            paymentState = PaymentState(
                status = enumValueOf(paymentStatus),
                detailCode = paymentDetailCode?.let(::enumValueOfOrNull),
                detailMessage = paymentDetailMessage
            ),
            providerReference = providerReference,
            inventoryLines = json.decodeFromString<List<FinalizationInventoryLine>>(inventoryContent),
            auditId = auditId,
            auditMessage = auditMessage,
            eventId = eventId,
            eventType = eventType,
            eventPayload = eventPayload,
            lastError = lastError,
            preparedAtEpochMs = preparedAt,
            updatedAtEpochMs = updatedAt
        )
    }
}
