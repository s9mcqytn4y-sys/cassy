package id.azureenterprise.cassy.masterdata.di

import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import id.azureenterprise.cassy.masterdata.db.MasterDataDatabase
import org.koin.core.module.Module
import org.koin.dsl.module

actual val masterDataDatabaseModule: Module = module {
    single {
        val driver = AndroidSqliteDriver(MasterDataDatabase.Schema, get(), "masterdata.db")
        driver.harden()
        MasterDataDatabase(driver)
    }
}

private fun AndroidSqliteDriver.harden() {
    execute(null, "PRAGMA foreign_keys = ON", 0)
    execute(null, "PRAGMA journal_mode = WAL", 0)
    execute(null, "PRAGMA busy_timeout = 5000", 0)
    execute(null, "PRAGMA synchronous = NORMAL", 0)
}
