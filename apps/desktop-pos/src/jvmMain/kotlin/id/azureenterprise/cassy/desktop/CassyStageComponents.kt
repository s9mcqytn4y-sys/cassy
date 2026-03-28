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
            isReady = state.storeProfile.fieldErrors.isEmpty() &&
                state.storeProfile.streetAddress.isNotBlank() &&
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
    val previewPhone = listOf(state.storeProfile.phoneCountryCode, state.storeProfile.phoneNumber)
        .joinToString(" ") { it.trim() }
        .trim()
        .ifBlank { "Belum diisi" }

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
                                helperText = "Nama ini tampil di aplikasi, struk, dan invoice.",
                                errorText = state.storeProfile.visibleError(StoreProfileUiField.BusinessName),
                                placeholder = "Contoh: Toko Berkah Jaya",
                                leadingIcon = Icons.Default.Storefront
                            )
                            if (mode == BootstrapMode.FullSetup) {
                                SemanticTextField(
                                    label = "Nama terminal",
                                    value = state.bootstrap.terminalName,
                                    onValueChange = { onFieldChanged(BootstrapField.TerminalName, it) },
                                    helperText = "Gunakan nama perangkat yang mudah dikenali, misalnya Kasir-01.",
                                    errorText = state.bootstrap.visibleError(BootstrapField.TerminalName),
                                    placeholder = "Contoh: Kasir-01",
                                    leadingIcon = Icons.Default.PointOfSale
                                )
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        "Nama terminal",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 16.dp),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.PointOfSale,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                text = state.shell.terminalName.orEmpty().ifBlank { "Terminal utama" },
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                    Text(
                                        "Terminal ini sudah terdaftar dan tidak perlu diubah di tahap ini.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            SemanticTextField(
                                label = "Alamat jalan",
                                value = state.storeProfile.streetAddress,
                                onValueChange = { onStoreProfileFieldChanged(StoreProfileUiField.StreetAddress, it) },
                                helperText = "Alamat utama yang tampil di struk.",
                                errorText = state.storeProfile.visibleError(StoreProfileUiField.StreetAddress),
                                placeholder = "Contoh: Jl. Jayagiri No. 10",
                                leadingIcon = Icons.Default.Home,
                                singleLine = false
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                                SemanticTextField(
                                    label = "RT / RW",
                                    value = state.storeProfile.neighborhood,
                                    onValueChange = { onStoreProfileFieldChanged(StoreProfileUiField.Neighborhood, it) },
                                    helperText = "Wajib diisi.",
                                    errorText = state.storeProfile.visibleError(StoreProfileUiField.Neighborhood),
                                    placeholder = "02/05",
                                    modifier = Modifier.weight(0.6f)
                                )
                                SemanticTextField(
                                    label = "Kelurahan / Desa",
                                    value = state.storeProfile.village,
                                    onValueChange = { onStoreProfileFieldChanged(StoreProfileUiField.Village, it) },
                                    helperText = "Wajib diisi.",
                                    errorText = state.storeProfile.visibleError(StoreProfileUiField.Village),
                                    placeholder = "Jayagiri",
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                                SemanticTextField(
                                    label = "Kecamatan",
                                    value = state.storeProfile.district,
                                    onValueChange = { onStoreProfileFieldChanged(StoreProfileUiField.District, it) },
                                    errorText = state.storeProfile.visibleError(StoreProfileUiField.District),
                                    placeholder = "Lembang",
                                    modifier = Modifier.weight(1f)
                                )
                                SemanticTextField(
                                    label = "Kota / Kabupaten",
                                    value = state.storeProfile.city,
                                    onValueChange = { onStoreProfileFieldChanged(StoreProfileUiField.City, it) },
                                    errorText = state.storeProfile.visibleError(StoreProfileUiField.City),
                                    placeholder = "Bandung Barat",
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                                SemanticTextField(
                                    label = "Provinsi",
                                    value = state.storeProfile.province,
                                    onValueChange = { onStoreProfileFieldChanged(StoreProfileUiField.Province, it) },
                                    errorText = state.storeProfile.visibleError(StoreProfileUiField.Province),
                                    placeholder = "Jawa Barat",
                                    modifier = Modifier.weight(1f)
                                )
                                SemanticTextField(
                                    label = "Kode pos",
                                    value = state.storeProfile.postalCode,
                                    onValueChange = { onStoreProfileFieldChanged(StoreProfileUiField.PostalCode, it) },
                                    errorText = state.storeProfile.visibleError(StoreProfileUiField.PostalCode),
                                    placeholder = "40391",
                                    modifier = Modifier.width(128.dp)
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                                SemanticTextField(
                                    label = "Kode negara",
                                    value = state.storeProfile.phoneCountryCode,
                                    onValueChange = { onStoreProfileFieldChanged(StoreProfileUiField.PhoneCountryCode, it) },
                                    errorText = state.storeProfile.visibleError(StoreProfileUiField.PhoneCountryCode),
                                    placeholder = "+62",
                                    modifier = Modifier.width(120.dp)
                                )
                                SemanticTextField(
                                    label = "Nomor telepon",
                                    value = state.storeProfile.phoneNumber,
                                    onValueChange = { onStoreProfileFieldChanged(StoreProfileUiField.PhoneNumber, it) },
                                    helperText = "Dipakai untuk kontak usaha di struk.",
                                    errorText = state.storeProfile.visibleError(StoreProfileUiField.PhoneNumber),
                                    placeholder = "81234567890",
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                                SemanticTextField(
                                    label = "Email usaha",
                                    value = state.storeProfile.businessEmail,
                                    onValueChange = { onStoreProfileFieldChanged(StoreProfileUiField.BusinessEmail, it) },
                                    helperText = "Opsional. Dipakai untuk invoice dasar.",
                                    errorText = state.storeProfile.visibleError(StoreProfileUiField.BusinessEmail),
                                    placeholder = "contoh@usaha.com",
                                    modifier = Modifier.weight(1f)
                                )
                                SemanticTextField(
                                    label = "NIB / NPWP / ID legal",
                                    value = state.storeProfile.legalId,
                                    onValueChange = { onStoreProfileFieldChanged(StoreProfileUiField.LegalId, it) },
                                    helperText = "Opsional, tetapi disarankan.",
                                    errorText = state.storeProfile.visibleError(StoreProfileUiField.LegalId),
                                    placeholder = "Opsional",
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            SemanticTextField(
                                label = "Catatan struk",
                                value = state.storeProfile.receiptNote,
                                onValueChange = { onStoreProfileFieldChanged(StoreProfileUiField.ReceiptNote, it) },
                                helperText = "Wajib diisi. Dipakai sebagai footer struk.",
                                errorText = state.storeProfile.visibleError(StoreProfileUiField.ReceiptNote),
                                placeholder = "Contoh: Terima kasih sudah berbelanja",
                                singleLine = false,
                                leadingIcon = Icons.Default.ReceiptLong
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                OutlinedButton(onClick = onSelectStoreLogo, modifier = Modifier.weight(1f)) { Text("Pilih logo") }
                                OutlinedButton(
                                    onClick = onClearStoreLogo,
                                    enabled = state.storeProfile.logoPath != null,
                                    modifier = Modifier.weight(1f)
                                ) { Text("Hapus logo") }
                            }
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text("Tampilan struk", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                                    BootstrapReceiptToggleRow(
                                        label = "Tampilkan logo",
                                        checked = state.storeProfile.showLogoOnReceipt,
                                        onCheckedChange = { onStoreProfileToggleChanged(StoreProfileToggleField.ShowLogoOnReceipt, it) }
                                    )
                                    BootstrapReceiptToggleRow(
                                        label = "Tampilkan alamat",
                                        checked = state.storeProfile.showAddressOnReceipt,
                                        onCheckedChange = { onStoreProfileToggleChanged(StoreProfileToggleField.ShowAddressOnReceipt, it) }
                                    )
                                    BootstrapReceiptToggleRow(
                                        label = "Tampilkan telepon",
                                        checked = state.storeProfile.showPhoneOnReceipt,
                                        onCheckedChange = { onStoreProfileToggleChanged(StoreProfileToggleField.ShowPhoneOnReceipt, it) }
                                    )
                                }
                            }
                        }

                        StageSectionCard(
                            title = "Preview identitas",
                            modifier = Modifier.width(296.dp)
                        ) {
                            ManagedImagePreview(
                                imagePath = state.storeProfile.logoPath,
                                fallbackLabel = state.storeProfile.businessName.ifBlank { "Usaha" },
                                contentDescription = "Preview logo usaha",
                                modifier = Modifier.fillMaxWidth().height(132.dp)
                            )
                            BootstrapPreviewRow("Nama toko", state.storeProfile.businessName.ifBlank { "Belum diisi" })
                            BootstrapPreviewRow(
                                "Nama terminal",
                                if (mode == BootstrapMode.FullSetup) {
                                    state.bootstrap.terminalName.ifBlank { "Belum diisi" }
                                } else {
                                    state.shell.terminalName.orEmpty().ifBlank { "Belum diisi" }
                                }
                            )
                            BootstrapPreviewRow("Alamat", state.storeProfile.address.ifBlank { "Belum diisi" })
                            BootstrapPreviewRow("Telepon", previewPhone)
                            BootstrapPreviewRow(
                                "Nama kasir",
                                if (mode == BootstrapMode.FullSetup) state.bootstrap.cashierName.ifBlank { "Belum diisi" } else "Sudah tersimpan"
                            )
                            BootstrapPreviewRow(
                                "Nama supervisor",
                                if (mode == BootstrapMode.FullSetup) state.bootstrap.supervisorName.ifBlank { "Belum diisi" } else "Sudah tersimpan"
                            )
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                            Text("Preview struk", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                            Text(
                                state.storeProfile.businessName.ifBlank { "Nama usaha akan tampil di sini" },
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (state.storeProfile.showAddressOnReceipt) {
                                Text(state.storeProfile.address.ifBlank { "Alamat usaha tampil di sini" }, style = MaterialTheme.typography.bodySmall)
                            }
                            if (state.storeProfile.showPhoneOnReceipt) {
                                Text(previewPhone, style = MaterialTheme.typography.bodySmall)
                            }
                            if (state.storeProfile.businessEmail.isNotBlank()) {
                                Text(state.storeProfile.businessEmail, style = MaterialTheme.typography.bodySmall)
                            }
                            if (state.storeProfile.legalId.isNotBlank()) {
                                Text("ID legal: ${state.storeProfile.legalId}", style = MaterialTheme.typography.bodySmall)
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                            Text(
                                state.storeProfile.receiptNote.ifBlank { "Catatan struk tampil di sini." },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (mode == BootstrapMode.FullSetup) {
                        StageSectionCard(title = "Operator awal") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            BootstrapRoleCard(
                                title = "Kasir",
                                detail = "Peran utama untuk transaksi, buka hari bisnis, buka shift, dan kontrol kas harian.",
                                avatarPath = state.bootstrap.cashierAvatarPath,
                                onSelectAvatar = { onSelectAvatar(BootstrapField.CashierAvatar) },
                                onClearAvatar = { onClearAvatar(BootstrapField.CashierAvatar) },
                                modifier = Modifier.weight(1f)
                            ) {
                                SemanticTextField(
                                    label = "Nama Kasir",
                                    value = state.bootstrap.cashierName,
                                    onValueChange = { onFieldChanged(BootstrapField.CashierName, it) },
                                    helperText = "Nama operator yang dipakai untuk transaksi harian.",
                                    errorText = state.bootstrap.visibleError(BootstrapField.CashierName),
                                    placeholder = "Contoh: Rani",
                                    leadingIcon = Icons.Default.Person
                                )
                                SemanticPinField(
                                    label = "PIN Kasir",
                                    value = state.bootstrap.cashierPin,
                                    onValueChange = { onFieldChanged(BootstrapField.CashierPin, it) },
                                    helperText = "Wajib 6 digit numerik.",
                                    errorText = state.bootstrap.visibleError(BootstrapField.CashierPin)
                                )
                            }

                            BootstrapRoleCard(
                                title = "Supervisor",
                                detail = "Memantau hasil, memberi approval penting, dan menangani pemulihan bila ada masalah.",
                                avatarPath = state.bootstrap.supervisorAvatarPath,
                                onSelectAvatar = { onSelectAvatar(BootstrapField.SupervisorAvatar) },
                                onClearAvatar = { onClearAvatar(BootstrapField.SupervisorAvatar) },
                                modifier = Modifier.weight(1f)
                            ) {
                                SemanticTextField(
                                    label = "Nama Supervisor",
                                    value = state.bootstrap.supervisorName,
                                    onValueChange = { onFieldChanged(BootstrapField.SupervisorName, it) },
                                    helperText = "Supervisor dipakai untuk approval penting dan pemulihan operasional.",
                                    errorText = state.bootstrap.visibleError(BootstrapField.SupervisorName),
                                    placeholder = "Contoh: Bayu",
                                    leadingIcon = Icons.Default.Badge
                                )
                                SemanticPinField(
                                    label = "PIN Supervisor",
                                    value = state.bootstrap.supervisorPin,
                                    onValueChange = { onFieldChanged(BootstrapField.SupervisorPin, it) },
                                    helperText = "Simpan hanya ke orang yang berwenang.",
                                    errorText = state.bootstrap.visibleError(BootstrapField.SupervisorPin),
                                    onImeAction = onBootstrap
                                )
                            }
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

@Composable
fun LoginStage(
    state: DesktopAppState,
    onSelectOperator: (String) -> Unit,
    onPinChanged: (String) -> Unit,
    onLogin: () -> Unit
) {
    val selectedOperator = state.login.operators.firstOrNull { it.id == state.login.selectedOperatorId }
    val focusRequester = remember { FocusRequester() }
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

    Box(modifier = Modifier.fillMaxSize().padding(horizontal = 40.dp, vertical = 28.dp)) {
        Surface(
            modifier = Modifier.fillMaxWidth().widthIn(max = 1160.dp).align(Alignment.TopCenter),
            tonalElevation = 1.dp,
            shape = RoundedCornerShape(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().heightIn(min = 720.dp)
            ) {
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

                Column(
                    modifier = Modifier.weight(1f).fillMaxHeight().padding(horizontal = 36.dp, vertical = 32.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        tonalElevation = 0.dp,
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { focusRequester.requestFocus() }
                                .padding(horizontal = 46.dp, vertical = 34.dp),
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
                                modifier = Modifier.widthIn(max = 420.dp),
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
                ShortcutNominalRow(
                    values = listOf("100000", "200000", "500000", "1000000"),
                    onShortcutSelected = onShortcutSelected
                )
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
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Ringkasan kesiapan", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(snapshot.headline, style = MaterialTheme.typography.bodyMedium)
            if (snapshot.pendingApprovalCount > 0) {
                Text(
                    text = "${snapshot.pendingApprovalCount} approval operasional menunggu keputusan.",
                    style = MaterialTheme.typography.bodySmall,
                    color = toneColor(UiTone.Warning)
                )
            }
            snapshot.salesHomeBlocker?.let {
                Text(
                    text = "Status kasir: $it",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                snapshot.decisions.forEach { decision ->
                    OperationDecisionRow(decision)
                }
            }
        }
    }
}

@Composable
fun CashControlDialog(
    state: OperationsState,
    onDismiss: () -> Unit,
    onTypeSelected: (CashMovementType) -> Unit,
    onAmountChanged: (String) -> Unit,
    onReasonCodeChanged: (String) -> Unit,
    onReasonDetailChanged: (String) -> Unit,
    onSubmit: () -> Unit,
    onApprove: (String) -> Unit,
    onDeny: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Kontrol Kas", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OperationalDashboardCard(state.dashboard)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CashMovementType.entries.forEach { type ->
                        FilterChip(
                            selected = state.cashMovementType == type,
                            onClick = { onTypeSelected(type) },
                            label = { Text(type.toUiLabel()) }
                        )
                    }
                }
                ShortcutNominalRow(
                    values = listOf("50000", "100000", "200000", "500000"),
                    onShortcutSelected = onAmountChanged
                )
                CassyCurrencyInput(
                    label = "Nominal",
                    value = state.cashMovementAmountInput,
                    onValueChange = onAmountChanged,
                    helperText = "Shift aktif wajib ada. Reason code harus valid."
                )
                ReasonOptionGroup(
                    title = "Pilih Alasan",
                    options = state.cashMovementReasonOptions,
                    selectedCode = state.cashMovementReasonCode,
                    onSelected = onReasonCodeChanged
                )
                FormField(
                    label = "Catatan",
                    value = state.cashMovementReasonDetail,
                    helperText = "Isi singkat konteks perpindahan kas."
                ) { onReasonDetailChanged(it) }
                if (state.pendingApprovals.isNotEmpty()) {
                    Text("Approval Menunggu", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    state.pendingApprovals.filter {
                        it.operationType == OperationType.CASH_IN ||
                            it.operationType == OperationType.CASH_OUT ||
                            it.operationType == OperationType.SAFE_DROP
                    }.forEach { approval ->
                        PendingApprovalRow(approval, onApprove, onDeny)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onSubmit) { Text("Simpan Kontrol Kas") }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Tutup") }
        }
    )
}

@Composable
fun CloseShiftWizardDialog(
    state: OperationsState,
    onDismiss: () -> Unit,
    onClosingCashChanged: (String) -> Unit,
    onReasonCodeChanged: (String) -> Unit,
    onReasonDetailChanged: (String) -> Unit,
    onSubmit: () -> Unit,
    onApprove: (String) -> Unit,
    onDeny: (String) -> Unit
) {
    val review = state.closeShiftReview
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Wizard Tutup Shift", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OperationalDashboardCard(state.dashboard)
                review?.let {
                    Surface(
                        tonalElevation = 1.dp,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Review Readiness", fontWeight = FontWeight.Bold)
                            Text(it.decision.message)
                            Text("Expected cash: Rp ${it.expectedCash.toInt()}")
                            Text("Cash sales: Rp ${it.cashSalesTotal.toInt()}")
                            Text("Cash in: Rp ${it.cashMovementTotals.cashInTotal.toInt()}")
                            Text("Cash out: Rp ${it.cashMovementTotals.cashOutTotal.toInt()}")
                            Text("Safe drop: Rp ${it.cashMovementTotals.safeDropTotal.toInt()}")
                            it.variance?.let { variance -> Text("Variance: Rp ${variance.toInt()}") }
                            if (it.pendingTransactions.isNotEmpty()) {
                                Text("Pending transaction:", fontWeight = FontWeight.Bold)
                                it.pendingTransactions.forEach { pending ->
                                    Text("${pending.localNumber} | Rp ${pending.amount.toInt()}")
                                }
                            }
                        }
                    }
                }
                CassyCurrencyInput(
                    label = "Closing Cash Aktual",
                    value = state.closingCashInput,
                    onValueChange = onClosingCashChanged,
                    helperText = "Hitung kas aktual dulu, baru review variance."
                )
                ReasonOptionGroup(
                    title = "Alasan Selisih",
                    options = state.closeShiftReasonOptions,
                    selectedCode = state.closeShiftReasonCode,
                    onSelected = onReasonCodeChanged
                )
                FormField(
                    label = "Catatan Selisih",
                    value = state.closeShiftReasonDetail,
                    helperText = "Wajib diisi bila variance perlu approval."
                ) { onReasonDetailChanged(it) }
                state.pendingApprovals.filter { it.operationType == OperationType.CLOSE_SHIFT }.forEach { approval ->
                    PendingApprovalRow(approval, onApprove, onDeny)
                }
            }
        },
        confirmButton = {
            Button(onClick = onSubmit) { Text("Eksekusi Tutup Shift") }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Kembali") }
        }
    )
}

@Composable
fun CloseDayReviewDialog(
    operations: OperationsState,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val closeDayDecision = operations.dashboard.decisions.firstOrNull { it.type == OperationType.CLOSE_BUSINESS_DAY }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Review Tutup Hari", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OperationalDashboardCard(operations.dashboard)
                closeDayDecision?.let {
                    Surface(
                        color = toneColor(
                            when (it.status) {
                                OperationStatus.READY -> UiTone.Success
                                OperationStatus.REQUIRES_APPROVAL -> UiTone.Warning
                                else -> UiTone.Danger
                            }
                        ).copy(alpha = 0.08f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(it.title, fontWeight = FontWeight.Bold)
                            Text(it.message)
                            Text("CTA: ${it.actionLabel ?: "Perbaiki blocker lalu review ulang"}")
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = closeDayDecision?.status == OperationStatus.READY
            ) { Text("Tutup Hari") }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Tinjau Lagi") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportingSummaryDialog(
    state: OperationsState,
    onDismiss: () -> Unit,
    onExport: () -> Unit,
    isBusy: Boolean = false
) {
    BasicAlertDialog(
        onDismissRequest = onDismiss,
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            tonalElevation = 4.dp,
            modifier = Modifier.widthIn(min = 920.dp, max = 1080.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Ringkasan Operasional", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Text(
                            "Snapshot lokal untuk owner/supervisor. Export menjaga truth terminal saat ini, bukan mirror backend.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    ShortcutHintBar(hints = listOf("F8 Buka", "Ctrl+E Export", "Esc Tutup"))
                }
                state.reportingSummary?.let { summary ->
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                        ReportingMetricTile(
                            title = "Penjualan Hari Ini",
                            primaryValue = "Rp ${summary.totalSales.toInt()}",
                            supporting = "${summary.transactionCount} transaksi | void ${summary.voidedSaleCount} / Rp ${summary.voidedSalesTotal.toInt()}",
                            modifier = Modifier.weight(1f)
                        )
                        ReportingMetricTile(
                            title = "Kontrol Operasional",
                            primaryValue = "${summary.openShiftCount}/${summary.shiftCount} shift aktif",
                            supporting = "${summary.pendingApprovalCount} approval pending | net cash Rp ${summary.netCashMovement.toInt()}",
                            modifier = Modifier.weight(1f)
                        )
                        ReportingMetricTile(
                            title = "Status Sync",
                            primaryValue = summary.syncStatus.toUiLabel(),
                            supporting = "pending ${summary.syncStatus.pendingCount} | failed ${summary.syncStatus.failedCount}",
                            modifier = Modifier.weight(1f),
                            tone = when (summary.syncStatus.level) {
                                SyncLevel.HEALTHY -> UiTone.Success
                                SyncLevel.PENDING, SyncLevel.DELAYED -> UiTone.Warning
                                SyncLevel.STALLED, SyncLevel.ERROR -> UiTone.Danger
                            }
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                        StageSectionCard(title = "Readback Sync", modifier = Modifier.weight(1f)) {
                            ReportingKeyValue("Level", summary.syncStatus.toUiLabel())
                            ReportingKeyValue("Pending Event", summary.syncStatus.pendingCount.toString())
                            ReportingKeyValue("Failed Event", summary.syncStatus.failedCount.toString())
                            ReportingKeyValue("Sync Sukses Terakhir", summary.syncStatus.lastSyncAt?.toUiTimestamp() ?: "-")
                            ReportingKeyValue("Pending Tertua", summary.syncStatus.oldestPendingAt?.toUiTimestamp() ?: "-")
                            summary.syncStatus.lastErrorMessage?.let {
                                Text(
                                    text = "Error terakhir: $it",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (summary.syncStatus.level == SyncLevel.ERROR) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        StageSectionCard(title = "Shift Relevan", modifier = Modifier.weight(1f)) {
                            val shift = state.reportingShiftSummary
                            if (shift == null) {
                                Text(
                                    "Belum ada shift yang relevan untuk snapshot ini. Daily summary tetap bisa diekspor.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                ReportingKeyValue("Shift", shift.shiftId)
                                ReportingKeyValue("Operator", shift.operatorName)
                                ReportingKeyValue("Expected Cash", "Rp ${shift.expectedCash.toInt()}")
                                ReportingKeyValue("Variance", shift.variance?.let { "Rp ${it.toInt()}" } ?: "-")
                                ReportingKeyValue("Pending Transaksi", shift.pendingTransactionCount.toString())
                                ReportingKeyValue("Status", shift.status)
                            }
                        }
                    }

                        StageSectionCard(title = "Aturan Export & Output") {
                        Text(state.reportingExportRuleNote, style = MaterialTheme.typography.bodySmall)
                        ReportingKeyValue("Void Tercatat", "${summary.voidedSaleCount} transaksi")
                        ReportingKeyValue("Lokasi Terakhir", state.reportingExportPath ?: "-")
                        ReportingKeyValue("Diekspor Terakhir", state.reportingExportedAt?.toUiTimestamp() ?: "-")
                        Text(
                            "Bundle export berisi daily-summary.csv, shift-summary.csv, operational-issues.csv, dan README.html.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Text("Masalah & Tindakan", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    Box(modifier = Modifier.fillMaxWidth().heightIn(min = 240.dp, max = 360.dp)) {
                        OperationalIssueList(issues = summary.issues)
                    }
                } ?: Text("Data ringkasan tidak tersedia.")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss) { Text("Tutup") }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(onClick = onExport, enabled = !isBusy && state.reportingSummary != null) {
                        Text(if (isBusy) "Mengekspor..." else "Export Bundle CSV")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoidSaleDialog(
    voidState: VoidSaleState,
    recentSales: List<SaleHistoryEntry>,
    onDismiss: () -> Unit,
    onSelectSale: (String) -> Unit,
    onReasonCodeChanged: (String) -> Unit,
    onReasonDetailChanged: (String) -> Unit,
    onInventoryFollowUpChanged: (String) -> Unit,
    onConfirm: () -> Unit,
    isBusy: Boolean = false
) {
    BasicAlertDialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            tonalElevation = 4.dp,
            modifier = Modifier.widthIn(min = 920.dp, max = 1080.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Void Penjualan", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Text(
                            "Jalur ini hanya untuk sale CASH yang sudah final. Refund kas dicatat eksplisit, stok tidak dibalik otomatis.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    ShortcutHintBar(hints = listOf("F7 Buka", "Review Refund", "Esc Tutup"))
                }

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                    StageSectionCard(title = "Pilih Transaksi", modifier = Modifier.weight(1f)) {
                        if (recentSales.isEmpty()) {
                            Text(
                                "Belum ada transaksi recent yang bisa direview.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxWidth().heightIn(min = 220.dp, max = 300.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(recentSales) { sale ->
                                    ElevatedCard(
                                        onClick = { onSelectSale(sale.saleId) },
                                        shape = RoundedCornerShape(14.dp),
                                        colors = if (voidState.selectedSaleId == sale.saleId) {
                                            CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                                        } else {
                                            CardDefaults.elevatedCardColors()
                                        }
                                    ) {
                                        Column(
                                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text("${sale.localNumber} | ${sale.paymentMethod}", fontWeight = FontWeight.Bold)
                                            Text(
                                                "Rp ${sale.finalAmount.toInt()} | ${sale.saleStatus.name} | ${sale.voidedAtEpochMs?.let { "voided" } ?: "normal"}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    StageSectionCard(title = "Assessment Void", modifier = Modifier.weight(1f)) {
                        ReportingKeyValue("Sale", voidState.selectedLocalNumber ?: "-")
                        ReportingKeyValue("Metode Bayar", voidState.selectedPaymentMethod ?: "-")
                        ReportingKeyValue("Status", voidState.selectedSaleStatus ?: "-")
                        ReportingKeyValue(
                            "Nominal",
                            voidState.selectedAmount?.let { "Rp ${it.toInt()}" } ?: "-"
                        )
                        ReportingKeyValue("Inventory", voidState.inventoryImpactClassification)
                        Text(
                            voidState.assessmentMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (voidState.canExecute) toneColor(UiTone.Warning) else MaterialTheme.colorScheme.error
                        )
                    }
                }

                StageSectionCard(title = "Alasan & Follow-up") {
                    ReasonOptionGroup(
                        title = "Alasan pembatalan",
                        options = voidState.reasonOptions,
                        selectedCode = voidState.reasonCode,
                        onSelected = onReasonCodeChanged
                    )
                    SemanticTextField(
                        label = "Catatan Void",
                        value = voidState.reasonDetail,
                        onValueChange = onReasonDetailChanged,
                        helperText = "Jelaskan konteks koreksi transaksi secara singkat dan bisa diaudit.",
                        placeholder = "Contoh: double input sebelum pelanggan pergi",
                        leadingIcon = Icons.Default.EditNote
                    )
                    SemanticTextField(
                        label = "Catatan Follow-up Stok",
                        value = voidState.inventoryFollowUpNote,
                        onValueChange = onInventoryFollowUpChanged,
                        helperText = "Tidak mengembalikan stok otomatis. Gunakan kolom ini untuk instruksi investigasi atau follow-up manual.",
                        placeholder = "Contoh: barang masih di meja kasir, cek fisik sebelum adjustment terpisah",
                        leadingIcon = Icons.Default.Inventory2,
                        singleLine = false
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    OutlinedButton(onClick = onDismiss) { Text("Tutup") }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = onConfirm,
                        enabled = !isBusy && voidState.canExecute && voidState.reasonCode.isNotBlank()
                    ) {
                        Text(if (isBusy) "Memproses..." else "Eksekusi Void")
                    }
                }
            }
        }
    }
}

@Composable
private fun ReasonOptionGroup(
    title: String,
    options: List<ReasonOption>,
    selectedCode: String,
    onSelected: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        options.forEach { option ->
            FilterChip(
                selected = selectedCode == option.code,
                onClick = { onSelected(option.code) },
                label = { Text(option.title) }
            )
        }
    }
}

@Composable
private fun PendingApprovalRow(
    approval: id.azureenterprise.cassy.kernel.domain.PendingApprovalSummary,
    onApprove: (String) -> Unit,
    onDeny: (String) -> Unit
) {
    Surface(
        tonalElevation = 1.dp,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(approval.title, fontWeight = FontWeight.Bold)
            Text(approval.detail)
            approval.amount?.let { Text("Nominal: Rp ${it.toInt()}") }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { onDeny(approval.id) }, modifier = Modifier.weight(1f)) { Text("Tolak") }
                Button(onClick = { onApprove(approval.id) }, modifier = Modifier.weight(1f)) { Text("Setujui") }
            }
        }
    }
}

@Composable
fun OperationDecisionRow(decision: OperationDecision) {
    val tone = when (decision.status) {
        OperationStatus.READY -> UiTone.Success
        OperationStatus.COMPLETED -> UiTone.Info
        OperationStatus.REQUIRES_APPROVAL -> UiTone.Warning
        OperationStatus.BLOCKED,
        OperationStatus.UNAVAILABLE -> UiTone.Danger
    }
    val icon = when (decision.status) {
        OperationStatus.READY,
        OperationStatus.COMPLETED -> Icons.Default.CheckCircle
        OperationStatus.REQUIRES_APPROVAL -> Icons.Default.Warning
        OperationStatus.BLOCKED,
        OperationStatus.UNAVAILABLE -> Icons.Default.Lock
    }
    Surface(
        color = toneColor(tone).copy(alpha = 0.08f),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OperationDecisionIcon(icon, tone)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(humanizeOperationDecisionTitle(decision), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Text(humanizeDecisionMessage(decision.message), style = MaterialTheme.typography.bodySmall)
            }
            Text(
                text = decision.actionLabel?.replace("Business Day", "hari bisnis") ?: decision.type.toShortLabel(),
                style = MaterialTheme.typography.labelSmall,
                color = toneColor(tone)
            )
        }
    }
}

@Composable
private fun OperationDecisionIcon(icon: ImageVector, tone: UiTone) {
    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = toneColor(tone)
    )
}

@Composable
private fun ShortcutNominalRow(
    values: List<String>,
    onShortcutSelected: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Shortcut nominal", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            values.take(2).forEach { value ->
                OutlinedButton(
                    onClick = { onShortcutSelected(value) },
                    modifier = Modifier.weight(1f).height(40.dp)
                ) {
                    Text(value.toShortcutLabel())
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            values.drop(2).forEach { value ->
                OutlinedButton(
                    onClick = { onShortcutSelected(value) },
                    modifier = Modifier.weight(1f).height(40.dp)
                ) {
                    Text(value.toShortcutLabel())
                }
            }
        }
    }
}

@Composable
fun CenterPanel(
    title: String,
    subtitle: String,
    contentWidth: androidx.compose.ui.unit.Dp = 480.dp,
    action: @Composable () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        ElevatedCard(modifier = Modifier.width(contentWidth), shape = RoundedCornerShape(24.dp)) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(32.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                Text(subtitle, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), textAlign = TextAlign.Center)
                action()
            }
        }
    }
}

@Composable
private fun FormField(
    label: String,
    value: String,
    modifier: Modifier = Modifier.fillMaxWidth(),
    masked: Boolean = false,
    helperText: String? = null,
    onValueChange: (String) -> Unit
) {
    SemanticTextField(
        label = label,
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        helperText = helperText,
        visualTransformation = if (masked) androidx.compose.ui.text.input.PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None
    )
}

@Composable
fun BannerCard(banner: UiBanner, onDismiss: () -> Unit) {
    val tone = banner.tone
    val icon = when (tone) {
        UiTone.Info -> Icons.Default.Info
        UiTone.Success -> Icons.Default.CheckCircle
        UiTone.Warning -> Icons.Default.Warning
        UiTone.Danger -> Icons.Default.Error
    }
    val title = when (tone) {
        UiTone.Info -> "Info"
        UiTone.Success -> "Berhasil"
        UiTone.Warning -> "Perhatian"
        UiTone.Danger -> "Masalah Operasional"
    }
    val message = banner.message.ifBlank {
        when (tone) {
            UiTone.Info -> "Ada pembaruan status, tetapi detail pesan tidak tersedia."
            UiTone.Success -> "Aksi selesai, tetapi detail pesan tidak tersedia."
            UiTone.Warning -> "Perlu perhatian operator, tetapi detail pesan tidak tersedia."
            UiTone.Danger -> "Terjadi masalah, tetapi detail pesan tidak tersedia."
        }
    }
    ElevatedCard(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = toneContainerColor(banner.tone)
        ),
        modifier = Modifier.widthIn(min = 320.dp, max = 440.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(icon, contentDescription = null, tint = toneContentColor(tone), modifier = Modifier.padding(top = 2.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, fontWeight = FontWeight.Bold, color = toneContentColor(tone))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 3,
                    color = toneContentColor(tone)
                )
            }
            IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Tutup", tint = toneContentColor(tone))
            }
        }
    }
}

@Composable
private fun StageSectionCard(
    title: String,
    modifier: Modifier = Modifier.fillMaxWidth(),
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        tonalElevation = 1.dp,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                content()
            }
        )
    }
}

private data class BootstrapReadinessItem(
    val title: String,
    val detail: String,
    val isReady: Boolean
)

@Composable
private fun BootstrapReceiptToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun BootstrapReadinessRow(item: BootstrapReadinessItem) {
    val icon = if (item.isReady) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked
    Surface(
        color = if (item.isReady) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f)
        },
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (item.isReady) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(item.title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Text(
                    item.detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun BootstrapRoleCard(
    title: String,
    detail: String,
    avatarPath: String?,
    onSelectAvatar: () -> Unit,
    onClearAvatar: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BootstrapAvatarPlaceholder(
                    title = title,
                    avatarPath = avatarPath
                )
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        detail,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = onSelectAvatar, modifier = Modifier.weight(1f)) { Text("Pilih foto") }
                OutlinedButton(
                    onClick = onClearAvatar,
                    enabled = avatarPath != null,
                    modifier = Modifier.weight(1f)
                ) { Text("Hapus foto") }
            }

            avatarPath?.let {
                Text(
                    "File lokal: ${java.io.File(it).name}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            content()
        }
    }
}

@Composable
private fun BootstrapAvatarPlaceholder(
    title: String,
    avatarPath: String?
) {
    ManagedAvatarPreview(
        imagePath = avatarPath,
        fallbackLabel = title,
        contentDescription = "Foto $title",
        size = 72.dp
    )
}

@Composable
private fun BootstrapPreviewRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.End
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
    ElevatedCard(
        modifier = modifier.border(
            BorderStroke(
                1.dp,
                if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                else MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)
            ),
            RoundedCornerShape(14.dp)
        ),
        shape = RoundedCornerShape(14.dp),
        onClick = onClick,
        colors = if (selected) {
            CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
        } else {
            CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f))
        }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BootstrapAvatarPlaceholder(
                title = option.displayName.take(1).ifBlank { option.roleLabel.take(1) },
                avatarPath = option.avatarPath
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(option.displayName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text(
                    option.roleLabel.roleUiLabel(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    if (selected) "Operator terpilih untuk login" else option.capabilitySummary,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }
            if (selected) {
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.padding(6.dp).size(14.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
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
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(6) { index ->
            val isFilled = index < pin.length
            val isActive = index == pin.length.coerceAtMost(5) && pin.length < 6
            Surface(
                modifier = Modifier.weight(1f).height(70.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.38f),
                border = BorderStroke(
                    1.dp,
                    when {
                        isActive -> MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)
                        isFilled -> MaterialTheme.colorScheme.primary.copy(alpha = 0.28f)
                        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)
                    }
                )
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    when {
                        isFilled -> Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        ) {}
                        isActive -> HorizontalDivider(
                            modifier = Modifier.width(20.dp),
                            thickness = 2.dp,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                        )
                        else -> Text(" ", style = MaterialTheme.typography.bodyMedium)
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
                    icon = Icons.Default.Backspace,
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
                    icon = Icons.Default.ArrowForward,
                    onClick = onSubmit,
                    enabled = submitEnabled && !isBusy,
                    modifier = Modifier.weight(1f),
                    tone = UiTone.Success
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
        shape = RoundedCornerShape(999.dp),
        color = toneContainerColor(tone)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelMedium,
            color = toneColor(tone),
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun ReportingMetricTile(
    title: String,
    primaryValue: String,
    supporting: String,
    modifier: Modifier = Modifier,
    tone: UiTone = UiTone.Info
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = toneContainerColor(tone)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(title, style = MaterialTheme.typography.labelLarge, color = toneColor(tone))
            Text(primaryValue, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
            Text(supporting, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ReportingKeyValue(
    label: String,
    value: String
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.End)
    }
}

private fun OperationType.toShortLabel(): String = when (this) {
    OperationType.OPEN_BUSINESS_DAY -> "Buka Hari"
    OperationType.START_SHIFT -> "Buka Shift"
    OperationType.CASH_IN -> "Uang Masuk"
    OperationType.CASH_OUT -> "Uang Keluar"
    OperationType.SAFE_DROP -> "Simpan Kas"
    OperationType.CLOSE_SHIFT -> "Tutup Shift"
    OperationType.CLOSE_BUSINESS_DAY -> "Tutup Hari"
    OperationType.VOID_SALE -> "Void"
    OperationType.STOCK_ADJUSTMENT -> "Adj. Stok"
    OperationType.RESOLVE_STOCK_DISCREPANCY -> "Selisih Stok"
}

private fun humanizeOperationDecisionTitle(decision: OperationDecision): String = when (decision.type) {
    OperationType.OPEN_BUSINESS_DAY -> "Buka hari bisnis"
    OperationType.START_SHIFT -> "Buka shift"
    OperationType.CASH_IN -> "Catat uang masuk"
    OperationType.CASH_OUT -> "Catat uang keluar"
    OperationType.SAFE_DROP -> "Simpan uang ke brankas"
    OperationType.CLOSE_SHIFT -> "Tutup shift"
    OperationType.CLOSE_BUSINESS_DAY -> "Tutup hari"
    OperationType.VOID_SALE -> "Batalkan transaksi final"
    OperationType.STOCK_ADJUSTMENT -> "Sesuaikan stok"
    OperationType.RESOLVE_STOCK_DISCREPANCY -> "Tindak lanjuti selisih stok"
}

private fun humanizeDecisionMessage(message: String): String {
    return message
        .replace("Business day", "Hari bisnis")
        .replace("business day", "hari bisnis")
        .replace("Close day", "Tutup hari")
        .replace("Cash In", "uang masuk")
        .replace("Cash Out", "uang keluar")
        .replace("Safe Drop", "simpan uang ke brankas")
        .replace("Reason Code", "alasan operasional")
}

private fun CashMovementType.toUiLabel(): String = when (this) {
    CashMovementType.CASH_IN -> "Uang Masuk"
    CashMovementType.CASH_OUT -> "Uang Keluar"
    CashMovementType.SAFE_DROP -> "Simpan Kas"
}

private fun String.toShortcutLabel(): String = when (this) {
    "100000" -> "Rp 100rb"
    "200000" -> "Rp 200rb"
    "500000" -> "Rp 500rb"
    "1000000" -> "Rp 1jt"
    else -> "Rp $this"
}

private fun SyncStatus.toUiLabel(): String = when (level) {
    SyncLevel.HEALTHY -> "Sehat"
    SyncLevel.PENDING -> "Menunggu"
    SyncLevel.DELAYED -> "Terlambat"
    SyncLevel.STALLED -> "Macet"
    SyncLevel.ERROR -> "Error"
}

private fun Instant.toUiTimestamp(): String = toString()

private fun BootstrapState.visibleError(field: BootstrapField): String? {
    return fieldErrors[field]?.takeIf { submitAttempted || field in touchedFields }
}

private fun StoreProfileState.visibleError(field: StoreProfileUiField): String? {
    return fieldErrors[field]?.takeIf { submitAttempted || field in touchedFields }
}

private fun String.roleUiLabel(): String = when (uppercase()) {
    "CASHIER" -> "Kasir"
    "SUPERVISOR" -> "Supervisor"
    "OWNER" -> "Owner"
    else -> this
}
