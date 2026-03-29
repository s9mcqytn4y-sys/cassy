package id.azureenterprise.cassy.desktop

import java.text.NumberFormat
import java.util.Locale

/**
 * Utility untuk formatting Rupiah yang konsisten di seluruh aplikasi Desktop.
 */
fun Double.formatRupiah(): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    formatter.maximumFractionDigits = 0
    return formatter.format(this).replace("Rp", "Rp ")
}

fun Long.formatRupiah(): String = this.toDouble().formatRupiah()
