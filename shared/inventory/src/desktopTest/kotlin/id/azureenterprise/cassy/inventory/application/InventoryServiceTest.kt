package id.azureenterprise.cassy.inventory.application

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import id.azureenterprise.cassy.inventory.data.InventoryRepository
import id.azureenterprise.cassy.inventory.db.InventoryDatabase
import id.azureenterprise.cassy.inventory.domain.InventoryDiscrepancyStatus
import id.azureenterprise.cassy.inventory.domain.InventoryLayerStatus
import id.azureenterprise.cassy.inventory.domain.InventoryVoidImpactClassification
import id.azureenterprise.cassy.inventory.domain.SaleInventoryLine
import id.azureenterprise.cassy.inventory.domain.StockCountDraft
import id.azureenterprise.cassy.inventory.domain.StockAdjustmentDraft
import id.azureenterprise.cassy.kernel.application.AccessService
import id.azureenterprise.cassy.kernel.data.KernelRepository
import id.azureenterprise.cassy.kernel.db.KernelDatabase
import id.azureenterprise.cassy.kernel.domain.BootstrapStoreRequest
import id.azureenterprise.cassy.kernel.domain.OperatorRole
import id.azureenterprise.cassy.kernel.domain.PinHasher
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class InventoryServiceTest {

    @Test
    fun `sale completion groups duplicate product lines and writes explainable stock through inventory owner`() {
        runBlocking {
            val fixture = inventoryFixture()

            val result = fixture.service.recordSaleCompletion(
                saleId = "sale_1",
                terminalId = "terminal_1",
                lines = listOf(
                    SaleInventoryLine(productId = "product_1", quantity = 1.0),
                    SaleInventoryLine(productId = "product_1", quantity = 2.0)
                )
            )

            assertTrue(result.isSuccess)
            val readback = fixture.repository.getInventoryReadback("product_1")
            assertNotNull(readback)
            assertEquals(-3.0, readback.balance.quantity)
            assertEquals(1, readback.ledgerEntries.size)
            assertEquals("sale_1", readback.ledgerEntries.single().sourceId)
            assertEquals("SALE_FINALIZATION", readback.ledgerEntries.single().sourceType.name)
            assertTrue(
                readback.discrepancies.any { it.status == InventoryDiscrepancyStatus.INVESTIGATION_REQUIRED }
            )
        }
    }

    @Test
    fun `stock count records discrepancy without silent auto adjustment`() {
        runBlocking {
            val fixture = inventoryFixture()
            fixture.login(OperatorRole.SUPERVISOR)

            fixture.service.applyManualAdjustment(
                id.azureenterprise.cassy.inventory.domain.StockAdjustmentDraft(
                    productId = "product_1",
                    quantityDelta = 8.0,
                    terminalId = "terminal_1",
                    reasonCode = "FOUND_STOCK",
                    reasonDetail = "Saldo awal untuk tes count"
                )
            ).getOrThrow()
            fixture.login(OperatorRole.CASHIER)

            val review = fixture.service.submitStockCount(
                StockCountDraft(
                    productId = "product_1",
                    countedQuantity = 5.0,
                    terminalId = "terminal_1"
                )
            ).getOrThrow()

            val readback = fixture.service.getInventoryReadback("product_1").getOrThrow()
            assertNotNull(readback)
            assertEquals(8.0, readback.balance.quantity)
            assertEquals(InventoryDiscrepancyStatus.PENDING_REVIEW, review.status)
            assertEquals(-3.0, review.varianceQuantity)
            assertTrue(readback.discrepancies.any { it.id == review.id })
            assertEquals(1, readback.ledgerEntries.size)
        }
    }

    @Test
    fun `void impact classification blocks ambiguous mutation and marks investigation honestly`() {
        val policy = InventoryVoidImpactPolicy()

        val preSettlement = policy.classify(
            paymentSettled = false,
            physicalReturnConfirmed = false,
            explicitInventoryReasonProvided = false
        )
        val postSettlement = policy.classify(
            paymentSettled = true,
            physicalReturnConfirmed = false,
            explicitInventoryReasonProvided = true
        )
        val confirmedReturn = policy.classify(
            paymentSettled = true,
            physicalReturnConfirmed = true,
            explicitInventoryReasonProvided = true
        )
        val vague = policy.classify(
            paymentSettled = true,
            physicalReturnConfirmed = false,
            explicitInventoryReasonProvided = false
        )

        assertEquals(
            InventoryVoidImpactClassification.PRE_SETTLEMENT_VOID_NO_STOCK_EFFECT,
            preSettlement.classification
        )
        assertEquals(
            InventoryVoidImpactClassification.POST_SETTLEMENT_REVERSAL_CANDIDATE,
            postSettlement.classification
        )
        assertEquals(
            InventoryVoidImpactClassification.RETURN_REQUIRED,
            confirmedReturn.classification
        )
        assertEquals(
            InventoryVoidImpactClassification.MANUAL_INVESTIGATION_REQUIRED,
            vague.classification
        )
        assertTrue(preSettlement.blocksInventoryMutation)
        assertTrue(postSettlement.blocksInventoryMutation)
        assertTrue(confirmedReturn.blocksInventoryMutation)
        assertTrue(vague.blocksInventoryMutation)
    }

    @Test
    fun `positive layers are consumed in FIFO order for non expiry stock`() {
        runBlocking {
            val fixture = inventoryFixture()
            fixture.login(OperatorRole.SUPERVISOR)

            fixture.service.applyManualAdjustment(
                StockAdjustmentDraft(
                    productId = "product_1",
                    quantityDelta = 5.0,
                    terminalId = "terminal_1",
                    reasonCode = "FOUND_STOCK",
                    reasonDetail = "Batch pertama"
                )
            ).getOrThrow()
            fixture.service.applyManualAdjustment(
                StockAdjustmentDraft(
                    productId = "product_1",
                    quantityDelta = 4.0,
                    terminalId = "terminal_1",
                    reasonCode = "FOUND_STOCK",
                    reasonDetail = "Batch kedua"
                )
            ).getOrThrow()

            fixture.service.recordSaleCompletion(
                saleId = "sale_fifo_1",
                terminalId = "terminal_1",
                lines = listOf(SaleInventoryLine(productId = "product_1", quantity = 6.0))
            ).getOrThrow()

            val readback = fixture.repository.getInventoryReadback("product_1")
            val layers = fixture.repository.listActiveLayersByProduct("product_1")

            assertNotNull(readback)
            assertEquals(3.0, readback.balance.quantity)
            assertEquals(1, layers.size)
            assertEquals(4.0, layers.single().acquiredQuantity)
            assertEquals(3.0, layers.single().remainingQuantity)
            assertEquals(InventoryLayerStatus.OPEN, layers.single().status)
        }
    }
}

private fun inventoryFixture(): InventoryFixture {
    val inventoryDriver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
    val kernelDriver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
    InventoryDatabase.Schema.create(inventoryDriver)
    KernelDatabase.Schema.create(kernelDriver)
    val kernelRepository = KernelRepository(KernelDatabase(kernelDriver), EmptyCoroutineContext, Clock.System)
    val accessService = AccessService(kernelRepository, PinHasher(), Clock.System)
    runBlocking {
        accessService.bootstrapStore(
            BootstrapStoreRequest("Store", "T1", "Kasir", "111111", "Supervisor", "222222")
        ).getOrThrow()
        kernelRepository.ensureDefaultReasonCodes()
    }
    val inventoryRepository = InventoryRepository(
        InventoryDatabase(inventoryDriver),
        EmptyCoroutineContext,
        Clock.System
    )
    return InventoryFixture(
        repository = inventoryRepository,
        service = InventoryService(
            inventoryRepository = inventoryRepository,
            accessService = accessService,
            kernelRepository = kernelRepository,
            voidImpactPolicy = InventoryVoidImpactPolicy(),
            clock = Clock.System
        ),
        accessService = accessService,
        kernelRepository = kernelRepository
    )
}

private data class InventoryFixture(
    val repository: InventoryRepository,
    val service: InventoryService,
    val accessService: AccessService,
    val kernelRepository: KernelRepository
) {
    suspend fun login(role: OperatorRole) {
        accessService.logout()
        val operator = kernelRepository.listActiveOperators().first { it.role == role }
        accessService.login(operator.id, if (role == OperatorRole.CASHIER) "111111" else "222222")
    }
}
