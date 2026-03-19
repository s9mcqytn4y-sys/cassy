---
applyTo: "backend/**"
---

# Backend Instructions

- HQ is authority-convergent, not a hard dependency for phase-1 local operation.
- Sync ingest, conflict response, and master data distribution must stay explicit.
- Do not collapse client-visible sync states into opaque server-only logs.
- Schema, migration, contract, and tests should land together.
- Do not let server-side assumptions override local inventory truth without explicit discrepancy handling.
