package id.azureenterprise.cassy.sales.di

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import id.azureenterprise.cassy.sales.db.SalesDatabase
import org.koin.core.module.Module
import org.koin.dsl.module
import java.io.File

actual val salesDatabaseModule: Module = module {
    single {
        val databasePath = File(System.getProperty("user.home"), ".cassy/sales.db")
        val databaseAlreadyExists = databasePath.exists()
        databasePath.parentFile.mkdirs()
        val driver = JdbcSqliteDriver("jdbc:sqlite:${databasePath.absolutePath}")
        driver.harden()
        if (!databaseAlreadyExists) {
            SalesDatabase.Schema.create(driver)
        }
        SalesDatabase(driver)
    }
}

private fun JdbcSqliteDriver.harden() {
    execute(null, "PRAGMA foreign_keys = ON", 0)
    execute(null, "PRAGMA journal_mode = WAL", 0)
    execute(null, "PRAGMA busy_timeout = 5000", 0)
    execute(null, "PRAGMA synchronous = NORMAL", 0)
}
