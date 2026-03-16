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
    fun `full business day and shift lifecycle closure foundation`() {
        runBlocking {
            val fixture = desktopFixture()
            val controller = fixture.newController()

            // 1. Setup to OpenDay stage
            controller.load()
            controller.updateBootstrapField(BootstrapField.StoreName, "Store")
            controller.updateBootstrapField(BootstrapField.TerminalName, "T1")
            controller.updateBootstrapField(BootstrapField.CashierName, "C1")
            controller.updateBootstrapField(BootstrapField.CashierPin, "111111")
            controller.updateBootstrapField(BootstrapField.SupervisorName, "S1")
            controller.updateBootstrapField(BootstrapField.SupervisorPin, "222222")
            controller.bootstrapStore()
            val supervisorId = controller.state.value.login.operators.first { it.roleLabel == "SUPERVISOR" }.id
            controller.selectOperator(supervisorId)
            controller.updatePin("222222")
            controller.login()
            assertEquals(DesktopStage.OpenDay, controller.state.value.stage)

            // 2. Open Day -> StartShift
            controller.openBusinessDay()
            assertEquals(DesktopStage.StartShift, controller.state.value.stage)

            // 3. Start Shift -> Catalog
            controller.updateOpeningCashInput("100.0")
            controller.startShift()
            assertEquals(DesktopStage.Catalog, controller.state.value.stage)

            // 4. End Shift -> Back to StartShift (for next shift)
            controller.updateClosingCashInput("150.0")
            controller.endShift()
            assertEquals(DesktopStage.StartShift, controller.state.value.stage)

            // 5. Close Day -> Back to OpenDay
            controller.closeBusinessDay()
            assertEquals(DesktopStage.OpenDay, controller.state.value.stage)
        }
    }

    @Test
    fun `cannot close business day while shift is active`() {
        runBlocking {
            val fixture = desktopFixture()
            val controller = fixture.newController()

            // Reach Catalog stage
            controller.load()
            controller.updateBootstrapField(BootstrapField.StoreName, "Store")
            controller.updateBootstrapField(BootstrapField.TerminalName, "T1")
            controller.updateBootstrapField(BootstrapField.CashierName, "C1")
            controller.updateBootstrapField(BootstrapField.CashierPin, "111111")
            controller.updateBootstrapField(BootstrapField.SupervisorName, "S1")
            controller.updateBootstrapField(BootstrapField.SupervisorPin, "222222")
            controller.bootstrapStore()
            val supervisorId = controller.state.value.login.operators.first { it.roleLabel == "SUPERVISOR" }.id
            controller.selectOperator(supervisorId)
            controller.updatePin("222222")
            controller.login()
            controller.openBusinessDay()
            controller.updateOpeningCashInput("100.0")
            controller.startShift()

            assertEquals(DesktopStage.Catalog, controller.state.value.stage)

            // Try to close day without ending shift
            controller.closeBusinessDay()

            // Should remain in Catalog with error banner
            assertEquals(DesktopStage.Catalog, controller.state.value.stage)
            assertEquals("Shift aktif harus ditutup sebelum close day", controller.state.value.banner?.message)
        }
    }

    @Test
    fun `invalid cash inputs are honestly reported`() {
        runBlocking {
            val fixture = desktopFixture()
            val controller = fixture.newController()

            // Setup to StartShift
            controller.load()
            controller.updateBootstrapField(BootstrapField.StoreName, "S")
            controller.updateBootstrapField(BootstrapField.TerminalName, "T")
            controller.updateBootstrapField(BootstrapField.CashierName, "C")
            controller.updateBootstrapField(BootstrapField.CashierPin, "111111")
            controller.updateBootstrapField(BootstrapField.SupervisorName, "S")
            controller.updateBootstrapField(BootstrapField.SupervisorPin, "222222")
            controller.bootstrapStore()
            controller.selectOperator(controller.state.value.login.operators.first { it.roleLabel == "SUPERVISOR" }.id)
            controller.updatePin("222222")
            controller.login()
            controller.openBusinessDay()

            // Invalid format
            controller.updateOpeningCashInput("abc")
            controller.startShift()
            assertEquals("Opening cash harus berupa angka", controller.state.value.banner?.message)

            // Negative amount
            controller.updateOpeningCashInput("-50.0")
            controller.startShift()
            assertEquals("Opening cash tidak boleh negatif", controller.state.value.banner?.message)
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
