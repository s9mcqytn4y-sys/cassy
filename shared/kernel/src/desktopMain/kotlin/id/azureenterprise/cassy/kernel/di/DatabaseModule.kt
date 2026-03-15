package id.azureenterprise.cassy.kernel.di

import id.azureenterprise.cassy.kernel.db.KernelDatabase
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import org.koin.dsl.module
import org.koin.core.module.Module
import java.io.File

actual val databaseModule: Module = module {
    single {
        val databasePath = File(System.getProperty("user.home"), ".cassy/kernel.db")
        if (!databasePath.parentFile.exists()) {
            databasePath.parentFile.mkdirs()
        }
        val driver = JdbcSqliteDriver("jdbc:sqlite:${databasePath.absolutePath}")
        driver.harden()
        if (!databasePath.exists()) {
            KernelDatabase.Schema.create(driver)
        }
        KernelDatabase(driver)
    }
}

private fun JdbcSqliteDriver.harden() {
    execute(null, "PRAGMA foreign_keys = ON", 0)
    execute(null, "PRAGMA journal_mode = WAL", 0)
    execute(null, "PRAGMA busy_timeout = 5000", 0)
    execute(null, "PRAGMA synchronous = NORMAL", 0)
}
