package id.azureenterprise.cassy.desktop

import id.azureenterprise.cassy.kernel.domain.OperatorRole
import id.azureenterprise.cassy.kernel.domain.supports
import id.azureenterprise.cassy.kernel.domain.AccessCapability
import id.azureenterprise.cassy.sales.domain.SaleHistoryEntry

enum class DesktopWorkspace(
    val title: String,
    val shortLabel: String
) {
    Dashboard(title = "Guided Operations", shortLabel = "Dashboard"),
    Cashier(title = "Kasir", shortLabel = "Kasir"),
    History(title = "Riwayat Transaksi", shortLabel = "Riwayat"),
    Inventory(title = "Inventori", shortLabel = "Inventori"),
    Operations(title = "Operasional", shortLabel = "Operasional"),
    Reporting(title = "Laporan", shortLabel = "Laporan"),
    System(title = "Sistem", shortLabel = "Sistem")
}

enum class DesktopInventoryRoute(
    val title: String,
    val shortLabel: String
) {
    StockOverview(title = "Stock Truth", shortLabel = "Stock"),
    MasterData(title = "Master Data", shortLabel = "Master Data")
}

enum class DesktopOperationsRoute(
    val title: String,
    val shortLabel: String
) {
    CashControl(title = "Cash Control", shortLabel = "Kas"),
    VoidSale(title = "Void Sale", shortLabel = "Void"),
    CloseShift(title = "Close Shift", shortLabel = "Shift"),
    CloseDay(title = "Close Day", shortLabel = "Hari"),
    SyncCenter(title = "Sync Center", shortLabel = "Sync"),
    Diagnostics(title = "Diagnostics", shortLabel = "Diagnostik")
}

fun availableWorkspacesFor(
    role: OperatorRole?,
    canAccessSalesHome: Boolean
): List<DesktopWorkspace> {
    if (role == null) return listOf(DesktopWorkspace.Dashboard)

    val workspaces = mutableListOf<DesktopWorkspace>()
    if (canAccessSalesHome && role.supports(AccessCapability.ACCESS_CATALOG)) {
        workspaces += DesktopWorkspace.Cashier
    }
    workspaces += DesktopWorkspace.Dashboard
    workspaces += DesktopWorkspace.History
    workspaces += DesktopWorkspace.Inventory
    workspaces += DesktopWorkspace.Operations
    if (role != OperatorRole.CASHIER) {
        workspaces += DesktopWorkspace.Reporting
        workspaces += DesktopWorkspace.System
    }
    return workspaces.distinct()
}

fun humanizeShiftLabel(rawId: String?): String =
    humanizeEntityLabel(rawId, "Shift")

fun humanizeBusinessDayLabel(rawId: String?): String =
    humanizeEntityLabel(rawId, "Hari")

fun humanizeApprovalLabel(rawId: String?): String =
    humanizeEntityLabel(rawId, "Approval")

fun humanizeInventoryReviewLabel(rawId: String?): String =
    humanizeEntityLabel(rawId, "Review Stok")

fun humanizeTerminalLabel(name: String?, rawId: String?): String {
    return when {
        !name.isNullOrBlank() -> name
        !rawId.isNullOrBlank() -> humanizeEntityLabel(rawId, "Terminal")
        else -> "Terminal belum terikat"
    }
}

fun humanizeOperatorLabel(name: String?, role: String?): String {
    return when {
        !name.isNullOrBlank() && !role.isNullOrBlank() -> "$name • ${role.lowercase().replaceFirstChar(Char::titlecase)}"
        !name.isNullOrBlank() -> name
        !role.isNullOrBlank() -> role.lowercase().replaceFirstChar(Char::titlecase)
        else -> "Operator belum aktif"
    }
}

fun humanizeSaleReference(entry: SaleHistoryEntry): String =
    entry.localNumber.ifBlank { humanizeEntityLabel(entry.saleId, "Penjualan") }

private fun humanizeEntityLabel(rawId: String?, prefix: String): String {
    if (rawId.isNullOrBlank()) return "$prefix belum tersedia"
    val compact = rawId.substringAfterLast('_').takeLast(6).uppercase()
    return "$prefix $compact"
}
