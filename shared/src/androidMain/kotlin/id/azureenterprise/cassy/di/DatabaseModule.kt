package id.azureenterprise.cassy.di

import id.azureenterprise.cassy.db.CassyDatabase
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import org.koin.dsl.module

actual val databaseModule = module {
    single {
        val driver = AndroidSqliteDriver(CassyDatabase.Schema, get(), "cassy.db")
        CassyDatabase(driver)
    }
}
