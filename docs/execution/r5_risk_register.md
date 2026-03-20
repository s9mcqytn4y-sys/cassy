# R5 Risk Register: Visibility & Reporting Lite

Updated: 2026-03-19

| Risk ID | Description | Impact | Mitigation | Status |
| :--- | :--- | :--- | :--- | :--- |
| **R5-R01** | Date/Time Ambiguity | Incorrect Daily Summary if UTC is used instead of local terminal time. | Use `TimeZone` aware logic for daily bucket boundaries. | **OPEN** |
| **R5-R02** | Query Performance | Sluggish reporting if DB queries are not optimized (e.g., missing indices). | Audit SQLDelight queries; add indices for `shift_id` and `timestamp`. | **OPEN** |
| **R5-R03** | Data Flattening | Showing "Ready" when approval is pending. | Ensure Reporting Facade preserves `OperationStatus` taxonomy. | **MITIGATED** |
| **R5-R04** | UI Density | Reporting data cluttering the checkout screen. | Separate Reporting View/Tab or dense Sidebar Rail for visibility. | **OPEN** |
| **R5-R05** | Truth Drift | UI calculating totals instead of relying on Repo/Service. | Mandatory query facade for all reporting reads. | **MITIGATED** |
| **R5-R06** | Sync Visibility | Reporting inaccurate if background sync fails silently. | Add "Sync Age" or "Last Sync" metric to summary. | **OPEN** |
