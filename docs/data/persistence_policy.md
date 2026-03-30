# Persistence Policy [CURRENT]

Cassy menggunakan pendekatan **Local-First Persistence** untuk menjamin kelangsungan operasional tanpa ketergantungan internet.

## 1. Stack Teknologi
- **Engine**: SQLite 3.
- **Library**: SQLDelight 2.0.x (Kotlin Multiplatform).
- **Driver**:
  - Android: `AndroidSqliteDriver`.
  - Desktop: `JdbcSqliteDriver` (SQLite JDBC).

## 2. Kebijakan Identitas (Identity)
- **Primary Key (PK)**: Seluruh tabel baru **WAJIB** menggunakan `TEXT` sebagai tipe PK yang diisi dengan **UUIDv7**.
- **UUIDv7 Selection**: Dipilih karena mendukung pengurutan waktu secara alami (*time-ordered*) yang sangat membantu performa indeks SQLite dan debugging.
- **Business Numbers**: Kolom seperti `receipt_no` atau `invoice_no` harus dipisahkan dari PK. PK tetap bersifat opaque (tidak memiliki arti bisnis).

## 3. Struktur Tabel & Integrity
- **Foreign Keys**: Harus diaktifkan di level driver (`PRAGMA foreign_keys = ON`).
- **Audit Columns**: Tabel mutasi finansial wajib memiliki kolom `createdAt`, `terminalId`, dan `actorId`.
- **Soft Delete**: Gunakan kolom `status` atau `deletedAt` jika data memiliki referensi historis yang kuat. Hindari `DELETE` fisik pada data transaksi.

## 4. Manajemen Migrasi
- Seluruh perubahan skema harus dilakukan melalui file `.sqm` SQLDelight yang berurutan.
- Migrasi diverifikasi secara otomatis dalam pipeline CI untuk mencegah kegagalan runtime saat update aplikasi.

## 5. ACID & Transaksi
- Penulisan ke database harus dibungkus dalam transaksi SQLDelight (`database.transaction { ... }`).
- **Atomic Mutation**: Satu tindakan user (misal: Bayar) harus menulis ke tabel Bisnis, Ledger, Audit, dan Outbox dalam satu transaksi tunggal.

## 6. Local DB Location
- **Desktop**: Disimpan di folder data aplikasi user (Windows `%APPDATA%` atau subfolder `.cassy`).
- **Android**: Disimpan di folder database internal aplikasi.
