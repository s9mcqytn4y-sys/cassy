# Product Direction: Cassy Core [CURRENT]

## 1. Visi Utama
Cassy adalah **Retail Operating Core** yang dirancang untuk keandalan operasional tinggi di level outlet (single-store). Cassy bukan sekadar aplikasi POS, melainkan sistem saraf pusat untuk retail execution.

## 2. Prinsip Strategis

### A. Desktop-First (Windows)
*   **Alasan**: Lingkungan retail membutuhkan stabilitas periferal (printer, scanner, EDC) dan multitasking berat. Windows adalah platform standar industri untuk POS station.
*   **Implikasi**: `apps:desktop-pos` adalah prioritas utama rilis dan fitur.

### B. Local-First & Offline-Ready
*   **Alasan**: Downtime internet tidak boleh menghentikan transaksi.
*   **Implikasi**: Semua mutasi bisnis (Sales, Cashier, Inventory) ditulis ke SQLite lokal secara atomik. Sinkronisasi ke HQ adalah proses latar belakang yang durabel.

### C. Ledger-Based Truth
*   **Alasan**: Auditability adalah kunci retail. Saldo stok atau uang tidak boleh hanya berupa angka "total", melainkan harus bisa diurai dari sejarah mutasi (ledger).
*   **Implikasi**: Setiap perubahan stok menghasilkan `stock_ledger_entry`. Setiap mutasi kas menghasilkan `cash_movement`.

### D. Single-Outlet Operating Core
*   **Alasan**: Fokus pada kesempurnaan operasional di satu toko sebelum menangani kompleksitas multi-outlet.
*   **Implikasi**: Fitur multi-store, inter-store transfer, dan konsolidasi HQ adalah concern backend/HQ, bukan concern utama client POS.

## 3. Platform Lanes

| Platform | Peran | Status |
| :--- | :--- | :--- |
| **Windows Desktop** | Operasional Utama & Backoffice | **Primary Lane** |
| **Android Tablet/Mobile** | Parity Lane / Business Semantics | **Secondary Lane** |
| **iOS** | N/A | **Non-Goal (V1)** |

## 4. Non-Goals (V1)
*   **Multi-tenant Cloud POS**: Cassy V1 fokus pada self-hosted atau dedicated HQ integration.
*   **F&B Complex Features**: Tidak ada Kitchen Display System (KDS) atau Table Management kompleks di V1 (Retail focus).
*   **Full Shared UI**: UI Android dan Desktop boleh berbeda untuk mengoptimalkan UX masing-masing platform.
