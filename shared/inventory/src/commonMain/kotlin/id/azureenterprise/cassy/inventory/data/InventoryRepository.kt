package id.azureenterprise.cassy.inventory.data

import id.azureenterprise.cassy.inventory.db.InventoryDatabase
import id.azureenterprise.cassy.inventory.domain.InventoryTransaction
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext
import kotlinx.datetime.Clock

class InventoryRepository(
    private val database: InventoryDatabase,
    private val ioDispatcher: CoroutineContext,
    private val clock: Clock
) {
    private val queries = database.inventoryDatabaseQueries

    suspend fun recordTransaction(transaction: InventoryTransaction) = withContext(ioDispatcher) {
        database.transaction {
            // Ensure balance record exists
            queries.insertBalance(
                productId = transaction.productId,
                timestamp = transaction.timestamp.toEpochMilliseconds()
            )

            // Update balance
            queries.updateBalance(
                quantity = transaction.quantity,
                timestamp = transaction.timestamp.toEpochMilliseconds(),
                productId = transaction.productId
            )

            // Record ledger
            queries.insertLedger(
                id = transaction.id,
                productId = transaction.productId,
                quantity = transaction.quantity,
                type = transaction.type.name,
                referenceId = transaction.referenceId,
                terminalId = transaction.terminalId,
                timestamp = transaction.timestamp.toEpochMilliseconds()
            )
        }
    }

    suspend fun getStockLevel(productId: String): Double = withContext(ioDispatcher) {
        queries.getBalance(productId).executeAsOneOrNull()?.quantity ?: 0.0
    }
}
