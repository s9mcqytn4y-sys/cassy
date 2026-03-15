package id.azureenterprise.cassy.inventory.di

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import id.azureenterprise.cassy.inventory.db.InventoryDatabase
import org.koin.core.module.Module
import org.koin.dsl.module
import java.io.File

actual val inventoryDatabaseModule: Module = module {
    single {
        val databasePath = File(System.getProperty("user.home"), ".cassy/inventory.db")
        databasePath.parentFile.mkdirs()
        val driver = JdbcSqliteDriver("jdbc:sqlite:${databasePath.absolutePath}")
        driver.harden()
        if (!databasePath.exists()) {
            InventoryDatabase.Schema.create(driver)
        }
        InventoryDatabase(driver)
    }
}

private fun JdbcSqliteDriver.harden() {
    execute(null, "PRAGMA foreign_keys = ON", 0)
    execute(null, "PRAGMA journal_mode = WAL", 0)
    execute(null, "PRAGMA busy_timeout = 5000", 0)
    execute(null, "PRAGMA synchronous = NORMAL", 0)
}
