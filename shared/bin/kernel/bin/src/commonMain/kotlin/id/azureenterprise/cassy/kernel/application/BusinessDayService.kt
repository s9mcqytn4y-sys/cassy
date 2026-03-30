package id.azureenterprise.cassy.kernel.application

import id.azureenterprise.cassy.kernel.data.KernelRepository
import id.azureenterprise.cassy.kernel.domain.AccessCapability
import id.azureenterprise.cassy.kernel.domain.BusinessDay
import id.azureenterprise.cassy.kernel.domain.IdGenerator
import id.azureenterprise.cassy.kernel.domain.OperationBlockerCode
import id.azureenterprise.cassy.kernel.domain.OperationDecision
import id.azureenterprise.cassy.kernel.domain.OperationStatus
import id.azureenterprise.cassy.kernel.domain.OperationType

class BusinessDayService(
    private val kernelRepository: KernelRepository,
    private val accessService: AccessService
) {
    suspend fun isOpen(): Boolean = kernelRepository.isBusinessDayOpen()

    suspend fun getActiveBusinessDay(): BusinessDay? = kernelRepository.getActiveBusinessDay()

    suspend fun evaluateOpenDay(): OperationDecision {
        val context = accessService.restoreContext()
        val activeDay = kernelRepository.getActiveBusinessDay()
        if (context.terminalBinding == null) {
            return OperationDecision(
                type = OperationType.OPEN_BUSINESS_DAY,
                status = OperationStatus.BLOCKED,
                title = "Buka Business Day",
                message = "Terminal belum terikat ke store.",
                blockerCode = OperationBlockerCode.TERMINAL_NOT_BOUND
            )
        }
        if (context.activeSession == null) {
            return OperationDecision(
                type = OperationType.OPEN_BUSINESS_DAY,
                status = OperationStatus.BLOCKED,
                title = "Buka Business Day",
                message = "Login supervisor atau owner diperlukan.",
                blockerCode = OperationBlockerCode.ACCESS_NOT_ACTIVE
            )
        }
        if (activeDay != null) {
            return OperationDecision(
                type = OperationType.OPEN_BUSINESS_DAY,
                status = OperationStatus.COMPLETED,
                title = "Buka Business Day",
                message = "Business day ${activeDay.id} sudah aktif."
            )
        }
        val capabilityResult = accessService.requireCapability(AccessCapability.OPEN_DAY)
        if (capabilityResult.isFailure) {
            return OperationDecision(
                type = OperationType.OPEN_BUSINESS_DAY,
                status = OperationStatus.BLOCKED,
                title = "Buka Business Day",
                message = "Supervisor atau owner diperlukan untuk membuka business day.",
                blockerCode = OperationBlockerCode.CAPABILITY_DENIED
            )
        }
        return OperationDecision(
            type = OperationType.OPEN_BUSINESS_DAY,
            status = OperationStatus.READY,
            title = "Buka Business Day",
            message = "Siap membuka business day hari ini.",
            actionLabel = "Buka Business Day"
        )
    }

    suspend fun openNewDay(reason: String = ""): Result<BusinessDay> {
        val operator = accessService.requireCapability(AccessCapability.OPEN_DAY).getOrElse { return Result.failure(it) }
        if (isOpen()) return Result.failure(IllegalStateException("Business day sudah terbuka"))
        val binding = kernelRepository.getTerminalBinding()
            ?: return Result.failure(IllegalStateException("Terminal belum terikat"))

        val opened = kernelRepository.openBusinessDay(IdGenerator.nextId("bd"))
        val trimmedReason = reason.trim()
        kernelRepository.insertAudit(
            id = IdGenerator.nextId("audit"),
            message = buildString {
                append("Business day ${opened.id} dibuka oleh ${operator.displayName}")
                if (trimmedReason.isNotEmpty()) {
                    append(" dengan alasan: $trimmedReason")
                }
            },
            level = "INFO"
        )
        kernelRepository.insertEvent(
            id = IdGenerator.nextId("event"),
            type = "BUSINESS_DAY_OPENED",
            payload = """
                {"businessDayId":"${opened.id}","terminalId":"${binding.terminalId}","operatorId":"${operator.id}","reason":"${trimmedReason.asJsonValue()}"}
            """.trimIndent()
        )
        return Result.success(opened)
    }

    suspend fun evaluateCloseDay(): OperationDecision {
        val context = accessService.restoreContext()
        val activeDay = kernelRepository.getActiveBusinessDay()
            ?: return OperationDecision(
                type = OperationType.CLOSE_BUSINESS_DAY,
                status = OperationStatus.BLOCKED,
                title = "Tutup Hari",
                message = "Business day aktif belum ada.",
                blockerCode = OperationBlockerCode.BUSINESS_DAY_NOT_ACTIVE
            )
        if (context.activeSession == null) {
            return OperationDecision(
                type = OperationType.CLOSE_BUSINESS_DAY,
                status = OperationStatus.BLOCKED,
                title = "Tutup Hari",
                message = "Login supervisor atau owner diperlukan.",
                blockerCode = OperationBlockerCode.ACCESS_NOT_ACTIVE
            )
        }
        val operator = accessService.requireCapability(AccessCapability.CLOSE_DAY).getOrNull()
            ?: return OperationDecision(
                type = OperationType.CLOSE_BUSINESS_DAY,
                status = OperationStatus.BLOCKED,
                title = "Tutup Hari",
                message = "Supervisor atau owner diperlukan untuk tutup hari.",
                blockerCode = OperationBlockerCode.CAPABILITY_DENIED
            )
        val openShiftCount = kernelRepository.countOpenShiftsByBusinessDay(activeDay.id)
        if (openShiftCount > 0) {
            return OperationDecision(
                type = OperationType.CLOSE_BUSINESS_DAY,
                status = OperationStatus.BLOCKED,
                title = "Tutup Hari",
                message = "Masih ada shift aktif. Tutup semua shift dulu sebelum close day.",
                blockerCode = OperationBlockerCode.OPEN_SHIFT_EXISTS,
                actionLabel = "Tutup Shift Aktif"
            )
        }
        val pendingApprovalCount = kernelRepository.countPendingApprovalRequestsByBusinessDay(activeDay.id)
        if (pendingApprovalCount > 0) {
            return OperationDecision(
                type = OperationType.CLOSE_BUSINESS_DAY,
                status = OperationStatus.BLOCKED,
                title = "Tutup Hari",
                message = "Masih ada approval operasional yang belum diputuskan.",
                blockerCode = OperationBlockerCode.PENDING_APPROVAL_EXISTS,
                actionLabel = "Review Approval"
            )
        }
        return OperationDecision(
            type = OperationType.CLOSE_BUSINESS_DAY,
            status = OperationStatus.READY,
            title = "Tutup Hari",
            message = "Hari siap ditutup oleh ${operator.displayName}.",
            actionLabel = "Review Close Day"
        )
    }

    suspend fun closeCurrentDay(): Result<BusinessDay> {
        val review = evaluateCloseDay()
        if (review.status != OperationStatus.READY) {
            return Result.failure(IllegalStateException(review.message))
        }
        val operator = accessService.requireCapability(AccessCapability.CLOSE_DAY).getOrElse { return Result.failure(it) }
        val activeDay = kernelRepository.getActiveBusinessDay()
            ?: return Result.failure(IllegalStateException("Tidak ada business day aktif"))
        val binding = kernelRepository.getTerminalBinding()
            ?: return Result.failure(IllegalStateException("Terminal belum terikat"))

        val closed = kernelRepository.closeBusinessDay(activeDay.id)
        kernelRepository.insertAudit(
            id = IdGenerator.nextId("audit"),
            message = "Business day ${closed.id} ditutup oleh ${operator.displayName}",
            level = "INFO"
        )
        kernelRepository.insertEvent(
            id = IdGenerator.nextId("event"),
            type = "BUSINESS_DAY_CLOSED",
            payload = """
                {"businessDayId":"${closed.id}","terminalId":"${binding.terminalId}","operatorId":"${operator.id}"}
            """.trimIndent()
        )
        return Result.success(closed)
    }
}

private fun String.asJsonValue(): String {
    return replace("\\", "\\\\").replace("\"", "\\\"")
}
