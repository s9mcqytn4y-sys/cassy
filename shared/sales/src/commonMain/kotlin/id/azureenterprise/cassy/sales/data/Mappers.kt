package id.azureenterprise.cassy.sales.data

import id.azureenterprise.cassy.sales.db.Sale as DbSale
import id.azureenterprise.cassy.sales.domain.Sale
import kotlinx.datetime.Instant

fun DbSale.toDomain(): Sale = Sale(
    id = id,
    localNumber = localNumber,
    shiftId = shiftId,
    terminalId = terminalId,
    timestamp = Instant.fromEpochMilliseconds(timestamp),
    totalAmount = totalAmount,
    taxAmount = taxAmount,
    discountAmount = discountAmount,
    finalAmount = finalAmount,
    status = status,
    suspendedAt = suspendedAt?.let { Instant.fromEpochMilliseconds(it) }
)
