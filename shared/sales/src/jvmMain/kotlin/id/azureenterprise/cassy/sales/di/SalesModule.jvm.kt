package id.azureenterprise.cassy.sales.di

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import id.azureenterprise.cassy.sales.db.SalesDatabase
import org.koin.core.module.Module
import org.koin.dsl.module
import java.io.File

actual val salesDatabaseModule: Module = module {
    single {
        val databasePath = File(System.getProperty("user.home"), ".cassy/sales.db")
        databasePath.parentFile.mkdirs()
        val driver = JdbcSqliteDriver("jdbc:sqlite:${databasePath.absolutePath}")
        try {
            SalesDatabase.Schema.create(driver)
        } catch (e: Exception) {}
        SalesDatabase(driver)
    }
}
