package id.azureenterprise.cassy.kernel.data

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import id.azureenterprise.cassy.kernel.db.KernelDatabase
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.Test
import kotlin.test.assertEquals

class OutboxRepositoryTest {

    @Test
    fun `getPendingEvents only returns pending events and processed events are retained`() = runBlocking {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        KernelDatabase.Schema.create(driver)
        val database = KernelDatabase(driver)
        val repository = OutboxRepository(
            database = database,
            ioDispatcher = EmptyCoroutineContext,
            clock = Clock.System
        )

        repository.recordSale("SALE-001", 10_000.0)
        repository.recordSale("SALE-002", 20_000.0)
        repository.markEventProcessed("event_SALE-001")

        val pendingEvents = repository.getPendingEvents()
        val allEvents = database.kernelDatabaseQueries.selectAllEvents().executeAsList()

        assertEquals(listOf("event_SALE-002"), pendingEvents.map { it.id })
        assertEquals("PROCESSED", allEvents.first { it.id == "event_SALE-001" }.status)
        assertEquals("PENDING", allEvents.first { it.id == "event_SALE-002" }.status)
    }
}
