package id.azureenterprise.cassy.kernel.application

import id.azureenterprise.cassy.kernel.domain.*
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertNotNull

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
    }

    @Test
    fun `test start shift fails with negative cash`() = runTest {
        setupSupervisorAndDay()

        val result = service.startShift(-10.0)
        assertTrue(result.isFailure)
        assertEquals("Opening cash tidak boleh negatif", result.exceptionOrNull()?.message)
    }

    @Test
    fun `test end shift success`() = runTest {
        setupSupervisorAndDay()
        val shift = service.startShift(100000.0).getOrThrow()

        val result = service.endShift(150000.0)
        assertTrue(result.isSuccess)
        assertEquals(150000.0, result.getOrThrow().closingCash)
    }
}
