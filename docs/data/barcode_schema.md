# Barcode Schema & Scanner Policy [CURRENT]

Cassy menjadikan barcode sebagai bagian resmi dari master data produk untuk mendukung checkout yang cepat dan akurat.

## 1. Model Data Barcode
- **Relasi**: Satu Produk dapat memiliki banyak Barcode (`One-to-Many`).
- **Tipe Barcode**:
  - `EAN13` / `UPCA`: Barcode standar supplier.
  - `INTERNAL_FIXED`: Barcode buatan toko untuk item relabel/repack.
  - `VARIABLE_WEIGHT`: [TARGET-STATE] Barcode timbang (misal: Sayur/Daging).

## 2. Kebijakan Barcode Internal (Generator)
Untuk item yang tidak memiliki barcode asli, Cassy menyediakan generator otomatis:
- **Format**: `29 SSS PPPPPPP C` (13 Digit).
  - `29`: Prefix internal Cassy.
  - `SSS`: Kode/Namespace Store.
  - `PPPPPPP`: Serial produk unik.
  - `C`: Check Digit.
- **Aturan**: Generator wajib melakukan pengecekan tabrakan (*collision check*) sebelum menyimpan barcode baru.

## 3. Scanner Flow (Local-First)
1.  **Scan**: Scanner mengirim input ke aplikasi.
2.  **Normalize**: Aplikasi membersihkan spasi/karakter non-digit.
3.  **Lookup**: Pencarian dilakukan **hanya** pada database lokal (`product_barcode`).
4.  **Result**:
    - `FOUND`: Item masuk ke keranjang.
    - `NOT_FOUND`: Tampilkan UI error dengan opsi cari manual.
    - `COLLISION`: Jika satu barcode terdaftar di >1 produk, sistem harus **FAIL LOUD** (blokir dan minta koreksi data).

## 4. Collision Policy
Tabrakan barcode dianggap sebagai **Data Defect**.
- Sistem dilarang memilih produk secara otomatis jika terjadi duplikasi.
- Supervisor wajib memperbaiki data barcode di menu Master Data sebelum produk tersebut dapat di-scan kembali.

## 5. Offline Scan
Lookup barcode wajib bekerja 100% offline. Jika data produk belum sinkron dari HQ, sistem akan menampilkan pesan `MASTERDATA_NOT_READY`.
