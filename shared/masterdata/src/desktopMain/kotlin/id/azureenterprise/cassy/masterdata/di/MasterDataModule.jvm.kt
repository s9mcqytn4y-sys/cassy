package id.azureenterprise.cassy.masterdata.di

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import id.azureenterprise.cassy.masterdata.db.MasterDataDatabase
import org.koin.core.module.Module
import org.koin.dsl.module
import java.io.File

actual val masterDataDatabaseModule: Module = module {
    single {
        val databasePath = File(System.getProperty("user.home"), ".cassy/masterdata.db")
        val databaseAlreadyExists = databasePath.exists()
        databasePath.parentFile.mkdirs()
        val driver = JdbcSqliteDriver("jdbc:sqlite:${databasePath.absolutePath}")
        driver.harden()
        if (!databaseAlreadyExists) {
            MasterDataDatabase.Schema.create(driver)
        }
        MasterDataDatabase(driver)
    }
}

private fun JdbcSqliteDriver.harden() {
    execute(null, "PRAGMA foreign_keys = ON", 0)
    execute(null, "PRAGMA journal_mode = WAL", 0)
    execute(null, "PRAGMA busy_timeout = 5000", 0)
    execute(null, "PRAGMA synchronous = NORMAL", 0)
}
