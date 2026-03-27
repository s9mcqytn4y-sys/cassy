package id.azureenterprise.cassy.desktop

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.azureenterprise.cassy.kernel.domain.SyncLevel
import id.azureenterprise.cassy.kernel.domain.SyncStatus
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
    val formatter = remember {
        NumberFormat.getCurrencyInstance(Locale("in", "ID")).apply {
            maximumFractionDigits = 0
        }
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
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 4.dp)
            )
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

@Composable
fun SemanticTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth(),
    helperText: String? = null,
    placeholder: String? = null,
    leadingIcon: ImageVector? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    singleLine: Boolean = true,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    onImeAction: () -> Unit = {}
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = singleLine,
            shape = RoundedCornerShape(14.dp),
            placeholder = placeholder?.let { { Text(it) } },
            leadingIcon = leadingIcon?.let { icon ->
                { Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = imeAction
            ),
            keyboardActions = KeyboardActions(
                onDone = { onImeAction() },
                onGo = { onImeAction() },
                onSearch = { onImeAction() },
                onSend = { onImeAction() }
            ),
            visualTransformation = visualTransformation
        )
        helperText?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SemanticPinField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth(),
    helperText: String? = null,
    onImeAction: () -> Unit = {}
) {
    SemanticTextField(
        label = label,
        value = value,
        onValueChange = { newValue -> onValueChange(newValue.filter(Char::isDigit).take(6)) },
        modifier = modifier,
        helperText = helperText,
        placeholder = "6 digit PIN",
        leadingIcon = Icons.Default.Lock,
        keyboardType = KeyboardType.NumberPassword,
        imeAction = ImeAction.Done,
        visualTransformation = PasswordVisualTransformation(),
        onImeAction = onImeAction
    )
}

@Composable
fun ShortcutHintBar(
    hints: List<String>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        hints.forEach { hint ->
            AssistChip(
                onClick = {},
                enabled = false,
                label = { Text(hint) }
            )
        }
    }
}

/**
 * CassySlimRail: 72dp width rail for high-throughput desktop workspace.
 * Uses brand logo for identity.
 */
@Composable
fun CassySlimRail(
    state: DesktopShellState,
    selectedWorkspace: DesktopWorkspace,
    stage: DesktopStage,
    onSelectWorkspace: (DesktopWorkspace) -> Unit,
    onLogout: () -> Unit,
    onReload: () -> Unit
) {
    NavigationRail(
        modifier = Modifier.width(92.dp),
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        header = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = painterResource("logo.png"),
                    contentDescription = "Cassy Logo",
                    modifier = Modifier.size(54.dp).padding(top = 14.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Cassy",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(18.dp))
            }
        }
    ) {
        state.availableWorkspaces.forEach { workspace ->
            RailItem(
                selected = stage == DesktopStage.Workspace && workspace == selectedWorkspace,
                icon = workspace.toIcon(),
                label = workspace.shortLabel,
                onClick = { onSelectWorkspace(workspace) }
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        RailItem(
            selected = false,
            icon = Icons.Default.Refresh,
            label = "Sync",
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
    syncStatus: SyncStatus? = null,
    onOpenCommand: () -> Unit = {}
) {
    val runtimeChannel = remember { System.getProperty("cassy.runtime.channel", "unknown") }
    val releaseVersion = remember { System.getProperty("cassy.release.version", "dev") }
    val buildLabel = remember(runtimeChannel, releaseVersion) {
        if (runtimeChannel == "packaged-release-candidate") "RC $releaseVersion" else "DEV $releaseVersion"
    }
    val buildTone = if (runtimeChannel == "packaged-release-candidate") UiTone.Info else UiTone.Warning

    val syncLabel = syncStatus?.let {
        when (it.level) {
            SyncLevel.HEALTHY -> "Online"
            SyncLevel.PENDING -> "Sync (${it.pendingCount})"
            SyncLevel.DELAYED -> "Delayed (${it.pendingCount})"
            SyncLevel.STALLED -> "Stalled!"
            SyncLevel.ERROR -> when {
                it.failedCount > 0 -> "Error (${it.failedCount})"
                it.pendingCount > 0 -> "Error (${it.pendingCount})"
                else -> "Sync Error"
            }
        }
    } ?: "Offline"

    val syncTone = syncStatus?.let {
        when (it.level) {
            SyncLevel.HEALTHY -> UiTone.Success
            SyncLevel.PENDING -> UiTone.Info
            SyncLevel.DELAYED -> UiTone.Warning
            SyncLevel.STALLED, SyncLevel.ERROR -> UiTone.Danger
        }
    } ?: UiTone.Warning

    Surface(
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth().height(64.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = state.workspaceTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${state.storeName ?: "Toko belum aktif"} • ${state.terminalName ?: "Terminal belum terikat"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatusIndicator(label = "Build", status = buildLabel, tone = buildTone)
                state.nextActionLabel?.let {
                    StatusIndicator(label = "Next", status = it, tone = UiTone.Info)
                }
                StatusIndicator(label = "Sync", status = syncLabel, tone = syncTone)
                VerticalDivider(modifier = Modifier.height(16.dp))
                Text(
                    text = humanizeOperatorLabel(state.operatorName, state.roleLabel),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                AssistChip(
                    onClick = onOpenCommand,
                    label = { Text("Ctrl+K") }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CassyBottomStatusStrip(
    shell: DesktopShellState,
    operations: OperationsState,
    hardware: CashierHardwareSnapshot
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
        modifier = Modifier.fillMaxWidth()
    ) {
        FlowRow(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            AssistChip(
                onClick = {},
                enabled = false,
                label = { Text(humanizeBusinessDayLabel(operations.businessDayLabel)) }
            )
            AssistChip(
                onClick = {},
                enabled = false,
                label = { Text(humanizeShiftLabel(operations.shiftLabel)) }
            )
            AssistChip(
                onClick = {},
                enabled = false,
                label = { Text("Printer ${hardware.printer.label}") }
            )
            AssistChip(
                onClick = {},
                enabled = false,
                label = { Text("Scanner ${hardware.scanner.label}") }
            )
            shell.nextActionLabel?.let { next ->
                AssistChip(
                    onClick = {},
                    enabled = false,
                    label = { Text("Tindakan berikutnya: $next") }
                )
            }
        }
    }
}

@Composable
fun CassyCommandPalette(
    availableWorkspaces: List<DesktopWorkspace>,
    onDismiss: () -> Unit,
    onSelectWorkspace: (DesktopWorkspace) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Command Palette", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Pilih workspace utama. Fokus shortcut laptop tetap punya alias Ctrl-based.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                availableWorkspaces.forEach { workspace ->
                    Surface(
                        modifier = Modifier.fillMaxWidth().clickable { onSelectWorkspace(workspace) },
                        shape = RoundedCornerShape(12.dp),
                        tonalElevation = 1.dp
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(workspace.title, fontWeight = FontWeight.SemiBold)
                            Text(workspace.shortLabel, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        },
        confirmButton = { OutlinedButton(onClick = onDismiss) { Text("Tutup") } },
        dismissButton = {}
    )
}

@Composable
fun CassyShortcutHelpDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Shortcut Keyboard", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Ctrl+K: command palette")
                Text("Ctrl+/: bantuan shortcut")
                Text("Ctrl+Shift+S: sistem / sync / dev tools")
                Text("Ctrl+Shift+R: laporan")
                Text("Ctrl+Shift+I: inventori")
                Text("Ctrl+Shift+C: operasional")
                Text("Ctrl+Shift+H: guided dashboard")
                Text("F1/F5: replay sync")
                Text("F7-F12: alias legacy ke workspace operasional terkait")
                Text("Esc: tutup surface ringan")
            }
        },
        confirmButton = { OutlinedButton(onClick = onDismiss) { Text("Tutup") } },
        dismissButton = {}
    )
}

@Composable
fun StatusIndicator(label: String, status: String, tone: UiTone) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(modifier = Modifier.size(6.dp).background(toneColor(tone), CircleShape))
        Text(text = status, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium)
    }
}

fun toneColor(tone: UiTone): Color = when (tone) {
    UiTone.Info -> Color(0xFF1167B1)
    UiTone.Success -> Color(0xFF1D7A46)
    UiTone.Warning -> Color(0xFFB7791F)
    UiTone.Danger -> Color(0xFFB42318)
}

@Composable
fun toneContainerColor(tone: UiTone): Color = when (tone) {
    UiTone.Info -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.88f)
    UiTone.Success -> Color(0xFFE2F6E9)
    UiTone.Warning -> Color(0xFFFBEBC8)
    UiTone.Danger -> MaterialTheme.colorScheme.errorContainer
}

private fun hardwareTone(status: HardwareDeviceStatus): UiTone = when (status) {
    HardwareDeviceStatus.READY -> UiTone.Success
    HardwareDeviceStatus.UNKNOWN -> UiTone.Warning
    HardwareDeviceStatus.WARNING -> UiTone.Warning
    HardwareDeviceStatus.UNAVAILABLE -> UiTone.Danger
}

private fun DesktopWorkspace.toIcon(): ImageVector = when (this) {
    DesktopWorkspace.Dashboard -> Icons.Default.SpaceDashboard
    DesktopWorkspace.Cashier -> Icons.Default.PointOfSale
    DesktopWorkspace.History -> Icons.Default.History
    DesktopWorkspace.Inventory -> Icons.Default.Inventory2
    DesktopWorkspace.Operations -> Icons.Default.AdminPanelSettings
    DesktopWorkspace.Reporting -> Icons.Default.Assessment
    DesktopWorkspace.System -> Icons.Default.Settings
}
