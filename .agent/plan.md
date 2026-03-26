# Cassy Rolling Execution Plan (Updated: R4 Closed Local, R5 Done Lite, R6 Hardened)

## R1-R4 Retrospective (Hardened)
- **DONE**: R1 Desktop Cashier Finality (Atomic Bundles).
- **DONE**: R2 Operational Control (Shift/Day/Approval Taxonomy).
- **DONE**: R3 Inventory Truth Lite (Ledger/Discrepancy/Balance).
- **DONE**: R4 Windows Release Trust for local/repo lane (EXE + MSI + installer evidence + diagnostics).

## Current Mission: R6 Sync-Ready Boundary & Replay
- **R5 Visibility & Reporting Lite** (**DONE / VERIFIED**)
  - [x] `ReportingQueryFacade` hidup di `shared:kernel`.
  - [x] `DailySummary` dan `ShiftSummary` terhubung ke desktop.
  - [x] "Blocked/Pending/Error" issue visibility muncul di reporting summary.
  - [x] Sync visibility membaca pending backlog, last success, dan last error.
- **R6 Boundary Hardening** (**ACTIVE**)
  - [x] Outbox read path status-aware (`PENDING` only).
  - [x] Event processed dipertahankan untuk replay/integrity boundary.
  - [x] Worker replay minimal hidup di `shared:kernel`.
  - [x] Retry/requeue policy eksplisit untuk failed event.
  - [x] Desktop recovery trigger eksplisit (`Sync` / `F1` / `F5`).
  - [ ] Transport backend nyata.
  - [ ] Durable conflict lane.

## Strategic Lock
- Cassy is desktop-first single-outlet retail core.
- No ERP breadth.
- Local-first truth.
- Truthful reporting queries (Accuracy > Visuals).
