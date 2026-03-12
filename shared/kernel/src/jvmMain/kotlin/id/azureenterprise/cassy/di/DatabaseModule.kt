package id.azureenterprise.cassy.di

import id.azureenterprise.cassy.db.CassyDatabase
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import org.koin.dsl.module
import java.io.File

actual val databaseModule = module {
    single {
        val databasePath = File(System.getProperty("user.home"), ".cassy/cassy.db")
        if (!databasePath.parentFile.exists()) {
            databasePath.parentFile.mkdirs()
        }
        val driver = JdbcSqliteDriver("jdbc:sqlite:${databasePath.absolutePath}")
        try {
            CassyDatabase.Schema.create(driver)
        } catch (e: Exception) {
            // Database already exists
        }
        CassyDatabase(driver)
    }
}
