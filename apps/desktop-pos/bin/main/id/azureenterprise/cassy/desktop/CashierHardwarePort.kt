package id.azureenterprise.cassy.desktop

import id.azureenterprise.cassy.sales.domain.ReceiptPrintPayload
import id.azureenterprise.cassy.sales.domain.ReceiptPrintState
import id.azureenterprise.cassy.sales.domain.ReceiptPrintStatus

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
        label = "Belum diperiksa",
        detailMessage = "Struk tetap bisa dipreview dan dicetak ulang setelah printer siap."
    ),
    val scanner: HardwareDeviceState = HardwareDeviceState(
        status = HardwareDeviceStatus.UNKNOWN,
        label = "Belum diperiksa",
        detailMessage = "Kasir tetap bisa cari barang manual lewat SKU atau nama produk."
    ),
    val cashDrawer: HardwareDeviceState = HardwareDeviceState(
        status = HardwareDeviceStatus.UNKNOWN,
        label = "Belum diperiksa",
        detailMessage = "Transaksi tunai tetap bisa lanjut dengan catatan audit manual bila laci bermasalah."
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
                detailMessage = "Printer belum terhubung. Struk final tetap aman dan bisa dicetak ulang nanti."
            )
        )
    }
}
