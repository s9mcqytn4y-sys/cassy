# Cassy

Desktop-first retail operating core untuk single outlet. Fokus V1 saat ini adalah cashier core yang usable, guided operations yang jujur, dan inventory basic yang explainable.

## Repo Truth & Milestone Status (2026-03-19)

Status milestone di bawah ini didasarkan pada bukti nyata di dalam repository (code, unit tests, dan manual smoke evidence).

- **M0 (Setup):** **DONE** (Control plane & Agent context stabil)
- **M1 (Scope):** **DONE** (V1 functional scope terkunci)
- **M2 (Arch):** **DONE** (Kotlin 2.3.20, Multi-module Gradle, Build Logic stabil)
- **M3 (Bootstrap):** **DONE** (Desktop branding, Login, & State restore terverifikasi)
- **M4 (Ops):** **DONE** (Business Day & Shift lifecycle guardrails terverifikasi)
- **M5 (Catalog/Cart):** **DONE (Thin)** (Lookup barcode/SKU & Basket persistence stabil)
- **M6 (Checkout & Receipt Finality / R1):** **DONE** (desktop-first lane)
- **R2 (Operational Control):** **DONE** (desktop-first operational slice)
- **M7 / R3 (Inventory Truth Lite):** **DONE** (desktop-first hardened slice)

**Milestone berikutnya yang masih terbuka:** sync visibility breadth, hosted release evidence depth, dan future solver gaps yang memang belum di-scope.

## Verifikasi & Evidence Lane

Dokumentasi detail mengenai status dan cara verifikasi:
- `docs/execution/roadmap_bridge.md`: **Source of Truth** status milestone saat ini.
- `docs/execution/r4_windows_release_contract.md`: Kontrak foundation slice R4.
- `docs/execution/r4_jdk_workspace_truth.md`: Truth JDK/toolchain/workspace desktop.
- `docs/execution/r4_smoke_foundation.md`: Task map packaging + smoke path yang nyata.
- `docs/execution/windows_installer_smoke_checklist.md`: Panduan verifikasi manual installer Windows.
- `docs/execution/windows_desktop_runbook.md`: Langkah operasional untuk environment Desktop.
- `docs/execution/r4_windows_release_trust.md`: Status R4, baseline recovery, dan diagnostics Windows.

## Struktur Modul Utama

- `apps/desktop-pos`: Shell utama kasir (Windows/Desktop-first).
- `shared:kernel`: Inti operasional (Access, Business Day, Shift).
- `shared:masterdata`: Manajemen katalog dan pencarian produk (Barcode/SKU).
- `shared:sales`: Logika keranjang belanja (Basket) dan perhitungan harga baseline.
- `shared:inventory`: Pengelolaan stok dan ledger transaksi inventaris.

## Quick Start Verification

Jalankan perintah berikut untuk memverifikasi kesehatan repository secara lokal:

```powershell
# Jalankan gate desktop-first R1/R2/R3 yang bermakna
.\gradlew :shared:kernel:allTests :shared:sales:desktopTest :shared:inventory:desktopTest :shared:inventory:verifyCommonMainInventoryDatabaseMigration :apps:desktop-pos:test :apps:desktop-pos:smokeRun

# Jalankan build/test/lint repo
.\gradlew build
.\gradlew test
.\gradlew detekt
.\gradlew :apps:android-pos:lintDebug

# Build distribusi Windows lokal
.\gradlew :apps:desktop-pos:createDistributable :apps:desktop-pos:packageDistributionForCurrentOS

# Smoke distribution + kumpulkan diagnostics
powershell -ExecutionPolicy Bypass -File tooling/scripts/Invoke-DesktopDistributionSmoke.ps1
powershell -ExecutionPolicy Bypass -File tooling/scripts/Collect-WindowsReleaseDiagnostics.ps1

# Backup state lokal sebelum install/update candidate
powershell -ExecutionPolicy Bypass -File tooling/scripts/Backup-CassyDesktopState.ps1
```

## Catatan Penting
- **JDK 17** adalah standar wajib untuk pengembangan Desktop.
- **Active Basket Persistence:** M5 kini mendukung penyimpanan keranjang otomatis; jika aplikasi ditutup paksa, isi keranjang akan kembali saat dibuka (Survival on Restart).
- **Solver honesty:** `LIGHT_PIN` only, `PDF_NOT_SHIPPED`, dan Windows installer install/uninstall full evidence tetap belum boleh dioverclaim.

---
*Lihat `.agent/plan.md` untuk rencana eksekusi teknis mendalam.*
