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
- Dengan definisi milestone yang dikunci untuk Cassy V1 local-first, R6 dianggap `DONE` bila boundary replay lokal, retry/requeue, visibility, recovery path, dan evidence test/build sudah lengkap tanpa menjadikan backend harian sebagai hard dependency.

## ASSUMPTION
- Dalam posture local-first Cassy V1, transaksi harian tetap sah meski backend/sync engine belum aktif.
- Implementasi port backend nyata akan datang di lane infra/native berikutnya tanpa memindahkan ownership transaksi keluar dari shared core, tetapi itu bukan lagi exit gate untuk status R6 pada definisi yang aktif.

## RISK
- Belum ada transport backend nyata yang berjalan di repo ini, jadi replay end-to-end ke HQ masih menjadi lane berikutnya, bukan proof untuk R6 lokal.
- Conflict outcome sudah punya boundary typed, tetapi belum punya persistence khusus `sync_conflict`.

## RECOMMENDATION
- R6 cukup dipertahankan dengan test dan docs truth yang ketat.
- Implementasi port backend nyata dan persistence `sync_conflict` diperlakukan sebagai lane lanjutan, bukan syarat untuk tetap menyebut boundary sync desktop-ready.
