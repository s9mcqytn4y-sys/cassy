package id.azureenterprise.cassy.desktop

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CassyLightColors = lightColorScheme(
    primary = Color(0xFF0F7672),
    onPrimary = Color(0xFFF8FCFC),
    primaryContainer = Color(0xFFD9EFED),
    onPrimaryContainer = Color(0xFF083D3B),
    secondary = Color(0xFF67B8F7),
    surface = Color(0xFFF7FAFC),
    surfaceVariant = Color(0xFFE6EEF2),
    background = Color(0xFFF2F6F8),
    onBackground = Color(0xFF1F2937),
    onSurface = Color(0xFF1F2937),
    outline = Color(0xFFB2C2CC),
    error = Color(0xFFDC2626)
)

private val CassyDarkColors = darkColorScheme(
    primary = Color(0xFF1AA39D),
    onPrimary = Color(0xFF052A28),
    primaryContainer = Color(0xFF0B5350),
    onPrimaryContainer = Color(0xFFD9EFED),
    secondary = Color(0xFF67B8F7),
    surface = Color(0xFF10181C),
    surfaceVariant = Color(0xFF172229),
    background = Color(0xFF0C1215),
    onBackground = Color(0xFFE6EEF2),
    onSurface = Color(0xFFE6EEF2),
    outline = Color(0xFF48636F),
    error = Color(0xFFEF5350)
)

@Composable
fun CassyDesktopTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) CassyDarkColors else CassyLightColors,
        content = content
    )
}
