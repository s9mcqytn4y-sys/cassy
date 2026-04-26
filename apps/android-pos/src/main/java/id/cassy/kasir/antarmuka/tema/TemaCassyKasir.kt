package id.cassy.kasir.antarmuka.tema

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val WarnaPrimerCassy = Color(0xFF2F6F5E)
private val WarnaPermukaanCassy = Color(0xFFFFFBFE)
private val WarnaLatarCassy = Color(0xFFF8FAF9)
private val WarnaTeksUtamaCassy = Color(0xFF17201C)

private val SkemaWarnaCassyKasir = lightColorScheme(
    primary = WarnaPrimerCassy,
    background = WarnaLatarCassy,
    surface = WarnaPermukaanCassy,
    onPrimary = Color.White,
    onBackground = WarnaTeksUtamaCassy,
    onSurface = WarnaTeksUtamaCassy,
)

@Composable
fun TemaCassyKasir(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = SkemaWarnaCassyKasir,
        content = content,
    )
}
