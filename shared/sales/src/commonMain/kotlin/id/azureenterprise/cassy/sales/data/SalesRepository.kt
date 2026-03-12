package id.azureenterprise.cassy.sales.data

import id.azureenterprise.cassy.db.CassyDatabase
import id.azureenterprise.cassy.db.Sale
import id.azureenterprise.cassy.db.Shift
import id.azureenterprise.cassy.sales.domain.Basket
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext
import kotlinx.datetime.Clock

class SalesRepository(
    private val database: CassyDatabase,
    private val ioDispatcher: CoroutineContext,
    private val clock: Clock
) {
    private val queries = database.cassyDatabaseQueries

    suspend fun getActiveShift(terminalId: String): Shift? = withContext(ioDispatcher) {
        queries.getActiveShift(terminalId).executeAsOneOrNull()
    }

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

            queries.insertAudit(
                id = "audit_finalize_$saleId",
                timestamp = clock.now().toEpochMilliseconds(),
                message = "Sale $saleId finalized.",
                level = "INFO"
            )

            queries.insertEvent(
                id = "event_finalize_$saleId",
                timestamp = clock.now().toEpochMilliseconds(),
                type = "SALE_COMPLETED",
                payload = "{\"saleId\":\"$saleId\"}",
                status = "PENDING"
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
}
