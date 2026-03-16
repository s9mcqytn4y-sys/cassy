package id.azureenterprise.cassy.sales.application

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import id.azureenterprise.cassy.inventory.application.InventoryService
import id.azureenterprise.cassy.inventory.data.InventoryRepository
import id.azureenterprise.cassy.inventory.db.InventoryDatabase
import id.azureenterprise.cassy.kernel.data.KernelRepository
import id.azureenterprise.cassy.kernel.db.KernelDatabase
import id.azureenterprise.cassy.masterdata.data.ProductLookupRepositoryImpl
import id.azureenterprise.cassy.masterdata.db.MasterDataDatabase
import id.azureenterprise.cassy.masterdata.domain.BarcodeNormalizer
import id.azureenterprise.cassy.masterdata.domain.Product
import id.azureenterprise.cassy.masterdata.domain.ProductLookupUseCase
import id.azureenterprise.cassy.sales.data.SalesRepository
import id.azureenterprise.cassy.sales.db.SalesDatabase
import id.azureenterprise.cassy.sales.domain.PricingEngine
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SalesServiceTest {

    @Test
    fun `cart mutation is blocked without active day and shift`() {
        runBlocking {
            val fixture = salesFixture()

            val result = fixture.service.addProduct(sampleProduct())

            assertTrue(result.isFailure)
        }
    }

    @Test
    fun `pricing totals stay consistent when quantity changes`() {
        runBlocking {
            val fixture = salesFixture()
            fixture.kernelRepository.upsertTerminalBinding(fixture.binding)
            fixture.kernelRepository.openBusinessDay("bd_1")
            fixture.kernelRepository.openShift(
                id = "shift_1",
                businessDayId = "bd_1",
                terminalId = fixture.binding.terminalId,
                openingCash = 100.0,
                openedBy = "operator_1"
            )

            fixture.service.addProduct(sampleProduct(), quantity = 2.0).getOrThrow()
            fixture.service.setQuantity("product_1", 3.0).getOrThrow()

            val basket = fixture.service.basket.value
            assertEquals(30.0, basket.totals.subtotal)
            assertEquals(30.0, basket.totals.finalTotal)
        }
    }

    @Test
    fun `checkout records stock through inventory owner boundary`() {
        runBlocking {
            val fixture = salesFixture()
            fixture.kernelRepository.upsertTerminalBinding(fixture.binding)
            fixture.kernelRepository.openBusinessDay("bd_1")
            fixture.kernelRepository.openShift(
                id = "shift_1",
                businessDayId = "bd_1",
                terminalId = fixture.binding.terminalId,
                openingCash = 100.0,
                openedBy = "operator_1"
            )

            fixture.service.addProduct(sampleProduct(), quantity = 2.0).getOrThrow()
            val checkout = fixture.service.checkout("CASH")

            assertTrue(checkout.isSuccess)
            assertEquals(-2.0, fixture.inventoryRepository.getStockLevel("product_1"))
            assertEquals(1, fixture.inventoryRepository.getLedgerByProduct("product_1").size)
        }
    }

    private fun salesFixture(): SalesFixture {
        val kernelDriver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        val salesDriver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        val inventoryDriver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        val masterDataDriver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)

        KernelDatabase.Schema.create(kernelDriver)
        SalesDatabase.Schema.create(salesDriver)
        InventoryDatabase.Schema.create(inventoryDriver)
        MasterDataDatabase.Schema.create(masterDataDriver)

        val kernelRepository = KernelRepository(KernelDatabase(kernelDriver), EmptyCoroutineContext, Clock.System)
        val inventoryRepository = InventoryRepository(
            InventoryDatabase(inventoryDriver),
            EmptyCoroutineContext,
            Clock.System
        )

        val productLookupUseCase = ProductLookupUseCase(
            repository = ProductLookupRepositoryImpl(MasterDataDatabase(masterDataDriver), EmptyCoroutineContext),
            normalizer = BarcodeNormalizer()
        )

        return SalesFixture(
            service = SalesService(
                salesRepository = SalesRepository(SalesDatabase(salesDriver), EmptyCoroutineContext, Clock.System),
                inventoryService = InventoryService(inventoryRepository, Clock.System),
                kernelRepository = kernelRepository,
                pricingEngine = PricingEngine(),
                productLookupUseCase = productLookupUseCase,
                clock = Clock.System
            ),
            kernelRepository = kernelRepository,
            inventoryRepository = inventoryRepository,
            binding = id.azureenterprise.cassy.kernel.domain.TerminalBinding(
                storeId = "store_1",
                storeName = "Toko Test",
                terminalId = "terminal_1",
                terminalName = "Kasir Test",
                boundAt = Clock.System.now()
            )
        )
    }

    private fun sampleProduct(): Product = Product(
        id = "product_1",
        name = "Produk Uji",
        price = 10.0,
        categoryId = "cat_1",
        sku = "SKU-TEST-001"
    )
}

private data class SalesFixture(
    val service: SalesService,
    val kernelRepository: KernelRepository,
    val inventoryRepository: InventoryRepository,
    val binding: id.azureenterprise.cassy.kernel.domain.TerminalBinding
)
