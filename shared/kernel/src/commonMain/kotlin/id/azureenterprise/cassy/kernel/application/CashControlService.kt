package id.azureenterprise.cassy.kernel.application

import id.azureenterprise.cassy.kernel.data.KernelRepository
import id.azureenterprise.cassy.kernel.domain.AccessCapability
import id.azureenterprise.cassy.kernel.domain.ApprovalRequest
import id.azureenterprise.cassy.kernel.domain.ApprovalStatus
import id.azureenterprise.cassy.kernel.domain.CashMovementExecutionResult
import id.azureenterprise.cassy.kernel.domain.CashMovementPolicy
import id.azureenterprise.cassy.kernel.domain.CashMovementType
import id.azureenterprise.cassy.kernel.domain.IdGenerator
import id.azureenterprise.cassy.kernel.domain.OperationBlockerCode
import id.azureenterprise.cassy.kernel.domain.OperationDecision
import id.azureenterprise.cassy.kernel.domain.OperationStatus
import id.azureenterprise.cassy.kernel.domain.OperationType
import id.azureenterprise.cassy.kernel.domain.PendingApprovalSummary
import id.azureenterprise.cassy.kernel.domain.ReasonCategory
import id.azureenterprise.cassy.kernel.domain.supports

class CashControlService(
    private val kernelRepository: KernelRepository,
    private val accessService: AccessService,
    private val policy: CashMovementPolicy = CashMovementPolicy()
) {
    suspend fun listReasonCodes(category: ReasonCategory) = buildReasonCodes(category)

    suspend fun listPendingApprovals(): List<PendingApprovalSummary> {
        return kernelRepository.listPendingApprovalRequests()
            .filter { it.operationType in cashControlApprovalTypes }
            .map { request ->
                PendingApprovalSummary(
                    id = request.id,
                    operationType = request.operationType,
                    title = request.operationType.toTitle(),
                    detail = buildString {
                        append(request.reasonCode)
                        request.reasonDetail?.takeIf { it.isNotBlank() }?.let {
                            append(" | ")
                            append(it)
                        }
                    },
                    amount = request.amount
                )
            }
    }

    suspend fun evaluateMovement(
        type: CashMovementType,
        amount: Double?,
        reasonCode: String
    ): OperationDecision {
        val context = accessService.restoreContext()
        if (context.terminalBinding == null) {
            return blockedDecision(type, "Terminal belum terikat ke store.", OperationBlockerCode.TERMINAL_NOT_BOUND)
        }
        if (context.activeSession == null) {
            return blockedDecision(type, "Login operator diperlukan sebelum kontrol kas.", OperationBlockerCode.ACCESS_NOT_ACTIVE)
        }
        val operator = accessService.requireCapability(AccessCapability.RECORD_CASH_MOVEMENT).getOrNull()
            ?: return blockedDecision(type, "Operator aktif tidak diizinkan mengatur kontrol kas.", OperationBlockerCode.CAPABILITY_DENIED)
        val binding = kernelRepository.getTerminalBinding()
            ?: return blockedDecision(type, "Terminal belum terikat ke store.", OperationBlockerCode.TERMINAL_NOT_BOUND)
        val businessDay = kernelRepository.getActiveBusinessDay()
            ?: return blockedDecision(type, "Business day harus aktif sebelum kontrol kas dipakai.", OperationBlockerCode.BUSINESS_DAY_REQUIRED)
        val shift = kernelRepository.getActiveShift(binding.terminalId)
            ?: return blockedDecision(type, "Shift aktif diperlukan sebelum kontrol kas dipakai.", OperationBlockerCode.SHIFT_REQUIRED)
        if (amount == null) {
            return OperationDecision(
                type = type.toOperationType(),
                status = OperationStatus.BLOCKED,
                title = type.toTitle(),
                message = "Nominal harus berupa angka.",
                blockerCode = OperationBlockerCode.INVALID_CASH_MOVEMENT_AMOUNT
            )
        }
        if (amount <= 0) {
            return blockedDecision(type, "Nominal harus lebih besar dari nol.", OperationBlockerCode.INVALID_CASH_MOVEMENT_AMOUNT)
        }
        if (amount > policy.hardLimitAmount) {
            return blockedDecision(
                type,
                "Nominal melewati batas keras Rp ${policy.hardLimitAmount.toInt()}.",
                OperationBlockerCode.INVALID_CASH_MOVEMENT_AMOUNT
            )
        }
        val reason = findReasonCode(type.toReasonCategory(), reasonCode)
            ?: return blockedDecision(type, "Reason code wajib valid untuk kontrol kas.", OperationBlockerCode.REASON_CODE_REQUIRED)

        val threshold = thresholdFor(type)
        val needsApproval = amount > threshold || reason.requiresApproval
        val canApprove = operator.role.supports(AccessCapability.APPROVE_CASH_MOVEMENT_EXCEPTION)

        return when {
            needsApproval && !canApprove -> OperationDecision(
                type = type.toOperationType(),
                status = OperationStatus.REQUIRES_APPROVAL,
                title = type.toTitle(),
                message = "${type.toTitle()} di atas kebijakan. Login supervisor/owner untuk approve atau tolak.",
                blockerCode = OperationBlockerCode.APPROVAL_PENDING,
                actionLabel = "Review Approval"
            )
            else -> OperationDecision(
                type = type.toOperationType(),
                status = OperationStatus.READY,
                title = type.toTitle(),
                message = if (needsApproval) {
                    "${type.toTitle()} siap dicatat dengan approval supervisor/owner."
                } else {
                    "${type.toTitle()} siap dicatat untuk shift ${shift.id}."
                },
                actionLabel = type.toActionLabel()
            )
        }
    }

    suspend fun submitMovement(
        type: CashMovementType,
        amount: Double,
        reasonCode: String,
        reasonDetail: String = ""
    ): CashMovementExecutionResult {
        val decision = evaluateMovement(type, amount, reasonCode)
        if (decision.status == OperationStatus.BLOCKED || decision.status == OperationStatus.UNAVAILABLE) {
            return CashMovementExecutionResult.Blocked(decision)
        }

        val binding = kernelRepository.getTerminalBinding()
            ?: return CashMovementExecutionResult.Blocked(blockedDecision(type, "Terminal belum terikat ke store.", OperationBlockerCode.TERMINAL_NOT_BOUND))
        val businessDay = kernelRepository.getActiveBusinessDay()
            ?: return CashMovementExecutionResult.Blocked(blockedDecision(type, "Business day tidak aktif.", OperationBlockerCode.BUSINESS_DAY_NOT_ACTIVE))
        val shift = kernelRepository.getActiveShift(binding.terminalId)
            ?: return CashMovementExecutionResult.Blocked(blockedDecision(type, "Shift aktif tidak ditemukan.", OperationBlockerCode.SHIFT_REQUIRED))
        val operator = accessService.requireCapability(AccessCapability.RECORD_CASH_MOVEMENT).getOrElse {
            return CashMovementExecutionResult.Blocked(blockedDecision(type, it.message ?: "Operator tidak valid.", OperationBlockerCode.CAPABILITY_DENIED))
        }

        if (decision.status == OperationStatus.REQUIRES_APPROVAL) {
            val approval = kernelRepository.insertApprovalRequest(
                id = IdGenerator.nextId("approval"),
                operationType = type.toOperationType(),
                entityId = IdGenerator.nextId("cashmv"),
                businessDayId = businessDay.id,
                shiftId = shift.id,
                terminalId = binding.terminalId,
                amount = amount,
                reasonCode = reasonCode,
                reasonDetail = reasonDetail.takeIf { it.isNotBlank() },
                requestedBy = operator.id,
                approvedBy = null,
                status = ApprovalStatus.REQUESTED
            )
            kernelRepository.insertAudit(
                id = IdGenerator.nextId("audit"),
                message = "Approval diminta untuk ${type.toTitle().lowercase()} ${approval.amount} pada shift ${shift.id}.",
                level = "WARN"
            )
            kernelRepository.insertEvent(
                id = IdGenerator.nextId("event"),
                type = "APPROVAL_REQUESTED",
                payload = """{"approvalRequestId":"${approval.id}","operationType":"${approval.operationType.name}","shiftId":"${shift.id}"}"""
            )
            return CashMovementExecutionResult.ApprovalRequired(decision, approval.id)
        }

        val movement = kernelRepository.insertCashMovement(
            id = IdGenerator.nextId("cashmv"),
            businessDayId = businessDay.id,
            shiftId = shift.id,
            terminalId = binding.terminalId,
            type = type,
            amount = amount,
            reasonCode = reasonCode,
            reasonDetail = reasonDetail.takeIf { it.isNotBlank() },
            approvalRequestId = null,
            performedBy = operator.id
        )
        kernelRepository.insertAudit(
            id = IdGenerator.nextId("audit"),
            message = "${type.toTitle()} ${movement.amount} dicatat di shift ${shift.id}.",
            level = "INFO"
        )
        kernelRepository.insertEvent(
            id = IdGenerator.nextId("event"),
            type = "CASH_MOVEMENT_RECORDED",
            payload = """{"cashMovementId":"${movement.id}","type":"${movement.type.name}","shiftId":"${shift.id}","amount":${movement.amount}}"""
        )
        return CashMovementExecutionResult.Recorded(movement, approvalApplied = false)
    }

    suspend fun approveCashMovement(requestId: String): CashMovementExecutionResult {
        val request = kernelRepository.getApprovalRequestById(requestId)
            ?: return CashMovementExecutionResult.Blocked(blockedDecision(CashMovementType.CASH_OUT, "Approval request tidak ditemukan.", OperationBlockerCode.APPROVAL_PENDING))
        if (request.status != ApprovalStatus.REQUESTED) {
            return CashMovementExecutionResult.Blocked(
                OperationDecision(
                    type = request.operationType,
                    status = OperationStatus.COMPLETED,
                    title = request.operationType.toTitle(),
                    message = "Approval request ${request.id} sudah diproses."
                )
            )
        }
        val operator = accessService.requireCapability(AccessCapability.APPROVE_CASH_MOVEMENT_EXCEPTION).getOrElse {
            return CashMovementExecutionResult.Blocked(
                OperationDecision(
                    type = request.operationType,
                    status = OperationStatus.BLOCKED,
                    title = request.operationType.toTitle(),
                    message = it.message ?: "Supervisor/owner diperlukan untuk approve.",
                    blockerCode = OperationBlockerCode.CAPABILITY_DENIED
                )
            )
        }
        val resolved = kernelRepository.resolveApprovalRequest(
            id = request.id,
            status = ApprovalStatus.APPROVED,
            approvedBy = operator.id,
            decisionNote = "Approved by ${operator.displayName}"
        )
        val movement = kernelRepository.insertCashMovement(
            id = request.entityId,
            businessDayId = request.businessDayId,
            shiftId = requireNotNull(request.shiftId),
            terminalId = request.terminalId,
            type = request.operationType.toCashMovementType(),
            amount = requireNotNull(request.amount),
            reasonCode = request.reasonCode,
            reasonDetail = request.reasonDetail,
            approvalRequestId = resolved.id,
            performedBy = request.requestedBy
        )
        kernelRepository.insertAudit(
            id = IdGenerator.nextId("audit"),
            message = "Approval ${resolved.id} disetujui untuk ${movement.type.name} ${movement.amount}.",
            level = "INFO"
        )
        return CashMovementExecutionResult.Recorded(movement, approvalApplied = true)
    }

    suspend fun denyCashMovement(requestId: String, decisionNote: String): ApprovalRequest? {
        val operator = accessService.requireCapability(AccessCapability.APPROVE_CASH_MOVEMENT_EXCEPTION).getOrNull() ?: return null
        val request = kernelRepository.getApprovalRequestById(requestId) ?: return null
        if (request.status != ApprovalStatus.REQUESTED) return request
        val denied = kernelRepository.resolveApprovalRequest(
            id = request.id,
            status = ApprovalStatus.DENIED,
            approvedBy = operator.id,
            decisionNote = decisionNote.ifBlank { "Ditolak ${operator.displayName}" }
        )
        kernelRepository.insertAudit(
            id = IdGenerator.nextId("audit"),
            message = "Approval ${denied.id} ditolak untuk ${request.operationType.name}.",
            level = "WARN"
        )
        return denied
    }

    private suspend fun buildReasonCodes(category: ReasonCategory): List<id.azureenterprise.cassy.kernel.domain.ReasonCode> {
        kernelRepository.ensureDefaultReasonCodes()
        return kernelRepository.listActiveReasonCodes(category)
    }

    private suspend fun findReasonCode(category: ReasonCategory, code: String) =
        buildReasonCodes(category).firstOrNull { it.code == code }

    private fun thresholdFor(type: CashMovementType): Double = when (type) {
        CashMovementType.CASH_IN -> policy.maxCashierCashIn
        CashMovementType.CASH_OUT -> policy.maxCashierCashOut
        CashMovementType.SAFE_DROP -> policy.maxCashierSafeDrop
    }

    private fun blockedDecision(
        type: CashMovementType,
        message: String,
        blockerCode: OperationBlockerCode
    ) = OperationDecision(
        type = type.toOperationType(),
        status = OperationStatus.BLOCKED,
        title = type.toTitle(),
        message = message,
        blockerCode = blockerCode
    )

    private companion object {
        val cashControlApprovalTypes = setOf(
            OperationType.CASH_IN,
            OperationType.CASH_OUT,
            OperationType.SAFE_DROP
        )
    }
}

private fun CashMovementType.toOperationType(): OperationType = when (this) {
    CashMovementType.CASH_IN -> OperationType.CASH_IN
    CashMovementType.CASH_OUT -> OperationType.CASH_OUT
    CashMovementType.SAFE_DROP -> OperationType.SAFE_DROP
}

private fun OperationType.toCashMovementType(): CashMovementType = when (this) {
    OperationType.CASH_IN -> CashMovementType.CASH_IN
    OperationType.CASH_OUT -> CashMovementType.CASH_OUT
    OperationType.SAFE_DROP -> CashMovementType.SAFE_DROP
    else -> error("Operation $this bukan cash movement")
}

private fun CashMovementType.toReasonCategory(): ReasonCategory = when (this) {
    CashMovementType.CASH_IN -> ReasonCategory.CASH_IN
    CashMovementType.CASH_OUT -> ReasonCategory.CASH_OUT
    CashMovementType.SAFE_DROP -> ReasonCategory.SAFE_DROP
}

private fun CashMovementType.toTitle(): String = when (this) {
    CashMovementType.CASH_IN -> "Cash In"
    CashMovementType.CASH_OUT -> "Cash Out"
    CashMovementType.SAFE_DROP -> "Safe Drop"
}

private fun CashMovementType.toActionLabel(): String = when (this) {
    CashMovementType.CASH_IN -> "Catat Cash In"
    CashMovementType.CASH_OUT -> "Catat Cash Out"
    CashMovementType.SAFE_DROP -> "Catat Safe Drop"
}

private fun OperationType.toTitle(): String = when (this) {
    OperationType.CASH_IN -> "Cash In"
    OperationType.CASH_OUT -> "Cash Out"
    OperationType.SAFE_DROP -> "Safe Drop"
    OperationType.CLOSE_SHIFT -> "Tutup Shift"
    OperationType.CLOSE_BUSINESS_DAY -> "Tutup Hari"
    OperationType.OPEN_BUSINESS_DAY -> "Buka Business Day"
    OperationType.START_SHIFT -> "Buka Shift"
    OperationType.VOID_SALE -> "Void Penjualan"
    OperationType.STOCK_ADJUSTMENT -> "Adjustment Stok"
    OperationType.RESOLVE_STOCK_DISCREPANCY -> "Resolusi Discrepancy Stok"
}
