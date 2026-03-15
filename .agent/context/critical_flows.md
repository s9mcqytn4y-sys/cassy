# Cassy Critical Flows

## Priority
1. setup minimum
2. product master data dasar
3. product lookup
4. cart
5. payment state
6. finalize sale
7. receipt snapshot dasar
8. transaction history readback

## Mandatory semantics
- completed sale must be valid
- no fake success on failed persistence
- totals/pricing semantics must stay consistent cross-platform
- history/readback must match finalize-sale output

## Cross-platform stance
### Mandatory parity
- product lookup
- cart semantics
- pricing/totals semantics
- payment validity semantics
- finalize sale semantics
- receipt snapshot semantics
- history semantics

### Allowed divergence
- keyboard vs touch ergonomics
- printer/scanner/pairing UX
- packaging/distribution
