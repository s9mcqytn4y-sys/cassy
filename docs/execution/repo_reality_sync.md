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
- `apps:desktop-pos` sekarang punya `DesktopReportingExporter` untuk export bundle CSV/HTML yang tetap membaca snapshot lokal dari reporting facade.
- `shared:sales:VoidSaleService` sekarang membuka jalur void sale cash final dengan reason code, audit/event, cash refund, dan visibility reporting yang eksplisit.
- `shared:kernel:OutboxRepository` sekarang membaca pending/failed event secara status-aware, bisa requeue failed, dan memangkas processed lama.
- `shared:kernel:SyncReplayService` sekarang menjadi worker replay minimal yang dipakai desktop untuk retry sync eksplisit.
- Hosted `Mainline Evidence` sudah pernah sukses, jadi workflow Windows terbaru tidak lagi hanya "CI-configured". Authority aktif tetap mengikuti run terbaru yang benar-benar diverifikasi.

## ASSUMPTION
- V1 tetap single-outlet, terminal-bound, dan local-first.
- Transport backend nyata, conflict persistence, dan expansion Android tetap di luar slice aktif turn ini.

## INTERPRETATION
- Repo truth saat ini konsisten dengan posture "guided operations + cashier core + inventory basic + reporting lite + Windows release trust".
- R5 sekarang sudah melewati fase lite-only karena export operasional formal sudah ada.
- R6 boleh diklaim `DONE` untuk definisi local-boundary Cassy V1: replay lokal, retry/requeue, visibility, dan recovery path sudah hidup dan teruji.

## RISK
- Upgrade antar-versi installer sudah dibuktikan secara lokal, tetapi rollback binary lintas versi dan hosted beta-tag evidence belum dibuktikan.
- Conflict replay masih terbatas pada status failed + last error message, belum sampai persistence conflict detail.
- Profiler-backed memory/performance evidence masih berupa probe ringan, belum profiler snapshot formal.

## RECOMMENDATION
- Gunakan dokumen ini sebagai reality snapshot terbaru.
- Perlakukan dokumen block-level lama yang belum diperbarui sebagai jejak evolusi, bukan authority runtime.
