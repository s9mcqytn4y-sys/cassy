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
| M6 | Checkout & Receipt | **PENDING** | `SalesService.checkout` (Thin) | Baru mencakup pencatatan lokal. Finalisasi payment & printing masih deferred. |
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
- GitHub Actions build check (Kotlin 2.3.20 compliance).
- **UNKNOWN:** Hosted Windows Installer validation (dikarenakan limitasi runner environment).

## Milestone Berikutnya: M6 (Checkout & Payment Finalization)
**Target:** Menutup gap transaksi dari keranjang hingga "Lunas".
- Integrasi Payment Provider Interface (Stubs for M6).
- Receipt Template Engine (Markdown/JSON to Text).
- Finalisasi status "COMPLETED" di database sales.
