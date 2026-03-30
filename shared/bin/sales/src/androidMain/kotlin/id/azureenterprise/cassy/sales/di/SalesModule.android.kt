package id.azureenterprise.cassy.sales.di

import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import id.azureenterprise.cassy.sales.application.SalesKernelPort
import id.azureenterprise.cassy.sales.db.SalesDatabase
import org.koin.core.module.Module
import org.koin.dsl.module

actual val salesDatabaseModule: Module = module {
    single {
        val driver = AndroidSqliteDriver(SalesDatabase.Schema, get(), "sales.db")
        driver.harden()
        SalesDatabase(driver)
    }
}

actual val salesPlatformModule: Module = module {
    single<SalesKernelPort> { AndroidStubSalesKernelPort() }
}

private fun AndroidSqliteDriver.harden() {
    execute(null, "PRAGMA foreign_keys = ON", 0)
    execute(null, "PRAGMA journal_mode = WAL", 0)
    execute(null, "PRAGMA busy_timeout = 5000", 0)
    execute(null, "PRAGMA synchronous = NORMAL", 0)
}

private class AndroidStubSalesKernelPort : SalesKernelPort {
    override suspend fun getOperationalContext() = null

    override suspend fun recordAudit(auditId: String, message: String) = Unit

    override suspend fun recordEvent(eventId: String, type: String, payload: String) = Unit
}
