package id.cassy.kasir.di

import android.content.Context

class GudangDependensiKasir(
    private val konteksAplikasi: Context,
) {
    val penyediaViewModelKasir: PenyediaViewModelKasir by lazy {
        PenyediaViewModelKasir()
    }
}
