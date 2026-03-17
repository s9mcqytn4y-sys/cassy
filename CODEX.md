# Cassy Codex Entry (Updated 2026-03-20)

## UI Hardening Protocol (Finalized Phase 0-3)
- **Startup**: Application MUST start maximized.
- **Navigation**: 72dp Slim Rail with icon-only brand identity.
- **Transaction Workspace**: 380dp Fixed Right Cart Panel.
- **Input**: `CassyCurrencyInput` with right-alignment and bold pricing.
- **Safety**: Double-confirmation dialogs for all destructive shift/day actions.
- **Ergonomics**: Full F-key mapping (F1, F5, F11, F12) and Numpad Enter support.

## Troubleshooting & Environment
- **Process Lock (EBUSY)**: If `.index.tmp` or `jdt_ws` is locked, kill java processes:
  `Get-Process java -ErrorAction SilentlyContinue | Stop-Process -Force`
- Refer to `.agent/playbooks/environment_troubleshooting.md` for more details.

## Loading order
1. `AGENTS.md`
2. `.agent/README.md`
3. `.agent/plan.md`

## Verification Order
1. `.\gradlew :apps:desktop-pos:smokeRun`
2. `.\gradlew test`
3. `.\gradlew :apps:desktop-pos:run`
