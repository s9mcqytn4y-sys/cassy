# Cassy Migration Script Specification

## Document Overview
Migration baseline untuk SQLite/SQLDelight local clients dan PostgreSQL HQ backend, dengan wave plan, verification, dan rollback guidance.

## Purpose
• Menetapkan baseline migration yang langsung bisa dipakai engineering untuk menurunkan perubahan schema dari baseline lama ke target-state artefak proyek. • Menjaga traceability dari artefak SDLC hingga keputusan migration, rollout, verification, rollback, dan ownership imple...

## Scope
Phase 1 Retail POS as operational focus, with prepared boundary for F&B and Service Approved decisions Dual-track migration (SQLite local + PostgreSQL HQ), hybrid migration strategy, dual path fresh install + upgrade path Recommended wave-1 Shared Kernel Shift & Cash Control S...

## Key Decisions / Core Rules
Hybrid migration strategy dengan dominan expand → backfill → verify → contract untuk domain high-risk; retail kernel diprioritaskan lebih dulu.

## Detailed Content

### Normalized Source Body
Prescriptive baseline · Page 1
Prescriptive, traceable, and implementation-ready migration baseline
for SQLite / SQLDelight local clients and PostgreSQL HQ backend
Field
Value
Document type
Migration strategy + migration script handoff
package
Scope
Phase 1 Retail POS as operational focus, with
prepared boundary for F&B and Service
Approved decisions
Dual-track migration (SQLite local +
PostgreSQL HQ), hybrid migration strategy,
dual path fresh install + upgrade path
Recommended wave-1
Shared Kernel
Shift & Cash Control
Sales &
->
->
Checkout
Authoring date
2026-03-08
Evidence base
UML Source of Truth, Use Case, Activity,
Sequence, Domain Model, ERD v2,
Architecture, Module Structure, Traceability
Matrix, Test Specification, CI/CD Strategy
Executive decision summary
- 
Local SQLite remains the immediate operational source of truth on each terminal; HQ
PostgreSQL is the consolidation, sync-authority, and reporting source.
- 
Schema evolution must be split into two disciplined tracks: SQLDelight migrations for local
clients and PostgreSQL-native migrations for HQ backend.
- 
The migration program must prefer expand
backfill
verify
contract for high-risk
->
->
->
domains, with limited big-bang only for low-risk additions.
- 
Prepared boundaries for F&B and Service stay as non-shipping stubs until the retail kernel is
stable and observable.

Prescriptive baseline · Page 2
## 1. Tujuan dokumen
- 
Menetapkan baseline migration yang langsung bisa dipakai engineering untuk menurunkan
perubahan schema dari baseline lama ke target-state artefak proyek.
- 
Menjaga traceability dari artefak SDLC hingga keputusan migration, rollout, verification,
rollback, dan ownership implementasi.
- 
Menyediakan lampiran draft SQL terpisah untuk SQLite local client dan PostgreSQL HQ
backend sebagai starter pack implementasi.
## 2. Asumsi penting
- 
Dokumen ini memakai target-state artefak proyek sebagai source of truth desain, bukan repo
snapshot saat ini.
- 
Fresh install dan upgrade path sama-sama harus didukung; karena itu dokumen ini
memisahkan target schema, migration sequencing, dan compatibility handling.
- 
Exact current Schema.sq / current repo DDL tidak tersedia sebagai artefak yang dapat
diverifikasi langsung di percakapan ini. Maka draft SQL di lampiran bersifat preskriptif dan
siap handoff, tetapi tetap memerlukan reconciliation terakhir terhadap nama kolom/tabel
aktual di implementation snapshot sebelum dieksekusi.
- 
Phase 1 operasional difokuskan pada Retail POS; F&B dan Service hanya dipertahankan sebagai
prepared boundary agar tidak mencemari kernel retail dengan premature shipping scope.
## 3. Evidence base dan traceability input
Artefak sumber
Peran ke migration
UML-Modeling-Source-of-Truth.txt
Baseline istilah, boundary bisnis, dan
konsistensi nomenklatur lintas artefak.
Use Case Detail Specifications
Menentukan functional scope yang wajib tetap
survive sesudah migration.
Activity Detail Specifications
Menentukan alternate path, exception path,
dan kebutuhan persistable state.
Sequence Detail Specifications
Menentukan atomic bundle, boundary
transaksi, dan urutan interaksi yang harus
tetap valid sesudah perubahan schema.
Domain Model Detail Specifications v2
Menentukan aggregate root, invariant, dan
entitas target-state yang menjadi dasar
pemecahan wave migration.
Store POS ERD Specification v2
Menentukan target schema, gap terhadap
baseline lama, dan arah refactor data model.
Cassy Architecture Specification v1
Menentukan dual runtime boundary:
SQLite/SQLDelight local-first client dan
PostgreSQL HQ backend, plus rule

Prescriptive baseline · Page 3
Artefak sumber
Peran ke migration
sync/outbox/audit.
Module Project Structure Specification
Menentukan ownership modul, repo
placement, dan guardrail agar migration tidak
merusak boundary bisnis.
Traceability Matrix Store POS
Menentukan keterlacakan dari use case ke
implementation impact dan migration gap.
Test Specification + CI/CD Strategy
Menentukan verification gate, migration
replay, FK/integrity verification, dan
automation scope yang wajib ikut di PR yang
sama.
## 4. Final migration decisions
Decision
Selected option
Prescriptive consequence
Scope
SQLite local + PostgreSQL HQ
Semua deliverable harus
dipisah per runtime boundary;
jangan gabungkan script client
dan HQ dalam satu chain yang
ambigu.
Deliverable type
Full package
Dokumen harus memuat
strategy, wave plan, migration
matrix, verification, rollback,
dan lampiran SQL.
Baseline source
Fresh install + upgrade path
Harus ada target clean schema
dan jalur upgrade terkontrol
dari baseline lama.
Strategy
Hybrid, dominan expand-
contract
Domain high-risk memakai
staged migration; area
sederhana boleh big-bang bila
tidak mengancam correctness.
Phase scope
Retail POS + prepared
F&B/Service boundary
Schema retail jadi prioritas
shipping. F&B/Service hanya
menjaga compatibility
boundary, bukan aktivasi fitur
penuh.
Recommended wave-1
Shared Kernel
Shift & Cash
->
Control
Sales & Checkout
->
Urutan ini mengurangi risiko
karena identity, terminal,
business day, dan session
boundary harus stabil
sebelum transaksi sales

Prescriptive baseline · Page 4
Decision
Selected option
Prescriptive consequence
dipindahkan.
Local SQL style
Generic SQL + SQLDelight
mapping
Dokumen tetap readable
untuk review, tetapi setiap
langkah local harus dapat
dipetakan ke file migration
SQLDelight.
HQ SQL style
PostgreSQL-native
Index, constraint, and
transactional DDL boleh
memakai fitur Postgres bila
memang memberi kejelasan
operasional.
Backfill depth
High-risk domains only
Backfill rules wajib detail
minimal untuk inventory,
shift/cash, return/refund, dan
sync.
Rollback
Moderate per-step guidance
Rollback tidak boleh sekadar
slogan; setiap wave harus
punya decision point untuk
continue, hold, atau forward-
fix.
Traceability appendix
Mandatory
Setiap wave harus dapat
ditautkan kembali ke
domain/use case/artefak
sumber yang memaksa
perubahan tersebut.
## 5. Prinsip migration yang mengikat
1.
Pisahkan current state dari historical explanation trail. InventoryBalance adalah current state;
StockLedgerEntry adalah jejak penjelasan. Migration tidak boleh menyederhanakan keduanya
menjadi satu tabel serbaguna.
2.
Jaga atomic bundle tetap utuh. Completion sale, return, shift close, inventory adjustment, audit,
dan outbox intent harus tetap commit sebagai satu kesatuan bisnis dari sudut pandang local
client.
3.
Jangan biarkan migration merusak business boundary. Sales tidak boleh mengambil alih ledger
stok secara ad hoc, dan F&B/Service tidak boleh membuat sales/cash/inventory ledger paralel.
4.
Dual identity wajib dipertahankan. Primary key opaque (prefixed UUIDv7) dipisahkan dari
business number seperti receipt_no, invoice_no, business_day_no, transfer_no.

Prescriptive baseline · Page 5
5.
Local-first bukan cache-first. SQLite pada terminal adalah operational store, bukan sekadar
mirror data server; karena itu local migration harus mendapat disiplin yang sama seriusnya
dengan backend migration.
6.
Outbox saja tidak cukup. Untuk target-state, outbox_event harus diekspansi dengan sync_batch,
sync_item, sync_conflict, dan offline_operation_window agar orchestration sync menjadi
explainable dan testable.
7.
Retail kernel lebih penting daripada cakupan fitur luas. Prepared boundary F&B/Service
dipertahankan tanpa mengorbankan kestabilan retail kernel.
## 6. Target-state migration architecture
Runtime boundary
Database role
Migration vehicle
Non-negotiable rule
Client / terminal
Immediate
operational source of
truth pada device
boundary
SQLDelight migration
files + local
repository/component
tests
Foreign keys enabled
per connection; WAL
mode; transactions
coordinated in
application services.
HQ backend
Consolidation, sync
authority,
reconciliation, long-
horizon reporting
PostgreSQL migration
chain + backend
integration tests
Migrations live in
backend/db/postgres/
migrations and evolve
together with sync/API
contracts.
Boundary clarification
- 
SQLite local dan PostgreSQL HQ tidak boleh diperlakukan sebagai satu database logical yang
dimigrasi sekaligus. Mereka mempunyai writer model, failure mode, dan rollback posture
yang berbeda.
- 
Schema client dioptimalkan untuk operational correctness offline-first. Schema HQ
dioptimalkan untuk consolidation, reconciliation, reporting, dan sync authority.
- 
Contract evolution harus sinkron: entity identity, outbox/sync payload, status machine, dan
reconciliation endpoint tidak boleh berubah diam-diam di salah satu sisi.
## 7. Wave plan yang direkomendasikan
Wave
Domain focus
Why now
Migration
posture
Exit criteria
Wave 1
Shared Kernel
Menstabilkan
identity,
terminal,
business day,
reason, approval,
Expand +
selective backfill
pos_terminal,
business_day,
reason
expansion,
approval/audit

Prescriptive baseline · Page 6
Wave
Domain focus
Why now
Migration
posture
Exit criteria
audit, dan role
boundary
sebelum
transaksi inti
dipindahkan.
consistency live;
compatibility
bridge tersedia.
Wave 2
Shift & Cash
Control
Sales bergantung
pada
shift/terminal/bu
siness day yang
valid; cash
correctness tidak
boleh jadi
afterthought.
Expand + backfill
+ dual-read
cutover
cashier_session/
cash_session
telah punya
terminal_id/busi
ness_day_id, plus
safe_drop dan
cash_reconciliati
on.
Wave 3
Sales & Checkout
Setelah session
boundary stabil,
sale/payment/rec
eipt/invoice
dapat diturunkan
tanpa ambiguity
numbering dan
ownership.
Expand +
selective big-
bang for low-risk
columns
sale_transaction,
payment
allocation,
receipt, invoice,
suspended_sale
kompatibel
dengan target-
state.
Wave 4
Inventory
High-risk karena
menyentuh
current state vs
explanation trail
serta banyak
event source.
Expand-contract
dengan backfill
kuat
inventory_balanc
e menjadi source
of truth;
stock_ledger_entr
y menjelaskan
semua mutasi
sah.
Wave 5
Return & Refund
Butuh
dependensi sale,
inventory,
approval, dan
refund semantics
yang sudah
stabil.
Expand-contract
return_transactio
n, return_line,
policy decision,
refund/store
credit flow aktif.
Wave 6
Sync
orchestration +
reporting
hardening
Menutup gap
outbox-only dan
membuat
operational
observability
explainable.
Expand + cutover
sync_batch/
item/conflict,
offline_operation
_window,
master_data_sna
pshot dan
reporting
dependencies

Prescriptive baseline · Page 7
Wave
Domain focus
Why now
Migration
posture
Exit criteria
stabil.
Catatan: prepared boundary F&B dan Service dipertahankan sepanjang
wave 1-6 hanya sejauh diperlukan untuk menjaga relasi ke
sales/cash/inventory kernel yang sama. Tidak ada shipping activation baru
di dokumen ini.
## 8. Detailed migration plan per domain
### 8.1 Shared Kernel
- 
Tambahkan pos_terminal sebagai entitas wajib untuk mengikat uniqueness cashier-terminal
dan numbering scope operasional.
- 
Tambahkan business_day sebagai aggregate operasional lintas shift, sales, cash exception, dan
end-of-day reporting.
- 
Perluas reason_code.category agar tidak berhenti di ADJUST dan VOID; minimal tambah
RETURN, RECEIVING_GAP, SAFE_DROP, DAMAGE, PRICE_OVERRIDE, TRANSFER,
CYCLE_COUNT.
- 
Normalisasikan approval_request agar request_type, entity_type, dan entity_id konsisten lintas
domain.
- 
Perkuat audit_log untuk event audit store yang cross-cutting dan idempotency-aware.
### 8.2 Shift & Cash Control
- 
Upgrade cashier_session agar wajib mereferensi terminal_id dan business_day_id.
- 
Pisahkan concern cash_session, safe_drop, dan cash_reconciliation; jangan biarkan satu tabel
besar menanggung opening, movement, drop, dan close tanpa struktur yang eksplisit.
- 
Pastikan invariant: satu shift aktif per kombinasi cashier-terminal; opening cash ada sebelum
cash sale pertama; business day tidak boleh close bila masih ada shift aktif atau issue
rekonsiliasi kritis.
- 
Backfill shift lama memakai terminal default per device/terminal mapping dan business day
hasil derivasi tanggal operasional store.
### 8.3 Sales & Checkout
- 
Tambahkan business_day_id dan terminal_id pada sale_transaction serta entitas yang harus
ikut numbering/settlement scope.
- 
Pastikan payment_allocation tersedia bila satu pembayaran dapat dialokasikan ke sale, invoice,
refund, atau store credit.
- 
Pertahankan receipt/invoice sebagai business artifact tersendiri dengan readable number,
bukan foreign key surrogate yang membawa makna dokumen.

Prescriptive baseline · Page 8
- 
Pastikan suspended_sale tetap dapat berjalan tanpa merusak numbering dan audit path.
### 8.4 Inventory
- 
Turunkan source of truth stok dari product.stock_qty_milli ke inventory_balance.
- 
Migrasikan stock_movement lama ke stock_ledger_entry; bila perlu sediakan legacy
compatibility view/table sementara selama cutover.
- 
Pastikan setiap perubahan stok sah menghasilkan stock_ledger_entry dengan source_type dan
source_id yang jelas.
- 
Backfill inventory_balance dari saldo current-state lama, lalu rekonsiliasi terhadap ledger hasil
mapping event; selisih wajib masuk queue review, bukan diabaikan.
### 8.5 Return & Refund
- 
Jangan biarkan refund_record berdiri sendiri sebagai pseudo-return. Bentuk aggregate baru:
return_transaction, return_line, return_policy_decision, refund/store credit artifacts.
- 
Link return ke sale source, approval rule, refund effect, dan stock effect secara eksplisit.
- 
Pisahkan cash refund, payment reversal, dan store credit issuance sebagai business effect yang
berbeda.
### 8.6 Sync orchestration dan reporting hardening
- 
Pertahankan outbox_event sebagai capture intent, lalu tambahkan sync_batch, sync_item,
sync_conflict, master_data_snapshot, dan offline_operation_window untuk orchestration yang
explainable.
- 
Batch/item/conflict state harus menjadi dasar supervisor/manager reconciliation UI; jangan
bergantung pada log scraping atau error text bebas.
- 
Inbound updates harus tetap masuk via application service, bukan raw mutation script yang
memotong invariants bisnis.
Legacy area / symptom
Target-state direction
Migration handling
product.stock_qty_milli as
stock source
Derived/cache only;
inventory_balance becomes
source of truth
Expand inventory tables,
backfill balance, keep derived
column temporary, then
deprecate writes.
stock_movement too narrow
stock_ledger_entry as
canonical explanation trail
Map old movement rows into
ledger entries; expose
compatibility view
temporarily.
employee.role too narrow
Role / permission model
covers supervisor, inventory
staff, service staff, store
manager
Backfill roles conservatively;
default least privilege on
ambiguous records.
reason_code category only
## Adjust / Void
Cross-domain reason
categories
Seed expanded reason
catalogue before cutover flows
need it.

Prescriptive baseline · Page 9
Legacy area / symptom
Target-state direction
Migration handling
cashier_session / cash_session
lack terminal and business day
Session model anchored to
terminal and business day
Add columns, backfill
mapping, enforce uniqueness
after cleanup.
refund_record has no return
aggregate
return_transaction +
return_line +
policy/refund/store credit
artifacts
Introduce new aggregate first;
dual-read old refund record
during transition if needed.
outbox_event insufficient for
orchestration
outbox + sync_batch +
sync_item + sync_conflict +
offline window
Add orchestration tables
before enabling richer sync
worker.
## 9. SQLDelight local migration rules
- 
Setiap perubahan schema local harus hadir sebagai SQLDelight migration file dan diverifikasi
di CI pada PR yang sama dengan perubahan query, repository, dan use case terkait.
- 
Aktifkan foreign_keys untuk setiap koneksi database test dan runtime; jangan mengandalkan
DDL saja.
- 
Untuk perubahan struktural berat di SQLite, prioritaskan pattern create new table
backfill
->
->
validate
rename/swap daripada alter beruntun yang rapuh.
->
- 
Gunakan WAL mode untuk concurrency read/write lokal, tetapi perlakukan file DB sebagai
single-host asset; tidak ada peer replication antar terminal.
- 
Repository/data source adalah satu-satunya tempat yang boleh memetakan row SQLDelight ke
model aplikasi/domain; UI tidak boleh menyentuh generated query interface secara langsung.
- 
Semua retriable business mutation harus menulis audit dan outbox intent di transaksi lokal
yang sama dengan perubahan bisnisnya.
Recommended local file layout
- 
shared/db/src/commonMain/sqldelight/.../Schema.sq - target-state schema ownership.
- 
shared/db/src/commonMain/sqldelight/migrations/ - ordered migration files.
- 
shared tests/component tests - migration replay, FK verification, atomic bundle verification,
audit+outbox consistency.
## 10. PostgreSQL HQ migration rules
- 
Tempatkan chain migration di backend/db/postgres/migrations dan treat migration as first-class
citizen bersama API contract dan sync contract.
- 
Gunakan expand
backfill
verify
contract untuk perubahan yang memengaruhi ingestion
->
->
->
sync, reconciliation, atau reporting state.

Prescriptive baseline · Page 10
- 
Boleh memakai PostgreSQL-native features bila membantu correctness dan observability,
misalnya partial indexes atau richer constraints.
- 
Jangan terima destructive contract break tanpa compatibility window untuk client yang belum
seluruhnya berada pada versi schema atau payload baru.
- 
Idempotency key, version columns, dan sync status columns harus sejajar dengan state
machine sync yang diharapkan aplikasi client.
## 11. Verification, rollout, dan rollback
Gate
Required evidence
Why it exists
Migration replay
Fresh install path + upgrade
path replayable
Mencegah migrasi hanya lolos
pada database kosong atau
hanya pada satu snapshot
spesifik.
FK / integrity verification
Foreign keys active per
connection; orphan checks;
uniqueness checks
SQLite tidak mengaktifkan FK
otomatis dan target-state
sangat bergantung pada
relational correctness.
Atomic bundle tests
sale completion, return, shift
close, inventory adjustment,
audit+outbox commit
Architecture menuntut
transaction bundle tetap utuh
dari sudut pandang bisnis.
Backfill verification
row count, checksum/business
aggregate match, exception
queue report
Mencegah silent data drift
ketika memindahkan current-
state, ledger, dan session
semantics.
Sync compatibility tests
batch/item/conflict state,
partial failure, retry
idempotency
Outbox-only baseline tidak
cukup untuk target-state
orchestration.
Operational smoke
open shift, cash sale, close
shift, return, inventory
adjustment, sync retry
Membuktikan migration tidak
sekadar valid secara DDL
tetapi juga survive real
workflow.
Wave
Rollback posture
Recommended action when
verification fails
Wave 1 Shared Kernel
Forward-fix friendly
Hold constraint enforcement,
preserve bridge
columns/tables, correct
mapping, rerun validation.
Wave 2 Shift & Cash
Controlled stop-the-line
Do not enable final uniqueness
and close invariants until
backfill of

Prescriptive baseline · Page 11
Wave
Rollback posture
Recommended action when
verification fails
terminal/business_day is
clean.
Wave 3 Sales
Stop-the-line for
numbering/settlement
mismatch
Preserve old read path
temporarily; block cutover
until numbering and
settlement aggregates match.
Wave 4 Inventory
Strict review queue, avoid
blind rollback
Never overwrite mismatched
balances silently. Freeze
cutover, inspect discrepancy
queue, apply controlled
correction.
Wave 5 Return
Forward-fix or temporary
dual-read
Maintain compat read for
refund_record until return
aggregate verification passes.
Wave 6 Sync
Feature-flagged cutover
Disable new worker
orchestration path, keep intent
capture, inspect batch, item,
and conflict evidence.
## 12. Ownership, repo placement, dan delivery package
Area
Owner module/path
Deliverable expectation
Local schema + migration
KMP shared/db and bounded-
context data modules
Schema changes, ordered
migration files, repository
adaptation, component tests.
Application cutover
sales / cash / inventory /
returns / sync application
modules
Use case and facade updates
that adopt new tables without
leaking SQL to UI.
Backend HQ migration
backend/db/postgres/
migrations + related
repo/http/app packages
Migration chain, sync contract
update,
reconciliation/reporting
compatibility.
CI enforcement
GitHub Actions workflows for
shared, Android, desktop,
backend lanes
Migration verify, FK/integrity,
atomic bundle subset, backend
migration checks.
Operational rollout
Release / QA / supervisor pilot
coordination
Controlled wave rollout with
evidence capture, exception
queue, and hold criteria.

Prescriptive baseline · Page 12
## 13. Appendix - Traceability matrix ringkas
Migration concern
Primary forcing artifact
Why traceable
pos_terminal + business_day
foundation
ERD v2, Domain Model (Shift &
Cash), Architecture
Shift uniqueness, operational
day lifecycle, terminal-scoped
numbering and reconciliation
depend on it.
Expanded reason / approval /
audit model
Use Case exceptions, Activity
alternate paths, Domain Model
shared kernel
Supervisor approvals,
void/return/adjustment
reasons, and auditability
require persistable cross-
domain structure.
cashier_session / cash_session
refactor
Domain Model (CashierShift,
BusinessDay),
Activity/Sequence flows
Opening cash, safe drop,
reconciliation, and shift close
invariants are impossible to
prove on old shape.
Sales entity alignment
Sales & Checkout domain,
sequence bundles, ERD v2
Sale completion must bind
business_day, terminal,
payment, receipt, and
audit/outbox atomically.
Inventory balance + ledger
separation
ERD v2, Architecture, Module
guardrails, Test Spec
Current state and explanation
trail must remain separate
and testable.
Return aggregate introduction
Return & Refund domain, use
cases, ERD gap analysis
refund_record alone cannot
represent approval, stock
effect, and policy decision
correctly.
Sync orchestration expansion
Architecture sync bounded
context, ERD v2, CI/Test
strategy
outbox_event alone is not
enough for retry, conflict, and
explainability requirements.
Final implementation note
- 
Dokumen ini sengaja preskriptif: urutan wave, boundary runtime, guardrail domain, dan
gate verifikasi sudah dipilih untuk meminimalkan technical debt migration.
- 
Yang belum boleh diasumsikan selesai adalah reconciliation terakhir terhadap current
implementation snapshot. Itu adalah langkah verifikasi akhir engineering, bukan alasan
untuk membuat migration design kabur.


## Constraints / Policies
Current repo DDL/snapshot tidak tersedia penuh di percakapan; draft SQL perlu reconciliation terakhir sebelum eksekusi.

## Technical Notes
FK, WAL, outbox/sync orchestration, atomic bundle, dan dual identity harus dipertahankan sepanjang migration.

## Dependencies / Related Documents
- `store_pos_erd_specification_v2.md`
- `cassy_architecture_specification_v1.md`
- `store_pos_domain_model_detail_specifications_v2.md`
- `cassy_module_project_structure_specification.md`
- `traceability_matrix_store_pos.md`
- `store_pos_test_specification.md`
- `cassy_test_automation_specification.md`
- `cassy_cicd_pipeline_strategy_v1.md`

## Risks / Gaps / Ambiguities
- Dokumen menyatakan lampiran draft SQL perlu direkonsiliasi dengan implementation snapshot aktual sebelum dieksekusi.

## Reviewer Notes
- Struktur telah dinormalisasi ke Markdown yang konsisten dan siap dipakai untuk design review / engineering handoff.
- Istilah utama dipertahankan sedekat mungkin dengan dokumen sumber untuk menjaga traceability lintas artefak.
- Bila ada konflik antar dokumen, prioritaskan source of truth, artefak yang paling preskriptif, dan baseline yang paling implementable.

## Source Mapping
- Original source: `Cassy_Migration_Script_Specification.pdf` (PDF, 12 pages)
- Output markdown: `cassy_migration_script_specification.md`
- Conversion approach: text extraction -> normalization -> structural cleanup -> standardized documentation wrapper.
- Note: beberapa tabel/list di PDF dapat mengalami wrapping antar baris; esensi dipertahankan, tetapi layout tabel asli tidak dipertahankan 1:1.
