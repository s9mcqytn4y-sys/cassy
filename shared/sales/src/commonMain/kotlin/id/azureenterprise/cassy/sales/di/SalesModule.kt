package id.azureenterprise.cassy.sales.di

import id.azureenterprise.cassy.sales.application.SalesService
import id.azureenterprise.cassy.sales.data.SalesRepository
import id.azureenterprise.cassy.sales.domain.PricingEngine
import org.koin.dsl.module

val salesModule = module {
    single { SalesRepository(get(), get(), get()) }
    single { PricingEngine() }
    single { SalesService(get(), get(), get(), "terminal_01") }
}
