@file:Suppress("WildcardImport")

package id.azureenterprise.cassy.desktop

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import id.azureenterprise.cassy.inventory.application.InventoryService
import id.azureenterprise.cassy.inventory.application.InventoryVoidImpactPolicy
import id.azureenterprise.cassy.inventory.data.InventoryRepository
import id.azureenterprise.cassy.inventory.db.InventoryDatabase
import id.azureenterprise.cassy.kernel.application.*
import id.azureenterprise.cassy.kernel.data.KernelRepository
import id.azureenterprise.cassy.kernel.data.OutboxRepository
import id.azureenterprise.cassy.kernel.db.KernelDatabase
import id.azureenterprise.cassy.kernel.domain.*
import id.azureenterprise.cassy.masterdata.data.ProductLookupRepositoryImpl
import id.azureenterprise.cassy.masterdata.data.ProductRepository
import id.azureenterprise.cassy.masterdata.db.MasterDataDatabase
import id.azureenterprise.cassy.masterdata.domain.BarcodeNormalizer
import id.azureenterprise.cassy.masterdata.domain.Product
import id.azureenterprise.cassy.masterdata.domain.ProductLookupUseCase
import id.azureenterprise.cassy.sales.application.SalesService
import id.azureenterprise.cassy.sales.application.VoidSaleService
import id.azureenterprise.cassy.sales.data.SalesRepository
import id.azureenterprise.cassy.sales.db.SalesDatabase
import id.azureenterprise.cassy.sales.domain.PricingEngine
import id.azureenterprise.cassy.sales.domain.ReceiptPrintPayload
import id.azureenterprise.cassy.sales.domain.SaleStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import java.nio.file.Files
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.*

class DesktopAppControllerTest {

    @Test
    fun initial_state_is_Loading(): Unit = runBlocking {
        val fixture = desktopFixture()
        val controller = fixture.newController()

        assertEquals(DesktopStage.Loading, controller.state.value.stage)
    }

    @Test
    fun load_switches_to_Bootstrap_when_no_terminal_binding_exists(): Unit = runBlocking {
        val fixture = desktopFixture()
        val controller = fixture.newController()

        controller.load()

        assertEquals(DesktopStage.Bootstrap, controller.state.value.stage)
    }

    @Test
    fun load_switches_to_Login_when_terminal_binding_exists(): Unit = runBlocking {
        val fixture = desktopFixture()
        fixture.kernelRepository.upsertTerminalBinding(
            TerminalBinding(
                storeId = "S1",
                storeName = "Store 1",
                terminalId = "T1",
                terminalName = "Term 1",
                boundAt = Clock.System.now()
            )
        )
        fixture.insertOperator("O1", "Cashier", "123456", OperatorRole.CASHIER)

        val controller = fixture.newController()
        controller.load()

        assertEquals(DesktopStage.Login, controller.state.value.stage)
        assertEquals(1, controller.state.value.login.operators.size)
    }

    @Test
    fun login_with_valid_PIN_switches_to_Dashboard(): Unit = runBlocking {
        val fixture = desktopFixture()
        fixture.kernelRepository.upsertTerminalBinding(
            TerminalBinding("S1", "Store 1", "T1", "Term 1", Clock.System.now())
        )
        fixture.insertOperator("O1", "Cashier", "123456", OperatorRole.CASHIER)

        val controller = fixture.newController()
        controller.load()
        controller.selectOperator("O1")
        controller.updatePin("123456")
        controller.login()

        val stage = controller.state.value.stage
        assertEquals(DesktopStage.Workspace, stage)
        assertNotNull(controller.state.value.shell.operatorName)
    }

    @Test
    fun dashboard_allows_opening_business_day(): Unit = runBlocking {
        val fixture = desktopFixture()
        fixture.loginAsSupervisor()

        val controller = fixture.newController()
        controller.load()
        controller.openBusinessDay()

        val dashboard = controller.state.value.operations.dashboard
        assertTrue(
            dashboard.decisions.any {
                it.type == OperationType.OPEN_BUSINESS_DAY &&
                    it.status == OperationStatus.COMPLETED
            }
        )
    }

    @Test
    fun dashboard_allows_opening_shift_when_business_day_is_open(): Unit = runBlocking {
        val fixture = desktopFixture()
        fixture.loginAsSupervisor()
        fixture.businessDayService.openNewDay()

        val controller = fixture.newController()
        controller.load()
        controller.updateOpeningCashInput("100000")
        controller.startShift()

        val dashboard = controller.state.value.operations.dashboard
        assertTrue(
            dashboard.decisions.any {
                it.type == OperationType.START_SHIFT &&
                    it.status == OperationStatus.COMPLETED
            }
        )
        assertEquals(DesktopStage.Workspace, controller.state.value.stage)
    }

    @Test
    fun sale_stage_allows_adding_products_by_barcode(): Unit = runBlocking {
        val fixture = desktopFixture()
        fixture.loginAsSupervisor()
        fixture.businessDayService.openNewDay()
        fixture.openShift()

        // Insert sample product
        fixture.masterDataDatabase.masterDataDatabaseQueries.insertProduct(
            id = "P1",
            categoryId = "cat_1",
            name = "Product 1",
            price = 5000.0,
            sku = "SKU1",
            imageUrl = null,
            isActive = true
        )
        fixture.masterDataDatabase.masterDataDatabaseQueries.insertBarcode(
            barcode = "123456",
            productId = "P1",
            type = "GLOBAL"
        )

        val controller = fixture.newController()
        controller.load()

        controller.updateBarcodeInput("123456")
        controller.scanBarcodeOrSku()

        val basket = controller.state.value.catalog.basket
        assertEquals(1, basket.items.size)
        assertEquals("Product 1", basket.items.first().product.name)
        assertEquals(5000.0, basket.totals.finalTotal)
    }

    @Test
    fun checkout_completion_moves_to_result_stage(): Unit = runBlocking {
        val fixture = desktopFixture()
        fixture.loginAsSupervisor()
        fixture.businessDayService.openNewDay()
        fixture.openShift()

        // Add product to basket
        fixture.salesService.addProduct(
            Product("P1", "P1", 5000.0, "C1", "SKU1"),
            1.0
        )

        val controller = fixture.newController()
        controller.load()

        controller.updateCashReceivedInput("5000")
        controller.checkoutCash()

        assertNotNull(controller.state.value.catalog.lastFinalizedSaleId)
    }

    @Test
    fun export_operational_report_writes_export_path_into_state(): Unit = runBlocking {
        val fixture = desktopFixture()
        fixture.loginAsSupervisor()
        fixture.businessDayService.openNewDay()
        fixture.openShift()

        val controller = fixture.newController()
        controller.load()
        controller.exportOperationalReport()

        assertNotNull(controller.state.value.operations.reportingExportPath)
    }

    @Test
    fun beta_operational_flow_can_checkout_void_and_export_report(): Unit = runBlocking {
        val fixture = desktopFixture()
        fixture.loginAsSupervisor()
        fixture.businessDayService.openNewDay()
        fixture.openShift()

        val controller = fixture.newController()
        controller.load()
        controller.addProduct(Product("P1", "Produk Beta", 10.0, "C1", "SKU-BETA-1"))
        controller.updateCashReceivedInput("10")
        controller.checkoutCash()

        val recentSale = controller.state.value.catalog.recentSales.firstOrNull()
        assertNotNull(recentSale)

        controller.selectVoidSale(recentSale.saleId)
        controller.updateVoidReasonCode("VOID_DUPLICATE_INPUT")
        controller.updateVoidReasonDetail("Smoke beta duplicate input")
        controller.updateVoidInventoryFollowUpNote("Follow-up fisik smoke beta")
        controller.executeVoidSale()
        controller.exportOperationalReport()

        assertNotNull(controller.state.value.operations.reportingExportPath)
        assertEquals(SaleStatus.VOIDED, controller.state.value.catalog.recentSales.first().saleStatus)
    }

    @Suppress("LongMethod")
    private fun desktopFixture(): DesktopFixture {
        val kernelDriver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        val salesDriver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        val inventoryDriver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        val masterDataDriver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)

        KernelDatabase.Schema.create(kernelDriver)
        SalesDatabase.Schema.create(salesDriver)
        InventoryDatabase.Schema.create(inventoryDriver)
        MasterDataDatabase.Schema.create(masterDataDriver)

        val kernelRepo = KernelRepository(
            KernelDatabase(kernelDriver),
            EmptyCoroutineContext,
            Clock.System
        )
        val outboxRepo = OutboxRepository(
            KernelDatabase(kernelDriver),
            EmptyCoroutineContext,
            Clock.System
        )
        val accessService = AccessService(kernelRepo, PinHasher(), Clock.System)
        val storeProfileService = StoreProfileService(kernelRepo, Clock.System)
        val businessDayService = BusinessDayService(kernelRepo, accessService)
        val shiftService = ShiftService(kernelRepo, accessService, OpeningCashPolicy())
        val cashControlService = CashControlService(kernelRepo, accessService, CashMovementPolicy())
        val shiftClosingService = ShiftClosingService(
            kernelRepo,
            accessService,
            NoopOperationalSalesPort,
            ShiftClosePolicy()
        )

        val inventoryRepo = InventoryRepository(
            InventoryDatabase(inventoryDriver),
            EmptyCoroutineContext,
            Clock.System
        )
        val inventoryService = InventoryService(
            inventoryRepo,
            accessService,
            kernelRepo,
            InventoryVoidImpactPolicy(),
            Clock.System
        )

        val masterDataDb = MasterDataDatabase(masterDataDriver)
        val productRepo = ProductRepository(masterDataDb, EmptyCoroutineContext)
        val productLookupUseCase = ProductLookupUseCase(
            ProductLookupRepositoryImpl(masterDataDb, EmptyCoroutineContext),
            BarcodeNormalizer()
        )

        val salesRepo = SalesRepository(
            SalesDatabase(salesDriver),
            EmptyCoroutineContext,
            Clock.System
        )
        val salesService = SalesService(
            salesRepository = salesRepo,
            inventoryService = inventoryService,
            kernelPort = KernelRepositorySalesKernelPort(kernelRepo),
            paymentGatewayPort = id.azureenterprise.cassy.sales.application.LocalPaymentGatewayStub(),
            pricingEngine = PricingEngine(),
            productLookupUseCase = productLookupUseCase,
            clock = Clock.System
        )
        val voidSaleService = VoidSaleService(
            salesRepository = salesRepo,
            kernelRepository = kernelRepo,
            accessService = accessService,
            cashControlService = cashControlService,
            inventoryService = inventoryService
        )

        val operationalSalesPort = DesktopTestOperationalSalesPort(salesService)
        val reportingQueryFacade = ReportingQueryFacade(
            kernelRepo,
            outboxRepo,
            operationalSalesPort,
            NoopOperationalHardwarePort,
            Clock.System,
            TimeZone.currentSystemDefault()
        )
        val syncReplayService = SyncReplayService(
            outboxRepo,
            SyncVisibilityService(kernelRepo, outboxRepo, Clock.System),
            NoopSyncReplayPort,
            Clock.System
        )
        val reportingExporter = DesktopReportingExporter(
            clock = Clock.System,
            exportRootProvider = { Files.createTempDirectory("cassy-report-export-test") }
        )

        val operationalControlService = OperationalControlService(
            accessService,
            businessDayService,
            shiftService,
            cashControlService,
            shiftClosingService
        )

        return DesktopFixture(
            kernelRepository = kernelRepo,
            accessService = accessService,
            businessDayService = businessDayService,
            shiftService = shiftService,
            cashControlService = cashControlService,
            shiftClosingService = shiftClosingService,
            operationalControlService = operationalControlService,
            productRepository = productRepo,
            productLookupUseCase = productLookupUseCase,
            inventoryService = inventoryService,
            salesService = salesService,
            voidSaleService = voidSaleService,
            hardwarePort = NoopHardwarePort,
            masterDataDatabase = masterDataDb,
            reportingQueryFacade = reportingQueryFacade,
            syncReplayService = syncReplayService,
            reportingExporter = reportingExporter,
            storeProfileService = storeProfileService
        )
    }

    private suspend fun DesktopFixture.loginAsSupervisor() {
        kernelRepository.upsertTerminalBinding(
            TerminalBinding("S1", "Store 1", "T1", "Term 1", Clock.System.now())
        )
        val pin = "111111"
        insertOperator("SUP1", "Supervisor", pin, OperatorRole.SUPERVISOR)
        accessService.login("SUP1", pin)
    }

    private suspend fun DesktopFixture.openShift() {
        shiftService.submitStartShift(100_000.0)
    }

    private suspend fun DesktopFixture.insertOperator(
        id: String,
        name: String,
        pin: String,
        role: OperatorRole
    ) {
        val salt = "test-salt"
        val hasher = PinHasher()
        kernelRepository.upsertOperator(
            OperatorAccount(
                id = id,
                employeeCode = id,
                displayName = name,
                role = role,
                pinHash = hasher.hash(pin, salt),
                pinSalt = salt,
                failedAttempts = 0,
                lockedUntil = null,
                isActive = true,
                lastLoginAt = null
            )
        )
    }
}

private data class DesktopFixture(
    val kernelRepository: KernelRepository,
    val accessService: AccessService,
    val businessDayService: BusinessDayService,
    val shiftService: ShiftService,
    val cashControlService: CashControlService,
    val shiftClosingService: ShiftClosingService,
    val operationalControlService: OperationalControlService,
    val productRepository: ProductRepository,
    val productLookupUseCase: ProductLookupUseCase,
    val inventoryService: InventoryService,
    val salesService: SalesService,
    val voidSaleService: VoidSaleService,
    val hardwarePort: CashierHardwarePort,
    val masterDataDatabase: MasterDataDatabase,
    val reportingQueryFacade: ReportingQueryFacade,
    val syncReplayService: SyncReplayService,
    val reportingExporter: DesktopReportingExporter,
    val storeProfileService: StoreProfileService
) {
    fun newController(): DesktopAppController = DesktopAppController(
        accessService = accessService,
        businessDayService = businessDayService,
        shiftService = shiftService,
        cashControlService = cashControlService,
        shiftClosingService = shiftClosingService,
        operationalControlService = operationalControlService,
        productRepository = productRepository,
        productLookupUseCase = productLookupUseCase,
        inventoryService = inventoryService,
        salesService = salesService,
        voidSaleService = voidSaleService,
        hardwarePort = hardwarePort,
        reportingQueryFacade = reportingQueryFacade,
        syncReplayService = syncReplayService,
        reportingExporter = reportingExporter,
        bootstrapAvatarStore = NoopBootstrapAvatarStore,
        storeProfileService = storeProfileService,
        storeProfileLogoStore = NoopStoreProfileLogoStore
    )
}

private class DesktopTestOperationalSalesPort(
    private val salesService: SalesService
) : OperationalSalesPort {
    override suspend fun getShiftSalesSummary(shiftId: String): ShiftSalesSummary {
        return salesService.getShiftSalesSummary(shiftId)
    }

    override suspend fun getMultiShiftSalesSummary(shiftIds: List<String>): ShiftSalesSummary {
        return salesService.getMultiShiftSalesSummary(shiftIds)
    }

    override suspend fun getShiftVoidSummary(shiftId: String) =
        salesService.getShiftVoidSummaryForReporting(shiftId)

    override suspend fun getMultiShiftVoidSummary(shiftIds: List<String>) =
        salesService.getMultiShiftVoidSummaryForReporting(shiftIds)
}

private class KernelRepositorySalesKernelPort(
    private val kernelRepository: KernelRepository
) : id.azureenterprise.cassy.sales.application.SalesKernelPort {
    @Suppress("ReturnCount")
    override suspend fun getOperationalContext(): id.azureenterprise.cassy.sales.application.SalesOperationalContext? {
        val binding = kernelRepository.getTerminalBinding() ?: return null
        val shift = kernelRepository.getActiveShift(binding.terminalId) ?: return null
        return id.azureenterprise.cassy.sales.application.SalesOperationalContext(
            storeName = binding.storeName,
            terminalId = binding.terminalId,
            terminalName = binding.terminalName,
            shiftId = shift.id
        )
    }

    override suspend fun recordAudit(auditId: String, message: String) {
        kernelRepository.insertAudit(auditId, message, "INFO")
    }

    override suspend fun recordEvent(eventId: String, type: String, payload: String) {
        kernelRepository.insertEvent(eventId, type, payload)
    }
}

private object NoopHardwarePort : CashierHardwarePort {
    override suspend fun getSnapshot() = CashierHardwareSnapshot()
    override suspend fun handlePostFinalization(
        paymentMethod: String,
        receiptPayload: ReceiptPrintPayload
    ) = HardwarePostFinalizationResult(CashierHardwareSnapshot())

    override suspend fun printReceipt(
        receiptPayload: ReceiptPrintPayload
    ) = HardwarePrintExecutionResult(
        CashierHardwareSnapshot(),
        id.azureenterprise.cassy.sales.domain.ReceiptPrintState(
            id.azureenterprise.cassy.sales.domain.ReceiptPrintStatus.NOT_REQUESTED
        )
    )
}

private object NoopOperationalHardwarePort : id.azureenterprise.cassy.kernel.application.OperationalHardwarePort {
    override suspend fun getHardwareIssues(): List<OperationalIssue> = emptyList()
}

private object NoopBootstrapAvatarStore : BootstrapAvatarStore {
    override fun chooseAndImport(role: OperatorRole, existingPath: String?): Result<String?> = Result.success(null)
    override fun deleteManaged(path: String?) = Unit
}

private object NoopStoreProfileLogoStore : StoreProfileLogoStore {
    override fun chooseAndImport(storeId: String, existingPath: String?): Result<String?> = Result.success(null)
    override fun deleteManaged(path: String?) = Unit
}
