package id.azureenterprise.cassy.kernel.application

import id.azureenterprise.cassy.kernel.domain.ShiftSalesSummary

interface OperationalSalesPort {
    suspend fun getShiftSalesSummary(shiftId: String): ShiftSalesSummary
}

object NoopOperationalSalesPort : OperationalSalesPort {
    override suspend fun getShiftSalesSummary(shiftId: String): ShiftSalesSummary {
        return ShiftSalesSummary()
    }
}
