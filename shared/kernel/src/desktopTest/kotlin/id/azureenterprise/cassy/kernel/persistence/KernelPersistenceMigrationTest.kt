package id.azureenterprise.cassy.kernel.persistence

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import app.cash.sqldelight.db.QueryResult
import id.azureenterprise.cassy.kernel.db.KernelDatabase
import kotlin.test.Test
import kotlin.test.assertTrue

class KernelPersistenceMigrationTest {

    @Test
    fun `kernel schema v1 migrates to current version with operational tables`() {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)

        listOf(
            """
            CREATE TABLE OutboxEvent (
                id TEXT NOT NULL PRIMARY KEY,
                timestamp INTEGER NOT NULL,
                type TEXT NOT NULL,
                payload TEXT NOT NULL,
                status TEXT NOT NULL
            )
            """.trimIndent(),
            """
            CREATE TABLE AuditLog (
                id TEXT NOT NULL PRIMARY KEY,
                timestamp INTEGER NOT NULL,
                message TEXT NOT NULL,
                level TEXT NOT NULL
            )
            """.trimIndent(),
            """
            CREATE TABLE Metadata (
                key TEXT NOT NULL PRIMARY KEY,
                value TEXT NOT NULL
            )
            """.trimIndent(),
            """
            CREATE TABLE TerminalBinding (
                terminalId TEXT NOT NULL PRIMARY KEY,
                storeId TEXT NOT NULL,
                storeName TEXT NOT NULL,
                terminalName TEXT NOT NULL,
                boundAt INTEGER NOT NULL
            )
            """.trimIndent(),
            """
            CREATE TABLE OperatorAccount (
                id TEXT NOT NULL PRIMARY KEY,
                employeeCode TEXT NOT NULL UNIQUE,
                displayName TEXT NOT NULL,
                role TEXT NOT NULL,
                pinHash TEXT NOT NULL,
                pinSalt TEXT NOT NULL,
                failedAttempts INTEGER NOT NULL DEFAULT 0,
                lockedUntil INTEGER,
                isActive INTEGER NOT NULL DEFAULT 1,
                lastLoginAt INTEGER
            )
            """.trimIndent(),
            """
            CREATE TABLE AccessSession (
                id TEXT NOT NULL PRIMARY KEY,
                operatorId TEXT NOT NULL,
                terminalId TEXT NOT NULL,
                storeId TEXT NOT NULL,
                authMode TEXT NOT NULL,
                status TEXT NOT NULL,
                createdAt INTEGER NOT NULL,
                updatedAt INTEGER NOT NULL,
                FOREIGN KEY(operatorId) REFERENCES OperatorAccount(id),
                FOREIGN KEY(terminalId) REFERENCES TerminalBinding(terminalId)
            )
            """.trimIndent(),
            """
            CREATE TABLE BusinessDay (
                id TEXT NOT NULL PRIMARY KEY,
                openedAt INTEGER NOT NULL,
                closedAt INTEGER,
                status TEXT NOT NULL
            )
            """.trimIndent(),
            """
            CREATE TABLE Shift (
                id TEXT NOT NULL PRIMARY KEY,
                businessDayId TEXT NOT NULL,
                terminalId TEXT NOT NULL,
                openedAt INTEGER NOT NULL,
                openingCash REAL NOT NULL,
                closedAt INTEGER,
                closingCash REAL,
                openedBy TEXT NOT NULL,
                closedBy TEXT,
                status TEXT NOT NULL,
                FOREIGN KEY(businessDayId) REFERENCES BusinessDay(id)
            )
            """.trimIndent(),
            "PRAGMA user_version = 1"
        ).forEach { statement ->
            driver.execute(null, statement, 0)
        }

        KernelDatabase.Schema.migrate(driver, 1, KernelDatabase.Schema.version)

        assertTrue(driver.hasTable("ReasonCode"))
        assertTrue(driver.hasTable("ApprovalRequest"))
        assertTrue(driver.hasTable("CashMovement"))
        assertTrue(driver.hasTable("ShiftCloseReport"))
        assertTrue(driver.hasTable("StoreProfile"))
        assertTrue(driver.hasColumn("OperatorAccount", "avatarAssetPath"))
        assertTrue(driver.hasColumn("StoreProfile", "businessAddressStreet"))
        assertTrue(driver.hasColumn("StoreProfile", "businessAddressNeighborhood"))
        assertTrue(driver.hasColumn("StoreProfile", "businessAddressVillage"))
        assertTrue(driver.hasColumn("StoreProfile", "businessAddressDistrict"))
        assertTrue(driver.hasColumn("StoreProfile", "businessAddressCity"))
        assertTrue(driver.hasColumn("StoreProfile", "businessAddressProvince"))
        assertTrue(driver.hasColumn("StoreProfile", "businessPostalCode"))
        assertTrue(driver.hasColumn("StoreProfile", "businessEmail"))
        assertTrue(driver.hasColumn("StoreProfile", "businessLegalId"))
        assertTrue(driver.hasColumn("StoreProfile", "showLogoOnReceipt"))
        assertTrue(driver.hasColumn("StoreProfile", "showAddressOnReceipt"))
        assertTrue(driver.hasColumn("StoreProfile", "showPhoneOnReceipt"))
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

private fun JdbcSqliteDriver.hasColumn(tableName: String, columnName: String): Boolean {
    return executeQuery(
        null,
        "PRAGMA table_info($tableName)",
        { cursor ->
            var found = false
            while (cursor.next().value) {
                if (cursor.getString(1) == columnName) {
                    found = true
                    break
                }
            }
            QueryResult.Value(found)
        },
        0
    ).value
}
