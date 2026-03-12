# Cassy Agent Entry

Read these files in order before planning, generating, refactoring, or reviewing code.

1. `.agent/README.md`
2. `.agent/rules/architecture_rules.md`
3. `.agent/context/project_overview.md`
4. `.agent/context/bounded_contexts.md`
5. `.agent/context/module_map.md`
6. `.agent/context/critical_flows.md`
7. `.agent/context/known_repo_gaps.md`
8. `.agent/plan.md`

Hard rules:
- Do not bypass application boundaries for speed.
- Do not invent flows that contradict the Cassy source-of-truth.
- Treat repository code as an implementation snapshot when it conflicts with prescriptive design docs.
- Preserve local-first correctness, auditability, explicit sync state, and terminal-bound operational ownership.
- Do not add new F&B or Service complexity into retail shipping flows unless the task explicitly targets prepared boundaries.
