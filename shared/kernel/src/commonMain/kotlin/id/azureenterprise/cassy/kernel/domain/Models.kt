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
    val closedAt: Instant? = null,
    val openedBy: String,
    val closedBy: String? = null,
    val status: String
)
