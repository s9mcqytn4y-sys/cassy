# R5 Operational Issue Taxonomy & Severity

## Taxonomy
Issues are categorized by their origin and impact on the operational flow.

### 1. Synchronization Status (`SYNC_STATUS`)
- Tracks latency between local terminal and HQ.
- Derived from `OutboxRepository` pending count and `KernelRepository` last success metadata.

### 2. Pending Approvals (`PENDING_APPROVAL`)
- Actions requiring supervisor overrides (e.g., large cash movements, shift variances).
- Readback includes: `actor` (requester), `timestamp`, `reasonCode`, and current `status`.

### 3. Operational Blockers (`OPERATIONAL_BLOCKER`)
- Hard blocks preventing common flows (e.g., terminal not bound, access denied).

### 4. Discrepancies (`DISCREPANCY`)
- Durable differences in truth (e.g., cash variance in a closed shift).
- Readback includes: `actor` (who closed), `timestamp`, and `status`.

### 5. Hardware Issues (`HARDWARE_UNAVAILABLE`)
- Connection or state issues with printers, scanners, or cash drawers.
- Usually ephemeral but visible in the reporting drill-down if durable.

### 6. Pending Transactions (`PENDING_TRANSACTION`)
- Incomplete sales or carts preventing shift closure.

### 7. Open Work Units (`OPEN_WORK_UNIT`)
- Structural blockers like an open shift when attempting to close the business day.

## Severity Semantics
- **INFO**: Normal state visibility (e.g., active shift, healthy sync).
- **WARNING**: Discrepancy or delay that needs attention but doesn't hard-block (e.g., sync delay, pending approval).
- **CRITICAL**: Hard blocker for operational finalization (e.g., pending transactions, open shifts during day close).

## Normalization
All issues MUST be reported via `ReportingQueryFacade` using the `OperationalIssue` model to ensure consistency between summary cards and drill-down lists.
- `OperationalIssueType` provides the semantic class.
- `IssueSeverity` provides the visual/priority weight.
- Status naming is normalized to the underlying domain status (e.g., `REQUESTED`, `STALLED`, `OPEN`).
