package id.azureenterprise.cassy.desktop

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import id.azureenterprise.cassy.kernel.domain.CashMovementType
import id.azureenterprise.cassy.kernel.domain.OperationDecision
import id.azureenterprise.cassy.kernel.domain.OperationStatus
import id.azureenterprise.cassy.kernel.domain.OperationType
import id.azureenterprise.cassy.kernel.domain.OperationalControlSnapshot
import id.azureenterprise.cassy.kernel.domain.SyncLevel
import id.azureenterprise.cassy.kernel.domain.SyncStatus
import id.azureenterprise.cassy.sales.domain.SaleHistoryEntry
import kotlinx.datetime.Instant

@Composable
fun LoadingStage(state: DesktopLoadingState) {
    val animatedProgress by animateFloatAsState(
        targetValue = state.progress,
        animationSpec = tween(durationMillis = 460),
        label = "loading-progress"
    )
    val animatedScale by animateFloatAsState(
        targetValue = 0.92f + (animatedProgress * 0.12f),
        animationSpec = tween(durationMillis = 520),
        label = "loading-scale"
    )

    Box(
        modifier = Modifier.fillMaxSize().padding(horizontal = 28.dp, vertical = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.widthIn(max = 720.dp),
            shape = RoundedCornerShape(20.dp),
            tonalElevation = 2.dp
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 28.dp, vertical = 32.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(CASSY_BRAND_ICON_RESOURCE),
                    contentDescription = "Cassy POS",
                    modifier = Modifier
                        .size(96.dp)
                        .graphicsLayer {
                            scaleX = animatedScale
                            scaleY = animatedScale
                            alpha = 0.78f + (animatedProgress * 0.22f)
                        }
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(state.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text(
                        state.detail,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
                LinearProgressIndicator(
                    progress = { animatedProgress.coerceIn(0.06f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(8.dp)
                )
                Text(
                    "${(animatedProgress * 100).toInt()}% siap",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun FatalStage(message: String, onRetry: () -> Unit) {
    CenterPanel(
        title = "Gagal Memuat Data",
        subtitle = message,
        action = {
            Button(onClick = onRetry) { Text("Coba Lagi") }
        }
    )
}

@Composable
fun BootstrapStage(
    state: DesktopAppState,
    mode: BootstrapMode,
    onFieldChanged: (BootstrapField, String) -> Unit,
    onStoreProfileFieldChanged: (StoreProfileUiField, String) -> Unit,
    onStoreProfileToggleChanged: (StoreProfileToggleField, Boolean) -> Unit,
    onSelectAvatar: (BootstrapField) -> Unit,
    onClearAvatar: (BootstrapField) -> Unit,
    onSelectStoreLogo: () -> Unit,
    onClearStoreLogo: () -> Unit,
    onBootstrap: () -> Unit
) {
    val readinessItems = listOf(
        BootstrapReadinessItem(
            title = "Nama usaha",
            detail = "Nama yang muncul di aplikasi, struk, dan invoice.",
            isReady = state.storeProfile.businessName.isNotBlank()
        ),
        BootstrapReadinessItem(
            title = "Alamat dan kontak",
            detail = "Alamat terstruktur, telepon, dan catatan struk wajib lengkap.",
            isReady = state.storeProfile.streetAddress.isNotBlank() &&
                state.storeProfile.phoneNumber.isNotBlank() &&
                state.storeProfile.receiptNote.isNotBlank()
        ),
        BootstrapReadinessItem(
            title = "Terminal utama",
            detail = if (mode == BootstrapMode.FullSetup) "Penanda perangkat kasir utama." else "Terminal ini sudah terdaftar.",
            isReady = mode == BootstrapMode.CompleteIdentity || state.bootstrap.terminalName.isNotBlank()
        ),
        BootstrapReadinessItem(
            title = "Operator awal",
            detail = "Kasir dan supervisor awal wajib lengkap.",
            isReady = mode == BootstrapMode.CompleteIdentity || (
                state.bootstrap.cashierName.isNotBlank() &&
                    state.bootstrap.cashierPin.length == 6 &&
                    state.bootstrap.supervisorName.isNotBlank() &&
                    state.bootstrap.supervisorPin.length == 6
                )
        )
    )
    val completedCount = readinessItems.count { it.isReady }
    val stageTitle = if (mode == BootstrapMode.FullSetup) "Pengaturan awal toko" else "Lengkapi identitas usaha"
    val stageSubtitle = if (mode == BootstrapMode.FullSetup) {
        "Selesaikan identitas usaha, terminal utama, dan operator awal agar startup Cassy bisa lanjut ke login operator."
    } else {
        "Terminal sudah terdaftar. Lengkapi dulu identitas usaha agar struk, invoice dasar, dan login operator bisa dipakai."
    }

    Box(modifier = Modifier.fillMaxSize().padding(horizontal = 28.dp, vertical = 24.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().widthIn(max = 1460.dp).align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.width(278.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Surface(
                    tonalElevation = 1.dp,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(stageTitle, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Text(
                            stageSubtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f)
                        ) {
                            Text(
                                "$completedCount dari ${readinessItems.size} langkah selesai",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                StageSectionCard(title = "Yang wajib selesai") {
                    readinessItems.forEach { item ->
                        BootstrapReadinessRow(item)
                    }
                }

                Surface(
                    tonalElevation = 0.dp,
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Setelah disimpan", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text("1. Splash akan lanjut ke login operator lokal.", style = MaterialTheme.typography.bodySmall)
                        Text("2. Data usaha ini dipakai di struk dan invoice dasar.", style = MaterialTheme.typography.bodySmall)
                        Text("3. Kasir bisa membuka hari bisnis setelah login.", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Surface(
                modifier = Modifier.weight(1f),
                tonalElevation = 1.dp,
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(22.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("Isi data terminal utama", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text(
                                "Fokus dulu ke identitas usaha, lalu operator awal. Hari bisnis, shift, dan perangkat diproses sesudah login.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            ShortcutHintBar(
                                hints = listOf("Tab pindah field", "PIN 6 digit", "Enter simpan"),
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        StageSectionCard(
                            title = "Identitas usaha dan terminal",
                            modifier = Modifier.weight(1f)
                        ) {
                            SemanticTextField(
                                label = "Nama usaha",
                                value = state.storeProfile.businessName,
                                onValueChange = {
                                    onStoreProfileFieldChanged(StoreProfileUiField.BusinessName, it)
                                    if (mode == BootstrapMode.FullSetup) onFieldChanged(BootstrapField.StoreName, it)
                                },
                                placeholder = "Contoh: Cassy Coffee \u0026 Roastery",
                                leadingIcon = Icons.Default.Storefront,
                                helperText = "Wajib diisi sebagai identitas utama toko.",
                                errorText = state.storeProfile.fieldErrors[StoreProfileUiField.BusinessName]
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                SemanticTextField(
                                    label = "Telepon usaha",
                                    value = state.storeProfile.phoneNumber,
                                    onValueChange = { onStoreProfileFieldChanged(StoreProfileUiField.PhoneNumber, it) },
                                    placeholder = "81234567890",
                                    leadingIcon = Icons.Default.Phone,
                                    modifier = Modifier.weight(1f),
                                    helperText = "Muncul di struk belanja.",
                                    errorText = state.storeProfile.fieldErrors[StoreProfileUiField.PhoneNumber]
                                )
                                SemanticTextField(
                                    label = "Kode negara",
                                    value = state.storeProfile.phoneCountryCode,
                                    onValueChange = { onStoreProfileFieldChanged(StoreProfileUiField.PhoneCountryCode, it) },
                                    placeholder = "62",
                                    modifier = Modifier.width(100.dp),
                                    errorText = state.storeProfile.fieldErrors[StoreProfileUiField.PhoneCountryCode]
                                )
                            }

                            SemanticTextField(
                                label = "Alamat lengkap",
                                value = state.storeProfile.streetAddress,
                                onValueChange = { onStoreProfileFieldChanged(StoreProfileUiField.StreetAddress, it) },
                                placeholder = "Jl. Sudirman No. 123, Jakarta Selatan",
                                leadingIcon = Icons.Default.LocationOn,
                                helperText = "Wajib diisi untuk keperluan invoice dan pajak.",
                                errorText = state.storeProfile.fieldErrors[StoreProfileUiField.StreetAddress]
                            )

                            SemanticTextField(
                                label = "Catatan bawah struk",
                                value = state.storeProfile.receiptNote,
                                onValueChange = { onStoreProfileFieldChanged(StoreProfileUiField.ReceiptNote, it) },
                                placeholder = "Terima kasih atas kunjungannya!",
                                leadingIcon = Icons.AutoMirrored.Filled.ReceiptLong,
                                helperText = "Pesan singkat di akhir struk.",
                                errorText = state.storeProfile.fieldErrors[StoreProfileUiField.ReceiptNote]
                            )

                            if (mode == BootstrapMode.FullSetup) {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                SemanticTextField(
                                    label = "Nama terminal ini",
                                    value = state.bootstrap.terminalName,
                                    onValueChange = { onFieldChanged(BootstrapField.TerminalName, it) },
                                    placeholder = "Contoh: Kasir Utama, Terminal A",
                                    leadingIcon = Icons.Default.Computer,
                                    helperText = "Nama pengenal unik untuk perangkat ini.",
                                    errorText = state.bootstrap.fieldErrors[BootstrapField.TerminalName]
                                )
                            }
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            StageSectionCard(title = "Logo usaha (Opsional)") {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        modifier = Modifier.size(80.dp),
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            if (state.storeProfile.logoPath != null) {
                                                Image(
                                                    painter = painterResource(CASSY_BRAND_ICON_RESOURCE), // Mock for now
                                                    contentDescription = null,
                                                    modifier = Modifier.fillMaxSize().padding(12.dp)
                                                )
                                            } else {
                                                Icon(
                                                    imageVector = Icons.Default.AddPhotoAlternate,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                                )
                                            }
                                        }
                                    }

                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Button(
                                                onClick = onSelectStoreLogo,
                                                contentPadding = PaddingValues(horizontal = 12.dp),
                                                modifier = Modifier.height(36.dp)
                                            ) {
                                                Text("Pilih Logo", style = MaterialTheme.typography.labelLarge)
                                            }
                                            if (state.storeProfile.logoPath != null) {
                                                OutlinedButton(
                                                    onClick = onClearStoreLogo,
                                                    contentPadding = PaddingValues(horizontal = 12.dp),
                                                    modifier = Modifier.height(36.dp)
                                                ) {
                                                    Text("Hapus", style = MaterialTheme.typography.labelLarge)
                                                }
                                            }
                                        }
                                        Text(
                                            "Format PNG/JPG, maks 2MB. Logo akan muncul di struk digital.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            if (mode == BootstrapMode.FullSetup) {
                                StageSectionCard(title = "Operator awal") {
                                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                        BootstrapOperatorRow(
                                            role = "Kasir",
                                            nameValue = state.bootstrap.cashierName,
                                            pinValue = state.bootstrap.cashierPin,
                                            onNameChange = { onFieldChanged(BootstrapField.CashierName, it) },
                                            onPinChange = { onFieldChanged(BootstrapField.CashierPin, it) },
                                            errorText = state.bootstrap.fieldErrors[BootstrapField.CashierName] ?: state.bootstrap.fieldErrors[BootstrapField.CashierPin]
                                        )
                                        HorizontalDivider()
                                        BootstrapOperatorRow(
                                            role = "Supervisor",
                                            nameValue = state.bootstrap.supervisorName,
                                            pinValue = state.bootstrap.supervisorPin,
                                            onNameChange = { onFieldChanged(BootstrapField.SupervisorName, it) },
                                            onPinChange = { onFieldChanged(BootstrapField.SupervisorPin, it) },
                                            errorText = state.bootstrap.fieldErrors[BootstrapField.SupervisorName] ?: state.bootstrap.fieldErrors[BootstrapField.SupervisorPin]
                                        )
                                    }
                                }
                            }

                            Surface(
                                tonalElevation = 0.dp,
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text("Catatan operasional", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    Text(
                                        "Logo usaha tetap opsional. Alamat terstruktur, telepon, catatan struk, dan operator awal wajib lengkap karena langsung dipakai untuk gate startup dan template struk.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Button(
                                onClick = onBootstrap,
                                enabled = !state.isBusy,
                                modifier = Modifier.fillMaxWidth().height(52.dp)
                            ) {
                                Text(if (state.isBusy) "Menyimpan..." else "Simpan dan lanjut")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LoginStage(
    state: DesktopAppState,
    onSelectOperator: (String) -> Unit,
    onPinChanged: (String) -> Unit,
    onLogin: () -> Unit
) {
    val selectedOperator = state.login.operators.firstOrNull { it.id == state.login.selectedOperatorId }
    val focusRequester = remember { FocusRequester() }

    // Auto-login when 6 digits are entered
    LaunchedEffect(state.login.pin) {
        if (state.login.pin.length == 6 && selectedOperator != null && !state.isBusy) {
            onLogin()
        }
    }

    LaunchedEffect(state.login.selectedOperatorId) {
        if (state.login.selectedOperatorId != null) {
            focusRequester.requestFocus()
        }
    }

    fun appendDigit(digit: Int) {
        if (state.isBusy) return
        onPinChanged((state.login.pin + digit.toString()).take(6))
    }

    fun backspaceDigit() {
        if (state.isBusy) return
        onPinChanged(state.login.pin.dropLast(1))
    }

    fun clearPin() {
        if (state.isBusy) return
        onPinChanged("")
    }

    Box(modifier = Modifier.fillMaxSize().padding(horizontal = 40.dp, vertical = 28.dp), contentAlignment = Alignment.Center) {
        Surface(
            modifier = Modifier.fillMaxHeight().widthIn(max = 1160.dp),
            tonalElevation = 1.dp,
            shape = RoundedCornerShape(18.dp)
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                // Left Panel: Operator Selection
                Surface(
                    modifier = Modifier.widthIn(min = 360.dp, max = 420.dp).fillMaxHeight(),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.18f)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 28.dp, vertical = 30.dp),
                        verticalArrangement = Arrangement.spacedBy(22.dp)
                    ) {
                        Text("Cassy", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
                        Text(
                            "Pilih operator untuk memulai",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Pilih operator aktif di terminal ini, lalu masukkan PIN lokal 6 digit.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        LazyColumn(
                            modifier = Modifier.weight(1f, fill = false).fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        )
                        {
                            items(state.login.operators, key = { it.id }) { option ->
                                val selected = state.login.selectedOperatorId == option.id
                                LoginOperatorCard(
                                    option = option,
                                    selected = selected,
                                    onClick = { onSelectOperator(option.id) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            LoginStatusPill(
                                text = "Terminal ${state.shell.terminalName ?: "-"}",
                                tone = UiTone.Info
                            )
                            LoginStatusPill(
                                text = "Auth lokal aktif",
                                tone = UiTone.Success
                            )
                        }
                    }
                }

                // Right Panel: PIN Input
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(horizontal = 36.dp, vertical = 32.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Column(
                        modifier = Modifier
                            .widthIn(max = 420.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { focusRequester.requestFocus() },
                        verticalArrangement = Arrangement.spacedBy(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.42f),
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                modifier = Modifier.padding(18.dp).size(28.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        Text("Masukkan PIN", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
                        Text(
                            selectedOperator?.let { "Gunakan 6 digit PIN untuk ${it.displayName}." }
                                ?: "Pilih operator terlebih dahulu, lalu masukkan PIN 6 digit.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )

                        BasicTextField(
                            value = state.login.pin,
                            onValueChange = { onPinChanged(it.filter(Char::isDigit).take(6)) },
                            modifier = Modifier
                                .size(1.dp)
                                .focusRequester(focusRequester)
                                .graphicsLayer { alpha = 0f }
                        )

                        LoginPinSlotRow(
                            pin = state.login.pin,
                            modifier = Modifier.fillMaxWidth().widthIn(max = 420.dp)
                        )

                        state.login.feedback?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center
                            )
                        }

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            LoginPinKeypadRow(
                                items = listOf(
                                    LoginPadAction.Digit(1),
                                    LoginPadAction.Digit(2),
                                    LoginPadAction.Digit(3)
                                ),
                                onDigit = ::appendDigit,
                                onBackspace = ::backspaceDigit,
                                onClear = ::clearPin,
                                onSubmit = onLogin,
                                isBusy = state.isBusy,
                                submitEnabled = state.login.pin.length == 6 && selectedOperator != null
                            )
                            LoginPinKeypadRow(
                                items = listOf(
                                    LoginPadAction.Digit(4),
                                    LoginPadAction.Digit(5),
                                    LoginPadAction.Digit(6)
                                ),
                                onDigit = ::appendDigit,
                                onBackspace = ::backspaceDigit,
                                onClear = ::clearPin,
                                onSubmit = onLogin,
                                isBusy = state.isBusy,
                                submitEnabled = state.login.pin.length == 6 && selectedOperator != null
                            )
                            LoginPinKeypadRow(
                                items = listOf(
                                    LoginPadAction.Digit(7),
                                    LoginPadAction.Digit(8),
                                    LoginPadAction.Digit(9)
                                ),
                                onDigit = ::appendDigit,
                                onBackspace = ::backspaceDigit,
                                onClear = ::clearPin,
                                onSubmit = onLogin,
                                isBusy = state.isBusy,
                                submitEnabled = state.login.pin.length == 6 && selectedOperator != null
                            )
                            LoginPinKeypadRow(
                                items = listOf(
                                    LoginPadAction.Backspace,
                                    LoginPadAction.Digit(0),
                                    LoginPadAction.Submit
                                ),
                                onDigit = ::appendDigit,
                                onBackspace = ::backspaceDigit,
                                onClear = ::clearPin,
                                onSubmit = onLogin,
                                isBusy = state.isBusy,
                                submitEnabled = state.login.pin.length == 6 && selectedOperator != null
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            LoginStatusPill(
                                text = "PIN lokal 6 digit",
                                tone = UiTone.Info
                            )
                            LoginStatusPill(
                                text = if (state.isBusy) "Memproses..." else "Siap diverifikasi",
                                tone = if (state.isBusy) UiTone.Warning else UiTone.Success
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OpenDayStage(
    state: DesktopAppState,
    onOpenDay: () -> Unit,
    onLogout: () -> Unit
) {
    CenterPanel(
        title = "Hari Bisnis Belum Dibuka",
        subtitle = state.operations.dashboard.headline,
        action = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OperationalDashboardCard(state.operations.dashboard)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (state.operations.canOpenDay) {
                        Button(onClick = onOpenDay, enabled = !state.isBusy, modifier = Modifier.height(48.dp)) {
                            Text(if (state.isBusy) "Memproses..." else "Buka Hari Bisnis")
                        }
                    } else {
                        OutlinedButton(onClick = onLogout, modifier = Modifier.height(48.dp)) { Text("Ganti Operator") }
                    }
                }
            }
        }
    )
}

@Composable
fun StartShiftStage(
    state: DesktopAppState,
    onOpeningCashChanged: (String) -> Unit,
    onOpeningCashReasonChanged: (String) -> Unit,
    onShortcutSelected: (String) -> Unit,
    onStartShift: () -> Unit
) {
    CenterPanel(
        title = "Buka Kasir",
        subtitle = state.operations.dashboard.headline,
        contentWidth = 640.dp,
        action = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                ShortcutHintBar(hints = listOf("Shortcut Nominal", "Enter Buka Kasir", "Review Bloker"))
                OperationalDashboardCard(state.operations.dashboard)
                CassyCurrencyInput(
                    label = "Modal Awal Tunai",
                    value = state.operations.openingCashInput,
                    onValueChange = onOpeningCashChanged,
                    helperText = "Isi saldo laci kas aktual sebelum transaksi pertama.",
                    onImeAction = onStartShift
                )
                SemanticTextField(
                    label = "Catatan Operasional",
                    value = state.operations.openingCashReason,
                    onValueChange = onOpeningCashReasonChanged,
                    helperText = "Wajib diisi bila opening cash di luar kebijakan normal.",
                    placeholder = "Contoh: butuh pecahan untuk jam sibuk",
                    leadingIcon = Icons.Default.EditNote,
                    imeAction = androidx.compose.ui.text.input.ImeAction.Done,
                    onImeAction = onStartShift
                )
                state.operations.blockingMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                Button(onClick = onStartShift, enabled = !state.isBusy, modifier = Modifier.fillMaxWidth().height(48.dp)) {
                    Text(if (state.isBusy) "Memulai..." else "Buka Kasir")
                }
            }
        }
    )
}

@Composable
fun OperationalDashboardCard(
    snapshot: OperationalControlSnapshot,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Ringkasan Operasional", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = when (snapshot.decisions.firstOrNull()?.status ?: OperationStatus.UNAVAILABLE) {
                        OperationStatus.READY -> MaterialTheme.colorScheme.primaryContainer
                        OperationStatus.BLOCKED -> MaterialTheme.colorScheme.errorContainer
                        OperationStatus.REQUIRES_APPROVAL -> MaterialTheme.colorScheme.secondaryContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                ) {
                    Text(
                        text = snapshot.decisions.firstOrNull()?.status?.name ?: "UNKNOWN",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                snapshot.decisions.forEach { decision ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(decision.title, style = MaterialTheme.typography.bodySmall)
                        Text(decision.status.name, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

private sealed interface LoginPadAction {
    data class Digit(val value: Int) : LoginPadAction
    data object Backspace : LoginPadAction
    data object Submit : LoginPadAction
}

@Composable
private fun LoginPinSlotRow(
    pin: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(6) { index ->
            val digit = pin.getOrNull(index)
            Surface(
                modifier = Modifier.weight(1f).aspectRatio(1f),
                shape = RoundedCornerShape(12.dp),
                color = if (digit != null) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.24f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f),
                border = BorderStroke(1.dp, if (digit != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (digit != null) {
                        Surface(
                            modifier = Modifier.size(12.dp),
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.primary
                        ) {}
                    }
                }
            }
        }
    }
}

@Composable
private fun LoginPinKeypadRow(
    items: List<LoginPadAction>,
    onDigit: (Int) -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
    onSubmit: () -> Unit,
    isBusy: Boolean,
    submitEnabled: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items.forEach { action ->
            when (action) {
                is LoginPadAction.Digit -> LoginPadButton(
                    label = action.value.toString(),
                    onClick = { onDigit(action.value) },
                    enabled = !isBusy,
                    modifier = Modifier.weight(1f)
                )
                LoginPadAction.Backspace -> LoginPadButton(
                    label = "Hapus",
                    icon = Icons.AutoMirrored.Filled.Backspace,
                    onClick = {
                        if (submitEnabled) {
                            onBackspace()
                        } else {
                            onClear()
                        }
                    },
                    enabled = !isBusy,
                    modifier = Modifier.weight(1f),
                    tone = UiTone.Warning
                )
                LoginPadAction.Submit -> LoginPadButton(
                    label = if (isBusy) "Proses" else "Masuk",
                    icon = Icons.AutoMirrored.Filled.ArrowForward,
                    onClick = onSubmit,
                    enabled = !isBusy && submitEnabled,
                    modifier = Modifier.weight(1f),
                    tone = if (submitEnabled) UiTone.Success else UiTone.Info
                )
            }
        }
    }
}

@Composable
private fun LoginPadButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    tone: UiTone = UiTone.Info
) {
    FilledTonalButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(74.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = if (tone == UiTone.Success) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.42f),
            contentColor = if (tone == UiTone.Success) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
        )
    ) {
        if (icon != null) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
                Text(label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        } else {
            Text(label, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun LoginStatusPill(
    text: String,
    tone: UiTone
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = when (tone) {
            UiTone.Info -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
            UiTone.Success -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f)
            UiTone.Warning -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.45f)
            UiTone.Danger -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.45f)
        }
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = when (tone) {
                UiTone.Info -> MaterialTheme.colorScheme.primary
                UiTone.Success -> MaterialTheme.colorScheme.onSecondaryContainer
                UiTone.Warning -> MaterialTheme.colorScheme.error
                UiTone.Danger -> MaterialTheme.colorScheme.error
            },
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun LoginOperatorCard(
    option: OperatorOption,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.28f) else MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        tonalElevation = if (selected) 2.dp else 0.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(52.dp),
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.42f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = option.displayName.take(1),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(option.displayName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(option.roleLabel, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (selected) {
                    Text("Operator terpilih untuk login", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                }
            }

            if (selected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun SaleHistoryRow(
    entry: SaleHistoryEntry,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f) else Color.Transparent,
        border = if (selected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)) else null
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(8.dp),
                color = if (entry.voidedAtEpochMs != null) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (entry.voidedAtEpochMs != null) Icons.Default.Block else Icons.AutoMirrored.Filled.ReceiptLong,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = if (entry.voidedAtEpochMs != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(entry.localNumber, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(
                    formatInstant(Instant.fromEpochMilliseconds(entry.finalizedAtEpochMs)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                "Rp ${entry.finalAmount.toInt()}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.ExtraBold,
                color = if (entry.voidedAtEpochMs != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun SaleDetailPanel(
    entry: SaleHistoryEntry,
    isBusy: Boolean,
    onPrint: () -> Unit,
    onVoid: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(18.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Detail Transaksi", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            if (entry.voidedAtEpochMs != null) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.errorContainer
                ) {
                    Text(
                        "VOID",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        Surface(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            shape = RoundedCornerShape(10.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp).verticalScroll(rememberScrollState())) {
                DetailItemRow("Nomor Struk", entry.localNumber)
                DetailItemRow("Waktu", formatInstant(Instant.fromEpochMilliseconds(entry.finalizedAtEpochMs)))
                DetailItemRow("Terminal", entry.terminalId)
                DetailItemRow("Metode Bayar", entry.paymentMethod)
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                    Text("Rp ${entry.finalAmount.toInt()}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onPrint,
                enabled = !isBusy,
                modifier = Modifier.weight(1f).height(44.dp)
            ) {
                Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cetak Ulang")
            }
            if (entry.voidedAtEpochMs == null) {
                Button(
                    onClick = onVoid,
                    enabled = !isBusy,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.weight(1f).height(44.dp)
                ) {
                    Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Void")
                }
            }
        }
    }
}

@Composable
private fun DetailItemRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun CenterPanel(
    title: String,
    subtitle: String,
    action: @Composable () -> Unit,
    contentWidth: androidx.compose.ui.unit.Dp = 480.dp
) {
    Box(modifier = Modifier.fillMaxSize().padding(28.dp), contentAlignment = Alignment.Center) {
        Surface(
            modifier = Modifier.width(contentWidth),
            shape = RoundedCornerShape(18.dp),
            tonalElevation = 1.dp
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 28.dp, vertical = 32.dp),
                verticalArrangement = Arrangement.spacedBy(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center)
                    Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                }
                action()
            }
        }
    }
}

@Composable
private fun StageSectionCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier,
        tonalElevation = 1.dp,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            content()
        }
    }
}

private data class BootstrapReadinessItem(
    val title: String,
    val detail: String,
    val isReady: Boolean
)

@Composable
private fun BootstrapReadinessRow(item: BootstrapReadinessItem) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = if (item.isReady) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (item.isReady) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(18.dp).padding(top = 2.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = if (item.isReady) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(text = item.detail, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun BootstrapOperatorRow(
    role: String,
    nameValue: String,
    pinValue: String,
    onNameChange: (String) -> Unit,
    onPinChange: (String) -> Unit,
    errorText: String? = null
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(role, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SemanticTextField(
                label = "Nama $role",
                value = nameValue,
                onValueChange = onNameChange,
                placeholder = "Contoh: Aziz",
                modifier = Modifier.weight(1f),
                errorText = errorText
            )
            SemanticTextField(
                label = "PIN 6 Digit",
                value = pinValue,
                onValueChange = { if (it.length <= 6) onPinChange(it) },
                placeholder = "123456",
                modifier = Modifier.width(130.dp),
                errorText = errorText
            )
        }
    }
}

private fun formatInstant(instant: Instant): String {
    // Basic mock formatter for now
    return instant.toString().replace("T", " ").take(16)
}
