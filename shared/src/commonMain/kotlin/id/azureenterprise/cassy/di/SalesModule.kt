package id.azureenterprise.cassy.di

import id.azureenterprise.cassy.sales.application.SalesService
import id.azureenterprise.cassy.sales.data.SalesRepository
import id.azureenterprise.cassy.sales.domain.PricingEngine
import id.azureenterprise.cassy.sales.presentation.SalesViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val salesModule = module {
    single { SalesRepository(get(), get(), get()) }
    single { PricingEngine() }
    single { SalesService(get(), get(), get(), "terminal_01") }

    factory {
        SalesViewModel(
            get(),
            get(),
            CoroutineScope(SupervisorJob() + Dispatchers.Main)
        )
    }
}
