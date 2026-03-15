package id.azureenterprise.cassy.masterdata.di

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import id.azureenterprise.cassy.masterdata.db.MasterDataDatabase
import org.koin.core.module.Module
import org.koin.dsl.module
import java.io.File

actual val masterDataDatabaseModule: Module = module {
    single {
        val databasePath = File(System.getProperty("user.home"), ".cassy/masterdata.db")
        databasePath.parentFile.mkdirs()
        val driver = JdbcSqliteDriver("jdbc:sqlite:${databasePath.absolutePath}")
        try {
            MasterDataDatabase.Schema.create(driver)
        } catch (e: Exception) {}
        MasterDataDatabase(driver)
    }
}
