# Cassy Roadmap Execution Bridge

Updated: 2026-03-27

Dokumen ini adalah bridge antara roadmap PDF, context agent, dan repo reality. Repo diperlakukan sebagai implementation snapshot; status milestone hanya boleh naik bila ada evidence kode, test, dan verifikasi yang nyata.

## Status Ringkas

| ID | Milestone | Status Repo Jujur | Evidence Utama | Catatan |
|:---|:---|:---|:---|:---|
| M0 | Program Setup | **DONE** | `AGENTS.md`, `CODEX.md`, `.agent/` | Struktur kendali dasar telah stabil. |
| M1 | Scope Lock V1 | **DONE** | `.agent/context/project_overview.md` | Fokus desktop-first retail core terkunci. |
| M2 | Arch Control Plane | **DONE** | Kotlin 2.2.10, multi-module Gradle | Build logic dan dependency management sinkron dengan repo aktual. |
| M3 | Desktop Bootstrap | **DONE** | `Main.kt`, `DesktopAppControllerTest` | Flow login, restore, dan lockout telah terverifikasi. |
| M4 | Business Day & Shift | **DONE** | `ShiftService`, guardrail lifecycle | Lifecycle open/start/end/close tervalidasi dengan guardrail operasional. |
| M5 | Thin M5 (Catalog/Cart) | **DONE (BASELINES)** | `SalesService`, `ActiveBasket` persistence | Catalog lookup, cart mutation, dan basket persistence sudah stabil. |
| M6 | Checkout & Receipt | **DONE (DESKTOP-FIRST R1)** | `SalesService`, `SalesRepository`, `DesktopAppController` | Finality transaksi, snapshot struk final, readback, replay proof, dan settlement path tervalidasi untuk lane desktop. |
| M7 | Inventory Basic | **DONE (R3 HARDENED SLICE)** | `InventoryService`, `InventoryRepository`, inventory DB migration v3 | Sale -> inventory boundary sudah tegas dan integrity proof ada. |
| M8 | Reporting Dasar | **DONE (R5 HARDENED)** | `ReportingQueryFacade`, `DesktopReportingExporter`, desktop reporting dialog, export tests | Daily summary, shift summary, issue taxonomy, sync readback, dan export bundle CSV/HTML sudah hidup di repo. |
| M9 | Sync Visibility | **DONE (R6 LOCAL-BOUNDARY)** | `OutboxRepository`, `SyncVisibilityService`, `SyncReplayService`, `ReportingQueryFacade`, desktop sync trigger, replay tests | Pending, failed, retry, prune, recovery path, dan tested local replay boundary sudah lengkap untuk definisi R6 yang aktif. |
| M10 | Release (Windows) | **DONE (LOCAL + HOSTED EVIDENCE)** | source smoke, distribution smoke, installer evidence, diagnostics, Windows workflows, dan hosted evidence terbaru yang terverifikasi | Packaging, install/repair/uninstall MSI, diagnostics, recovery baseline, dan hosted artifact lane sudah terbukti. |
| R2-B1 | Operational Control Foundation | **DONE (FOUNDATION SLICE)** | `OperationalControlService`, `BusinessDayService`, `ShiftService`, `DesktopAppController` | Control tower, open day, shift gating, opening cash approval, dan cleanup orphan UI lama sudah hidup. |
| R2-B2 | Operational Control Hardening | **DONE (DESKTOP-FIRST SLICE)** | `CashControlService`, `ShiftClosingService`, `KernelRepository`, `DesktopAppController` | Cash control baseline, approval durability, close shift reconciliation, dan close day fail-closed review sudah hidup. |
| R2-B3 | Final Gate & Truth Sync | **DONE (DESKTOP-FIRST)** | rerun verification matrix + void execution lane | Gate teknis aktif sudah ditutup untuk scope desktop-first V1. |

## Bukti Verifikasi (Evidence Matrix)

### 1. Local Evidence (Unit/Integration Tests)
- `.\gradlew :shared:kernel:allTests` -> PASS
- `.\gradlew :apps:desktop-pos:build` -> PASS
- `.\gradlew test` -> PASS
- `.\gradlew lint detekt` -> PASS
- `.\gradlew :apps:desktop-pos:smokeRun` -> PASS
- `.\gradlew :shared:kernel:verifyCommonMainKernelDatabaseMigration :shared:inventory:verifyCommonMainInventoryDatabaseMigration :shared:masterdata:verifyCommonMainMasterDataDatabaseMigration :shared:sales:verifyCommonMainSalesDatabaseMigration` -> PASS

### 2. Manual / Local Smoke Evidence
- `powershell -ExecutionPolicy Bypass -File tooling/scripts/Invoke-DesktopDistributionSmoke.ps1` -> PASS
- `powershell -ExecutionPolicy Bypass -File tooling/scripts/Invoke-WindowsInstallerEvidence.ps1` -> PASS
- `powershell -ExecutionPolicy Bypass -File tooling/scripts/Invoke-WindowsUpgradeEvidence.ps1` -> PASS
- `powershell -ExecutionPolicy Bypass -File tooling/scripts/Invoke-DesktopPerformanceProbe.ps1` -> PASS
- diagnostics terbaru: `build/release-diagnostics/20260327-145516/`
- installer evidence terbaru: `build/installer-evidence/20260327-142816/`
- upgrade evidence terbaru: `build/upgrade-evidence/20260327-145121/`
- perf probe terbaru: `build/perf-probe/20260327-145425/`
- Baseline recovery data lokal: `tooling/scripts/Backup-CassyDesktopState.ps1`
- Baseline diagnostics Windows: `tooling/scripts/Collect-WindowsReleaseDiagnostics.ps1`

### 3. Hosted Evidence (CI/CD)
- Hosted `Mainline Evidence` terakhir yang benar-benar diverifikasi -> PASS.
- Job `windows-package-evidence` -> PASS.
- Job `mainline-evidence-manifest` -> PASS.
- Artifact hosted yang terbit:
  - `cassy-desktop-exe`
  - `cassy-desktop-app`
  - `cassy-desktop-msi`
  - `cassy-installer-evidence`
  - `cassy-release-diagnostics`
  - `cassy-mainline-evidence`

## Next Truthful Focus

- hosted beta tag / prerelease evidence
- burn-in rehearsal 2-3 hari untuk pilot terbatas
- code-signing readiness dan profiler-backed instrumentation yang lebih dalam bila benar-benar menambah evidence

## Historical Snapshot Rule

- File eksekusi yang masih mereferensikan snapshot lama, artifact lama, atau bukti tanggal lama harus dibaca sebagai historical snapshot kecuali dokumen ini, `repo_reality_sync.md`, dan checklist aktif menyatakan sebaliknya.

## R3 Final Gate Note

- R3 `DONE` hanya berarti inventory truth lite desktop-first sudah hidup dan terverifikasi.
- Ini tidak menaikkan status full void execution, second PIN / dual auth, atau PDF report export menjadi PASS.
