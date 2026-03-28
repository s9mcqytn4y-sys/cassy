package id.azureenterprise.cassy.kernel.application

import id.azureenterprise.cassy.kernel.data.KernelRepository
import id.azureenterprise.cassy.kernel.domain.DEFAULT_PHONE_COUNTRY_CODE
import id.azureenterprise.cassy.kernel.domain.IdGenerator
import id.azureenterprise.cassy.kernel.domain.StoreProfile
import id.azureenterprise.cassy.kernel.domain.StoreProfileDraft
import id.azureenterprise.cassy.kernel.domain.StoreProfileField
import id.azureenterprise.cassy.kernel.domain.StoreProfileFieldIssue
import id.azureenterprise.cassy.kernel.domain.StoreProfileValidationResult
import kotlinx.datetime.Clock

class StoreProfileService(
    private val kernelRepository: KernelRepository,
    private val clock: Clock
) {
    suspend fun loadDraft(): Result<StoreProfileDraft> {
        val binding = kernelRepository.getTerminalBinding()
            ?: return Result.failure(IllegalStateException("Profil usaha belum tersedia karena toko belum terdaftar"))
        val profile = kernelRepository.getStoreProfile(binding.storeId)
        return Result.success(
            if (profile != null) {
                profile.toDraft()
            } else {
                StoreProfileDraft(
                    businessName = binding.storeName,
                    phoneCountryCode = DEFAULT_PHONE_COUNTRY_CODE
                )
            }
        )
    }

    fun validate(draft: StoreProfileDraft): StoreProfileValidationResult {
        val normalizedDraft = StoreProfileDraft(
            businessName = normalizeSingleLine(draft.businessName),
            address = normalizeMultiline(draft.address),
            phoneCountryCode = normalizeCountryCode(draft.phoneCountryCode),
            phoneNumber = normalizePhoneNumber(draft.phoneNumber),
            receiptNote = normalizeMultiline(draft.receiptNote),
            logoPath = draft.logoPath?.trim()?.takeIf { it.isNotEmpty() }
        )

        val issues = buildList {
            if (normalizedDraft.businessName.isBlank()) {
                add(StoreProfileFieldIssue(StoreProfileField.BUSINESS_NAME, "Nama usaha wajib diisi"))
            } else if (normalizedDraft.businessName.length > MAX_BUSINESS_NAME_LENGTH) {
                add(StoreProfileFieldIssue(StoreProfileField.BUSINESS_NAME, "Nama usaha maksimal $MAX_BUSINESS_NAME_LENGTH karakter"))
            }

            if (normalizedDraft.address.isBlank()) {
                add(StoreProfileFieldIssue(StoreProfileField.ADDRESS, "Alamat usaha wajib diisi"))
            } else if (normalizedDraft.address.length > MAX_ADDRESS_LENGTH) {
                add(StoreProfileFieldIssue(StoreProfileField.ADDRESS, "Alamat usaha maksimal $MAX_ADDRESS_LENGTH karakter"))
            }

            if (!COUNTRY_CODE_REGEX.matches(normalizedDraft.phoneCountryCode)) {
                add(StoreProfileFieldIssue(StoreProfileField.PHONE_COUNTRY_CODE, "Kode negara harus diawali + lalu 1 sampai 4 digit"))
            }

            when {
                normalizedDraft.phoneNumber.isBlank() -> {
                    add(StoreProfileFieldIssue(StoreProfileField.PHONE_NUMBER, "Nomor telepon wajib diisi"))
                }
                normalizedDraft.phoneNumber.length < MIN_PHONE_NUMBER_LENGTH -> {
                    add(StoreProfileFieldIssue(StoreProfileField.PHONE_NUMBER, "Nomor telepon minimal $MIN_PHONE_NUMBER_LENGTH digit"))
                }
                normalizedDraft.phoneNumber.length > MAX_PHONE_NUMBER_LENGTH -> {
                    add(StoreProfileFieldIssue(StoreProfileField.PHONE_NUMBER, "Nomor telepon maksimal $MAX_PHONE_NUMBER_LENGTH digit"))
                }
            }

            if (normalizedDraft.receiptNote.length > MAX_RECEIPT_NOTE_LENGTH) {
                add(StoreProfileFieldIssue(StoreProfileField.RECEIPT_NOTE, "Catatan struk maksimal $MAX_RECEIPT_NOTE_LENGTH karakter"))
            }
        }

        return StoreProfileValidationResult(
            normalizedDraft = normalizedDraft,
            issues = issues
        )
    }

    suspend fun save(draft: StoreProfileDraft): Result<StoreProfile> {
        val binding = kernelRepository.getTerminalBinding()
            ?: return Result.failure(IllegalStateException("Profil usaha belum bisa disimpan karena toko belum terdaftar"))
        val validation = validate(draft)
        if (!validation.isValid) {
            return Result.failure(IllegalArgumentException(validation.issues.first().message))
        }

        val existing = kernelRepository.getStoreProfile(binding.storeId)
        val now = clock.now()
        val profile = StoreProfile(
            id = existing?.id ?: IdGenerator.nextId("store_profile"),
            storeId = binding.storeId,
            businessName = validation.normalizedDraft.businessName,
            address = validation.normalizedDraft.address,
            phoneCountryCode = validation.normalizedDraft.phoneCountryCode,
            phoneNumber = validation.normalizedDraft.phoneNumber,
            receiptNote = validation.normalizedDraft.receiptNote.takeIf { it.isNotBlank() },
            logoPath = validation.normalizedDraft.logoPath,
            createdAt = existing?.createdAt ?: now,
            updatedAt = now,
            revision = (existing?.revision ?: 0L) + 1L
        )
        kernelRepository.upsertStoreProfile(profile)
        kernelRepository.insertAudit(
            id = IdGenerator.nextId("audit"),
            message = "Profil usaha diperbarui untuk ${profile.businessName}",
            level = "INFO"
        )
        return Result.success(profile)
    }

    private fun StoreProfile.toDraft(): StoreProfileDraft {
        return StoreProfileDraft(
            businessName = businessName,
            address = address,
            phoneCountryCode = phoneCountryCode,
            phoneNumber = phoneNumber,
            receiptNote = receiptNote.orEmpty(),
            logoPath = logoPath
        )
    }

    private fun normalizeSingleLine(value: String): String {
        return value.trim()
            .replace(WHITESPACE_REGEX, " ")
    }

    private fun normalizeMultiline(value: String): String {
        return value
            .lines()
            .map { it.trim().replace(WHITESPACE_REGEX, " ") }
            .filter { it.isNotEmpty() }
            .joinToString(separator = "\n")
    }

    private fun normalizeCountryCode(value: String): String {
        val digitsOnly = value.filter(Char::isDigit)
        if (digitsOnly.isEmpty()) return DEFAULT_PHONE_COUNTRY_CODE
        return "+$digitsOnly"
    }

    private fun normalizePhoneNumber(value: String): String {
        return value
            .filter(Char::isDigit)
            .trimStart('0')
    }

    private companion object {
        val WHITESPACE_REGEX = Regex("\\s+")
        val COUNTRY_CODE_REGEX = Regex("^\\+[0-9]{1,4}$")
        const val MAX_BUSINESS_NAME_LENGTH = 120
        const val MAX_ADDRESS_LENGTH = 240
        const val MIN_PHONE_NUMBER_LENGTH = 8
        const val MAX_PHONE_NUMBER_LENGTH = 15
        const val MAX_RECEIPT_NOTE_LENGTH = 180
    }
}
