package id.azureenterprise.cassy.kernel.application

import id.azureenterprise.cassy.kernel.data.OutboxRepository
import id.azureenterprise.cassy.kernel.domain.*
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ReportingQueryFacadeTest {

    private val clock = Clock.System
    private val kernelRepo = FakeKernelRepository()
    private val pendingEvents = mutableListOf<id.azureenterprise.cassy.kernel.db.OutboxEvent>()

    // Mock OutboxRepository to avoid DB driver issues in commonTest
    private val outboxRepo = object : id.azureenterprise.cassy.kernel.data.OutboxRepository(
        database = null,
        ioDispatcher = EmptyCoroutineContext,
        clock = clock
    ) {
        override suspend fun getPendingEvents(): List<id.azureenterprise.cassy.kernel.db.OutboxEvent> = pendingEvents.toList()
    }

    private val salesPort = object : OperationalSalesPort {
        override suspend fun getShiftSalesSummary(shiftId: String): ShiftSalesSummary {
            return ShiftSalesSummary(
                completedCashSalesTotal = 100_000.0,
                completedNonCashSalesTotal = 50_000.0,
                completedSaleCount = 5,
                pendingTransactions = emptyList()
            )
        }

        override suspend fun getMultiShiftSalesSummary(shiftIds: List<String>): ShiftSalesSummary {
            return ShiftSalesSummary(
                completedCashSalesTotal = shiftIds.size * 100_000.0,
                completedNonCashSalesTotal = shiftIds.size * 50_000.0,
                completedSaleCount = shiftIds.size * 5,
                pendingTransactions = emptyList()
            )
        }
    }

    private val hardwarePort = NoopOperationalHardwarePort

    private val facade = ReportingQueryFacade(
        kernelRepo,
        outboxRepo,
        salesPort,
        hardwarePort,
        clock,
        TimeZone.UTC
    )

    @Test
    fun `getDailySummary should aggregate data and detect issues truthfully`() = runTest {
        val dayId = "DAY-001"
        kernelRepo.openBusinessDay(dayId)
        kernelRepo.openShift("SHIFT-001", dayId, "TERM-1", 50_000.0, "Op 1")
        kernelRepo.openShift("SHIFT-002", dayId, "TERM-2", 50_000.0, "Op 2")

        kernelRepo.insertCashMovement("M-1", dayId, "SHIFT-001", "TERM-1", CashMovementType.CASH_IN, 20_000.0, "IN", null, null, "Op 1")
        kernelRepo.insertCashMovement("M-2", dayId, "SHIFT-001", "TERM-1", CashMovementType.CASH_OUT, 10_000.0, "OUT", null, null, "Op 1")

        val summary = facade.getDailySummary(dayId)

        assertNotNull(summary)
        assertEquals(dayId, summary.businessDayId)
        assertEquals(2, summary.shiftCount)
        assertEquals(2, summary.openShiftCount)
        assertEquals(300_000.0, summary.totalSales) // (100k + 50k) * 2 shifts
        assertEquals(10, summary.transactionCount)
        assertEquals(10_000.0, summary.netCashMovement) // 20k - 10k

        // Check issues
        assertTrue(summary.hasOperationalIssues)
        assertTrue(summary.issues.any { it.type == OperationalIssueType.OPEN_WORK_UNIT && it.severity == IssueSeverity.CRITICAL })
    }

    @Test
    fun `getShiftSummary should calculate expected cash and detect issues correctly`() = runTest {
        val dayId = "DAY-001"
        val shiftId = "SHIFT-001"
        kernelRepo.openBusinessDay(dayId)
        kernelRepo.openShift(shiftId, dayId, "TERM-1", 50_000.0, "Op 1")

        kernelRepo.insertCashMovement("M-1", dayId, shiftId, "TERM-1", CashMovementType.CASH_IN, 20_000.0, "IN", null, null, "Op 1")

        val summary = facade.getShiftSummary(shiftId)

        assertNotNull(summary)
        assertEquals(50_000.0, summary.openingCash)
        assertEquals(150_000.0, summary.salesTotal)
        assertEquals(100_000.0, summary.cashSalesTotal)
        assertEquals(20_000.0, summary.cashInTotal)

        // expected = 50k (open) + 100k (cash sales) + 20k (cash in) = 170k
        assertEquals(170_000.0, summary.expectedCash)
        assertEquals(true, summary.hasUnresolvedIssues) // because status is OPEN
        assertTrue(summary.issues.any { it.type == OperationalIssueType.OPEN_WORK_UNIT && it.severity == IssueSeverity.INFO })
    }

    @Test
    fun `getLatestShiftSummary should prefer newest shift in business day`() = runTest {
        val dayId = "DAY-LATEST"
        kernelRepo.openBusinessDay(dayId)
        kernelRepo.openShift("SHIFT-OLD", dayId, "TERM-1", 50_000.0, "Op 1")
        kernelRepo.openShift("SHIFT-NEW", dayId, "TERM-1", 75_000.0, "Op 2")

        val summary = facade.getLatestShiftSummary(dayId)

        assertNotNull(summary)
        assertEquals("SHIFT-NEW", summary.shiftId)
        assertEquals("Op 2", summary.operatorName)
    }

    @Test
    fun `getSyncStatus should expose explicit sync error metadata`() = runTest {
        pendingEvents.clear()
        pendingEvents += id.azureenterprise.cassy.kernel.db.OutboxEvent(
            id = "event-1",
            timestamp = clock.now().toEpochMilliseconds(),
            type = "SALE_CREATED",
            payload = """{"saleId":"SALE-1"}""",
            status = "PENDING"
        )
        kernelRepo.upsertMetadata("sync.last_error_message", "HQ unreachable")

        val syncStatus = facade.getSyncStatus()

        assertEquals(SyncLevel.ERROR, syncStatus.level)
        assertEquals("HQ unreachable", syncStatus.message)
        assertEquals("HQ unreachable", syncStatus.lastErrorMessage)
        assertEquals(1, syncStatus.pendingCount)
        assertEquals(0, syncStatus.failedCount)
    }
}
