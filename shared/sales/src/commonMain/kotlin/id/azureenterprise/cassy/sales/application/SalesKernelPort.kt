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
    val receiptNote: String? = null
)
