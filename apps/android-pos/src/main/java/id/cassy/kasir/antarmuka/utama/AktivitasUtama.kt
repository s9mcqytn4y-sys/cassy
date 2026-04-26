package id.cassy.kasir.antarmuka.utama

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import id.cassy.kasir.antarmuka.navigasi.NavigasiAplikasiCassyKasir
import id.cassy.kasir.antarmuka.tema.TemaCassyKasir

class AktivitasUtama : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TemaCassyKasir {
                Surface {
                    NavigasiAplikasiCassyKasir()
                }
            }
        }
    }
}
