# Store POS Activity Detail Specifications

## Document Overview
Spesifikasi operasional untuk 39 activity diagram yang menurunkan flow dari katalog use case.

## Purpose
Design review, handoff engineering, traceability ke test case Status review Direview ulang dan dinormalisasi menjadi activity detail specifications Tanggal dokumen 2026-03-08

## Scope
39 activity diagram (UC-01 s.d. UC-39) Tujuan Design review, handoff engineering, traceability ke test case Status review Direview ulang dan dinormalisasi menjadi activity detail specifications Tanggal dokumen 2026-03-08

## Key Decisions / Core Rules
Activity diagram diperlakukan sebagai baseline perilaku sistem, bukan gambar dekoratif; detail recovery diperkaya bila diagram sengaja dibuat ringkas.

## Detailed Content

### Normalized Source Body
Store / POS System
Activity Detail Specifications
Turunan ter-review dari seluruh activity diagram .puml dan tetap traceable ke Use Case
Detail Specifications
Dokumen ini menyajikan spesifikasi aktivitas detail untuk UC-01 s.d. UC-39, lengkap dengan
review kualitas diagram, entry/exit criteria, alur utama, decision point, alternate path,
exception path, dan aturan bisnis yang relevan.
Sumber utama
Use Case Detail Specifications PDF + seluruh
file activity .puml
Cakupan
39 activity diagram (UC-01 s.d. UC-39)
Tujuan
Design review, handoff engineering,
traceability ke test case
Status review
Direview ulang dan dinormalisasi menjadi
activity detail specifications
Tanggal dokumen
2026-03-08

## 1. Tujuan dan ruang lingkup
Dokumen ini mengubah seluruh activity diagram .puml menjadi activity detail specifications
yang lebih operasional, tetap konsisten dengan use case asal, dan siap dipakai untuk design
review, traceability engineering, dan penyusunan test scenario.
- 
Setiap spesifikasi aktivitas ditautkan langsung ke UC asal dan file .puml yang menjadi
sumber diagram.
- 
Struktur setiap bagian dibuat konsisten: tujuan, trigger, preconditions, partitions, main
flow, alternate flow, exception flow, postconditions, dan business rules.
- 
Fokus dokumen ini adalah implementability: activity tidak diperlakukan sebagai gambar
dekoratif, tetapi sebagai baseline perilaku sistem.
## 2. Ringkasan review seluruh activity diagram
Jumlah diagram
Explicit start/stop
39/39 diagram memiliki start dan stop
eksplisit
Pemakaian partition
Seluruh diagram memakai
swimlane/partition actor/sistem
Rata-rata checkpoint keputusan
### 1.0 per diagram
Rata-rata aksi
### 1.0 per diagram
Catatan review utama
Sebagian diagram berhenti pada exception
terminal; detail pemulihan dan policy
handling didokumentasikan di spesifikasi
aktivitas ini agar tetap testable
Catatan review: secara umum model sudah konsisten dan cukup implementable. Penyempurnaan
utama yang dilakukan di dokumen ini adalah memperjelas recovery path, criteria transisi, dan
decision ownership yang pada diagram sengaja dibuat ringkas.
## 3. Prinsip penyusunan spesifikasi aktivitas
- 
Use case tetap menjadi sumber kebenaran bisnis; activity diagram diperlakukan sebagai
turunan operasional dari use case tersebut.
- 
Jika activity diagram ringkas, detail diperkaya menggunakan trigger, preconditions,
postconditions, dan business rules dari spesifikasi use case asal.
- 
Istilah, actor, dan boundary diseragamkan agar cocok untuk handoff engineering dan
pembuatan sequence/domain model berikutnya.
## 4. Matriks traceability
UC
Nama aktivitas
Primary actor
Partition utama
File .puml
UC-01
Mulai Shift Kasir
Kasir Identity
Service,
Supervisor (jika
Kasir, Sistem
POS, Supervisor
uc01_mulai_shift
_kasir_activity.p
uml

ada
UC-02
Proses Penjualan
Kasir
Kasir, Sistem
POS
uc02_proses_pen
jualan_activity.p
uml
UC-04
Terapkan
Member /
Voucher
Kasir /
Pelanggan
Kasir, Sistem
POS, Loyalty
Service,
Pelanggan
uc04_terapkan_
member_vouche
r_activity.puml
UC-05
Proses
Pembayaran
Kasir
Kasir, Sistem
POS, Payment
Gateway / EDC
uc05_proses_pe
mbayaran_activi
ty.puml
UC-07
Suspend /
Resume
Transaksi
Kasir
Kasir, Sistem
POS
uc07_suspend_re
sume_transaksi_
activity.puml
UC-10
Void Item /
Transaksi
Kasir
Kasir, Sistem
POS, Supervisor
uc10_void_item_t
ransaksi_activity
.puml
UC-11
Return / Refund
Kasir /
Pelanggan
Pelanggan, Kasir,
Sistem POS,
Supervisor
uc11_return_ref
und_activity.pu
ml
UC-12
Price Override /
Manual Discount
Kasir
Kasir, Sistem
POS, Supervisor
uc12_price_over
ride_manual_dis
count_activity.pu
ml
UC-18
Cash In / Cash
Out
Kasir
Kasir, Sistem
POS, Supervisor
uc18_cash_in_ca
sh_out_activity.p
uml
UC-19
Safe Drop
Supervisor
Supervisor,
Kasir, Sistem
POS
uc19_safe_drop_
activity.puml
UC-20
Tutup Shift
Kasir
Kasir, Sistem
POS, Supervisor
uc20_tutup_shift
_activity.puml
UC-23
Tutup Hari
Store Manager
Store Manager,
Sistem POS
uc23_tutup_hari_
activity.puml
UC-24
Cek Stok &
Movement
Staff Inventori
Staff Inventori,
Sistem POS
uc24_cek_stok_m
ovement_activity
.puml
UC-25
Receiving
Barang
Staff Inventori
Staff Inventori,
Sistem POS
uc25_receiving_b
arang_activity.p
uml
UC-26
Transfer Stok
Staff Inventori
Staff Inventori,
Sistem POS
uc26_transfer_st
ok_activity.puml

UC-27
Replenishment
Rak
Staff Inventori
Staff Inventori,
Sistem POS
uc27_replenishm
ent_rak_activity.
puml
UC-28
Stock
Adjustment
Staff Inventori /
Supervisor
Staff Inventori,
Sistem POS,
Supervisor
uc28_stock_adjus
tment_activity.p
uml
UC-29
Stock Opname /
Cycle Count
Staff Inventori /
Supervisor
Staff Inventori,
Sistem POS
uc29_stock_opna
me_cycle_count_
activity.puml
UC-30
Kelola Barang
Rusak / Expired
Staff Inventori
Staff Inventori,
Sistem POS,
Supervisor
uc30_kelola_bar
ang_rusak_expir
ed_activity.puml
UC-33
Cetak Label
Harga / Rak
Staff Inventori
Staff Inventori,
Sistem POS,
Store Device
uc33_cetak_label
_harga_rak_activ
ity.puml
UC-34
Lihat Laporan
Operasional
Toko
Store Manager
Store Manager,
Sistem POS
uc34_lihat_lapor
an_operasional_t
oko_activity.pum
l
UC-35
Sinkronisasi
Data Toko
Store Manager /
System
System
Scheduler / Store
Manager, Sistem
POS, HQ Store
Service
uc35_sinkronisas
i_data_toko_activ
ity.puml
UC-36
Mode Operasi
Offline
Sistem POS,
Store Staff
uc36_mode_oper
asi_offline_activi
ty.puml
UC-03
Kelola
Keranjang
Kasir
Kasir, Sistem
POS
uc03_kelola_kera
njang_activity.pu
ml
UC-06
Terbitkan
Receipt
Kasir
Sistem POS,
Store Device
uc06_terbitkan_r
eceipt_activity.p
uml
UC-08
Cari / Scan
Produk
Kasir / Staff
Inventori
User, Sistem POS
uc08_cari_scan_
produk_activity.
puml
UC-09
Hitung Harga,
Promo, Pajak
System
Sistem POS
uc09_hitung_har
ga_promo_pajak
_activity.puml
UC-13
Lookup Receipt
Kasir
Kasir, Sistem
POS
uc13_lookup_rec
eipt_activity.pu
ml
UC-14
Validasi Return
System
Sistem POS,
uc14_validasi_re

Policy
Supervisor
turn_policy_activ
ity.puml
UC-15
Supervisor
Approval
Supervisor
Sistem POS,
Supervisor
uc15_supervisor
_approval_activit
y.puml
UC-16
Catat Alasan
Exception
Kasir /
Supervisor / Staff
Inventori
User, Sistem POS
uc16_catat_alasa
n_exception_acti
vity.puml
UC-17
Input Kas Awal
Kasir
Kasir, Sistem
POS, Supervisor
uc17_input_kas_
awal_activity.pu
ml
UC-21
Rekonsiliasi Kas
Kasir /
Supervisor
User, Sistem
POS, Supervisor
uc21_rekonsilias
i_kas_activity.pu
ml
UC-22
Generate X / Z
Report
Supervisor
Supervisor,
Sistem POS,
Store Device
uc22_generate_x
_z_report_activit
y.puml
UC-31
Verifikasi
Delivery / PO
Staff Inventori
Staff Inventori,
Sistem POS
uc31_verifikasi_
delivery_po_acti
vity.puml
UC-32
Catat Selisih
Receiving
Staff Inventori
Staff Inventori,
Sistem POS,
Supervisor
uc32_catat_selisi
h_receiving_acti
vity.puml
UC-37
Rekonsiliasi
Sync Gagal
Store Manager /
Supervisor
Store Manager /
Supervisor,
Sistem POS
uc37_rekonsilias
i_sync_gagal_acti
vity.puml
UC-38
Autentikasi &
Validasi Role
Identity Service /
Store Staff
Store Staff,
Sistem POS,
Identity Service
uc38_autentikasi
_validasi_role_ac
tivity.puml
UC-39
Catat Audit Log
System
Sistem POS
uc39_catat_audit
_log_activity.pu
ml

## 5. Front office dan transaksi POS
UC-01 - Mulai Shift Kasir
Use case asal
UC-01 - Mulai Shift Kasir
File activity diagram
uc01_mulai_shift_kasir_activity.puml
Primary actor
Kasir Identity Service, Supervisor (jika ada
Supporting actors / external systems
pengecualian), Store Device
Partition pada diagram
Kasir, Sistem POS, Supervisor
Tujuan aktivitas
Membuka shift kerja kasir sehingga terminal POS siap dipakai untuk transaksi pada hari
operasional.
Entry criteria
- 
Trigger: ditutup atau diambil alih sesuai policy. Kasir memilih menu mulai shift.
- 
Precondition: Kasir terdaftar dan memiliki role aktif.
- 
Precondition: Terminal POS online atau dapat bekerja
- 
Precondition: Shift sebelumnya pada terminal sudah dalam mode offline yang diizinkan
Main activity flow
1.Kasir melakukan login ke POS.
2.Sistem menjalankan autentikasi dan validasi role.
3.Sistem memverifikasi bahwa tidak ada shift aktif yang konflik pada terminal tersebut.
4.Kasir memasukkan nominal kas awal.
5.Sistem mencatat shift baru beserta opening cash.
6.Sistem menampilkan status shift aktif dan terminal siap transaksi.
Decision points dan loop penting
- 
Decision: Identitas valid? Ada shift konflik? Exception disetujui? Nominal sesuai policy?
Disetujui?.
Alternate paths
- 
Autentikasi gagal, sistem menolak pembukaan shift.
- 
Jika ada shift lama belum ditutup, sistem meminta proses handover atau supervisor
handling.
Exception dan recovery handling
- 
Identity service tidak tersedia dan offline login tidak diizinkan -> shift tidak dapat dibuka.
- 
Nominal opening cash di luar policy -> perlu koreksi atau approval.
Exit criteria
- 
Postcondition: Shift aktif terbentuk.
- 
Postcondition: Opening cash tercatat.
Business rules dan traceability note
- 
Satu kasir tidak boleh memiliki dua shift aktif pada terminal berbeda tanpa policy eksplisit.
- 
Opening cash wajib diinput pada awal shift sesuai include UC-17.
- 
Related / included use cases: UC-17

- 
Review diagram: Diagram memakai explicit start/stop dan sudah cocok untuk traceability
ke skenario test. Swimlane/partition yang terlihat: Kasir, Sistem POS, Supervisor.
Checkpoint keputusan utama: Identitas valid? Ada shift konflik? Exception disetujui?
Nominal sesuai policy? Disetujui?.
UC-02 - Proses Penjualan
Use case asal
UC-02 - Proses Penjualan
File activity diagram
uc02_proses_penjualan_activity.puml
Primary actor
Kasir
Supporting actors / external systems
Pelanggan, Payment Gateway / EDC, Loyalty
Service, Store Device
Partition pada diagram
Kasir, Sistem POS
Tujuan aktivitas
Menyelesaikan transaksi penjualan barang hingga pembayaran diterima dan penjualan
tercatat sah.
Entry criteria
- 
Trigger: Pelanggan ingin membeli barang dan kasir memulai transaksi baru.
- 
Precondition: Shift kasir aktif.
- 
Precondition: POS memiliki akses ke katalog produk dan pricing rule terbaru atau cache
offline yang valid
Main activity flow
7.Kasir membuat transaksi penjualan baru.
8.Kasir menambah item ke keranjang melalui scan atau pencarian produk.
9.Sistem menghitung harga, promo, dan pajak setiap perubahan keranjang.
10.
Kasir dapat menerapkan member atau voucher bila diminta pelanggan.
11.
Kasir mengonfirmasi total pembayaran kepada pelanggan.
12.
Kasir memproses pembayaran sesuai metode yang dipilih.
13.
Sistem mengotorisasi pembayaran dan menyimpan transaksi penjualan.
14.
Sistem menerbitkan receipt dan menandai transaksi selesai.
15.
Sistem menulis audit log transaksi.
Decision points dan loop penting
- 
Loop: Masih ada item?.
- 
Decision: Produk valid dan dapat dijual? Pelanggan meminta member/voucher? Benefit
valid? Perlu ubah keranjang? Transaksi disuspend? Payment valid?.
Alternate paths
- 
Kasir mengubah kuantitas atau menghapus item sebelum pembayaran.
- 
Transaksi disuspend untuk dilanjutkan nanti.
- 
Receipt diterbitkan dalam bentuk digital bila printer tidak digunakan.
Exception dan recovery handling
- 
Produk tidak ditemukan atau barcode tidak valid.
- 
Perhitungan harga gagal karena master data corrupt -> transaksi ditahan.

- 
Pembayaran gagal -> transaksi tetap unpaid/pending dan tidak selesai.
Exit criteria
- 
Postcondition: Transaksi penjualan tersimpan.
- 
Postcondition: Stok dan ledger penjualan ter-update atau ditandai untuk sinkronisasi.
- 
Postcondition: Receipt tersedia.
Business rules dan traceability note
- 
Penjualan tidak boleh final tanpa status payment yang valid.
- 
UC-03, UC-05, dan UC-39 bersifat wajib melalui include.
- 
Related / included use cases: UC-03, UC-05, UC-39
- 
Review diagram: Diagram memakai explicit start/stop dan sudah cocok untuk traceability
ke skenario test. Swimlane/partition yang terlihat: Kasir, Sistem POS. Checkpoint keputusan
utama: Produk valid dan dapat dijual? Pelanggan meminta member/voucher? Benefit valid?
Perlu ubah keranjang? Transaksi disuspend? Payment valid?.
UC-03 - Kelola Keranjang
Use case asal
UC-03 - Kelola Keranjang
File activity diagram
uc03_kelola_keranjang_activity.puml
Primary actor
Kasir
Supporting actors / external systems
Store Device
Partition pada diagram
Kasir, Sistem POS
Tujuan aktivitas
Memelihara isi transaksi penjualan aktif sebelum finalisasi pembayaran.
Entry criteria
- 
Trigger: Kasir menambah, mengubah, atau menghapus item dari keranjang.
- 
Precondition: Ada transaksi penjualan aktif.
Main activity flow
16.
Kasir scan/cari item.
17.
Sistem menambahkan item ke keranjang.
18.
Kasir mengubah kuantitas atau menghapus item bila perlu.
19.
Sistem menghitung ulang harga.
Decision points dan loop penting
- 
Decision: Aksi = tambah item? Aksi = ubah qty? Item valid dan stok/jualable tersedia?.
Alternate paths
- 
Kasir menahan item tertentu untuk konfirmasi harga.
Exception dan recovery handling
- 
Item tidak dapat dijual atau stok nol.
Exit criteria
- 
Postcondition: Keranjang aktif ter-update.

Business rules dan traceability note
- 
Semua perubahan keranjang harus memicu pricing ulang.
- 
Review diagram: Diagram memakai explicit start/stop dan sudah cocok untuk traceability
ke skenario test. Swimlane/partition yang terlihat: Kasir, Sistem POS. Checkpoint keputusan
utama: Aksi = tambah item? Aksi = ubah qty? Item valid dan stok/jualable tersedia?.
UC-04 - Terapkan Member / Voucher
Use case asal
UC-04 - Terapkan Member / Voucher
File activity diagram
uc04_terapkan_member_voucher_activity.pu
ml
Primary actor
Kasir / Pelanggan
Supporting actors / external systems
Loyalty Service
Partition pada diagram
Kasir, Sistem POS, Loyalty Service, Pelanggan
Tujuan aktivitas
Menerapkan benefit member, poin, atau voucher yang sah ke transaksi aktif.
Entry criteria
- 
Trigger: Pelanggan meminta benefit loyalty atau menggunakan voucher.
- 
Precondition: Ada transaksi aktif.
- 
Precondition: Member ID, nomor telepon, QR voucher, atau kode promo tersedia
Main activity flow
20.
Kasir memilih fitur member/voucher.
21.
Kasir memasukkan atau memindai identitas member/voucher.
22.
Sistem memvalidasi ke Loyalty Service atau cache policy yang tersedia.
23.
Sistem menghitung ulang harga, diskon, atau earning point.
24.
Kasir menampilkan hasil benefit ke pelanggan.
25.
Pelanggan menyetujui hasil penerapan benefit.
Decision points dan loop penting
- 
Decision: Benefit valid? Perlu verifikasi tambahan? Verifikasi berhasil?.
Alternate paths
- 
Voucher valid tetapi hanya sebagian item yang eligible.
- 
Member ditemukan tetapi reward redemption memerlukan OTP atau verifikasi tambahan.
Exception dan recovery handling
- 
Voucher kadaluwarsa, sudah dipakai, atau tidak eligible.
- 
Loyalty service tidak tersedia dan tidak ada fallback offline -> benefit tidak diterapkan.
Exit criteria
- 
Postcondition: Benefit loyalty/voucher tercermin pada transaksi aktif atau ditolak dengan
alasan yang jelas.
Business rules dan traceability note
- 
Satu voucher biasanya sekali pakai kecuali tipe reusable.

- 
Perubahan benefit harus memicu recalculation via UC-09.
- 
Related / included use cases: UC-09
- 
Review diagram: Diagram memakai explicit start/stop dan sudah cocok untuk traceability
ke skenario test. Swimlane/partition yang terlihat: Kasir, Sistem POS, Loyalty Service,
Pelanggan. Checkpoint keputusan utama: Benefit valid? Perlu verifikasi tambahan?
Verifikasi berhasil?.
UC-05 - Proses Pembayaran
Use case asal
UC-05 - Proses Pembayaran
File activity diagram
uc05_proses_pembayaran_activity.puml
Primary actor
Kasir
Supporting actors / external systems
Pelanggan, Payment Gateway / EDC
Partition pada diagram
Kasir, Sistem POS, Payment Gateway / EDC
Tujuan aktivitas
Menerima pembayaran pelanggan dan menghasilkan status payment yang sah untuk
menyelesaikan transaksi.
Entry criteria
- 
Trigger: Kasir menekan proses pembayaran. pending, atau failed sesuai hasil.
- 
Precondition: Ada transaksi aktif dengan total final.
- 
Precondition: Metode pembayaran yang dipilih tersedia di toko
Main activity flow
26.
Kasir memilih metode pembayaran.
27.
Sistem menampilkan total yang harus dibayar.
28.
Untuk cash, kasir memasukkan nominal diterima dan sistem menghitung kembalian.
29.
Untuk non-cash, sistem mengirim request ke payment gateway/EDC.
30.
Payment provider mengembalikan hasil otorisasi.
31.
Sistem menandai payment sebagai sukses dan mengikatnya ke transaksi.
32.
Sistem mengembalikan kontrol ke alur penjualan untuk finalisasi.
Decision points dan loop penting
- 
Decision: Metode = cash? Nominal cukup? Authorized? Timeout / status tidak pasti?.
Alternate paths
- 
Split payment digunakan dan sistem memproses beberapa instrumen hingga total lunas.
- 
Pembayaran cashless membutuhkan retry pada terminal EDC.
Exception dan recovery handling
- 
Authorization declined.
- 
Timeout dari gateway/EDC menyebabkan status tidak pasti -> transaksi ditandai pending
investigation.
- 
Nominal pembayaran kurang dari total -> sistem menolak finalisasi.
Exit criteria
- 
Postcondition: Status payment tersimpan sebagai success,

- 
Postcondition: Referensi payment provider tersimpan bila applicable.
Business rules dan traceability note
- 
Finalisasi penjualan hanya boleh saat total outstanding = 0 dan status payment valid.
- 
Semua pajak/promo harus dihitung sebelum payment dimulai.
- 
Review diagram: Diagram memakai explicit start/stop dan sudah cocok untuk traceability
ke skenario test. Swimlane/partition yang terlihat: Kasir, Sistem POS, Payment Gateway /
EDC. Checkpoint keputusan utama: Metode = cash? Nominal cukup? Authorized? Timeout /
status tidak pasti?.
UC-06 - Terbitkan Receipt
Use case asal
UC-06 - Terbitkan Receipt
File activity diagram
uc06_terbitkan_receipt_activity.puml
Primary actor
Kasir
Supporting actors / external systems
Store Device
Partition pada diagram
Sistem POS, Store Device
Tujuan aktivitas
Menyediakan bukti transaksi kepada pelanggan setelah transaksi sah selesai.
Entry criteria
- 
Trigger: Sistem menyelesaikan transaksi penjualan atau return/refund.
- 
Precondition: Transaksi final dengan payment sah tersedia
Main activity flow
33.
Sistem membentuk data receipt.
34.
Sistem memilih channel receipt.
35.
Receipt dicetak atau dikirim digital.
36.
Sistem menyimpan referensi receipt.
Decision points dan loop penting
- 
Decision: Printed receipt? Cetak berhasil?.
Alternate paths
- 
Reprint dilakukan kemudian melalui receipt lookup.
Exception dan recovery handling
- 
Printer gagal atau kehabisan kertas.
Exit criteria
- 
Postcondition: Receipt tersedia atau kegagalan cetak tercatat.
Business rules dan traceability note
- 
Receipt hanya untuk transaksi final.
- 
Review diagram: Diagram memakai explicit start/stop dan sudah cocok untuk traceability
ke skenario test. Swimlane/partition yang terlihat: Sistem POS, Store Device. Checkpoint
keputusan utama: Printed receipt? Cetak berhasil?.

UC-07 - Suspend / Resume Transaksi
Use case asal
UC-07 - Suspend / Resume Transaksi
File activity diagram
uc07_suspend_resume_transaksi_activity.pu
ml
Primary actor
Kasir
Supporting actors / external systems
Supervisor (opsional)
Partition pada diagram
Kasir, Sistem POS
Tujuan aktivitas
Menunda transaksi aktif tanpa kehilangan state dan melanjutkannya kembali di terminal yang
diizinkan.
Entry criteria
- 
Trigger: Kasir memilih suspend atau membuka daftar transaksi tertunda untuk resume.
- 
Precondition: Ada transaksi aktif yang belum dibayar.
- 
Precondition: Policy store mengizinkan suspend.
Main activity flow
37.
Kasir memilih suspend transaksi aktif.
38.
Sistem memvalidasi transaksi belum final dan belum memiliki payment sukses.
39.
Sistem menyimpan snapshot keranjang dan context transaksi.
40.
Sistem memberi identifier transaksi suspend.
41.
Pada waktu resume, kasir memilih transaksi suspend.
42.
Sistem memuat ulang isi keranjang dan menghitung ulang harga bila diperlukan.
Decision points dan loop penting
- 
Decision: Aksi = suspend? Eligible untuk suspend? Snapshot valid dan belum diresume
terminal lain? Harga berubah?.
Alternate paths
- 
Resume dilakukan oleh kasir lain jika policy store mengizinkan handover.
- 
Harga berubah sejak suspend dan sistem meminta konfirmasi sebelum lanjut.
Exception dan recovery handling
- 
Snapshot transaksi rusak atau sudah kadaluwarsa.
- 
Transaksi sudah di-resume terminal lain -> sistem menolak duplicate resume.
Exit criteria
- 
Postcondition: Transaksi berada pada state suspended atau resumed dengan konsisten.
Business rules dan traceability note
- 
Transaksi suspended tidak boleh mengurangi stok final sebelum selesai.
- 
Masa berlaku suspend dapat dibatasi per policy.
- 
Review diagram: Diagram memakai explicit start/stop dan sudah cocok untuk traceability
ke skenario test. Swimlane/partition yang terlihat: Kasir, Sistem POS. Checkpoint keputusan
utama: Aksi = suspend? Eligible untuk suspend? Snapshot valid dan belum diresume
terminal lain? Harga berubah?.

UC-08 - Cari / Scan Produk
Use case asal
UC-08 - Cari / Scan Produk
File activity diagram
uc08_cari_scan_produk_activity.puml
Primary actor
Kasir / Staff Inventori
Supporting actors / external systems
Store Device
Partition pada diagram
User, Sistem POS
Tujuan aktivitas
Mengidentifikasi produk yang akan dimasukkan ke transaksi atau proses inventori.
Entry criteria
- 
Trigger: User memindai barcode atau mengetik kata kunci SKU/nama.
- 
Precondition: Perangkat scan atau data pencarian tersedia.
Main activity flow
43.
User scan barcode atau masukkan keyword.
44.
Sistem mencari produk.
45.
Sistem menampilkan hasil yang cocok.
46.
User memilih item target.
Decision points dan loop penting
- 
Decision: Produk ditemukan? Scan gagal? Produk ditemukan?.
Alternate paths
- 
Barcode tidak terbaca lalu fallback ke pencarian manual.
Exception dan recovery handling
- 
Produk tidak ditemukan atau master data belum tersedia.
Exit criteria
- 
Postcondition: Produk teridentifikasi.
Business rules dan traceability note
- 
Pencarian harus mendukung SKU, barcode, dan nama pendek minimal.
- 
Review diagram: Diagram memakai explicit start/stop dan sudah cocok untuk traceability
ke skenario test. Swimlane/partition yang terlihat: User, Sistem POS. Checkpoint keputusan
utama: Produk ditemukan? Scan gagal? Produk ditemukan?.
UC-09 - Hitung Harga, Promo, Pajak
Use case asal
UC-09 - Hitung Harga, Promo, Pajak
File activity diagram
uc09_hitung_harga_promo_pajak_activity.pu
ml
Primary actor
System
Supporting actors / external systems
Loyalty Service, HQ rule cache
Partition pada diagram
Sistem POS

Tujuan aktivitas
Menghasilkan total transaksi yang benar berdasarkan item, promo, voucher, dan aturan pajak.
Entry criteria
- 
Trigger: Ada perubahan item, member/voucher, atau metode pembayaran yang
memengaruhi total.
- 
Precondition: Keranjang transaksi tersedia.
Main activity flow
47.
Sistem membaca item basket.
48.
Sistem menerapkan promo dan pajak yang relevan.
49.
Sistem menghitung subtotal, diskon, pajak, dan grand total.
50.
Sistem mengembalikan hasil ke caller.
Decision points dan loop penting
- 
Decision: Rule lokal/cache tersedia? Rule konflik atau data harga hilang?.
Alternate paths
- 
Rule berasal dari cache lokal saat offline.
Exception dan recovery handling
- 
Rule conflict atau data harga hilang.
Exit criteria
- 
Postcondition: Nilai transaksi terbaru tersedia.
Business rules dan traceability note
- 
Pricing harus deterministic untuk input yang sama pada timestamp bisnis yang sama.
- 
Review diagram: Diagram memakai explicit start/stop dan sudah cocok untuk traceability
ke skenario test. Swimlane/partition yang terlihat: Sistem POS. Checkpoint keputusan
utama: Rule lokal/cache tersedia? Rule konflik atau data harga hilang?.
UC-10 - Void Item / Transaksi
Use case asal
UC-10 - Void Item / Transaksi
File activity diagram
uc10_void_item_transaksi_activity.puml
Primary actor
Kasir
Supporting actors / external systems
Supervisor, Lookup Receipt diidentifikasi
tersedia.
Partition pada diagram
Kasir, Sistem POS, Supervisor
Tujuan aktivitas
Membatalkan item tertentu atau seluruh transaksi sesuai otorisasi dan jejak audit yang
diwajibkan.
Entry criteria
- 
Trigger: Kasir memilih void item atau void transaksi.
- 
Precondition: Transaksi aktif atau transaksi yang dapat

- 
Precondition: Kasir memiliki hak void sesuai level otorisasinya atau dapat meminta
approval
Main activity flow
51.
Kasir memilih item/transaksi yang akan di-void.
52.
Sistem memuat detail transaksi terkait bila diperlukan.
53.
Kasir memasukkan alasan exception.
54.
Sistem mengecek apakah approval supervisor diperlukan.
55.
Bila diperlukan, supervisor memberikan approval.
56.
Sistem membatalkan item/transaksi sesuai scope void.
57.
Sistem menulis audit log.
Decision points dan loop penting
- 
Decision: Reason lengkap? Approval diperlukan? Disetujui? Transaksi settled dan tidak
boleh di-void?.
Alternate paths
- 
Void item dilakukan sebelum payment sehingga tidak perlu receipt lookup.
- 
Void penuh setelah payment diperlakukan sebagai reversal sesuai policy.
Exception dan recovery handling
- 
Receipt/transaksi tidak ditemukan.
- 
Approval ditolak.
- 
Transaksi sudah settled dan tidak boleh di-void, harus lewat return/refund.
Exit criteria
- 
Postcondition: Item/transaksi dibatalkan sesuai kebijakan, atau permintaan ditolak dengan
alasan jelas.
Business rules dan traceability note
- 
Void selalu membutuhkan reason code.
- 
Use return/refund, bukan void, bila barang sudah keluar dan transaksi telah selesai secara
hukum/akuntansi.
- 
Review diagram: Diagram memakai explicit start/stop dan sudah cocok untuk traceability
ke skenario test. Swimlane/partition yang terlihat: Kasir, Sistem POS, Supervisor.
Checkpoint keputusan utama: Reason lengkap? Approval diperlukan? Disetujui? Transaksi
settled dan tidak boleh di-void?.
UC-11 - Return / Refund
Use case asal
UC-11 - Return / Refund
File activity diagram
uc11_return_refund_activity.puml
Primary actor
Kasir / Pelanggan
Supporting actors / external systems
Supervisor, Payment Gateway / EDC
Partition pada diagram
Pelanggan, Kasir, Sistem POS, Supervisor

Tujuan aktivitas
Menerima pengembalian barang pelanggan dan mengembalikan dana atau kredit sesuai
kebijakan toko.
Entry criteria
- 
Trigger: Pelanggan meminta return atau refund.
- 
Precondition: Barang return tersedia untuk diperiksa.
- 
Precondition: Receipt atau bukti transaksi dapat dicari, kecuali policy no-receipt return
diizinkan
Main activity flow
58.
Kasir memulai transaksi return.
59.
Kasir mencari receipt atau referensi transaksi asli.
60.
Sistem memvalidasi return policy berdasarkan item, tanggal, kondisi barang, dan
metode pembayaran awal.
61.
Kasir memilih item dan kuantitas yang direturn.
62.
Kasir mencatat alasan exception/return reason.
63.
Jika diperlukan, supervisor memberi approval.
64.
Sistem menghitung nilai refund.
65.
Kasir mengeksekusi refund ke cash, kartu, atau store credit sesuai policy.
66.
Sistem menyimpan transaksi return dan audit log.
Decision points dan loop penting
- 
Decision: Receipt ditemukan atau no-receipt diizinkan? Eligible return? Approval
diperlukan? Disetujui? Metode asli tidak dapat dipulihkan? Refund ke kartu/gateway
berhasil?.
Alternate paths
- 
Return parsial untuk sebagian item.
- 
Refund dikonversi menjadi store credit jika metode asli tidak dapat dipulihkan.
Exception dan recovery handling
- 
Receipt tidak ditemukan dan no-receipt return tidak diizinkan.
- 
Masa return policy telah lewat.
- 
Refund ke kartu gagal dan perlu fallback sesuai policy.
Exit criteria
- 
Postcondition: Transaksi return tercatat.
- 
Postcondition: Stok barang kembali atau dikirim ke damaged/inspection bucket sesuai
kondisi.
Business rules dan traceability note
- 
Return policy harus tervalidasi sebelum refund.
- 
Tidak semua item boleh direturn, misalnya clearance/final sale/perishable tertentu.
- 
Review diagram: Diagram memakai explicit start/stop dan sudah cocok untuk traceability
ke skenario test. Swimlane/partition yang terlihat: Pelanggan, Kasir, Sistem POS, Supervisor.
Checkpoint keputusan utama: Receipt ditemukan atau no-receipt diizinkan? Eligible
return? Approval diperlukan? Disetujui? Metode asli tidak dapat dipulihkan? Refund ke
kartu/gateway berhasil?.

UC-12 - Price Override / Manual Discount
Use case asal
UC-12 - Price Override / Manual Discount
File activity diagram
uc12_price_override_manual_discount_activit
y.puml
Primary actor
Kasir
Supporting actors / external systems
Supervisor
Partition pada diagram
Kasir, Sistem POS, Supervisor
Tujuan aktivitas
Mengubah harga jual atau memberi diskon manual dalam batas policy toko.
Entry criteria
- 
Trigger: Kasir memilih override harga atau manual discount.
- 
Precondition: Ada item atau transaksi aktif yang eligible
- 
Precondition: Policy threshold tersedia. untuk override/discount
Main activity flow
67.
Kasir memilih item atau transaksi target.
68.
Kasir memasukkan nilai override/diskon dan alasan exception.
69.
Sistem mengecek threshold otorisasi.
70.
Supervisor memberikan approval sesuai policy.
71.
Sistem menerapkan harga/diskon baru.
72.
Sistem menghitung ulang total dan menyimpan audit log.
Decision points dan loop penting
- 
Decision: Item eligible? Masih dalam limit kasir? Disetujui?.
Alternate paths
- 
Diskon masih dalam limit kasir sehingga approval tidak perlu.
- 
Override berlaku pada item tunggal, bukan seluruh basket.
Exception dan recovery handling
- 
Nilai diskon melewati limit maksimum.
- 
Approval ditolak.
- 
Item tidak eligible karena promo exclusive atau price-locked.
Exit criteria
- 
Postcondition: Harga override/manual discount tersimpan pada transaksi aktif atau ditolak.
Business rules dan traceability note
- 
Setiap override/discound manual wajib punya reason code dan audit trail.
- 
Perhitungan akhir selalu melalui pricing engine.
- 
Review diagram: Diagram memakai explicit start/stop dan sudah cocok untuk traceability
ke skenario test. Swimlane/partition yang terlihat: Kasir, Sistem POS, Supervisor.
Checkpoint keputusan utama: Item eligible? Masih dalam limit kasir? Disetujui?.

UC-13 - Lookup Receipt
Use case asal
UC-13 - Lookup Receipt
File activity diagram
uc13_lookup_receipt_activity.puml
Primary actor
Kasir
Supporting actors / external systems
Store Manager
Partition pada diagram
Kasir, Sistem POS
Tujuan aktivitas
Menemukan transaksi/receipt sebelumnya sebagai referensi exception atau customer service.
Entry criteria
- 
Trigger: Kasir perlu mencari transaksi lama.
- 
Precondition: Kunci pencarian tersedia.
Main activity flow
73.
Kasir memasukkan nomor receipt, kartu, tanggal, atau atribut lain.
74.
Sistem mencari transaksi yang cocok.
75.
Sistem menampilkan hasil dan detail receipt.
Decision points dan loop penting
- 
Decision: Offline mode? Receipt ditemukan?.
Alternate paths
- 
Pencarian menggunakan data lokal terbatas saat offline.
Exception dan recovery handling
- 
Receipt tidak ditemukan.
Exit criteria
- 
Postcondition: Receipt target ditemukan atau tidak.
Business rules dan traceability note
- 
Hak akses data receipt harus mengikuti role.
- 
Review diagram: Diagram memakai explicit start/stop dan sudah cocok untuk traceability
ke skenario test. Swimlane/partition yang terlihat: Kasir, Sistem POS. Checkpoint keputusan
utama: Offline mode? Receipt ditemukan?.
UC-14 - Validasi Return Policy
Use case asal
UC-14 - Validasi Return Policy
File activity diagram
uc14_validasi_return_policy_activity.puml
Primary actor
System
Supporting actors / external systems
Supervisor
Partition pada diagram
Sistem POS, Supervisor
Tujuan aktivitas
Memastikan permintaan return memenuhi kebijakan toko dan regulasi yang berlaku.

Entry criteria
- 
Trigger: Proses return dimulai atau item return dipilih.
- 
Precondition: Item return dan referensi transaksi tersedia atau informasi minimum
tersedia
Main activity flow
76.
Sistem mengecek tanggal pembelian, kategori item, kondisi, dan metode bayar.
77.
Sistem mengecek aturan no-return/final sale/perishable.
78.
Sistem menentukan eligible/ineligible beserta syaratnya.
Decision points dan loop penting
- 
Decision: Data pembelian cukup? Policy terpenuhi? Policy override tersedia? Override
disetujui?.
Alternate paths
- 
Policy override tersedia dengan approval supervisor.
Exception dan recovery handling
- 
Data pembelian tidak cukup untuk validasi.
Exit criteria
- 
Postcondition: Keputusan eligibility return tersedia.
Business rules dan traceability note
- 
Policy return harus konsisten antar kanal yang relevan, kecuali memang store-specific.
- 
Review diagram: Diagram memakai explicit start/stop dan sudah cocok untuk traceability
ke skenario test. Swimlane/partition yang terlihat: Sistem POS, Supervisor. Checkpoint
keputusan utama: Data pembelian cukup? Policy terpenuhi? Policy override tersedia?
Override disetujui?.
UC-15 - Supervisor Approval
Use case asal
UC-15 - Supervisor Approval
File activity diagram
uc15_supervisor_approval_activity.puml
Primary actor
Supervisor
Supporting actors / external systems
Kasir / Staff Inventori
Partition pada diagram
Sistem POS, Supervisor
Tujuan aktivitas
Memberikan keputusan otorisasi untuk operasi sensitif yang melewati kewenangan staf biasa.
Entry criteria
- 
Trigger: Sistem meminta approval.
- 
Precondition: Ada permintaan approval aktif.
- 
Precondition: Supervisor memiliki kredensial valid.
Main activity flow
79.
Sistem menampilkan detail permintaan approval.
80.
Supervisor meninjau konteks, alasan, dan dampak.

81.
Supervisor menyetujui atau menolak.
82.
Sistem mencatat keputusan.
Decision points dan loop penting
- 
Decision: Autentikasi supervisor valid? Supervisor menyetujui?.
Alternate paths
- 
Supervisor login langsung di terminal requester.
Exception dan recovery handling
- 
Supervisor gagal autentikasi.
Exit criteria
- 
Postcondition: Keputusan approval tercatat.
Business rules dan traceability note
- 
Approval harus mengandung siapa, kapan, apa yang diizinkan, dan untuk alasan apa.
- 
Review diagram: Diagram memakai explicit start/stop dan sudah cocok untuk traceability
ke skenario test. Swimlane/partition yang terlihat: Sistem POS, Supervisor. Checkpoint
keputusan utama: Autentikasi supervisor valid? Supervisor menyetujui?.
UC-16 - Catat Alasan Exception
Use case asal
UC-16 - Catat Alasan Exception
File activity diagram
uc16_catat_alasan_exception_activity.puml
Primary actor
Kasir / Supervisor / Staff Inventori
Supporting actors / external systems
System
Partition pada diagram
User, Sistem POS
Tujuan aktivitas
Merekam reason code dan catatan operasional untuk tindakan exception.
Entry criteria
- 
Trigger: User menjalankan void, return, override, receiving gap, adjustment, atau exception
lain.
- 
Precondition: Ada proses exception yang sedang berjalan.
Main activity flow
83.
Sistem meminta reason code.
84.
User memilih reason code dan menambah catatan bila perlu.
85.
Sistem memvalidasi reason dan menyimpannya.
Decision points dan loop penting
- 
Decision: Tambahkan catatan atau dokumen? Reason wajib dan sudah lengkap?.
Alternate paths
- 
Foto/dokumen pendukung ditambahkan.
Exception dan recovery handling
- 
Reason code wajib tetapi belum diisi.

Exit criteria
- 
Postcondition: Alasan exception tersimpan.
Business rules dan traceability note
- 
Alasan exception tidak boleh null untuk proses yang mewajibkannya.
- 
Review diagram: Diagram memakai explicit start/stop dan sudah cocok untuk traceability
ke skenario test. Swimlane/partition yang terlihat: User, Sistem POS. Checkpoint keputusan
utama: Tambahkan catatan atau dokumen? Reason wajib dan sudah lengkap?.

## 6. Shift, kas, dan operasional penutupan
UC-17 - Input Kas Awal
Use case asal
UC-17 - Input Kas Awal
File activity diagram
uc17_input_kas_awal_activity.puml
Primary actor
Kasir
Supporting actors / external systems
Supervisor
Partition pada diagram
Kasir, Sistem POS, Supervisor
Tujuan aktivitas
Mencatat saldo awal laci kas pada awal shift.
Entry criteria
- 
Trigger: Bagian opening cash pada mulai shift dimulai.
- 
Precondition: Shift akan dibuka.
Main activity flow
86.
Kasir menghitung kas awal.
87.
Kasir memasukkan nominal.
88.
Sistem menyimpan opening cash.
Decision points dan loop penting
- 
Decision: Nominal valid? Perlu verifikasi supervisor? Disetujui?.
Alternate paths
- 
Supervisor memverifikasi nominal.
Exception dan recovery handling
- 
Nominal tidak valid.
Exit criteria
- 
Postcondition: Opening cash tercatat.
Business rules dan traceability note
- 
Opening cash bagian wajib dari start shift.
- 
Review diagram: Diagram memakai explicit start/stop dan sudah cocok untuk traceability
ke skenario test. Swimlane/partition yang terlihat: Kasir, Sistem POS, Supervisor.
Checkpoint keputusan utama: Nominal valid? Perlu verifikasi supervisor? Disetujui?.
UC-18 - Cash In / Cash Out
Use case asal
UC-18 - Cash In / Cash Out
File activity diagram
uc18_cash_in_cash_out_activity.puml
Primary actor
Kasir
Supporting actors / external systems
Supervisor (opsional)
Partition pada diagram
Kasir, Sistem POS, Supervisor

Tujuan aktivitas
Mencatat pergerakan kas non-penjualan selama shift dengan alasan yang terkontrol.
Entry criteria
- 
Trigger: Kasir perlu menambah atau mengeluarkan uang dari laci kas untuk keperluan
operasional.
- 
Precondition: Shift aktif.
- 
Precondition: Jenis cash movement tersedia di master reason
Main activity flow
89.
Kasir memilih cash in atau cash out.
90.
Kasir memilih reason code.
91.
Kasir memasukkan nominal dan catatan tambahan bila perlu.
92.
Sistem memvalidasi batas nominal.
93.
Sistem menyimpan cash movement dan memperbarui expected drawer balance.
94.
Sistem menulis audit log.
Decision points dan loop penting
- 
Decision: Ada dokumen pendukung? Shift aktif? Nominal dan reason valid? Approval
supervisor diperlukan? Disetujui?.
Alternate paths
- 
Supervisor approval diminta untuk nominal besar.
- 
Dokumen pendukung difoto/diunggah bila toko menerapkan kontrol tambahan.
Exception dan recovery handling
- 
Nominal tidak valid atau reason code tidak diizinkan.
- 
Shift tidak aktif.
Exit criteria
- 
Postcondition: Pergerakan kas tercatat dan dapat direkonsiliasi saat tutup shift.
Business rules dan traceability note
- 
Semua cash movement harus terhubung ke shift aktif.
- 
Tidak boleh ada cash out tanpa reason code.
- 
Review diagram: Diagram memakai explicit start/stop dan sudah cocok untuk traceability
ke skenario test. Swimlane/partition yang terlihat: Kasir, Sistem POS, Supervisor.
Checkpoint keputusan utama: Ada dokumen pendukung? Shift aktif? Nominal dan reason
valid? Approval supervisor diperlukan? Disetujui?.
UC-19 - Safe Drop
Use case asal
UC-19 - Safe Drop
File activity diagram
uc19_safe_drop_activity.puml
Primary actor
Supervisor
Supporting actors / external systems
Kasir
Partition pada diagram
Supervisor, Kasir, Sistem POS

Tujuan aktivitas
Memindahkan sebagian uang tunai dari laci kas ke safe untuk mengurangi risiko cash-onhand.
Entry criteria
- 
Trigger: ambang aman atau supervisor memutuskan safe drop.
- 
Precondition: Ada shift aktif dan kas tunai melebihi ambang aman atau supervisor
memutuskan safe drop.
Main activity flow
95.
Supervisor memilih shift/terminal yang akan di-safe-drop.
96.
Supervisor menghitung nominal uang yang akan dipindahkan.
97.
Sistem mencatat nominal, waktu, pelaksana, dan referensi shift.
98.
Supervisor mengonfirmasi bahwa uang telah dimasukkan ke safe.
99.
Sistem memperbarui expected drawer balance dan audit log.
Decision points dan loop penting
- 
Decision: Kasir menyiapkan uang? Nominal <= expected cash drawer? Dibatalkan sebelum
konfirmasi?.
Alternate paths
- 
Kasir menyiapkan uang, supervisor hanya mengotorisasi dan mengonfirmasi.
- 
Safe drop dilakukan beberapa kali dalam satu shift.
Exception dan recovery handling
- 
Nominal safe drop melebihi expected cash di drawer.
- 
Safe drop dibatalkan sebelum konfirmasi.
Exit criteria
- 
Postcondition: Supervisor menjalankan safe drop.
- 
Postcondition: Transaksi safe drop tercatat dan saldo kas laci berkurang.
Business rules dan traceability note
- 
Safe drop harus dapat diaudit dan terkait ke shift/terminal tertentu.
- 
Review diagram: Diagram memakai explicit start/stop dan sudah cocok untuk traceability
ke skenario test. Swimlane/partition yang terlihat: Supervisor, Kasir, Sistem POS.
Checkpoint keputusan utama: Kasir menyiapkan uang? Nominal <= expected cash drawer?
Dibatalkan sebelum konfirmasi?.
UC-20 - Tutup Shift
Use case asal
UC-20 - Tutup Shift
File activity diagram
uc20_tutup_shift_activity.puml
Primary actor
Kasir
Supporting actors / external systems
Supervisor
Partition pada diagram
Kasir, Sistem POS, Supervisor
Tujuan aktivitas
Menutup shift kasir secara resmi dengan rekonsiliasi kas dan pelaporan dasar.

Entry criteria
- 
Trigger: Kasir memilih tutup shift.
- 
Precondition: Shift aktif.
- 
Precondition: Semua transaksi pending pada shift telah ditangani atau di-mark sesuai
policy
Main activity flow
100. Kasir memulai proses close shift.
101. Sistem menampilkan expected cash drawer balance.
102. Kasir menghitung kas fisik dan memasukkan hasil hitung.
103. Sistem menjalankan rekonsiliasi kas.
104. Sistem menghasilkan X/Z report sesuai konfigurasi.
105. Kasir mengonfirmasi penutupan shift.
106. Sistem menutup shift dan menulis audit log.
Decision points dan loop penting
- 
Decision: Masih ada transaksi suspend/pending yang dilarang policy? Selisih <= tolerance?
Disetujui?.
Alternate paths
- 
Supervisor membantu verifikasi ketika ada selisih kas.
- 
Beberapa transaksi pending tetap dibawa ke investigasi setelah shift ditutup.
Exception dan recovery handling
- 
Selisih kas melebihi tolerance dan membutuhkan supervisor sign-off.
- 
Masih ada transaksi suspend/pending yang belum diresolusikan dan policy menolak close
shift.
Exit criteria
- 
Postcondition: Shift berubah menjadi closed.
- 
Postcondition: Data kas shift siap untuk end-of-day dan store reporting.
Business rules dan traceability note
- 
Close shift wajib include cash reconciliation dan report generation.
- 
Shift closed tidak boleh menerima transaksi baru.
- 
Review diagram: Diagram memakai explicit start/stop dan sudah cocok untuk traceability
ke skenario test. Swimlane/partition yang terlihat: Kasir, Sistem POS, Supervisor.
Checkpoint keputusan utama: Masih ada transaksi suspend/pending yang dilarang policy?
Selisih <= tolerance? Disetujui?.
UC-21 - Rekonsiliasi Kas
Use case asal
UC-21 - Rekonsiliasi Kas
File activity diagram
uc21_rekonsiliasi_kas_activity.puml
Primary actor
Kasir / Supervisor
Supporting actors / external systems
System
Partition pada diagram
User, Sistem POS, Supervisor

Tujuan aktivitas
Membandingkan kas fisik dan kas sistem untuk menemukan selisih shift.
Entry criteria
- 
Trigger: Close shift atau cash audit dilakukan.
- 
Precondition: Shift aktif yang akan ditutup memiliki expected cash balance
Main activity flow
107. User memasukkan hasil hitung fisik.
108. Sistem membandingkan dengan expected balance.
109. Sistem menampilkan selisih.
110. User mengonfirmasi hasil.
Decision points dan loop penting
- 
Decision: Selisih di atas tolerance? Input salah?.
Alternate paths
- 
Supervisor menandatangani selisih di atas tolerance.
Exception dan recovery handling
- 
Perhitungan ulang diperlukan karena input salah.
Exit criteria
- 
Postcondition: Hasil rekonsiliasi tersimpan.
Business rules dan traceability note
- 
Selisih harus dapat ditelusuri ke cash movement dan transaksi.
- 
Review diagram: Diagram memakai explicit start/stop dan sudah cocok untuk traceability
ke skenario test. Swimlane/partition yang terlihat: User, Sistem POS, Supervisor. Checkpoint
keputusan utama: Selisih di atas tolerance? Input salah?.
UC-22 - Generate X / Z Report
Use case asal
UC-22 - Generate X / Z Report
File activity diagram
uc22_generate_x_z_report_activity.puml
Primary actor
Supervisor
Supporting actors / external systems
Store Device
Partition pada diagram
Supervisor, Sistem POS, Store Device
Tujuan aktivitas
Menghasilkan ringkasan transaksi dan kas untuk kontrol shift atau harian.
Entry criteria
- 
Trigger: User meminta X report atau Z report.
- 
Precondition: Data transaksi dan kas tersedia untuk periode/report type
Main activity flow
111. User memilih jenis report.
112. Sistem mengumpulkan data relevan.

113. Sistem menghasilkan report.
114. Report ditampilkan atau dicetak.
Decision points dan loop penting
- 
Decision: Data report lengkap? Simpan digital?.
Alternate paths
- 
Report disimpan digital.
Exception dan recovery handling
- 
Data report belum lengkap.
Exit criteria
- 
Postcondition: Report tersedia.
Business rules dan traceability note
- 
Z report umumnya final/closing; X report interim/control.
- 
Review diagram: Diagram memakai explicit start/stop dan sudah cocok untuk traceability
ke skenario test. Swimlane/partition yang terlihat: Supervisor, Sistem POS, Store Device.
Checkpoint keputusan utama: Data report lengkap? Simpan digital?.
UC-23 - Tutup Hari
Use case asal
UC-23 - Tutup Hari
File activity diagram
uc23_tutup_hari_activity.puml
Primary actor
Store Manager
Supporting actors / external systems
Supervisor, HQ Store Service
Partition pada diagram
Store Manager, Sistem POS
Tujuan aktivitas
Menutup operasi toko untuk satu hari bisnis dan menghasilkan rekap operasional final.
Entry criteria
- 
Trigger: Store Manager menjalankan end of day.
- 
Precondition: Semua shift yang wajib ditutup telah selesai
- 
Precondition: Sinkronisasi minimum operasional tersedia. atau ditangani sesuai exception
policy
Main activity flow
115. Manager membuka fungsi end-of-day.
116. Sistem memverifikasi kesiapan: shift, settlement store-level, dan data wajib.
117. Sistem menghasilkan store operational report final.
118. Sistem menulis audit log end-of-day.
119. Sistem menandai business day selesai.
Decision points dan loop penting
- 
Decision: Masih ada shift aktif? Data kritikal belum lengkap? Operasi offline dipakai?.

Alternate paths
- 
Manager menunda penutupan hari hingga outstanding issue terselesaikan.
- 
Store tetap end-of-day dalam mode offline dan sinkronisasi dilakukan kemudian.
Exception dan recovery handling
- 
Masih ada shift aktif yang belum ditutup.
- 
Data kritikal belum lengkap sehingga hari tidak boleh ditutup.
Exit criteria
- 
Postcondition: Hari bisnis ditutup.
- 
Postcondition: Laporan operasional final tersedia untuk toko/HQ.
Business rules dan traceability note
- 
Satu toko hanya boleh memiliki satu business day aktif pada satu waktu.
- 
Review diagram: Diagram memakai explicit start/stop dan sudah cocok untuk traceability
ke skenario test. Swimlane/partition yang terlihat: Store Manager, Sistem POS. Checkpoint
keputusan utama: Masih ada shift aktif? Data kritikal belum lengkap? Operasi offline
dipakai?.

## 7. Inventori toko
UC-24 - Cek Stok & Movement
Use case asal
UC-24 - Cek Stok & Movement
File activity diagram
uc24_cek_stok_movement_activity.puml
Primary actor
Staff Inventori
Supporting actors / external systems
HQ Store Service (opsional)
Partition pada diagram
Staff Inventori, Sistem POS
Tujuan aktivitas
Melihat saldo stok dan histori movement untuk pengambilan keputusan operasional toko.
Entry criteria
- 
Trigger: tersedia.
- 
Precondition: User inventori terautentikasi.
- 
Precondition: Data stok lokal atau hasil sinkronisasi tersedia.
Main activity flow
120. Staff inventori mencari item berdasarkan SKU/barcode/nama.
121. Sistem menampilkan on-hand, available, reserved, dan lokasi bila tersedia.
## 122. Sistem menampilkan histori movement utama: sales, return, receiving, transfer,
adjustment.
123. User menganalisis hasil untuk tindakan lanjut.
Decision points dan loop penting
- 
Decision: Item ditemukan? Data movement full sync tersedia?.
Alternate paths
- 
Pencarian dilakukan per rak/lokasi.
- 
Data movement diambil dari cache lokal saat offline.
Exception dan recovery handling
- 
Item tidak ditemukan.
- 
Data movement belum sinkron penuh.
Exit criteria
- 
Postcondition: Staff inventori mencari stok barang.
- 
Postcondition: Informasi stok dan movement berhasil ditampilkan.
Business rules dan traceability note
- 
Movement harus memiliki timestamp dan source transaction yang bisa ditelusuri.
- 
Review diagram: Diagram memakai explicit start/stop dan sudah cocok untuk traceability
ke skenario test. Swimlane/partition yang terlihat: Staff Inventori, Sistem POS. Checkpoint
keputusan utama: Item ditemukan? Data movement full sync tersedia?.
UC-25 - Receiving Barang
Use case asal
UC-25 - Receiving Barang

File activity diagram
uc25_receiving_barang_activity.puml
Primary actor
Staff Inventori
Supporting actors / external systems
HQ Store Service, Device
Partition pada diagram
Staff Inventori, Sistem POS
Tujuan aktivitas
Menerima barang yang datang ke toko dan memperbarui stok penerimaan secara akurat.
Entry criteria
- 
Trigger: Staff inventori memulai proses receiving.
- 
Precondition: Ada delivery note / PO / transfer in yang
- 
Precondition: Barang fisik telah tiba di toko. valid
Main activity flow
124. Staff inventori memilih referensi delivery/PO.
125. Sistem memverifikasi data expected items.
126. Staff memindai atau menghitung item yang diterima.
127. Sistem membandingkan actual vs expected.
128. Jika ada selisih, staff mencatat receiving gap.
129. Staff mengonfirmasi receiving.
130. Sistem memperbarui stok dan menulis audit log.
Decision points dan loop penting
- 
Decision: Referensi valid? Ada selisih? Gap dijelaskan dengan valid?.
Alternate paths
- 
Receiving parsial dilakukan untuk pengiriman yang belum lengkap.
- 
Receiving dilakukan per carton/box sebelum per-item detail.
Exception dan recovery handling
- 
Referensi delivery tidak valid.
- 
Barang lebih/kurang dari expected tanpa penjelasan yang dapat diterima.
- 
Koneksi HQ tidak tersedia dan data referensi belum ada di lokal.
Exit criteria
- 
Postcondition: Stok receiving bertambah.
- 
Postcondition: Discrepancy tercatat bila ada.
Business rules dan traceability note
- 
Receiving harus include verifikasi delivery/PO.
- 
Selisih receiving harus terdokumentasi, tidak boleh hilang diam-diam.
- 
Review diagram: Diagram memakai explicit start/stop dan sudah cocok untuk traceability
ke skenario test. Swimlane/partition yang terlihat: Staff Inventori, Sistem POS. Checkpoint
keputusan utama: Referensi valid? Ada selisih? Gap dijelaskan dengan valid?.
UC-26 - Transfer Stok
Use case asal
UC-26 - Transfer Stok

File activity diagram
uc26_transfer_stok_activity.puml
Primary actor
Staff Inventori
Supporting actors / external systems
HQ Store Service
Partition pada diagram
Staff Inventori, Sistem POS
Tujuan aktivitas
Memindahkan stok antar lokasi/store dengan jejak transaksi yang jelas.
Entry criteria
- 
Trigger: Staff inventori membuat atau memproses transfer stok.
- 
Precondition: Item dan lokasi sumber/tujuan valid.
- 
Precondition: Stock tersedia di lokasi sumber.
Main activity flow
131. Staff memilih item dan kuantitas transfer.
132. Sistem memvalidasi stok sumber.
133. Staff memilih lokasi/store tujuan.
134. Sistem membuat dokumen transfer dan mengurangi stok sumber sesuai status proses.
135. Sistem menulis audit log dan status transfer.
Decision points dan loop penting
- 
Decision: Stok sumber cukup? Lokasi tujuan valid? Transfer hanya request?.
Alternate paths
- 
Transfer hanya request dan menunggu approval/proses toko tujuan.
- 
Transfer antar backroom-rak dalam satu toko disederhanakan sebagai internal transfer.
Exception dan recovery handling
- 
Stok sumber tidak cukup.
- 
Lokasi tujuan tidak valid.
- 
Transfer dibatalkan sebelum dikirim.
Exit criteria
- 
Postcondition: Dokumen transfer tercatat dan stok/commitment berubah sesuai status
transfer.
Business rules dan traceability note
- 
Tidak boleh memindahkan stok melebihi available stock.
- 
Review diagram: Diagram memakai explicit start/stop dan sudah cocok untuk traceability
ke skenario test. Swimlane/partition yang terlihat: Staff Inventori, Sistem POS. Checkpoint
keputusan utama: Stok sumber cukup? Lokasi tujuan valid? Transfer hanya request?.
UC-27 - Replenishment Rak
Use case asal
UC-27 - Replenishment Rak
File activity diagram
uc27_replenishment_rak_activity.puml
Primary actor
Staff Inventori

Supporting actors / external systems
Device
Partition pada diagram
Staff Inventori, Sistem POS
Tujuan aktivitas
Memindahkan stok dari backroom ke rak display agar ketersediaan barang untuk penjualan
terjaga.
Entry criteria
- 
Trigger: Staff inventori menjalankan replenishment.
- 
Precondition: Ada kebutuhan replenishment.
- 
Precondition: Stok tersedia di backroom atau lokasi sumber
Main activity flow
136. Staff melihat daftar item yang perlu direplenish.
137. Staff mengambil barang dari lokasi sumber.
138. Staff memindahkan dan menaruh barang ke rak.
139. Sistem atau staff mengonfirmasi kuantitas yang dipindah.
140. Sistem memperbarui lokasi stok internal.
Decision points dan loop penting
- 
Decision: Stok sumber tersedia? Rak valid dan tidak penuh?.
Alternate paths
- 
Replenishment dipicu berdasarkan min shelf quantity.
- 
Sebagian item tidak tersedia sehingga dipindahkan parsial.
Exception dan recovery handling
- 
Stok sumber ternyata tidak ada saat diambil.
- 
Lokasi rak tidak valid atau penuh.
Exit criteria
- 
Postcondition: Rak terisi ulang dan movement internal tercatat.
Business rules dan traceability note
- 
Replenishment tidak mengubah total on-hand toko, hanya lokasi internalnya.
- 
Review diagram: Diagram memakai explicit start/stop dan sudah cocok untuk traceability
ke skenario test. Swimlane/partition yang terlihat: Staff Inventori, Sistem POS. Checkpoint
keputusan utama: Stok sumber tersedia? Rak valid dan tidak penuh?.
UC-28 - Stock Adjustment
Use case asal
UC-28 - Stock Adjustment
File activity diagram
uc28_stock_adjustment_activity.puml
Primary actor
Staff Inventori / Supervisor
Supporting actors / external systems
Supervisor Approval
Partition pada diagram
Staff Inventori, Sistem POS, Supervisor

Tujuan aktivitas
Mengoreksi saldo stok karena kehilangan, kerusakan, mismatch sistem, atau sebab
operasional lain.
Entry criteria
- 
Trigger: Staff inventori mengajukan adjustment stok.
- 
Precondition: Item target dapat diidentifikasi.
- 
Precondition: Alasan adjustment tersedia.
Main activity flow
141. User memilih item dan kuantitas adjustment.
142. User menentukan arah adjustment dan alasan.
143. Sistem mengecek threshold dan kebutuhan approval.
144. Jika perlu, supervisor memberikan approval.
145. Sistem memperbarui stok serta ledger adjustment.
146. Sistem menulis audit log.
Decision points dan loop penting
- 
Decision: Item diblokir untuk adjustment? Perlu approval? Disetujui? Menyebabkan stok
negatif yang dilarang? Menyebabkan stok negatif yang dilarang?.
Alternate paths
- 
Adjustment kecil dalam tolerance dapat langsung diproses.
- 
Adjustment massal dilakukan dari hasil stock count.
Exception dan recovery handling
- 
Approval ditolak.
- 
Adjustment menyebabkan stok negatif yang tidak diizinkan.
- 
Item diblokir untuk adjustment.
Exit criteria
- 
Postcondition: Saldo stok berubah secara sah dan dapat diaudit.
Business rules dan traceability note
- 
Adjustment harus reason-based dan traceable.
- 
Adjustment besar wajib approval.
- 
Review diagram: Diagram memakai explicit start/stop dan sudah cocok untuk traceability
ke skenario test. Swimlane/partition yang terlihat: Staff Inventori, Sistem POS, Supervisor.
Checkpoint keputusan utama: Item diblokir untuk adjustment? Perlu approval? Disetujui?
Menyebabkan stok negatif yang dilarang? Menyebabkan stok negatif yang dilarang?.
UC-29 - Stock Opname / Cycle Count
Use case asal
UC-29 - Stock Opname / Cycle Count
File activity diagram
uc29_stock_opname_cycle_count_activity.pu
ml
Primary actor
Staff Inventori / Supervisor
Supporting actors / external systems
Device

Partition pada diagram
Staff Inventori, Sistem POS
Tujuan aktivitas
Menghitung stok fisik dan membandingkannya dengan saldo sistem untuk menjaga akurasi
inventori.
Entry criteria
- 
Trigger: Staff inventori memulai cycle count atau stock opname.
- 
Precondition: Periode count atau assignment count
- 
Precondition: Area/item count telah ditetapkan. tersedia
Main activity flow
147. User memilih batch/area count.
148. User menghitung item fisik dan memasukkan hasil atau memindai item.
149. Sistem membandingkan hasil count dengan stok sistem.
150. Sistem menandai selisih.
151. User mereview dan mengonfirmasi hasil count.
152. Jika policy mengizinkan, selisih diturunkan menjadi stock adjustment.
153. Sistem menulis audit log.
Decision points dan loop penting
- 
Decision: Double count diperlukan? Selisih boleh diturunkan ke adjustment? Butuh
approval tambahan?.
Alternate paths
- 
Double count dijalankan untuk item bernilai tinggi.
- 
Supervisor mereview item dengan mismatch besar.
Exception dan recovery handling
- 
Batch count dibatalkan.
- 
Data count tidak lengkap.
- 
Adjustment otomatis ditolak karena butuh approval.
Exit criteria
- 
Postcondition: Hasil count tersimpan dan discrepancy teridentifikasi.
Business rules dan traceability note
- 
Cycle count harus punya snapshot waktu yang jelas agar selisih dapat dijelaskan.
- 
Review diagram: Diagram memakai explicit start/stop dan sudah cocok untuk traceability
ke skenario test. Swimlane/partition yang terlihat: Staff Inventori, Sistem POS. Checkpoint
keputusan utama: Double count diperlukan? Selisih boleh diturunkan ke adjustment?
Butuh approval tambahan?.
UC-30 - Kelola Barang Rusak / Expired
Use case asal
UC-30 - Kelola Barang Rusak / Expired
File activity diagram
uc30_kelola_barang_rusak_expired_activity.p
uml
Primary actor
Staff Inventori

Supporting actors / external systems
Supervisor (opsional)
Partition pada diagram
Staff Inventori, Sistem POS, Supervisor
Tujuan aktivitas
Mengidentifikasi dan memproses barang rusak atau kedaluwarsa agar tidak dijual ke
pelanggan.
Entry criteria
- 
Trigger: Staff inventori memproses barang rusak/expired. normal.
- 
Precondition: Barang teridentifikasi rusak/expired atau
- 
Precondition: Kategori disposal tersedia. mendekati expired
Main activity flow
154. User memilih item dan kuantitas.
155. User mencatat kategori: damaged, expired, quarantine, disposal, atau return-to-vendor.
156. Sistem memindahkan stok ke bucket status yang sesuai.
157. Jika perlu, supervisor mereview dan menyetujui tindakan.
158. Sistem menyimpan transaksi dan audit trail.
Decision points dan loop penting
- 
Decision: Kuantitas <= stok tersedia? Perlu review supervisor? Disetujui?.
Alternate paths
- 
Barang ditandai quarantine untuk inspeksi lebih lanjut.
- 
Barang dikembalikan ke vendor mengikuti proses RTV.
Exception dan recovery handling
- 
Item tidak eligible untuk disposal tanpa verifikasi tambahan.
- 
Kuantitas melebihi stok tersedia.
Exit criteria
- 
Postcondition: Barang tidak lagi tersedia untuk penjualan
- 
Postcondition: Status dan lokasi stok tercatat sesuai tindakan.
Business rules dan traceability note
- 
Barang expired tidak boleh tetap berada pada sellable stock.
- 
Review diagram: Diagram memakai explicit start/stop dan sudah cocok untuk traceability
ke skenario test. Swimlane/partition yang terlihat: Staff Inventori, Sistem POS, Supervisor.
Checkpoint keputusan utama: Kuantitas <= stok tersedia? Perlu review supervisor?
Disetujui?.
UC-31 - Verifikasi Delivery / PO
Use case asal
UC-31 - Verifikasi Delivery / PO
File activity diagram
uc31_verifikasi_delivery_po_activity.puml
Primary actor
Staff Inventori
Supporting actors / external systems
HQ Store Service

Partition pada diagram
Staff Inventori, Sistem POS
Tujuan aktivitas
Memastikan barang yang diterima mengacu ke dokumen supply yang sah.
Entry criteria
- 
Trigger: Receiving dimulai.
- 
Precondition: Nomor delivery/PO tersedia.
Main activity flow
159. User memasukkan referensi delivery/PO.
160. Sistem mengambil expected items.
161. Sistem memvalidasi dokumen aktif dan tujuan toko.
Decision points dan loop penting
- 
Decision: Data lokal tersedia? Dokumen aktif dan milik toko?.
Alternate paths
- 
Data diambil dari cache lokal.
Exception dan recovery handling
- 
Dokumen tidak valid atau bukan milik toko.
Exit criteria
- 
Postcondition: Dokumen receiving tervalidasi.
Business rules dan traceability note
- 
Receiving tanpa referensi hanya boleh jika policy exception mengizinkan.
- 
Review diagram: Diagram memakai explicit start/stop dan sudah cocok untuk traceability
ke skenario test. Swimlane/partition yang terlihat: Staff Inventori, Sistem POS. Checkpoint
keputusan utama: Data lokal tersedia? Dokumen aktif dan milik toko?.
UC-32 - Catat Selisih Receiving
Use case asal
UC-32 - Catat Selisih Receiving
File activity diagram
uc32_catat_selisih_receiving_activity.puml
Primary actor
Staff Inventori
Supporting actors / external systems
Supervisor
Partition pada diagram
Staff Inventori, Sistem POS, Supervisor
Tujuan aktivitas
Mencatat gap antara quantity expected dan quantity actual saat receiving.
Entry criteria
- 
Trigger: Sistem mendeteksi mismatch expected vs actual.
- 
Precondition: Receiving sedang berlangsung dan ditemukan selisih
Main activity flow
162. User memilih item mismatch.

163. User mencatat jenis gap: short, over, damaged, wrong item.
164. Sistem menyimpan discrepancy record.
Decision points dan loop penting
- 
Decision: Selisih besar? Gap sudah dijelaskan?.
Alternate paths
- 
Supervisor diminta untuk selisih besar.
Exception dan recovery handling
- 
User mencoba menutup receiving tanpa menjelaskan gap.
Exit criteria
- 
Postcondition: Discrepancy receiving tersimpan.
Business rules dan traceability note
- 
Semua mismatch receiving wajib tercatat.
- 
Review diagram: Diagram memakai explicit start/stop dan sudah cocok untuk traceability
ke skenario test. Swimlane/partition yang terlihat: Staff Inventori, Sistem POS, Supervisor.
Checkpoint keputusan utama: Selisih besar? Gap sudah dijelaskan?.
UC-33 - Cetak Label Harga / Rak
Use case asal
UC-33 - Cetak Label Harga / Rak
File activity diagram
uc33_cetak_label_harga_rak_activity.puml
Primary actor
Staff Inventori
Supporting actors / external systems
Store Device
Partition pada diagram
Staff Inventori, Sistem POS, Store Device
Tujuan aktivitas
Mencetak label harga atau label rak yang akurat untuk mendukung display toko.
Entry criteria
- 
Trigger: Staff inventori memilih cetak label.
- 
Precondition: Template label tersedia.
- 
Precondition: Data harga/SKU valid.
Main activity flow
165. User memilih item atau rak target.
166. Sistem mengambil data harga, nama, barcode, dan atribut label.
167. User memilih jumlah label.
168. Sistem mengirim job ke printer label.
169. Printer mencetak label.
Decision points dan loop penting
- 
Decision: Data harga valid dan sinkron? Perlu preview? Printer tersedia dan template
cocok?.

Alternate paths
- 
Cetak label massal dari hasil price change atau replenishment.
- 
Preview label ditampilkan sebelum cetak.
Exception dan recovery handling
- 
Printer tidak tersedia.
- 
Data harga belum sinkron.
- 
Template label tidak cocok dengan device.
Exit criteria
- 
Postcondition: Label tercetak atau job gagal dengan error yang bisa ditindaklanjuti.
Business rules dan traceability note
- 
Label harus menggunakan harga aktif yang berlaku untuk toko terkait.
- 
Review diagram: Diagram memakai explicit start/stop dan sudah cocok untuk traceability
ke skenario test. Swimlane/partition yang terlihat: Staff Inventori, Sistem POS, Store Device.
Checkpoint keputusan utama: Data harga valid dan sinkron? Perlu preview? Printer
tersedia dan template cocok?.
UC-34 - Lihat Laporan Operasional Toko
Use case asal
UC-34 - Lihat Laporan Operasional Toko
File activity diagram
uc34_lihat_laporan_operasional_toko_activity
.puml
Primary actor
Store Manager
Supporting actors / external systems
HQ Store Service
Partition pada diagram
Store Manager, Sistem POS
Tujuan aktivitas
Melihat KPI dan laporan operasional toko untuk monitoring performa dan kontrol harian.
Entry criteria
- 
Trigger: Manager membuka menu laporan operasional.
- 
Precondition: Manager terautentikasi.
- 
Precondition: Data laporan tersedia untuk periode yang diminta
Main activity flow
170. Manager memilih periode/laporan.
## 171. Sistem mengambil data penjualan, return, kas, inventori, dan sinkronisasi yang
relevan.
172. Sistem menampilkan laporan operasional toko.
173. Manager melakukan analisis atau ekspor bila tersedia.
Decision points dan loop penting
- 
Decision: Filter tambahan diperlukan? Offline mode? Role berhak dan data cukup?.
Alternate paths
- 
Laporan ditampilkan dari cache lokal terakhir saat offline.

- 
Manager memfilter per shift atau per kategori.
Exception dan recovery handling
- 
Data laporan belum lengkap.
- 
Akses role tidak cukup untuk laporan tertentu.
Exit criteria
- 
Postcondition: Laporan tampil untuk keperluan operasional/pengambilan keputusan.
Business rules dan traceability note
- 
Definisi metrik harus konsisten dengan data transaksi dan business day.
- 
Review diagram: Diagram memakai explicit start/stop dan sudah cocok untuk traceability
ke skenario test. Swimlane/partition yang terlihat: Store Manager, Sistem POS. Checkpoint
keputusan utama: Filter tambahan diperlukan? Offline mode? Role berhak dan data
cukup?.

## 8. Sinkronisasi dan layanan internal
UC-35 - Sinkronisasi Data Toko
Use case asal
UC-35 - Sinkronisasi Data Toko
File activity diagram
uc35_sinkronisasi_data_toko_activity.puml
Primary actor
Store Manager / System
Supporting actors / external systems
HQ Store Service
Partition pada diagram
System Scheduler / Store Manager, Sistem
POS, HQ Store Service
Tujuan aktivitas
Menyamakan data transaksi, stok, master data, dan status operasional toko dengan HQ
services.
Entry criteria
- 
Trigger: Manager menjalankan sinkronisasi manual atau sistem menjadwalkan sync.
- 
Precondition: Koneksi ke HQ tersedia atau job sync dapat
- 
Precondition: Identitas toko valid. diantrikan
Main activity flow
174. Sistem menyiapkan batch data yang perlu dikirim/diambil.
175. Sistem mengirim transaksi/store events ke HQ.
176. Sistem mengambil update master data dan status yang diperlukan.
177. Sistem memproses hasil sinkronisasi dan menandai item sukses.
178. Bila ada kegagalan parsial, sistem membuat daftar rekonsiliasi.
179. Status sinkronisasi ditampilkan ke user.
Decision points dan loop penting
- 
Decision: Semua batch sukses? Retry otomatis diizinkan? Retry berhasil?.
Alternate paths
- 
Sync hanya data tertentu, misalnya master price atau transaksi penjualan.
- 
Retry otomatis dijalankan untuk batch gagal.
Exception dan recovery handling
- 
Koneksi HQ gagal.
- 
Conflict data terjadi antara local dan HQ.
- 
Sebagian batch gagal dan perlu reconciliation.
Exit criteria
- 
Postcondition: Data sukses tersinkron atau item gagal masuk daftar reconcile.
Business rules dan traceability note
- 
Sync harus idempotent sebisa mungkin.
- 
Kegagalan sinkron tidak boleh silently drop data.
- 
Review diagram: Diagram memakai explicit start/stop dan sudah cocok untuk traceability
ke skenario test. Swimlane/partition yang terlihat: System Scheduler / Store Manager,

Sistem POS, HQ Store Service. Checkpoint keputusan utama: Semua batch sukses? Retry
otomatis diizinkan? Retry berhasil?.
UC-36 - Mode Operasi Offline
Use case asal
UC-36 - Mode Operasi Offline
File activity diagram
uc36_mode_operasi_offline_activity.puml
Primary actor
Supporting actors / external systems
Store Staff Store Manager, HQ Store Service,
Payment Gateway / EDC
Partition pada diagram
Sistem POS, Store Staff
Tujuan aktivitas
Menjaga toko tetap dapat beroperasi ketika koneksi ke service pusat atau provider tertentu
terganggu.
Entry criteria
- 
Trigger: terbatas.
- 
Precondition: Gangguan konektivitas terdeteksi atau
- 
Precondition: Store policy mengizinkan operasi offline external dependency tidak tersedia
Main activity flow
180. Sistem mengubah status operasional menjadi offline mode.
181. Sistem mengaktifkan aturan fallback untuk data lokal/cached.
## 182. User tetap menjalankan operasi yang diizinkan, seperti penjualan dengan batasan
tertentu.
183. Sistem menandai transaksi dan perubahan data sebagai pending sync.
184. Saat koneksi pulih, sistem menjalankan sinkronisasi dan reconciliation.
Decision points dan loop penting
- 
Decision: Policy mengizinkan offline mode? Data cache tidak memadai? Koneksi pulih?.
Alternate paths
- 
Hanya fitur tertentu yang offline, misalnya loyalty nonaktif tetapi cash sales tetap berjalan.
- 
Manager menonaktifkan sementara fitur yang terlalu berisiko saat offline.
Exception dan recovery handling
- 
Data cache tidak memadai sehingga operasi tertentu harus diblokir.
- 
Clock/time drift membuat sinkronisasi berisiko conflict.
Exit criteria
- 
Postcondition: Sistem mendeteksi gangguan atau user mengaktifkan mode offline sesuai
policy.
- 
Postcondition: Store beroperasi dalam degraded mode dengan backlog sinkronisasi yang
terkontrol.

Business rules dan traceability note
- 
Offline mode bukan bypass total; hanya fungsi yang aman secara bisnis yang boleh tetap
jalan.
- 
Semua transaksi offline harus dapat direconcile saat koneksi kembali.
- 
## 7. Supporting Use Case Specifications Catatan: Section ini memuat supporting/internal use
cases yang muncul pada relasi include/extend atau reusable operational step. Detailnya
dibuat lebih ringkas, tetapi tetap cukup untuk design review dan turunan test.
- 
Review diagram: Diagram memakai explicit start/stop dan sudah cocok untuk traceability
ke skenario test. Swimlane/partition yang terlihat: Sistem POS, Store Staff. Checkpoint
keputusan utama: Policy mengizinkan offline mode? Data cache tidak memadai? Koneksi
pulih?.
UC-37 - Rekonsiliasi Sync Gagal
Use case asal
UC-37 - Rekonsiliasi Sync Gagal
File activity diagram
uc37_rekonsiliasi_sync_gagal_activity.puml
Primary actor
Store Manager / Supervisor
Supporting actors / external systems
HQ Store Service
Partition pada diagram
Store Manager / Supervisor, Sistem POS
Tujuan aktivitas
Menyelesaikan batch sinkronisasi yang gagal agar data toko dan HQ kembali konsisten.
Entry criteria
- 
Trigger: Manager membuka daftar sync gagal atau sistem memberi alert.
- 
Precondition: Ada batch sync berstatus gagal atau parsial.
Main activity flow
185. User meninjau item sync gagal.
186. Sistem menampilkan penyebab dan payload ringkas.
187. User memilih retry, resolve conflict, atau mark for manual investigation.
188. Sistem memperbarui status rekonsiliasi.
Decision points dan loop penting
- 
Decision: Pilih retry? Retry berhasil? Pilih resolve conflict?.
Alternate paths
- 
Retry otomatis berhasil tanpa intervensi manual.
Exception dan recovery handling
- 
Conflict tidak dapat di-resolve otomatis.
Exit criteria
- 
Postcondition: Item sync gagal berkurang atau tercatat untuk investigasi.
Business rules dan traceability note
- 
Tidak boleh ada sync failure yang hilang tanpa status akhir.

- 
Review diagram: Diagram memakai explicit start/stop dan sudah cocok untuk traceability
ke skenario test. Swimlane/partition yang terlihat: Store Manager / Supervisor, Sistem POS.
Checkpoint keputusan utama: Pilih retry? Retry berhasil? Pilih resolve conflict?.
UC-38 - Autentikasi & Validasi Role
Use case asal
UC-38 - Autentikasi & Validasi Role
File activity diagram
uc38_autentikasi_validasi_role_activity.puml
Primary actor
Identity Service / Store Staff
Supporting actors / external systems
System
Partition pada diagram
Store Staff, Sistem POS, Identity Service
Tujuan aktivitas
Memastikan hanya user dengan identitas dan role yang sah yang dapat menjalankan fungsi
sistem.
Entry criteria
- 
Trigger: User login atau mengakses fungsi berizin.
- 
Precondition: Kredensial atau token tersedia.
Main activity flow
189. Sistem menerima kredensial.
190. Sistem memverifikasi identitas.
191. Sistem memuat role dan permission.
192. Sistem memberikan keputusan allow/deny.
Decision points dan loop penting
- 
Decision: Online identity service tersedia? Offline credential cache diizinkan?
Role/permission sesuai?.
Alternate paths
- 
Offline credential cache dipakai sesuai policy.
Exception dan recovery handling
- 
Identity service tidak tersedia dan fallback tidak diizinkan.
Exit criteria
- 
Postcondition: Sesi user valid atau akses ditolak.
Business rules dan traceability note
- 
Authorization harus berbasis role yang sesuai fungsi.
- 
Review diagram: Diagram memakai explicit start/stop dan sudah cocok untuk traceability
ke skenario test. Swimlane/partition yang terlihat: Store Staff, Sistem POS, Identity Service.
Checkpoint keputusan utama: Online identity service tersedia? Offline credential cache
diizinkan? Role/permission sesuai?.

UC-39 - Catat Audit Log
Use case asal
UC-39 - Catat Audit Log
File activity diagram
uc39_catat_audit_log_activity.puml
Primary actor
System
Supporting actors / external systems
All business use cases
Partition pada diagram
Sistem POS
Tujuan aktivitas
Menyimpan jejak audit atas tindakan penting yang relevan untuk kontrol dan investigasi.
Entry criteria
- 
Trigger: Use case yang mewajibkan audit selesai atau berubah state penting.
- 
Precondition: Ada aksi bisnis signifikan atau event keamanan
Main activity flow
193. Sistem membentuk record audit.
## 194. Sistem menyimpan timestamp, actor, action, target object, before/after context bila
perlu.
195. Sistem memastikan audit record immutable atau terlindungi.
Decision points dan loop penting
- 
Decision: Storage audit lokal tersedia? Perlu kirim central store async?.
Alternate paths
- 
Audit dikirim async ke central store setelah disimpan lokal.
Exception dan recovery handling
- 
Storage audit sementara tidak tersedia -> event diantrikan secara aman.
Exit criteria
- 
Postcondition: Audit trail tersimpan.
Business rules dan traceability note
- 
Aksi sensitif tidak boleh lolos tanpa audit record.
- 
## 8. Traceability Ringkas
- 
UC-02 Proses Penjualan menurunkan activity diagram checkout, sequence diagram sales
checkout, domain model Sale/Cart/Payment/Receipt, dan test untuk
success/declined/pending payment.
- 
UC-11 Return / Refund menurunkan sequence refund authorization, domain
ReturnTransaction/ReturnPolicy/RefundPayment, dan test policy eligibility.
- 
UC-20 Tutup Shift dan UC-23 Tutup Hari menurunkan cash reconciliation, reporting, dan
operational closing controls.
- 
UC-25 sampai UC-30 menurunkan domain stock ledger, stock movement, receiving
discrepancy, adjustment reason, dan cycle count result.
- 
UC-35 sampai UC-37 menurunkan integration architecture untuk sync queue, retry, conflict
handling, serta offline backlog.
- 
## 9. Kritik Model dan Rekomendasi Perbaikan

- 
Diagram saat ini cukup rapi secara struktur paket, tetapi ada campuran antara user-goal
use case dan internal service use case. Itu tidak salah total, tetapi untuk review bisnis lebih
bersih jika UC_Auth dan UC_AuditLog dipindahkan sebagai internal behavior note atau
supporting service catalogue.
- 
UC_OfflineMode sebagai use case bisa diterima bila dimaknai sebagai business-operational
capability. Namun secara implementasi, ia lebih dekat ke system state/cross-cutting
capability dibanding goal actor tunggal. Pada level artefak berikutnya, lebih baik
dimodelkan juga pada architecture/state/sequence, bukan hanya use case.
- 
UC_Receipt menggunakan <<extend>> dari UC_Sales. Itu masuk akal bila receipt benar-
benar conditional. Jika receipt selalu wajib setelah penjualan sukses, relasi yang lebih tepat
secara semantik adalah <<include>> atau cukup dianggap bagian dari basic flow UC_Sales.
- 
UC_Override belum dihubungkan langsung ke actor pada diagram. Secara operasional,
seharusnya Kasir (dan kadang Supervisor) memang terasosiasi langsung agar scope user-
goal tidak ambigu.
- 
Related / included use cases: UC-02, UC-11, UC-20, UC-23, UC-25, UC-30, UC-35, UC-37
- 
Review diagram: Diagram memakai explicit start/stop dan sudah cocok untuk traceability
ke skenario test. Swimlane/partition yang terlihat: Sistem POS. Checkpoint keputusan
utama: Storage audit lokal tersedia? Perlu kirim central store async?.

## 9. Catatan penutup
Dokumen ini dimaksudkan sebagai baseline tekstual yang lebih presisi daripada diagram saja.
Untuk tahap berikutnya, artefak yang paling logis adalah sequence diagram per UC utama,
karena interaction contract kini sudah cukup jelas: actor, trigger, decision gate, dan exit state
telah dibakukan.
- 
UC dengan prioritas tertinggi untuk dilanjutkan ke sequence diagram: UC-02, UC-11, UC-20,
UC-25, UC-28, UC-35.
- 
Untuk implementasi, decision gate yang paling sensitif adalah supervisor approval,
payment pending/failed, offline sync, dan inventory discrepancy.
- 
Untuk QA, setiap section di dokumen ini sudah dapat dipakai sebagai sumber happy path,
alternate path, dan failure path.


## Constraints / Policies
Tetap traceable ke use case asal dan file activity .puml terkait.

## Technical Notes
Partition/swinmlane, ownership decision, dan checkpoint transisi penting harus dipertahankan karena menjadi input sequence dan test derivation.

## Dependencies / Related Documents
- `uml_modeling_source_of_truth.md`
- `store_pos_use_case_detail_specifications.md`
- `store_pos_sequence_detail_specifications.md`
- `traceability_matrix_store_pos.md`
- `store_pos_test_specification.md`

## Risks / Gaps / Ambiguities
- Tidak ditemukan gap fatal saat ekstraksi. Tetap review ulang bagian tabel/angka jika dokumen ini akan dijadikan baseline implementasi final.

## Reviewer Notes
- Struktur telah dinormalisasi ke Markdown yang konsisten dan siap dipakai untuk design review / engineering handoff.
- Istilah utama dipertahankan sedekat mungkin dengan dokumen sumber untuk menjaga traceability lintas artefak.
- Bila ada konflik antar dokumen, prioritaskan source of truth, artefak yang paling preskriptif, dan baseline yang paling implementable.

## Source Mapping
- Original source: `store_pos_activity_detail_specifications.pdf` (PDF, 46 pages)
- Output markdown: `store_pos_activity_detail_specifications.md`
- Conversion approach: text extraction -> normalization -> structural cleanup -> standardized documentation wrapper.
- Note: beberapa tabel/list di PDF dapat mengalami wrapping antar baris; esensi dipertahankan, tetapi layout tabel asli tidak dipertahankan 1:1.
