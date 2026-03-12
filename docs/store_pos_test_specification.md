# Store POS Test Specification

## Document Overview
QA engineering baseline yang menurunkan strategi test, coverage, critical flow, evidence, dan data verification dari artefak desain.

## Purpose
Mulai shift gagal ketika autentikasi gagal, shift konflik, atau opening cash di luar policy Prioritas P1 / Wave-1 Trace refs ACT-01, SEQ-01 · S2,S3,S4 · S1-S7 Layer Domain/App + Data + Integration + UI selektif Automation target Unit/Component + Integration + Manual/UAT Platfo...

## Scope
Phase 1 Retail POS sebagai fokus operasional utama, dengan boundary F&B; dan Service tetap diakui pada level arsitektur/ERD. Keputusan final Per use case + sub-skenario kritikal; layered testing; risk-first; offline/sync eksplisit; verifikasi data lintas layer; Android POS man...

## Key Decisions / Core Rules
Risk-first; output UI bukan satu-satunya target verifikasi; state domain, persistence, audit, outbox, retry, dan reconciliation harus diuji.

## Detailed Content

### Normalized Source Body
8 Maret 2026
Dokumen QA engineering yang menurunkan strategi, coverage, dan test case detail dari artifact source project secara end-to-end.
Peran dokumen
Baseline Test Specification formal untuk design review, implementation handoff, dan persiapan automation / test execution.
Scope
Phase 1 Retail POS sebagai fokus operasional utama, dengan boundary F&B; dan Service tetap diakui pada level arsitektur/ERD.
Keputusan final
Per use case + sub-skenario kritikal; layered testing; risk-first; offline/sync eksplisit; verifikasi data lintas layer; Android POS mandatory, Desktop Backoffice selektif, Android Mobile sangat selektif.
Status
Siap dipakai sebagai baseline penyusunan test case, automation mapping, dan gate CI sebelum implementation spec final tersedia.
use case
primary use case
supporting use case
critical flow
prioritas P1
Inti pendekatan QA
- Dokumen ini sengaja memposisikan test specification sebagai artefak formal pertama setelah Traceability Matrix. Artinya, fokus utamanya adalah mengubah jejak desain menjadi keluarga uji
yang bisa dijalankan, bukan hanya membuat checklist UI.
- Happy path tetap dicakup, tetapi wave-1 diarahkan ke 17 critical flow yang paling rawan menghasilkan defect mahal: payment, return/refund, shift close, end-of-day, receiving discrepancy, stock
adjustment, offline degradation, sync conflict, auth fallback, dan audit fallback.
- Setiap test case penting memverifikasi bukan hanya output UI, tetapi juga state domain, perubahan persistence, audit/outbox, serta readiness untuk retry/reconcile pada arsitektur local-first.

8 Maret 2026
## 1. Basis riset dan artifact source
Sumber di bawah ini dipakai sebagai evidence base utama. Dokumen ini sengaja menjaga traceability ke source project, bukan mendefinisikan perilaku baru yang tidak didukung artefak.
Kode
Artefak sumber
Peran pada penyusunan test
SRC-01
UML-Modeling-Source-of-Truth.txt
Urutan SDLC wajib dan aturan bahwa test harus diturunkan dari artefak sebelumnya.
SRC-02
store_pos_use_case_detail_specifications.pdf
Tujuan bisnis, precondition, main flow, alternate flow, exception flow, dan business rule per use case.
SRC-03
store_pos_activity_detail_specifications.pdf
Checkpoint keputusan, recovery path, partition, dan exit criteria yang paling berguna untuk turunan test.
SRC-04
store_pos_sequence_detail_specifications.pdf
Urutan interaksi lintas UI, application, service, repository, database, dan external integration.
SRC-05
store_pos_domain_model_detail_specifications_v2.pdf
Bounded context, aggregate root, invariant, supporting domain object, dan state penting.
SRC-06
Cassy_Architecture_Specification_v1.pdf
Aturan layer, local-first, outbox/sync, single writer, transaction bundle, platform scope, dan CI baseline.
SRC-07
Store_POS_ERD_Specification_v2.pdf
Entity target-state, integritas persistence, migration gap, idempotency, dan sync orchestration entities.
Referensi eksternal validasi teknis
Kode
Referensi
Peran validasi
EXT-01
Kotlin Multiplatform official docs
Validasi bahwa commonTest dan kotlin.test dapat dipakai untuk shared business logic lintas target.
EXT-02
Android Developers testing docs
Validasi pemisahan local test vs instrumented test serta alasan memakai device test hanya saat perlu environment Android.
EXT-03
SQLite foreign key docs
Validasi bahwa foreign key harus diaktifkan per koneksi dan menjadi bagian penting dari verifikasi DB test.
- Urutan penurunan yang dipakai tetap mengikuti source of truth: Use Case -> Activity -> Sequence -> Domain Model -> Architecture -> Database -> Implementation -> Test.
- Karena Implementation Specification formal belum tersedia, area implementation pada dokumen ini tetap diperlakukan sebagai implementation baseline yang diturunkan dari arsitektur dan ERD,
bukan mapping final ke class/interface runtime.
- Dokumen ini tidak mengklaim status pass/fail execution. Fokusnya adalah test intent, scope, level, dan evidence yang wajib disiapkan sebelum eksekusi QA resmi.
## 2. Strategi pengujian dan aturan desain test
Dimensi
Keputusan yang dipakai
Bentuk dokumen
Per use case sebagai tulang punggung, ditambah appendix detail untuk critical alternate/exception flow.
Granularity
Satu baseline per use case, dengan sub-skenario untuk happy path, alternate path, exception path, guard/approval, offline/sync, dan verifikasi data bila relevan.
Layer test
Domain/Application + Data sebagai basis utama; Integration dipakai saat ada boundary eksternal atau orchestration lintas modul; UI/instrumented dipakai selektif untuk flow yang benar-benar butuh
fidelity platform.
Prioritas eksekusi
Risk-first: 17 critical flow menjadi wave-1; coverage ke semua 39 use case tetap dipertahankan sebagai baseline wave-2/3.
Offline/sync
Bukan catatan tambahan. Offline/sync menjadi dimensi eksplisit pada semua test yang menyentuh transaksi lokal, auth fallback, outbox, retry, batch, conflict, dan reconciliation.

8 Maret 2026
Verifikasi hasil
Wajib memeriksa output UI/response, state domain, perubahan DB kunci, audit trail, dan outbox/sync state jika use case menyentuh transaksi persisten.
Aturan kualitas test case
- Jangan membuat test case yang hanya memeriksa label UI bila invariant bisnis dan perubahan persistence adalah titik risiko utamanya.
- Semua jalur write yang retriable harus diuji untuk idempotency; retry lokal maupun retry sync tidak boleh menduplikasi efek bisnis.
- Untuk kasus local-first, bundle transaksi wajib dicek secara atomik: perubahan utama, audit, dan outbox tidak boleh committed parsial.
- Role guard dan approval harus diuji sebagai decision point bisnis, bukan sekadar visibility rule di layar.
## 3. Scope platform, environment, dan evidence
Area
Mandatory / selektif
Catatan QA
Android POS
Mandatory
Writer utama phase-1 untuk checkout, shift, cash, inventory operasi toko, dan offline-mode. Menjadi fokus utama manual + automation.
Desktop Backoffice
Selektif tetapi penting
Diprioritaskan untuk reporting, reconciliation, close business-day, dan resolve sync conflict yang bersifat operasional/backoffice.
Android Mobile
Sangat selektif
Hanya untuk approval, inventory assist, atau skenario terbatas bila kemudian diimplementasikan. Bukan checkout terminal pengganti pada phase-1.
External integration
stub/sandbox
Mandatory untuk test
tertentu
Payment Gateway/EDC, Identity Service, Loyalty, dan HQ Store Service perlu stub yang dapat memicu success, declined, timeout, pending, dan conflict.
Evidence minimum per eksekusi
- Identifier test case, build/version, nama environment, dan timestamp eksekusi.
- Input data uji yang relevan: user/role, terminal, business day, payment stub scenario, connectivity mode, dan initial DB state.
- Bukti hasil: screenshot/rekaman layar bila perlu, log aplikasi, response integration, dan snapshot query untuk tabel kunci seperti sale_transaction, payment, audit_log, outbox_event,
sync_batch/sync_item/sync_conflict, cashier_session, inventory_balance, stock_ledger_entry.
- Untuk test failure/retry/offline, lampirkan state sebelum dan sesudah recovery agar jejak reconciliability dapat diverifikasi.

8 Maret 2026
## 4. Coverage matrix baseline per use case
Bagian ini adalah peta kerja utama QA. Setiap row menjaga hubungan antara UC, artefak turunan, prioritas, layer test, target automation, platform, dan verifikasi data kunci.
ID
Use case
Tipe / Prioritas
Trace
Level uji
Automation target
Platform utama
Fokus verifikasi data
UC-01
Mulai Shift Kasir
Primary
P1
Offline: Eksplisit
ACT-01, SEQ-01
S1-S7
Domain/App + Data + Integration
+ UI selektif
Unit/Component + Integration
+ Manual/UAT
Android POS
business_day, cashier_session, opening_cash, pos_terminal,
audit_log
HP, ALT, EXC, AUTH, OFF, DATA
UC-02
Proses Penjualan
Primary
P1
Offline: Eksplisit
ACT-02, SEQ-02
S1-S7
Domain/App + Data + Integration
+ UI selektif
Unit/Component + Integration
+ Manual/UAT
Android POS
sale_transaction, sale_item, payment, receipt, outbox_event
HP, ALT, EXC, OFF, DATA
UC-03
Kelola Keranjang
Supporting
P2
Offline: Bersyarat
ACT-03, SEQ-03
S1-S6
Domain/App + Data + UI selektif
Unit/Component + Integration
Android POS
sale_item (draft), suspended_sale snapshot bila diparkir
HP, ALT, EXC, DATA
UC-04
Terapkan Member / Voucher
Primary
P1
Offline: Eksplisit
ACT-04, SEQ-04
S2-S7
Domain/App + Data + Integration
+ UI selektif
Unit/Component + Integration
+ Manual/UAT
Android POS
sale_transaction, price_policy, voucher/member snapshot
opsional, audit_log
HP, ALT, EXC, OFF, DATA
UC-05
Proses Pembayaran
Primary
P1
Offline: Eksplisit
ACT-05, SEQ-05
S2-S7
Domain/App + Data + Integration
+ UI selektif
Unit/Component + Integration
+ Manual/UAT
Android POS
payment, payment_allocation, sale_transaction, receipt,
outbox_event
HP, ALT, EXC, OFF, DATA
UC-06
Terbitkan Receipt
Supporting
P2
Offline: Bersyarat
ACT-06, SEQ-06
S2-S7
Domain/App + Data + UI selektif
Unit/Component + Integration
Android POS
receipt, sale_transaction, file_asset opsional
HP, ALT, EXC, DATA
UC-07
Suspend / Resume Transaksi
Primary
P1
Offline: Bersyarat
ACT-07, SEQ-07
S2-S7
Domain/App + Data + Integration
+ UI selektif
Unit/Component + Integration
+ Manual/UAT
Android POS
suspended_sale, sale_item snapshot, audit_log
HP, ALT, EXC, AUTH, DATA
UC-08
Cari / Scan Produk
Supporting
P2
Offline: Eksplisit
ACT-08, SEQ-08
S2-S6
Domain/App + Data + Integration
+ UI selektif
Unit/Component + Integration
+ Manual/UAT
Android POS
product snapshot / master data cache
HP, ALT, EXC, OFF
UC-09
Hitung Harga, Promo, Pajak
Supporting
P2
Offline: Bersyarat
ACT-09, SEQ-09
S1-S6
Domain/App + Data + UI selektif
Unit/Component + Integration
Android POS
sale_transaction, price_policy, tax context
HP, ALT, EXC, DATA
UC-10
Void Item / Transaksi
Primary
P1
Offline: Bersyarat
ACT-10, SEQ-10
S2-S7
Domain/App + Data + Integration
+ UI selektif
Unit/Component + Integration
+ Manual/UAT
Android POS
sale_transaction, sale_item, approval_request, reason_code,
audit_log
HP, ALT, EXC, AUTH, DATA
UC-11
Return / Refund
Primary
P1
Offline: Bersyarat
ACT-11, SEQ-11
S2-S7
Domain/App + Data + Integration
+ UI selektif
Unit/Component + Integration
+ Manual/UAT
Android POS
return_transaction, return_line, return_policy_decision,
refund_record, store_credit_*
HP, ALT, EXC, AUTH, DATA
UC-12
Price Override / Manual Discount
Primary
P1
Offline: Bersyarat
ACT-12, SEQ-12
S2-S7
Domain/App + Data + Integration
+ UI selektif
Unit/Component + Integration
+ Manual/UAT
Android POS
sale_transaction, approval_request, reason_code, audit_log
HP, ALT, EXC, AUTH, DATA
UC-13
Lookup Receipt
Supporting
P2
Offline: Bersyarat
ACT-13, SEQ-13
S2-S7
Domain/App + Data + UI selektif
Unit/Component + Integration
Android POS
receipt, sale_transaction, return_transaction snapshot
HP, ALT, EXC, DATA

8 Maret 2026
ID
Use case
Tipe / Prioritas
Trace
Level uji
Automation target
Platform utama
Fokus verifikasi data
UC-14
Validasi Return Policy
Supporting
P2
Offline: Bersyarat
ACT-14, SEQ-14
S2-S7
Domain/App + Data + UI selektif
Unit/Component + Integration
+ Manual/UAT
Android POS
return_policy_decision, reason_code, approval_request
opsional
HP, ALT, EXC, AUTH, DATA
UC-15
Supervisor Approval
Supporting
P2
Offline: Eksplisit
ACT-15, SEQ-15
S2-S7
Domain/App + Data + UI selektif
Unit/Component + Integration
+ Manual/UAT
Android POS
approval_request, audit_log, employee/pin state
HP, ALT, EXC, AUTH, OFF, DATA
UC-16
Catat Alasan Exception
Supporting
P2
Offline: Bersyarat
ACT-16, SEQ-16
S2-S7
Domain/App + Data + UI selektif
Unit/Component + Integration
Android POS / Desktop
Backoffice
reason_code, audit_log, file_asset opsional
HP, ALT, EXC, DATA
UC-17
Input Kas Awal
Supporting
P2
Offline: Bersyarat
ACT-17, SEQ-17
S2-S7
Domain/App + Data + Integration
+ UI selektif
Unit/Component + Integration
+ Manual/UAT
Android POS
opening_cash, cashier_session, audit_log
HP, ALT, EXC, AUTH, DATA
UC-18
Cash In / Cash Out
Primary
P1
Offline: Bersyarat
ACT-18, SEQ-18
S2-S7
Domain/App + Data + Integration
+ UI selektif
Unit/Component + Integration
+ Manual/UAT
Android POS / Desktop
Backoffice
cash_movement, cashier_session, business_day,
reason_code, audit_log
HP, ALT, EXC, AUTH, DATA
UC-19
Safe Drop
Primary
P1
Offline: Bersyarat
ACT-19, SEQ-19
S2-S7
Domain/App + Data + Integration
+ UI selektif
Unit/Component + Integration
+ Manual/UAT
Android POS
safe_drop, cash_session, cashier_session, audit_log
HP, ALT, EXC, AUTH, DATA
UC-20
Tutup Shift
Primary
P1
Offline: Bersyarat
ACT-20, SEQ-20
S2-S7
Domain/App + Data + Integration
+ UI selektif
Unit/Component + Integration
+ Manual/UAT
Android POS / Desktop
Backoffice
cashier_session, cash_reconciliation, shift_report, audit_log
HP, ALT, EXC, AUTH, DATA
UC-21
Rekonsiliasi Kas
Supporting
P2
Offline: Bersyarat
ACT-21, SEQ-21
S2-S7
Domain/App + Data + Integration
+ UI selektif
Unit/Component + Integration
+ Manual/UAT
Android POS / Desktop
Backoffice
cash_reconciliation, cash_movement, safe_drop, payment
summary
HP, ALT, EXC, AUTH, DATA
UC-22
Generate X / Z Report
Supporting
P2
Offline: Bersyarat
ACT-22, SEQ-22
S2-S7
Domain/App + Data + UI selektif
Unit/Component + Integration
Android POS / Desktop
Backoffice
shift_report, store_operational_report, file_asset opsional
HP, ALT, EXC, DATA
UC-23
Tutup Hari
Primary
P1
Offline: Eksplisit
ACT-23, SEQ-23
S2-S7
Domain/App + Data + UI selektif
Unit/Component + Integration
+ Manual/UAT
Android POS / Desktop
Backoffice
business_day, store_operational_report, cashier_session,
audit_log
HP, ALT, EXC, AUTH, OFF, DATA
UC-24
Cek Stok & Movement
Primary
P1
Offline: Bersyarat
ACT-24, SEQ-24
S2-S7
Domain/App + Data + UI selektif
Unit/Component + Integration
Android POS / Desktop
Backoffice
inventory_balance, stock_ledger_entry, inventory_location,
inventory_bucket
HP, ALT, EXC, DATA
UC-25
Receiving Barang
Primary
P1
Offline: Eksplisit
ACT-25, SEQ-25
S2-S7
Domain/App + Data + Integration
Unit/Component + Integration
+ Manual/UAT
Android POS
receiving_document, receiving_line, receiving_discrepancy,
inventory_balance, stock_ledger_entry
HP, ALT, EXC, OFF, DATA
UC-26
Transfer Stok
Primary
P1
Offline: Bersyarat
ACT-26, SEQ-26
S2-S7
Domain/App + Data + UI selektif
Unit/Component + Integration
Android POS
stock_transfer, stock_transfer_line, inventory_balance,
stock_ledger_entry
HP, ALT, EXC, DATA
UC-27
Replenishment Rak
Primary
P1
Offline: Bersyarat
ACT-27, SEQ-27
S2-S7
Domain/App + Data + UI selektif
Unit/Component + Integration
Android POS
replenishment_task, replenishment_line, inventory_balance,
stock_ledger_entry
HP, ALT, EXC, DATA
UC-28
Stock Adjustment
Primary
P1
Offline: Bersyarat
ACT-28, SEQ-28
S2-S7
Domain/App + Data + UI selektif
Unit/Component + Integration
+ Manual/UAT
Android POS
stock_adjustment, stock_adjustment_line, reason_code,
approval_request, stock_ledger_entry, audit_log
HP, ALT, EXC, AUTH, DATA

8 Maret 2026
ID
Use case
Tipe / Prioritas
Trace
Level uji
Automation target
Platform utama
Fokus verifikasi data
UC-29
Stock Opname / Cycle Count
Primary
P1
Offline: Bersyarat
ACT-29, SEQ-29
S2-S7
Domain/App + Data + UI selektif
Unit/Component + Integration
+ Manual/UAT
Android POS
cycle_count, cycle_count_line, stock_adjustment opsional,
audit_log
HP, ALT, EXC, AUTH, DATA
UC-30
Kelola Barang Rusak / Expired
Primary
P1
Offline: Bersyarat
ACT-30, SEQ-30
S2-S7
Domain/App + Data + UI selektif
Unit/Component + Integration
+ Manual/UAT
Android POS
damage_disposition, damage_disposition_line,
inventory_balance, stock_ledger_entry
HP, ALT, EXC, AUTH, DATA
UC-31
Verifikasi Delivery / PO
Supporting
P2
Offline: Eksplisit
ACT-31, SEQ-31
S2-S7
Domain/App + Data + Integration
Unit/Component + Integration
+ Manual/UAT
Android POS
receiving_document reference, source delivery/PO metadata
HP, ALT, EXC, OFF, DATA
UC-32
Catat Selisih Receiving
Supporting
P2
Offline: Bersyarat
ACT-32, SEQ-32
S2-S7
Domain/App + Data + UI selektif
Unit/Component + Integration
+ Manual/UAT
Android POS
receiving_discrepancy, reason_code, approval_request
opsional, audit_log
HP, ALT, EXC, AUTH, DATA
UC-33
Cetak Label Harga / Rak
Primary
P1
Offline: Bersyarat
ACT-33, SEQ-33
S2-S7
Domain/App + Data + UI selektif
Unit/Component + Integration
Android POS
price_label_job, price_label_line, product/pricing snapshot
HP, ALT, EXC, DATA
UC-34
Lihat Laporan Operasional Toko
Primary
P1
Offline: Bersyarat
ACT-34, SEQ-34
S2-S7
Domain/App + Data + UI selektif
Unit/Component + Integration
Android POS / Desktop
Backoffice
store_operational_report, report_snapshot_line, business_day
HP, ALT, EXC, DATA
UC-35
Sinkronisasi Data Toko
Primary
P1
Offline: Eksplisit
ACT-35, SEQ-35
S2-S7
Domain/App + Data + Integration
Unit/Component + Integration
+ Manual/UAT
Android POS / Desktop
Backoffice / Android Mobile
(selektif)
outbox_event, sync_batch, sync_item,
master_data_snapshot, offline_operation_window
HP, ALT, EXC, OFF, DATA
UC-36
Mode Operasi Offline
Primary
P1
Offline: Eksplisit
ACT-36, SEQ-36
S1-S7
Domain/App + Data + UI selektif
Unit/Component + Integration
+ Manual/UAT
Android POS
offline_operation_window, outbox_event, sync status marker
HP, ALT, EXC, AUTH, OFF, DATA
UC-37
Rekonsiliasi Sync Gagal
Supporting
P2
Offline: Eksplisit
ACT-37, SEQ-37
S2-S7
Domain/App + Data + UI selektif
Unit/Component + Integration
+ Manual/UAT
Android POS / Desktop
Backoffice
sync_conflict, sync_item, sync_batch, outbox_event
HP, ALT, EXC, OFF, DATA
UC-38
Autentikasi & Validasi Role
Supporting
P1
Offline: Eksplisit
ACT-38, SEQ-38
S2-S7
Domain/App + Data + Integration
Unit/Component + Integration
+ Manual/UAT
Android POS
employee, role/pin state, audit_log opsional
HP, ALT, EXC, AUTH, OFF
UC-39
Catat Audit Log
Supporting
P1
Offline: Eksplisit
ACT-39, SEQ-39
S1-S7
Domain/App + Data + UI selektif
Unit/Component + Integration
+ Manual/UAT
Android POS
audit_log, outbox_event opsional
HP, ALT, EXC, OFF, DATA
## 5. Prioritas coverage per domain bisnis
A. Front Office, Checkout, Return, dan Approval
- Jumlah use case: 12; prioritas P1: 8; supporting/internal tetap dilacak bila memengaruhi correctness bisnis atau auditability.
- Fokus domain/arsitektur dominan: Shift & Cash Control dan modul terkait pada application/data/UI sesuai artefak architecture.
- Coverage dasar wajib memuat happy path, alternate/exception yang tercatat pada activity/use case spec, dan verifikasi state persistence yang berasal dari ERD v2 target-state.
UC
Nama
Prioritas
Catatan QA domain
UC-01
Mulai Shift Kasir
P1
Open-shift use case -> validasi role -> cek konflik shift -> tulis shift + opening cash + audit + outbox bila perlu.

8 Maret 2026
UC
Nama
Prioritas
Catatan QA domain
UC-02
Proses Penjualan
P1
Checkout facade menyimpan transaksi lokal dulu, lalu finalisasi settlement, receipt, audit, dan event sync.
UC-03
Kelola Keranjang
P2
Operasi add/update/remove item tetap lewat application layer; pricing dihitung ulang setelah perubahan item.
UC-04
Terapkan Member / Voucher
P1
Application service memvalidasi eligibilitas benefit, menyimpan snapshot benefit, lalu hitung ulang total.
UC-05
Proses Pembayaran
P1
Payment flow harus menghasilkan state success/pending/failed yang sah sebelum sale boleh selesai.
UC-06
Terbitkan Receipt
P2
Receipt dibuat dari transaksi final dan wajib konsisten dengan settlement state, print/digital channel, dan reprint policy.
UC-07
Suspend / Resume Transaksi
P1
Suspend menyimpan snapshot keranjang; resume wajib cegah duplicate resume dan reprice bila policy mewajibkan.
UC-08
Cari / Scan Produk
P2
Lookup memakai katalog lokal/cached yang valid untuk mendukung operasi online-first dengan fallback offline terkontrol.
UC-09
Hitung Harga, Promo, Pajak
P2
Pricing engine menjadi satu jalur resmi perhitungan sebelum payment dimulai; UI tidak boleh menghitung sendiri.
UC-10
Void Item / Transaksi
P1
Void wajib reason-based, bisa memerlukan approval, dan harus meninggalkan audit trail yang tidak ambigu.
UC-11
Return / Refund
P1
Return memisahkan intent, policy, disposition stok, dan settlement refund/store credit; bukan refund administratif biasa.
UC-12
Price Override / Manual
Discount
P1
Override hanya lewat pricing path resmi; threshold menentukan perlu/tidaknya approval supervisor.
B. Shift, Cash Control, dan End-of-Day
- Jumlah use case: 12; prioritas P1: 5; supporting/internal tetap dilacak bila memengaruhi correctness bisnis atau auditability.
- Fokus domain/arsitektur dominan: Return & Refund dan modul terkait pada application/data/UI sesuai artefak architecture.
- Coverage dasar wajib memuat happy path, alternate/exception yang tercatat pada activity/use case spec, dan verifikasi state persistence yang berasal dari ERD v2 target-state.
UC
Nama
Prioritas
Catatan QA domain
UC-13
Lookup Receipt
P2
Lookup receipt menjadi pintu masuk validasi return dan harus menjaga jejak transaksi asli yang dicari.
UC-14
Validasi Return Policy
P2
Policy validation dieksekusi sebelum refund; hasilnya harus tersimpan sebagai snapshot keputusan yang dapat diaudit.
UC-15
Supervisor Approval
P2
Approval merekam siapa, kapan, untuk apa, dan hasil keputusannya; mendukung PIN lokal/offline sesuai policy.
UC-16
Catat Alasan Exception
P2
Reason code wajib untuk flow sensitif: void, return, override, receiving gap, adjustment, dan exception operasional lain.
UC-17
Input Kas Awal
P2
Opening cash diinput di awal shift dan menjadi bagian dari transaksi pembukaan shift, bukan catatan terpisah yang longgar.
UC-18
Cash In / Cash Out
P1
Movement non-sale harus punya tipe semantik, alasan, actor, dan approval bila threshold policy terlampaui.
UC-19
Safe Drop
P1
Safe drop diperlakukan sebagai entity sendiri dengan actor, witness/approver, bag/reference, dan state yang jelas.
UC-20
Tutup Shift
P1
Close shift wajib mengikat expected cash, counted cash, reconciliation, report generation, dan audit dalam alur yang konsisten.
UC-21
Rekonsiliasi Kas
P2
Expected vs counted harus punya variance, decision, approver, dan notes; selisih tidak boleh hilang sebagai angka tanpa keputusan.
UC-22
Generate X / Z Report
P2
Report generation menarik snapshot totals yang tervalidasi; tidak dibangun dari counter UI ad hoc.
UC-23
Tutup Hari
P1
End-of-day memverifikasi kesiapan store-level, menghasilkan laporan final, lalu menutup business day secara eksplisit.
UC-24
Cek Stok & Movement
P1
View stok harus memisahkan current state vs historical explanation dan mendukung filter item/lokasi/bucket.
C. Inventory Operations

8 Maret 2026
- Jumlah use case: 9; prioritas P1: 7; supporting/internal tetap dilacak bila memengaruhi correctness bisnis atau auditability.
- Fokus domain/arsitektur dominan: Inventory dan modul terkait pada application/data/UI sesuai artefak architecture.
- Coverage dasar wajib memuat happy path, alternate/exception yang tercatat pada activity/use case spec, dan verifikasi state persistence yang berasal dari ERD v2 target-state.
UC
Nama
Prioritas
Catatan QA domain
UC-25
Receiving Barang
P1
Receiving membandingkan expected vs actual, lalu menulis balance + ledger + discrepancy secara traceable.
UC-26
Transfer Stok
P1
Transfer menyimpan dokumen, status proses, dan perubahan stok sesuai state transfer; tidak boleh update stok tanpa source document.
UC-27
Replenishment Rak
P1
Replenishment memindahkan stok antar lokasi internal tanpa mengubah total on-hand toko.
UC-28
Stock Adjustment
P1
Adjustment harus reason-based, approval-aware, dan menulis balance + ledger + audit secara atomik.
UC-29
Stock Opname / Cycle Count
P1
Cycle count menyimpan snapshot area/item, counted qty, variance, dan keputusan resolusi; bukan hanya overwrite stok.
UC-30
Kelola Barang Rusak /
Expired
P1
Barang rusak/expired harus punya disposition outcome yang eksplisit: salvage, return to vendor, destroy, atau write-off.
UC-31
Verifikasi Delivery / PO
P2
Verifikasi referensi delivery/PO adalah gate sebelum receiving; jika referensi tidak valid maka receiving tidak boleh lanjut.
UC-32
Catat Selisih Receiving
P2
Receiving gap wajib tercatat dengan alasan dan keputusan handling; selisih tidak boleh hilang diam-diam.
UC-33
Cetak Label Harga / Rak
P1
Pencetakan label mengambil snapshot harga yang sah agar hasil print dapat ditelusuri ke konteks pricing saat itu.
D. Reporting, Sync, Offline, Auth, dan Audit
- Jumlah use case: 6; prioritas P1: 5; supporting/internal tetap dilacak bila memengaruhi correctness bisnis atau auditability.
- Fokus domain/arsitektur dominan: Reporting & Sync dan modul terkait pada application/data/UI sesuai artefak architecture.
- Coverage dasar wajib memuat happy path, alternate/exception yang tercatat pada activity/use case spec, dan verifikasi state persistence yang berasal dari ERD v2 target-state.
UC
Nama
Prioritas
Catatan QA domain
UC-34
Lihat Laporan Operasional
Toko
P1
Laporan operasional dibangun dari event dan state yang tervalidasi, bukan dari counter bebas atau query lintas context yang tidak dikendalikan.
UC-35
Sinkronisasi Data Toko
P1
Sync menyiapkan batch, kirim/ambil data, tandai sukses, dan membuat daftar reconcile untuk partial failure; tidak boleh silent drop.
UC-36
Mode Operasi Offline
P1
Offline mode adalah controlled degradation: hanya fungsi aman yang boleh jalan, semua mutasi diberi penanda pending sync.
UC-37
Rekonsiliasi Sync Gagal
P2
User harus bisa retry, resolve conflict, atau mark manual investigation; sync failure wajib punya status akhir yang terlihat.
UC-38
Autentikasi & Validasi Role
P1
Login online-first mendukung cache grant/offline fallback sesuai policy; role guard tetap dievaluasi di application boundary.
UC-39
Catat Audit Log
P1
Audit adalah append-only dan harus ikut dalam keputusan bisnis lokal; bila store pusat unavailable, event dapat diantrikan dengan aman.

8 Maret 2026
## 6. Detailed test cases untuk critical flow wave-1
Bagian ini memuat test case detail yang paling penting untuk gelombang pertama. Setiap case disusun agar langsung bisa dipakai oleh QA manual, automation engineer, atau developer yang
sedang menyiapkan component/integration test.
CF-01 · UC-01 Mulai Shift Kasir
Tujuan
Mulai shift gagal ketika autentikasi gagal, shift konflik, atau opening cash di luar
policy
Prioritas
P1 / Wave-1
Trace refs
ACT-01, SEQ-01 · S2,S3,S4 · S1-S7
Layer
Domain/App + Data + Integration + UI selektif
Automation target
Unit/Component + Integration + Manual/UAT
Platform
Android POS
Entity / tabel fokus
cashier_session, opening_cash, approval_request, audit_log
Kode uji
EXC, AUTH, OFF, DATA
Precondition
- Kasir terdaftar pada terminal POS.
- Terminal memiliki state business_day aktif.
- Role supervisor tersedia untuk jalur approval bila diizinkan policy.
Data uji
- User kasir valid + user kasir tanpa grant.
- Satu shift aktif konflik pada terminal yang sama.
- Nilai opening cash di bawah/di atas threshold policy.
Langkah uji
- 1. Login sebagai kasir dan coba buka shift pada terminal yang sudah memiliki shift aktif.
- 2. Masukkan nominal opening cash di luar policy.
- 3. Lakukan percobaan tanpa approval supervisor lalu ulangi dengan approval yang sah.
- 4. Simulasikan identity service tidak tersedia dan cek apakah fallback offline diizinkan.
Expected result
- Sistem menolak pembukaan shift tanpa grant/approval yang sah.
- Tidak terbentuk cashier_session aktif ganda pada terminal.
- Opening cash hanya tersimpan jika policy/approval terpenuhi.
- Audit trail dan reason/approval tercatat saat jalur exception dipakai.
Fokus verifikasi data
Periksa state entity/tabel berikut setelah langkah kunci: cashier_session, opening_cash, approval_request, audit_log. Validasikan juga bahwa tidak ada side effect ganda pada business_day,
cashier_session, opening_cash, pos_terminal, audit_log serta audit/outbox tetap konsisten dengan keputusan bisnis final.
Catatan risiko
Tanpa ini, shift bisa terbuka tanpa otorisasi atau dengan state kas awal yang tidak sah.
CF-02 · UC-05 Proses Pembayaran
Tujuan
Pembayaran tidak memfinalkan sale saat declined, timeout, atau pending
investigation
Prioritas
P1 / Wave-1
Trace refs
ACT-05, SEQ-05 · S2,S4,S6 · S2-S7
Layer
Domain/App + Data + Integration + UI selektif
Automation target
Unit/Component + Integration + Manual/UAT
Platform
Android POS
Entity / tabel fokus
payment, payment_allocation, sale_transaction, outbox_event
Kode uji
EXC, OFF, DATA

8 Maret 2026
Precondition
- Transaksi penjualan aktif sudah memiliki item dan total final.
- Metode pembayaran EDC/gateway tersedia atau stub integration aktif.
Data uji
- Kartu/EDC declined.
- Response timeout dari provider.
- Response pending investigation / reversal uncertain.
Langkah uji
- 1. Lakukan checkout normal sampai langkah pembayaran.
- 2. Picu declined, timeout, dan pending pada provider stub.
- 3. Coba cetak receipt/finalisasi sale setelah masing-masing kondisi.
- 4. Lakukan retry yang sah dan verifikasi hanya satu settlement final yang terbentuk.
Expected result
- Sale tidak berpindah ke completed state saat payment belum valid.
- payment/payment_allocation menyimpan status yang eksplisit.
- Receipt final tidak diterbitkan untuk payment yang belum final.
- Retry tidak menduplikasi efek finansial maupun outbox event.
Fokus verifikasi data
Periksa state entity/tabel berikut setelah langkah kunci: payment, payment_allocation, sale_transaction, outbox_event. Validasikan juga bahwa tidak ada side effect ganda pada payment,
payment_allocation, sale_transaction, receipt, outbox_event serta audit/outbox tetap konsisten dengan keputusan bisnis final.
Catatan risiko
Mencegah sale dianggap final saat payment belum valid.
CF-03 · UC-07 Suspend / Resume Transaksi
Tujuan
Suspend/resume mencegah duplicate resume dan menolak snapshot rusak
Prioritas
P1 / Wave-1
Trace refs
ACT-07, SEQ-07 · S2,S4,S5 · S2-S7
Layer
Domain/App + Data + Integration + UI selektif
Automation target
Unit/Component + Integration + Manual/UAT
Platform
Android POS
Entity / tabel fokus
suspended_sale, audit_log
Kode uji
EXC, DATA
Precondition
- Ada transaksi aktif yang belum memiliki payment sukses.
- Policy store mengizinkan suspend/resume.
Data uji
- Snapshot suspended normal.
- Snapshot kedaluwarsa/korup.
- Resume dari terminal lain setelah transaksi lebih dulu diresume.
Langkah uji
- 1. Suspend transaksi aktif.
- 2. Resume transaksi dari terminal sah.
- 3. Ulangi resume dari terminal kedua atau pakai snapshot yang sengaja dirusak/kedaluwarsa.
- 4. Verifikasi perhitungan harga ulang jika policy mengharuskan reprice.
Expected result
- Hanya satu terminal yang dapat memiliki sesi resumed yang sah.
- Snapshot rusak/kedaluwarsa ditolak dengan pesan yang dapat ditindaklanjuti.
- Tidak ada mutasi stok final sampai transaksi benar-benar selesai.
- Audit log menangkap handover/resume exception yang penting.

8 Maret 2026
Fokus verifikasi data
Periksa state entity/tabel berikut setelah langkah kunci: suspended_sale, audit_log. Validasikan juga bahwa tidak ada side effect ganda pada suspended_sale, sale_item snapshot, audit_log serta
audit/outbox tetap konsisten dengan keputusan bisnis final.
Catatan risiko
Mencegah dua terminal menghidupkan transaksi yang sama.
CF-04 · UC-10 Void Item / Transaksi
Tujuan
Void setelah payment wajib reason-based dan approval-aware
Prioritas
P1 / Wave-1
Trace refs
ACT-10, SEQ-10 · S2,S5,S7 · S2-S7
Layer
Domain/App + Data + Integration + UI selektif
Automation target
Unit/Component + Integration + Manual/UAT
Platform
Android POS
Entity / tabel fokus
approval_request, reason_code, audit_log, sale_transaction
Kode uji
AUTH, EXC, DATA
Precondition
- Sale telah memiliki payment sukses atau sebagian.
- Reason code dan policy approval tersedia.
Data uji
- Reason code valid.
- Reason code kosong/tidak valid.
- Approval supervisor ditolak.
Langkah uji
- 1. Pilih void item/transaksi setelah payment.
- 2. Isi reason code valid lalu ulangi dengan reason kosong.
- 3. Jalankan jalur approval ditolak dan approval diterima.
- 4. Verifikasi efek reversal terhadap sale, payment, dan audit.
Expected result
- Void tidak boleh lolos tanpa reason code yang sah.
- Policy approval dijalankan sebelum state finansial berubah.
- Audit log menyimpan actor, alasan, dan keputusan approval.
- State transaksi konsisten dan tidak menghasilkan reversal ganda.
Fokus verifikasi data
Periksa state entity/tabel berikut setelah langkah kunci: approval_request, reason_code, audit_log, sale_transaction. Validasikan juga bahwa tidak ada side effect ganda pada sale_transaction, sale_item,
approval_request, reason_code, audit_log serta audit/outbox tetap konsisten dengan keputusan bisnis final.
Catatan risiko
Flow sensitif finansial harus reason-based dan auditable.
CF-05 · UC-11 Return / Refund
Tujuan
Return/refund menangani receipt hilang, policy lewat, dan fallback store credit
Prioritas
P1 / Wave-1
Trace refs
ACT-11, SEQ-11 · S2,S5,S7 · S2-S7
Layer
Domain/App + Data + Integration + UI selektif
Automation target
Unit/Component + Integration + Manual/UAT
Platform
Android POS
Entity / tabel fokus
return_transaction, return_policy_decision, refund_record, store_credit_*
Kode uji
ALT, EXC, AUTH, DATA
Precondition
- Data transaksi asal tersedia atau tidak tersedia sesuai skenario.
- Policy return aktif dan metode refund dapat disimulasikan gagal.

8 Maret 2026
Data uji
- Receipt valid.
- Receipt tidak ditemukan.
- Tanggal transaksi melewati policy.
- Refund kartu gagal dan store credit diizinkan.
Langkah uji
- 1. Mulai proses return dengan receipt valid lalu receipt tidak ditemukan.
- 2. Jalankan policy decision sampai kondisi lewat batas.
- 3. Picu kegagalan refund kartu.
- 4. Aktifkan jalur fallback store credit dan verifikasi final state.
Expected result
- return_policy_decision merekam eligibility yang eksplisit.
- Return yang tidak eligible berhenti dengan alasan yang jelas.
- Saat refund kartu gagal, fallback hanya terjadi jika policy mengizinkan.
- return_transaction, refund_record, dan store_credit konsisten serta tidak dobel.
Fokus verifikasi data
Periksa state entity/tabel berikut setelah langkah kunci: return_transaction, return_policy_decision, refund_record, store_credit_*. Validasikan juga bahwa tidak ada side effect ganda pada
return_transaction, return_line, return_policy_decision, refund_record, store_credit_* serta audit/outbox tetap konsisten dengan keputusan bisnis final.
Catatan risiko
Return di project ini bukan refund administratif sederhana.
CF-06 · UC-12 Price Override / Manual Discount
Tujuan
Manual discount/price override berhenti saat threshold terlampaui atau item
tidak eligible
Prioritas
P1 / Wave-1
Trace refs
ACT-12, SEQ-12 · S2,S5,S7 · S2-S7
Layer
Domain/App + Data + Integration + UI selektif
Automation target
Unit/Component + Integration + Manual/UAT
Platform
Android POS
Entity / tabel fokus
approval_request, reason_code, sale_transaction, audit_log
Kode uji
AUTH, EXC, DATA
Precondition
- Ada item dalam keranjang yang dapat/ tidak dapat diberi override.
- Policy threshold discount tersedia.
Data uji
- Item eligible.
- Item non-eligible.
- Diskon melewati threshold dan approval ditolak.
Langkah uji
- 1. Terapkan manual discount pada item eligible.
- 2. Ulangi pada item non-eligible.
- 3. Masukkan diskon di atas threshold lalu jalankan approval ditolak dan diterima.
- 4. Lanjutkan ke payment untuk memastikan harga final konsisten.
Expected result
- Hanya item eligible yang dapat menerima override.
- Approval menjadi gate sebelum harga final berubah untuk kasus threshold tinggi.
- Audit dan reason tercatat.
- Pricing final hanya berasal dari pricing path resmi, bukan hitung UI lokal.
Fokus verifikasi data
Periksa state entity/tabel berikut setelah langkah kunci: approval_request, reason_code, sale_transaction, audit_log. Validasikan juga bahwa tidak ada side effect ganda pada sale_transaction,
approval_request, reason_code, audit_log serta audit/outbox tetap konsisten dengan keputusan bisnis final.

8 Maret 2026
Catatan risiko
Menjaga pricing rule tetap satu jalur resmi.
CF-07 · UC-18 Cash In / Cash Out
Tujuan
Cash movement di atas limit wajib approval dan jejak audit
Prioritas
P1 / Wave-1
Trace refs
ACT-18, SEQ-18 · S2,S5,S7 · S2-S7
Layer
Domain/App + Data + Integration + UI selektif
Automation target
Unit/Component + Integration + Manual/UAT
Platform
Android POS / Desktop Backoffice
Entity / tabel fokus
cash_movement, reason_code, approval_request, audit_log
Kode uji
AUTH, EXC, DATA
Precondition
- Shift aktif tersedia.
- Limit safe drop / petty cash movement sudah dikonfigurasi.
Data uji
- Nominal di bawah limit.
- Nominal di atas limit.
- Approval ditolak.
Langkah uji
- 1. Catat cash movement normal.
- 2. Ulangi dengan nominal di atas limit.
- 3. Jalankan approval ditolak dan approval diterima.
- 4. Periksa saldo kas, movement log, dan audit trail.
Expected result
- Nominal di bawah limit selesai tanpa approval tambahan.
- Nominal di atas limit berhenti sampai approval sah diterima.
- cash_movement dan audit_log selalu sinkron dengan keputusan final.
- Tidak ada movement yang committed sebagian.
Fokus verifikasi data
Periksa state entity/tabel berikut setelah langkah kunci: cash_movement, reason_code, approval_request, audit_log. Validasikan juga bahwa tidak ada side effect ganda pada cash_movement,
cashier_session, business_day, reason_code, audit_log serta audit/outbox tetap konsisten dengan keputusan bisnis final.
Catatan risiko
Mengurangi celah shrinkage dan manipulasi kas.
CF-08 · UC-20 Tutup Shift
Tujuan
Tutup shift ditolak saat selisih kas melewati tolerance atau masih ada transaksi
pending
Prioritas
P1 / Wave-1
Trace refs
ACT-20, SEQ-20 · S2,S4,S7 · S2-S7
Layer
Domain/App + Data + Integration + UI selektif
Automation target
Unit/Component + Integration + Manual/UAT
Platform
Android POS / Desktop Backoffice
Entity / tabel fokus
cash_reconciliation, shift_report, cashier_session, audit_log
Kode uji
EXC, AUTH, DATA
Precondition
- Shift aktif dan transaksi hari berjalan tersedia.
- Tolerance reconciliation dikonfigurasi.

8 Maret 2026
Data uji
- Selisih kas kecil dalam tolerance.
- Selisih kas di atas tolerance.
- Transaksi payment pending belum selesai.
Langkah uji
- 1. Jalankan tutup shift dengan rekonsiliasi normal.
- 2. Ulangi dengan selisih besar.
- 3. Tambahkan transaksi payment pending lalu coba close shift.
- 4. Lakukan approval bila policy mengizinkan.
Expected result
- Close shift sukses hanya bila reconciliation memenuhi syarat.
- Transaksi pending mencegah shift ditutup final.
- shift_report dan cash_reconciliation konsisten dengan keputusan close.
- Audit/outbox menjadi satu bundle atomik dengan perubahan status shift.
Fokus verifikasi data
Periksa state entity/tabel berikut setelah langkah kunci: cash_reconciliation, shift_report, cashier_session, audit_log. Validasikan juga bahwa tidak ada side effect ganda pada cashier_session,
cash_reconciliation, shift_report, audit_log serta audit/outbox tetap konsisten dengan keputusan bisnis final.
Catatan risiko
Close shift tidak boleh menutup masalah operasional secara diam-diam.
CF-09 · UC-23 Tutup Hari
Tujuan
Tutup hari menolak kondisi shift aktif, data belum lengkap, atau mode offline
berisiko
Prioritas
P1 / Wave-1
Trace refs
ACT-23, SEQ-23 · S2,S5,S6,S7 · S2-S7
Layer
Domain/App + Data + UI selektif
Automation target
Unit/Component + Integration + Manual/UAT
Platform
Android POS / Desktop Backoffice
Entity / tabel fokus
business_day, store_operational_report, cashier_session, outbox_event
Kode uji
ALT, EXC, OFF, DATA
Precondition
- Business day aktif.
- Laporan operasional dan shift sudah tersedia sebagian/seluruhnya.
Data uji
- Masih ada shift aktif.
- Data rekonsiliasi/laporan belum lengkap.
- Store dalam offline mode.
Langkah uji
- 1. Coba tutup hari saat semua syarat terpenuhi.
- 2. Ulangi saat masih ada shift aktif.
- 3. Ulangi saat data kritikal belum lengkap.
- 4. Ulangi dalam kondisi offline yang tidak diizinkan policy.
Expected result
- Business day hanya bisa ditutup sekali dan dalam kondisi siap.
- Sistem menolak penutupan bila ada syarat minimum yang belum terpenuhi.
- outbox_event / reporting snapshot sesuai dengan status final.
- Operator mendapat alasan blokir yang jelas untuk recovery.
Fokus verifikasi data
Periksa state entity/tabel berikut setelah langkah kunci: business_day, store_operational_report, cashier_session, outbox_event. Validasikan juga bahwa tidak ada side effect ganda pada business_day,
store_operational_report, cashier_session, audit_log serta audit/outbox tetap konsisten dengan keputusan bisnis final.

8 Maret 2026
Catatan risiko
Business day harus tunggal dan tertutup dengan kesiapan minimum yang jelas.
CF-10 · UC-25 Receiving Barang
Tujuan
Receiving mencatat discrepancy saat referensi delivery tidak valid atau parsial
Prioritas
P1 / Wave-1
Trace refs
ACT-25, SEQ-25 · S2,S5,S7 · S2-S7
Layer
Domain/App + Data + Integration
Automation target
Unit/Component + Integration + Manual/UAT
Platform
Android POS
Entity / tabel fokus
receiving_document, receiving_line, receiving_discrepancy, stock_ledger_entry
Kode uji
ALT, EXC, OFF, DATA
Precondition
- Dokumen referensi delivery/PO tersedia atau sengaja tidak sinkron.
- User inventory memiliki akses receiving.
Data uji
- Receiving penuh sesuai referensi.
- Receiving parsial.
- Referensi delivery tidak valid / belum ada di lokal.
Langkah uji
- 1. Lakukan receiving normal.
- 2. Ulangi dengan jumlah parsial.
- 3. Ulangi dengan referensi invalid atau belum tersinkron.
- 4. Verifikasi pembentukan discrepancy dan stock ledger.
Expected result
- Receiving parsial menghasilkan discrepancy yang terdokumentasi.
- Referensi invalid memblokir finalisasi receiving.
- stock_ledger_entry hanya terbentuk dari event yang valid.
- Receiving tidak boleh diam-diam memutasi saldo tanpa jejak.
Fokus verifikasi data
Periksa state entity/tabel berikut setelah langkah kunci: receiving_document, receiving_line, receiving_discrepancy, stock_ledger_entry. Validasikan juga bahwa tidak ada side effect ganda pada
receiving_document, receiving_line, receiving_discrepancy, inventory_balance, stock_ledger_entry serta audit/outbox tetap konsisten dengan keputusan bisnis final.
Catatan risiko
Receiving adalah sumber mutasi stok dan gap harus terdokumentasi.
CF-11 · UC-28 Stock Adjustment
Tujuan
Stock adjustment menolak stock negatif, item diblokir, atau approval gagal
Prioritas
P1 / Wave-1
Trace refs
ACT-28, SEQ-28 · S2,S5,S7 · S2-S7
Layer
Domain/App + Data + UI selektif
Automation target
Unit/Component + Integration + Manual/UAT
Platform
Android POS
Entity / tabel fokus
stock_adjustment*, approval_request, reason_code, stock_ledger_entry
Kode uji
AUTH, EXC, DATA
Precondition
- User inventory/supervisor tersedia.
- Stock balance awal diketahui.

8 Maret 2026
Data uji
- Adjustment wajar.
- Adjustment yang membuat saldo negatif.
- Item diblokir.
- Approval ditolak.
Langkah uji
- 1. Buat stock adjustment normal.
- 2. Ulangi dengan saldo menjadi negatif.
- 3. Ulangi pada item diblokir.
- 4. Jalankan approval ditolak pada adjustment sensitif.
Expected result
- Saldo negatif tidak boleh dihasilkan jika policy melarang.
- Item diblokir tidak dapat diadjust tanpa jalur resmi.
- approval_request, reason_code, dan stock_ledger_entry saling konsisten.
- Tidak ada mutation partial tanpa ledger explanation.
Fokus verifikasi data
Periksa state entity/tabel berikut setelah langkah kunci: stock_adjustment*, approval_request, reason_code, stock_ledger_entry. Validasikan juga bahwa tidak ada side effect ganda pada
stock_adjustment, stock_adjustment_line, reason_code, approval_request, stock_ledger_entry, audit_log serta audit/outbox tetap konsisten dengan keputusan bisnis final.
Catatan risiko
Adjustment adalah titik rawan penyalahgunaan inventori.
CF-12 · UC-29 Stock Opname / Cycle Count
Tujuan
Cycle count varians besar memerlukan double count atau menolak auto-adjust
Prioritas
P1 / Wave-1
Trace refs
ACT-29, SEQ-29 · S2,S5,S7 · S2-S7
Layer
Domain/App + Data + UI selektif
Automation target
Unit/Component + Integration + Manual/UAT
Platform
Android POS
Entity / tabel fokus
cycle_count*, stock_adjustment*, audit_log
Kode uji
ALT, EXC, AUTH, DATA
Precondition
- Siklus stock count aktif.
- Policy threshold high-value item tersedia.
Data uji
- Variance kecil.
- Variance besar pada high-value item.
- Auto-adjust ditolak.
Langkah uji
- 1. Input hasil count normal.
- 2. Ulangi dengan variance besar.
- 3. Jalankan jalur double count atau penolakan auto-adjust.
- 4. Verifikasi hubungan cycle_count dengan stock_adjustment turunan.
Expected result
- Variance besar tidak langsung overwrite inventory balance.
- Sistem meminta double count/approval sesuai policy.
- Jejak resolusi variance lengkap dan dapat diaudit.
- Adjustment turunan hanya tercipta setelah gate policy terpenuhi.
Fokus verifikasi data
Periksa state entity/tabel berikut setelah langkah kunci: cycle_count*, stock_adjustment*, audit_log. Validasikan juga bahwa tidak ada side effect ganda pada cycle_count, cycle_count_line,
stock_adjustment opsional, audit_log serta audit/outbox tetap konsisten dengan keputusan bisnis final.

8 Maret 2026
Catatan risiko
Variance besar perlu jejak resolusi, bukan overwrite saldo.
CF-13 · UC-35 Sinkronisasi Data Toko
Tujuan
Sync batch menangani partial failure, retry, dan conflict dengan state terminal
eksplisit
Prioritas
P1 / Wave-1
Trace refs
ACT-35, SEQ-35 · S2,S4,S6,S7 · S2-S7
Layer
Domain/App + Data + Integration
Automation target
Unit/Component + Integration + Manual/UAT
Platform
Android POS / Desktop Backoffice / Android Mobile (selektif)
Entity / tabel fokus
sync_batch, sync_item, sync_conflict, outbox_event
Kode uji
ALT, EXC, OFF, DATA
Precondition
- Ada outbox_event pending.
- Koneksi HQ dapat dipicu gagal sebagian.
Data uji
- Batch semua sukses.
- Sebagian item gagal.
- Conflict local vs HQ.
- Retry berhasil / gagal ulang.
Langkah uji
- 1. Jalankan sync normal.
- 2. Picu partial failure pada beberapa item.
- 3. Picu conflict antara state lokal dan HQ.
- 4. Lakukan retry dan/atau create sync_conflict untuk manual reconcile.
Expected result
- sync_batch dan sync_item menyimpan hasil per item, bukan status global samar.
- Item gagal dapat di-retry tanpa menduplikasi efek bisnis.
- Conflict menghasilkan sync_conflict dengan resolution status eksplisit.
- Tidak ada outbox_event yang hilang tanpa jejak status.
Fokus verifikasi data
Periksa state entity/tabel berikut setelah langkah kunci: sync_batch, sync_item, sync_conflict, outbox_event. Validasikan juga bahwa tidak ada side effect ganda pada outbox_event, sync_batch,
sync_item, master_data_snapshot, offline_operation_window serta audit/outbox tetap konsisten dengan keputusan bisnis final.
Catatan risiko
Sync adalah subsistem bisnis yang terlihat, bukan queue latar belakang yang sunyi.
CF-14 · UC-36 Mode Operasi Offline
Tujuan
Offline mode memblokir fitur berisiko saat cache tidak memadai atau time drift
tinggi
Prioritas
P1 / Wave-1
Trace refs
ACT-36, SEQ-36 · S2,S3,S6,S7 · S1-S7
Layer
Domain/App + Data + UI selektif
Automation target
Unit/Component + Integration + Manual/UAT
Platform
Android POS
Entity / tabel fokus
offline_operation_window, outbox_event, sync markers
Kode uji
EXC, AUTH, OFF, DATA
Precondition
- Store policy mengizinkan controlled offline mode.
- Cache lokal tersedia sebagian/seluruhnya.

8 Maret 2026
Data uji
- Cache memadai.
- Cache tidak memadai.
- Clock/time drift tinggi.
- Fitur loyalty nonaktif, cash sales tetap aktif.
Langkah uji
- 1. Aktifkan offline mode karena gangguan koneksi.
- 2. Lakukan operasi aman seperti cash sale.
- 3. Coba operasi berisiko saat cache tidak memadai atau time drift tinggi.
- 4. Pulihkan koneksi dan jalankan reconcile.
Expected result
- Sistem hanya mengizinkan fungsi yang aman secara bisnis.
- Semua mutasi offline ditandai pending sync.
- Operasi berisiko diblokir dengan alasan jelas.
- Saat koneksi pulih, backlog dapat direconcile tanpa kehilangan jejak.
Fokus verifikasi data
Periksa state entity/tabel berikut setelah langkah kunci: offline_operation_window, outbox_event, sync markers. Validasikan juga bahwa tidak ada side effect ganda pada offline_operation_window,
outbox_event, sync status marker serta audit/outbox tetap konsisten dengan keputusan bisnis final.
Catatan risiko
Offline mode bersifat terbatas dan harus bisa direkonsiliasi.
CF-15 · UC-37 Rekonsiliasi Sync Gagal
Tujuan
Rekonsiliasi sync gagal menyediakan retry, resolve, atau manual investigation
yang jelas
Prioritas
P1 / Wave-1
Trace refs
ACT-37, SEQ-37 · S2,S4,S6,S7 · S2-S7
Layer
Domain/App + Data + UI selektif
Automation target
Unit/Component + Integration + Manual/UAT
Platform
Android POS / Desktop Backoffice
Entity / tabel fokus
sync_conflict, sync_item, batch status
Kode uji
EXC, OFF, DATA
Precondition
- Ada sync_batch gagal dan sync_conflict tersedia.
- User manager/supervisor memiliki hak reconcile.
Data uji
- Retry sukses.
- Retry tetap gagal.
- Conflict perlu keputusan manual.
Langkah uji
- 1. Buka daftar batch gagal.
- 2. Lakukan retry atas item yang layak.
- 3. Pilih resolve/manual investigation untuk conflict yang tidak bisa diotomasi.
- 4. Verifikasi status akhir tiap conflict dan batch.
Expected result
- Setiap kegagalan memiliki terminal state yang terlihat.
- Retry hanya berlaku pada item yang masih eligible.
- Manual investigation meninggalkan jejak siapa memutuskan apa.
- Sistem tidak menyembunyikan conflict di background queue.
Fokus verifikasi data
Periksa state entity/tabel berikut setelah langkah kunci: sync_conflict, sync_item, batch status. Validasikan juga bahwa tidak ada side effect ganda pada sync_conflict, sync_item, sync_batch,
outbox_event serta audit/outbox tetap konsisten dengan keputusan bisnis final.

8 Maret 2026
Catatan risiko
Tidak boleh ada sync failure yang hilang tanpa status akhir.
CF-16 · UC-38 Autentikasi & Validasi Role
Tujuan
Login/role guard berhenti saat identity service gagal dan fallback offline tidak
diizinkan
Prioritas
P1 / Wave-1
Trace refs
ACT-38, SEQ-38 · S2,S4,S6 · S2-S7
Layer
Domain/App + Data + Integration
Automation target
Unit/Component + Integration + Manual/UAT
Platform
Android POS
Entity / tabel fokus
employee role/pin state, session cache, audit_log
Kode uji
EXC, AUTH, OFF
Precondition
- Identity service dapat diputus.
- Kebijakan offline credential cache dibedakan per role.
Data uji
- User dengan cache grant valid.
- User tanpa cache grant.
- Role yang memerlukan online validation.
Langkah uji
- 1. Lakukan login saat online.
- 2. Putuskan identity service dan coba login user dengan cache grant.
- 3. Ulangi dengan user tanpa cache grant atau fungsi yang memerlukan validation online.
- 4. Coba akses fungsi berizin setelah login fallback.
Expected result
- Fallback offline hanya berlaku untuk user/role yang diizinkan policy.
- Akses fungsi tetap diputuskan di application boundary.
- Session cache dan audit log merekam mode validasi yang dipakai.
- Role guard tidak boleh melemah hanya karena koneksi buruk.
Fokus verifikasi data
Periksa state entity/tabel berikut setelah langkah kunci: employee role/pin state, session cache, audit_log. Validasikan juga bahwa tidak ada side effect ganda pada employee, role/pin state, audit_log
opsional serta audit/outbox tetap konsisten dengan keputusan bisnis final.
Catatan risiko
Menjaga role guard tetap benar saat konektivitas buruk.
CF-17 · UC-39 Catat Audit Log
Tujuan
Audit log tetap terlindungi saat storage utama unavailable
Prioritas
P1 / Wave-1
Trace refs
ACT-39, SEQ-39 · S2,S4,S6,S7 · S1-S7
Layer
Domain/App + Data + UI selektif
Automation target
Unit/Component + Integration + Manual/UAT
Platform
Android POS
Entity / tabel fokus
audit_log, outbox_event
Kode uji
EXC, OFF, DATA
Precondition
- Use case sensitif memicu audit.
- Storage audit dapat disimulasikan unavailable.

8 Maret 2026
Data uji
- Audit write normal.
- Audit storage unavailable.
- Central store unavailable sehingga event harus diantrikan.
Langkah uji
- 1. Jalankan aksi sensitif yang memicu audit.
- 2. Picu kegagalan penulisan audit utama.
- 3. Verifikasi fallback enqueue yang aman.
- 4. Setelah recovery, jalankan flush/sync audit intent.
Expected result
- Aksi sensitif tidak lolos tanpa jejak audit atau enqueue yang setara.
- audit_log/outbox_event tetap konsisten dengan keputusan bisnis.
- Tidak ada partial commit yang menghilangkan evidensi tindakan.
- Recovery tidak menduplikasi record audit.
Fokus verifikasi data
Periksa state entity/tabel berikut setelah langkah kunci: audit_log, outbox_event. Validasikan juga bahwa tidak ada side effect ganda pada audit_log, outbox_event opsional serta audit/outbox tetap
konsisten dengan keputusan bisnis final.
Catatan risiko
Aksi sensitif tidak boleh lolos tanpa jejak audit yang terlindungi.

8 Maret 2026
## 7. Mapping automation dan gate CI
Dokumen ini tidak memaksa semua test menjadi UI/E2E. Targetnya adalah layering yang sehat: common business logic diuji seawal mungkin, integration dipakai untuk boundary nyata, dan
UI/device test dipakai ketika fidelity platform memang dibutuhkan.
Workflow / gate
Scope minimal
Implikasi ke test specification
ci-shared.yml
Shared module compile, unit tests
domain/application/data, verifikasi SQLDelight
migration, static analysis
Test case yang dominan di domain/application/data wajib punya kandidat automation pada shared/common test atau component test.
ci-android.yml
Assemble Android target, unit tests, packaging
checks untuk android-pos dan android-mobile
Gunakan Android local tests untuk logic yang tidak butuh device; instrumented tests hanya untuk integration/perilaku Android yang memang perlu environment nyata.
ci-desktop.yml
Build desktop-backoffice JVM app dan desktop
module unit tests
Flow reporting/reconcile/backoffice yang penting harus punya baseline unit/component test di modul desktop/shared yang relevan.
ci-backend.yml
go test ./..., build API, migration checks, API
contract/unit tests
Kontrak sync dan migration yang mengubah persistence semantics harus diuji dalam PR yang sama, bukan ditunda ke release.
release.yml
Versioning, packaging, changelog, deployment
gates opsional
Bukan pengganti kualitas harian. Test spec ini mengasumsikan fast checks tetap berjalan di setiap PR.
Klasifikasi target automation
Jenis
Kapan dipakai
Contoh area
Unit / common test
Ketika perilaku murni bisnis atau application rule dapat
diuji tanpa framework UI/perangkat.
Pricing, eligibility, policy decision, totals, role guard rule, idempotency logic, sync item state machine.
Component / repository
test
Saat repository/data source/transaction coordinator
perlu diuji dengan DB lokal/stub.
Atomic bundle sale/return/shift close, audit+outbox commit, foreign key/integrity, migration behavior.
Integration test
Saat ada boundary ke payment, identity, loyalty, HQ
sync, printer, atau workflow lintas module.
Payment declined/pending, auth fallback, sync batch partial failure, reconcile conflict.
UI / instrumented /
desktop functional
Saat interaksi perangkat / Android / desktop UI
benar-benar memengaruhi correctness atau evidence
bisnis.
Scanner input, printed receipt flow, Android activity lifecycle edge, desktop reconcile screen flow.
Manual / UAT
Untuk eksplorasi operasional, usability, dan jalur
exception dengan bukti bisnis yang perlu ditinjau
manusia.
Shift handover lapangan, offline emergency operations, approval escalation, end-of-day operational review.
## 8. Database integrity checks yang wajib muncul
- Foreign key enforcement harus diverifikasi pada koneksi database yang dipakai test, bukan diasumsikan aktif otomatis.
- Uniqueness dan idempotency perlu diuji untuk receipt/invoice/business number, idempotency key, resume token, dan sync item/batch handling bila relevan.
- Periksa pemisahan current state vs explanation trail: inventory_balance adalah current state; stock_ledger_entry adalah penjelasan perubahan. Keduanya tidak boleh saling menggantikan.
- Semua bundle transaksi lokal penting wajib dibuktikan atomik: sale completion, return bundle, shift close bundle, inventory adjustment bundle, dan audit/outbox append.
- sync_conflict tidak boleh berakhir tanpa resolution_status yang eksplisit; outbox_event gagal tidak boleh hilang tanpa history retry/error yang memadai.
## 9. Gap, asumsi, dan tindak lanjut

8 Maret 2026
- Implementation Specification formal belum tersedia. Karena itu beberapa target automation masih dipetakan ke level module/bounded context, belum ke class/interface final.
- Dokumen ini mengasumsikan phase-1 tetap retail-first dan writer utama tetap terminal POS. Bila nanti multi-writer offline di satu store diaktifkan, test model untuk concurrency harus dinaikkan
signifikan.
- Beberapa use case supporting/internal tetap dipertahankan karena berdampak besar ke correctness: auth, pricing, audit, approval, sync reconcile. Menghapusnya dari test scope akan
menciptakan blind spot.
- Setelah implementation spec siap, langkah berikutnya adalah menurunkan nomor test case eksekusi, ownership automation, dan status Planned / In Progress / Passed / Failed per case.
Lampiran A. Checklist review cepat sebelum eksekusi
Checkpoint
Pertanyaan review
Traceability
Apakah setiap test case mengacu minimal ke UC + Activity + Sequence yang benar?
Policy gate
Apakah approval, role guard, dan reason code diuji sebagai decision bisnis, bukan hanya visibility rule UI?
Persistence
Apakah perubahan DB utama, audit, dan outbox diperiksa bersama untuk jalur write?
Offline/sync
Apakah state pending sync, retry, conflict, dan reconciliation diperiksa eksplisit?
Atomicity
Apakah ada verifikasi bahwa bundle transaksi tidak committed separuh jalan?
Idempotency
Apakah retry lokal/sync diuji agar tidak menggandakan efek bisnis?
Evidence
Apakah screenshot/log/query snapshot dan build info dikumpulkan cukup untuk audit QA?


## Constraints / Policies
Implementation spec formal belum tersedia penuh; isi dokumen diperlakukan sebagai test baseline, bukan execution report.

## Technical Notes
Dokumen ini harus dibaca bersama traceability matrix dan architecture/test automation baseline.

## Dependencies / Related Documents
- `uml_modeling_source_of_truth.md`
- `traceability_matrix_store_pos.md`
- `store_pos_use_case_detail_specifications.md`
- `store_pos_activity_detail_specifications.md`
- `store_pos_sequence_detail_specifications.md`
- `store_pos_domain_model_detail_specifications_v2.md`
- `cassy_architecture_specification_v1.md`
- `store_pos_erd_specification_v2.md`
- `cassy_test_automation_specification.md`
- `cassy_cicd_pipeline_strategy_v1.md`

## Risks / Gaps / Ambiguities
- Tidak ditemukan gap fatal saat ekstraksi. Tetap review ulang bagian tabel/angka jika dokumen ini akan dijadikan baseline implementasi final.

## Reviewer Notes
- Struktur telah dinormalisasi ke Markdown yang konsisten dan siap dipakai untuk design review / engineering handoff.
- Istilah utama dipertahankan sedekat mungkin dengan dokumen sumber untuk menjaga traceability lintas artefak.
- Bila ada konflik antar dokumen, prioritaskan source of truth, artefak yang paling preskriptif, dan baseline yang paling implementable.

## Source Mapping
- Original source: `store_pos_test_specification.pdf` (PDF, 22 pages)
- Output markdown: `store_pos_test_specification.md`
- Conversion approach: text extraction -> normalization -> structural cleanup -> standardized documentation wrapper.
- Note: beberapa tabel/list di PDF dapat mengalami wrapping antar baris; esensi dipertahankan, tetapi layout tabel asli tidak dipertahankan 1:1.
