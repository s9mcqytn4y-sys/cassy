package id.azureenterprise.cassy.masterdata.data

import id.azureenterprise.cassy.db.CassyDatabase
import id.azureenterprise.cassy.masterdata.domain.Category
import id.azureenterprise.cassy.masterdata.domain.Product
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext
import id.azureenterprise.cassy.db.Product as DbProduct
import id.azureenterprise.cassy.db.ProductCategory as DbCategory

class ProductRepository(
    private val database: CassyDatabase,
    private val ioDispatcher: CoroutineContext
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

    private fun DbProduct.toDomain(): Product = Product(
        id = id,
        name = name,
        price = price,
        categoryId = categoryId,
        sku = sku,
        imageUrl = imageUrl,
        isActive = isActive
    )

    private fun DbCategory.toDomain(): Category = Category(
        id = id,
        name = name,
        color = color
    )
}
