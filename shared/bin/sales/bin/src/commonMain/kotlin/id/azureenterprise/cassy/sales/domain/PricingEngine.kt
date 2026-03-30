package id.azureenterprise.cassy.sales.domain

import id.azureenterprise.cassy.masterdata.domain.Product

class PricingEngine {
    fun calculateTotals(items: List<BasketItem>): BasketTotals {
        val subtotal = items.sumOf { it.unitPrice * it.quantity }
        val taxTotal = items.sumOf { it.taxAmount }
        val discountTotal = items.sumOf { it.discountAmount }
        val finalTotal = subtotal - discountTotal + taxTotal

        return BasketTotals(
            subtotal = subtotal,
            taxTotal = taxTotal,
            discountTotal = discountTotal,
            finalTotal = finalTotal
        )
    }

    fun createBasketItem(product: Product, quantity: Double): BasketItem {
        return BasketItem(
            product = product,
            quantity = quantity,
            unitPrice = product.price,
            taxAmount = 0.0,
            discountAmount = 0.0
        )
    }
}
