package id.azureenterprise.cassy.sales.di

import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import id.azureenterprise.cassy.sales.db.SalesDatabase
import org.koin.core.module.Module
import org.koin.dsl.module

actual val salesDatabaseModule: Module = module {
    single {
        val driver = AndroidSqliteDriver(SalesDatabase.Schema, get(), "sales.db")
        SalesDatabase(driver)
    }
}
