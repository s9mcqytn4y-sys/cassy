# Cassy

Desktop-first retail operating core untuk single outlet. Fokus V1 saat ini adalah cashier core yang usable, guided operations yang jujur, dan inventory basic yang explainable.

Versi repo saat ini: `0.2.0-beta.3` dengan posture `Private Beta / Controlled Beta`.

## Repo Truth & Milestone Status (2026-03-27)

Status milestone di bawah ini didasarkan pada bukti nyata di dalam repository (code, unit tests, dan manual smoke evidence).

- **M0 (Setup):** **DONE** (Control plane & Agent context stabil)
- **M1 (Scope):** **DONE** (V1 functional scope terkunci)
- **M2 (Arch):** **DONE** (Kotlin 2.2.10, Multi-module Gradle, Build Logic stabil)
- **M3 (Bootstrap):** **DONE** (Desktop branding, Login, & State restore terverifikasi)
- **M4 (Ops):** **DONE** (Business Day & Shift lifecycle guardrails terverifikasi)
- **M5 (Catalog/Cart):** **DONE (Thin)** (Lookup barcode/SKU & Basket persistence stabil)
- **M6 (Checkout & Receipt Finality / R1):** **DONE** (desktop-first lane)
- **R2 (Operational Control):** **DONE** (desktop-first operational slice)
- **M7 / R3 (Inventory Truth Lite):** **DONE** (desktop-first hardened slice)
- **M8 / R5 (Visibility & Reporting Lite):** **DONE / HARDENED**
- **M9 / R6 (Sync-Ready Boundary & Replay):** **DONE** untuk definisi local-boundary Cassy V1
- **M10 / R4 (Windows Release Trust):** **DONE** untuk lane lokal/repo dengan evidence hosted yang sudah hidup

**Fokus berikutnya yang masih terbuka:** burn-in beta terbatas, hosted beta tag/release evidence, code-signing posture, dan profiler-backed performance/resource proof yang lebih dalam.

## Verifikasi & Evidence Lane

Dokumentasi detail mengenai status dan cara verifikasi:
- `docs/execution/roadmap_bridge.md`: **Source of Truth** status milestone saat ini.
- `docs/execution/repo_reality_sync.md`: Snapshot repo truth yang terbaru.
- `docs/execution/windows_installer_smoke_checklist.md`: Panduan verifikasi manual installer Windows.
- `docs/execution/windows_desktop_runbook.md`: Langkah operasional untuk environment Desktop.
- `docs/execution/release_candidate_checklist.md`: Definisi release candidate yang berlaku.
- `docs/execution/beta_burn_in_checklist.md`: Checklist burn-in beta.
- `docs/execution/beta_release_readiness.md`: Status kesiapan beta release.
- `docs/execution/windows_code_signing_posture.md`: Posture code signing Windows yang jujur.
- `docs/execution/desktop_device_support_matrix.md`: Authority dukungan device/peripheral desktop.
- `docs/user/operator_quickstart.md`: Panduan singkat operator, shortcut, dan step-up approval mini-flow.
- `docs/user/daily_operations_guide.md`: Panduan operasi harian outlet.
- `LICENSE`, `EULA.md`, `PRIVACY.md`, `SECURITY.md`, `THIRD_PARTY_NOTICES.md`: baseline legal/privacy/security.

## Struktur Modul Utama

- `apps/desktop-pos`: Shell utama kasir (Windows/Desktop-first).
- `shared:kernel`: Inti operasional (Access, Business Day, Shift).
- `shared:masterdata`: Manajemen katalog dan pencarian produk (Barcode/SKU).
- `shared:sales`: Logika keranjang belanja (Basket) dan perhitungan harga baseline.
- `shared:inventory`: Pengelolaan stok dan ledger transaksi inventaris.

## Quick Start Verification

Jalankan perintah berikut untuk memverifikasi kesehatan repository secara lokal:

```powershell
# Jalankan gate repo utama
.\gradlew --version
.\gradlew clean
.\gradlew build
.\gradlew test
.\gradlew lint detekt
.\gradlew :shared:kernel:verifyCommonMainKernelDatabaseMigration :shared:inventory:verifyCommonMainInventoryDatabaseMigration :shared:masterdata:verifyCommonMainMasterDataDatabaseMigration :shared:sales:verifyCommonMainSalesDatabaseMigration

# Gate desktop-first utama
.\gradlew :apps:desktop-pos:createDistributable :apps:desktop-pos:packageExe :apps:desktop-pos:packageMsi :apps:desktop-pos:smokeRun

# Smoke distribution + kumpulkan diagnostics
powershell -ExecutionPolicy Bypass -File tooling/scripts/Invoke-DesktopDistributionSmoke.ps1
powershell -ExecutionPolicy Bypass -File tooling/scripts/Invoke-WindowsInstallerEvidence.ps1
powershell -ExecutionPolicy Bypass -File tooling/scripts/Invoke-WindowsUpgradeEvidence.ps1
powershell -ExecutionPolicy Bypass -File tooling/scripts/Invoke-DesktopPerformanceProbe.ps1
powershell -ExecutionPolicy Bypass -File tooling/scripts/Collect-WindowsReleaseDiagnostics.ps1

# Backup state lokal sebelum install/update candidate
powershell -ExecutionPolicy Bypass -File tooling/scripts/Backup-CassyDesktopState.ps1
```

## Catatan Penting
- **JDK 17** adalah standar wajib untuk pengembangan Desktop.
- **Windows release lane:** repo saat ini mengandalkan `EXE` + `MSI`, per-user install, scripted installer evidence, dan diagnostics baseline.
- **Beta release lane:** repo sekarang juga punya changelog, release manifest, workflow `Beta Release`, dan checklist burn-in.
- **Active Basket Persistence:** keranjang otomatis disimpan ke database lokal dan dipulihkan saat restart paksa.
- **Solver honesty:** breadth hardware, backend sync nyata, dan profiler evidence belum boleh dioverclaim.

---
*Lihat `.agent/plan.md` untuk rencana eksekusi teknis mendalam.*
