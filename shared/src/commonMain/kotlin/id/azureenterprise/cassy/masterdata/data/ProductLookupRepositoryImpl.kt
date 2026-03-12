package id.azureenterprise.cassy.masterdata.data

import id.azureenterprise.cassy.db.CassyDatabase
import id.azureenterprise.cassy.db.Product
import id.azureenterprise.cassy.masterdata.domain.ProductLookupResult
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

class ProductLookupRepositoryImpl(
    private val database: CassyDatabase,
    private val ioDispatcher: CoroutineContext
) {
    private val queries = database.cassyDatabaseQueries

    suspend fun findByBarcode(barcode: String): ProductLookupResult = withContext(ioDispatcher) {
        val products = queries.findProductByBarcode(barcode).executeAsList()
        when {
            products.isEmpty() -> ProductLookupResult.NotFound
            products.size > 1 -> ProductLookupResult.Collision
            else -> ProductLookupResult.FoundSingle(products.first())
        }
    }

    suspend fun findBySku(sku: String): ProductLookupResult = withContext(ioDispatcher) {
        // Simple implementation for SKU lookup, using the existing searchProducts or adding a specific one
        val products = queries.searchProducts(sku, sku).executeAsList().filter { it.sku == sku }
        when {
            products.isEmpty() -> ProductLookupResult.NotFound
            products.size > 1 -> ProductLookupResult.Collision
            else -> ProductLookupResult.FoundSingle(products.first())
        }
    }
}
