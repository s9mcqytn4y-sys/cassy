package id.azureenterprise.cassy.kernel.data

import id.azureenterprise.cassy.kernel.db.KernelDatabase
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlin.coroutines.CoroutineContext

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
        queries?.selectPendingEvents()?.executeAsList() ?: emptyList()
    }

    open suspend fun getFailedEvents() = withContext(ioDispatcher) {
        queries?.selectEventsByStatus("FAILED")?.executeAsList() ?: emptyList()
    }

    open suspend fun countEventsByStatus(status: String): Long = withContext(ioDispatcher) {
        queries?.countEventsByStatus(status)?.executeAsOneOrNull() ?: 0L
    }

    open suspend fun markEventProcessed(id: String) = withContext(ioDispatcher) {
        queries?.updateEventStatus("PROCESSED", id)
    }

    open suspend fun markEventFailed(id: String) = withContext(ioDispatcher) {
        queries?.updateEventStatus("FAILED", id)
    }

    open suspend fun requeueFailedEvents() = withContext(ioDispatcher) {
        queries?.updateEventsByStatus("PENDING", "FAILED")
    }

    open suspend fun pruneProcessedEventsBefore(cutoffEpochMs: Long) = withContext(ioDispatcher) {
        queries?.deleteProcessedEventsBefore(cutoffEpochMs)
    }
}
