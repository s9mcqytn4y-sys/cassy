# R5 Operational Issue Visibility

## Overview
Operational Issue Visibility ensures that any state preventing or delaying normal store operations is explicitly visible to the operator and supervisor. This avoids the "flattening" of pending or blocked states into a generic "healthy" or "error" state.

## Implementation Truth
1. **Source of Truth**: `ReportingQueryFacade` aggregates data from `KernelRepository`, `OutboxRepository`, `OperationalSalesPort`, and `OperationalHardwarePort`.
2. **Explicit Taxonomy**: Every issue is categorized using `OperationalIssueType` (e.g., `SYNC_STATUS`, `PENDING_APPROVAL`, `OPEN_WORK_UNIT`).
3. **Drill-down Consistency**: The same `OperationalIssue` model is used for both summary counts and detailed lists.
4. **Readback Detail**: Issues include `actor`, `timestamp`, and `reasonCode` where applicable, providing a complete audit trail in the UI.

## Visibility Rules
- **Sync**: Latency > 1 hour is `STALLED` (Warning). Latency > 5 mins is `DELAYED` (Info/Warning).
- **Approvals**: Any `REQUESTED` approval is a `PENDING_APPROVAL` issue.
- **Transactions**: Any incomplete transaction in a closing shift is a `CRITICAL` blocker.
- **Work Units**: Any open shift when closing a day is a `CRITICAL` blocker.
- **Hardware**: Any disconnected or failed hardware is reported as `UNAVAILABLE`.
