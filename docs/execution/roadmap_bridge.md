# Cassy Roadmap Execution Bridge

Updated: 2026-03-19

Dokumen ini adalah bridge antara roadmap PDF, context agent, dan repo reality. Repo diperlakukan sebagai implementation snapshot; status milestone hanya boleh naik bila ada evidence kode, test, dan verifikasi yang nyata.

## Status Ringkas

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
| M10 | Release (Windows) | **FOUNDATION-OK** | `smokeRun`, hosted `Mainline Evidence`, manual evidence pack | Source/runtime smoke terbukti; install/uninstall installer masih manual-soft-blocker. |
| R2-B1 | Operational Control Foundation | **DONE (FOUNDATION SLICE)** | `OperationalControlService`, `BusinessDayService`, `ShiftService`, `DesktopAppController` | Control tower, open day, shift gating, opening cash approval, dan legacy orphan cleanup sudah hidup di desktop-first lane. |
| R2-B2 | Operational Control Hardening | **DONE (DESKTOP-FIRST SLICE)** | `CashControlService`, `ShiftClosingService`, `KernelRepository`, `DesktopAppController` | Cash control baseline, approval durability, close shift reconciliation, close day fail-closed review, dan kernel migration handling sudah hidup di desktop-first lane. |
| R2-B3 | Final Gate & Truth Sync | **PARTIAL (HONEST VERDICT)** | `r2_final_gate_report.md`, rerun verification matrix 2026-03-19 | Gate teknis lulus, tetapi R2 penuh belum boleh diklaim `DONE` karena void resolver dan release evidence installer masih terbuka. |

## Bukti Verifikasi (Evidence Matrix)

### 1. Local Evidence (Unit/Integration Tests)
- `.\gradlew :shared:kernel:allTests` -> Access, business day, shift, approval policy, dashboard readiness (PASSED)
- `.\gradlew :shared:sales:desktopTest` -> Cart logic, receipt snapshot, failure path, retry/idempotency, replay (PASSED)
- `.\gradlew :shared:inventory:desktopTest` -> Stock transaction invariants (PASSED)
- `.\gradlew :apps:desktop-pos:test` -> Desktop cashier + operational control lane (PASSED)
- `.\gradlew :shared:kernel:desktopTest --tests "id.azureenterprise.cassy.kernel.persistence.KernelPersistenceMigrationTest.*"` -> kernel operational migration proof (PASSED)

### 2. Manual / Local Smoke Evidence
- `.\gradlew :apps:desktop-pos:smokeRun` -> PASS
- Source smoke dan distribution smoke repo-local sudah ada.
- **Installer install/uninstall** masih belum boleh diklaim PASS sebelum checklist manual di `docs/execution/windows_installer_smoke_checklist.md` dijalankan.

### 3. Hosted Evidence (CI/CD)
- Hosted evidence harus dibuktikan dari run remote yang benar-benar selesai.
- Status hosted run tidak boleh diangkat dari verifikasi lokal saja.

## Next Truthful Focus
- void execution resolver lintas sales/cashflow/inventory/reporting
- close report export baseline bila benar-benar dibutuhkan
- release evidence installer manual agar gap Windows delivery makin kecil
- hosted CI reality capture bila ada remote run baru yang benar-benar selesai
