package id.azureenterprise.cassy.sales.presentation

import id.azureenterprise.cassy.masterdata.domain.ProductLookupResult
import id.azureenterprise.cassy.masterdata.domain.ProductLookupUseCase
import id.azureenterprise.cassy.sales.application.SalesService
import id.azureenterprise.cassy.sales.domain.Basket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class SalesUiState {
    data object Idle : SalesUiState()
    data object Loading : SalesUiState()
    data class Error(val message: String) : SalesUiState()
    data class Success(val saleId: String) : SalesUiState()
    data class ProductCollision(val barcode: String) : SalesUiState()
}

class SalesViewModel(
    private val salesService: SalesService,
    private val lookupUseCase: ProductLookupUseCase,
    private val scope: CoroutineScope
) {
    val basket: StateFlow<Basket> = salesService.basket

    private val _uiState = MutableStateFlow<SalesUiState>(SalesUiState.Idle)
    val uiState: StateFlow<SalesUiState> = _uiState.asStateFlow()

    fun scanBarcode(barcode: String) {
        scope.launch {
            _uiState.value = SalesUiState.Loading
            when (val result = lookupUseCase.execute(barcode)) {
                is ProductLookupResult.FoundSingle -> {
                    // Mapping from MasterData Product to DB Product if needed,
                    // or use a generic Product interface.
                    // In this monolith-to-modular transition, we'll keep it simple.
                    // salesService.addProduct(result.product)
                }
                ProductLookupResult.Collision -> {
                    _uiState.value = SalesUiState.ProductCollision(barcode)
                }
                ProductLookupResult.NotFound -> {
                    _uiState.value = SalesUiState.Error("Product not found: $barcode")
                }
                is ProductLookupResult.InvalidInput -> {
                    _uiState.value = SalesUiState.Error(result.message)
                }
                ProductLookupResult.Unavailable -> {
                    _uiState.value = SalesUiState.Error("Product is currently unavailable")
                }
            }
        }
    }

    fun checkout(method: String) {
        scope.launch {
            _uiState.value = SalesUiState.Loading
            val result = salesService.checkout(method)
            result.onSuccess { completion ->
                _uiState.value = SalesUiState.Success(completion.saleId)
            }.onFailure { err ->
                _uiState.value = SalesUiState.Error(err.message ?: "Checkout failed")
            }
        }
    }

    fun clearState() {
        _uiState.value = SalesUiState.Idle
    }

    fun removeFromCart(productId: String) {
        scope.launch {
            salesService.removeProduct(productId)
        }
    }
}
