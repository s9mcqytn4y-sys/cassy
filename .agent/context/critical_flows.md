# Critical Flows

## 1. First operational setup
Preconditions: activation, terminal binding, minimal data, printer setup.
Invariant: no first transaction before setup gate completes.

## 2. Open business day
Invariant: business day must be active before shift and checkout.

## 3. Start shift
Invariant: opening cash and shift validity required before sales.

## 4. Checkout + payment + receipt
Invariant: no final sale without valid payment state and receipt snapshot.
Note: printer failure must not cancel a finalized sale.

## 5. Cash movement / safe drop / approval-aware side flows
Invariant: reason code and approval rules must be enforced at the application boundary.

## 6. Inventory light operations
Invariant: high-risk ops are offline-restricted and approval-aware.

## 7. Sync visibility and reconciliation
Invariant: offline/pending/failed/conflict must be visible and actionable.

## 8. Close shift
Invariant: pending blockers and variance policy must be handled before close.

## 9. Close business day
Invariant: no open shift, no missing critical data, no unresolved minimum readiness blocker.
