# Traceability Matrix Store POS

## Document Overview
Matrix QA yang memetakan jejak end-to-end dari use case hingga domain, architecture, database, implementation baseline, dan test baseline.

## Purpose
Menjamin tidak ada orphan design/test area dan memberi landasan impact analysis.

## Scope
Phase 1 Retail POS sebagai matrix utama, dengan boundary F&B; dan Service tetap diakui pada level arsitektur/ERD tetapi tidak dijadikan fokus operasional utama. Keputusan yang dipakai Scope inti + appendix boundary, 1 row per use case plus critical alternate/exception flow, du...

## Key Decisions / Core Rules
Menggunakan dual-layer trace: matrix utama per use case dan matrix kritikal untuk sub-flow risiko tinggi; implementation/test columns diperlakukan sebagai baseline kerja, belum artefak final.

## Detailed Content

### Normalized Source Body
8 Maret 2026
Baseline QA yang traceable ke artifact source project
Peran dokumen
QA Engineering perspective - dokumen ini memetakan jejak end-to-end dari use case hingga baseline implementasi dan baseline uji.
Scope
Phase 1 Retail POS sebagai matrix utama, dengan boundary F&B; dan Service tetap diakui pada level arsitektur/ERD tetapi tidak dijadikan fokus operasional utama.
Keputusan yang dipakai
Scope inti + appendix boundary, 1 row per use case plus critical alternate/exception flow, dual trace target-state + migration gap, implementation trace sampai
bounded context/application/data/DB, offline/sync sebagai dimensi eksplisit, dan dual identifier yang stabil.
Status
Siap dipakai untuk design review, handoff ke implementation specification berikutnya, dan penyusunan test case detail.
use case
activity trace
sequence trace
source artifacts inti
critical flow
Catatan penting: matrix ini sengaja jujur membedakan dua hal. Pertama, jejak desain end-to-end sudah kuat dan lengkap. Kedua, kolom implementasi serta baseline uji masih berupa turunan yang
siap dipakai sebagai baseline kerja berikutnya, karena artifact Implementation Specification dan Test Specification formal belum tersedia di project saat dokumen ini disusun.

8 Maret 2026
## 1. Tujuan, metode, dan aturan baca
- Dokumen ini menyusun traceability matrix dua arah: dari use case ke activity, sequence, domain, architecture, database, implementation baseline, dan baseline uji; lalu dicek balik agar tidak ada
area desain atau uji yang menjadi orphan.
- Primary use case dipakai sebagai tulang punggung matrix. Supporting/internal use case tetap dicatat, tetapi ditandai sebagai supporting trace agar tidak mengganggu fokus business goal.
- Karena pengguna memilih 1 row utama per use case plus critical alternate/exception flow, dokumen ini dibagi menjadi dua lapisan: matrix utama 39 use case dan matrix kritikal 17 sub-flow risiko
tinggi.
- Kolom implementasi dan baseline uji diturunkan dari artefak desain yang tersedia; jadi isi kolom ini adalah baseline kerja yang sangat praktis, tetapi belum mewakili artefak implementation spec
atau test case spec final.
Legenda kode uji dan referensi eksternal yang divalidasi
Kode
Makna
HP
Happy path
ALT
Alternate path / variasi alur
EXC
Exception / failure path
AUTH
Approval / authorization / role guard
OFF
Offline / sync / retry / conflict
DATA
Persistensi, integritas, idempotensi, dan audit trail
Kode
Sumber
Peran pada matrix
E1
Kotlin Multiplatform official documentation
Validasi pola share business logic sambil tetap mempertahankan U
native.
E2
SQLite official foreign key documentation
Validasi enforcement foreign key per koneksi aplikasi dan constrain
perilaku transaksi.
E3
RFC 9562 (UUIDs)
Validasi basis standar untuk UUIDv7 sebagai opaque primary key
time-ordered.
E4
IBM / Jama requirements traceability references
Validasi prinsip bidirectional traceability dan impact analysis pada
RTM.
Status trace dokumen: Lengkap pada level desain; turunan pada level implementasi/test. Artinya seluruh use case sudah memiliki jejak ke activity, sequence, domain, architecture, dan database,
namun belum memiliki referensi ke dokumen implementation spec dan test execution formal yang berdiri sendiri.
Register sumber artefak internal
Kode
Sumber
Peran pada matrix
S1
UML-Modeling-Source-of-Truth.txt
Aturan SDLC wajib, prinsip traceability, dan aturan turunan test.
S2
store_pos_use_case_detail_specifications.pdf
Katalog 39 use case, tujuan bisnis, main/alternate/exception flow, dan business rules.
S3
store_pos_activity_detail_specifications.pdf
Turunan activity untuk UC-01 s.d. UC-39, termasuk decision point, recovery path, dan partition.
S4
store_pos_sequence_detail_specifications.pdf
Turunan sequence untuk UC-01 s.d. UC-39, termasuk interaction order, retry, partial failure, dan integration touchpoint.

8 Maret 2026
Kode
Sumber
Peran pada matrix
S5
store_pos_domain_model_detail_specifications_v2.pdf
Bounded context, aggregate root, shared kernel, dan matriks use case ke domain.
S6
Cassy_Architecture_Specification_v1.pdf
Baseline arsitektur preskriptif: KMP hybrid, local-first SQLite, outbox sync, module map, dan implementation mapping.
S7
Store_POS_ERD_Specification_v2.pdf
Target-state ERD v2, entity utama per domain, gap schema lama, dan aturan integritas inti.

8 Maret 2026
## 2. Traceability Matrix Utama
Masing-masing tabel di bawah ini menyambungkan use case ke artefak turunan, bounded context/arsitektur, entity database, implementation baseline, dan baseline uji wajib. Kolom Ref
menunjukkan sumber artefak yang menjadi basis pemetaan row tersebut.
A. Front Office, Checkout, Return, dan Approval
UC
Artefak Turunan
Domain & Arsitektur
Database / ERD
Implementasi Baseline
Baseline Uji
Ref
UC-01
Mulai Shift Kasir
Primary
ACT-01, SEQ-01
S2/S3/S4 terhubung
oleh ID use case
yang sama.
Shift & Cash Control
Aggregate: CashierShift, OpeningCash
UI Shift POS; shared/cash; auth + identity
integration
business_day, cashier_session,
opening_cash, pos_terminal, audit_log
Open-shift use case -> validasi role -> cek konflik shift -> tulis
shift + opening cash + audit + outbox bila perlu.
AUTH, OFF, DATA
S1-S7
UC-02
Proses
Penjualan
Primary
ACT-02, SEQ-02
S2/S3/S4 terhubung
oleh ID use case
yang sama.
Sales & Checkout
Aggregate: Sale
UI Sales POS; shared/sales; payment/printer
adapter
sale_transaction, sale_item, payment, receipt,
outbox_event
Checkout facade menyimpan transaksi lokal dulu, lalu
finalisasi settlement, receipt, audit, dan event sync.
OFF, DATA
S1-S7
UC-03
Kelola
Keranjang
Supporting
ACT-03, SEQ-03
S2/S3/S4 terhubung
oleh ID use case
yang sama.
Sales & Checkout
BasketTotals, ProductSnapshot
UI Sales POS; shared/sales
sale_item (draft), suspended_sale snapshot
bila diparkir
Operasi add/update/remove item tetap lewat application layer;
pricing dihitung ulang setelah perubahan item.
DATA
S1-S6
UC-04
Terapkan
Member /
Voucher
Primary
ACT-04, SEQ-04
S2/S3/S4 terhubung
oleh ID use case
yang sama.
Sales & Checkout
AppliedBenefit
UI Sales POS; shared/sales; loyalty integration
sale_transaction, price_policy,
voucher/member snapshot opsional, audit_log
Application service memvalidasi eligibilitas benefit,
menyimpan snapshot benefit, lalu hitung ulang total.
OFF, DATA
S2-S7
UC-05
Proses
Pembayaran
Primary
ACT-05, SEQ-05
S2/S3/S4 terhubung
oleh ID use case
yang sama.
Sales & Checkout
Payment
UI Sales POS; shared/sales;
integrations/payment
payment, payment_allocation,
sale_transaction, receipt, outbox_event
Payment flow harus menghasilkan state
success/pending/failed yang sah sebelum sale boleh selesai.
OFF, DATA
S2-S7
UC-06
Terbitkan
Receipt
Supporting
ACT-06, SEQ-06
S2/S3/S4 terhubung
oleh ID use case
yang sama.
Sales & Checkout
Receipt
UI Receipt; shared/sales; printer adapter
receipt, sale_transaction, file_asset opsional
Receipt dibuat dari transaksi final dan wajib konsisten dengan
settlement state, print/digital channel, dan reprint policy.
DATA
S2-S7
UC-07
Suspend /
Resume
Transaksi
Primary
ACT-07, SEQ-07
S2/S3/S4 terhubung
oleh ID use case
yang sama.
Sales + Shared Kernel
SuspendedSaleSnapshot
UI Sales POS; shared/sales; kernel approval
bila handover
suspended_sale, sale_item snapshot,
audit_log
Suspend menyimpan snapshot keranjang; resume wajib
cegah duplicate resume dan reprice bila policy mewajibkan.
AUTH, DATA
S2-S7
UC-08
Cari / Scan
Produk
Supporting
ACT-08, SEQ-08
S2/S3/S4 terhubung
oleh ID use case
yang sama.
Sales & Checkout
Product lookup
UI Sales POS / Inventory; shared/masterdata +
sales
product snapshot / master data cache
Lookup memakai katalog lokal/cached yang valid untuk
mendukung operasi online-first dengan fallback offline
terkontrol.
OFF
S2-S6
UC-09
Hitung Harga,
Promo, Pajak
Supporting
ACT-09, SEQ-09
S2/S3/S4 terhubung
oleh ID use case
yang sama.
Sales & Checkout
PricingDecision
shared/sales application; masterdata + pricing
policy
sale_transaction, price_policy, tax context
Pricing engine menjadi satu jalur resmi perhitungan sebelum
payment dimulai; UI tidak boleh menghitung sendiri.
DATA
S1-S6

8 Maret 2026
UC
Artefak Turunan
Domain & Arsitektur
Database / ERD
Implementasi Baseline
Baseline Uji
Ref
UC-10
Void Item /
Transaksi
Primary
ACT-10, SEQ-10
S2/S3/S4 terhubung
oleh ID use case
yang sama.
Sales + Shared Kernel
ApprovalDecision, ExceptionReasonRecord
UI Sales POS; shared/sales + kernel
approval/audit
sale_transaction, sale_item, approval_request,
reason_code, audit_log
Void wajib reason-based, bisa memerlukan approval, dan
harus meninggalkan audit trail yang tidak ambigu.
AUTH, DATA
S2-S7
UC-11
Return / Refund
Primary
ACT-11, SEQ-11
S2/S3/S4 terhubung
oleh ID use case
yang sama.
Return & Refund
Aggregate: ReturnTransaction
UI Sales/Approval; shared/returns; payment
integration
return_transaction, return_line,
return_policy_decision, refund_record,
store_credit_*
Return memisahkan intent, policy, disposition stok, dan
settlement refund/store credit; bukan refund administratif
biasa.
AUTH, DATA
S2-S7
UC-12
Price Override /
Manual
Discount
Primary
ACT-12, SEQ-12
S2/S3/S4 terhubung
oleh ID use case
yang sama.
Sales + Shared Kernel
ApprovalDecision, ExceptionReasonRecord
UI Sales POS; shared/sales + kernel approval
sale_transaction, approval_request,
reason_code, audit_log
Override hanya lewat pricing path resmi; threshold
menentukan perlu/tidaknya approval supervisor.
AUTH, DATA
S2-S7
UC-13
Lookup Receipt
Supporting
ACT-13, SEQ-13
S2/S3/S4 terhubung
oleh ID use case
yang sama.
Return & Refund
OriginalSaleReference
UI Sales POS; shared/returns + shared/sales
query facade
receipt, sale_transaction, return_transaction
snapshot
Lookup receipt menjadi pintu masuk validasi return dan harus
menjaga jejak transaksi asli yang dicari.
DATA
S2-S7
UC-14
Validasi Return
Policy
Supporting
ACT-14, SEQ-14
S2/S3/S4 terhubung
oleh ID use case
yang sama.
Return & Refund
ReturnPolicyDecision
shared/returns application; policy facade
return_policy_decision, reason_code,
approval_request opsional
Policy validation dieksekusi sebelum refund; hasilnya harus
tersimpan sebagai snapshot keputusan yang dapat diaudit.
AUTH, DATA
S2-S7
UC-15
Supervisor
Approval
Supporting
ACT-15, SEQ-15
S2/S3/S4 terhubung
oleh ID use case
yang sama.
Shared Kernel
ApprovalDecision
feature-approval-ui; shared/kernel + auth
approval_request, audit_log, employee/pin
state
Approval merekam siapa, kapan, untuk apa, dan hasil
keputusannya; mendukung PIN lokal/offline sesuai policy.
AUTH, OFF, DATA
S2-S7
UC-16
Catat Alasan
Exception
Supporting
ACT-16, SEQ-16
S2/S3/S4 terhubung
oleh ID use case
yang sama.
Shared Kernel
ExceptionReasonRecord
cross-cutting kernel component
reason_code, audit_log, file_asset opsional
Reason code wajib untuk flow sensitif: void, return, override,
receiving gap, adjustment, dan exception operasional lain.
DATA
S2-S7
B. Shift, Cash Control, dan End-of-Day
UC
Artefak Turunan
Domain & Arsitektur
Database / ERD
Implementasi Baseline
Baseline Uji
Ref
UC-17
Input Kas Awal
Supporting
ACT-17, SEQ-17
S2/S3/S4 terhubung
oleh ID use case
yang sama.
Shift & Cash Control
OpeningCash
UI Shift POS; shared/cash
opening_cash, cashier_session, audit_log
Opening cash diinput di awal shift dan menjadi bagian dari
transaksi pembukaan shift, bukan catatan terpisah yang
longgar.
AUTH, DATA
S2-S7
UC-18
Cash In / Cash
Out
Primary
ACT-18, SEQ-18
S2/S3/S4 terhubung
oleh ID use case
yang sama.
Shift & Cash Control
CashMovement
UI Shift POS; shared/cash
cash_movement, cashier_session,
business_day, reason_code, audit_log
Movement non-sale harus punya tipe semantik, alasan, actor,
dan approval bila threshold policy terlampaui.
AUTH, DATA
S2-S7
UC-19
Safe Drop
Primary
ACT-19, SEQ-19
S2/S3/S4 terhubung
oleh ID use case
yang sama.
Shift & Cash Control
SafeDrop
UI Shift POS; shared/cash
safe_drop, cash_session, cashier_session,
audit_log
Safe drop diperlakukan sebagai entity sendiri dengan actor,
witness/approver, bag/reference, dan state yang jelas.
AUTH, DATA
S2-S7

8 Maret 2026
UC
Artefak Turunan
Domain & Arsitektur
Database / ERD
Implementasi Baseline
Baseline Uji
Ref
UC-20
Tutup Shift
Primary
ACT-20, SEQ-20
S2/S3/S4 terhubung
oleh ID use case
yang sama.
Shift & Cash Control
CashierShift, ShiftReport
UI Shift POS; shared/cash + reporting + printer
cashier_session, cash_reconciliation,
shift_report, audit_log
Close shift wajib mengikat expected cash, counted cash,
reconciliation, report generation, dan audit dalam alur yang
konsisten.
AUTH, DATA
S2-S7
UC-21
Rekonsiliasi
Kas
Supporting
ACT-21, SEQ-21
S2/S3/S4 terhubung
oleh ID use case
yang sama.
Shift & Cash Control
CashReconciliation
UI Shift POS / desktop reconcile; shared/cash
cash_reconciliation, cash_movement,
safe_drop, payment summary
Expected vs counted harus punya variance, decision,
approver, dan notes; selisih tidak boleh hilang sebagai angka
tanpa keputusan.
AUTH, DATA
S2-S7
UC-22
Generate X / Z
Report
Supporting
ACT-22, SEQ-22
S2/S3/S4 terhubung
oleh ID use case
yang sama.
Shift & Cash Control
ShiftReport
UI Shift POS / desktop reporting;
shared/reporting
shift_report, store_operational_report,
file_asset opsional
Report generation menarik snapshot totals yang tervalidasi;
tidak dibangun dari counter UI ad hoc.
DATA
S2-S7
UC-23
Tutup Hari
Primary
ACT-23, SEQ-23
S2/S3/S4 terhubung
oleh ID use case
yang sama.
Shift & Cash Control + Reporting
BusinessDay
desktop-backoffice reporting; shared/cash +
reporting + sync
business_day, store_operational_report,
cashier_session, audit_log
End-of-day memverifikasi kesiapan store-level, menghasilkan
laporan final, lalu menutup business day secara eksplisit.
AUTH, OFF, DATA
S2-S7
C. Inventory Operations
UC
Artefak Turunan
Domain & Arsitektur
Database / ERD
Implementasi Baseline
Baseline Uji
Ref
UC-24
Cek Stok &
Movement
Primary
ACT-24, SEQ-24
S2/S3/S4 terhubung
oleh ID use case
yang sama.
Inventory
InventoryBalance, StockLedgerEntry
feature-inventory-ui; shared/inventory
inventory_balance, stock_ledger_entry,
inventory_location, inventory_bucket
View stok harus memisahkan current state vs historical
explanation dan mendukung filter item/lokasi/bucket.
DATA
S2-S7
UC-25
Receiving
Barang
Primary
ACT-25, SEQ-25
S2/S3/S4 terhubung
oleh ID use case
yang sama.
Inventory
ReceivingDocument
feature-inventory-ui; shared/inventory; HQ
integration
receiving_document, receiving_line,
receiving_discrepancy, inventory_balance,
stock_ledger_entry
Receiving membandingkan expected vs actual, lalu menulis
balance + ledger + discrepancy secara traceable.
OFF, DATA
S2-S7
UC-26
Transfer Stok
Primary
ACT-26, SEQ-26
S2/S3/S4 terhubung
oleh ID use case
yang sama.
Inventory
StockTransfer
feature-inventory-ui; shared/inventory
stock_transfer, stock_transfer_line,
inventory_balance, stock_ledger_entry
Transfer menyimpan dokumen, status proses, dan perubahan
stok sesuai state transfer; tidak boleh update stok tanpa
source document.
DATA
S2-S7
UC-27
Replenishment
Rak
Primary
ACT-27, SEQ-27
S2/S3/S4 terhubung
oleh ID use case
yang sama.
Inventory
ReplenishmentTask
feature-inventory-ui; shared/inventory
replenishment_task, replenishment_line,
inventory_balance, stock_ledger_entry
Replenishment memindahkan stok antar lokasi internal tanpa
mengubah total on-hand toko.
DATA
S2-S7
UC-28
Stock
Adjustment
Primary
ACT-28, SEQ-28
S2/S3/S4 terhubung
oleh ID use case
yang sama.
Inventory + Shared Kernel
StockAdjustment
feature-inventory-ui; shared/inventory + kernel
approval
stock_adjustment, stock_adjustment_line,
reason_code, approval_request,
stock_ledger_entry, audit_log
Adjustment harus reason-based, approval-aware, dan menulis
balance + ledger + audit secara atomik.
AUTH, DATA
S2-S7
UC-29
Stock Opname /
Cycle Count
Primary
ACT-29, SEQ-29
S2/S3/S4 terhubung
oleh ID use case
yang sama.
Inventory
CycleCount
feature-inventory-ui; shared/inventory
cycle_count, cycle_count_line,
stock_adjustment opsional, audit_log
Cycle count menyimpan snapshot area/item, counted qty,
variance, dan keputusan resolusi; bukan hanya overwrite stok.
AUTH, DATA
S2-S7

8 Maret 2026
UC
Artefak Turunan
Domain & Arsitektur
Database / ERD
Implementasi Baseline
Baseline Uji
Ref
UC-30
Kelola Barang
Rusak / Expired
Primary
ACT-30, SEQ-30
S2/S3/S4 terhubung
oleh ID use case
yang sama.
Inventory
DamageDisposition
feature-inventory-ui; shared/inventory
damage_disposition, damage_disposition_line,
inventory_balance, stock_ledger_entry
Barang rusak/expired harus punya disposition outcome yang
eksplisit: salvage, return to vendor, destroy, atau write-off.
AUTH, DATA
S2-S7
UC-31
Verifikasi
Delivery / PO
Supporting
ACT-31, SEQ-31
S2/S3/S4 terhubung
oleh ID use case
yang sama.
Inventory
Receiving pre-check
feature-inventory-ui; shared/inventory; HQ
integration
receiving_document reference, source
delivery/PO metadata
Verifikasi referensi delivery/PO adalah gate sebelum
receiving; jika referensi tidak valid maka receiving tidak boleh
lanjut.
OFF, DATA
S2-S7
UC-32
Catat Selisih
Receiving
Supporting
ACT-32, SEQ-32
S2/S3/S4 terhubung
oleh ID use case
yang sama.
Inventory + Shared Kernel
ReceivingDiscrepancy
feature-inventory-ui; shared/inventory + kernel
reason/audit
receiving_discrepancy, reason_code,
approval_request opsional, audit_log
Receiving gap wajib tercatat dengan alasan dan keputusan
handling; selisih tidak boleh hilang diam-diam.
AUTH, DATA
S2-S7
UC-33
Cetak Label
Harga / Rak
Primary
ACT-33, SEQ-33
S2/S3/S4 terhubung
oleh ID use case
yang sama.
Inventory
PriceLabelJob
feature-inventory-ui; shared/inventory; printer
adapter
price_label_job, price_label_line,
product/pricing snapshot
Pencetakan label mengambil snapshot harga yang sah agar
hasil print dapat ditelusuri ke konteks pricing saat itu.
DATA
S2-S7
D. Reporting, Sync, Offline, Auth, dan Audit
UC
Artefak Turunan
Domain & Arsitektur
Database / ERD
Implementasi Baseline
Baseline Uji
Ref
UC-34
Lihat Laporan
Operasional
Toko
Primary
ACT-34, SEQ-34
S2/S3/S4 terhubung
oleh ID use case
yang sama.
Reporting & Sync
StoreOperationalReport
desktop-backoffice reporting; shared/reporting
store_operational_report,
report_snapshot_line, business_day
Laporan operasional dibangun dari event dan state yang
tervalidasi, bukan dari counter bebas atau query lintas context
yang tidak dikendalikan.
DATA
S2-S7
UC-35
Sinkronisasi
Data Toko
Primary
ACT-35, SEQ-35
S2/S3/S4 terhubung
oleh ID use case
yang sama.
Reporting & Sync
SyncBatch, SyncItem
feature-sync-ui; shared/sync; integrations/hqapi
outbox_event, sync_batch, sync_item,
master_data_snapshot,
offline_operation_window
Sync menyiapkan batch, kirim/ambil data, tandai sukses, dan
membuat daftar reconcile untuk partial failure; tidak boleh
silent drop.
OFF, DATA
S2-S7
UC-36
Mode Operasi
Offline
Primary
ACT-36, SEQ-36
S2/S3/S4 terhubung
oleh ID use case
yang sama.
Reporting & Sync
OfflineOperationWindow
app-shell + shared/sync + auth + sales/cash
offline_operation_window, outbox_event, sync
status marker
Offline mode adalah controlled degradation: hanya fungsi
aman yang boleh jalan, semua mutasi diberi penanda pending
sync.
AUTH, OFF, DATA
S1-S7
UC-37
Rekonsiliasi
Sync Gagal
Supporting
ACT-37, SEQ-37
S2/S3/S4 terhubung
oleh ID use case
yang sama.
Reporting & Sync
SyncConflict
feature-sync-ui; desktop reconcile; shared/sync
sync_conflict, sync_item, sync_batch,
outbox_event
User harus bisa retry, resolve conflict, atau mark manual
investigation; sync failure wajib punya status akhir yang
terlihat.
OFF, DATA
S2-S7
UC-38
Autentikasi &
Validasi Role
Supporting
ACT-38, SEQ-38
S2/S3/S4 terhubung
oleh ID use case
yang sama.
Shared Kernel / Auth
Session & role guard
app-shell + shared/auth + integrations/identity
employee, role/pin state, audit_log opsional
Login online-first mendukung cache grant/offline fallback
sesuai policy; role guard tetap dievaluasi di application
boundary.
AUTH, OFF
S2-S7
UC-39
Catat Audit Log
Supporting
ACT-39, SEQ-39
S2/S3/S4 terhubung
oleh ID use case
yang sama.
Shared Kernel
AuditLog
cross-cutting kernel + data adapter
audit_log, outbox_event opsional
Audit adalah append-only dan harus ikut dalam keputusan
bisnis lokal; bila store pusat unavailable, event dapat
diantrikan dengan aman.
OFF, DATA
S1-S7

8 Maret 2026

8 Maret 2026
## 3. Matrix Critical Alternate / Exception Flow
Bagian ini menangkap sub-flow yang paling mahal secara QA: approval, decline, pending state, offline degradation, sync conflict, discrepancy inventory, dan audit fallback. Ini adalah tempat yang
paling sering menjadi sumber defect laten walaupun happy path tampak benar.
ID
UC
Skenario kritikal
Dampak data / implementasi
Test wajib
Catatan risiko
CF-01
UC-01
S2,S3,S4
Autentikasi gagal / shift konflik / opening cash di luar
policy
cashier_session, opening_cash, approval_request, audit_log
EXC, AUTH, OFF,
DATA
Tanpa ini, shift bisa terbuka tanpa otorisasi atau dengan
state kas awal yang tidak sah.
CF-02
UC-05
S2,S4,S6
Authorization declined / timeout / status payment
pending investigation
payment, payment_allocation, sale_transaction, outbox_event
EXC, OFF, DATA
Mencegah sale dianggap final saat payment belum valid.
CF-03
UC-07
S2,S4,S5
Duplicate resume atau snapshot transaksi
rusak/kedaluwarsa
suspended_sale, audit_log
EXC, DATA
Mencegah dua terminal menghidupkan transaksi yang
sama.
CF-04
UC-10
S2,S5,S7
Void setelah payment / perlu approval / reason code
wajib
approval_request, reason_code, audit_log, sale_transaction
AUTH, EXC, DATA
Flow sensitif finansial harus reason-based dan auditable.
CF-05
UC-11
S2,S5,S7
Receipt tidak ditemukan / policy lewat / refund kartu
gagal -> fallback store credit
return_transaction, return_policy_decision, refund_record,
store_credit_*
ALT, EXC, AUTH,
DATA
Return di project ini bukan refund administratif sederhana.
CF-06
UC-12
S2,S5,S7
Discount melewati threshold / item tidak eligible /
approval ditolak
approval_request, reason_code, sale_transaction, audit_log
AUTH, EXC, DATA
Menjaga pricing rule tetap satu jalur resmi.
CF-07
UC-18
S2,S5,S7
Cash movement melewati limit / butuh approval
cash_movement, reason_code, approval_request, audit_log
AUTH, EXC, DATA
Mengurangi celah shrinkage dan manipulasi kas.
CF-08
UC-20
S2,S4,S7
Selisih kas melebihi tolerance / transaksi pending
menolak close shift
cash_reconciliation, shift_report, cashier_session, audit_log
EXC, AUTH, DATA
Close shift tidak boleh menutup masalah operasional
secara diam-diam.
CF-09
UC-23
S2,S5,S6,S7
Masih ada shift aktif / data kritikal belum lengkap /
end-of-day offline
business_day, store_operational_report, cashier_session,
outbox_event
ALT, EXC, OFF,
DATA
Business day harus tunggal dan tertutup dengan kesiapan
minimum yang jelas.
CF-10
UC-25
S2,S5,S7
Receiving parsial / referensi delivery tidak valid / HQ
reference belum ada di lokal
receiving_document, receiving_line, receiving_discrepancy,
stock_ledger_entry
ALT, EXC, OFF,
DATA
Receiving adalah sumber mutasi stok dan gap harus
terdokumentasi.
CF-11
UC-28
S2,S5,S7
Approval ditolak / stock negatif tidak diizinkan / item
diblokir
stock_adjustment*, approval_request, reason_code,
stock_ledger_entry
AUTH, EXC, DATA
Adjustment adalah titik rawan penyalahgunaan inventori.
CF-12
UC-29
S2,S5,S7
Double count untuk item bernilai tinggi / auto-adjust
ditolak
cycle_count*, stock_adjustment*, audit_log
ALT, EXC, AUTH,
DATA
Variance besar perlu jejak resolusi, bukan overwrite saldo.
CF-13
UC-35
S2,S4,S6,S7
Batch gagal parsial / retry / conflict local vs HQ
sync_batch, sync_item, sync_conflict, outbox_event
ALT, EXC, OFF,
DATA
Sync adalah subsistem bisnis yang terlihat, bukan queue
latar belakang yang sunyi.
CF-14
UC-36
S2,S3,S6,S7
Cache tidak memadai / time drift berisiko conflict /
fitur berisiko diblokir
offline_operation_window, outbox_event, sync markers
EXC, AUTH, OFF,
DATA
Offline mode bersifat terbatas dan harus bisa direkonsiliasi.
CF-15
UC-37
S2,S4,S6,S7
Conflict tidak dapat di-resolve otomatis
sync_conflict, sync_item, batch status
EXC, OFF, DATA
Tidak boleh ada sync failure yang hilang tanpa status akhir.
CF-16
UC-38
S2,S4,S6
Identity service tidak tersedia dan fallback tidak
diizinkan
employee role/pin state, session cache, audit_log
EXC, AUTH, OFF
Menjaga role guard tetap benar saat konektivitas buruk.

8 Maret 2026
ID
UC
Skenario kritikal
Dampak data / implementasi
Test wajib
Catatan risiko
CF-17
UC-39
S2,S4,S6,S7
Storage audit unavailable -> event diantrikan aman
audit_log, outbox_event
EXC, OFF, DATA
Aksi sensitif tidak boleh lolos tanpa jejak audit yang
terlindungi.

8 Maret 2026
## 4. Observasi QA dan rekomendasi tindak lanjut
Kekuatan utama
- Rantai artefak inti sudah lengkap dari Use Case -> Activity -> Sequence -> Domain Model -> Architecture -> Database, sehingga basis matrix kuat untuk design review dan handoff engineering.
- Bounded context serta aggregate penting sudah jelas: Sales, Returns, Cash, Inventory, Reporting, Sync, dan Shared Kernel.
- Arsitektur sudah tegas pada local-first, outbox + batch/item/conflict, serta single writer per terminal; ini membuat trace ke offline/sync dapat dimodelkan eksplisit, bukan catatan samping.
Gap yang harus jujur dicatat
- Artifact implementation specification formal belum tersedia. Karena itu kolom implementasi pada matrix ini adalah implementation baseline hasil turunan dari architecture + ERD, bukan mapping ke
interface/class yang sudah final.
- Artifact test specification formal belum tersedia. Karena itu kolom baseline uji memetakan keluarga skenario uji wajib, belum sampai nomor test case final atau status eksekusi QA.
- Use case internal/supporting seperti auth dan audit tetap dicatat, tetapi diposisikan sebagai supporting trace agar tidak mengaburkan business goal use case utama.
Implikasi QA
- Dokumen ini cocok dipakai sebagai baseline review coverage, penurunan implementation spec berikutnya, dan penyusunan test case detail.
- Dokumen ini belum bisa dipakai sebagai bukti pass/fail testing karena belum ada artefak test execution resmi.
- Prioritas tindak lanjut setelah dokumen ini: Implementation Specification, Test Case Specification, dan mapping test execution per UC kritikal.
Rekomendasi urutan artefak berikutnya
- Susun Implementation Specification per bounded context dengan kontrak antarmuka, transaction boundary, dependency rule, dan migration plan yang merujuk row matrix ini.
- Turunkan Test Case Specification dari kolom baseline uji, dimulai dari UC/critical flow berisiko tinggi: payment, return/refund, close shift, end-of-day, receiving discrepancy, stock adjustment, offline
mode, dan sync reconciliation.
- Tambahkan trace status operasional setelah implementation berjalan: Implemented / Partially Implemented / Not Implemented dan Test Planned / In Progress / Passed / Failed.
Kesimpulan QA: dari sisi desain, project ini sudah cukup matang untuk membangun RTM yang serius dan dapat dipakai untuk review engineering. Risiko utamanya bukan kekurangan artefak
desain, tetapi belum adanya artefak implementation spec dan test spec formal yang akan mengubah matrix ini dari baseline perencanaan menjadi alat kontrol delivery harian.


## Constraints / Policies
Ref source dan identifier UC/ACT/SEQ harus tetap stabil agar trace tetap dua arah.

## Technical Notes
Matrix ini berperan sebagai tulang punggung QA planning dan coverage prioritization.

## Dependencies / Related Documents
- `uml_modeling_source_of_truth.md`
- `store_pos_use_case_detail_specifications.md`
- `store_pos_activity_detail_specifications.md`
- `store_pos_sequence_detail_specifications.md`
- `store_pos_domain_model_detail_specifications_v2.md`
- `cassy_architecture_specification_v1.md`
- `store_pos_erd_specification_v2.md`
- `store_pos_test_specification.md`
- `cassy_test_automation_specification.md`

## Risks / Gaps / Ambiguities
- Tidak ditemukan gap fatal saat ekstraksi. Tetap review ulang bagian tabel/angka jika dokumen ini akan dijadikan baseline implementasi final.

## Reviewer Notes
- Struktur telah dinormalisasi ke Markdown yang konsisten dan siap dipakai untuk design review / engineering handoff.
- Istilah utama dipertahankan sedekat mungkin dengan dokumen sumber untuk menjaga traceability lintas artefak.
- Bila ada konflik antar dokumen, prioritaskan source of truth, artefak yang paling preskriptif, dan baseline yang paling implementable.

## Source Mapping
- Original source: `traceability_matrix_store_pos.pdf` (PDF, 11 pages)
- Output markdown: `traceability_matrix_store_pos.md`
- Conversion approach: text extraction -> normalization -> structural cleanup -> standardized documentation wrapper.
- Note: beberapa tabel/list di PDF dapat mengalami wrapping antar baris; esensi dipertahankan, tetapi layout tabel asli tidak dipertahankan 1:1.
