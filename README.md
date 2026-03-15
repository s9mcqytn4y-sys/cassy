# Cassy Agent Pack V2

Pack V2 ini adalah versi yang lebih lengkap untuk repository Cassy, disusun agar lebih kompatibel dan lebih berguna untuk:
- Codex
- Claude
- Gemini
- GitHub Copilot / IDE agents yang membaca `.github/*`

## Kenapa V2
V2 mengikuti pola yang sudah dipakai di repo Cassy:
- `AGENTS.md` sebagai entrypoint utama lintas agent
- `.agent/README.md` sebagai compression layer AI + manusia
- `CLAUDE.md` sebagai load-order khusus Claude
- file placement yang juga merekomendasikan `.github/copilot-instructions.md`, `.github/instructions/*`, dan `.github/prompts/*`

## Isi utama
### Root
- `AGENTS.md`
- `CODEX.md`
- `CLAUDE.md`
- `GEMINI.md`
- `.aiexclude`
- `README_INSTALLATION.md`

### `.agent/`
- `README.md`
- `plan.md`
- `rules/architecture_rules.md`
- `context/*.md`
- `playbooks/*.md`
- `templates/*.md`
- `memory/*.md`

### `.github/`
- `copilot-instructions.md`
- `instructions/*.instructions.md`
- `prompts/*.prompt.md`

## Operational posture
Pack ini **mengizinkan secara policy** agent untuk:
- read
- create
- edit
- move
- rename
- delete
- git status / diff / add / commit / branch
- build / test / lint

Tetapi pack ini **tidak bisa mem-bypass permission runtime**. Capability aktual tetap ditentukan oleh tool agent yang dipakai.
