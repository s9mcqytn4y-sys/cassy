package id.azureenterprise.cassy.desktop

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.azureenterprise.cassy.kernel.domain.OperationDecision
import id.azureenterprise.cassy.kernel.domain.OperationStatus
import id.azureenterprise.cassy.masterdata.domain.Product
import java.text.NumberFormat
import java.util.*

/**
 * Shared UI Components for Cassy POS Desktop
 */

@Composable
fun WorkspacePage(
    title: String,
    subtitle: String,
    scrollable: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    val modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 20.dp).let { base ->
        if (scrollable) base.verticalScroll(rememberScrollState()) else base
    }
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
        Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
        content()
    }
}

@Composable
fun WorkspaceCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 1.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.ExtraBold)
            content()
        }
    }
}

@Composable
fun SummaryRowV2(label: String, value: String, isAlert: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = if (isAlert) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun ApprovalRow(
    title: String,
    detail: String,
    label: String,
    onApprove: () -> Unit,
    onDeny: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = RoundedCornerShape(4.dp)) {
                    Text(label, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }
            Text(detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onDeny, modifier = Modifier.weight(1f)) { Text("Tolak") }
                Button(onClick = onApprove, modifier = Modifier.weight(1f)) { Text("Setujui") }
            }
        }
    }
}

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
            shape = RoundedCornerShape(8.dp)
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
    errorText: String? = null,
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
            shape = RoundedCornerShape(8.dp),
            isError = errorText != null,
            placeholder = placeholder?.let { { Text(it) } },
            leadingIcon = leadingIcon?.let { icon ->
                { Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = imeAction
            ),
            keyboardActions = KeyboardActions(onDone = { onImeAction() }),
            visualTransformation = visualTransformation
        )
        errorText?.let {
            Text(text = it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
        } ?: helperText?.let {
            Text(text = it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
    errorText: String? = null,
    onImeAction: () -> Unit = {}
) {
    SemanticTextField(
        label = label,
        value = value,
        onValueChange = { newValue -> onValueChange(newValue.filter(Char::isDigit).take(6)) },
        modifier = modifier,
        helperText = helperText,
        errorText = errorText,
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
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        hints.forEach { hint ->
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
            ) {
                Text(
                    text = hint,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

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
        shape = RoundedCornerShape(10.dp),
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

@Composable
fun CassyCommandPalette(
    availableWorkspaces: List<DesktopWorkspace>,
    onDismiss: () -> Unit,
    onSelectWorkspace: (DesktopWorkspace) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pusat Perintah", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Pilih area kerja utama. Pintasan laptop tetap punya alias Ctrl-based.",
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
        title = { Text("Bantuan Pintasan", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Ctrl+K: pusat perintah")
                Text("Ctrl+/: bantuan shortcut")
                Text("Ctrl+Shift+S: sync center")
                Text("Ctrl+Shift+R: laporan")
                Text("Ctrl+Shift+I: inventori / stock truth")
                Text("Ctrl+Shift+C: cash control")
                Text("Ctrl+Shift+H: guided dashboard")
                Text("F1/F5: replay sync")
                Text("F7: void, F8: laporan, F9: inventori, F10: kas, F11: close shift, F12: diagnostics")
                Text("Esc: tutup surface ringan")
            }
        },
        confirmButton = { OutlinedButton(onClick = onDismiss) { Text("Tutup") } },
        dismissButton = {}
    )
}

@Composable
fun CassyStepUpAuthDialog(
    state: StepUpAuthState,
    onDismiss: () -> Unit,
    onApproverChanged: (String) -> Unit,
    onPinChanged: (String) -> Unit,
    onDecisionNoteChanged: (String) -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(state.title, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    state.detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (state.approverOptions.isEmpty()) {
                    Text(
                        "Belum ada operator supervisor/owner aktif di terminal ini.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Pilih approver", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                        state.approverOptions.forEach { option ->
                            Surface(
                                modifier = Modifier.fillMaxWidth().clickable { onApproverChanged(option.id) },
                                shape = RoundedCornerShape(12.dp),
                                tonalElevation = if (state.approverOperatorId == option.id) 2.dp else 0.dp
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(option.displayName, fontWeight = FontWeight.SemiBold)
                                    Text(option.roleLabel, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
                SemanticPinField(
                    label = "PIN Approver",
                    value = state.pin,
                    onValueChange = onPinChanged,
                    helperText = "PIN tidak mengganti sesi kasir aktif. Ini hanya step-up auth."
                )
                SemanticTextField(
                    label = "Catatan Keputusan",
                    value = state.decisionNote,
                    onValueChange = onDecisionNoteChanged,
                    singleLine = false,
                    helperText = "Catatan ini dipakai sebagai jejak approval/penolakan."
                )
                state.error?.let { error ->
                    Text(
                        error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = state.approverOptions.isNotEmpty()
            ) {
                Text("Verifikasi & Lanjutkan")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

@Composable
fun BannerCard(
    banner: UiBanner,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.widthIn(max = 420.dp),
        shape = RoundedCornerShape(12.dp),
        color = toneContainerColor(banner.tone),
        tonalElevation = 4.dp,
        border = BorderStroke(1.dp, toneColor(banner.tone).copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = when (banner.tone) {
                    UiTone.Success -> Icons.Default.CheckCircle
                    UiTone.Warning -> Icons.Default.Warning
                    UiTone.Danger -> Icons.Default.Error
                    UiTone.Info -> Icons.Default.Info
                },
                contentDescription = null,
                tint = toneColor(banner.tone),
                modifier = Modifier.size(24.dp)
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = banner.message,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = toneContentColor(banner.tone)
                )
            }
            IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = toneColor(banner.tone).copy(alpha = 0.6f))
            }
        }
    }
}

@Composable
fun OperationDecisionRow(
    decision: OperationDecision,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = toneContainerColor(
            when (decision.status) {
                OperationStatus.READY -> UiTone.Success
                OperationStatus.REQUIRES_APPROVAL -> UiTone.Warning
                else -> UiTone.Danger
            }
        ).copy(alpha = 0.4f),
        border = BorderStroke(1.dp, toneColor(
            when (decision.status) {
                OperationStatus.READY -> UiTone.Success
                OperationStatus.REQUIRES_APPROVAL -> UiTone.Warning
                else -> UiTone.Danger
            }
        ).copy(alpha = 0.15f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when (decision.status) {
                    OperationStatus.READY -> Icons.Default.CheckCircle
                    OperationStatus.REQUIRES_APPROVAL -> Icons.Default.Pending
                    else -> Icons.Default.Block
                },
                contentDescription = null,
                tint = toneColor(
                    when (decision.status) {
                        OperationStatus.READY -> UiTone.Success
                        OperationStatus.REQUIRES_APPROVAL -> UiTone.Warning
                        else -> UiTone.Danger
                    }
                ),
                modifier = Modifier.size(20.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(decision.title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Text(decision.message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun StatusIndicator(label: String, status: String, tone: UiTone) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Surface(modifier = Modifier.size(6.dp), shape = CircleShape, color = toneColor(tone)) {}
        Text(text = "$label: $status", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium)
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
    UiTone.Info -> Color(0xFFD5EBF4)
    UiTone.Success -> Color(0xFFD9F0DF)
    UiTone.Warning -> Color(0xFFF3DEB0)
    UiTone.Danger -> Color(0xFFF2D3D0)
}

fun toneContentColor(tone: UiTone): Color = when (tone) {
    UiTone.Info -> Color(0xFF163D4E)
    UiTone.Success -> Color(0xFF1C4028)
    UiTone.Warning -> Color(0xFF5C3A00)
    UiTone.Danger -> Color(0xFF5A1713)
}
