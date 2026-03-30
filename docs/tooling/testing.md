# Testing Strategy [CURRENT]

Cassy menjunjung tinggi kualitas kode melalui piramida testing yang disiplin.

## 1. Unit Testing (Mandatory)
- **Target**: Domain Logic dan Application Use Cases.
- **Framework**: `kotlin.test`, `MockK` (jika diperlukan, namun prefer Fakes/Stubs).
- **Aturan**: Setiap PR yang mengubah logika bisnis wajib menyertakan unit test pendamping.
- **Location**: `commonTest` di setiap modul shared.

## 2. Database Integration Testing
- **Target**: SQLDelight Repository dan Migrasi.
- **Framework**: In-memory SQLite driver (`JdbcSqliteDriver` / `AndroidSqliteDriver`).
- **Aturan**: Verifikasi integritas skema dan kueri kompleks (JOIN/Aggregation) harus diuji di level ini.

## 3. Platform Smoke Testing
- **Desktop**: Verifikasi startup, pemuatan native SQLite, dan navigasi dasar.
- **Android**: Verifikasi permission handling dan lifecycle UI.

## 4. Sync & Outbox Testing
- **Target**: `SyncReplayService` dan `OutboxRepository`.
- **Skenario**: Menguji kegagalan jaringan (simulasi), retry logic, dan integritas urutan data (FIFO) saat sinkronisasi.

## 5. Audit Log Verification
- Setiap tindakan sensitif (Void, Refund, Stok Opname) harus diuji untuk memastikan record audit log terbentuk secara atomik.
