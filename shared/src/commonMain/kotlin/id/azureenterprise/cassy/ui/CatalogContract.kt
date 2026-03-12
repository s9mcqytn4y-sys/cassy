package id.azureenterprise.cassy.ui

import id.azureenterprise.cassy.db.Product
import id.azureenterprise.cassy.db.ProductCategory

data class CatalogState(
    val products: List<Product> = emptyList(),
    val categories: List<ProductCategory> = emptyList(),
    val selectedCategoryId: String? = null,
    val searchQuery: String = "",
    val cart: Map<Product, Int> = emptyMap(),
    val isLoading: Boolean = false,
    val isCheckoutInProgress: Boolean = false,
    val error: String? = null
) {
    val totalPrice: Double get() = cart.entries.sumOf { it.key.price * it.value }
    val cartItemsCount: Int get() = cart.values.sum()
}

sealed interface CatalogEvent {
    data class CategorySelected(val categoryId: String?) : CatalogEvent
    data class SearchQueryChanged(val query: String) : CatalogEvent
    data class AddToCart(val product: Product) : CatalogEvent
    data class RemoveFromCart(val product: Product) : CatalogEvent
    object Checkout : CatalogEvent
    object ClearError : CatalogEvent
}
