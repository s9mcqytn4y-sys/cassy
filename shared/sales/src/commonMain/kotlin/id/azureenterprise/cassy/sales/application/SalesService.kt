package id.azureenterprise.cassy.sales.application

import id.azureenterprise.cassy.inventory.data.InventoryRepository
import id.azureenterprise.cassy.inventory.domain.InventoryTransaction
import id.azureenterprise.cassy.inventory.domain.TransactionType
import id.azureenterprise.cassy.kernel.data.KernelRepository
import id.azureenterprise.cassy.kernel.domain.IdGenerator
import id.azureenterprise.cassy.masterdata.domain.Product
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
    private val inventoryRepository: InventoryRepository,
    private val kernelRepository: KernelRepository,
    private val pricingEngine: PricingEngine,
    private val clock: Clock,
    private val terminalId: String
) {
    private val _basket = MutableStateFlow(Basket())
    val basket: StateFlow<Basket> = _basket.asStateFlow()

    private var activeSaleId: String? = null

    suspend fun addProduct(product: Product, quantity: Double = 1.0) {
        val currentItems = _basket.value.items.toMutableList()
        val existingIndex = currentItems.indexOfFirst { it.product.id == product.id }

        if (existingIndex >= 0) {
            val existingItem = currentItems[existingIndex]
            currentItems[existingIndex] = existingItem.copy(quantity = existingItem.quantity + quantity)
        } else {
            currentItems.add(pricingEngine.createBasketItem(product, quantity))
        }

        updateBasket(currentItems)
    }

    suspend fun removeProduct(productId: String) {
        val currentItems = _basket.value.items.filterNot { it.product.id == productId }
        updateBasket(currentItems)
    }

    private fun updateBasket(items: List<BasketItem>) {
        val newTotals = pricingEngine.calculateTotals(items)
        _basket.value = Basket(items = items, totals = newTotals)
    }

    suspend fun checkout(paymentMethod: String): Result<String> {
        val currentBasket = _basket.value
        if (currentBasket.items.isEmpty()) return Result.failure(Exception("Cart is empty"))

        // 0. Guardrail: Business Day must be open
        if (!kernelRepository.isBusinessDayOpen()) {
            return Result.failure(Exception("Business day is not open"))
        }

        // 1. Get Active Shift
        val shift = kernelRepository.getActiveShift(terminalId) ?: return Result.failure(Exception("No active shift"))

        val saleId = activeSaleId ?: IdGenerator.nextId("sale")
        val localNumber = "INV-${clock.now().toEpochMilliseconds()}"

        // 2. Save Pending Sale
        salesRepository.saveSale(saleId, localNumber, shift.id, terminalId, currentBasket)

        // 3. Process Payment
        val paymentId = IdGenerator.nextId("pay")
        salesRepository.recordPayment(paymentId, saleId, paymentMethod, currentBasket.totals.finalTotal, "SUCCESS", null)

        // 4. Finalize Sale
        val receiptContent = buildReceiptContent(currentBasket, localNumber)
        salesRepository.finalizeSale(saleId, receiptContent)

        // 5. Update Inventory
        currentBasket.items.forEach { item ->
            inventoryRepository.recordTransaction(
                InventoryTransaction(
                    id = IdGenerator.nextId("inv"),
                    productId = item.product.id,
                    quantity = -item.quantity,
                    type = TransactionType.SALE,
                    referenceId = saleId,
                    timestamp = clock.now(),
                    terminalId = terminalId
                )
            )
        }

        // Clear local state
        _basket.value = Basket()
        activeSaleId = null

        return Result.success(saleId)
    }

    private fun buildReceiptContent(basket: Basket, orderNumber: String): String {
        return "Order: $orderNumber\nTotal: ${basket.totals.finalTotal}"
    }

    suspend fun suspendSale(): Result<Unit> {
        val saleId = activeSaleId ?: return Result.failure(Exception("No active sale to suspend"))
        salesRepository.suspendSale(saleId)
        _basket.value = Basket()
        activeSaleId = null
        return Result.success(Unit)
    }
}
