package id.azureenterprise.cassy.di

import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import id.azureenterprise.cassy.masterdata.di.masterDataModule
import id.azureenterprise.cassy.sales.di.salesModule

/**
 * Main entry point for Koin DI.
 * Aggregates all feature modules and kernel infrastructure.
 */
fun initKoin(appDeclaration: KoinAppDeclaration = {}) =
    startKoin {
        appDeclaration()
        modules(
            kernelModule,     // Infrastructure (Dispatchers, Clock, Outbox)
            databaseModule,   // Platform-specific database (Android/Desktop)
            masterDataModule, // Product & Inventory
            salesModule,      // Transaction & Pricing
            catalogModule     // Presentation & ViewModels
        )
    }

fun initKoin() = initKoin {}
