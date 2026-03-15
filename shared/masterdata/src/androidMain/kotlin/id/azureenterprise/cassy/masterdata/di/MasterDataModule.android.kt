package id.azureenterprise.cassy.masterdata.di

import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import id.azureenterprise.cassy.masterdata.db.MasterDataDatabase
import org.koin.core.module.Module
import org.koin.dsl.module

actual val masterDataDatabaseModule: Module = module {
    single {
        val driver = AndroidSqliteDriver(MasterDataDatabase.Schema, get(), "masterdata.db")
        MasterDataDatabase(driver)
    }
}
