package id.azureenterprise.cassy.sales.domain

import id.azureenterprise.cassy.masterdata.domain.Product
import kotlin.test.Test
import kotlin.test.assertEquals

class PricingEngineTest {
    private val pricingEngine = PricingEngine()

    @Test
    fun `totals are derived from line values without UI participation`() {
        val product = Product(
            id = "p_1",
            name = "Produk",
            price = 12.5,
            categoryId = "cat",
            sku = "SKU-1"
        )

        val first = pricingEngine.createBasketItem(product, quantity = 2.0)
        val second = first.copy(quantity = 1.0, discountAmount = 2.5)

        val totals = pricingEngine.calculateTotals(listOf(first, second))

        assertEquals(37.5, totals.subtotal)
        assertEquals(2.5, totals.discountTotal)
        assertEquals(35.0, totals.finalTotal)
    }
}
