package id.azureenterprise.cassy.desktop

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val CassyOperationalColors = lightColorScheme(
    primary = Color(0xFF1E6F67),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD6ECE7),
    onPrimaryContainer = Color(0xFF113F3A),
    secondary = Color(0xFF2F6EA6),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD9E7F5),
    onSecondaryContainer = Color(0xFF193956),
    tertiary = Color(0xFF9A6A14),
    onTertiary = Color(0xFFFFFFFF),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFF1F4F6),
    background = Color(0xFFF5F7F8),
    onBackground = Color(0xFF1F2937),
    onSurface = Color(0xFF1F2937),
    onSurfaceVariant = Color(0xFF5B6675),
    outline = Color(0xFFD4DCE3),
    error = Color(0xFFB8403A),
    errorContainer = Color(0xFFF8DEDB)
)

private val CassyOperationalShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(10.dp),
    large = RoundedCornerShape(12.dp),
    extraLarge = RoundedCornerShape(14.dp)
)

@Composable
fun CassyDesktopTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = CassyOperationalColors,
        shapes = CassyOperationalShapes,
        content = content
    )
}
