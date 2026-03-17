package id.azureenterprise.cassy.desktop

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.azureenterprise.cassy.masterdata.domain.Product
import java.text.NumberFormat
import java.util.*

/**
 * CassyCurrencyInput: Hardened input for money/nominal values.
 * Optimized for Numpad usage and fast retail input.
 */
@Composable
fun CassyCurrencyInput(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth(),
    helperText: String? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    onImeAction: () -> Unit = {}
) {
    val localeID = Locale("in", "ID")
    val formatter = NumberFormat.getCurrencyInstance(localeID).apply {
        maximumFractionDigits = 0
    }

    val displayValue = remember(value) {
        val numericValue = value.filter { it.isDigit() }.toLongOrNull() ?: 0L
        if (numericValue == 0L && value.isEmpty()) ""
        else formatter.format(numericValue).replace("Rp", "Rp ").trim()
    }

    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = displayValue,
            onValueChange = { newValue ->
                val digitsOnly = newValue.filter { it.isDigit() }
                if (digitsOnly.length <= 12) {
                    onValueChange(digitsOnly)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            textStyle = LocalTextStyle.current.copy(
                textAlign = TextAlign.End,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp
            ),
            placeholder = { Text("Rp 0", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = { onImeAction() }),
            isError = isError,
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )
        if (isError && errorMessage != null) {
            CassyErrorFeedback(message = errorMessage)
        } else if (helperText != null) {
            Text(
                text = helperText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

/**
 * CassySlimRail: 72dp width rail for high-throughput desktop workspace.
 */
@Composable
fun CassySlimRail(
    selectedStage: DesktopStage,
    onLogout: () -> Unit,
    onReload: () -> Unit
) {
    NavigationRail(
        modifier = Modifier.width(72.dp),
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        header = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = "Cassy POS",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp).padding(top = 12.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    ) {
        RailItem(
            selected = selectedStage is DesktopStage.Catalog,
            icon = Icons.AutoMirrored.Filled.List,
            label = "Kasir",
            onClick = {}
        )
        RailItem(
            selected = false,
            icon = Icons.Default.DateRange,
            label = "Riwayat",
            onClick = {}
        )
        RailItem(
            selected = false,
            icon = Icons.Default.Settings,
            label = "Sistem",
            onClick = {}
        )

        Spacer(modifier = Modifier.weight(1f))

        RailItem(
            selected = false,
            icon = Icons.Default.Refresh,
            label = "Muat",
            onClick = onReload
        )
        RailItem(
            selected = false,
            icon = Icons.AutoMirrored.Filled.ExitToApp,
            label = "Keluar",
            onClick = onLogout,
            tone = UiTone.Danger
        )
        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
private fun RailItem(
    selected: Boolean,
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    tone: UiTone = UiTone.Info
) {
    NavigationRailItem(
        selected = selected,
        onClick = onClick,
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (selected) MaterialTheme.colorScheme.primary else if (tone == UiTone.Danger) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        label = { Text(label, fontSize = 10.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal) },
        colors = NavigationRailItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.primary,
            indicatorColor = MaterialTheme.colorScheme.primaryContainer
        )
    )
}

/**
 * CassyDenseProductRow: Throughput-oriented product representation.
 */
@Composable
fun CassyDenseProductRow(
    product: Product,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "SKU: ${product.sku}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Rp ${product.price.toInt()}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/**
 * CassyTopBar: Global readiness and status monitoring.
 */
@Composable
fun CassyTopBar(
    state: DesktopShellState,
    hardware: CashierHardwareSnapshot,
    syncStatus: String = "Online"
) {
    Surface(
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth().height(48.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = state.storeName ?: "Cassy Store",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = " • ${state.terminalName ?: "T01"}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatusIndicator(label = "Sync", status = syncStatus, tone = if (syncStatus == "Online") UiTone.Success else UiTone.Warning)
                StatusIndicator(label = "Print", status = hardware.printer.label, tone = hardwareTone(hardware.printer.status))
                StatusIndicator(label = "Scan", status = hardware.scanner.label, tone = hardwareTone(hardware.scanner.status))
                StatusIndicator(label = "Drawer", status = hardware.cashDrawer.label, tone = hardwareTone(hardware.cashDrawer.status))

                VerticalDivider(modifier = Modifier.height(16.dp))

                Text(
                    text = state.operatorName ?: "No User",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun StatusIndicator(label: String, status: String, tone: UiTone) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(modifier = Modifier.size(6.dp).background(toneColor(tone), CircleShape))
        Text(text = status, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium)
    }
}

fun toneColor(tone: UiTone): Color = when (tone) {
    UiTone.Info -> Color(0xFF0E74AF)
    UiTone.Success -> Color(0xFF16A34A)
    UiTone.Warning -> Color(0xFFD97706)
    UiTone.Danger -> Color(0xFFDC2626)
}

private fun hardwareTone(status: HardwareDeviceStatus): UiTone = when (status) {
    HardwareDeviceStatus.READY -> UiTone.Success
    HardwareDeviceStatus.UNKNOWN -> UiTone.Warning
    HardwareDeviceStatus.WARNING -> UiTone.Warning
    HardwareDeviceStatus.UNAVAILABLE -> UiTone.Danger
}
