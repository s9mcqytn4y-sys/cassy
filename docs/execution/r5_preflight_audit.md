# R5 Preflight Audit: Visibility & Reporting Lite

Updated: 2026-03-19

## R1-R4 Baseline Verification
- **R1 (Sales)**: PASS. Finalization bundles are atomic.
- **R2 (Ops)**: PASS. Decision/Blocker taxonomy is mature.
- **R3 (Inventory)**: PASS. Ledger and Balance truth established.
- **R4 (Desktop)**: PASS. UI hardened with safety gates and shortcuts.

## Target R5 Surfaces
1. **Kernel Query Facade**: `shared:kernel:application:ReportingQueryFacade`
2. **Sales Summary Port**: `shared:sales:application:OperationalSalesPort`
3. **Desktop Reporting UI**: `apps:desktop-pos:CassyReportingComponents`
4. **Daily Summary Logic**: `shared:kernel:application:DailySummaryService`

## Metrics Baseline
| Metric | Source | Status |
| :--- | :--- | :--- |
| Completed Sales | `sales.db` | **SAFE** |
| Expected Cash | `kernel.db` | **SAFE** |
| Variance | `kernel.db` | **SAFE** |
| Ledger Balance | `inventory.db` | **SAFE** |
| Sync Lag | `shared:kernel:SyncState` | **UNSAFE** (Visibility Lite only) |

## Foundation Hardening Requirements
- **Query Unification**: Move summary logic out of services into a dedicated query facade.
- **Timezone Safety**: Ensure Daily Summary uses the terminal's local business date, not system UTC.
- **Issue Visibility**: Ensure `OperationBlockerCode` includes sync-delay or approval-pending status for reporting.
