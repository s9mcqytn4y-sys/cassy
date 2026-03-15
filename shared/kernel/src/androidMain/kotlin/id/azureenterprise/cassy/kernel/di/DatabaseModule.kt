package id.azureenterprise.cassy.kernel.di

import id.azureenterprise.cassy.kernel.db.KernelDatabase
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import org.koin.dsl.module
import org.koin.core.module.Module

actual val databaseModule: Module = module {
    single {
        val driver = AndroidSqliteDriver(KernelDatabase.Schema, get(), "kernel.db")
        KernelDatabase(driver)
    }
}
