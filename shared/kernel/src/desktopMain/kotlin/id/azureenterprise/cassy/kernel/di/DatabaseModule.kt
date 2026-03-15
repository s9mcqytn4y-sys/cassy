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
        if (!databasePath.exists()) {
            KernelDatabase.Schema.create(driver)
        }
        KernelDatabase(driver)
    }
}
