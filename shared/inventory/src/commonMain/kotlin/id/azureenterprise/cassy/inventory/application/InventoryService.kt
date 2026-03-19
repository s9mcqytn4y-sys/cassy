package id.azureenterprise.cassy.inventory.application

import id.azureenterprise.cassy.inventory.data.InventoryRepository
import id.azureenterprise.cassy.inventory.domain.AppliedInventoryMutation
import id.azureenterprise.cassy.inventory.domain.InventoryActionExecutionResult
import id.azureenterprise.cassy.inventory.domain.InventoryAdjustmentPolicy
import id.azureenterprise.cassy.inventory.domain.InventoryApprovalAction
import id.azureenterprise.cassy.inventory.domain.InventoryApprovalActionStatus
import id.azureenterprise.cassy.inventory.domain.InventoryApprovalActionType
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
import id.azureenterprise.cassy.kernel.domain.ApprovalStatus
import id.azureenterprise.cassy.kernel.domain.IdGenerator
import id.azureenterprise.cassy.kernel.domain.OperationType
import id.azureenterprise.cassy.kernel.domain.ReasonCategory
import id.azureenterprise.cassy.kernel.domain.supports
import kotlinx.datetime.Clock

class InventoryService(
    private val inventoryRepository: InventoryRepository,
    private val accessService: AccessService,
    private val kernelRepository: KernelRepository,
    private val voidImpactPolicy: InventoryVoidImpactPolicy,
    private val clock: Clock,
    private val policy: InventoryAdjustmentPolicy = InventoryAdjustmentPolicy()
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
                approvalMode = policy.shippedApprovalMode,
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

    suspend fun applyManualAdjustment(draft: StockAdjustmentDraft): InventoryActionExecutionResult {
        return runCatching {
            val context = requireInventoryOperationalContext()
            require(draft.productId.isNotBlank()) { "Produk wajib dipilih" }
            require(draft.quantityDelta != 0.0) { "Adjustment quantity tidak boleh nol" }
            require(draft.terminalId.isNotBlank()) { "Terminal wajib ada" }
            require(kotlin.math.abs(draft.quantityDelta) <= policy.hardLimitQuantityDelta) {
                "Adjustment melewati hard limit inventory."
            }
            kernelRepository.ensureDefaultReasonCodes()
            val reason = kernelRepository.getReasonCode(draft.reasonCode)
                ?: error("Reason code inventory tidak valid")
            check(reason.category == ReasonCategory.INVENTORY_ADJUSTMENT) { "Reason code inventory tidak valid" }

            val requiresApproval = reason.requiresApproval ||
                kotlin.math.abs(draft.quantityDelta) > policy.maxDirectQuantityWithoutApproval
            val canApproveNow = context.operator.role.supports(AccessCapability.APPROVE_STOCK_ADJUSTMENT)
            if (requiresApproval && !canApproveNow) {
                return requestApprovalForManualAdjustment(context, draft, reason.code)
            }

            val applied = applyManualAdjustmentNow(
                draft = draft,
                actorId = context.operator.id,
                approvalApplied = requiresApproval
            )
            InventoryActionExecutionResult.Applied(
                mutation = applied,
                approvalApplied = requiresApproval
            )
        }.getOrElse { InventoryActionExecutionResult.Blocked(it.message ?: "Adjustment inventory gagal") }
    }

    suspend fun resolveStockCount(
        reviewId: String,
        reasonCode: String,
        reasonDetail: String?
    ): InventoryActionExecutionResult {
        return runCatching {
            val context = requireInventoryOperationalContext()
            val review = inventoryRepository.getDiscrepancyById(reviewId) ?: error("Review discrepancy tidak ditemukan")
            require(review.status == InventoryDiscrepancyStatus.PENDING_REVIEW) { "Review discrepancy tidak lagi menunggu" }
            kernelRepository.ensureDefaultReasonCodes()
            val reason = kernelRepository.getReasonCode(reasonCode) ?: error("Reason code inventory tidak valid")
            check(reason.category == ReasonCategory.INVENTORY_ADJUSTMENT) { "Reason code inventory tidak valid" }

            val requiresApproval = reason.requiresApproval ||
                kotlin.math.abs(review.varianceQuantity) > policy.maxDiscrepancyResolutionWithoutApproval
            val canApproveNow = context.operator.role.supports(AccessCapability.APPROVE_STOCK_ADJUSTMENT)
            if (requiresApproval && !canApproveNow) {
                return requestApprovalForDiscrepancyResolution(
                    context = context,
                    review = review,
                    reasonCode = reason.code,
                    reasonDetail = reasonDetail
                )
            }

            val applied = resolveStockCountNow(
                review = review,
                reasonCode = reason.code,
                reasonDetail = reasonDetail,
                actorId = context.operator.id,
                approvalApplied = requiresApproval
            )
            InventoryActionExecutionResult.Applied(
                mutation = applied,
                approvalApplied = requiresApproval
            )
        }.getOrElse { InventoryActionExecutionResult.Blocked(it.message ?: "Resolve discrepancy gagal") }
    }

    suspend fun approvePendingAction(actionId: String): InventoryActionExecutionResult {
        return runCatching {
            val operator = accessService.requireCapability(AccessCapability.APPROVE_STOCK_ADJUSTMENT).getOrThrow()
            val action = inventoryRepository.getApprovalActionById(actionId)
                ?: error("Inventory approval action tidak ditemukan")
            require(action.status == InventoryApprovalActionStatus.REQUESTED) {
                "Inventory approval action sudah diproses"
            }
            action.approvalRequestId?.let { requestId ->
                val request = kernelRepository.getApprovalRequestById(requestId)
                    ?: error("Approval request inventory tidak ditemukan")
                if (request.status == ApprovalStatus.REQUESTED) {
                    kernelRepository.resolveApprovalRequest(
                        id = requestId,
                        status = ApprovalStatus.APPROVED,
                        approvedBy = operator.id,
                        decisionNote = "Light approval oleh ${operator.displayName}"
                    )
                }
            }
            val applied = when (action.actionType) {
                InventoryApprovalActionType.STOCK_ADJUSTMENT -> applyManualAdjustmentNow(
                    draft = StockAdjustmentDraft(
                        productId = action.productId,
                        quantityDelta = action.quantityDelta,
                        terminalId = action.terminalId,
                        reasonCode = action.reasonCode,
                        reasonDetail = action.reasonDetail
                    ),
                    actorId = operator.id,
                    approvalApplied = true,
                    sourceIdOverride = action.id
                )
                InventoryApprovalActionType.DISCREPANCY_RESOLUTION -> {
                    val review = action.discrepancyReviewId
                        ?.let { inventoryRepository.getDiscrepancyById(it) }
                        ?: error("Review discrepancy approval tidak ditemukan")
                    resolveStockCountNow(
                        review = review,
                        reasonCode = action.reasonCode,
                        reasonDetail = action.reasonDetail,
                        actorId = operator.id,
                        approvalApplied = true,
                        sourceIdOverride = action.id
                    )
                }
            }
            inventoryRepository.resolveApprovalAction(
                id = action.id,
                status = InventoryApprovalActionStatus.APPLIED,
                decidedBy = operator.id,
                decisionNote = "Light approval diterapkan",
                appliedLedgerEntryId = applied.ledgerEntry.id
            )
            InventoryActionExecutionResult.Applied(
                mutation = applied,
                approvalApplied = true
            )
        }.getOrElse { InventoryActionExecutionResult.Blocked(it.message ?: "Approve inventory action gagal") }
    }

    suspend fun denyPendingAction(actionId: String, decisionNote: String): Result<InventoryApprovalAction> = runCatching {
        val operator = accessService.requireCapability(AccessCapability.APPROVE_STOCK_ADJUSTMENT).getOrThrow()
        val action = inventoryRepository.getApprovalActionById(actionId)
            ?: error("Inventory approval action tidak ditemukan")
        require(action.status == InventoryApprovalActionStatus.REQUESTED) { "Inventory approval action sudah diproses" }
        action.approvalRequestId?.let { requestId ->
            val request = kernelRepository.getApprovalRequestById(requestId)
                ?: error("Approval request inventory tidak ditemukan")
            if (request.status == ApprovalStatus.REQUESTED) {
                kernelRepository.resolveApprovalRequest(
                    id = requestId,
                    status = ApprovalStatus.DENIED,
                    approvedBy = operator.id,
                    decisionNote = decisionNote.ifBlank { "Ditolak ${operator.displayName}" }
                )
            }
        }
        val denied = inventoryRepository.resolveApprovalAction(
            id = action.id,
            status = InventoryApprovalActionStatus.DENIED,
            decidedBy = operator.id,
            decisionNote = decisionNote.ifBlank { "Ditolak ${operator.displayName}" },
            appliedLedgerEntryId = null
        )
        recordAudit("Inventory approval action ${action.id} ditolak")
        recordEvent(
            eventId = "event_inventory_approval_denied_${action.id}",
            type = "INVENTORY_APPROVAL_DENIED",
            payload = """{"actionId":"${action.id}","actionType":"${action.actionType.name}"}"""
        )
        denied
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

    suspend fun listPendingApprovalActions(): List<InventoryApprovalAction> {
        return inventoryRepository.listPendingApprovalActions()
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

    private suspend fun requestApprovalForManualAdjustment(
        context: InventoryOperationalContext,
        draft: StockAdjustmentDraft,
        reasonCode: String
    ): InventoryActionExecutionResult.ApprovalRequired {
        val actionId = IdGenerator.nextId("inv_action")
        val approval = kernelRepository.insertApprovalRequest(
            id = IdGenerator.nextId("approval"),
            operationType = OperationType.STOCK_ADJUSTMENT,
            entityId = actionId,
            businessDayId = context.businessDayId,
            shiftId = context.shiftId,
            terminalId = context.terminalId,
            amount = kotlin.math.abs(draft.quantityDelta),
            reasonCode = reasonCode,
            reasonDetail = draft.reasonDetail,
            requestedBy = context.operator.id,
            approvedBy = null,
            status = ApprovalStatus.REQUESTED
        )
        val action = inventoryRepository.createApprovalAction(
            InventoryApprovalAction(
                id = actionId,
                approvalRequestId = approval.id,
                actionType = InventoryApprovalActionType.STOCK_ADJUSTMENT,
                productId = draft.productId,
                quantityDelta = draft.quantityDelta,
                discrepancyReviewId = null,
                reasonCode = reasonCode,
                reasonDetail = draft.reasonDetail,
                requestedBy = context.operator.id,
                terminalId = context.terminalId,
                approvalMode = policy.shippedApprovalMode,
                status = InventoryApprovalActionStatus.REQUESTED,
                createdAt = clock.now(),
                decidedAt = null,
                decidedBy = null,
                decisionNote = null,
                appliedLedgerEntryId = null
            )
        )
        recordAudit("Approval inventory diminta untuk adjustment $actionId pada ${draft.productId}")
        recordEvent(
            eventId = "event_inventory_approval_requested_$actionId",
            type = "INVENTORY_APPROVAL_REQUESTED",
            payload = """{"actionId":"$actionId","operationType":"STOCK_ADJUSTMENT","productId":"${draft.productId}","approvalMode":"${policy.shippedApprovalMode.name}"}"""
        )
        return InventoryActionExecutionResult.ApprovalRequired(
            action = action,
            message = "Adjustment inventory butuh LIGHT_PIN supervisor/owner. SECOND_PIN dan DUAL_AUTH belum shipped."
        )
    }

    private suspend fun requestApprovalForDiscrepancyResolution(
        context: InventoryOperationalContext,
        review: InventoryDiscrepancyReview,
        reasonCode: String,
        reasonDetail: String?
    ): InventoryActionExecutionResult.ApprovalRequired {
        val actionId = IdGenerator.nextId("inv_action")
        val approval = kernelRepository.insertApprovalRequest(
            id = IdGenerator.nextId("approval"),
            operationType = OperationType.RESOLVE_STOCK_DISCREPANCY,
            entityId = actionId,
            businessDayId = context.businessDayId,
            shiftId = context.shiftId,
            terminalId = context.terminalId,
            amount = kotlin.math.abs(review.varianceQuantity),
            reasonCode = reasonCode,
            reasonDetail = reasonDetail ?: review.reasonDetail,
            requestedBy = context.operator.id,
            approvedBy = null,
            status = ApprovalStatus.REQUESTED
        )
        val action = inventoryRepository.createApprovalAction(
            InventoryApprovalAction(
                id = actionId,
                approvalRequestId = approval.id,
                actionType = InventoryApprovalActionType.DISCREPANCY_RESOLUTION,
                productId = review.productId,
                quantityDelta = review.varianceQuantity,
                discrepancyReviewId = review.id,
                reasonCode = reasonCode,
                reasonDetail = reasonDetail ?: review.reasonDetail,
                requestedBy = context.operator.id,
                terminalId = context.terminalId,
                approvalMode = policy.shippedApprovalMode,
                status = InventoryApprovalActionStatus.REQUESTED,
                createdAt = clock.now(),
                decidedAt = null,
                decidedBy = null,
                decisionNote = null,
                appliedLedgerEntryId = null
            )
        )
        recordAudit("Approval inventory diminta untuk discrepancy ${review.id}")
        recordEvent(
            eventId = "event_inventory_discrepancy_approval_$actionId",
            type = "INVENTORY_DISCREPANCY_APPROVAL_REQUESTED",
            payload = """{"actionId":"$actionId","reviewId":"${review.id}","approvalMode":"${policy.shippedApprovalMode.name}"}"""
        )
        return InventoryActionExecutionResult.ApprovalRequired(
            action = action,
            message = "Resolusi discrepancy butuh LIGHT_PIN supervisor/owner. SECOND_PIN dan DUAL_AUTH belum shipped."
        )
    }

    private suspend fun applyManualAdjustmentNow(
        draft: StockAdjustmentDraft,
        actorId: String,
        approvalApplied: Boolean,
        sourceIdOverride: String? = null
    ): AppliedInventoryMutation {
        val sourceId = sourceIdOverride ?: IdGenerator.nextId("inv_adjustment")
        val entry = StockLedgerEntry(
            id = IdGenerator.nextId("stock_ledger"),
            productId = draft.productId,
            quantityDelta = draft.quantityDelta,
            mutationType = InventoryMutationType.STOCK_ADJUSTMENT,
            sourceType = InventorySourceType.MANUAL_STOCK_ADJUSTMENT,
            sourceId = sourceId,
            sourceLineId = null,
            reasonCode = draft.reasonCode,
            reasonDetail = draft.reasonDetail,
            actorId = actorId,
            terminalId = draft.terminalId,
            status = InventoryLedgerStatus.FINAL,
            createdAt = clock.now()
        )
        val applied = inventoryRepository.applyMutation(entry)
        createInvestigationReviewIfNeeded(
            applied = applied,
            terminalId = draft.terminalId,
            requestedBy = actorId,
            note = "Adjustment diterapkan tetapi provenance layer stok masih perlu investigasi."
        )
        recordAudit("Manual adjustment ${entry.sourceId} untuk ${draft.productId} delta ${draft.quantityDelta}")
        recordEvent(
            eventId = "event_inventory_adjustment_${entry.sourceId}",
            type = "INVENTORY_ADJUSTMENT_RECORDED",
            payload = """{"sourceId":"${entry.sourceId}","productId":"${entry.productId}","delta":${entry.quantityDelta},"approvalMode":"${policy.shippedApprovalMode.name}","approvalApplied":$approvalApplied}"""
        )
        return applied
    }

    private suspend fun resolveStockCountNow(
        review: InventoryDiscrepancyReview,
        reasonCode: String,
        reasonDetail: String?,
        actorId: String,
        approvalApplied: Boolean,
        sourceIdOverride: String? = null
    ): AppliedInventoryMutation {
        val entry = StockLedgerEntry(
            id = IdGenerator.nextId("stock_ledger"),
            productId = review.productId,
            quantityDelta = review.varianceQuantity,
            mutationType = InventoryMutationType.STOCK_OPNAME_ADJUSTMENT,
            sourceType = InventorySourceType.STOCK_OPNAME_RESOLUTION,
            sourceId = sourceIdOverride ?: review.id,
            sourceLineId = review.sourceLineId,
            reasonCode = reasonCode,
            reasonDetail = reasonDetail ?: review.reasonDetail,
            actorId = actorId,
            terminalId = review.terminalId,
            status = InventoryLedgerStatus.FINAL,
            createdAt = clock.now()
        )
        val applied = inventoryRepository.applyMutation(entry)
        inventoryRepository.resolveDiscrepancy(
            id = review.id,
            status = InventoryDiscrepancyStatus.RESOLVED_ADJUSTED,
            reasonCode = reasonCode,
            reasonDetail = reasonDetail,
            resolvedBy = actorId,
            resolutionNote = "Selisih stock opname disesuaikan eksplisit",
            relatedLedgerEntryId = applied.ledgerEntry.id
        )
        createInvestigationReviewIfNeeded(
            applied = applied,
            terminalId = review.terminalId,
            requestedBy = actorId,
            note = "Resolusi stock opname membutuhkan investigasi layer lanjutan."
        )
        recordAudit("Review stock opname ${review.id} diselesaikan untuk ${review.productId}")
        recordEvent(
            eventId = "event_inventory_discrepancy_${review.id}",
            type = "INVENTORY_DISCREPANCY_RESOLVED",
            payload = """{"reviewId":"${review.id}","ledgerId":"${applied.ledgerEntry.id}","approvalMode":"${policy.shippedApprovalMode.name}","approvalApplied":$approvalApplied}"""
        )
        return applied
    }

    private suspend fun requireInventoryOperationalContext(): InventoryOperationalContext {
        val context = accessService.restoreContext()
        val operator = context.activeOperator ?: error("Login operator diperlukan sebelum tindakan inventory.")
        val binding = context.terminalBinding ?: error("Terminal belum terikat ke store.")
        val businessDay = kernelRepository.getActiveBusinessDay()
            ?: error("Business day harus aktif sebelum tindakan inventory.")
        val shift = kernelRepository.getActiveShift(binding.terminalId)
            ?: error("Shift aktif diperlukan sebelum tindakan inventory.")
        return InventoryOperationalContext(
            operator = operator,
            terminalId = binding.terminalId,
            businessDayId = businessDay.id,
            shiftId = shift.id
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
                approvalMode = policy.shippedApprovalMode,
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

private data class InventoryOperationalContext(
    val operator: id.azureenterprise.cassy.kernel.domain.OperatorAccount,
    val terminalId: String,
    val businessDayId: String,
    val shiftId: String
)
