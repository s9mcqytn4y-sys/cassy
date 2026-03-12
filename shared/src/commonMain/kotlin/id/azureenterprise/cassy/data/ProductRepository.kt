package id.azureenterprise.cassy.data

import id.azureenterprise.cassy.db.CassyDatabase
import id.azureenterprise.cassy.db.Product
import id.azureenterprise.cassy.db.ProductCategory
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext
import kotlinx.datetime.Clock

class ProductRepository(
    private val database: CassyDatabase,
    private val ioDispatcher: CoroutineContext,
    private val clock: Clock
) {
    private val queries = database.cassyDatabaseQueries

    suspend fun getAllProducts(): List<Product> = withContext(ioDispatcher) {
        queries.selectAllProducts().executeAsList()
    }

    suspend fun getProductsByCategory(categoryId: String): List<Product> = withContext(ioDispatcher) {
        queries.getProductsByCategory(categoryId).executeAsList()
    }

    suspend fun searchProducts(query: String): List<Product> = withContext(ioDispatcher) {
        queries.searchProducts(query, query).executeAsList()
    }

    suspend fun getAllCategories(): List<ProductCategory> = withContext(ioDispatcher) {
        queries.selectAllCategories().executeAsList()
    }

    suspend fun checkout(items: List<Pair<Product, Int>>) = withContext(ioDispatcher) {
        val saleId = "sale_${clock.now().toEpochMilliseconds()}"
        val totalAmount = items.sumOf { it.first.price * it.second }

        database.transaction {
            // Record sale intent in AuditLog
            queries.insertAudit(
                id = "audit_$saleId",
                timestamp = clock.now().toEpochMilliseconds(),
                message = "Checkout completed. Items: ${items.size}, Total: $totalAmount",
                level = "INFO"
            )

            // Atomic Outbox Event for synchronization
            queries.insertEvent(
                id = "event_$saleId",
                timestamp = clock.now().toEpochMilliseconds(),
                type = "CHECKOUT_COMPLETED",
                payload = buildPayload(saleId, items, totalAmount),
                status = "PENDING"
            )
        }
    }

    private fun buildPayload(saleId: String, items: List<Pair<Product, Int>>, total: Double): String {
        // Simple manual JSON-like string for the reference implementation
        val itemsJson = items.joinToString(",") { (p, q) ->
            "{\"id\":\"${p.id}\", \"name\":\"${p.name}\", \"price\":${p.price}, \"quantity\":$q}"
        }
        return "{\"saleId\":\"$saleId\", \"total\":$total, \"items\":[$itemsJson]}"
    }
}
