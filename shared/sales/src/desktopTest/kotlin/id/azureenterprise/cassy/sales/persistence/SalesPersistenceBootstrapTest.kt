package id.azureenterprise.cassy.sales.persistence

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import id.azureenterprise.cassy.sales.db.SalesDatabase
import id.azureenterprise.cassy.sales.di.isForeignKeysEnabled
import id.azureenterprise.cassy.sales.di.openOrMigrateSalesDatabase
import id.azureenterprise.cassy.sales.di.readUserVersion
import java.nio.file.Files
import java.sql.DriverManager
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class SalesPersistenceBootstrapTest {

    @Test
    fun `fresh install path creates latest sales schema and enables foreign keys`() {
        val dbPath = Files.createTempFile("cassy-sales-fresh", ".db").toFile()
        val driver = JdbcSqliteDriver("jdbc:sqlite:${dbPath.absolutePath}?foreign_keys=on")

        driver.openOrMigrateSalesDatabase()

        assertEquals(SalesDatabase.Schema.version, driver.readUserVersion())
        assertTrue(driver.isForeignKeysEnabled())

        DriverManager.getConnection("jdbc:sqlite:${dbPath.absolutePath}").use { connection ->
            connection.createStatement().use { stmt ->
                stmt.executeQuery("PRAGMA table_info('ReceiptSnapshot')").use { rs ->
                    val columns = mutableSetOf<String>()
                    while (rs.next()) {
                        columns += rs.getString("name")
                    }
                    assertTrue("snapshotVersion" in columns)
                    assertTrue("templateId" in columns)
                    assertTrue("paperWidthMm" in columns)
                }
            }
        }
    }

    @Test
    fun `upgrade path migrates v5 sales schema to latest without losing persisted receipt`() {
        val dbPath = Files.createTempFile("cassy-sales-upgrade", ".db").toFile()

        DriverManager.getConnection("jdbc:sqlite:${dbPath.absolutePath}").use { connection ->
            connection.createStatement().use { stmt ->
                stmt.execute("PRAGMA user_version = 5")
                stmt.execute(
                    """
                    CREATE TABLE Sale (
                        id TEXT NOT NULL PRIMARY KEY,
                        localNumber TEXT NOT NULL,
                        shiftId TEXT NOT NULL,
                        terminalId TEXT NOT NULL,
                        timestamp INTEGER NOT NULL,
                        totalAmount REAL NOT NULL,
                        taxAmount REAL NOT NULL,
                        discountAmount REAL NOT NULL,
                        finalAmount REAL NOT NULL,
                        status TEXT NOT NULL,
                        suspendedAt INTEGER
                    )
                    """.trimIndent()
                )
                stmt.execute(
                    """
                    CREATE TABLE SaleItem (
                        id TEXT NOT NULL PRIMARY KEY,
                        saleId TEXT NOT NULL,
                        productId TEXT NOT NULL,
                        productName TEXT NOT NULL,
                        unitPrice REAL NOT NULL,
                        quantity REAL NOT NULL,
                        totalPrice REAL NOT NULL,
                        taxAmount REAL NOT NULL,
                        discountAmount REAL NOT NULL,
                        FOREIGN KEY(saleId) REFERENCES Sale(id)
                    )
                    """.trimIndent()
                )
                stmt.execute(
                    """
                    CREATE TABLE SalePayment (
                        id TEXT NOT NULL PRIMARY KEY,
                        saleId TEXT NOT NULL,
                        method TEXT NOT NULL,
                        amount REAL NOT NULL,
                        status TEXT NOT NULL,
                        statusReasonCode TEXT,
                        statusDetailMessage TEXT,
                        providerReference TEXT,
                        timestamp INTEGER NOT NULL,
                        FOREIGN KEY(saleId) REFERENCES Sale(id)
                    )
                    """.trimIndent()
                )
                stmt.execute(
                    """
                    CREATE TABLE ReceiptSnapshot (
                        saleId TEXT NOT NULL PRIMARY KEY,
                        content TEXT NOT NULL,
                        snapshotVersion INTEGER NOT NULL,
                        templateId TEXT NOT NULL,
                        paperWidthMm INTEGER NOT NULL,
                        createdAt INTEGER NOT NULL,
                        FOREIGN KEY(saleId) REFERENCES Sale(id)
                    )
                    """.trimIndent()
                )
                stmt.execute(
                    """
                    CREATE TABLE ActiveBasket (
                        id INTEGER PRIMARY KEY CHECK (id = 1),
                        content TEXT NOT NULL,
                        updatedAt INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                stmt.execute("INSERT INTO Sale(id, localNumber, shiftId, terminalId, timestamp, totalAmount, taxAmount, discountAmount, finalAmount, status) VALUES ('sale_1', 'INV-1', 'shift_1', 'terminal_1', 1, 100, 0, 0, 100, 'COMPLETED')")
                stmt.execute("INSERT INTO SalePayment(id, saleId, method, amount, status, timestamp) VALUES ('pay_1', 'sale_1', 'CASH', 100, 'SUCCESS', 1)")
                stmt.execute("INSERT INTO ReceiptSnapshot(saleId, content, snapshotVersion, templateId, paperWidthMm, createdAt) VALUES ('sale_1', '{\"version\":1,\"saleId\":\"sale_1\",\"localNumber\":\"INV-1\",\"shiftId\":\"shift_1\",\"terminalId\":\"terminal_1\",\"finalizedAtEpochMs\":1,\"payment\":{\"method\":\"CASH\",\"amount\":100.0,\"state\":{\"status\":\"SUCCESS\"}},\"totals\":{\"subtotal\":100.0,\"taxTotal\":0.0,\"discountTotal\":0.0,\"finalTotal\":100.0},\"items\":[]}', 1, 'thermal-80mm-v1', 80, 1)")
            }
        }

        val driver = JdbcSqliteDriver("jdbc:sqlite:${dbPath.absolutePath}?foreign_keys=on")
        driver.openOrMigrateSalesDatabase()

        assertEquals(SalesDatabase.Schema.version, driver.readUserVersion())

        DriverManager.getConnection("jdbc:sqlite:${dbPath.absolutePath}").use { connection ->
            connection.createStatement().use { stmt ->
                stmt.executeQuery("SELECT templateId, paperWidthMm, snapshotVersion FROM ReceiptSnapshot WHERE saleId = 'sale_1'").use { rs ->
                    assertTrue(rs.next())
                    assertEquals("thermal-80mm-v1", rs.getString("templateId"))
                    assertEquals(80, rs.getInt("paperWidthMm"))
                    assertEquals(1, rs.getInt("snapshotVersion"))
                }
                stmt.executeQuery("SELECT name FROM sqlite_master WHERE type = 'table' AND name = 'FinalizationBundle'").use { rs ->
                    assertTrue(rs.next())
                }
            }
        }
    }

    @Test
    fun `foreign key and pending payment detail constraints are enforced honestly`() {
        val dbPath = Files.createTempFile("cassy-sales-fk", ".db").toFile()
        val driver = JdbcSqliteDriver("jdbc:sqlite:${dbPath.absolutePath}?foreign_keys=on")
        driver.openOrMigrateSalesDatabase()

        assertFailsWith<Throwable> {
            driver.execute(
                null,
                "INSERT INTO SalePayment(id, saleId, method, amount, status, statusReasonCode, statusDetailMessage, providerReference, timestamp) VALUES ('pay_missing', 'missing_sale', 'CASH', 10, 'SUCCESS', NULL, NULL, NULL, 1)",
                0
            )
        }

        driver.execute(
            null,
            "INSERT INTO Sale(id, localNumber, shiftId, terminalId, timestamp, totalAmount, taxAmount, discountAmount, finalAmount, status, suspendedAt) VALUES ('sale_ok', 'INV-OK', 'shift_1', 'terminal_1', 1, 10, 0, 0, 10, 'PENDING', NULL)",
            0
        )

        assertFailsWith<Throwable> {
            driver.execute(
                null,
                "INSERT INTO SalePayment(id, saleId, method, amount, status, statusReasonCode, statusDetailMessage, providerReference, timestamp) VALUES ('pay_bad', 'sale_ok', 'CASH', 10, 'PENDING', NULL, NULL, NULL, 1)",
                0
            )
        }
    }
}
