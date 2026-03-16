# Cassy

Desktop-first retail operating core untuk single outlet. Fokus V1 saat ini adalah cashier foundation yang tangguh: access gate, business day, shift, catalog, cart (basket), dan pricing baseline.

## Repo Truth & Milestone Status (2026-03-20)

Status milestone di bawah ini didasarkan pada bukti nyata di dalam repository (code, unit tests, dan manual smoke evidence).

- **M0 (Setup):** **DONE** (Control plane & Agent context stabil)
- **M1 (Scope):** **DONE** (V1 functional scope terkunci)
- **M2 (Arch):** **DONE** (Kotlin 2.3.20, Multi-module Gradle, Build Logic stabil)
- **M3 (Bootstrap):** **DONE** (Desktop branding, Login, & State restore terverifikasi)
- **M4 (Ops):** **DONE** (Business Day & Shift lifecycle guardrails terverifikasi)
- **M5 (Catalog/Cart):** **DONE (Thin)** (Lookup barcode/SKU & Basket persistence stabil)
- **M7 (Inventory):** **DONE (Thin)** (Integrasi mutasi stok otomatis saat checkout baseline)

**Milestone Berikutnya:** **M6 (Checkout & Payment)** - Menutup gap transaksi hingga finalisasi pembayaran dan cetak struk (Lunas).

## Verifikasi & Evidence Lane

Dokumentasi detail mengenai status dan cara verifikasi:
- `docs/execution/roadmap_bridge.md`: **Source of Truth** status milestone saat ini.
- `docs/execution/windows_installer_smoke_checklist.md`: Panduan verifikasi manual installer Windows.
- `docs/execution/windows_desktop_runbook.md`: Langkah operasional untuk environment Desktop.

## Struktur Modul Utama

- `apps/desktop-pos`: Shell utama kasir (Windows/Desktop-first).
- `shared:kernel`: Inti operasional (Access, Business Day, Shift).
- `shared:masterdata`: Manajemen katalog dan pencarian produk (Barcode/SKU).
- `shared:sales`: Logika keranjang belanja (Basket) dan perhitungan harga baseline.
- `shared:inventory`: Pengelolaan stok dan ledger transaksi inventaris.

## Quick Start Verification

Jalankan perintah berikut untuk memverifikasi kesehatan repository secara lokal:

```powershell
# Jalankan smoke test UI Desktop (Otomatis exit setelah load)
.\gradlew :apps:desktop-pos:smokeRun

# Jalankan semua Unit Test (Shared & Desktop)
.\gradlew test

# Build installer Windows (EXE) secara lokal
.\gradlew :apps:desktop-pos:packageExe
```

## Catatan Penting
- **JDK 17** adalah standar wajib untuk pengembangan Desktop.
- **Active Basket Persistence:** M5 kini mendukung penyimpanan keranjang otomatis; jika aplikasi ditutup paksa, isi keranjang akan kembali saat dibuka (Survival on Restart).
- **Checkout Baseline:** Checkout saat ini sudah mencatat transaksi ke database lokal dan memotong stok di `shared:inventory`, namun finalisasi pembayaran eksternal masih dalam pengembangan (M6).

---
*Lihat `.agent/plan.md` untuk rencana eksekusi teknis mendalam.*
