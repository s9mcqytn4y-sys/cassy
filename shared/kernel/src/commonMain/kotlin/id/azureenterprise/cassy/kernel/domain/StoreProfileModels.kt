package id.azureenterprise.cassy.kernel.domain

import kotlinx.datetime.Instant

data class StoreProfile(
    val id: String,
    val storeId: String,
    val businessName: String,
    val address: String,
    val phoneCountryCode: String,
    val phoneNumber: String,
    val receiptNote: String?,
    val logoPath: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
    val revision: Long
)

data class StoreProfileDraft(
    val businessName: String = "",
    val address: String = "",
    val phoneCountryCode: String = DEFAULT_PHONE_COUNTRY_CODE,
    val phoneNumber: String = "",
    val receiptNote: String = "",
    val logoPath: String? = null
)

enum class StoreProfileField {
    BUSINESS_NAME,
    ADDRESS,
    PHONE_COUNTRY_CODE,
    PHONE_NUMBER,
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
