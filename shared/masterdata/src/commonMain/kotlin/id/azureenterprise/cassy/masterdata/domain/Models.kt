package id.azureenterprise.cassy.masterdata.domain

data class Product(
    val id: String,
    val name: String,
    val price: Double,
    val categoryId: String,
    val sku: String,
    val imageUrl: String? = null,
    val isActive: Boolean = true
)

data class Category(
    val id: String,
    val name: String,
    val color: String
)
