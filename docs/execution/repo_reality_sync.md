# Repo Reality Sync

Updated: 2026-03-27

## FACT
- Repo saat ini adalah desktop-first retail operating core untuk single outlet.
- `apps:desktop-pos` adalah lane operasional utama; Android tetap parity/business-semantics lane.
- Ownership concern operasional aktif berada di:
  - `shared:kernel` untuk business day, shift, approval, reason, audit, readiness, close review, outbox, dan reporting facade
  - `shared:sales` untuk finality transaksi dan read model summary yang dibutuhkan closing
  - `shared:inventory` untuk stock mutation truth
- `:shared` tetap ada sebagai legacy bridge, tetapi concern operasional baru tidak ditambahkan ke sana.
- R4 sekarang punya evidence lokal untuk source smoke, distribution smoke, installer evidence MSI, diagnostics, dan recovery baseline.
- Packaging desktop yang benar-benar dikonfigurasi saat ini adalah Windows `EXE` dan `MSI`.
- `shared:kernel:ReportingQueryFacade` sudah hidup dengan daily summary, shift summary, sync visibility, dan explicit last-error readback.
- `shared:kernel:OutboxRepository` sekarang membaca pending/failed event secara status-aware, bisa requeue failed, dan memangkas processed lama.
- `shared:kernel:SyncReplayService` sekarang menjadi worker replay minimal yang dipakai desktop untuk retry sync eksplisit.

## ASSUMPTION
- V1 tetap single-outlet, terminal-bound, dan local-first.
- Transport backend nyata, conflict persistence, dan expansion Android tetap di luar slice aktif turn ini.

## INTERPRETATION
- Repo truth saat ini konsisten dengan posture "guided operations + cashier core + inventory basic + reporting lite + Windows release trust".
- R6 belum boleh diklaim `DONE`, tetapi recovery path dan boundary replay-nya sekarang eksplisit dan bisa diuji.

## RISK
- Hosted CI remote completion untuk workflow yang sudah diperbarui belum dibuktikan pada turn ini.
- Upgrade/rollback antar-versi installer belum dibuktikan.
- Hosted CI remote completion untuk workflow yang sudah diperbarui belum dibuktikan pada turn ini.
- Upgrade/rollback antar-versi installer belum dibuktikan.
- Conflict replay masih terbatas pada status failed + last error message, belum sampai persistence conflict detail.
## RECOMMENDATION
- Gunakan dokumen ini sebagai reality snapshot terbaru.
- Perlakukan dokumen block-level lama yang belum diperbarui sebagai jejak evolusi, bukan authority runtime.
