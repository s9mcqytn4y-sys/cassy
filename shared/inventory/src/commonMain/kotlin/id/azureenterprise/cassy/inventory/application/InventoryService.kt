package id.azureenterprise.cassy.inventory.application

import id.azureenterprise.cassy.inventory.data.InventoryRepository
import id.azureenterprise.cassy.inventory.domain.AppliedInventoryMutation
import id.azureenterprise.cassy.inventory.domain.InventoryApprovalMode
import id.azureenterprise.cassy.inventory.domain.InventoryDiscrepancyReview
import id.azureenterprise.cassy.inventory.domain.InventoryDiscrepancyStatus
import id.azureenterprise.cassy.inventory.domain.InventoryLedgerStatus
import id.azureenterprise.cassy.inventory.domain.InventoryMutationType
import id.azureenterprise.cassy.inventory.domain.InventoryReadback
import id.azureenterprise.cassy.inventory.domain.InventorySourceType
import id.azureenterprise.cassy.inventory.domain.SaleInventoryLine
import id.azureenterprise.cassy.inventory.domain.StockAdjustmentDraft
import id.azureenterprise.cassy.inventory.domain.StockCountDraft
import id.azureenterprise.cassy.inventory.domain.StockLedgerEntry
import id.azureenterprise.cassy.inventory.domain.VoidImpactAssessment
import id.azureenterprise.cassy.kernel.application.AccessService
import id.azureenterprise.cassy.kernel.data.KernelRepository
import id.azureenterprise.cassy.kernel.domain.AccessCapability
import id.azureenterprise.cassy.kernel.domain.IdGenerator
import id.azureenterprise.cassy.kernel.domain.ReasonCategory
import kotlinx.datetime.Clock

class InventoryService(
    private val inventoryRepository: InventoryRepository,
    private val accessService: AccessService,
    private val kernelRepository: KernelRepository,
    private val voidImpactPolicy: InventoryVoidImpactPolicy,
    private val clock: Clock
) {
    suspend fun recordSaleCompletion(
        saleId: String,
        terminalId: String,
        lines: List<SaleInventoryLine>
    ): Result<Unit> = runCatching {
        require(saleId.isNotBlank()) { "Sale reference wajib ada" }
        require(terminalId.isNotBlank()) { "Terminal wajib ada" }
        require(lines.isNotEmpty()) { "Sale inventory lines tidak boleh kosong" }

        lines.groupBy { it.productId }
            .mapValues { (_, grouped) -> grouped.sumOf { it.quantity } }
            .forEach { (productId, quantity) ->
                require(productId.isNotBlank()) { "Product id wajib ada" }
                require(quantity > 0.0) { "Quantity sale harus lebih besar dari 0" }
                val lineId = lines.firstOrNull { it.productId == productId }?.sourceLineId
                    ?: "sale_line_$productId"
                val mutation = StockLedgerEntry(
                    id = IdGenerator.nextId("stock_ledger"),
                    productId = productId,
                    quantityDelta = -quantity,
                    mutationType = InventoryMutationType.SALE_COMPLETION,
                    sourceType = InventorySourceType.SALE_FINALIZATION,
                    sourceId = saleId,
                    sourceLineId = lineId,
                    reasonCode = null,
                    reasonDetail = "Pengurangan stok dari sale final",
                    actorId = null,
                    terminalId = terminalId,
                    status = InventoryLedgerStatus.FINAL,
                    createdAt = clock.now()
                )
                val existing = inventoryRepository.findLedgerEntry(
                    productId = productId,
                    mutationType = mutation.mutationType,
                    sourceType = mutation.sourceType,
                    sourceId = mutation.sourceId,
                    sourceLineId = mutation.sourceLineId
                )
                if (existing != null) {
                    check(existing.quantityDelta == mutation.quantityDelta) {
                        "Replay finalization inventory mismatch untuk sale $saleId produk $productId"
                    }
                    return@forEach
                }
                val applied = inventoryRepository.applyMutation(mutation)
                createInvestigationReviewIfNeeded(
                    applied = applied,
                    terminalId = terminalId,
                    requestedBy = "SYSTEM_SALE_FINALIZATION",
                    note = "Layer stok tidak cukup lengkap untuk menjelaskan deduction sale secara FIFO/FEFO penuh."
                )
            }
    }

    suspend fun submitStockCount(draft: StockCountDraft): Result<InventoryDiscrepancyReview> = runCatching {
        val operator = accessService.requireCapability(AccessCapability.RECORD_STOCK_COUNT).getOrThrow()
        require(draft.productId.isNotBlank()) { "Produk wajib dipilih" }
        require(draft.countedQuantity >= 0.0) { "Counted quantity tidak boleh negatif" }
        val balance = inventoryRepository.getBalanceSnapshot(draft.productId)
        val bookQuantity = balance?.quantity ?: 0.0
        val variance = draft.countedQuantity - bookQuantity
        val review = inventoryRepository.createDiscrepancy(
            InventoryDiscrepancyReview(
                id = IdGenerator.nextId("inv_count"),
                productId = draft.productId,
                bookQuantity = bookQuantity,
                countedQuantity = draft.countedQuantity,
                varianceQuantity = variance,
                status = if (variance == 0.0) {
                    InventoryDiscrepancyStatus.MATCHED
                } else {
                    InventoryDiscrepancyStatus.PENDING_REVIEW
                },
                approvalMode = InventoryApprovalMode.LIGHT_PIN,
                sourceType = InventorySourceType.STOCK_OPNAME_COUNT,
                sourceId = IdGenerator.nextId("stock_opname"),
                sourceLineId = null,
                reasonCode = null,
                reasonDetail = if (variance == 0.0) {
                    "Count cocok dengan buku stok. Tidak ada adjustment otomatis."
                } else {
                    "Selisih count harus ditinjau dan disesuaikan secara eksplisit."
                },
                requestedBy = operator.id,
                resolvedBy = if (variance == 0.0) operator.id else null,
                terminalId = draft.terminalId,
                createdAt = clock.now(),
                resolvedAt = if (variance == 0.0) clock.now() else null,
                resolutionNote = if (variance == 0.0) "Count cocok. Tidak ada mutasi stok." else null,
                relatedLedgerEntryId = null
            )
        )
        recordAudit("Stock count ${review.id} untuk ${draft.productId} variance ${review.varianceQuantity}")
        recordEvent(
            eventId = "event_inventory_count_${review.id}",
            type = "INVENTORY_COUNT_RECORDED",
            payload = """{"reviewId":"${review.id}","productId":"${review.productId}","variance":${review.varianceQuantity}}"""
        )
        review
    }

    suspend fun applyManualAdjustment(draft: StockAdjustmentDraft): Result<AppliedInventoryMutation> = runCatching {
        val operator = accessService.requireCapability(AccessCapability.APPLY_STOCK_ADJUSTMENT).getOrThrow()
        require(draft.productId.isNotBlank()) { "Produk wajib dipilih" }
        require(draft.quantityDelta != 0.0) { "Adjustment quantity tidak boleh nol" }
        require(draft.terminalId.isNotBlank()) { "Terminal wajib ada" }
        kernelRepository.ensureDefaultReasonCodes()
        val reason = kernelRepository.getReasonCode(draft.reasonCode)
            ?: error("Reason code inventory tidak valid")
        check(reason.category == ReasonCategory.INVENTORY_ADJUSTMENT) { "Reason code inventory tidak valid" }
        val adjustmentId = IdGenerator.nextId("inv_adjustment")
        val entry = StockLedgerEntry(
            id = IdGenerator.nextId("stock_ledger"),
            productId = draft.productId,
            quantityDelta = draft.quantityDelta,
            mutationType = InventoryMutationType.STOCK_ADJUSTMENT,
            sourceType = InventorySourceType.MANUAL_STOCK_ADJUSTMENT,
            sourceId = adjustmentId,
            sourceLineId = null,
            reasonCode = reason.code,
            reasonDetail = draft.reasonDetail,
            actorId = operator.id,
            terminalId = draft.terminalId,
            status = InventoryLedgerStatus.FINAL,
            createdAt = clock.now()
        )
        val applied = inventoryRepository.applyMutation(entry)
        createInvestigationReviewIfNeeded(
            applied = applied,
            terminalId = draft.terminalId,
            requestedBy = operator.id,
            note = "Adjustment diterapkan tetapi provenance layer stok masih perlu investigasi."
        )
        recordAudit("Manual adjustment ${entry.sourceId} untuk ${draft.productId} delta ${draft.quantityDelta}")
        recordEvent(
            eventId = "event_inventory_adjustment_${entry.sourceId}",
            type = "INVENTORY_ADJUSTMENT_RECORDED",
            payload = """{"sourceId":"${entry.sourceId}","productId":"${entry.productId}","delta":${entry.quantityDelta},"approvalMode":"LIGHT_PIN"}"""
        )
        applied
    }

    suspend fun resolveStockCount(reviewId: String, reasonCode: String, reasonDetail: String?): Result<AppliedInventoryMutation> = runCatching {
        val operator = accessService.requireCapability(AccessCapability.APPROVE_STOCK_ADJUSTMENT).getOrThrow()
        val review = inventoryRepository.getDiscrepancyById(reviewId) ?: error("Review discrepancy tidak ditemukan")
        require(review.status == InventoryDiscrepancyStatus.PENDING_REVIEW) { "Review discrepancy tidak lagi menunggu" }
        kernelRepository.ensureDefaultReasonCodes()
        val reason = kernelRepository.getReasonCode(reasonCode) ?: error("Reason code inventory tidak valid")
        check(reason.category == ReasonCategory.INVENTORY_ADJUSTMENT) { "Reason code inventory tidak valid" }

        val entry = StockLedgerEntry(
            id = IdGenerator.nextId("stock_ledger"),
            productId = review.productId,
            quantityDelta = review.varianceQuantity,
            mutationType = InventoryMutationType.STOCK_OPNAME_ADJUSTMENT,
            sourceType = InventorySourceType.STOCK_OPNAME_RESOLUTION,
            sourceId = review.id,
            sourceLineId = review.sourceLineId,
            reasonCode = reason.code,
            reasonDetail = reasonDetail ?: review.reasonDetail,
            actorId = operator.id,
            terminalId = review.terminalId,
            status = InventoryLedgerStatus.FINAL,
            createdAt = clock.now()
        )
        val applied = inventoryRepository.applyMutation(entry)
        inventoryRepository.resolveDiscrepancy(
            id = review.id,
            status = InventoryDiscrepancyStatus.RESOLVED_ADJUSTED,
            reasonCode = reason.code,
            reasonDetail = reasonDetail,
            resolvedBy = operator.id,
            resolutionNote = "Selisih stock opname disesuaikan eksplisit",
            relatedLedgerEntryId = applied.ledgerEntry.id
        )
        createInvestigationReviewIfNeeded(
            applied = applied,
            terminalId = review.terminalId,
            requestedBy = operator.id,
            note = "Resolusi stock opname membutuhkan investigasi layer lanjutan."
        )
        recordAudit("Review stock opname ${review.id} diselesaikan untuk ${review.productId}")
        recordEvent(
            eventId = "event_inventory_discrepancy_${review.id}",
            type = "INVENTORY_DISCREPANCY_RESOLVED",
            payload = """{"reviewId":"${review.id}","ledgerId":"${applied.ledgerEntry.id}","approvalMode":"LIGHT_PIN"}"""
        )
        applied
    }

    suspend fun markDiscrepancyForInvestigation(reviewId: String, note: String): Result<InventoryDiscrepancyReview> = runCatching {
        val operator = accessService.requireCapability(AccessCapability.APPROVE_STOCK_ADJUSTMENT).getOrThrow()
        val review = inventoryRepository.getDiscrepancyById(reviewId) ?: error("Review discrepancy tidak ditemukan")
        inventoryRepository.resolveDiscrepancy(
            id = review.id,
            status = InventoryDiscrepancyStatus.INVESTIGATION_REQUIRED,
            reasonCode = review.reasonCode,
            reasonDetail = review.reasonDetail,
            resolvedBy = operator.id,
            resolutionNote = note,
            relatedLedgerEntryId = review.relatedLedgerEntryId
        )
    }

    suspend fun getInventoryReadback(productId: String): Result<InventoryReadback?> = runCatching {
        accessService.requireCapability(AccessCapability.ACCESS_CATALOG).getOrThrow()
        inventoryRepository.getInventoryReadback(productId)
    }

    suspend fun listInventoryBalances(): Result<List<InventoryReadback>> = runCatching {
        accessService.requireCapability(AccessCapability.ACCESS_CATALOG).getOrThrow()
        inventoryRepository.listBalances().map { balance ->
            inventoryRepository.getInventoryReadback(balance.productId)
                ?: InventoryReadback(balance, emptyList(), emptyList())
        }
    }

    suspend fun listReasonCodes(): List<id.azureenterprise.cassy.kernel.domain.ReasonCode> {
        kernelRepository.ensureDefaultReasonCodes()
        return kernelRepository.listActiveReasonCodes(ReasonCategory.INVENTORY_ADJUSTMENT)
    }

    suspend fun listUnresolvedDiscrepancies(): List<InventoryDiscrepancyReview> {
        return inventoryRepository.listUnresolvedDiscrepancies()
    }

    fun classifyVoidImpact(
        paymentSettled: Boolean,
        physicalReturnConfirmed: Boolean,
        explicitInventoryReasonProvided: Boolean
    ): VoidImpactAssessment {
        return voidImpactPolicy.classify(
            paymentSettled = paymentSettled,
            physicalReturnConfirmed = physicalReturnConfirmed,
            explicitInventoryReasonProvided = explicitInventoryReasonProvided
        )
    }

    private suspend fun createInvestigationReviewIfNeeded(
        applied: AppliedInventoryMutation,
        terminalId: String,
        requestedBy: String,
        note: String
    ) {
        if (applied.remainingShortageQuantity <= 0.0 && applied.balance.quantity >= 0.0) {
            return
        }
        inventoryRepository.createDiscrepancy(
            InventoryDiscrepancyReview(
                id = IdGenerator.nextId("inv_issue"),
                productId = applied.ledgerEntry.productId,
                bookQuantity = applied.balance.quantity - applied.ledgerEntry.quantityDelta,
                countedQuantity = applied.balance.quantity,
                varianceQuantity = if (applied.remainingShortageQuantity > 0.0) {
                    applied.remainingShortageQuantity
                } else {
                    -applied.balance.quantity
                },
                status = InventoryDiscrepancyStatus.INVESTIGATION_REQUIRED,
                approvalMode = InventoryApprovalMode.LIGHT_PIN,
                sourceType = applied.ledgerEntry.sourceType,
                sourceId = applied.ledgerEntry.sourceId,
                sourceLineId = applied.ledgerEntry.sourceLineId,
                reasonCode = applied.ledgerEntry.reasonCode ?: "MANUAL_CORRECTION",
                reasonDetail = note,
                requestedBy = requestedBy,
                resolvedBy = null,
                terminalId = terminalId,
                createdAt = clock.now(),
                resolvedAt = null,
                resolutionNote = null,
                relatedLedgerEntryId = applied.ledgerEntry.id
            )
        )
    }

    private suspend fun recordAudit(message: String) {
        ignoreDuplicateConstraint {
            kernelRepository.insertAudit(
                id = IdGenerator.nextId("audit_inventory"),
                message = message,
                level = "INFO"
            )
        }
    }

    private suspend fun recordEvent(eventId: String, type: String, payload: String) {
        ignoreDuplicateConstraint {
            kernelRepository.insertEvent(
                id = eventId,
                type = type,
                payload = payload
            )
        }
    }

    private suspend fun ignoreDuplicateConstraint(block: suspend () -> Unit) {
        runCatching { block() }
            .getOrElse { error ->
                val normalized = error.message?.uppercase().orEmpty()
                if ("UNIQUE" !in normalized && "PRIMARY KEY" !in normalized) {
                    throw error
                }
            }
    }
}
