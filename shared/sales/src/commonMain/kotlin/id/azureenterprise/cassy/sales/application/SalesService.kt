package id.azureenterprise.cassy.sales.application

import id.azureenterprise.cassy.inventory.application.InventoryService
import id.azureenterprise.cassy.inventory.application.SaleInventoryLine
import id.azureenterprise.cassy.kernel.data.KernelRepository
import id.azureenterprise.cassy.kernel.domain.IdGenerator
import id.azureenterprise.cassy.masterdata.domain.Product
import id.azureenterprise.cassy.masterdata.domain.ProductLookupUseCase
import id.azureenterprise.cassy.masterdata.domain.ProductLookupResult
import id.azureenterprise.cassy.sales.data.SalesRepository
import id.azureenterprise.cassy.sales.domain.Basket
import id.azureenterprise.cassy.sales.domain.BasketItem
import id.azureenterprise.cassy.sales.domain.PricingEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.Clock

class SalesService(
    private val salesRepository: SalesRepository,
    private val inventoryService: InventoryService,
    private val kernelRepository: KernelRepository,
    private val pricingEngine: PricingEngine,
    private val productLookupUseCase: ProductLookupUseCase,
    private val clock: Clock
) {
    private val _basket = MutableStateFlow(Basket())
    val basket: StateFlow<Basket> = _basket.asStateFlow()

    private var activeSaleId: String? = null

    suspend fun initialize() {
        salesRepository.getActiveBasket()?.let {
            _basket.value = it
        }
    }

    suspend fun addProductByLookup(input: String, quantity: Double = 1.0): Result<Basket> {
        val lookupResult = productLookupUseCase.execute(input)
        return when (lookupResult) {
            is ProductLookupResult.FoundSingle -> addProduct(lookupResult.product, quantity)
            is ProductLookupResult.NotFound -> Result.failure(Exception("Produk tidak ditemukan"))
            is ProductLookupResult.InvalidInput -> Result.failure(Exception(lookupResult.message))
            else -> Result.failure(Exception("Gagal mencari produk: hasil tidak unik atau tidak tersedia"))
        }
    }

    suspend fun addProduct(product: Product, quantity: Double = 1.0): Result<Basket> {
        requireOperationalContext().getOrElse { return Result.failure(it) }
        if (quantity <= 0) return Result.failure(IllegalArgumentException("Quantity harus lebih besar dari 0"))

        val currentItems = _basket.value.items.toMutableList()
        val existingIndex = currentItems.indexOfFirst { it.product.id == product.id }

        if (existingIndex >= 0) {
            val existingItem = currentItems[existingIndex]
            currentItems[existingIndex] = existingItem.copy(quantity = existingItem.quantity + quantity)
        } else {
            currentItems.add(pricingEngine.createBasketItem(product, quantity))
        }

        updateBasket(currentItems)
        return Result.success(_basket.value)
    }

    suspend fun setQuantity(productId: String, quantity: Double): Result<Basket> {
        requireOperationalContext().getOrElse { return Result.failure(it) }
        if (quantity < 0) return Result.failure(IllegalArgumentException("Quantity tidak boleh negatif"))

        val currentItems = _basket.value.items.toMutableList()
        val existingIndex = currentItems.indexOfFirst { it.product.id == productId }
        if (existingIndex < 0) return Result.failure(IllegalStateException("Item tidak ditemukan di cart"))

        if (quantity == 0.0) {
            currentItems.removeAt(existingIndex)
        } else {
            val existingItem = currentItems[existingIndex]
            currentItems[existingIndex] = existingItem.copy(quantity = quantity)
        }
        updateBasket(currentItems)
        return Result.success(_basket.value)
    }

    suspend fun removeProduct(productId: String): Result<Basket> {
        requireOperationalContext().getOrElse { return Result.failure(it) }
        val currentItems = _basket.value.items.filterNot { it.product.id == productId }
        updateBasket(currentItems)
        return Result.success(_basket.value)
    }

    private suspend fun updateBasket(items: List<BasketItem>) {
        val newTotals = pricingEngine.calculateTotals(items)
        val newBasket = Basket(items = items, totals = newTotals)
        _basket.value = newBasket
        salesRepository.saveActiveBasket(newBasket)
    }

    /**
     * M5 Note: Checkout logic is baseline only.
     * Complex payment orchestration and receipt finalization deferred to M6.
     */
    suspend fun checkout(paymentMethod: String): Result<String> {
        val currentBasket = _basket.value
        if (currentBasket.items.isEmpty()) return Result.failure(Exception("Cart is empty"))

        val binding = kernelRepository.getTerminalBinding()
            ?: return Result.failure(IllegalStateException("Terminal belum terikat"))
        requireOperationalContext().getOrElse { return Result.failure(it) }
        val shift = kernelRepository.getActiveShift(binding.terminalId)
            ?: return Result.failure(Exception("No active shift"))

        val saleId = activeSaleId ?: IdGenerator.nextId("sale")
        val localNumber = "INV-${clock.now().toEpochMilliseconds()}"

        // Save Sale & Items
        salesRepository.saveSale(saleId, localNumber, shift.id, binding.terminalId, currentBasket)

        // M5: Basic Payment Recording
        val paymentId = IdGenerator.nextId("pay")
        salesRepository.recordPayment(paymentId, saleId, paymentMethod, currentBasket.totals.finalTotal, "SUCCESS", null)

        // M5: Basic Receipt Content
        val receiptContent = "Order: $localNumber\nTotal: ${currentBasket.totals.finalTotal}"
        salesRepository.finalizeSale(saleId, receiptContent)

        // Update Inventory
        inventoryService.recordSaleCompletion(
            saleId = saleId,
            terminalId = binding.terminalId,
            lines = currentBasket.items.map {
                SaleInventoryLine(
                    productId = it.product.id,
                    quantity = it.quantity
                )
            }
        ).getOrThrow()

        clearCart()
        activeSaleId = null

        return Result.success(saleId)
    }

    suspend fun suspendSale(): Result<Unit> {
        val saleId = activeSaleId ?: return Result.failure(Exception("No active sale to suspend"))
        salesRepository.suspendSale(saleId)
        clearCart()
        activeSaleId = null
        return Result.success(Unit)
    }

    suspend fun clearCart(): Result<Basket> {
        _basket.value = Basket()
        salesRepository.clearActiveBasket()
        return Result.success(_basket.value)
    }

    private suspend fun requireOperationalContext(): Result<Unit> {
        val binding = kernelRepository.getTerminalBinding()
            ?: return Result.failure(IllegalStateException("Terminal belum terikat"))
        if (!kernelRepository.isBusinessDayOpen()) {
            return Result.failure(IllegalStateException("Business day belum aktif"))
        }
        if (kernelRepository.getActiveShift(binding.terminalId) == null) {
            return Result.failure(IllegalStateException("Shift belum aktif"))
        }
        return Result.success(Unit)
    }
}
