package id.azureenterprise.cassy.inventory.domain

import kotlinx.datetime.Instant

enum class InventoryApprovalMode {
    LIGHT_PIN,
    SECOND_PIN,
    DUAL_AUTH
}

enum class RotationPolicy {
    FIFO,
    FEFO,
    MANUAL
}

enum class InventoryMutationType {
    SALE_COMPLETION,
    STOCK_ADJUSTMENT,
    STOCK_OPNAME_ADJUSTMENT,
    MIGRATION_BASELINE
}

enum class InventorySourceType {
    SALE_FINALIZATION,
    MANUAL_STOCK_ADJUSTMENT,
    STOCK_OPNAME_COUNT,
    STOCK_OPNAME_RESOLUTION,
    MIGRATION_BALANCE_BASELINE,
    LEGACY_IMPORTED,
    RETURN_COMPLETION
}

enum class InventoryLedgerStatus {
    FINAL,
    UNRESOLVED,
    INVESTIGATION_REQUIRED
}

enum class InventoryDiscrepancyStatus {
    MATCHED,
    PENDING_REVIEW,
    RESOLVED_ADJUSTED,
    INVESTIGATION_REQUIRED
}

enum class InventoryLayerStatus {
    OPEN,
    CONSUMED
}

enum class InventoryVoidImpactClassification {
    PRE_SETTLEMENT_VOID_NO_STOCK_EFFECT,
    POST_SETTLEMENT_REVERSAL_CANDIDATE,
    RETURN_REQUIRED,
    MANUAL_INVESTIGATION_REQUIRED
}

data class InventoryBalanceSnapshot(
    val productId: String,
    val quantity: Double,
    val rotationPolicy: RotationPolicy,
    val lastLedgerEntryId: String?,
    val lastUpdatedAt: Instant
)

data class StockLedgerEntry(
    val id: String,
    val productId: String,
    val quantityDelta: Double,
    val mutationType: InventoryMutationType,
    val sourceType: InventorySourceType,
    val sourceId: String,
    val sourceLineId: String?,
    val reasonCode: String?,
    val reasonDetail: String?,
    val actorId: String?,
    val terminalId: String,
    val status: InventoryLedgerStatus,
    val createdAt: Instant
)

data class InventoryDiscrepancyReview(
    val id: String,
    val productId: String,
    val bookQuantity: Double,
    val countedQuantity: Double,
    val varianceQuantity: Double,
    val status: InventoryDiscrepancyStatus,
    val approvalMode: InventoryApprovalMode,
    val sourceType: InventorySourceType,
    val sourceId: String,
    val sourceLineId: String?,
    val reasonCode: String?,
    val reasonDetail: String?,
    val requestedBy: String,
    val resolvedBy: String?,
    val terminalId: String,
    val createdAt: Instant,
    val resolvedAt: Instant?,
    val resolutionNote: String?,
    val relatedLedgerEntryId: String?
)

data class InventoryLayer(
    val id: String,
    val productId: String,
    val sourceType: InventorySourceType,
    val sourceId: String,
    val sourceLineId: String?,
    val acquiredQuantity: Double,
    val remainingQuantity: Double,
    val acquiredAt: Instant,
    val expiryAt: Instant?,
    val rotationPolicy: RotationPolicy,
    val status: InventoryLayerStatus
)

data class InventoryReadback(
    val balance: InventoryBalanceSnapshot,
    val ledgerEntries: List<StockLedgerEntry>,
    val discrepancies: List<InventoryDiscrepancyReview>
)

data class AppliedInventoryMutation(
    val ledgerEntry: StockLedgerEntry,
    val balance: InventoryBalanceSnapshot,
    val remainingShortageQuantity: Double = 0.0
)

data class SaleInventoryLine(
    val productId: String,
    val quantity: Double,
    val sourceLineId: String? = null
)

data class StockCountDraft(
    val productId: String,
    val countedQuantity: Double,
    val terminalId: String
)

data class StockAdjustmentDraft(
    val productId: String,
    val quantityDelta: Double,
    val terminalId: String,
    val reasonCode: String,
    val reasonDetail: String?
)

data class VoidImpactAssessment(
    val classification: InventoryVoidImpactClassification,
    val message: String,
    val blocksInventoryMutation: Boolean
)
