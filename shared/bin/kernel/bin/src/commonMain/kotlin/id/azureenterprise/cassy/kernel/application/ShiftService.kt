package id.azureenterprise.cassy.kernel.application

import id.azureenterprise.cassy.kernel.data.KernelRepository
import id.azureenterprise.cassy.kernel.domain.AccessCapability
import id.azureenterprise.cassy.kernel.domain.IdGenerator
import id.azureenterprise.cassy.kernel.domain.OpeningCashPolicy
import id.azureenterprise.cassy.kernel.domain.OperationBlockerCode
import id.azureenterprise.cassy.kernel.domain.OperationDecision
import id.azureenterprise.cassy.kernel.domain.OperationStatus
import id.azureenterprise.cassy.kernel.domain.OperationType
import id.azureenterprise.cassy.kernel.domain.ShiftStartPolicyAssessment
import id.azureenterprise.cassy.kernel.domain.StartShiftExecutionResult
import id.azureenterprise.cassy.kernel.domain.Shift
import id.azureenterprise.cassy.kernel.domain.supports

class ShiftService(
    private val kernelRepository: KernelRepository,
    private val accessService: AccessService,
    private val openingCashPolicy: OpeningCashPolicy = OpeningCashPolicy()
) {
    suspend fun getActiveShift(): Shift? {
        val binding = kernelRepository.getTerminalBinding() ?: return null
        return kernelRepository.getActiveShift(binding.terminalId)
    }

    suspend fun startShift(openingCash: Double): Result<Shift> {
        return when (val result = submitStartShift(openingCash)) {
            is StartShiftExecutionResult.Started -> Result.success(result.shift)
            is StartShiftExecutionResult.ApprovalRequired -> Result.failure(IllegalStateException(result.decision.message))
            is StartShiftExecutionResult.Blocked -> Result.failure(IllegalStateException(result.decision.message))
        }
    }

    suspend fun evaluateStartShift(
        openingCash: Double?,
        approvalReason: String
    ): ShiftStartPolicyAssessment {
        val context = accessService.restoreContext()
        if (context.terminalBinding == null) {
            return blockedAssessment(
                message = "Terminal belum terikat ke store.",
                blockerCode = OperationBlockerCode.TERMINAL_NOT_BOUND
            )
        }
        if (context.activeSession == null) {
            return blockedAssessment(
                message = "Login operator diperlukan sebelum buka shift.",
                blockerCode = OperationBlockerCode.ACCESS_NOT_ACTIVE
            )
        }
        val operator = accessService.requireCapability(AccessCapability.START_SHIFT).getOrNull()
            ?: return blockedAssessment(
                message = "Operator aktif tidak diizinkan membuka shift.",
                blockerCode = OperationBlockerCode.CAPABILITY_DENIED
            )
        val binding = kernelRepository.getTerminalBinding()
            ?: return blockedAssessment(
                message = "Terminal belum terikat ke store.",
                blockerCode = OperationBlockerCode.TERMINAL_NOT_BOUND
            )
        val businessDay = kernelRepository.getActiveBusinessDay()
            ?: return blockedAssessment(
                message = "Business day harus aktif sebelum buka shift.",
                blockerCode = OperationBlockerCode.BUSINESS_DAY_REQUIRED
            )
        if (kernelRepository.getActiveShift(binding.terminalId) != null) {
            return ShiftStartPolicyAssessment(
                decision = OperationDecision(
                    type = OperationType.START_SHIFT,
                    status = OperationStatus.COMPLETED,
                    title = "Buka Shift",
                    message = "Shift aktif sudah ada di terminal ini."
                ),
                requiresApproval = false,
                approvalWillBeApplied = false
            )
        }
        if (openingCash == null) {
            return blockedAssessment(
                message = "Opening cash harus berupa angka.",
                blockerCode = OperationBlockerCode.INVALID_OPENING_CASH
            )
        }
        if (openingCash < 0) {
            return blockedAssessment(
                message = "Opening cash tidak boleh negatif.",
                blockerCode = OperationBlockerCode.INVALID_OPENING_CASH
            )
        }
        if (openingCash > openingCashPolicy.hardLimitOpeningCash) {
            return blockedAssessment(
                message = "Opening cash melewati batas keras Rp ${openingCashPolicy.hardLimitOpeningCash.toInt()}.",
                blockerCode = OperationBlockerCode.OPENING_CASH_OUT_OF_POLICY,
                requiresReason = true
            )
        }

        val trimmedReason = approvalReason.trim()
        val requiresApproval = openingCash > openingCashPolicy.maxCashierOpeningCash
        val operatorCanApprove = operator.role.supports(AccessCapability.APPROVE_OPENING_CASH_EXCEPTION)

        if (requiresApproval && trimmedReason.isBlank()) {
            return blockedAssessment(
                message = "Alasan wajib diisi untuk opening cash di luar kebijakan.",
                blockerCode = OperationBlockerCode.REASON_REQUIRED,
                requiresReason = true
            )
        }
        if (requiresApproval && !operatorCanApprove) {
            return ShiftStartPolicyAssessment(
                decision = OperationDecision(
                    type = OperationType.START_SHIFT,
                    status = OperationStatus.REQUIRES_APPROVAL,
                    title = "Buka Shift",
                    message = "Opening cash di atas batas kasir. Login supervisor/owner untuk menyetujui.",
                    blockerCode = OperationBlockerCode.OPENING_CASH_OUT_OF_POLICY,
                    actionLabel = "Minta Approval Supervisor",
                    requiresReason = true
                ),
                requiresApproval = true,
                approvalWillBeApplied = false
            )
        }

        val readyMessage = if (requiresApproval) {
            "Supervisor/owner dapat membuka shift dengan approval tercatat."
        } else {
            "Shift siap dibuka di terminal ini."
        }
        return ShiftStartPolicyAssessment(
            decision = OperationDecision(
                type = OperationType.START_SHIFT,
                status = OperationStatus.READY,
                title = "Buka Shift",
                message = readyMessage,
                actionLabel = "Buka Shift",
                requiresReason = requiresApproval
            ),
            requiresApproval = requiresApproval,
            approvalWillBeApplied = requiresApproval
        )
    }

    suspend fun submitStartShift(
        openingCash: Double,
        approvalReason: String = ""
    ): StartShiftExecutionResult {
        val assessment = evaluateStartShift(openingCash, approvalReason)
        when (assessment.decision.status) {
            OperationStatus.REQUIRES_APPROVAL -> {
                kernelRepository.insertAudit(
                    id = IdGenerator.nextId("audit"),
                    message = "Start shift ditahan: ${assessment.decision.message}",
                    level = "WARN"
                )
                return StartShiftExecutionResult.ApprovalRequired(assessment.decision)
            }
            OperationStatus.BLOCKED,
            OperationStatus.UNAVAILABLE,
            OperationStatus.COMPLETED -> {
                return StartShiftExecutionResult.Blocked(assessment.decision)
            }
            OperationStatus.READY -> Unit
        }

        val operator = accessService.requireCapability(AccessCapability.START_SHIFT).getOrElse {
            return StartShiftExecutionResult.Blocked(
                blockedDecision(
                    message = it.message ?: "Operator aktif tidak diizinkan membuka shift.",
                    blockerCode = OperationBlockerCode.CAPABILITY_DENIED
                )
            )
        }
        val binding = kernelRepository.getTerminalBinding()
            ?: return StartShiftExecutionResult.Blocked(
                blockedDecision(
                    message = "Terminal belum terikat ke store.",
                    blockerCode = OperationBlockerCode.TERMINAL_NOT_BOUND
                )
            )
        val businessDay = kernelRepository.getActiveBusinessDay()
            ?: return StartShiftExecutionResult.Blocked(
                blockedDecision(
                    message = "Business day harus aktif sebelum buka shift.",
                    blockerCode = OperationBlockerCode.BUSINESS_DAY_REQUIRED
                )
            )

        val shift = kernelRepository.openShift(
            id = IdGenerator.nextId("shift"),
            businessDayId = businessDay.id,
            terminalId = binding.terminalId,
            openingCash = openingCash,
            openedBy = operator.id
        )
        val trimmedReason = approvalReason.trim()
        val approvalApplied = assessment.approvalWillBeApplied
        kernelRepository.insertAudit(
            id = IdGenerator.nextId("audit"),
            message = buildString {
                append("Shift ${shift.id} dimulai dengan opening cash $openingCash oleh ${operator.displayName}")
                if (approvalApplied) {
                    append(" dengan approval karena di luar kebijakan")
                }
                if (trimmedReason.isNotEmpty()) {
                    append(". Alasan: $trimmedReason")
                }
            },
            level = "INFO"
        )
        kernelRepository.insertEvent(
            id = IdGenerator.nextId("event"),
            type = "SHIFT_OPENED",
            payload = """
                {"shiftId":"${shift.id}","businessDayId":"${businessDay.id}","terminalId":"${binding.terminalId}","operatorId":"${operator.id}","openingCash":$openingCash,"approvalApplied":$approvalApplied,"reason":"${trimmedReason.asJsonValue()}"}
            """.trimIndent()
        )
        return StartShiftExecutionResult.Started(
            shift = shift,
            approvalApplied = approvalApplied
        )
    }

    suspend fun endShift(closingCash: Double): Result<Shift> {
        if (closingCash < 0) return Result.failure(IllegalArgumentException("Closing cash tidak boleh negatif"))
        val operator = accessService.requireCapability(AccessCapability.END_SHIFT).getOrElse { return Result.failure(it) }
        val binding = kernelRepository.getTerminalBinding()
            ?: return Result.failure(IllegalStateException("Terminal belum terikat"))
        val shift = kernelRepository.getActiveShift(binding.terminalId)
            ?: return Result.failure(IllegalStateException("Tidak ada shift aktif"))

        val closed = kernelRepository.closeShift(shift.id, closingCash, operator.id)
        kernelRepository.insertAudit(
            id = IdGenerator.nextId("audit"),
            message = "Shift ${closed.id} ditutup dengan closing cash $closingCash oleh ${operator.displayName}",
            level = "INFO"
        )
        kernelRepository.insertEvent(
            id = IdGenerator.nextId("event"),
            type = "SHIFT_CLOSED",
            payload = """
                {"shiftId":"${closed.id}","terminalId":"${binding.terminalId}","operatorId":"${operator.id}","closingCash":$closingCash}
            """.trimIndent()
        )
        return Result.success(closed)
    }

    private fun blockedAssessment(
        message: String,
        blockerCode: OperationBlockerCode,
        requiresReason: Boolean = false
    ): ShiftStartPolicyAssessment {
        return ShiftStartPolicyAssessment(
            decision = blockedDecision(message, blockerCode, requiresReason),
            requiresApproval = false,
            approvalWillBeApplied = false
        )
    }

    private fun blockedDecision(
        message: String,
        blockerCode: OperationBlockerCode,
        requiresReason: Boolean = false
    ): OperationDecision {
        return OperationDecision(
            type = OperationType.START_SHIFT,
            status = OperationStatus.BLOCKED,
            title = "Buka Shift",
            message = message,
            blockerCode = blockerCode,
            requiresReason = requiresReason
        )
    }
}

private fun String.asJsonValue(): String {
    return replace("\\", "\\\\").replace("\"", "\\\"")
}
