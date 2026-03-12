# Store POS Use Case Detail Specifications

## Document Overview
Baseline use case tekstual untuk 39 use case Store/POS System, lengkap dengan tujuan bisnis, precondition, trigger, main flow, alternate flow, exception flow, dan business rules.

## Purpose
• Mendefinisikan use case detail specifications yang konsisten dengan diagram Store / POS System. • Menjadi baseline untuk activity diagram, sequence diagram, desain domain, database, implementation, dan test case. • Menjaga traceability antara tujuan bisnis, aktor, alur utama...

## Scope
Primary business use cases + supporting/internal use cases Pendekatan Spesifikasi tekstual dengan traceability ke include/extend Catatan Beberapa use case internal diperlakukan sebagai supporting use case, bukan user goal utama

## Key Decisions / Core Rules
Membedakan primary use case vs supporting use case; include diperlakukan sebagai perilaku wajib reuse; extend diperlakukan sebagai perilaku kondisional.

## Detailed Content

### Normalized Source Body
Use Case Detail Specifications
Store / POS System
Turunan tekstual dari use case diagram PlantUML yang diberikan pengguna
Format: business-ready, design-review-ready, dan testable
Sumber Input
PlantUML use case diagram Store / POS
System
Cakupan
Primary business use cases +
supporting/internal use cases
Pendekatan
Spesifikasi tekstual dengan traceability ke
include/extend
Catatan
Beberapa use case internal diperlakukan
sebagai supporting use case, bukan user goal
utama

## 1. Tujuan Dokumen
- Mendefinisikan use case detail specifications yang konsisten dengan diagram Store / POS
System.
- Menjadi baseline untuk activity diagram, sequence diagram, desain domain, database,
implementation, dan test case.
- Menjaga traceability antara tujuan bisnis, aktor, alur utama, alternate flow, exception flow,
dan aturan bisnis.
## 2. Asumsi Penting
- Store / POS System digunakan di toko retail fisik dengan mode online-first, namun tetap
mendukung operasi offline terbatas.
- Satu shift kasir terkait dengan satu user kasir dan satu terminal POS pada satu rentang waktu
kerja.
- Supervisor approval diperlukan hanya pada kondisi yang dikontrol policy, misalnya void
setelah payment, return tanpa receipt valid, stock adjustment besar, atau manual discount di
atas threshold.
- Loyalty, payment gateway/EDC, identity service, dan HQ store service adalah external systems
di luar system boundary.
- Use case UC_Auth dan UC_AuditLog adalah supporting internal services. Keduanya bukan
tujuan bisnis utama actor, tetapi tetap dispesifikasikan agar desain dan test tidak ambigu.
- Receipt dapat berupa printed receipt atau digital receipt bila device/store configuration
mendukung.
## 3. Boundary dan Scope
- In scope: penjualan POS, return/refund, shift & cash control, operasional inventori toko,
pelaporan operasional, sinkronisasi data toko, serta supporting services internal.
- Out of scope: procurement HQ, pricing rule authoring di HQ, master data authoring, settlement
bank di luar toko, dan customer self-service channel non-store.
## 4. Normalisasi Model Use Case
- Use case yang directly memberi nilai bisnis ke actor diperlakukan sebagai primary use case.
- Use case yang hanya reusable step atau internal capability diperlakukan sebagai supporting
use case.
- Relasi <<include>> diinterpretasikan sebagai perilaku wajib yang reused.
- Relasi <<extend>> diinterpretasikan sebagai perilaku kondisional/opsional yang dipicu aturan
tertentu.
## 5. Katalog Use Case
ID
Use Case
Primary Actor
Kategori
UC-01
Mulai Shift Kasir
Kasir
Primary
UC-02
Proses Penjualan
Kasir
Primary

UC-03
Kelola Keranjang
Kasir
Supporting
UC-04
Terapkan Member /
Voucher
Kasir / Pelanggan
Primary
UC-05
Proses Pembayaran
Kasir
Primary
UC-06
Terbitkan Receipt
Kasir
Supporting
UC-07
Suspend / Resume
Transaksi
Kasir
Primary
UC-08
Cari / Scan Produk
Kasir
Supporting
UC-09
Hitung Harga, Promo,
Pajak
System
Supporting
UC-10
Void Item / Transaksi
Kasir
Primary
UC-11
Return / Refund
Kasir / Pelanggan
Primary
UC-12
Price Override /
Manual Discount
Kasir
Primary
UC-13
Lookup Receipt
Kasir
Supporting
UC-14
Validasi Return Policy
System
Supporting
UC-15
Supervisor Approval
Supervisor
Supporting
UC-16
Catat Alasan
Exception
Kasir / Supervisor
Supporting
UC-17
Input Kas Awal
Kasir
Supporting
UC-18
Cash In / Cash Out
Kasir
Primary
UC-19
Safe Drop
Supervisor
Primary
UC-20
Tutup Shift
Kasir
Primary
UC-21
Rekonsiliasi Kas
Supervisor / Kasir
Supporting
UC-22
Generate X / Z Report
Supervisor
Supporting
UC-23
Tutup Hari
Store Manager
Primary
UC-24
Cek Stok & Movement
Staff Inventori
Primary
UC-25
Receiving Barang
Staff Inventori
Primary
UC-26
Transfer Stok
Staff Inventori
Primary
UC-27
Replenishment Rak
Staff Inventori
Primary
UC-28
Stock Adjustment
Staff Inventori /
Supervisor
Primary
UC-29
Stock Opname / Cycle
Count
Staff Inventori /
Supervisor
Primary
UC-30
Kelola Barang Rusak /
Expired
Staff Inventori
Primary
UC-31
Verifikasi Delivery /
PO
System / Staff
Inventori
Supporting
UC-32
Catat Selisih
Receiving
Staff Inventori
Supporting
UC-33
Cetak Label Harga /
Rak
Staff Inventori
Primary
UC-34
Lihat Laporan
Operasional Toko
Store Manager
Primary
UC-35
Sinkronisasi Data
Store Manager /
Primary

Toko
System
UC-36
Mode Operasi Offline
Store Staff
Primary
UC-37
Rekonsiliasi Sync
Gagal
Store Manager /
Supervisor
Supporting
UC-38
Autentikasi & Validasi
Role
Identity Service /
Store Staff
Supporting
UC-39
Catat Audit Log
System
Supporting

## 6. Detailed Specifications - Primary Use Cases
UC-01 - Mulai Shift Kasir
Tujuan
Membuka shift kerja kasir sehingga terminal
POS siap dipakai untuk transaksi pada hari
operasional.
Primary Actor
Kasir
Supporting Actors
Identity Service, Supervisor (jika ada
pengecualian), Store Device
Preconditions
- Kasir terdaftar dan memiliki role aktif.
- Terminal POS online atau dapat bekerja
dalam mode offline yang diizinkan.
- Shift sebelumnya pada terminal sudah
ditutup atau diambil alih sesuai policy.
Trigger
Kasir memilih menu mulai shift.
Postconditions
- Shift aktif terbentuk.
- Opening cash tercatat.
Main Flow
1. Kasir melakukan login ke POS.
2. Sistem menjalankan autentikasi dan validasi role.
3. Sistem memverifikasi bahwa tidak ada shift aktif yang konflik pada terminal tersebut.
4. Kasir memasukkan nominal kas awal.
5. Sistem mencatat shift baru beserta opening cash.
6. Sistem menampilkan status shift aktif dan terminal siap transaksi.
Alternate Flow
- Autentikasi gagal, sistem menolak pembukaan shift.
- Jika ada shift lama belum ditutup, sistem meminta proses handover atau supervisor handling.
Exception Flow
- Identity service tidak tersedia dan offline login tidak diizinkan -> shift tidak dapat dibuka.
- Nominal opening cash di luar policy -> perlu koreksi atau approval.
Business Rules
- Satu kasir tidak boleh memiliki dua shift aktif pada terminal berbeda tanpa policy eksplisit.
- Opening cash wajib diinput pada awal shift sesuai include UC-17.
UC-02 - Proses Penjualan
Tujuan
Menyelesaikan transaksi penjualan barang
hingga pembayaran diterima dan penjualan
tercatat sah.
Primary Actor
Kasir
Supporting Actors
Pelanggan, Payment Gateway / EDC, Loyalty

Service, Store Device
Preconditions
- Shift kasir aktif.
- POS memiliki akses ke katalog produk dan
pricing rule terbaru atau cache offline yang
valid.
Trigger
Pelanggan ingin membeli barang dan kasir
memulai transaksi baru.
Postconditions
- Transaksi penjualan tersimpan.
- Stok dan ledger penjualan ter-update atau
ditandai untuk sinkronisasi.
- Receipt tersedia.
Main Flow
1. Kasir membuat transaksi penjualan baru.
2. Kasir menambah item ke keranjang melalui scan atau pencarian produk.
3. Sistem menghitung harga, promo, dan pajak setiap perubahan keranjang.
4. Kasir dapat menerapkan member atau voucher bila diminta pelanggan.
5. Kasir mengonfirmasi total pembayaran kepada pelanggan.
6. Kasir memproses pembayaran sesuai metode yang dipilih.
7. Sistem mengotorisasi pembayaran dan menyimpan transaksi penjualan.
8. Sistem menerbitkan receipt dan menandai transaksi selesai.
9. Sistem menulis audit log transaksi.
Alternate Flow
- Kasir mengubah kuantitas atau menghapus item sebelum pembayaran.
- Transaksi disuspend untuk dilanjutkan nanti.
- Receipt diterbitkan dalam bentuk digital bila printer tidak digunakan.
Exception Flow
- Produk tidak ditemukan atau barcode tidak valid.
- Perhitungan harga gagal karena master data corrupt -> transaksi ditahan.
- Pembayaran gagal -> transaksi tetap unpaid/pending dan tidak selesai.
Business Rules
- Penjualan tidak boleh final tanpa status payment yang valid.
- UC-03, UC-05, dan UC-39 bersifat wajib melalui include.
UC-04 - Terapkan Member / Voucher
Tujuan
Menerapkan benefit member, poin, atau
voucher yang sah ke transaksi aktif.
Primary Actor
Kasir / Pelanggan
Supporting Actors
Loyalty Service
Preconditions
- Ada transaksi aktif.
- Member ID, nomor telepon, QR voucher,

atau kode promo tersedia.
Trigger
Pelanggan meminta benefit loyalty atau
menggunakan voucher.
Postconditions
- Benefit loyalty/voucher tercermin pada
transaksi aktif atau ditolak dengan alasan
yang jelas.
Main Flow
1. Kasir memilih fitur member/voucher.
2. Kasir memasukkan atau memindai identitas member/voucher.
3. Sistem memvalidasi ke Loyalty Service atau cache policy yang tersedia.
4. Sistem menghitung ulang harga, diskon, atau earning point.
5. Kasir menampilkan hasil benefit ke pelanggan.
6. Pelanggan menyetujui hasil penerapan benefit.
Alternate Flow
- Voucher valid tetapi hanya sebagian item yang eligible.
- Member ditemukan tetapi reward redemption memerlukan OTP atau verifikasi tambahan.
Exception Flow
- Voucher kadaluwarsa, sudah dipakai, atau tidak eligible.
- Loyalty service tidak tersedia dan tidak ada fallback offline -> benefit tidak diterapkan.
Business Rules
- Satu voucher biasanya sekali pakai kecuali tipe reusable.
- Perubahan benefit harus memicu recalculation via UC-09.
UC-05 - Proses Pembayaran
Tujuan
Menerima pembayaran pelanggan dan
menghasilkan status payment yang sah untuk
menyelesaikan transaksi.
Primary Actor
Kasir
Supporting Actors
Pelanggan, Payment Gateway / EDC
Preconditions
- Ada transaksi aktif dengan total final.
- Metode pembayaran yang dipilih tersedia di
toko.
Trigger
Kasir menekan proses pembayaran.
Postconditions
- Status payment tersimpan sebagai success,
pending, atau failed sesuai hasil.
- Referensi payment provider tersimpan bila
applicable.
Main Flow
1. Kasir memilih metode pembayaran.

2. Sistem menampilkan total yang harus dibayar.
3. Untuk cash, kasir memasukkan nominal diterima dan sistem menghitung kembalian.
4. Untuk non-cash, sistem mengirim request ke payment gateway/EDC.
5. Payment provider mengembalikan hasil otorisasi.
6. Sistem menandai payment sebagai sukses dan mengikatnya ke transaksi.
7. Sistem mengembalikan kontrol ke alur penjualan untuk finalisasi.
Alternate Flow
- Split payment digunakan dan sistem memproses beberapa instrumen hingga total lunas.
- Pembayaran cashless membutuhkan retry pada terminal EDC.
Exception Flow
- Authorization declined.
- Timeout dari gateway/EDC menyebabkan status tidak pasti -> transaksi ditandai pending
investigation.
- Nominal pembayaran kurang dari total -> sistem menolak finalisasi.
Business Rules
- Finalisasi penjualan hanya boleh saat total outstanding = 0 dan status payment valid.
- Semua pajak/promo harus dihitung sebelum payment dimulai.
UC-07 - Suspend / Resume Transaksi
Tujuan
Menunda transaksi aktif tanpa kehilangan
state dan melanjutkannya kembali di terminal
yang diizinkan.
Primary Actor
Kasir
Supporting Actors
Supervisor (opsional)
Preconditions
- Ada transaksi aktif yang belum dibayar.
- Policy store mengizinkan suspend.
Trigger
Kasir memilih suspend atau membuka daftar
transaksi tertunda untuk resume.
Postconditions
- Transaksi berada pada state suspended atau
resumed dengan konsisten.
Main Flow
1. Kasir memilih suspend transaksi aktif.
2. Sistem memvalidasi transaksi belum final dan belum memiliki payment sukses.
3. Sistem menyimpan snapshot keranjang dan context transaksi.
4. Sistem memberi identifier transaksi suspend.
5. Pada waktu resume, kasir memilih transaksi suspend.
6. Sistem memuat ulang isi keranjang dan menghitung ulang harga bila diperlukan.
Alternate Flow
- Resume dilakukan oleh kasir lain jika policy store mengizinkan handover.

- Harga berubah sejak suspend dan sistem meminta konfirmasi sebelum lanjut.
Exception Flow
- Snapshot transaksi rusak atau sudah kadaluwarsa.
- Transaksi sudah di-resume terminal lain -> sistem menolak duplicate resume.
Business Rules
- Transaksi suspended tidak boleh mengurangi stok final sebelum selesai.
- Masa berlaku suspend dapat dibatasi per policy.
UC-10 - Void Item / Transaksi
Tujuan
Membatalkan item tertentu atau seluruh
transaksi sesuai otorisasi dan jejak audit yang
diwajibkan.
Primary Actor
Kasir
Supporting Actors
Supervisor, Lookup Receipt
Preconditions
- Transaksi aktif atau transaksi yang dapat
diidentifikasi tersedia.
- Kasir memiliki hak void sesuai level
otorisasinya atau dapat meminta approval.
Trigger
Kasir memilih void item atau void transaksi.
Postconditions
- Item/transaksi dibatalkan sesuai kebijakan,
atau permintaan ditolak dengan alasan jelas.
Main Flow
1. Kasir memilih item/transaksi yang akan di-void.
2. Sistem memuat detail transaksi terkait bila diperlukan.
3. Kasir memasukkan alasan exception.
4. Sistem mengecek apakah approval supervisor diperlukan.
5. Bila diperlukan, supervisor memberikan approval.
6. Sistem membatalkan item/transaksi sesuai scope void.
7. Sistem menulis audit log.
Alternate Flow
- Void item dilakukan sebelum payment sehingga tidak perlu receipt lookup.
- Void penuh setelah payment diperlakukan sebagai reversal sesuai policy.
Exception Flow
- Receipt/transaksi tidak ditemukan.
- Approval ditolak.
- Transaksi sudah settled dan tidak boleh di-void, harus lewat return/refund.
Business Rules
- Void selalu membutuhkan reason code.

- Use return/refund, bukan void, bila barang sudah keluar dan transaksi telah selesai secara
hukum/akuntansi.
UC-11 - Return / Refund
Tujuan
Menerima pengembalian barang pelanggan
dan mengembalikan dana atau kredit sesuai
kebijakan toko.
Primary Actor
Kasir / Pelanggan
Supporting Actors
Supervisor, Payment Gateway / EDC
Preconditions
- Barang return tersedia untuk diperiksa.
- Receipt atau bukti transaksi dapat dicari,
kecuali policy no-receipt return diizinkan.
Trigger
Pelanggan meminta return atau refund.
Postconditions
- Transaksi return tercatat.
- Stok barang kembali atau dikirim ke
damaged/inspection bucket sesuai kondisi.
Main Flow
1. Kasir memulai transaksi return.
2. Kasir mencari receipt atau referensi transaksi asli.
## 3. Sistem memvalidasi return policy berdasarkan item, tanggal, kondisi barang, dan metode
pembayaran awal.
4. Kasir memilih item dan kuantitas yang direturn.
5. Kasir mencatat alasan exception/return reason.
6. Jika diperlukan, supervisor memberi approval.
7. Sistem menghitung nilai refund.
8. Kasir mengeksekusi refund ke cash, kartu, atau store credit sesuai policy.
9. Sistem menyimpan transaksi return dan audit log.
Alternate Flow
- Return parsial untuk sebagian item.
- Refund dikonversi menjadi store credit jika metode asli tidak dapat dipulihkan.
Exception Flow
- Receipt tidak ditemukan dan no-receipt return tidak diizinkan.
- Masa return policy telah lewat.
- Refund ke kartu gagal dan perlu fallback sesuai policy.
Business Rules
- Return policy harus tervalidasi sebelum refund.
- Tidak semua item boleh direturn, misalnya clearance/final sale/perishable tertentu.

UC-12 - Price Override / Manual Discount
Tujuan
Mengubah harga jual atau memberi diskon
manual dalam batas policy toko.
Primary Actor
Kasir
Supporting Actors
Supervisor
Preconditions
- Ada item atau transaksi aktif yang eligible
untuk override/discount.
- Policy threshold tersedia.
Trigger
Kasir memilih override harga atau manual
discount.
Postconditions
- Harga override/manual discount tersimpan
pada transaksi aktif atau ditolak.
Main Flow
1. Kasir memilih item atau transaksi target.
2. Kasir memasukkan nilai override/diskon dan alasan exception.
3. Sistem mengecek threshold otorisasi.
4. Supervisor memberikan approval sesuai policy.
5. Sistem menerapkan harga/diskon baru.
6. Sistem menghitung ulang total dan menyimpan audit log.
Alternate Flow
- Diskon masih dalam limit kasir sehingga approval tidak perlu.
- Override berlaku pada item tunggal, bukan seluruh basket.
Exception Flow
- Nilai diskon melewati limit maksimum.
- Approval ditolak.
- Item tidak eligible karena promo exclusive atau price-locked.
Business Rules
- Setiap override/discound manual wajib punya reason code dan audit trail.
- Perhitungan akhir selalu melalui pricing engine.
UC-18 - Cash In / Cash Out
Tujuan
Mencatat pergerakan kas non-penjualan
selama shift dengan alasan yang terkontrol.
Primary Actor
Kasir
Supporting Actors
Supervisor (opsional)
Preconditions
- Shift aktif.
- Jenis cash movement tersedia di master
reason.
Trigger
Kasir perlu menambah atau mengeluarkan
uang dari laci kas untuk keperluan

operasional.
Postconditions
- Pergerakan kas tercatat dan dapat
direkonsiliasi saat tutup shift.
Main Flow
1. Kasir memilih cash in atau cash out.
2. Kasir memilih reason code.
3. Kasir memasukkan nominal dan catatan tambahan bila perlu.
4. Sistem memvalidasi batas nominal.
5. Sistem menyimpan cash movement dan memperbarui expected drawer balance.
6. Sistem menulis audit log.
Alternate Flow
- Supervisor approval diminta untuk nominal besar.
- Dokumen pendukung difoto/diunggah bila toko menerapkan kontrol tambahan.
Exception Flow
- Nominal tidak valid atau reason code tidak diizinkan.
- Shift tidak aktif.
Business Rules
- Semua cash movement harus terhubung ke shift aktif.
- Tidak boleh ada cash out tanpa reason code.
UC-19 - Safe Drop
Tujuan
Memindahkan sebagian uang tunai dari laci
kas ke safe untuk mengurangi risiko cash-on-
hand.
Primary Actor
Supervisor
Supporting Actors
Kasir
Preconditions
- Ada shift aktif dan kas tunai melebihi
ambang aman atau supervisor memutuskan
safe drop.
Trigger
Supervisor menjalankan safe drop.
Postconditions
- Transaksi safe drop tercatat dan saldo kas
laci berkurang.
Main Flow
1. Supervisor memilih shift/terminal yang akan di-safe-drop.
2. Supervisor menghitung nominal uang yang akan dipindahkan.
3. Sistem mencatat nominal, waktu, pelaksana, dan referensi shift.
4. Supervisor mengonfirmasi bahwa uang telah dimasukkan ke safe.
5. Sistem memperbarui expected drawer balance dan audit log.

Alternate Flow
- Kasir menyiapkan uang, supervisor hanya mengotorisasi dan mengonfirmasi.
- Safe drop dilakukan beberapa kali dalam satu shift.
Exception Flow
- Nominal safe drop melebihi expected cash di drawer.
- Safe drop dibatalkan sebelum konfirmasi.
Business Rules
- Safe drop harus dapat diaudit dan terkait ke shift/terminal tertentu.
UC-20 - Tutup Shift
Tujuan
Menutup shift kasir secara resmi dengan
rekonsiliasi kas dan pelaporan dasar.
Primary Actor
Kasir
Supporting Actors
Supervisor
Preconditions
- Shift aktif.
- Semua transaksi pending pada shift telah
ditangani atau di-mark sesuai policy.
Trigger
Kasir memilih tutup shift.
Postconditions
- Shift berubah menjadi closed.
- Data kas shift siap untuk end-of-day dan
store reporting.
Main Flow
1. Kasir memulai proses close shift.
2. Sistem menampilkan expected cash drawer balance.
3. Kasir menghitung kas fisik dan memasukkan hasil hitung.
4. Sistem menjalankan rekonsiliasi kas.
5. Sistem menghasilkan X/Z report sesuai konfigurasi.
6. Kasir mengonfirmasi penutupan shift.
7. Sistem menutup shift dan menulis audit log.
Alternate Flow
- Supervisor membantu verifikasi ketika ada selisih kas.
- Beberapa transaksi pending tetap dibawa ke investigasi setelah shift ditutup.
Exception Flow
- Selisih kas melebihi tolerance dan membutuhkan supervisor sign-off.
- Masih ada transaksi suspend/pending yang belum diresolusikan dan policy menolak close
shift.
Business Rules
- Close shift wajib include cash reconciliation dan report generation.
- Shift closed tidak boleh menerima transaksi baru.

UC-23 - Tutup Hari
Tujuan
Menutup operasi toko untuk satu hari bisnis
dan menghasilkan rekap operasional final.
Primary Actor
Store Manager
Supporting Actors
Supervisor, HQ Store Service
Preconditions
- Semua shift yang wajib ditutup telah selesai
atau ditangani sesuai exception policy.
- Sinkronisasi minimum operasional tersedia.
Trigger
Store Manager menjalankan end of day.
Postconditions
- Hari bisnis ditutup.
- Laporan operasional final tersedia untuk
toko/HQ.
Main Flow
1. Manager membuka fungsi end-of-day.
2. Sistem memverifikasi kesiapan: shift, settlement store-level, dan data wajib.
3. Sistem menghasilkan store operational report final.
4. Sistem menulis audit log end-of-day.
5. Sistem menandai business day selesai.
Alternate Flow
- Manager menunda penutupan hari hingga outstanding issue terselesaikan.
- Store tetap end-of-day dalam mode offline dan sinkronisasi dilakukan kemudian.
Exception Flow
- Masih ada shift aktif yang belum ditutup.
- Data kritikal belum lengkap sehingga hari tidak boleh ditutup.
Business Rules
- Satu toko hanya boleh memiliki satu business day aktif pada satu waktu.
UC-24 - Cek Stok & Movement
Tujuan
Melihat saldo stok dan histori movement
untuk pengambilan keputusan operasional
toko.
Primary Actor
Staff Inventori
Supporting Actors
HQ Store Service (opsional)
Preconditions
- User inventori terautentikasi.
- Data stok lokal atau hasil sinkronisasi
tersedia.
Trigger
Staff inventori mencari stok barang.
Postconditions
- Informasi stok dan movement berhasil
ditampilkan.

Main Flow
1. Staff inventori mencari item berdasarkan SKU/barcode/nama.
2. Sistem menampilkan on-hand, available, reserved, dan lokasi bila tersedia.
3. Sistem menampilkan histori movement utama: sales, return, receiving, transfer, adjustment.
4. User menganalisis hasil untuk tindakan lanjut.
Alternate Flow
- Pencarian dilakukan per rak/lokasi.
- Data movement diambil dari cache lokal saat offline.
Exception Flow
- Item tidak ditemukan.
- Data movement belum sinkron penuh.
Business Rules
- Movement harus memiliki timestamp dan source transaction yang bisa ditelusuri.
UC-25 - Receiving Barang
Tujuan
Menerima barang yang datang ke toko dan
memperbarui stok penerimaan secara akurat.
Primary Actor
Staff Inventori
Supporting Actors
HQ Store Service, Device
Preconditions
- Ada delivery note / PO / transfer in yang
valid.
- Barang fisik telah tiba di toko.
Trigger
Staff inventori memulai proses receiving.
Postconditions
- Stok receiving bertambah.
- Discrepancy tercatat bila ada.
Main Flow
1. Staff inventori memilih referensi delivery/PO.
2. Sistem memverifikasi data expected items.
3. Staff memindai atau menghitung item yang diterima.
4. Sistem membandingkan actual vs expected.
5. Jika ada selisih, staff mencatat receiving gap.
6. Staff mengonfirmasi receiving.
7. Sistem memperbarui stok dan menulis audit log.
Alternate Flow
- Receiving parsial dilakukan untuk pengiriman yang belum lengkap.
- Receiving dilakukan per carton/box sebelum per-item detail.
Exception Flow
- Referensi delivery tidak valid.

- Barang lebih/kurang dari expected tanpa penjelasan yang dapat diterima.
- Koneksi HQ tidak tersedia dan data referensi belum ada di lokal.
Business Rules
- Receiving harus include verifikasi delivery/PO.
- Selisih receiving harus terdokumentasi, tidak boleh hilang diam-diam.
UC-26 - Transfer Stok
Tujuan
Memindahkan stok antar lokasi/store dengan
jejak transaksi yang jelas.
Primary Actor
Staff Inventori
Supporting Actors
HQ Store Service
Preconditions
- Item dan lokasi sumber/tujuan valid.
- Stock tersedia di lokasi sumber.
Trigger
Staff inventori membuat atau memproses
transfer stok.
Postconditions
- Dokumen transfer tercatat dan
stok/commitment berubah sesuai status
transfer.
Main Flow
1. Staff memilih item dan kuantitas transfer.
2. Sistem memvalidasi stok sumber.
3. Staff memilih lokasi/store tujuan.
4. Sistem membuat dokumen transfer dan mengurangi stok sumber sesuai status proses.
5. Sistem menulis audit log dan status transfer.
Alternate Flow
- Transfer hanya request dan menunggu approval/proses toko tujuan.
- Transfer antar backroom-rak dalam satu toko disederhanakan sebagai internal transfer.
Exception Flow
- Stok sumber tidak cukup.
- Lokasi tujuan tidak valid.
- Transfer dibatalkan sebelum dikirim.
Business Rules
- Tidak boleh memindahkan stok melebihi available stock.
UC-27 - Replenishment Rak
Tujuan
Memindahkan stok dari backroom ke rak
display agar ketersediaan barang untuk
penjualan terjaga.
Primary Actor
Staff Inventori

Supporting Actors
Device
Preconditions
- Ada kebutuhan replenishment.
- Stok tersedia di backroom atau lokasi
sumber.
Trigger
Staff inventori menjalankan replenishment.
Postconditions
- Rak terisi ulang dan movement internal
tercatat.
Main Flow
1. Staff melihat daftar item yang perlu direplenish.
2. Staff mengambil barang dari lokasi sumber.
3. Staff memindahkan dan menaruh barang ke rak.
4. Sistem atau staff mengonfirmasi kuantitas yang dipindah.
5. Sistem memperbarui lokasi stok internal.
Alternate Flow
- Replenishment dipicu berdasarkan min shelf quantity.
- Sebagian item tidak tersedia sehingga dipindahkan parsial.
Exception Flow
- Stok sumber ternyata tidak ada saat diambil.
- Lokasi rak tidak valid atau penuh.
Business Rules
- Replenishment tidak mengubah total on-hand toko, hanya lokasi internalnya.
UC-28 - Stock Adjustment
Tujuan
Mengoreksi saldo stok karena kehilangan,
kerusakan, mismatch sistem, atau sebab
operasional lain.
Primary Actor
Staff Inventori / Supervisor
Supporting Actors
Supervisor Approval
Preconditions
- Item target dapat diidentifikasi.
- Alasan adjustment tersedia.
Trigger
Staff inventori mengajukan adjustment stok.
Postconditions
- Saldo stok berubah secara sah dan dapat
diaudit.
Main Flow
1. User memilih item dan kuantitas adjustment.
2. User menentukan arah adjustment dan alasan.
3. Sistem mengecek threshold dan kebutuhan approval.
4. Jika perlu, supervisor memberikan approval.
5. Sistem memperbarui stok serta ledger adjustment.

6. Sistem menulis audit log.
Alternate Flow
- Adjustment kecil dalam tolerance dapat langsung diproses.
- Adjustment massal dilakukan dari hasil stock count.
Exception Flow
- Approval ditolak.
- Adjustment menyebabkan stok negatif yang tidak diizinkan.
- Item diblokir untuk adjustment.
Business Rules
- Adjustment harus reason-based dan traceable.
- Adjustment besar wajib approval.
UC-29 - Stock Opname / Cycle Count
Tujuan
Menghitung stok fisik dan
membandingkannya dengan saldo sistem
untuk menjaga akurasi inventori.
Primary Actor
Staff Inventori / Supervisor
Supporting Actors
Device
Preconditions
- Periode count atau assignment count
tersedia.
- Area/item count telah ditetapkan.
Trigger
Staff inventori memulai cycle count atau stock
opname.
Postconditions
- Hasil count tersimpan dan discrepancy
teridentifikasi.
Main Flow
1. User memilih batch/area count.
2. User menghitung item fisik dan memasukkan hasil atau memindai item.
3. Sistem membandingkan hasil count dengan stok sistem.
4. Sistem menandai selisih.
5. User mereview dan mengonfirmasi hasil count.
6. Jika policy mengizinkan, selisih diturunkan menjadi stock adjustment.
7. Sistem menulis audit log.
Alternate Flow
- Double count dijalankan untuk item bernilai tinggi.
- Supervisor mereview item dengan mismatch besar.
Exception Flow
- Batch count dibatalkan.
- Data count tidak lengkap.

- Adjustment otomatis ditolak karena butuh approval.
Business Rules
- Cycle count harus punya snapshot waktu yang jelas agar selisih dapat dijelaskan.
UC-30 - Kelola Barang Rusak / Expired
Tujuan
Mengidentifikasi dan memproses barang
rusak atau kedaluwarsa agar tidak dijual ke
pelanggan.
Primary Actor
Staff Inventori
Supporting Actors
Supervisor (opsional)
Preconditions
- Barang teridentifikasi rusak/expired atau
mendekati expired.
- Kategori disposal tersedia.
Trigger
Staff inventori memproses barang
rusak/expired.
Postconditions
- Barang tidak lagi tersedia untuk penjualan
normal.
- Status dan lokasi stok tercatat sesuai
tindakan.
Main Flow
1. User memilih item dan kuantitas.
2. User mencatat kategori: damaged, expired, quarantine, disposal, atau return-to-vendor.
3. Sistem memindahkan stok ke bucket status yang sesuai.
4. Jika perlu, supervisor mereview dan menyetujui tindakan.
5. Sistem menyimpan transaksi dan audit trail.
Alternate Flow
- Barang ditandai quarantine untuk inspeksi lebih lanjut.
- Barang dikembalikan ke vendor mengikuti proses RTV.
Exception Flow
- Item tidak eligible untuk disposal tanpa verifikasi tambahan.
- Kuantitas melebihi stok tersedia.
Business Rules
- Barang expired tidak boleh tetap berada pada sellable stock.
UC-33 - Cetak Label Harga / Rak
Tujuan
Mencetak label harga atau label rak yang
akurat untuk mendukung display toko.
Primary Actor
Staff Inventori
Supporting Actors
Store Device

Preconditions
- Template label tersedia.
- Data harga/SKU valid.
Trigger
Staff inventori memilih cetak label.
Postconditions
- Label tercetak atau job gagal dengan error
yang bisa ditindaklanjuti.
Main Flow
1. User memilih item atau rak target.
2. Sistem mengambil data harga, nama, barcode, dan atribut label.
3. User memilih jumlah label.
4. Sistem mengirim job ke printer label.
5. Printer mencetak label.
Alternate Flow
- Cetak label massal dari hasil price change atau replenishment.
- Preview label ditampilkan sebelum cetak.
Exception Flow
- Printer tidak tersedia.
- Data harga belum sinkron.
- Template label tidak cocok dengan device.
Business Rules
- Label harus menggunakan harga aktif yang berlaku untuk toko terkait.
UC-34 - Lihat Laporan Operasional Toko
Tujuan
Melihat KPI dan laporan operasional toko
untuk monitoring performa dan kontrol
harian.
Primary Actor
Store Manager
Supporting Actors
HQ Store Service
Preconditions
- Manager terautentikasi.
- Data laporan tersedia untuk periode yang
diminta.
Trigger
Manager membuka menu laporan
operasional.
Postconditions
- Laporan tampil untuk keperluan
operasional/pengambilan keputusan.
Main Flow
1. Manager memilih periode/laporan.
2. Sistem mengambil data penjualan, return, kas, inventori, dan sinkronisasi yang relevan.
3. Sistem menampilkan laporan operasional toko.
4. Manager melakukan analisis atau ekspor bila tersedia.

Alternate Flow
- Laporan ditampilkan dari cache lokal terakhir saat offline.
- Manager memfilter per shift atau per kategori.
Exception Flow
- Data laporan belum lengkap.
- Akses role tidak cukup untuk laporan tertentu.
Business Rules
- Definisi metrik harus konsisten dengan data transaksi dan business day.
UC-35 - Sinkronisasi Data Toko
Tujuan
Menyamakan data transaksi, stok, master
data, dan status operasional toko dengan HQ
services.
Primary Actor
Store Manager / System
Supporting Actors
HQ Store Service
Preconditions
- Koneksi ke HQ tersedia atau job sync dapat
diantrikan.
- Identitas toko valid.
Trigger
Manager menjalankan sinkronisasi manual
atau sistem menjadwalkan sync.
Postconditions
- Data sukses tersinkron atau item gagal
masuk daftar reconcile.
Main Flow
1. Sistem menyiapkan batch data yang perlu dikirim/diambil.
2. Sistem mengirim transaksi/store events ke HQ.
3. Sistem mengambil update master data dan status yang diperlukan.
4. Sistem memproses hasil sinkronisasi dan menandai item sukses.
5. Bila ada kegagalan parsial, sistem membuat daftar rekonsiliasi.
6. Status sinkronisasi ditampilkan ke user.
Alternate Flow
- Sync hanya data tertentu, misalnya master price atau transaksi penjualan.
- Retry otomatis dijalankan untuk batch gagal.
Exception Flow
- Koneksi HQ gagal.
- Conflict data terjadi antara local dan HQ.
- Sebagian batch gagal dan perlu reconciliation.
Business Rules
- Sync harus idempotent sebisa mungkin.
- Kegagalan sinkron tidak boleh silently drop data.

UC-36 - Mode Operasi Offline
Tujuan
Menjaga toko tetap dapat beroperasi ketika
koneksi ke service pusat atau provider
tertentu terganggu.
Primary Actor
Store Staff
Supporting Actors
Store Manager, HQ Store Service, Payment
Gateway / EDC
Preconditions
- Gangguan konektivitas terdeteksi atau
external dependency tidak tersedia.
- Store policy mengizinkan operasi offline
terbatas.
Trigger
Sistem mendeteksi gangguan atau user
mengaktifkan mode offline sesuai policy.
Postconditions
- Store beroperasi dalam degraded mode
dengan backlog sinkronisasi yang terkontrol.
Main Flow
1. Sistem mengubah status operasional menjadi offline mode.
2. Sistem mengaktifkan aturan fallback untuk data lokal/cached.
3. User tetap menjalankan operasi yang diizinkan, seperti penjualan dengan batasan tertentu.
4. Sistem menandai transaksi dan perubahan data sebagai pending sync.
5. Saat koneksi pulih, sistem menjalankan sinkronisasi dan reconciliation.
Alternate Flow
- Hanya fitur tertentu yang offline, misalnya loyalty nonaktif tetapi cash sales tetap berjalan.
- Manager menonaktifkan sementara fitur yang terlalu berisiko saat offline.
Exception Flow
- Data cache tidak memadai sehingga operasi tertentu harus diblokir.
- Clock/time drift membuat sinkronisasi berisiko conflict.
Business Rules
- Offline mode bukan bypass total; hanya fungsi yang aman secara bisnis yang boleh tetap jalan.
- Semua transaksi offline harus dapat direconcile saat koneksi kembali.

## 7. Supporting Use Case Specifications
Catatan: Section ini memuat supporting/internal use cases yang muncul pada relasi
include/extend atau reusable operational step. Detailnya dibuat lebih ringkas, tetapi tetap cukup
untuk design review dan turunan test.
UC-03 - Kelola Keranjang
Tujuan
Memelihara isi transaksi penjualan aktif
sebelum finalisasi pembayaran.
Primary Actor
Kasir
Supporting Actors
Store Device
Preconditions
- Ada transaksi penjualan aktif.
Trigger
Kasir menambah, mengubah, atau
menghapus item dari keranjang.
Postconditions
- Keranjang aktif ter-update.
Main Flow
1. Kasir scan/cari item.
2. Sistem menambahkan item ke keranjang.
3. Kasir mengubah kuantitas atau menghapus item bila perlu.
4. Sistem menghitung ulang harga.
Alternate Flow
- Kasir menahan item tertentu untuk konfirmasi harga.
Exception Flow
- Item tidak dapat dijual atau stok nol.
Business Rules
- Semua perubahan keranjang harus memicu pricing ulang.
UC-06 - Terbitkan Receipt
Tujuan
Menyediakan bukti transaksi kepada
pelanggan setelah transaksi sah selesai.
Primary Actor
Kasir
Supporting Actors
Store Device
Preconditions
- Transaksi final dengan payment sah
tersedia.
Trigger
Sistem menyelesaikan transaksi penjualan
atau return/refund.
Postconditions
- Receipt tersedia atau kegagalan cetak
tercatat.
Main Flow
1. Sistem membentuk data receipt.

2. Sistem memilih channel receipt.
3. Receipt dicetak atau dikirim digital.
4. Sistem menyimpan referensi receipt.
Alternate Flow
- Reprint dilakukan kemudian melalui receipt lookup.
Exception Flow
- Printer gagal atau kehabisan kertas.
Business Rules
- Receipt hanya untuk transaksi final.
UC-08 - Cari / Scan Produk
Tujuan
Mengidentifikasi produk yang akan
dimasukkan ke transaksi atau proses
inventori.
Primary Actor
Kasir / Staff Inventori
Supporting Actors
Store Device
Preconditions
- Perangkat scan atau data pencarian tersedia.
Trigger
User memindai barcode atau mengetik kata
kunci SKU/nama.
Postconditions
- Produk teridentifikasi.
Main Flow
1. User scan barcode atau masukkan keyword.
2. Sistem mencari produk.
3. Sistem menampilkan hasil yang cocok.
4. User memilih item target.
Alternate Flow
- Barcode tidak terbaca lalu fallback ke pencarian manual.
Exception Flow
- Produk tidak ditemukan atau master data belum tersedia.
Business Rules
- Pencarian harus mendukung SKU, barcode, dan nama pendek minimal.
UC-09 - Hitung Harga, Promo, Pajak
Tujuan
Menghasilkan total transaksi yang benar
berdasarkan item, promo, voucher, dan
aturan pajak.
Primary Actor
System
Supporting Actors
Loyalty Service, HQ rule cache

Preconditions
- Keranjang transaksi tersedia.
Trigger
Ada perubahan item, member/voucher, atau
metode pembayaran yang memengaruhi total.
Postconditions
- Nilai transaksi terbaru tersedia.
Main Flow
1. Sistem membaca item basket.
2. Sistem menerapkan promo dan pajak yang relevan.
3. Sistem menghitung subtotal, diskon, pajak, dan grand total.
4. Sistem mengembalikan hasil ke caller.
Alternate Flow
- Rule berasal dari cache lokal saat offline.
Exception Flow
- Rule conflict atau data harga hilang.
Business Rules
- Pricing harus deterministic untuk input yang sama pada timestamp bisnis yang sama.
UC-13 - Lookup Receipt
Tujuan
Menemukan transaksi/receipt sebelumnya
sebagai referensi exception atau customer
service.
Primary Actor
Kasir
Supporting Actors
Store Manager
Preconditions
- Kunci pencarian tersedia.
Trigger
Kasir perlu mencari transaksi lama.
Postconditions
- Receipt target ditemukan atau tidak.
Main Flow
1. Kasir memasukkan nomor receipt, kartu, tanggal, atau atribut lain.
2. Sistem mencari transaksi yang cocok.
3. Sistem menampilkan hasil dan detail receipt.
Alternate Flow
- Pencarian menggunakan data lokal terbatas saat offline.
Exception Flow
- Receipt tidak ditemukan.
Business Rules
- Hak akses data receipt harus mengikuti role.

UC-14 - Validasi Return Policy
Tujuan
Memastikan permintaan return memenuhi
kebijakan toko dan regulasi yang berlaku.
Primary Actor
System
Supporting Actors
Supervisor
Preconditions
- Item return dan referensi transaksi tersedia
atau informasi minimum tersedia.
Trigger
Proses return dimulai atau item return
dipilih.
Postconditions
- Keputusan eligibility return tersedia.
Main Flow
1. Sistem mengecek tanggal pembelian, kategori item, kondisi, dan metode bayar.
2. Sistem mengecek aturan no-return/final sale/perishable.
3. Sistem menentukan eligible/ineligible beserta syaratnya.
Alternate Flow
- Policy override tersedia dengan approval supervisor.
Exception Flow
- Data pembelian tidak cukup untuk validasi.
Business Rules
- Policy return harus konsisten antar kanal yang relevan, kecuali memang store-specific.
UC-15 - Supervisor Approval
Tujuan
Memberikan keputusan otorisasi untuk
operasi sensitif yang melewati kewenangan
staf biasa.
Primary Actor
Supervisor
Supporting Actors
Kasir / Staff Inventori
Preconditions
- Ada permintaan approval aktif.
- Supervisor memiliki kredensial valid.
Trigger
Sistem meminta approval.
Postconditions
- Keputusan approval tercatat.
Main Flow
1. Sistem menampilkan detail permintaan approval.
2. Supervisor meninjau konteks, alasan, dan dampak.
3. Supervisor menyetujui atau menolak.
4. Sistem mencatat keputusan.
Alternate Flow
- Supervisor login langsung di terminal requester.

Exception Flow
- Supervisor gagal autentikasi.
Business Rules
- Approval harus mengandung siapa, kapan, apa yang diizinkan, dan untuk alasan apa.
UC-16 - Catat Alasan Exception
Tujuan
Merekam reason code dan catatan
operasional untuk tindakan exception.
Primary Actor
Kasir / Supervisor / Staff Inventori
Supporting Actors
System
Preconditions
- Ada proses exception yang sedang berjalan.
Trigger
User menjalankan void, return, override,
receiving gap, adjustment, atau exception
lain.
Postconditions
- Alasan exception tersimpan.
Main Flow
1. Sistem meminta reason code.
2. User memilih reason code dan menambah catatan bila perlu.
3. Sistem memvalidasi reason dan menyimpannya.
Alternate Flow
- Foto/dokumen pendukung ditambahkan.
Exception Flow
- Reason code wajib tetapi belum diisi.
Business Rules
- Alasan exception tidak boleh null untuk proses yang mewajibkannya.
UC-17 - Input Kas Awal
Tujuan
Mencatat saldo awal laci kas pada awal shift.
Primary Actor
Kasir
Supporting Actors
Supervisor
Preconditions
- Shift akan dibuka.
Trigger
Bagian opening cash pada mulai shift dimulai.
Postconditions
- Opening cash tercatat.
Main Flow
1. Kasir menghitung kas awal.
2. Kasir memasukkan nominal.
3. Sistem menyimpan opening cash.

Alternate Flow
- Supervisor memverifikasi nominal.
Exception Flow
- Nominal tidak valid.
Business Rules
- Opening cash bagian wajib dari start shift.
UC-21 - Rekonsiliasi Kas
Tujuan
Membandingkan kas fisik dan kas sistem
untuk menemukan selisih shift.
Primary Actor
Kasir / Supervisor
Supporting Actors
System
Preconditions
- Shift aktif yang akan ditutup memiliki
expected cash balance.
Trigger
Close shift atau cash audit dilakukan.
Postconditions
- Hasil rekonsiliasi tersimpan.
Main Flow
1. User memasukkan hasil hitung fisik.
2. Sistem membandingkan dengan expected balance.
3. Sistem menampilkan selisih.
4. User mengonfirmasi hasil.
Alternate Flow
- Supervisor menandatangani selisih di atas tolerance.
Exception Flow
- Perhitungan ulang diperlukan karena input salah.
Business Rules
- Selisih harus dapat ditelusuri ke cash movement dan transaksi.
UC-22 - Generate X / Z Report
Tujuan
Menghasilkan ringkasan transaksi dan kas
untuk kontrol shift atau harian.
Primary Actor
Supervisor
Supporting Actors
Store Device
Preconditions
- Data transaksi dan kas tersedia untuk
periode/report type.
Trigger
User meminta X report atau Z report.
Postconditions
- Report tersedia.

Main Flow
1. User memilih jenis report.
2. Sistem mengumpulkan data relevan.
3. Sistem menghasilkan report.
4. Report ditampilkan atau dicetak.
Alternate Flow
- Report disimpan digital.
Exception Flow
- Data report belum lengkap.
Business Rules
- Z report umumnya final/closing; X report interim/control.
UC-31 - Verifikasi Delivery / PO
Tujuan
Memastikan barang yang diterima mengacu
ke dokumen supply yang sah.
Primary Actor
Staff Inventori
Supporting Actors
HQ Store Service
Preconditions
- Nomor delivery/PO tersedia.
Trigger
Receiving dimulai.
Postconditions
- Dokumen receiving tervalidasi.
Main Flow
1. User memasukkan referensi delivery/PO.
2. Sistem mengambil expected items.
3. Sistem memvalidasi dokumen aktif dan tujuan toko.
Alternate Flow
- Data diambil dari cache lokal.
Exception Flow
- Dokumen tidak valid atau bukan milik toko.
Business Rules
- Receiving tanpa referensi hanya boleh jika policy exception mengizinkan.
UC-32 - Catat Selisih Receiving
Tujuan
Mencatat gap antara quantity expected dan
quantity actual saat receiving.
Primary Actor
Staff Inventori
Supporting Actors
Supervisor
Preconditions
- Receiving sedang berlangsung dan
ditemukan selisih.

Trigger
Sistem mendeteksi mismatch expected vs
actual.
Postconditions
- Discrepancy receiving tersimpan.
Main Flow
1. User memilih item mismatch.
2. User mencatat jenis gap: short, over, damaged, wrong item.
3. Sistem menyimpan discrepancy record.
Alternate Flow
- Supervisor diminta untuk selisih besar.
Exception Flow
- User mencoba menutup receiving tanpa menjelaskan gap.
Business Rules
- Semua mismatch receiving wajib tercatat.
UC-37 - Rekonsiliasi Sync Gagal
Tujuan
Menyelesaikan batch sinkronisasi yang gagal
agar data toko dan HQ kembali konsisten.
Primary Actor
Store Manager / Supervisor
Supporting Actors
HQ Store Service
Preconditions
- Ada batch sync berstatus gagal atau parsial.
Trigger
Manager membuka daftar sync gagal atau
sistem memberi alert.
Postconditions
- Item sync gagal berkurang atau tercatat
untuk investigasi.
Main Flow
1. User meninjau item sync gagal.
2. Sistem menampilkan penyebab dan payload ringkas.
3. User memilih retry, resolve conflict, atau mark for manual investigation.
4. Sistem memperbarui status rekonsiliasi.
Alternate Flow
- Retry otomatis berhasil tanpa intervensi manual.
Exception Flow
- Conflict tidak dapat di-resolve otomatis.
Business Rules
- Tidak boleh ada sync failure yang hilang tanpa status akhir.

UC-38 - Autentikasi & Validasi Role
Tujuan
Memastikan hanya user dengan identitas dan
role yang sah yang dapat menjalankan fungsi
sistem.
Primary Actor
Identity Service / Store Staff
Supporting Actors
System
Preconditions
- Kredensial atau token tersedia.
Trigger
User login atau mengakses fungsi berizin.
Postconditions
- Sesi user valid atau akses ditolak.
Main Flow
1. Sistem menerima kredensial.
2. Sistem memverifikasi identitas.
3. Sistem memuat role dan permission.
4. Sistem memberikan keputusan allow/deny.
Alternate Flow
- Offline credential cache dipakai sesuai policy.
Exception Flow
- Identity service tidak tersedia dan fallback tidak diizinkan.
Business Rules
- Authorization harus berbasis role yang sesuai fungsi.
UC-39 - Catat Audit Log
Tujuan
Menyimpan jejak audit atas tindakan penting
yang relevan untuk kontrol dan investigasi.
Primary Actor
System
Supporting Actors
All business use cases
Preconditions
- Ada aksi bisnis signifikan atau event
keamanan.
Trigger
Use case yang mewajibkan audit selesai atau
berubah state penting.
Postconditions
- Audit trail tersimpan.
Main Flow
1. Sistem membentuk record audit.
2. Sistem menyimpan timestamp, actor, action, target object, before/after context bila perlu.
3. Sistem memastikan audit record immutable atau terlindungi.
Alternate Flow
- Audit dikirim async ke central store setelah disimpan lokal.

Exception Flow
- Storage audit sementara tidak tersedia -> event diantrikan secara aman.
Business Rules
- Aksi sensitif tidak boleh lolos tanpa audit record.

## 8. Traceability Ringkas
- UC-02 Proses Penjualan menurunkan activity diagram checkout, sequence diagram sales
checkout, domain model Sale/Cart/Payment/Receipt, dan test untuk success/declined/pending
payment.
- UC-11 Return / Refund menurunkan sequence refund authorization, domain
ReturnTransaction/ReturnPolicy/RefundPayment, dan test policy eligibility.
- UC-20 Tutup Shift dan UC-23 Tutup Hari menurunkan cash reconciliation, reporting, dan
operational closing controls.
- UC-25 sampai UC-30 menurunkan domain stock ledger, stock movement, receiving
discrepancy, adjustment reason, dan cycle count result.
- UC-35 sampai UC-37 menurunkan integration architecture untuk sync queue, retry, conflict
handling, serta offline backlog.
## 9. Kritik Model dan Rekomendasi Perbaikan
- Diagram saat ini cukup rapi secara struktur paket, tetapi ada campuran antara user-goal use
case dan internal service use case. Itu tidak salah total, tetapi untuk review bisnis lebih bersih
jika UC_Auth dan UC_AuditLog dipindahkan sebagai internal behavior note atau supporting
service catalogue.
- UC_OfflineMode sebagai use case bisa diterima bila dimaknai sebagai business-operational
capability. Namun secara implementasi, ia lebih dekat ke system state/cross-cutting capability
dibanding goal actor tunggal. Pada level artefak berikutnya, lebih baik dimodelkan juga pada
architecture/state/sequence, bukan hanya use case.
- UC_Receipt menggunakan <<extend>> dari UC_Sales. Itu masuk akal bila receipt benar-benar
conditional. Jika receipt selalu wajib setelah penjualan sukses, relasi yang lebih tepat secara
semantik adalah <<include>> atau cukup dianggap bagian dari basic flow UC_Sales.
- UC_Override belum dihubungkan langsung ke actor pada diagram. Secara operasional,
seharusnya Kasir (dan kadang Supervisor) memang terasosiasi langsung agar scope user-goal
tidak ambigu.


## Constraints / Policies
Tidak memasukkan detail teknis internal sistem ke level use case; mempertahankan sistem boundary dan aktor eksternal dengan jelas.

## Technical Notes
UC ID dan istilah aktor harus dipertahankan stabil karena menjadi anchor traceability ke activity, sequence, domain, database, dan test.

## Dependencies / Related Documents
- `uml_modeling_source_of_truth.md`
- `store_pos_activity_detail_specifications.md`
- `store_pos_sequence_detail_specifications.md`
- `store_pos_domain_model_detail_specifications_v2.md`
- `traceability_matrix_store_pos.md`
- `store_pos_test_specification.md`

## Risks / Gaps / Ambiguities
- Tidak ditemukan gap fatal saat ekstraksi. Tetap review ulang bagian tabel/angka jika dokumen ini akan dijadikan baseline implementasi final.

## Reviewer Notes
- Struktur telah dinormalisasi ke Markdown yang konsisten dan siap dipakai untuk design review / engineering handoff.
- Istilah utama dipertahankan sedekat mungkin dengan dokumen sumber untuk menjaga traceability lintas artefak.
- Bila ada konflik antar dokumen, prioritaskan source of truth, artefak yang paling preskriptif, dan baseline yang paling implementable.

## Source Mapping
- Original source: `store_pos_use_case_detail_specifications.pdf` (PDF, 33 pages)
- Output markdown: `store_pos_use_case_detail_specifications.md`
- Conversion approach: text extraction -> normalization -> structural cleanup -> standardized documentation wrapper.
- Note: beberapa tabel/list di PDF dapat mengalami wrapping antar baris; esensi dipertahankan, tetapi layout tabel asli tidak dipertahankan 1:1.
