package id.azureenterprise.cassy.inventory.data

import id.azureenterprise.cassy.inventory.db.InventoryDatabase
import id.azureenterprise.cassy.inventory.domain.AppliedInventoryMutation
import id.azureenterprise.cassy.inventory.domain.InventoryApprovalMode
import id.azureenterprise.cassy.inventory.domain.InventoryApprovalAction
import id.azureenterprise.cassy.inventory.domain.InventoryApprovalActionStatus
import id.azureenterprise.cassy.inventory.domain.InventoryApprovalActionType
import id.azureenterprise.cassy.inventory.domain.InventoryBalanceSnapshot
import id.azureenterprise.cassy.inventory.domain.InventoryDiscrepancyReview
import id.azureenterprise.cassy.inventory.domain.InventoryDiscrepancyStatus
import id.azureenterprise.cassy.inventory.domain.InventoryLayerStatus
import id.azureenterprise.cassy.inventory.domain.InventoryLedgerStatus
import id.azureenterprise.cassy.inventory.domain.InventoryMutationType
import id.azureenterprise.cassy.inventory.domain.InventoryReadback
import id.azureenterprise.cassy.inventory.domain.InventorySourceType
import id.azureenterprise.cassy.inventory.domain.InventoryLayer
import id.azureenterprise.cassy.inventory.domain.RotationPolicy
import id.azureenterprise.cassy.inventory.domain.StockLedgerEntry
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.coroutines.CoroutineContext

class InventoryRepository(
    private val database: InventoryDatabase,
    private val ioDispatcher: CoroutineContext,
    private val clock: Clock
) {
    private val queries = database.inventoryDatabaseQueries

    suspend fun applyMutation(
        entry: StockLedgerEntry,
        rotationPolicyOverride: RotationPolicy? = null
    ): AppliedInventoryMutation = withContext(ioDispatcher) {
        var result: AppliedInventoryMutation? = null
        database.transaction {
            val timestamp = entry.createdAt.toEpochMilliseconds()
            queries.insertBalance(
                productId = entry.productId,
                lastUpdatedAt = timestamp
            )
            val existingBalance = queries.getBalance(entry.productId).executeAsOne()
            val rotationPolicy = rotationPolicyOverride ?: RotationPolicy.valueOf(existingBalance.rotationPolicy)
            if (rotationPolicyOverride != null && existingBalance.rotationPolicy != rotationPolicyOverride.name) {
                queries.setRotationPolicy(rotationPolicyOverride.name, timestamp, entry.productId)
            }

            val remainingShortage = when {
                entry.quantityDelta > 0.0 -> {
                    insertPositiveLayer(entry, rotationPolicy)
                    0.0
                }
                entry.quantityDelta < 0.0 -> consumeLayers(
                    productId = entry.productId,
                    requiredQuantity = -entry.quantityDelta,
                    rotationPolicy = rotationPolicy
                )
                else -> 0.0
            }

            queries.insertLedgerEntry(
                id = entry.id,
                productId = entry.productId,
                quantityDelta = entry.quantityDelta,
                mutationType = entry.mutationType.name,
                sourceType = entry.sourceType.name,
                sourceId = entry.sourceId,
                sourceLineId = entry.sourceLineId.orEmpty(),
                reasonCode = entry.reasonCode,
                reasonDetail = entry.reasonDetail,
                actorId = entry.actorId,
                terminalId = entry.terminalId,
                status = entry.status.name,
                createdAt = timestamp
            )
            queries.updateBalance(
                quantity = entry.quantityDelta,
                lastLedgerEntryId = entry.id,
                lastUpdatedAt = timestamp,
                productId = entry.productId
            )
            result = AppliedInventoryMutation(
                ledgerEntry = entry,
                balance = queries.getBalance(entry.productId).executeAsOne().toSnapshot(),
                remainingShortageQuantity = remainingShortage
            )
        }
        result ?: error("Mutasi inventory gagal diterapkan")
    }

    suspend fun createDiscrepancy(review: InventoryDiscrepancyReview): InventoryDiscrepancyReview = withContext(ioDispatcher) {
        queries.insertDiscrepancy(
            id = review.id,
            productId = review.productId,
            bookQuantity = review.bookQuantity,
            countedQuantity = review.countedQuantity,
            varianceQuantity = review.varianceQuantity,
            status = review.status.name,
            approvalMode = review.approvalMode.name,
            sourceType = review.sourceType.name,
            sourceId = review.sourceId,
            sourceLineId = review.sourceLineId.orEmpty(),
            reasonCode = review.reasonCode,
            reasonDetail = review.reasonDetail,
            requestedBy = review.requestedBy,
            resolvedBy = review.resolvedBy,
            terminalId = review.terminalId,
            createdAt = review.createdAt.toEpochMilliseconds(),
            resolvedAt = review.resolvedAt?.toEpochMilliseconds(),
            resolutionNote = review.resolutionNote,
            relatedLedgerEntryId = review.relatedLedgerEntryId
        )
        getDiscrepancyById(review.id) ?: error("Gagal menyimpan discrepancy review")
    }

    suspend fun getBalanceSnapshot(productId: String): InventoryBalanceSnapshot? = withContext(ioDispatcher) {
        queries.getBalance(productId).executeAsOneOrNull()?.toSnapshot()
    }

    suspend fun listBalances(): List<InventoryBalanceSnapshot> = withContext(ioDispatcher) {
        queries.listBalances().executeAsList().map { it.toSnapshot() }
    }

    suspend fun listLedgerByProduct(productId: String): List<StockLedgerEntry> = withContext(ioDispatcher) {
        queries.listLedgerByProduct(productId).executeAsList().map { record ->
            StockLedgerEntry(
                id = record.id,
                productId = record.productId,
                quantityDelta = record.quantityDelta,
                mutationType = InventoryMutationType.valueOf(record.mutationType),
                sourceType = InventorySourceType.valueOf(record.sourceType),
                sourceId = record.sourceId,
                sourceLineId = record.sourceLineId.takeIf(String::isNotBlank),
                reasonCode = record.reasonCode,
                reasonDetail = record.reasonDetail,
                actorId = record.actorId,
                terminalId = record.terminalId,
                status = InventoryLedgerStatus.valueOf(record.status),
                createdAt = Instant.fromEpochMilliseconds(record.createdAt)
            )
        }
    }

    suspend fun findLedgerEntry(
        productId: String,
        mutationType: InventoryMutationType,
        sourceType: InventorySourceType,
        sourceId: String,
        sourceLineId: String?
    ): StockLedgerEntry? = withContext(ioDispatcher) {
        listLedgerByProduct(productId).firstOrNull { entry ->
            entry.mutationType == mutationType &&
                entry.sourceType == sourceType &&
                entry.sourceId == sourceId &&
                entry.sourceLineId == sourceLineId
        }
    }

    suspend fun getDiscrepancyById(id: String): InventoryDiscrepancyReview? = withContext(ioDispatcher) {
        queries.getDiscrepancyById(id).executeAsOneOrNull()?.toDiscrepancy()
    }

    suspend fun listDiscrepanciesByProduct(productId: String): List<InventoryDiscrepancyReview> = withContext(ioDispatcher) {
        queries.listDiscrepanciesByProduct(productId).executeAsList().map { it.toDiscrepancy() }
    }

    suspend fun listUnresolvedDiscrepancies(): List<InventoryDiscrepancyReview> = withContext(ioDispatcher) {
        queries.listUnresolvedDiscrepancies().executeAsList().map { it.toDiscrepancy() }
    }

    suspend fun listActiveLayersByProduct(productId: String): List<InventoryLayer> = withContext(ioDispatcher) {
        queries.listActiveLayersByProduct(productId).executeAsList().map { it.toLayer() }
    }

    suspend fun createApprovalAction(action: InventoryApprovalAction): InventoryApprovalAction = withContext(ioDispatcher) {
        queries.insertApprovalAction(
            id = action.id,
            approvalRequestId = action.approvalRequestId,
            actionType = action.actionType.name,
            productId = action.productId,
            quantityDelta = action.quantityDelta,
            discrepancyReviewId = action.discrepancyReviewId,
            reasonCode = action.reasonCode,
            reasonDetail = action.reasonDetail,
            requestedBy = action.requestedBy,
            terminalId = action.terminalId,
            approvalMode = action.approvalMode.name,
            status = action.status.name,
            createdAt = action.createdAt.toEpochMilliseconds(),
            decidedAt = action.decidedAt?.toEpochMilliseconds(),
            decidedBy = action.decidedBy,
            decisionNote = action.decisionNote,
            appliedLedgerEntryId = action.appliedLedgerEntryId
        )
        getApprovalActionById(action.id) ?: error("Gagal menyimpan inventory approval action")
    }

    suspend fun getApprovalActionById(id: String): InventoryApprovalAction? = withContext(ioDispatcher) {
        queries.getApprovalActionById(id).executeAsOneOrNull()?.toApprovalAction()
    }

    suspend fun listPendingApprovalActions(): List<InventoryApprovalAction> = withContext(ioDispatcher) {
        queries.listPendingApprovalActions().executeAsList().map { it.toApprovalAction() }
    }

    suspend fun listApprovalActionsByProduct(productId: String): List<InventoryApprovalAction> = withContext(ioDispatcher) {
        queries.listApprovalActionsByProduct(productId).executeAsList().map { it.toApprovalAction() }
    }

    suspend fun resolveApprovalAction(
        id: String,
        status: InventoryApprovalActionStatus,
        decidedBy: String,
        decisionNote: String?,
        appliedLedgerEntryId: String?
    ): InventoryApprovalAction = withContext(ioDispatcher) {
        queries.resolveApprovalAction(
            status = status.name,
            decidedAt = clock.now().toEpochMilliseconds(),
            decidedBy = decidedBy,
            decisionNote = decisionNote,
            appliedLedgerEntryId = appliedLedgerEntryId,
            id = id
        )
        getApprovalActionById(id) ?: error("Gagal memperbarui inventory approval action")
    }

    suspend fun resolveDiscrepancy(
        id: String,
        status: InventoryDiscrepancyStatus,
        reasonCode: String?,
        reasonDetail: String?,
        resolvedBy: String,
        resolutionNote: String?,
        relatedLedgerEntryId: String?
    ): InventoryDiscrepancyReview = withContext(ioDispatcher) {
        queries.resolveDiscrepancy(
            status = status.name,
            reasonCode = reasonCode,
            reasonDetail = reasonDetail,
            resolvedBy = resolvedBy,
            resolvedAt = clock.now().toEpochMilliseconds(),
            resolutionNote = resolutionNote,
            relatedLedgerEntryId = relatedLedgerEntryId,
            id = id
        )
        getDiscrepancyById(id) ?: error("Gagal resolve discrepancy review")
    }

    suspend fun getInventoryReadback(productId: String): InventoryReadback? = withContext(ioDispatcher) {
        val balance = queries.getBalance(productId).executeAsOneOrNull()?.toSnapshot() ?: return@withContext null
        InventoryReadback(
            balance = balance,
            ledgerEntries = listLedgerByProduct(productId),
            discrepancies = listDiscrepanciesByProduct(productId)
        )
    }

    private fun insertPositiveLayer(
        entry: StockLedgerEntry,
        rotationPolicy: RotationPolicy
    ) {
        queries.insertLayer(
            id = "layer_${entry.id}",
            productId = entry.productId,
            sourceType = entry.sourceType.name,
            sourceId = entry.sourceId,
            sourceLineId = entry.sourceLineId.orEmpty(),
            acquiredQuantity = entry.quantityDelta,
            remainingQuantity = entry.quantityDelta,
            acquiredAt = entry.createdAt.toEpochMilliseconds(),
            expiryAt = null,
            rotationPolicy = rotationPolicy.name,
            status = InventoryLayerStatus.OPEN.name
        )
    }

    private fun consumeLayers(
        productId: String,
        requiredQuantity: Double,
        rotationPolicy: RotationPolicy
    ): Double {
        var remaining = requiredQuantity
        val layers = when (rotationPolicy) {
            RotationPolicy.FEFO -> queries.listFefoLayersByProduct(productId).executeAsList()
            RotationPolicy.FIFO,
            RotationPolicy.MANUAL -> queries.listActiveLayersByProduct(productId).executeAsList()
        }
        layers.forEach { layer ->
            if (remaining <= 0.0) return@forEach
            val available = layer.remainingQuantity
            if (available <= 0.0) return@forEach
            val consumed = minOf(available, remaining)
            val after = available - consumed
            queries.updateLayer(
                remainingQuantity = after,
                status = if (after <= 0.0) InventoryLayerStatus.CONSUMED.name else InventoryLayerStatus.OPEN.name,
                id = layer.id
            )
            remaining -= consumed
        }
        return remaining
    }

    private fun id.azureenterprise.cassy.inventory.db.InventoryBalance.toSnapshot(): InventoryBalanceSnapshot {
        return InventoryBalanceSnapshot(
            productId = productId,
            quantity = quantity,
            rotationPolicy = RotationPolicy.valueOf(rotationPolicy),
            lastLedgerEntryId = lastLedgerEntryId,
            lastUpdatedAt = Instant.fromEpochMilliseconds(lastUpdatedAt)
        )
    }

    private fun id.azureenterprise.cassy.inventory.db.InventoryDiscrepancyReview.toDiscrepancy(): InventoryDiscrepancyReview {
        return InventoryDiscrepancyReview(
            id = id,
            productId = productId,
            bookQuantity = bookQuantity,
            countedQuantity = countedQuantity,
            varianceQuantity = varianceQuantity,
            status = InventoryDiscrepancyStatus.valueOf(status),
            approvalMode = InventoryApprovalMode.valueOf(approvalMode),
            sourceType = InventorySourceType.valueOf(sourceType),
            sourceId = sourceId,
            sourceLineId = sourceLineId.takeIf(String::isNotBlank),
            reasonCode = reasonCode,
            reasonDetail = reasonDetail,
            requestedBy = requestedBy,
            resolvedBy = resolvedBy,
            terminalId = terminalId,
            createdAt = Instant.fromEpochMilliseconds(createdAt),
            resolvedAt = resolvedAt?.let(Instant::fromEpochMilliseconds),
            resolutionNote = resolutionNote,
            relatedLedgerEntryId = relatedLedgerEntryId
        )
    }

    private fun id.azureenterprise.cassy.inventory.db.InventoryLayer.toLayer(): InventoryLayer {
        return InventoryLayer(
            id = id,
            productId = productId,
            sourceType = InventorySourceType.valueOf(sourceType),
            sourceId = sourceId,
            sourceLineId = sourceLineId.takeIf(String::isNotBlank),
            acquiredQuantity = acquiredQuantity,
            remainingQuantity = remainingQuantity,
            acquiredAt = Instant.fromEpochMilliseconds(acquiredAt),
            expiryAt = expiryAt?.let(Instant::fromEpochMilliseconds),
            rotationPolicy = RotationPolicy.valueOf(rotationPolicy),
            status = InventoryLayerStatus.valueOf(status)
        )
    }

    private fun id.azureenterprise.cassy.inventory.db.InventoryApprovalAction.toApprovalAction(): InventoryApprovalAction {
        return InventoryApprovalAction(
            id = id,
            approvalRequestId = approvalRequestId,
            actionType = InventoryApprovalActionType.valueOf(actionType),
            productId = productId,
            quantityDelta = quantityDelta,
            discrepancyReviewId = discrepancyReviewId,
            reasonCode = reasonCode,
            reasonDetail = reasonDetail,
            requestedBy = requestedBy,
            terminalId = terminalId,
            approvalMode = InventoryApprovalMode.valueOf(approvalMode),
            status = InventoryApprovalActionStatus.valueOf(status),
            createdAt = Instant.fromEpochMilliseconds(createdAt),
            decidedAt = decidedAt?.let(Instant::fromEpochMilliseconds),
            decidedBy = decidedBy,
            decisionNote = decisionNote,
            appliedLedgerEntryId = appliedLedgerEntryId
        )
    }
}
