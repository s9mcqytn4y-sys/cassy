# Cassy Agent Entry

Read these files in order before planning, generating, refactoring, reviewing, or deleting code.

1. `.agent/README.md`
2. `.agent/rules/architecture_rules.md`
3. `.agent/context/project_overview.md`
4. `.agent/context/bounded_contexts.md`
5. `.agent/context/module_map.md`
6. `.agent/context/critical_flows.md`
7. `.agent/context/known_repo_gaps.md`
8. `.agent/plan.md`

## Strategic Lock (PDF v1.1)
- **Posture**: Desktop-First Retail Operating Core.
- **V1 Scope**: POS Core + Inventory Basic + Sync Visibility + Migration Replay + Release Evidence.
- **Release Lane**: Desktop is primary; Android is semantic parity lane.
- **Model**: Solo Agile End-to-End (2-week sprints).

## Hard rules
- Do not bypass application boundaries for speed.
- Do not invent flows that contradict the Cassy source-of-truth.
- Treat repository code as an implementation snapshot when it conflicts with prescriptive design docs.
- Preserve local-first correctness, auditability, explicit sync state, and terminal-bound operational ownership.
- Do not add new F&B or Service complexity into retail shipping flows unless the task explicitly targets prepared boundaries.

## Operational permission posture
When the runtime/tool permits it, you are authorized in this repository to:
- read any relevant repository file except intentionally excluded local noise
- create files/folders
- edit files/folders
- move files/folders
- rename files/folders
- delete files/folders
- run git status / diff / add / commit / branch actions
- run build / test / lint / format commands

## Destructive-change policy
Allowed does not mean careless:
- do not mass-delete without a checkpoint
- do not rewrite git history unless explicitly asked
- do not rename large trees cosmetically
- do not claim Desktop readiness from placeholder scaffolding
