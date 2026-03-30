package id.azureenterprise.cassy.kernel.di

import id.azureenterprise.cassy.kernel.db.KernelDatabase
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import org.koin.dsl.module
import org.koin.core.module.Module
import java.io.File

actual val databaseModule: Module = module {
    single {
        val databasePath = File(resolveDesktopDataRoot(), "kernel.db")
        val databaseAlreadyExists = databasePath.exists()
        if (!databasePath.parentFile.exists()) {
            databasePath.parentFile.mkdirs()
        }
        val driver = JdbcSqliteDriver("jdbc:sqlite:${databasePath.absolutePath}")
        driver.openOrMigrateKernelDatabase(databaseAlreadyExists)
        KernelDatabase(driver)
    }
}

private fun JdbcSqliteDriver.openOrMigrateKernelDatabase(databaseAlreadyExists: Boolean) {
    harden()
    val persistedVersion = readUserVersion()
    val currentVersion = when {
        persistedVersion != 0L -> persistedVersion
        databaseAlreadyExists && hasTable("BusinessDay") -> 1L
        else -> 0L
    }
    when {
        currentVersion == 0L -> {
            KernelDatabase.Schema.create(this)
            execute(null, "PRAGMA user_version = ${KernelDatabase.Schema.version}", 0)
        }
        currentVersion < KernelDatabase.Schema.version -> {
            execute(null, "PRAGMA foreign_keys = OFF", 0)
            try {
                KernelDatabase.Schema.migrate(this, currentVersion, KernelDatabase.Schema.version)
            } finally {
                execute(null, "PRAGMA foreign_keys = ON", 0)
            }
        }
    }
    execute(null, "PRAGMA user_version = ${KernelDatabase.Schema.version}", 0)
}

private fun JdbcSqliteDriver.readUserVersion(): Long {
    return executeQuery(
        null,
        "PRAGMA user_version",
        { cursor ->
            check(cursor.next().value)
            QueryResult.Value(cursor.getLong(0) ?: 0L)
        },
        0
    ).value
}

private fun JdbcSqliteDriver.hasTable(tableName: String): Boolean {
    return executeQuery(
        null,
        "SELECT 1 FROM sqlite_master WHERE type = 'table' AND name = ? LIMIT 1",
        { cursor -> QueryResult.Value(cursor.next().value) },
        1
    ) {
        bindString(0, tableName)
    }.value
}

private fun JdbcSqliteDriver.harden() {
    execute(null, "PRAGMA foreign_keys = ON", 0)
    execute(null, "PRAGMA journal_mode = WAL", 0)
    execute(null, "PRAGMA busy_timeout = 5000", 0)
    execute(null, "PRAGMA synchronous = NORMAL", 0)
}

private fun resolveDesktopDataRoot(): File {
    val explicit = System.getProperty("cassy.data.dir")
        ?: System.getenv("CASSY_DATA_DIR")
    return explicit?.let(::File) ?: File(System.getProperty("user.home"), ".cassy")
}
