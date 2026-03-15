package id.azureenterprise.cassy.kernel.domain

import kotlinx.datetime.Instant

data class BusinessDay(
    val id: String,
    val openedAt: Instant,
    val closedAt: Instant? = null,
    val status: String
)

data class Shift(
    val id: String,
    val businessDayId: String,
    val terminalId: String,
    val openedAt: Instant,
    val openingCash: Double,
    val closedAt: Instant? = null,
    val closingCash: Double? = null,
    val openedBy: String,
    val closedBy: String? = null,
    val status: String
)
