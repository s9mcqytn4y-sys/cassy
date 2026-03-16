package id.azureenterprise.cassy.inventory.application

import id.azureenterprise.cassy.inventory.data.InventoryRepository
import id.azureenterprise.cassy.inventory.domain.InventoryTransaction
import id.azureenterprise.cassy.inventory.domain.TransactionType
import id.azureenterprise.cassy.kernel.domain.IdGenerator
import kotlinx.datetime.Clock

class InventoryService(
    private val inventoryRepository: InventoryRepository,
    private val clock: Clock
) {
    suspend fun recordSaleCompletion(
        saleId: String,
        terminalId: String,
        lines: List<SaleInventoryLine>
    ): Result<Unit> = runCatching {
        require(saleId.isNotBlank()) { "Sale reference wajib ada" }
        require(terminalId.isNotBlank()) { "Terminal wajib ada" }
        require(lines.isNotEmpty()) { "Sale inventory lines tidak boleh kosong" }

        lines.groupBy { it.productId }
            .mapValues { (_, grouped) -> grouped.sumOf { it.quantity } }
            .forEach { (productId, quantity) ->
                require(productId.isNotBlank()) { "Product id wajib ada" }
                require(quantity > 0.0) { "Quantity sale harus lebih besar dari 0" }

                inventoryRepository.recordTransaction(
                    InventoryTransaction(
                        id = IdGenerator.nextId("inv"),
                        productId = productId,
                        quantity = -quantity,
                        type = TransactionType.SALE,
                        referenceId = saleId,
                        timestamp = clock.now(),
                        terminalId = terminalId
                    )
                )
            }
    }
}

data class SaleInventoryLine(
    val productId: String,
    val quantity: Double
)
