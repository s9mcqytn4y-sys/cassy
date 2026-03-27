package id.azureenterprise.cassy.masterdata.data

import id.azureenterprise.cassy.masterdata.db.MasterDataDatabase
import id.azureenterprise.cassy.masterdata.domain.Category
import id.azureenterprise.cassy.masterdata.domain.Product
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext
import id.azureenterprise.cassy.masterdata.db.ProductBarcode as DbBarcode
import id.azureenterprise.cassy.masterdata.db.Product as DbProduct
import id.azureenterprise.cassy.masterdata.db.ProductCategory as DbCategory

class ProductRepository(
    private val database: MasterDataDatabase,
    private val ioDispatcher: CoroutineContext
) {
    private val queries = database.masterDataDatabaseQueries

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

    suspend fun getProductById(productId: String): Product? = withContext(ioDispatcher) {
        queries.selectProductById(productId).executeAsOneOrNull()?.toDomain()
    }

    suspend fun getBarcodesByProduct(productId: String): List<ProductBarcodeRecord> = withContext(ioDispatcher) {
        queries.selectBarcodesByProduct(productId).executeAsList().map { it.toDomain() }
    }

    suspend fun upsertCategory(category: Category) = withContext(ioDispatcher) {
        queries.upsertCategory(category.id, category.name, category.color)
    }

    suspend fun upsertProduct(product: Product) = withContext(ioDispatcher) {
        queries.insertProduct(
            id = product.id,
            categoryId = product.categoryId,
            name = product.name,
            price = product.price,
            sku = product.sku,
            imageUrl = product.imageUrl,
            isActive = product.isActive
        )
    }

    suspend fun upsertBarcode(barcode: String, productId: String, type: String) = withContext(ioDispatcher) {
        queries.insertBarcode(barcode = barcode, productId = productId, type = type)
    }

    suspend fun deleteBarcode(barcode: String) = withContext(ioDispatcher) {
        queries.deleteBarcode(barcode)
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

    private fun DbBarcode.toDomain(): ProductBarcodeRecord = ProductBarcodeRecord(
        barcode = barcode,
        productId = productId,
        type = type
    )
}

data class ProductBarcodeRecord(
    val barcode: String,
    val productId: String,
    val type: String
)
