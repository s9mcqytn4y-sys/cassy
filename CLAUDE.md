# Cassy Claude Memory

Use `AGENTS.md` as the primary entrypoint.

Project memory loading order for this repository:
1. `AGENTS.md`
2. `.agent/README.md`
3. `.agent/rules/architecture_rules.md`
4. `.agent/context/project_overview.md`
5. `.agent/context/bounded_contexts.md`
6. `.agent/context/module_map.md`
7. `.agent/context/critical_flows.md`
8. `.agent/context/known_repo_gaps.md`
9. `.agent/plan.md`

Behavior rules:
- Favor bounded-context ownership over convenience shortcuts.
- Keep native app-shell and device-heavy integration outside shared business modules.
- Never treat sync as a hidden background queue.
- Sensitive flows must leave durable audit evidence.
