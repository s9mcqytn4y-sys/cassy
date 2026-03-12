# Cassy Rolling AI Plan

## Current objective
Use the AI context layer to reduce architectural drift, speed up navigation in the monorepo, and keep generated code aligned with local-first + audit-heavy Cassy rules.

## Active workstreams
1. Harden entrypoint rules and bounded-context ownership.
2. Make repo drift explicit so AI does not normalize transitional blobs.
3. Push implementation and review flows through traceability-aware playbooks.
4. Keep Android POS, shared core, sync, and migration work aligned.

## Immediate risks
- False modularization.
- Extending `AppContainer` beyond bridge scope.
- Legacy/new DB dual-path becoming permanent.
- Sync visibility reduced to logs.
- Desktop parity being claimed before operational flows are real.

## Update checklist
- Update after milestone changes.
- Update after namespace cutover decisions.
- Update after each migration wave.
- Update after any source-of-truth revision that changes architecture meaningfully.
