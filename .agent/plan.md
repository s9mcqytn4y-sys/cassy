# Cassy Rolling Execution Plan (Updated: 0.2.0-beta.1, R4/R5/R6 Done, Beta Hardening Active)

## R1-R4 Retrospective (Hardened)
- **DONE**: R1 Desktop Cashier Finality (Atomic Bundles).
- **DONE**: R2 Operational Control (Shift/Day/Approval Taxonomy).
- **DONE**: R3 Inventory Truth Lite (Ledger/Discrepancy/Balance).
- **DONE**: R4 Windows Release Trust for local/repo lane (EXE + MSI + installer evidence + diagnostics).

## Current Mission: Desktop Beta Hardening
- **R5 Visibility & Reporting** (**DONE / HARDENED**)
  - [x] `ReportingQueryFacade` hidup di `shared:kernel`.
  - [x] `DailySummary` dan `ShiftSummary` terhubung ke desktop.
  - [x] "Blocked/Pending/Error" issue visibility muncul di reporting summary.
  - [x] Sync visibility membaca pending backlog, last success, dan last error.
  - [x] Export bundle CSV/HTML hidup di desktop dan tetap lahir dari snapshot lokal yang sama.
- **R6 Boundary Hardening** (**DONE / LOCAL-BOUNDARY**)
  - [x] Outbox read path status-aware (`PENDING` only).
  - [x] Event processed dipertahankan untuk replay/integrity boundary.
  - [x] Worker replay minimal hidup di `shared:kernel`.
  - [x] Retry/requeue policy eksplisit untuk failed event.
  - [x] Desktop recovery trigger eksplisit (`Sync` / `F1` / `F5`).
  - [x] Test/build/evidence cukup untuk definisi milestone aktif.
  - [ ] Transport backend nyata tetap future lane.
  - [ ] Durable conflict lane tetap future lane.

## Active RC Hardening Track
- [x] Sinkronkan `README` dan entry docs agar tidak drift dari `docs/execution`.
- [x] Finalkan baseline legal/privacy/security (`LICENSE`, `EULA`, `PRIVACY`, `SECURITY`, third-party notice).
- [x] Finalkan support matrix device dan operator quickstart.
- [x] Tambahkan release-candidate checklist sebagai authority gate desktop.
- [x] Tutup void sale execution lane untuk desktop-first V1.
- [x] Finalkan versioning beta, upgrade evidence, perf probe, dan burn-in checklist.
- [x] Kumpulkan evidence build/test/lint/package/smoke terbaru setelah patch beta.
- [ ] Push commit beta ke GitHub + verifikasi hosted workflow/tag terbaru.
- [ ] Selesaikan burn-in rehearsal 2-3 hari untuk pilot terbatas.

## Strategic Lock
- Cassy is desktop-first single-outlet retail core.
- No ERP breadth.
- Local-first truth.
- Truthful reporting queries (Accuracy > Visuals).
