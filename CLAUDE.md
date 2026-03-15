# Cassy Claude Memory

Use `AGENTS.md` as the primary entrypoint.

Project memory loading order:
1. `AGENTS.md`
2. `.agent/README.md`
3. `.agent/rules/architecture_rules.md`
4. `.agent/context/project_overview.md`
5. `.agent/context/bounded_contexts.md`
6. `.agent/context/module_map.md`
7. `.agent/context/critical_flows.md`
8. `.agent/context/known_repo_gaps.md`
9. `.agent/plan.md`

## Behavior rules
- favor bounded-context ownership over convenience shortcuts
- keep native app-shell and device-heavy integration outside shared business modules
- **Desktop-First**: prioritize Desktop as the primary operational and release target for V1
- never treat sync as a hidden background queue
- sensitive flows must leave durable audit evidence

## Operational permission posture
When runtime/tooling allows it, Claude may:
- create/edit/move/rename/delete files
- run git status/diff/add/commit/branch
- update docs, code, context, prompts, and instructions
