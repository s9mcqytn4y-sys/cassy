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

/**
 * Main entry point for Koin DI.
 * Aggregates all feature modules and kernel infrastructure.
 */
fun initKoin(appDeclaration: KoinAppDeclaration = {}) =
    startKoin {
        appDeclaration()
        modules(
            kernelModule,            // Infrastructure (Dispatchers, Clock, Outbox)
            databaseModule,          // Kernel Database (Audit, Outbox, Day/Shift)
            masterDataModule,        // Product & Metadata
            masterDataDatabaseModule, // Product DB
            salesModule,             // Transaction & Pricing
            salesDatabaseModule,      // Sales DB
            salesPlatformModule,      // Sales -> Kernel bridge
            inventoryModule,         // Inventory Domain & Data
            inventoryDatabaseModule, // Inventory DB
            catalogModule            // Presentation & ViewModels
        )
    }

fun initKoin() = initKoin {}
