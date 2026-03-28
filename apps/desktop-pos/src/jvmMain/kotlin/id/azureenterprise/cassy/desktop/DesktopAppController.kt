package id.azureenterprise.cassy.desktop

import id.azureenterprise.cassy.inventory.application.InventoryService
import id.azureenterprise.cassy.inventory.domain.InventoryActionExecutionResult
import id.azureenterprise.cassy.inventory.domain.InventoryApprovalAction
import id.azureenterprise.cassy.inventory.domain.InventoryDiscrepancyReview
import id.azureenterprise.cassy.inventory.domain.InventoryDiscrepancyStatus
import id.azureenterprise.cassy.inventory.domain.InventoryReadback
import id.azureenterprise.cassy.inventory.domain.StockAdjustmentDraft
import id.azureenterprise.cassy.inventory.domain.StockCountDraft
import id.azureenterprise.cassy.kernel.application.*
import id.azureenterprise.cassy.kernel.domain.*
import id.azureenterprise.cassy.masterdata.data.ProductBarcodeRecord
import id.azureenterprise.cassy.masterdata.data.ProductRepository
import id.azureenterprise.cassy.masterdata.domain.Category
import id.azureenterprise.cassy.masterdata.domain.Product
import id.azureenterprise.cassy.masterdata.domain.ProductLookupResult
import id.azureenterprise.cassy.masterdata.domain.ProductLookupUseCase
import id.azureenterprise.cassy.sales.application.SalesService
import id.azureenterprise.cassy.sales.application.VoidSaleService
import id.azureenterprise.cassy.sales.domain.Basket
import id.azureenterprise.cassy.sales.domain.CashTenderQuote
import id.azureenterprise.cassy.sales.domain.CompleteSaleOutcome
import id.azureenterprise.cassy.sales.domain.ReceiptPrintState
import id.azureenterprise.cassy.sales.domain.ReceiptPrintStatus
import id.azureenterprise.cassy.sales.domain.SaleHistoryEntry
import id.azureenterprise.cassy.sales.domain.SaleStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.File

class DesktopAppController(
    private val accessService: AccessService,
    private val businessDayService: BusinessDayService,
    private val shiftService: ShiftService,
    private val cashControlService: CashControlService,
    private val shiftClosingService: ShiftClosingService,
    private val operationalControlService: OperationalControlService,
    private val productRepository: ProductRepository,
    private val productLookupUseCase: ProductLookupUseCase,
    private val inventoryService: InventoryService,
    private val salesService: SalesService,
    private val voidSaleService: VoidSaleService,
    private val hardwarePort: CashierHardwarePort,
    private val reportingQueryFacade: ReportingQueryFacade,
    private val syncReplayService: SyncReplayService,
    private val reportingExporter: DesktopReportingExporter,
    private val bootstrapAvatarStore: BootstrapAvatarStore,
    private val storeProfileService: StoreProfileService,
    private val storeProfileLogoStore: StoreProfileLogoStore
) {
    private val _state = MutableStateFlow(DesktopAppState())
    val state: StateFlow<DesktopAppState> = _state.asStateFlow()

    suspend fun load() {
        mutateBusy(true)
        updateLoadingState(
            phase = DesktopLoadingPhase.PreparingStorage,
            detail = "Menyiapkan data lokal Cassy"
        )
        runCatching {
            updateLoadingState(
                phase = DesktopLoadingPhase.RestoringContext,
                detail = "Membaca terminal, operator, dan sesi terakhir"
            )
            refreshStage()
        }
            .onFailure { error ->
                _state.update {
                    it.copy(
                        isBusy = false,
                        stage = DesktopStage.FatalError(error.message ?: "Gagal memuat state desktop"),
                        banner = UiBanner(UiTone.Danger, error.message ?: "Gagal memuat state desktop")
                    )
                }
            }
    }

    suspend fun replaySyncAndReload() {
        mutateBusy(true)
        val result = syncReplayService.replayPending()
        val banner = when (result) {
            SyncReplayResult.Idle -> UiBanner(UiTone.Info, "Tidak ada event sync yang perlu diproses")
            is SyncReplayResult.Unavailable -> UiBanner(
                UiTone.Warning,
                "Sync belum dikonfigurasi. Pending ${result.pendingCount}, failed ${result.failedCount}"
            )
            is SyncReplayResult.Completed -> when {
                result.failedCount == 0 -> UiBanner(
                    UiTone.Success,
                    "Sync memproses ${result.processedCount}/${result.attemptedCount} event. Sisa pending ${result.pendingRemaining}."
                )
                result.processedCount > 0 -> UiBanner(
                    UiTone.Warning,
                    "Sync sebagian berhasil. OK ${result.processedCount}, gagal ${result.failedCount}, conflict ${result.conflictCount}."
                )
                else -> UiBanner(
                    UiTone.Danger,
                    result.message ?: "Sync gagal. Tidak ada event yang berhasil diproses."
                )
            }
        }
        refreshStage(banner)
    }

    suspend fun exportOperationalReport() {
        mutateBusy(true)
        val reportingSummary = state.value.operations.reportingSummary
            ?: return pushBanner(UiBanner(UiTone.Warning, "Ringkasan operasional belum tersedia untuk diekspor"))
        val shiftSummary = state.value.operations.reportingShiftSummary
        runCatching {
            reportingExporter.export(
                OperationalReportBundle(
                    shell = state.value.shell,
                    dailySummary = reportingSummary,
                    shiftSummary = shiftSummary,
                    exportedBy = state.value.shell.operatorName
                )
            )
        }.onSuccess { result ->
            _state.update {
                it.copy(
                    isBusy = false,
                    operations = it.operations.copy(
                        reportingExportPath = result.exportDirectory.toAbsolutePath().toString(),
                        reportingExportRuleNote = result.ruleNote,
                        reportingExportedAt = result.exportedAt
                    ),
                    banner = UiBanner(
                        UiTone.Success,
                        "Laporan operasional diekspor ke ${result.exportDirectory.toAbsolutePath()}"
                    )
                )
            }
        }.onFailure { error ->
            pushBanner(UiBanner(UiTone.Danger, error.message ?: "Export laporan operasional gagal"))
        }
    }

    fun updateBootstrapField(field: BootstrapField, value: String) {
        _state.update {
            val updatedBootstrap = when (field) {
                BootstrapField.StoreName -> it.bootstrap.copy(storeName = value)
                BootstrapField.TerminalName -> it.bootstrap.copy(terminalName = value)
                BootstrapField.CashierName -> it.bootstrap.copy(cashierName = value)
                BootstrapField.CashierPin -> it.bootstrap.copy(cashierPin = value.take(6))
                BootstrapField.CashierAvatar -> it.bootstrap
                BootstrapField.SupervisorName -> it.bootstrap.copy(supervisorName = value)
                BootstrapField.SupervisorPin -> it.bootstrap.copy(supervisorPin = value.take(6))
                BootstrapField.SupervisorAvatar -> it.bootstrap
            }.copy(
                touchedFields = it.bootstrap.touchedFields + field
            )
            it.copy(
                bootstrap = applyBootstrapValidation(updatedBootstrap)
            )
        }
    }

    fun selectBootstrapAvatar(field: BootstrapField) {
        val role = when (field) {
            BootstrapField.CashierAvatar -> OperatorRole.CASHIER
            BootstrapField.SupervisorAvatar -> OperatorRole.SUPERVISOR
            else -> return
        }
        val existingPath = when (field) {
            BootstrapField.CashierAvatar -> state.value.bootstrap.cashierAvatarPath
            BootstrapField.SupervisorAvatar -> state.value.bootstrap.supervisorAvatarPath
            else -> null
        }
        bootstrapAvatarStore.chooseAndImport(role, existingPath)
            .onSuccess { importedPath ->
                if (importedPath == null) return
                _state.update {
                    val updatedBootstrap = when (field) {
                        BootstrapField.CashierAvatar -> it.bootstrap.copy(cashierAvatarPath = importedPath)
                        BootstrapField.SupervisorAvatar -> it.bootstrap.copy(supervisorAvatarPath = importedPath)
                        else -> it.bootstrap
                    }.copy(
                        touchedFields = it.bootstrap.touchedFields + field
                    )
                    it.copy(
                        bootstrap = applyBootstrapValidation(updatedBootstrap),
                        banner = UiBanner(UiTone.Success, "Foto profil lokal dipilih")
                    )
                }
            }
            .onFailure { error ->
                pushBanner(UiBanner(UiTone.Warning, error.message ?: "Foto profil tidak bisa dipakai"))
            }
    }

    fun clearBootstrapAvatar(field: BootstrapField) {
        _state.update {
            when (field) {
                BootstrapField.CashierAvatar -> bootstrapAvatarStore.deleteManaged(it.bootstrap.cashierAvatarPath)
                BootstrapField.SupervisorAvatar -> bootstrapAvatarStore.deleteManaged(it.bootstrap.supervisorAvatarPath)
                else -> return@update it
            }
            val updatedBootstrap = when (field) {
                BootstrapField.CashierAvatar -> it.bootstrap.copy(cashierAvatarPath = null)
                BootstrapField.SupervisorAvatar -> it.bootstrap.copy(supervisorAvatarPath = null)
                else -> it.bootstrap
            }.copy(
                touchedFields = it.bootstrap.touchedFields + field
            )
            it.copy(
                bootstrap = applyBootstrapValidation(updatedBootstrap),
                banner = UiBanner(UiTone.Info, "Foto profil dihapus")
            )
        }
    }

    suspend fun bootstrapStore() {
        mutateBusy(true)
        val currentState = state.value
        val validatedBootstrap = applyBootstrapValidation(
            currentState.bootstrap.copy(submitAttempted = true)
        )
        val validatedStoreProfile = applyStoreProfileValidation(
            currentState.storeProfile.copy(submitAttempted = true)
        )
        _state.update {
            it.copy(
                bootstrap = validatedBootstrap,
                storeProfile = validatedStoreProfile
            )
        }
        val requiresBootstrapIdentity = currentState.bootstrapMode == BootstrapMode.FullSetup
        val hasBootstrapErrors = requiresBootstrapIdentity && validatedBootstrap.fieldErrors.isNotEmpty()
        val hasStoreProfileErrors = validatedStoreProfile.fieldErrors.isNotEmpty()
        if (hasBootstrapErrors || hasStoreProfileErrors) {
            mutateBusy(false)
            pushBanner(UiBanner(UiTone.Warning, "Lengkapi dulu data usaha dan operator yang masih belum valid"))
            return
        }

        val bootstrapResult = if (requiresBootstrapIdentity) {
            accessService.bootstrapStore(validatedBootstrap.toBootstrapStoreRequest())
        } else {
            Result.success(accessService.restoreContext().terminalBinding)
        }
        val storeId = bootstrapResult.getOrNull()?.storeId
            ?: accessService.restoreContext().terminalBinding?.storeId
        if (storeId == null) {
            mutateBusy(false)
            pushBanner(UiBanner(UiTone.Danger, "Store belum siap disimpan. Ulangi bootstrap terminal utama."))
            return
        }

        val profileResult = storeProfileService.save(validatedStoreProfile.toDraft())
        refreshStage(
            profileResult.fold(
                onSuccess = {
                    val message = if (requiresBootstrapIdentity) {
                        "Identitas usaha lengkap. Lanjut login operator lokal."
                    } else {
                        "Identitas usaha sudah lengkap. Login operator bisa dilanjutkan."
                    }
                    UiBanner(UiTone.Success, message)
                },
                onFailure = { UiBanner(UiTone.Danger, it.message ?: "Profil usaha gagal disimpan") }
            )
        )
    }

    fun updateStoreProfileField(field: StoreProfileUiField, value: String) {
        _state.update {
            val updated = when (field) {
                StoreProfileUiField.BusinessName -> it.storeProfile.copy(businessName = value)
                StoreProfileUiField.StreetAddress -> it.storeProfile.copy(streetAddress = value)
                StoreProfileUiField.Neighborhood -> it.storeProfile.copy(neighborhood = value)
                StoreProfileUiField.Village -> it.storeProfile.copy(village = value)
                StoreProfileUiField.District -> it.storeProfile.copy(district = value)
                StoreProfileUiField.City -> it.storeProfile.copy(city = value)
                StoreProfileUiField.Province -> it.storeProfile.copy(province = value)
                StoreProfileUiField.PostalCode -> it.storeProfile.copy(postalCode = value)
                StoreProfileUiField.PhoneCountryCode -> it.storeProfile.copy(phoneCountryCode = value)
                StoreProfileUiField.PhoneNumber -> it.storeProfile.copy(phoneNumber = value)
                StoreProfileUiField.BusinessEmail -> it.storeProfile.copy(businessEmail = value)
                StoreProfileUiField.LegalId -> it.storeProfile.copy(legalId = value)
                StoreProfileUiField.ReceiptNote -> it.storeProfile.copy(receiptNote = value)
                StoreProfileUiField.LogoPath -> it.storeProfile
            }.copy(
                touchedFields = it.storeProfile.touchedFields + field
            )
            it.copy(storeProfile = applyStoreProfileValidation(updated))
        }
    }

    fun updateStoreProfileToggle(field: StoreProfileToggleField, enabled: Boolean) {
        _state.update {
            val updated = when (field) {
                StoreProfileToggleField.ShowLogoOnReceipt -> it.storeProfile.copy(showLogoOnReceipt = enabled)
                StoreProfileToggleField.ShowAddressOnReceipt -> it.storeProfile.copy(showAddressOnReceipt = enabled)
                StoreProfileToggleField.ShowPhoneOnReceipt -> it.storeProfile.copy(showPhoneOnReceipt = enabled)
            }
            it.copy(storeProfile = applyStoreProfileValidation(updated))
        }
    }

    fun selectStoreProfileLogo() {
        val storeId = state.value.shell.storeId
            ?: return pushBanner(UiBanner(UiTone.Warning, "Profil usaha belum bisa diubah sebelum toko terdaftar"))
        storeProfileLogoStore.chooseAndImport(storeId, state.value.storeProfile.logoPath)
            .onSuccess { importedPath ->
                if (importedPath == null) return
                _state.update {
                    val updated = it.storeProfile.copy(
                        logoPath = importedPath,
                        touchedFields = it.storeProfile.touchedFields + StoreProfileUiField.LogoPath
                    )
                    it.copy(
                        storeProfile = applyStoreProfileValidation(updated),
                        banner = UiBanner(UiTone.Success, "Logo usaha lokal dipilih")
                    )
                }
            }
            .onFailure { error ->
                pushBanner(UiBanner(UiTone.Warning, error.message ?: "Logo usaha tidak bisa dipakai"))
            }
    }

    fun clearStoreProfileLogo() {
        _state.update {
            storeProfileLogoStore.deleteManaged(it.storeProfile.logoPath)
            val updated = it.storeProfile.copy(
                logoPath = null,
                touchedFields = it.storeProfile.touchedFields + StoreProfileUiField.LogoPath
            )
            it.copy(
                storeProfile = applyStoreProfileValidation(updated),
                banner = UiBanner(UiTone.Info, "Logo usaha dihapus")
            )
        }
    }

    suspend fun saveStoreProfile() {
        mutateBusy(true)
        val validated = applyStoreProfileValidation(
            state.value.storeProfile.copy(submitAttempted = true)
        )
        _state.update { it.copy(storeProfile = validated) }
        if (validated.fieldErrors.isNotEmpty()) {
            mutateBusy(false)
            pushBanner(UiBanner(UiTone.Warning, "Periksa lagi field profil usaha yang belum valid"))
            return
        }

        val result = storeProfileService.save(validated.toDraft())
        refreshStage(
            result.fold(
                onSuccess = { UiBanner(UiTone.Success, "Profil usaha berhasil disimpan") },
                onFailure = { UiBanner(UiTone.Danger, it.message ?: "Profil usaha gagal disimpan") }
            )
        )
    }

    fun selectOperator(operatorId: String) {
        _state.update {
            it.copy(login = it.login.copy(selectedOperatorId = operatorId, pin = "", feedback = null))
        }
    }

    fun updatePin(pin: String) {
        _state.update { it.copy(login = it.login.copy(pin = pin.take(6), feedback = null)) }
    }

    fun selectWorkspace(workspace: DesktopWorkspace) {
        _state.update { current ->
            if (current.stage != DesktopStage.Workspace) return@update current
            val allowed = current.shell.availableWorkspaces
            val resolved = workspace.takeIf { it in allowed } ?: current.activeWorkspace
            current.copy(
                activeWorkspace = resolved,
                shell = current.shell.copy(
                    workspaceTitle = resolveWorkspaceTitle(
                        resolved,
                        current.inventoryRoute,
                        current.operationsRoute
                    )
                )
            )
        }
    }

    fun selectInventoryRoute(route: DesktopInventoryRoute) {
        _state.update {
            it.copy(
                inventoryRoute = route,
                activeWorkspace = DesktopWorkspace.Inventory,
                shell = it.shell.copy(workspaceTitle = resolveWorkspaceTitle(DesktopWorkspace.Inventory, route, it.operationsRoute))
            )
        }
    }

    fun selectOperationsRoute(route: DesktopOperationsRoute) {
        _state.update {
            it.copy(
                operationsRoute = route,
                activeWorkspace = DesktopWorkspace.Operations,
                shell = it.shell.copy(workspaceTitle = resolveWorkspaceTitle(DesktopWorkspace.Operations, it.inventoryRoute, route))
            )
        }
    }

    suspend fun login() {
        val operatorId = state.value.login.selectedOperatorId
            ?: return pushBanner(UiBanner(UiTone.Warning, "Pilih operator terlebih dahulu"))
        mutateBusy(true)
        when (val result = accessService.login(operatorId, state.value.login.pin)) {
            is LoginResult.Success -> refreshStage(UiBanner(UiTone.Success, "Login ${result.operator.displayName} berhasil"))
            is LoginResult.WrongPin -> {
                mutateBusy(false)
                _state.update {
                    it.copy(
                        login = it.login.copy(
                            feedback = "PIN salah. Sisa percobaan sebelum lock: ${result.remainingBeforeLock}"
                        ),
                        banner = UiBanner(UiTone.Warning, "PIN operator salah")
                    )
                }
            }
            is LoginResult.Locked -> {
                mutateBusy(false)
                _state.update {
                    it.copy(
                        login = it.login.copy(
                            feedback = "Akses terkunci sampai ${result.lockedUntil ?: "-"}"
                        ),
                        banner = UiBanner(UiTone.Danger, "Akses operator terkunci sementara")
                    )
                }
            }
            LoginResult.OperatorNotFound -> {
                mutateBusy(false)
                pushBanner(UiBanner(UiTone.Danger, "Operator tidak ditemukan"))
            }
            LoginResult.NotBound -> refreshStage(UiBanner(UiTone.Danger, "Terminal belum terikat ke store"))
        }
    }

    suspend fun logout() {
        mutateBusy(true)
        accessService.logout()
        refreshStage(UiBanner(UiTone.Info, "Session operator diakhiri"))
    }

    suspend fun openBusinessDay() {
        mutateBusy(true)
        val result = businessDayService.openNewDay()
        refreshStage(result.fold(
            onSuccess = { UiBanner(UiTone.Success, "Hari bisnis ${it.id} berhasil dibuka") },
            onFailure = { UiBanner(UiTone.Danger, it.message ?: "Buka hari bisnis gagal") }
        ))
    }

    suspend fun startShift() {
        mutateBusy(true)
        val openingCash = state.value.operations.openingCashInput.toDoubleOrNull()
        if (openingCash == null) {
            mutateBusy(false)
            return pushBanner(UiBanner(UiTone.Warning, "Opening cash harus berupa angka"))
        }
        when (
            val result = shiftService.submitStartShift(
                openingCash = openingCash,
                approvalReason = state.value.operations.openingCashReason
            )
        ) {
            is StartShiftExecutionResult.Started -> {
                val message = if (result.approvalApplied) {
                    "Shift ${result.shift.id} dimulai dengan approval opening cash"
                } else {
                    "Shift ${result.shift.id} dimulai"
                }
                refreshStage(UiBanner(UiTone.Success, message))
            }
            is StartShiftExecutionResult.ApprovalRequired -> {
                refreshStage(UiBanner(UiTone.Warning, result.decision.message))
            }
            is StartShiftExecutionResult.Blocked -> {
                refreshStage(UiBanner(UiTone.Danger, result.decision.message))
            }
        }
    }

    suspend fun endShift() {
        mutateBusy(true)
        val closingCash = state.value.operations.closingCashInput.toDoubleOrNull()
        if (closingCash == null) {
            mutateBusy(false)
            return pushBanner(UiBanner(UiTone.Warning, "Closing cash harus berupa angka"))
        }
        when (
            val result = shiftClosingService.closeShift(
                actualCash = closingCash,
                reasonCode = state.value.operations.closeShiftReasonCode,
                reasonDetail = state.value.operations.closeShiftReasonDetail
            )
        ) {
            is ShiftCloseExecutionResult.Closed -> refreshStage(
                UiBanner(
                    if (result.approvalApplied) UiTone.Warning else UiTone.Success,
                    "Shift ${result.shift.id} ditutup. Selisih kas Rp ${result.report.variance.toInt()}"
                )
            )
            is ShiftCloseExecutionResult.ApprovalRequired -> refreshStage(UiBanner(UiTone.Warning, result.decision.message))
            is ShiftCloseExecutionResult.Blocked -> refreshStage(UiBanner(UiTone.Danger, result.decision.message))
        }
    }

    suspend fun closeBusinessDay() {
        mutateBusy(true)
        val result = businessDayService.closeCurrentDay()
        refreshStage(result.fold(
            onSuccess = { UiBanner(UiTone.Success, "Hari bisnis ${it.id} berhasil ditutup") },
            onFailure = { UiBanner(UiTone.Danger, it.message ?: "Tutup hari gagal") }
        ))
    }

    suspend fun updateCatalogQuery(query: String) {
        _state.update { it.copy(catalog = it.catalog.copy(searchQuery = query)) }
        refreshCatalog()
    }

    fun updateBarcodeInput(value: String) {
        _state.update { it.copy(catalog = it.catalog.copy(barcodeInput = value)) }
    }

    suspend fun scanBarcodeOrSku() {
        mutateBusy(true)
        val input = state.value.catalog.barcodeInput
        val banner = when (val result = productLookupUseCase.execute(input)) {
            is ProductLookupResult.FoundSingle -> {
                salesService.addProduct(result.product).fold(
                    onSuccess = {
                        _state.update {
                            it.copy(
                                catalog = it.catalog.copy(
                                    lookupFeedback = LookupFeedback(
                                        tone = UiTone.Success,
                                        message = "${result.product.name} ditambahkan. Scan berulang otomatis menambah qty."
                                    )
                                )
                            )
                        }
                        UiBanner(UiTone.Success, "${result.product.name} ditambahkan ke cart")
                    },
                    onFailure = { UiBanner(UiTone.Danger, it.message ?: "Gagal menambah item") }
                )
            }
            ProductLookupResult.Collision -> {
                _state.update {
                    it.copy(
                        catalog = it.catalog.copy(
                            lookupFeedback = LookupFeedback(
                                tone = UiTone.Warning,
                                message = "Kode bentrok. Pakai pencarian nama produk agar tidak salah scan."
                            )
                        )
                    )
                }
                UiBanner(UiTone.Warning, "Barcode bentrok. Gunakan pencarian manual.")
            }
            ProductLookupResult.NotFound -> {
                _state.update {
                    it.copy(
                        catalog = it.catalog.copy(
                            lookupFeedback = LookupFeedback(
                                tone = UiTone.Warning,
                                message = "Produk belum ditemukan. Cek barcode/SKU atau daftarkan lebih dulu."
                            )
                        )
                    )
                }
                UiBanner(UiTone.Warning, "Barang tidak ditemukan untuk input: $input")
            }
            ProductLookupResult.Unavailable -> {
                _state.update {
                    it.copy(
                        catalog = it.catalog.copy(
                            lookupFeedback = LookupFeedback(
                                tone = UiTone.Warning,
                                message = "Produk sedang tidak tersedia untuk dijual."
                            )
                        )
                    )
                }
                UiBanner(UiTone.Warning, "Barang sedang tidak tersedia")
            }
            is ProductLookupResult.InvalidInput -> {
                _state.update {
                    it.copy(
                        catalog = it.catalog.copy(
                            lookupFeedback = LookupFeedback(
                                tone = UiTone.Warning,
                                message = result.message
                            )
                        )
                    )
                }
                UiBanner(UiTone.Warning, result.message)
            }
        }
        _state.update { it.copy(catalog = it.catalog.copy(barcodeInput = "")) }
        refreshStage(banner)
    }

    suspend fun addProduct(product: Product) {
        mutateBusy(true)
        val result = salesService.addProduct(product)
        _state.update {
            it.copy(catalog = it.catalog.copy(reviewConfirmed = false))
        }
        refreshStage(result.fold(
            onSuccess = { UiBanner(UiTone.Success, "${product.name} ditambahkan ke keranjang") },
            onFailure = { UiBanner(UiTone.Danger, it.message ?: "Gagal menambah item") }
        ))
    }

    suspend fun incrementItem(product: Product) {
        addProduct(product)
    }

    suspend fun decrementItem(product: Product, currentQuantity: Double) {
        mutateBusy(true)
        val result = salesService.setQuantity(product.id, currentQuantity - 1)
        _state.update {
            it.copy(catalog = it.catalog.copy(reviewConfirmed = false))
        }
        refreshStage(result.fold(
            onSuccess = { UiBanner(UiTone.Info, "${product.name} diperbarui di keranjang") },
            onFailure = { UiBanner(UiTone.Danger, it.message ?: "Gagal memperbarui item") }
        ))
    }

    suspend fun checkoutCash() {
        mutateBusy(true)
        val tenderQuote = currentCashTenderQuote()
            ?: return pushBanner(UiBanner(UiTone.Warning, "Masukkan uang diterima pelanggan terlebih dahulu"))
        if (!tenderQuote.isSufficient) {
            return pushBanner(
                UiBanner(
                    UiTone.Warning,
                    "Uang pelanggan kurang Rp ${tenderQuote.shortageAmount.toInt()}"
                )
            )
        }
        val result = salesService.completeSale("CASH")
        result.fold(
            onSuccess = { outcome ->
                when (outcome) {
                    is CompleteSaleOutcome.Completed -> {
                        val saleId = outcome.result.saleId
                        val finalizedSale = outcome.result.readback
                        val receiptPreview = salesService.getReceiptForPrint(saleId).getOrThrow()
                        val hardwareResult = hardwarePort.handlePostFinalization(
                            paymentMethod = finalizedSale.receiptSnapshot.payment.method,
                            receiptPayload = receiptPreview
                        )
                        refreshStage(
                            UiBanner(
                                tone = if (hardwareResult.warningMessage == null) UiTone.Success else UiTone.Warning,
                                message = hardwareResult.warningMessage
                                    ?: "Transaksi ${finalizedSale.receiptSnapshot.localNumber} selesai. Kembalian Rp ${tenderQuote.changeAmount.toInt()}"
                            )
                        )
                        _state.update {
                            it.copy(
                                hardware = hardwareResult.snapshot,
                                catalog = it.catalog.copy(
                                    lastFinalizedSaleId = saleId,
                                    lastReceiptPreview = receiptPreview.renderedContent,
                                    recentSales = loadRecentSaleHistory(),
                                    cashReceivedInput = "",
                                    cashTenderQuote = null,
                                    reviewConfirmed = false,
                                    memberNumberInput = "",
                                    memberNameInput = "",
                                    memberSkipped = false,
                                    donationOffered = false,
                                    donationSkipped = false,
                                    donationAmountInput = "",
                                    receiptPreview = ReceiptPreviewState(
                                        saleId = saleId,
                                        localNumber = finalizedSale.receiptSnapshot.localNumber,
                                        content = receiptPreview.renderedContent,
                                        availabilityMessage = "Preview struk final siap ditinjau"
                                    ),
                                    printState = receiptPreview.printState,
                                    lookupFeedback = null
                                )
                            )
                        }
                    }
                    is CompleteSaleOutcome.Pending -> {
                        refreshStage(
                            UiBanner(
                                UiTone.Warning,
                                outcome.paymentState.detailMessage ?: "Pembayaran masih pending"
                            )
                        )
                    }
                    is CompleteSaleOutcome.Rejected -> {
                        refreshStage(
                            UiBanner(
                                UiTone.Danger,
                                outcome.paymentState.detailMessage ?: "Pembayaran ditolak"
                            )
                        )
                    }
                }
            },
            onFailure = { error ->
                refreshStage(UiBanner(UiTone.Danger, error.message ?: "Finalisasi transaksi gagal"))
            }
        )
    }

    suspend fun printLastReceipt() {
        val saleId = state.value.catalog.lastFinalizedSaleId
            ?: return pushBanner(UiBanner(UiTone.Warning, "Belum ada struk final yang siap dicetak"))
        mutateBusy(true)
        _state.update {
            it.copy(
                catalog = it.catalog.copy(
                    printState = ReceiptPrintState(
                        status = ReceiptPrintStatus.NOT_REQUESTED,
                        detailMessage = "Mengirim struk ke printer..."
                    )
                )
            )
        }
        val result = salesService.getReceiptForPrint(saleId)
        result.fold(
            onSuccess = { receipt ->
                val printResult = hardwarePort.printReceipt(receipt)
                _state.update {
                    it.copy(
                        isBusy = false,
                        hardware = printResult.snapshot,
                        catalog = it.catalog.copy(
                            lastReceiptPreview = receipt.renderedContent,
                            receiptPreview = ReceiptPreviewState(
                                saleId = saleId,
                                localNumber = receipt.snapshot.localNumber,
                                content = receipt.renderedContent,
                                availabilityMessage = "Preview struk final siap ditinjau"
                            ),
                            printState = printResult.printState
                        ),
                        banner = UiBanner(
                            tone = if (printResult.printState.status == ReceiptPrintStatus.PRINTED) UiTone.Success else UiTone.Warning,
                            message = printResult.printState.detailMessage ?: "Status print berubah"
                        )
                    )
                }
            },
            onFailure = { error ->
                pushBanner(UiBanner(UiTone.Danger, error.message ?: "Gagal memuat struk final"))
            }
        )
    }

    suspend fun reprintLastReceipt() {
        val saleId = state.value.catalog.lastFinalizedSaleId
            ?: return pushBanner(UiBanner(UiTone.Warning, "Belum ada struk final untuk dicetak ulang"))
        mutateBusy(true)
        val result = salesService.getReceiptForPrint(saleId, isReprint = true)
        result.fold(
            onSuccess = { receipt ->
                val printResult = hardwarePort.printReceipt(receipt)
                _state.update {
                    it.copy(
                        isBusy = false,
                        hardware = printResult.snapshot,
                        catalog = it.catalog.copy(
                            lastReceiptPreview = receipt.renderedContent,
                            receiptPreview = ReceiptPreviewState(
                                saleId = saleId,
                                localNumber = receipt.snapshot.localNumber,
                                content = receipt.renderedContent,
                                availabilityMessage = "Preview struk final hasil reprint siap ditinjau"
                            ),
                            printState = printResult.printState
                        ),
                        banner = UiBanner(
                            tone = if (printResult.printState.status == ReceiptPrintStatus.PRINTED) UiTone.Success else UiTone.Warning,
                            message = printResult.printState.detailMessage ?: "Struk final siap dicetak ulang"
                        )
                    )
                }
            },
            onFailure = { error ->
                pushBanner(UiBanner(UiTone.Danger, error.message ?: "Gagal memuat struk final"))
            }
        )
    }

    suspend fun cancelCurrentSale() {
        mutateBusy(true)
        val result = salesService.cancelActiveSale()
        _state.update {
            it.copy(
                catalog = it.catalog.copy(
                    reviewConfirmed = false,
                    memberNumberInput = "",
                    memberNameInput = "",
                    memberSkipped = false,
                    donationOffered = false,
                    donationSkipped = false,
                    donationAmountInput = ""
                )
            )
        }
        refreshStage(
            result.fold(
                onSuccess = { UiBanner(UiTone.Info, "Pesanan draft dibatalkan. Struk final sebelumnya tetap aman.") },
                onFailure = { UiBanner(UiTone.Danger, it.message ?: "Gagal membatalkan pesanan") }
            )
        )
    }

    fun confirmCartReview() {
        _state.update {
            it.copy(catalog = it.catalog.copy(reviewConfirmed = it.catalog.basket.items.isNotEmpty()))
        }
    }

    fun updateMemberNumberInput(value: String) {
        _state.update {
            it.copy(
                catalog = it.catalog.copy(
                    memberNumberInput = value.filter(Char::isDigit).take(16),
                    memberSkipped = false
                )
            )
        }
    }

    fun updateMemberNameInput(value: String) {
        _state.update {
            it.copy(
                catalog = it.catalog.copy(
                    memberNameInput = value.take(48),
                    memberSkipped = false
                )
            )
        }
    }

    fun skipMemberStep() {
        _state.update {
            it.copy(
                catalog = it.catalog.copy(
                    memberNumberInput = "",
                    memberNameInput = "",
                    memberSkipped = true
                )
            )
        }
    }

    fun offerDonation(value: Boolean) {
        _state.update {
            it.copy(
                catalog = it.catalog.copy(
                    donationOffered = value,
                    donationSkipped = !value,
                    donationAmountInput = if (value) it.catalog.donationAmountInput else ""
                )
            )
        }
    }

    fun updateDonationAmountInput(value: String) {
        _state.update {
            it.copy(
                catalog = it.catalog.copy(
                    donationAmountInput = value.filter(Char::isDigit).take(6),
                    donationOffered = true,
                    donationSkipped = false
                )
            )
        }
    }

    fun skipDonationStep() {
        _state.update {
            it.copy(
                catalog = it.catalog.copy(
                    donationOffered = false,
                    donationSkipped = true,
                    donationAmountInput = ""
                )
            )
        }
    }

    fun updateOpeningCashInput(value: String) {
        _state.update { it.copy(operations = it.operations.copy(openingCashInput = value)) }
    }

    fun updateClosingCashInput(value: String) {
        _state.update { it.copy(operations = it.operations.copy(closingCashInput = value)) }
    }

    fun updateOpeningCashReasonInput(value: String) {
        _state.update { it.copy(operations = it.operations.copy(openingCashReason = value)) }
    }

    fun updateCashMovementType(type: CashMovementType) {
        _state.update { current ->
            current.copy(
                operations = current.operations.copy(
                    cashMovementType = type,
                    cashMovementReasonCode = "",
                    cashMovementReasonDetail = ""
                )
            )
        }
    }

    fun updateCashMovementAmountInput(value: String) {
        _state.update { it.copy(operations = it.operations.copy(cashMovementAmountInput = value.filter(Char::isDigit))) }
    }

    fun updateCashMovementReasonCode(value: String) {
        _state.update { it.copy(operations = it.operations.copy(cashMovementReasonCode = value)) }
    }

    fun updateCashMovementReasonDetail(value: String) {
        _state.update { it.copy(operations = it.operations.copy(cashMovementReasonDetail = value)) }
    }

    suspend fun submitCashMovement() {
        mutateBusy(true)
        val amount = state.value.operations.cashMovementAmountInput.toDoubleOrNull()
        if (amount == null) {
            mutateBusy(false)
            return pushBanner(UiBanner(UiTone.Warning, "Nominal kontrol kas harus berupa angka"))
        }
        when (
            val result = cashControlService.submitMovement(
                type = state.value.operations.cashMovementType,
                amount = amount,
                reasonCode = state.value.operations.cashMovementReasonCode,
                reasonDetail = state.value.operations.cashMovementReasonDetail
            )
        ) {
            is CashMovementExecutionResult.Recorded -> refreshStage(
                UiBanner(
                    if (result.approvalApplied) UiTone.Warning else UiTone.Success,
                    "${result.movement.type.name.replace('_', ' ')} dicatat Rp ${result.movement.amount.toInt()}"
                )
            )
            is CashMovementExecutionResult.ApprovalRequired -> refreshStage(UiBanner(UiTone.Warning, result.decision.message))
            is CashMovementExecutionResult.Blocked -> refreshStage(UiBanner(UiTone.Danger, result.decision.message))
        }
    }

    suspend fun approveCashMovement(requestId: String) {
        val direct = accessService.requireCapability(AccessCapability.APPROVE_CASH_MOVEMENT_EXCEPTION).getOrNull()
        if (direct == null) {
            return openStepUpFlow(
                title = "Setujui Kontrol Kas",
                detail = "Supervisor atau owner perlu memasukkan PIN untuk menyetujui kontrol kas ini.",
                capability = AccessCapability.APPROVE_CASH_MOVEMENT_EXCEPTION,
                action = StepUpAction.APPROVE_CASH_MOVEMENT,
                targetId = requestId,
                decisionNote = "Disetujui via step-up desktop"
            )
        }
        mutateBusy(true)
        when (val result = cashControlService.approveCashMovement(requestId, direct)) {
            is CashMovementExecutionResult.Recorded -> refreshStage(
                UiBanner(UiTone.Success, "${result.movement.type.name.replace('_', ' ')} disetujui dan dicatat")
            )
            is CashMovementExecutionResult.ApprovalRequired -> refreshStage(UiBanner(UiTone.Warning, result.decision.message))
            is CashMovementExecutionResult.Blocked -> refreshStage(UiBanner(UiTone.Danger, result.decision.message))
        }
    }

    suspend fun denyCashMovement(requestId: String) {
        val direct = accessService.requireCapability(AccessCapability.APPROVE_CASH_MOVEMENT_EXCEPTION).getOrNull()
        if (direct == null) {
            return openStepUpFlow(
                title = "Tolak Kontrol Kas",
                detail = "Supervisor atau owner perlu memasukkan PIN untuk menolak kontrol kas ini.",
                capability = AccessCapability.APPROVE_CASH_MOVEMENT_EXCEPTION,
                action = StepUpAction.DENY_CASH_MOVEMENT,
                targetId = requestId,
                decisionNote = "Ditolak dari dashboard desktop"
            )
        }
        mutateBusy(true)
        val denied = cashControlService.denyCashMovement(
            requestId = requestId,
            decisionNote = "Ditolak dari dashboard desktop",
            approverOverride = direct
        )
        refreshStage(
            UiBanner(
                tone = if (denied != null) UiTone.Warning else UiTone.Danger,
                message = if (denied != null) "Approval ${denied.id} ditolak" else "Approval tidak bisa ditolak"
            )
        )
    }

    fun updateCloseShiftReasonCode(value: String) {
        _state.update { it.copy(operations = it.operations.copy(closeShiftReasonCode = value)) }
    }

    fun updateCloseShiftReasonDetail(value: String) {
        _state.update { it.copy(operations = it.operations.copy(closeShiftReasonDetail = value)) }
    }

    suspend fun approveCloseShift(requestId: String) {
        val direct = accessService.requireCapability(AccessCapability.APPROVE_SHIFT_CLOSE_EXCEPTION).getOrNull()
        if (direct == null) {
            return openStepUpFlow(
                title = "Setujui Tutup Shift",
                detail = "Supervisor atau owner perlu memasukkan PIN untuk menyetujui tutup shift.",
                capability = AccessCapability.APPROVE_SHIFT_CLOSE_EXCEPTION,
                action = StepUpAction.APPROVE_CLOSE_SHIFT,
                targetId = requestId,
                decisionNote = "Disetujui via step-up desktop"
            )
        }
        mutateBusy(true)
        when (val result = shiftClosingService.approveCloseShift(requestId, direct)) {
            is ShiftCloseExecutionResult.Closed -> refreshStage(
                UiBanner(UiTone.Success, "Shift ${result.shift.id} ditutup lewat approval")
            )
            is ShiftCloseExecutionResult.ApprovalRequired -> refreshStage(UiBanner(UiTone.Warning, result.decision.message))
            is ShiftCloseExecutionResult.Blocked -> refreshStage(UiBanner(UiTone.Danger, result.decision.message))
        }
    }

    suspend fun denyCloseShift(requestId: String) {
        val direct = accessService.requireCapability(AccessCapability.APPROVE_SHIFT_CLOSE_EXCEPTION).getOrNull()
        if (direct == null) {
            return openStepUpFlow(
                title = "Tolak Tutup Shift",
                detail = "Supervisor atau owner perlu memasukkan PIN untuk menolak approval tutup shift.",
                capability = AccessCapability.APPROVE_SHIFT_CLOSE_EXCEPTION,
                action = StepUpAction.DENY_CLOSE_SHIFT,
                targetId = requestId,
                decisionNote = "Ditolak dari wizard close shift"
            )
        }
        mutateBusy(true)
        val denied = shiftClosingService.denyCloseShift(
            requestId = requestId,
            decisionNote = "Ditolak dari wizard close shift",
            approverOverride = direct
        )
        refreshStage(
            UiBanner(
                tone = if (denied) UiTone.Warning else UiTone.Danger,
                message = if (denied) "Approval close shift ditolak" else "Approval close shift tidak bisa ditolak"
            )
        )
    }

    fun selectVoidSale(saleId: String) {
        _state.update {
            val updated = it.operations.voidSale.copy(selectedSaleId = saleId)
            it.copy(operations = it.operations.copy(voidSale = recalculateVoidDraft(updated, it.catalog.recentSales)))
        }
    }

    fun updateVoidReasonCode(value: String) {
        _state.update {
            val updated = it.operations.voidSale.copy(reasonCode = value)
            it.copy(operations = it.operations.copy(voidSale = recalculateVoidDraft(updated, it.catalog.recentSales)))
        }
    }

    fun updateVoidReasonDetail(value: String) {
        _state.update {
            val updated = it.operations.voidSale.copy(reasonDetail = value)
            it.copy(operations = it.operations.copy(voidSale = recalculateVoidDraft(updated, it.catalog.recentSales)))
        }
    }

    fun updateVoidInventoryFollowUpNote(value: String) {
        _state.update {
            val updated = it.operations.voidSale.copy(inventoryFollowUpNote = value)
            it.copy(operations = it.operations.copy(voidSale = recalculateVoidDraft(updated, it.catalog.recentSales)))
        }
    }

    suspend fun executeVoidSale() {
        mutateBusy(true)
        val voidState = state.value.operations.voidSale
        val saleId = voidState.selectedSaleId
            ?: return pushBanner(UiBanner(UiTone.Warning, "Pilih transaksi final yang akan di-void"))
        if (voidState.reasonCode.isBlank()) {
            return pushBanner(UiBanner(UiTone.Warning, "Pilih reason code void terlebih dahulu"))
        }
        val result = voidSaleService.executeVoid(
            saleId = saleId,
            reasonCode = voidState.reasonCode,
            reasonDetail = voidState.reasonDetail,
            inventoryFollowUpNote = voidState.inventoryFollowUpNote
        )
        refreshStage(
            result.fold(
                onSuccess = {
                    UiBanner(
                        UiTone.Warning,
                        "Sale ${it.saleVoid.localNumber} di-void. Refund kas dicatat dan stok tetap perlu follow-up manual."
                    )
                },
                onFailure = {
                    UiBanner(UiTone.Danger, it.message ?: "Pembatalan transaksi final gagal dijalankan")
                }
            )
        )
    }

    fun updateCashReceivedInput(value: String) {
        val digitsOnly = value.filter { it.isDigit() }
        val receivedAmount = digitsOnly.toDoubleOrNull() ?: 0.0
        _state.update {
            it.copy(
                catalog = it.catalog.copy(
                    cashReceivedInput = digitsOnly,
                    cashTenderQuote = salesService.quoteCashTender(receivedAmount).getOrNull()
                )
            )
        }
    }

    suspend fun selectInventoryProduct(productId: String) {
        _state.update {
            it.copy(
                inventory = it.inventory.copy(
                    selectedProductId = productId
                )
            )
        }
        refreshStage()
    }

    suspend fun selectMasterCategory(categoryId: String?) {
        _state.update {
            it.copy(
                masterData = it.masterData.copy(
                    selectedCategoryId = categoryId
                ),
                inventoryRoute = DesktopInventoryRoute.MasterData
            )
        }
        refreshStage()
    }

    suspend fun updateMasterDataSearchQuery(value: String) {
        _state.update { it.copy(masterData = it.masterData.copy(searchQuery = value)) }
        refreshStage()
    }

    suspend fun selectMasterProduct(productId: String) {
        _state.update {
            it.copy(
                masterData = it.masterData.copy(selectedProductId = productId),
                inventoryRoute = DesktopInventoryRoute.MasterData
            )
        }
        refreshStage()
    }

    fun prepareNewMasterProduct() {
        _state.update {
            it.copy(
                masterData = it.masterData.copy(
                    selectedProductId = null,
                    productNameInput = "",
                    productSkuInput = "",
                    productPriceInput = "",
                    productCategoryId = it.masterData.selectedCategoryId ?: it.masterData.productCategoryId,
                    productImageRefInput = "",
                    productIsActive = true,
                    barcodeDraft = "",
                    barcodes = emptyList()
                ),
                inventoryRoute = DesktopInventoryRoute.MasterData
            )
        }
    }

    fun updateMasterProductName(value: String) {
        _state.update { it.copy(masterData = it.masterData.copy(productNameInput = value)) }
    }

    fun updateMasterProductSku(value: String) {
        _state.update { it.copy(masterData = it.masterData.copy(productSkuInput = value.uppercase())) }
    }

    fun updateMasterProductPrice(value: String) {
        _state.update { it.copy(masterData = it.masterData.copy(productPriceInput = value.filter { char -> char.isDigit() || char == '.' })) }
    }

    fun updateMasterProductCategory(value: String) {
        _state.update { it.copy(masterData = it.masterData.copy(productCategoryId = value)) }
    }

    fun updateMasterProductImageRef(value: String) {
        _state.update { it.copy(masterData = it.masterData.copy(productImageRefInput = value)) }
    }

    fun updateMasterProductActive(value: Boolean) {
        _state.update { it.copy(masterData = it.masterData.copy(productIsActive = value)) }
    }

    fun updateMasterBarcodeDraft(value: String) {
        _state.update { it.copy(masterData = it.masterData.copy(barcodeDraft = value.filter(Char::isLetterOrDigit).uppercase())) }
    }

    fun updateMasterBarcodeType(value: String) {
        _state.update { it.copy(masterData = it.masterData.copy(barcodeType = value)) }
    }

    fun updateNewCategoryName(value: String) {
        _state.update { it.copy(masterData = it.masterData.copy(newCategoryName = value)) }
    }

    fun updateNewCategoryColor(value: String) {
        _state.update { it.copy(masterData = it.masterData.copy(newCategoryColor = value)) }
    }

    suspend fun saveMasterCategory() {
        mutateBusy(true)
        val name = state.value.masterData.newCategoryName.trim()
        if (name.isBlank()) {
            mutateBusy(false)
            return pushBanner(UiBanner(UiTone.Warning, "Nama kategori wajib diisi"))
        }
        val color = state.value.masterData.newCategoryColor.trim().ifBlank { "#1F7A8C" }
        productRepository.upsertCategory(
            Category(
                id = IdGenerator.nextId("cat"),
                name = name,
                color = color
            )
        )
        _state.update {
            it.copy(
                masterData = it.masterData.copy(
                    newCategoryName = "",
                    newCategoryColor = color
                )
            )
        }
        refreshStage(UiBanner(UiTone.Success, "Kategori $name ditambahkan ke master data"))
    }

    suspend fun saveMasterProduct() {
        mutateBusy(true)
        val current = state.value.masterData
        val name = current.productNameInput.trim()
        val sku = current.productSkuInput.trim()
        val price = current.productPriceInput.toDoubleOrNull()
        val categoryId = current.productCategoryId
        if (name.isBlank()) return pushBanner(UiBanner(UiTone.Warning, "Nama produk wajib diisi"))
        if (sku.isBlank()) return pushBanner(UiBanner(UiTone.Warning, "SKU wajib diisi"))
        if (price == null || price <= 0.0) return pushBanner(UiBanner(UiTone.Warning, "Harga produk harus valid"))
        if (categoryId.isBlank()) return pushBanner(UiBanner(UiTone.Warning, "Kategori produk wajib dipilih"))
        val productId = current.selectedProductId ?: IdGenerator.nextId("product")
        productRepository.upsertProduct(
            Product(
                id = productId,
                name = name,
                price = price,
                categoryId = categoryId,
                sku = sku,
                imageUrl = current.productImageRefInput.ifBlank { null },
                isActive = current.productIsActive
            )
        )
        _state.update { it.copy(masterData = it.masterData.copy(selectedProductId = productId)) }
        refreshStage(UiBanner(UiTone.Success, "$name disimpan ke master data"))
    }

    suspend fun addMasterBarcode() {
        mutateBusy(true)
        val current = state.value.masterData
        val productId = current.selectedProductId
            ?: return pushBanner(UiBanner(UiTone.Warning, "Pilih atau simpan produk dulu sebelum menambah barcode"))
        val barcode = current.barcodeDraft.trim()
        if (barcode.isBlank()) {
            mutateBusy(false)
            return pushBanner(UiBanner(UiTone.Warning, "Barcode tidak boleh kosong"))
        }
        productRepository.upsertBarcode(barcode = barcode, productId = productId, type = current.barcodeType)
        _state.update { it.copy(masterData = it.masterData.copy(barcodeDraft = "")) }
        refreshStage(UiBanner(UiTone.Success, "Barcode $barcode ditambahkan"))
    }

    suspend fun removeMasterBarcode(barcode: String) {
        mutateBusy(true)
        productRepository.deleteBarcode(barcode)
        refreshStage(UiBanner(UiTone.Info, "Barcode $barcode dilepas dari produk"))
    }

    fun updateInventoryCountQuantityInput(value: String) {
        _state.update {
            it.copy(
                inventory = it.inventory.copy(
                    countQuantityInput = value.toDecimalInput()
                )
            )
        }
    }

    fun updateInventoryAdjustmentDirection(direction: InventoryAdjustmentDirection) {
        _state.update {
            it.copy(
                inventory = it.inventory.copy(
                    adjustmentDirection = direction
                )
            )
        }
    }

    fun updateInventoryAdjustmentQuantityInput(value: String) {
        _state.update {
            it.copy(
                inventory = it.inventory.copy(
                    adjustmentQuantityInput = value.toDecimalInput()
                )
            )
        }
    }

    fun updateInventoryAdjustmentReasonCode(value: String) {
        _state.update {
            it.copy(
                inventory = it.inventory.copy(
                    adjustmentReasonCode = value
                )
            )
        }
    }

    fun updateInventoryAdjustmentReasonDetail(value: String) {
        _state.update {
            it.copy(
                inventory = it.inventory.copy(
                    adjustmentReasonDetail = value
                )
            )
        }
    }

    suspend fun submitInventoryCount() {
        mutateBusy(true)
        val productId = state.value.inventory.selectedProductId
            ?: return pushBanner(UiBanner(UiTone.Warning, "Pilih produk untuk stock count dulu"))
        val countedQuantity = state.value.inventory.countQuantityInput.toDoubleOrNull()
        if (countedQuantity == null) {
            mutateBusy(false)
            return pushBanner(UiBanner(UiTone.Warning, "Qty count harus berupa angka"))
        }
        val terminalId = accessService.restoreContext().terminalBinding?.terminalId
            ?: return pushBanner(UiBanner(UiTone.Danger, "Terminal belum terikat"))
        val result = inventoryService.submitStockCount(
            StockCountDraft(
                productId = productId,
                countedQuantity = countedQuantity,
                terminalId = terminalId
            )
        )
        refreshStage(
            result.fold(
                onSuccess = {
                    if (it.status == InventoryDiscrepancyStatus.MATCHED) {
                        UiBanner(UiTone.Success, "Count cocok. Tidak ada adjustment otomatis.")
                    } else {
                        UiBanner(UiTone.Warning, "Count terekam. Selisih masuk review queue.")
                    }
                },
                onFailure = { UiBanner(UiTone.Danger, it.message ?: "Stock count gagal direkam") }
            )
        )
    }

    suspend fun applyInventoryAdjustment() {
        mutateBusy(true)
        val productId = state.value.inventory.selectedProductId
            ?: return pushBanner(UiBanner(UiTone.Warning, "Pilih produk untuk adjustment dulu"))
        val quantity = state.value.inventory.adjustmentQuantityInput.toDoubleOrNull()
        if (quantity == null || quantity <= 0.0) {
            mutateBusy(false)
            return pushBanner(UiBanner(UiTone.Warning, "Qty adjustment harus lebih besar dari 0"))
        }
        val terminalId = accessService.restoreContext().terminalBinding?.terminalId
            ?: return pushBanner(UiBanner(UiTone.Danger, "Terminal belum terikat"))
        val reasonCode = state.value.inventory.adjustmentReasonCode
        if (reasonCode.isBlank()) {
            mutateBusy(false)
            return pushBanner(UiBanner(UiTone.Warning, "Pilih reason code inventory dulu"))
        }
        val signedDelta = when (state.value.inventory.adjustmentDirection) {
            InventoryAdjustmentDirection.INCREASE -> quantity
            InventoryAdjustmentDirection.DECREASE -> -quantity
        }
        when (
            val result = inventoryService.applyManualAdjustment(
            StockAdjustmentDraft(
                productId = productId,
                quantityDelta = signedDelta,
                terminalId = terminalId,
                reasonCode = reasonCode,
                reasonDetail = state.value.inventory.adjustmentReasonDetail.ifBlank { null }
            )
        )
        ) {
            is InventoryActionExecutionResult.Applied -> refreshStage(
                UiBanner(
                    tone = if (result.mutation.remainingShortageQuantity > 0.0 || result.mutation.balance.quantity < 0.0) {
                        UiTone.Warning
                    } else {
                        UiTone.Success
                    },
                    message = if (result.approvalApplied) {
                        "Penyesuaian stok dicatat setelah verifikasi PIN supervisor. Saldo sekarang ${result.mutation.balance.quantity}."
                    } else {
                        "Adjustment inventory dicatat. Balance sekarang ${result.mutation.balance.quantity}."
                    }
                )
            )
            is InventoryActionExecutionResult.ApprovalRequired -> refreshStage(
                UiBanner(
                    UiTone.Warning,
                    "${result.message} Queue approval: ${result.action.id}."
                )
            )
            is InventoryActionExecutionResult.Blocked -> refreshStage(
                UiBanner(UiTone.Danger, result.message)
            )
        }
    }

    suspend fun resolveInventoryDiscrepancy(reviewId: String) {
        mutateBusy(true)
        val reasonCode = state.value.inventory.adjustmentReasonCode
        if (reasonCode.isBlank()) {
            mutateBusy(false)
            return pushBanner(UiBanner(UiTone.Warning, "Pilih reason code sebelum resolve discrepancy"))
        }
        when (
            val result = inventoryService.resolveStockCount(
            reviewId = reviewId,
            reasonCode = reasonCode,
            reasonDetail = state.value.inventory.adjustmentReasonDetail.ifBlank { null }
        )
        ) {
            is InventoryActionExecutionResult.Applied -> refreshStage(
                UiBanner(
                    if (result.approvalApplied) UiTone.Warning else UiTone.Success,
                    if (result.approvalApplied) {
                        "Selisih stok diselesaikan setelah verifikasi PIN supervisor."
                    } else {
                        "Discrepancy diselesaikan with adjustment eksplisit."
                    }
                )
            )
            is InventoryActionExecutionResult.ApprovalRequired -> refreshStage(
                UiBanner(UiTone.Warning, result.message)
            )
            is InventoryActionExecutionResult.Blocked -> refreshStage(
                UiBanner(UiTone.Danger, result.message)
            )
        }
    }

    suspend fun markInventoryDiscrepancyInvestigation(reviewId: String) {
        mutateBusy(true)
        val note = state.value.inventory.adjustmentReasonDetail.ifBlank {
            "Discrepancy perlu investigasi manual lebih lanjut."
        }
        val result = inventoryService.markDiscrepancyForInvestigation(reviewId, note)
        refreshStage(
            result.fold(
                onSuccess = { UiBanner(UiTone.Warning, "Discrepancy ditandai investigasi manual.") },
                onFailure = { UiBanner(UiTone.Danger, it.message ?: "Gagal menandai discrepancy") }
            )
        )
    }

    suspend fun approveInventoryAction(actionId: String) {
        val direct = accessService.requireCapability(AccessCapability.APPROVE_STOCK_ADJUSTMENT).getOrNull()
        if (direct == null) {
            return openStepUpFlow(
                title = "Approval Inventory",
                detail = "Supervisor/owner perlu memasukkan PIN untuk menerapkan mutasi stok ini.",
                capability = AccessCapability.APPROVE_STOCK_ADJUSTMENT,
                action = StepUpAction.APPROVE_INVENTORY_ACTION,
                targetId = actionId,
                decisionNote = "Light approval via desktop step-up"
            )
        }
        mutateBusy(true)
        when (val result = inventoryService.approvePendingAction(actionId, direct)) {
            is InventoryActionExecutionResult.Applied -> refreshStage(
                UiBanner(
                    UiTone.Success,
                    "Approval inventory diterapkan. Balance sekarang ${result.mutation.balance.quantity}."
                )
            )
            is InventoryActionExecutionResult.ApprovalRequired -> refreshStage(
                UiBanner(UiTone.Warning, result.message)
            )
            is InventoryActionExecutionResult.Blocked -> refreshStage(
                UiBanner(UiTone.Danger, result.message)
            )
        }
    }

    suspend fun denyInventoryAction(actionId: String) {
        val direct = accessService.requireCapability(AccessCapability.APPROVE_STOCK_ADJUSTMENT).getOrNull()
        if (direct == null) {
            return openStepUpFlow(
                title = "Tolak Approval Inventory",
                detail = "Supervisor/owner perlu memasukkan PIN untuk menolak approval inventory ini.",
                capability = AccessCapability.APPROVE_STOCK_ADJUSTMENT,
                action = StepUpAction.DENY_INVENTORY_ACTION,
                targetId = actionId,
                decisionNote = "Ditolak dari queue inventory desktop"
            )
        }
        mutateBusy(true)
        val result = inventoryService.denyPendingAction(
            actionId = actionId,
            decisionNote = "Ditolak dari queue inventory desktop",
            approverOverride = direct
        )
        refreshStage(
            result.fold(
                onSuccess = { UiBanner(UiTone.Warning, "Approval inventory ditolak. Tidak ada mutasi stok final.") },
                onFailure = { UiBanner(UiTone.Danger, it.message ?: "Penolakan approval inventory gagal") }
            )
        )
    }

    fun deferInventoryDiscrepancy(reviewId: String) {
        val review = state.value.inventory.unresolvedDiscrepancies.firstOrNull { it.id == reviewId }
        val productLabel = review?.productId ?: reviewId
        pushBanner(UiBanner(UiTone.Info, "Discrepancy $productLabel dibiarkan tetap di review queue untuk tindak lanjut nanti."))
    }

    fun updateStepUpApprover(operatorId: String) {
        _state.update {
            it.copy(
                stepUpAuth = it.stepUpAuth.copy(
                    approverOperatorId = operatorId,
                    error = null
                )
            )
        }
    }

    fun updateStepUpPin(pin: String) {
        _state.update {
            it.copy(
                stepUpAuth = it.stepUpAuth.copy(
                    pin = pin.take(6),
                    error = null
                )
            )
        }
    }

    fun updateStepUpDecisionNote(value: String) {
        _state.update {
            it.copy(
                stepUpAuth = it.stepUpAuth.copy(
                    decisionNote = value,
                    error = null
                )
            )
        }
    }

    fun dismissStepUp() {
        _state.update { it.copy(stepUpAuth = StepUpAuthState()) }
    }

    suspend fun confirmStepUp() {
        val flow = state.value.stepUpAuth
        val capability = flow.capability
            ?: return pushBanner(UiBanner(UiTone.Danger, "Capability step-up tidak tersedia"))
        val approverId = flow.approverOperatorId
            ?: return _state.update { it.copy(stepUpAuth = it.stepUpAuth.copy(error = "Pilih operator approver")) }
        if (flow.pin.length != 6) {
            return _state.update { it.copy(stepUpAuth = it.stepUpAuth.copy(error = "PIN approver harus 6 digit")) }
        }
        mutateBusy(true)
        val approver = accessService.verifyStepUp(
            operatorId = approverId,
            pin = flow.pin,
            capability = capability
        ).getOrElse { error ->
            mutateBusy(false)
            return _state.update { it.copy(stepUpAuth = it.stepUpAuth.copy(error = error.message ?: "Step-up auth gagal")) }
        }
        val targetId = flow.targetId
            ?: return pushBanner(UiBanner(UiTone.Danger, "Target step-up tidak tersedia"))
        dismissStepUp()
        when (flow.action) {
            StepUpAction.APPROVE_CASH_MOVEMENT -> when (val result = cashControlService.approveCashMovement(targetId, approver)) {
                is CashMovementExecutionResult.Recorded -> refreshStage(UiBanner(UiTone.Success, "${result.movement.type.name.replace('_', ' ')} disetujui dan dicatat"))
                is CashMovementExecutionResult.ApprovalRequired -> refreshStage(UiBanner(UiTone.Warning, result.decision.message))
                is CashMovementExecutionResult.Blocked -> refreshStage(UiBanner(UiTone.Danger, result.decision.message))
            }
            StepUpAction.DENY_CASH_MOVEMENT -> {
                val denied = cashControlService.denyCashMovement(targetId, flow.decisionNote, approver)
                refreshStage(
                    UiBanner(
                        tone = if (denied != null) UiTone.Warning else UiTone.Danger,
                        message = if (denied != null) "Approval ${denied.id} ditolak" else "Approval tidak bisa ditolak"
                    )
                )
            }
            StepUpAction.APPROVE_CLOSE_SHIFT -> when (val result = shiftClosingService.approveCloseShift(targetId, approver)) {
                is ShiftCloseExecutionResult.Closed -> refreshStage(UiBanner(UiTone.Success, "Shift ${result.shift.id} ditutup lewat approval"))
                is ShiftCloseExecutionResult.ApprovalRequired -> refreshStage(UiBanner(UiTone.Warning, result.decision.message))
                is ShiftCloseExecutionResult.Blocked -> refreshStage(UiBanner(UiTone.Danger, result.decision.message))
            }
            StepUpAction.DENY_CLOSE_SHIFT -> {
                val denied = shiftClosingService.denyCloseShift(targetId, flow.decisionNote, approver)
                refreshStage(
                    UiBanner(
                        tone = if (denied) UiTone.Warning else UiTone.Danger,
                        message = if (denied) "Approval close shift ditolak" else "Approval close shift tidak bisa ditolak"
                    )
                )
            }
            StepUpAction.APPROVE_INVENTORY_ACTION -> when (val result = inventoryService.approvePendingAction(targetId, approver)) {
                is InventoryActionExecutionResult.Applied -> refreshStage(UiBanner(UiTone.Success, "Approval inventory diterapkan. Balance sekarang ${result.mutation.balance.quantity}."))
                is InventoryActionExecutionResult.ApprovalRequired -> refreshStage(UiBanner(UiTone.Warning, result.message))
                is InventoryActionExecutionResult.Blocked -> refreshStage(UiBanner(UiTone.Danger, result.message))
            }
            StepUpAction.DENY_INVENTORY_ACTION -> {
                val result = inventoryService.denyPendingAction(targetId, flow.decisionNote, approver)
                refreshStage(
                    result.fold(
                        onSuccess = { UiBanner(UiTone.Warning, "Approval inventory ditolak. Tidak ada mutasi stok final.") },
                        onFailure = { UiBanner(UiTone.Danger, it.message ?: "Penolakan approval inventory gagal") }
                    )
                )
            }
            null -> pushBanner(UiBanner(UiTone.Danger, "Aksi step-up belum dipilih"))
        }
    }

    fun dismissBanner() {
        _state.update { it.copy(banner = null) }
    }

    private suspend fun refreshStage(banner: UiBanner? = state.value.banner) {
        val context = accessService.restoreContext()
        updateLoadingState(
            phase = DesktopLoadingPhase.RestoringContext,
            detail = "Membaca status terminal dan operator"
        )
        val businessDay = businessDayService.getActiveBusinessDay()
        val activeShift = shiftService.getActiveShift()
        updateLoadingState(
            phase = DesktopLoadingPhase.CheckingOperations,
            detail = "Memeriksa kesiapan operasional terminal"
        )
        val operationalSnapshot = operationalControlService.buildSnapshot(
            openingCashInput = state.value.operations.openingCashInput,
            openingCashReason = state.value.operations.openingCashReason
        )
        val cashReasonOptions = cashControlService.listReasonCodes(
            state.value.operations.cashMovementType.toReasonCategory()
        ).map { ReasonOption(it.code, it.title) }
        val selectedCashReasonCode = state.value.operations.cashMovementReasonCode
            .takeIf { code -> cashReasonOptions.any { it.code == code } }
            ?: cashReasonOptions.firstOrNull()?.code.orEmpty()
        val closeShiftReasonOptions = shiftClosingService.listVarianceReasonCodes()
            .map { ReasonOption(it.code, it.title) }
        val selectedCloseShiftReasonCode = state.value.operations.closeShiftReasonCode
            .takeIf { code -> closeShiftReasonOptions.any { it.code == code } }
            ?: closeShiftReasonOptions.firstOrNull()?.code.orEmpty()
        val voidReasonOptions = cashControlService.listReasonCodes(ReasonCategory.VOID_SALE)
            .map { ReasonOption(it.code, it.title) }
        val recentSales = loadRecentSaleHistory()
        val selectedVoidSaleId = state.value.operations.voidSale.selectedSaleId
            ?.takeIf { selectedId -> recentSales.any { it.saleId == selectedId } }
            ?: recentSales.firstOrNull { it.saleStatus == SaleStatus.COMPLETED }?.saleId
            ?: recentSales.firstOrNull()?.saleId
        val selectedVoidReasonCode = state.value.operations.voidSale.reasonCode
            .takeIf { code -> voidReasonOptions.any { it.code == code } }
            ?: voidReasonOptions.firstOrNull()?.code.orEmpty()
        val closeShiftReview = shiftClosingService.reviewCloseShift(
            state.value.operations.closingCashInput.toDoubleOrNull()
        )
        val pendingApprovals = cashControlService.listPendingApprovals() + shiftClosingService.listPendingApprovals()
        val activeOperator = context.activeOperator
        updateLoadingState(
            phase = DesktopLoadingPhase.CheckingIdentity,
            detail = "Memeriksa identitas usaha dan template struk"
        )
        val draftFromState = state.value.storeProfile.toDraft()
        val storeProfileDraft = context.terminalBinding?.let {
            storeProfileService.loadDraft().getOrNull()
        } ?: draftFromState
        val storeProfileValidation = storeProfileService.validate(storeProfileDraft)
        val validatedStoreProfile = run {
            val draft = storeProfileDraft
            val previous = state.value.storeProfile
            applyStoreProfileValidation(
                previous.copy(
                    businessName = draft.businessName,
                    address = draft.address,
                    streetAddress = draft.streetAddress,
                    neighborhood = draft.neighborhood,
                    village = draft.village,
                    district = draft.district,
                    city = draft.city,
                    province = draft.province,
                    postalCode = draft.postalCode,
                    phoneCountryCode = draft.phoneCountryCode,
                    phoneNumber = draft.phoneNumber,
                    businessEmail = draft.businessEmail,
                    legalId = draft.legalId,
                    receiptNote = draft.receiptNote,
                    logoPath = draft.logoPath,
                    showLogoOnReceipt = draft.showLogoOnReceipt,
                    showAddressOnReceipt = draft.showAddressOnReceipt,
                    showPhoneOnReceipt = draft.showPhoneOnReceipt
                )
            )
        }
        val requiresIdentityCompletion = context.terminalBinding != null &&
            context.operators.isNotEmpty() &&
            !storeProfileValidation.isValid
        val operators = context.operators.map {
            OperatorOption(
                id = it.id,
                displayName = it.displayName,
                roleLabel = it.role.name,
                avatarPath = it.avatarPath,
                capabilitySummary = when (it.role) {
                    OperatorRole.CASHIER -> "Menjalankan transaksi, buka hari bisnis, buka shift, dan kontrol kas harian"
                    OperatorRole.SUPERVISOR -> "Memantau hasil, memberi approval penting, dan menangani pemulihan bila ada masalah"
                    OperatorRole.OWNER -> "Akses penuh"
                }
            )
        }
        val catalogProducts = if (operationalSnapshot.canAccessSalesHome) {
            loadProducts(state.value.catalog.searchQuery)
        } else {
            emptyList()
        }
        val inventoryProducts = if (operationalSnapshot.canAccessSalesHome) {
            productRepository.getAllProducts()
        } else {
            emptyList()
        }

        val availableWorkspaces = availableWorkspacesFor(
            role = activeOperator?.role,
            canAccessSalesHome = operationalSnapshot.canAccessSalesHome
        )
        val activeWorkspace = state.value.activeWorkspace
            .takeIf { it in availableWorkspaces }
            ?: DesktopWorkspace.Dashboard
        val stage = when {
            context.terminalBinding == null || context.operators.isEmpty() -> DesktopStage.Bootstrap
            requiresIdentityCompletion -> DesktopStage.Bootstrap
            context.activeSession == null -> DesktopStage.Login
            else -> DesktopStage.Workspace
        }
        val bootstrapMode = if (context.terminalBinding == null || context.operators.isEmpty()) {
            BootstrapMode.FullSetup
        } else {
            BootstrapMode.CompleteIdentity
        }

        // R5 Reporting Aggregation
        val reportingState = businessDay?.let { reportingQueryFacade.getDailySummary(it.id) }
        val reportingShiftState = when {
            activeShift != null -> reportingQueryFacade.getShiftSummary(activeShift.id)
            businessDay != null -> reportingQueryFacade.getLatestShiftSummary(businessDay.id)
            else -> null
        }
        val voidAssessment = selectedVoidSaleId?.let { saleId ->
            voidSaleService.assessVoid(
                saleId = saleId,
                inventoryFollowUpNote = state.value.operations.voidSale.inventoryFollowUpNote
            ).getOrNull()
        }

        var resolvedBanner = banner
        if (operationalSnapshot.canAccessSalesHome) {
            val recoveredCount = salesService.recoverIncompleteFinalizations().getOrDefault(0)
            if (recoveredCount > 0) {
                resolvedBanner = UiBanner(
                    UiTone.Info,
                    "Finalisasi transaksi tertunda berhasil dipulihkan tanpa efek ganda"
                )
            }
        }
        val refreshedBasket = salesService.basket.value
        val refreshedCashQuote = salesService.quoteCashTender(
            state.value.catalog.cashReceivedInput.toDoubleOrNull() ?: 0.0
        ).getOrNull()
        val refreshedInventory = if (stage == DesktopStage.Workspace) {
            buildInventoryState(
                previous = state.value.inventory,
                products = inventoryProducts
            )
        } else {
            InventoryPanelState()
        }
        val refreshedMasterData = if (stage == DesktopStage.Workspace) {
            buildMasterDataState(
                previous = state.value.masterData
            )
        } else {
            MasterDataPanelState()
        }

        _state.update {
            it.copy(
                isBusy = false,
                stage = stage,
                activeWorkspace = activeWorkspace,
                shell = DesktopShellState(
                    storeId = context.terminalBinding?.storeId,
                    storeName = validatedStoreProfile.businessName.ifBlank { context.terminalBinding?.storeName.orEmpty() }
                        .ifBlank { context.terminalBinding?.storeName },
                    terminalName = context.terminalBinding?.terminalName,
                    operatorName = activeOperator?.displayName,
                    roleLabel = activeOperator?.role?.name,
                    dayStatus = businessDay?.status ?: "CLOSED",
                    shiftStatus = activeShift?.status ?: "LOCKED",
                    nextActionLabel = operationalSnapshot.primaryAction?.toUiLabel(),
                    workspaceTitle = resolveWorkspaceTitle(activeWorkspace, it.inventoryRoute, it.operationsRoute),
                    availableWorkspaces = availableWorkspaces
                ),
                login = it.login.copy(
                    operators = operators,
                    selectedOperatorId = it.login.selectedOperatorId ?: operators.firstOrNull()?.id,
                    pin = if (stage == DesktopStage.Login) it.login.pin else "",
                    feedback = if (stage == DesktopStage.Login) it.login.feedback else null
                ),
                bootstrap = if (stage == DesktopStage.Bootstrap) {
                    if (bootstrapMode == BootstrapMode.FullSetup) {
                        applyBootstrapValidation(it.bootstrap)
                    } else {
                        it.bootstrap.copy(fieldErrors = emptyMap(), submitAttempted = false)
                    }
                } else {
                    BootstrapState()
                },
                storeProfile = if (stage == DesktopStage.Workspace || stage == DesktopStage.Bootstrap) {
                    validatedStoreProfile
                } else {
                    StoreProfileState()
                },
                bootstrapMode = bootstrapMode,
                loading = DesktopLoadingState(
                    phase = DesktopLoadingPhase.Ready,
                    title = "Siap dipakai",
                    detail = "Shell desktop sudah siap dipakai."
                ),
                operations = OperationsState(
                    canOpenDay = activeOperator?.role?.supports(AccessCapability.OPEN_DAY) == true,
                    blockingMessage = when {
                        context.activeSession == null -> "Login operator diperlukan"
                        businessDay == null && activeOperator?.role?.supports(AccessCapability.OPEN_DAY) != true ->
                            "Operator aktif belum bisa membuka hari bisnis"
                        else -> operationalSnapshot.salesHomeBlocker
                    },
                    businessDayLabel = operationalSnapshot.businessDayId,
                    shiftLabel = operationalSnapshot.shiftId,
                    openingCashInput = it.operations.openingCashInput,
                    closingCashInput = it.operations.closingCashInput,
                    openingCashReason = it.operations.openingCashReason,
                    cashMovementType = it.operations.cashMovementType,
                    cashMovementAmountInput = it.operations.cashMovementAmountInput,
                    cashMovementReasonCode = selectedCashReasonCode,
                    cashMovementReasonDetail = it.operations.cashMovementReasonDetail,
                    cashMovementReasonOptions = cashReasonOptions,
                    closeShiftReasonCode = selectedCloseShiftReasonCode,
                    closeShiftReasonDetail = it.operations.closeShiftReasonDetail,
                    closeShiftReasonOptions = closeShiftReasonOptions,
                    closeShiftReview = closeShiftReview,
                    pendingApprovals = pendingApprovals,
                    dashboard = operationalSnapshot,
                    reportingSummary = reportingState,
                    reportingShiftSummary = reportingShiftState,
                    voidSale = VoidSaleState(
                        selectedSaleId = selectedVoidSaleId,
                        reasonCode = selectedVoidReasonCode,
                        reasonDetail = it.operations.voidSale.reasonDetail,
                        inventoryFollowUpNote = it.operations.voidSale.inventoryFollowUpNote,
                        reasonOptions = voidReasonOptions,
                        assessmentMessage = voidAssessment?.message
                            ?: "Pilih transaksi final untuk review void.",
                        inventoryImpactClassification = voidAssessment?.inventoryImpactClassification ?: "NOT_READY",
                        canExecute = voidAssessment?.isEligible == true,
                        selectedLocalNumber = voidAssessment?.localNumber,
                        selectedPaymentMethod = voidAssessment?.paymentMethod,
                        selectedAmount = voidAssessment?.originalAmount,
                        selectedSaleStatus = voidAssessment?.saleStatus?.name
                    ),
                    reportingExportPath = it.operations.reportingExportPath,
                    reportingExportRuleNote = it.operations.reportingExportRuleNote.ifBlank {
                        "Export mengikuti snapshot lokal desktop. Data yang keluar harus dibaca sebagai operational truth terminal saat ini."
                    },
                    reportingExportedAt = it.operations.reportingExportedAt
                ),
                catalog = it.catalog.copy(
                    products = catalogProducts,
                    basket = refreshedBasket,
                    recentSales = recentSales,
                    cashTenderQuote = refreshedCashQuote
                ),
                inventory = refreshedInventory,
                masterData = refreshedMasterData,
                hardware = hardwarePort.getSnapshot(),
                banner = resolvedBanner
            )
        }
    }

    private suspend fun buildInventoryState(
        previous: InventoryPanelState,
        products: List<Product>
    ): InventoryPanelState {
        val reasonOptions = inventoryService.listReasonCodes().map { ReasonOption(it.code, it.title) }
        val selectedReasonCode = previous.adjustmentReasonCode
            .takeIf { code -> reasonOptions.any { it.code == code } }
            ?: reasonOptions.firstOrNull()?.code.orEmpty()
        val selectedProductId = previous.selectedProductId
            ?.takeIf { selectedId -> products.any { it.id == selectedId } }
            ?: previous.unresolvedDiscrepancies.firstOrNull()?.productId
            ?: products.firstOrNull()?.id
        val selectedProduct = products.firstOrNull { it.id == selectedProductId }
        val selectedReadback = selectedProductId?.let {
            inventoryService.getInventoryReadback(it).getOrNull()
        }
        val unresolved = inventoryService.listUnresolvedDiscrepancies()
        val pendingApprovalActions = inventoryService.listPendingApprovalActions()
        val imageResolution = selectedProduct?.let(::resolveProductImage)
            ?: ProductImageResolution(
                ref = null,
                status = "Folder gambar lokal aktif. Pilih produk untuk melihat kecocokan file."
            )
        return previous.copy(
            availableProducts = products,
            selectedProductId = selectedProductId,
            selectedReadback = selectedReadback,
            unresolvedDiscrepancies = unresolved,
            pendingApprovalActions = pendingApprovalActions,
            adjustmentReasonOptions = reasonOptions,
            adjustmentReasonCode = selectedReasonCode,
            inputImagesFolder = File("input_images").absolutePath,
            selectedImageRef = imageResolution.ref,
            imageIoStatus = imageResolution.status,
            voidContractNote = "Pembatalan inventori masih perlu investigasi manual bila kasusnya ambigu.",
            approvalLimitationNote = "Persetujuan inventori saat ini memakai verifikasi PIN supervisor."
        )
    }

    private suspend fun buildMasterDataState(previous: MasterDataPanelState): MasterDataPanelState {
        val categories = productRepository.getAllCategories()
        val selectedCategoryId = previous.selectedCategoryId
            ?.takeIf { categoryId -> categories.any { it.id == categoryId } }
        val products = when {
            previous.searchQuery.isNotBlank() -> productRepository.searchProducts(previous.searchQuery)
            !selectedCategoryId.isNullOrBlank() -> productRepository.getProductsByCategory(selectedCategoryId)
            else -> productRepository.getAllProducts()
        }
        val selectedProductId = previous.selectedProductId
            ?.takeIf { productId -> products.any { it.id == productId } }
            ?: products.firstOrNull()?.id
        val selectedProduct = if (selectedProductId != null) {
            productRepository.getProductById(selectedProductId)
        } else {
            null
        }
        val barcodes = if (selectedProductId != null) {
            productRepository.getBarcodesByProduct(selectedProductId)
        } else {
            emptyList()
        }
        val categoryCounts = productRepository.getAllProducts()
            .groupingBy { it.categoryId }
            .eachCount()
        return previous.copy(
            categories = categories.map { category ->
                MasterCategorySummary(
                    id = category.id,
                    name = category.name,
                    color = category.color,
                    productCount = categoryCounts[category.id] ?: 0
                )
            },
            selectedCategoryId = selectedCategoryId,
            selectedProductId = selectedProductId,
            products = products,
            productNameInput = selectedProduct?.name ?: previous.productNameInput,
            productSkuInput = selectedProduct?.sku ?: previous.productSkuInput,
            productPriceInput = selectedProduct?.price?.toInt()?.toString() ?: previous.productPriceInput,
            productCategoryId = selectedProduct?.categoryId ?: previous.productCategoryId.takeIf { it.isNotBlank() }
                ?: categories.firstOrNull()?.id.orEmpty(),
            productImageRefInput = selectedProduct?.imageUrl.orEmpty(),
            productIsActive = selectedProduct?.isActive ?: previous.productIsActive,
            barcodes = barcodes
        )
    }

    private fun resolveWorkspaceTitle(
        workspace: DesktopWorkspace,
        inventoryRoute: DesktopInventoryRoute,
        operationsRoute: DesktopOperationsRoute
    ): String = when (workspace) {
        DesktopWorkspace.Inventory -> "${workspace.title} / ${inventoryRoute.title}"
        DesktopWorkspace.Operations -> "${workspace.title} / ${operationsRoute.title}"
        else -> workspace.title
    }

    private fun openStepUpFlow(
        title: String,
        detail: String,
        capability: AccessCapability,
        action: StepUpAction,
        targetId: String,
        decisionNote: String
    ) {
        val approvers = state.value.login.operators.filter { option ->
            runCatching { OperatorRole.valueOf(option.roleLabel).supports(capability) }.getOrDefault(false)
        }
        _state.update {
            it.copy(
                stepUpAuth = StepUpAuthState(
                    isVisible = true,
                    title = title,
                    detail = detail,
                    capability = capability,
                    targetId = targetId,
                    action = action,
                    approverOptions = approvers,
                    approverOperatorId = approvers.firstOrNull()?.id,
                    decisionNote = decisionNote
                ),
                banner = UiBanner(UiTone.Warning, "Step-up auth diperlukan sebelum aksi ini diterapkan."),
                isBusy = false
            )
        }
    }

    private fun resolveProductImage(product: Product): ProductImageResolution {
        product.imageUrl?.takeIf(String::isNotBlank)?.let { imageUrl ->
            return ProductImageResolution(
                ref = imageUrl,
                status = "Referensi gambar lama masih dipakai. Folder gambar lokal tersedia sebagai jalur aman berikutnya."
            )
        }
        val inputDir = File("input_images")
        if (!inputDir.exists()) {
            return ProductImageResolution(
                ref = null,
                status = "Folder gambar lokal belum ada di runtime desktop ini."
            )
        }
        val matched = inputDir.walkTopDown()
            .filter { it.isFile }
            .firstOrNull { file ->
                val baseName = file.nameWithoutExtension
                baseName.equals(product.id, ignoreCase = true) || baseName.equals(product.sku, ignoreCase = true)
            }
        return if (matched != null) {
            ProductImageResolution(
                ref = matched.relativeTo(File(".")).path,
                status = "Folder gambar lokal dipakai sebagai sumber file untuk ${product.sku}."
            )
        } else {
            ProductImageResolution(
                ref = null,
                status = "Folder gambar lokal aktif, tetapi belum ada file yang cocok untuk ${product.sku}."
            )
        }
    }

    private suspend fun refreshCatalog() {
        val products = loadProducts(state.value.catalog.searchQuery)
        _state.update { it.copy(catalog = it.catalog.copy(products = products)) }
    }

    private suspend fun loadProducts(query: String): List<Product> {
        return if (query.isBlank()) {
            productRepository.getAllProducts()
        } else {
            productRepository.searchProducts(query)
        }
    }

    private fun mutateBusy(value: Boolean) {
        _state.update { it.copy(isBusy = value) }
    }

    private fun updateLoadingState(
        phase: DesktopLoadingPhase,
        detail: String = phase.title
    ) {
        _state.update {
            it.copy(
                loading = DesktopLoadingState(
                    phase = phase,
                    title = phase.title,
                    detail = detail,
                    progress = phase.progress
                )
            )
        }
    }

    private fun pushBanner(banner: UiBanner) {
        _state.update { it.copy(isBusy = false, banner = banner) }
    }

    private fun applyBootstrapValidation(bootstrap: BootstrapState): BootstrapState {
        val validation = accessService.validateBootstrapRequest(bootstrap.toBootstrapStoreRequest())
        return bootstrap.copy(
            fieldErrors = validation.issues.associate { it.field.toUiField() to it.message }
        )
    }

    private fun applyStoreProfileValidation(storeProfile: StoreProfileState): StoreProfileState {
        val validation = runCatching {
            storeProfileService.validate(storeProfile.toDraft())
        }.getOrElse {
            return storeProfile.copy(
                fieldErrors = emptyMap()
            )
        }
        return storeProfile.copy(
            fieldErrors = validation.issues.associate { it.field.toUiField() to it.message }
        )
    }

    private suspend fun loadRecentSaleHistory(): List<SaleHistoryEntry> {
        return salesService.getRecentSaleHistory(limit = 8).getOrDefault(emptyList())
    }

    private fun currentCashTenderQuote(): CashTenderQuote? {
        return state.value.catalog.cashTenderQuote
    }

    private fun recalculateVoidDraft(
        draft: VoidSaleState,
        recentSales: List<SaleHistoryEntry>
    ): VoidSaleState {
        val selectedSale = recentSales.firstOrNull { it.saleId == draft.selectedSaleId }
            ?: return draft.copy(
                selectedLocalNumber = null,
                selectedPaymentMethod = null,
                selectedAmount = null,
                selectedSaleStatus = null,
                assessmentMessage = "Pilih transaksi final untuk review void.",
                inventoryImpactClassification = "NOT_READY",
                canExecute = false
            )
        val canExecute = selectedSale.saleStatus == SaleStatus.COMPLETED && selectedSale.paymentMethod == "CASH"
        val assessmentMessage = when {
            selectedSale.saleStatus == SaleStatus.VOIDED ->
                "Penjualan ini sudah pernah di-void dan tidak boleh diproses ulang."
            selectedSale.paymentMethod != "CASH" ->
                "Void desktop V1 hanya mengeksekusi penjualan CASH. CARD/QRIS tetap perlu reversal/refund eksternal."
            else ->
                "Pembatalan transaksi tunai siap dijalankan. Refund kas akan dicatat, stok tetap perlu tindak lanjut manual."
        }
        val inventoryImpact = if (draft.inventoryFollowUpNote.isBlank()) {
            "MANUAL_INVESTIGATION_REQUIRED"
        } else {
            "POST_SETTLEMENT_REVERSAL_CANDIDATE"
        }
        return draft.copy(
            selectedLocalNumber = selectedSale.localNumber,
            selectedPaymentMethod = selectedSale.paymentMethod,
            selectedAmount = selectedSale.finalAmount,
            selectedSaleStatus = selectedSale.saleStatus.name,
            assessmentMessage = assessmentMessage,
            inventoryImpactClassification = inventoryImpact,
            canExecute = canExecute
        )
    }
}

data class DesktopAppState(
    val stage: DesktopStage = DesktopStage.Loading,
    val activeWorkspace: DesktopWorkspace = DesktopWorkspace.Dashboard,
    val inventoryRoute: DesktopInventoryRoute = DesktopInventoryRoute.StockOverview,
    val operationsRoute: DesktopOperationsRoute = DesktopOperationsRoute.CashControl,
    val bootstrapMode: BootstrapMode = BootstrapMode.FullSetup,
    val loading: DesktopLoadingState = DesktopLoadingState(),
    val shell: DesktopShellState = DesktopShellState(),
    val bootstrap: BootstrapState = BootstrapState(),
    val login: LoginState = LoginState(),
    val storeProfile: StoreProfileState = StoreProfileState(),
    val operations: OperationsState = OperationsState(),
    val catalog: DesktopCatalogState = DesktopCatalogState(),
    val inventory: InventoryPanelState = InventoryPanelState(),
    val masterData: MasterDataPanelState = MasterDataPanelState(),
    val stepUpAuth: StepUpAuthState = StepUpAuthState(),
    val hardware: CashierHardwareSnapshot = CashierHardwareSnapshot(),
    val banner: UiBanner? = null,
    val isBusy: Boolean = false
)

sealed interface DesktopStage {
    data object Loading : DesktopStage
    data object Bootstrap : DesktopStage
    data object Login : DesktopStage
    data object Workspace : DesktopStage
    data class FatalError(val message: String) : DesktopStage
}

enum class BootstrapMode {
    FullSetup,
    CompleteIdentity
}

data class DesktopLoadingState(
    val phase: DesktopLoadingPhase = DesktopLoadingPhase.PreparingStorage,
    val title: String = "Menyiapkan Cassy",
    val detail: String = "Menunggu startup desktop selesai",
    val progress: Float = 0.1f
)

enum class DesktopLoadingPhase(
    val title: String,
    val progress: Float
) {
    PreparingStorage("Menyiapkan Cassy", 0.16f),
    RestoringContext("Memuat konteks lokal", 0.38f),
    CheckingOperations("Memeriksa kesiapan terminal", 0.64f),
    CheckingIdentity("Memeriksa identitas usaha", 0.82f),
    Ready("Siap dipakai", 1f)
}

data class DesktopShellState(
    val storeId: String? = null,
    val storeName: String? = null,
    val terminalName: String? = null,
    val operatorName: String? = null,
    val roleLabel: String? = null,
    val dayStatus: String = "CLOSED",
    val shiftStatus: String = "LOCKED",
    val nextActionLabel: String? = null,
    val workspaceTitle: String = DesktopWorkspace.Dashboard.title,
    val availableWorkspaces: List<DesktopWorkspace> = listOf(DesktopWorkspace.Dashboard)
)

data class BootstrapState(
    val storeName: String = "",
    val terminalName: String = "",
    val cashierName: String = "",
    val cashierPin: String = "",
    val cashierAvatarPath: String? = null,
    val supervisorName: String = "",
    val supervisorPin: String = "",
    val supervisorAvatarPath: String? = null,
    val fieldErrors: Map<BootstrapField, String> = emptyMap(),
    val touchedFields: Set<BootstrapField> = emptySet(),
    val submitAttempted: Boolean = false
)

enum class BootstrapField {
    StoreName,
    TerminalName,
    CashierName,
    CashierPin,
    CashierAvatar,
    SupervisorName,
    SupervisorPin,
    SupervisorAvatar
}

data class LoginState(
    val operators: List<OperatorOption> = emptyList(),
    val selectedOperatorId: String? = null,
    val pin: String = "",
    val feedback: String? = null
)

data class StoreProfileState(
    val businessName: String = "",
    val address: String = "",
    val streetAddress: String = "",
    val neighborhood: String = "",
    val village: String = "",
    val district: String = "",
    val city: String = "",
    val province: String = "",
    val postalCode: String = "",
    val phoneCountryCode: String = DEFAULT_PHONE_COUNTRY_CODE,
    val phoneNumber: String = "",
    val businessEmail: String = "",
    val legalId: String = "",
    val receiptNote: String = "",
    val logoPath: String? = null,
    val showLogoOnReceipt: Boolean = true,
    val showAddressOnReceipt: Boolean = true,
    val showPhoneOnReceipt: Boolean = true,
    val fieldErrors: Map<StoreProfileUiField, String> = emptyMap(),
    val touchedFields: Set<StoreProfileUiField> = emptySet(),
    val submitAttempted: Boolean = false
)

enum class StoreProfileUiField {
    BusinessName,
    StreetAddress,
    Neighborhood,
    Village,
    District,
    City,
    Province,
    PostalCode,
    PhoneCountryCode,
    PhoneNumber,
    BusinessEmail,
    LegalId,
    ReceiptNote,
    LogoPath
}

enum class StoreProfileToggleField {
    ShowLogoOnReceipt,
    ShowAddressOnReceipt,
    ShowPhoneOnReceipt
}

data class OperatorOption(
    val id: String,
    val displayName: String,
    val roleLabel: String,
    val avatarPath: String? = null,
    val capabilitySummary: String = ""
)

data class OperationsState(
    val canOpenDay: Boolean = false,
    val blockingMessage: String? = null,
    val businessDayLabel: String? = null,
    val shiftLabel: String? = null,
    val openingCashInput: String = "",
    val closingCashInput: String = "",
    val openingCashReason: String = "",
    val cashMovementType: CashMovementType = CashMovementType.CASH_IN,
    val cashMovementAmountInput: String = "",
    val cashMovementReasonCode: String = "",
    val cashMovementReasonDetail: String = "",
    val cashMovementReasonOptions: List<ReasonOption> = emptyList(),
    val closeShiftReasonCode: String = "",
    val closeShiftReasonDetail: String = "",
    val closeShiftReasonOptions: List<ReasonOption> = emptyList(),
    val closeShiftReview: ShiftCloseReview? = null,
    val pendingApprovals: List<PendingApprovalSummary> = emptyList(),
    val dashboard: OperationalControlSnapshot = OperationalControlSnapshot(
        headline = "Operasional belum siap.",
        primaryAction = null,
        canAccessSalesHome = false,
        salesHomeBlocker = "Login operator diperlukan.",
        businessDayId = null,
        shiftId = null,
        pendingApprovalCount = 0,
        decisions = emptyList()
    ),
    val reportingSummary: DailySummary? = null,
    val reportingShiftSummary: ShiftSummary? = null,
    val voidSale: VoidSaleState = VoidSaleState(),
    val reportingExportPath: String? = null,
    val reportingExportRuleNote: String = "Export mengikuti snapshot lokal desktop. Data yang keluar harus dibaca sebagai operational truth terminal saat ini.",
    val reportingExportedAt: kotlinx.datetime.Instant? = null
)

data class VoidSaleState(
    val selectedSaleId: String? = null,
    val selectedLocalNumber: String? = null,
    val selectedPaymentMethod: String? = null,
    val selectedAmount: Double? = null,
    val selectedSaleStatus: String? = null,
    val reasonCode: String = "",
    val reasonDetail: String = "",
    val inventoryFollowUpNote: String = "",
    val reasonOptions: List<ReasonOption> = emptyList(),
    val assessmentMessage: String = "Pilih transaksi final untuk review void.",
    val inventoryImpactClassification: String = "NOT_READY",
    val canExecute: Boolean = false
)

data class ReasonOption(
    val code: String,
    val title: String
)

data class DesktopCatalogState(
    val searchQuery: String = "",
    val barcodeInput: String = "",
    val products: List<Product> = emptyList(),
    val basket: Basket = Basket(),
    val reviewConfirmed: Boolean = false,
    val memberNumberInput: String = "",
    val memberNameInput: String = "",
    val memberSkipped: Boolean = false,
    val donationOffered: Boolean = false,
    val donationSkipped: Boolean = false,
    val donationAmountInput: String = "",
    val cashReceivedInput: String = "",
    val cashTenderQuote: CashTenderQuote? = null,
    val lastFinalizedSaleId: String? = null,
    val lastReceiptPreview: String? = null,
    val recentSales: List<SaleHistoryEntry> = emptyList(),
    val receiptPreview: ReceiptPreviewState = ReceiptPreviewState(),
    val printState: ReceiptPrintState = ReceiptPrintState(
        status = ReceiptPrintStatus.NOT_REQUESTED,
        detailMessage = "Belum ada struk final"
    ),
    val lookupFeedback: LookupFeedback? = null
)

enum class InventoryAdjustmentDirection {
    INCREASE,
    DECREASE
}

data class InventoryPanelState(
    val availableProducts: List<Product> = emptyList(),
    val selectedProductId: String? = null,
    val selectedReadback: InventoryReadback? = null,
    val unresolvedDiscrepancies: List<InventoryDiscrepancyReview> = emptyList(),
    val pendingApprovalActions: List<InventoryApprovalAction> = emptyList(),
    val countQuantityInput: String = "",
    val adjustmentDirection: InventoryAdjustmentDirection = InventoryAdjustmentDirection.INCREASE,
    val adjustmentQuantityInput: String = "",
    val adjustmentReasonCode: String = "",
    val adjustmentReasonDetail: String = "",
    val adjustmentReasonOptions: List<ReasonOption> = emptyList(),
    val inputImagesFolder: String = "input_images",
    val selectedImageRef: String? = null,
    val imageIoStatus: String = "Folder gambar lokal belum dipakai.",
    val voidContractNote: String = "Pembatalan inventori masih perlu review manual.",
    val approvalLimitationNote: String = "Persetujuan inventori memakai verifikasi PIN supervisor."
)

data class MasterDataPanelState(
    val categories: List<MasterCategorySummary> = emptyList(),
    val selectedCategoryId: String? = null,
    val searchQuery: String = "",
    val selectedProductId: String? = null,
    val products: List<Product> = emptyList(),
    val productNameInput: String = "",
    val productSkuInput: String = "",
    val productPriceInput: String = "",
    val productCategoryId: String = "",
    val productImageRefInput: String = "",
    val productIsActive: Boolean = true,
    val barcodeDraft: String = "",
    val barcodeType: String = "GLOBAL",
    val barcodes: List<ProductBarcodeRecord> = emptyList(),
    val newCategoryName: String = "",
    val newCategoryColor: String = "#1F7A8C",
    val groupingHint: String = "Kelompokkan produk berdasarkan kategori yang dipakai operator di lantai toko."
)

data class MasterCategorySummary(
    val id: String,
    val name: String,
    val color: String,
    val productCount: Int
)

data class StepUpAuthState(
    val isVisible: Boolean = false,
    val title: String = "",
    val detail: String = "",
    val capability: AccessCapability? = null,
    val targetId: String? = null,
    val action: StepUpAction? = null,
    val approverOptions: List<OperatorOption> = emptyList(),
    val approverOperatorId: String? = null,
    val pin: String = "",
    val decisionNote: String = "",
    val error: String? = null
)

enum class StepUpAction {
    APPROVE_CASH_MOVEMENT,
    DENY_CASH_MOVEMENT,
    APPROVE_CLOSE_SHIFT,
    DENY_CLOSE_SHIFT,
    APPROVE_INVENTORY_ACTION,
    DENY_INVENTORY_ACTION
}

data class ReceiptPreviewState(
    val saleId: String? = null,
    val localNumber: String? = null,
    val content: String? = null,
    val availabilityMessage: String = "Preview struk final belum tersedia"
)

data class LookupFeedback(
    val tone: UiTone,
    val message: String
)

enum class UiTone {
    Info,
    Success,
    Warning,
    Danger
}

data class UiBanner(
    val tone: UiTone,
    val message: String
)

private data class ProductImageResolution(
    val ref: String?,
    val status: String
)

private fun OperationType.toUiLabel(): String = when (this) {
    OperationType.OPEN_BUSINESS_DAY -> "Buka hari bisnis"
    OperationType.START_SHIFT -> "Buka Shift"
    OperationType.CASH_IN -> "Catat uang masuk"
    OperationType.CASH_OUT -> "Catat uang keluar"
    OperationType.SAFE_DROP -> "Simpan uang ke brankas"
    OperationType.CLOSE_SHIFT -> "Tutup Shift"
    OperationType.CLOSE_BUSINESS_DAY -> "Tutup Hari"
    OperationType.VOID_SALE -> "Review void transaksi"
    OperationType.STOCK_ADJUSTMENT -> "Sesuaikan stok"
    OperationType.RESOLVE_STOCK_DISCREPANCY -> "Tindak lanjuti selisih stok"
}

private fun CashMovementType.toReasonCategory(): ReasonCategory = when (this) {
    CashMovementType.CASH_IN -> ReasonCategory.CASH_IN
    CashMovementType.CASH_OUT -> ReasonCategory.CASH_OUT
    CashMovementType.SAFE_DROP -> ReasonCategory.SAFE_DROP
}

private fun String.toDecimalInput(): String {
    val sanitized = buildString {
        var dotUsed = false
        this@toDecimalInput.forEach { char ->
            when {
                char.isDigit() -> append(char)
                char == '.' && !dotUsed -> {
                    append(char)
                    dotUsed = true
                }
            }
        }
    }
    return if (sanitized.startsWith(".")) "0$sanitized" else sanitized
}

private fun BootstrapState.toBootstrapStoreRequest(): BootstrapStoreRequest {
    return BootstrapStoreRequest(
        storeName = storeName,
        terminalName = terminalName,
        cashierName = cashierName,
        cashierPin = cashierPin,
        supervisorName = supervisorName,
        supervisorPin = supervisorPin,
        cashierAvatarPath = cashierAvatarPath,
        supervisorAvatarPath = supervisorAvatarPath
    )
}

private fun BootstrapStoreField.toUiField(): BootstrapField = when (this) {
    BootstrapStoreField.STORE_NAME -> BootstrapField.StoreName
    BootstrapStoreField.TERMINAL_NAME -> BootstrapField.TerminalName
    BootstrapStoreField.CASHIER_NAME -> BootstrapField.CashierName
    BootstrapStoreField.CASHIER_PIN -> BootstrapField.CashierPin
    BootstrapStoreField.CASHIER_AVATAR -> BootstrapField.CashierAvatar
    BootstrapStoreField.SUPERVISOR_NAME -> BootstrapField.SupervisorName
    BootstrapStoreField.SUPERVISOR_PIN -> BootstrapField.SupervisorPin
    BootstrapStoreField.SUPERVISOR_AVATAR -> BootstrapField.SupervisorAvatar
}

private fun StoreProfileField.toUiField(): StoreProfileUiField = when (this) {
    StoreProfileField.BUSINESS_NAME -> StoreProfileUiField.BusinessName
    StoreProfileField.STREET_ADDRESS -> StoreProfileUiField.StreetAddress
    StoreProfileField.NEIGHBORHOOD -> StoreProfileUiField.Neighborhood
    StoreProfileField.VILLAGE -> StoreProfileUiField.Village
    StoreProfileField.DISTRICT -> StoreProfileUiField.District
    StoreProfileField.CITY -> StoreProfileUiField.City
    StoreProfileField.PROVINCE -> StoreProfileUiField.Province
    StoreProfileField.POSTAL_CODE -> StoreProfileUiField.PostalCode
    StoreProfileField.PHONE_COUNTRY_CODE -> StoreProfileUiField.PhoneCountryCode
    StoreProfileField.PHONE_NUMBER -> StoreProfileUiField.PhoneNumber
    StoreProfileField.BUSINESS_EMAIL -> StoreProfileUiField.BusinessEmail
    StoreProfileField.LEGAL_ID -> StoreProfileUiField.LegalId
    StoreProfileField.RECEIPT_NOTE -> StoreProfileUiField.ReceiptNote
    StoreProfileField.LOGO_PATH -> StoreProfileUiField.LogoPath
}

private fun StoreProfileState.toDraft(): StoreProfileDraft {
    return StoreProfileDraft(
        businessName = businessName,
        address = address,
        streetAddress = streetAddress,
        neighborhood = neighborhood,
        village = village,
        district = district,
        city = city,
        province = province,
        postalCode = postalCode,
        phoneCountryCode = phoneCountryCode,
        phoneNumber = phoneNumber,
        businessEmail = businessEmail,
        legalId = legalId,
        receiptNote = receiptNote,
        logoPath = logoPath,
        showLogoOnReceipt = showLogoOnReceipt,
        showAddressOnReceipt = showAddressOnReceipt,
        showPhoneOnReceipt = showPhoneOnReceipt
    )
}
