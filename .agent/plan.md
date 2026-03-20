# Cassy Rolling Execution Plan (Updated: R5 Block 1 Lock)

## R1-R4 Retrospective (Hardened)
- **DONE**: R1 Desktop Cashier Finality (Atomic Bundles).
- **DONE**: R2 Operational Control (Shift/Day/Approval Taxonomy).
- **DONE**: R3 Inventory Truth Lite (Ledger/Discrepancy/Balance).
- **DONE**: R4 Desktop Foundation (Maximized UI/Shortcuts/Safety).

## Current Mission: R5 Visibility & Reporting Lite
- **Block 1: Preflight Audit & Risk Lock** (**DONE**)
  - [x] R1-R4 Re-verification.
  - [x] Reporting Query Facade Surface Identification.
  - [x] Safe vs Unsafe Metrics Lock.
  - [x] Risk Register establishment.
- **Block 2: Core Reporting Facade (Next)**
  - [ ] Implement `ReportingQueryFacade` in `shared:kernel`.
  - [ ] Implement `OperationalSalesPort` in `shared:sales`.
  - [ ] Implement `DailySummaryService` with timezone safety.
  - [ ] Add `ShiftSummary` to `ReportingQueryFacade`.
- **Block 3: Visibility UI & Issue Readback**
  - [ ] Implement dense `ReportingPanel` in desktop UI.
  - [ ] Add "Blocked/Pending" issue visibility to summaries.
  - [ ] Add Sync Visibility (Last Sync Age).
  - [ ] Finalize R5 Smoke tests.

## Strategic Lock
- Cassy is desktop-first single-outlet retail core.
- No ERP breadth.
- Local-first truth.
- Truthful reporting queries (Accuracy > Visuals).
