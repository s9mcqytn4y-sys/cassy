# R3 Contract Refresh

Updated: 2026-03-19

## FACT
- `inventory_balance` tetap source of truth current state.
- `stock_ledger_entry` tetap canonical explanation trail.
- `InventoryDiscrepancyReview` tetap queue evidence/review, bukan cache balance.
- `InventoryApprovalAction` sekarang menjadi payload durable untuk action inventory yang perlu approval.
- Approval mode shape yang ada:
  - `LIGHT_PIN`
  - `SECOND_PIN`
  - `DUAL_AUTH`
- Yang shipped hanya `LIGHT_PIN`.

## INTERPRETATION
- Contract R3 sekarang cukup future-safe tanpa memalsukan breadth approval atau return engine.

## RECOMMENDATION
- Semua future inventory effect harus tetap membawa source semantics eksplisit dan tidak melewati owner boundary.
