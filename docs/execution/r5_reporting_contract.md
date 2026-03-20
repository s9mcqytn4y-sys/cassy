# R5 Reporting Contract

## Boundary Policy
- UI components MUST NOT execute SQL queries for reporting.
- All reporting data MUST be fetched via `ReportingQueryFacade`.
- Reporting models MUST be immutable and reflect the source-of-truth at the time of query.

## Metric Truth
- **Sales Total**: Sum of completed transactions from `OperationalSalesPort`.
- **Expected Cash**: `Opening Cash + Cash In + Cash Sales - Cash Out - Safe Drop`.
- **Variance**: `Actual Cash - Expected Cash`.
- **Unavailable Metrics**: Use `null` or specific "Unavailable" status if data cannot be derived truthfully.

## Date & Boundaries
- Business Day is the primary temporal boundary.
- `dateLabel` is derived using the `TimeZone` provided to the facade (defaulting to system default).
- Shift boundaries are strictly within a single Business Day.
