# Android POS Instructions

Use for work under `apps/android-pos/**`.

## Rules
- Android is the operational baseline
- native lifecycle, permission, scanner, printer, and hardware handling stay native
- UI may orchestrate, but must not own business invariants
- prove smoke path for critical retail flows
