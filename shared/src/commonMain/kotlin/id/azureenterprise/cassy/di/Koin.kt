package id.azureenterprise.cassy.di

import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import id.azureenterprise.cassy.masterdata.di.masterDataModule
import id.azureenterprise.cassy.sales.di.salesModule

fun initKoin(appDeclaration: KoinAppDeclaration = {}) =
    startKoin {
        appDeclaration()
        modules(
            coreModule,       // From :shared:kernel
            databaseModule,   // From :shared:kernel (platform specific)
            catalogModule,    // From :shared
            masterDataModule, // From :shared:masterdata
            salesModule       // From :shared:sales
        )
    }

fun initKoin() = initKoin {}
