package id.azureenterprise.cassy.sales.domain

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

enum class SaleStatus {
    PENDING,
    COMPLETED,
    CANCELLED,
    SUSPENDED
}

@Serializable
enum class PaymentStatus {
    PENDING,
    SUCCESS,
    FAILED,
    CANCELLED
}

@Serializable
enum class PaymentStatusDetailCode {
    AWAITING_FINALIZATION,
    AWAITING_PROVIDER_CONFIRMATION,
    DECLINED,
    VALIDATION_ERROR,
    TECHNICAL_FAILURE,
    CANCELLED_BY_OPERATOR
}

@Serializable
data class PaymentState(
    val status: PaymentStatus,
    val detailCode: PaymentStatusDetailCode? = null,
    val detailMessage: String? = null
) {
    init {
        if (status == PaymentStatus.SUCCESS) {
            require(detailCode == null) { "Payment success tidak boleh punya status detail" }
        }
        if (status == PaymentStatus.PENDING || status == PaymentStatus.FAILED) {
            require(detailCode != null) { "Payment pending/failed wajib punya detail code" }
        }
    }

    companion object {
        fun pending(
            detailCode: PaymentStatusDetailCode,
            detailMessage: String? = null
        ): PaymentState = PaymentState(
            status = PaymentStatus.PENDING,
            detailCode = detailCode,
            detailMessage = detailMessage
        )

        fun success(): PaymentState = PaymentState(status = PaymentStatus.SUCCESS)

        fun failed(
            detailCode: PaymentStatusDetailCode,
            detailMessage: String? = null
        ): PaymentState = PaymentState(
            status = PaymentStatus.FAILED,
            detailCode = detailCode,
            detailMessage = detailMessage
        )

        fun cancelled(
            detailCode: PaymentStatusDetailCode = PaymentStatusDetailCode.CANCELLED_BY_OPERATOR,
            detailMessage: String? = null
        ): PaymentState = PaymentState(
            status = PaymentStatus.CANCELLED,
            detailCode = detailCode,
            detailMessage = detailMessage
        )
    }

    val isFinal: Boolean
        get() = status == PaymentStatus.SUCCESS || status == PaymentStatus.FAILED || status == PaymentStatus.CANCELLED

    val canCompleteSale: Boolean
        get() = status == PaymentStatus.SUCCESS
}

@Serializable
data class ReceiptTemplateSnapshot(
    val templateId: String = "thermal-80mm-v1",
    val templateVersion: Int = 1,
    val paperWidthMm: Int = 80,
    val lineWidth: Int = 32
)

enum class ReceiptPrintStatus {
    NOT_REQUESTED,
    READY_FOR_PRINT,
    PRINTED,
    FAILED
}

data class ReceiptPrintState(
    val status: ReceiptPrintStatus,
    val detailMessage: String? = null
)

data class Sale(
    val id: String,
    val localNumber: String,
    val shiftId: String,
    val terminalId: String,
    val timestamp: Instant,
    val totalAmount: Double,
    val taxAmount: Double,
    val discountAmount: Double,
    val finalAmount: Double,
    val status: SaleStatus,
    val suspendedAt: Instant? = null
)

data class Shift(
    val id: String,
    val businessDayId: String,
    val terminalId: String,
    val openedAt: Instant,
    val closedAt: Instant? = null,
    val openedBy: String,
    val closedBy: String? = null,
    val status: String
)

data class Payment(
    val id: String,
    val saleId: String,
    val method: String,
    val amount: Double,
    val state: PaymentState,
    val providerReference: String? = null,
    val timestamp: Instant
)

@Serializable
data class ReceiptPaymentSnapshot(
    val method: String,
    val amount: Double,
    val state: PaymentState,
    val providerReference: String? = null
)

@Serializable
data class ReceiptItemSnapshot(
    val productId: String,
    val productName: String,
    val quantity: Double,
    val unitPrice: Double,
    val lineTotal: Double,
    val taxAmount: Double,
    val discountAmount: Double
)

@Serializable
data class ReceiptSnapshotDocument(
    val version: Int = 2,
    val saleId: String,
    val localNumber: String,
    val storeName: String = "",
    val shiftId: String,
    val terminalId: String,
    val terminalName: String? = null,
    val finalizedAtEpochMs: Long,
    val template: ReceiptTemplateSnapshot = ReceiptTemplateSnapshot(),
    val payment: ReceiptPaymentSnapshot,
    val totals: BasketTotals,
    val items: List<ReceiptItemSnapshot>,
    val footerLines: List<String> = listOf("Terima kasih")
)

data class CompletedSaleReadback(
    val sale: Sale,
    val receiptSnapshot: ReceiptSnapshotDocument
)

typealias FinalizedSale = CompletedSaleReadback

data class SaleHistoryEntry(
    val saleId: String,
    val localNumber: String,
    val terminalId: String,
    val finalizedAtEpochMs: Long,
    val finalAmount: Double,
    val paymentMethod: String,
    val paymentState: PaymentState
)

data class PersistedReceiptSnapshot(
    val snapshot: ReceiptSnapshotDocument,
    val createdAtEpochMs: Long
)

data class PersistedSaleItem(
    val productId: String,
    val productName: String,
    val quantity: Double,
    val unitPrice: Double,
    val totalPrice: Double,
    val taxAmount: Double,
    val discountAmount: Double
)

data class PendingSaleReadback(
    val sale: Sale,
    val items: List<PersistedSaleItem>,
    val payment: Payment?
)

data class ReceiptPrintPayload(
    val saleId: String,
    val snapshot: ReceiptSnapshotDocument,
    val renderedContent: String,
    val templateId: String,
    val paperWidthMm: Int,
    val printState: ReceiptPrintState,
    val isReprint: Boolean
)

data class SaleCompletionResult(
    val saleId: String,
    val localNumber: String,
    val readback: CompletedSaleReadback,
    val printState: ReceiptPrintState
)

sealed interface CompleteSaleOutcome {
    data class Completed(
        val result: SaleCompletionResult,
        val replayed: Boolean = false
    ) : CompleteSaleOutcome

    data class Pending(
        val saleId: String,
        val localNumber: String,
        val paymentState: PaymentState
    ) : CompleteSaleOutcome

    data class Rejected(
        val saleId: String,
        val localNumber: String,
        val paymentState: PaymentState
    ) : CompleteSaleOutcome
}
