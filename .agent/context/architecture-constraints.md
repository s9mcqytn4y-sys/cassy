# Cassy Architecture Constraints

Dokumen ini mendefinisikan batasan teknis yang **WAJIB** dipatuhi oleh Agent saat melakukan perubahan kode atau arsitektur.

## 1. Kotlin Multiplatform (KMP) Layers
- **Shared Kernel**: Business logic utama di `shared/kernel`.
- **Domain Layer**: Harus murni Kotlin (tidak ada dependensi platform).
- **Application Layer**: Use cases dan facades.
- **Data Layer**: Implementasi repository, SQLDelight, dan network.
- **Presentation Layer**: Native per platform (Android/Desktop). Jangan mencoba men-share UI kecuali diminta spesifik.

## 2. Persistence Strategy
- **SQLite 3 + SQLDelight**: Satu-satunya mekanisme persistence lokal yang didukung.
- **UUIDv7**: Gunakan UUIDv7 untuk semua primary key baru.
- **Atomic Transactions**: Perubahan status bisnis (misal: Sale) harus dalam satu transaksi SQLite yang mencakup data bisnis + outbox record.

## 3. Local-First & Sync
- **Outbox Pattern**: Setiap mutasi lokal harus menghasilkan record di tabel outbox untuk sinkronisasi nantinya.
- **Idempotency**: Semua operasi tulis harus memiliki idempotency key.
- **Offline Readiness**: Logika bisnis tidak boleh bergantung pada ketersediaan network secara synchronous.

## 4. Module Dependency Rules
- Modul UI -> Modul App Shell -> Modul Application.
- Modul Application -> Modul Domain.
- Modul Data -> Modul Domain & Modul Application (via ports).
- **Dilarang**: Circular dependencies antar bounded contexts (Sales, Inventory, MasterData).
