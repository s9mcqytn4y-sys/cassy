package id.azureenterprise.cassy.di

import id.azureenterprise.cassy.masterdata.data.ProductLookupRepositoryImpl
import id.azureenterprise.cassy.masterdata.domain.BarcodeNormalizer
import id.azureenterprise.cassy.masterdata.domain.ProductLookupUseCase
import org.koin.dsl.module

val masterDataModule = module {
    single { BarcodeNormalizer() }
    single { ProductLookupRepositoryImpl(get(), get()) }
    single { ProductLookupUseCase(get(), get()) }
}
