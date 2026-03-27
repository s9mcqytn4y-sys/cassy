package id.azureenterprise.cassy.sales.application

import id.azureenterprise.cassy.inventory.application.InventoryService
import id.azureenterprise.cassy.kernel.application.AccessService
import id.azureenterprise.cassy.kernel.application.CashControlService
import id.azureenterprise.cassy.kernel.data.KernelRepository
import id.azureenterprise.cassy.kernel.domain.AccessCapability
import id.azureenterprise.cassy.kernel.domain.CashMovementExecutionResult
import id.azureenterprise.cassy.kernel.domain.CashMovementType
import id.azureenterprise.cassy.kernel.domain.IdGenerator
import id.azureenterprise.cassy.kernel.domain.ReasonCategory
import id.azureenterprise.cassy.sales.data.SalesRepository
import id.azureenterprise.cassy.sales.domain.SaleStatus
import id.azureenterprise.cassy.sales.domain.VoidSaleAssessment
import id.azureenterprise.cassy.sales.domain.VoidSaleExecutionResult

class VoidSaleService(
    private val salesRepository: SalesRepository,
    private val kernelRepository: KernelRepository,
    private val accessService: AccessService,
    private val cashControlService: CashControlService,
    private val inventoryService: InventoryService
) {
    suspend fun assessVoid(
        saleId: String,
        inventoryFollowUpNote: String?
    ): Result<VoidSaleAssessment> = runCatching {
        val readback = salesRepository.getCompletedSaleReadback(saleId)
            ?: error("Sale final tidak ditemukan")
        val existingVoid = salesRepository.getSaleVoidBySaleId(saleId)
        val paymentMethod = readback.receiptSnapshot.payment.method
        val classification = inventoryService.classifyVoidImpact(
            paymentSettled = true,
            physicalReturnConfirmed = false,
            explicitInventoryReasonProvided = !inventoryFollowUpNote.isNullOrBlank()
        )
        val saleStatus = readback.sale.status
        when {
            existingVoid != null -> VoidSaleAssessment(
                saleId = saleId,
                localNumber = readback.receiptSnapshot.localNumber,
                paymentMethod = paymentMethod,
                originalAmount = readback.receiptSnapshot.totals.finalTotal,
                saleStatus = SaleStatus.VOIDED,
                isEligible = false,
                message = "Penjualan ini sudah pernah di-void pada terminal.",
                inventoryImpactClassification = existingVoid.inventoryImpactClassification,
                inventoryFollowUpNote = existingVoid.inventoryFollowUpNote,
                existingVoid = existingVoid
            )
            saleStatus != SaleStatus.COMPLETED -> VoidSaleAssessment(
                saleId = saleId,
                localNumber = readback.receiptSnapshot.localNumber,
                paymentMethod = paymentMethod,
                originalAmount = readback.receiptSnapshot.totals.finalTotal,
                saleStatus = saleStatus,
                isEligible = false,
                message = "Hanya penjualan final dengan status COMPLETED yang boleh masuk jalur void.",
                inventoryImpactClassification = classification.classification.name,
                inventoryFollowUpNote = inventoryFollowUpNote,
                existingVoid = null
            )
            paymentMethod != CASH_METHOD -> VoidSaleAssessment(
                saleId = saleId,
                localNumber = readback.receiptSnapshot.localNumber,
                paymentMethod = paymentMethod,
                originalAmount = readback.receiptSnapshot.totals.finalTotal,
                saleStatus = saleStatus,
                isEligible = false,
                message = "Void desktop V1 hanya mengeksekusi penjualan CASH. CARD/QRIS tetap perlu reversal/refund eksternal yang belum dibuka.",
                inventoryImpactClassification = classification.classification.name,
                inventoryFollowUpNote = inventoryFollowUpNote,
                existingVoid = null
            )
            else -> VoidSaleAssessment(
                saleId = saleId,
                localNumber = readback.receiptSnapshot.localNumber,
                paymentMethod = paymentMethod,
                originalAmount = readback.receiptSnapshot.totals.finalTotal,
                saleStatus = saleStatus,
                isEligible = true,
                message = buildString {
                    append("Void sale cash siap dijalankan. Refund kas akan dicatat sebagai CASH_OUT. ")
                    append("Dampak stok tetap tidak dibalik otomatis dan harus di-follow up manual sesuai contract inventory.")
                },
                inventoryImpactClassification = classification.classification.name,
                inventoryFollowUpNote = inventoryFollowUpNote,
                existingVoid = null
            )
        }
    }

    suspend fun executeVoid(
        saleId: String,
        reasonCode: String,
        reasonDetail: String,
        inventoryFollowUpNote: String
    ): Result<VoidSaleExecutionResult> = runCatching {
        val operator = accessService.requireCapability(AccessCapability.VOID_COMPLETED_SALE).getOrThrow()
        kernelRepository.ensureDefaultReasonCodes()
        val reason = kernelRepository.getReasonCode(reasonCode)
            ?: error("Reason code void tidak valid")
        check(reason.category == ReasonCategory.VOID_SALE) { "Reason code void tidak valid" }

        val assessment = assessVoid(saleId, inventoryFollowUpNote).getOrThrow()
        check(assessment.isEligible) { assessment.message }

        val binding = kernelRepository.getTerminalBinding()
            ?: error("Terminal belum terikat ke store")
        val activeShift = kernelRepository.getActiveShift(binding.terminalId)
            ?: error("Shift aktif wajib ada untuk mengeksekusi void")

        val readback = salesRepository.getCompletedSaleReadback(saleId)
            ?: error("Snapshot sale final tidak ditemukan")
        val cashRefund = cashControlService.submitMovement(
            type = CashMovementType.CASH_OUT,
            amount = readback.receiptSnapshot.totals.finalTotal,
            reasonCode = CASH_REFUND_REASON,
            reasonDetail = buildString {
                append("Void ")
                append(readback.receiptSnapshot.localNumber)
                if (reasonDetail.isNotBlank()) {
                    append(" | ")
                    append(reasonDetail.trim())
                }
            }
        )

        val refundMovementId = when (cashRefund) {
            is CashMovementExecutionResult.Recorded -> cashRefund.movement.id
            is CashMovementExecutionResult.ApprovalRequired -> error(cashRefund.decision.message)
            is CashMovementExecutionResult.Blocked -> error(cashRefund.decision.message)
        }

        try {
            val saleVoid = salesRepository.recordSaleVoid(
                id = IdGenerator.nextId("sale_void"),
                saleId = saleId,
                businessDayId = activeShift.businessDayId,
                shiftId = activeShift.id,
                terminalId = binding.terminalId,
                localNumber = readback.receiptSnapshot.localNumber,
                paymentMethod = readback.receiptSnapshot.payment.method,
                originalAmount = readback.receiptSnapshot.totals.finalTotal,
                cashRefundMovementId = refundMovementId,
                inventoryImpactClassification = assessment.inventoryImpactClassification,
                inventoryFollowUpNote = inventoryFollowUpNote.takeIf { it.isNotBlank() },
                reasonCode = reason.code,
                reasonDetail = reasonDetail.takeIf { it.isNotBlank() },
                voidedBy = operator.id
            )

            kernelRepository.insertAudit(
                id = IdGenerator.nextId("audit"),
                message = "Sale ${saleVoid.localNumber} di-void oleh ${operator.displayName}. Refund cash movement=${refundMovementId}. Inventory=${saleVoid.inventoryImpactClassification}.",
                level = "WARN"
            )
            kernelRepository.insertEvent(
                id = IdGenerator.nextId("event"),
                type = "SALE_VOIDED",
                payload = """{"saleId":"${saleVoid.saleId}","localNumber":"${saleVoid.localNumber}","cashRefundMovementId":"$refundMovementId","inventoryImpact":"${saleVoid.inventoryImpactClassification}"}"""
            )

            VoidSaleExecutionResult(
                assessment = assessment,
                saleVoid = saleVoid,
                cashRefundMovementId = refundMovementId
            )
        } catch (error: Throwable) {
            kernelRepository.insertAudit(
                id = IdGenerator.nextId("audit"),
                message = "Void sale ${readback.receiptSnapshot.localNumber} gagal dituntaskan setelah refund kas $refundMovementId. Investigasi manual wajib dilakukan.",
                level = "ERROR"
            )
            throw IllegalStateException(
                "Refund kas sudah tercatat tetapi sale belum ditandai void. Investigasi manual wajib dilakukan.",
                error
            )
        }
    }

    private companion object {
        const val CASH_METHOD = "CASH"
        const val CASH_REFUND_REASON = "VOID_CASH_REFUND"
    }
}
