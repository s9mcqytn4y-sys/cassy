package id.azureenterprise.cassy.kernel.application

import id.azureenterprise.cassy.kernel.data.KernelRepository
import id.azureenterprise.cassy.kernel.domain.*
import kotlinx.datetime.Instant
import kotlinx.datetime.Clock

class FakeKernelRepository : id.azureenterprise.cassy.kernel.data.KernelRepository(
    database = null,
    ioDispatcher = kotlin.coroutines.EmptyCoroutineContext,
    clock = Clock.System
) {
    private var terminalBinding: TerminalBinding? = null
    private val storeProfiles = mutableMapOf<String, StoreProfile>()
    private var monotonicTick = 0L
    private val operators = mutableMapOf<String, OperatorAccount>()
    private var activeSession: AccessSession? = null
    private var activeBusinessDay: BusinessDay? = null
    private val shifts = mutableMapOf<String, Shift>()
    private val reasonCodes = mutableMapOf<String, ReasonCode>()
    private val approvals = mutableMapOf<String, ApprovalRequest>()
    private val cashMovements = mutableListOf<CashMovement>()
    private val shiftReports = mutableMapOf<String, ShiftCloseReport>()
    private val metadata = mutableMapOf<String, String>()
    val audits = mutableListOf<String>()
    val events = mutableListOf<String>()

    override suspend fun isBusinessDayOpen(): Boolean = activeBusinessDay?.status == "OPEN"
    override suspend fun getTerminalBinding(): TerminalBinding? = terminalBinding
    override suspend fun upsertTerminalBinding(binding: TerminalBinding) { terminalBinding = binding }
    override suspend fun getStoreProfile(storeId: String): StoreProfile? = storeProfiles[storeId]
    override suspend fun upsertStoreProfile(profile: StoreProfile) { storeProfiles[profile.storeId] = profile }
    override suspend fun listActiveOperators(): List<OperatorAccount> = operators.values.filter { it.isActive }
    override suspend fun getOperatorById(id: String): OperatorAccount? = operators[id]
    override suspend fun upsertOperator(operator: OperatorAccount) { operators[operator.id] = operator }
    override suspend fun updateOperatorAccessState(operatorId: String, failedAttempts: Int, lockedUntil: Instant?, lastLoginAt: Instant?) {
        operators[operatorId]?.let {
            operators[operatorId] = it.copy(failedAttempts = failedAttempts, lockedUntil = lockedUntil, lastLoginAt = lastLoginAt)
        }
    }
    override suspend fun getActiveAccessSession(): AccessSession? = activeSession
    override suspend fun createAccessSession(session: AccessSession) { activeSession = session }
    override suspend fun clearActiveAccessSessions() { activeSession = null }
    override suspend fun getActiveBusinessDay(): BusinessDay? = activeBusinessDay
    override suspend fun getBusinessDayById(id: String): BusinessDay? = if (activeBusinessDay?.id == id) activeBusinessDay else null
    override suspend fun openBusinessDay(id: String): BusinessDay {
        val day = BusinessDay(id, Clock.System.now(), null, "OPEN")
        activeBusinessDay = day
        return day
    }
    override suspend fun closeBusinessDay(id: String): BusinessDay {
        val day = activeBusinessDay?.copy(closedAt = Clock.System.now(), status = "CLOSED") ?: error("No active day")
        activeBusinessDay = day
        return day
    }
    override suspend fun getActiveShift(terminalId: String): Shift? = shifts.values.find { it.terminalId == terminalId && it.status == "OPEN" }
    override suspend fun getShiftById(id: String): Shift? = shifts[id]
    override suspend fun openShift(id: String, businessDayId: String, terminalId: String, openingCash: Double, openedBy: String): Shift {
        val shift = Shift(id, businessDayId, terminalId, nextInstant(), openingCash, null, 0.0, openedBy, null, "OPEN")
        shifts[id] = shift
        return shift
    }
    override suspend fun closeShift(id: String, closingCash: Double, closedBy: String): Shift {
        val shift = shifts[id]?.copy(closedAt = nextInstant(), closingCash = closingCash, closedBy = closedBy, status = "CLOSED") ?: error("No shift")
        shifts[id] = shift
        return shift
    }
    override suspend fun countOpenShiftsByBusinessDay(businessDayId: String): Long {
        return shifts.values.count { it.businessDayId == businessDayId && it.status == "OPEN" }.toLong()
    }
    override suspend fun listShiftsByBusinessDay(businessDayId: String): List<Shift> {
        return shifts.values.filter { it.businessDayId == businessDayId }
    }
    override suspend fun listShiftsByBusinessDayLatestFirst(businessDayId: String): List<Shift> {
        return shifts.values
            .filter { it.businessDayId == businessDayId }
            .sortedWith(
                compareByDescending<Shift> { it.openedAt }
                    .thenByDescending { it.id }
            )
    }
    override suspend fun ensureDefaultReasonCodes() {
        if (reasonCodes.isEmpty()) {
            listOf(
                ReasonCode("OPENING_NEEDS_CHANGE", ReasonCategory.OPENING_CASH_EXCEPTION, "Butuh pecahan pembukaan", true, true, 10),
                ReasonCode("FLOAT_TOP_UP", ReasonCategory.CASH_IN, "Top up modal receh", false, true, 10),
                ReasonCode("BANK_WITHDRAWAL", ReasonCategory.CASH_IN, "Ambil tunai dari bank", true, true, 20),
                ReasonCode("PETTY_CASH", ReasonCategory.CASH_OUT, "Kas kecil operasional", false, true, 10),
                ReasonCode("SUPPLIER_PAYMENT", ReasonCategory.CASH_OUT, "Bayar supplier tunai", true, true, 20),
                ReasonCode("VOID_CASH_REFUND", ReasonCategory.CASH_OUT, "Refund tunai karena void penjualan", false, true, 30),
                ReasonCode("SAFE_DROP_ROUTINE", ReasonCategory.SAFE_DROP, "Safe drop rutin", false, true, 10),
                ReasonCode("SAFE_DROP_OVERFLOW", ReasonCategory.SAFE_DROP, "Laci kas terlalu penuh", true, true, 20),
                ReasonCode("COUNTING_ERROR", ReasonCategory.SHIFT_CLOSE_VARIANCE, "Selisih hitung kas", false, true, 10),
                ReasonCode("UNRECORDED_DRAWER_ACTIVITY", ReasonCategory.SHIFT_CLOSE_VARIANCE, "Aktivitas laci kas belum tercatat", true, true, 20),
                ReasonCode("COUNT_VARIANCE", ReasonCategory.INVENTORY_ADJUSTMENT, "Selisih hasil stock opname", false, true, 10),
                ReasonCode("DAMAGED_STOCK", ReasonCategory.INVENTORY_ADJUSTMENT, "Barang rusak", true, true, 20),
                ReasonCode("FOUND_STOCK", ReasonCategory.INVENTORY_ADJUSTMENT, "Stok fisik ditemukan", false, true, 30),
                ReasonCode("MANUAL_CORRECTION", ReasonCategory.INVENTORY_ADJUSTMENT, "Koreksi manual terkontrol", true, true, 40),
                ReasonCode("VOID_DUPLICATE_INPUT", ReasonCategory.VOID_SALE, "Double input transaksi", false, true, 10),
                ReasonCode("VOID_OPERATOR_MISTAKE", ReasonCategory.VOID_SALE, "Kesalahan kasir sebelum barang benar-benar keluar", false, true, 20),
                ReasonCode("VOID_PRICE_CORRECTION", ReasonCategory.VOID_SALE, "Harga atau item perlu dikoreksi total", true, true, 30)
            ).forEach { reasonCodes[it.code] = it }
        }
    }
    override suspend fun listActiveReasonCodes(category: ReasonCategory): List<ReasonCode> {
        ensureDefaultReasonCodes()
        return reasonCodes.values.filter { it.category == category && it.isActive }.sortedBy { it.sortOrder }
    }
    override suspend fun getReasonCode(code: String): ReasonCode? {
        ensureDefaultReasonCodes()
        return reasonCodes[code]
    }
    override suspend fun insertApprovalRequest(
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
        decisionNote: String?
    ): ApprovalRequest {
        val request = ApprovalRequest(
            id = id,
            operationType = operationType,
            entityId = entityId,
            businessDayId = businessDayId,
            shiftId = shiftId,
            terminalId = terminalId,
            amount = amount,
            reasonCode = reasonCode,
            reasonDetail = reasonDetail,
            requestedBy = requestedBy,
            approvedBy = approvedBy,
            status = status,
            requestedAtEpochMs = Clock.System.now().toEpochMilliseconds(),
            decidedAtEpochMs = if (status == ApprovalStatus.REQUESTED) null else Clock.System.now().toEpochMilliseconds(),
            decisionNote = decisionNote
        )
        approvals[id] = request
        return request
    }
    override suspend fun getApprovalRequestById(id: String): ApprovalRequest? = approvals[id]
    override suspend fun listPendingApprovalRequests(): List<ApprovalRequest> = approvals.values.filter { it.status == ApprovalStatus.REQUESTED }
    override suspend fun countPendingApprovalRequestsByBusinessDay(businessDayId: String): Long {
        return approvals.values.count { it.businessDayId == businessDayId && it.status == ApprovalStatus.REQUESTED }.toLong()
    }
    override suspend fun resolveApprovalRequest(id: String, status: ApprovalStatus, approvedBy: String, decisionNote: String?): ApprovalRequest {
        val request = approvals[id] ?: error("No approval")
        val resolved = request.copy(
            status = status,
            approvedBy = approvedBy,
            decidedAtEpochMs = Clock.System.now().toEpochMilliseconds(),
            decisionNote = decisionNote
        )
        approvals[id] = resolved
        return resolved
    }
    override suspend fun insertCashMovement(
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
    ): CashMovement {
        val movement = CashMovement(
            id = id,
            businessDayId = businessDayId,
            shiftId = shiftId,
            terminalId = terminalId,
            type = type,
            amount = amount,
            reasonCode = reasonCode,
            reasonDetail = reasonDetail,
            approvalRequestId = approvalRequestId,
            performedBy = performedBy,
            createdAtEpochMs = Clock.System.now().toEpochMilliseconds()
        )
        cashMovements.add(movement)
        return movement
    }
    override suspend fun listCashMovementsByShift(shiftId: String): List<CashMovement> = cashMovements.filter { it.shiftId == shiftId }
    override suspend fun getCashMovementTotalsByShift(shiftId: String): CashMovementTotals {
        val entries = cashMovements.filter { it.shiftId == shiftId }
        return CashMovementTotals(
            cashInTotal = entries.filter { it.type == CashMovementType.CASH_IN }.sumOf { it.amount },
            cashOutTotal = entries.filter { it.type == CashMovementType.CASH_OUT }.sumOf { it.amount },
            safeDropTotal = entries.filter { it.type == CashMovementType.SAFE_DROP }.sumOf { it.amount }
        )
    }
    override suspend fun getCashMovementTotalsByMultiShift(shiftIds: List<String>): CashMovementTotals {
        val entries = cashMovements.filter { it.shiftId in shiftIds }
        return CashMovementTotals(
            cashInTotal = entries.filter { it.type == CashMovementType.CASH_IN }.sumOf { it.amount },
            cashOutTotal = entries.filter { it.type == CashMovementType.CASH_OUT }.sumOf { it.amount },
            safeDropTotal = entries.filter { it.type == CashMovementType.SAFE_DROP }.sumOf { it.amount }
        )
    }
    override suspend fun insertShiftCloseReport(
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
    ): ShiftCloseReport {
        val report = ShiftCloseReport(
            id = id,
            shiftId = shiftId,
            businessDayId = businessDayId,
            terminalId = terminalId,
            openingCash = openingCash,
            cashSalesTotal = cashSalesTotal,
            cashInTotal = cashInTotal,
            cashOutTotal = cashOutTotal,
            safeDropTotal = safeDropTotal,
            expectedCash = expectedCash,
            actualCash = actualCash,
            variance = variance,
            pendingTransactionCount = pendingTransactionCount,
            approvalRequestId = approvalRequestId,
            generatedBy = generatedBy,
            generatedAtEpochMs = Clock.System.now().toEpochMilliseconds()
        )
        shiftReports[shiftId] = report
        return report
    }
    override suspend fun getShiftCloseReport(shiftId: String): ShiftCloseReport? = shiftReports[shiftId]
    override suspend fun insertAudit(id: String, message: String, level: String) { audits.add(message) }
    override suspend fun insertEvent(id: String, type: String, payload: String) {
        events.add("$type|$payload")
    }
    override suspend fun getMetadata(key: String): String? = metadata[key]
    override suspend fun upsertMetadata(key: String, value: String) { metadata[key] = value }

    private fun nextInstant(): Instant {
        val now = Clock.System.now().toEpochMilliseconds() + monotonicTick
        monotonicTick += 1
        return Instant.fromEpochMilliseconds(now)
    }
}
