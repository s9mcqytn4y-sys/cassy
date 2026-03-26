# R6 Sync-Ready Boundary & Replay

Updated: 2026-03-27

## FACT
- Outbox event disimpan di `shared:kernel` sebagai `OutboxEvent` dengan kolom `status`.
- `OutboxRepository` sekarang punya jalur status-aware untuk `PENDING`, `FAILED`, `PROCESSED`, requeue `FAILED -> PENDING`, dan prune processed lama.
- `SyncReplayService` sekarang menjadi worker replay minimal yang:
  - memproses batch event pending
  - bisa me-requeue event failed sebelum retry
  - menandai event `PROCESSED` atau `FAILED`
  - menulis `sync.last_success_timestamp` dan `sync.last_error_message`
  - memangkas event `PROCESSED` lama agar outbox tidak tumbuh tanpa batas
- `SyncReplayPort` membentuk boundary native/infra yang eksplisit. Default repo saat ini adalah `NoopSyncReplayPort`, jadi backend bukan hard dependency transaksi harian.
- `ReportingQueryFacade.getSyncStatus()` sekarang membaca pending backlog, failed backlog, last success, dan explicit last error.
- Desktop top bar, reporting summary, dan action `Sync` di rail sekarang memberi recovery path yang eksplisit lewat `replaySyncAndReload()`.
- Test `SyncReplayServiceTest` memverifikasi success path, requeue failed path, dan unavailable backend path.
- Test `OutboxRepositoryTest` memverifikasi processed event tidak lagi ikut terbaca sebagai pending dan tetap tertahan sebagai row terpisah.

## ASSUMPTION
- Dalam posture local-first Cassy V1, transaksi harian tetap sah meski backend/sync engine belum aktif.
- Implementasi port backend nyata akan datang di lane infra/native berikutnya tanpa memindahkan ownership transaksi keluar dari shared core.

## RISK
- Belum ada transport backend nyata yang berjalan di repo ini, jadi replay end-to-end ke HQ masih bergantung pada implementasi port berikutnya.
- Conflict outcome sudah punya boundary typed, tetapi belum punya persistence khusus `sync_conflict`.
- Hosted Windows evidence untuk workflow yang diperbarui belum saya verifikasi dari run remote baru pada dokumen ini.

## RECOMMENDATION
- Langkah berikutnya untuk benar-benar menutup R6 adalah menambahkan implementasi port backend nyata atau harness integrasi yang sah, lalu rerun hosted evidence.
- Setelah itu, tambah persistence `sync_conflict` bila recovery supervisor butuh drill-down lebih kaya daripada `last_error_message`.
