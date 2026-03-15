# Cassy Module Map

## Runtime-visible topology
- `apps/android-pos`: Android retail client (Semantic Parity lane)
- `apps/desktop-pos`: Desktop retail client (Primary Release lane)
- `shared`: Monolithic legacy bridge (to be evacuated)
- `shared:kernel`: Core infrastructure (Auth, Audit, Outbox, Day/Shift)
- `shared:masterdata`: Product and metadata context
- `shared:sales`: Transaction and pricing context
- `shared:inventory`: Stock ledger and balance context (Active)

## Target-state direction
- native app-shell per platform
- platform-device split
- shared core per bounded context
- selective hybrid shared UI
- stronger build-logic and instructions

## Practical interpretation
- Desktop is the primary operational target for V1.
- Android remains a semantic peer but follows Desktop's lead.
- `:shared:inventory` is now live and part of the core checkout flow.
