package id.azureenterprise.cassy.kernel.application

import id.azureenterprise.cassy.kernel.domain.*
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertNotNull

class BusinessDayServiceTest {

    private class LocalFakePinHasher : PinHasher() {
        override fun hash(pin: String, salt: String): String = "$salt:$pin"
    }

    private val fakeRepo = FakeKernelRepository()
    private val clock = Clock.System
    private val accessService = AccessService(fakeRepo, LocalFakePinHasher(), clock)
    private val service = BusinessDayService(fakeRepo, accessService)

    private suspend fun setupSupervisor() {
        accessService.bootstrapStore(
            BootstrapStoreRequest("S1", "T1", "C1", "111111", "Sup", "222222")
        )
        val supervisor = accessService.restoreContext().operators.find { it.role == OperatorRole.SUPERVISOR }!!
        accessService.login(supervisor.id, "222222")
    }

    @Test
    fun `test open business day success`() = runTest {
        setupSupervisor()

        val result = service.openNewDay()
        assertTrue(result.isSuccess)
        assertTrue(service.isOpen())
        assertNotNull(service.getActiveBusinessDay())
        assertTrue(fakeRepo.audits.any { it.contains("dibuka oleh") })
        assertTrue(fakeRepo.events.any { it.contains("BUSINESS_DAY_OPENED") })
    }

    @Test
    fun `test open business day fails if already open`() = runTest {
        setupSupervisor()
        service.openNewDay()

        val result = service.openNewDay()
        assertTrue(result.isFailure)
        assertEquals("Business day sudah terbuka", result.exceptionOrNull()?.message)
    }

    @Test
    fun `test close business day success`() = runTest {
        setupSupervisor()
        service.openNewDay()

        val result = service.closeCurrentDay()
        assertTrue(result.isSuccess)
        assertFalse(service.isOpen())
        assertTrue(fakeRepo.events.any { it.contains("BUSINESS_DAY_CLOSED") })
    }

    @Test
    fun `evaluate open day blocks cashier and allows supervisor`() = runTest {
        accessService.bootstrapStore(
            BootstrapStoreRequest("S1", "T1", "Kasir", "111111", "Sup", "222222")
        )
        val cashier = accessService.restoreContext().operators.find { it.role == OperatorRole.CASHIER }!!
        accessService.login(cashier.id, "111111")

        val cashierDecision = service.evaluateOpenDay()

        assertTrue(cashierDecision.message.contains("Supervisor"))

        accessService.logout()
        val supervisor = accessService.restoreContext().operators.find { it.role == OperatorRole.SUPERVISOR }!!
        accessService.login(supervisor.id, "222222")
        val supervisorDecision = service.evaluateOpenDay()

        assertEquals("Buka Business Day", supervisorDecision.title)
        assertEquals("Buka Business Day", supervisorDecision.actionLabel)
    }
}
