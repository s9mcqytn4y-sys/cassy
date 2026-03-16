# Cassy Claude Entry (Updated 2026-03-20)

Use this file first when the agent is Claude or another Anthropic-like agent.

## Start Here
1. `AGENTS.md`
2. `.agent/README.md`
3. `.agent/rules/architecture_rules.md`
4. `.agent/context/project_overview.md`
5. `.agent/context/bounded_contexts.md`
6. `.agent/context/module_map.md`
7. `.agent/context/critical_flows.md`
8. `.agent/context/known_repo_gaps.md`
9. `.agent/plan.md`

## Strategic Context
- **Posture**: Desktop-First Retail Operating Core.
- **V1 Focus**: Access, Shift, Catalog, Cart, and Inventory Hardening.
- **Hardened Status**: M2, M3, M4, and Thin M5 are **DONE & STABLE**.
- **JDK 17**: Mandatory for Desktop development and packaging.
- **Kotlin 2.3.20**: Current stable baseline.

## Operating Rules
- Maintain application boundaries and stock mutation ownership.
- Ensure active basket persistence for survival on restart.
- Prioritize Desktop-first execution without neglecting Android parity.
- Execute only from verified evidence (Build, Test, Smoke).
