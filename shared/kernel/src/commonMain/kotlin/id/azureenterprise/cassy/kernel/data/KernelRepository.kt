package id.azureenterprise.cassy.kernel.data

import id.azureenterprise.cassy.kernel.db.KernelDatabase
import id.azureenterprise.cassy.kernel.domain.Shift
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

class KernelRepository(
    private val database: KernelDatabase,
    private val ioDispatcher: CoroutineContext,
    private val clock: Clock
) {
    private val queries = database.kernelDatabaseQueries

    suspend fun getActiveShift(terminalId: String): Shift? = withContext(ioDispatcher) {
        queries.getActiveShift(terminalId).executeAsOneOrNull()?.let {
            Shift(
                id = it.id,
                businessDayId = it.businessDayId,
                terminalId = it.terminalId,
                openedAt = Instant.fromEpochMilliseconds(it.openedAt),
                closedAt = it.closedAt?.let { ts -> Instant.fromEpochMilliseconds(ts) },
                openedBy = it.openedBy,
                closedBy = it.closedBy,
                status = it.status
            )
        }
    }

    suspend fun isBusinessDayOpen(): Boolean = withContext(ioDispatcher) {
        queries.getActiveBusinessDay().executeAsOneOrNull() != null
    }

    suspend fun openBusinessDay(id: String) = withContext(ioDispatcher) {
        queries.insertBusinessDay(id, clock.now().toEpochMilliseconds(), "OPEN")
    }

    suspend fun insertAudit(id: String, message: String, level: String) = withContext(ioDispatcher) {
        queries.insertAudit(id, clock.now().toEpochMilliseconds(), message, level)
    }

    suspend fun insertEvent(id: String, type: String, payload: String) = withContext(ioDispatcher) {
        queries.insertEvent(id, clock.now().toEpochMilliseconds(), type, payload, "PENDING")
    }
}
