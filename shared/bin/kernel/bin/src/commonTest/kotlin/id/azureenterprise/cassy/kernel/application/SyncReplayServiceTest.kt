package id.azureenterprise.cassy.kernel.application

import id.azureenterprise.cassy.kernel.data.OutboxRepository
import id.azureenterprise.cassy.kernel.db.OutboxEvent
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class SyncReplayServiceTest {

    private val clock = Clock.System
    private val kernelRepo = FakeKernelRepository()
    private val outboxRepo = FakeOutboxRepository(clock)
    private val syncVisibilityService = SyncVisibilityService(kernelRepo, outboxRepo, clock)

    @Test
    fun `replayPending processes pending events and records success`() = runTest {
        outboxRepo.seedPending("event-1")
        outboxRepo.seedPending("event-2")
        val service = SyncReplayService(
            outboxRepository = outboxRepo,
            syncVisibilityService = syncVisibilityService,
            syncReplayPort = object : SyncReplayPort {
                override val isAvailable: Boolean = true
                override suspend fun replay(event: OutboxEvent) = SyncReplayOutcome.Acknowledged
            },
            clock = clock
        )

        val result = service.replayPending()

        result as SyncReplayResult.Completed
        assertEquals(2, result.processedCount)
        assertEquals(0, result.failedCount)
        assertEquals(0, result.pendingRemaining)
        assertEquals(0, result.failedRemaining)
        assertNotNull(kernelRepo.getMetadata(SyncVisibilityService.LAST_SYNC_SUCCESS_KEY))
        assertEquals("", kernelRepo.getMetadata(SyncVisibilityService.LAST_SYNC_ERROR_KEY))
    }

    @Test
    fun `replayPending requeues failed events before replay when requested`() = runTest {
        outboxRepo.seedFailed("event-1")
        val service = SyncReplayService(
            outboxRepository = outboxRepo,
            syncVisibilityService = syncVisibilityService,
            syncReplayPort = object : SyncReplayPort {
                override val isAvailable: Boolean = true
                override suspend fun replay(event: OutboxEvent) = SyncReplayOutcome.Acknowledged
            },
            clock = clock
        )

        val result = service.replayPending(includeFailed = true)

        result as SyncReplayResult.Completed
        assertEquals(1, result.requeuedCount)
        assertEquals(1, result.processedCount)
        assertEquals(0, result.failedRemaining)
    }

    @Test
    fun `replayPending records unconfigured backend as unavailable`() = runTest {
        outboxRepo.seedPending("event-1")
        val service = SyncReplayService(
            outboxRepository = outboxRepo,
            syncVisibilityService = syncVisibilityService,
            syncReplayPort = NoopSyncReplayPort,
            clock = clock
        )

        val result = service.replayPending()

        result as SyncReplayResult.Unavailable
        assertEquals(1, result.pendingCount)
        assertEquals("Backend sync belum dikonfigurasi", kernelRepo.getMetadata(SyncVisibilityService.LAST_SYNC_ERROR_KEY))
    }
}

private class FakeOutboxRepository(
    private val clock: Clock
) : OutboxRepository(
    database = null,
    ioDispatcher = EmptyCoroutineContext,
    clock = clock
) {
    private val events = linkedMapOf<String, OutboxEvent>()

    fun seedPending(id: String) {
        events[id] = OutboxEvent(
            id = id,
            timestamp = clock.now().toEpochMilliseconds(),
            type = "SALE_CREATED",
            payload = """{"id":"$id"}""",
            status = "PENDING"
        )
    }

    fun seedFailed(id: String) {
        events[id] = OutboxEvent(
            id = id,
            timestamp = clock.now().toEpochMilliseconds(),
            type = "SALE_CREATED",
            payload = """{"id":"$id"}""",
            status = "FAILED"
        )
    }

    override suspend fun getPendingEvents(): List<OutboxEvent> {
        return events.values.filter { it.status == "PENDING" }
    }

    override suspend fun getFailedEvents(): List<OutboxEvent> {
        return events.values.filter { it.status == "FAILED" }
    }

    override suspend fun markEventProcessed(id: String) {
        val event = events.getValue(id)
        events[id] = event.copy(status = "PROCESSED")
    }

    override suspend fun markEventFailed(id: String) {
        val event = events.getValue(id)
        events[id] = event.copy(status = "FAILED")
    }

    override suspend fun requeueFailedEvents() {
        events.replaceAll { _, event ->
            if (event.status == "FAILED") event.copy(status = "PENDING") else event
        }
    }

    override suspend fun pruneProcessedEventsBefore(cutoffEpochMs: Long) {
        val staleIds = events.values
            .filter { it.status == "PROCESSED" && it.timestamp < cutoffEpochMs }
            .map { it.id }
        staleIds.forEach(events::remove)
    }
}
