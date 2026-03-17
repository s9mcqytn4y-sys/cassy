# Cassy Rolling Execution Plan (Updated: R1 / M6 Finalization Flow Hardening)

## Current Default Focus: R1 / M6 Cashier Core Finality
Closing transaction truth from checkout to persisted receipt snapshot on the desktop-first lane.
- **Phase 1 (DONE):** Scope lock and owner boundary for sales finality.
- **Phase 2 (DONE):** Typed finality contract for payment state, sale completion, readback, and print separation.
- **Phase 3 (DONE):** Persistence hardening for sales schema, migration, FK behavior, and fresh-install/upgrade-path verification.
- **Phase 4 (DONE with atomicity caveat):** Complete-sale facade, payment gateway stub, callback/idempotency guard, and honest desktop hardware status.

## Current Status Posture
- M0 - M5: **DONE & STABLE**
- R1 / M6 Finality Contracts: **DONE**
- R1 / M6 Persistence Hardening: **DONE**
- R1 / M6 Finalization Flow Hardening: **DONE for desktop-first lane**
- M6 (Checkout & Receipt Finality): **DONE for desktop-first lane**
- Cross-context atomicity proof: **NOT YET PROVEN**

## Strategic Bridge
Refer to `docs/execution/roadmap_bridge.md` for synchronization with repo truth.
