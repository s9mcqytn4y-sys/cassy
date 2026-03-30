# Module Map & Dependency Rules [CURRENT]

Cassy menggunakan struktur Monorepo yang memisahkan concern aplikasi (*App Shell*) dari inti bisnis (*Shared Multiplatform*).

## 1. Modul Aplikasi (Apps)
Modul ini adalah entry point platform-specific. Tidak boleh ada logika bisnis di sini.
- `:apps:desktop-pos`: (Primary) Shell Compose Multiplatform untuk Windows. Memiliki konfigurasi packaging MSI/EXE.
- `:apps:android-pos`: (Parity) Shell Android untuk tablet/mobile.

## 2. Shared Core (Kotlin Multiplatform)
Seluruh logika bisnis hidup di bawah folder `shared/` dan dipecah berdasarkan **Bounded Context**.

| Modul | Tanggung Jawab |
| :--- | :--- |
| `:shared:kernel` | Auth, Audit, Shift, Business Day, Outbox Repository. |
| `:shared:sales` | Checkout, Pricing, Payments, Receipt Formatting, Void Sale. |
| `:shared:inventory`| Saldo Stok, Ledger Mutasi, Adjustment, Receiving. |
| `:shared:masterdata`| Katalog Produk, Kategori, Harga, Barcode Schema. |
| `:shared:cash` | Laci Kas (Cash Drawer), Opening/Closing Shift. |

## 3. Dependency Rules (MANDATORY)

Setiap modul dipecah secara internal menjadi 3 layer utama:
1.  **Domain**: Bergantung pada `shared:kernel:domain`. Dilarang bergantung pada layer lain.
2.  **Application**: Bergantung pada `domain`. Berisi facade yang diekspos ke UI.
3.  **Data**: Bergantung pada `domain` dan `application`. Implementasi repository dan SQLDelight.

**Aturan Emas:**
*   Dilarang keras melakukan circular dependency antar konteks (misal: `sales` -> `inventory` dan `inventory` -> `sales`). Gunakan `shared:kernel` sebagai jembatan jika diperlukan.
*   UI (Apps) dilarang mengakses modul `:data` atau query SQL secara langsung. UI hanya boleh berbicara ke `:application` facade.

## 4. Tooling & Build Logic
- `:tooling:build-logic`: Berisi Convention Plugins Gradle untuk standarisasi compiler, lint, dan testing di seluruh modul.
- `:tooling:sqlite-worker-init`: Inisialisasi runtime khusus untuk native SQLite di environment Windows.
