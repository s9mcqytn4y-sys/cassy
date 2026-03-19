package id.azureenterprise.cassy.kernel.application

import id.azureenterprise.cassy.kernel.domain.BootstrapStoreRequest
import id.azureenterprise.cassy.kernel.domain.OperationStatus
import id.azureenterprise.cassy.kernel.domain.OperationType
import id.azureenterprise.cassy.kernel.domain.OperatorRole
import id.azureenterprise.cassy.kernel.domain.PinHasher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class OperationalControlServiceTest {

    private class LocalFakePinHasher : PinHasher() {
        override fun hash(pin: String, salt: String): String = "$salt:$pin"
    }

    private fun fixture(): Fixture {
        val repository = FakeKernelRepository()
        val accessService = AccessService(repository, LocalFakePinHasher(), Clock.System)
        val businessDayService = BusinessDayService(repository, accessService)
        val shiftService = ShiftService(repository, accessService)
        val cashControlService = CashControlService(repository, accessService)
        val shiftClosingService = ShiftClosingService(repository, accessService, NoopOperationalSalesPort)
        return Fixture(
            accessService = accessService,
            businessDayService = businessDayService,
            shiftService = shiftService,
            cashControlService = cashControlService,
            shiftClosingService = shiftClosingService,
            service = OperationalControlService(
                accessService,
                businessDayService,
                shiftService,
                cashControlService,
                shiftClosingService
            )
        )
    }

    @Test
    fun `dashboard blocks sales home and points to open day first`() = runTest {
        val fixture = fixture()
        fixture.bootstrapAndLogin(OperatorRole.CASHIER)

        val snapshot = fixture.service.buildSnapshot(openingCashInput = "", openingCashReason = "")

        assertFalse(snapshot.canAccessSalesHome)
        assertEquals(OperationType.OPEN_BUSINESS_DAY, snapshot.primaryAction)
        assertTrue(snapshot.salesHomeBlocker?.contains("Business day harus aktif") == true)
        assertEquals(
            OperationStatus.BLOCKED,
            snapshot.decisions.first { it.type == OperationType.OPEN_BUSINESS_DAY }.status
        )
    }

    @Test
    fun `dashboard shows approval requirement for cashier opening cash di luar kebijakan`() = runTest {
        val fixture = fixture()
        fixture.bootstrapAndLogin(OperatorRole.SUPERVISOR)
        fixture.businessDayService.openNewDay().getOrThrow()
        fixture.accessService.logout()
        fixture.login(OperatorRole.CASHIER)

        val snapshot = fixture.service.buildSnapshot(
            openingCashInput = "750000",
            openingCashReason = "Butuh modal ekstra untuk pecahan"
        )

        val decision = snapshot.decisions.first { it.type == OperationType.START_SHIFT }
        assertEquals(OperationStatus.REQUIRES_APPROVAL, decision.status)
        assertEquals(OperationType.START_SHIFT, snapshot.primaryAction)
    }
}

private data class Fixture(
    val accessService: AccessService,
    val businessDayService: BusinessDayService,
    val shiftService: ShiftService,
    val cashControlService: CashControlService,
    val shiftClosingService: ShiftClosingService,
    val service: OperationalControlService
) {
    suspend fun bootstrapAndLogin(role: OperatorRole) {
        accessService.bootstrapStore(
            BootstrapStoreRequest("Toko R2", "Kasir-01", "Kasir", "111111", "Supervisor", "222222")
        )
        login(role)
    }

    suspend fun login(role: OperatorRole) {
        val operator = accessService.restoreContext().operators.first { it.role == role }
        accessService.login(operator.id, if (role == OperatorRole.CASHIER) "111111" else "222222")
    }
}
