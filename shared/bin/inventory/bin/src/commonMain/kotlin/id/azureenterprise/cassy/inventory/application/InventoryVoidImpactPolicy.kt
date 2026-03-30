package id.azureenterprise.cassy.inventory.application

import id.azureenterprise.cassy.inventory.domain.InventoryVoidImpactClassification
import id.azureenterprise.cassy.inventory.domain.VoidImpactAssessment

class InventoryVoidImpactPolicy {

    fun classify(
        paymentSettled: Boolean,
        physicalReturnConfirmed: Boolean,
        explicitInventoryReasonProvided: Boolean
    ): VoidImpactAssessment {
        val classification = when {
            !paymentSettled -> InventoryVoidImpactClassification.PRE_SETTLEMENT_VOID_NO_STOCK_EFFECT
            physicalReturnConfirmed -> InventoryVoidImpactClassification.RETURN_REQUIRED
            explicitInventoryReasonProvided -> InventoryVoidImpactClassification.POST_SETTLEMENT_REVERSAL_CANDIDATE
            else -> InventoryVoidImpactClassification.MANUAL_INVESTIGATION_REQUIRED
        }
        return when (classification) {
            InventoryVoidImpactClassification.PRE_SETTLEMENT_VOID_NO_STOCK_EFFECT -> VoidImpactAssessment(
                classification = classification,
                message = "Void sebelum settlement tidak boleh memutasi stok final.",
                blocksInventoryMutation = true
            )

            InventoryVoidImpactClassification.POST_SETTLEMENT_REVERSAL_CANDIDATE -> VoidImpactAssessment(
                classification = classification,
                message = "Reversal pasca-settlement perlu contract inventory effect terpisah dan tidak boleh dieksekusi otomatis di Block 1.",
                blocksInventoryMutation = true
            )

            InventoryVoidImpactClassification.RETURN_REQUIRED -> VoidImpactAssessment(
                classification = classification,
                message = "Barang sudah keluar. Kembalinya stok harus lewat flow return terpisah, bukan void samar.",
                blocksInventoryMutation = true
            )

            InventoryVoidImpactClassification.MANUAL_INVESTIGATION_REQUIRED -> VoidImpactAssessment(
                classification = classification,
                message = "Dampak inventory tidak dapat dibuktikan dengan aman. Wajib masuk investigasi manual.",
                blocksInventoryMutation = true
            )
        }
    }
}
