package id.azureenterprise.cassy.kernel.application

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import id.azureenterprise.cassy.kernel.data.KernelRepository
import id.azureenterprise.cassy.kernel.db.KernelDatabase
import id.azureenterprise.cassy.kernel.domain.AccessCapability
import id.azureenterprise.cassy.kernel.domain.BootstrapStoreRequest
import id.azureenterprise.cassy.kernel.domain.LoginResult
import id.azureenterprise.cassy.kernel.domain.PinHasher
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AccessAndOperationsServiceTest {

    @Test
    fun `login success, wrong pin, locked, and restore binding are persisted honestly`() {
        runBlocking {
            val fixture = kernelFixture()

            val bootstrap = fixture.accessService.bootstrapStore(
                BootstrapStoreRequest(
                    storeName = "Toko Pilot",
                    terminalName = "Kasir-01",
                    cashierName = "Sinta",
                    cashierPin = "123456",
                    supervisorName = "Budi",
                    supervisorPin = "654321"
                )
            )
            assertTrue(bootstrap.isSuccess)

            val context = fixture.accessService.restoreContext()
            assertNotNull(context.terminalBinding)
            assertEquals("Toko Pilot", context.terminalBinding.storeName)
            assertEquals(2, context.operators.size)

            val cashier = context.operators.first { it.employeeCode == "cashier" }
            val wrongPin = fixture.accessService.login(cashier.id, "000000")
            assertIs<LoginResult.WrongPin>(wrongPin)
            assertEquals(1, wrongPin.failedAttempts)

            fixture.accessService.login(cashier.id, "000000")
            val locked = fixture.accessService.login(cashier.id, "000000")
            assertIs<LoginResult.Locked>(locked)

            val supervisor = context.operators.first { it.employeeCode == "supervisor" }
            val success = fixture.accessService.login(supervisor.id, "654321")
            assertIs<LoginResult.Success>(success)
            assertEquals("supervisor", success.operator.employeeCode)

            val restored = fixture.accessService.restoreContext()
            assertEquals(context.terminalBinding, restored.terminalBinding)
            assertNotNull(restored.activeSession)
            assertEquals(supervisor.id, restored.activeSession.operatorId)
        }
    }

    @Test
    fun `cashier is forbidden to open day but supervisor can complete day-shift flow`() {
        runBlocking {
            val fixture = kernelFixture()
            fixture.accessService.bootstrapStore(
                BootstrapStoreRequest(
                    storeName = "Toko Shift",
                    terminalName = "Kasir-02",
                    cashierName = "Dina",
                    cashierPin = "123456",
                    supervisorName = "Raka",
                    supervisorPin = "654321"
                )
            )

            val operators = fixture.accessService.restoreContext().operators
            val cashier = operators.first { it.employeeCode == "cashier" }
            val supervisor = operators.first { it.employeeCode == "supervisor" }

            fixture.accessService.login(cashier.id, "123456")
            val forbiddenOpenDay = fixture.businessDayService.openNewDay()
            assertTrue(forbiddenOpenDay.isFailure)
            assertTrue(forbiddenOpenDay.exceptionOrNull()?.message?.contains(AccessCapability.OPEN_DAY.name) == true)

            fixture.accessService.logout()
            fixture.accessService.login(supervisor.id, "654321")
            val openedDay = fixture.businessDayService.openNewDay().getOrThrow()
            assertEquals("OPEN", openedDay.status)

            val startedShift = fixture.shiftService.startShift(openingCash = 150.0).getOrThrow()
            assertEquals(150.0, startedShift.openingCash)

            val closeDayWhileShiftActive = fixture.businessDayService.closeCurrentDay()
            assertTrue(closeDayWhileShiftActive.isFailure)

            val closedShift = fixture.shiftService.endShift(closingCash = 175.0).getOrThrow()
            assertEquals(175.0, closedShift.closingCash)

            val closedDay = fixture.businessDayService.closeCurrentDay().getOrThrow()
            assertEquals("CLOSED", closedDay.status)
            assertNull(fixture.shiftService.getActiveShift())
        }
    }

    @Test
    fun `step up auth verifies supervisor without replacing cashier session`() {
        runBlocking {
            val fixture = kernelFixture()
            fixture.accessService.bootstrapStore(
                BootstrapStoreRequest(
                    storeName = "Toko StepUp",
                    terminalName = "Kasir-03",
                    cashierName = "Maya",
                    cashierPin = "123456",
                    supervisorName = "Ardi",
                    supervisorPin = "654321"
                )
            )

            val operators = fixture.accessService.restoreContext().operators
            val cashier = operators.first { it.employeeCode == "cashier" }
            val supervisor = operators.first { it.employeeCode == "supervisor" }

            val login = fixture.accessService.login(cashier.id, "123456")
            assertIs<LoginResult.Success>(login)

            val verified = fixture.accessService.verifyStepUp(
                operatorId = supervisor.id,
                pin = "654321",
                capability = AccessCapability.OPEN_DAY
            ).getOrThrow()

            val restored = fixture.accessService.restoreContext()
            assertEquals(supervisor.id, verified.id)
            assertNotNull(restored.activeSession)
            assertEquals(cashier.id, restored.activeSession.operatorId)
        }
    }

    private fun kernelFixture(): KernelFixture {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        KernelDatabase.Schema.create(driver)
        val repository = KernelRepository(
            database = KernelDatabase(driver),
            ioDispatcher = EmptyCoroutineContext,
            clock = Clock.System
        )
        val accessService = AccessService(repository, PinHasher(), Clock.System)
        return KernelFixture(
            accessService = accessService,
            businessDayService = BusinessDayService(repository, accessService),
            shiftService = ShiftService(repository, accessService)
        )
    }
}

private data class KernelFixture(
    val accessService: AccessService,
    val businessDayService: BusinessDayService,
    val shiftService: ShiftService
)
