package id.azureenterprise.cassy.desktop

import java.text.NumberFormat
import java.util.*

object CassyFormatter {
    private val rupiahFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
        maximumFractionDigits = 0
    }

    fun formatRupiah(amount: Double): String {
        return rupiahFormat.format(amount).replace("Rp", "Rp ")
    }

    fun formatRupiah(amount: Number): String {
        return formatRupiah(amount.toDouble())
    }
}

fun Double.toRupiah(): String = CassyFormatter.formatRupiah(this)
fun Int.toRupiah(): String = CassyFormatter.formatRupiah(this.toDouble())
fun Long.toRupiah(): String = CassyFormatter.formatRupiah(this.toDouble())
