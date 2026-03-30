package id.azureenterprise.cassy.masterdata.di

import id.azureenterprise.cassy.masterdata.data.ProductLookupRepositoryImpl
import id.azureenterprise.cassy.masterdata.data.ProductRepository
import id.azureenterprise.cassy.masterdata.domain.BarcodeNormalizer
import id.azureenterprise.cassy.masterdata.domain.ProductLookupUseCase
import org.koin.dsl.module
import org.koin.core.module.Module
import kotlin.coroutines.CoroutineContext

val masterDataModule = module {
    single { BarcodeNormalizer() }
    single { ProductLookupRepositoryImpl(get(), get<CoroutineContext>()) }
    single { ProductLookupUseCase(get(), get()) }
    single { ProductRepository(get(), get<CoroutineContext>()) }
}

expect val masterDataDatabaseModule: Module
