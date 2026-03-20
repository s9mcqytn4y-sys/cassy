# Cassy Agent Entry (Updated 2026-03-19)

Read these files in order before planning, generating, refactoring, reviewing, or deleting code.

1. `.agent/README.md`
2. `.agent/rules/architecture_rules.md`
3. `.agent/context/project_overview.md`
4. `.agent/context/bounded_contexts.md`
5. `.agent/context/module_map.md`
6. `.agent/context/critical_flows.md`
7. `.agent/context/known_repo_gaps.md`
8. `.agent/plan.md`

## Product Position & Strategic Lock (R5 Mission)
- **Posture**: Desktop-First Retail Operating Core.
- **Core Focus**: Single-outlet retail management. Local-first truth.
- **Anti-Scope**: No ERP breadth. No multi-outlet HQ analytics. No fake business intelligence.
- **R5 Mission**: Visibility & Reporting Lite + Hardening foundations for truth/consistency.
- **Release Lane**: Desktop is primary; Android is semantic parity lane.
- **Hardened Status**: R1-R3 verified on desktop-first lane.

## Quality Bar
- **Reporting**: Query facade correctness is paramount. Accuracy > Visuals.
- **UI**: Follow `GEMINI.md` hardening protocols.
- **Testing**: Business logic must have unit/integration tests in `commonTest`.
- **Docs**: Keep `docs/execution` synced with implementation reality.

## Hard Rules
- **No Fake Claims**: Do not claim milestone completion without evidence. Use "PARTIAL" or "FAIL" honestly.
- **Local-First Truth**: Offline operations must remain valid; sync is secondary visibility.
- **No ERP Creep**: Keep logic focused on retail floor operations, not back-office accounting.
- **Stock Truth**: Must stay inside `shared:inventory`; do not let sales or UI mutate ledger/balance directly.
- **JDK 17**: Desktop development and packaging must stay on **JDK 17**.

## Operational Permission Posture
Authorized for all repo operations: Read/Create/Edit/Move/Rename/Delete, Git, Build, Test, Package.

## Destructive-Change Policy
- Do not mass-delete without a checkpoint.
- Do not rewrite git history unless explicitly asked.
- Do not claim Desktop readiness from placeholder scaffolding.

## Compatibility Note
`GEMINI.md` (in same dir) governs specific UI hardening and interaction patterns. `AGENTS.md` is the strategic and architectural authority.
