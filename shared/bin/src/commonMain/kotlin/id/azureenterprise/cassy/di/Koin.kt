package id.azureenterprise.cassy.di

import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import id.azureenterprise.cassy.kernel.di.kernelModule
import id.azureenterprise.cassy.kernel.di.databaseModule
import id.azureenterprise.cassy.masterdata.di.masterDataModule
import id.azureenterprise.cassy.masterdata.di.masterDataDatabaseModule
import id.azureenterprise.cassy.sales.di.salesModule
import id.azureenterprise.cassy.sales.di.salesDatabaseModule
import id.azureenterprise.cassy.sales.di.salesPlatformModule
import id.azureenterprise.cassy.inventory.di.inventoryModule
import id.azureenterprise.cassy.inventory.di.inventoryDatabaseModule

fun initKoin(appDeclaration: KoinAppDeclaration = {}) =
    startKoin {
        appDeclaration()
        modules(
            kernelModule,
            databaseModule,
            masterDataModule,
            masterDataDatabaseModule,
            salesModule,
            salesDatabaseModule,
            salesPlatformModule,
            inventoryModule,
            inventoryDatabaseModule
        )
    }

fun initKoin() = initKoin {}
