# Cassy Roadmap Execution Bridge

Dokumen ini adalah bridge antara roadmap PDF, context agent, dan repo reality. Repo diperlakukan sebagai implementation snapshot; status milestone hanya boleh naik bila ada evidence kode, test, dan verifikasi yang nyata.

## Status ringkas per 2026-03-16

| ID | Milestone | Status repo jujur | Evidence utama | Catatan |
|:---|:---|:---|:---|:---|
| M0 | Program setup | DONE | `AGENTS.md`, `CODEX.md`, `.agent/`, Gradle wrapper | Control plane dasar ada |
| M1 | Scope lock V1 | DONE | `AGENTS.md`, `.agent/context/project_overview.md` | Desktop-first retail core terkunci |
| M2 | Architecture control plane | PARTIAL / FOUNDATION | multi-module Gradle, build-logic Java 17, SQLDelight bounded context | `:shared` aggregator dan legacy bridge masih ada |
| M3 | Desktop access bootstrap | PARTIAL / FOUNDATION | `apps/desktop-pos`, `AccessService`, desktop controller test | Sebelumnya false-ready karena source set desktop belum benar-benar dibuild |
| M4 | Business day & shift | PARTIAL / FOUNDATION | `BusinessDayService`, `ShiftService`, service tests, desktop guarded flow | Guardrail inti hidup, tetapi belum layak disebut fully done |
| M5 | Catalog + cart + pricing baseline | PARTIAL / FOUNDATION | `SalesService`, `PricingEngine`, product lookup tests | Checkout penuh, receipt, dan payment state belum dibuka |
| M6 | Checkout + receipt | PENDING | - | Hanya placeholder jujur di desktop shell |
| M7 | Inventory basic | PARTIAL / FOUNDATION | `:shared:inventory` module, repository wiring | Belum terbukti end-to-end ke closing/checkout |
| M8 | Reporting dasar | PENDING | - | Belum ada evidence runtime |
| M9 | Sync visibility + replay | PENDING | outbox/infra parsial | Belum ada closure evidence |
| M10 | Release + hypercare | PENDING | CI dasar + local Windows package evidence | Windows pilot CI belum terbukti di hosted runner |

## False readiness yang sudah dibongkar

- `apps/desktop-pos` sempat terlihat ada, tetapi source utama berada di `src/jvmMain` saat module memakai source set `main`; hasilnya desktop build sebelumnya bisa lolos dengan `NO-SOURCE`.
- M3 dan M4 sempat ditandai done hanya karena screen/navigasi tampil, bukan karena access/day/shift guardrail benar-benar hidup.
- Packaging malam di CI hanya menghasilkan Debian package pada Ubuntu; itu bukan evidence kesiapan distribusi Windows.
- desktop run sempat bocor ke Java 21 daemon criteria dan crash karena mixed Compose/Skiko runtime; lane ini sekarang dipaksa kembali ke JDK 17 only dan punya smoke run eksplisit.

## Definition of done minimum per lane foundation

### M2
- task build/test/lint/package yang benar-benar ada terdokumentasi
- boundary shared vs native jelas
- command matrix dan verification matrix sinkron dengan repo

### M3
- bootstrap store/terminal hidup
- login PIN baseline hidup
- restore access context hidup
- locked/wrong pin/error/loading state jujur
- navigation guard mencegah bypass
- test minimal untuk success/wrong pin/restore

### M4
- business day dan shift state dimodelkan
- open/start/end/close punya service guardrail
- cart diblok saat day/shift invalid
- negative-path test ada
- UI menampilkan state error/blocked secara jujur

### Thin M5
- catalog read model dan search hidup
- cart mutation hidup
- pricing baseline hidup di shared domain/application
- unit/service tests untuk invariant pricing dan cart ada
- checkout tetap ditahan

## Verifikasi yang dipakai untuk status ini

- `.\gradlew --version`
- `.\gradlew :apps:desktop-pos:smokeRun`
- `.\gradlew clean`
- `.\gradlew build`
- `.\gradlew test`
- `.\gradlew detekt`
- `.\gradlew :apps:android-pos:lintDebug`
- `.\gradlew :apps:desktop-pos:createDistributable`
- `.\gradlew :apps:desktop-pos:packageDistributionForCurrentOS`
- `.\tooling\scripts\Invoke-DesktopDistributionSmoke.ps1`

Lihat `docs/execution/windows_desktop_runbook.md` dan `docs/execution/workspace_jdk_guide.md` untuk command matrix, artifact path, JDK 17 policy, dan gap packaging Windows.
