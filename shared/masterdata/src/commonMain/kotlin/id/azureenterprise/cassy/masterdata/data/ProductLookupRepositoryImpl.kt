package id.azureenterprise.cassy.masterdata.data

import id.azureenterprise.cassy.masterdata.db.MasterDataDatabase
import id.azureenterprise.cassy.masterdata.domain.Product
import id.azureenterprise.cassy.masterdata.domain.ProductLookupResult
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext
import id.azureenterprise.cassy.masterdata.db.Product as DbProduct

class ProductLookupRepositoryImpl(
    private val database: MasterDataDatabase,
    private val ioDispatcher: CoroutineContext
) {
    private val queries = database.masterDataDatabaseQueries

    suspend fun findByBarcode(barcode: String): ProductLookupResult = withContext(ioDispatcher) {
        val products = queries.findProductByBarcode(barcode).executeAsList()
        when {
            products.isEmpty() -> ProductLookupResult.NotFound
            products.size > 1 -> ProductLookupResult.Collision
            else -> ProductLookupResult.FoundSingle(products.first().toDomain())
        }
    }

    suspend fun findBySku(sku: String): ProductLookupResult = withContext(ioDispatcher) {
        val products = queries.searchProducts(sku, sku).executeAsList().filter { it.sku == sku }
        when {
            products.isEmpty() -> ProductLookupResult.NotFound
            products.size > 1 -> ProductLookupResult.Collision
            else -> ProductLookupResult.FoundSingle(products.first().toDomain())
        }
    }

    private fun DbProduct.toDomain(): Product = Product(
        id = id,
        name = name,
        price = price,
        categoryId = categoryId,
        sku = sku,
        imageUrl = imageUrl,
        isActive = isActive
    )
}
