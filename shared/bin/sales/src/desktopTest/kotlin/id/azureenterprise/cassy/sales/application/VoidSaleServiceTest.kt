package id.azureenterprise.cassy.sales.application

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import id.azureenterprise.cassy.inventory.application.InventoryService
import id.azureenterprise.cassy.inventory.application.InventoryVoidImpactPolicy
import id.azureenterprise.cassy.inventory.data.InventoryRepository
import id.azureenterprise.cassy.inventory.db.InventoryDatabase
import id.azureenterprise.cassy.kernel.application.AccessService
import id.azureenterprise.cassy.kernel.application.CashControlService
import id.azureenterprise.cassy.kernel.data.KernelRepository
import id.azureenterprise.cassy.kernel.db.KernelDatabase
import id.azureenterprise.cassy.kernel.domain.CashMovementPolicy
import id.azureenterprise.cassy.kernel.domain.OperatorAccount
import id.azureenterprise.cassy.kernel.domain.OperatorRole
import id.azureenterprise.cassy.kernel.domain.PinHasher
import id.azureenterprise.cassy.kernel.domain.TerminalBinding
import id.azureenterprise.cassy.masterdata.data.ProductLookupRepositoryImpl
import id.azureenterprise.cassy.masterdata.db.MasterDataDatabase
import id.azureenterprise.cassy.masterdata.domain.BarcodeNormalizer
import id.azureenterprise.cassy.masterdata.domain.Product
import id.azureenterprise.cassy.masterdata.domain.ProductLookupUseCase
import id.azureenterprise.cassy.sales.data.SalesRepository
import id.azureenterprise.cassy.sales.db.SalesDatabase
import id.azureenterprise.cassy.sales.application.PaymentGatewayRequest
import id.azureenterprise.cassy.sales.application.PaymentGatewayResult
import id.azureenterprise.cassy.sales.domain.PaymentState
import id.azureenterprise.cassy.sales.domain.SaleStatus
import id.azureenterprise.cassy.sales.domain.PricingEngine
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class VoidSaleServiceTest {

    @Test
    fun `cash sale can be voided and records cash refund with sales summary reduced`() {
        runBlocking {
            val fixture = fixture()
            fixture.loginSupervisor()
            fixture.kernelRepository.openBusinessDay("bd_1")
            fixture.kernelRepository.openShift(
                id = "shift_1",
                businessDayId = "bd_1",
                terminalId = fixture.binding.terminalId,
                openingCash = 100_000.0,
                openedBy = "sup_1"
            )

            fixture.salesService.addProduct(sampleProduct()).getOrThrow()
            val saleId = fixture.salesService.checkout("CASH").getOrThrow().saleId

            val result = fixture.voidSaleService.executeVoid(
                saleId = saleId,
                reasonCode = "VOID_DUPLICATE_INPUT",
                reasonDetail = "Double input operator",
                inventoryFollowUpNote = "Cek fisik barang di meja kasir"
            ).getOrThrow()

            assertEquals("VOID_DUPLICATE_INPUT", result.saleVoid.reasonCode)
            assertEquals(10.0, fixture.kernelRepository.getCashMovementTotalsByShift("shift_1").cashOutTotal)
            assertEquals(0, fixture.salesService.getShiftSalesSummary("shift_1").completedSaleCount)
            assertEquals(1, fixture.salesService.getShiftVoidSummary("shift_1").count)

            val recent = fixture.salesService.getRecentSaleHistory(limit = 5).getOrThrow().first()
            assertEquals(SaleStatus.VOIDED, recent.saleStatus)
            assertNotNull(recent.voidedAtEpochMs)
        }
    }

    @Test
    fun `non cash sale assessment stays blocked and points to external reversal gap honestly`() {
        runBlocking {
            val fixture = fixture()
            fixture.loginSupervisor()
            fixture.kernelRepository.openBusinessDay("bd_1")
            fixture.kernelRepository.openShift(
                id = "shift_1",
                businessDayId = "bd_1",
                terminalId = fixture.binding.terminalId,
                openingCash = 100_000.0,
                openedBy = "sup_1"
            )

            fixture.salesService.addProduct(sampleProduct()).getOrThrow()
            val saleId = fixture.salesService.checkout("CARD").getOrThrow().saleId

            val assessment = fixture.voidSaleService.assessVoid(saleId, null).getOrThrow()

            assertFalse(assessment.isEligible)
            assertTrue(assessment.message.contains("CASH"))
            assertEquals(SaleStatus.COMPLETED, assessment.saleStatus)
        }
    }

    private fun fixture(): VoidFixture {
        val kernelDriver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        val salesDriver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        val inventoryDriver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        val masterDataDriver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)

        KernelDatabase.Schema.create(kernelDriver)
        SalesDatabase.Schema.create(salesDriver)
        InventoryDatabase.Schema.create(inventoryDriver)
        MasterDataDatabase.Schema.create(masterDataDriver)

        val kernelRepository = KernelRepository(KernelDatabase(kernelDriver), EmptyCoroutineContext, Clock.System)
        val accessService = AccessService(kernelRepository, PinHasher(), Clock.System)
        val inventoryService = InventoryService(
            inventoryRepository = InventoryRepository(InventoryDatabase(inventoryDriver), EmptyCoroutineContext, Clock.System),
            accessService = accessService,
            kernelRepository = kernelRepository,
            voidImpactPolicy = InventoryVoidImpactPolicy(),
            clock = Clock.System
        )
        val salesRepository = SalesRepository(SalesDatabase(salesDriver), EmptyCoroutineContext, Clock.System)
        val salesService = SalesService(
            salesRepository = salesRepository,
            inventoryService = inventoryService,
            kernelPort = KernelRepositorySalesKernelPort(kernelRepository),
            paymentGatewayPort = object : PaymentGatewayPort {
                override suspend fun finalizePayment(request: PaymentGatewayRequest): PaymentGatewayResult =
                    PaymentGatewayResult(
                        paymentState = PaymentState.success(),
                        providerReference = "provider_${request.paymentMethod.lowercase()}"
                    )
            },
            pricingEngine = PricingEngine(),
            productLookupUseCase = ProductLookupUseCase(
                repository = ProductLookupRepositoryImpl(MasterDataDatabase(masterDataDriver), EmptyCoroutineContext),
                normalizer = BarcodeNormalizer()
            ),
            clock = Clock.System
        )
        val cashControlService = CashControlService(kernelRepository, accessService, CashMovementPolicy())
        val binding = TerminalBinding(
            storeId = "store_1",
            storeName = "Toko Uji",
            terminalId = "terminal_1",
            terminalName = "Kasir-01",
            boundAt = Clock.System.now()
        )

        return VoidFixture(
            binding = binding,
            kernelRepository = kernelRepository,
            accessService = accessService,
            salesService = salesService,
            voidSaleService = VoidSaleService(
                salesRepository = salesRepository,
                kernelRepository = kernelRepository,
                accessService = accessService,
                cashControlService = cashControlService,
                inventoryService = inventoryService
            )
        )
    }

    private suspend fun VoidFixture.loginSupervisor() {
        kernelRepository.upsertTerminalBinding(binding)
        val salt = "void-test-salt"
        val hasher = PinHasher()
        kernelRepository.upsertOperator(
            OperatorAccount(
                id = "sup_1",
                employeeCode = "sup_1",
                displayName = "Supervisor",
                role = OperatorRole.SUPERVISOR,
                pinHash = hasher.hash("123456", salt),
                pinSalt = salt,
                failedAttempts = 0,
                lockedUntil = null,
                isActive = true,
                lastLoginAt = null
            )
        )
        accessService.login("sup_1", "123456")
    }

    private fun sampleProduct(): Product = Product(
        id = "product_1",
        name = "Produk Uji",
        price = 10.0,
        categoryId = "cat_1",
        sku = "SKU-TEST-001"
    )
}

private data class VoidFixture(
    val binding: TerminalBinding,
    val kernelRepository: KernelRepository,
    val accessService: AccessService,
    val salesService: SalesService,
    val voidSaleService: VoidSaleService
)

private class KernelRepositorySalesKernelPort(
    private val kernelRepository: KernelRepository
) : SalesKernelPort {
    override suspend fun getOperationalContext(): SalesOperationalContext? {
        val binding = kernelRepository.getTerminalBinding() ?: return null
        if (!kernelRepository.isBusinessDayOpen()) return null
        val shift = kernelRepository.getActiveShift(binding.terminalId) ?: return null
        return SalesOperationalContext(
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
