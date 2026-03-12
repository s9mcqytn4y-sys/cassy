package id.azureenterprise.cassy.di

import org.koin.dsl.module
import kotlinx.coroutines.Dispatchers
import id.azureenterprise.cassy.data.OutboxRepository
import kotlinx.datetime.Clock
import kotlin.coroutines.CoroutineContext
import org.koin.core.module.Module

val kernelModule = module {
    single<CoroutineContext> { Dispatchers.Default }
    single<Clock> { Clock.System }
    single { OutboxRepository(get(), get(), get()) }
}

expect val databaseModule: Module
