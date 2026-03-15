# Cassy Project Overview

## Product shape
Cassy is a **Desktop-First** retail-first operational POS operating core.

## V1 Strategic Focus (PDF v1.1)
- **Primary Release Lane**: Desktop POS (Full retail capability).
- **Parity Lane**: Android POS (Semantic parity with Desktop).
- **Core Scope**:
  - product master data & metadata
  - cart / checkout / finalize sale
  - mandatory inventory ledger & balance
  - business day & shift guardrails
  - receipt snapshot & history
  - sync visibility & offline safety

## Technical posture
- Kotlin Multiplatform (Domain, Application, Data)
- Compose Multiplatform (Native app-shell per platform)
- SQLDelight (Bounded-context databases)
- Koin (DI)
- local-first persistence with honest sync state

## Important truth
The active repo surface is currently converging toward the strategic target-state.
- `:shared:inventory` is active.
- `:apps:desktop-pos` is the primary focus.
- Do not pretend every feature is already operational; check `docs/execution/roadmap_bridge.md` for current milestone status.
