package id.azureenterprise.cassy.inventory.di

import id.azureenterprise.cassy.inventory.application.InventoryService
import id.azureenterprise.cassy.inventory.data.InventoryRepository
import org.koin.dsl.module
import org.koin.core.module.Module

val inventoryModule = module {
    single { InventoryRepository(get(), get(), get()) }
    single { InventoryService(get(), get()) }
}

expect val inventoryDatabaseModule: Module
