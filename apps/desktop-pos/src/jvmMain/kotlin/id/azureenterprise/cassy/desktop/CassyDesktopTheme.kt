package id.azureenterprise.cassy.desktop

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CassyLightColors = lightColorScheme(
    primary = Color(0xFF0F6C78),
    onPrimary = Color(0xFFF7FCFD),
    primaryContainer = Color(0xFFD7EEF2),
    onPrimaryContainer = Color(0xFF08353B),
    secondary = Color(0xFF2F7EA1),
    onSecondary = Color(0xFFFFFFFF),
    surface = Color(0xFFF7F8F6),
    surfaceVariant = Color(0xFFE8EEEA),
    background = Color(0xFFF1F4F1),
    onBackground = Color(0xFF17212B),
    onSurface = Color(0xFF17212B),
    outline = Color(0xFFA9B9B7),
    error = Color(0xFFB42318)
)

private val CassyDarkColors = darkColorScheme(
    primary = Color(0xFF63B2BE),
    onPrimary = Color(0xFF042A30),
    primaryContainer = Color(0xFF0D4E58),
    onPrimaryContainer = Color(0xFFD7EEF2),
    secondary = Color(0xFF79B4D0),
    onSecondary = Color(0xFF0B2430),
    surface = Color(0xFF101716),
    surfaceVariant = Color(0xFF182321),
    background = Color(0xFF0D1312),
    onBackground = Color(0xFFE7EEEA),
    onSurface = Color(0xFFE7EEEA),
    outline = Color(0xFF48615E),
    error = Color(0xFFF16E62)
)

@Composable
fun CassyDesktopTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) CassyDarkColors else CassyLightColors,
        content = content
    )
}
