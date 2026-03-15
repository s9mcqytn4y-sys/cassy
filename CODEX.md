# Cassy Codex Entry

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

## Codex behavior bias
- optimize for execution clarity and correct boundaries
- prefer narrow, shippable POS-first scope over broad ERP-like expansion
- **Desktop-First**: prioritize Desktop as the primary operational and release target
- do not let checkout semantics drift across Android and Desktop
- keep device-heavy concerns native
- surface repo gaps explicitly instead of painting over them

## Allowed repo operations
If runtime allows it, you may:
- create/edit/move/rename/delete files and folders
- run git status/diff/add/commit/branch
- patch source code, docs, prompts, and instructions
- run build/test/lint commands relevant to the task
