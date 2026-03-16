package id.azureenterprise.cassy.inventory.application

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import id.azureenterprise.cassy.inventory.data.InventoryRepository
import id.azureenterprise.cassy.inventory.db.InventoryDatabase
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InventoryServiceTest {

    @Test
    fun `sale completion groups duplicate product lines and writes stock through inventory owner`() {
        runBlocking {
            val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
            InventoryDatabase.Schema.create(driver)
            val repository = InventoryRepository(InventoryDatabase(driver), EmptyCoroutineContext, Clock.System)
            val service = InventoryService(repository, Clock.System)

            val result = service.recordSaleCompletion(
                saleId = "sale_1",
                terminalId = "terminal_1",
                lines = listOf(
                    SaleInventoryLine(productId = "product_1", quantity = 1.0),
                    SaleInventoryLine(productId = "product_1", quantity = 2.0)
                )
            )

            assertTrue(result.isSuccess)
            assertEquals(-3.0, repository.getStockLevel("product_1"))
            assertEquals(1, repository.getLedgerByProduct("product_1").size)
        }
    }
}
