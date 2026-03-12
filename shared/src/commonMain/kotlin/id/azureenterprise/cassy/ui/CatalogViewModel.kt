package id.azureenterprise.cassy.ui

import id.azureenterprise.cassy.masterdata.data.ProductRepository
import id.azureenterprise.cassy.masterdata.domain.Product
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CatalogViewModel(
    private val repository: ProductRepository,
    private val viewModelScope: CoroutineScope
) {
    private val _state = MutableStateFlow(CatalogState())
    val state: StateFlow<CatalogState> = _state.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val categories = repository.getAllCategories()
                val products = repository.getAllProducts()
                _state.update { it.copy(
                    categories = categories,
                    products = products,
                    isLoading = false
                ) }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun onEvent(event: CatalogEvent) {
        when (event) {
            is CatalogEvent.CategorySelected -> {
                _state.update { it.copy(selectedCategoryId = event.categoryId) }
                loadProducts()
            }
            is CatalogEvent.SearchQueryChanged -> {
                _state.update { it.copy(searchQuery = event.query) }
                loadProducts()
            }
            is CatalogEvent.AddToCart -> {
                _state.update {
                    val product = event.product
                    val currentCount = it.cart[product] ?: 0
                    val newCart = it.cart.toMutableMap()
                    newCart[product] = currentCount + 1
                    it.copy(cart = newCart)
                }
            }
            is CatalogEvent.RemoveFromCart -> {
                _state.update {
                    val product = event.product
                    val currentCount = it.cart[product] ?: 0
                    val newCart = it.cart.toMutableMap()
                    if (currentCount > 1) {
                        newCart[product] = currentCount - 1
                    } else {
                        newCart.remove(product)
                    }
                    it.copy(cart = newCart)
                }
            }
            CatalogEvent.Checkout -> performCheckout()
            CatalogEvent.ClearError -> _state.update { it.copy(error = null) }
        }
    }

    private fun loadProducts() {
        viewModelScope.launch {
            val query = _state.value.searchQuery
            val categoryId = _state.value.selectedCategoryId

            val products = if (query.isNotEmpty()) {
                repository.searchProducts(query)
            } else if (categoryId != null) {
                repository.getProductsByCategory(categoryId)
            } else {
                repository.getAllProducts()
            }

            _state.update { it.copy(products = products) }
        }
    }

    private fun performCheckout() {
        val cartItems = _state.value.cart.toList()
        if (cartItems.isEmpty()) return

        // Checkout logic moved to SalesService in real scenarios,
        // but for now we keep the repository call if it still exists there or refactor to SalesService
        // In this refactor, ProductRepository.checkout was kernel/outbox leak.
    }
}
