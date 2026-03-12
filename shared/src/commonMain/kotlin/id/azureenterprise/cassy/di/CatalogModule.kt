package id.azureenterprise.cassy.di

import id.azureenterprise.cassy.data.ProductRepository
import id.azureenterprise.cassy.ui.CatalogViewModel
import org.koin.dsl.module
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.Dispatchers

val catalogModule = module {
    single { ProductRepository(get(), get(), get()) }

    // For simplicity in this reference implementation, we provide a global scope
    // In a real app, this might be tied to the platform lifecycle
    factory { CatalogViewModel(get(), CoroutineScope(SupervisorJob() + Dispatchers.Main)) }
}
