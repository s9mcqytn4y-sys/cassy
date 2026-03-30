package id.azureenterprise.cassy.kernel.application

import id.azureenterprise.cassy.kernel.domain.ApprovalStatus
import id.azureenterprise.cassy.kernel.domain.BootstrapStoreRequest
import id.azureenterprise.cassy.kernel.domain.CashMovementExecutionResult
import id.azureenterprise.cassy.kernel.domain.CashMovementType
import id.azureenterprise.cassy.kernel.domain.OperatorRole
import id.azureenterprise.cassy.kernel.domain.PinHasher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class CashControlServiceTest {

    private class LocalFakePinHasher : PinHasher() {
        override fun hash(pin: String, salt: String): String = "$salt:$pin"
    }

    private fun fixture(): CashControlFixture {
        val repository = FakeKernelRepository()
        val accessService = AccessService(repository, LocalFakePinHasher(), Clock.System)
        val businessDayService = BusinessDayService(repository, accessService)
        val shiftService = ShiftService(repository, accessService)
        val service = CashControlService(repository, accessService)
        return CashControlFixture(repository, accessService, businessDayService, shiftService, service)
    }

    @Test
    fun `cash movement under threshold is recorded with valid reason code`() = runTest {
        val fixture = fixture()
        fixture.bootstrapAndOpenShift(OperatorRole.CASHIER)

        val result = fixture.service.submitMovement(
            type = CashMovementType.CASH_OUT,
            amount = 100_000.0,
            reasonCode = "PETTY_CASH",
            reasonDetail = "Beli galon"
        )

        val recorded = assertIs<CashMovementExecutionResult.Recorded>(result)
        assertEquals(CashMovementType.CASH_OUT, recorded.movement.type)
        assertTrue(fixture.repository.events.any { it.contains("CASH_MOVEMENT_RECORDED") })
    }

    @Test
    fun `cash movement above threshold creates approval request for cashier and can be denied`() = runTest {
        val fixture = fixture()
        fixture.bootstrapAndOpenShift(OperatorRole.CASHIER)

        val result = fixture.service.submitMovement(
            type = CashMovementType.SAFE_DROP,
            amount = 1_500_000.0,
            reasonCode = "SAFE_DROP_OVERFLOW",
            reasonDetail = "Laci penuh"
        )

        val pending = assertIs<CashMovementExecutionResult.ApprovalRequired>(result)
        assertEquals(ApprovalStatus.REQUESTED, fixture.repository.getApprovalRequestById(pending.approvalRequestId)?.status)

        fixture.reloginAs(OperatorRole.SUPERVISOR)
        val denied = fixture.service.denyCashMovement(pending.approvalRequestId, "Belum waktunya safe drop")

        assertEquals(ApprovalStatus.DENIED, denied?.status)
        val terminalId = requireNotNull(fixture.repository.getTerminalBinding()).terminalId
        val activeShift = requireNotNull(fixture.repository.getActiveShift(terminalId))
        assertTrue(fixture.repository.listCashMovementsByShift(activeShift.id).isEmpty())
    }

    @Test
    fun `cash movement above threshold can be approved by supervisor`() = runTest {
        val fixture = fixture()
        fixture.bootstrapAndOpenShift(OperatorRole.CASHIER)

        val result = fixture.service.submitMovement(
            type = CashMovementType.CASH_IN,
            amount = 700_000.0,
            reasonCode = "BANK_WITHDRAWAL",
            reasonDetail = "Tambah float receh"
        )

        val pending = assertIs<CashMovementExecutionResult.ApprovalRequired>(result)
        fixture.reloginAs(OperatorRole.SUPERVISOR)
        val approved = fixture.service.approveCashMovement(pending.approvalRequestId)

        val recorded = assertIs<CashMovementExecutionResult.Recorded>(approved)
        assertTrue(recorded.approvalApplied)
        assertEquals(ApprovalStatus.APPROVED, fixture.repository.getApprovalRequestById(pending.approvalRequestId)?.status)
    }
}

private data class CashControlFixture(
    val repository: FakeKernelRepository,
    val accessService: AccessService,
    val businessDayService: BusinessDayService,
    val shiftService: ShiftService,
    val service: CashControlService
) {
    suspend fun bootstrapAndOpenShift(role: OperatorRole) {
        accessService.bootstrapStore(
            BootstrapStoreRequest("Store", "T1", "Kasir", "111111", "Sup", "222222")
        )
        reloginAs(OperatorRole.SUPERVISOR)
        businessDayService.openNewDay().getOrThrow()
        shiftService.startShift(100_000.0).getOrThrow()
        if (role == OperatorRole.CASHIER) {
            accessService.logout()
            reloginAs(OperatorRole.CASHIER)
        }
    }

    suspend fun reloginAs(role: OperatorRole) {
        accessService.logout()
        val operator = accessService.restoreContext().operators.first { it.role == role }
        accessService.login(operator.id, if (role == OperatorRole.CASHIER) "111111" else "222222")
    }
}
