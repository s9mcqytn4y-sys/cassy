package id.azureenterprise.cassy.data

import id.azureenterprise.cassy.db.CassyDatabase
import id.azureenterprise.cassy.masterdata.domain.Category
import id.azureenterprise.cassy.masterdata.domain.Product
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext
import kotlinx.datetime.Clock
import id.azureenterprise.cassy.db.Product as DbProduct
import id.azureenterprise.cassy.db.ProductCategory as DbCategory

class ProductRepository(
    private val database: CassyDatabase,
    private val ioDispatcher: CoroutineContext,
    private val clock: Clock
) {
    private val queries = database.cassyDatabaseQueries

    suspend fun getAllProducts(): List<Product> = withContext(ioDispatcher) {
        queries.selectAllProducts().executeAsList().map { it.toDomain() }
    }

    suspend fun getProductsByCategory(categoryId: String): List<Product> = withContext(ioDispatcher) {
        queries.getProductsByCategory(categoryId).executeAsList().map { it.toDomain() }
    }

    suspend fun searchProducts(query: String): List<Product> = withContext(ioDispatcher) {
        queries.searchProducts(query, query).executeAsList().map { it.toDomain() }
    }

    suspend fun getAllCategories(): List<Category> = withContext(ioDispatcher) {
        queries.selectAllCategories().executeAsList().map { it.toDomain() }
    }

    suspend fun checkout(items: List<Pair<Product, Int>>) = withContext(ioDispatcher) {
        val saleId = "sale_${clock.now().toEpochMilliseconds()}"
        val totalAmount = items.sumOf { it.first.price * it.second }

        database.transaction {
            queries.insertAudit(
                id = "audit_$saleId",
                timestamp = clock.now().toEpochMilliseconds(),
                message = "Checkout completed. Items: ${items.size}, Total: $totalAmount",
                level = "INFO"
            )

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
        val itemsJson = items.joinToString(",") { (p, q) ->
            "{\"id\":\"${p.id}\", \"name\":\"${p.name}\", \"price\":${p.price}, \"quantity\":$q}"
        }
        return "{\"saleId\":\"$saleId\", \"total\":$total, \"items\":[$itemsJson]}"
    }

    private fun DbProduct.toDomain(): Product = Product(
        id = id,
        name = name,
        price = price,
        categoryId = categoryId,
        sku = sku,
        description = description,
        imageUrl = imageUrl
    )

    private fun DbCategory.toDomain(): Category = Category(
        id = id,
        name = name,
        description = description
    )
}
