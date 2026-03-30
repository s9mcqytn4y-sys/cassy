package id.azureenterprise.cassy.desktop

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val CassyLightColors = lightColorScheme(
    primary = Color(0xFF275D75),
    onPrimary = Color(0xFFF9FBFC),
    primaryContainer = Color(0xFFD9EAF2),
    onPrimaryContainer = Color(0xFF163949),
    secondary = Color(0xFF556B7A),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE1E8ED),
    onSecondaryContainer = Color(0xFF23323C),
    tertiary = Color(0xFFB7791F),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFF5E3BF),
    onTertiaryContainer = Color(0xFF573400),
    surface = Color(0xFFFBF9F6),
    surfaceVariant = Color(0xFFF1ECE5),
    background = Color(0xFFF4F1EC),
    onBackground = Color(0xFF1D2730),
    onSurface = Color(0xFF1D2730),
    onSurfaceVariant = Color(0xFF66727C),
    outline = Color(0xFFD4CDC4),
    outlineVariant = Color(0xFFE6DED4),
    error = Color(0xFFB42318),
    errorContainer = Color(0xFFF6D8D5)
)

private val CassyDarkColors = darkColorScheme(
    primary = Color(0xFF9EC6D8),
    onPrimary = Color(0xFF153341),
    primaryContainer = Color(0xFF224B5F),
    onPrimaryContainer = Color(0xFFD8EAF3),
    secondary = Color(0xFFB0BEC8),
    onSecondary = Color(0xFF21303A),
    secondaryContainer = Color(0xFF334550),
    onSecondaryContainer = Color(0xFFE3EAEE),
    tertiary = Color(0xFFF0C46C),
    onTertiary = Color(0xFF4D3000),
    tertiaryContainer = Color(0xFF6C4A12),
    onTertiaryContainer = Color(0xFFF9E3AF),
    surface = Color(0xFF1A1F24),
    surfaceVariant = Color(0xFF232A31),
    background = Color(0xFF15191E),
    onBackground = Color(0xFFF1F3F5),
    onSurface = Color(0xFFF1F3F5),
    onSurfaceVariant = Color(0xFFBDC7CE),
    outline = Color(0xFF48555E),
    outlineVariant = Color(0xFF323C43),
    error = Color(0xFFF28479),
    errorContainer = Color(0xFF6A221B)
)

private val CassyTypography = Typography(
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        letterSpacing = (-0.2).sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 21.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 19.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 17.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 15.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 14.sp
    )
)

@Composable
fun CassyDesktopTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) CassyDarkColors else CassyLightColors,
        typography = CassyTypography,
        content = content
    )
}
