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
        if (!databasePath.exists()) {
            InventoryDatabase.Schema.create(driver)
        }
        InventoryDatabase(driver)
    }
}
