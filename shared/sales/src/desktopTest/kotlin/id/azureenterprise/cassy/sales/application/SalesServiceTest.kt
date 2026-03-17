package id.azureenterprise.cassy.sales.application

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import id.azureenterprise.cassy.inventory.application.InventoryService
import id.azureenterprise.cassy.inventory.data.InventoryRepository
import id.azureenterprise.cassy.inventory.db.InventoryDatabase
import id.azureenterprise.cassy.kernel.data.KernelRepository
import id.azureenterprise.cassy.kernel.db.KernelDatabase
import id.azureenterprise.cassy.kernel.domain.TerminalBinding
import id.azureenterprise.cassy.masterdata.data.ProductLookupRepositoryImpl
import id.azureenterprise.cassy.masterdata.db.MasterDataDatabase
import id.azureenterprise.cassy.masterdata.domain.BarcodeNormalizer
import id.azureenterprise.cassy.masterdata.domain.Product
import id.azureenterprise.cassy.masterdata.domain.ProductLookupUseCase
import id.azureenterprise.cassy.sales.data.SalesRepository
import id.azureenterprise.cassy.sales.db.SalesDatabase
import id.azureenterprise.cassy.sales.domain.CompleteSaleOutcome
import id.azureenterprise.cassy.sales.domain.PaymentState
import id.azureenterprise.cassy.sales.domain.PaymentStatus
import id.azureenterprise.cassy.sales.domain.PaymentStatusDetailCode
import id.azureenterprise.cassy.sales.domain.ReceiptPrintStatus
import id.azureenterprise.cassy.sales.domain.PricingEngine
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
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
    fun `payment pending and failed states require explicit detail code`() {
        assertFailsWith<IllegalArgumentException> {
            PaymentState(status = PaymentStatus.PENDING)
        }
        assertFailsWith<IllegalArgumentException> {
            PaymentState(status = PaymentStatus.FAILED)
        }

        val pending = PaymentState.pending(PaymentStatusDetailCode.AWAITING_FINALIZATION)
        val failed = PaymentState.failed(PaymentStatusDetailCode.TECHNICAL_FAILURE)

        assertEquals(PaymentStatus.PENDING, pending.status)
        assertEquals(PaymentStatusDetailCode.AWAITING_FINALIZATION, pending.detailCode)
        assertEquals(PaymentStatus.FAILED, failed.status)
        assertEquals(PaymentStatusDetailCode.TECHNICAL_FAILURE, failed.detailCode)
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
            assertEquals(PaymentStatus.SUCCESS, checkout.getOrThrow().readback.receiptSnapshot.payment.state.status)
            assertEquals(ReceiptPrintStatus.READY_FOR_PRINT, checkout.getOrThrow().printState.status)
            assertEquals(-2.0, fixture.inventoryRepository.getStockLevel("product_1"))
            assertEquals(1, fixture.inventoryRepository.getLedgerByProduct("product_1").size)
        }
    }

    @Test
    fun `finalized sale readback history and reprint use the same final snapshot source`() {
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
            val completion = fixture.service.checkout("CASH").getOrThrow()
            val saleId = completion.saleId

            val finalizedSale = fixture.service.getFinalizedSale(saleId).getOrThrow()
            val receipt = fixture.service.getReceiptForPrint(saleId).getOrThrow()
            val history = fixture.service.getSaleHistory().getOrThrow()

            assertEquals(saleId, finalizedSale.sale.id)
            assertEquals("CASH", finalizedSale.receiptSnapshot.payment.method)
            assertEquals(PaymentStatus.SUCCESS, finalizedSale.receiptSnapshot.payment.state.status)
            assertEquals(ReceiptPrintStatus.READY_FOR_PRINT, receipt.printState.status)
            assertTrue(receipt.renderedContent.contains(finalizedSale.receiptSnapshot.localNumber))
            assertTrue(receipt.renderedContent.contains("Produk Uji"))
            assertEquals(saleId, history.first().saleId)
            assertEquals(finalizedSale.receiptSnapshot.localNumber, history.first().localNumber)
            assertEquals(PaymentStatus.SUCCESS, history.first().paymentState.status)
        }
    }

    @Test
    fun `checkout records audit and outbox intent only after finalization`() {
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

            fixture.service.addProduct(sampleProduct()).getOrThrow()
            val saleId = fixture.service.checkout("CASH").getOrThrow().saleId

            assertTrue(fixture.kernelRepository.audits.any { it.contains("Sale finalized") })
            assertEquals("SALE_FINALIZED", fixture.kernelRepository.events.single().type)
            assertTrue(fixture.kernelRepository.events.single().payload.contains(saleId))
        }
    }

    @Test
    fun `pending gateway keeps sale non-final and does not create receipt or inventory effect`() {
        runBlocking {
            val fixture = salesFixture()
            fixture.gateway.nextResult = PaymentGatewayResult(
                paymentState = PaymentState.pending(
                    detailCode = PaymentStatusDetailCode.AWAITING_PROVIDER_CONFIRMATION,
                    detailMessage = "Menunggu callback provider"
                ),
                providerReference = "qris:sale_pending"
            )
            fixture.kernelRepository.upsertTerminalBinding(fixture.binding)
            fixture.kernelRepository.openBusinessDay("bd_1")
            fixture.kernelRepository.openShift(
                id = "shift_1",
                businessDayId = "bd_1",
                terminalId = fixture.binding.terminalId,
                openingCash = 100.0,
                openedBy = "operator_1"
            )

            fixture.service.addProduct(sampleProduct()).getOrThrow()
            val outcome = fixture.service.completeSale("QRIS").getOrThrow()

            val pendingOutcome = outcome as CompleteSaleOutcome.Pending
            assertEquals(PaymentStatus.PENDING, pendingOutcome.paymentState.status)
            assertEquals(0.0, fixture.inventoryRepository.getStockLevel("product_1"))
            assertTrue(fixture.service.getReceiptForPrint(pendingOutcome.saleId).isFailure)
            assertTrue(fixture.kernelRepository.audits.isEmpty())
            assertTrue(fixture.kernelRepository.events.isEmpty())
        }
    }

    @Test
    fun `declined gateway does not finalize sale and keeps basket for human retry`() {
        runBlocking {
            val fixture = salesFixture()
            fixture.gateway.nextResult = PaymentGatewayResult(
                paymentState = PaymentState.failed(
                    detailCode = PaymentStatusDetailCode.DECLINED,
                    detailMessage = "Pembayaran ditolak"
                ),
                providerReference = "card:declined"
            )
            fixture.kernelRepository.upsertTerminalBinding(fixture.binding)
            fixture.kernelRepository.openBusinessDay("bd_1")
            fixture.kernelRepository.openShift(
                id = "shift_1",
                businessDayId = "bd_1",
                terminalId = fixture.binding.terminalId,
                openingCash = 100.0,
                openedBy = "operator_1"
            )

            fixture.service.addProduct(sampleProduct()).getOrThrow()
            val outcome = fixture.service.completeSale("CARD").getOrThrow()

            assertTrue(outcome is CompleteSaleOutcome.Rejected)
            assertEquals(1, fixture.service.basket.value.items.size)
            assertEquals(0.0, fixture.inventoryRepository.getStockLevel("product_1"))
            assertTrue(fixture.kernelRepository.audits.isEmpty())
            assertTrue(fixture.kernelRepository.events.isEmpty())
        }
    }

    @Test
    fun `duplicate callback replay does not duplicate settlement inventory or intents`() {
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
            fixture.gateway.nextResult = PaymentGatewayResult(
                paymentState = PaymentState.pending(
                    detailCode = PaymentStatusDetailCode.AWAITING_PROVIDER_CONFIRMATION,
                    detailMessage = "Menunggu callback"
                ),
                providerReference = "qris:retry-safe"
            )

            val firstOutcome = fixture.service.completeSale("QRIS").getOrThrow()
            val saleId = (firstOutcome as CompleteSaleOutcome.Pending).saleId
            val callback = PaymentCallbackRequest(
                saleId = saleId,
                providerReference = "qris:retry-safe",
                paymentState = PaymentState.success()
            )

            val completed = fixture.service.handlePaymentCallback(callback).getOrThrow()
            val replayed = fixture.service.handlePaymentCallback(callback).getOrThrow()

            val replayedCompleted = replayed as CompleteSaleOutcome.Completed
            assertTrue(completed is CompleteSaleOutcome.Completed)
            assertTrue(replayedCompleted.replayed)
            assertEquals(-2.0, fixture.inventoryRepository.getStockLevel("product_1"))
            assertEquals(1, fixture.inventoryRepository.getLedgerByProduct("product_1").size)
            assertEquals(1, fixture.kernelRepository.audits.size)
            assertEquals(1, fixture.kernelRepository.events.size)
            assertEquals(PaymentStatus.SUCCESS, fixture.service.getFinalizedSale(saleId).getOrThrow().receiptSnapshot.payment.state.status)
        }
    }

    @Test
    fun `retry on pending sale reuses same pending session without duplicate effects`() {
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

            fixture.service.addProduct(sampleProduct()).getOrThrow()
            fixture.gateway.nextResult = PaymentGatewayResult(
                paymentState = PaymentState.pending(
                    detailCode = PaymentStatusDetailCode.AWAITING_PROVIDER_CONFIRMATION,
                    detailMessage = "Provider masih memproses"
                ),
                providerReference = "qris:retry-loop"
            )

            val first = fixture.service.completeSale("QRIS").getOrThrow()
            val second = fixture.service.completeSale("QRIS").getOrThrow()

            val firstPending = first as CompleteSaleOutcome.Pending
            val secondPending = second as CompleteSaleOutcome.Pending
            assertEquals(firstPending.saleId, secondPending.saleId)
            assertEquals(0.0, fixture.inventoryRepository.getStockLevel("product_1"))
            assertTrue(fixture.kernelRepository.events.isEmpty())
        }
    }

    @Test
    fun `crash after inventory apply keeps sale non-final and replay completes without duplicate stock`() {
        runBlocking {
            val hooks = CrashInjectionHooks()
            val fixture = salesFixture(finalizationHooks = hooks)
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
            hooks.failAfterInventory = true

            val failed = fixture.service.completeSale("CASH")

            assertTrue(failed.isFailure)
            assertEquals(-2.0, fixture.inventoryRepository.getStockLevel("product_1"))
            assertEquals(1, fixture.inventoryRepository.getLedgerByProduct("product_1").size)
            assertTrue(fixture.service.getSaleHistory().getOrThrow().isEmpty())
            assertTrue(fixture.service.getReceiptForPrint("sale_missing").isFailure)

            hooks.failAfterInventory = false
            val recoveredCount = fixture.service.recoverIncompleteFinalizations().getOrThrow()

            assertEquals(1, recoveredCount)
            assertEquals(1, fixture.inventoryRepository.getLedgerByProduct("product_1").size)
            assertEquals(1, fixture.kernelRepository.audits.size)
            assertEquals(1, fixture.kernelRepository.events.size)
            assertEquals(1, fixture.service.getSaleHistory().getOrThrow().size)
        }
    }

    @Test
    fun `crash after kernel intent keeps sale non-final and replay completes without duplicate audit or event`() {
        runBlocking {
            val hooks = CrashInjectionHooks()
            val fixture = salesFixture(finalizationHooks = hooks)
            fixture.kernelRepository.upsertTerminalBinding(fixture.binding)
            fixture.kernelRepository.openBusinessDay("bd_1")
            fixture.kernelRepository.openShift(
                id = "shift_1",
                businessDayId = "bd_1",
                terminalId = fixture.binding.terminalId,
                openingCash = 100.0,
                openedBy = "operator_1"
            )

            fixture.service.addProduct(sampleProduct()).getOrThrow()
            hooks.failAfterKernel = true

            val failed = fixture.service.completeSale("CASH")

            assertTrue(failed.isFailure)
            assertEquals(1, fixture.kernelRepository.audits.size)
            assertEquals(1, fixture.kernelRepository.events.size)
            assertTrue(fixture.service.getSaleHistory().getOrThrow().isEmpty())

            hooks.failAfterKernel = false
            val recoveredCount = fixture.service.recoverIncompleteFinalizations().getOrThrow()

            assertEquals(1, recoveredCount)
            assertEquals(1, fixture.kernelRepository.audits.size)
            assertEquals(1, fixture.kernelRepository.events.size)
            assertEquals(PaymentStatus.SUCCESS, fixture.service.getSaleHistory().getOrThrow().single().paymentState.status)
        }
    }

    private fun salesFixture(
        finalizationHooks: SalesFinalizationHooks = NoopSalesFinalizationHooks
    ): SalesFixture {
        val kernelDriver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        val salesDriver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        val inventoryDriver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        val masterDataDriver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)

        KernelDatabase.Schema.create(kernelDriver)
        SalesDatabase.Schema.create(salesDriver)
        InventoryDatabase.Schema.create(inventoryDriver)
        MasterDataDatabase.Schema.create(masterDataDriver)

        val kernelRepository = RecordingKernelRepository(KernelDatabase(kernelDriver), Clock.System)
        val inventoryRepository = InventoryRepository(
            InventoryDatabase(inventoryDriver),
            EmptyCoroutineContext,
            Clock.System
        )

        val productLookupUseCase = ProductLookupUseCase(
            repository = ProductLookupRepositoryImpl(MasterDataDatabase(masterDataDriver), EmptyCoroutineContext),
            normalizer = BarcodeNormalizer()
        )

        val gateway = FakePaymentGatewayPort()

        return SalesFixture(
            service = SalesService(
                salesRepository = SalesRepository(SalesDatabase(salesDriver), EmptyCoroutineContext, Clock.System),
                inventoryService = InventoryService(inventoryRepository, Clock.System),
                kernelPort = RecordingSalesKernelPort(kernelRepository),
                paymentGatewayPort = gateway,
                pricingEngine = PricingEngine(),
                productLookupUseCase = productLookupUseCase,
                clock = Clock.System,
                finalizationHooks = finalizationHooks
            ),
            kernelRepository = kernelRepository,
            inventoryRepository = inventoryRepository,
            gateway = gateway,
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
    val kernelRepository: RecordingKernelRepository,
    val inventoryRepository: InventoryRepository,
    val gateway: FakePaymentGatewayPort,
    val binding: TerminalBinding
)

private class RecordingSalesKernelPort(
    private val kernelRepository: RecordingKernelRepository
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
        runCatching { kernelRepository.insertAudit(auditId, message, "INFO") }
            .getOrElse { error ->
                val normalized = error.message?.uppercase().orEmpty()
                if ("UNIQUE" !in normalized && "PRIMARY KEY" !in normalized) {
                    throw error
                }
            }
    }

    override suspend fun recordEvent(eventId: String, type: String, payload: String) {
        runCatching { kernelRepository.insertEvent(eventId, type, payload) }
            .getOrElse { error ->
                val normalized = error.message?.uppercase().orEmpty()
                if ("UNIQUE" !in normalized && "PRIMARY KEY" !in normalized) {
                    throw error
                }
            }
    }
}

private class RecordingKernelRepository(
    database: KernelDatabase,
    clock: Clock
) : KernelRepository(database, EmptyCoroutineContext, clock) {
    val audits = mutableListOf<String>()
    val events = mutableListOf<RecordedEvent>()

    override suspend fun insertAudit(id: String, message: String, level: String) {
        super.insertAudit(id, message, level)
        audits += message
    }

    override suspend fun insertEvent(id: String, type: String, payload: String) {
        super.insertEvent(id, type, payload)
        events += RecordedEvent(id = id, type = type, payload = payload)
    }
}

private data class RecordedEvent(
    val id: String,
    val type: String,
    val payload: String
)

private class FakePaymentGatewayPort : PaymentGatewayPort {
    var nextResult: PaymentGatewayResult? = null

    override suspend fun finalizePayment(request: PaymentGatewayRequest): PaymentGatewayResult {
        return nextResult ?: PaymentGatewayResult(
            paymentState = PaymentState.success(),
            providerReference = "${request.paymentMethod.lowercase()}:${request.saleId}"
        )
    }
}

private class CrashInjectionHooks : SalesFinalizationHooks {
    var failAfterInventory: Boolean = false
    var failAfterKernel: Boolean = false

    override suspend fun afterInventoryApplied(saleId: String) {
        if (failAfterInventory) {
            throw IllegalStateException("Simulasi crash setelah inventory sale=$saleId")
        }
    }

    override suspend fun afterKernelApplied(saleId: String) {
        if (failAfterKernel) {
            throw IllegalStateException("Simulasi crash setelah kernel sale=$saleId")
        }
    }
}
