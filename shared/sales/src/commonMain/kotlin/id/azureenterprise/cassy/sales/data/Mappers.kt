package id.azureenterprise.cassy.sales.data

import id.azureenterprise.cassy.db.Shift as DbShift
import id.azureenterprise.cassy.db.Sale as DbSale
import id.azureenterprise.cassy.sales.domain.Shift
import id.azureenterprise.cassy.sales.domain.Sale
import kotlinx.datetime.Instant

fun DbShift.toDomain(): Shift = Shift(
    id = id,
    businessDayId = businessDayId,
    terminalId = terminalId,
    openedAt = Instant.fromEpochMilliseconds(openedAt),
    closedAt = closedAt?.let { Instant.fromEpochMilliseconds(it) },
    openedBy = openedBy,
    closedBy = closedBy,
    status = status
)

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
