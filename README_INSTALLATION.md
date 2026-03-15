# Installation — Cassy Agent Pack V2

## 1. Ekstrak ke root repo
Ekstrak zip ini ke root repository `Cassy/`.

## 2. Pastikan struktur minimal
```text
Cassy/
├── AGENTS.md
├── CODEX.md
├── CLAUDE.md
├── GEMINI.md
├── .aiexclude
├── .agent/
├── .github/
└── README_INSTALLATION.md
```

## 3. Prioritas loading
### General
1. `AGENTS.md`
2. `.agent/README.md`
3. `.agent/rules/architecture_rules.md`
4. `.agent/context/*`
5. `.agent/plan.md`

### Codex
1. `CODEX.md`
2. `AGENTS.md`
3. `.agent/README.md`

### Claude
1. `CLAUDE.md`
2. `AGENTS.md`
3. `.agent/README.md`

### Gemini
1. `GEMINI.md`
2. `AGENTS.md`
3. `.agent/README.md`

### GitHub Copilot / IDE agents
1. `.github/copilot-instructions.md`
2. `.github/instructions/*.instructions.md`
3. `.github/prompts/*.prompt.md`

## 4. Merge policy
Kalau repo lokal sudah punya file sejenis:
- review dulu
- merge yang lebih kuat
- jangan overwrite buta
