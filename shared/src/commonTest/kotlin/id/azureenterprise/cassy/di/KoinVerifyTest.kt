package id.azureenterprise.cassy.di

import id.azureenterprise.cassy.kernel.di.kernelModule
import id.azureenterprise.cassy.kernel.di.databaseModule
import id.azureenterprise.cassy.kernel.application.NoopOperationalSalesPort
import id.azureenterprise.cassy.kernel.application.OperationalSalesPort
import id.azureenterprise.cassy.masterdata.di.masterDataModule
import id.azureenterprise.cassy.masterdata.di.masterDataDatabaseModule
import id.azureenterprise.cassy.sales.di.salesModule
import id.azureenterprise.cassy.sales.di.salesDatabaseModule
import id.azureenterprise.cassy.sales.di.salesPlatformModule
import id.azureenterprise.cassy.inventory.di.inventoryModule
import id.azureenterprise.cassy.inventory.di.inventoryDatabaseModule
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.verify.verify
import kotlin.test.Test

class KoinVerifyTest : KoinTest {

    @OptIn(KoinExperimentalAPI::class)
    @Test
    fun verifyKoinModules() {
        module {
            includes(
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
            single<OperationalSalesPort> { NoopOperationalSalesPort }
        }.verify()
    }
}
