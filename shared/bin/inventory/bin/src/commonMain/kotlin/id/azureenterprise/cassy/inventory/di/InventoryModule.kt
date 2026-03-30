package id.azureenterprise.cassy.inventory.di

import id.azureenterprise.cassy.inventory.application.InventoryService
import id.azureenterprise.cassy.inventory.application.InventoryVoidImpactPolicy
import id.azureenterprise.cassy.inventory.data.InventoryRepository
import id.azureenterprise.cassy.inventory.domain.InventoryAdjustmentPolicy
import id.azureenterprise.cassy.inventory.domain.InventoryApprovalMode
import org.koin.core.module.Module
import org.koin.dsl.module

val inventoryModule = module {
    single { InventoryRepository(get(), get(), get()) }
    single { InventoryVoidImpactPolicy() }
    single<InventoryApprovalMode> { InventoryApprovalMode.LIGHT_PIN }
    single { InventoryAdjustmentPolicy() }
    single { InventoryService(get(), get(), get(), get(), get(), get()) }
}

expect val inventoryDatabaseModule: Module
