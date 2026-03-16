# Cassy Rolling Execution Plan

## Current Default Focus

Cassy V1 Foundation Closure & Hardening:
- **M2 (Hardened):** Control plane truth & build verification (Kotlin 2.3.20 sync).
- **M3 (Hardened):** Desktop access & bootstrap foundation.
- **M4 (Hardened):** Business day & shift guardrails.
- **Thin M5 (Hardened):** Catalog lookup + Cart mutation + Basket persistence.

## Current Status Posture

- M0: **DONE**
- M1: **DONE**
- M2: **DONE**
- M3: **DONE**
- M4: **DONE**
- Thin M5: **DONE**
- M6: **PENDING** (Checkout & Payment Gap)
- M7: **DONE (THIN)** (Inventory Integration)

## Next Planning Order: M6 & M9 (Checkout & Visibility)

1. **M6: Checkout Finalization**
   - Implement basic `PaymentProcessor` interface (Stubs for M6).
   - Solidify `SaleStatus.COMPLETED` state in DB.
   - Initial Receipt Template (Markdown-based).
2. **M9: Sync & Replay**
   - Outbox pattern audit for sales transactions.
   - Sync status visibility in Desktop UI.
3. **M10: CI Release Validation**
   - Resolve hosted Windows runner limitations for automated installer verification.

## Strategic Bridge

Refer ke `docs/execution/roadmap_bridge.md` untuk status milestone yang sinkron dengan repo truth.
