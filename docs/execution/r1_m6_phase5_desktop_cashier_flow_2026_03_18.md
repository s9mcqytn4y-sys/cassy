# R1 / M6 Phase 5 Desktop Cashier Flow

Dokumen ini merangkum perubahan PHASE 5 yang benar-benar hidup di repo.

## Scope yang ditutup

- desktop checkout action memakai kontrak final R1 yang sama dengan `shared:sales`
- preview struk memakai snapshot final yang persisted
- print dan reprint punya status yang terlihat di UI
- print failure terlihat jelas dan tidak membatalkan sale final
- readback/history tetap dari source final yang sama
- finalization bundle durable dipakai untuk replay setelah crash di sela inventory/kernel

## Perilaku desktop yang sekarang terbukti

### Checkout tunai

- kasir memasukkan barang lewat pencarian atau barcode/SKU
- scan produk yang sama tetap menggabungkan qty di cart, bukan membuat baris liar baru
- kasir memasukkan uang diterima pelanggan
- quote kembalian dihitung dari total basket yang berasal dari `SalesService`, bukan dari hitungan UI terpisah
- checkout tunai ditolak bila uang pelanggan masih kurang

### Preview dan print

- setelah finalisasi sukses, desktop menampilkan preview struk dari snapshot final
- tombol `Cetak Struk` memakai source final yang sama
- tombol `Print Ulang Struk` selalu membaca snapshot final yang persisted
- bila printer gagal, status print tetap terlihat dan sale final tetap sah

### Batal draft

- tombol batal hanya membatalkan pesanan draft/pending
- sale yang sudah final tidak disentuh; untuk itu tetap butuh flow retur/refund yang memang di luar scope R1

## Atomicity yang benar-benar dibuktikan

- `shared:sales` kini menyimpan `FinalizationBundle` durable sebelum sale dianggap final
- jika crash terjadi setelah inventory atau setelah kernel intent, replay dapat melanjutkan finalisasi tanpa menggandakan efek bisnis
- karena itu claim yang benar:
  - operator tidak akan melihat sale `COMPLETED` sebelum bundle selesai
  - replay pasca-crash dapat menyelesaikan bundle tanpa duplikasi inventory/audit/outbox
- claim yang sengaja tidak dibuat:
  - satu transaksi ACID fisik lintas database `sales`, `inventory`, dan `kernel`

## Hardware tanpa device fisik

- desktop tetap memakai `CashierHardwarePort`
- default runtime: status hardware jujur `UNKNOWN`
- test memakai fake adapter untuk printer/drawer/scanner
- ini adalah baseline best practice tanpa mengarang readiness perangkat fisik
