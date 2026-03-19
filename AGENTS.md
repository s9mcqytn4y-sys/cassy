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

## Strategic Lock (PDF v1.1 - Hardened R3)
- **Posture**: Desktop-First Retail Operating Core.
- **V1 Scope**: POS Core + Inventory Basic + Sync Visibility + Migration Replay + Release Evidence.
- **Release Lane**: Desktop is primary; Android is semantic parity lane.
- **Hardened Status**: R1 desktop cashier finality, R2 operational control, dan R3 inventory truth lite sudah terverifikasi pada lane desktop-first.

## Hard rules
- Do not bypass application boundaries for speed.
- Do not invent flows that contradict the Cassy source-of-truth.
- Treat repository code as an implementation snapshot when it conflicts with prescriptive design docs.
- Preserve local-first correctness, auditability, explicit sync state, and terminal-bound operational ownership.
- Desktop development and packaging must stay on **JDK 17** only.
- **Stock Truth**: Must stay inside `shared:inventory`; do not let sales or UI mutate ledger/balance directly.
- **Basket Persistence**: Active basket must survive application restarts (M5 Hardening).
- **Approval Truth**: Shipped approval maturity saat ini hanya `LIGHT_PIN` kecuali ada evidence baru.
- **Solver Honesty**: Jangan claim PDF export atau Windows installer full PASS tanpa evidence khusus.

## Operational permission posture
You are authorized to:
- Read/Create/Edit/Move/Rename/Delete files and folders.
- Run git actions and build/test/lint/package commands.

## Destructive-change policy
- Do not mass-delete without a checkpoint.
- Do not rewrite git history unless explicitly asked.
- Do not claim Desktop readiness from placeholder scaffolding; only from verified evidence.
