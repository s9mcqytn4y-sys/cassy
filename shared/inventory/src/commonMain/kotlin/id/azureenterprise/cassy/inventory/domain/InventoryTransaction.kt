package id.azureenterprise.cassy.inventory.domain

import kotlinx.datetime.Instant

data class InventoryTransaction(
    val id: String,
    val productId: String,
    val quantity: Double,
    val type: TransactionType,
    val referenceId: String?,
    val timestamp: Instant,
    val terminalId: String
)

enum class TransactionType {
    SALE,
    RETURN,
    ADJUSTMENT,
    RECEIVING
}
