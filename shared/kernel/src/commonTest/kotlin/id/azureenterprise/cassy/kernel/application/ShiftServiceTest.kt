package id.azureenterprise.cassy.kernel.application

import id.azureenterprise.cassy.kernel.domain.*
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ShiftServiceTest {

    private class LocalFakePinHasher : PinHasher() {
        override fun hash(pin: String, salt: String): String = "$salt:$pin"
    }

    private val fakeRepo = FakeKernelRepository()
    private val clock = Clock.System
    private val accessService = AccessService(fakeRepo, LocalFakePinHasher(), clock)
    private val businessDayService = BusinessDayService(fakeRepo, accessService)
    private val service = ShiftService(fakeRepo, accessService)

    private suspend fun setupSupervisorAndDay() {
        accessService.bootstrapStore(
            BootstrapStoreRequest("S1", "T1", "C1", "111111", "Sup", "222222")
        )
        val supervisor = accessService.restoreContext().operators.find { it.role == OperatorRole.SUPERVISOR }!!
        accessService.login(supervisor.id, "222222")
        businessDayService.openNewDay()
    }

    @Test
    fun `test start shift success`() = runTest {
        setupSupervisorAndDay()

        val result = service.startShift(100000.0)
        assertTrue(result.isSuccess)
        assertNotNull(service.getActiveShift())
        assertTrue(fakeRepo.events.any { it.contains("SHIFT_OPENED") })
    }

    @Test
    fun `test start shift fails with negative cash`() = runTest {
        setupSupervisorAndDay()

        val result = service.startShift(-10.0)
        assertTrue(result.isFailure)
        assertEquals("Opening cash tidak boleh negatif.", result.exceptionOrNull()?.message)
    }

    @Test
    fun `test end shift success`() = runTest {
        setupSupervisorAndDay()
        service.startShift(100000.0).getOrThrow()

        val result = service.endShift(150000.0)
        assertTrue(result.isSuccess)
        assertEquals(150000.0, result.getOrThrow().closingCash)
        assertTrue(fakeRepo.events.any { it.contains("SHIFT_CLOSED") })
    }

    @Test
    fun `cashier opening cash di luar kebijakan requires approval`() = runTest {
        accessService.bootstrapStore(
            BootstrapStoreRequest("S1", "T1", "Kasir", "111111", "Sup", "222222")
        )
        val supervisor = accessService.restoreContext().operators.find { it.role == OperatorRole.SUPERVISOR }!!
        accessService.login(supervisor.id, "222222")
        businessDayService.openNewDay().getOrThrow()
        accessService.logout()

        val cashier = accessService.restoreContext().operators.find { it.role == OperatorRole.CASHIER }!!
        accessService.login(cashier.id, "111111")

        val result = service.submitStartShift(
            openingCash = 750000.0,
            approvalReason = "Butuh pecahan untuk pembukaan"
        )

        val approval = assertIs<StartShiftExecutionResult.ApprovalRequired>(result)
        assertTrue(approval.decision.message.contains("supervisor", ignoreCase = true))
        assertTrue(service.getActiveShift() == null)
    }

    @Test
    fun `supervisor can approve out of policy opening cash with reason`() = runTest {
        setupSupervisorAndDay()

        val result = service.submitStartShift(
            openingCash = 750000.0,
            approvalReason = "Hari ramai, butuh pecahan tambahan"
        )

        val started = assertIs<StartShiftExecutionResult.Started>(result)
        assertTrue(started.approvalApplied)
        assertEquals(750000.0, started.shift.openingCash)
        assertTrue(fakeRepo.audits.any { it.contains("approval karena di luar kebijakan") })
    }

    @Test
    fun `duplicate active shift is prevented explicitly`() = runTest {
        setupSupervisorAndDay()
        service.startShift(100000.0).getOrThrow()

        val result = service.submitStartShift(120000.0)

        val blocked = assertIs<StartShiftExecutionResult.Blocked>(result)
        assertTrue(blocked.decision.message.contains("Shift aktif sudah ada"))
    }
}
