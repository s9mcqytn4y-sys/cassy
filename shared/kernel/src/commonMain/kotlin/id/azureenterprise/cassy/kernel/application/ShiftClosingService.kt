package id.azureenterprise.cassy.kernel.application

import id.azureenterprise.cassy.kernel.data.KernelRepository
import id.azureenterprise.cassy.kernel.domain.AccessCapability
import id.azureenterprise.cassy.kernel.domain.ApprovalStatus
import id.azureenterprise.cassy.kernel.domain.IdGenerator
import id.azureenterprise.cassy.kernel.domain.OperationBlockerCode
import id.azureenterprise.cassy.kernel.domain.OperationDecision
import id.azureenterprise.cassy.kernel.domain.OperationStatus
import id.azureenterprise.cassy.kernel.domain.OperationType
import id.azureenterprise.cassy.kernel.domain.OperatorAccount
import id.azureenterprise.cassy.kernel.domain.PendingApprovalSummary
import id.azureenterprise.cassy.kernel.domain.ReasonCategory
import id.azureenterprise.cassy.kernel.domain.ShiftCloseExecutionResult
import id.azureenterprise.cassy.kernel.domain.ShiftClosePolicy
import id.azureenterprise.cassy.kernel.domain.ShiftCloseReport
import id.azureenterprise.cassy.kernel.domain.ShiftCloseReview
import id.azureenterprise.cassy.kernel.domain.supports
import kotlin.math.abs

class ShiftClosingService(
    private val kernelRepository: KernelRepository,
    private val accessService: AccessService,
    private val salesPort: OperationalSalesPort,
    private val policy: ShiftClosePolicy = ShiftClosePolicy()
) {
    suspend fun evaluateCloseShiftReadiness(): OperationDecision {
        val binding = kernelRepository.getTerminalBinding()
            ?: return OperationDecision(
                type = OperationType.CLOSE_SHIFT,
                status = OperationStatus.BLOCKED,
                title = "Tutup Shift",
                message = "Terminal belum terikat ke store.",
                blockerCode = OperationBlockerCode.TERMINAL_NOT_BOUND
            )
        val shift = kernelRepository.getActiveShift(binding.terminalId)
            ?: return OperationDecision(
                type = OperationType.CLOSE_SHIFT,
                status = OperationStatus.BLOCKED,
                title = "Tutup Shift",
                message = "Shift aktif belum ada.",
                blockerCode = OperationBlockerCode.SHIFT_REQUIRED
            )
        val summary = salesPort.getShiftSalesSummary(shift.id)
        return if (summary.pendingTransactions.isNotEmpty()) {
            OperationDecision(
                type = OperationType.CLOSE_SHIFT,
                status = OperationStatus.BLOCKED,
                title = "Tutup Shift",
                message = "Masih ada transaksi pending. Selesaikan atau batalkan dulu sebelum tutup shift.",
                blockerCode = OperationBlockerCode.PENDING_TRANSACTION_PRESENT,
                actionLabel = "Review Pending"
            )
        } else {
            OperationDecision(
                type = OperationType.CLOSE_SHIFT,
                status = OperationStatus.READY,
                title = "Tutup Shift",
                message = "Wizard tutup shift siap dibuka untuk review kas akhir.",
                actionLabel = "Buka Wizard"
            )
        }
    }

    suspend fun listPendingApprovals(): List<PendingApprovalSummary> {
        return kernelRepository.listPendingApprovalRequests()
            .filter { it.operationType == OperationType.CLOSE_SHIFT }
            .map { request ->
                PendingApprovalSummary(
                    id = request.id,
                    type = request.operationType,
                    title = "Approval Tutup Shift",
                    detail = request.reasonDetail ?: request.reasonCode,
                    amount = request.amount,
                    requestedBy = request.requestedBy
                )
            }
    }

    suspend fun listVarianceReasonCodes() = run {
        kernelRepository.ensureDefaultReasonCodes()
        kernelRepository.listActiveReasonCodes(ReasonCategory.SHIFT_CLOSE_VARIANCE)
    }

    suspend fun reviewCloseShift(actualCash: Double?): ShiftCloseReview {
        val binding = kernelRepository.getTerminalBinding()
        val shift = binding?.let { kernelRepository.getActiveShift(it.terminalId) }
        if (binding == null || shift == null) {
            return ShiftCloseReview(
                shiftId = "",
                businessDayId = "",
                terminalId = binding?.terminalId.orEmpty(),
                openingCash = 0.0,
                cashSalesTotal = 0.0,
                cashMovementTotals = kernelRepository.getCashMovementTotalsByShift(""),
                expectedCash = 0.0,
                actualCash = actualCash,
                variance = null,
                pendingTransactions = emptyList(),
                decision = OperationDecision(
                    type = OperationType.CLOSE_SHIFT,
                    status = OperationStatus.BLOCKED,
                    title = "Tutup Shift",
                    message = "Shift aktif belum ada.",
                    blockerCode = OperationBlockerCode.SHIFT_REQUIRED
                )
            )
        }
        val salesSummary = salesPort.getShiftSalesSummary(shift.id)
        val movementTotals = kernelRepository.getCashMovementTotalsByShift(shift.id)
        val expectedCash = shift.openingCash +
            movementTotals.cashInTotal +
            salesSummary.completedCashSalesTotal -
            movementTotals.cashOutTotal -
            movementTotals.safeDropTotal
        val variance = actualCash?.minus(expectedCash)
        val decision = evaluateReviewDecision(
            actualCash = actualCash,
            variance = variance,
            pendingCount = salesSummary.pendingTransactions.size
        )
        return ShiftCloseReview(
            shiftId = shift.id,
            businessDayId = shift.businessDayId,
            terminalId = shift.terminalId,
            openingCash = shift.openingCash,
            cashSalesTotal = salesSummary.completedCashSalesTotal,
            cashMovementTotals = movementTotals,
            expectedCash = expectedCash,
            actualCash = actualCash,
            variance = variance,
            pendingTransactions = salesSummary.pendingTransactions,
            decision = decision
        )
    }

    suspend fun closeShift(
        actualCash: Double,
        reasonCode: String = "",
        reasonDetail: String = ""
    ): ShiftCloseExecutionResult {
        val review = reviewCloseShift(actualCash)
        when (review.decision.status) {
            OperationStatus.BLOCKED,
            OperationStatus.UNAVAILABLE,
            OperationStatus.COMPLETED -> return ShiftCloseExecutionResult.Blocked(review.decision)
            OperationStatus.READY,
            OperationStatus.REQUIRES_APPROVAL -> Unit
        }

        val operator = accessService.requireCapability(AccessCapability.END_SHIFT).getOrElse {
            return ShiftCloseExecutionResult.Blocked(
                OperationDecision(
                    type = OperationType.CLOSE_SHIFT,
                    status = OperationStatus.BLOCKED,
                    title = "Tutup Shift",
                    message = it.message ?: "Operator aktif tidak diizinkan menutup shift.",
                    blockerCode = OperationBlockerCode.CAPABILITY_DENIED
                )
            )
        }
        val shift = kernelRepository.getShiftById(review.shiftId) ?: return ShiftCloseExecutionResult.Blocked(review.decision)
        val absoluteVariance = abs(review.variance ?: 0.0)
        if (absoluteVariance > policy.varianceTolerance) {
            kernelRepository.ensureDefaultReasonCodes()
            val validReason = kernelRepository.listActiveReasonCodes(ReasonCategory.SHIFT_CLOSE_VARIANCE)
                .any { it.code == reasonCode }
            if (!validReason) {
                return ShiftCloseExecutionResult.Blocked(
                    OperationDecision(
                        type = OperationType.CLOSE_SHIFT,
                        status = OperationStatus.BLOCKED,
                        title = "Tutup Shift",
                        message = "Reason code selisih kas wajib valid sebelum tutup shift.",
                        blockerCode = OperationBlockerCode.REASON_CODE_REQUIRED,
                        actionLabel = "Pilih Reason Code"
                    )
                )
            }
        }

        if (review.decision.status == OperationStatus.REQUIRES_APPROVAL) {
            val approval = kernelRepository.insertApprovalRequest(
                id = IdGenerator.nextId("approval"),
                operationType = OperationType.CLOSE_SHIFT,
                entityId = shift.id,
                businessDayId = shift.businessDayId,
                shiftId = shift.id,
                terminalId = shift.terminalId,
                amount = actualCash,
                reasonCode = reasonCode,
                reasonDetail = reasonDetail.takeIf { it.isNotBlank() },
                requestedBy = operator.id,
                approvedBy = null,
                status = ApprovalStatus.REQUESTED
            )
            kernelRepository.insertAudit(
                id = IdGenerator.nextId("audit"),
                message = "Approval diminta untuk tutup shift ${shift.id} dengan selisih kas ${review.variance}.",
                level = "WARN"
            )
            return ShiftCloseExecutionResult.ApprovalRequired(review.decision, approval.id)
        }

        val report = createReport(
            shiftId = shift.id,
            businessDayId = shift.businessDayId,
            terminalId = shift.terminalId,
            openingCash = review.openingCash,
            cashSalesTotal = review.cashSalesTotal,
            totals = review.cashMovementTotals,
            expectedCash = review.expectedCash,
            actualCash = actualCash,
            variance = review.variance ?: 0.0,
            pendingTransactionCount = review.pendingTransactions.size,
            approvalRequestId = null,
            generatedBy = operator.id
        )
        val closed = kernelRepository.closeShift(shift.id, actualCash, operator.id)
        kernelRepository.insertAudit(
            id = IdGenerator.nextId("audit"),
            message = "Shift ${closed.id} ditutup. Expected ${report.expectedCash}, actual ${report.actualCash}, variance ${report.variance}.",
            level = "INFO"
        )
        kernelRepository.insertEvent(
            id = IdGenerator.nextId("event"),
            type = "SHIFT_CLOSED",
            payload = """{"shiftId":"${closed.id}","reportId":"${report.id}","variance":${report.variance}}"""
        )
        return ShiftCloseExecutionResult.Closed(closed, report, approvalApplied = false)
    }

    suspend fun approveCloseShift(
        requestId: String,
        approverOverride: OperatorAccount? = null
    ): ShiftCloseExecutionResult {
        val request = kernelRepository.getApprovalRequestById(requestId)
            ?: return ShiftCloseExecutionResult.Blocked(
                OperationDecision(
                    type = OperationType.CLOSE_SHIFT,
                    status = OperationStatus.BLOCKED,
                    title = "Tutup Shift",
                    message = "Approval request tidak ditemukan.",
                    blockerCode = OperationBlockerCode.TERMINAL_NOT_BOUND
                )
            )
        if (request.status != ApprovalStatus.REQUESTED) {
            return ShiftCloseExecutionResult.Blocked(
                OperationDecision(
                    type = OperationType.CLOSE_SHIFT,
                    status = OperationStatus.COMPLETED,
                    title = "Tutup Shift",
                    message = "Approval request ${request.id} sudah diproses."
                )
            )
        }
        val approver = resolveApprover(
            capability = AccessCapability.APPROVE_SHIFT_CLOSE_EXCEPTION,
            operatorOverride = approverOverride
        ).getOrElse {
            return ShiftCloseExecutionResult.Blocked(
                OperationDecision(
                    type = OperationType.CLOSE_SHIFT,
                    status = OperationStatus.BLOCKED,
                    title = "Tutup Shift",
                    message = it.message ?: "Supervisor/owner diperlukan untuk approve close shift.",
                    blockerCode = OperationBlockerCode.CAPABILITY_DENIED
                )
            )
        }
        val shift = request.shiftId?.let { kernelRepository.getShiftById(it) }
            ?: return ShiftCloseExecutionResult.Blocked(
                OperationDecision(
                    type = OperationType.CLOSE_SHIFT,
                    status = OperationStatus.BLOCKED,
                    title = "Tutup Shift",
                    message = "Shift untuk approval tidak ditemukan.",
                    blockerCode = OperationBlockerCode.SHIFT_REQUIRED
                )
            )
        val review = reviewCloseShift(request.amount)
        val resolved = kernelRepository.resolveApprovalRequest(
            id = request.id,
            status = ApprovalStatus.APPROVED,
            approvedBy = approver.id,
            decisionNote = "Approved by ${approver.displayName}"
        )
        val report = createReport(
            shiftId = shift.id,
            businessDayId = shift.businessDayId,
            terminalId = shift.terminalId,
            openingCash = review.openingCash,
            cashSalesTotal = review.cashSalesTotal,
            totals = review.cashMovementTotals,
            expectedCash = review.expectedCash,
            actualCash = requireNotNull(request.amount),
            variance = review.variance ?: 0.0,
            pendingTransactionCount = review.pendingTransactions.size,
            approvalRequestId = resolved.id,
            generatedBy = request.requestedBy
        )
        val closed = kernelRepository.closeShift(shift.id, requireNotNull(request.amount), approver.id)
        kernelRepository.insertAudit(
            id = IdGenerator.nextId("audit"),
            message = "Shift ${closed.id} ditutup dengan approval ${resolved.id}. Variance ${report.variance}.",
            level = "INFO"
        )
        return ShiftCloseExecutionResult.Closed(closed, report, approvalApplied = true)
    }

    suspend fun denyCloseShift(
        requestId: String,
        decisionNote: String,
        approverOverride: OperatorAccount? = null
    ): Boolean {
        val approver = resolveApprover(
            capability = AccessCapability.APPROVE_SHIFT_CLOSE_EXCEPTION,
            operatorOverride = approverOverride
        ).getOrNull() ?: return false
        val request = kernelRepository.getApprovalRequestById(requestId) ?: return false
        if (request.status != ApprovalStatus.REQUESTED) return false
        kernelRepository.resolveApprovalRequest(
            id = request.id,
            status = ApprovalStatus.DENIED,
            approvedBy = approver.id,
            decisionNote = decisionNote.ifBlank { "Ditolak ${approver.displayName}" }
        )
        kernelRepository.insertAudit(
            id = IdGenerator.nextId("audit"),
            message = "Approval ${request.id} ditolak untuk tutup shift ${request.shiftId}.",
            level = "WARN"
        )
        return true
    }

    private suspend fun createReport(
        shiftId: String,
        businessDayId: String,
        terminalId: String,
        openingCash: Double,
        cashSalesTotal: Double,
        totals: id.azureenterprise.cassy.kernel.domain.CashMovementTotals,
        expectedCash: Double,
        actualCash: Double,
        variance: Double,
        pendingTransactionCount: Int,
        approvalRequestId: String?,
        generatedBy: String
    ): ShiftCloseReport {
        return kernelRepository.insertShiftCloseReport(
            id = IdGenerator.nextId("shiftclose"),
            shiftId = shiftId,
            businessDayId = businessDayId,
            terminalId = terminalId,
            openingCash = openingCash,
            cashSalesTotal = cashSalesTotal,
            cashInTotal = totals.cashInTotal,
            cashOutTotal = totals.cashOutTotal,
            safeDropTotal = totals.safeDropTotal,
            expectedCash = expectedCash,
            actualCash = actualCash,
            variance = variance,
            pendingTransactionCount = pendingTransactionCount,
            approvalRequestId = approvalRequestId,
            generatedBy = generatedBy
        )
    }

    private suspend fun resolveApprover(
        capability: AccessCapability,
        operatorOverride: OperatorAccount?
    ): Result<OperatorAccount> {
        val approvedOverride = operatorOverride?.takeIf { it.role.supports(capability) }
        return if (approvedOverride != null) {
            Result.success(approvedOverride)
        } else {
            accessService.requireCapability(capability)
        }
    }

    private fun evaluateReviewDecision(
        actualCash: Double?,
        variance: Double?,
        pendingCount: Int
    ): OperationDecision {
        if (actualCash == null) {
            return OperationDecision(
                type = OperationType.CLOSE_SHIFT,
                status = OperationStatus.BLOCKED,
                title = "Tutup Shift",
                message = "Closing cash harus berupa angka.",
                blockerCode = OperationBlockerCode.INVALID_CLOSING_CASH
            )
        }
        if (actualCash < 0) {
            return OperationDecision(
                type = OperationType.CLOSE_SHIFT,
                status = OperationStatus.BLOCKED,
                title = "Tutup Shift",
                message = "Closing cash tidak boleh negatif.",
                blockerCode = OperationBlockerCode.INVALID_CLOSING_CASH
            )
        }
        if (pendingCount > 0) {
            return OperationDecision(
                type = OperationType.CLOSE_SHIFT,
                status = OperationStatus.BLOCKED,
                title = "Tutup Shift",
                message = "Masih ada transaksi pending. Selesaikan atau batalkan dulu sebelum tutup shift.",
                blockerCode = OperationBlockerCode.PENDING_TRANSACTION_PRESENT,
                actionLabel = "Review Pending"
            )
        }
        val absoluteVariance = abs(variance ?: 0.0)
        if (absoluteVariance > policy.hardLimitVariance) {
            return OperationDecision(
                type = OperationType.CLOSE_SHIFT,
                status = OperationStatus.BLOCKED,
                title = "Tutup Shift",
                message = "Selisih kas melewati batas keras and harus diselidiki dulu.",
                blockerCode = OperationBlockerCode.SHIFT_VARIANCE_OUT_OF_TOLERANCE,
                actionLabel = "Investigasi Selisih"
            )
        }
        if (absoluteVariance > policy.approvalThresholdVariance) {
            return OperationDecision(
                type = OperationType.CLOSE_SHIFT,
                status = OperationStatus.REQUIRES_APPROVAL,
                title = "Tutup Shift",
                message = "Selisih kas melewati batas approval supervisor/owner.",
                blockerCode = OperationBlockerCode.SHIFT_VARIANCE_OUT_OF_TOLERANCE,
                actionLabel = "Minta Approval"
            )
        }
        return OperationDecision(
            type = OperationType.CLOSE_SHIFT,
            status = OperationStatus.READY,
            title = "Tutup Shift",
            message = if (absoluteVariance > policy.varianceTolerance) {
                "Shift boleh ditutup, tetapi selisih kas wajib dicatat di laporan penutupan."
            } else {
                "Shift siap ditutup."
            },
            actionLabel = "Tutup Shift"
        )
    }
}
