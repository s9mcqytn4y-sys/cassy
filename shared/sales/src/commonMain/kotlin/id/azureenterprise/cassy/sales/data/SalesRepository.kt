package id.azureenterprise.cassy.sales.data

import id.azureenterprise.cassy.sales.db.SalesDatabase
import id.azureenterprise.cassy.sales.domain.Basket
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext
import kotlinx.datetime.Clock

class SalesRepository(
    private val database: SalesDatabase,
    private val ioDispatcher: CoroutineContext,
    private val clock: Clock
) {
    private val queries = database.salesDatabaseQueries

    suspend fun saveSale(
        saleId: String,
        localNumber: String,
        shiftId: String,
        terminalId: String,
        basket: Basket
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
                status = "PENDING"
            )

            basket.items.forEach { item ->
                queries.insertSaleItem(
                    id = "si_${saleId}_${item.product.id}",
                    saleId = saleId,
                    productId = item.product.id,
                    unitPrice = item.unitPrice,
                    quantity = item.quantity,
                    totalPrice = item.totalPrice,
                    taxAmount = item.taxAmount,
                    discountAmount = item.discountAmount
                )
            }
        }
    }

    suspend fun recordPayment(
        paymentId: String,
        saleId: String,
        method: String,
        amount: Double,
        status: String,
        providerReference: String?
    ) = withContext(ioDispatcher) {
        queries.insertSalePayment(
            id = paymentId,
            saleId = saleId,
            method = method,
            amount = amount,
            status = status,
            providerReference = providerReference,
            timestamp = clock.now().toEpochMilliseconds()
        )
    }

    suspend fun finalizeSale(saleId: String, receiptContent: String) = withContext(ioDispatcher) {
        database.transaction {
            queries.updateSaleStatus(
                status = "COMPLETED",
                suspendedAt = null,
                id = saleId
            )
            queries.insertReceiptSnapshot(
                saleId = saleId,
                content = receiptContent,
                createdAt = clock.now().toEpochMilliseconds()
            )
        }
    }

    suspend fun suspendSale(saleId: String) = withContext(ioDispatcher) {
        queries.updateSaleStatus(
            status = "SUSPENDED",
            suspendedAt = clock.now().toEpochMilliseconds(),
            id = saleId
        )
    }

    // Shift retrieval moved to KernelRepository/Service or kept as bridge
    // For now, SalesService will need a way to check shift.
}
