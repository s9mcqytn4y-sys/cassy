package id.cassy.kasir

import android.app.Application
import id.cassy.kasir.di.GudangDependensiKasir

class AplikasiKasir : Application() {
    val gudangDependensiKasir: GudangDependensiKasir by lazy {
        GudangDependensiKasir(konteksAplikasi = this)
    }
}
