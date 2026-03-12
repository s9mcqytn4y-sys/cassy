# Store POS Sequence Detail Specifications

## Document Overview
Spesifikasi interaksi lintas actor, UI, application/control, repository, database, dan external system untuk 39 use case.

## Purpose
Design review, implementation handoff, test traceability, dan baseline domain/architecture design Disusun secara konsisten dengan alur SDLC: Use Case -> Activity -> Sequence -> Domain Model -> Architecture -> Database -> Implementation -> Test

## Scope
UC-01 s.d. UC-39 sequence detail specifications Sumber Sequence .puml + use case detail specifications + activity detail specifications Tujuan Design review, implementation handoff, test traceability, dan baseline domain/architecture design Disusun secara konsisten dengan alur...

## Key Decisions / Core Rules
Sequence dimodelkan mengikuti boundary actor → UI → application/control → repository/service → database/external system; supporting use case internal tetap dispesifikasikan.

## Detailed Content

### Normalized Source Body
Store / POS System
Sequence Diagram Detail Specifications
Dokumen spesifikasi detail untuk 39 sequence diagram hasil turunan use case dan activity project
ini
Tanggal dokumen
2026-03-08
Scope
UC-01 s.d. UC-39 sequence detail specifications
Sumber
Sequence .puml + use case detail specifications +
activity detail specifications
Tujuan
Design review, implementation handoff, test
traceability, dan baseline domain/architecture design
Disusun secara konsisten dengan alur SDLC: Use Case -> Activity -> Sequence -> Domain Model -> Architecture ->
Database -> Implementation -> Test

## 1. Tujuan dokumen
- 
Menerjemahkan seluruh sequence diagram Store / POS menjadi spesifikasi tekstual yang lebih
operasional, konsisten, dan implementable.
- 
Menjaga traceability dari use case, activity diagram, dan sequence diagram ke desain domain, arsitektur,
database, implementation, dan test.
- 
Menyediakan baseline yang siap dipakai untuk design review, handoff engineering, dan penyusunan test
scenario.
## 2. Asumsi penting
- 
Store / POS System berjalan online-first tetapi tetap memiliki fallback offline terbatas untuk flow tertentu
yang diizinkan policy.
- 
Sequence diagram dimodelkan mengikuti boundary actor -> UI -> application/control -> repository/service
-> database/external system.
- 
Supporting use case internal seperti autentikasi, pricing, audit log, dan rekonsiliasi tetap dispesifikasikan
karena berdampak langsung ke implementasi dan pengujian.
- 
Dokumen ini memprioritaskan correctness bisnis dan implementability, bukan sekadar narasi diagram.
## 3. Cara membaca dokumen
- 
Setiap bagian UC berisi ringkasan konteks bisnis, daftar lifeline/participant, alur interaksi utama, alternatif,
exception, serta catatan implementasi.
- 
Main interaction flow diturunkan langsung dari message flow pada file .puml agar tetap traceable ke
artefak sequence sumber.
- 
Istilah aktor, service, repository, dan external system disamakan dengan istilah di project ini untuk
menghindari ambiguity lintas artefak.
## 4. Katalog sequence specifications
ID
Nama Use Case
File Sequence
Catatan
UC-01
Mulai Shift Kasir
uc01_mulai_shift_kasir_seq
uence.puml
Primary / operational
UC-02
Proses Penjualan
uc02_proses_penjualan_se
quence.puml
Primary / operational
UC-03
Kelola Keranjang
uc03_kelola_keranjang_se
quence.puml
Primary / operational
UC-04
Terapkan Member /
Voucher
uc04_terapkan_member_v
oucher_sequence.puml
Primary / operational
UC-05
Proses Pembayaran
uc05_proses_pembayaran
_sequence.puml
Primary / operational
UC-06
Terbitkan Receipt
uc06_terbitkan_receipt_seq
Primary / operational

ID
Nama Use Case
File Sequence
Catatan
uence.puml
UC-07
Suspend / Resume
Transaksi
uc07_suspend_resume_tra
nsaksi_sequence.puml
Primary / operational
UC-08
Cari / Scan Produk
uc08_cari_scan_produk_se
quence.puml
Primary / operational
UC-09
Hitung Harga, Promo,
Pajak
uc09_hitung_harga_promo
_pajak_sequence.puml
Primary / operational
UC-10
Void Item / Transaksi
uc10_void_item_transaksi_
sequence.puml
Primary / operational
UC-11
Return / Refund
uc11_return_refund_seque
nce.puml
Primary / operational
UC-12
Price Override / Manual
Discount
uc12_price_override_manu
al_discount_sequence.pum
l
Primary / operational
UC-13
Lookup Receipt
uc13_lookup_receipt_sequ
ence.puml
Supporting
UC-14
Validasi Return Policy
uc14_validasi_return_policy
_sequence.puml
Supporting
UC-15
Supervisor Approval
uc15_supervisor_approval_
sequence.puml
Supporting
UC-16
Catat Alasan Exception
uc16_catat_alasan_excepti
on_sequence.puml
Primary / operational
UC-17
Input Kas Awal
uc17_input_kas_awal_sequ
ence.puml
Primary / operational
UC-18
Cash In / Cash Out
uc18_cash_in_cash_out_s
equence.puml
Primary / operational
UC-19
Safe Drop
uc19_safe_drop_sequence.
puml
Primary / operational
UC-20
Tutup Shift
uc20_tutup_shift_sequence
.puml
Primary / operational
UC-21
Rekonsiliasi Kas
uc21_rekonsiliasi_kas_seq
uence.puml
Primary / operational

ID
Nama Use Case
File Sequence
Catatan
UC-22
Generate X / Z Report
uc22_generate_x_z_report
_sequence.puml
Primary / operational
UC-23
Tutup Hari
uc23_tutup_hari_sequence.
puml
Primary / operational
UC-24
Cek Stok & Movement
uc24_cek_stok_movement
_sequence.puml
Primary / operational
UC-25
Receiving Barang
uc25_receiving_barang_se
quence.puml
Primary / operational
UC-26
Transfer Stok
uc26_transfer_stok_sequen
ce.puml
Primary / operational
UC-27
Replenishment Rak
uc27_replenishment_rak_s
equence.puml
Primary / operational
UC-28
Stock Adjustment
uc28_stock_adjustment_se
quence.puml
Primary / operational
UC-29
Stock Opname / Cycle
Count
uc29_stock_opname_cycle
_count_sequence.puml
Primary / operational
UC-30
Kelola Barang Rusak /
Expired
uc30_kelola_barang_rusak
_expired_sequence.puml
Primary / operational
UC-31
Verifikasi Delivery / PO
uc31_verifikasi_delivery_po
_sequence.puml
Primary / operational
UC-32
Catat Selisih Receiving
uc32_catat_selisih_receivin
g_sequence.puml
Primary / operational
UC-33
Cetak Label Harga / Rak
uc33_cetak_label_harga_ra
k_sequence.puml
Primary / operational
UC-34
Lihat Laporan Operasional
Toko
uc34_lihat_laporan_operasi
onal_toko_sequence.puml
Primary / operational
UC-35
Sinkronisasi Data Toko
uc35_sinkronisasi_data_tok
o_sequence.puml
Primary / operational
UC-36
Mode Operasi Offline
uc36_mode_operasi_offline
_sequence.puml
Primary / operational
UC-37
Rekonsiliasi Sync Gagal
uc37_rekonsiliasi_sync_ga
Primary / operational

ID
Nama Use Case
File Sequence
Catatan
gal_sequence.puml
UC-38
Autentikasi & Validasi Role
uc38_autentikasi_validasi_r
ole_sequence.puml
Supporting
UC-39
Catat Audit Log
uc39_catat_audit_log_sequ
ence.puml
Supporting
UC-01 - Mulai Shift Kasir
Membuka shift kerja kasir sehingga terminal POS siap
dipakai untuk transaksi pada hari operasional.
Primary actor
Kasir
Supporting actors
Identity Service, Supervisor (jika ada pengecualian),
Store Device
Trigger
Kasir memilih menu mulai shift.
Preconditions
- Kasir terdaftar dan memiliki role aktif. - Terminal POS
online atau dapat bekerja dalam mode offline yang
diizinkan. - Shift sebelumnya pada terminal sudah
ditutup atau diambil alih sesuai policy.
Postconditions
- Shift aktif terbentuk. - Opening cash tercatat.
Source sequence file
uc01_mulai_shift_kasir_sequence.puml
Traceability
Use case spec + activity spec
(uc01_mulai_shift_kasir_activity.puml)
Interaction participants
Participant
Tipe
Peran pada sequence
Kasir
Actor
Memicu atau menerima hasil bisnis
dari flow.
POS UI
UI/Boundary
Mewakili UI/boundary yang
berinteraksi langsung dengan actor.
Shift Controller
Application/Service
Menjalankan orkestrasi use case,
validasi, atau layanan aplikasi.
Auth Service
Application/Service
Menjalankan orkestrasi use case,
validasi, atau layanan aplikasi.

Participant
Tipe
Peran pada sequence
Approval Service
Application/Service
Menjalankan orkestrasi use case,
validasi, atau layanan aplikasi.
Shift Repository
Repository/External Entity
Mewakili repository atau sistem
eksternal yang terlibat.
Store DB
Database
Penyimpanan data transaksi, policy,
audit, atau ledger.
Identity Service
Repository/External Entity
Mewakili repository atau sistem
eksternal yang terlibat.
Main interaction flow
1.
Kasir -> POS UI: Login + pilih mulai shift
2.
POS UI -> Shift Controller: startShift(credentials, terminalId)
3.
Shift Controller -> Auth Service: authenticateAndAuthorize(credentials, ROLE_CASHIER)
Alternatif / keputusan
- 
Cabang 1: identity service online
4.
Auth Service -> Identity Service: verify credentials and role
5.
Identity Service -> Auth Service: valid / invalid
- 
Cabang 2: offline credential cache allowed
6.
Auth Service -> Store DB: validate cached credential
7.
Store DB -> Auth Service: valid / invalid
8.
Auth Service -> Shift Controller: auth result
Alternatif / keputusan
- 
Cabang 1: auth invalid
9.
Shift Controller -> POS UI: reject start shift
10.
POS UI -> Kasir: tampilkan error autentikasi
- 
Cabang 2: auth valid
11.
Shift Controller -> Shift Repository: findActiveShiftByTerminal(terminalId)
12.
Shift Repository -> Store DB: select active shift
13.
Store DB -> Shift Repository: active shift / none
14.
Shift Repository -> Shift Controller: conflict status
Alternatif / keputusan
- 
Cabang 1: shift conflict
15.Shift Controller -> POS UI: request supervisor handling
16.POS UI -> Kasir: input opening cash + panggil supervisor
17.Kasir -> POS UI: submit openingCash

18.POS UI -> Approval Service: request shift handover approval
19.Approval Service -> POS UI: approved / rejected
Alternatif / keputusan
- 
Cabang 1: rejected
20.
POS UI -> Kasir: shift cannot be opened
- 
Cabang 2: approved
21.
POS UI -> Shift Controller: continue start shift(openingCash)
22.
Shift Controller -> Shift Repository: createShift(openingCash, cashierId, terminalId)
23.
Shift Repository -> Store DB: insert shift and opening cash
24.
Store DB -> Shift Repository: saved
25.
Shift Repository -> Shift Controller: shift created
26.
Shift Controller -> POS UI: shift active
27.
POS UI -> Kasir: terminal siap transaksi
- 
Cabang 2: no conflict
28.POS UI -> Kasir: input opening cash
29.Kasir -> POS UI: submit openingCash
30.POS UI -> Shift Controller: continue start shift(openingCash)
Alternatif / keputusan
- 
Cabang 1: opening cash out of policy
31.
Shift Controller -> Approval Service: request override approval
32.
Approval Service -> Shift Controller: approved / rejected
Alternatif / keputusan
- 
Cabang 1: approved or within policy
33.
Shift Controller -> Shift Repository: createShift(openingCash, cashierId, terminalId)
34.
Shift Repository -> Store DB: insert shift and opening cash
35.
Store DB -> Shift Repository: saved
36.
Shift Repository -> Shift Controller: shift created
37.
Shift Controller -> POS UI: shift active
38.
POS UI -> Kasir: terminal siap transaksi
- 
Cabang 2: rejected
39.
Shift Controller -> POS UI: reject invalid opening cash
40.
POS UI -> Kasir: koreksi nominal
Alternate and exception considerations
Alternate flow
- 
Autentikasi gagal, sistem menolak pembukaan shift.
- 
Jika ada shift lama belum ditutup, sistem meminta proses handover atau supervisor handling.
Exception flow
- 
Identity service tidak tersedia dan offline login tidak diizinkan -> shift tidak dapat dibuka.

- 
Nominal opening cash di luar policy -> perlu koreksi atau approval.
Business rules and implementation notes
- 
Satu kasir tidak boleh memiliki dua shift aktif pada terminal berbeda tanpa policy eksplisit.
- 
Opening cash wajib diinput pada awal shift sesuai include UC-17.
- 
Sequence ini menyentuh persistence layer; pastikan boundary transaksi, idempotency, dan partial failure
handling didefinisikan jelas pada implementation.
- 
Ada dependency ke external service/integration; retry policy, timeout, fallback, dan audit trail tidak boleh
dibiarkan implisit.
- 
Flow mengandung gate approval; pastikan authorization, reason capture, dan actor accountability tercatat
secara persistable.

UC-02 - Proses Penjualan
Menyelesaikan transaksi penjualan barang hingga
pembayaran diterima dan penjualan tercatat sah.
Primary actor
Kasir
Supporting actors
Pelanggan, Payment Gateway / EDC, Loyalty Service,
Store Device
Trigger
Pelanggan ingin membeli barang dan kasir memulai
transaksi baru.
Preconditions
- Shift kasir aktif. - POS memiliki akses ke katalog
produk dan pricing rule terbaru atau cache offline yang
valid.
Postconditions
- Transaksi penjualan tersimpan. - Stok dan ledger
penjualan ter-update atau ditandai untuk sinkronisasi. -
Receipt tersedia.
Source sequence file
uc02_proses_penjualan_sequence.puml
Traceability
Use case spec + activity spec
(uc02_proses_penjualan_activity.puml)
Interaction participants
Participant
Tipe
Peran pada sequence
Pelanggan
Actor
Memicu atau menerima hasil bisnis
dari flow.
Kasir
Actor
Memicu atau menerima hasil bisnis
dari flow.
POS UI
UI/Boundary
Mewakili UI/boundary yang
berinteraksi langsung dengan actor.
Sales Controller
Application/Service
Menjalankan orkestrasi use case,
validasi, atau layanan aplikasi.
Cart Service
Application/Service
Menjalankan orkestrasi use case,
validasi, atau layanan aplikasi.
Pricing Service
Application/Service
Menjalankan orkestrasi use case,
validasi, atau layanan aplikasi.
Loyalty Service Adapter
Application/Service
Menjalankan orkestrasi use case,
validasi, atau layanan aplikasi.
Payment Service
Application/Service
Menjalankan orkestrasi use case,
validasi, atau layanan aplikasi.

Participant
Tipe
Peran pada sequence
Receipt Service
Application/Service
Menjalankan orkestrasi use case,
validasi, atau layanan aplikasi.
Audit Service
Application/Service
Menjalankan orkestrasi use case,
validasi, atau layanan aplikasi.
Sales Repository
Repository/External Entity
Mewakili repository atau sistem
eksternal yang terlibat.
Store DB
Database
Penyimpanan data transaksi, policy,
audit, atau ledger.
Payment Gateway / EDC
Repository/External Entity
Mewakili repository atau sistem
eksternal yang terlibat.
Loyalty Service
Repository/External Entity
Mewakili repository atau sistem
eksternal yang terlibat.
Main interaction flow
## 41. Kasir -> POS UI: Buat transaksi baru
## 42. POS UI -> Sales Controller: createSale(shiftId)
## 43. Sales Controller -> Sales Repository: createDraftSale(shiftId)
## 44. Sales Repository -> Store DB: insert sale draft
## 45. Store DB -> Sales Repository: saleId
## 46. Sales Repository -> Sales Controller: draft sale
## 47. Sales Controller -> POS UI: sale draft ready
Loop: untuk setiap item yang dibeli
48.
Kasir -> POS UI: scan / cari produk + qty
49.
POS UI -> Cart Service: addOrUpdateItem(saleId, item, qty)
50.
Cart Service -> Pricing Service: recalculate(saleId)
51.
Pricing Service -> Store DB: read price, promo, tax rule
52.
Store DB -> Pricing Service: pricing inputs
53.
Pricing Service -> Cart Service: basket totals
54.
Cart Service -> Sales Repository: save cart snapshot
55.
Sales Repository -> Store DB: upsert sale lines and totals
56.
Store DB -> Sales Repository: saved
57.
Sales Repository -> Cart Service: ok
58.
Cart Service -> POS UI: updated basket
Opsional: pelanggan minta member atau voucher
59.
Pelanggan -> Kasir: berikan member/voucher
60.
Kasir -> POS UI: apply benefit(memberOrVoucher)
61.
POS UI -> Loyalty Service Adapter: validateBenefit(saleId, memberOrVoucher)
62.
Loyalty Service Adapter -> Loyalty Service: validate member/voucher
63.
Loyalty Service -> Loyalty Service Adapter: eligible / ineligible

Alternatif / keputusan
- Cabang 1: benefit valid
64.
Loyalty Service Adapter -> Pricing Service: recalculate with benefit
65.
Pricing Service -> Loyalty Service Adapter: updated totals
66.
Loyalty Service Adapter -> Sales Repository: save benefit and totals
67.
Sales Repository -> Store DB: update sale benefit
68.
Store DB -> Sales Repository: saved
69.
Sales Repository -> Loyalty Service Adapter: ok
70.
Loyalty Service Adapter -> POS UI: benefit applied
- Cabang 2: benefit invalid
71.
Loyalty Service Adapter -> POS UI: reject benefit
## 72. Kasir -> POS UI: konfirmasi total dan proses pembayaran
## 73. POS UI -> Payment Service: pay(saleId, paymentRequest)
Alternatif / keputusan
- 
Cabang 1: pembayaran cash
74.
Payment Service -> Sales Repository: mark cash received
75.
Sales Repository -> Store DB: update payment and change
76.
Store DB -> Sales Repository: saved
77.
Sales Repository -> Payment Service: payment success
- 
Cabang 2: pembayaran non-cash
78.
Payment Service -> Payment Gateway / EDC: authorize(amount, method)
79.
Payment Gateway / EDC -> Payment Service: success / failed / pending
Alternatif / keputusan
- 
Cabang 1: success
80.Payment Service -> Sales Repository: save provider reference
81.Sales Repository -> Store DB: update payment success
82.Store DB -> Sales Repository: saved
- 
Cabang 2: failed or pending
83.Payment Service -> POS UI: payment failed / pending
Alternatif / keputusan
- 
Cabang 1: payment success
84.
Payment Service -> Sales Controller: payment valid
85.
Sales Controller -> Sales Repository: finalizeSale(saleId)
86.
Sales Repository -> Store DB: commit sale, stock ledger, sale ledger
87.
Store DB -> Sales Repository: finalized
88.
Sales Repository -> Sales Controller: sale finalized
89.
Sales Controller -> Receipt Service: issueReceipt(saleId)
90.
Receipt Service -> Store DB: read receipt payload
91.
Store DB -> Receipt Service: receipt data
92.
Receipt Service -> POS UI: receipt ready

93.
Sales Controller -> Audit Service: log sale finalized
94.
Audit Service -> Store DB: insert audit log
95.
Store DB -> Audit Service: saved
96.
Audit Service -> Sales Controller: ok
97.
Sales Controller -> POS UI: transaction completed
98.
POS UI -> Kasir: tampilkan sukses + receipt
99.
POS UI -> Pelanggan: serahkan receipt
- 
Cabang 2: payment failed / pending
100.
Payment Service -> POS UI: sale remains unpaid/pending
101.
POS UI -> Kasir: tindak lanjuti retry atau suspend
Alternate and exception considerations
Alternate flow
- 
Kasir mengubah kuantitas atau menghapus item sebelum pembayaran.
- 
Transaksi disuspend untuk dilanjutkan nanti.
- 
Receipt diterbitkan dalam bentuk digital bila printer tidak digunakan.
Exception flow
- 
Produk tidak ditemukan atau barcode tidak valid.
- 
Perhitungan harga gagal karena master data corrupt -> transaksi ditahan.
- 
Pembayaran gagal -> transaksi tetap unpaid/pending dan tidak selesai.
Business rules and implementation notes
- 
Penjualan tidak boleh final tanpa status payment yang valid.
- 
UC-03, UC-05, dan UC-39 bersifat wajib melalui include.
- 
Sequence ini menyentuh persistence layer; pastikan boundary transaksi, idempotency, dan partial failure
handling didefinisikan jelas pada implementation.
- 
Ada dependency ke external service/integration; retry policy, timeout, fallback, dan audit trail tidak boleh
dibiarkan implisit.
- 
Audit logging diperlakukan sebagai kebutuhan implementasi, bukan kosmetik; event penting harus
memiliki correlation id dan actor context.

UC-03 - Kelola Keranjang
Memelihara isi transaksi penjualan aktif sebelum
finalisasi pembayaran.
Primary actor
Kasir
Supporting actors
Store Device
Trigger
Kasir menambah, mengubah, atau menghapus item
dari keranjang.
Preconditions
- Ada transaksi penjualan aktif.
Postconditions
- Keranjang aktif ter-update.
Source sequence file
uc03_kelola_keranjang_sequence.puml
Traceability
Use case spec + activity spec
(uc03_kelola_keranjang_activity.puml)
Interaction participants
Participant
Tipe
Peran pada sequence
Kasir
Actor
Memicu atau menerima hasil bisnis
dari flow.
POS UI
UI/Boundary
Mewakili UI/boundary yang
berinteraksi langsung dengan actor.
Cart Service
Application/Service
Menjalankan orkestrasi use case,
validasi, atau layanan aplikasi.
Pricing Service
Application/Service
Menjalankan orkestrasi use case,
validasi, atau layanan aplikasi.
Sales Repository
Repository/External Entity
Mewakili repository atau sistem
eksternal yang terlibat.
Store DB
Database
Penyimpanan data transaksi, policy,
audit, atau ledger.
Main interaction flow
102.
Kasir -> POS UI: tambah / ubah / hapus item
103.
POS UI -> Cart Service: maintainCart(saleId, action, payload)
Alternatif / keputusan
- 
Cabang 1: tambah item
104.
Cart Service -> Store DB: find sellable item and stock

105.
Store DB -> Cart Service: item valid / invalid
Alternatif / keputusan
- 
Cabang 1: item valid
106.
Cart Service -> Sales Repository: upsert sale line
107.
Sales Repository -> Store DB: insert or update line
108.
Store DB -> Sales Repository: saved
- 
Cabang 2: item invalid or stock zero
109.
Cart Service -> POS UI: reject item
- 
Cabang 2: ubah qty atau hapus item
110.
Cart Service -> Sales Repository: updateLine(saleId, lineId, qty)
111.
Sales Repository -> Store DB: update or delete line
112.
Store DB -> Sales Repository: saved
Opsional: item ditahan untuk konfirmasi harga
## 113. Cart Service -> POS UI: mark item pending confirmation
114.
Cart Service -> Pricing Service: recalculate(saleId)
115.
Pricing Service -> Store DB: read cart and pricing rule
116.
Store DB -> Pricing Service: pricing inputs
117.
Pricing Service -> Cart Service: updated totals
118.
Cart Service -> Sales Repository: save totals
119.
Sales Repository -> Store DB: update basket totals
120.
Store DB -> Sales Repository: saved
121.
Cart Service -> POS UI: cart updated
Alternate and exception considerations
Alternate flow
- 
- Kasir menahan item tertentu untuk konfirmasi harga.
Exception flow
- 
- Item tidak dapat dijual atau stok nol.
Business rules and implementation notes
- 
- Semua perubahan keranjang harus memicu pricing ulang.
- 
Sequence ini menyentuh persistence layer; pastikan boundary transaksi, idempotency, dan partial failure
handling didefinisikan jelas pada implementation.
- 
Ada dependency ke external service/integration; retry policy, timeout, fallback, dan audit trail tidak boleh
dibiarkan implisit.

UC-04 - Terapkan Member / Voucher
Menerapkan benefit member, poin, atau voucher yang
sah ke transaksi aktif.
Primary actor
Kasir / Pelanggan
Supporting actors
Loyalty Service
Trigger
Pelanggan meminta benefit loyalty atau menggunakan
voucher.
Preconditions
- Ada transaksi aktif. - Member ID, nomor telepon, QR
voucher, atau kode promo tersedia.
Postconditions
- Benefit loyalty/voucher tercermin pada transaksi aktif
atau ditolak dengan alasan yang jelas.
Source sequence file
uc04_terapkan_member_voucher_sequence.puml
Traceability
Use case spec + activity spec (n/a)
Interaction participants
Participant
Tipe
Peran pada sequence
Pelanggan
Actor
Memicu atau menerima hasil bisnis
dari flow.
Kasir
Actor
Memicu atau menerima hasil bisnis
dari flow.
POS UI
UI/Boundary
Mewakili UI/boundary yang
berinteraksi langsung dengan actor.
Loyalty Use Case
Application/Service
Menjalankan orkestrasi use case,
validasi, atau layanan aplikasi.
Pricing Service
Application/Service
Menjalankan orkestrasi use case,
validasi, atau layanan aplikasi.
Loyalty Service
Repository/External Entity
Mewakili repository atau sistem
eksternal yang terlibat.
Sales Repository
Repository/External Entity
Mewakili repository atau sistem
eksternal yang terlibat.
Store DB
Database
Penyimpanan data transaksi, policy,
audit, atau ledger.

Main interaction flow
122.
Pelanggan -> Kasir: minta gunakan member/voucher
123.
Kasir -> POS UI: pilih apply member/voucher
124.
POS UI -> Loyalty Use Case: applyBenefit(saleId, credential)
125.
Loyalty Use Case -> Loyalty Service: validate member/voucher
Alternatif / keputusan
- 
Cabang 1: service available
126.
Loyalty Service -> Loyalty Use Case: valid / invalid / need extra verification
- 
Cabang 2: fallback cache allowed
127.
Loyalty Use Case -> Store DB: validate local cache/policy
128.
Store DB -> Loyalty Use Case: valid / invalid
Alternatif / keputusan
- 
Cabang 1: need extra verification
129.
POS UI -> Pelanggan: minta OTP/verifikasi tambahan
130.
Pelanggan -> POS UI: kirim OTP
131.
POS UI -> Loyalty Use Case: confirm verification
132.
Loyalty Use Case -> Loyalty Service: verify OTP
133.
Loyalty Service -> Loyalty Use Case: success / failed
Alternatif / keputusan
- 
Cabang 1: benefit valid
134.
Loyalty Use Case -> Pricing Service: recalculate with loyalty benefit
135.
Pricing Service -> Store DB: read basket and pricing data
136.
Store DB -> Pricing Service: pricing inputs
137.
Pricing Service -> Loyalty Use Case: updated totals and points
138.
Loyalty Use Case -> Sales Repository: save benefit application
139.
Sales Repository -> Store DB: update loyalty/voucher usage on sale
140.
Store DB -> Sales Repository: saved
141.
Sales Repository -> Loyalty Use Case: ok
142.
Loyalty Use Case -> POS UI: benefit applied
143.
POS UI -> Kasir: tampilkan total baru
144.
Kasir -> Pelanggan: konfirmasi benefit
- 
Cabang 2: benefit invalid
145.
Loyalty Use Case -> POS UI: reject with reason
146.
POS UI -> Kasir: tampilkan alasan penolakan
Alternate and exception considerations
Alternate flow
- 
Voucher valid tetapi hanya sebagian item yang eligible.
- 
Member ditemukan tetapi reward redemption memerlukan OTP atau verifikasi tambahan.
Exception flow

- 
Voucher kadaluwarsa, sudah dipakai, atau tidak eligible.
- 
Loyalty service tidak tersedia dan tidak ada fallback offline -> benefit tidak diterapkan.
Business rules and implementation notes
- 
Satu voucher biasanya sekali pakai kecuali tipe reusable.
- 
Perubahan benefit harus memicu recalculation via UC-09.
- 
Sequence ini menyentuh persistence layer; pastikan boundary transaksi, idempotency, dan partial failure
handling didefinisikan jelas pada implementation.
- 
Ada dependency ke external service/integration; retry policy, timeout, fallback, dan audit trail tidak boleh
dibiarkan implisit.

UC-05 - Proses Pembayaran
Menerima pembayaran pelanggan dan menghasilkan
status payment yang sah untuk menyelesaikan
transaksi.
Primary actor
Kasir
Supporting actors
Pelanggan, Payment Gateway / EDC
Trigger
Kasir menekan proses pembayaran.
Preconditions
- Ada transaksi aktif dengan total final. - Metode
pembayaran yang dipilih tersedia di toko.
Postconditions
- Status payment tersimpan sebagai success, pending,
atau failed sesuai hasil. - Referensi payment provider
tersimpan bila applicable.
Source sequence file
uc05_proses_pembayaran_sequence.puml
Traceability
Use case spec + activity spec
(uc05_proses_pembayaran_activity.puml)
Interaction participants
Participant
Tipe
Peran pada sequence
Kasir
Actor
Memicu atau menerima hasil bisnis
dari flow.
Pelanggan
Actor
Memicu atau menerima hasil bisnis
dari flow.
POS UI
UI/Boundary
Mewakili UI/boundary yang
berinteraksi langsung dengan actor.
Payment Use Case
Application/Service
Menjalankan orkestrasi use case,
validasi, atau layanan aplikasi.
Payment Gateway / EDC
Repository/External Entity
Mewakili repository atau sistem
eksternal yang terlibat.
Sales Repository
Repository/External Entity
Mewakili repository atau sistem
eksternal yang terlibat.
Store DB
Database
Penyimpanan data transaksi, policy,
audit, atau ledger.
Main interaction flow
147.
Kasir -> POS UI: pilih metode pembayaran
148.
POS UI -> Payment Use Case: startPayment(saleId, method)

149.
Payment Use Case -> Store DB: read final totals
150.
Store DB -> Payment Use Case: total due
151.
Payment Use Case -> POS UI: show payable amount
Alternatif / keputusan
- 
Cabang 1: metode cash
152.
Kasir -> POS UI: input nominal diterima
153.
POS UI -> Payment Use Case: confirmCash(receivedAmount)
Alternatif / keputusan
- 
Cabang 1: nominal cukup
154.
Payment Use Case -> Sales Repository: save cash payment and change
155.
Sales Repository -> Store DB: update payment success
156.
Store DB -> Sales Repository: saved
157.
Sales Repository -> Payment Use Case: success
158.
Payment Use Case -> POS UI: payment success
- 
Cabang 2: nominal kurang
159.
Payment Use Case -> POS UI: reject insufficient cash
- 
Cabang 2: metode non-cash
160.
POS UI -> Payment Use Case: request authorization
161.
Payment Use Case -> Payment Gateway / EDC: authorize(amount, method, terminalId)
Alternatif / keputusan
- 
Cabang 1: authorized
162.
Payment Gateway / EDC -> Payment Use Case: authorization approved + providerRef
163.
Payment Use Case -> Sales Repository: save providerRef and success status
164.
Sales Repository -> Store DB: update payment success
165.
Store DB -> Sales Repository: saved
166.
Sales Repository -> Payment Use Case: success
167.
Payment Use Case -> POS UI: payment success
- 
Cabang 2: declined
168.
Payment Gateway / EDC -> Payment Use Case: authorization declined
169.
Payment Use Case -> POS UI: payment failed
- 
Cabang 3: timeout or uncertain
170.
Payment Gateway / EDC -> Payment Use Case: timeout / unknown
171.
Payment Use Case -> Sales Repository: save pending investigation
172.
Sales Repository -> Store DB: update payment pending
173.
Store DB -> Sales Repository: saved
174.
Sales Repository -> Payment Use Case: pending saved
175.
Payment Use Case -> POS UI: payment pending investigation
Opsional: split payment

Loop: hingga outstanding = 0 atau gagal
176.
POS UI -> Payment Use Case: apply next instrument(amount)
177.
Payment Use Case -> POS UI: instrument result
178.
POS UI -> Pelanggan: inform payment outcome
Alternate and exception considerations
Alternate flow
- 
Split payment digunakan dan sistem memproses beberapa instrumen hingga total lunas.
- 
Pembayaran cashless membutuhkan retry pada terminal EDC.
Exception flow
- 
Authorization declined.
- 
Timeout dari gateway/EDC menyebabkan status tidak pasti -> transaksi ditandai pending investigation.
- 
Nominal pembayaran kurang dari total -> sistem menolak finalisasi.
Business rules and implementation notes
- 
Finalisasi penjualan hanya boleh saat total outstanding = 0 dan status payment valid.
- 
Semua pajak/promo harus dihitung sebelum payment dimulai.
- 
Sequence ini menyentuh persistence layer; pastikan boundary transaksi, idempotency, dan partial failure
handling didefinisikan jelas pada implementation.
- 
Ada dependency ke external service/integration; retry policy, timeout, fallback, dan audit trail tidak boleh
dibiarkan implisit.

UC-06 - Terbitkan Receipt
Menyediakan bukti transaksi kepada pelanggan
setelah transaksi sah selesai.
Primary actor
Kasir
Supporting actors
Store Device
Trigger
Sistem menyelesaikan transaksi penjualan atau
return/refund.
Preconditions
- Transaksi final dengan payment sah tersedia.
Postconditions
- Receipt tersedia atau kegagalan cetak tercatat.
Source sequence file
uc06_terbitkan_receipt_sequence.puml
Traceability
Use case spec + activity spec
(uc06_terbitkan_receipt_activity.puml)
Interaction participants
Participant
Tipe
Peran pada sequence
Kasir
Actor
Memicu atau menerima hasil bisnis
dari flow.
POS UI
UI/Boundary
Mewakili UI/boundary yang
berinteraksi langsung dengan actor.
Receipt Service
Application/Service
Menjalankan orkestrasi use case,
validasi, atau layanan aplikasi.
Receipt Repository
Repository/External Entity
Mewakili repository atau sistem
eksternal yang terlibat.
Printer / Digital Channel
Repository/External Entity
Mewakili repository atau sistem
eksternal yang terlibat.
Store DB
Database
Penyimpanan data transaksi, policy,
audit, atau ledger.
Main interaction flow
179.
POS UI -> Receipt Service: issueReceipt(transactionId, preferredChannel)
180.
Receipt Service -> Receipt Repository: buildReceiptPayload(transactionId)
181.
Receipt Repository -> Store DB: read finalized transaction
182.
Store DB -> Receipt Repository: receipt data
183.
Receipt Repository -> Receipt Service: payload ready
Alternatif / keputusan

- 
Cabang 1: printed receipt
184.
Receipt Service -> Printer / Digital Channel: print(payload)
Alternatif / keputusan
- 
Cabang 1: print success
185.
Printer / Digital Channel -> Receipt Service: printed
- 
Cabang 2: printer error
186.
Printer / Digital Channel -> Receipt Service: failed
- 
Cabang 2: digital receipt
187.
Receipt Service -> Printer / Digital Channel: sendDigital(payload)
188.
Printer / Digital Channel -> Receipt Service: sent / failed
189.
Receipt Service -> Receipt Repository: save receipt reference(status, channel)
190.
Receipt Repository -> Store DB: insert receipt record
191.
Store DB -> Receipt Repository: saved
192.
Receipt Repository -> Receipt Service: ok
193.
Receipt Service -> POS UI: receipt result
194.
POS UI -> Kasir: receipt tersedia atau error tercatat
Alternate and exception considerations
Alternate flow
- 
- Reprint dilakukan kemudian melalui receipt lookup.
Exception flow
- 
- Printer gagal atau kehabisan kertas.
Business rules and implementation notes
- 
- Receipt hanya untuk transaksi final.
- 
Sequence ini menyentuh persistence layer; pastikan boundary transaksi, idempotency, dan partial failure
handling didefinisikan jelas pada implementation.
- 
Ada dependency ke external service/integration; retry policy, timeout, fallback, dan audit trail tidak boleh
dibiarkan implisit.

UC-07 - Suspend / Resume Transaksi
Menunda transaksi aktif tanpa kehilangan state dan
melanjutkannya kembali di terminal yang diizinkan.
Primary actor
Kasir
Supporting actors
Supervisor (opsional)
Trigger
Kasir memilih suspend atau membuka daftar transaksi
tertunda untuk resume.
Preconditions
- Ada transaksi aktif yang belum dibayar. - Policy store
mengizinkan suspend.
Postconditions
- Transaksi berada pada state suspended atau
resumed dengan konsisten.
Source sequence file
uc07_suspend_resume_transaksi_sequence.puml
Traceability
Use case spec + activity spec (n/a)
Interaction participants
Participant
Tipe
Peran pada sequence
Kasir
Actor
Memicu atau menerima hasil bisnis
dari flow.
POS UI
UI/Boundary
Mewakili UI/boundary yang
berinteraksi langsung dengan actor.
Suspend Transaction Service
Application/Service
Menjalankan orkestrasi use case,
validasi, atau layanan aplikasi.
Pricing Service
Application/Service
Menjalankan orkestrasi use case,
validasi, atau layanan aplikasi.
Sales Repository
Repository/External Entity
Mewakili repository atau sistem
eksternal yang terlibat.
Store DB
Database
Penyimpanan data transaksi, policy,
audit, atau ledger.
Main interaction flow
Alternatif / keputusan
- 
Cabang 1: suspend transaksi aktif
195.
Kasir -> POS UI: pilih suspend transaksi
196.
POS UI -> Suspend Transaction Service: suspend(saleId, cashierId)
197.
Suspend Transaction Service -> Sales Repository: read draft sale state

198.
Sales Repository -> Store DB: select sale state
199.
Store DB -> Sales Repository: draft / invalid
200.
Sales Repository -> Suspend Transaction Service: sale state
Alternatif / keputusan
- 
Cabang 1: eligible untuk suspend
201.
Suspend Transaction Service -> Sales Repository: save suspended snapshot
202.
Sales Repository -> Store DB: update state suspended + snapshot
203.
Store DB -> Sales Repository: saved
204.
Sales Repository -> Suspend Transaction Service: suspendId
205.
Suspend Transaction Service -> POS UI: suspend success(suspendId)
206.
POS UI -> Kasir: tampilkan kode suspend
- 
Cabang 2: not eligible
207.
Suspend Transaction Service -> POS UI: reject suspend
- 
Cabang 2: resume transaksi
208.
Kasir -> POS UI: pilih transaksi suspend
209.
POS UI -> Suspend Transaction Service: resume(suspendId, terminalId)
210.
Suspend Transaction Service -> Sales Repository: load suspended snapshot
211.
Sales Repository -> Store DB: select suspended transaction
212.
Store DB -> Sales Repository: snapshot / not found
213.
Sales Repository -> Suspend Transaction Service: snapshot state
Alternatif / keputusan
- 
Cabang 1: snapshot valid dan belum diresume terminal lain
214.
Suspend Transaction Service -> Pricing Service: recalculate before resume
215.
Pricing Service -> Store DB: read latest pricing rules
216.
Store DB -> Pricing Service: latest inputs
217.
Pricing Service -> Suspend Transaction Service: repriced basket
218.
Suspend Transaction Service -> Sales Repository: mark resumed
219.
Sales Repository -> Store DB: update state resumed
220.
Store DB -> Sales Repository: saved
221.
Sales Repository -> Suspend Transaction Service: ok
222.
Suspend Transaction Service -> POS UI: basket restored
223.
POS UI -> Kasir: lanjutkan transaksi
- 
Cabang 2: invalid / duplicate resume
224.
Suspend Transaction Service -> POS UI: reject resume
Alternate and exception considerations
Alternate flow
- 
Resume dilakukan oleh kasir lain jika policy store mengizinkan handover.
- 
Harga berubah sejak suspend dan sistem meminta konfirmasi sebelum lanjut.
Exception flow
- 
Snapshot transaksi rusak atau sudah kadaluwarsa.

- 
Transaksi sudah di-resume terminal lain -> sistem menolak duplicate resume.
Business rules and implementation notes
- 
Transaksi suspended tidak boleh mengurangi stok final sebelum selesai.
- 
Masa berlaku suspend dapat dibatasi per policy.
- 
Sequence ini menyentuh persistence layer; pastikan boundary transaksi, idempotency, dan partial failure
handling didefinisikan jelas pada implementation.
- 
Ada dependency ke external service/integration; retry policy, timeout, fallback, dan audit trail tidak boleh
dibiarkan implisit.

UC-08 - Cari / Scan Produk
Mengidentifikasi produk yang akan dimasukkan ke
transaksi atau proses inventori.
Primary actor
Kasir / Staff Inventori
Supporting actors
Store Device
Trigger
User memindai barcode atau mengetik kata kunci
SKU/nama.
Preconditions
- Perangkat scan atau data pencarian tersedia.
Postconditions
- Produk teridentifikasi.
Source sequence file
uc08_cari_scan_produk_sequence.puml
Traceability
Use case spec + activity spec
(uc08_cari_scan_produk_activity.puml)
Interaction participants
Participant
Tipe
Peran pada sequence
User
Actor
Memicu atau menerima hasil bisnis
dari flow.
POS UI / Inventory UI
UI/Boundary
Mewakili UI/boundary yang
berinteraksi langsung dengan actor.
Product Lookup Service
Application/Service
Menjalankan orkestrasi use case,
validasi, atau layanan aplikasi.
Catalog Repository
Repository/External Entity
Mewakili repository atau sistem
eksternal yang terlibat.
Store DB
Database
Penyimpanan data transaksi, policy,
audit, atau ledger.
Main interaction flow
225.
User -> POS UI / Inventory UI: scan barcode atau masukkan keyword
226.
POS UI / Inventory UI -> Product Lookup Service: findProduct(criteria)
Alternatif / keputusan
- 
Cabang 1: barcode scan gagal
227.
POS UI / Inventory UI -> User: minta fallback pencarian manual
228.
User -> POS UI / Inventory UI: input SKU / nama
229.
POS UI / Inventory UI -> Product Lookup Service: findProduct(criteria)
230.
Product Lookup Service -> Catalog Repository: search product(criteria)
231.
Catalog Repository -> Store DB: query by barcode / SKU / short name

232.
Store DB -> Catalog Repository: matched items / none
233.
Catalog Repository -> Product Lookup Service: result set
Alternatif / keputusan
- 
Cabang 1: produk ditemukan
234.
Product Lookup Service -> POS UI / Inventory UI: tampilkan hasil
235.
User -> POS UI / Inventory UI: pilih item target
236.
POS UI / Inventory UI -> User: produk teridentifikasi
- 
Cabang 2: produk tidak ditemukan
237.
Product Lookup Service -> POS UI / Inventory UI: no result
238.
POS UI / Inventory UI -> User: tampilkan error produk tidak ditemukan
Alternate and exception considerations
Alternate flow
- 
- Barcode tidak terbaca lalu fallback ke pencarian manual.
Exception flow
- 
- Produk tidak ditemukan atau master data belum tersedia.
Business rules and implementation notes
- 
- Pencarian harus mendukung SKU, barcode, dan nama pendek minimal.
- 
Sequence ini menyentuh persistence layer; pastikan boundary transaksi, idempotency, dan partial failure
handling didefinisikan jelas pada implementation.
- 
Ada dependency ke external service/integration; retry policy, timeout, fallback, dan audit trail tidak boleh
dibiarkan implisit.

UC-09 - Hitung Harga, Promo, Pajak
Menghasilkan total transaksi yang benar berdasarkan
item, promo, voucher, dan aturan pajak.
Primary actor
System
Supporting actors
Loyalty Service, HQ rule cache
Trigger
Ada perubahan item, member/voucher, atau metode
pembayaran yang memengaruhi total.
Preconditions
- Keranjang transaksi tersedia.
Postconditions
- Nilai transaksi terbaru tersedia.
Source sequence file
uc09_hitung_harga_promo_pajak_sequence.puml
Traceability
Use case spec + activity spec (n/a)
Interaction participants
Participant
Tipe
Peran pada sequence
Caller
UI/Boundary
Mewakili UI/boundary yang
berinteraksi langsung dengan actor.
Pricing Service
Application/Service
Menjalankan orkestrasi use case,
validasi, atau layanan aplikasi.
Rule Cache / Catalog Repository
Repository/External Entity
Mewakili repository atau sistem
eksternal yang terlibat.
Store DB
Database
Penyimpanan data transaksi, policy,
audit, atau ledger.
Main interaction flow
239.
Caller -> Pricing Service: recalculate(saleId)
240.
Pricing Service -> Rule Cache / Catalog Repository: load basket, price, promo, tax rules
Alternatif / keputusan
- 
Cabang 1: online / local store data available
241.
Rule Cache / Catalog Repository -> Store DB: read basket and pricing inputs
242.
Store DB -> Rule Cache / Catalog Repository: pricing inputs
- 
Cabang 2: offline fallback
243.
Rule Cache / Catalog Repository -> Store DB: read cached rule set
244.
Store DB -> Rule Cache / Catalog Repository: cached pricing inputs
245.
Rule Cache / Catalog Repository -> Pricing Service: pricing data
Alternatif / keputusan

- 
Cabang 1: rule conflict or missing price
246.
Pricing Service -> Caller: pricing error
- 
Cabang 2: deterministic pricing success
247.
Pricing Service -> Pricing Service: apply promo
248.
Pricing Service -> Pricing Service: apply tax
249.
Pricing Service -> Pricing Service: compute subtotal, discount, total
250.
Pricing Service -> Caller: updated totals
Alternate and exception considerations
Alternate flow
- 
- Rule berasal dari cache lokal saat offline.
Exception flow
- 
- Rule conflict atau data harga hilang.
Business rules and implementation notes
- 
- Pricing harus deterministic untuk input yang sama pada timestamp bisnis yang sama.
- 
Sequence ini menyentuh persistence layer; pastikan boundary transaksi, idempotency, dan partial failure
handling didefinisikan jelas pada implementation.
- 
Ada dependency ke external service/integration; retry policy, timeout, fallback, dan audit trail tidak boleh
dibiarkan implisit.

UC-10 - Void Item / Transaksi
Membatalkan item tertentu atau seluruh transaksi
sesuai otorisasi dan jejak audit yang diwajibkan.
Primary actor
Kasir
Supporting actors
Supervisor, Lookup Receipt
Trigger
Kasir memilih void item atau void transaksi.
Preconditions
- Transaksi aktif atau transaksi yang dapat diidentifikasi
tersedia. - Kasir memiliki hak void sesuai level
otorisasinya atau dapat meminta approval.
Postconditions
- Item/transaksi dibatalkan sesuai kebijakan, atau
permintaan ditolak dengan alasan jelas.
Source sequence file
uc10_void_item_transaksi_sequence.puml
Traceability
Use case spec + activity spec
(uc10_void_item_transaksi_activity.puml)
Interaction participants
Participant
Tipe
Peran pada sequence
Kasir
Actor
Memicu atau menerima hasil bisnis
dari flow.
Supervisor
Actor
Memicu atau menerima hasil bisnis
dari flow.
POS UI
UI/Boundary
Mewakili UI/boundary yang
berinteraksi langsung dengan actor.
Void Use Case
Application/Service
Menjalankan orkestrasi use case,
validasi, atau layanan aplikasi.
Approval Service
Application/Service
Menjalankan orkestrasi use case,
validasi, atau layanan aplikasi.
Audit Service
Application/Service
Menjalankan orkestrasi use case,
validasi, atau layanan aplikasi.
Sales Repository
Repository/External Entity
Mewakili repository atau sistem
eksternal yang terlibat.
Store DB
Database
Penyimpanan data transaksi, policy,
audit, atau ledger.

Main interaction flow
251.
Kasir -> POS UI: pilih void item / transaksi
252.
POS UI -> Void Use Case: requestVoid(target, reason)
253.
Void Use Case -> Sales Repository: load transaction context(target)
254.
Sales Repository -> Store DB: select transaction / receipt detail
255.
Store DB -> Sales Repository: context / not found
256.
Sales Repository -> Void Use Case: context
Alternatif / keputusan
- 
Cabang 1: transaction not found
257.
Void Use Case -> POS UI: reject not found
- 
Cabang 2: context found
Alternatif / keputusan
- 
Cabang 1: reason missing
258.
Void Use Case -> POS UI: minta reason code
- 
Cabang 2: reason complete
259.
Void Use Case -> Void Use Case: evaluate void policy
Alternatif / keputusan
- 
Cabang 1: approval required
## 260. POS UI -> Supervisor: minta approval
## 261. Supervisor -> POS UI: approve / reject
## 262. POS UI -> Approval Service: validate approval
## 263. Approval Service -> Void Use Case: approved / rejected
Alternatif / keputusan
- 
Cabang 1: settled and no longer voidable
## 264. Void Use Case -> POS UI: redirect to return/refund flow
- 
Cabang 2: approved or within authority
## 265. Void Use Case -> Sales Repository: apply void(target)
## 266. Sales Repository -> Store DB: update sale / reversal marker
## 267. Store DB -> Sales Repository: saved
## 268. Sales Repository -> Void Use Case: void success
## 269. Void Use Case -> Audit Service: log void event
## 270. Audit Service -> Store DB: insert audit log
## 271. Store DB -> Audit Service: saved
## 272. Audit Service -> Void Use Case: ok
## 273. Void Use Case -> POS UI: void completed
- 
Cabang 3: approval rejected
## 274. Void Use Case -> POS UI: void rejected

Alternate and exception considerations
Alternate flow
- 
Void item dilakukan sebelum payment sehingga tidak perlu receipt lookup.
- 
Void penuh setelah payment diperlakukan sebagai reversal sesuai policy.
Exception flow
- 
Receipt/transaksi tidak ditemukan.
- 
Approval ditolak.
- 
Transaksi sudah settled dan tidak boleh di-void, harus lewat return/refund.
Business rules and implementation notes
- 
Void selalu membutuhkan reason code.
- 
Use return/refund, bukan void, bila barang sudah keluar dan transaksi telah selesai secara hukum/akuntansi.
- 
Sequence ini menyentuh persistence layer; pastikan boundary transaksi, idempotency, dan partial failure
handling didefinisikan jelas pada implementation.
- 
Ada dependency ke external service/integration; retry policy, timeout, fallback, dan audit trail tidak boleh
dibiarkan implisit.
- 
Flow mengandung gate approval; pastikan authorization, reason capture, dan actor accountability tercatat
secara persistable.
- 
Audit logging diperlakukan sebagai kebutuhan implementasi, bukan kosmetik; event penting harus
memiliki correlation id dan actor context.

UC-11 - Return / Refund
Menerima pengembalian barang pelanggan dan
mengembalikan dana atau kredit sesuai kebijakan
toko.
Primary actor
Kasir / Pelanggan
Supporting actors
Supervisor, Payment Gateway / EDC
Trigger
Pelanggan meminta return atau refund.
Preconditions
- Barang return tersedia untuk diperiksa. - Receipt atau
bukti transaksi dapat dicari, kecuali policy no-receipt
return diizinkan.
Postconditions
- Transaksi return tercatat. - Stok barang kembali atau
dikirim ke damaged/inspection bucket sesuai kondisi.
Source sequence file
uc11_return_refund_sequence.puml
Traceability
Use case spec + activity spec
(uc11_return_refund_activity.puml)
Interaction participants
Participant
Tipe
Peran pada sequence
Pelanggan
Actor
Memicu atau menerima hasil bisnis
dari flow.
Kasir
Actor
Memicu atau menerima hasil bisnis
dari flow.
Supervisor
Actor
Memicu atau menerima hasil bisnis
dari flow.
POS UI
UI/Boundary
Mewakili UI/boundary yang
berinteraksi langsung dengan actor.
Return Use Case
Application/Service
Menjalankan orkestrasi use case,
validasi, atau layanan aplikasi.
Return Policy Service
Application/Service
Menjalankan orkestrasi use case,
validasi, atau layanan aplikasi.
Refund Service
Application/Service
Menjalankan orkestrasi use case,
validasi, atau layanan aplikasi.
Audit Service
Application/Service
Menjalankan orkestrasi use case,
validasi, atau layanan aplikasi.
Sales Repository
Repository/External Entity
Mewakili repository atau sistem

Participant
Tipe
Peran pada sequence
eksternal yang terlibat.
Payment Gateway / EDC
Repository/External Entity
Mewakili repository atau sistem
eksternal yang terlibat.
Store DB
Database
Penyimpanan data transaksi, policy,
audit, atau ledger.
Main interaction flow
275.
Pelanggan -> Kasir: ajukan return/refund
276.
Kasir -> POS UI: mulai return
277.
POS UI -> Return Use Case: startReturn(criteria)
278.
Return Use Case -> Sales Repository: lookupOriginalReceipt(criteria)
279.
Sales Repository -> Store DB: search original transaction
280.
Store DB -> Sales Repository: receipt found / not found
281.
Sales Repository -> Return Use Case: transaction context
Alternatif / keputusan
- 
Cabang 1: receipt not found and no-receipt not allowed
282.
Return Use Case -> POS UI: reject return
- 
Cabang 2: receipt found or no-receipt allowed
283.
Return Use Case -> Return Policy Service: validateReturnPolicy(items, purchaseDate,
itemCondition, paymentMethod)
284.
Return Policy Service -> Store DB: read return policy
285.
Store DB -> Return Policy Service: policy rules
286.
Return Policy Service -> Return Use Case: eligible / ineligible / approval required
Alternatif / keputusan
- 
Cabang 1: ineligible
287.
Return Use Case -> POS UI: reject by policy
- 
Cabang 2: eligible
288.
Kasir -> POS UI: pilih item, qty, dan reason
289.
POS UI -> Return Use Case: confirmReturn(lines, reason)
Alternatif / keputusan
- 
Cabang 1: approval required
## 290. POS UI -> Supervisor: minta approval return exception
## 291. Supervisor -> POS UI: approve / reject
## 292. POS UI -> Return Use Case: supervisorDecision
Alternatif / keputusan

- 
Cabang 1: rejected
## 293. Return Use Case -> POS UI: return rejected
- 
Cabang 2: approved
## 294. Return Use Case -> Refund Service: calculateRefund(lines)
## 295. Refund Service -> Store DB: read original payment and totals
## 296. Store DB -> Refund Service: refund basis
## 297. Refund Service -> Return Use Case: refund amount and eligible method
Alternatif / keputusan
- 
Cabang 1: refund to original card
298.
Refund Service -> Payment Gateway / EDC: refund(providerRef, amount)
299.
Payment Gateway / EDC -> Refund Service: success / failed
- 
Cabang 2: refund cash or store credit
300.
Refund Service -> Store DB: create store credit or cash refund record
301.
Store DB -> Refund Service: saved
Alternatif / keputusan
- 
Cabang 1: refund success or fallback allowed
302.
Return Use Case -> Sales Repository: save return transaction + inventory
disposition
303.
Sales Repository -> Store DB: insert return, stock ledger, refund record
304.
Store DB -> Sales Repository: saved
305.
Sales Repository -> Return Use Case: return saved
306.
Return Use Case -> Audit Service: log return/refund
307.
Audit Service -> Store DB: insert audit log
308.
Store DB -> Audit Service: saved
309.
Audit Service -> Return Use Case: ok
310.
Return Use Case -> POS UI: return completed
- 
Cabang 2: refund failed and no fallback
311.
Return Use Case -> POS UI: refund pending/manual investigation
Alternate and exception considerations
Alternate flow
- 
Return parsial untuk sebagian item.
- 
Refund dikonversi menjadi store credit jika metode asli tidak dapat dipulihkan.
Exception flow
- 
Receipt tidak ditemukan dan no-receipt return tidak diizinkan.
- 
Masa return policy telah lewat.
- 
Refund ke kartu gagal dan perlu fallback sesuai policy.
Business rules and implementation notes
- 
Return policy harus tervalidasi sebelum refund.

- 
Tidak semua item boleh direturn, misalnya clearance/final sale/perishable tertentu.
- 
Sequence ini menyentuh persistence layer; pastikan boundary transaksi, idempotency, dan partial failure
handling didefinisikan jelas pada implementation.
- 
Ada dependency ke external service/integration; retry policy, timeout, fallback, dan audit trail tidak boleh
dibiarkan implisit.
- 
Flow mengandung gate approval; pastikan authorization, reason capture, dan actor accountability tercatat
secara persistable.
- 
Audit logging diperlakukan sebagai kebutuhan implementasi, bukan kosmetik; event penting harus
memiliki correlation id dan actor context.

UC-12 - Price Override / Manual Discount
Mengubah harga jual atau memberi diskon manual
dalam batas policy toko.
Primary actor
Kasir
Supporting actors
Supervisor
Trigger
Kasir memilih override harga atau manual discount.
Preconditions
- Ada item atau transaksi aktif yang eligible untuk
override/discount. - Policy threshold tersedia.
Postconditions
- Harga override/manual discount tersimpan pada
transaksi aktif atau ditolak.
Source sequence file
uc12_price_override_manual_discount_sequence.puml
Traceability
Use case spec + activity spec (n/a)
Interaction participants
Participant
Tipe
Peran pada sequence
Kasir
Actor
Memicu atau menerima hasil bisnis
dari flow.
Supervisor
Actor
Memicu atau menerima hasil bisnis
dari flow.
POS UI
UI/Boundary
Mewakili UI/boundary yang
berinteraksi langsung dengan actor.
Discount Override Use Case
Application/Service
Menjalankan orkestrasi use case,
validasi, atau layanan aplikasi.
Approval Service
Application/Service
Menjalankan orkestrasi use case,
validasi, atau layanan aplikasi.
Pricing Service
Application/Service
Menjalankan orkestrasi use case,
validasi, atau layanan aplikasi.
Audit Service
Application/Service
Menjalankan orkestrasi use case,
validasi, atau layanan aplikasi.
Sales Repository
Repository/External Entity
Mewakili repository atau sistem
eksternal yang terlibat.
Store DB
Database
Penyimpanan data transaksi, policy,
audit, atau ledger.

Main interaction flow
312.
Kasir -> POS UI: pilih override harga / discount manual
313.
POS UI -> Discount Override Use Case: requestOverride(saleId, target, value, reason)
314.
Discount Override Use Case -> Store DB: read target item and policy threshold
315.
Store DB -> Discount Override Use Case: eligibility and threshold
Alternatif / keputusan
- 
Cabang 1: item not eligible or price locked
316.
Discount Override Use Case -> POS UI: reject override
- 
Cabang 2: eligible
Alternatif / keputusan
- 
Cabang 1: within cashier authority
317.
Discount Override Use Case -> Sales Repository: save override/discount
318.
Sales Repository -> Store DB: update sale line / sale header
319.
Store DB -> Sales Repository: saved
- 
Cabang 2: approval required
320.
POS UI -> Supervisor: minta approval
321.
Supervisor -> POS UI: approve / reject
322.
POS UI -> Approval Service: validate approval
323.
Approval Service -> Discount Override Use Case: approved / rejected
Alternatif / keputusan
- 
Cabang 1: approved
## 324. Discount Override Use Case -> Sales Repository: save override/discount
## 325. Sales Repository -> Store DB: update sale line / sale header
## 326. Store DB -> Sales Repository: saved
- 
Cabang 2: rejected
## 327. Discount Override Use Case -> POS UI: reject override
Alternatif / keputusan
- 
Cabang 1: override saved
328.
Discount Override Use Case -> Pricing Service: recalculate(saleId)
329.
Pricing Service -> Discount Override Use Case: updated totals
330.
Discount Override Use Case -> Audit Service: log override action
331.
Audit Service -> Store DB: insert audit log
332.
Store DB -> Audit Service: saved
333.
Audit Service -> Discount Override Use Case: ok
334.
Discount Override Use Case -> POS UI: updated price and total
Alternate and exception considerations
Alternate flow
- 
Diskon masih dalam limit kasir sehingga approval tidak perlu.

- 
Override berlaku pada item tunggal, bukan seluruh basket.
Exception flow
- 
Nilai diskon melewati limit maksimum.
- 
Approval ditolak.
- 
Item tidak eligible karena promo exclusive atau price-locked.
Business rules and implementation notes
- 
Setiap override/discound manual wajib punya reason code dan audit trail.
- 
Perhitungan akhir selalu melalui pricing engine.
- 
Sequence ini menyentuh persistence layer; pastikan boundary transaksi, idempotency, dan partial failure
handling didefinisikan jelas pada implementation.
- 
Ada dependency ke external service/integration; retry policy, timeout, fallback, dan audit trail tidak boleh
dibiarkan implisit.
- 
Flow mengandung gate approval; pastikan authorization, reason capture, dan actor accountability tercatat
secara persistable.
- 
Audit logging diperlakukan sebagai kebutuhan implementasi, bukan kosmetik; event penting harus
memiliki correlation id dan actor context.

UC-13 - Lookup Receipt
Menemukan transaksi/receipt sebelumnya sebagai
referensi exception atau customer service.
Primary actor
Kasir
Supporting actors
Store Manager
Trigger
Kasir perlu mencari transaksi lama.
Preconditions
- Kunci pencarian tersedia.
Postconditions
- Receipt target ditemukan atau tidak.
Source sequence file
uc13_lookup_receipt_sequence.puml
Traceability
Use case spec + activity spec
(uc13_lookup_receipt_activity.puml)
Interaction participants
Participant
Tipe
Peran pada sequence
Kasir
Actor
Memicu atau menerima hasil bisnis
dari flow.
POS UI
UI/Boundary
Mewakili UI/boundary yang
berinteraksi langsung dengan actor.
Receipt Lookup Service
Application/Service
Menjalankan orkestrasi use case,
validasi, atau layanan aplikasi.
Sales Repository
Repository/External Entity
Mewakili repository atau sistem
eksternal yang terlibat.
Store DB
Database
Penyimpanan data transaksi, policy,
audit, atau ledger.
Main interaction flow
335.
Kasir -> POS UI: input nomor receipt / kartu / tanggal
336.
POS UI -> Receipt Lookup Service: searchReceipt(criteria)
Alternatif / keputusan
- 
Cabang 1: offline mode
337.
Receipt Lookup Service -> Store DB: search local limited receipt index
- 
Cabang 2: online / normal mode
338.
Receipt Lookup Service -> Sales Repository: search full receipt data
339.
Sales Repository -> Store DB: query receipt and sale headers

340.
Store DB -> Receipt Lookup Service: matching receipt / none
Alternatif / keputusan
- 
Cabang 1: receipt ditemukan
341.
Receipt Lookup Service -> POS UI: list result + detail
342.
POS UI -> Kasir: tampilkan receipt
- 
Cabang 2: not found
343.
Receipt Lookup Service -> POS UI: no result
344.
POS UI -> Kasir: receipt tidak ditemukan
Alternate and exception considerations
Alternate flow
- 
- Pencarian menggunakan data lokal terbatas saat offline.
Exception flow
- 
- Receipt tidak ditemukan.
Business rules and implementation notes
- 
- Hak akses data receipt harus mengikuti role.
- 
Sequence ini menyentuh persistence layer; pastikan boundary transaksi, idempotency, dan partial failure
handling didefinisikan jelas pada implementation.
- 
Ada dependency ke external service/integration; retry policy, timeout, fallback, dan audit trail tidak boleh
dibiarkan implisit.

UC-14 - Validasi Return Policy
Memastikan permintaan return memenuhi kebijakan
toko dan regulasi yang berlaku.
Primary actor
System
Supporting actors
Supervisor
Trigger
Proses return dimulai atau item return dipilih.
Preconditions
- Item return dan referensi transaksi tersedia atau
informasi minimum tersedia.
Postconditions
- Keputusan eligibility return tersedia.
Source sequence file
uc14_validasi_return_policy_sequence.puml
Traceability
Use case spec + activity spec
(uc14_validasi_return_policy_activity.puml)
Interaction participants
Participant
Tipe
Peran pada sequence
Return Use Case
UI/Boundary
Mewakili UI/boundary yang
berinteraksi langsung dengan actor.
Return Policy Service
Application/Service
Menjalankan orkestrasi use case,
validasi, atau layanan aplikasi.
Approval Service
Application/Service
Menjalankan orkestrasi use case,
validasi, atau layanan aplikasi.
Policy Repository
Repository/External Entity
Mewakili repository atau sistem
eksternal yang terlibat.
Store DB
Database
Penyimpanan data transaksi, policy,
audit, atau ledger.
Main interaction flow
345.
Return Use Case -> Return Policy Service: validateReturn(item, transactionRef, condition, paymentMethod)
346.
Return Policy Service -> Policy Repository: load store return policy
347.
Policy Repository -> Store DB: read policy + purchase context
348.
Store DB -> Policy Repository: policy inputs
349.
Policy Repository -> Return Policy Service: policy data
Alternatif / keputusan
- 
Cabang 1: data pembelian tidak cukup
350.
Return Policy Service -> Return Use Case: insufficient data

- 
Cabang 2: policy violation
Alternatif / keputusan
- 
Cabang 1: override policy tersedia
351.
Return Policy Service -> Approval Service: request exception approval
352.
Approval Service -> Return Policy Service: approved / rejected
353.
Return Policy Service -> Return Use Case: eligibility by override decision
- 
Cabang 2: no override
354.
Return Policy Service -> Return Use Case: ineligible
- 
Cabang 3: policy satisfied
355.
Return Policy Service -> Return Use Case: eligible
Alternate and exception considerations
Alternate flow
- 
- Policy override tersedia dengan approval supervisor.
Exception flow
- 
- Data pembelian tidak cukup untuk validasi.
Business rules and implementation notes
- 
- Policy return harus konsisten antar kanal yang relevan, kecuali memang store-specific.
- 
Sequence ini menyentuh persistence layer; pastikan boundary transaksi, idempotency, dan partial failure
handling didefinisikan jelas pada implementation.
- 
Ada dependency ke external service/integration; retry policy, timeout, fallback, dan audit trail tidak boleh
dibiarkan implisit.
- 
Flow mengandung gate approval; pastikan authorization, reason capture, dan actor accountability tercatat
secara persistable.

UC-15 - Supervisor Approval
Memberikan keputusan otorisasi untuk operasi sensitif
yang melewati kewenangan staf biasa.
Primary actor
Supervisor
Supporting actors
Kasir / Staff Inventori
Trigger
Sistem meminta approval.
Preconditions
- Ada permintaan approval aktif. - Supervisor memiliki
kredensial valid.
Postconditions
- Keputusan approval tercatat.
Source sequence file
uc15_supervisor_approval_sequence.puml
Traceability
Use case spec + activity spec
(uc15_supervisor_approval_activity.puml)
Interaction participants
Participant
Tipe
Peran pada sequence
Supervisor
Actor
Memicu atau menerima hasil bisnis
dari flow.
Requester UI
UI/Boundary
Mewakili UI/boundary yang
berinteraksi langsung dengan actor.
Approval Service
Application/Service
Menjalankan orkestrasi use case,
validasi, atau layanan aplikasi.
Auth Service
Application/Service
Menjalankan orkestrasi use case,
validasi, atau layanan aplikasi.
Approval Repository
Repository/External Entity
Mewakili repository atau sistem
eksternal yang terlibat.
Store DB
Database
Penyimpanan data transaksi, policy,
audit, atau ledger.
Main interaction flow
356.
Requester UI -> Approval Service: requestApproval(context, reason, requester)
357.
Approval Service -> Requester UI: present approval prompt
358.
Supervisor -> Requester UI: login + approve/reject
359.
Requester UI -> Auth Service: authenticateSupervisor(credentials)
360.
Auth Service -> Store DB: verify supervisor role
361.
Store DB -> Auth Service: valid / invalid
362.
Auth Service -> Approval Service: auth result

Alternatif / keputusan
- 
Cabang 1: auth invalid
363.
Approval Service -> Requester UI: approval denied
- 
Cabang 2: auth valid
364.
Approval Service -> Approval Repository: save decision(context, decision, supervisorId)
365.
Approval Repository -> Store DB: insert approval record
366.
Store DB -> Approval Repository: saved
367.
Approval Repository -> Approval Service: ok
368.
Approval Service -> Requester UI: approval result
Alternate and exception considerations
Alternate flow
- 
- Supervisor login langsung di terminal requester.
Exception flow
- 
- Supervisor gagal autentikasi.
Business rules and implementation notes
- 
- Approval harus mengandung siapa, kapan, apa yang diizinkan, dan untuk alasan apa.
- 
Sequence ini menyentuh persistence layer; pastikan boundary transaksi, idempotency, dan partial failure
handling didefinisikan jelas pada implementation.
- 
Ada dependency ke external service/integration; retry policy, timeout, fallback, dan audit trail tidak boleh
dibiarkan implisit.
- 
Flow mengandung gate approval; pastikan authorization, reason capture, dan actor accountability tercatat
secara persistable.

UC-16 - Catat Alasan Exception
Merekam reason code dan catatan operasional untuk
tindakan exception.
Primary actor
Kasir / Supervisor / Staff Inventori
Supporting actors
System
Trigger
User menjalankan void, return, override, receiving gap,
adjustment, atau exception lain.
Preconditions
- Ada proses exception yang sedang berjalan.
Postconditions
- Alasan exception tersimpan.
Source sequence file
uc16_catat_alasan_exception_sequence.puml
Traceability
Use case spec + activity spec
(uc16_catat_alasan_exception_activity.puml)
Interaction participants
Participant
Tipe
Peran pada sequence
User
Actor
Memicu atau menerima hasil bisnis
dari flow.
POS UI
UI/Boundary
Mewakili UI/boundary yang
berinteraksi langsung dengan actor.
Exception Reason Service
Application/Service
Menjalankan orkestrasi use case,
validasi, atau layanan aplikasi.
Reference Repository
Repository/External Entity
Mewakili repository atau sistem
eksternal yang terlibat.
Store DB
Database
Penyimpanan data transaksi, policy,
audit, atau ledger.
Main interaction flow
369.
POS UI -> User: minta reason code
370.
User -> POS UI: pilih reason + catatan + lampiran opsional
371.
POS UI -> Exception Reason Service: captureReason(exceptionType, reason, note, attachment)
372.
Exception Reason Service -> Reference Repository: validate reason code against exception type
373.
Reference Repository -> Store DB: query reason master
374.
Store DB -> Reference Repository: valid / invalid
375.
Reference Repository -> Exception Reason Service: validation result
Alternatif / keputusan
- 
Cabang 1: reason invalid or missing

376.
Exception Reason Service -> POS UI: reject and require completion
- 
Cabang 2: valid
Opsional: ada lampiran
377.
Exception Reason Service -> Store DB: save attachment metadata
378.
Store DB -> Exception Reason Service: saved
379.
Exception Reason Service -> Store DB: save exception reason record
380.
Store DB -> Exception Reason Service: saved
381.
Exception Reason Service -> POS UI: reason captured
Alternate and exception considerations
Alternate flow
- 
- Foto/dokumen pendukung ditambahkan.
Exception flow
- 
- Reason code wajib tetapi belum diisi.
Business rules and implementation notes
- 
- Alasan exception tidak boleh null untuk proses yang mewajibkannya.
- 
Sequence ini menyentuh persistence layer; pastikan boundary transaksi, idempotency, dan partial failure
handling didefinisikan jelas pada implementation.
- 
Ada dependency ke external service/integration; retry policy, timeout, fallback, dan audit trail tidak boleh
dibiarkan implisit.

UC-17 - Input Kas Awal
Mencatat saldo awal laci kas pada awal shift.
Primary actor
Kasir
Supporting actors
Supervisor
Trigger
Bagian opening cash pada mulai shift dimulai.
Preconditions
- Shift akan dibuka.
Postconditions
- Opening cash tercatat.
Source sequence file
uc17_input_kas_awal_sequence.puml
Traceability
Use case spec + activity spec
(uc17_input_kas_awal_activity.puml)
Interaction participants
Participant
Tipe
Peran pada sequence
Kasir
Actor
Memicu atau menerima hasil bisnis
dari flow.
Supervisor
Actor
Memicu atau menerima hasil bisnis
dari flow.
POS UI
UI/Boundary
Mewakili UI/boundary yang
berinteraksi langsung dengan actor.
Opening Cash Service
Application/Service
Menjalankan orkestrasi use case,
validasi, atau layanan aplikasi.
Shift Repository
Repository/External Entity
Mewakili repository atau sistem
eksternal yang terlibat.
Store DB
Database
Penyimpanan data transaksi, policy,
audit, atau ledger.
Main interaction flow
382.
Kasir -> POS UI: input nominal kas awal
383.
POS UI -> Opening Cash Service: captureOpeningCash(terminalId, amount)
Alternatif / keputusan
- 
Cabang 1: nominal invalid
384.
Opening Cash Service -> POS UI: reject invalid amount
- 
Cabang 2: nominal valid

Alternatif / keputusan
- 
Cabang 1: verification required
385.
POS UI -> Supervisor: minta verifikasi
386.
Supervisor -> POS UI: approve / reject
Alternatif / keputusan
- 
Cabang 1: approved or not required
387.
Opening Cash Service -> Shift Repository: saveOpeningCash(amount)
388.
Shift Repository -> Store DB: insert opening cash
389.
Store DB -> Shift Repository: saved
390.
Shift Repository -> Opening Cash Service: ok
391.
Opening Cash Service -> POS UI: opening cash saved
- 
Cabang 2: rejected
392.
Opening Cash Service -> POS UI: opening cash rejected
Alternate and exception considerations
Alternate flow
- 
- Supervisor memverifikasi nominal.
Exception flow
- 
- Nominal tidak valid.
Business rules and implementation notes
- 
- Opening cash bagian wajib dari start shift.
- 
Sequence ini menyentuh persistence layer; pastikan boundary transaksi, idempotency, dan partial failure
handling didefinisikan jelas pada implementation.
- 
Ada dependency ke external service/integration; retry policy, timeout, fallback, dan audit trail tidak boleh
dibiarkan implisit.
- 
Flow mengandung gate approval; pastikan authorization, reason capture, dan actor accountability tercatat
secara persistable.

UC-18 - Cash In / Cash Out
Mencatat pergerakan kas non-penjualan selama shift
dengan alasan yang terkontrol.
Primary actor
Kasir
Supporting actors
Supervisor (opsional)
Trigger
Kasir perlu menambah atau mengeluarkan uang dari
laci kas untuk keperluan operasional.
Preconditions
- Shift aktif. - Jenis cash movement tersedia di master
reason.
Postconditions
- Pergerakan kas tercatat dan dapat direkonsiliasi saat
tutup shift.
Source sequence file
uc18_cash_in_cash_out_sequence.puml
Traceability
Use case spec + activity spec
(uc18_cash_in_cash_out_activity.puml)
Interaction participants
Participant
Tipe
Peran pada sequence
Kasir
Actor
Memicu atau menerima hasil bisnis
dari flow.
Supervisor
Actor
Memicu atau menerima hasil bisnis
dari flow.
POS UI
UI/Boundary
Mewakili UI/boundary yang
berinteraksi langsung dengan actor.
Cash Movement Use Case
Application/Service
Menjalankan orkestrasi use case,
validasi, atau layanan aplikasi.
Approval Service
Application/Service
Menjalankan orkestrasi use case,
validasi, atau layanan aplikasi.
Audit Service
Application/Service
Menjalankan orkestrasi use case,
validasi, atau layanan aplikasi.
Cash Movement Repository
Repository/External Entity
Mewakili repository atau sistem
eksternal yang terlibat.
Store DB
Database
Penyimpanan data transaksi, policy,
audit, atau ledger.

Main interaction flow
393.
Kasir -> POS UI: pilih cash in / cash out
394.
POS UI -> Cash Movement Use Case: createCashMovement(shiftId, type, reason, amount, note)
395.
Cash Movement Use Case -> Store DB: validate active shift and reason code
396.
Store DB -> Cash Movement Use Case: valid / invalid
Alternatif / keputusan
- 
Cabang 1: invalid reason or no active shift
397.
Cash Movement Use Case -> POS UI: reject movement
- 
Cabang 2: valid
Opsional: nominal besar / perlu approval
398.
POS UI -> Supervisor: minta approval
399.
Supervisor -> POS UI: approve / reject
400.
POS UI -> Approval Service: validate approval
401.
Approval Service -> Cash Movement Use Case: approved / rejected
Alternatif / keputusan
- 
Cabang 1: approved or not required
402.
Cash Movement Use Case -> Cash Movement Repository: save cash movement
403.
Cash Movement Repository -> Store DB: insert movement + update expected drawer
balance
404.
Store DB -> Cash Movement Repository: saved
405.
Cash Movement Repository -> Cash Movement Use Case: movement saved
406.
Cash Movement Use Case -> Audit Service: log cash movement
407.
Audit Service -> Store DB: insert audit log
408.
Store DB -> Audit Service: saved
409.
Audit Service -> Cash Movement Use Case: ok
410.
Cash Movement Use Case -> POS UI: cash movement completed
- 
Cabang 2: rejected
411.
Cash Movement Use Case -> POS UI: cash movement rejected
Alternate and exception considerations
Alternate flow
- 
Supervisor approval diminta untuk nominal besar.
- 
Dokumen pendukung difoto/diunggah bila toko menerapkan kontrol tambahan.
Exception flow
- 
Nominal tidak valid atau reason code tidak diizinkan.
- 
Shift tidak aktif.
Business rules and implementation notes
- 
Semua cash movement harus terhubung ke shift aktif.
- 
Tidak boleh ada cash out tanpa reason code.
- 
Sequence ini menyentuh persistence layer; pastikan boundary transaksi, idempotency, dan partial failure
handling didefinisikan jelas pada implementation.

- 
Ada dependency ke external service/integration; retry policy, timeout, fallback, dan audit trail tidak boleh
dibiarkan implisit.
- 
Flow mengandung gate approval; pastikan authorization, reason capture, dan actor accountability tercatat
secara persistable.
- 
Audit logging diperlakukan sebagai kebutuhan implementasi, bukan kosmetik; event penting harus
memiliki correlation id dan actor context.

UC-19 - Safe Drop
Memindahkan sebagian uang tunai dari laci kas ke
safe untuk mengurangi risiko cash-on- hand.
Primary actor
Supervisor
Supporting actors
Kasir
Trigger
Supervisor menjalankan safe drop.
Preconditions
- Ada shift aktif dan kas tunai melebihi ambang aman
atau supervisor memutuskan safe drop.
Postconditions
- Transaksi safe drop tercatat dan saldo kas laci
berkurang.
Source sequence file
uc19_safe_drop_sequence.puml
Traceability
Use case spec + activity spec
(uc19_safe_drop_activity.puml)
Interaction participants
Participant
Tipe
Peran pada sequence
Supervisor
Actor
Memicu atau menerima hasil bisnis
dari flow.
Kasir
Actor
Memicu atau menerima hasil bisnis
dari flow.
POS UI
UI/Boundary
Mewakili UI/boundary yang
berinteraksi langsung dengan actor.
Safe Drop Use Case
Application/Service
Menjalankan orkestrasi use case,
validasi, atau layanan aplikasi.
Audit Service
Application/Service
Menjalankan orkestrasi use case,
validasi, atau layanan aplikasi.
Cash Movement Repository
Repository/External Entity
Mewakili repository atau sistem
eksternal yang terlibat.
Store DB
Database
Penyimpanan data transaksi, policy,
audit, atau ledger.
Main interaction flow
412.
Supervisor -> POS UI: pilih shift/terminal untuk safe drop
413.
POS UI -> Safe Drop Use Case: startSafeDrop(shiftId, amount)
414.
Safe Drop Use Case -> Store DB: read expected drawer balance
415.
Store DB -> Safe Drop Use Case: expected cash

Alternatif / keputusan
- 
Cabang 1: amount > expected cash
416.
Safe Drop Use Case -> POS UI: reject over safe drop
- 
Cabang 2: valid amount
Opsional: kasir menyiapkan uang
417.
POS UI -> Kasir: siapkan uang cash untuk supervisor
418.
Kasir -> POS UI: uang siap
419.
Supervisor -> POS UI: konfirmasi uang masuk safe
420.
POS UI -> Safe Drop Use Case: confirmSafeDrop()
421.
Safe Drop Use Case -> Cash Movement Repository: save safe drop
422.
Cash Movement Repository -> Store DB: insert safe drop + reduce expected drawer balance
423.
Store DB -> Cash Movement Repository: saved
424.
Cash Movement Repository -> Safe Drop Use Case: ok
425.
Safe Drop Use Case -> Audit Service: log safe drop
426.
Audit Service -> Store DB: insert audit log
427.
Store DB -> Audit Service: saved
428.
Audit Service -> Safe Drop Use Case: ok
429.
Safe Drop Use Case -> POS UI: safe drop completed
Alternate and exception considerations
Alternate flow
- 
Kasir menyiapkan uang, supervisor hanya mengotorisasi dan mengonfirmasi.
- 
Safe drop dilakukan beberapa kali dalam satu shift.
Exception flow
- 
Nominal safe drop melebihi expected cash di drawer.
- 
Safe drop dibatalkan sebelum konfirmasi.
Business rules and implementation notes
- 
- Safe drop harus dapat diaudit dan terkait ke shift/terminal tertentu.
- 
Sequence ini menyentuh persistence layer; pastikan boundary transaksi, idempotency, dan partial failure
handling didefinisikan jelas pada implementation.
- 
Ada dependency ke external service/integration; retry policy, timeout, fallback, dan audit trail tidak boleh
dibiarkan implisit.
- 
Flow mengandung gate approval; pastikan authorization, reason capture, dan actor accountability tercatat
secara persistable.
- 
Audit logging diperlakukan sebagai kebutuhan implementasi, bukan kosmetik; event penting harus
memiliki correlation id dan actor context.

UC-20 - Tutup Shift
Menutup shift kasir secara resmi dengan rekonsiliasi
kas dan pelaporan dasar.
Primary actor
Kasir
Supporting actors
Supervisor
Trigger
Kasir memilih tutup shift.
Preconditions
- Shift aktif. - Semua transaksi pending pada shift telah
ditangani atau di-mark sesuai policy.
Postconditions
- Shift berubah menjadi closed. - Data kas shift siap
untuk end-of-day dan store reporting.
Source sequence file
uc20_tutup_shift_sequence.puml
Traceability
Use case spec + activity spec
(uc20_tutup_shift_activity.puml)
Interaction participants
Participant
Tipe
Peran pada sequence
Kasir
Actor
Memicu atau menerima hasil bisnis
dari flow.
Supervisor
Actor
Memicu atau menerima hasil bisnis
dari flow.
POS UI
UI/Boundary
Mewakili UI/boundary yang
berinteraksi langsung dengan actor.
Shift Closing Use Case
Application/Service
Menjalankan orkestrasi use case,
validasi, atau layanan aplikasi.
Cash Reconciliation Service
Application/Service
Menjalankan orkestrasi use case,
validasi, atau layanan aplikasi.
Report Service
Application/Service
Menjalankan orkestrasi use case,
validasi, atau layanan aplikasi.
Audit Service
Application/Service
Menjalankan orkestrasi use case,
validasi, atau layanan aplikasi.
Shift Repository
Repository/External Entity
Mewakili repository atau sistem
eksternal yang terlibat.
Store DB
Database
Penyimpanan data transaksi, policy,
audit, atau ledger.

Main interaction flow
430.
Kasir -> POS UI: pilih tutup shift
431.
POS UI -> Shift Closing Use Case: startCloseShift(shiftId)
432.
Shift Closing Use Case -> Shift Repository: validate closable shift
433.
Shift Repository -> Store DB: read active shift + pending transaction status
434.
Store DB -> Shift Repository: shift context
435.
Shift Repository -> Shift Closing Use Case: context
Alternatif / keputusan
- 
Cabang 1: policy blocks close due to pending items
436.
Shift Closing Use Case -> POS UI: reject close shift
- 
Cabang 2: closable
437.
Shift Closing Use Case -> POS UI: show expected drawer balance
438.
Kasir -> POS UI: input hasil hitung fisik
439.
POS UI -> Cash Reconciliation Service: reconcile(shiftId, physicalCash)
440.
Cash Reconciliation Service -> Store DB: read expected drawer balance and cash movements
441.
Store DB -> Cash Reconciliation Service: reconciliation inputs
442.
Cash Reconciliation Service -> Shift Closing Use Case: variance result
Alternatif / keputusan
- 
Cabang 1: variance above tolerance
443.
POS UI -> Supervisor: minta sign-off variance
444.
Supervisor -> POS UI: approve / reject
Alternatif / keputusan
- 
Cabang 1: approved or within tolerance
445.
Shift Closing Use Case -> Report Service: generate X/Z report(shiftId)
446.
Report Service -> Store DB: read shift sales and cash summary
447.
Store DB -> Report Service: shift summary
448.
Report Service -> Shift Closing Use Case: report ready
449.
Shift Closing Use Case -> Shift Repository: closeShift(shiftId)
450.
Shift Repository -> Store DB: update shift closed
451.
Store DB -> Shift Repository: saved
452.
Shift Repository -> Shift Closing Use Case: closed
453.
Shift Closing Use Case -> Audit Service: log shift closed
454.
Audit Service -> Store DB: insert audit log
455.
Store DB -> Audit Service: saved
456.
Audit Service -> Shift Closing Use Case: ok
457.
Shift Closing Use Case -> POS UI: shift closed successfully
- 
Cabang 2: rejected
458.
Shift Closing Use Case -> POS UI: close shift rejected
Alternate and exception considerations
Alternate flow

- 
Supervisor membantu verifikasi ketika ada selisih kas.
- 
Beberapa transaksi pending tetap dibawa ke investigasi setelah shift ditutup.
Exception flow
- 
Selisih kas melebihi tolerance dan membutuhkan supervisor sign-off.
- 
Masih ada transaksi suspend/pending yang belum diresolusikan dan policy menolak close shift.
Business rules and implementation notes
- 
Close shift wajib include cash reconciliation dan report generation.
- 
Shift closed tidak boleh menerima transaksi baru.
- 
Sequence ini menyentuh persistence layer; pastikan boundary transaksi, idempotency, dan partial failure
handling didefinisikan jelas pada implementation.
- 
Ada dependency ke external service/integration; retry policy, timeout, fallback, dan audit trail tidak boleh
dibiarkan implisit.
- 
Flow mengandung gate approval; pastikan authorization, reason capture, dan actor accountability tercatat
secara persistable.
- 
Audit logging diperlakukan sebagai kebutuhan implementasi, bukan kosmetik; event penting harus
memiliki correlation id dan actor context.

UC-21 - Rekonsiliasi Kas
Membandingkan kas fisik dan kas sistem untuk
menemukan selisih shift.
Primary actor
Kasir / Supervisor
Supporting actors
System
Trigger
Close shift atau cash audit dilakukan.
Preconditions
- Shift aktif yang akan ditutup memiliki expected cash
balance.
Postconditions
- Hasil rekonsiliasi tersimpan.
Source sequence file
uc21_rekonsiliasi_kas_sequence.puml
Traceability
Use case spec + activity spec
(uc21_rekonsiliasi_kas_activity.puml)
Interaction participants
Participant
Tipe
Peran pada sequence
User
Actor
Memicu atau menerima hasil bisnis
dari flow.
Supervisor
Actor
Memicu atau menerima hasil bisnis
dari flow.
POS UI
UI/Boundary
Mewakili UI/boundary yang
berinteraksi langsung dengan actor.
Cash Reconciliation Service
Application/Service
Menjalankan orkestrasi use case,
validasi, atau layanan aplikasi.
Cash Movement Repository
Repository/External Entity
Mewakili repository atau sistem
eksternal yang terlibat.
Store DB
Database
Penyimpanan data transaksi, policy,
audit, atau ledger.
Main interaction flow
459.
User -> POS UI: input hasil hitung fisik
460.
POS UI -> Cash Reconciliation Service: reconcile(shiftId, physicalCash)
461.
Cash Reconciliation Service -> Cash Movement Repository: read expected balance and movements
462.
Cash Movement Repository -> Store DB: query opening cash, sales cash, cash movement, safe drop
463.
Store DB -> Cash Movement Repository: reconciliation inputs
464.
Cash Movement Repository -> Cash Reconciliation Service: expected balance
465.
Cash Reconciliation Service -> Cash Reconciliation Service: compute variance
466.
Cash Reconciliation Service -> POS UI: variance result

Alternatif / keputusan
- 
Cabang 1: variance above tolerance
467.
POS UI -> Supervisor: minta sign-off selisih
468.
Supervisor -> POS UI: approve / reject
Alternatif / keputusan
- 
Cabang 1: confirmed
469.
Cash Reconciliation Service -> Store DB: save reconciliation result
470.
Store DB -> Cash Reconciliation Service: saved
471.
Cash Reconciliation Service -> POS UI: reconciliation saved
- 
Cabang 2: input salah / rejected
472.
Cash Reconciliation Service -> POS UI: perlu hitung ulang atau investigasi
Alternate and exception considerations
Alternate flow
- 
- Supervisor menandatangani selisih di atas tolerance.
Exception flow
- 
- Perhitungan ulang diperlukan karena input salah.
Business rules and implementation notes
- 
- Selisih harus dapat ditelusuri ke cash movement dan transaksi.
- 
Sequence ini menyentuh persistence layer; pastikan boundary transaksi, idempotency, dan partial failure
handling didefinisikan jelas pada implementation.
- 
Ada dependency ke external service/integration; retry policy, timeout, fallback, dan audit trail tidak boleh
dibiarkan implisit.
- 
Flow mengandung gate approval; pastikan authorization, reason capture, dan actor accountability tercatat
secara persistable.

UC-22 - Generate X / Z Report
Menghasilkan ringkasan transaksi dan kas untuk
kontrol shift atau harian.
Primary actor
Supervisor
Supporting actors
Store Device
Trigger
User meminta X report atau Z report.
Preconditions
- Data transaksi dan kas tersedia untuk periode/report
type.
Postconditions
- Report tersedia.
Source sequence file
uc22_generate_x_z_report_sequence.puml
Traceability
Use case spec + activity spec
(uc22_generate_x_z_report_activity.puml)
Interaction participants
Participant
Tipe
Peran pada sequence
Supervisor
Actor
Memicu atau menerima hasil bisnis
dari flow.
POS UI
UI/Boundary
Mewakili UI/boundary yang
berinteraksi langsung dengan actor.
Report Service
Application/Service
Menjalankan orkestrasi use case,
validasi, atau layanan aplikasi.
Report Repository
Repository/External Entity
Mewakili repository atau sistem
eksternal yang terlibat.
Printer / Export Device
Repository/External Entity
Mewakili repository atau sistem
eksternal yang terlibat.
Store DB
Database
Penyimpanan data transaksi, policy,
audit, atau ledger.
Main interaction flow
473.
Supervisor -> POS UI: pilih X report / Z report
474.
POS UI -> Report Service: generateReport(type, period)
475.
Report Service -> Report Repository: collect report data
476.
Report Repository -> Store DB: query sales, return, cash, shift summary
477.
Store DB -> Report Repository: report inputs
478.
Report Repository -> Report Service: report dataset
Alternatif / keputusan

- 
Cabang 1: data belum lengkap
479.
Report Service -> POS UI: report cannot be generated
- 
Cabang 2: report ready
480.
Report Service -> POS UI: tampilkan report
Alternatif / keputusan
- 
Cabang 1: print atau export
481.
POS UI -> Report Service: output(report, channel)
482.
Report Service -> Printer / Export Device: print/export report
483.
Printer / Export Device -> Report Service: success / failed
484.
Report Service -> POS UI: output result
Alternate and exception considerations
Alternate flow
- 
- Report disimpan digital.
Exception flow
- 
- Data report belum lengkap.
Business rules and implementation notes
- 
- Z report umumnya final/closing; X report interim/control.
- 
Sequence ini menyentuh persistence layer; pastikan boundary transaksi, idempotency, dan partial failure
handling didefinisikan jelas pada implementation.
- 
Ada dependency ke external service/integration; retry policy, timeout, fallback, dan audit trail tidak boleh
dibiarkan implisit.
- 
Flow mengandung gate approval; pastikan authorization, reason capture, dan actor accountability tercatat
secara persistable.

UC-23 - Tutup Hari
Menutup operasi toko untuk satu hari bisnis dan
menghasilkan rekap operasional final.
Primary actor
Store Manager
Supporting actors
Supervisor, HQ Store Service
Trigger
Store Manager menjalankan end of day.
Preconditions
- Semua shift yang wajib ditutup telah selesai atau
ditangani sesuai exception policy. - Sinkronisasi
minimum operasional tersedia.
Postconditions
- Hari bisnis ditutup. - Laporan operasional final
tersedia untuk toko/HQ.
Source sequence file
uc23_tutup_hari_sequence.puml
Traceability
Use case spec + activity spec
(uc23_tutup_hari_activity.puml)
Interaction participants
Participant
Tipe
Peran pada sequence
Store Manager
Actor
Memicu atau menerima hasil bisnis
dari flow.
Backoffice UI
UI/Boundary
Mewakili UI/boundary yang
berinteraksi langsung dengan actor.
End Of Day Use Case
Application/Service
Menjalankan orkestrasi use case,
validasi, atau layanan aplikasi.
Report Service
Application/Service
Menjalankan orkestrasi use case,
validasi, atau layanan aplikasi.
Audit Service
Application/Service
Menjalankan orkestrasi use case,
validasi, atau layanan aplikasi.
Business Day Repository
Repository/External Entity
Mewakili repository atau sistem
eksternal yang terlibat.
Store DB
Database
Penyimpanan data transaksi, policy,
audit, atau ledger.
HQ Store Service
Repository/External Entity
Mewakili repository atau sistem
eksternal yang terlibat.

Main interaction flow
485.
Store Manager -> Backoffice UI: jalankan end of day
486.
Backoffice UI -> End Of Day Use Case: closeBusinessDay(storeId, businessDate)
487.
End Of Day Use Case -> Business Day Repository: validate store readiness
488.
Business Day Repository -> Store DB: read open shifts, settlement status, required sync status
489.
Store DB -> Business Day Repository: readiness state
490.
Business Day Repository -> End Of Day Use Case: ready / blocked
Alternatif / keputusan
- 
Cabang 1: blocked by open shift or missing critical data
491.
End Of Day Use Case -> Backoffice UI: reject close day
- 
Cabang 2: ready
Opsional: minimal sync verification to HQ
492.
End Of Day Use Case -> HQ Store Service: verify store sync baseline
493.
HQ Store Service -> End Of Day Use Case: ok / unavailable
494.
End Of Day Use Case -> Report Service: generate final operational report
495.
Report Service -> Store DB: query final day summary
496.
Store DB -> Report Service: final summary
497.
Report Service -> End Of Day Use Case: report ready
498.
End Of Day Use Case -> Business Day Repository: close business day
499.
Business Day Repository -> Store DB: update business day status closed
500.
Store DB -> Business Day Repository: saved
501.
Business Day Repository -> End Of Day Use Case: closed
502.
End Of Day Use Case -> Audit Service: log end of day
503.
Audit Service -> Store DB: insert audit log
504.
Store DB -> Audit Service: saved
505.
Audit Service -> End Of Day Use Case: ok
506.
End Of Day Use Case -> Backoffice UI: end of day completed
Alternate and exception considerations
Alternate flow
- 
Manager menunda penutupan hari hingga outstanding issue terselesaikan.
- 
Store tetap end-of-day dalam mode offline dan sinkronisasi dilakukan kemudian.
Exception flow
- 
Masih ada shift aktif yang belum ditutup.
- 
Data kritikal belum lengkap sehingga hari tidak boleh ditutup.
Business rules and implementation notes
- 
- Satu toko hanya boleh memiliki satu business day aktif pada satu waktu.
- 
Sequence ini menyentuh persistence layer; pastikan boundary transaksi, idempotency, dan partial failure
handling didefinisikan jelas pada implementation.
- 
Ada dependency ke external service/integration; retry policy, timeout, fallback, dan audit trail tidak boleh
dibiarkan implisit.
- 
Audit logging diperlakukan sebagai kebutuhan implementasi, bukan kosmetik; event penting harus
memiliki correlation id dan actor context.

UC-24 - Cek Stok & Movement
Melihat saldo stok dan histori movement untuk
pengambilan keputusan operasional toko.
Primary actor
Staff Inventori
Supporting actors
HQ Store Service (opsional)
Trigger
Staff inventori mencari stok barang.
Preconditions
- User inventori terautentikasi. - Data stok lokal atau
hasil sinkronisasi tersedia.
Postconditions
- Informasi stok dan movement berhasil ditampilkan.
Source sequence file
uc24_cek_stok_movement_sequence.puml
Traceability
Use case spec + activity spec
(uc24_cek_stok_movement_activity.puml)
Interaction participants
Participant
Tipe
Peran pada sequence
Staff Inventori
Actor
Memicu atau menerima hasil bisnis
dari flow.
Inventory UI
UI/Boundary
Mewakili UI/boundary yang
berinteraksi langsung dengan actor.
Inventory Inquiry Service
Application/Service
Menjalankan orkestrasi use case,
validasi, atau layanan aplikasi.
Stock Repository
Repository/External Entity
Mewakili repository atau sistem
eksternal yang terlibat.
Store DB
Database
Penyimpanan data transaksi, policy,
audit, atau ledger.
Main interaction flow
507.
Staff Inventori -> Inventory UI: cari item SKU/barcode/nama
508.
Inventory UI -> Inventory Inquiry Service: getStockAndMovement(criteria)
509.
Inventory Inquiry Service -> Stock Repository: findStock(criteria)
510.
Stock Repository -> Store DB: query onHand, available, reserved, location
511.
Store DB -> Stock Repository: stock snapshot / none
512.
Stock Repository -> Inventory Inquiry Service: stock result
Alternatif / keputusan
- 
Cabang 1: item tidak ditemukan
513.
Inventory Inquiry Service -> Inventory UI: no result

- 
Cabang 2: item ditemukan
514.
Inventory Inquiry Service -> Stock Repository: getMovementHistory(itemId)
515.
Stock Repository -> Store DB: query sales, return, receiving, transfer, adjustment ledger
516.
Store DB -> Stock Repository: movement history / partial history
517.
Stock Repository -> Inventory Inquiry Service: movement data
518.
Inventory Inquiry Service -> Inventory UI: stock + movement detail
Alternate and exception considerations
Alternate flow
- 
Pencarian dilakukan per rak/lokasi.
- 
Data movement diambil dari cache lokal saat offline.
Exception flow
- 
Item tidak ditemukan.
- 
Data movement belum sinkron penuh.
Business rules and implementation notes
- 
- Movement harus memiliki timestamp dan source transaction yang bisa ditelusuri.
- 
Sequence ini menyentuh persistence layer; pastikan boundary transaksi, idempotency, dan partial failure
handling didefinisikan jelas pada implementation.
- 
Ada dependency ke external service/integration; retry policy, timeout, fallback, dan audit trail tidak boleh
dibiarkan implisit.

UC-25 - Receiving Barang
Menerima barang yang datang ke toko dan
memperbarui stok penerimaan secara akurat.
Primary actor
Staff Inventori
Supporting actors
HQ Store Service, Device
Trigger
Staff inventori memulai proses receiving.
Preconditions
- Ada delivery note / PO / transfer in yang valid. -
Barang fisik telah tiba di toko.
Postconditions
- Stok receiving bertambah. - Discrepancy tercatat bila
ada.
Source sequence file
uc25_receiving_barang_sequence.puml
Traceability
Use case spec + activity spec
(uc25_receiving_barang_activity.puml)
Interaction participants
Participant
Tipe
Peran pada sequence
Staff Inventori
Actor
Memicu atau menerima hasil bisnis
dari flow.
Inventory UI
UI/Boundary
Mewakili UI/boundary yang
berinteraksi langsung dengan actor.
Receiving Use Case
Application/Service
Menjalankan orkestrasi use case,
validasi, atau layanan aplikasi.
Delivery Verification Service
Application/Service
Menjalankan orkestrasi use case,
validasi, atau layanan aplikasi.
Audit Service
Application/Service
Menjalankan orkestrasi use case,
validasi, atau layanan aplikasi.
Receiving Repository
Repository/External Entity
Mewakili repository atau sistem
eksternal yang terlibat.
Store DB
Database
Penyimpanan data transaksi, policy,
audit, atau ledger.
HQ Store Service
Repository/External Entity
Mewakili repository atau sistem
eksternal yang terlibat.

Main interaction flow
519.
Staff Inventori -> Inventory UI: mulai receiving + pilih referensi delivery/PO
520.
Inventory UI -> Receiving Use Case: startReceiving(referenceNo)
521.
Receiving Use Case -> Delivery Verification Service: verifyDelivery(referenceNo, storeId)
Alternatif / keputusan
- 
Cabang 1: local reference available
522.
Delivery Verification Service -> Store DB: read cached delivery/PO
523.
Store DB -> Delivery Verification Service: expected items / none
- 
Cabang 2: query HQ
524.
Delivery Verification Service -> HQ Store Service: getDelivery(referenceNo, storeId)
525.
HQ Store Service -> Delivery Verification Service: expected items / invalid
526.
Delivery Verification Service -> Receiving Use Case: expected items / invalid
Alternatif / keputusan
- 
Cabang 1: reference invalid
527.
Receiving Use Case -> Inventory UI: reject receiving
- 
Cabang 2: reference valid
Loop: scan / count actual items
528.
Staff Inventori -> Inventory UI: input actual qty by item
529.
Inventory UI -> Receiving Use Case: captureActual(item, qty)
530.
Receiving Use Case -> Receiving Repository: save receiving line draft
531.
Receiving Repository -> Store DB: upsert receiving line
532.
Store DB -> Receiving Repository: saved
533.
Receiving Repository -> Receiving Use Case: ok
534.
Receiving Use Case -> Receiving Use Case: compare actual vs expected
Alternatif / keputusan
- 
Cabang 1: discrepancy exists
535.
Receiving Use Case -> Inventory UI: minta catat gap receiving
536.
Staff Inventori -> Inventory UI: input short/over/damaged/wrong item reason
537.
Inventory UI -> Receiving Use Case: save discrepancy
538.
Receiving Use Case -> Receiving Repository: save discrepancy record
539.
Receiving Repository -> Store DB: insert discrepancy
540.
Store DB -> Receiving Repository: saved
541.
Staff Inventori -> Inventory UI: konfirmasi receiving
542.
Inventory UI -> Receiving Use Case: finalizeReceiving()
543.
Receiving Use Case -> Receiving Repository: post stock receiving and status
544.
Receiving Repository -> Store DB: insert receiving doc + stock ledger
545.
Store DB -> Receiving Repository: saved
546.
Receiving Repository -> Receiving Use Case: finalized
547.
Receiving Use Case -> Audit Service: log receiving event
548.
Audit Service -> Store DB: insert audit log
549.
Store DB -> Audit Service: saved
550.
Audit Service -> Receiving Use Case: ok

551.
Receiving Use Case -> Inventory UI: receiving completed
Alternate and exception considerations
Alternate flow
- 
Receiving parsial dilakukan untuk pengiriman yang belum lengkap.
- 
Receiving dilakukan per carton/box sebelum per-item detail.
Exception flow
- 
Referensi delivery tidak valid.
- 
Barang lebih/kurang dari expected tanpa penjelasan yang dapat diterima.
- 
Koneksi HQ tidak tersedia dan data referensi belum ada di lokal.
Business rules and implementation notes
- 
Receiving harus include verifikasi delivery/PO.
- 
Selisih receiving harus terdokumentasi, tidak boleh hilang diam-diam.
- 
Sequence ini menyentuh persistence layer; pastikan boundary transaksi, idempotency, dan partial failure
handling didefinisikan jelas pada implementation.
- 
Ada dependency ke external service/integration; retry policy, timeout, fallback, dan audit trail tidak boleh
dibiarkan implisit.
- 
Audit logging diperlakukan sebagai kebutuhan implementasi, bukan kosmetik; event penting harus
memiliki correlation id dan actor context.

UC-26 - Transfer Stok
Memindahkan stok antar lokasi/store dengan jejak
transaksi yang jelas.
Primary actor
Staff Inventori
Supporting actors
HQ Store Service
Trigger
Staff inventori membuat atau memproses transfer stok.
Preconditions
- Item dan lokasi sumber/tujuan valid. - Stock tersedia
di lokasi sumber.
Postconditions
- Dokumen transfer tercatat dan stok/commitment
berubah sesuai status transfer.
Source sequence file
uc26_transfer_stok_sequence.puml
Traceability
Use case spec + activity spec
(uc26_transfer_stok_activity.puml)
Interaction participants
Participant
Tipe
Peran pada sequence
Staff Inventori
Actor
Memicu atau menerima hasil bisnis
dari flow.
Inventory UI
UI/Boundary
Mewakili UI/boundary yang
berinteraksi langsung dengan actor.
Stock Transfer Use Case
Application/Service
Menjalankan orkestrasi use case,
validasi, atau layanan aplikasi.
Transfer Repository
Repository/External Entity
Mewakili repository atau sistem
eksternal yang terlibat.
Store DB
Database
Penyimpanan data transaksi, policy,
audit, atau ledger.
HQ Store Service
Repository/External Entity
Mewakili repository atau sistem
eksternal yang terlibat.
Main interaction flow
552.
Staff Inventori -> Inventory UI: buat / proses transfer stok
553.
Inventory UI -> Stock Transfer Use Case: createTransfer(item, qty, source, destination)
554.
Stock Transfer Use Case -> Store DB: validate source stock and destination
555.
Store DB -> Stock Transfer Use Case: valid / invalid
Alternatif / keputusan

- 
Cabang 1: invalid stock or destination
556.
Stock Transfer Use Case -> Inventory UI: reject transfer
- 
Cabang 2: valid
Alternatif / keputusan
- 
Cabang 1: transfer request only
557.
Stock Transfer Use Case -> Transfer Repository: save transfer request
558.
Transfer Repository -> Store DB: insert transfer doc status REQUESTED
559.
Store DB -> Transfer Repository: saved
- 
Cabang 2: direct process
560.
Stock Transfer Use Case -> Transfer Repository: reserve / deduct source stock
561.
Transfer Repository -> Store DB: insert transfer doc + stock commitment/ledger
562.
Store DB -> Transfer Repository: saved
Opsional: transfer antar store perlu notifikasi HQ
563.
Stock Transfer Use Case -> HQ Store Service: publish transfer event
564.
HQ Store Service -> Stock Transfer Use Case: accepted
565.
Stock Transfer Use Case -> Inventory UI: transfer saved
Alternate and exception considerations
Alternate flow
- 
Transfer hanya request dan menunggu approval/proses toko tujuan.
- 
Transfer antar backroom-rak dalam satu toko disederhanakan sebagai internal transfer.
Exception flow
- 
Stok sumber tidak cukup.
- 
Lokasi tujuan tidak valid.
- 
Transfer dibatalkan sebelum dikirim.
Business rules and implementation notes
- 
- Tidak boleh memindahkan stok melebihi available stock.
- 
Sequence ini menyentuh persistence layer; pastikan boundary transaksi, idempotency, dan partial failure
handling didefinisikan jelas pada implementation.
- 
Ada dependency ke external service/integration; retry policy, timeout, fallback, dan audit trail tidak boleh
dibiarkan implisit.

UC-27 - Replenishment Rak
Memindahkan stok dari backroom ke rak display agar
ketersediaan barang untuk penjualan terjaga.
Primary actor
Staff Inventori
Supporting actors
Device
Trigger
Staff inventori menjalankan replenishment.
Preconditions
- Ada kebutuhan replenishment. - Stok tersedia di
backroom atau lokasi sumber.
Postconditions
- Rak terisi ulang dan movement internal tercatat.
Source sequence file
uc27_replenishment_rak_sequence.puml
Traceability
Use case spec + activity spec
(uc27_replenishment_rak_activity.puml)
Interaction participants
Participant
Tipe
Peran pada sequence
Staff Inventori
Actor
Memicu atau menerima hasil bisnis
dari flow.
Inventory UI
UI/Boundary
Mewakili UI/boundary yang
berinteraksi langsung dengan actor.
Replenishment Service
Application/Service
Menjalankan orkestrasi use case,
validasi, atau layanan aplikasi.
Stock Repository
Repository/External Entity
Mewakili repository atau sistem
eksternal yang terlibat.
Store DB
Database
Penyimpanan data transaksi, policy,
audit, atau ledger.
Main interaction flow
566.
Staff Inventori -> Inventory UI: buka daftar replenishment
567.
Inventory UI -> Replenishment Service: getReplenishmentCandidates(location)
568.
Replenishment Service -> Stock Repository: read min shelf qty and backroom stock
569.
Stock Repository -> Store DB: query shelf deficit and source stock
570.
Store DB -> Stock Repository: candidate list
571.
Stock Repository -> Replenishment Service: candidates
572.
Replenishment Service -> Inventory UI: item yang perlu direplenish
Loop: untuk setiap item yang dipindah
## 573. Staff Inventori -> Inventory UI: konfirmasi qty dipindah

## 574. Inventory UI -> Replenishment Service: moveInternalStock(item, qty, source, shelf)
## 575. Replenishment Service -> Store DB: validate source availability and shelf capacity
## 576. Store DB -> Replenishment Service: valid / invalid
Alternatif / keputusan
- Cabang 1: valid
577.
Replenishment Service -> Stock Repository: save internal movement
578.
Stock Repository -> Store DB: update location stock ledger
579.
Store DB -> Stock Repository: saved
580.
Stock Repository -> Replenishment Service: ok
581.
Replenishment Service -> Inventory UI: move confirmed
- Cabang 2: invalid
582.
Replenishment Service -> Inventory UI: reject move
Alternate and exception considerations
Alternate flow
- 
Replenishment dipicu berdasarkan min shelf quantity.
- 
Sebagian item tidak tersedia sehingga dipindahkan parsial.
Exception flow
- 
Stok sumber ternyata tidak ada saat diambil.
- 
Lokasi rak tidak valid atau penuh.
Business rules and implementation notes
- 
- Replenishment tidak mengubah total on-hand toko, hanya lokasi internalnya.
- 
Sequence ini menyentuh persistence layer; pastikan boundary transaksi, idempotency, dan partial failure
handling didefinisikan jelas pada implementation.
- 
Ada dependency ke external service/integration; retry policy, timeout, fallback, dan audit trail tidak boleh
dibiarkan implisit.

UC-28 - Stock Adjustment
Mengoreksi saldo stok karena kehilangan, kerusakan,
mismatch sistem, atau sebab operasional lain.
Primary actor
Staff Inventori / Supervisor
Supporting actors
Supervisor Approval
Trigger
Staff inventori mengajukan adjustment stok.
Preconditions
- Item target dapat diidentifikasi. - Alasan adjustment
tersedia.
Postconditions
- Saldo stok berubah secara sah dan dapat diaudit.
Source sequence file
uc28_stock_adjustment_sequence.puml
Traceability
Use case spec + activity spec
(uc28_stock_adjustment_activity.puml)
Interaction participants
Participant
Tipe
Peran pada sequence
Staff Inventori
Actor
Memicu atau menerima hasil bisnis
dari flow.
Supervisor
Actor
Memicu atau menerima hasil bisnis
dari flow.
Inventory UI
UI/Boundary
Mewakili UI/boundary yang
berinteraksi langsung dengan actor.
Stock Adjustment Use Case
Application/Service
Menjalankan orkestrasi use case,
validasi, atau layanan aplikasi.
Approval Service
Application/Service
Menjalankan orkestrasi use case,
validasi, atau layanan aplikasi.
Audit Service
Application/Service
Menjalankan orkestrasi use case,
validasi, atau layanan aplikasi.
Stock Repository
Repository/External Entity
Mewakili repository atau sistem
eksternal yang terlibat.
Store DB
Database
Penyimpanan data transaksi, policy,
audit, atau ledger.
Main interaction flow
583.
Staff Inventori -> Inventory UI: ajukan stock adjustment

584.
Inventory UI -> Stock Adjustment Use Case: adjustStock(item, qty, direction, reason)
585.
Stock Adjustment Use Case -> Store DB: validate item, threshold, negative stock policy
586.
Store DB -> Stock Adjustment Use Case: validation result
Alternatif / keputusan
- 
Cabang 1: item blocked or invalid
587.
Stock Adjustment Use Case -> Inventory UI: reject adjustment
- 
Cabang 2: valid request
Alternatif / keputusan
- 
Cabang 1: approval required
588.
Inventory UI -> Supervisor: minta approval jika perlu
Opsional: approval required
589.
Supervisor -> Inventory UI: approve / reject
590.
Inventory UI -> Approval Service: validate approval
591.
Approval Service -> Stock Adjustment Use Case: approved / rejected
Alternatif / keputusan
- 
Cabang 1: rejected
592.
Stock Adjustment Use Case -> Inventory UI: adjustment rejected
- 
Cabang 2: approved or not required
593.
Stock Adjustment Use Case -> Stock Repository: post stock adjustment
594.
Stock Repository -> Store DB: insert adjustment ledger + update stock balance
595.
Store DB -> Stock Repository: saved
596.
Stock Repository -> Stock Adjustment Use Case: adjustment saved
597.
Stock Adjustment Use Case -> Audit Service: log stock adjustment
598.
Audit Service -> Store DB: insert audit log
599.
Store DB -> Audit Service: saved
600.
Audit Service -> Stock Adjustment Use Case: ok
601.
Stock Adjustment Use Case -> Inventory UI: adjustment completed
Alternate and exception considerations
Alternate flow
- 
Adjustment kecil dalam tolerance dapat langsung diproses.
- 
Adjustment massal dilakukan dari hasil stock count.
Exception flow
- 
Approval ditolak.
- 
Adjustment menyebabkan stok negatif yang tidak diizinkan.
- 
Item diblokir untuk adjustment.
Business rules and implementation notes
- 
Adjustment harus reason-based dan traceable.
- 
Adjustment besar wajib approval.

- 
Sequence ini menyentuh persistence layer; pastikan boundary transaksi, idempotency, dan partial failure
handling didefinisikan jelas pada implementation.
- 
Ada dependency ke external service/integration; retry policy, timeout, fallback, dan audit trail tidak boleh
dibiarkan implisit.
- 
Flow mengandung gate approval; pastikan authorization, reason capture, dan actor accountability tercatat
secara persistable.
- 
Audit logging diperlakukan sebagai kebutuhan implementasi, bukan kosmetik; event penting harus
memiliki correlation id dan actor context.

UC-29 - Stock Opname / Cycle Count
Menghitung stok fisik dan membandingkannya dengan
saldo sistem untuk menjaga akurasi inventori.
Primary actor
Staff Inventori / Supervisor
Supporting actors
Device
Trigger
Staff inventori memulai cycle count atau stock opname.
Preconditions
- Periode count atau assignment count tersedia. -
Area/item count telah ditetapkan.
Postconditions
- Hasil count tersimpan dan discrepancy teridentifikasi.
Source sequence file
uc29_stock_opname_cycle_count_sequence.puml
Traceability
Use case spec + activity spec (n/a)
Interaction participants
Participant
Tipe
Peran pada sequence
Staff Inventori
Actor
Memicu atau menerima hasil bisnis
dari flow.
Supervisor
Actor
Memicu atau menerima hasil bisnis
dari flow.
Inventory UI
UI/Boundary
Mewakili UI/boundary yang
berinteraksi langsung dengan actor.
Cycle Count Use Case
Application/Service
Menjalankan orkestrasi use case,
validasi, atau layanan aplikasi.
Audit Service
Application/Service
Menjalankan orkestrasi use case,
validasi, atau layanan aplikasi.
Count Repository
Repository/External Entity
Mewakili repository atau sistem
eksternal yang terlibat.
Store DB
Database
Penyimpanan data transaksi, policy,
audit, atau ledger.
Main interaction flow
602.
Staff Inventori -> Inventory UI: pilih batch/area count
603.
Inventory UI -> Cycle Count Use Case: startCount(batchId)
604.
Cycle Count Use Case -> Count Repository: load count assignment and snapshot
605.
Count Repository -> Store DB: read count batch and system snapshot
606.
Store DB -> Count Repository: count baseline

607.
Count Repository -> Cycle Count Use Case: batch ready
Loop: untuk setiap item yang dihitung
## 608. Staff Inventori -> Inventory UI: input / scan hasil fisik
## 609. Inventory UI -> Cycle Count Use Case: captureCount(item, physicalQty)
## 610. Cycle Count Use Case -> Count Repository: save count line
## 611. Count Repository -> Store DB: upsert count result
## 612. Store DB -> Count Repository: saved
## 613. Count Repository -> Cycle Count Use Case: ok
614.
Cycle Count Use Case -> Cycle Count Use Case: compare physical vs system
Alternatif / keputusan
- 
Cabang 1: double count required
615.
Cycle Count Use Case -> Inventory UI: minta double count item bernilai tinggi / mismatch besar
616.
Staff Inventori -> Inventory UI: review dan konfirmasi hasil
617.
Inventory UI -> Cycle Count Use Case: finalizeCount()
618.
Cycle Count Use Case -> Count Repository: save discrepancy summary
619.
Count Repository -> Store DB: update count status finalized
620.
Store DB -> Count Repository: saved
621.
Count Repository -> Cycle Count Use Case: finalized
Opsional: policy allows auto-adjustment
## 622. Cycle Count Use Case -> Inventory UI: hasil siap diturunkan ke stock adjustment
623.
Cycle Count Use Case -> Audit Service: log cycle count
624.
Audit Service -> Store DB: insert audit log
625.
Store DB -> Audit Service: saved
626.
Audit Service -> Cycle Count Use Case: ok
627.
Cycle Count Use Case -> Inventory UI: count completed
Alternate and exception considerations
Alternate flow
- 
Double count dijalankan untuk item bernilai tinggi.
- 
Supervisor mereview item dengan mismatch besar.
Exception flow
- 
Batch count dibatalkan.
- 
Data count tidak lengkap.
- 
Adjustment otomatis ditolak karena butuh approval.
Business rules and implementation notes
- 
- Cycle count harus punya snapshot waktu yang jelas agar selisih dapat dijelaskan.
- 
Sequence ini menyentuh persistence layer; pastikan boundary transaksi, idempotency, dan partial failure
handling didefinisikan jelas pada implementation.
- 
Ada dependency ke external service/integration; retry policy, timeout, fallback, dan audit trail tidak boleh
dibiarkan implisit.
- 
Flow mengandung gate approval; pastikan authorization, reason capture, dan actor accountability tercatat
secara persistable.
- 
Audit logging diperlakukan sebagai kebutuhan implementasi, bukan kosmetik; event penting harus
memiliki correlation id dan actor context.

UC-30 - Kelola Barang Rusak / Expired
Mengidentifikasi dan memproses barang rusak atau
kedaluwarsa agar tidak dijual ke pelanggan.
Primary actor
Staff Inventori
Supporting actors
Supervisor (opsional)
Trigger
Staff inventori memproses barang rusak/expired.
Preconditions
- Barang teridentifikasi rusak/expired atau mendekati
expired. - Kategori disposal tersedia.
Postconditions
- Barang tidak lagi tersedia untuk penjualan normal. -
Status dan lokasi stok tercatat sesuai tindakan.
Source sequence file
uc30_kelola_barang_rusak_expired_sequence.puml
Traceability
Use case spec + activity spec (n/a)
Interaction participants
Participant
Tipe
Peran pada sequence
Staff Inventori
Actor
Memicu atau menerima hasil bisnis
dari flow.
Supervisor
Actor
Memicu atau menerima hasil bisnis
dari flow.
Inventory UI
UI/Boundary
Mewakili UI/boundary yang
berinteraksi langsung dengan actor.
Damaged Expired Handling Use Case
Application/Service
Menjalankan orkestrasi use case,
validasi, atau layanan aplikasi.
Approval Service
Application/Service
Menjalankan orkestrasi use case,
validasi, atau layanan aplikasi.
Stock Repository
Repository/External Entity
Mewakili repository atau sistem
eksternal yang terlibat.
Store DB
Database
Penyimpanan data transaksi, policy,
audit, atau ledger.
Main interaction flow
628.
Staff Inventori -> Inventory UI: pilih item dan qty rusak/expired
629.
Inventory UI -> Damaged Expired Handling Use Case: processNonSellable(item, qty, category)
630.
Damaged Expired Handling Use Case -> Store DB: validate available qty and policy
631.
Store DB -> Damaged Expired Handling Use Case: valid / invalid / review required

Alternatif / keputusan
- 
Cabang 1: invalid qty or policy violation
632.
Damaged Expired Handling Use Case -> Inventory UI: reject handling
- 
Cabang 2: valid
Opsional: supervisor review required
633.
Inventory UI -> Supervisor: minta approval kategori/tindakan
634.
Supervisor -> Inventory UI: approve / reject
635.
Inventory UI -> Approval Service: validate approval
636.
Approval Service -> Damaged Expired Handling Use Case: approved / rejected
Alternatif / keputusan
- 
Cabang 1: approved or not required
637.
Damaged Expired Handling Use Case -> Stock Repository: move stock to
damaged/expired/quarantine/RTV bucket
638.
Stock Repository -> Store DB: update stock status and location ledger
639.
Store DB -> Stock Repository: saved
640.
Stock Repository -> Damaged Expired Handling Use Case: ok
641.
Damaged Expired Handling Use Case -> Inventory UI: non-sellable item processed
- 
Cabang 2: rejected
642.
Damaged Expired Handling Use Case -> Inventory UI: action rejected
Alternate and exception considerations
Alternate flow
- 
Barang ditandai quarantine untuk inspeksi lebih lanjut.
- 
Barang dikembalikan ke vendor mengikuti proses RTV.
Exception flow
- 
Item tidak eligible untuk disposal tanpa verifikasi tambahan.
- 
Kuantitas melebihi stok tersedia.
Business rules and implementation notes
- 
- Barang expired tidak boleh tetap berada pada sellable stock.
- 
Sequence ini menyentuh persistence layer; pastikan boundary transaksi, idempotency, dan partial failure
handling didefinisikan jelas pada implementation.
- 
Ada dependency ke external service/integration; retry policy, timeout, fallback, dan audit trail tidak boleh
dibiarkan implisit.
- 
Flow mengandung gate approval; pastikan authorization, reason capture, dan actor accountability tercatat
secara persistable.

UC-31 - Verifikasi Delivery / PO
Memastikan barang yang diterima mengacu ke
dokumen supply yang sah.
Primary actor
Staff Inventori
Supporting actors
HQ Store Service
Trigger
Receiving dimulai.
Preconditions
- Nomor delivery/PO tersedia.
Postconditions
- Dokumen receiving tervalidasi.
Source sequence file
uc31_verifikasi_delivery_po_sequence.puml
Traceability
Use case spec + activity spec
(uc31_verifikasi_delivery_po_activity.puml)
Interaction participants
Participant
Tipe
Peran pada sequence
Staff Inventori
Actor
Memicu atau menerima hasil bisnis
dari flow.
Inventory UI
UI/Boundary
Mewakili UI/boundary yang
berinteraksi langsung dengan actor.
Delivery Verification Service
Application/Service
Menjalankan orkestrasi use case,
validasi, atau layanan aplikasi.
HQ Store Service
Repository/External Entity
Mewakili repository atau sistem
eksternal yang terlibat.
Store DB
Database
Penyimpanan data transaksi, policy,
audit, atau ledger.
Main interaction flow
643.
Staff Inventori -> Inventory UI: input referensi delivery/PO
644.
Inventory UI -> Delivery Verification Service: verify(referenceNo, storeId)
Alternatif / keputusan
- 
Cabang 1: cached local reference tersedia
645.
Delivery Verification Service -> Store DB: read local delivery/PO cache
646.
Store DB -> Delivery Verification Service: document / none
- 
Cabang 2: query HQ
647.
Delivery Verification Service -> HQ Store Service: fetch delivery/PO(referenceNo, storeId)

648.
HQ Store Service -> Delivery Verification Service: document / invalid
Alternatif / keputusan
- 
Cabang 1: document valid dan milik toko
649.
Delivery Verification Service -> Inventory UI: expected items ready
- 
Cabang 2: invalid or not owned by store
650.
Delivery Verification Service -> Inventory UI: verification failed
Alternate and exception considerations
Alternate flow
- 
- Data diambil dari cache lokal.
Exception flow
- 
- Dokumen tidak valid atau bukan milik toko.
Business rules and implementation notes
- 
- Receiving tanpa referensi hanya boleh jika policy exception mengizinkan.
- 
Sequence ini menyentuh persistence layer; pastikan boundary transaksi, idempotency, dan partial failure
handling didefinisikan jelas pada implementation.
- 
Ada dependency ke external service/integration; retry policy, timeout, fallback, dan audit trail tidak boleh
dibiarkan implisit.

UC-32 - Catat Selisih Receiving
Mencatat gap antara quantity expected dan quantity
actual saat receiving.
Primary actor
Staff Inventori
Supporting actors
Supervisor
Trigger
Sistem mendeteksi mismatch expected vs actual.
Preconditions
- Receiving sedang berlangsung dan ditemukan selisih.
Postconditions
- Discrepancy receiving tersimpan.
Source sequence file
uc32_catat_selisih_receiving_sequence.puml
Traceability
Use case spec + activity spec
(uc32_catat_selisih_receiving_activity.puml)
Interaction participants
Participant
Tipe
Peran pada sequence
Staff Inventori
Actor
Memicu atau menerima hasil bisnis
dari flow.
Supervisor
Actor
Memicu atau menerima hasil bisnis
dari flow.
Inventory UI
UI/Boundary
Mewakili UI/boundary yang
berinteraksi langsung dengan actor.
Receiving Discrepancy Service
Application/Service
Menjalankan orkestrasi use case,
validasi, atau layanan aplikasi.
Receiving Repository
Repository/External Entity
Mewakili repository atau sistem
eksternal yang terlibat.
Store DB
Database
Penyimpanan data transaksi, policy,
audit, atau ledger.
Main interaction flow
651.
Inventory UI -> Receiving Discrepancy Service: discrepancy detected(receivingId, item, expected, actual)
652.
Receiving Discrepancy Service -> Inventory UI: request discrepancy reason
653.
Staff Inventori -> Inventory UI: pilih jenis gap + catatan
654.
Inventory UI -> Receiving Discrepancy Service: saveGap(type, note)
Alternatif / keputusan
- 
Cabang 1: large discrepancy

655.
Inventory UI -> Supervisor: minta review discrepancy
656.
Supervisor -> Inventory UI: acknowledge / approve
657.
Receiving Discrepancy Service -> Receiving Repository: persist discrepancy
658.
Receiving Repository -> Store DB: insert receiving discrepancy record
659.
Store DB -> Receiving Repository: saved
660.
Receiving Repository -> Receiving Discrepancy Service: ok
661.
Receiving Discrepancy Service -> Inventory UI: discrepancy recorded
Alternate and exception considerations
Alternate flow
- 
- Supervisor diminta untuk selisih besar.
Exception flow
- 
- User mencoba menutup receiving tanpa menjelaskan gap.
Business rules and implementation notes
- 
- Semua mismatch receiving wajib tercatat.
- 
Sequence ini menyentuh persistence layer; pastikan boundary transaksi, idempotency, dan partial failure
handling didefinisikan jelas pada implementation.
- 
Ada dependency ke external service/integration; retry policy, timeout, fallback, dan audit trail tidak boleh
dibiarkan implisit.
- 
Flow mengandung gate approval; pastikan authorization, reason capture, dan actor accountability tercatat
secara persistable.

UC-33 - Cetak Label Harga / Rak
Mencetak label harga atau label rak yang akurat untuk
mendukung display toko.
Primary actor
Staff Inventori
Supporting actors
Store Device
Trigger
Staff inventori memilih cetak label.
Preconditions
- Template label tersedia. - Data harga/SKU valid.
Postconditions
- Label tercetak atau job gagal dengan error yang bisa
ditindaklanjuti.
Source sequence file
uc33_cetak_label_harga_rak_sequence.puml
Traceability
Use case spec + activity spec
(uc33_cetak_label_harga_rak_activity.puml)
Interaction participants
Participant
Tipe
Peran pada sequence
Staff Inventori
Actor
Memicu atau menerima hasil bisnis
dari flow.
Inventory UI
UI/Boundary
Mewakili UI/boundary yang
berinteraksi langsung dengan actor.
Label Printing Service
Application/Service
Menjalankan orkestrasi use case,
validasi, atau layanan aplikasi.
Label Repository
Repository/External Entity
Mewakili repository atau sistem
eksternal yang terlibat.
Label Printer
Repository/External Entity
Mewakili repository atau sistem
eksternal yang terlibat.
Store DB
Database
Penyimpanan data transaksi, policy,
audit, atau ledger.
Main interaction flow
662.
Staff Inventori -> Inventory UI: pilih item/rak dan jumlah label
663.
Inventory UI -> Label Printing Service: printLabel(target, qty, template)
664.
Label Printing Service -> Label Repository: load active price, SKU, barcode, template data
665.
Label Repository -> Store DB: query label payload
666.
Store DB -> Label Repository: payload / invalid
667.
Label Repository -> Label Printing Service: label data
Alternatif / keputusan

- 
Cabang 1: data harga belum sinkron atau template invalid
668.
Label Printing Service -> Inventory UI: reject print job
- 
Cabang 2: data valid
Opsional: preview label
669.
Label Printing Service -> Inventory UI: render preview
670.
Label Printing Service -> Label Printer: print(labelPayload, qty)
671.
Label Printer -> Label Printing Service: success / failed
672.
Label Printing Service -> Inventory UI: print result
Alternate and exception considerations
Alternate flow
- 
Cetak label massal dari hasil price change atau replenishment.
- 
Preview label ditampilkan sebelum cetak.
Exception flow
- 
Printer tidak tersedia.
- 
Data harga belum sinkron.
- 
Template label tidak cocok dengan device.
Business rules and implementation notes
- 
- Label harus menggunakan harga aktif yang berlaku untuk toko terkait.
- 
Sequence ini menyentuh persistence layer; pastikan boundary transaksi, idempotency, dan partial failure
handling didefinisikan jelas pada implementation.
- 
Ada dependency ke external service/integration; retry policy, timeout, fallback, dan audit trail tidak boleh
dibiarkan implisit.

UC-34 - Lihat Laporan Operasional Toko
Melihat KPI dan laporan operasional toko untuk
monitoring performa dan kontrol harian.
Primary actor
Store Manager
Supporting actors
HQ Store Service
Trigger
Manager membuka menu laporan operasional.
Preconditions
- Manager terautentikasi. - Data laporan tersedia untuk
periode yang diminta.
Postconditions
- Laporan tampil untuk keperluan
operasional/pengambilan keputusan.
Source sequence file
uc34_lihat_laporan_operasional_toko_sequence.puml
Traceability
Use case spec + activity spec (n/a)
Interaction participants
Participant
Tipe
Peran pada sequence
Store Manager
Actor
Memicu atau menerima hasil bisnis
dari flow.
Backoffice UI
UI/Boundary
Mewakili UI/boundary yang
berinteraksi langsung dengan actor.
Operational Report Query
Application/Service
Menjalankan orkestrasi use case,
validasi, atau layanan aplikasi.
Report Repository
Repository/External Entity
Mewakili repository atau sistem
eksternal yang terlibat.
Store DB
Database
Penyimpanan data transaksi, policy,
audit, atau ledger.
HQ Store Service
Repository/External Entity
Mewakili repository atau sistem
eksternal yang terlibat.
Main interaction flow
673.
Store Manager -> Backoffice UI: pilih periode / filter laporan
674.
Backoffice UI -> Operational Report Query: getOperationalReport(period, filters)
Alternatif / keputusan
- 
Cabang 1: online and HQ metrics needed
675.
Operational Report Query -> HQ Store Service: fetch supplementary KPI(period, storeId)

676.
HQ Store Service -> Operational Report Query: KPI snapshot / unavailable
677.
Operational Report Query -> Report Repository: collect sales, return, cash, inventory, sync data
678.
Report Repository -> Store DB: query operational aggregates
679.
Store DB -> Report Repository: dataset / incomplete
680.
Report Repository -> Operational Report Query: report data
Alternatif / keputusan
- 
Cabang 1: data cukup dan role authorized
681.
Operational Report Query -> Backoffice UI: show dashboard/report
Opsional: export requested
682.
Backoffice UI -> Operational Report Query: export(reportId)
683.
Operational Report Query -> Backoffice UI: export ready
- 
Cabang 2: insufficient data or unauthorized
684.
Operational Report Query -> Backoffice UI: reject report access / incomplete data
Alternate and exception considerations
Alternate flow
- 
Laporan ditampilkan dari cache lokal terakhir saat offline.
- 
Manager memfilter per shift atau per kategori.
Exception flow
- 
Data laporan belum lengkap.
- 
Akses role tidak cukup untuk laporan tertentu.
Business rules and implementation notes
- 
- Definisi metrik harus konsisten dengan data transaksi dan business day.
- 
Sequence ini menyentuh persistence layer; pastikan boundary transaksi, idempotency, dan partial failure
handling didefinisikan jelas pada implementation.
- 
Ada dependency ke external service/integration; retry policy, timeout, fallback, dan audit trail tidak boleh
dibiarkan implisit.

UC-35 - Sinkronisasi Data Toko
Menyamakan data transaksi, stok, master data, dan
status operasional toko dengan HQ services.
Primary actor
Store Manager / System
Supporting actors
HQ Store Service
Trigger
Manager menjalankan sinkronisasi manual atau sistem
menjadwalkan sync.
Preconditions
- Koneksi ke HQ tersedia atau job sync dapat
diantrikan. - Identitas toko valid.
Postconditions
- Data sukses tersinkron atau item gagal masuk daftar
reconcile.
Source sequence file
uc35_sinkronisasi_data_toko_sequence.puml
Traceability
Use case spec + activity spec
(uc35_sinkronisasi_data_toko_activity.puml)
Interaction participants
Participant
Tipe
Peran pada sequence
Store Manager
Actor
Memicu atau menerima hasil bisnis
dari flow.
Sync UI / Scheduler
UI/Boundary
Mewakili UI/boundary yang
berinteraksi langsung dengan actor.
Sync Orchestrator
Application/Service
Menjalankan orkestrasi use case,
validasi, atau layanan aplikasi.
Conflict Resolver
Application/Service
Menjalankan orkestrasi use case,
validasi, atau layanan aplikasi.
Sync Queue Repository
Repository/External Entity
Mewakili repository atau sistem
eksternal yang terlibat.
Store DB
Database
Penyimpanan data transaksi, policy,
audit, atau ledger.
HQ Store Service
Repository/External Entity
Mewakili repository atau sistem
eksternal yang terlibat.
Main interaction flow
Alternatif / keputusan
- 
Cabang 1: manual sync

685.
Store Manager -> Sync UI / Scheduler: jalankan sinkronisasi
- 
Cabang 2: scheduled sync
686.
Sync UI / Scheduler -> Sync Orchestrator: scheduled trigger
687.
Sync UI / Scheduler -> Sync Orchestrator: startSync(storeId, scope)
688.
Sync Orchestrator -> Sync Queue Repository: load outbound and inbound batches
689.
Sync Queue Repository -> Store DB: read pending transactions, stock events, master version
690.
Store DB -> Sync Queue Repository: sync batches
691.
Sync Queue Repository -> Sync Orchestrator: prepared batches
692.
Sync Orchestrator -> HQ Store Service: push outbound batches
693.
HQ Store Service -> Sync Orchestrator: push result
694.
Sync Orchestrator -> HQ Store Service: pull inbound updates
695.
HQ Store Service -> Sync Orchestrator: master data and status updates
Alternatif / keputusan
- 
Cabang 1: all success
696.
Sync Orchestrator -> Sync Queue Repository: mark synced
697.
Sync Queue Repository -> Store DB: update sync status success
698.
Store DB -> Sync Queue Repository: saved
699.
Sync Queue Repository -> Sync Orchestrator: ok
700.
Sync Orchestrator -> Sync UI / Scheduler: sync completed
- 
Cabang 2: partial failure or conflict
701.
Sync Orchestrator -> Conflict Resolver: analyze failed batches
702.
Conflict Resolver -> Sync Orchestrator: retryable / conflict / manual review
Alternatif / keputusan
- 
Cabang 1: retryable
703.
Sync Orchestrator -> HQ Store Service: retry failed batch
704.
HQ Store Service -> Sync Orchestrator: retry result
705.
Sync Orchestrator -> Sync Queue Repository: save reconcile items
706.
Sync Queue Repository -> Store DB: insert reconciliation records
707.
Store DB -> Sync Queue Repository: saved
708.
Sync Queue Repository -> Sync Orchestrator: ok
709.
Sync Orchestrator -> Sync UI / Scheduler: sync partial / failed with reconcile list
Alternate and exception considerations
Alternate flow
- 
Sync hanya data tertentu, misalnya master price atau transaksi penjualan.
- 
Retry otomatis dijalankan untuk batch gagal.
Exception flow
- 
Koneksi HQ gagal.
- 
Conflict data terjadi antara local dan HQ.
- 
Sebagian batch gagal dan perlu reconciliation.

Business rules and implementation notes
- 
Sync harus idempotent sebisa mungkin.
- 
Kegagalan sinkron tidak boleh silently drop data.
- 
Sequence ini menyentuh persistence layer; pastikan boundary transaksi, idempotency, dan partial failure
handling didefinisikan jelas pada implementation.
- 
Ada dependency ke external service/integration; retry policy, timeout, fallback, dan audit trail tidak boleh
dibiarkan implisit.

UC-36 - Mode Operasi Offline
Menjaga toko tetap dapat beroperasi ketika koneksi ke
service pusat atau provider tertentu terganggu.
Primary actor
Store Staff
Supporting actors
Store Manager, HQ Store Service, Payment Gateway /
EDC
Trigger
Sistem mendeteksi gangguan atau user mengaktifkan
mode offline sesuai policy.
Preconditions
- Gangguan konektivitas terdeteksi atau external
dependency tidak tersedia. - Store policy mengizinkan
operasi offline terbatas.
Postconditions
- Store beroperasi dalam degraded mode dengan
backlog sinkronisasi yang terkontrol.
Source sequence file
uc36_mode_operasi_offline_sequence.puml
Traceability
Use case spec + activity spec
(uc36_mode_operasi_offline_activity.puml)
Interaction participants
Participant
Tipe
Peran pada sequence
Store Staff
Actor
Memicu atau menerima hasil bisnis
dari flow.
POS UI
UI/Boundary
Mewakili UI/boundary yang
berinteraksi langsung dengan actor.
Offline Mode Manager
Application/Service
Menjalankan orkestrasi use case,
validasi, atau layanan aplikasi.
Sync Orchestrator
Application/Service
Menjalankan orkestrasi use case,
validasi, atau layanan aplikasi.
Store DB
Database
Penyimpanan data transaksi, policy,
audit, atau ledger.
HQ Store Service
Repository/External Entity
Mewakili repository atau sistem
eksternal yang terlibat.
Payment Gateway / EDC
Repository/External Entity
Mewakili repository atau sistem
eksternal yang terlibat.
Main interaction flow
710.
Offline Mode Manager -> HQ Store Service: health check HQ dependency

711.
HQ Store Service -> Offline Mode Manager: reachable / unreachable
712.
Offline Mode Manager -> Payment Gateway / EDC: health check payment dependency
713.
Payment Gateway / EDC -> Offline Mode Manager: reachable / unreachable
Alternatif / keputusan
- 
Cabang 1: policy allows degraded offline mode
714.
Offline Mode Manager -> Store DB: enable offline flags and local cache policy
715.
Store DB -> Offline Mode Manager: offline mode enabled
716.
Offline Mode Manager -> POS UI: degraded features and blocked features
717.
Store Staff -> POS UI: jalankan operasi yang diizinkan
718.
POS UI -> Store DB: persist offline transactions and pending sync markers
719.
Store DB -> POS UI: locally saved
Loop: saat koneksi belum pulih
720.
Offline Mode Manager -> HQ Store Service: retry connectivity probe
721.
HQ Store Service -> Offline Mode Manager: still down / up
722.
Offline Mode Manager -> Sync Orchestrator: connectivity restored, reconcile backlog
723.
Sync Orchestrator -> Store DB: load pending offline events
724.
Store DB -> Sync Orchestrator: backlog
725.
Sync Orchestrator -> HQ Store Service: sync backlog
726.
HQ Store Service -> Sync Orchestrator: sync result
727.
Sync Orchestrator -> POS UI: backlog status updated
- 
Cabang 2: cache insufficient or policy forbids
728.
Offline Mode Manager -> POS UI: block risky operations
Alternate and exception considerations
Alternate flow
- 
Hanya fitur tertentu yang offline, misalnya loyalty nonaktif tetapi cash sales tetap berjalan.
- 
Manager menonaktifkan sementara fitur yang terlalu berisiko saat offline.
Exception flow
- 
Data cache tidak memadai sehingga operasi tertentu harus diblokir.
- 
Clock/time drift membuat sinkronisasi berisiko conflict.
Business rules and implementation notes
- 
Offline mode bukan bypass total; hanya fungsi yang aman secara bisnis yang boleh tetap jalan.
- 
Semua transaksi offline harus dapat direconcile saat koneksi kembali. 7. Supporting Use Case Specifications
Catatan: Section ini memuat supporting/internal use cases yang muncul pada relasi include/extend atau
reusable operational step. Detailnya dibuat lebih ringkas, tetapi tetap cukup untuk design review dan turunan
test. UC-03 - Kelola Keranjang Tujuan Memelihara isi transaksi penjualan aktif sebelum finalisasi pembayaran.
Primary Actor Kasir Supporting Actors Store Device Preconditions - Ada transaksi penjualan aktif. Trigger
Kasir menambah, mengubah, atau menghapus item dari keranjang. Postconditions - Keranjang aktif ter-
update. Main Flow 1. Kasir scan/cari item. 2. Sistem menambahkan item ke keranjang. 3. Kasir mengubah
kuantitas atau menghapus item bila perlu. 4. Sistem menghitung ulang harga. Alternate Flow
- 
Kasir menahan item tertentu untuk konfirmasi harga. Exception Flow
- 
Item tidak dapat dijual atau stok nol. Business Rules
- 
Semua perubahan keranjang harus memicu pricing ulang. UC-06 - Terbitkan Receipt Tujuan Menyediakan
bukti transaksi kepada pelanggan setelah transaksi sah selesai. Primary Actor Kasir Supporting Actors Store

Device Preconditions - Transaksi final dengan payment sah tersedia. Trigger Sistem menyelesaikan transaksi
penjualan atau return/refund. Postconditions - Receipt tersedia atau kegagalan cetak tercatat. Main Flow 1.
Sistem membentuk data receipt. 2. Sistem memilih channel receipt. 3. Receipt dicetak atau dikirim digital. 4.
Sistem menyimpan referensi receipt. Alternate Flow
- 
Reprint dilakukan kemudian melalui receipt lookup. Exception Flow
- 
Printer gagal atau kehabisan kertas. Business Rules
- 
Receipt hanya untuk transaksi final. UC-08 - Cari / Scan Produk Tujuan Mengidentifikasi produk yang akan
dimasukkan ke transaksi atau proses inventori. Primary Actor Kasir / Staff Inventori Supporting Actors Store
Device Preconditions - Perangkat scan atau data pencarian tersedia. Trigger User memindai barcode atau
mengetik kata kunci SKU/nama. Postconditions - Produk teridentifikasi. Main Flow 1. User scan barcode atau
masukkan keyword. 2. Sistem mencari produk. 3. Sistem menampilkan hasil yang cocok. 4. User memilih item
target. Alternate Flow
- 
Barcode tidak terbaca lalu fallback ke pencarian manual. Exception Flow
- 
Produk tidak ditemukan atau master data belum tersedia. Business Rules
- 
Pencarian harus mendukung SKU, barcode, dan nama pendek minimal. UC-09 - Hitung Harga, Promo, Pajak
Tujuan Menghasilkan total transaksi yang benar berdasarkan item, promo, voucher, dan aturan pajak. Primary
Actor System Supporting Actors Loyalty Service, HQ rule cache Preconditions - Keranjang transaksi tersedia.
Trigger Ada perubahan item, member/voucher, atau metode pembayaran yang memengaruhi total.
Postconditions - Nilai transaksi terbaru tersedia. Main Flow 1. Sistem membaca item basket. 2. Sistem
menerapkan promo dan pajak yang relevan. 3. Sistem menghitung subtotal, diskon, pajak, dan grand total. 4.
Sistem mengembalikan hasil ke caller. Alternate Flow
- 
Rule berasal dari cache lokal saat offline. Exception Flow
- 
Rule conflict atau data harga hilang. Business Rules
- 
Pricing harus deterministic untuk input yang sama pada timestamp bisnis yang sama. UC-13 - Lookup Receipt
Tujuan Menemukan transaksi/receipt sebelumnya sebagai referensi exception atau customer service. Primary
Actor Kasir Supporting Actors Store Manager Preconditions - Kunci pencarian tersedia. Trigger Kasir perlu
mencari transaksi lama. Postconditions - Receipt target ditemukan atau tidak. Main Flow 1. Kasir
memasukkan nomor receipt, kartu, tanggal, atau atribut lain. 2. Sistem mencari transaksi yang cocok. 3.
Sistem menampilkan hasil dan detail receipt. Alternate Flow
- 
Pencarian menggunakan data lokal terbatas saat offline. Exception Flow
- 
Receipt tidak ditemukan. Business Rules
- 
Hak akses data receipt harus mengikuti role. UC-14 - Validasi Return Policy Tujuan Memastikan permintaan
return memenuhi kebijakan toko dan regulasi yang berlaku. Primary Actor System Supporting Actors
Supervisor Preconditions - Item return dan referensi transaksi tersedia atau informasi minimum tersedia.
Trigger Proses return dimulai atau item return dipilih. Postconditions - Keputusan eligibility return tersedia.
Main Flow 1. Sistem mengecek tanggal pembelian, kategori item, kondisi, dan metode bayar. 2. Sistem
mengecek aturan no-return/final sale/perishable. 3. Sistem menentukan eligible/ineligible beserta syaratnya.
Alternate Flow
- 
Policy override tersedia dengan approval supervisor. Exception Flow
- 
Data pembelian tidak cukup untuk validasi. Business Rules
- 
Policy return harus konsisten antar kanal yang relevan, kecuali memang store-specific. UC-15 - Supervisor
Approval Tujuan Memberikan keputusan otorisasi untuk operasi sensitif yang melewati kewenangan staf
biasa. Primary Actor Supervisor Supporting Actors Kasir / Staff Inventori Preconditions - Ada permintaan
approval aktif. - Supervisor memiliki kredensial valid. Trigger Sistem meminta approval. Postconditions -
Keputusan approval tercatat. Main Flow 1. Sistem menampilkan detail permintaan approval. 2. Supervisor
meninjau konteks, alasan, dan dampak. 3. Supervisor menyetujui atau menolak. 4. Sistem mencatat
keputusan. Alternate Flow
- 
Supervisor login langsung di terminal requester. Exception Flow
- 
Supervisor gagal autentikasi. Business Rules
- 
Approval harus mengandung siapa, kapan, apa yang diizinkan, dan untuk alasan apa. UC-16 - Catat Alasan
Exception Tujuan Merekam reason code dan catatan operasional untuk tindakan exception. Primary Actor
Kasir / Supervisor / Staff Inventori Supporting Actors System Preconditions - Ada proses exception yang

sedang berjalan. Trigger User menjalankan void, return, override, receiving gap, adjustment, atau exception
lain. Postconditions - Alasan exception tersimpan. Main Flow 1. Sistem meminta reason code. 2. User memilih
reason code dan menambah catatan bila perlu. 3. Sistem memvalidasi reason dan menyimpannya. Alternate
Flow
- 
Foto/dokumen pendukung ditambahkan. Exception Flow
- 
Reason code wajib tetapi belum diisi. Business Rules
- 
Alasan exception tidak boleh null untuk proses yang mewajibkannya. UC-17 - Input Kas Awal Tujuan Mencatat
saldo awal laci kas pada awal shift. Primary Actor Kasir Supporting Actors Supervisor Preconditions - Shift
akan dibuka. Trigger Bagian opening cash pada mulai shift dimulai. Postconditions - Opening cash tercatat.
Main Flow 1. Kasir menghitung kas awal. 2. Kasir memasukkan nominal. 3. Sistem menyimpan opening cash.
Alternate Flow
- 
Supervisor memverifikasi nominal. Exception Flow
- 
Nominal tidak valid. Business Rules
- 
Opening cash bagian wajib dari start shift. UC-21 - Rekonsiliasi Kas Tujuan Membandingkan kas fisik dan kas
sistem untuk menemukan selisih shift. Primary Actor Kasir / Supervisor Supporting Actors System
Preconditions - Shift aktif yang akan ditutup memiliki expected cash balance. Trigger Close shift atau cash
audit dilakukan. Postconditions - Hasil rekonsiliasi tersimpan. Main Flow 1. User memasukkan hasil hitung
fisik. 2. Sistem membandingkan dengan expected balance. 3. Sistem menampilkan selisih. 4. User
mengonfirmasi hasil. Alternate Flow
- 
Supervisor menandatangani selisih di atas tolerance. Exception Flow
- 
Perhitungan ulang diperlukan karena input salah. Business Rules
- 
Selisih harus dapat ditelusuri ke cash movement dan transaksi. UC-22 - Generate X / Z Report Tujuan
Menghasilkan ringkasan transaksi dan kas untuk kontrol shift atau harian. Primary Actor Supervisor
Supporting Actors Store Device Preconditions - Data transaksi dan kas tersedia untuk periode/report type.
Trigger User meminta X report atau Z report. Postconditions - Report tersedia. Main Flow 1. User memilih
jenis report. 2. Sistem mengumpulkan data relevan. 3. Sistem menghasilkan report. 4. Report ditampilkan atau
dicetak. Alternate Flow
- 
Report disimpan digital. Exception Flow
- 
Data report belum lengkap. Business Rules
- 
Z report umumnya final/closing; X report interim/control. UC-31 - Verifikasi Delivery / PO Tujuan Memastikan
barang yang diterima mengacu ke dokumen supply yang sah. Primary Actor Staff Inventori Supporting Actors
HQ Store Service Preconditions - Nomor delivery/PO tersedia. Trigger Receiving dimulai. Postconditions -
Dokumen receiving tervalidasi. Main Flow 1. User memasukkan referensi delivery/PO. 2. Sistem mengambil
expected items. 3. Sistem memvalidasi dokumen aktif dan tujuan toko. Alternate Flow
- 
Data diambil dari cache lokal. Exception Flow
- 
Dokumen tidak valid atau bukan milik toko. Business Rules
- 
Receiving tanpa referensi hanya boleh jika policy exception mengizinkan. UC-32 - Catat Selisih Receiving
Tujuan Mencatat gap antara quantity expected dan quantity actual saat receiving. Primary Actor Staff Inventori
Supporting Actors Supervisor Preconditions - Receiving sedang berlangsung dan ditemukan selisih. Trigger
Sistem mendeteksi mismatch expected vs actual. Postconditions - Discrepancy receiving tersimpan. Main
Flow 1. User memilih item mismatch. 2. User mencatat jenis gap: short, over, damaged, wrong item. 3. Sistem
menyimpan discrepancy record. Alternate Flow
- 
Supervisor diminta untuk selisih besar. Exception Flow
- 
User mencoba menutup receiving tanpa menjelaskan gap. Business Rules
- 
Semua mismatch receiving wajib tercatat.
- 
Sequence ini menyentuh persistence layer; pastikan boundary transaksi, idempotency, dan partial failure
handling didefinisikan jelas pada implementation.
- 
Ada dependency ke external service/integration; retry policy, timeout, fallback, dan audit trail tidak boleh
dibiarkan implisit.

UC-37 - Rekonsiliasi Sync Gagal
Menyelesaikan batch sinkronisasi yang gagal agar data
toko dan HQ kembali konsisten.
Primary actor
Store Manager / Supervisor
Supporting actors
HQ Store Service
Trigger
Manager membuka daftar sync gagal atau sistem
memberi alert.
Preconditions
- Ada batch sync berstatus gagal atau parsial.
Postconditions
- Item sync gagal berkurang atau tercatat untuk
investigasi.
Source sequence file
uc37_rekonsiliasi_sync_gagal_sequence.puml
Traceability
Use case spec + activity spec
(uc37_rekonsiliasi_sync_gagal_activity.puml)
Interaction participants
Participant
Tipe
Peran pada sequence
Store Manager / Supervisor
Actor
Memicu atau menerima hasil bisnis
dari flow.
Sync Reconcile UI
UI/Boundary
Mewakili UI/boundary yang
berinteraksi langsung dengan actor.
Sync Reconciliation Use Case
Application/Service
Menjalankan orkestrasi use case,
validasi, atau layanan aplikasi.
Conflict Resolver
Application/Service
Menjalankan orkestrasi use case,
validasi, atau layanan aplikasi.
Sync Queue Repository
Repository/External Entity
Mewakili repository atau sistem
eksternal yang terlibat.
Store DB
Database
Penyimpanan data transaksi, policy,
audit, atau ledger.
HQ Store Service
Repository/External Entity
Mewakili repository atau sistem
eksternal yang terlibat.
Main interaction flow
729.
Store Manager / Supervisor -> Sync Reconcile UI: buka daftar sync gagal
730.
Sync Reconcile UI -> Sync Reconciliation Use Case: loadFailedBatches()
731.
Sync Reconciliation Use Case -> Sync Queue Repository: find failed or partial sync items
732.
Sync Queue Repository -> Store DB: query failed batches

733.
Store DB -> Sync Queue Repository: failed items
734.
Sync Queue Repository -> Sync Reconciliation Use Case: reconcile list
735.
Sync Reconciliation Use Case -> Sync Reconcile UI: show failures and cause summary
736.
Store Manager / Supervisor -> Sync Reconcile UI: pilih retry / resolve conflict / mark investigation
Alternatif / keputusan
- 
Cabang 1: retry
737.
Sync Reconcile UI -> Sync Reconciliation Use Case: retry(batchId)
738.
Sync Reconciliation Use Case -> HQ Store Service: resend or replay batch
739.
HQ Store Service -> Sync Reconciliation Use Case: success / failed
- 
Cabang 2: resolve conflict
740.
Sync Reconcile UI -> Sync Reconciliation Use Case: resolve(batchId, resolution)
741.
Sync Reconciliation Use Case -> Conflict Resolver: apply resolution
742.
Conflict Resolver -> Sync Reconciliation Use Case: resolved / unresolved
- 
Cabang 3: manual investigation
743.
Sync Reconcile UI -> Sync Reconciliation Use Case: markManual(batchId)
744.
Sync Reconciliation Use Case -> Sync Queue Repository: update reconcile status
745.
Sync Queue Repository -> Store DB: persist final status
746.
Store DB -> Sync Queue Repository: saved
747.
Sync Queue Repository -> Sync Reconciliation Use Case: ok
748.
Sync Reconciliation Use Case -> Sync Reconcile UI: reconciliation result
Alternate and exception considerations
Alternate flow
- 
- Retry otomatis berhasil tanpa intervensi manual.
Exception flow
- 
- Conflict tidak dapat di-resolve otomatis.
Business rules and implementation notes
- 
- Tidak boleh ada sync failure yang hilang tanpa status akhir.
- 
Sequence ini menyentuh persistence layer; pastikan boundary transaksi, idempotency, dan partial failure
handling didefinisikan jelas pada implementation.
- 
Ada dependency ke external service/integration; retry policy, timeout, fallback, dan audit trail tidak boleh
dibiarkan implisit.
- 
Flow mengandung gate approval; pastikan authorization, reason capture, dan actor accountability tercatat
secara persistable.

UC-38 - Autentikasi & Validasi Role
Memastikan hanya user dengan identitas dan role yang
sah yang dapat menjalankan fungsi sistem.
Primary actor
Identity Service / Store Staff
Supporting actors
System
Trigger
User login atau mengakses fungsi berizin.
Preconditions
- Kredensial atau token tersedia.
Postconditions
- Sesi user valid atau akses ditolak.
Source sequence file
uc38_autentikasi_validasi_role_sequence.puml
Traceability
Use case spec + activity spec
(uc38_autentikasi_validasi_role_activity.puml)
Interaction participants
Participant
Tipe
Peran pada sequence
Store Staff
Actor
Memicu atau menerima hasil bisnis
dari flow.
POS UI
UI/Boundary
Mewakili UI/boundary yang
berinteraksi langsung dengan actor.
Auth Service
Application/Service
Menjalankan orkestrasi use case,
validasi, atau layanan aplikasi.
Store DB
Database
Penyimpanan data transaksi, policy,
audit, atau ledger.
Identity Service
Repository/External Entity
Mewakili repository atau sistem
eksternal yang terlibat.
Main interaction flow
749.
Store Staff -> POS UI: login / akses fungsi berizin
750.
POS UI -> Auth Service: authenticate(credentials or token, requestedPermission)
Alternatif / keputusan
- 
Cabang 1: identity service online
751.
Auth Service -> Identity Service: verify identity and roles
752.
Identity Service -> Auth Service: user profile + permissions / invalid
- 
Cabang 2: offline cache allowed
753.
Auth Service -> Store DB: verify cached credential and permission

754.
Store DB -> Auth Service: cached permission / invalid
Alternatif / keputusan
- 
Cabang 1: invalid credential
755.
Auth Service -> POS UI: deny access
- 
Cabang 2: valid credential but insufficient permission
756.
Auth Service -> POS UI: forbidden
- 
Cabang 3: valid and authorized
757.
Auth Service -> Store DB: persist session/token metadata
758.
Store DB -> Auth Service: saved
759.
Auth Service -> POS UI: allow access
Alternate and exception considerations
Alternate flow
- 
- Offline credential cache dipakai sesuai policy.
Exception flow
- 
- Identity service tidak tersedia dan fallback tidak diizinkan.
Business rules and implementation notes
- 
- Authorization harus berbasis role yang sesuai fungsi.
- 
Sequence ini menyentuh persistence layer; pastikan boundary transaksi, idempotency, dan partial failure
handling didefinisikan jelas pada implementation.
- 
Ada dependency ke external service/integration; retry policy, timeout, fallback, dan audit trail tidak boleh
dibiarkan implisit.

UC-39 - Catat Audit Log
Menyimpan jejak audit atas tindakan penting yang
relevan untuk kontrol dan investigasi.
Primary actor
System
Supporting actors
All business use cases
Trigger
Use case yang mewajibkan audit selesai atau berubah
state penting.
Preconditions
- Ada aksi bisnis signifikan atau event keamanan.
Postconditions
- Audit trail tersimpan.
Source sequence file
uc39_catat_audit_log_sequence.puml
Traceability
Use case spec + activity spec
(uc01_mulai_shift_kasir_activity.puml)
Interaction participants
Participant
Tipe
Peran pada sequence
Business Use Case
UI/Boundary
Mewakili UI/boundary yang
berinteraksi langsung dengan actor.
Audit Service
Application/Service
Menjalankan orkestrasi use case,
validasi, atau layanan aplikasi.
Audit Repository
Repository/External Entity
Mewakili repository atau sistem
eksternal yang terlibat.
Store DB
Database
Penyimpanan data transaksi, policy,
audit, atau ledger.
Central Audit Store
Repository/External Entity
Mewakili repository atau sistem
eksternal yang terlibat.
Main interaction flow
760.
Business Use Case -> Audit Service: log(eventType, actor, target, context)
761.
Audit Service -> Audit Repository: build immutable audit record
762.
Audit Repository -> Store DB: insert local audit trail
Alternatif / keputusan
- 
Cabang 1: local audit storage available
763.
Store DB -> Audit Repository: saved
764.
Audit Repository -> Audit Service: local audit saved
Opsional: async central forwarding enabled

765.
Audit Service -> Central Audit Store: forward audit event async
766.
Central Audit Store -> Audit Service: accepted / unavailable
767.
Audit Service -> Business Use Case: audit success
- 
Cabang 2: local storage unavailable
768.
Store DB -> Audit Repository: failed
769.
Audit Repository -> Store DB: enqueue durable audit backlog
770.
Store DB -> Audit Repository: queued
771.
Audit Repository -> Audit Service: queued for retry
772.
Audit Service -> Business Use Case: continue with safe backlog state
Alternate and exception considerations
Alternate flow
- 
- Audit dikirim async ke central store setelah disimpan lokal.
Exception flow
- 
- Storage audit sementara tidak tersedia -> event diantrikan secara aman.
Business rules and implementation notes
- 
Aksi sensitif tidak boleh lolos tanpa audit record. 8. Traceability Ringkas
- 
UC-02 Proses Penjualan menurunkan activity diagram checkout, sequence diagram sales checkout, domain
model Sale/Cart/Payment/Receipt, dan test untuk success/declined/pending payment.
- 
UC-11 Return / Refund menurunkan sequence refund authorization, domain
ReturnTransaction/ReturnPolicy/RefundPayment, dan test policy eligibility.
- 
UC-20 Tutup Shift dan UC-23 Tutup Hari menurunkan cash reconciliation, reporting, dan operational closing
controls.
- 
UC-25 sampai UC-30 menurunkan domain stock ledger, stock movement, receiving discrepancy, adjustment
reason, dan cycle count result.
- 
UC-35 sampai UC-37 menurunkan integration architecture untuk sync queue, retry, conflict handling, serta
offline backlog. 9. Kritik Model dan Rekomendasi Perbaikan
- 
Diagram saat ini cukup rapi secara struktur paket, tetapi ada campuran antara user-goal use case dan internal
service use case. Itu tidak salah total, tetapi untuk review bisnis lebih bersih jika UC_Auth dan UC_AuditLog
dipindahkan sebagai internal behavior note atau supporting service catalogue.
- 
UC_OfflineMode sebagai use case bisa diterima bila dimaknai sebagai business-operational capability.
Namun secara implementasi, ia lebih dekat ke system state/cross-cutting capability dibanding goal actor
tunggal. Pada level artefak berikutnya, lebih baik dimodelkan juga pada architecture/state/sequence, bukan
hanya use case.
- 
UC_Receipt menggunakan <<extend>> dari UC_Sales. Itu masuk akal bila receipt benar-benar conditional.
Jika receipt selalu wajib setelah penjualan sukses, relasi yang lebih tepat secara semantik adalah <<include>>
atau cukup dianggap bagian dari basic flow UC_Sales.
- 
UC_Override belum dihubungkan langsung ke actor pada diagram. Secara operasional, seharusnya Kasir
(dan kadang Supervisor) memang terasosiasi langsung agar scope user-goal tidak ambigu.
- 
Sequence ini menyentuh persistence layer; pastikan boundary transaksi, idempotency, dan partial failure
handling didefinisikan jelas pada implementation.
- 
Ada dependency ke external service/integration; retry policy, timeout, fallback, dan audit trail tidak boleh
dibiarkan implisit.
- 
Audit logging diperlakukan sebagai kebutuhan implementasi, bukan kosmetik; event penting harus
memiliki correlation id dan actor context.


## Constraints / Policies
Tidak boleh melompati layer arsitektur tanpa alasan; exception dan alternate flow harus tetap visible.

## Technical Notes
Dokumen ini punya dampak besar ke arsitektur, transaksi, retry policy, dan contract integration.

## Dependencies / Related Documents
- `uml_modeling_source_of_truth.md`
- `store_pos_use_case_detail_specifications.md`
- `store_pos_activity_detail_specifications.md`
- `store_pos_domain_model_detail_specifications_v2.md`
- `cassy_architecture_specification_v1.md`
- `store_pos_erd_specification_v2.md`
- `store_pos_test_specification.md`

## Risks / Gaps / Ambiguities
- Tidak ditemukan gap fatal saat ekstraksi. Tetap review ulang bagian tabel/angka jika dokumen ini akan dijadikan baseline implementasi final.

## Reviewer Notes
- Struktur telah dinormalisasi ke Markdown yang konsisten dan siap dipakai untuk design review / engineering handoff.
- Istilah utama dipertahankan sedekat mungkin dengan dokumen sumber untuk menjaga traceability lintas artefak.
- Bila ada konflik antar dokumen, prioritaskan source of truth, artefak yang paling preskriptif, dan baseline yang paling implementable.

## Source Mapping
- Original source: `store_pos_sequence_detail_specifications.pdf` (PDF, 102 pages)
- Output markdown: `store_pos_sequence_detail_specifications.md`
- Conversion approach: text extraction -> normalization -> structural cleanup -> standardized documentation wrapper.
- Note: beberapa tabel/list di PDF dapat mengalami wrapping antar baris; esensi dipertahankan, tetapi layout tabel asli tidak dipertahankan 1:1.
