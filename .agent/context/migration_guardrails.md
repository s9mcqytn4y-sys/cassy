# Migration Guardrails

- Do not keep legacy and target paths indefinitely.
- Compatibility facades may exist, but must stay thin.
- Do not add new critical behavior to legacy monolith ownership if the target bounded context already exists.
- No raw SQL from UI or app-shell.
- No silent data correction jobs that rewrite truth without audit trail.
- No sales-side ad hoc stock ownership.
- Namespace cutover must be deliberate and staged.
- Schema change, migration, contract change, and tests should land together.
