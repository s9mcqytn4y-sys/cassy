package id.azureenterprise.cassy.sales.application

interface SalesKernelPort {
    suspend fun getOperationalContext(): SalesOperationalContext?
    suspend fun recordAudit(message: String)
    suspend fun recordEvent(type: String, payload: String)
}

data class SalesOperationalContext(
    val storeName: String,
    val terminalId: String,
    val terminalName: String? = null,
    val shiftId: String
)
