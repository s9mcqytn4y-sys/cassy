# Cassy Architecture Rules

## Non-negotiable
- Local-first client persistence is the immediate operational source of truth.
- One operational writer per terminal/device for checkout and shift-critical flows.
- Dependencies point inward: UI -> App Shell -> Application -> Domain -> Data -> Database / External.
- Current state and explanation trail are separate concerns.
- Audit and outbox are part of the business decision, not optional extras.
- Sync is explicit, visible, durable, and conflict-aware.
- Business correctness wins over pretty layering.

## Layer rules
### UI
May own screen state, rendering, navigation, and UX-local validation.
Must not own invariants, repository SQL, sync tables, or approval bypasses.

### App Shell
May own bootstrap, session bootstrap, terminal binding, connectivity state, permission gates, and dependency composition.
Must not own aggregate rules.

### Application
Owns use cases, command/query handlers, orchestration, and transaction boundaries.

### Domain
Owns aggregates, policies, value objects, invariants, and decision models.

### Data
Owns repository implementations, SQLDelight adapters, HTTP sync adapters, DTO/entity mapping, and cache policy.

## Guardrails
- No raw SQL from UI or ViewModel.
- No stock mutation outside inventory boundary.
- No destructive rewrite of approval or audit history.
- No server-roundtrip dependency in checkout happy path.
- No hidden last-write-wins for financially sensitive divergence.
