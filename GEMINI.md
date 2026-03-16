# Cassy Gemini Entry (Updated 2026-03-20)

Use this file first if the agent is Gemini or another agent that benefits from a compact repo policy file.

## Start here
1. `AGENTS.md`
2. `.agent/README.md`
3. `.agent/rules/architecture_rules.md`
4. `.agent/context/project_overview.md`
5. `.agent/context/bounded_contexts.md`
6. `.agent/context/module_map.md`
7. `.agent/context/critical_flows.md`
8. `.agent/context/known_repo_gaps.md`
9. `.agent/plan.md`

## Working mode
- **Truthful Scope**: Prefer accurate implementation over broad ambition.
- **Desktop-First**: Desktop is the primary operational and release target for V1.
- **Hardened Baseline**: M2, M3, M4, and Thin M5 are **DONE & STABLE**.
- **JDK 17**: Mandatory for Desktop development and packaging.
- **Basket Persistence**: Active basket must survive application restarts (M5 Hardening).

## Permission posture
When tool/runtime allows, you may read, create, edit, move, rename, delete, and use normal git operations in this repository.
