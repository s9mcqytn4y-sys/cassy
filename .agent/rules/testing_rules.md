# Cassy Testing Rules

- Derive tests from traceability, not from random UI clicks.
- For sensitive flows, verify output + domain state + DB state + audit state + sync state.
- Include failure and alternate paths, not only happy paths.
- Printer failure must not invalidate finalized sale.
- Offline and retry behavior must end in explicit state.
- FK/migration replay checks are mandatory when schema changes.
