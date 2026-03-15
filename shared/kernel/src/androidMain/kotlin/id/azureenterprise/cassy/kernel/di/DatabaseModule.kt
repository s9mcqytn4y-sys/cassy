package id.azureenterprise.cassy.kernel.di

import id.azureenterprise.cassy.kernel.db.KernelDatabase
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import org.koin.dsl.module
import org.koin.core.module.Module

actual val databaseModule: Module = module {
    single {
        val driver = AndroidSqliteDriver(KernelDatabase.Schema, get(), "kernel.db")
        driver.harden()
        KernelDatabase(driver)
    }
}

private fun AndroidSqliteDriver.harden() {
    execute(null, "PRAGMA foreign_keys = ON", 0)
    execute(null, "PRAGMA journal_mode = WAL", 0)
    execute(null, "PRAGMA busy_timeout = 5000", 0)
    execute(null, "PRAGMA synchronous = NORMAL", 0)
}
