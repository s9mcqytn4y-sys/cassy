package id.azureenterprise.cassy.kernel.di

import org.koin.dsl.module
import kotlinx.coroutines.Dispatchers
import id.azureenterprise.cassy.kernel.data.OutboxRepository
import id.azureenterprise.cassy.kernel.data.KernelRepository
import id.azureenterprise.cassy.kernel.application.AccessService
import id.azureenterprise.cassy.kernel.application.BusinessDayService
import id.azureenterprise.cassy.kernel.application.ShiftService
import id.azureenterprise.cassy.kernel.domain.PinHasher
import kotlinx.datetime.Clock
import kotlin.coroutines.CoroutineContext
import org.koin.core.module.Module

val kernelModule = module {
    single<CoroutineContext> { Dispatchers.Default }
    single<Clock> { Clock.System }
    single { PinHasher() }
    single { OutboxRepository(get(), get(), get()) }
    single { KernelRepository(get(), get(), get()) }
    single { AccessService(get(), get(), get()) }
    single { BusinessDayService(get(), get()) }
    single { ShiftService(get(), get()) }
}

expect val databaseModule: Module
