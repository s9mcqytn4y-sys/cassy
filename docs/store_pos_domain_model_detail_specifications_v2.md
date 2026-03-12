# Store POS Domain Model Detail Specifications v2

## Document Overview
Baseline domain model yang memetakan bounded context, aggregate root, entity, value object, lifecycle, invariant, dan supporting object.

## Purpose
Menetapkan bounded context, aggregate root, entity, value object, relasi bisnis, lifecycle, dan invariant utama Target pembaca Software architect, backend engineer, Android / POS engineer, QA, analyst Posisi pada alur SDLC Use Case -> Activity -> Sequence -> Domain Model -> Ar...

## Scope
Shared Kernel, Sales & Checkout, Return & Refund, Shift & Cash Control, Inventory, Reporting & Sync Tujuan Menetapkan bounded context, aggregate root, entity, value object, relasi bisnis, lifecycle, dan invariant utama Target pembaca Software architect, backend engineer, Andro...

## Key Decisions / Core Rules
Aggregate dipilih berdasarkan batas konsistensi; value object dipakai untuk nilai bisnis penting tanpa identity mandiri; external system diperlakukan sebagai integration boundary.

## Detailed Content

### Normalized Source Body
Store / POS System
Domain Model Detail Specifications
Dokumen spesifikasi detail domain model yang diturunkan dari use case, activity,
sequence, dan source of truth project ini.
Format disusun untuk design review, handoff engineering, dan baseline
penurunan architecture, database, implementation, dan test.
Tanggal dokumen: 2026-03-08
Basis sumber: UML-Modeling-Source-of-Truth.txt, Use Case Detail Specifications, Activity Detail Specifications,
Sequence Diagram Detail Specifications, dan .puml domain model yang telah diturunkan di project ini.
Jenis dokumen
Detailed specification / design baseline
Cakupan
Shared Kernel, Sales & Checkout, Return &
Refund, Shift & Cash Control, Inventory, Reporting
& Sync
Tujuan
Menetapkan bounded context, aggregate root,
entity, value object, relasi bisnis, lifecycle, dan
invariant utama
Target pembaca
Software architect, backend engineer, Android /
POS engineer, QA, analyst
Posisi pada alur SDLC
Use Case -> Activity -> Sequence -> Domain Model
-> Architecture -> Database -> Implementation ->
Test

## 1. Tujuan dokumen
- Mendefinisikan model domain Store / POS System secara konsisten, implementable, dan
tetap traceable terhadap artefak sebelumnya pada project ini.
- Menetapkan candidate aggregate root, entity, value object, relasi bisnis, status penting, dan
supporting domain object yang diperlukan untuk desain lanjutan.
- Menjadi baseline untuk arsitektur, database / ERD, boundary aplikasi, desain repository,
contract integration, dan test scenario.
## 2. Ruang lingkup dan asumsi penting
- Domain model memprioritaskan kebenaran bisnis dan consistency boundary, bukan bentuk
layar UI atau struktur tabel persistence.
- Sistem berjalan online-first namun tetap mendukung offline terbatas untuk flow yang
diizinkan policy toko.
- External system seperti Payment Gateway / EDC, Loyalty Service, Identity Service, dan HQ
Store Service berada di luar domain inti dan diperlakukan sebagai integration boundary.
- Supporting object seperti ApprovalDecision, ExceptionReasonRecord, AuditLog, dan
OfflineBacklogItem dimodelkan ulang sebagai reusable supporting domain object agar tidak
terduplikasi lintas aggregate.
- Receipt diperlakukan sebagai artefak transaksi final; walaupun bersifat supporting terhadap
sale flow, ia tetap relevan sebagai entitas domain karena memiliki identity, lifecycle, dan
aturan reprint / delivery.
## 3. Prinsip penyusunan model domain
- Bounded context dipecah agar diagram tetap terbaca, realistis, dan mudah diturunkan ke
implementasi.
- Aggregate root dipilih hanya pada area yang membutuhkan batas konsistensi yang jelas.
- Value object dipakai untuk nilai yang tidak memiliki identity mandiri namun sangat penting
secara bisnis, seperti Money, Quantity, ActorRef, dan ProductRef.
- State penting dimodelkan sebagai enum bila memengaruhi policy, decision path, atau
integritas data.
- Relasi hanya ditampilkan bila membantu kejelasan desain; class diagram tidak dipakai
sebagai dump seluruh class aplikasi.
## 4. Ringkasan bounded context
Bounded Context
Tujuan bisnis
Aggregate root utama
Sumber UC dominan

Shared Kernel
Objek nilai dan support
lintas domain
ApprovalDecision,
ExceptionReasonRecord
, AuditLog,
OfflineBacklogItem
Cross-cutting
Sales & Checkout
Penjualan, keranjang,
member/voucher,
pembayaran, receipt
Sale
UC-02, 03, 04, 05, 06, 07,
08, 09, 10, 12
Return & Refund
Pengembalian barang
dan refund
ReturnTransaction
UC-11, 13, 14, 15, 16
Shift & Cash Control
Shift kasir, cash
movement,
reconciliation, business
day
CashierShift,
BusinessDay
UC-01, 17, 18, 19, 20, 21,
22, 23
Inventory
Balance, receiving,
transfer,
replenishment,
adjustment, cycle count,
damaged goods, labels
InventoryBalance,
ReceivingDocument,
StockTransfer,
ReplenishmentTask,
StockAdjustment,
CycleCount,
DamageDisposition,
PriceLabelJob
UC-24 s.d. UC-33
Reporting & Sync
Laporan operasional,
sync queue, conflict
handling, offline
backlog
StoreOperationalReport
, SyncBatch
UC-34, 35, 36, 37
## 5. Matriks traceability use case ke domain
Kelompok use case
Domain tujuan
Catatan penurunan domain
UC-02 Proses Penjualan; UC-03
Kelola Keranjang; UC-08
Cari/Scan Produk; UC-09 Hitung
Harga Promo Pajak
Sales & Checkout
Menurunkan Sale, SaleLine,
ProductSnapshot, BasketTotals,
PricingDecision
UC-04 Terapkan
Member/Voucher; UC-05 Proses
Pembayaran; UC-06 Terbitkan
Receipt
Sales & Checkout
Menurunkan AppliedBenefit,
Payment, Receipt, aturan
settlement, delivery, dan reprint
UC-07 Suspend/Resume; UC-10
Void; UC-12 Price
Override/Manual Discount
Sales & Checkout + Shared
Kernel
Membutuhkan
SuspendedSaleSnapshot,
ApprovalDecision,
ExceptionReasonRecord,
AuditLog
UC-11 Return/Refund; UC-13
Lookup Receipt; UC-14 Validasi
Return Policy
Return & Refund
Menurunkan
ReturnTransaction, ReturnLine,
OriginalSaleReference,
ReturnPolicyDecision,
RefundPayment
UC-01 Mulai Shift; UC-17 Input
Kas Awal; UC-18 Cash In/Cash
Shift & Cash Control
Menurunkan CashierShift,
OpeningCash, CashMovement

Out
UC-19 Safe Drop; UC-20 Tutup
Shift; UC-21 Rekonsiliasi Kas;
UC-22 Generate X/Z Report;
UC-23 Tutup Hari
Shift & Cash Control
Menurunkan SafeDrop,
CashReconciliation, ShiftReport,
BusinessDay
UC-24 s.d. UC-33 inventori
Inventory
Menurunkan InventoryBalance,
StockLedgerEntry,
ReceivingDocument,
StockTransfer,
ReplenishmentTask,
StockAdjustment, CycleCount,
DamageDisposition,
PriceLabelJob
UC-34 Lihat Laporan
Operasional; UC-35 Sinkronisasi
Data; UC-36 Mode Offline; UC-37
Rekonsiliasi Sync Gagal
Reporting & Sync
Menurunkan
StoreOperationalReport,
KpiSnapshot, SyncBatch,
SyncItem, SyncConflict,
OfflineOperationWindow
## 6. Shared Kernel Domain
Menyediakan reusable building blocks lintas bounded context, terutama untuk identity ringan,
alasan exception, approval, audit trail, dan backlog sinkronisasi.
Aggregate focus: Tidak diposisikan sebagai satu aggregate bisnis tunggal; berisi reusable entity
dan value object lintas context.
Referensi file .puml: store_pos_shared_kernel_domain.puml
Objek domain utama
Nama
Tipe
Peran bisnis
Money
Value Object
Nilai moneter dengan amount +
currency; wajib dipakai
konsisten untuk subtotal,
discount, refund, balance, dan
variance.
Quantity
Value Object
Representasi kuantitas + unit of
measure; dipakai pada line item,
stock balance, receiving,
transfer, dan cycle count.
ActorRef / StoreRef /
TerminalRef / ProductRef /
LocationRef
Value Object
Reference object ringan untuk
menghindari coupling ke
aggregate lain hanya demi
lookup identitas.
ReasonCode
Value Object
Kode alasan terstandar untuk
adjustment, exception,
discrepancy, void, dan
investigasi.
ApprovalDecision
Entity
Catatan keputusan approval

dengan contextType/contextId,
approver, status, timestamp, dan
note.
ExceptionReasonRecord
Entity
Menyimpan alasan bisnis saat
flow keluar dari jalur normal,
misalnya manual override,
return exception, atau stock
discrepancy.
AuditLog
Entity
Jejak audit immutable untuk
event penting domain dan
compliance.
OfflineBacklogItem
Entity
Representasi event lokal yang
belum berhasil dikirim ke HQ /
sistem pusat.
Invariant dan aturan desain
- ApprovalDecision harus mereferensikan context yang jelas dan tidak boleh orphan.
- AuditLog bersifat append-only; update langsung terhadap event historis harus dihindari.
- ReasonCode harus distandardisasi lintas use case agar pelaporan exception tidak ambigu.
- OfflineBacklogItem wajib memiliki aggregateType, aggregateId, eventType, queuedAt, dan
retryCount.
Relasi dan boundary
- Supporting objects ini dipakai lintas domain lain melalui association atau composition
ringan.
- Shared Kernel tidak boleh berkembang menjadi dumping ground untuk service aplikasi
atau DTO teknis.
## 7. Sales & Checkout Domain
Memodelkan transaksi penjualan dari keranjang sampai settlement pembayaran dan issuance
receipt.
Aggregate focus: Sale sebagai aggregate root utama.
Referensi file .puml: store_pos_sales_checkout_domain.puml
Objek domain utama
Nama
Tipe
Peran bisnis
Sale
Aggregate Root
Merepresentasikan satu
transaksi penjualan end-to-end.
Menyimpan status, identitas
toko/terminal/kasir, total,
suspend code, dan outstanding
amount.

SaleLine
Entity
Baris item pada transaksi.
Menyimpan product snapshot,
qty, unit price, discount, tax,
manual override, dan line total.
ProductSnapshot
Value Object
Snapshot atribut produk yang
relevan saat transaksi diproses
agar histori transaksi tidak
pecah jika master data berubah.
BasketTotals
Value Object
Ringkasan subtotal, discount
total, tax total, dan grand total
yang dihitung deterministik.
AppliedBenefit
Entity
Benefit yang diterapkan ke
transaksi, misalnya member,
voucher, promo code, atau point
redemption.
Payment
Entity
Instrumen settlement; bisa lebih
dari satu per sale untuk split
payment.
Receipt
Entity
Output transaksi final; mencatat
nomor receipt, channel,
destination, issue time, dan
reprint count.
PricingDecision
Value Object
Jejak rule version dan hash
keputusan pricing untuk
kebutuhan auditability dan
reproduksi perhitungan.
SuspendedSaleSnapshot
Entity
Snapshot transaksi saat
disuspend dan metadata
resume.
Invariant dan aturan desain
- Sale wajib punya minimal satu SaleLine sebelum status menuju READY_FOR_PAYMENT.
- Grand total = subtotal - discountTotal + taxTotal; outstanding amount tidak boleh negatif.
- Sale berstatus COMPLETED hanya bila outstanding amount nol dan payment yang
diperlukan sudah SUCCESS atau ekuivalen.
- ProductSnapshot harus dibekukan pada saat line disimpan untuk menjaga histori.
- Suspended sale tidak boleh aktif di dua terminal secara bersamaan tanpa mekanisme
lock / resume policy.
- Void atau manual discount di atas threshold harus menghasilkan ApprovalDecision
dan/atau ExceptionReasonRecord sesuai policy.
Relasi dan boundary
- Sale mengomposisi SaleLine, AppliedBenefit, Payment, dan opsional Receipt serta
SuspendedSaleSnapshot.

- Payment dapat terkait ApprovalDecision untuk metode sensitif atau kondisi fallback.
- AuditLog dan OfflineBacklogItem dapat tercipta dari event sale tertentu seperti
completion, void, failed sync, dan retry.
## 8. Return & Refund Domain
Memodelkan pengembalian barang, validasi policy, penentuan disposition inventori, dan
refund ke instrumen yang sah.
Aggregate focus: ReturnTransaction sebagai aggregate root utama.
Referensi file .puml: store_pos_return_refund_domain.puml
Objek domain utama
Nama
Tipe
Peran bisnis
ReturnTransaction
Aggregate Root
Satu transaksi return/refund
lengkap dengan referensi sale
asal, cashier, store, status, dan
total refund.
ReturnLine
Entity
Baris item return, termasuk qty,
kondisi barang, refund amount,
dan disposition inventori.
ReturnPolicyDecision
Value Object
Hasil evaluasi rule return:
eligible/tidak, policy version,
alasan, approval requirement,
dan refund method yang
diizinkan.
RefundPayment
Entity
Realisasi refund ke
cash/card/store credit/alternatif
lain.
OriginalSaleReference
Value Object
Snapshot sale asal yang cukup
untuk validasi purchase date,
payment method, dan receipt.
StoreCredit
Entity
Opsi fallback atau policy-based
refund bila pengembalian ke
metode asal tidak tersedia.
Invariant dan aturan desain
- ReturnTransaction harus memiliki dasar referensi transaksi asal atau policy fallback yang
eksplisit.
- Total refund tidak boleh melebihi nilai yang diizinkan policy dan histori pembelian.
- Disposition inventori harus konsisten dengan kondisi barang: good -> restock;
damaged/uncertain -> quarantine/damage bucket sesuai policy.
- Jika requiresApproval = true, transaksi tidak boleh diselesaikan tanpa ApprovalDecision.

Relasi dan boundary
- ReturnTransaction mengomposisi ReturnLine, ReturnPolicyDecision, dan satu atau lebih
RefundPayment.
- StoreCredit bersifat opsional namun tetap bagian dari outcome domain return.
- ReturnLine dapat memiliki ExceptionReasonRecord untuk kasus abnormal seperti receipt
tidak valid atau manual inspection.
## 9. Shift & Cash Control Domain
Mengatur lifecycle shift kasir, cash drawer, movement uang, reconciliation, reporting shift,
dan business day store.
Aggregate focus: CashierShift dan BusinessDay sebagai aggregate root utama.
Referensi file .puml: store_pos_shift_cash_control_domain.puml
Objek domain utama
Nama
Tipe
Peran bisnis
CashierShift
Aggregate Root
Lifecycle satu shift kasir pada
satu terminal dan satu toko.
OpeningCash
Entity
Kas awal yang diverifikasi ketika
shift dibuka.
CashMovement
Entity
Pergerakan uang selama shift:
cash in, cash out, safe drop,
inflow penjualan tunai, outflow
refund tunai, closing
adjustment.
SafeDrop
Entity
Pengeluaran kas ke lokasi aman
dengan konfirmasi petugas
berwenang.
CashReconciliation
Entity
Perbandingan expected cash vs
physical cash saat penutupan
shift.
ShiftReport
Entity
Laporan operasional periodik
seperti X report dan Z report.
BusinessDay
Aggregate Root
Representasi hari operasional
toko yang menaungi beberapa
shift.
DayOperationalSummary
Value Object
Ringkasan gross/net sales,
return, cash variance, dan closed
shift count.
Invariant dan aturan desain
- Satu shift hanya boleh aktif untuk kombinasi cashier-terminal yang sama pada waktu
yang sama.

- Opening cash harus tercatat sebelum transaksi tunai pertama diproses.
- Cash reconciliation wajib dilakukan sebelum shift final CLOSED.
- Business day tidak boleh CLOSED jika masih ada shift aktif atau issue rekonsiliasi kritis
yang belum diresolusikan.
Relasi dan boundary
- CashierShift mengomposisi OpeningCash, CashMovement, SafeDrop, CashReconciliation,
dan ShiftReport.
- BusinessDay menaungi satu atau lebih CashierShift dan merangkum hasil operasional hari
itu.
- ApprovalDecision diperlukan pada handover atau exception tertentu seperti variance
besar.
## 10. Inventory Domain
Memodelkan ketersediaan stok, jejak ledger, receiving, transfer, replenishment, adjustment,
cycle count, penanganan barang non-sellable, dan pencetakan label.
Aggregate focus: InventoryBalance, ReceivingDocument, StockTransfer, ReplenishmentTask,
StockAdjustment, CycleCount, DamageDisposition, PriceLabelJob.
Referensi file .puml: store_pos_inventory_domain.puml
Objek domain utama
Nama
Tipe
Peran bisnis
CatalogProduct
Entity / Master Reference
Representasi produk referensi
yang direplikasi ke store; bukan
aggregate transaksi.
InventoryBalance
Aggregate Root
Posisi stok per store-product-
location-bucket.
StockLedgerEntry
Entity
Jejak perubahan kuantitas dari
setiap source event: sale, return,
receiving, transfer, adjustment,
cycle count, dll.
ReceivingDocument +
ReceivingLine +
ReceivingDiscrepancy
Aggregate + Entity
Dokumen penerimaan barang,
detail line, dan selisih receiving.
StockTransfer +
StockTransferLine
Aggregate + Entity
Perpindahan stok antar lokasi
atau antar store.
ReplenishmentTask +
ReplenishmentLine
Aggregate + Entity
Pemindahan stok dari backroom
ke rak penjualan.
StockAdjustment +
StockAdjustmentLine
Aggregate + Entity
Perubahan stok manual yang
harus beralasan dan seringkali
butuh approval.

CycleCount + CycleCountLine
Aggregate + Entity
Snapshot dan hasil stock
opname / cycle count.
DamageDisposition +
DamageDispositionLine
Aggregate + Entity
Penanganan stok
rusak/expired/non-sellable.
PriceLabelJob + PriceLabelLine
Aggregate + Entity
Job cetak label harga atau label
rak.
Invariant dan aturan desain
- InventoryBalance adalah current state; StockLedgerEntry adalah historical explanation.
Keduanya tidak boleh dipertukarkan.
- Perubahan stok yang sah harus dapat dijelaskan oleh source event yang jelas dan
menghasilkan ledger entry.
- Receiving discrepancy, stock adjustment besar, dan damage disposition tertentu harus
memiliki reason code dan approval bila policy mewajibkan.
- Cycle count yang menghasilkan variance harus ditutup dengan keputusan jelas: accepted,
double count, atau adjustment.
- Transfer status harus realistis: REQUESTED -> APPROVED -> IN_TRANSIT -> RECEIVED,
tanpa lompatan ilegal kecuali policy exception.
Relasi dan boundary
- CatalogProduct direferensikan oleh banyak aggregate inventori melalui ProductRef.
- InventoryBalance berasosiasi kuat dengan StockLedgerEntry sebagai explanation trail.
- CycleCount dapat menghasilkan StockAdjustment; Receiving dapat menghasilkan
discrepancy record; DamageDisposition memindahkan stok ke bucket non-sellable /
disposal.
## 11. Reporting & Sync Domain
Mengelola laporan operasional toko, orkestrasi sinkronisasi ke HQ, retry, conflict handling,
dan periode operasi offline.
Aggregate focus: StoreOperationalReport dan SyncBatch sebagai aggregate root utama.
Referensi file .puml: store_pos_reporting_sync_domain.puml
Objek domain utama
Nama
Tipe
Peran bisnis
StoreOperationalReport
Aggregate Root
Dokumen laporan operasional
per periode.
KpiSnapshot
Value Object
Snapshot metrik yang
ditampilkan pada laporan.

SyncBatch
Aggregate Root
Satuan kerja sinkronisasi untuk
kumpulan event / aggregate
changes.
SyncItem
Entity
Item individual dalam satu
batch sync.
SyncConflict
Entity
Catatan konflik versi atau
perbedaan state antara lokal
dan HQ.
MasterDataSnapshot
Entity
Snapshot data master yang
diterima dari pusat dengan
versioning.
OfflineOperationWindow
Entity
Periode ketika store beroperasi
dalam mode offline terbatas.
Invariant dan aturan desain
- Setiap SyncItem harus memiliki aggregateType, aggregateId, eventType, status, dan
retryCount yang valid.
- Conflict tidak boleh dianggap selesai tanpa resolutionStatus yang eksplisit.
- Mode offline harus meninggalkan backlog yang dapat direkonsiliasi; data tidak boleh
hilang diam-diam.
- Laporan operasional harus diturunkan dari event domain yang tervalidasi, bukan angka
ad hoc.
Relasi dan boundary
- SyncBatch mengomposisi banyak SyncItem dan dapat menghasilkan SyncConflict.
- SyncBatch menguras OfflineBacklogItem; MasterDataSnapshot direfresh melalui batch
inbound.
- StoreOperationalReport menggunakan KpiSnapshot sebagai representasi ringkas metrik.
## 12. Cross-domain design decisions
- Reference object dipilih untuk mengurangi dependency langsung antar aggregate. Contoh:
Sale menyimpan ProductSnapshot/ProductRef, bukan memegang CatalogProduct aggregate
penuh.
- Shared Kernel menampung concern reusable seperti approval, exception reason, audit, dan
backlog sync. Ini lebih sehat dibanding menduplikasi struktur yang sama di setiap
aggregate.
- InventoryBalance dipisah dari StockLedgerEntry agar current state dan histori tidak
bercampur. Ini krusial untuk auditability dan rekonstruksi stok.

- BusinessDay dipisah dari CashierShift agar closure harian dapat mengagregasi multi-shift
secara eksplisit.
- Sync domain diperlakukan sebagai domain tersendiri, bukan sekadar technical queue,
karena conflict handling, retry, dan offline policy memiliki dampak bisnis langsung.
## 13. Candidate mapping ke persistence dan implementation
Area
Implikasi implementasi
Catatan risiko
Sales
Butuh transaction boundary
kuat di aggregate Sale; Payment
sebaiknya child entity dengan
idempotency key/provider
reference.
Risiko race condition pada split
payment, retry callback, dan
resume transaction.
Return
Perlu referensi sale historis dan
policy version yang terekam.
Risiko refund melebihi hak
pelanggan jika snapshot/lookup
sale lemah.
Shift & Cash
Kas fisik perlu audit trail ketat
dan mutable surface minimal.
Risiko fraud atau unexplained
variance bila audit/event
sourcing parsial.
Inventory
Ledger + balance update harus
konsisten dan dapat di-replay
untuk investigasi.
Risiko negative stock semu,
double posting, atau transfer
ghost state.
Sync
Perlu queue/backlog yang
durable, retry policy, dan
conflict resolution workflow.
Risiko data hilang saat offline
atau loop retry tanpa
observability.
## 14. Review notes dan best practice
- Jangan campur domain model dengan DTO, controller, facade, atau repository. Itu milik
application / data layer, bukan inti bisnis.
- Jangan jadikan class diagram sebagai copy struktur tabel. Domain model harus menjelaskan
alasan bisnis di balik relasi persistence.
- Status enum hanya dipertahankan bila benar-benar memengaruhi rule atau jalur proses.
Status kosmetik sebaiknya dihindari.
- Jika nanti ada kebutuhan state machine yang lebih kompleks, turunkan state diagram khusus
dari aggregate yang relevan, misalnya Sale, ReturnTransaction, StockTransfer, atau
SyncBatch.
- Saat pindah ke ERD, jangan memaksakan semua value object menjadi tabel terpisah.
Sebagian besar cocok sebagai embedded columns.
## 15. Rekomendasi nama file .puml
- store_pos_shared_kernel_domain.puml

- store_pos_sales_checkout_domain.puml
- store_pos_return_refund_domain.puml
- store_pos_shift_cash_control_domain.puml
- store_pos_inventory_domain.puml
- store_pos_reporting_sync_domain.puml
## 16. Penutup
Dokumen ini menempatkan domain model sebagai artefak transisional yang menghubungkan
sequence ke architecture dan database. Fokus utamanya adalah correctness bisnis,
traceability, dan implementability. Dengan pemisahan bounded context yang tegas, desain
dapat diturunkan ke codebase dan persistence model tanpa terjebak over-modeling maupun
technical coupling yang tidak perlu.


## Constraints / Policies
Model domain harus bisnis-driven, bukan UI-driven atau sekadar cerminan tabel persistence.

## Technical Notes
Pisahkan domain entity dari DTO/persistence model; state penting dipertahankan karena memengaruhi policy dan integrity.

## Dependencies / Related Documents
- `uml_modeling_source_of_truth.md`
- `store_pos_use_case_detail_specifications.md`
- `store_pos_activity_detail_specifications.md`
- `store_pos_sequence_detail_specifications.md`
- `cassy_architecture_specification_v1.md`
- `store_pos_erd_specification_v2.md`
- `cassy_migration_script_specification.md`

## Risks / Gaps / Ambiguities
- Tidak ditemukan gap fatal saat ekstraksi. Tetap review ulang bagian tabel/angka jika dokumen ini akan dijadikan baseline implementasi final.

## Reviewer Notes
- Struktur telah dinormalisasi ke Markdown yang konsisten dan siap dipakai untuk design review / engineering handoff.
- Istilah utama dipertahankan sedekat mungkin dengan dokumen sumber untuk menjaga traceability lintas artefak.
- Bila ada konflik antar dokumen, prioritaskan source of truth, artefak yang paling preskriptif, dan baseline yang paling implementable.

## Source Mapping
- Original source: `store_pos_domain_model_detail_specifications_v2.pdf` (PDF, 13 pages)
- Output markdown: `store_pos_domain_model_detail_specifications_v2.md`
- Conversion approach: text extraction -> normalization -> structural cleanup -> standardized documentation wrapper.
- Note: beberapa tabel/list di PDF dapat mengalami wrapping antar baris; esensi dipertahankan, tetapi layout tabel asli tidak dipertahankan 1:1.
