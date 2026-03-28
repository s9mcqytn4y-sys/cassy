package id.azureenterprise.cassy.sales.application

interface SalesKernelPort {
    suspend fun getOperationalContext(): SalesOperationalContext?
    suspend fun recordAudit(auditId: String, message: String)
    suspend fun recordEvent(eventId: String, type: String, payload: String)
}

data class SalesOperationalContext(
    val storeName: String,
    val terminalId: String,
    val terminalName: String? = null,
    val shiftId: String,
    val operatorName: String? = null,
    val businessAddress: String? = null,
    val businessPhone: String? = null,
    val businessEmail: String? = null,
    val businessLegalId: String? = null,
    val receiptNote: String? = null,
    val showLogoOnReceipt: Boolean = true,
    val showAddressOnReceipt: Boolean = true,
    val showPhoneOnReceipt: Boolean = true
)
