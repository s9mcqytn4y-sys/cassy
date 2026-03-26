package id.azureenterprise.cassy.sales.di

import id.azureenterprise.cassy.sales.application.LocalPaymentGatewayStub
import id.azureenterprise.cassy.sales.application.NoopSalesFinalizationHooks
import id.azureenterprise.cassy.sales.application.PaymentGatewayPort
import id.azureenterprise.cassy.sales.application.SalesFinalizationHooks
import id.azureenterprise.cassy.sales.application.SalesService
import id.azureenterprise.cassy.sales.data.SalesRepository
import id.azureenterprise.cassy.sales.domain.PricingEngine
import org.koin.core.module.Module
import org.koin.dsl.module
import kotlinx.serialization.json.Json

val salesModule = module {
    single<Json> { Json }
    single { SalesRepository(get(), get(), get(), get()) }
    single { PricingEngine() }
    single<PaymentGatewayPort> { LocalPaymentGatewayStub() }
    single<SalesFinalizationHooks> { NoopSalesFinalizationHooks }
    single {
        SalesService(
            salesRepository = get(),
            inventoryService = get(),
            kernelPort = get(),
            paymentGatewayPort = get(),
            pricingEngine = get(),
            productLookupUseCase = get(),
            clock = get(),
            finalizationHooks = get()
        )
    }
}

expect val salesDatabaseModule: Module
expect val salesPlatformModule: Module
