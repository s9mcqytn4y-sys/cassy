package id.azureenterprise.cassy.kernel.application

import id.azureenterprise.cassy.kernel.domain.OperationDecision
import id.azureenterprise.cassy.kernel.domain.OperationStatus
import id.azureenterprise.cassy.kernel.domain.OperationType
import id.azureenterprise.cassy.kernel.domain.OperationalControlSnapshot
import id.azureenterprise.cassy.kernel.domain.CashMovementType

class OperationalControlService(
    private val accessService: AccessService,
    private val businessDayService: BusinessDayService,
    private val shiftService: ShiftService,
    private val cashControlService: CashControlService,
    private val shiftClosingService: ShiftClosingService
) {
    suspend fun buildSnapshot(
        openingCashInput: String,
        openingCashReason: String
    ): OperationalControlSnapshot {
        val context = accessService.restoreContext()
        val businessDay = businessDayService.getActiveBusinessDay()
        val activeShift = shiftService.getActiveShift()
        val parsedOpeningCash = openingCashInput.toDoubleOrNull()

        val openDayDecision = businessDayService.evaluateOpenDay()
        val startShiftDecision = shiftService.evaluateStartShift(
            openingCash = parsedOpeningCash,
            approvalReason = openingCashReason
        ).decision
        val cashInDecision = cashControlService.evaluateMovement(CashMovementType.CASH_IN, 1.0, "FLOAT_TOP_UP")
        val cashOutDecision = cashControlService.evaluateMovement(CashMovementType.CASH_OUT, 1.0, "PETTY_CASH")
        val safeDropDecision = cashControlService.evaluateMovement(CashMovementType.SAFE_DROP, 1.0, "SAFE_DROP_ROUTINE")
        val closeShiftDecision = shiftClosingService.evaluateCloseShiftReadiness()
        val closeDayDecision = businessDayService.evaluateCloseDay()
        val voidDecision = evaluateVoidDecision(
            businessDayActive = businessDay != null,
            shiftActive = activeShift != null
        )

        val canAccessSalesHome = businessDay != null && activeShift != null
        val salesHomeBlocker = when {
            context.terminalBinding == null -> "Terminal belum terikat ke store."
            context.activeSession == null -> "Login operator diperlukan."
            businessDay == null -> "Business day harus aktif sebelum kasir dibuka."
            activeShift == null -> "Shift aktif dan opening cash valid diperlukan sebelum kasir bisa dipakai."
            else -> null
        }

        val decisions = listOf(
            openDayDecision,
            startShiftDecision,
            cashInDecision,
            cashOutDecision,
            safeDropDecision,
            closeShiftDecision,
            closeDayDecision,
            voidDecision
        )
        val primaryAction = when {
            businessDay == null -> OperationType.OPEN_BUSINESS_DAY
            activeShift == null -> OperationType.START_SHIFT
            closeShiftDecision.status == OperationStatus.BLOCKED &&
                closeShiftDecision.blockerCode == id.azureenterprise.cassy.kernel.domain.OperationBlockerCode.PENDING_TRANSACTION_PRESENT ->
                OperationType.CLOSE_SHIFT
            else -> decisions.firstOrNull { it.status in actionableStatuses }?.type
        }
        val headline = when {
            canAccessSalesHome -> "Kasir siap dipakai di terminal ini."
            primaryAction == OperationType.OPEN_BUSINESS_DAY -> "Buka business day untuk memulai operasional."
            primaryAction == OperationType.START_SHIFT -> "Buka shift agar kasir bisa dipakai."
            primaryAction == OperationType.CLOSE_SHIFT -> "Tindak lanjuti blocker shift sebelum operasional ditutup."
            else -> salesHomeBlocker ?: "Operasional belum siap."
        }
        val pendingApprovalCount = cashControlService.listPendingApprovals().size +
            shiftClosingService.listPendingApprovals().size

        return OperationalControlSnapshot(
            headline = headline,
            primaryAction = primaryAction,
            canAccessSalesHome = canAccessSalesHome,
            salesHomeBlocker = salesHomeBlocker,
            businessDayId = businessDay?.id,
            shiftId = activeShift?.id,
            pendingApprovalCount = pendingApprovalCount,
            decisions = decisions
        )
    }

    private fun evaluateVoidDecision(
        businessDayActive: Boolean,
        shiftActive: Boolean
    ): OperationDecision {
        val message = when {
            !businessDayActive -> "Void diblokir: business day belum aktif."
            !shiftActive -> "Void diblokir: shift aktif belum ada."
            else -> "Void belum dibuka di Block 1. Resolver sales, cashflow, accounting report, dan inventory reversal masih harus di-hardening di blok berikutnya."
        }
        return OperationDecision(
            type = OperationType.VOID_SALE,
            status = OperationStatus.UNAVAILABLE,
            title = "Void Penjualan",
            message = message
        )
    }

    private companion object {
        val actionableStatuses = setOf(OperationStatus.READY, OperationStatus.REQUIRES_APPROVAL)
    }
}
