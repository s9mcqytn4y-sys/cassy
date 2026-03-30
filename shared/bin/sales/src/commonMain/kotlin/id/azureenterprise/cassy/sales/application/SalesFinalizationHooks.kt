package id.azureenterprise.cassy.sales.application

interface SalesFinalizationHooks {
    suspend fun afterBundlePrepared(saleId: String) = Unit

    suspend fun afterInventoryApplied(saleId: String) = Unit

    suspend fun afterKernelApplied(saleId: String) = Unit
}

object NoopSalesFinalizationHooks : SalesFinalizationHooks
