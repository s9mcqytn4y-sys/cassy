package id.azureenterprise.cassy.kernel.data

import id.azureenterprise.cassy.kernel.db.KernelDatabase
import id.azureenterprise.cassy.kernel.domain.AccessSession
import id.azureenterprise.cassy.kernel.domain.ApprovalRequest
import id.azureenterprise.cassy.kernel.domain.ApprovalStatus
import id.azureenterprise.cassy.kernel.domain.CashMovement
import id.azureenterprise.cassy.kernel.domain.CashMovementTotals
import id.azureenterprise.cassy.kernel.domain.CashMovementType
import id.azureenterprise.cassy.kernel.domain.OperationType
import id.azureenterprise.cassy.kernel.domain.ReasonCategory
import id.azureenterprise.cassy.kernel.domain.ReasonCode
import id.azureenterprise.cassy.kernel.domain.ShiftCloseReport
import id.azureenterprise.cassy.kernel.domain.Shift
import id.azureenterprise.cassy.kernel.domain.OperatorAccount
import id.azureenterprise.cassy.kernel.domain.OperatorRole
import id.azureenterprise.cassy.kernel.domain.TerminalBinding
import id.azureenterprise.cassy.kernel.domain.BusinessDay
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

open class KernelRepository(
    private val database: KernelDatabase?,
    private val ioDispatcher: CoroutineContext,
    private val clock: Clock
) {
    private val queries = database?.kernelDatabaseQueries

    open suspend fun isBusinessDayOpen(): Boolean = withContext(ioDispatcher) {
        queries?.getActiveBusinessDay()?.executeAsOneOrNull() != null
    }

    open suspend fun getTerminalBinding(): TerminalBinding? = withContext(ioDispatcher) {
        queries?.getTerminalBinding()?.executeAsOneOrNull()?.let {
            TerminalBinding(
                storeId = it.storeId,
                storeName = it.storeName,
                terminalId = it.terminalId,
                terminalName = it.terminalName,
                boundAt = Instant.fromEpochMilliseconds(it.boundAt)
            )
        }
    }

    open suspend fun upsertTerminalBinding(binding: TerminalBinding) {
        withContext(ioDispatcher) {
            queries?.upsertTerminalBinding(
                binding.terminalId,
                binding.storeId,
                binding.storeName,
                binding.terminalName,
                binding.boundAt.toEpochMilliseconds()
            )
        }
    }

    open suspend fun listActiveOperators(): List<OperatorAccount> = withContext(ioDispatcher) {
        queries?.listActiveOperators()?.executeAsList()?.map { record ->
            OperatorAccount(
                id = record.id,
                employeeCode = record.employeeCode,
                displayName = record.displayName,
                role = OperatorRole.valueOf(record.role),
                pinHash = record.pinHash,
                pinSalt = record.pinSalt,
                failedAttempts = record.failedAttempts.toInt(),
                lockedUntil = record.lockedUntil?.let(Instant::fromEpochMilliseconds),
                isActive = record.isActive,
                lastLoginAt = record.lastLoginAt?.let(Instant::fromEpochMilliseconds)
            )
        } ?: emptyList()
    }

    open suspend fun getOperatorById(id: String): OperatorAccount? = withContext(ioDispatcher) {
        queries?.selectOperatorById(id)?.executeAsOneOrNull()?.let { record ->
            OperatorAccount(
                id = record.id,
                employeeCode = record.employeeCode,
                displayName = record.displayName,
                role = OperatorRole.valueOf(record.role),
                pinHash = record.pinHash,
                pinSalt = record.pinSalt,
                failedAttempts = record.failedAttempts.toInt(),
                lockedUntil = record.lockedUntil?.let(Instant::fromEpochMilliseconds),
                isActive = record.isActive,
                lastLoginAt = record.lastLoginAt?.let(Instant::fromEpochMilliseconds)
            )
        }
    }

    open suspend fun upsertOperator(operator: OperatorAccount) {
        withContext(ioDispatcher) {
            queries?.insertOperator(
                operator.id,
                operator.employeeCode,
                operator.displayName,
                operator.role.name,
                operator.pinHash,
                operator.pinSalt,
                operator.failedAttempts.toLong(),
                operator.lockedUntil?.toEpochMilliseconds(),
                operator.isActive,
                operator.lastLoginAt?.toEpochMilliseconds()
            )
        }
    }

    open suspend fun updateOperatorAccessState(
        operatorId: String,
        failedAttempts: Int,
        lockedUntil: Instant?,
        lastLoginAt: Instant?
    ) {
        withContext(ioDispatcher) {
            queries?.updateOperatorAccessState(
                failedAttempts.toLong(),
                lockedUntil?.toEpochMilliseconds(),
                lastLoginAt?.toEpochMilliseconds(),
                operatorId
            )
        }
    }

    open suspend fun getActiveAccessSession(): AccessSession? = withContext(ioDispatcher) {
        queries?.getActiveAccessSession()?.executeAsOneOrNull()?.let { record ->
            AccessSession(
                id = record.id,
                operatorId = record.operatorId,
                terminalId = record.terminalId,
                storeId = record.storeId,
                authMode = record.authMode,
                status = record.status,
                createdAt = Instant.fromEpochMilliseconds(record.createdAt),
                updatedAt = Instant.fromEpochMilliseconds(record.updatedAt)
            )
        }
    }

    open suspend fun createAccessSession(session: AccessSession) {
        withContext(ioDispatcher) {
            queries?.insertAccessSession(
                session.id,
                session.operatorId,
                session.terminalId,
                session.storeId,
                session.authMode,
                session.status,
                session.createdAt.toEpochMilliseconds(),
                session.updatedAt.toEpochMilliseconds()
            )
        }
    }

    open suspend fun clearActiveAccessSessions() {
        withContext(ioDispatcher) {
            queries?.clearActiveAccessSessions("TERMINATED", clock.now().toEpochMilliseconds())
        }
    }

    open suspend fun getActiveBusinessDay(): BusinessDay? = withContext(ioDispatcher) {
        queries?.getActiveBusinessDay()?.executeAsOneOrNull()?.let { record ->
            BusinessDay(
                id = record.id,
                openedAt = Instant.fromEpochMilliseconds(record.openedAt),
                closedAt = record.closedAt?.let(Instant::fromEpochMilliseconds),
                status = record.status
            )
        }
    }

    open suspend fun getBusinessDayById(id: String): BusinessDay? = withContext(ioDispatcher) {
        queries?.getBusinessDayById(id)?.executeAsOneOrNull()?.let { record ->
            BusinessDay(
                id = record.id,
                openedAt = Instant.fromEpochMilliseconds(record.openedAt),
                closedAt = record.closedAt?.let(Instant::fromEpochMilliseconds),
                status = record.status
            )
        }
    }

    open suspend fun openBusinessDay(id: String): BusinessDay = withContext(ioDispatcher) {
        queries?.insertBusinessDay(id, clock.now().toEpochMilliseconds(), "OPEN")
        getBusinessDayById(id) ?: error("Failed to open day")
    }

    open suspend fun closeBusinessDay(id: String): BusinessDay = withContext(ioDispatcher) {
        val closedAt = clock.now().toEpochMilliseconds()
        queries?.closeBusinessDay(closedAt, id)
        getBusinessDayById(id) ?: error("Failed to close day")
    }

    open suspend fun getActiveShift(terminalId: String): Shift? = withContext(ioDispatcher) {
        queries?.getActiveShift(terminalId)?.executeAsOneOrNull()?.let { record ->
            Shift(
                id = record.id,
                businessDayId = record.businessDayId,
                terminalId = record.terminalId,
                openedAt = Instant.fromEpochMilliseconds(record.openedAt),
                openingCash = record.openingCash,
                closedAt = record.closedAt?.let(Instant::fromEpochMilliseconds),
                closingCash = record.closingCash,
                openedBy = record.openedBy,
                closedBy = record.closedBy,
                status = record.status
            )
        }
    }

    open suspend fun getShiftById(id: String): Shift? = withContext(ioDispatcher) {
        queries?.getShiftById(id)?.executeAsOneOrNull()?.let { record ->
            Shift(
                id = record.id,
                businessDayId = record.businessDayId,
                terminalId = record.terminalId,
                openedAt = Instant.fromEpochMilliseconds(record.openedAt),
                openingCash = record.openingCash,
                closedAt = record.closedAt?.let(Instant::fromEpochMilliseconds),
                closingCash = record.closingCash,
                openedBy = record.openedBy,
                closedBy = record.closedBy,
                status = record.status
            )
        }
    }

    open suspend fun openShift(
        id: String,
        businessDayId: String,
        terminalId: String,
        openingCash: Double,
        openedBy: String
    ): Shift = withContext(ioDispatcher) {
        val openedAt = clock.now().toEpochMilliseconds()
        queries?.insertShift(id, businessDayId, terminalId, openedAt, openingCash, openedBy, "OPEN")
        getShiftById(id) ?: error("Failed to open shift")
    }

    open suspend fun closeShift(id: String, closingCash: Double, closedBy: String): Shift = withContext(ioDispatcher) {
        val closedAt = clock.now().toEpochMilliseconds()
        queries?.closeShift(closedAt, closingCash, closedBy, id)
        getShiftById(id) ?: error("Failed to close shift")
    }

    open suspend fun countOpenShiftsByBusinessDay(businessDayId: String): Long = withContext(ioDispatcher) {
        queries?.countOpenShiftsByBusinessDay(businessDayId)?.executeAsOneOrNull() ?: 0L
    }

    open suspend fun listShiftsByBusinessDay(businessDayId: String): List<Shift> = withContext(ioDispatcher) {
        queries?.listShiftsByBusinessDay(businessDayId)?.executeAsList()?.map { record ->
            Shift(
                id = record.id,
                businessDayId = record.businessDayId,
                terminalId = record.terminalId,
                openedAt = Instant.fromEpochMilliseconds(record.openedAt),
                openingCash = record.openingCash,
                closedAt = record.closedAt?.let(Instant::fromEpochMilliseconds),
                closingCash = record.closingCash,
                openedBy = record.openedBy,
                closedBy = record.closedBy,
                status = record.status
            )
        } ?: emptyList()
    }

    open suspend fun ensureDefaultReasonCodes() {
        withContext(ioDispatcher) {
            defaultReasonCodes.forEach { reason ->
                queries?.insertOrIgnoreReasonCode(
                    reason.code,
                    reason.category.name,
                    reason.title,
                    reason.requiresApproval,
                    reason.isActive,
                    reason.sortOrder.toLong()
                )
            }
        }
    }

    open suspend fun listActiveReasonCodes(category: ReasonCategory): List<ReasonCode> = withContext(ioDispatcher) {
        queries?.listActiveReasonCodesByCategory(category.name)?.executeAsList()?.map { record ->
            ReasonCode(
                code = record.code,
                category = ReasonCategory.valueOf(record.category),
                title = record.title,
                requiresApproval = record.requiresApproval,
                isActive = record.isActive,
                sortOrder = record.sortOrder.toInt()
            )
        } ?: emptyList()
    }

    open suspend fun getReasonCode(code: String): ReasonCode? {
        return listActiveReasonCodes(category = ReasonCategory.CASH_IN)
            .plus(listActiveReasonCodes(ReasonCategory.CASH_OUT))
            .plus(listActiveReasonCodes(ReasonCategory.SAFE_DROP))
            .plus(listActiveReasonCodes(ReasonCategory.SHIFT_CLOSE_VARIANCE))
            .plus(listActiveReasonCodes(ReasonCategory.OPENING_CASH_EXCEPTION))
            .plus(listActiveReasonCodes(ReasonCategory.INVENTORY_ADJUSTMENT))
            .firstOrNull { it.code == code }
    }

    open suspend fun insertApprovalRequest(
        id: String,
        operationType: OperationType,
        entityId: String,
        businessDayId: String,
        shiftId: String?,
        terminalId: String,
        amount: Double?,
        reasonCode: String,
        reasonDetail: String?,
        requestedBy: String,
        approvedBy: String?,
        status: ApprovalStatus,
        decisionNote: String? = null
    ): ApprovalRequest = withContext(ioDispatcher) {
        val requestedAt = clock.now().toEpochMilliseconds()
        val decidedAt = if (status == ApprovalStatus.REQUESTED) null else requestedAt
        queries?.insertApprovalRequest(
            id,
            operationType.name,
            entityId,
            businessDayId,
            shiftId,
            terminalId,
            amount,
            reasonCode,
            reasonDetail,
            requestedBy,
            approvedBy,
            status.name,
            requestedAt,
            decidedAt,
            decisionNote
        )
        getApprovalRequestById(id) ?: error("Failed to insert approval request")
    }

    open suspend fun getApprovalRequestById(id: String): ApprovalRequest? = withContext(ioDispatcher) {
        queries?.getApprovalRequestById(id)?.executeAsOneOrNull()?.toApprovalRequest()
    }

    open suspend fun listPendingApprovalRequests(): List<ApprovalRequest> = withContext(ioDispatcher) {
        queries?.listPendingApprovalRequests()?.executeAsList()?.map { it.toApprovalRequest() } ?: emptyList()
    }

    open suspend fun countPendingApprovalRequestsByBusinessDay(businessDayId: String): Long = withContext(ioDispatcher) {
        queries?.countPendingApprovalRequestsByBusinessDay(businessDayId)?.executeAsOneOrNull() ?: 0L
    }

    open suspend fun resolveApprovalRequest(
        id: String,
        status: ApprovalStatus,
        approvedBy: String,
        decisionNote: String?
    ): ApprovalRequest = withContext(ioDispatcher) {
        queries?.resolveApprovalRequest(
            status.name,
            approvedBy,
            clock.now().toEpochMilliseconds(),
            decisionNote,
            id
        )
        getApprovalRequestById(id) ?: error("Failed to resolve approval request")
    }

    open suspend fun insertCashMovement(
        id: String,
        businessDayId: String,
        shiftId: String,
        terminalId: String,
        type: CashMovementType,
        amount: Double,
        reasonCode: String,
        reasonDetail: String?,
        approvalRequestId: String?,
        performedBy: String
    ): CashMovement = withContext(ioDispatcher) {
        val createdAt = clock.now().toEpochMilliseconds()
        queries?.insertCashMovement(
            id,
            businessDayId,
            shiftId,
            terminalId,
            type.name,
            amount,
            reasonCode,
            reasonDetail,
            approvalRequestId,
            performedBy,
            createdAt
        )
        listCashMovementsByShift(shiftId).firstOrNull { it.id == id } ?: error("Failed to insert cash movement")
    }

    open suspend fun listCashMovementsByShift(shiftId: String): List<CashMovement> = withContext(ioDispatcher) {
        queries?.listCashMovementsByShift(shiftId)?.executeAsList()?.map { record ->
            CashMovement(
                id = record.id,
                businessDayId = record.businessDayId,
                shiftId = record.shiftId,
                terminalId = record.terminalId,
                type = CashMovementType.valueOf(record.type),
                amount = record.amount,
                reasonCode = record.reasonCode,
                reasonDetail = record.reasonDetail,
                approvalRequestId = record.approvalRequestId,
                performedBy = record.performedBy,
                createdAtEpochMs = record.createdAt
            )
        } ?: emptyList()
    }

    open suspend fun getCashMovementTotalsByShift(shiftId: String): CashMovementTotals = withContext(ioDispatcher) {
        CashMovementTotals(
            cashInTotal = queries?.sumCashMovementAmountByShiftAndType(shiftId, CashMovementType.CASH_IN.name)?.executeAsOneOrNull() ?: 0.0,
            cashOutTotal = queries?.sumCashMovementAmountByShiftAndType(shiftId, CashMovementType.CASH_OUT.name)?.executeAsOneOrNull() ?: 0.0,
            safeDropTotal = queries?.sumCashMovementAmountByShiftAndType(shiftId, CashMovementType.SAFE_DROP.name)?.executeAsOneOrNull() ?: 0.0
        )
    }

    open suspend fun getCashMovementTotalsByMultiShift(shiftIds: List<String>): CashMovementTotals = withContext(ioDispatcher) {
        if (shiftIds.isEmpty()) return@withContext CashMovementTotals(0.0, 0.0, 0.0)
        CashMovementTotals(
            cashInTotal = queries?.sumCashMovementAmountByMultiShiftAndType(shiftIds, CashMovementType.CASH_IN.name)?.executeAsOneOrNull() ?: 0.0,
            cashOutTotal = queries?.sumCashMovementAmountByMultiShiftAndType(shiftIds, CashMovementType.CASH_OUT.name)?.executeAsOneOrNull() ?: 0.0,
            safeDropTotal = queries?.sumCashMovementAmountByMultiShiftAndType(shiftIds, CashMovementType.SAFE_DROP.name)?.executeAsOneOrNull() ?: 0.0
        )
    }

    open suspend fun insertShiftCloseReport(
        id: String,
        shiftId: String,
        businessDayId: String,
        terminalId: String,
        openingCash: Double,
        cashSalesTotal: Double,
        cashInTotal: Double,
        cashOutTotal: Double,
        safeDropTotal: Double,
        expectedCash: Double,
        actualCash: Double,
        variance: Double,
        pendingTransactionCount: Int,
        approvalRequestId: String?,
        generatedBy: String
    ): ShiftCloseReport = withContext(ioDispatcher) {
        val generatedAt = clock.now().toEpochMilliseconds()
        queries?.insertShiftCloseReport(
            id,
            shiftId,
            businessDayId,
            terminalId,
            openingCash,
            cashSalesTotal,
            cashInTotal,
            cashOutTotal,
            safeDropTotal,
            expectedCash,
            actualCash,
            variance,
            pendingTransactionCount.toLong(),
            approvalRequestId,
            generatedBy,
            generatedAt
        )
        getShiftCloseReport(shiftId) ?: error("Failed to insert shift close report")
    }

    open suspend fun getShiftCloseReport(shiftId: String): ShiftCloseReport? = withContext(ioDispatcher) {
        queries?.getShiftCloseReport(shiftId)?.executeAsOneOrNull()?.let { record ->
            ShiftCloseReport(
                id = record.id,
                shiftId = record.shiftId,
                businessDayId = record.businessDayId,
                terminalId = record.terminalId,
                openingCash = record.openingCash,
                cashSalesTotal = record.cashSalesTotal,
                cashInTotal = record.cashInTotal,
                cashOutTotal = record.cashOutTotal,
                safeDropTotal = record.safeDropTotal,
                expectedCash = record.expectedCash,
                actualCash = record.actualCash,
                variance = record.variance,
                pendingTransactionCount = record.pendingTransactionCount.toInt(),
                approvalRequestId = record.approvalRequestId,
                generatedBy = record.generatedBy,
                generatedAtEpochMs = record.generatedAt
            )
        }
    }

    open suspend fun insertAudit(id: String, message: String, level: String) {
        withContext(ioDispatcher) {
            queries?.insertAudit(id, clock.now().toEpochMilliseconds(), message, level)
        }
    }

    open suspend fun insertEvent(id: String, type: String, payload: String) {
        withContext(ioDispatcher) {
            queries?.insertEvent(id, clock.now().toEpochMilliseconds(), type, payload, "PENDING")
        }
    }

    open suspend fun getMetadata(key: String): String? = withContext(ioDispatcher) {
        queries?.getMetadata(key)?.executeAsOneOrNull()
    }

    open suspend fun upsertMetadata(key: String, value: String): Unit = withContext(ioDispatcher) {
        queries?.upsertMetadata(key, value)
    }

    private fun id.azureenterprise.cassy.kernel.db.ApprovalRequest.toApprovalRequest(): ApprovalRequest {
        return ApprovalRequest(
            id = id,
            operationType = OperationType.valueOf(operationType),
            entityId = entityId,
            businessDayId = businessDayId,
            shiftId = shiftId,
            terminalId = terminalId,
            amount = amount,
            reasonCode = reasonCode,
            reasonDetail = reasonDetail,
            requestedBy = requestedBy,
            approvedBy = approvedBy,
            status = ApprovalStatus.valueOf(status),
            requestedAtEpochMs = requestedAt,
            decidedAtEpochMs = decidedAt,
            decisionNote = decisionNote
        )
    }

    private companion object {
        val defaultReasonCodes = listOf(
            ReasonCode("OPENING_NEEDS_CHANGE", ReasonCategory.OPENING_CASH_EXCEPTION, "Butuh pecahan pembukaan", true, true, 10),
            ReasonCode("OPENING_EVENT_DAY", ReasonCategory.OPENING_CASH_EXCEPTION, "Prediksi hari ramai", true, true, 20),
            ReasonCode("FLOAT_TOP_UP", ReasonCategory.CASH_IN, "Top up modal receh", false, true, 10),
            ReasonCode("BANK_WITHDRAWAL", ReasonCategory.CASH_IN, "Ambil tunai dari bank", true, true, 20),
            ReasonCode("PETTY_CASH", ReasonCategory.CASH_OUT, "Kas kecil operasional", false, true, 10),
            ReasonCode("SUPPLIER_PAYMENT", ReasonCategory.CASH_OUT, "Bayar supplier tunai", true, true, 20),
            ReasonCode("SAFE_DROP_ROUTINE", ReasonCategory.SAFE_DROP, "Safe drop rutin", false, true, 10),
            ReasonCode("SAFE_DROP_OVERFLOW", ReasonCategory.SAFE_DROP, "Laci kas terlalu penuh", true, true, 20),
            ReasonCode("COUNTING_ERROR", ReasonCategory.SHIFT_CLOSE_VARIANCE, "Selisih hitung kas", false, true, 10),
            ReasonCode("UNRECORDED_DRAWER_ACTIVITY", ReasonCategory.SHIFT_CLOSE_VARIANCE, "Aktivitas laci kas belum tercatat", true, true, 20),
            ReasonCode("COUNT_VARIANCE", ReasonCategory.INVENTORY_ADJUSTMENT, "Selisih hasil stock opname", false, true, 10),
            ReasonCode("DAMAGED_STOCK", ReasonCategory.INVENTORY_ADJUSTMENT, "Barang rusak", true, true, 20),
            ReasonCode("FOUND_STOCK", ReasonCategory.INVENTORY_ADJUSTMENT, "Stok fisik ditemukan", false, true, 30),
            ReasonCode("MANUAL_CORRECTION", ReasonCategory.INVENTORY_ADJUSTMENT, "Koreksi manual terkontrol", true, true, 40)
        )
    }
}
