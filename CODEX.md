# Cassy Codex Entry (Updated 2026-03-20)

Use this file first when the agent is Codex or Codex-like.

## Loading order
1. `AGENTS.md`
2. `.agent/README.md`
3. `.agent/rules/architecture_rules.md`
4. `.agent/context/project_overview.md`
5. `.agent/context/bounded_contexts.md`
6. `.agent/context/module_map.md`
7. `.agent/context/critical_flows.md`
8. `.agent/context/known_repo_gaps.md`
9. `.agent/plan.md`

## Codex Behavior Bias
- **Execution Clarity**: Prioritize narrow, shippable POS-first scope.
- **Desktop-First**: Desktop is the primary operational and release target.
- **Hardened Baseline**: M2, M3, M4, and Thin M5 are **DONE & STABLE**.
- **Kotlin 2.3.20**: Ensure all new code complies with Kotlin 2.3.20 standards.
- **Stock Truth**: Keep stock mutation ownership inside `shared:inventory`.
- **Basket Persistence**: Active basket must survive application restarts (M5 Hardening).

## Allowed Repo Operations
You may create/edit/move/rename/delete files and folders, run git actions, and execute build/test/package commands.

## Verification Order
1. `.\gradlew :apps:desktop-pos:smokeRun`
2. `.\gradlew test` (Covers all shared & desktop tests)
3. `.\gradlew :apps:desktop-pos:packageExe`
4. `.\tooling\scripts\Invoke-DesktopDistributionSmoke.ps1`
