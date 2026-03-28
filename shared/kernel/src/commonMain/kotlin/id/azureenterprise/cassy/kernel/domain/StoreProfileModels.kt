package id.azureenterprise.cassy.kernel.domain

import kotlinx.datetime.Instant

data class StoreProfile(
    val id: String,
    val storeId: String,
    val businessName: String,
    val address: String,
    val streetAddress: String,
    val neighborhood: String,
    val village: String,
    val district: String,
    val city: String,
    val province: String,
    val postalCode: String,
    val phoneCountryCode: String,
    val phoneNumber: String,
    val businessEmail: String?,
    val legalId: String?,
    val receiptNote: String?,
    val logoPath: String?,
    val showLogoOnReceipt: Boolean,
    val showAddressOnReceipt: Boolean,
    val showPhoneOnReceipt: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant,
    val revision: Long
)

data class StoreProfileDraft(
    val businessName: String = "",
    val address: String = "",
    val streetAddress: String = "",
    val neighborhood: String = "",
    val village: String = "",
    val district: String = "",
    val city: String = "",
    val province: String = "",
    val postalCode: String = "",
    val phoneCountryCode: String = DEFAULT_PHONE_COUNTRY_CODE,
    val phoneNumber: String = "",
    val businessEmail: String = "",
    val legalId: String = "",
    val receiptNote: String = "",
    val logoPath: String? = null,
    val showLogoOnReceipt: Boolean = true,
    val showAddressOnReceipt: Boolean = true,
    val showPhoneOnReceipt: Boolean = true
)

enum class StoreProfileField {
    BUSINESS_NAME,
    STREET_ADDRESS,
    NEIGHBORHOOD,
    VILLAGE,
    DISTRICT,
    CITY,
    PROVINCE,
    POSTAL_CODE,
    PHONE_COUNTRY_CODE,
    PHONE_NUMBER,
    BUSINESS_EMAIL,
    LEGAL_ID,
    RECEIPT_NOTE,
    LOGO_PATH
}

data class StoreProfileFieldIssue(
    val field: StoreProfileField,
    val message: String
)

data class StoreProfileValidationResult(
    val normalizedDraft: StoreProfileDraft,
    val issues: List<StoreProfileFieldIssue>
) {
    val isValid: Boolean
        get() = issues.isEmpty()
}

const val DEFAULT_PHONE_COUNTRY_CODE = "+62"
