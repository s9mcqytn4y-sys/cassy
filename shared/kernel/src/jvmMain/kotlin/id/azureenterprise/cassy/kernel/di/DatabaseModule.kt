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
        try {
            KernelDatabase.Schema.create(driver)
        } catch (e: Exception) {
            // Database already exists
        }
        KernelDatabase(driver)
    }
}
// This is an actual declaration for JVM (Desktop)
