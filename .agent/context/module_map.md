# Cassy Module Map

## Runtime-visible topology
- `apps/android-pos`
- `apps/desktop-pos`
- `shared`
- `shared:kernel`
- `shared:masterdata`
- `shared:sales`

## Target-state direction
- native app-shell per platform
- platform-device split
- shared core per bounded context
- selective hybrid shared UI
- stronger build-logic and instructions

## Practical interpretation
- Android is closer to operational truth
- Desktop still needs proof
- a giant shared blob is not a finished design merely because new modules exist
