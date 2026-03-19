# R3 Inventory Migration Baseline

Updated: 2026-03-19

## FACT
- Inventory truth baseline sekarang berada di schema inventory v3.
- Migrasi v3 menambahkan:
  - FK internal untuk `StockLedgerEntry`, `InventoryDiscrepancyReview`, dan `InventoryLayer`
  - tabel `InventoryApprovalAction`
- Migrasi/backfill tetap menjaga pemisahan:
  - `InventoryBalance` = current state
  - `StockLedgerEntry` = explanation trail
- Verifikasi migration lane yang benar-benar ada:
  - `:shared:inventory:verifyCommonMainInventoryDatabaseMigration`
  - `InventoryPersistenceMigrationTest`
- Integrity check tambahan dijalankan lewat `PRAGMA foreign_key_check` pada test persistence.

## ASSUMPTION
- DB live yang sudah sampai baseline v2 tidak membawa orphan row inventori di luar pola yang diantisipasi migrasi.

## INTERPRETATION
- Block ini bukan rewrite storage total; ini hardening bertahap agar inventory truth punya integrity floor yang lebih kuat.

## RISK
- Cross-db approval link tetap string-based karena kernel dan inventory berada di database berbeda; FK lintas database memang tidak realistis di SQLite repo ini.
- Jika ada data legacy yang sangat menyimpang dari bentuk v1/v2 yang diketahui, verifikasi tambahan mungkin tetap dibutuhkan.

## RECOMMENDATION
- Jika ada perubahan schema inventory berikutnya, pertahankan pola expand/backfill/verify sebelum cutover.
- Jangan overclaim bahwa migration proof ini sama dengan Windows installer proof; keduanya berbeda.
