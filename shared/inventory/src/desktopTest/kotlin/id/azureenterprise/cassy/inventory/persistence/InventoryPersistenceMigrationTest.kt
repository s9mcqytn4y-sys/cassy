package id.azureenterprise.cassy.inventory.persistence

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import id.azureenterprise.cassy.inventory.db.InventoryDatabase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InventoryPersistenceMigrationTest {

    @Test
    fun `inventory schema v1 migrates to current balance ledger and discrepancy baseline`() {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)

        listOf(
            """
            CREATE TABLE InventoryBalance (
                productId TEXT NOT NULL PRIMARY KEY,
                quantity REAL NOT NULL,
                lastUpdatedAt INTEGER NOT NULL
            )
            """.trimIndent(),
            """
            CREATE TABLE StockLedger (
                id TEXT NOT NULL PRIMARY KEY,
                productId TEXT NOT NULL,
                quantity REAL NOT NULL,
                type TEXT NOT NULL,
                referenceId TEXT,
                terminalId TEXT NOT NULL,
                timestamp INTEGER NOT NULL
            )
            """.trimIndent(),
            "INSERT INTO InventoryBalance(productId, quantity, lastUpdatedAt) VALUES ('product_1', 5.0, 1000)",
            "INSERT INTO InventoryBalance(productId, quantity, lastUpdatedAt) VALUES ('product_2', -2.0, 2000)",
            "INSERT INTO StockLedger(id, productId, quantity, type, referenceId, terminalId, timestamp) VALUES ('ledger_1', 'product_1', -1.0, 'SALE', 'sale_1', 'terminal_1', 1500)",
            "PRAGMA user_version = 1"
        ).forEach { statement ->
            driver.execute(null, statement, 0)
        }

        InventoryDatabase.Schema.migrate(driver, 1, InventoryDatabase.Schema.version)
        val database = InventoryDatabase(driver)

        assertTrue(driver.hasTable("InventoryBalance"))
        assertTrue(driver.hasTable("StockLedgerEntry"))
        assertTrue(driver.hasTable("InventoryDiscrepancyReview"))
        assertTrue(driver.hasTable("InventoryLayer"))

        val product1 = database.inventoryDatabaseQueries.getBalance("product_1").executeAsOne()
        val product2 = database.inventoryDatabaseQueries.getBalance("product_2").executeAsOne()
        val migratedLedger = database.inventoryDatabaseQueries.listLedgerByProduct("product_1").executeAsList()
        val unresolved = database.inventoryDatabaseQueries.listUnresolvedDiscrepancies().executeAsList()
        val layers = database.inventoryDatabaseQueries.listActiveLayersByProduct("product_1").executeAsList()

        assertEquals(5.0, product1.quantity)
        assertEquals("FIFO", product1.rotationPolicy)
        assertEquals(-2.0, product2.quantity)
        assertEquals(1, migratedLedger.size)
        assertEquals("SALE_FINALIZATION", migratedLedger.single().sourceType)
        assertEquals("sale_1", migratedLedger.single().sourceId)
        assertEquals(1, unresolved.size)
        assertEquals("product_2", unresolved.single().productId)
        assertEquals(1, layers.size)
        assertEquals(5.0, layers.single().remainingQuantity)
    }
}

private fun JdbcSqliteDriver.hasTable(tableName: String): Boolean {
    return executeQuery(
        null,
        "SELECT 1 FROM sqlite_master WHERE type = 'table' AND name = ? LIMIT 1",
        { cursor -> QueryResult.Value(cursor.next().value) },
        1
    ) {
        bindString(0, tableName)
    }.value
}
