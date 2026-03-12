---
applyTo: "shared/**"
---

# Shared Core Instructions

- No UI imports.
- No platform APIs.
- Repositories hide SQLDelight generated queries behind data/application boundaries.
- Cross-context access only via explicit contracts, facades, or reference objects.
- Preserve aggregate invariants, idempotency, auditability, and explicit sync state.
