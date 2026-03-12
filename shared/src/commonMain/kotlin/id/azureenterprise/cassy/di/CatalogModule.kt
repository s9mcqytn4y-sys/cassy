package id.azureenterprise.cassy.di

import id.azureenterprise.cassy.masterdata.data.ProductRepository
import id.azureenterprise.cassy.ui.CatalogViewModel
import org.koin.dsl.module
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.Dispatchers

val catalogModule = module {
    // ProductRepository is now provided by masterDataModule in :shared:masterdata
    // We only need to provide the ViewModel here
    factory { CatalogViewModel(get(), CoroutineScope(SupervisorJob() + Dispatchers.Main)) }
}
