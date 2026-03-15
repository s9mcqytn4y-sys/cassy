# Cassy Known Repo Gaps

## Risks that must stay visible
- **Pseudo-modularization (M2 Debt)**: `:shared` still acts as an aggregator for UI and DI.
- **AppContainer / Service-locator blob**: Still present in `:shared/src/commonMain/kotlin/id/azureenterprise/cassy/di/Koin.kt`.
- **Legacy/new DB coexistence**: Migration baseline for SQLDelight contexts.
- **Desktop Placeholder risk**: (Mitigated) Desktop now has `CatalogScreen.kt`.
- **Parity drift**: Android and Desktop UI coordination.
- **Inventory ownership ambiguity**: `:shared:inventory` exists but isn't integrated.
- **Docs more advanced than runtime**: (Mitigated) `roadmap_bridge.md` updated to match actual repo truth.

## Resolved Gaps (Audit Fix)
- [x] Missing `CatalogScreen` reference in Desktop POS.
- [x] Roadmap Bridge status sync (M3 marked as Done).
