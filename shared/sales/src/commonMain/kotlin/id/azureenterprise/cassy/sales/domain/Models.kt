package id.azureenterprise.cassy.sales.domain

import kotlinx.datetime.Instant

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
    val status: String,
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
    val status: String,
    val providerReference: String? = null,
    val timestamp: Instant
)
