package id.azureenterprise.cassy.kernel.application

import id.azureenterprise.cassy.kernel.domain.*
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class AccessServiceTest {

    private class LocalFakePinHasher : PinHasher() {
        override fun hash(pin: String, salt: String): String = "$salt:$pin"
    }

    private val fakeRepo = FakeKernelRepository()
    private val clock = Clock.System
    private val service = AccessService(fakeRepo, LocalFakePinHasher(), clock)

    @Test
    fun `test bootstrap store success`() = runTest {
        val request = BootstrapStoreRequest(
            storeName = "Test Store",
            terminalName = "T01",
            cashierName = "Alice",
            cashierPin = "111111",
            supervisorName = "Bob",
            supervisorPin = "222222"
        )

        val result = service.bootstrapStore(request)
        assertTrue(result.isSuccess)

        val context = service.restoreContext()
        assertEquals("Test Store", context.terminalBinding?.storeName)
        assertEquals(2, context.operators.size)
        assertFalse(service.needsBootstrap())
    }

    @Test
    fun `test bootstrap fails if already bootstrapped`() = runTest {
        val request = BootstrapStoreRequest("S1", "T1", "C1", "111111", "S1", "222222")
        service.bootstrapStore(request)

        val result = service.bootstrapStore(request)
        assertTrue(result.isFailure)
        assertEquals("Store sudah di-bootstrap", result.exceptionOrNull()?.message)
    }

    @Test
    fun `test login success and session creation`() = runTest {
        val request = BootstrapStoreRequest("S1", "T1", "C1", "111111", "S1", "222222")
        service.bootstrapStore(request)

        val cashier = service.restoreContext().operators.find { it.role == OperatorRole.CASHIER }!!
        val loginResult = service.login(cashier.id, "111111")

        assertTrue(loginResult is LoginResult.Success)
        val context = service.restoreContext()
        assertNotNull(context.activeSession)
        assertEquals(cashier.id, context.activeOperator?.id)
    }

    @Test
    fun `test login wrong pin decreases attempts`() = runTest {
        val request = BootstrapStoreRequest("S1", "T1", "C1", "111111", "S1", "222222")
        service.bootstrapStore(request)

        val cashier = service.restoreContext().operators.find { it.role == OperatorRole.CASHIER }!!
        val loginResult = service.login(cashier.id, "wrong-pin")

        if (loginResult is LoginResult.WrongPin) {
            assertEquals(2, loginResult.remainingBeforeLock)
        } else {
            assertTrue(false, "Result should be WrongPin")
        }
    }

    @Test
    fun `restoreContext seeds operational metadata defaults`() = runTest {
        service.restoreContext()

        assertEquals("1", fakeRepo.getMetadata("report.export.schema_version"))
        assertEquals("LOCAL_FIRST_SNAPSHOT", fakeRepo.getMetadata("report.export.policy"))
        assertEquals("LOCAL_BOUNDARY_READY", fakeRepo.getMetadata("sync.replay.scope"))
    }

    @Test
    fun `validate bootstrap normalizes names and checks required fields`() = runTest {
        val validation = service.validateBootstrapRequest(
            BootstrapStoreRequest(
                storeName = "  Toko   Uji  ",
                terminalName = "  Kasir-01 ",
                cashierName = "",
                cashierPin = "12a45",
                supervisorName = "  Bayu  ",
                supervisorPin = "222222"
            )
        )

        assertEquals("Toko Uji", validation.normalizedRequest.storeName)
        assertEquals("Kasir-01", validation.normalizedRequest.terminalName)
        assertEquals("Bayu", validation.normalizedRequest.supervisorName)
        assertTrue(validation.issues.any { it.field == BootstrapStoreField.CASHIER_NAME })
        assertTrue(validation.issues.any { it.field == BootstrapStoreField.CASHIER_PIN })
    }

    @Test
    fun `bootstrap persists avatar path for operator`() = runTest {
        val avatarPath = "C:/sandbox/operator-avatars/cashier-initial.png"
        val result = service.bootstrapStore(
            BootstrapStoreRequest(
                storeName = "Store Avatar",
                terminalName = "T01",
                cashierName = "Alice",
                cashierPin = "111111",
                supervisorName = "Bob",
                supervisorPin = "222222",
                cashierAvatarPath = avatarPath
            )
        )

        assertTrue(result.isSuccess)
        val cashier = service.restoreContext().operators.first { it.role == OperatorRole.CASHIER }
        assertEquals(avatarPath, cashier.avatarPath)
    }
}
