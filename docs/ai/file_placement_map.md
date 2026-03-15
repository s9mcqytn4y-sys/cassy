# Cassy AI Context File Placement Map

## Root-level files
- `AGENTS.md` -> root repository
- `CLAUDE.md` -> root repository
- `CODEX.md` -> root repository
- `GEMINI.md` -> root repository
- `.aiexclude` -> root repository
- `README_INSTALLATION.md` -> root repository

## `.agent/`
- `.agent/README.md` -> entrypoint manusia + AI
- `.agent/plan.md` -> rolling execution plan
- `.agent/rules/*` -> hard constraints dan review rules
- `.agent/context/*` -> reusable knowledge base per concern
- `.agent/playbooks/*` -> how-to workflows for AI tasks
- `.agent/templates/*` -> structured request templates
- `.agent/memory/*` -> evolving team memory

## `.github/`
- `.github/copilot-instructions.md` -> repo-wide Copilot instructions
- `.github/instructions/*.instructions.md` -> path-specific instructions
- `.github/prompts/*.prompt.md` -> reusable prompt files

## Placement recommendation by repository area
- `shared/**` tasks -> read `shared_core.instructions.md`
- `apps/android-pos/**` tasks -> read `android_pos.instructions.md`
- `apps/desktop-pos/**` tasks -> read `desktop_retail.instructions.md`
- `backend/**` tasks -> read `backend.instructions.md`
- sync contract / outbox / reconciliation work -> read `sync_contract.instructions.md`

## Commit policy
Commit everything in this pack except any future local-only notes you intentionally keep outside version control.
