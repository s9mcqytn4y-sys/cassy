package id.azureenterprise.cassy.data

import id.azureenterprise.cassy.db.CassyDatabase
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext
import kotlinx.datetime.Clock

class OutboxRepository(
    private val database: CassyDatabase,
    private val ioDispatcher: CoroutineContext,
    private val clock: Clock
) {
    private val queries = database.cassyDatabaseQueries

    suspend fun recordSale(saleId: String, amount: Double) = withContext(ioDispatcher) {
        database.transaction {
            queries.insertAudit(
                id = "audit_$saleId",
                timestamp = clock.now().toEpochMilliseconds(),
                message = "Sale recorded: $saleId, amount: $amount",
                level = "INFO"
            )

            queries.insertEvent(
                id = "event_$saleId",
                timestamp = clock.now().toEpochMilliseconds(),
                type = "SALE_CREATED",
                payload = "{\"saleId\":\"$saleId\", \"amount\":$amount}",
                status = "PENDING"
            )
        }
    }

    suspend fun getPendingEvents() = withContext(ioDispatcher) {
        queries.selectAllEvents().executeAsList()
    }

    suspend fun markEventProcessed(id: String) = withContext(ioDispatcher) {
        queries.deleteEvent(id)
    }
}
