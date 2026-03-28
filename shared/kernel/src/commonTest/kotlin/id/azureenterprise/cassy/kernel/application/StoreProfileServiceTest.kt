package id.azureenterprise.cassy.kernel.application

import id.azureenterprise.cassy.kernel.domain.BootstrapStoreRequest
import id.azureenterprise.cassy.kernel.domain.DEFAULT_PHONE_COUNTRY_CODE
import id.azureenterprise.cassy.kernel.domain.PinHasher
import id.azureenterprise.cassy.kernel.domain.StoreProfileDraft
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StoreProfileServiceTest {

    private class LocalFakePinHasher : PinHasher() {
        override fun hash(pin: String, salt: String): String = "$salt:$pin"
    }

    private val fakeRepo = FakeKernelRepository()
    private val accessService = AccessService(fakeRepo, LocalFakePinHasher(), Clock.System)
    private val service = StoreProfileService(fakeRepo, Clock.System)

    @Test
    fun `load draft falls back to binding data when profile not saved yet`() = runTest {
        accessService.bootstrapStore(
            BootstrapStoreRequest(
                storeName = "Toko Sinar Jaya",
                terminalName = "Kasir Depan",
                cashierName = "Ayu",
                cashierPin = "111111",
                supervisorName = "Doni",
                supervisorPin = "222222"
            )
        )

        val draft = service.loadDraft().getOrThrow()

        assertEquals("Toko Sinar Jaya", draft.businessName)
        assertEquals(DEFAULT_PHONE_COUNTRY_CODE, draft.phoneCountryCode)
    }

    @Test
    fun `save normalizes data and stores profile`() = runTest {
        val binding = accessService.bootstrapStore(
            BootstrapStoreRequest(
                storeName = "Toko Maju",
                terminalName = "Kasir 1",
                cashierName = "Lina",
                cashierPin = "111111",
                supervisorName = "Bagus",
                supervisorPin = "222222"
            )
        ).getOrThrow()

        val result = service.save(
            StoreProfileDraft(
                businessName = "  Toko   Maju Bersama  ",
                address = "  Jl. Melati No. 8  \n  Bandung  ",
                phoneCountryCode = "62",
                phoneNumber = "0812-3456-7890",
                receiptNote = "  Terima kasih   sudah belanja  "
            )
        )

        val profile = result.getOrThrow()
        assertEquals("Toko Maju Bersama", profile.businessName)
        assertEquals("Jl. Melati No. 8\nBandung", profile.address)
        assertEquals("+62", profile.phoneCountryCode)
        assertEquals("81234567890", profile.phoneNumber)
        assertEquals("Terima kasih sudah belanja", profile.receiptNote)
        assertEquals(profile, fakeRepo.getStoreProfile(binding.storeId))
    }

    @Test
    fun `save rejects invalid required fields`() = runTest {
        accessService.bootstrapStore(
            BootstrapStoreRequest(
                storeName = "Toko Uji",
                terminalName = "Kasir 1",
                cashierName = "Lina",
                cashierPin = "111111",
                supervisorName = "Bagus",
                supervisorPin = "222222"
            )
        )

        val validation = service.validate(
            StoreProfileDraft(
                businessName = "   ",
                address = "",
                phoneCountryCode = "+62",
                phoneNumber = "0812"
            )
        )

        assertTrue(!validation.isValid)
        assertEquals(3, validation.issues.size)
        assertEquals("Nama usaha wajib diisi", validation.issues[0].message)
    }
}
