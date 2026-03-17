package id.azureenterprise.cassy.desktop

import id.azureenterprise.cassy.sales.domain.ReceiptPrintPayload

enum class HardwareDeviceStatus {
    READY,
    UNKNOWN,
    WARNING,
    UNAVAILABLE
}

data class HardwareDeviceState(
    val status: HardwareDeviceStatus,
    val label: String,
    val detailMessage: String? = null
)

data class CashierHardwareSnapshot(
    val printer: HardwareDeviceState = HardwareDeviceState(
        status = HardwareDeviceStatus.UNKNOWN,
        label = "Unknown",
        detailMessage = "Status printer terbatas"
    ),
    val scanner: HardwareDeviceState = HardwareDeviceState(
        status = HardwareDeviceStatus.UNKNOWN,
        label = "Unknown",
        detailMessage = "Status scanner terbatas"
    ),
    val cashDrawer: HardwareDeviceState = HardwareDeviceState(
        status = HardwareDeviceStatus.UNKNOWN,
        label = "Unknown",
        detailMessage = "Status cash drawer terbatas"
    )
)

data class HardwarePostFinalizationResult(
    val snapshot: CashierHardwareSnapshot,
    val warningMessage: String? = null
)

interface CashierHardwarePort {
    suspend fun getSnapshot(): CashierHardwareSnapshot

    suspend fun handlePostFinalization(
        paymentMethod: String,
        receiptPayload: ReceiptPrintPayload
    ): HardwarePostFinalizationResult
}

class DesktopNoopCashierHardwarePort : CashierHardwarePort {
    private val snapshot = CashierHardwareSnapshot()

    override suspend fun getSnapshot(): CashierHardwareSnapshot = snapshot

    override suspend fun handlePostFinalization(
        paymentMethod: String,
        receiptPayload: ReceiptPrintPayload
    ): HardwarePostFinalizationResult = HardwarePostFinalizationResult(snapshot = snapshot)
}
