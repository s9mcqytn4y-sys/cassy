package id.azureenterprise.cassy.di

import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration

fun initKoin(appDeclaration: KoinAppDeclaration = {}) =
    startKoin {
        appDeclaration()
        modules(
            coreModule,
            databaseModule,
            catalogModule,
            masterDataModule,
            salesModule
        )
    }

// called by Desktop
fun initKoin() = initKoin {}

expect val databaseModule: Module
