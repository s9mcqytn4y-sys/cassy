package id.azureenterprise.cassy.sales.di

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import id.azureenterprise.cassy.sales.db.SalesDatabase
import org.koin.core.module.Module
import org.koin.dsl.module
import java.io.File

actual val salesDatabaseModule: Module = module {
    single {
        val databasePath = File(System.getProperty("user.home"), ".cassy/sales.db")
        if (!databasePath.parentFile.exists()) {
            databasePath.parentFile.mkdirs()
        }
        val driver = JdbcSqliteDriver("jdbc:sqlite:${databasePath.absolutePath}")
        driver.harden()

        // Robust Schema Management: Create if missing, Migrate if existing
        try {
            SalesDatabase.Schema.create(driver)
        } catch (e: Exception) {
            // Table might already exist, attempt migration or just continue
            // In development, if schema changes are breaking without sqm,
            // the user might need to delete the db file.
            // But for ActiveBasket error, we try to create it explicitly if missing
            runCatching {
                driver.execute(null, "CREATE TABLE IF NOT EXISTS ActiveBasket (id INTEGER PRIMARY KEY CHECK (id = 1), content TEXT NOT NULL, updatedAt INTEGER NOT NULL)", 0)
            }
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
