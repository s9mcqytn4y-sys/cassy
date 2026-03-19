package id.azureenterprise.cassy.desktop

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import id.azureenterprise.cassy.inventory.application.InventoryService
import id.azureenterprise.cassy.inventory.application.InventoryVoidImpactPolicy
import id.azureenterprise.cassy.inventory.data.InventoryRepository
import id.azureenterprise.cassy.inventory.db.InventoryDatabase
import id.azureenterprise.cassy.kernel.application.AccessService
import id.azureenterprise.cassy.kernel.application.BusinessDayService
import id.azureenterprise.cassy.kernel.application.CashControlService
import id.azureenterprise.cassy.kernel.application.OperationalControlService
import id.azureenterprise.cassy.kernel.application.OperationalSalesPort
import id.azureenterprise.cassy.kernel.application.ShiftService
import id.azureenterprise.cassy.kernel.application.ShiftClosingService
import id.azureenterprise.cassy.kernel.data.KernelRepository
import id.azureenterprise.cassy.kernel.db.KernelDatabase
import id.azureenterprise.cassy.kernel.domain.PinHasher
import id.azureenterprise.cassy.masterdata.data.ProductLookupRepositoryImpl
import id.azureenterprise.cassy.masterdata.data.ProductRepository
import id.azureenterprise.cassy.masterdata.db.MasterDataDatabase
import id.azureenterprise.cassy.masterdata.domain.BarcodeNormalizer
import id.azureenterprise.cassy.masterdata.domain.ProductLookupUseCase
import id.azureenterprise.cassy.sales.application.PaymentGatewayPort
import id.azureenterprise.cassy.sales.application.PaymentGatewayRequest
import id.azureenterprise.cassy.sales.application.PaymentGatewayResult
import id.azureenterprise.cassy.sales.application.SalesService
import id.azureenterprise.cassy.sales.data.SalesRepository
import id.azureenterprise.cassy.sales.db.SalesDatabase
import id.azureenterprise.cassy.sales.domain.PaymentStatus
import id.azureenterprise.cassy.sales.domain.ReceiptPrintState
import id.azureenterprise.cassy.sales.domain.ReceiptPrintStatus
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
            assertEquals(
                "Masih ada shift aktif. Tutup semua shift dulu sebelum close day.",
                controller.state.value.banner?.message
            )
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
            assertEquals("Opening cash tidak boleh negatif.", controller.state.value.banner?.message)
        }
    }

    @Test
    fun `dashboard blocks cashier until business day and shift are ready`() {
        runBlocking {
            val fixture = desktopFixture()
            val controller = fixture.newController()

            controller.load()
            controller.updateBootstrapField(BootstrapField.StoreName, "Store")
            controller.updateBootstrapField(BootstrapField.TerminalName, "T1")
            controller.updateBootstrapField(BootstrapField.CashierName, "C1")
            controller.updateBootstrapField(BootstrapField.CashierPin, "111111")
            controller.updateBootstrapField(BootstrapField.SupervisorName, "S1")
            controller.updateBootstrapField(BootstrapField.SupervisorPin, "222222")
            controller.bootstrapStore()
            controller.selectOperator(controller.state.value.login.operators.first { it.roleLabel == "CASHIER" }.id)
            controller.updatePin("111111")
            controller.login()

            assertEquals(DesktopStage.OpenDay, controller.state.value.stage)
            assertEquals("Buka Business Day", controller.state.value.shell.nextActionLabel)
            assertTrue(
                controller.state.value.operations.dashboard.salesHomeBlocker
                    ?.contains("Business day harus aktif") == true
            )
        }
    }

    @Test
    fun `cashier out of policy opening cash is blocked until supervisor approval lane takes over`() {
        runBlocking {
            val fixture = desktopFixture()
            val controller = fixture.newController()

            controller.load()
            controller.updateBootstrapField(BootstrapField.StoreName, "Store")
            controller.updateBootstrapField(BootstrapField.TerminalName, "T1")
            controller.updateBootstrapField(BootstrapField.CashierName, "C1")
            controller.updateBootstrapField(BootstrapField.CashierPin, "111111")
            controller.updateBootstrapField(BootstrapField.SupervisorName, "S1")
            controller.updateBootstrapField(BootstrapField.SupervisorPin, "222222")
            controller.bootstrapStore()
            controller.selectOperator(controller.state.value.login.operators.first { it.roleLabel == "SUPERVISOR" }.id)
            controller.updatePin("222222")
            controller.login()
            controller.openBusinessDay()
            controller.logout()
            controller.selectOperator(controller.state.value.login.operators.first { it.roleLabel == "CASHIER" }.id)
            controller.updatePin("111111")
            controller.login()

            controller.updateOpeningCashInput("750000")
            controller.updateOpeningCashReasonInput("Butuh pecahan pembukaan")
            controller.startShift()

            assertEquals(DesktopStage.StartShift, controller.state.value.stage)
            assertEquals(
                "Opening cash di atas batas kasir. Login supervisor/owner untuk menyetujui.",
                controller.state.value.banner?.message
            )
        }
    }

    @Test
    fun `supervisor can start shift with approval reason when opening cash is out of policy`() {
        runBlocking {
            val fixture = desktopFixture()
            val controller = fixture.newController()

            controller.load()
            controller.updateBootstrapField(BootstrapField.StoreName, "Store")
            controller.updateBootstrapField(BootstrapField.TerminalName, "T1")
            controller.updateBootstrapField(BootstrapField.CashierName, "C1")
            controller.updateBootstrapField(BootstrapField.CashierPin, "111111")
            controller.updateBootstrapField(BootstrapField.SupervisorName, "S1")
            controller.updateBootstrapField(BootstrapField.SupervisorPin, "222222")
            controller.bootstrapStore()
            controller.selectOperator(controller.state.value.login.operators.first { it.roleLabel == "SUPERVISOR" }.id)
            controller.updatePin("222222")
            controller.login()
            controller.openBusinessDay()

            controller.updateOpeningCashInput("750000")
            controller.updateOpeningCashReasonInput("Hari ramai, butuh pecahan tambahan")
            controller.startShift()

            assertEquals(DesktopStage.Catalog, controller.state.value.stage)
            assertEquals(
                "Shift aktif sudah ada di terminal ini.",
                controller.state.value.operations.dashboard.decisions
                    .first { it.type.name == "START_SHIFT" }
                    .message
            )
        }
    }

    @Test
    fun `cash movement approval can be requested by cashier and approved by supervisor`() {
        runBlocking {
            val fixture = desktopFixture()
            val controller = fixture.newController()

            controller.load()
            controller.updateBootstrapField(BootstrapField.StoreName, "Store")
            controller.updateBootstrapField(BootstrapField.TerminalName, "T1")
            controller.updateBootstrapField(BootstrapField.CashierName, "C1")
            controller.updateBootstrapField(BootstrapField.CashierPin, "111111")
            controller.updateBootstrapField(BootstrapField.SupervisorName, "S1")
            controller.updateBootstrapField(BootstrapField.SupervisorPin, "222222")
            controller.bootstrapStore()
            controller.selectOperator(controller.state.value.login.operators.first { it.roleLabel == "SUPERVISOR" }.id)
            controller.updatePin("222222")
            controller.login()
            controller.openBusinessDay()
            controller.updateOpeningCashInput("100000")
            controller.startShift()
            controller.logout()
            controller.selectOperator(controller.state.value.login.operators.first { it.roleLabel == "CASHIER" }.id)
            controller.updatePin("111111")
            controller.login()

            controller.updateCashMovementType(id.azureenterprise.cassy.kernel.domain.CashMovementType.SAFE_DROP)
            controller.updateCashMovementAmountInput("1500000")
            controller.updateCashMovementReasonCode("SAFE_DROP_OVERFLOW")
            controller.updateCashMovementReasonDetail("Laci penuh")
            controller.submitCashMovement()

            assertTrue(controller.state.value.operations.pendingApprovals.isNotEmpty())

            val approvalId = controller.state.value.operations.pendingApprovals.first().id
            controller.logout()
            controller.selectOperator(controller.state.value.login.operators.first { it.roleLabel == "SUPERVISOR" }.id)
            controller.updatePin("222222")
            controller.login()
            controller.approveCashMovement(approvalId)

            assertTrue(controller.state.value.operations.pendingApprovals.isEmpty())
            assertEquals(
                "SAFE_DROP",
                fixture.kernelRepository
                    .listCashMovementsByShift(controller.state.value.operations.shiftLabel!!)
                    .last()
                    .type
                    .name
            )
        }
    }

    @Test
    fun `close shift is blocked by pending transaction from sales lane`() {
        runBlocking {
            val fixture = desktopFixture(paymentGateway = FakePendingDesktopPaymentGatewayPort())
            val controller = fixture.newController()

            controller.load()
            controller.updateBootstrapField(BootstrapField.StoreName, "Store")
            controller.updateBootstrapField(BootstrapField.TerminalName, "T1")
            controller.updateBootstrapField(BootstrapField.CashierName, "C1")
            controller.updateBootstrapField(BootstrapField.CashierPin, "111111")
            controller.updateBootstrapField(BootstrapField.SupervisorName, "S1")
            controller.updateBootstrapField(BootstrapField.SupervisorPin, "222222")
            controller.bootstrapStore()
            controller.selectOperator(controller.state.value.login.operators.first { it.roleLabel == "SUPERVISOR" }.id)
            controller.updatePin("222222")
            controller.login()
            controller.openBusinessDay()
            controller.updateOpeningCashInput("100000")
            controller.startShift()
            controller.addProduct(controller.state.value.catalog.products.first())
            controller.updateCashReceivedInput("10000")
            controller.checkoutCash()

            controller.updateClosingCashInput("100000")
            controller.endShift()

            assertTrue(controller.state.value.banner?.message?.contains("pending", ignoreCase = true) == true)
            assertEquals(DesktopStage.Catalog, controller.state.value.stage)
        }
    }

    @Test
    fun `desktop cashier lane can finalize cash sale and reprint from persisted snapshot`() {
        runBlocking {
            val fixture = desktopFixture()
            val controller = fixture.newController()

            controller.load()
            controller.updateBootstrapField(BootstrapField.StoreName, "Store")
            controller.updateBootstrapField(BootstrapField.TerminalName, "T1")
            controller.updateBootstrapField(BootstrapField.CashierName, "C1")
            controller.updateBootstrapField(BootstrapField.CashierPin, "111111")
            controller.updateBootstrapField(BootstrapField.SupervisorName, "S1")
            controller.updateBootstrapField(BootstrapField.SupervisorPin, "222222")
            controller.bootstrapStore()
            controller.selectOperator(controller.state.value.login.operators.first { it.roleLabel == "SUPERVISOR" }.id)
            controller.updatePin("222222")
            controller.login()
            controller.openBusinessDay()
            controller.updateOpeningCashInput("100.0")
            controller.startShift()

            val product = controller.state.value.catalog.products.first()
            controller.addProduct(product)
            controller.updateCashReceivedInput("20000")
            controller.checkoutCash()

            assertEquals(DesktopStage.Catalog, controller.state.value.stage)
            assertTrue(controller.state.value.catalog.basket.items.isEmpty())
            assertTrue(controller.state.value.catalog.lastFinalizedSaleId != null)
            assertTrue(controller.state.value.catalog.lastReceiptPreview?.contains("No. Struk:") == true)
            assertEquals(
                "Preview struk final siap ditinjau",
                controller.state.value.catalog.receiptPreview.availabilityMessage
            )

            val readback = fixture.salesService
                .getCompletedSaleReadback(controller.state.value.catalog.lastFinalizedSaleId!!)
                .getOrThrow()
            val printPayload = fixture.salesService
                .getReceiptForPrint(controller.state.value.catalog.lastFinalizedSaleId!!, isReprint = true)
                .getOrThrow()

            assertEquals(PaymentStatus.SUCCESS, readback.receiptSnapshot.payment.state.status)
            assertEquals(ReceiptPrintStatus.READY_FOR_PRINT, printPayload.printState.status)

            controller.reprintLastReceipt()

            assertEquals(ReceiptPrintStatus.PRINTED, controller.state.value.catalog.printState.status)
            assertTrue(controller.state.value.catalog.lastReceiptPreview?.contains(product.name) == true)
        }
    }

    @Test
    fun `hardware warning after finalization stays post settlement and sale remains final`() {
        runBlocking {
            val fixture = desktopFixture(
                hardwarePort = FakeCashierHardwarePort(
                    postFinalizationWarning = "Cash drawer tidak merespons, transaksi tetap sah"
                )
            )
            val controller = fixture.newController()

            controller.load()
            controller.updateBootstrapField(BootstrapField.StoreName, "Store")
            controller.updateBootstrapField(BootstrapField.TerminalName, "T1")
            controller.updateBootstrapField(BootstrapField.CashierName, "C1")
            controller.updateBootstrapField(BootstrapField.CashierPin, "111111")
            controller.updateBootstrapField(BootstrapField.SupervisorName, "S1")
            controller.updateBootstrapField(BootstrapField.SupervisorPin, "222222")
            controller.bootstrapStore()
            controller.selectOperator(controller.state.value.login.operators.first { it.roleLabel == "SUPERVISOR" }.id)
            controller.updatePin("222222")
            controller.login()
            controller.openBusinessDay()
            controller.updateOpeningCashInput("100.0")
            controller.startShift()

            controller.addProduct(controller.state.value.catalog.products.first())
            controller.updateCashReceivedInput("10000")
            controller.checkoutCash()

            val saleId = controller.state.value.catalog.lastFinalizedSaleId!!
            assertEquals("Cash drawer tidak merespons, transaksi tetap sah", controller.state.value.banner?.message)
            assertEquals(
                PaymentStatus.SUCCESS,
                fixture.salesService
                    .getCompletedSaleReadback(saleId)
                    .getOrThrow()
                    .receiptSnapshot
                    .payment
                    .state
                    .status
            )
        }
    }

    @Test
    fun `print failure is visible and finalized sale stays valid`() {
        runBlocking {
            val fixture = desktopFixture(
                hardwarePort = FakeCashierHardwarePort(
                    printState = ReceiptPrintState(
                        status = ReceiptPrintStatus.FAILED,
                        detailMessage = "Printer offline. Struk final tetap aman untuk reprint nanti."
                    )
                )
            )
            val controller = fixture.newController()

            controller.load()
            controller.updateBootstrapField(BootstrapField.StoreName, "Store")
            controller.updateBootstrapField(BootstrapField.TerminalName, "T1")
            controller.updateBootstrapField(BootstrapField.CashierName, "C1")
            controller.updateBootstrapField(BootstrapField.CashierPin, "111111")
            controller.updateBootstrapField(BootstrapField.SupervisorName, "S1")
            controller.updateBootstrapField(BootstrapField.SupervisorPin, "222222")
            controller.bootstrapStore()
            controller.selectOperator(controller.state.value.login.operators.first { it.roleLabel == "SUPERVISOR" }.id)
            controller.updatePin("222222")
            controller.login()
            controller.openBusinessDay()
            controller.updateOpeningCashInput("100.0")
            controller.startShift()

            controller.addProduct(controller.state.value.catalog.products.first())
            controller.updateCashReceivedInput("10000")
            controller.checkoutCash()
            controller.printLastReceipt()

            val saleId = controller.state.value.catalog.lastFinalizedSaleId!!
            assertEquals(ReceiptPrintStatus.FAILED, controller.state.value.catalog.printState.status)
            assertEquals(
                "Printer offline. Struk final tetap aman untuk reprint nanti.",
                controller.state.value.banner?.message
            )
            assertEquals(
                PaymentStatus.SUCCESS,
                fixture.salesService.getCompletedSaleReadback(saleId).getOrThrow().receiptSnapshot.payment.state.status
            )
        }
    }

    @Test
    fun `inventory desktop flow keeps current state separate from count discrepancy until explicit resolve`() {
        runBlocking {
            val fixture = desktopFixture()
            val controller = fixture.newController()

            controller.load()
            controller.updateBootstrapField(BootstrapField.StoreName, "Store")
            controller.updateBootstrapField(BootstrapField.TerminalName, "T1")
            controller.updateBootstrapField(BootstrapField.CashierName, "C1")
            controller.updateBootstrapField(BootstrapField.CashierPin, "111111")
            controller.updateBootstrapField(BootstrapField.SupervisorName, "S1")
            controller.updateBootstrapField(BootstrapField.SupervisorPin, "222222")
            controller.bootstrapStore()
            controller.selectOperator(controller.state.value.login.operators.first { it.roleLabel == "SUPERVISOR" }.id)
            controller.updatePin("222222")
            controller.login()
            controller.openBusinessDay()
            controller.updateOpeningCashInput("100000")
            controller.startShift()

            val productId = controller.state.value.inventory.availableProducts.first().id
            controller.selectInventoryProduct(productId)
            controller.updateInventoryAdjustmentDirection(InventoryAdjustmentDirection.INCREASE)
            controller.updateInventoryAdjustmentQuantityInput("12")
            controller.updateInventoryAdjustmentReasonCode("FOUND_STOCK")
            controller.updateInventoryAdjustmentReasonDetail("Saldo awal rak depan")
            controller.applyInventoryAdjustment()

            assertEquals(12.0, controller.state.value.inventory.selectedReadback?.balance?.quantity)
            assertTrue(controller.state.value.inventory.imageIoStatus.contains("input_images"))

            controller.updateInventoryCountQuantityInput("10")
            controller.submitInventoryCount()

            assertEquals(12.0, controller.state.value.inventory.selectedReadback?.balance?.quantity)
            val reviewId = controller.state.value.inventory.unresolvedDiscrepancies
                .first { it.productId == productId }
                .id

            controller.updateInventoryAdjustmentReasonCode("COUNT_VARIANCE")
            controller.updateInventoryAdjustmentReasonDetail("Koreksi hasil stock opname")
            controller.resolveInventoryDiscrepancy(reviewId)

            assertEquals(10.0, controller.state.value.inventory.selectedReadback?.balance?.quantity)
            assertTrue(controller.state.value.inventory.unresolvedDiscrepancies.none { it.id == reviewId })
            assertTrue(
                controller.state.value.inventory.selectedReadback
                    ?.ledgerEntries
                    ?.any { it.sourceType.name == "STOCK_OPNAME_RESOLUTION" } == true
            )
        }
    }

    @Test
    fun `cashier inventory adjustment enters approval queue and supervisor can approve from desktop state`() {
        runBlocking {
            val fixture = desktopFixture()
            val controller = fixture.newController()

            controller.load()
            controller.updateBootstrapField(BootstrapField.StoreName, "Store")
            controller.updateBootstrapField(BootstrapField.TerminalName, "T1")
            controller.updateBootstrapField(BootstrapField.CashierName, "C1")
            controller.updateBootstrapField(BootstrapField.CashierPin, "111111")
            controller.updateBootstrapField(BootstrapField.SupervisorName, "S1")
            controller.updateBootstrapField(BootstrapField.SupervisorPin, "222222")
            controller.bootstrapStore()
            controller.selectOperator(controller.state.value.login.operators.first { it.roleLabel == "SUPERVISOR" }.id)
            controller.updatePin("222222")
            controller.login()
            controller.openBusinessDay()
            controller.updateOpeningCashInput("100000")
            controller.startShift()
            controller.logout()
            controller.selectOperator(controller.state.value.login.operators.first { it.roleLabel == "CASHIER" }.id)
            controller.updatePin("111111")
            controller.login()

            val productId = controller.state.value.inventory.availableProducts.first().id
            controller.selectInventoryProduct(productId)
            controller.updateInventoryAdjustmentDirection(InventoryAdjustmentDirection.INCREASE)
            controller.updateInventoryAdjustmentQuantityInput("15")
            controller.updateInventoryAdjustmentReasonCode("MANUAL_CORRECTION")
            controller.updateInventoryAdjustmentReasonDetail("Perlu koreksi stok besar")
            controller.applyInventoryAdjustment()

            assertTrue(controller.state.value.banner?.message?.contains("LIGHT_PIN") == true)
            val actionId = controller.state.value.inventory.pendingApprovalActions.first().id
            assertEquals(null, controller.state.value.inventory.selectedReadback)

            controller.logout()
            controller.selectOperator(controller.state.value.login.operators.first { it.roleLabel == "SUPERVISOR" }.id)
            controller.updatePin("222222")
            controller.login()
            controller.approveInventoryAction(actionId)

            assertTrue(controller.state.value.inventory.pendingApprovalActions.isEmpty())
            assertEquals(15.0, controller.state.value.inventory.selectedReadback?.balance?.quantity)
        }
    }

    @Suppress("LongMethod")
    private fun desktopFixture(
        hardwarePort: CashierHardwarePort = FakeCashierHardwarePort(),
        paymentGateway: PaymentGatewayPort = FakeDesktopPaymentGatewayPort()
    ): DesktopFixture {
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
        val inventoryService = InventoryService(
            inventoryRepository = inventoryRepository,
            accessService = accessService,
            kernelRepository = kernelRepository,
            voidImpactPolicy = InventoryVoidImpactPolicy(),
            clock = Clock.System
        )
        val salesService = SalesService(
            salesRepository = SalesRepository(SalesDatabase(salesDriver), EmptyCoroutineContext, Clock.System),
            inventoryService = inventoryService,
            kernelPort = KernelRepositorySalesKernelPort(kernelRepository),
            paymentGatewayPort = paymentGateway,
            pricingEngine = PricingEngine(),
            productLookupUseCase = productLookupUseCase,
            clock = Clock.System
        )
        val operationalSalesPort: OperationalSalesPort = DesktopTestOperationalSalesPort(salesService)
        val cashControlService = CashControlService(kernelRepository, accessService)
        val shiftClosingService = ShiftClosingService(
            kernelRepository = kernelRepository,
            accessService = accessService,
            salesPort = operationalSalesPort
        )
        val operationalControlService = OperationalControlService(
            accessService,
            businessDayService,
            shiftService,
            cashControlService,
            shiftClosingService
        )

        return DesktopFixture(
            kernelRepository = kernelRepository,
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
            hardwarePort = hardwarePort
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
    val hardwarePort: CashierHardwarePort
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
        hardwarePort = hardwarePort
    )
}

private class DesktopTestOperationalSalesPort(
    private val salesService: SalesService
) : OperationalSalesPort {
    override suspend fun getShiftSalesSummary(shiftId: String) = salesService.getShiftSalesSummary(shiftId)
}

private class KernelRepositorySalesKernelPort(
    private val kernelRepository: KernelRepository
) : id.azureenterprise.cassy.sales.application.SalesKernelPort {
    override suspend fun getOperationalContext(): id.azureenterprise.cassy.sales.application.SalesOperationalContext? {
        val binding = kernelRepository.getTerminalBinding()
        val shift = binding?.let { kernelRepository.getActiveShift(it.terminalId) }
        val isReady = binding != null && shift != null && kernelRepository.isBusinessDayOpen()
        return if (isReady) {
            id.azureenterprise.cassy.sales.application.SalesOperationalContext(
                storeName = binding.storeName,
                terminalId = binding.terminalId,
                terminalName = binding.terminalName,
                shiftId = shift.id
            )
        } else {
            null
        }
    }

    override suspend fun recordAudit(auditId: String, message: String) {
        kernelRepository.insertAudit(auditId, message, "INFO")
    }

    override suspend fun recordEvent(eventId: String, type: String, payload: String) {
        kernelRepository.insertEvent(eventId, type, payload)
    }
}

private class FakeDesktopPaymentGatewayPort : PaymentGatewayPort {
    override suspend fun finalizePayment(request: PaymentGatewayRequest): PaymentGatewayResult {
        return PaymentGatewayResult(
            paymentState = id.azureenterprise.cassy.sales.domain.PaymentState.success(),
            providerReference = "cash:${request.saleId}"
        )
    }
}

private class FakePendingDesktopPaymentGatewayPort : PaymentGatewayPort {
    override suspend fun finalizePayment(request: PaymentGatewayRequest): PaymentGatewayResult {
        return PaymentGatewayResult(
            paymentState = id.azureenterprise.cassy.sales.domain.PaymentState.pending(
                id.azureenterprise.cassy.sales.domain.PaymentStatusDetailCode.AWAITING_FINALIZATION,
                "Payment masih pending"
            ),
            providerReference = "pending:${request.saleId}"
        )
    }
}

private class FakeCashierHardwarePort(
    private val snapshot: CashierHardwareSnapshot = CashierHardwareSnapshot(),
    private val postFinalizationWarning: String? = null,
    private val printState: ReceiptPrintState = ReceiptPrintState(
        status = ReceiptPrintStatus.PRINTED,
        detailMessage = "Struk berhasil dicetak"
    )
) : CashierHardwarePort {
    override suspend fun getSnapshot(): CashierHardwareSnapshot = snapshot

    override suspend fun handlePostFinalization(
        paymentMethod: String,
        receiptPayload: id.azureenterprise.cassy.sales.domain.ReceiptPrintPayload
    ): HardwarePostFinalizationResult {
        return HardwarePostFinalizationResult(
            snapshot = snapshot,
            warningMessage = postFinalizationWarning
        )
    }

    override suspend fun printReceipt(
        receiptPayload: id.azureenterprise.cassy.sales.domain.ReceiptPrintPayload
    ): HardwarePrintExecutionResult {
        return HardwarePrintExecutionResult(
            snapshot = snapshot,
            printState = printState
        )
    }
}
