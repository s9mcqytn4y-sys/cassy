# Cassy AI Context README

## Purpose
This folder is the human-readable and AI-readable compression layer for the Cassy source-of-truth.

## Loading order
1. `../AGENTS.md`
2. `rules/architecture_rules.md`
3. `context/project_overview.md`
4. `context/bounded_contexts.md`
5. `context/module_map.md`
6. `context/critical_flows.md`
7. `context/known_repo_gaps.md`
8. `plan.md`

## What belongs here
- Stable rules that repeatedly steer implementation.
- Domain and architecture context that should not be re-explained in every prompt.
- Brutally honest notes about migration debt and repo drift.
- Playbooks and templates that standardize how AI assists engineering tasks.

## What does not belong here
- New product requirements with no source backing.
- Temporary scratch notes mixed into stable rules.
- Raw OCR dumps or giant copied PDFs.
- Wishful assumptions about repo health.
