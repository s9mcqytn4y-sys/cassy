package id.azureenterprise.cassy.kernel.di

import org.koin.dsl.module
import kotlinx.coroutines.Dispatchers
import id.azureenterprise.cassy.kernel.data.OutboxRepository
import id.azureenterprise.cassy.kernel.data.KernelRepository
import id.azureenterprise.cassy.kernel.application.AccessService
import id.azureenterprise.cassy.kernel.application.BusinessDayService
import id.azureenterprise.cassy.kernel.application.CashControlService
import id.azureenterprise.cassy.kernel.application.OperationalControlService
import id.azureenterprise.cassy.kernel.application.ShiftService
import id.azureenterprise.cassy.kernel.application.ShiftClosingService
import id.azureenterprise.cassy.kernel.domain.CashMovementPolicy
import id.azureenterprise.cassy.kernel.domain.OpeningCashPolicy
import id.azureenterprise.cassy.kernel.domain.PinHasher
import id.azureenterprise.cassy.kernel.domain.ShiftClosePolicy
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
    single { OpeningCashPolicy() }
    single { CashMovementPolicy() }
    single { ShiftClosePolicy() }
    single { BusinessDayService(get(), get()) }
    single { ShiftService(get(), get(), get()) }
    single { CashControlService(get(), get(), get()) }
    single { ShiftClosingService(get(), get(), get(), get()) }
    single { OperationalControlService(get(), get(), get(), get(), get()) }
}

expect val databaseModule: Module
