package id.azureenterprise.cassy.sales.application

import id.azureenterprise.cassy.sales.domain.PaymentState
import id.azureenterprise.cassy.sales.domain.PaymentStatusDetailCode

class LocalPaymentGatewayStub : PaymentGatewayPort {
    override suspend fun finalizePayment(request: PaymentGatewayRequest): PaymentGatewayResult {
        return when (request.paymentMethod) {
            "CASH" -> PaymentGatewayResult(
                paymentState = PaymentState.success(),
                providerReference = "cash:${request.saleId}"
            )
            "CARD", "QRIS" -> PaymentGatewayResult(
                paymentState = PaymentState.pending(
                    detailCode = PaymentStatusDetailCode.AWAITING_PROVIDER_CONFIRMATION,
                    detailMessage = "Menunggu konfirmasi adapter pembayaran"
                ),
                providerReference = "${request.paymentMethod.lowercase()}:${request.saleId}"
            )
            else -> PaymentGatewayResult(
                paymentState = PaymentState.failed(
                    detailCode = PaymentStatusDetailCode.VALIDATION_ERROR,
                    detailMessage = "Metode pembayaran tidak didukung"
                )
            )
        }
    }
}
