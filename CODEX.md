# Cassy Codex Entry (Updated 2026-03-27)

## Current Truth
- Desktop adalah frontline utama.
- Repo sedang berada pada posture `0.2.0-beta.1` dengan target `Private Beta / Controlled Beta`.
- R1 cashier finality, R2 operational control, R3 inventory truth lite, R4 Windows release trust, R5 reporting/export lite, dan R6 local-boundary replay sudah hidup di repo.
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
1. `.\gradlew --version`
2. `.\gradlew clean`
3. `.\gradlew build`
4. `.\gradlew test`
5. `.\gradlew lint detekt`
6. `.\gradlew :shared:kernel:verifyCommonMainKernelDatabaseMigration :shared:inventory:verifyCommonMainInventoryDatabaseMigration :shared:masterdata:verifyCommonMainMasterDataDatabaseMigration :shared:sales:verifyCommonMainSalesDatabaseMigration`
7. `.\gradlew :apps:desktop-pos:createDistributable :apps:desktop-pos:packageExe :apps:desktop-pos:packageMsi :apps:desktop-pos:smokeRun`
8. `powershell -ExecutionPolicy Bypass -File tooling/scripts/Invoke-DesktopDistributionSmoke.ps1`
9. `powershell -ExecutionPolicy Bypass -File tooling/scripts/Invoke-WindowsInstallerEvidence.ps1`
10. `powershell -ExecutionPolicy Bypass -File tooling/scripts/Invoke-WindowsUpgradeEvidence.ps1`
11. `powershell -ExecutionPolicy Bypass -File tooling/scripts/Invoke-DesktopPerformanceProbe.ps1`
