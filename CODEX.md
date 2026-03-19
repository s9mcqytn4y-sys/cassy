# Cassy Codex Entry (Updated 2026-03-19)

## Current Truth
- Desktop adalah frontline utama.
- R1 cashier finality, R2 operational control, dan R3 inventory truth lite sudah hidup di repo.
- `shared:inventory` adalah owner tunggal untuk balance/ledger/discrepancy stock truth.

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
1. `.\gradlew :shared:kernel:allTests :shared:sales:desktopTest :shared:inventory:desktopTest :shared:inventory:verifyCommonMainInventoryDatabaseMigration :apps:desktop-pos:test :apps:desktop-pos:smokeRun`
2. `.\gradlew build`
3. `.\gradlew detekt lint`
