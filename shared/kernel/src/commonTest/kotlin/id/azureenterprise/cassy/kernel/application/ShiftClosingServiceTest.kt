package id.azureenterprise.cassy.kernel.application

import id.azureenterprise.cassy.kernel.domain.BootstrapStoreRequest
import id.azureenterprise.cassy.kernel.domain.OperatorRole
import id.azureenterprise.cassy.kernel.domain.PinHasher
import id.azureenterprise.cassy.kernel.domain.ShiftCloseExecutionResult
import id.azureenterprise.cassy.kernel.domain.ShiftSalesSummary
import id.azureenterprise.cassy.kernel.domain.PendingTransactionSummary
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ShiftClosingServiceTest {

    private class LocalFakePinHasher : PinHasher() {
        override fun hash(pin: String, salt: String): String = "$salt:$pin"
    }

    @Test
    fun `close shift is blocked by pending transaction`() = runTest {
        val repository = FakeKernelRepository()
        val accessService = AccessService(repository, LocalFakePinHasher(), Clock.System)
        val businessDayService = BusinessDayService(repository, accessService)
        val shiftService = ShiftService(repository, accessService)
        accessService.bootstrapStore(BootstrapStoreRequest("Store", "T1", "Kasir", "111111", "Sup", "222222"))
        val supervisor = accessService.restoreContext().operators.first { it.role == OperatorRole.SUPERVISOR }
        accessService.login(supervisor.id, "222222")
        businessDayService.openNewDay().getOrThrow()
        shiftService.startShift(100_000.0).getOrThrow()

        val service = ShiftClosingService(
            repository,
            accessService,
            object : OperationalSalesPort {
                override suspend fun getShiftSalesSummary(shiftId: String): ShiftSalesSummary {
                    return ShiftSalesSummary(
                        completedCashSalesTotal = 250_000.0,
                        pendingTransactions = listOf(PendingTransactionSummary("sale-1", "INV-1", 25_000.0))
                    )
                }
            }
        )

        val result = service.closeShift(actualCash = 340_000.0)

        val blocked = assertIs<ShiftCloseExecutionResult.Blocked>(result)
        assertTrue(blocked.decision.message.contains("pending", ignoreCase = true))
    }

    @Test
    fun `close shift with over tolerance variance requires approval and can be approved`() = runTest {
        val repository = FakeKernelRepository()
        val accessService = AccessService(repository, LocalFakePinHasher(), Clock.System)
        val businessDayService = BusinessDayService(repository, accessService)
        val shiftService = ShiftService(repository, accessService)
        accessService.bootstrapStore(BootstrapStoreRequest("Store", "T1", "Kasir", "111111", "Sup", "222222"))
        val supervisor = accessService.restoreContext().operators.first { it.role == OperatorRole.SUPERVISOR }
        accessService.login(supervisor.id, "222222")
        businessDayService.openNewDay().getOrThrow()
        shiftService.startShift(100_000.0).getOrThrow()

        val service = ShiftClosingService(
            repository,
            accessService,
            object : OperationalSalesPort {
                override suspend fun getShiftSalesSummary(shiftId: String): ShiftSalesSummary {
                    return ShiftSalesSummary(completedCashSalesTotal = 200_000.0)
                }
            }
        )

        accessService.logout()
        val cashier = accessService.restoreContext().operators.first { it.role == OperatorRole.CASHIER }
        accessService.login(cashier.id, "111111")
        val pending = service.closeShift(
            actualCash = 500_000.0,
            reasonCode = "UNRECORDED_DRAWER_ACTIVITY",
            reasonDetail = "Kas belum match"
        )

        val approvalRequired = assertIs<ShiftCloseExecutionResult.ApprovalRequired>(pending)
        accessService.logout()
        accessService.login(supervisor.id, "222222")
        val approved = service.approveCloseShift(approvalRequired.approvalRequestId)

        val closed = assertIs<ShiftCloseExecutionResult.Closed>(approved)
        assertTrue(closed.approvalApplied)
        assertEquals("CLOSED", closed.shift.status)
    }
}
