# Inventory Ledger Truth [CURRENT]

Cassy tidak mengelola stok hanya sebagai angka saldo tunggal (*single counter*). Seluruh akurasi stok dibangun di atas prinsip **Stock Ledger**.

## 1. Model Data Inti

### A. `InventoryBalance` (Current State)
*   **Peran**: Menyimpan saldo akhir stok per produk di toko/terminal.
*   **Fungsi**: Digunakan untuk query cepat (misal: "Apakah stok barang X masih ada?").
*   **Kekuatan**: Merupakan agregasi dari seluruh transaksi ledger.

### B. `StockLedgerEntry` (The Truth)
*   **Peran**: Catatan atomik setiap perubahan stok (Masuk, Keluar, Koreksi, Penjualan, Return).
*   **Aturan**: Ledger bersifat *append-only*. Tidak boleh ada `UPDATE` atau `DELETE` pada baris ledger yang sudah sukses ditulis.
*   **Integrity**: `InventoryBalance.quantity` harus selalu sama dengan `SUM(StockLedgerEntry.quantityDelta)`.

## 2. Alur Mutasi Stok

Setiap mutasi stok harus menyertakan konteks lengkap:
- `mutationType`: `SALE`, `RETURN`, `ADJUSTMENT`, `RECEIVING`.
- `sourceType`: Menunjuk ke entitas pemicu (misal: `SALE_TRANSACTION`).
- `sourceId`: ID transaksi pemicu.
- `reasonCode`: Mengapa mutasi ini terjadi (terutama untuk Adjustment).

## 3. Discrepancy & Review
Jika terjadi perbedaan antara stok fisik dan sistem (misal saat Stock Opname):
1.  Sistem mencatat di `InventoryDiscrepancyReview`.
2.  Data ini membutuhkan **Approval Mode** (Manual atau Automatic berdasarkan threshold).
3.  Setelah disetujui, sistem membuat `StockLedgerEntry` koreksi dan memperbarui `InventoryBalance` secara atomik.

## 4. FIFO & Layering [TARGET-STATE]
Cassy mendukung pencatatan layer stok (batch/expiry) melalui tabel `InventoryLayer`. Ini memungkinkan pelacakan harga pokok penjualan (COGS/HPP) yang akurat berdasarkan urutan barang masuk.

---
**Peringatan**: dilarang mengubah `InventoryBalance` secara langsung tanpa membuat `StockLedgerEntry` pendamping. Pelanggaran ini akan merusak audit trail sistem.
