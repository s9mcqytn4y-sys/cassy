# Cassy Event Contract Sync Specification v1

## Document Overview
Detailed sync event contract untuk outbox, batch, item, conflict, offline window, dan master snapshot.

## Purpose
Mengunci kontrak event yang explainable, idempotent, auditable, dan siap diturunkan ke schema lokal, API HQ, dan automation.

## Scope
Outbound + inbound + reconciliation Dokumen ini mengunci write path, pull/update path, dan admin recovery state. Envelope Hybrid Cassy envelope Metadata konsisten; payload domain tetap kaya dan traceable. Identity Prefixed UUIDv7 + separate idempotency_key Ordering temporal le...

## Key Decisions / Core Rules
Hybrid Cassy envelope aligned with CloudEvents principles; batch envelope dengan item-level acknowledgement; conflict diselesaikan explicit policy-based reconciliation.

## Detailed Content

### Normalized Source Body
Event Contract (Sync) Specification · Page 1
Cassy
Event Contract (Sync)
Specification
Phase 1 Retail POS · Outbox / Batch / Item / Conflict / Offline Window / Master Snapshot
Field
Value
Document type
Detailed sync event contract and transport specification
Status
Prescriptive baseline for implementation handoff
Primary scope
Phase 1 Retail POS; prepared F&B and Service
boundaries in appendix
Canonical style
Hybrid Cassy event envelope aligned with CloudEvents
principles
Transport unit
Batch envelope with item-level acknowledgement
Authoring date
2026-03-08
Repository target
GitHub Cassy monorepo
Target readers
Software architect, backend engineer, Android / POS
engineer, QA, reviewer
Design stance
Dokumen ini sengaja preskriptif. Tujuannya bukan mendeskripsikan queue teknis abstrak, tetapi
mengunci kontrak event yang explainable, idempotent, auditable, dan siap diturunkan ke KMP shared
sync context, SQLite/SQLDelight schema, Go REST HQ API, serta test automation.
Daftar Isi
- 
## 1. Executive Summary
- 
## 2. Scope, Decisions, and Assumptions
- 
## 3. Source Basis and Normative References
- 
## 4. Sync Context and Architecture Position
- 
## 5. Canonical Event Model
- 
## 6. Identity, Versioning, and Idempotency
- 
## 7. Batch Transport Contract
- 
## 8. Event Catalog
- 
## 9. State Machines
- 
## 10. Payload Rules and Data Classification
- 
## 11. API Contract Outline
- 
## 12. Persistence Mapping
- 
## 13. Reconciliation, Conflict, and Offline Policy
- 
## 14. Security, Observability, and Operational Control

Event Contract (Sync) Specification · Page 2
- 
## 15. Test and Validation Baseline
- 
## 16. Implementation Guardrails
- 
Appendix A. Prepared Boundaries
- 
Appendix B. Sample JSON Payload Catalog
- 
Appendix C. Naming Conventions and Prefixes

Event Contract (Sync) Specification · Page 3
## 1. Executive Summary
Cassy memakai strategi local-first di terminal POS. Setiap keputusan bisnis ditulis lebih dulu ke
database lokal, lalu dikirim ke HQ melalui orkestrasi sinkronisasi yang eksplisit. Karena itu kontrak
event tidak boleh berhenti di outbox_event sederhana. Dokumen ini menetapkan model target-state
yang memisahkan capture intent, batch transport, item result, conflict record, offline window, dan
master-data snapshot supaya retry, partial failure, serta reconciliation tetap explainable dan testable.
Keputusan final yang dipakai di dokumen ini: scope mencakup outbound client
HQ, inbound
->
HQ
client, dan flow reconciliation/admin; kontrak menggunakan hybrid Cassy envelope yang
->
mengambil disiplin metadata ala CloudEvents tetapi tetap mempertahankan field domain yang
dibutuhkan Cassy; identitas event memakai prefixed UUIDv7; idempotency_key dipisahkan dari
event_id; transport utama adalah batch envelope; dan conflict diselesaikan melalui policy-based
explicit reconciliation, bukan blanket last-write-wins.
Area
Final decision
Consequence
Scope
Outbound + inbound + reconciliation
Dokumen ini mengunci write path,
pull/update path, dan admin
recovery state.
Envelope
Hybrid Cassy envelope
Metadata konsisten; payload domain
tetap kaya dan traceable.
Identity
Prefixed UUIDv7 + separate
idempotency_key
Ordering temporal lebih baik; replay
dan dedup semantik tetap dipisah.
Granularity
Per aggregate / business decision
Tidak jatuh menjadi event per row
tabel atau event terlalu gemuk.
Transport
Batch envelope + item result
Cocok dengan sync_batch dan
sync_item sebagai model persisted
orchestration.
Conflict policy
Explicit reconciliation
Sync failure tetap visible sampai
resolution status final.
Inbound apply
Via application services
HQ tidak boleh memotong invariant
lewat mutation script mentah.
Prepared boundaries
F&B dan Service hanya appendix
Boundary tetap hidup tanpa
mencemari fokus retail phase-1.
## 2. Scope, Decisions, and Assumptions
- 
Body utama hanya mencakup event yang wajib shipping pada Phase 1 Retail POS: kernel, shift &
cash, sales & payment, returns, inventory basic, sync/reconciliation, dan master-data snapshot
refresh.
- 
Sync diperlakukan sebagai bounded context mandiri. Kegagalan, retry, conflict, dan offline
window adalah state bisnis yang terlihat, bukan best-effort background queue.
- 
Satu terminal POS adalah writer operasional utama pada flow checkout dan shift-critical. Android
Mobile dan Desktop Backoffice dapat memicu flow tertentu, tetapi tidak menjadi checkout writer
kedua.
- 
Contract evolution harus sinkron antara schema SQLite/SQLDelight lokal, API HQ, dan persistence
PostgreSQL agar tidak terjadi version drift pada payload, state machine, atau reconciliation
endpoint.

Event Contract (Sync) Specification · Page 4
- 
Dokumen ini memprioritaskan implementation handoff. Karena itu isinya berhenti pada kontrak,
state machine, payload catalog, dan mapping implementasi; bukan class-by-class code blueprint.
Non-goals
Dokumen ini tidak meresmikan event sourcing penuh, peer-to-peer terminal replication, shared UI
framework, atau multi-writer store node. Dokumen ini juga tidak mendefinisikan full OpenAPI seluruh
backend; hanya area yang dibutuhkan untuk sync contract.
## 3. Source Basis and Normative References
Internal source of truth yang dipakai untuk menurunkan dokumen ini:
Source
Role in this specification
Architecture Specification v1
Menetapkan local-first, outbox + batch/item/conflict
orchestration, single-writer rule, offline policy, conflict
posture, dan API surface sync.
ERD Specification v2
Menetapkan entitas outbox_event, sync_batch,
sync_item, sync_conflict, master_data_snapshot,
offline_operation_window, field minimum, prefix ID, dan
aturan relasi inti.
Domain Model Detail Specifications v2
Menetapkan SyncBatch sebagai aggregate root,
SyncItem/SyncConflict/MasterDataSnapshot/OfflineOper
ationWindow sebagai objek domain, serta invariant
domain sync.
Sequence and Activity Specifications
Menetapkan flow utama UC-35/36/37: batch preparation,
push/pull, retry, conflict analysis, manual investigation,
dan offline degraded mode.
Test Specification
Menetapkan state yang wajib tervisualisasi dan diuji:
partial failure, retry, explicit conflict resolution, offline
pending sync, dan no silent drop.
Migration Script Specification
Menegaskan bahwa outbox saja tidak cukup;
orchestration tables dan API contracts harus evolve
bersama migration chain.
Normative external references yang memengaruhi keputusan teknis:
Reference
How it is used here
CloudEvents
Dipakai sebagai disiplin envelope metadata; Cassy
mengadopsi prinsip event envelope yang konsisten,
tetapi tidak memaksakan pure CloudEvents untuk
semua field domain.
RFC 9562 UUIDv7
Dipakai untuk event_id, batch_id, conflict_id,
snapshot_id, dan entity PK time-ordered yang tetap
opaque.
IETF Idempotency-Key draft
Dipakai untuk memisahkan event identity dari
command idempotency, plus aturan
reuse/expiry/fingerprint.
SQLite foreign key / WAL / partial index docs
Dipakai untuk local persistence rules: FK aktif per
koneksi, WAL satu host, dan partial index untuk row

Event Contract (Sync) Specification · Page 5
Reference
How it is used here
aktif / tombstone / idempotency.
## 4. Sync Context and Architecture Position
Pada Cassy, sync bukan lapisan infrastruktur tersembunyi. Sync adalah bounded context reporting-
and-control yang memiliki aggregate root SyncBatch dan entity SyncItem, SyncConflict,
MasterDataSnapshot, serta OfflineOperationWindow. Context ini berdiri di atas transaksi bisnis lokal,
mengorkestrasi pengiriman, pull, retry, conflict handling, dan visibility ke UI POS/backoffice.
1.
Application service context asal menulis keputusan bisnis lokal beserta audit intent dan
outbox_event dalam satu transaksi lokal.
2.
Sync worker membaca outbox_event pending lalu membentuk sync_batch dan sync_item untuk
outbound/inbound orchestration.
3.
Batch dikirim ke HQ melalui /sync/batches; HQ merespons hasil batch dan hasil item, termasuk
accepted, conflict, rejected, retryable, atau deferred.
4.
Inbound update dari HQ - terutama master data snapshot dan status result - diterapkan melalui
application services, bukan mutation script yang menabrak invariant.
5.
Jika terjadi partial failure atau divergence versi, sistem membentuk sync_conflict atau final
failed/manual-investigation state yang visible bagi actor operasional.
6.
Manager dan supervisor menyelesaikan failure lewat UI reconciliation yang membaca state
persistence resmi, bukan log scraping.
Terminal state is mandatory
Sync failure tidak boleh hilang tanpa status akhir. Retry otomatis boleh terjadi, tetapi setelah batas
retry atau ketika conflict policy memintanya, state harus berakhir jelas: synced, conflicted, rejected,
manual_investigation, atau superseded.
## 5. Canonical Event Model
Kontrak yang dipakai adalah hybrid Cassy envelope: sebuah event tetap punya metadata lintas
domain yang stabil dan payload domain yang eksplisit. Envelope disimpan di outbox_event saat
capture intent, lalu diperkaya oleh sync_item ketika masuk proses transport. Batch envelope memuat
kumpulan sync_item untuk satu arah perjalanan.
Envelope field
Required
Description
event_id
Yes
Opaque prefixed UUIDv7 untuk
identitas event instance.
event_type
Yes
Nama semantic type bertitik:
<context>.<aggregate>.<action>.v<m
ajor>.
contract_version
Yes
Versi mayor envelope Cassy. Dimulai
dari 1.
occurred_at
Yes
Timestamp UTC saat keputusan
bisnis resmi terjadi.
produced_by
Yes
Service/application facade yang

Event Contract (Sync) Specification · Page 6
Envelope field
Required
Description
membentuk event.
source_node
Yes
Origin node: pos-terminal, android-
mobile, desktop-backoffice, atau hq-
api.
direction
Yes
OUTBOUND atau INBOUND.
aggregate_type
Yes
Jenis aggregate bisnis asal
perubahan.
aggregate_id
Yes
ID aggregate bisnis.
aggregate_version
Conditional
Wajib pada entity/versioned
aggregate yang conflict-prone.
idempotency_key
Conditional
Wajib pada retriable command /
syncable mutation yang dapat
diulang.
correlation_id
Yes
Korelasi satu flow lintas event dan
lintas service.
causation_id
Optional
Event atau command pemicu
langsung; berguna untuk audit dan
diagnosis.
actor_context
Yes
Actor, role, store_id, terminal_id,
business_day_id, dan approval
context bila relevan.
payload
Yes
Isi domain event yang divalidasi dan
cukup untuk HQ processing /
reconciliation.
payload_hash
Yes
Hash deterministik payload
canonicalized; dipakai pada
sync_item dan server
dedup/fingerprint.
schema_ref
Optional
Nama logical schema atau payload
profile bila satu event type memiliki
varian minor.
{
"event_id": "obx_0195b2f7-5c30-7d8b-8b52-3be4fc3f4d1a",
"event_type": "sales.sale.completed.v1",
"contract_version": 1,
"occurred_at": "2026-03-08T10:14:33.412Z",
"produced_by": "sales.CheckoutFacade",
"source_node": "android-pos",
"direction": "OUTBOUND",
"aggregate_type": "sale_transaction",
"aggregate_id": "sal_0195b2f7-5c1c-7225-a958-67eb7b6fd584",
"aggregate_version": 7,
"idempotency_key": "idem_sal_complete_ter_01_20260308_000123",
"correlation_id": "cor_0195b2f7-5c18-73e8-9f82-e51f145eb9e0",
"causation_id": "cmd_0195b2f7-5bff-78ba-b8d6-1aa0ae5219e9",
"actor_context": {
"actor_id": "emp_0195a0ea-7f30-74ea-bd15-39e071b34920",
"actor_role": "cashier",

Event Contract (Sync) Specification · Page 7
"store_id": "str_01958e24-0ce2-7c71-a8f2-5293c4f8e510",
"terminal_id": "ter_0195904f-dfe9-7b7a-934f-82689411b5b3",
"business_day_id": "bdy_0195b1a6-d31e-7924-98c2-17d4844573c1"
},
"payload_hash": "sha256:dc09ab...8fc7",
"payload": { "... domain fields ..." }
}
### 5.1 Naming convention
- 
Gunakan dot-separated semantic type: <context>.<aggregate>.<action>.v<major>.
- 
Context mengikuti bounded context bisnis, bukan nama tabel atau package teknis.
- 
Aggregate mengikuti model domain: sale, return, cashier_shift, business_day,
inventory_adjustment, sync_batch, approval, audit, dan sebagainya.
- 
Action memakai bentuk lampau/hasil keputusan yang menjelaskan apa yang sudah sah secara
bisnis, misalnya completed, accepted, closed, adjusted, resolved, snapshot_refreshed.
- 
Version major dinaikkan hanya untuk perubahan breaking pada payload event type tersebut.
## 6. Identity, Versioning, and Idempotency
Concern
Rule
Rationale
Primary IDs
Semua
entity/event/batch/conflict/snapshot/
window memakai prefixed UUIDv7.
Time-ordering lebih baik dan tetap
opaque.
Business numbers
receipt_no, business_day_no,
transfer_no, dst tetap kolom bisnis
terpisah.
Format boleh berubah tanpa
merusak referensial.
Event vs idempotency
event_id tidak boleh dianggap
sinonim idempotency_key.
Satu business mutation bisa direplay
tanpa mengganti semantik
keputusan.
Aggregate version
Wajib pada entity sync-participating
yang bisa legitimately conflict.
HQ perlu tahu divergence lokal vs
remote.
Payload fingerprint
payload_hash wajib untuk setiap
sync_item.
Mendukung dedup dan diagnosis
reuse key/payload mismatch.
Envelope version
contract_version dimulai dari 1 dan
naik saat envelope berubah
breaking.
Memisahkan perubahan envelope
dari perubahan event_type.
Event version
event_type memakai suffix .vN.
Breaking payload hanya
memengaruhi event type terkait.
Idempotency expiry
Server mendokumentasikan
lifecycle key dan window expiry.
Mencegah key reuse tak terkontrol
sekaligus memberi batas
penyimpanan.
### 6.1 ID prefixes
Artifact
Prefix
Example
outbox_event
obx
obx_0195...
sync_batch
syb
syb_0195...

Event Contract (Sync) Specification · Page 8
Artifact
Prefix
Example
sync_item
syi
syi_0195...
sync_conflict
syc
syc_0195...
master_data_snapshot
mds
mds_0195...
offline_operation_window
ofw
ofw_0195...
sale_transaction
sal
sal_0195...
business_day
bdy
bdy_0195...
cashier_session
chs
chs_0195...
### 6.2 Idempotency rules
7.
Setiap retriable business command dan syncable mutation wajib memiliki idempotency_key yang
stabil dalam satu intent bisnis.
8.
Idempotency key tidak boleh dipakai ulang untuk payload berbeda.
9.
Server wajib mampu mengenali replay berdasarkan kombinasi idempotency_key, aggregate
identity, dan bila perlu payload_hash.
## 10. Jika key sama dipakai untuk payload berbeda, HQ mengembalikan status error kontrak; item
tidak boleh di-accept diam-diam.
## 11. Retry jaringan yang mengirim ulang event yang sama harus menghasilkan item result yang
idempotent: accepted-replayed, already_applied, atau outcome ekuivalen, bukan duplikasi
business effect.
## 7. Batch Transport Contract
Transport utama untuk Phase 1 adalah batch envelope. Satu request /sync/batches memuat satu batch
dengan metadata batch dan daftar item. Batch adalah unit pengiriman dan observability; item adalah
unit evaluasi hasil. Karena itu sistem harus menyimpan result di dua level: batch summary dan item
detail.
Batch field
Required
Meaning
batch_id
Yes
Prefixed UUIDv7 untuk satu
pengiriman.
batch_direction
Yes
OUTBOUND atau
INBOUND_RESPONSE.
origin_node
Yes
Node pengirim batch.
scope
Yes
FULL_SYNC, OUTBOUND_ONLY,
MASTERDATA_ONLY,
RECONCILE_ONLY, atau scope lain
yang dikunci policy.
created_at
Yes
Waktu batch disiapkan.
started_at
Conditional
Waktu push/pull dimulai.
completed_at
Conditional
Waktu batch mencapai terminal
state.
status
Yes
PREPARED, IN_FLIGHT, PARTIAL,

Event Contract (Sync) Specification · Page 9
Batch field
Required
Meaning
SYNCED, FAILED, CONFLICTED,
MANUAL_REVIEW, CANCELLED.
attempt_no
Yes
Nomor attempt batch untuk retry
orchestration.
origin_store_id
Yes
Store boundary pengirim.
origin_terminal_id
Conditional
Wajib untuk operational writer POS.
item_count
Yes
Jumlah item dalam batch.
accepted_count / conflict_count /
rejected_count / retryable_count
Yes
Ringkasan hasil item.
transport_meta
Optional
HTTP status, latency, server trace,
token/source snapshot version.
### 7.1 Item result vocabulary
Item status
Meaning
Retryable
Creates conflict?
## Prepared
Item baru dipetakan dari
outbox/backlog; belum
dikirim.
No
No
IN_FLIGHT
Sedang dalam attempt
pengiriman.
No
No
## Accepted
HQ menerima dan
mengaplikasikan
mutation.
No
No
ACCEPTED_REPLAY
Event dikenal sebagai
replay sah dan business
effect tidak digandakan.
No
No
PULLED
Inbound item berhasil
ditarik dari HQ.
No
No
APPLIED_LOCAL
Inbound item berhasil
diterapkan via application
service lokal.
No
No
RETRYABLE_FAILED
Kegagalan sementara;
item boleh diattempt
ulang.
Yes
No
## Conflicted
Versi/state divergen dan
butuh resolution policy.
Depends
Yes
## Rejected
Payload valid secara teknis
tetapi ditolak secara
bisnis/kontrak.
No
Possible separate issue
MANUAL_INVESTIGATION
Retry otomatis berhenti;
actor manusia harus
menilai.
No
May coexist
## Superseded
Item sudah tidak relevan
karena event baru / state
baru menggantikannya.
No
No

Event Contract (Sync) Specification · Page 10
{
"batch_id": "syb_0195b301-bfd8-7004-bb19-3ee0f6a5a421",
"batch_direction": "OUTBOUND",
"scope": "FULL_SYNC",
"origin_node": "android-pos",
"origin_store_id": "str_01958e24-0ce2-7c71-a8f2-5293c4f8e510",
"origin_terminal_id": "ter_0195904f-dfe9-7b7a-934f-82689411b5b3",
"created_at": "2026-03-08T10:15:01.000Z",
"attempt_no": 1,
"items": [
{ "... sync item envelope ..." },
{ "... sync item envelope ..." }
]
}
## 8. Event Catalog
Katalog berikut adalah baseline event phase-1 yang harus didukung kontrak. Daftar ini sengaja
berada pada level aggregate/business decision. Ia bukan daftar semua perubahan tabel dan juga
bukan full event sourcing stream.
### 8.1 Shared Kernel and Control Events
Event type
Direction
Aggregate
Trigger / meaning
Notes
kernel.approval.deci
ded.v1
## Outbound
approval_request
Supervisor / policy
gate disetujui atau
ditolak.
Tidak menggantikan
event bisnis utama;
menjadi evidence
pendukung.
kernel.reason.record
ed.v1
## Outbound
reason_code context
Reason exception
tercatat pada flow
abnormal.
Boleh embedded di
payload event bisnis
bila bukan decision
mandiri.
kernel.audit.logged.v
## Outbound
audit_log
Audit penting
domain dicatat lokal.
Tidak semua audit
harus menjadi
transport utama;
event ini dipakai
untuk audit-forward
bila policy
menuntut.
auth.session.refresh
ed.v1
## Inbound
session cache
Grant/permission
cache disegarkan
dari HQ.
Tidak dipakai untuk
login online-first
handshake penuh.
### 8.2 Shift and Cash Events
Event type
Direction
Aggregate
Trigger / meaning
Notes
cash.shift.opened.v1
## Outbound
cashier_session
Shift baru sah
dibuka pada
terminal.
Payload wajib
memuat
business_day_id dan
terminal_id.

Event Contract (Sync) Specification · Page 11
Event type
Direction
Aggregate
Trigger / meaning
Notes
cash.opening_cash.r
ecorded.v1
## Outbound
opening_cash
Kas awal tervalidasi.
Idempotent per shift.
cash.movement.reco
rded.v1
## Outbound
cash_movement
Cash in/out, safe
drop, atau
adjustment non-sale.
Reason dan
approval context
wajib bila policy
memintanya.
cash.shift.closed.v1
## Outbound
cashier_session
Shift selesai setelah
reconciliation.
Tidak boleh terbit
sebelum close
invariant terpenuhi.
cash.reconciliation.fi
nalized.v1
## Outbound
cash_reconciliation
Expected vs counted
menghasilkan
decision final.
Conflict tidak
memakai last-write-
wins.
business_day.closed.
v1
## Outbound
business_day
Hari operasional
ditutup lokal.
Bisa pending sync
tetapi readiness
blocker tetap
tercatat.
### 8.3 Sales and Payment Events
Event type
Direction
Aggregate
Trigger / meaning
Notes
sales.sale.completed.
v1
## Outbound
sale_transaction
Checkout selesai
dengan settlement
sah.
Event utama untuk
HQ ingestion.
sales.sale.voided.v1
## Outbound
sale_transaction
Void transaksi
final/suspended
sesuai policy.
Reason dan
approval wajib saat
threshold policy
tercapai.
sales.suspended_sale
.saved.v1
## Outbound
suspended_sale
Transaksi diparkir
untuk resume.
Bisa tetap lokal-only
bila policy
memutuskan tidak
perlu HQ mirror
cepat.
payments.payment.a
uthorized.v1
## Outbound
payment
Instrumen
pembayaran
mendapat
otorisasi/ack sah.
Gunakan hanya
untuk keputusan
yang sudah durable.
payments.payment.f
ailed.v1
## Outbound
payment
Pembayaran gagal
dengan alasan
eksplisit.
Diperlukan bila
memengaruhi audit
atau recovery pusat.
receipts.receipt.issue
d.v1
## Outbound
receipt
Receipt final
diterbitkan / dikirim.
Tidak menggantikan
sale.completed.
### 8.4 Return Events
Event type
Direction
Aggregate
Trigger / meaning
Notes
returns.return.accep
ted.v1
## Outbound
return_transaction
Return disetujui
sesuai policy.
Payload memuat
original sale
snapshot minimum.

Event Contract (Sync) Specification · Page 12
Event type
Direction
Aggregate
Trigger / meaning
Notes
returns.refund.settle
d.v1
## Outbound
refund_record
Refund diselesaikan
ke instrumen yang
sah.
Bisa satu atau lebih
tergantung split
refund.
returns.store_credit.i
ssued.v1
## Outbound
store_credit_account
Fallback refund ke
store credit
diterbitkan.
Tidak boleh diam-
diam menggantikan
refund lain tanpa
evidence.
### 8.5 Inventory Basic Events
Event type
Direction
Aggregate
Trigger / meaning
Notes
inventory.receiving.
posted.v1
## Outbound
receiving_document
Receiving dengan
discrepancy final
diposting.
Stock ledger harus
explainable.
inventory.adjustmen
t.applied.v1
## Outbound
stock_adjustment
Adjustment manual
final.
Reason dan
approval policy
wajib tercermin.
inventory.cycle_cou
nt.closed.v1
## Outbound
cycle_count
Cycle count selesai
dengan resolution
final.
Variance besar tidak
boleh otomatis
overwrite.
inventory.damage.di
sposed.v1
## Outbound
damage_disposition
Barang
rusak/expired
dipindahkan ke
bucket/decision
akhir.
Korelasi ke stock
ledger wajib.
### 8.6 Sync and Reconciliation Events
Event type
Direction
Aggregate
Trigger / meaning
Notes
sync.batch.dispatche
d.v1
## Outbound
sync_batch
Batch dikirim ke HQ.
Event kontrol;
opsional bila status
batch sudah cukup
di persistence.
sync.item.conflicted.
v1
## Outbound
sync_conflict
Divergence
state/version
tercatat.
Menyediakan
evidence conflict
intake.
sync.conflict.resolve
d.v1
## Outbound
sync_conflict
Conflict memperoleh
resolution final.
Wajib memuat siapa
memutuskan apa.
sync.batch.marked_
manual.v1
## Outbound
sync_batch
Batch/item dialihkan
ke manual
investigation.
Visibility ke
backoffice wajib.
sync.offline_window
.opened.v1
## Outbound
offline_operation_wi
ndow
Terminal masuk
degraded/offline
mode.
Tidak sama dengan
sekadar ping gagal
sesaat.
sync.offline_window
.closed.v1
## Outbound
offline_operation_wi
ndow
Terminal keluar dari
offline mode dan
backlog mulai
reconcile.
Wajib membawa
summary
backlog/resolution
marker.

Event Contract (Sync) Specification · Page 13
### 8.7 Master Data and Inbound Control Events
Event type
Direction
Aggregate
Trigger / meaning
Notes
masterdata.snapshot
.refreshed.v1
## Inbound
master_data_snapsh
ot
HQ mengirim
snapshot baru untuk
referensi seperti
product/policy/prici
ng.
Harus diterapkan
via application
service.
masterdata.snapshot
.rejected.v1
## Inbound
master_data_snapsh
ot
Snapshot tidak dapat
diterapkan karena
precondition/version
mismatch.
Membentuk visible
failure state.
sync.item.accepted.v
INBOUND_RESPONS
E
sync_item
HQ mengakui item
tertentu.
Umumnya tidak
disimpan sebagai
outbox event baru;
cukup ke result
state.
sync.item.rejected.v1
INBOUND_RESPONS
E
sync_item
HQ menolak item
tertentu.
Alasan harus
machine-readable.
sync.item.retryable_f
ailed.v1
INBOUND_RESPONS
E
sync_item
HQ gagal sementara.
Menjaga retry policy
eksplisit.
## 9. State Machines
### 9.1 outbox_event
## 12. CAPTURED
READY: event intent berhasil ditulis bersama keputusan bisnis lokal.
->
## 13. READY
GROUPED: item sudah dipetakan ke sync_batch/sync_item aktif.
->
## 14. GROUPED
RETAINED: outbox boleh dipertahankan sebagai evidence walau sync_item sudah
->
selesai.
## 15. READY/GROUPED
FAILED_CAPTURE: hanya jika serialisasi/persist gagal; business transaction
->
utama harus ikut rollback.
## 16. READY/GROUPED
EXPIRED/SUPERSEDED: hanya untuk event yang secara eksplisit boleh
->
digantikan policy; tidak berlaku untuk transaksi finansial final.
### 9.2 sync_batch
## Prepared
-> IN_FLIGHT
-> SYNCED
-> PARTIAL
-> FAILED
-> CONFLICTED
-> MANUAL_REVIEW
-> CANCELLED
## Partial
-> IN_FLIGHT (retry subset)
-> MANUAL_REVIEW
-> CONFLICTED
-> SYNCED (if all remaining items resolved)

Event Contract (Sync) Specification · Page 14
### 9.3 sync_item
## Prepared
-> IN_FLIGHT
-> ACCEPTED
-> ACCEPTED_REPLAY
-> PULLED
-> APPLIED_LOCAL
-> RETRYABLE_FAILED
-> CONFLICTED
-> REJECTED
-> SUPERSEDED
-> MANUAL_INVESTIGATION
### 9.4 sync_conflict
Field / state
Rule
OPEN
Conflict baru tercatat dan belum diputuskan.
RETRY_PENDING
Conflict dipilih untuk retry otomatis/terbatas.
RESOLVED_LOCAL_WINS
Dipakai hanya pada domain/policy yang secara eksplisit
mengizinkan local precedence.
RESOLVED_REMOTE_WINS
Umum untuk master data dan referensi server-
authoritative.
RESOLVED_MERGED
Dipakai jika ada merge rule yang sah dan
terdokumentasi.
MANUAL_INVESTIGATION
Butuh keputusan manusia / ticket lanjutan.
## Cancelled
Conflict tidak relevan lagi karena item superseded atau
policy change yang terdokumentasi.
### 9.5 offline_operation_window
## 17. CLOSED
OPEN ketika konektivitas/policy membawa terminal ke degraded mode dan operasi
->
offline terbatas dimulai.
## 18. OPEN
RECONCILING ketika koneksi pulih dan backlog mulai diproses.
->
## 19. RECONCILING
CLOSED ketika backlog yang termasuk ke window itu mencapai state final atau
->
supervisor menandai unresolved carry-over secara eksplisit.
20. OPEN atau RECONCILING tidak boleh hilang tanpa record ofw final dan summary backlog.
## 10. Payload Rules and Data Classification
Class
Description
Examples
Required operational
Harus ada di payload karena
dibutuhkan HQ untuk validasi,
replay, atau reconciliation.
aggregate identity, actor context,
store_id, terminal_id,
business_day_id, amount summary,
version, reason/approval refs
Restricted transit
Boleh lewat backend, tetapi tidak
boleh dipantulkan utuh ke
customer contact, masked PAN/token
reference, internal provider raw

Event Contract (Sync) Specification · Page 15
Class
Description
Examples
observability umum.
response
Masked-in-observability
Boleh disimpan di payload tetapi
harus disamarkan pada
dashboard/log.
receipt recipient email, phone, token
tail
Derived / omit
Jangan dikirim jika bisa dihitung
ulang atau tidak diperlukan untuk
authority/reconciliation.
UI-only text, layout flags, temporary
client screen state
### 10.1 Actor and context metadata minimum
- 
actor_id dan actor_role
- 
store_id
- 
terminal_id bila flow berasal dari terminal operasional
- 
business_day_id untuk flow kas/checkout/operational close
- 
cashier_session_id atau shift context bila flow membutuhkan jejak shift
- 
approval_request_id, approver_id, reason_code bila policy gate dipakai
- 
correlation_id, idempotency_key, payload_hash
- 
occurred_at dan timezone-normalized UTC timestamp
### 10.2 Aggregate-specific payload minimum
Aggregate
Minimum payload fields
sale_transaction
sale_id, receipt_no snapshot, totals, payment summary,
settlement_status, source_channel,
cashier/terminal/store/business_day refs
cashier_session / business_day
status, opening/closing summary, reconciliation decision,
shift/day refs, actor context
return_transaction
original sale reference snapshot, policy decision,
disposition outcome, refund summary
stock_adjustment / receiving / cycle_count
source document no, location/bucket, product lines
summary, reason/approval refs, ledger effect summary
master_data_snapshot
snapshot_type, version, effective_at, hash, applies_to
store/policy scope
sync_conflict
conflict_type, local_version, remote_version,
local_summary, remote_summary, proposed resolution,
decided_by
## 11. API Contract Outline
Dokumen ini tidak mendefinisikan seluruh backend API, tetapi area berikut bersifat wajib karena
event contract, status machine, dan reconciliation flow bergantung padanya.
Endpoint
Method
Purpose
Core response
semantics
/sync/batches
POST
Push outbound batch dari
client ke HQ.
Return batch result + item
results: accepted / replay /
retryable_failed /

Event Contract (Sync) Specification · Page 16
Endpoint
Method
Purpose
Core response
semantics
conflicted / rejected.
/sync/reconcile
POST
Kirim keputusan
retry/resolve/manual
investigation untuk item
atau batch tertentu.
Return updated reconcile
status dan optional follow-
up actions.
/sync/conflicts
GET
Daftar unresolved conflict
untuk backoffice /
supervisor.
Filter by store, terminal,
batch, aggregate type,
resolution status.
/sync/conflicts/{id}/resolve
POST
Finalisasi conflict
resolution.
Return resolved status +
resulting sync item/batch
status.
/master-data/{type}
GET
Tarik snapshot referensi
versi baru.
Return snapshot version,
payload hash, and
effective window.
### 11.1 Recommended response object
{
"batch_id": "syb_0195b301-bfd8-7004-bb19-3ee0f6a5a421",
"status": "PARTIAL",
"server_received_at": "2026-03-08T10:15:03.115Z",
"accepted_count": 8,
"retryable_count": 1,
"conflict_count": 1,
"rejected_count": 0,
"item_results": [
{
"sync_item_id": "syi_0195b301-c06c-7744-9f7d-40d33aee1d6a",
"event_id": "obx_0195b2f7-5c30-7d8b-8b52-3be4fc3f4d1a",
"status": "ACCEPTED",
"remote_aggregate_version": 7
},
{
"sync_item_id": "syi_0195b301-c080-7e2f-b9c0-8ec88ff90d84",
"event_id": "obx_0195b2f7-5f10-7f8f-b112-63ce4d2bca55",
"status": "CONFLICTED",
"conflict_id": "syc_0195b301-d0ef-7129-9ccd-64a4fbeb9864",
"retryable": false,
"reason_code": "VERSION_DIVERGED"
}
]
}
### 11.2 Error contract rules
- 
Gunakan HTTP status code untuk layer transport saja; state bisnis akhir tetap disampaikan di
payload item result.
- 
Batch dapat tetap HTTP 200/207-like semantic secara payload walau sebagian item gagal; jangan
menjadikan seluruh batch opaque karena satu item conflict.
- 
Reuse idempotency key untuk payload berbeda harus menghasilkan response kontrak yang jelas;
item result ditandai rejected/contract_error.

Event Contract (Sync) Specification · Page 17
- 
Timeout atau kegagalan jaringan yang membuat hasil remote tidak pasti harus berakhir sebagai
retryable_failed atau unknown_remote_commit state yang visible, bukan silent drop.
## 12. Persistence Mapping
### 12.1 Minimum local tables
Table
Purpose
Key fields to enforce early
outbox_event
Capture intent dari keputusan bisnis
lokal.
id, event_type, aggregate_type,
aggregate_id, direction,
payload_json, payload_hash,
idempotency_key, occurred_at,
status
sync_batch
Unit orkestrasi pengiriman/pull.
id, status, direction, scope,
created_at, started_at, completed_at,
attempt_no, origin_store_id,
origin_terminal_id
sync_item
Unit evaluasi item per aggregate
change.
id, batch_id, event_id/outbox_id,
aggregate_type, aggregate_id,
event_type, direction, retry_count,
status, payload_hash, error_code
sync_conflict
State divergensi local-vs-remote.
id, sync_item_id, conflict_type,
local_version, remote_version,
resolution_status, resolved_by,
resolved_at
master_data_snapshot
Snapshot referensi dari HQ.
id, snapshot_type, version,
effective_at, payload_hash,
applied_at, status
offline_operation_window
Jejak periode degraded/offline mode.
id, opened_at, closed_at,
trigger_reason, status,
backlog_summary, resolved_by
### 12.2 Constraint and indexing guidance
- 
Foreign key enforcement wajib aktif pada setiap koneksi runtime dan test.
- 
Gunakan partial index untuk row aktif, unresolved conflict, pending/retryable item, dan
idempotency key bila query pattern membutuhkannya.
- 
WAL mode dipakai untuk operational database lokal, tetapi database tetap diperlakukan single-
host file.
- 
Unique constraint disarankan minimal pada (event_id), pada kombinasi dedup yang relevan
untuk idempotency, dan pada unresolved active state yang tidak boleh ganda.
- 
Entity sync-participating yang conflict-prone wajib memiliki version kolom optimistic
concurrency.
### 12.3 Transaction boundary rules
## 21. Keputusan bisnis lokal harus commit atomik bersama audit intent dan outbox_event; serialisasi
event yang gagal harus menggagalkan business transaction utama.
## 22. Pembentukan sync_batch dan sync_item adalah transaksi terpisah dari keputusan bisnis asal,
tetapi tetap durable dan recoverable.
## 23. Penerapan inbound update ke tabel bisnis harus berjalan di application service transaction yang
memvalidasi invariant domain.

Event Contract (Sync) Specification · Page 18
## 24. Perubahan final pada sync_conflict, manual investigation, atau resolution outcome harus persist
bersama jejak actor/accountability.
## 13. Reconciliation, Conflict, and Offline Policy
### 13.1 Conflict policy by data class
Data class
Authority posture
Resolution rule
Master data snapshots
Mostly server-authoritative
HQ snapshot menang kecuali policy
override window yang
terdokumentasi.
Financially sensitive entities
Explicit reconciliation
Sale/payment/cash/business_day
divergence tidak boleh last-write-
wins.
Inventory documents
Document/event level resolution
Selesaikan pada sumber event atau
dokumen, bukan overwrite balance
diam-diam.
Audit / approval evidence
Append-only evidence
Tidak di-merge destruktif; gunakan
follow-up evidence bila ada koreksi.
### 13.2 Offline policy
- 
Open shift, opening cash, cash sale, checkout standar, dan receipt issuance boleh tetap berjalan
offline sesuai policy toko.
- 
Return/refund, inventory adjustment tertentu, atau flow yang membutuhkan referensi/policy
segar dibatasi atau ditunda jika cache tidak memadai.
- 
Masuk dan keluar offline mode harus menghasilkan offline_operation_window yang dapat
diaudit.
- 
Semua mutasi offline yang sah harus ditandai pending sync dan dapat direconcile tanpa
kehilangan jejak.
- 
Time drift, cache tidak memadai, atau dependency kritis yang hilang harus memblokir flow
berisiko dengan alasan yang jelas.
### 13.3 Reconciliation operator actions
Action
Who can do it
Result
Retry item/batch
Manager / Supervisor / automated
policy
Menciptakan attempt baru;
retry_count naik; item/batch status
diperbarui.
Resolve conflict
Manager / Supervisor / policy-
specific service
sync_conflict memperoleh
resolution_status final dan sync_item
mengikuti outcome.
Mark manual investigation
Manager / Supervisor
Status akhir terlihat; perlu tindak
lanjut ticket/operational review.
Refresh master snapshot
System / Manager
Membentuk mds record baru;
application service apply path
berjalan.

Event Contract (Sync) Specification · Page 19
## 14. Security, Observability, and Operational Control
- 
Setiap event harus membawa actor context, store_id, terminal_id, dan correlation/idempotency
metadata.
- 
Approval sensitif tetap online-first, tetapi jalur supervisor PIN/offline credential yang diizinkan
policy harus meninggalkan approval evidence lokal untuk sync lanjutan.
- 
Sync dashboard dan backoffice wajib membaca sync_batch, sync_item, sync_conflict,
offline_operation_window, dan master_data_snapshot; log hanyalah evidence tambahan.
- 
Payload sensitif boleh melewati backend bila memang diperlukan, tetapi observability publik
harus memakai masking/redaction sesuai klasifikasi field.
- 
Audit events untuk operasi sensitif bukan telemetry opsional; ia bagian dari keputusan bisnis
yang durable.
Observability contract
Operational control yang sehat membutuhkan batch status, item result, conflict age, unresolved count,
pending offline windows, dan business-day readiness blocker. Dashboard yang hanya membaca log text
dianggap tidak memenuhi kontrak.
## 15. Test and Validation Baseline
Test layer
What must be proven
Unit/common test
Event naming, state machine transitions, idempotency
logic, payload canonicalization, retry eligibility rules.
Component/repository test
Atomic audit+outbox commit, batch/item persistence,
FK/integrity, migration behavior, conflict table update
semantics.
Integration test
/sync/batches partial failure, accepted replay, conflict
response, master snapshot apply path, auth fallback
implications.
UI/functional test
POS sync visibility, desktop reconcile screen, conflict
resolution actions, offline mode gating and reason
display.
Manual/UAT
Emergency offline operations, failed sync backlog
review, supervisor reconciliation, business-day
readiness review.
- 
Wajib dibuktikan bahwa sync_batch dan sync_item menyimpan hasil per item, bukan status
global samar.
- 
Retry tidak boleh menggandakan efek bisnis pada aggregate tujuan.
- 
sync_conflict tidak boleh berakhir tanpa resolution_status yang eksplisit.
- 
outbox_event gagal atau item gagal tidak boleh hilang tanpa history retry/error yang memadai.
- 
offline_operation_window harus menghubungkan masuk-keluar degraded mode dengan backlog
yang direconcile.

Event Contract (Sync) Specification · Page 20
## 16. Implementation Guardrails
Do this
Reject this
Tuliskan outbox_event bersama audit intent di transaksi
lokal yang sama dengan keputusan bisnis.
Mengirim audit/sync record lewat fire-and-forget
coroutine setelah transaksi selesai.
Bangun SyncFacade/SyncOrchestrator dan repository
sync terpisah dari context bisnis asal.
Menyuruh setiap feature menulis langsung ke sync
tables feature lain secara ad hoc.
Terapkan inbound item via application services.
Menjalankan raw SQL mutation script dari payload HQ
ke tabel bisnis.
Pertahankan batch result dan item result keduanya.
Meringkas seluruh attempt menjadi satu flag
sukses/gagal global.
Jadikan conflict dan offline window visible ke UI
operasional.
Menyembunyikan failure di background retry loop
tanpa terminal state.
Pisahkan event_id, aggregate_id, dan idempotency_key.
Menggunakan satu string untuk semua konsep identitas
demi 'kesederhanaan'.
Appendix A. Prepared Boundaries
- 
F&B settlement tetap harus converge ke sales/payment/receipt kernel. Jika kelak ada event F&B,
penamaannya mengikuti pola yang sama, misalnya fb.order.settled.v1, tetapi batch transport dan
conflict policy tetap memakai sync context yang sama.
- 
Service order dan service_part_usage juga harus converge ke sales/invoice dan inventory ledger.
Event service kelak tidak boleh menciptakan ledger paralel.
- 
Prepared boundaries tidak mengubah kontrak inti phase-1; mereka hanya mewarisi envelope,
batch transport, idempotency, dan reconciliation model yang sama.
Appendix B. Sample JSON Payload Catalog
B.1 sales.sale.completed.v1
{
"event_type": "sales.sale.completed.v1",
"aggregate_type": "sale_transaction",
"aggregate_id": "sal_0195b2f7-5c1c-7225-a958-67eb7b6fd584",
"aggregate_version": 7,
"payload": {
"receipt_no": "STR01-20260308-000123",
"source_channel": "RETAIL_DIRECT",
"settlement_status": "SETTLED",
"totals": {
"subtotal": {"amount": "180000", "currency": "IDR"},
"discount_total": {"amount": "10000", "currency": "IDR"},
"tax_total": {"amount": "17000", "currency": "IDR"},
"grand_total": {"amount": "187000", "currency": "IDR"}
},
"payments": [
{"payment_id": "pay_...", "method": "CASH", "status": "SUCCESS", "amount": {"amount": "187000", "currency":
"IDR"}}
]
}
}

Event Contract (Sync) Specification · Page 21
B.2 inventory.adjustment.applied.v1
{
"event_type": "inventory.adjustment.applied.v1",
"aggregate_type": "stock_adjustment",
"aggregate_id": "adj_0195b31e-2d4d-73ba-b95f-973c0a2d5a10",
"aggregate_version": 3,
"payload": {
"adjustment_no": "ADJ-STR01-20260308-00015",
"reason_code": "DAMAGE",
"approval_request_id": "apr_0195b31e-19af-707d-b33b-0db6d6d74af2",
"lines": [
{
"product_id": "prd_...",
"location_id": "loc_...",
"bucket_code": "SELLABLE",
"delta_qty": {"amount": "-2", "uom": "EA"}
}
],
"ledger_summary": {"source_type": "STOCK_ADJUSTMENT", "entry_count": 1}
}
}
B.3 sync_conflict record
{
"conflict_id": "syc_0195b301-d0ef-7129-9ccd-64a4fbeb9864",
"sync_item_id": "syi_0195b301-c080-7e2f-b9c0-8ec88ff90d84",
"conflict_type": "VERSION_DIVERGED",
"aggregate_type": "sale_transaction",
"aggregate_id": "sal_0195b2f7-5c1c-7225-a958-67eb7b6fd584",
"local_version": 7,
"remote_version": 8,
"resolution_status": "OPEN",
"local_summary": {"settlement_status": "SETTLED", "grand_total": "187000"},
"remote_summary": {"settlement_status": "VOIDED", "grand_total": "187000"}
}
B.4 offline_operation_window
{
"window_id": "ofw_0195b3a4-f9c1-7333-b9f0-82c7e6417c12",
"opened_at": "2026-03-08T09:10:12.000Z",
"closed_at": null,
"trigger_reason": "HQ_CONNECTIVITY_DOWN",
"status": "OPEN",
"backlog_summary": {
"pending_outbox_count": 17,
"financial_events": 6,
"masterdata_required": true
}
}
B.5 masterdata.snapshot.refreshed.v1
{

Event Contract (Sync) Specification · Page 22
"event_type": "masterdata.snapshot.refreshed.v1",
"direction": "INBOUND",
"aggregate_type": "master_data_snapshot",
"aggregate_id": "mds_0195b3b9-4e0d-76a2-b995-6301d4c5eed1",
"payload": {
"snapshot_type": "price_policy",
"version": "2026.03.08.01",
"effective_at": "2026-03-08T11:00:00.000Z",
"records": [
{"policy_id": "ppc_...", "name": "Retail Standard", "tax_included": false}
]
}
}
Appendix C. Naming Conventions and Prefixes
- 
Context names: kernel, auth, cash, sales, payments, receipts, returns, inventory, masterdata, sync,
reporting, fb, service.
- 
Status enum names: gunakan UPPER_SNAKE_CASE untuk persistence; gunakan string yang sama
di API untuk menghindari translation layer ambigu.
- 
Reason/error code names: gunakan machine-readable code seperti VERSION_DIVERGED,
IDEMPOTENCY_REUSED, POLICY_NOT_AVAILABLE_OFFLINE, AGGREGATE_NOT_FOUND,
SNAPSHOT_VERSION_CONFLICT.
- 
Correlation/idempotency string: boleh human-debuggable, tetapi tetap opaque dan tidak
menyandikan rahasia.
- End of Specification -


## Constraints / Policies
Sync bukan background queue diam-diam; failure harus berakhir pada terminal state yang jelas.

## Technical Notes
Idempotency key dipisahkan dari event identity; inbound apply wajib lewat application services.

## Dependencies / Related Documents
- `cassy_architecture_specification_v1.md`
- `store_pos_erd_specification_v2.md`
- `store_pos_domain_model_detail_specifications_v2.md`
- `cassy_migration_script_specification.md`
- `store_pos_test_specification.md`
- `cassy_test_automation_specification.md`

## Risks / Gaps / Ambiguities
- Tidak ditemukan gap fatal saat ekstraksi. Tetap review ulang bagian tabel/angka jika dokumen ini akan dijadikan baseline implementasi final.

## Reviewer Notes
- Struktur telah dinormalisasi ke Markdown yang konsisten dan siap dipakai untuk design review / engineering handoff.
- Istilah utama dipertahankan sedekat mungkin dengan dokumen sumber untuk menjaga traceability lintas artefak.
- Bila ada konflik antar dokumen, prioritaskan source of truth, artefak yang paling preskriptif, dan baseline yang paling implementable.

## Source Mapping
- Original source: `Cassy_Event_Contract_Sync_Specification_v1.pdf` (PDF, 22 pages)
- Output markdown: `cassy_event_contract_sync_specification_v1.md`
- Conversion approach: text extraction -> normalization -> structural cleanup -> standardized documentation wrapper.
- Note: beberapa tabel/list di PDF dapat mengalami wrapping antar baris; esensi dipertahankan, tetapi layout tabel asli tidak dipertahankan 1:1.
