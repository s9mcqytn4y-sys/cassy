package id.azureenterprise.cassy.sales.application

import id.azureenterprise.cassy.sales.domain.PaymentState

data class PaymentGatewayRequest(
    val saleId: String,
    val paymentId: String,
    val localNumber: String,
    val paymentMethod: String,
    val amount: Double,
    val idempotencyKey: String,
    val isRetry: Boolean
)

data class PaymentGatewayResult(
    val paymentState: PaymentState,
    val providerReference: String? = null
)

data class PaymentCallbackRequest(
    val saleId: String,
    val providerReference: String,
    val paymentState: PaymentState
)

interface PaymentGatewayPort {
    suspend fun finalizePayment(request: PaymentGatewayRequest): PaymentGatewayResult
}
