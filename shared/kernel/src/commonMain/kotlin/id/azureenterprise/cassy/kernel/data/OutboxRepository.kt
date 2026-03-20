package id.azureenterprise.cassy.kernel.data

import id.azureenterprise.cassy.kernel.db.KernelDatabase
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext
import kotlinx.datetime.Clock

open class OutboxRepository(
    private val database: KernelDatabase?,
    private val ioDispatcher: CoroutineContext,
    private val clock: Clock
) {
    private val queries = database?.kernelDatabaseQueries

    open suspend fun recordSale(saleId: String, amount: Double) = withContext(ioDispatcher) {
        database?.transaction {
            queries?.insertAudit(
                id = "audit_$saleId",
                timestamp = clock.now().toEpochMilliseconds(),
                message = "Sale recorded: $saleId, amount: $amount",
                level = "INFO"
            )

            queries?.insertEvent(
                id = "event_$saleId",
                timestamp = clock.now().toEpochMilliseconds(),
                type = "SALE_CREATED",
                payload = "{\"saleId\":\"$saleId\", \"amount\":$amount}",
                status = "PENDING"
            )
        }
    }

    open suspend fun getPendingEvents() = withContext(ioDispatcher) {
        queries?.selectAllEvents()?.executeAsList() ?: emptyList()
    }

    open suspend fun markEventProcessed(id: String) = withContext(ioDispatcher) {
        queries?.deleteEvent(id)
    }
}
