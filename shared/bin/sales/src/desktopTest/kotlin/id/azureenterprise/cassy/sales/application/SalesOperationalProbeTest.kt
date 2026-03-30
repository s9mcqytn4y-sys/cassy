package id.azureenterprise.cassy.sales.application

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import id.azureenterprise.cassy.inventory.application.InventoryService
import id.azureenterprise.cassy.inventory.application.InventoryVoidImpactPolicy
import id.azureenterprise.cassy.inventory.data.InventoryRepository
import id.azureenterprise.cassy.inventory.db.InventoryDatabase
import id.azureenterprise.cassy.kernel.application.AccessService
import id.azureenterprise.cassy.kernel.application.BusinessDayService
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
import id.azureenterprise.cassy.sales.domain.PaymentState
import id.azureenterprise.cassy.sales.domain.PricingEngine
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.system.measureTimeMillis
import kotlin.test.Test
import kotlin.test.assertTrue

class SalesOperationalProbeTest {

    @Test
    fun cashier_critical_path_stays_within_lightweight_probe_budget() {
        runBlocking {
            val fixture = fixture()
            fixture.loginSupervisor()
            fixture.kernelRepository.openBusinessDay("bd_probe")
            fixture.kernelRepository.openShift(
                id = "shift_probe",
                businessDayId = "bd_probe",
                terminalId = fixture.binding.terminalId,
                openingCash = 100_000.0,
                openedBy = "sup_probe"
            )

            val addProductMs = measureTimeMillis {
                repeat(25) {
                    fixture.salesService.addProduct(sampleProduct()).getOrThrow()
                }
            }

            val checkoutMs = measureTimeMillis {
                fixture.salesService.checkout("CASH").getOrThrow()
            }

            val recentSale = fixture.salesService.getRecentSaleHistory(limit = 5).getOrThrow().first()
            val voidMs = measureTimeMillis {
                fixture.voidSaleService.executeVoid(
                    saleId = recentSale.saleId,
                    reasonCode = "VOID_DUPLICATE_INPUT",
                    reasonDetail = "Probe duplicate input",
                    inventoryFollowUpNote = "Probe stock follow-up"
                ).getOrThrow()
            }

            val summaryMs = measureTimeMillis {
                fixture.salesService.getShiftSalesSummary("shift_probe")
                fixture.salesService.getShiftVoidSummary("shift_probe")
                fixture.salesService.getRecentSaleHistory(limit = 10).getOrThrow()
            }

            println(
                "CASSY_PERF_PROBE add_product_ms=$addProductMs checkout_ms=$checkoutMs void_ms=$voidMs summary_ms=$summaryMs"
            )

            assertTrue(addProductMs < 5_000, "Add product probe terlalu lambat: ${addProductMs}ms")
            assertTrue(checkoutMs < 5_000, "Checkout probe terlalu lambat: ${checkoutMs}ms")
            assertTrue(voidMs < 5_000, "Void probe terlalu lambat: ${voidMs}ms")
            assertTrue(summaryMs < 5_000, "Summary probe terlalu lambat: ${summaryMs}ms")
        }
    }

    private fun fixture(): ProbeFixture {
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
            kernelPort = ProbeKernelPort(kernelRepository),
            paymentGatewayPort = object : PaymentGatewayPort {
                override suspend fun finalizePayment(request: PaymentGatewayRequest): PaymentGatewayResult =
                    PaymentGatewayResult(
                        paymentState = PaymentState.success(),
                        providerReference = "probe_${request.paymentMethod.lowercase()}"
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
        val businessDayService = BusinessDayService(kernelRepository, accessService)
        val binding = TerminalBinding(
            storeId = "store_probe",
            storeName = "Probe Store",
            terminalId = "terminal_probe",
            terminalName = "Probe Terminal",
            boundAt = Clock.System.now()
        )

        return ProbeFixture(
            binding = binding,
            kernelRepository = kernelRepository,
            accessService = accessService,
            businessDayService = businessDayService,
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

    private suspend fun ProbeFixture.loginSupervisor() {
        kernelRepository.upsertTerminalBinding(binding)
        val salt = "probe-salt"
        val hasher = PinHasher()
        kernelRepository.upsertOperator(
            OperatorAccount(
                id = "sup_probe",
                employeeCode = "sup_probe",
                displayName = "Supervisor Probe",
                role = OperatorRole.SUPERVISOR,
                pinHash = hasher.hash("123456", salt),
                pinSalt = salt,
                failedAttempts = 0,
                lockedUntil = null,
                isActive = true,
                lastLoginAt = null
            )
        )
        accessService.login("sup_probe", "123456")
    }

    private fun sampleProduct(): Product = Product(
        id = "probe_product_1",
        name = "Probe Product",
        price = 10.0,
        categoryId = "cat_probe",
        sku = "SKU-PROBE-001"
    )
}

private data class ProbeFixture(
    val binding: TerminalBinding,
    val kernelRepository: KernelRepository,
    val accessService: AccessService,
    val businessDayService: BusinessDayService,
    val salesService: SalesService,
    val voidSaleService: VoidSaleService
)

private class ProbeKernelPort(
    private val kernelRepository: KernelRepository
) : SalesKernelPort {
    override suspend fun getOperationalContext(): SalesOperationalContext? {
        val binding = kernelRepository.getTerminalBinding() ?: return null
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
