# R1 / M6 Phase 3 Persistence Sync

Dokumen ini mengunci perubahan persistence yang benar-benar hidup di repo untuk `R1 / M6 Cashier Core Finality`.

## Scope yang diselesaikan

- schema `shared:sales` diperketat untuk payment state final dan receipt snapshot final
- migrasi SQLDelight disediakan untuk perubahan persistence
- fresh install path diverifikasi
- upgrade path dari schema lama diverifikasi
- foreign key dan integrity behavior dicek jujur, tidak diasumsikan aktif otomatis

## Perubahan persistence aktual

### 1. Final payment state

- tabel `SalePayment` menyimpan:
  - `status`
  - `statusReasonCode`
  - `statusDetailMessage`
  - `providerReference`
- `status` dibatasi ke `PENDING | SUCCESS | FAILED | CANCELLED`
- `PENDING` dan `FAILED` wajib punya `statusReasonCode`

### 2. Final receipt snapshot

- tabel `ReceiptSnapshot` menjadi artefak final yang persisted
- kolom baru:
  - `snapshotVersion`
  - `templateId`
  - `paperWidthMm`
  - `createdAt`
- `content` tetap JSON snapshot terstruktur, bukan plain-text receipt tunggal

### 3. Thermal print structure

- snapshot receipt sekarang menyimpan metadata template:
  - `templateId = thermal-80mm-v1`
  - `paperWidthMm = 80`
- print payload dibentuk dari snapshot final yang persisted
- outcome print tetap terpisah dari validity settlement

### 4. Readback source

- `CompletedSaleReadback` dibangun dari `ReceiptSnapshot` yang persisted
- history/readback/reprint tidak boleh memakai proyeksi UI ad hoc

### 5. Integrity dan atomicity

- `SaleItem`, `SalePayment`, dan `ReceiptSnapshot` memiliki foreign key ke `Sale`
- foreign key diaktifkan eksplisit pada bootstrap driver desktop/jvm/android
- bootstrap migrasi desktop/jvm men-disable FK hanya selama migrate, lalu enable lagi
- checkout membuat sale + item + pending payment dalam satu transaksi repository

## Migrasi

- `2.sqm`: tambah `statusReasonCode` dan `statusDetailMessage` pada `SalePayment`
- `3.sqm`: rebuild tabel sales untuk constraint, FK `ON DELETE CASCADE`, metadata receipt snapshot, dan index baru

## Verifikasi yang wajib dibaca bersama implementasi

- `:shared:sales:verifyCommonMainSalesDatabaseMigration`
- `:shared:sales:desktopTest`
- `SalesPersistenceBootstrapTest`
  - fresh install path
  - upgrade path
  - FK/integrity enforcement

## Batasan yang tetap jujur

- belum ada print spool/job persistence penuh
- Android masih parity lane, bukan owner UX finality
- status hosted run remote harus dibuktikan dari GitHub setelah push, bukan dari task lokal
