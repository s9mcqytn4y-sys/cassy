package id.azureenterprise.cassy.inventory.di

import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import id.azureenterprise.cassy.inventory.db.InventoryDatabase
import org.koin.core.module.Module
import org.koin.dsl.module

actual val inventoryDatabaseModule: Module = module {
    single {
        val driver = AndroidSqliteDriver(InventoryDatabase.Schema, get(), "inventory.db")
        driver.harden()
        InventoryDatabase(driver)
    }
}

private fun AndroidSqliteDriver.harden() {
    execute(null, "PRAGMA foreign_keys = ON", 0)
    execute(null, "PRAGMA journal_mode = WAL", 0)
    execute(null, "PRAGMA busy_timeout = 5000", 0)
    execute(null, "PRAGMA synchronous = NORMAL", 0)
}
