package id.azureenterprise.cassy.kernel.application

import id.azureenterprise.cassy.kernel.domain.ShiftSalesSummary
import id.azureenterprise.cassy.kernel.domain.VoidSalesSummary

interface OperationalSalesPort {
    suspend fun getShiftSalesSummary(shiftId: String): ShiftSalesSummary
    suspend fun getMultiShiftSalesSummary(shiftIds: List<String>): ShiftSalesSummary
    suspend fun getShiftVoidSummary(shiftId: String): VoidSalesSummary
    suspend fun getMultiShiftVoidSummary(shiftIds: List<String>): VoidSalesSummary
}

object NoopOperationalSalesPort : OperationalSalesPort {
    override suspend fun getShiftSalesSummary(shiftId: String): ShiftSalesSummary {
        return ShiftSalesSummary()
    }

    override suspend fun getMultiShiftSalesSummary(shiftIds: List<String>): ShiftSalesSummary {
        return ShiftSalesSummary()
    }

    override suspend fun getShiftVoidSummary(shiftId: String): VoidSalesSummary {
        return VoidSalesSummary()
    }

    override suspend fun getMultiShiftVoidSummary(shiftIds: List<String>): VoidSalesSummary {
        return VoidSalesSummary()
    }
}
