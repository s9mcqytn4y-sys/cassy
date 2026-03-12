package id.azureenterprise.cassy.sales.domain

import id.azureenterprise.cassy.masterdata.domain.Product

data class BasketItem(
    val product: Product,
    val quantity: Double,
    val unitPrice: Double,
    val discountAmount: Double = 0.0,
    val taxAmount: Double = 0.0
) {
    val totalPrice: Double get() = (unitPrice * quantity) - discountAmount + taxAmount
}

data class BasketTotals(
    val subtotal: Double,
    val taxTotal: Double,
    val discountTotal: Double,
    val finalTotal: Double
)

data class Basket(
    val items: List<BasketItem> = emptyList(),
    val totals: BasketTotals = BasketTotals(0.0, 0.0, 0.0, 0.0)
)
