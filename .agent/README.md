# Cassy AI Context README

## Purpose
This folder is the source-of-truth for Cassy's operational rules and architectural guardrails.

## Current Truth
- Desktop-first retail operating core.
- R1 desktop cashier finality sudah hidup dan diverifikasi.
- R2 Block 1 operational foundation sudah menambah control tower, open day, start shift, opening cash policy, dan readiness truth.
- R2 operational hardening dan R3 inventory truth lite juga sudah hidup pada lane desktop-first.
- Approval maturity yang shipped saat ini tetap `LIGHT_PIN`.

## UI/UX Hardening Protocol
1. **Screen Fit**: Standard application start is Maximized (`rememberWindowState`).
2. **Auto-Feedback**: All `UiBanner` (toasts) MUST auto-dismiss after 3000ms.
3. **Safety Gates**: Critical actions (End Shift, Close Day) MUST use `CassySafetyDialog` with human-readable warnings.
4. **Ergonomics**:
   - `F1/F5`: Sync/Refresh.
   - `F11/F12`: End Shift / Close Day.
   - `NumPadEnter`: Fast submit for currency inputs.
   - shortcut nominal diprioritaskan untuk opening cash dan lane tunai yang throughput-sensitive.
5. **Terminology**: Avoid engineering jargon.
   - Use "ID Terminal" instead of "Node ID".
   - Use "Buka Kasir" instead of "Start Shift".

## Loading order
1. `../AGENTS.md`
2. `rules/architecture_rules.md`
3. `context/project_overview.md`
4. `plan.md`
