package id.azureenterprise.cassy.desktop

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import id.azureenterprise.cassy.inventory.application.InventoryService
import id.azureenterprise.cassy.inventory.data.InventoryRepository
import id.azureenterprise.cassy.inventory.db.InventoryDatabase
import id.azureenterprise.cassy.kernel.application.AccessService
import id.azureenterprise.cassy.kernel.application.BusinessDayService
import id.azureenterprise.cassy.kernel.application.ShiftService
import id.azureenterprise.cassy.kernel.data.KernelRepository
import id.azureenterprise.cassy.kernel.db.KernelDatabase
import id.azureenterprise.cassy.kernel.domain.PinHasher
import id.azureenterprise.cassy.masterdata.data.ProductLookupRepositoryImpl
import id.azureenterprise.cassy.masterdata.data.ProductRepository
import id.azureenterprise.cassy.masterdata.db.MasterDataDatabase
import id.azureenterprise.cassy.masterdata.domain.BarcodeNormalizer
import id.azureenterprise.cassy.masterdata.domain.ProductLookupUseCase
import id.azureenterprise.cassy.sales.application.SalesService
import id.azureenterprise.cassy.sales.data.SalesRepository
import id.azureenterprise.cassy.sales.db.SalesDatabase
import id.azureenterprise.cassy.sales.domain.PricingEngine
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DesktopAppControllerTest {

    @Test
    fun `bootstrap to catalog flow is reachable through guarded desktop stages`() {
        runBlocking {
            val kernelDriver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
            val masterDataDriver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
            val salesDriver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
            val inventoryDriver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
            KernelDatabase.Schema.create(kernelDriver)
            MasterDataDatabase.Schema.create(masterDataDriver)
            SalesDatabase.Schema.create(salesDriver)
            InventoryDatabase.Schema.create(inventoryDriver)

            val kernelRepository = KernelRepository(KernelDatabase(kernelDriver), EmptyCoroutineContext, Clock.System)
            val accessService = AccessService(kernelRepository, PinHasher(), Clock.System)
            val controller = DesktopAppController(
                accessService = accessService,
                businessDayService = BusinessDayService(kernelRepository, accessService),
                shiftService = ShiftService(kernelRepository, accessService),
                productRepository = ProductRepository(MasterDataDatabase(masterDataDriver), EmptyCoroutineContext),
                productLookupUseCase = ProductLookupUseCase(
                    ProductLookupRepositoryImpl(MasterDataDatabase(masterDataDriver), EmptyCoroutineContext),
                    BarcodeNormalizer()
                ),
                salesService = SalesService(
                    salesRepository = SalesRepository(SalesDatabase(salesDriver), EmptyCoroutineContext, Clock.System),
                    inventoryService = InventoryService(
                        InventoryRepository(
                            InventoryDatabase(inventoryDriver),
                            EmptyCoroutineContext,
                            Clock.System
                        ),
                        Clock.System
                    ),
                    kernelRepository = kernelRepository,
                    pricingEngine = PricingEngine(),
                    clock = Clock.System
                )
            )

            controller.load()
            assertEquals(DesktopStage.Bootstrap, controller.state.value.stage)

            controller.updateBootstrapField(BootstrapField.StoreName, "Pilot Store")
            controller.updateBootstrapField(BootstrapField.TerminalName, "POS-01")
            controller.updateBootstrapField(BootstrapField.CashierName, "Lina")
            controller.updateBootstrapField(BootstrapField.CashierPin, "123456")
            controller.updateBootstrapField(BootstrapField.SupervisorName, "Roni")
            controller.updateBootstrapField(BootstrapField.SupervisorPin, "654321")
            controller.bootstrapStore()
            assertEquals(DesktopStage.Login, controller.state.value.stage)

            val supervisorId = controller.state.value.login.operators.first { it.roleLabel == "SUPERVISOR" }.id
            controller.selectOperator(supervisorId)
            controller.updatePin("654321")
            controller.login()
            assertEquals(DesktopStage.OpenDay, controller.state.value.stage)

            controller.openBusinessDay()
            assertEquals(DesktopStage.StartShift, controller.state.value.stage)

            controller.updateOpeningCashInput("250.0")
            controller.startShift()
            assertEquals(DesktopStage.Catalog, controller.state.value.stage)
            assertTrue(controller.state.value.catalog.products.isNotEmpty())
        }
    }

    @Test
    fun `invalid bootstrap input stays in bootstrap stage with honest feedback`() {
        runBlocking {
            val kernelDriver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
            val masterDataDriver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
            val salesDriver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
            val inventoryDriver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
            KernelDatabase.Schema.create(kernelDriver)
            MasterDataDatabase.Schema.create(masterDataDriver)
            SalesDatabase.Schema.create(salesDriver)
            InventoryDatabase.Schema.create(inventoryDriver)

            val kernelRepository = KernelRepository(KernelDatabase(kernelDriver), EmptyCoroutineContext, Clock.System)
            val accessService = AccessService(kernelRepository, PinHasher(), Clock.System)
            val controller = DesktopAppController(
                accessService = accessService,
                businessDayService = BusinessDayService(kernelRepository, accessService),
                shiftService = ShiftService(kernelRepository, accessService),
                productRepository = ProductRepository(MasterDataDatabase(masterDataDriver), EmptyCoroutineContext),
                productLookupUseCase = ProductLookupUseCase(
                    ProductLookupRepositoryImpl(MasterDataDatabase(masterDataDriver), EmptyCoroutineContext),
                    BarcodeNormalizer()
                ),
                salesService = SalesService(
                    salesRepository = SalesRepository(SalesDatabase(salesDriver), EmptyCoroutineContext, Clock.System),
                    inventoryService = InventoryService(
                        InventoryRepository(
                            InventoryDatabase(inventoryDriver),
                            EmptyCoroutineContext,
                            Clock.System
                        ),
                        Clock.System
                    ),
                    kernelRepository = kernelRepository,
                    pricingEngine = PricingEngine(),
                    clock = Clock.System
                )
            )

            controller.load()
            controller.bootstrapStore()

            assertEquals(DesktopStage.Bootstrap, controller.state.value.stage)
            assertEquals("Nama toko wajib diisi", controller.state.value.banner?.message)
        }
    }

    @Test
    fun `wrong pin and lockout stay in login stage with honest feedback`() {
        runBlocking {
            val fixture = desktopFixture()
            val controller = fixture.newController()

            controller.load()
            controller.updateBootstrapField(BootstrapField.StoreName, "Pilot Store")
            controller.updateBootstrapField(BootstrapField.TerminalName, "POS-01")
            controller.updateBootstrapField(BootstrapField.CashierName, "Lina")
            controller.updateBootstrapField(BootstrapField.CashierPin, "123456")
            controller.updateBootstrapField(BootstrapField.SupervisorName, "Roni")
            controller.updateBootstrapField(BootstrapField.SupervisorPin, "654321")
            controller.bootstrapStore()

            val cashierId = controller.state.value.login.operators.first { it.roleLabel == "CASHIER" }.id
            controller.selectOperator(cashierId)
            controller.updatePin("000000")
            controller.login()
            assertEquals(DesktopStage.Login, controller.state.value.stage)
            assertEquals("PIN operator salah", controller.state.value.banner?.message)

            controller.updatePin("000000")
            controller.login()
            controller.updatePin("000000")
            controller.login()

            assertEquals(DesktopStage.Login, controller.state.value.stage)
            assertEquals("Akses operator terkunci sementara", controller.state.value.banner?.message)
            assertTrue(controller.state.value.login.feedback?.contains("Akses terkunci") == true)
        }
    }

    @Test
    fun `restored session resumes guarded stage honestly`() {
        runBlocking {
            val fixture = desktopFixture()
            val controller = fixture.newController()

            controller.load()
            controller.updateBootstrapField(BootstrapField.StoreName, "Pilot Store")
            controller.updateBootstrapField(BootstrapField.TerminalName, "POS-01")
            controller.updateBootstrapField(BootstrapField.CashierName, "Lina")
            controller.updateBootstrapField(BootstrapField.CashierPin, "123456")
            controller.updateBootstrapField(BootstrapField.SupervisorName, "Roni")
            controller.updateBootstrapField(BootstrapField.SupervisorPin, "654321")
            controller.bootstrapStore()

            val supervisorId = controller.state.value.login.operators.first { it.roleLabel == "SUPERVISOR" }.id
            controller.selectOperator(supervisorId)
            controller.updatePin("654321")
            controller.login()
            assertEquals(DesktopStage.OpenDay, controller.state.value.stage)

            val restoredOpenDay = fixture.newController()
            restoredOpenDay.load()
            assertEquals(DesktopStage.OpenDay, restoredOpenDay.state.value.stage)

            controller.openBusinessDay()
            controller.updateOpeningCashInput("250.0")
            controller.startShift()
            assertEquals(DesktopStage.Catalog, controller.state.value.stage)

            val restoredCatalog = fixture.newController()
            restoredCatalog.load()
            assertEquals(DesktopStage.Catalog, restoredCatalog.state.value.stage)
        }
    }

    private fun desktopFixture(): DesktopFixture {
        val kernelDriver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        val masterDataDriver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        val salesDriver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        val inventoryDriver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        KernelDatabase.Schema.create(kernelDriver)
        MasterDataDatabase.Schema.create(masterDataDriver)
        SalesDatabase.Schema.create(salesDriver)
        InventoryDatabase.Schema.create(inventoryDriver)

        val kernelRepository = KernelRepository(KernelDatabase(kernelDriver), EmptyCoroutineContext, Clock.System)
        val accessService = AccessService(kernelRepository, PinHasher(), Clock.System)
        val businessDayService = BusinessDayService(kernelRepository, accessService)
        val shiftService = ShiftService(kernelRepository, accessService)
        val productRepository = ProductRepository(MasterDataDatabase(masterDataDriver), EmptyCoroutineContext)
        val productLookupUseCase = ProductLookupUseCase(
            ProductLookupRepositoryImpl(MasterDataDatabase(masterDataDriver), EmptyCoroutineContext),
            BarcodeNormalizer()
        )
        val inventoryRepository = InventoryRepository(
            InventoryDatabase(inventoryDriver),
            EmptyCoroutineContext,
            Clock.System
        )
        val salesService = SalesService(
            salesRepository = SalesRepository(SalesDatabase(salesDriver), EmptyCoroutineContext, Clock.System),
            inventoryService = InventoryService(inventoryRepository, Clock.System),
            kernelRepository = kernelRepository,
            pricingEngine = PricingEngine(),
            clock = Clock.System
        )

        return DesktopFixture(
            accessService = accessService,
            businessDayService = businessDayService,
            shiftService = shiftService,
            productRepository = productRepository,
            productLookupUseCase = productLookupUseCase,
            salesService = salesService
        )
    }
}

private data class DesktopFixture(
    val accessService: AccessService,
    val businessDayService: BusinessDayService,
    val shiftService: ShiftService,
    val productRepository: ProductRepository,
    val productLookupUseCase: ProductLookupUseCase,
    val salesService: SalesService
) {
    fun newController(): DesktopAppController = DesktopAppController(
        accessService = accessService,
        businessDayService = businessDayService,
        shiftService = shiftService,
        productRepository = productRepository,
        productLookupUseCase = productLookupUseCase,
        salesService = salesService
    )
}
