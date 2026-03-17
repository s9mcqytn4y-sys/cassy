# Cassy Roadmap Execution Bridge

Dokumen ini adalah bridge antara roadmap PDF, context agent, dan repo reality. Repo diperlakukan sebagai implementation snapshot; status milestone hanya boleh naik bila ada evidence kode, test, dan verifikasi yang nyata.

## Status Ringkas per 2026-03-20

| ID | Milestone | Status Repo Jujur | Evidence Utama | Catatan |
|:---|:---|:---|:---|:---|
| M0 | Program Setup | **DONE** | `AGENTS.md`, `CODEX.md`, `.agent/` | Struktur kendali dasar telah stabil. |
| M1 | Scope Lock V1 | **DONE** | `.agent/context/project_overview.md` | Fokus Desktop-first retail core terkunci. |
| M2 | Arch Control Plane | **DONE** | Kotlin 2.3.20, Multi-module Gradle | Build logic dan dependency management (libs.versions.toml) sudah modern & sinkron. |
| M3 | Desktop Bootstrap | **DONE** | `Main.kt` branding, `DesktopAppControllerTest` | Flow Login, Restore, dan Lockout telah terverifikasi. |
| M4 | Business Day & Shift | **DONE** | `ShiftService`, Guardrail Lifecycle | Lifecycle Open/Start/End/Close tervalidasi dengan guardrail operasional. |
| M5 | Thin M5 (Catalog/Cart) | **DONE (BASELINES)** | `SalesService`, `ActiveBasket` persistence | Catalog lookup, cart mutation, dan basket persistence (survival on restart) sudah stabil. |
| M6 | Checkout & Receipt | **DONE (DESKTOP-FIRST R1)** | `SalesService`, `SalesRepository`, `DesktopAppController`, sales DB migrations | Finality transaksi, snapshot struk final, readback/reprint dari source final, complete-sale facade, payment callback/idempotency guard, durable finalization bundle, crash/replay proof, cash tender helper, preview/print status, dan separation print vs settlement validity sudah tervalidasi untuk lane desktop. |
| M7 | Inventory Basic | **DONE (THIN)** | `InventoryService`, `recordSaleCompletion` | Integrasi transaksi stok otomatis saat checkout baseline sudah aktif. |
| M8 | Reporting Dasar | **PENDING** | - | Belum ada implementasi runtime atau UI reporting. |
| M9 | Sync Visibility | **PENDING** | outbox/infra parsial | Replay mechanism dan sync state visibility belum di-close. |
| M10 | Release (Windows) | **FOUNDATION-OK** | `packageExe` task, Manual Evidence Pack | Installer lokal tervalidasi manual, menunggu stabilisasi CI hosted runner. |

## Bukti Verifikasi (Evidence Matrix)

### 1. Local Evidence (Unit/Integration Tests)
- `.\gradlew :shared:kernel:desktopTest` -> Lifecycle Shift & Access (PASSED)
- `.\gradlew :shared:sales:commonTest` -> Cart Logic & Persistence (PASSED)
- `.\gradlew :shared:inventory:commonTest` -> Stock Transaction Invariants (PASSED)

### 2. Manual Evidence (Smoke Test)
- **Windows Installer:** `Cassy-0.1.0.exe` berhasil install/uninstall dan launch (Lihat `docs/execution/windows_installer_smoke_checklist.md`).
- **Product Lookup:** Barcode entry di UI Desktop berhasil menarik data `shared:masterdata`.
- **Basket Persistence:** Menutup aplikasi saat basket terisi, lalu membukanya kembali: basket ter-restore otomatis (M5 baseline).

### 3. Hosted Evidence (CI/CD)
- Hosted evidence harus dibuktikan dari run remote yang benar-benar selesai.
- Status hosted run tidak boleh diangkat dari verifikasi lokal saja.

## Milestone Berikutnya: Post-R1 Hardening
**Target:** Menutup area di luar kontrak minimum R1 tanpa merusak desktop cashier lane.
- hardening bundle lebih lanjut hanya bila ingin satu coordinator lintas context yang lebih luas dari R1
- hosted installer evidence yang repeatable
- Android parity follow-up tanpa memindahkan ownership ke `:shared`
- pembuktian atomicity lintas `sales` / `inventory` / `kernel` bila memang ingin diklaim ACID penuh
