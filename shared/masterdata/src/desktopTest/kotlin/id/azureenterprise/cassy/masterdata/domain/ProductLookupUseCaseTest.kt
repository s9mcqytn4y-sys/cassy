package id.azureenterprise.cassy.masterdata.domain

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import id.azureenterprise.cassy.masterdata.data.ProductLookupRepositoryImpl
import id.azureenterprise.cassy.masterdata.db.MasterDataDatabase
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.Test
import kotlin.test.assertIs

class ProductLookupUseCaseTest {

    @Test
    fun `barcode and sku lookup share the same contract`() {
        runBlocking {
            val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
            MasterDataDatabase.Schema.create(driver)
            val repository = ProductLookupRepositoryImpl(MasterDataDatabase(driver), EmptyCoroutineContext)
            val useCase = ProductLookupUseCase(repository, BarcodeNormalizer())

            assertIs<ProductLookupResult.FoundSingle>(useCase.execute("8996001600033"))
            assertIs<ProductLookupResult.FoundSingle>(useCase.execute("SKU-OFC-005"))
            assertIs<ProductLookupResult.InvalidInput>(useCase.execute(" "))
            assertIs<ProductLookupResult.NotFound>(useCase.execute("UNREGISTERED"))
        }
    }
}
