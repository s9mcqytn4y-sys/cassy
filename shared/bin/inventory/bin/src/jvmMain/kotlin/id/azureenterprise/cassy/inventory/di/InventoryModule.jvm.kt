package id.azureenterprise.cassy.inventory.di

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import id.azureenterprise.cassy.inventory.db.InventoryDatabase
import org.koin.core.module.Module
import org.koin.dsl.module
import java.io.File

actual val inventoryDatabaseModule: Module = module {
    single {
        val databasePath = File(System.getProperty("user.home"), ".cassy/inventory.db")
        val databaseAlreadyExists = databasePath.exists()
        databasePath.parentFile.mkdirs()
        val driver = JdbcSqliteDriver("jdbc:sqlite:${databasePath.absolutePath}")
        driver.openOrMigrateInventoryDatabase(databaseAlreadyExists)
        InventoryDatabase(driver)
    }
}

private fun JdbcSqliteDriver.openOrMigrateInventoryDatabase(databaseAlreadyExists: Boolean) {
    harden()
    val persistedVersion = readUserVersion()
    val currentVersion = when {
        persistedVersion != 0L -> persistedVersion
        databaseAlreadyExists && hasTable("StockLedgerEntry") -> InventoryDatabase.Schema.version
        databaseAlreadyExists && hasTable("StockLedger") -> 2L
        else -> 0L
    }
    when {
        currentVersion == 0L -> {
            InventoryDatabase.Schema.create(this)
            execute(null, "PRAGMA user_version = ${InventoryDatabase.Schema.version}", 0)
        }
        currentVersion < InventoryDatabase.Schema.version -> {
            execute(null, "PRAGMA foreign_keys = OFF", 0)
            try {
                InventoryDatabase.Schema.migrate(this, currentVersion, InventoryDatabase.Schema.version)
            } finally {
                execute(null, "PRAGMA foreign_keys = ON", 0)
            }
        }
    }
    execute(null, "PRAGMA user_version = ${InventoryDatabase.Schema.version}", 0)
}

private fun JdbcSqliteDriver.readUserVersion(): Long {
    return executeQuery(
        null,
        "PRAGMA user_version",
        { cursor ->
            check(cursor.next().value)
            QueryResult.Value(cursor.getLong(0) ?: 0L)
        },
        0
    ).value
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

private fun JdbcSqliteDriver.harden() {
    execute(null, "PRAGMA foreign_keys = ON", 0)
    execute(null, "PRAGMA journal_mode = WAL", 0)
    execute(null, "PRAGMA busy_timeout = 5000", 0)
    execute(null, "PRAGMA synchronous = NORMAL", 0)
}
