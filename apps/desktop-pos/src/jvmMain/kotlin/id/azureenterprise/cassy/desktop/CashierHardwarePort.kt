package id.azureenterprise.cassy.desktop

import id.azureenterprise.cassy.sales.domain.ReceiptPrintPayload
import id.azureenterprise.cassy.sales.domain.ReceiptPrintState
import id.azureenterprise.cassy.sales.domain.ReceiptPrintStatus

enum class HardwareDeviceStatus {
    READY,
    DISCONNECTED,
    UNSTABLE,
    ABNORMAL,
    BLOCKED,
    FALLBACK
}

data class HardwareDeviceState(
    val status: HardwareDeviceStatus,
    val label: String,
    val detailMessage: String? = null
)

data class CashierHardwareSnapshot(
    val printer: HardwareDeviceState = HardwareDeviceState(
        status = HardwareDeviceStatus.DISCONNECTED,
        label = "Belum terhubung",
        detailMessage = "Struk tetap bisa dipreview dan dicetak ulang setelah printer siap."
    ),
    val scanner: HardwareDeviceState = HardwareDeviceState(
        status = HardwareDeviceStatus.FALLBACK,
        label = "Input manual aktif",
        detailMessage = "Scan belum tersedia. Cari barang lewat SKU atau nama produk."
    ),
    val cashDrawer: HardwareDeviceState = HardwareDeviceState(
        status = HardwareDeviceStatus.FALLBACK,
        label = "Mode cadangan",
        detailMessage = "Transaksi tunai tetap bisa lanjut dengan catatan audit manual bila perlu."
    )
)

data class HardwarePostFinalizationResult(
    val snapshot: CashierHardwareSnapshot,
    val warningMessage: String? = null
)

data class HardwarePrintExecutionResult(
    val snapshot: CashierHardwareSnapshot,
    val printState: ReceiptPrintState
)

interface CashierHardwarePort {
    suspend fun getSnapshot(): CashierHardwareSnapshot

    suspend fun handlePostFinalization(
        paymentMethod: String,
        receiptPayload: ReceiptPrintPayload
    ): HardwarePostFinalizationResult

    suspend fun printReceipt(
        receiptPayload: ReceiptPrintPayload
    ): HardwarePrintExecutionResult
}

class DesktopNoopCashierHardwarePort : CashierHardwarePort {
    private val snapshot = CashierHardwareSnapshot()

    override suspend fun getSnapshot(): CashierHardwareSnapshot = snapshot

    override suspend fun handlePostFinalization(
        paymentMethod: String,
        receiptPayload: ReceiptPrintPayload
    ): HardwarePostFinalizationResult = HardwarePostFinalizationResult(snapshot = snapshot)

    override suspend fun printReceipt(receiptPayload: ReceiptPrintPayload): HardwarePrintExecutionResult {
        return HardwarePrintExecutionResult(
            snapshot = snapshot,
            printState = ReceiptPrintState(
                status = ReceiptPrintStatus.FAILED,
                detailMessage = "Printer belum terhubung. Struk final aman dan bisa dicetak ulang nanti."
            )
        )
    }
}
