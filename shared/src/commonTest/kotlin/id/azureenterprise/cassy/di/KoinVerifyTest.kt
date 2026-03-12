package id.azureenterprise.cassy.di

import org.koin.test.KoinTest
import org.koin.test.verify.verify
import kotlin.test.Test

class KoinVerifyTest : KoinTest {

    @Test
    fun verifyKoinModules() {
        // We verify the aggregation of all modules.
        // Note: databaseModule is an 'expect' val,
        // in commonTest it will check the definition structure.
        kernelModule.verify()
        id.azureenterprise.cassy.masterdata.di.masterDataModule.verify()
        id.azureenterprise.cassy.sales.di.salesModule.verify()
        catalogModule.verify()
    }
}
