package id.azureenterprise.cassy.desktop

import id.azureenterprise.cassy.kernel.domain.AccessCapability
import id.azureenterprise.cassy.kernel.domain.OperatorRole
import id.azureenterprise.cassy.kernel.domain.supports
import id.azureenterprise.cassy.sales.domain.SaleHistoryEntry

enum class DesktopWorkspace(
    val title: String,
    val shortLabel: String
) {
    Dashboard(title = "Operasional Harian", shortLabel = "Beranda"),
    Cashier(title = "Kasir", shortLabel = "Kasir"),
    History(title = "Riwayat Transaksi", shortLabel = "Riwayat"),
    Inventory(title = "Inventori", shortLabel = "Inventori"),
    Operations(title = "Operasional", shortLabel = "Operasional"),
    Reporting(title = "Laporan", shortLabel = "Laporan"),
    System(title = "Sistem", shortLabel = "Sistem"),
    Settings(title = "Pengaturan", shortLabel = "Setelan")
}

enum class DesktopInventoryRoute(
    val title: String,
    val shortLabel: String
) {
    StockOverview(title = "Ringkasan Stok", shortLabel = "Stok"),
    StockCount(title = "Stok Opname", shortLabel = "Opname"),
    Adjustment(title = "Penyesuaian", shortLabel = "Koreksi"),
    Discrepancy(title = "Discrepancy", shortLabel = "Selisih"),
    MasterData(title = "Data Produk", shortLabel = "Produk")
}

enum class DesktopOperationsRoute(
    val title: String,
    val shortLabel: String
) {
    CashControl(title = "Kontrol Kas", shortLabel = "Kas"),
    VoidSale(title = "Void Transaksi", shortLabel = "Void"),
    CloseShift(title = "Tutup Shift", shortLabel = "Shift"),
    CloseDay(title = "Tutup Hari", shortLabel = "Hari"),
    SyncCenter(title = "Sinkronisasi", shortLabel = "Sinkron"),
    Diagnostics(title = "Diagnostik", shortLabel = "Cek")
}

enum class DesktopSettingsRoute {
    StoreProfile,
    System
}

fun availableWorkspacesFor(
    role: OperatorRole?,
    canAccessSalesHome: Boolean
): List<DesktopWorkspace> {
    if (role == null) return listOf(DesktopWorkspace.Dashboard)

    val workspaces = mutableListOf<DesktopWorkspace>()
    if (canAccessSalesHome && role in setOf(OperatorRole.CASHIER, OperatorRole.OWNER) && role.supports(AccessCapability.ACCESS_CATALOG)) {
        workspaces += DesktopWorkspace.Cashier
    }
    workspaces += DesktopWorkspace.Dashboard
    workspaces += DesktopWorkspace.History
    workspaces += DesktopWorkspace.Inventory
    workspaces += DesktopWorkspace.Operations
    if (role != OperatorRole.CASHIER) {
        workspaces += DesktopWorkspace.Reporting
        workspaces += DesktopWorkspace.System
        workspaces += DesktopWorkspace.Settings
    }
    return workspaces.distinct()
}

fun humanizeShiftLabel(rawId: String?): String =
    if (rawId.isNullOrBlank()) "Shift belum dibuka" else "Shift aktif"

fun humanizeBusinessDayLabel(rawId: String?): String =
    if (rawId.isNullOrBlank()) "Hari bisnis belum dibuka" else "Hari bisnis aktif"

fun humanizeApprovalLabel(rawId: String?): String =
    if (rawId.isNullOrBlank()) "Menunggu review" else "Perlu keputusan"

fun humanizeInventoryReviewLabel(rawId: String?): String =
    if (rawId.isNullOrBlank()) "Belum ada review stok" else "Perlu tindak lanjut stok"

fun humanizeTerminalLabel(name: String?, rawId: String?): String {
    return when {
        !name.isNullOrBlank() -> name
        !rawId.isNullOrBlank() -> "Perangkat kasir aktif"
        else -> "Perangkat kasir belum terhubung"
    }
}

fun humanizeOperatorLabel(name: String?, role: String?): String {
    return when {
        !name.isNullOrBlank() && !role.isNullOrBlank() -> "$name - ${role.lowercase().replaceFirstChar(Char::titlecase)}"
        !name.isNullOrBlank() -> name
        !role.isNullOrBlank() -> role.lowercase().replaceFirstChar(Char::titlecase)
        else -> "Operator belum aktif"
    }
}

fun humanizeSaleReference(entry: SaleHistoryEntry): String =
    entry.localNumber.ifBlank { "Transaksi final" }
