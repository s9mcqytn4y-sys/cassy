# Store POS ERD Specification v2

## Document Overview
Target-state ERD/database specification yang menutup gap schema lama dan menyelaraskan persistence model lintas domain bisnis.

## Purpose
Menetapkan spesifikasi ERD v2 yang traceable ke artefak proyek, menutup kekurangan Schema.sq saat ini, dan menyelaraskan persistence model untuk POS/Retail, F&B, Service Order, Inventory, Cash Control, Reporting, dan Sync/Offline. Dokumen ini diposisikan pada langkah Database...

## Scope
Shared kernel, sales, return/refund, shift & cash, inventory, F&B boundary, service order boundary, reporting, sync, dan offline.

## Key Decisions / Core Rules
Current state dipisah dari historical explanation trail; business numbers dipisah dari surrogate key; offline-first entity wajib punya version/sync marker yang eksplisit.

## Detailed Content

### Normalized Source Body
Store / POS System
ERD Specification v2
Retail · F&B · Service Order · Inventory · Shift/Cash · Offline Sync
Document type
Detailed database / ERD design specification
Purpose
Baseline for architecture, schema refactor,
implementation, migration, and test traceability
Primary evidence base
UML source of truth, use case spec, activity spec,
sequence spec, domain model spec v2, and
attached Schema.sq
Date
2026-03-08
Dokumen ini menyusun target-state ERD v2 yang bukan sekadar menyalin Schema.sq, tetapi menutup
gap yang dibuktikan oleh artefak use case, activity, sequence, dan domain model. Fokusnya adalah
integrasi data lintas bisnis dengan batas konsistensi yang realistis dan siap diturunkan ke implementasi
SQLite / SQLDelight serta sinkronisasi ke HQ.

## 1. Tujuan dokumen
Menetapkan spesifikasi ERD v2 yang traceable ke artefak proyek, menutup kekurangan Schema.sq saat
ini, dan menyelaraskan persistence model untuk POS/Retail, F&B, Service Order, Inventory, Cash
Control, Reporting, dan Sync/Offline.
Dokumen ini diposisikan pada langkah Database dalam urutan SDLC Use Case
Activity
Sequence
->
->
->
Domain Model
Architecture
Database
Implementation
Test. Karena itu isi dokumen ini sengaja
->
->
->
->
fokus pada model data implementable, bukan diagram kosmetik.
## 2. Basis riset dan evidence yang dipakai
Ref
Sumber
Peran dalam desain
SRC-01
UML-Modeling-Source-of-Truth.txt
Mewajibkan traceability dari use
case sampai database serta
menekankan correctness bisnis dan
integritas persistence.
SRC-02
store_pos_use_case_detail_specificat
ions.pdf
Menetapkan scope: sales,
return/refund, shift/cash control,
inventory operations, reporting,
sync, dan offline mode.
SRC-03
store_pos_activity_detail_specificati
ons.pdf
Menjelaskan flow receiving,
transfer, return/refund, cash
movement, dan exception path
yang menuntut state + audit
persistable.
SRC-04
store_pos_sequence_detail_specifica
tions.pdf
Menunjukkan persistence
touchpoints: approval, audit,
opening cash, end of day,
repository, database, idempotency,
retry, dan partial failure handling.
SRC-05
store_pos_domain_model_detail_spe
cifications_v2.pdf
Menetapkan aggregate penting:
Sale, ReturnTransaction,
CashierShift, BusinessDay,
InventoryBalance,
ReceivingDocument, StockTransfer,
CycleCount, DamageDisposition,
PriceLabelJob,
StoreOperationalReport, SyncBatch.
SRC-06
Schema.sq
Baseline implementasi aktual yang
sudah memuat master data, sale,
payment, refund, outbox, dine-in,
service order, dan device config
namun masih memiliki gap
penting.
EXT-01
## Rfc 9562
Rujukan standar untuk UUID,
khususnya UUIDv7 sebagai time-
ordered identifier.
EXT-02
SQLite foreign key / partial index /
WAL docs
Rujukan untuk constraint
enforcement, partial index, dan
karakteristik
concurrency/performance SQLite.

## 3. Diagnosis terhadap Schema.sq saat ini
Schema.sq sudah kuat untuk MVP operasional penjualan. Kekuatan utamanya: master data dasar, sales
snapshot, payment detail, approval/audit, refund administratif, service order, order session dine-in,
kitchen ticket, outbox event, dan device configuration.
- 
Shift/Cash belum lengkap karena aturan bisnis menuntut cashier-terminal uniqueness, opening
cash, cash movement, safe drop, reconciliation, shift report, dan business day. Schema saat ini baru
memisahkan cashier_session dan cash_session tanpa business_day, terminal master, safe_drop, atau
cash_reconciliation terpisah.
- 
Inventory domain masih MVP. Dokumen domain meminta current-state balance per store-product-
location-bucket dan historical explanation yang eksplisit. Schema saat ini masih mengandalkan
product.stock_qty_milli + stock_movement; itu tidak cukup untuk receiving, transfer, replenishment,
cycle count, damaged bucket, dan multi-location.
- 
Return/refund belum memodelkan return aggregate secara penuh. Activity dan domain menuntut
eligibility, return line, policy decision, approval, inventory disposition, dan fallback outcome seperti
store credit; schema sekarang baru menangkap refund_record dan refund_item.
- 
Reporting & sync belum cukup granular. Domain sudah memerlukan SyncBatch, SyncItem,
SyncConflict, OfflineOperationWindow, MasterDataSnapshot, dan operational report. Schema saat
ini baru punya outbox_event sederhana.
- 
Integrasi lintas bisnis sudah mulai ada lewat order_type, order_session, service_order, dan
sale_transaction, tetapi belum ada shared business day, shared stock ledger, shared approval
context, dan numbering strategy yang konsisten lintas domain.
## 4. Prinsip desain target-state ERD v2
- 
Satu sumber kebenaran per concern: current state dipisah dari historical trail. Contoh:
InventoryBalance ≠ StockLedgerEntry.
- 
Document numbers dipisah dari surrogate key. Nomor seperti receipt_no, invoice_no, transfer_no,
business_day_no diperlakukan sebagai business identifier yang dapat berubah format, sedangkan
PK tetap opaque dan immutable.
- 
Seluruh flow exception yang memengaruhi auditability harus punya jejak persistable: reason_code,
reason_note, requester, approver, decided_at, actor, idempotency_key, version, dan sync status bila
relevan.
- 
Entity yang aktif offline-first wajib memiliki kombinasi minimal: id, version, created_at, updated_at,
server_sync_at atau sync marker, serta metadata conflict/resolution bila ada.
- 
Lintas bisnis dipersatukan melalui shared kernel: store, terminal, employee, product, order_type,
business_day, reason_code, approval_request, audit_log, outbox/sync, dan identity convention yang
seragam.
## 5. Boundary data lintas bisnis
Area
Entitas utama
Arah integrasi
Shared Kernel
store_profile, pos_terminal,
employee, role/pin state, file_asset,
reason_code, approval_request,
Digunakan oleh semua bounded
context.

Area
Entitas utama
Arah integrasi
audit_log, app_kv
Retail / POS Sales
sale_transaction, sale_item,
payment, payment_allocation,
receipt, invoice, suspended_sale,
price_policy, voucher/member
snapshot opsional
Checkout final yang menghasilkan
settlement dan ledger event.
Return & Refund
return_transaction, return_line,
refund_record, refund_item,
return_policy_decision,
store_credit_account,
store_credit_ledger
Memisahkan return intent/policy
dari refund settlement.
Shift & Cash
business_day, cashier_session,
opening_cash, cash_session,
cash_movement, safe_drop,
cash_reconciliation, shift_report
Menaungi operasi kas dan hari
operasional.
Inventory
inventory_location,
inventory_bucket,
inventory_balance,
stock_ledger_entry,
receiving_document/line/discrepan
cy, stock_transfer/line,
replenishment_task/line,
stock_adjustment/line,
cycle_count/line,
damage_disposition/line,
price_label_job/line
Satu model stok untuk retail, F&B,
dan service parts/consumables.
F&B
table_room, table_slot,
order_session, order_session_item,
table_session, kitchen_ticket,
routing_profile
Front-of-house/back-of-house flow
yang tetap settle ke
sale_transaction.
Service Order
service_order, service_status_log,
service_task/service_part usage
opsional
Order kerja layanan yang dapat
tertagih melalui invoice atau sale.
Reporting & Sync
store_operational_report,
report_snapshot_line, sync_batch,
sync_item, sync_conflict,
master_data_snapshot,
offline_operation_window,
outbox_event
Menjaga sinkronisasi dan
rekonsiliasi antar node.
## 6. Shared Kernel dan identitas referensial
Target-state ERD v2 menambahkan dua fondasi yang saat ini belum cukup eksplisit di schema: (1)
pos_terminal untuk mengikat cashier shift ke terminal fisik/logis, dan (2) business_day sebagai payung
operasional lintas shift, sales, cash, inventory exception, dan end-of-day report.
Entitas
Peran
Keputusan v2
store_profile
Master toko / cabang.
Tetap dipertahankan; menjadi
parent untuk business_day,
terminal, inventory location, dan
report.
pos_terminal
Terminal POS fisik/logis per store.
Baru. Dibutuhkan karena aturan

Entitas
Peran
Keputusan v2
shift mensyaratkan kombinasi
cashier-terminal yang unik.
employee
Pelaku sistem dan approver.
Perlu diekstensi role agar
mencakup supervisor, inventory
staff, dan service staff, bukan hanya
owner/manager/cashier.
reason_code
Master alasan exception.
Perlu diperluas kategori: VOID,
RETURN, ADJUST, RECEIVING_GAP,
SAFE_DROP, DAMAGE,
PRICE_OVERRIDE, TRANSFER,
CYCLE_COUNT.
approval_request
Persetujuan lintas domain.
Tetap dipertahankan tetapi
request_type harus lebih generik
dan entity_type+entity_id wajib
konsisten.
audit_log
Jejak perubahan dan keputusan.
Tetap dipertahankan sebagai cross-
cutting event/audit store.
## 7. Spesifikasi ERD per domain
### 7.1 Sales & Checkout
Sales tetap menjadi aggregate operasional inti untuk settlement akhir. Semua channel komersial-
Retail, Dine-In/Takeaway/Delivery, dan Service-pada akhirnya harus dapat menghasilkan
sale_transaction, invoice, atau keduanya.
Entitas
Peran
Perubahan v2
sale_transaction
Header transaksi final.
Tambah business_day_id,
terminal_id, pricing_context_id
opsional, settlement_status,
tax_rounding_policy, and
source_channel.
sale_item
Snapshot line item.
Tetap; perlu
source_session_type/source_session
_id agar dapat ditelusuri dari
order_session atau service_order.
payment
Catatan pembayaran individual.
Tetap; source perlu
mengakomodasi gateway callback
dan reversal.
payment_allocation
Alokasi pembayaran ke
sale/invoice/refund/store credit.
Perlu diperluas target_type:
STORE_CREDIT_TOPUP,
STORE_CREDIT_APPLY.
receipt
Artefak transaksi final.
Tetap; wajib refer ke
routing_profile/print channel bila
printed receipt diperlakukan
penting.
invoice
Piutang / dokumen tagihan.
Tetap; untuk service order dan
akun bisnis.
suspended_sale
Transaksi parkir / resume.
Baru; dibutuhkan oleh use case
suspend/resume dan mencegah
overload sale_transaction.

### 7.2 Return, Refund, dan Store Credit
Dokumen activity/sequence menunjukkan bahwa return bukan sekadar refund administratif. Karena
itu v2 memisahkan return intent, eligibility, inventory disposition, dan refund settlement.
Entitas
Peran
Keputusan v2
return_transaction
Aggregate root return.
Baru. Menyimpan
original_sale_tx_id,
original_receipt_no snapshot,
policy_decision, disposition
outcome, requested_by,
approved_by, status.
return_line
Item yang dikembalikan.
Baru. Menyimpan
original_tx_item_id, qty_returned,
condition_code, eligibility_result,
inventory_disposition_bucket.
return_policy_decision
Snapshot hasil validasi policy.
Baru. Menyimpan rule_version,
decision, reasons, override flag.
refund_record / refund_item
Settlement uang dari return atau
koreksi finansial.
Dipertahankan tetapi direlasikan ke
return_transaction jika refund
berasal dari return.
store_credit_account
Saldo store credit per
customer/contact.
Baru. Diperlukan untuk fallback
refund ketika metode asli tidak
dapat dipulihkan.
store_credit_ledger
Jejak mutasi store credit.
Baru. Mencatat top-up, apply,
expire, reverse.
### 7.3 Shift, Cash Control, dan Business Day
Domain model mewajibkan CashierShift dan BusinessDay sebagai aggregate utama. Karena itu v2
menormalisasi area ini dan mengurangi ambiguitas antara cashier_session dan cash_session.
Entitas
Peran
Perubahan v2
business_day
Hari operasional toko.
Baru. Menyimpan business_date,
status, open/close timestamps,
opened_by, closed_by, operational
summary, readiness flags.
cashier_session
Shift kasir pada satu terminal.
Tambah store_id, business_day_id,
terminal_id, status lifecycle yang
lebih kaya: OPEN,
HANDOVER_PENDING, CLOSED,
FORCE_CLOSED.
opening_cash
Kas awal tervalidasi.
Baru bila ingin audit lebih baik;
bisa juga tetap embedded di
cash_session tetapi entity terpisah
lebih traceable.
cash_session
Laci kas / drawer session.
Dipertahankan sebagai drawer-
level session; dapat linked ke satu
atau banyak cashier_session
tergantung policy toko.
cash_movement
Cash in/out non-sale.
Tambahkan movement_type yang
lebih semantik: CASH_IN,
CASH_OUT, SALE_INFLOW,
REFUND_OUTFLOW, SAFE_DROP,

Entitas
Peran
Perubahan v2
CLOSING_ADJUSTMENT.
safe_drop
Pengeluaran kas ke lokasi aman.
Baru. Diperlukan karena safe drop
punya actor, witness/approver,
amount, bag/receipt ref, dan state
terpisah.
cash_reconciliation
Perbandingan expected vs counted.
Baru. Menyimpan counted_total,
expected_total, diff, decision,
approved_by, notes.
shift_report
X/Z report per shift/day.
Baru. Menyimpan report_type,
generated_at, snapshot totals,
file_asset_id opsional.
### 7.4 Inventory, Receiving, Transfer, Cycle Count, Damage, Label
Ini adalah gap terbesar pada Schema.sq. V2 mengubah inventory dari sekadar kolom qty di product
menjadi subsystem yang mampu menjelaskan state saat ini, jejak historis, dan dokumen sumber
perubahan stok.
Entitas
Peran
Keputusan v2
inventory_location
Lokasi fisik di toko atau area kerja.
Baru. Contoh: sales_floor,
backroom, kitchen, service_bench,
quarantine.
inventory_bucket
Bucket status stok.
Baru. Contoh: SELLABLE,
RESERVED, DAMAGED, EXPIRED,
INSPECTION, IN_TRANSIT.
inventory_balance
Current state per store-product-
location-bucket.
Baru. Menjadi sumber saldo real-
time; menggantikan
product.stock_qty_milli sebagai
source of truth.
stock_ledger_entry
Historical explanation trail.
Baru. Satu-satunya jejak resmi
perubahan stok dari sale, return,
receiving, transfer, adjustment,
cycle count, damage, service usage.
receiving_document / receiving_line
/ receiving_discrepancy
Dokumen penerimaan barang.
Baru. Mendukung partial receiving
dan discrepancy yang tidak boleh
hilang diam-diam.
stock_transfer / stock_transfer_line
Transfer antar lokasi / store.
Baru. Status: REQUESTED,
APPROVED, IN_TRANSIT,
RECEIVED, CANCELLED.
replenishment_task /
replenishment_line
Backroom
selling floor / kitchen.
->
Baru. Menyokong replenishment
operasional cepat.
stock_adjustment /
stock_adjustment_line
Perubahan stok manual.
Baru. Wajib reason code dan
approval sesuai threshold.
cycle_count / cycle_count_line
Stock opname / cycle count.
Baru. Menyimpan book_qty,
counted_qty, variance, resolution.
damage_disposition /
damage_disposition_line
Barang rusak/expired/non-sellable.
Baru. Mengatur keputusan salvage,
return to vendor, destroy, write-off.
price_label_job / price_label_line
Cetak label harga atau rak.
Baru. Berhubungan ke product dan
pricing snapshot.

Konsekuensi desain: product.stock_qty_milli pada schema lama sebaiknya diturunkan menjadi
cache/denormalized column atau dihapus bertahap setelah inventory_balance stabil. Current state dan
ledger tidak boleh dipertukarkan.
### 7.5 F&B / Table Service
Entitas
Peran
Keputusan v2
table_room, table_slot
Master area dan meja.
Dipertahankan.
order_session
Header order
DINE_IN/TAKEAWAY/DELIVERY.
Tetap; tambah business_day_id,
terminal_id/opened_terminal_id,
kitchen_status summary,
guest_count, linked_invoice_id
opsional.
order_session_item
Line item order.
Tetap; tambah fulfilment_status dan
fire_course / prep station bila
dibutuhkan.
table_session
Occupancy lifecycle meja.
Tetap.
routing_profile
Routing printer/channel.
Tetap.
kitchen_ticket
Ticket persiapan dapur.
Tetap; idealnya ditambah
kitchen_station, reprint_count,
cancelled_by.
Integrasi kunci: order_session tidak menjadi transaksi finansial final. Settlement tetap masuk ke
sale_transaction/payment/receipt sehingga laporan kas dan pajak tetap satu jalur dengan retail dan
service.
### 7.6 Service Order
Entitas
Peran
Keputusan v2
service_order
Header order layanan.
Dipertahankan; tambah
business_day_id,
terminal_id_created,
promised_sla_class,
service_category, asset/customer
reference opsional.
service_status_log
Jejak perubahan status.
Dipertahankan.
service_task
Pekerjaan detail per order.
Baru opsional namun sangat
dianjurkan jika layanan punya
multi-step execution.
service_part_usage
Pemakaian sparepart/consumable.
Baru; menghasilkan stock ledger
entry agar inventory service
tersinkron.
### 7.7 Reporting, Sync, Offline
Entitas
Peran
Keputusan v2
store_operational_report
Laporan operasional per period.
Baru. Menyimpan snapshot KPI
yang diturunkan dari event
tervalidasi.
report_snapshot_line
Detail metrik laporan.
Baru. Memudahkan komposisi KPI
tanpa mengubah schema report
utama.

Entitas
Peran
Keputusan v2
outbox_event
Outbound event local-first.
Dipertahankan; jadikan lower-level
queue, bukan satu-satunya model
sync.
sync_batch
Unit kerja sync ke/dari HQ.
Baru. Menaungi kumpulan sync
item dan status batch.
sync_item
Per item aggregate change.
Baru. Menyimpan aggregate_type,
aggregate_id, event_type, direction,
retry_count, status, payload_hash.
sync_conflict
Konflik state/version.
Baru. Menyimpan local_version,
remote_version, resolution_status,
resolved_by, resolved_at.
master_data_snapshot
Snapshot data master dari HQ.
Baru. Mendukung versioning data
referensi seperti product, policy,
price list.
offline_operation_window
Periode operasi offline.
Baru. Diperlukan agar backlog dan
resync dapat diaudit.
## 8. Aturan relasi inti yang wajib dijaga
- 
business_day 1..* cashier_session; business_day 1..* order_session; business_day 1..* service_order;
business_day 1..* store_operational_report.
- 
cashier_session harus mereferensikan tepat satu pos_terminal dan satu employee (cashier) aktif
pada saat dibuka.
- 
sale_transaction dapat berasal dari retail langsung, settlement order_session, atau settlement
service_order; source_session_type + source_session_id harus tersedia untuk traceability.
- 
inventory_balance memiliki unique key: (store_id, product_id, location_id, bucket_code).
- 
setiap perubahan stok yang sah harus menghasilkan stock_ledger_entry dengan source_type +
source_id + source_line_id yang jelas.
- 
return_line dapat menghasilkan stock_ledger_entry masuk ke bucket tertentu
(SELLABLE/INSPECTION/DAMAGED), bukan otomatis ke saldo jual.
- 
service_part_usage dan order_session_item yang memakai stok harus menghasilkan
stock_ledger_entry sehingga semua channel berbagi ledger yang sama.
- 
sync_conflict tidak boleh ditandai selesai tanpa resolution_status yang eksplisit; outbox_event gagal
tidak boleh hilang tanpa jejak retry/error.
## 9. Rencana penutupan gap terhadap Schema.sq
Area lama
Masalah
Arah v2
product.stock_qty_milli
Masih menjadi sumber saldo stok.
Ganti menjadi cache/derived field
dari inventory_balance; jangan lagi
jadi source of truth.
stock_movement
Masih terlalu sempit dan hanya
mendukung sebagian event.
Migrasi ke stock_ledger_entry;
stock_movement dapat dipetakan
sementara sebagai legacy
view/compat table.
employee.role
Role enum terlalu sempit.
Perlu role/permission model yang
mencakup supervisor, inventory
staff, service staff, store manager,

Area lama
Masalah
Arah v2
atau role table terpisah.
reason_code.category
Baru ADJUST dan VOID.
Perlu kategori lebih luas lintas
domain.
cashier_session
Belum punya terminal_id dan
business_day_id.
Wajib ditambah.
cash_session
Belum punya safe_drop /
reconciliation terpisah.
Tambahkan safe_drop dan
cash_reconciliation.
refund_record
Belum memodelkan return
aggregate.
Tambahkan return_transaction,
return_line, return_policy_decision,
store_credit tables.
outbox_event
Belum cukup untuk sync
orchestration.
Tambah sync_batch, sync_item,
sync_conflict,
master_data_snapshot,
offline_operation_window.
service_order
Sudah baik sebagai stub domain.
Tambah service_task dan
service_part_usage bila layanan
butuh detail operasional dan
inventory trace.
order_session
Sudah mendukung F&B dasar.
Tambah relasi ke
business_day/terminal dan status
fulfillment summary.
## 10. Standar pengkodean ID dan business numbering
Rekomendasi inti: gunakan dua lapis identitas. Lapis pertama adalah surrogate primary key yang
opaque, immutable, dan tidak menyimpan makna bisnis. Lapis kedua adalah business/document
number yang readable untuk operator dan pelacakan operasional. Strategi ini menjaga integritas
referensial sekaligus fleksibel terhadap perubahan format dokumen.
- 
PK entity menggunakan pola application convention: <prefix>_<uuidv7>. Contoh: sal_019588a2-c240-
7b18-98f0-2d3f4c5e6a7b. Bagian UUIDv7 mengikuti RFC 9562 dan memberi ordering temporal yang
lebih ramah indeks daripada UUID acak penuh.
- 
Business number dipisahkan ke kolom tersendiri: receipt_no, invoice_no, transfer_no, receiving_no,
business_day_no, session_no, ticket_no, order_no.
- 
Jangan menyisipkan store code, tanggal, shift, atau nomor urut ke PK. Informasi itu boleh muncul di
business number dan dapat diubah formatnya tanpa merusak foreign key.
- 
Untuk SQLite mobile/offline, simpan PK sebagai TEXT bila ergonomi debugging lebih penting;
namun jika ukuran file menjadi isu besar, pertimbangkan BLOB 16-byte untuk raw UUIDv7 dengan
external string wrapper di application layer.
- 
Setiap tabel yang di-create offline dan disinkronkan ke HQ wajib punya idempotency key bila ada
command retriable, plus version untuk optimistic conflict handling.
Tabel prefix yang direkomendasikan:
Entity
Prefix
store_profile
str
pos_terminal
ter
employee
emp
category
cat

Entity
Prefix
file_asset
ast
product
prd
price_policy
ppc
business_day
bdy
cashier_session
chs
cash_session
cshs
opening_cash
opc
cash_movement
csm
safe_drop
sfd
cash_reconciliation
crn
shift_report
shr
sale_transaction
sal
sale_item
sli
payment
pay
payment_allocation
pal
receipt
rcp
invoice
invc
suspended_sale
sus
Entity
Prefix
return_transaction
rtn
return_line
rtl
refund_record
rfd
refund_item
rfi
store_credit_account
sca
store_credit_ledger
scl
inventory_location
loc
inventory_balance
ibl
stock_ledger_entry
led
receiving_document
rcv
receiving_line
rcl
receiving_discrepancy
rcd
stock_transfer
trf
stock_transfer_line
trl
replenishment_task
rpt
replenishment_line
rpl
stock_adjustment
adj
stock_adjustment_line
adl
cycle_count
cyc
cycle_count_line
cyl
damage_disposition
dmg
damage_disposition_line
dgl
Entity
Prefix
price_label_job
plj
price_label_line
pll
table_room
tblr
table_slot
tbl
order_session
ors
order_session_item
osi

Entity
Prefix
table_session
tbs
routing_profile
rtp
kitchen_ticket
ktt
service_order
svo
service_status_log
svl
service_task
svt
service_part_usage
spu
approval_request
apr
audit_log
aud
outbox_event
obx
sync_batch
syb
sync_item
syi
sync_conflict
syc
master_data_snapshot
mds
offline_operation_window
ofw
store_operational_report
rpto
Entity
Prefix
report_snapshot_line
rps
## 11. Aturan business number
Number field
Tujuan
Contoh format
receipt_no
Readable oleh kasir dan pelanggan.
STR01-20260308-000123 atau
format lain yang diatur store/HQ.
invoice_no
Dokumen penagihan formal.
INV-STR01-202603-000045.
session_no
Nomor order session F&B.
OS-DI-20260308-00012.
ticket_no
Nomor kitchen ticket.
KT-KITCHEN-20260308-00077.
order_no
Nomor service order.
SO-SVC-20260308-00034.
business_day_no
Hari operasional.
BD-STR01-20260308.
receiving_no
Dokumen receiving.
RCV-STR01-20260308-00009.
transfer_no
Dokumen transfer.
TRF-STR01-STR02-20260308-00003.
adjustment_no
Stock adjustment.
ADJ-STR01-20260308-00015.
cycle_count_no
Cycle count/opname.
CC-STR01-ZN-A-20260308-00008.
Catatan penting: format nomor dokumen boleh berubah sesuai regulasi, kebijakan HQ, atau branding.
Karena itu document number tidak boleh menjadi foreign key utama antartabel.
## 12. Rekomendasi implementasi SQLite / SQLDelight
- 
Pastikan foreign key enforcement dinyalakan konsisten pada setiap koneksi aplikasi, bukan hanya
sekali di script schema.
- 
Pertahankan partial indexes untuk data aktif/deleted_at IS NULL dan idempotency key; ini sangat
cocok untuk local database yang banyak tombstone/soft-delete.
- 
Pertimbangkan WAL mode pada database operasional lokal agar baca/tulis lebih konkuren, tetapi
tetap kelola checkpoint dan pahami bahwa semua proses harus berada pada host yang sama.

- 
Gunakan unique composite key pada entity balance/stateful untuk mencegah duplikasi state:
misalnya inventory_balance(store_id, product_id, location_id, bucket_code).
- 
Pisahkan write model dan read model bila dashboard/reporting mulai berat; namun source of truth
tetap event + state tables yang tervalidasi, bukan angka ad hoc.
## 13. Kesimpulan keputusan desain
- 
Schema.sq saat ini cukup sebagai MVP transaksi, tetapi belum cukup sebagai baseline enterprise
store system lintas retail, F&B, dan services.
- 
ERD v2 harus menambahkan business_day, pos_terminal, inventory subsystem penuh, return
aggregate, store credit, safe_drop/reconciliation, dan sync orchestration entities.
- 
Semua channel komersial harus converge ke shared sales/cash/inventory/reporting kernel agar data
tidak terpecah per bisnis.
- 
Standar ID terbaik untuk fase ini adalah prefixed UUIDv7 sebagai PK + business numbering terpisah
untuk operasional.
Appendix A. Target-state ERD v2 entity inventory
Area
Entitas
Shared Kernel
store_profile, pos_terminal, employee, category,
file_asset, order_type, price_policy, reason_code,
approval_request, audit_log, pin_guard_state, app_kv
Sales
sale_transaction, sale_item, payment,
payment_allocation, receipt, invoice, suspended_sale
Return
return_transaction, return_line,
return_policy_decision, refund_record, refund_item,
store_credit_account, store_credit_ledger
Shift/Cash
business_day, cashier_session, opening_cash,
cash_session, cash_movement, safe_drop,
cash_reconciliation, shift_report
Inventory
inventory_location, inventory_bucket,
inventory_balance, stock_ledger_entry,
receiving_document, receiving_line,
receiving_discrepancy, stock_transfer,
stock_transfer_line, replenishment_task,
replenishment_line, stock_adjustment,
stock_adjustment_line, cycle_count, cycle_count_line,
damage_disposition, damage_disposition_line,
price_label_job, price_label_line
F&B
table_room, table_slot, order_session,
order_session_item, table_session, routing_profile,
kitchen_ticket
Service
service_order, service_status_log, service_task,
service_part_usage
Reporting/Sync
store_operational_report, report_snapshot_line,
outbox_event, sync_batch, sync_item, sync_conflict,
master_data_snapshot, offline_operation_window
Appendix B. External standards referenced

Referensi
Pemakaian dalam dokumen
## Rfc 9562
Standar IETF untuk UUID; dipakai sebagai dasar
rekomendasi UUIDv7 time-ordered.
SQLite Foreign Key Support
Menjelaskan enforcement foreign key yang harus
diaktifkan per koneksi.
SQLite Partial Indexes
Dasar penggunaan partial index untuk row aktif/soft-
deleted dan idempotency key.
SQLite WAL
Dasar rekomendasi WAL untuk local-first workload
dengan concurrency baca/tulis.
Companion artifact yang disarankan: file PlantUML ERD v2 terpisah untuk visual review dan iterasi
teknik. Dokumen PDF ini berfungsi sebagai specification baseline yang menjelaskan kenapa struktur
data tersebut ada, apa boundary-nya, dan bagaimana entity lintas bisnis saling berintegrasi.


## Constraints / Policies
Auditability, idempotency, FK integrity, numbering, dan cross-domain linkage wajib persistable.

## Technical Notes
ERD v2 harus dibaca bersama domain model, architecture, migration, dan sync contract.

## Dependencies / Related Documents
- `uml_modeling_source_of_truth.md`
- `store_pos_domain_model_detail_specifications_v2.md`
- `cassy_architecture_specification_v1.md`
- `cassy_migration_script_specification.md`
- `cassy_event_contract_sync_specification_v1.md`
- `store_pos_test_specification.md`

## Risks / Gaps / Ambiguities
- Tidak ditemukan gap fatal saat ekstraksi. Tetap review ulang bagian tabel/angka jika dokumen ini akan dijadikan baseline implementasi final.

## Reviewer Notes
- Struktur telah dinormalisasi ke Markdown yang konsisten dan siap dipakai untuk design review / engineering handoff.
- Istilah utama dipertahankan sedekat mungkin dengan dokumen sumber untuk menjaga traceability lintas artefak.
- Bila ada konflik antar dokumen, prioritaskan source of truth, artefak yang paling preskriptif, dan baseline yang paling implementable.

## Source Mapping
- Original source: `Store_POS_ERD_Specification_v2.pdf` (PDF, 14 pages)
- Output markdown: `store_pos_erd_specification_v2.md`
- Conversion approach: text extraction -> normalization -> structural cleanup -> standardized documentation wrapper.
- Note: beberapa tabel/list di PDF dapat mengalami wrapping antar baris; esensi dipertahankan, tetapi layout tabel asli tidak dipertahankan 1:1.
