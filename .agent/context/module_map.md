# Module Map

## Apps
- `apps/desktop-pos`: Shell utama desktop Compose Multiplatform (JVM). Prioritas utama rilis.
- `apps/android-pos`: Parity lane Android.

## Shared (KMP)
- `shared/kernel`: Inti operasional (Auth, Shift, Approval, Audit, DB Schema, Outbox).
- `shared/masterdata`: Katalog produk, kategori, harga, dan barcode.
- `shared/sales`: Transaksi penjualan, keranjang, pembayaran, dan struk.
- `shared/inventory`: Saldo stok dan ledger mutasi.
- `shared/cash`: Manajemen kas masuk/keluar di level terminal.
- `shared/sync`: Orkestrasi sinkronisasi ke HQ backend.

## Tooling & Scripts
- `tooling/scripts/`: Script PowerShell untuk audit, release evidence, dan performance probe.
- `tooling/sqlite-worker-init`: Inisialisasi runtime SQLite worker.
