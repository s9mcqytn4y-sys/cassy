package id.azureenterprise.cassy.inventory.application

import id.azureenterprise.cassy.inventory.data.InventoryRepository
import id.azureenterprise.cassy.inventory.domain.InventoryTransaction
import id.azureenterprise.cassy.inventory.domain.TransactionType
import kotlinx.datetime.Clock

class InventoryService(
    private val inventoryRepository: InventoryRepository,
    private val clock: Clock
) {
    private var inventorySequence: Long = 0

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

                runCatching {
                    inventoryRepository.recordTransaction(
                        InventoryTransaction(
                            id = nextInventoryId(),
                            productId = productId,
                            quantity = -quantity,
                            type = TransactionType.SALE,
                            referenceId = saleId,
                            timestamp = clock.now(),
                            terminalId = terminalId
                        )
                    )
                }.getOrElse { error ->
                    val normalized = error.message?.uppercase().orEmpty()
                    if ("UNIQUE" !in normalized && "PRIMARY KEY" !in normalized) {
                        throw error
                    }
                }
            }
    }

    private fun nextInventoryId(): String {
        inventorySequence += 1
        return "inv_${clock.now().toEpochMilliseconds()}_$inventorySequence"
    }
}

data class SaleInventoryLine(
    val productId: String,
    val quantity: Double
)
