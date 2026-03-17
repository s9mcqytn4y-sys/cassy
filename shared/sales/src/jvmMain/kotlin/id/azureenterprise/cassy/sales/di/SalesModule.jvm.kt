package id.azureenterprise.cassy.sales.di

import id.azureenterprise.cassy.kernel.data.KernelRepository
import id.azureenterprise.cassy.sales.application.SalesKernelPort
import id.azureenterprise.cassy.sales.application.SalesOperationalContext
import id.azureenterprise.cassy.sales.db.SalesDatabase
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import org.koin.core.module.Module
import org.koin.dsl.module
import java.io.File

actual val salesDatabaseModule: Module = module {
    single {
        val databasePath = File(System.getProperty("user.home"), ".cassy/sales.db")
        databasePath.parentFile.mkdirs()
        val driver = JdbcSqliteDriver("jdbc:sqlite:${databasePath.absolutePath}?foreign_keys=on")
        driver.openOrMigrateSalesDatabase()
        SalesDatabase(driver)
    }
}

actual val salesPlatformModule: Module = module {
    single<SalesKernelPort> { KernelSalesKernelPort(get()) }
}

internal fun JdbcSqliteDriver.openOrMigrateSalesDatabase() {
    harden()
    val persistedVersion = readUserVersion()
    val currentVersion = when {
        persistedVersion != 0L -> persistedVersion
        hasTable("Sale") -> detectLegacySchemaVersion()
        else -> 0L
    }
    when {
        currentVersion == 0L -> {
            SalesDatabase.Schema.create(this)
            execute(null, "PRAGMA user_version = ${SalesDatabase.Schema.version}", 0)
        }
        currentVersion < SalesDatabase.Schema.version -> {
            execute(null, "PRAGMA foreign_keys = OFF", 0)
            try {
                SalesDatabase.Schema.migrate(
                    this,
                    currentVersion,
                    SalesDatabase.Schema.version
                )
            } finally {
                execute(null, "PRAGMA foreign_keys = ON", 0)
            }
        }
    }
    execute(null, "PRAGMA user_version = ${SalesDatabase.Schema.version}", 0)
}

internal fun JdbcSqliteDriver.readUserVersion(): Long {
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

internal fun JdbcSqliteDriver.hasTable(tableName: String): Boolean {
    return executeQuery(
        null,
        "SELECT 1 FROM sqlite_master WHERE type = 'table' AND name = ? LIMIT 1",
        { cursor -> QueryResult.Value(cursor.next().value) },
        1
    ) {
        bindString(0, tableName)
    }.value
}

internal fun JdbcSqliteDriver.hasColumn(tableName: String, columnName: String): Boolean {
    return executeQuery(
        null,
        "PRAGMA table_info($tableName)",
        { cursor ->
            var found = false
            while (cursor.next().value) {
                if (cursor.getString(1) == columnName) {
                    found = true
                    break
                }
            }
            QueryResult.Value(found)
        },
        0
    ).value
}

internal fun JdbcSqliteDriver.detectLegacySchemaVersion(): Long {
    if (!hasTable("SalePayment")) return 1L
    return when {
        hasColumn("ReceiptSnapshot", "snapshotVersion") -> SalesDatabase.Schema.version
        hasColumn("SalePayment", "statusReasonCode") -> 3L
        else -> 2L
    }
}

internal fun JdbcSqliteDriver.harden() {
    execute(null, "PRAGMA foreign_keys = ON", 0)
    execute(null, "PRAGMA journal_mode = WAL", 0)
    execute(null, "PRAGMA busy_timeout = 5000", 0)
    execute(null, "PRAGMA synchronous = NORMAL", 0)
}

private class KernelSalesKernelPort(
    private val kernelRepository: KernelRepository
) : SalesKernelPort {
    override suspend fun getOperationalContext(): SalesOperationalContext? {
        val binding = kernelRepository.getTerminalBinding() ?: return null
        if (!kernelRepository.isBusinessDayOpen()) return null
        val shift = kernelRepository.getActiveShift(binding.terminalId) ?: return null
        return SalesOperationalContext(
            storeName = binding.storeName,
            terminalId = binding.terminalId,
            terminalName = binding.terminalName,
            shiftId = shift.id
        )
    }

    override suspend fun recordAudit(auditId: String, message: String) {
        ignoreDuplicateConstraint {
            kernelRepository.insertAudit(
                id = auditId,
                message = message,
                level = "INFO"
            )
        }
    }

    override suspend fun recordEvent(eventId: String, type: String, payload: String) {
        ignoreDuplicateConstraint {
            kernelRepository.insertEvent(
                id = eventId,
                type = type,
                payload = payload
            )
        }
    }
}

private suspend fun ignoreDuplicateConstraint(block: suspend () -> Unit) {
    runCatching { block() }
        .getOrElse { error ->
            val normalized = error.message?.uppercase().orEmpty()
            if ("UNIQUE" !in normalized && "PRIMARY KEY" !in normalized) {
                throw error
            }
        }
}
