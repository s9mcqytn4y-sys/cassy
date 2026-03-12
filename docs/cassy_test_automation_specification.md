# Cassy Test Automation Specification

## Document Overview
Prescriptive automation baseline untuk shared core, Android POS, Desktop Backoffice, dan HQ Backend.

## Purpose
• Menetapkan baseline test automation yang langsung bisa dipakai engineering untuk mengubah artefak QA dan CI/CD yang masih berupa baseline desain menjadi sistem quality gate harian yang bisa dijalankan. • Menjaga traceability dari Use Case -> Activity -> Sequence -> Domain Mo...

## Scope
Shared core + Android POS + Desktop Backoffice + Backend Dokumen membahas automation wajib untuk KMP shared, Android POS, desktop JVM, dan backend Go. iOS hanya selective appendix concern. Deliverable depth Full package Strategy, matrix, tooling recommendation, folder placemen...

## Key Decisions / Core Rules
17 critical flow mandatory automated coverage; UI/device automation dipakai selektif; shared core menjadi tulang punggung correctness.

## Detailed Content

### Normalized Source Body
Prescriptive, traceable, and handoff-ready baseline for Shared Core, Android POS, Desktop Backoffice, and
HQ Backend
Phase 1 operational focus: Retail POS, with prepared boundary for F&B and Service
Authoring date: 9 March 2026
Deliverable type: strategy + automation matrix + tooling recommendation + module placement + CI workflow
mapping + sample test skeleton
Executive decisions
Scope dikunci ke Shared business core + Android POS + Desktop Backoffice + Backend. iOS tetap selective lane, bukan
mandatory harian. Fokus automation wave-1 adalah 17 critical flow sebagai mandatory automated coverage; use case lain
tetap punya baseline tetapi tidak dipaksa menjadi UI/E2E. Arsitektur local-first, single operational writer per terminal, outbox
+ batch/item/conflict sync model, dan dual runtime SQLite local + PostgreSQL HQ menjadi constraint utama desain
automation ini.

## 1. Tujuan dokumen
- Menetapkan baseline test automation yang langsung bisa dipakai engineering untuk mengubah artefak QA dan CI/CD
yang masih berupa baseline desain menjadi sistem quality gate harian yang bisa dijalankan.
- Menjaga traceability dari Use Case -> Activity -> Sequence -> Domain Model -> Architecture -> Database ->
Implementation -> Test, sehingga automation tidak lepas dari flow bisnis dan boundary sistem.
- Memberi arahan preskriptif tentang apa yang harus diuji di common/shared, repository/component, integration, UI
local, device/instrumented, desktop functional, dan backend contract/integration.
- Menentukan placement test, lane CI, artefak bukti, naming convention, dan definition of done agar handoff ke
developer maupun QA automation tidak ambigu.
### 1.1 Scope yang disetujui
Decision
Selected option
Prescriptive consequence
Scope
Shared core + Android POS +
Desktop Backoffice + Backend
Dokumen membahas automation wajib untuk KMP shared, Android POS,
desktop JVM, dan backend Go. iOS hanya selective appendix concern.
Deliverable depth
Full package
Strategy, matrix, tooling recommendation, folder placement, CI mapping,
sample skeleton.
Wave-1 target
17 critical flow mandatory
automated
Critical flow P1 harus punya kandidat automation nyata; use case lain
minimal punya baseline planned coverage.
Output
PDF
Dokumen diformat untuk review arsitektur, QA, dan engineering handoff.
## 2. Asumsi penting
- Source of truth tetap artefak target-state project, bukan snapshot repo transisional.
- Phase 1 tetap retail-first dan writer operasional utama untuk checkout/shift adalah terminal POS; model multi-writer
offline dalam satu store belum menjadi baseline harian.
- Implementation specification formal sampai level class/interface final belum tersedia penuh; karena itu mapping
automation dalam dokumen ini sengaja berhenti di level module, bounded context, contract, dan test family yang
implementable.
- Hybrid shared UI diizinkan hanya untuk concern presentasi yang stabil. App shell, permission, printer, scanner,
payment terminal callback, dan lifecycle tetap native; maka strategy automation juga harus memisahkan logic shared
dari fidelity device-heavy.
- SQLite local dan PostgreSQL HQ memiliki failure mode berbeda. Local correctness, migration, idempotency, dan
audit/outbox wajib diuji di boundary masing-masing; jangan memperlakukan keduanya sebagai satu logical DB.
## 3. Evidence base dan traceability input
Artefak sumber
Peran ke Test Automation
UML Source of Truth
Menjaga urutan SDLC, konsistensi istilah, system boundary, dan larangan traceability yang putus.
Use Case / Activity / Sequence
Menentukan flow bisnis, alternate path, exception path, transaction order, dan boundary antar
aktor, UI, service, repository, DB, dan external system.
Domain Model + ERD v2
Menentukan aggregate/invariant, current state vs explanation trail, serta entity yang harus
diverifikasi di test.
Architecture Specification
Menentukan local-first, single writer per terminal, outbox + batch/item/conflict, atomic bundle, layer
ownership, dan runtime boundary.
Module Project Structure
Menentukan placement test, app-shell vs shared UI vs domain/application/data, serta monorepo
file map yang sehat.

Artefak sumber
Peran ke Test Automation
Traceability Matrix
Menentukan critical flow, prioritas risiko, dan gap yang harus diterjemahkan menjadi automation
plan.
Test Specification + CI/CD
Strategy
Menentukan layering, gate harian, runner matrix, required checks, nightly pack, dan evidence
minimum.
## 4. Prinsip Test Automation yang mengikat
- Layering sehat lebih penting daripada memaksa semua test menjadi UI/E2E. Common business logic diuji sedini
mungkin; repository/component dipakai untuk boundary data yang nyata; integration dipakai untuk external boundary;
device/UI dipakai hanya saat fidelity platform benar-benar memengaruhi correctness.
- Business correctness menang atas visual convenience. Approval, role guard, reason code, audit, offline fallback, dan
sync reconcile diuji sebagai keputusan bisnis, bukan sekadar visibility rule UI.
- Atomic bundle harus dibuktikan, bukan diasumsikan. Sale completion, return bundle, shift close, inventory adjustment,
serta audit/outbox append harus divalidasi sebagai satu keputusan bisnis atomik.
- Current state dan explanation trail harus diuji terpisah. inventory_balance bukan pengganti stock_ledger_entry;
sync_conflict bukan noise logging; audit_log dan outbox_event adalah bagian keputusan bisnis.
- Idempotency wajib menjadi first-class concern. Retry lokal, retry sync, replay migration, duplicate callback payment,
dan auth/sync recovery tidak boleh menggandakan efek bisnis.
- Perubahan persistence semantics, sync contract, atau migration tidak boleh dipisahkan dari test verification pada PR
lain. Test, migration verification, dan contract evolution harus hidup di PR yang sama.
- Desktop Backoffice bukan aksesori. Flow reconcile/reporting/admin yang material terhadap business day dan sync
visibility wajib punya baseline automation di desktop/shared yang relevan.
- Backend testing tidak boleh berhenti di unit test murni. API contract, migration checks, sync ingestion, reconcile, dan
convergence state adalah bagian dari automation inti.
## 5. Target operating model untuk automation
### 5.1 Coverage model per layer
Test family
Dipakai ketika
Contoh area wajib
Unit / common
test
Rule bisnis dan application policy dapat
diuji tanpa framework UI/perangkat
pricing, eligibility, totals, role guard, reason resolution, approval
decision, idempotency logic, sync item state machine
Component /
repository test
Repository, transaction coordinator,
SQLDelight adapter, dan local DB
behavior harus diuji dengan DB lokal/stub
sale/return/shift/inventory atomic bundle, audit+outbox commit,
FK/integrity, migration replay
Integration test
Ada boundary ke payment, identity,
loyalty, HQ sync, printer, atau workflow
lintas module
payment declined/pending, auth fallback, sync partial failure,
conflict reconcile, printer intent/result
UI local / desktop
functional
State flow dan evidence bisnis
dipengaruhi presentasi, tetapi tidak
memerlukan real device
desktop reporting/reconcile/admin, Android local UI logic, error
rendering, session restore UI state
Instrumented /
device
Scanner, printer, lifecycle, callback SDK,
atau OS fidelity memengaruhi correctness
scanner input, printed receipt flow, Android lifecycle edge, smoke
package install
Manual / UAT
Kebutuhan usability, operational review,
atau scenario lapangan masih
memerlukan penilaian manusia
shift handover, offline emergency operation, approval escalation,
operational review
### 5.2 Mandatory platform scope

Platform / runtime
Status
Prescriptive expectation
Shared KMP core
Mandatory PR gate
Domain/application/data test menjadi tulang punggung correctness dan harus
memuat mayoritas critical-flow logic.
Android POS
Mandatory PR gate
Local tests dominan; instrumented hanya subset fidelity tinggi di nightly/RC.
Desktop Backoffice
Mandatory PR gate
Unit/component/functional untuk reporting, reconcile, admin, end-of-day
visibility.
Go backend +
PostgreSQL
Mandatory PR gate
go test, API contract, migration check, sync ingestion/reconcile tests.
iOS selective lane
Informational by
default
Muncul bila path iOS/shared UI tersentuh; bukan pengganti gate
Android/Desktop/Backend.

## 6. Prioritas wave-1 automation
Strategi yang direkomendasikan adalah hybrid: 17 critical flow P1 harus memiliki automation wajib, sedangkan use case
lain tetap memiliki baseline planned coverage dan sebagian tetap manual/UAT sampai implementation detail lebih
stabil. Ini menghindari dua ekstrem yang sama buruknya: mengotomasi semuanya secara dangkal, atau hanya memilih
happy path checkout lalu buta terhadap auth/audit/sync.
### 6.1 Kelompok critical flow mandatory
Group
Representative critical flows
Dominant test family
Shift & cash
Mulai shift, tutup shift, safe drop, cash reconciliation,
tutup hari
unit/common + component + integration + selective
UI
Sales & payment
Proses pembayaran, sale completion, receipt issuance,
payment decline/pending
unit/common + component + payment integration
Returns &
approval
return/refund, approval supervisor, policy gate
unit/common + component + integration
Inventory high-risk
receiving discrepancy, stock adjustment, cycle count
variance besar, damaged/expired handling
component + integration + selective UI
Auth & audit
offline auth fallback, role guard, audit
fallback/unavailable storage
unit/common + integration + component
Sync & reconcile
sinkronisasi data toko, partial failure, conflict resolution,
desktop reconcile flow
unit/common + integration + desktop functional +
backend contract
### 6.2 Non-negotiable database checks
- Foreign keys harus dinyalakan dan diverifikasi per connection database test/runtime; SQLite tidak mengaktifkannya
otomatis.
- Uniqueness dan idempotency harus diuji untuk receipt/invoice/business number, idempotency key, resume token, dan
sync batch/item handling.
- Pemisahan current state vs explanation trail wajib dibuktikan: inventory_balance tetap current state;
stock_ledger_entry tetap canonical explanation trail.
- Semua bundle transaksi lokal penting harus terbukti atomik: sale completion, return bundle, shift close bundle,
inventory adjustment bundle, serta audit/outbox append.
- sync_conflict tidak boleh berakhir tanpa resolution_status yang eksplisit; outbox_event gagal tidak boleh hilang tanpa
history retry/error.
## 7. Module ownership dan test placement
### 7.1 Rule of placement
Module group
Harus diuji di sini
Jangan dorong ke sini
apps/*/app-shell
bootstrap, feature flag wiring, connectivity
observer, route host, session restore
rule bisnis inti, SQL langsung, DTO transport
apps/*/platform-device/
*
printer bridge, scanner lifecycle, payment
SDK callback, permission adapter
orchestration lintas context, state machine bisnis
shared/*/domain
aggregate invariant, value object, domain
policy, state machine
Android API, Compose widget, raw SQL, vendor SDK
shared/*/application
use case, facade, transaction boundary,
approval/permission business flow
UI toolkit, lifecycle, query interface terekspos ke UI
shared/*/data
repository impl, SQLDelight adapter, DTO
mapping, outbox persistence
Compose state, navigation, business branching yang
harusnya di domain/application

Module group
Harus diuji di sini
Jangan dorong ke sini
backend/internal/*
API handler contract, app service, repo
integration, migration, sync
converge/reconcile
client UI concern, ad hoc business duplication
### 7.2 Recommended file map
docs/test/
Cassy_Test_Automation_Specification.pdf
shared/
kernel/domain/src/commonTest/...
sales/application/src/commonTest/...
inventory/data/src/commonTest/...
db/src/commonTest/... # migration replay + FK/integrity
apps/android-pos/
src/test/... # JVM local tests
src/androidTest/... # device/instrumented selective
apps/desktop-backoffice/
src/test/... # desktop functional/component
backend/
internal/.../*_test.go
db/postgres/migrations/
internal/testkit/...
tooling/
scripts/
ci/
.github/workflows/
pr-gate-summary.yml
shared-fast.yml
android-pos-fast.yml
desktop-fast.yml
backend-fast.yml
android-device-nightly.yml
ios-selective-info.yml
## 8. Tooling stack yang direkomendasikan
Area
Recommendation
Reasoning
KMP shared
kotlin.test + coroutine test utilities +
fake/stub-first approach
Mayoritas business rule harus bisa diuji lintas platform, cepat, dan
stabil di PR gate.
Android local
JUnit-based local tests, Robolectric hanya
bila perlu, fake payment/identity/printer
adapters
Fokus ke speed dan maintainability; jangan refleks memakai
instrumented.
Android device
Compose/UI instrumented test untuk
scanner/receipt/lifecycle fidelity
Dipakai selektif saat environment nyata memengaruhi correctness.
Desktop
JVM test + desktop functional test
harness
Reporting/reconcile/admin butuh evidence perilaku layar tanpa
harus jadi E2E mahal.
SQLite/SQLDelig
ht
migration replay test, FK verification
helper, query snapshot helper
Persistence semantics adalah first-class risk di local-first.
Backend Go
go test, API contract test,
migration/integration test, sync
ingestion/reconcile harness
Server convergence dan sync authority tidak boleh hanya diuji unit.
Cross-cutting
test data builders, fixture pack,
deterministic clock/idgen, fake network
fault injector
Mengurangi flakiness dan membuat scenario offline/retry/reconcile
terkontrol.
## 9. External integration automation strategy

Boundary
Default strategy
Mandatory scenarios
Payment gateway /
EDC
Adapter-level integration +
callback contract + selective
device
declined, timeout, pending investigation, duplicate callback, recovery
after retry
Identity / auth
integration with fallback policy +
shared role-guard tests
online-first login, cached grant fallback, unauthorized offline, supervisor
approval PIN
Loyalty
contract/integration subset
point accrual eligibility, service unavailable degradation, duplicate
posting protection
HQ sync
client integration + backend
contract/integration
batch partial failure, retry, conflict creation, reconciliation visibility,
idempotent re-send
Printer / scanner
platform adapter tests +
selective device
printed receipt flow, barcode/scanner input normalization,
lifecycle/interruption handling

## 10. CI/CD workflow mapping
### 10.1 Lane policy
Lane / job
Primary purpose
Minimum automation pack
pr-gate-summary
single truth of scope/result
orchestration
must always appear; no silent skip ambiguity
shared-fast
fast correctness for shared
core
compile, unit/common, component subset, SQLDelight migration verify,
static analysis
android-pos-fast
Android POS local confidence
assemble, local unit/component, selective UI-local tests; no heavy
instrumented suite
desktop-fast
desktop backoffice confidence
build + unit/component/functional for reporting/reconcile/admin
backend-fast
server convergence and
contract
go test, postgres migration checks, API contract/unit tests
android-device-night
ly
high-fidelity device regression
scanner, printer, lifecycle, install/smoke, extended sync fault pack
ios-selective-info
cross-platform visibility for
shared UI changes
informational on most PR; becomes visible when iOS/shared-ui path
touched
release / RC
promotion confidence, not
daily replacement
smoke package, selected device suite, manual/UAT evidence,
changelog/manifest
### 10.2 Rules that may not be waived
- Perubahan sync contract atau persistence semantics tidak boleh dipisahkan dari test dan migration verification pada
PR berbeda.
- Semua perubahan SQLDelight harus hadir sebagai migration file dan diverifikasi di CI.
- Android instrumented test bukan required daily gate default; gunakan hanya ketika local test tidak cukup
merepresentasikan fidelity platform.
- Backend migration check bukan opsional. Perubahan PostgreSQL, ingestion, atau reconciliation state harus punya
automated verification pada PR yang sama.
- Required check names harus stabil; pipeline tidak boleh menyebabkan Pending check hanya karena path filter yang
buruk.
## 11. Prescriptive automation matrix
Context
Common / Component
Integration
UI / Device
Backend / Contract
Kernel
reason code, approval, audit intent, role
guard, idempotency metadata
identity fallback subset
approval UI selective
n/a
Sales
pricing, totals, payment state transition,
sale completion bundle
payment callback /
loyalty subset
receipt preview +
printed receipt
selective
sales payload contract
Returns
policy gate, refund linkage, store credit
rule
payment refund /
approval integration
return approval UI
selective
return settlement contract
Cash
shift/session invariant, reconciliation
bundle, business day close
identity/approval, outbox
enqueue
desktop close day /
reconcile flow
cash summary ingest/report
contract
Inventory
adjustment/cycle count/receiving
component tests
barcode/scanner subset,
sync posting
scanner lifecycle
selective
inventory ingest/report contract
Reporting
query facade correctness
sync visibility/reconcile
fetch
desktop report/admin
screens
report aggregation contract

Context
Common / Component
Integration
UI / Device
Backend / Contract
Sync
state machine, retry, conflict status,
outbox replay
HQ partial failure/conflict
sync status screen
selective
ingestion/reconcile/idempotenc
y mandatory
## 12. Sample test skeleton and naming convention
Gunakan naming yang membuat failure mudah ditelusuri ke bounded context, use case, dan skenario. Hindari nama
generik seperti TestPaymentUseCase atau IntegrationTest1.
// Common / shared
class CompleteSale_IdempotencyTest
class StartShift_ApprovalPolicyTest
class SyncStateMachine_PartialFailureTest
// Component / repository
class SaleRepository_AtomicBundleComponentTest
class ShiftCloseRepository_AuditOutboxComponentTest
class SqlDelightMigrationReplay_DbContractTest
class InventoryAdjustment_FkIntegrityComponentTest
// Integration
class PaymentGateway_DeclinedThenRetry_IntegrationTest
class IdentityService_OfflineFallbackPolicy_IntegrationTest
class HqSync_PartialFailureAndConflict_IntegrationTest
// Android device
class ReceiptPrint_DeviceTest
class ScannerLifecycle_Reentry_DeviceTest
// Desktop
class ReconcileScreen_ConflictResolution_FunctionalTest
// Backend Go
func TestSyncIngest_IdempotentReplay(t *testing.T)
func TestPostgresMigration_FreshInstallAndUpgrade(t *testing.T)
## 13. Evidence and reporting contract
- Setiap eksekusi minimal menyimpan identifier test case, build/version, nama environment, dan timestamp eksekusi.
- Input data uji penting harus terlihat: user/role, terminal, business day, payment stub scenario, connectivity mode, dan
initial DB state.
- Bukti hasil mencakup screenshot/rekaman bila relevan, application log, integration response, dan snapshot query
untuk tabel kunci seperti sale_transaction, payment, audit_log, outbox_event, sync_batch/sync_item/sync_conflict,
cashier_session, inventory_balance, stock_ledger_entry.
- Untuk failure, retry, offline, dan recovery, sertakan state sebelum dan sesudah recovery agar reconciliability dapat
diverifikasi.
- Status yang disarankan per case: Planned / In Progress / Automated / Blocked / Passing / Failing / Quarantined.
Jangan campur status implementasi dengan status eksekusi.
## 14. Rollout plan yang direkomendasikan
Phase
Focus
Exit criteria
## 0. Control plane
Job names stabil, required checks selalu
muncul, test result publishing siap
Tidak ada Pending check ambigu; baseline artefak test tersimpan.
## 1. Shared +
backend
correctness
Common tests, DB contract, migration
replay, FK checks, backend contract
Local-first invariant, schema evolution, dan sync contract sudah
masuk gate harian.
## 2. Android POS
+ Desktop
Pindahkan logic liar ke boundary testable;
aktifkan android-pos-fast dan desktop-fast
Critical POS + backoffice flow material terlindungi di PR gate.

Phase
Focus
Exit criteria
## 3. Nightly quality
Tambah instrumented/device, sync-fault
pack, smoke install
Coverage fidelity naik tanpa merusak kecepatan PR.
## 4. Release /
promotion
RC smoke, signed package, manual
evidence, operational checklist
Promotion repeatable, traceable, dan tidak menggantikan fast
checks.

## 15. Traceability appendix - critical flow starter pack
Critical flow
Primary contexts
Mandatory automated families
Key persistence evidence
CF-01 Start Shift
cash + kernel + auth
common, component, identity
integration
business_day, cashier_session, opening_cash,
audit_log
CF-02 Payment /
complete sale
sales + kernel +
integrations
common, component, payment
integration
sale_transaction, payment, receipt, audit_log,
outbox_event
CF-05 Return /
refund
returns + sales +
kernel
common, component, integration
return_transaction, refund linkage, stock_ledger_entry,
audit_log
CF-08 Close shift /
reconcile
cash + reporting
component, desktop functional,
integration subset
cash_reconciliation, cashier_session, shift_report,
outbox_event
CF-11 Stock
adjustment
inventory + kernel
component, integration, selective UI
stock_adjustment, stock_ledger_entry,
inventory_balance, audit_log
CF-12 Cycle count
variance
inventory + approval
common, component, integration
cycle_count, stock_adjustment(optional), audit_log
CF-13 Store sync
sync + backend
common, integration, desktop
functional, backend contract
sync_batch, sync_item, sync_conflict, outbox_event
CF-16 Offline auth
fallback
auth + kernel
common, integration
employee role/pin state, session cache, audit_log
CF-17 Audit log
fallback
kernel + sync
component, integration
audit_log, outbox_event
## 16. Final recommendations
- Jangan mulai dari device test. Mulai dari common + component test untuk memindahkan business invariants ke
boundary yang benar.
- Jangan jadikan Android POS satu-satunya fokus. Desktop Backoffice dan backend sync/reconcile adalah bagian
correctness phase 1, bukan secondary citizen.
- Jangan menunda migration replay dan FK verification sampai release lane. Itu harus masuk PR gate untuk shared dan
backend.
- Jangan menghilangkan use case supporting seperti auth, approval, audit, dan sync reconcile dari scope automation.
Justru di local-first POS area ini adalah sumber blind spot paling mahal.
- Begitu implementation spec dan class/interface final tersedia, turunkan dokumen ini menjadi execution matrix per test
case dengan owner, status, flakiness, runtime, dan evidence link.


## Constraints / Policies
Mapping berhenti di level module/bounded context/contract karena implementation spec class-level belum tersedia penuh.

## Technical Notes
Automation harus membuktikan atomic bundle, idempotency, sync correctness, dan auditability.

## Dependencies / Related Documents
- `store_pos_test_specification.md`
- `traceability_matrix_store_pos.md`
- `cassy_architecture_specification_v1.md`
- `cassy_module_project_structure_specification.md`
- `store_pos_erd_specification_v2.md`
- `cassy_cicd_pipeline_strategy_v1.md`

## Risks / Gaps / Ambiguities
- Tidak ditemukan gap fatal saat ekstraksi. Tetap review ulang bagian tabel/angka jika dokumen ini akan dijadikan baseline implementasi final.

## Reviewer Notes
- Struktur telah dinormalisasi ke Markdown yang konsisten dan siap dipakai untuk design review / engineering handoff.
- Istilah utama dipertahankan sedekat mungkin dengan dokumen sumber untuk menjaga traceability lintas artefak.
- Bila ada konflik antar dokumen, prioritaskan source of truth, artefak yang paling preskriptif, dan baseline yang paling implementable.

## Source Mapping
- Original source: `Cassy_Test_Automation_Specification.pdf` (PDF, 11 pages)
- Output markdown: `cassy_test_automation_specification.md`
- Conversion approach: text extraction -> normalization -> structural cleanup -> standardized documentation wrapper.
- Note: beberapa tabel/list di PDF dapat mengalami wrapping antar baris; esensi dipertahankan, tetapi layout tabel asli tidak dipertahankan 1:1.
