package id.azureenterprise.cassy.inventory.di

import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import id.azureenterprise.cassy.inventory.db.InventoryDatabase
import org.koin.core.module.Module
import org.koin.dsl.module

actual val inventoryDatabaseModule: Module = module {
    single {
        val driver = AndroidSqliteDriver(InventoryDatabase.Schema, get(), "inventory.db")
        InventoryDatabase(driver)
    }
}
