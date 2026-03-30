# Current Implementation Reality [AS OF 2026-03-27]

Dokumen ini memetakan status nyata repositori Cassy dibandingkan dengan target arsitektur. Digunakan sebagai jangkar kejujuran bagi engineering dan product.

## 1. Core Implementation Status

| Feature | Status | Reality |
| :--- | :--- | :--- |
| **Persistence** | **READY** | SQLite + SQLDelight + UUIDv7 diimplementasikan di `:shared:kernel`. |
| **Desktop Lane** | **ACTIVE** | `:apps:desktop-pos` sudah bisa build MSI/EXE dan menjalankan flow kasir dasar. |
| **Android Lane** | **PARITY** | `:apps:android-pos` berfungsi sebagai business semantics validator. |
| **Inventory** | **CORE-ONLY** | `shared:inventory` memiliki model ledger, namun UI stock-opname masih minimal. |
| **Sync** | **TRANSITION** | `OutboxRepository` ada, tapi `SyncReplayService` masih bersifat manual retry. |
| **Reporting** | **LITE** | `ReportingQueryFacade` tersedia untuk shift & daily summary lokal. |

## 2. Evidence of Implementation (Repo-First)

*   **Database**: Migrasi SQLDelight aktif dan terverifikasi di CI.
*   **Release**: Workflow GitHub Actions (`mainline.yml`, `beta-release.yml`) menghasilkan artifact Windows yang valid.
*   **Kernel**: `shared:kernel` telah memisahkan concern `BusinessDay` dan `Shift` secara eksplisit.
*   **Sales**: `VoidSaleService` sudah mengimplementasikan flow pembatalan dengan reason code dan audit trail.

## 3. Critical Gaps (Reality Check)

1.  **Conflict Resolution**: Belum ada UI untuk menangani konflik data yang dikembalikan backend (HQ).
2.  **Hardware Drivers**: Integrasi printer/scanner di desktop masih bergantung pada standar OS, belum ada layer abstraksi driver yang mendalam.
3.  **AppContainer Legacy**: Masih ada sisa-sisa pola `AppContainer` yang sedang dimigrasikan ke Koin DI.

## 4. Next Technical Focus
*   Penyelesaian migrasi Koin di seluruh modul `:shared`.
*   Penguatan `SyncReplayService` agar lebih otomatis.
*   Pemisahan `platform-device` adapter di `:apps:desktop-pos`.
