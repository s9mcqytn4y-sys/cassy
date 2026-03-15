package id.azureenterprise.cassy.di

import id.azureenterprise.cassy.masterdata.data.ProductRepository
import id.azureenterprise.cassy.ui.CatalogViewModel
import org.koin.dsl.module
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.Dispatchers

val catalogModule = module {
    single<CoroutineScope> { CoroutineScope(SupervisorJob() + Dispatchers.Main) }
    factory { CatalogViewModel(get(), get()) }
}
