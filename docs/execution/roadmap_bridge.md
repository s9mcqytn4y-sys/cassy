# Cassy Roadmap Execution Bridge

Dokumen ini adalah bridge antara roadmap PDF, context agent, dan repo reality. Repo diperlakukan sebagai implementation snapshot; status milestone hanya boleh naik bila ada evidence kode, test, dan verifikasi yang nyata.

## Status ringkas per 2026-03-16

| ID | Milestone | Status repo jujur | Evidence utama | Catatan |
|:---|:---|:---|:---|:---|
| M0 | Program setup | DONE | `AGENTS.md`, `CODEX.md`, `.agent/`, Gradle wrapper | Control plane dasar ada |
| M1 | Scope lock V1 | DONE | `AGENTS.md`, `.agent/context/project_overview.md` | Desktop-first retail core terkunci |
| M2 | Architecture control plane | DONE | multi-module Gradle, build-logic Java 17, hosted Windows evidence sukses | `:shared` aggregator masih ada, tetapi sunset path dan control-plane truth sudah tegas |
| M3 | Desktop access bootstrap | DONE | `Main.kt` branding, `DesktopAppControllerTest` | Bootstrap, Login, Restore, dan Lockout terverifikasi |
| M4 | Business day & shift | FOUNDATION-CLOSED | `BusinessDayServiceTest`, `ShiftServiceTest` | Guardrails terverifikasi di commonTest |
| M5 | Catalog + cart + pricing baseline | FOUNDATION-CLOSED | `SalesService`, desktop catalog flow | Cart mutation terverifikasi |
| M6 | Checkout + receipt | PENDING | - | Hanya placeholder jujur di desktop shell |
| M7 | Inventory basic | PARTIAL / FOUNDATION | `:shared:inventory`, `InventoryService` | Sales checkout memakai inventory boundary |
| M8 | Reporting dasar | PENDING | - | Belum ada evidence runtime |
| M9 | Sync visibility + replay | PENDING | outbox/infra parsial | Belum ada closure evidence |
| M10 | Release + hypercare | PENDING | CI dasar + local Windows package evidence | Windows pilot CI belum terbukti di hosted runner |

## Verifikasi yang dipakai untuk status ini

- `.\gradlew :shared:kernel:desktopTest` (13 tests passed)
- `.\gradlew :apps:desktop-pos:run --args="--smoke-run"`
- `.\gradlew :apps:desktop-pos:test` (Controller logic evidence)

Lihat `docs/execution/windows_desktop_runbook.md`, `docs/execution/workspace_jdk_guide.md`, `docs/execution/ci_topology_truth.md`, dan `docs/execution/windows_installer_smoke_checklist.md`.
