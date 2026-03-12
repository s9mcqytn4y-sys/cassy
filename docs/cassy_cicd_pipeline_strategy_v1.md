# Cassy CI/CD Pipeline Strategy v1

## Document Overview
Prescriptive baseline untuk GitHub Actions, quality gates, packaging, staged deployment, runner matrix, dan governance.

## Purpose
Cakupan SLA PR Fast Gate pull_request Verifikasi cepat, required checks, tanpa deploy Shared, Android POS, Desktop, Backend; iOS selective 8-18 menit target Mainline push ke main Re-validate, Shared, Android 15-30 menit target Maret 2026 Packaging package internal artifacts, o...

## Scope
SLA PR Fast Gate pull_request Verifikasi cepat, required checks, tanpa deploy Shared, Android POS, Desktop, Backend; iOS selective 8-18 menit target Mainline push ke main Re-validate, Shared, Android 15-30 menit target Maret 2026 Packaging package internal artifacts, optional...

## Key Decisions / Core Rules
Stable required checks harus selalu muncul; workflow modular dengan reusable subworkflow; Android POS, Desktop, Shared, dan Backend adalah mandatory lane phase 1; iOS selective lane.

## Detailed Content

### Normalized Source Body
Maret 2026
Cassy CI/CD Pipeline Strategy
Prescriptive baseline for GitHub Actions, quality gates, release packaging, and staged
deployment
Status
Source of Truth
Finalized Scope
Final baseline for engineering
handoff
Target-state artefacts, not current
repo snapshot
Phase 1 mandatory: Shared +
Android POS + Desktop +
Backend
Keputusan final yang mengikat dokumen ini
Q1-B target-state artefacts menjadi baseline utama; Q2-B hybrid shared UI selektif; Q3-B mandatory lane
Shared + Android POS + Desktop + Backend; Q4-B iOS tetap ada sebagai selective lane di macOS runner;
Q5-B dan Q6-B menempatkan unit/component/integration kritikal sebagai gate harian dan
instrumented/device suite di nightly atau release candidate; Q7-B tag-driven/manual release; Q8-C
backend staging/production plus Android internal artifact dan desktop internal package; Q10-B
dependency verification dan retention discipline menjadi baseline security menengah; Q11-B multiple
workflows dengan reusable subworkflow; Q12-C dual-layer document untuk executive summary dan
engineering detail.
Disusun untuk Cassy Design System / engineering handoff - 8 Maret 2026

Maret 2026
## 1. Executive Summary
Strategi CI/CD Cassy harus melindungi correctness bisnis untuk sistem retail local-first, bukan
sekadar menghasilkan build hijau. Artinya pipeline harus memastikan transaksi lokal tetap
atomik, audit dan outbox tidak pernah menjadi best-effort, migrasi SQLDelight dan perubahan
persistence semantics selalu diverifikasi, serta jalur sync yang retriable tetap idempotent dan dapat
direkonsiliasi. Baseline ini memakai target-state artefacts sebagai sumber utama, lalu
menerjemahkannya menjadi workflow GitHub Actions yang preskriptif, berlapis, dan tidak jatuh
ke mega-pipeline rapuh. [INT-01][INT-03][INT-04]
Cassy diposisikan sebagai monorepo dengan KMP shared core, Android POS sebagai writer utama
phase 1, desktop-backoffice sebagai node operasional untuk reporting dan reconciliation, backend
Go REST + PostgreSQL sebagai authority HQ, dan iOS dipertahankan sebagai selective lane, bukan
mandatory gate harian. Hybrid shared UI diterima secara selektif, namun app-shell dan platform
adapter tetap native. [INT-01][INT-02]
Hasil akhir yang ditetapkan dokumen ini
Empat lane operasional utama - PR Fast Gate, Mainline Packaging, Nightly Quality, dan Release/Promotion
- ditambah reusable workflow per concern. Branch protection mengandalkan stable required checks yang
selalu muncul, sehingga path-based scope reduction tidak meninggalkan required check dalam status
Pending. Deployment backend dibagi menjadi staging dan production dengan GitHub Environments
sebagai gate. Android dan desktop dibangun sebagai internal deliverables sejak phase 1, bukan sekadar
artefak sampingan. [EXT-01][EXT-02][EXT-03][EXT-04]
### 1.1 Prinsip keputusan
- 
Business correctness wins over throughput: gate boleh sedikit lebih mahal bila mencegah defect
mahal pada payment, return/refund, shift close, business-day close, receiving discrepancy, stock
adjustment, offline degradation, sync conflict, auth fallback, dan audit fallback. [INT-01][INT-04]
- 
Local-first berarti build/test harus membuktikan atomic bundle transaksi lokal, foreign key
enforcement per koneksi, dan retry safety; bukan hanya memeriksa response UI. [INT-01]
[EXT-08]
- 
Pipeline wajib modular: reusable workflow dipakai untuk shared, android-pos, desktop-
backoffice, backend, dan iOS selective lane, sehingga tiap lane bisa berkembang tanpa copy-
paste workflow. [INT-01][EXT-01]
- 
Release bukan pengganti kualitas harian. Fast checks tetap mandatory di setiap PR; packaging
dan promotion hanya menambah jaminan, bukan menggantikan test harian. [INT-04]
## 2. Baseline, Scope, dan Penyelesaian Konflik Artefak
Repo GitHub saat ini diperlakukan sebagai implementation snapshot, bukan source of truth desain.
Baseline final diambil dari artefak target-state agar pipeline yang dihasilkan tidak hanya
merefleksikan struktur repo transisional, tetapi juga arah implementasi yang benar. Traceability
matrix juga secara eksplisit menyatakan bahwa implementasi dan baseline uji pada saat artefak itu
dibuat masih berupa turunan yang siap dipakai, belum bukti delivery harian. [INT-02][INT-03]

Maret 2026
Area keputusan
Final choice
Implikasi pipeline
Sumber utama
Target-state artefacts
Pipeline dan required checks
mengikuti arsitektur target,
bukan struktur repo transisional.
Strategi UI
Hybrid shared UI selektif
Shared Compose diperbolehkan
hanya untuk presentasi yang
stabil; shell, permission, scanner,
printer, payment, lifecycle, dan
bootstrap tetap native.
Scope mandatory phase 1
Shared + Android POS + Desktop
+ Backend
Desktop tidak diposisikan sebagai
aksesori; ia adalah node
operasional backoffice untuk
reconcile, report, dan close
business-day.
Status iOS
Selective lane di macOS runner
Ada lane iOS untuk menjaga
evolusi lintas platform, tetapi ia
tidak menjadi mandatory gate
pada mayoritas PR.
Release model
Tag-driven atau manual approval
Tidak ada auto-release setiap
merge ke main; packaging dan
promotion dipicu secara sadar.
Konflik penting yang harus ditutup secara eksplisit adalah baseline UI. Architecture Specification
lama menolak shared UI penuh dan mendorong UI native per platform, sedangkan Module Project
Structure Specification memperbarui area itu menjadi hybrid shared UI. Dokumen ini mengadopsi
revisi terbaru: business core tetap KMP shared, shared UI hanya untuk layer presentasi yang stabil,
sedangkan concern device-heavy tetap native. Dengan begitu pipeline tetap dapat menambah lane
iOS selective tanpa melanggar dependency direction, auditability, atau offline correctness. [INT-01]
[INT-02]
## 3. Target Operating Model untuk CI/CD Cassy
Model operasi pipeline dibagi menjadi empat lane yang masing-masing memiliki tujuan berbeda.
Pembagian ini sengaja dipilih agar feedback pada PR tetap cepat, sementara kualitas yang lebih
mahal dan packaging release ditempatkan pada jalur yang tepat. Ini sejalan dengan baseline
arsitektur yang menolak brittle mega-pipeline dan menekankan PR-fast checks plus stronger main-
branch guarantees. [INT-01]
Lane
Trigger
Tujuan
Cakupan
SLA
PR Fast Gate
pull_request
Verifikasi cepat,
required checks,
tanpa deploy
Shared, Android
POS, Desktop,
Backend; iOS
selective
8-18 menit target
Mainline
push ke main
Re-validate,
Shared, Android
15-30 menit target

Maret 2026
Packaging
package internal
artifacts, optional
staging deploy
backend
POS, Desktop,
Backend
Nightly Quality
schedule + manual
Menjalankan suite
mahal: device,
sync-fault pack,
migration replay,
extended integrity
Android, Desktop,
Backend, selected
shared scenarios
Tidak memblok PR
Release /
Promotion
tag +
workflow_dispatch
Versioning,
changelog, signed
packaging, staging
or production
promotion
Android internal
release, desktop
package, backend
staging/production
, optional iOS build
Manual approval
aware
Aturan desain lane
PR Fast Gate harus selalu ter-trigger agar stable required checks tidak pernah hilang. Pengurangan scope
dilakukan di dalam workflow melalui change detection dan conditional jobs, bukan dengan mematikan
workflow menggunakan path filter pada level trigger. GitHub menjelaskan bahwa workflow yang terskip
karena path filtering, branch filtering, atau skip command dapat meninggalkan required checks pada
status Pending dan memblok merge. [EXT-04]
## 4. Workflow Topology dan File Map yang Direkomendasikan
Topologi workflow di bawah ini menggabungkan keputusan arsitektur internal dengan kapabilitas
reusable workflow pada GitHub Actions. Reusable workflow dipanggil lewat jobs.<job_id>.uses dan
`workflow_call`, sehingga logika build/test per concern dapat dipakai ulang pada PR, mainline,
nightly, dan release tanpa duplikasi YAML. [INT-01][EXT-01]
Workflow file
Tipe
Peran preskriptif
.github/workflows/pr-gate.yml
Caller / required
Workflow utama untuk
pull_request. Selalu jalan.
Melakukan change detection,
memanggil reusable workflow
yang relevan, dan menerbitkan
stable checks.
.github/workflows/mainline.yml
Caller
Workflow untuk push ke main.
Mengulang fast gate yang
relevan, mengemas internal
artifacts, dan boleh melakukan
deploy backend ke staging bila
area backend berubah.
.github/workflows/nightly-
quality.yml
Caller
Workflow terjadwal untuk suite
mahal: instrumented/device,

Maret 2026
extended sync-reconcile
scenarios, migration replay,
dependency verification refresh,
dan smoke package install.
.github/workflows/release.yml
Caller
Workflow tag-driven atau
manual untuk versioning,
changelog, signed packaging,
attestation jika applicable, dan
promotion ke staging/production.
.github/workflows/reusable-
shared.yml
Reusable
Compile, static analysis,
common/unit/component tests,
SQLDelight migration
verification, integrity-oriented
repository tests.
.github/workflows/reusable-
android-pos.yml
Reusable
Assemble Android POS, local unit
tests, Compose/static checks,
packaging sanity, selected smoke
verification.
.github/workflows/reusable-
desktop.yml
Reusable
Build desktop-backoffice JVM app,
unit/component tests, package
sanity untuk internal
distribution.
.github/workflows/reusable-
backend.yml
Reusable
go test ./..., build cassy-api,
postgres migration checks, API
contract/unit tests, deployment
packaging.
.github/workflows/reusable-ios-
selective.yml
Reusable
Selective macOS lane untuk ios-
pos ketika path terkait iOS/shared
UI berubah atau saat
workflow_dispatch/release
memintanya.
Job names di required workflow harus unik lintas workflow untuk menghindari ambiguous status
check results, sebagaimana diperingatkan oleh GitHub untuk protected branches. Karena itu nama
check tidak boleh terlalu generik seperti `build` atau `test`; gunakan nama stabil seperti `shared-
fast`, `android-pos-fast`, `desktop-fast`, `backend-fast`, dan `pr-gate-summary`. [EXT-04]
## 5. PR Fast Gate - Required Checks Harian
PR Fast Gate adalah pusat kualitas harian Cassy. Required checks pada branch protection mengacu
ke job yang selalu muncul pada workflow ini. GitHub menyatakan required status checks harus
berstatus successful, skipped, atau neutral; maka pengurangan scope dilakukan dengan conditional
execution di level job, bukan dengan men-skip keseluruhan workflow. [EXT-04]
Check name
Kapan aktif
Kontrak minimum

Maret 2026
pr-gate-summary
Selalu
Validasi change detection,
menampilkan scope job, fail-fast
bila workflow graph tidak
konsisten.
shared-fast
shared/**, tooling/build-logic/**,
gradle/**, DB contracts
Compile shared, static analysis,
common/unit/component tests,
SQLDelight migration verify,
FK/integrity smoke.
android-pos-fast
apps/android-pos/**, shared/**
tertentu,
scanner/printer/payment
adapters
Assemble debug or CI build, local
unit tests, packaging sanity, lint
relevan, no instrumented by
default.
desktop-fast
apps/desktop-backoffice/**,
reporting/reconcile/admin shared
UI, shared/reporting/**,
shared/sync/**
Build desktop-backoffice, run
unit/component tests untuk
reporting/reconcile/admin.
backend-fast
backend/**, sync contracts,
postgres migrations, API layer
go test ./..., build cassy-api,
migration checks, API
contract/unit tests.
ios-selective-info
apps/ios-pos/**,
shared/compose/**, shared UI
contracts, workflow_dispatch
macOS lane selective; non-
blocking pada mayoritas PR,
tetapi wajib terlihat jika scope
memang menyentuh iOS/shared
UI.
Rule non-negotiable untuk PR Fast Gate
Perubahan yang mengubah sync contract atau persistence semantics tidak boleh dipisahkan dari test dan
migration verification di PR lain. Architecture dan Test Specification sama-sama menegaskan bahwa
perubahan schema, migration, sync contract, dan persistence semantics harus diverifikasi pada PR yang
sama. [INT-01][INT-04]
- 
Semua perubahan SQLDelight wajib hadir sebagai migration file dan diverifikasi di CI. [INT-01]
- 
Foreign keys harus dinyalakan dan diverifikasi per koneksi database test; SQLite tidak
mengaktifkan foreign keys secara otomatis untuk setiap connection. [INT-01][EXT-08]
- 
Atomic bundle sale completion, return, shift close, inventory adjustment, serta audit/outbox
append wajib punya kandidat automation di shared/common atau component test. [INT-01]
[INT-04]
- 
Android instrumented tests tidak masuk required PR gate kecuali ada kebutuhan fidelity
perangkat yang tak bisa dipenuhi local tests. Android Developers menyarankan instrumented
tests hanya ketika benar-benar perlu perilaku real device. [EXT-07]

Maret 2026
## 6. Test Automation Strategy per Lane
Dokumen test Cassy sudah eksplisit: layering sehat lebih penting daripada memaksa semua test
menjadi UI/E2E. Common business logic diuji sedini mungkin, component/repository test dipakai
untuk boundary data yang nyata, integration dipakai untuk payment/identity/loyalty/HQ
sync/printer, dan UI or device tests dipakai ketika fidelity platform memang berpengaruh pada
correctness atau evidence bisnis. [INT-04]
Jenis test
Lane default
Cakupan preskriptif
Unit / common test
PR Fast Gate
Shared domain/application rules:
pricing, eligibility, totals,
idempotency, sync item state
machine, role guard.
Component / repository test
PR Fast Gate
Local DB + stub adapters untuk
atomic bundle, audit+outbox
commit, FK/integrity, migration
behavior.
Integration test
PR Fast Gate untuk subset;
Nightly untuk extended pack
Payment declined/pending, auth
fallback, sync batch partial
failure, reconcile conflict.
UI local test / desktop functional
PR Fast Gate selektif
Desktop
reconcile/reporting/admin dan
Android local UI logic yang tidak
butuh real device.
Instrumented / device test
Nightly + RC + manual
Scanner input, printed receipt
flow, Android lifecycle edge,
desktop install or smoke flow bila
perlu OS packaging fidelity.
Manual / UAT
Milestone / RC
Shift handover lapangan, offline
emergency operations, approval
escalation, operational review.
Wave-1 critical flows yang wajib diprioritaskan untuk automation dan gating adalah payment,
return/refund, cash in/out di atas limit, close shift, end-of-day, receiving discrepancy, stock
adjustment, offline degradation, sync conflict, auth fallback, dan audit fallback. Coverage ini bukan
preferensi QA semata; ia tercatat konsisten pada Test Specification dan Traceability Matrix sebagai
area defect paling mahal. [INT-03][INT-04]
## 7. Build, Dependency, dan Database Policy
Pipeline Cassy tidak boleh hanya menyalakan cache dan berharap reproducibility terjadi sendiri.
Karena monorepo ini menggabungkan shared KMP, Android, desktop, dan backend, kebijakan
build harus mengutamakan determinism lebih dulu, baru optimisasi. [INT-01][INT-02]

Maret 2026
### 7.1 Gradle dan dependency verification
- 
Gunakan Gradle wrapper di semua workflow. Jangan mengandalkan Gradle yang terpasang di
runner. [INT-01]
- 
Aktifkan Gradle dependency verification melalui `gradle/verification-metadata.xml`. Gradle
menjelaskan bahwa verifikasi checksum dan signature tersedia bawaan, tetapi tidak aktif
secara default; begitu metadata file ada, strict verification menjadi baseline. [EXT-05]
- 
Gunakan strict mode untuk lane PR, mainline, dan release. Lenient mode hanya boleh dipakai
sementara saat bootstrap atau update dependency, lalu tidak menjadi bagian dari gate tetap.
[EXT-05]
- 
Jangan jadikan configuration cache sebagai gate wajib phase 1. Gradle saat ini menyebut
configuration cache sebagai preferred mode, tetapi dukungan plugin, CI, dan IDE sync masih
berkembang. Terapkan bertahap melalui nightly experiment sampai stabil. [EXT-06]
### 7.2 Database dan migration policy
- 
Setiap perubahan schema SQLDelight wajib datang bersama migration file dan migration
verification di CI. [INT-01]
- 
Smoke test integrity minimal harus memverifikasi FK enforcement, idempotency key
uniqueness, dan separation between current state vs explanation trail seperti
inventory_balance versus stock_ledger_entry. [INT-01][INT-04]
- 
Backend migration checks untuk PostgreSQL wajib berjalan pada reusable-backend workflow
setiap kali backend schema, sync contract, atau transaction policy berubah. [INT-01]
- 
Nightly harus memiliki migration replay pack yang menguji database dari snapshot schema
lama ke schema terbaru, lalu menjalankan subset critical flows di atas hasil migrasi tersebut.
[INT-04]
Yang tidak boleh dilakukan
Jangan menyembunyikan kegagalan integrity di bawah retry otomatis. Sync failure tidak boleh
menghilang tanpa explicit terminal state, dan sync_conflict tidak boleh selesai tanpa resolution_status
yang eksplisit. Build hijau yang mengabaikan state ini justru berbahaya bagi Cassy. [INT-01][INT-04]
## 8. Artifacts, Retention, Environments, dan Secrets
GitHub Actions artifacts dipakai untuk berbagi data antarkerja dan menyimpan hasil build/test
setelah workflow selesai. Cassy harus memakainya secara sadar: artefak bukan tempat sampah
permanen, tetapi evidence dan deliverable yang retention-nya dibedakan menurut lane. [EXT-02]
Lane
Artifact minimum
Retention
Tujuan
PR Fast Gate
test reports, coverage
summary, build scan
summary, unsigned
sanity packages bila
perlu
7 hari
Debugging PR dan
review failure

Maret 2026
Mainline Packaging
Android internal
artifacts, desktop
package candidate,
backend deploy bundle,
changelog preview
14 hari
Internal QA dan staging
validation
Nightly Quality
extended reports, device
logs, migration replay
evidence, flaky-triage
bundle
14 hari
Operational quality and
triage
Release / Promotion
signed Android release
artifacts, desktop
internal package,
release notes,
checksums, backend
deploy manifest
90 hari minimum
Traceability release dan
rollback reference
### 8.1 Environments dan approval
- 
Gunakan dua GitHub Environments: `staging` dan `production`. Environment dipakai untuk
deployment target, approval, branch restriction, dan pemisahan secrets. [EXT-03]
- 
Backend deploy ke staging boleh otomatis dari `mainline.yml` hanya jika reusable-backend
lulus dan scope backend benar-benar berubah. Production deploy hanya dari `release.yml`
dengan approval environment. [EXT-03]
- 
Android internal artifact dan desktop internal package tidak wajib memakai environment
deployment bila hanya dipublikasikan sebagai artifact internal. Namun promosi ke distribusi
formal atau kanal produksi harus terjadi di release lane dengan approval yang setara.
- 
Untuk private repository, fitur tertentu seperti required reviewers pada environment
bergantung pada plan GitHub. Karena itu dokumen ini memisahkan baseline yang wajib dan
advanced option yang tergantung plan. [EXT-03]
### 8.2 Secrets discipline
- 
Repository secrets hanya untuk kebutuhan build global yang tidak spesifik environment.
Secrets deployment diletakkan di environment agar aksesnya baru terbuka ketika job benar-
benar masuk ke environment tersebut. [EXT-03]
- 
Jangan expose signing key Android, credential staging/production backend, atau token
packaging pada workflow dari forked PR. PR dari fork cukup menjalankan subset aman tanpa
secrets deployment.
- 
Pisahkan credentials untuk staging dan production. Tidak boleh ada reuse satu token admin
untuk kedua environment.
## 9. Branch Protection dan Governance Rules
Cassy membutuhkan branch protection yang ketat pada `main`. GitHub menjelaskan bahwa
protected branches dapat mewajibkan status checks, review, dan aturan merge lain; required

Maret 2026
status checks pada protected branch harus memiliki nama unik dan harus lolos sebelum merge.
[EXT-04]
Aturan
Keputusan preskriptif
Protected branch
`main`
Merge policy
Pull request only; no direct push kecuali
administrator pada keadaan darurat yang tercatat
Required checks
pr-gate-summary, shared-fast, android-pos-fast,
desktop-fast, backend-fast
Strictness
Require branch up to date before merge untuk
mencegah false green dari base branch drift
Required reviews
Minimal 1 engineering review; 2 bila menyentuh
sync contract, schema, payment, atau auth fallback
Linear history
Disarankan aktif untuk menjaga traceability dan
rollback lebih mudah
Penanganan path-based scope reduction yang benar
Jangan menaruh path filter di trigger workflow required. Taruh workflow required agar selalu jalan, lalu
di dalamnya lakukan change detection dan conditional jobs. Ini menghindari kasus workflow terskip
yang membuat check tetap Pending. Selain itu, job name harus unik lintas workflow untuk menghindari
ambiguous status checks. [EXT-04]
## 10. Release, Promotion, dan Deployment Strategy
Release Cassy bersifat tag-driven atau manual approval, bukan auto-release pada setiap merge ke
main. Ini konsisten dengan baseline architecture yang hanya menempatkan `release.yml` pada tag
atau manual trigger dan dengan keputusan finalisasi Anda. [INT-01]
Area
Prescriptive strategy
Version source
Annotated git tag `vMAJOR.MINOR.PATCH` untuk
release; pre-release opsional `-rc.N`
Release trigger
Push tag atau workflow_dispatch oleh maintainer
Android output
Signed internal AAB/APK bila signing secrets
tersedia; bila belum tersedia, hasil release lane
dianggap candidate, bukan production-ready
Desktop output
Windows-first internal package untuk desktop-
backoffice, plus checksum and manifest; OS lain
opsional
Backend staging
Auto or manual from mainline when backend
changed and staging environment allows

Maret 2026
Backend production
Release lane only, after production approval and
successful staging validation
Rollback policy
Production deploy bundle wajib menyimpan
release manifest, build SHA, migration version, dan
rollback note minimum
### 10.1 Attestation dan supply-chain hardening
Artifact attestations layak ditempatkan sebagai advanced option, bukan baseline wajib. GitHub
menyediakan attestations untuk semua plan saat repo publik, tetapi untuk repo private/internal
pada GitHub Free, Pro, atau Team fitur ini tidak tersedia; private/internal memerlukan GitHub
Enterprise Cloud. Karena itu strategi Cassy menetapkannya sebagai enhancement pada release
artifacts yang memang akan didistribusikan, bukan pada setiap build testing. [EXT-03]
- 
Baseline wajib: dependency verification, protected branches, environment discipline, retention
discipline, reproducible packaging, dan explicit release manifest. [EXT-04][EXT-05]
- 
Advanced option: attestation untuk signed Android artifact, desktop release package, dan
backend deployment manifest pada public repo atau private repo dengan plan yang
mendukung. [EXT-03]
## 11. Runner Matrix dan Platform Scope
Job
Runner
Alasan
shared-fast
ubuntu-latest
Cepat, murah, cukup untuk KMP
shared
compile/test/static/migration
verify
android-pos-fast
ubuntu-latest
Build Android local tests lebih
efisien di Linux runner;
instrumented dipindah ke nightly
desktop-fast
ubuntu-latest
JVM desktop build dan tests
cukup di Linux untuk gate
harian; packaging fidelity
Windows diuji pada
nightly/release bila diperlukan
backend-fast
ubuntu-latest
Go build/test dan migration
checks paling stabil dan
ekonomis di Linux
ios-selective
macos-latest
Hanya untuk path iOS/shared-ui
yang relevan atau release/manual
lane
android-device-nightly
macos-latest atau self-hosted /
dedicated emulator runner
Dipilih sesuai toolchain device
farm yang tersedia; tidak wajib
pada fase awal jika belum ada
infra

Maret 2026
Runner matrix ini sengaja menekan biaya dan durasi: mayoritas lane harian berjalan di Ubuntu,
sedangkan macOS hanya dipakai untuk iOS selective lane dan kebutuhan device/build fidelity
tertentu. Keputusan ini juga selaras dengan module structure yang sudah mengunci selective
macOS CI untuk iOS walau workstation utama Windows. [INT-02]
## 12. Rollout Plan - Dari Snapshot Repo ke Target Pipeline
Pipeline ini preskriptif, tetapi implementasinya tetap perlu staged rollout agar repo transisional
tidak langsung pecah. Urutan di bawah ini meminimalkan blast radius dan menjaga deliverability.
[INT-02][INT-03]
Phase
Fokus kerja
Exit criteria
Phase 0 - Control plane
Buat protected branch, stable job
names, pr-gate.yml, reusable
workflow skeleton, retention
policy.
PR checks selalu muncul dan
tidak ada Pending karena path
filter.
Phase 1 - Shared and backend
correctness
Aktifkan reusable-shared dan
reusable-backend dengan
migration verify, FK checks,
unit/component tests, API
contract tests.
Schema, sync contract, dan local-
first invariants masuk gate
harian.
Phase 2 - Android POS and
Desktop
Aktifkan android-pos-fast dan
desktop-fast; pindahkan logic
yang masih liar ke testable
application boundaries.
Android POS dan desktop-
backoffice masuk mandatory lane
phase 1.
Phase 3 - Nightly quality
Tambahkan instrumented/device,
migration replay, sync-fault pack,
smoke package install.
Coverage fidelity tinggi tanpa
menghambat PR.
Phase 4 - Release and promotion
Aktifkan release.yml, staging
deployment, signed artifacts,
environment approvals, optional
attestation.
Promotion menjadi repeatable
dan traceable.
Definition of Done untuk implementasi awal strategi ini
Strategi ini dianggap terpasang dengan benar bila: (1) `main` sudah protected; (2) PR selalu menampilkan
stable checks yang tidak menggantung; (3) shared, android-pos, desktop, dan backend sudah memiliki
reusable workflow aktif; (4) migration verification dan FK integrity checks sudah masuk gate; (5) nightly
berjalan dengan suite mahal; (6) release lane dapat mengemas Android internal artifact, desktop package,
dan deploy backend ke staging dengan approval yang benar.

Maret 2026
Appendix A - Required Check Contract
Check
Scope
Status
Catatan
pr-gate-summary
Repo-wide
Selalu required
Memberi satu titik
kebenaran untuk scope
dan orchestration hasil
diff.
shared-fast
shared, gradle, build
logic, DB contract
changes
Required
Job boleh skipped via
internal condition bila
area tidak berubah.
android-pos-fast
android-pos and related
shared areas
Required
Tidak menjalankan
instrumented suite
harian.
desktop-fast
desktop-backoffice and
reporting/reconcile/adm
in shared areas
Required
Menegaskan desktop
adalah in-scope phase 1.
backend-fast
backend, postgres
migrations, sync/API
contracts
Required
Menjaga server
convergence dan
contract correctness.
ios-selective-info
ios/shared-ui selective
paths
Informational by default
Boleh dipromosikan
menjadi required pada
milestone iOS tertentu.
Appendix B - Official References used for external validation
EXT-01 GitHub Docs - Reusing workflows - Validasi penggunaan reusable workflow via `workflow_call` dan
`jobs.<job_id>.uses`.
EXT-02 GitHub Docs - Store and share data with workflow artifacts - Validasi artifact upload/download,
retention, dan passing data antar-job.
EXT-03 GitHub Docs - Deployment environments, deployments and environments, artifact attestations
- Validasi staging/production environments, approval rules, secrets discipline, serta batas availability
attestation pada plan GitHub.
EXT-04 GitHub Docs - Protected branches, troubleshooting required status checks, skipping workflow
runs - Validasi job name uniqueness, required checks, dan risiko Pending check ketika workflow terskip.
EXT-05 Gradle User Guide - Dependency verification - Validasi strict dependency verification, verification
metadata, dan penggunaan lenient mode hanya saat bootstrap/update.
EXT-06 Gradle User Guide - Configuration Cache - Validasi bahwa configuration cache adalah preferred
mode tetapi dukungan CI dan IDE sync masih berkembang.
EXT-07 Android Developers - Build instrumented tests - Validasi bahwa instrumented tests lebih lambat
dan dipakai ketika butuh perilaku real device.
EXT-08 SQLite Documentation - Foreign Key Support - Validasi bahwa foreign keys harus diaktifkan per
connection dan tidak otomatis aktif secara default.

Maret 2026
Appendix C - Internal Artefact References
INT-01 Cassy Architecture Specification v1 (8 Maret 2026) - Local-first KMP + SQLite/SQLDelight +
Go/PostgreSQL HQ, module map, transaction bundles, CI/CD architecture baseline.
INT-02 Cassy Module Project Structure Specification (8 Maret 2026) - Implementation baseline terbaru:
hybrid shared UI selektif, monorepo target, selective macOS CI untuk iOS.
INT-03 Traceability Matrix Store / POS System (8 Maret 2026) - Baseline dual trace design-to-
implementation/test dan prioritas tindak lanjut untuk implementation/test artefacts.
INT-04 Test Specification Store / POS System (8 Maret 2026) - Layered testing, risk-first, critical flow
wave-1, dan mapping automation/gate CI.
INT-05 UML-Modeling-Source-of-Truth - Aturan SDLC, traceability, dan consistency rules yang membatasi
struktur dokumen dan implementasi.


## Constraints / Policies
Tidak boleh memakai brittle mega-pipeline; packaging/release bukan pengganti quality gate harian.

## Technical Notes
Pipeline harus memverifikasi atomic local-first bundle, migration replay, FK enforcement, retry safety, dan sync correctness.

## Dependencies / Related Documents
- `cassy_architecture_specification_v1.md`
- `cassy_module_project_structure_specification.md`
- `cassy_test_automation_specification.md`
- `cassy_migration_script_specification.md`
- `store_pos_test_specification.md`

## Risks / Gaps / Ambiguities
- Tidak ditemukan gap fatal saat ekstraksi. Tetap review ulang bagian tabel/angka jika dokumen ini akan dijadikan baseline implementasi final.

## Reviewer Notes
- Struktur telah dinormalisasi ke Markdown yang konsisten dan siap dipakai untuk design review / engineering handoff.
- Istilah utama dipertahankan sedekat mungkin dengan dokumen sumber untuk menjaga traceability lintas artefak.
- Bila ada konflik antar dokumen, prioritaskan source of truth, artefak yang paling preskriptif, dan baseline yang paling implementable.

## Source Mapping
- Original source: `Cassy_CICD_Pipeline_Strategy_v1.pdf` (PDF, 14 pages)
- Output markdown: `cassy_cicd_pipeline_strategy_v1.md`
- Conversion approach: text extraction -> normalization -> structural cleanup -> standardized documentation wrapper.
- Note: beberapa tabel/list di PDF dapat mengalami wrapping antar baris; esensi dipertahankan, tetapi layout tabel asli tidak dipertahankan 1:1.
