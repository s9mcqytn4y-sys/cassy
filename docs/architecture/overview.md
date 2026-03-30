# Architecture Overview [CURRENT]

Cassy adalah sistem **Local-First, Distributed-Write, Central-Convergent**.

## 1. Client Architecture (KMP)

Cassy menggunakan Kotlin Multiplatform (KMP) untuk berbagi business logic, namun tetap mempertahankan native shell untuk performa dan integrasi hardware maksimal.

### Layering Model (Clean Architecture)
1.  **Domain Layer**: Pure Kotlin. Berisi aggregate (Sales, Shift), value objects (UUIDv7), invariants, dan domain events.
2.  **Application Layer**: Use cases dan facades. Mengatur alur transaksi lokal dan orkestrasi antar bounded context.
3.  **Data Layer**: Implementasi repository, SQLDelight drivers, dan network DTOs.
4.  **Presentation Layer**: Native per platform (Desktop Compose / Android Compose). UI hanya diperbolehkan mengakses `Application Facade` atau `Presenter/ViewModel` resmi.

## 2. Bounded Contexts

Pemisahan modul didasarkan pada domain bisnis, bukan fungsi teknis:
- **Kernel**: Auth, Audit, Shift, Business Day, Outbox.
- **Master Data**: Produk, Harga, Katalog.
- **Sales**: Checkout, Payment, Struk, Void.
- **Inventory**: Ledger Mutasi dan Saldo Stok.
- **Cash**: Manajemen Kas Laci dan Rekonsiliasi.

## 3. Persistence Strategy
- **SQLite + SQLDelight**: Standar tunggal persistence.
- **UUIDv7**: Seluruh primary key harus menggunakan UUIDv7 (Time-ordered).
- **Atomic Transactions**: Perubahan status bisnis (misal: Selesaikan Penjualan) harus dalam satu transaksi SQLite yang mencakup (Data Bisnis + Stock Ledger + Audit Log + Outbox Record).

## 4. Local-First & Sync Posture
Cassy tidak melakukan "Save to Server" saat checkout.
1.  **Commit**: Simpan ke DB lokal.
2.  **Append**: Tambahkan record ke tabel `outbox`.
3.  **Sync**: `SyncReplayService` secara periodik membaca outbox dan mengirim ke HQ.
4.  **Convergence**: HQ adalah otoritas akhir untuk konsolidasi data multi-toko.

## 5. Dependency Injection (Koin)
- Platform Module (Desktop/Android) mendaftarkan adapter hardware.
- Shared Module mendaftarkan service/domain logic.
- **App Shell** menjadi composition root yang menggabungkan keduanya.

---
**Risiko**: Pelanggaran layer (misal: UI memanggil SQL langsung) akan dideteksi oleh static analysis (Detekt) dan ditolak dalam PR review.
