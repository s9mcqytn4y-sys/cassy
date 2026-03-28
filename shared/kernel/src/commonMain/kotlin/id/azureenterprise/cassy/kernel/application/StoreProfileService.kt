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
                    phoneCountryCode = DEFAULT_PHONE_COUNTRY_CODE,
                    showLogoOnReceipt = true,
                    showAddressOnReceipt = true,
                    showPhoneOnReceipt = true
                )
            }
        )
    }

    fun validate(draft: StoreProfileDraft): StoreProfileValidationResult {
        val normalizedDraft = StoreProfileDraft(
            businessName = normalizeSingleLine(draft.businessName),
            address = buildFormattedAddress(
                streetAddress = draft.streetAddress,
                neighborhood = draft.neighborhood,
                village = draft.village,
                district = draft.district,
                city = draft.city,
                province = draft.province,
                postalCode = draft.postalCode
            ),
            streetAddress = normalizeSingleLine(draft.streetAddress),
            neighborhood = normalizeSingleLine(draft.neighborhood),
            village = normalizeSingleLine(draft.village),
            district = normalizeSingleLine(draft.district),
            city = normalizeSingleLine(draft.city),
            province = normalizeSingleLine(draft.province),
            postalCode = normalizePostalCode(draft.postalCode),
            phoneCountryCode = normalizeCountryCode(draft.phoneCountryCode),
            phoneNumber = normalizePhoneNumber(draft.phoneNumber),
            businessEmail = normalizeOptionalEmail(draft.businessEmail),
            legalId = normalizeOptionalSingleLine(draft.legalId),
            receiptNote = normalizeMultiline(draft.receiptNote),
            logoPath = draft.logoPath?.trim()?.takeIf { it.isNotEmpty() },
            showLogoOnReceipt = draft.showLogoOnReceipt,
            showAddressOnReceipt = draft.showAddressOnReceipt,
            showPhoneOnReceipt = draft.showPhoneOnReceipt
        )

        val issues = buildList {
            if (normalizedDraft.businessName.isBlank()) {
                add(StoreProfileFieldIssue(StoreProfileField.BUSINESS_NAME, "Nama usaha wajib diisi"))
            } else if (normalizedDraft.businessName.length > MAX_BUSINESS_NAME_LENGTH) {
                add(StoreProfileFieldIssue(StoreProfileField.BUSINESS_NAME, "Nama usaha maksimal $MAX_BUSINESS_NAME_LENGTH karakter"))
            }

            validateRequiredSingleLine(
                value = normalizedDraft.streetAddress,
                field = StoreProfileField.STREET_ADDRESS,
                emptyMessage = "Alamat jalan wajib diisi",
                maxLength = MAX_STREET_ADDRESS_LENGTH
            ).let(::addAll)
            validateRequiredSingleLine(
                value = normalizedDraft.neighborhood,
                field = StoreProfileField.NEIGHBORHOOD,
                emptyMessage = "RT/RW atau lingkungan wajib diisi",
                maxLength = MAX_NEIGHBORHOOD_LENGTH
            ).let(::addAll)
            validateRequiredSingleLine(
                value = normalizedDraft.village,
                field = StoreProfileField.VILLAGE,
                emptyMessage = "Kelurahan atau desa wajib diisi",
                maxLength = MAX_VILLAGE_LENGTH
            ).let(::addAll)
            validateRequiredSingleLine(
                value = normalizedDraft.district,
                field = StoreProfileField.DISTRICT,
                emptyMessage = "Kecamatan wajib diisi",
                maxLength = MAX_DISTRICT_LENGTH
            ).let(::addAll)
            validateRequiredSingleLine(
                value = normalizedDraft.city,
                field = StoreProfileField.CITY,
                emptyMessage = "Kota atau kabupaten wajib diisi",
                maxLength = MAX_CITY_LENGTH
            ).let(::addAll)
            validateRequiredSingleLine(
                value = normalizedDraft.province,
                field = StoreProfileField.PROVINCE,
                emptyMessage = "Provinsi wajib diisi",
                maxLength = MAX_PROVINCE_LENGTH
            ).let(::addAll)

            when {
                normalizedDraft.postalCode.isBlank() -> {
                    add(StoreProfileFieldIssue(StoreProfileField.POSTAL_CODE, "Kode pos wajib diisi"))
                }
                normalizedDraft.postalCode.length !in POSTAL_CODE_LENGTH_RANGE -> {
                    add(StoreProfileFieldIssue(StoreProfileField.POSTAL_CODE, "Kode pos harus 5 digit"))
                }
            }

            if (normalizedDraft.address.length > MAX_FORMATTED_ADDRESS_LENGTH) {
                add(StoreProfileFieldIssue(StoreProfileField.STREET_ADDRESS, "Alamat usaha terlalu panjang"))
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

            if (normalizedDraft.businessEmail.isNotBlank() && !EMAIL_REGEX.matches(normalizedDraft.businessEmail)) {
                add(StoreProfileFieldIssue(StoreProfileField.BUSINESS_EMAIL, "Email usaha belum valid"))
            }

            if (normalizedDraft.legalId.length > MAX_LEGAL_ID_LENGTH) {
                add(StoreProfileFieldIssue(StoreProfileField.LEGAL_ID, "ID legal maksimal $MAX_LEGAL_ID_LENGTH karakter"))
            }

            if (normalizedDraft.receiptNote.isBlank()) {
                add(StoreProfileFieldIssue(StoreProfileField.RECEIPT_NOTE, "Catatan struk wajib diisi"))
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
            streetAddress = validation.normalizedDraft.streetAddress,
            neighborhood = validation.normalizedDraft.neighborhood,
            village = validation.normalizedDraft.village,
            district = validation.normalizedDraft.district,
            city = validation.normalizedDraft.city,
            province = validation.normalizedDraft.province,
            postalCode = validation.normalizedDraft.postalCode,
            phoneCountryCode = validation.normalizedDraft.phoneCountryCode,
            phoneNumber = validation.normalizedDraft.phoneNumber,
            businessEmail = validation.normalizedDraft.businessEmail.takeIf { it.isNotBlank() },
            legalId = validation.normalizedDraft.legalId.takeIf { it.isNotBlank() },
            receiptNote = validation.normalizedDraft.receiptNote.takeIf { it.isNotBlank() },
            logoPath = validation.normalizedDraft.logoPath,
            showLogoOnReceipt = validation.normalizedDraft.showLogoOnReceipt,
            showAddressOnReceipt = validation.normalizedDraft.showAddressOnReceipt,
            showPhoneOnReceipt = validation.normalizedDraft.showPhoneOnReceipt,
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
            streetAddress = streetAddress,
            neighborhood = neighborhood,
            village = village,
            district = district,
            city = city,
            province = province,
            postalCode = postalCode,
            phoneCountryCode = phoneCountryCode,
            phoneNumber = phoneNumber,
            businessEmail = businessEmail.orEmpty(),
            legalId = legalId.orEmpty(),
            receiptNote = receiptNote.orEmpty(),
            logoPath = logoPath,
            showLogoOnReceipt = showLogoOnReceipt,
            showAddressOnReceipt = showAddressOnReceipt,
            showPhoneOnReceipt = showPhoneOnReceipt
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

    private fun normalizeOptionalSingleLine(value: String): String {
        return normalizeSingleLine(value)
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

    private fun normalizePostalCode(value: String): String {
        return value.filter(Char::isDigit).take(5)
    }

    private fun normalizeOptionalEmail(value: String): String {
        return value.trim().lowercase()
    }

    private fun validateRequiredSingleLine(
        value: String,
        field: StoreProfileField,
        emptyMessage: String,
        maxLength: Int
    ): List<StoreProfileFieldIssue> {
        return buildList {
            when {
                value.isBlank() -> add(StoreProfileFieldIssue(field, emptyMessage))
                value.length > maxLength -> add(StoreProfileFieldIssue(field, "Maksimal $maxLength karakter"))
            }
        }
    }

    private fun buildFormattedAddress(
        streetAddress: String,
        neighborhood: String,
        village: String,
        district: String,
        city: String,
        province: String,
        postalCode: String
    ): String {
        val normalizedStreet = normalizeSingleLine(streetAddress)
        val normalizedNeighborhood = normalizeSingleLine(neighborhood)
        val normalizedVillage = normalizeSingleLine(village)
        val normalizedDistrict = normalizeSingleLine(district)
        val normalizedCity = normalizeSingleLine(city)
        val normalizedProvince = normalizeSingleLine(province)
        val normalizedPostalCode = normalizePostalCode(postalCode)

        return listOf(
            listOf(normalizedStreet, normalizedNeighborhood).filter { it.isNotBlank() }.joinToString(", "),
            listOf(normalizedVillage, normalizedDistrict).filter { it.isNotBlank() }.joinToString(", "),
            listOf(normalizedCity, normalizedProvince, normalizedPostalCode).filter { it.isNotBlank() }.joinToString(", ")
        ).filter { it.isNotBlank() }
            .joinToString(separator = "\n")
    }

    private companion object {
        val WHITESPACE_REGEX = Regex("\\s+")
        val COUNTRY_CODE_REGEX = Regex("^\\+[0-9]{1,4}$")
        val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")
        const val MAX_BUSINESS_NAME_LENGTH = 120
        const val MAX_STREET_ADDRESS_LENGTH = 140
        const val MAX_NEIGHBORHOOD_LENGTH = 40
        const val MAX_VILLAGE_LENGTH = 80
        const val MAX_DISTRICT_LENGTH = 80
        const val MAX_CITY_LENGTH = 80
        const val MAX_PROVINCE_LENGTH = 80
        val POSTAL_CODE_LENGTH_RANGE = 5..5
        const val MAX_FORMATTED_ADDRESS_LENGTH = 320
        const val MIN_PHONE_NUMBER_LENGTH = 8
        const val MAX_PHONE_NUMBER_LENGTH = 15
        const val MAX_LEGAL_ID_LENGTH = 64
        const val MAX_RECEIPT_NOTE_LENGTH = 180
    }
}
