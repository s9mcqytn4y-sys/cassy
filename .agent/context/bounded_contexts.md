# Cassy Bounded Contexts

## Active / visible now
- **kernel**: infrastructure, outbox, audit, day/shift
- **masterdata**: product, category, metadata
- **sales**: basket, pricing, sale finalization
- **inventory**: stock ledger, balance ownership (Live)

## Wider target-state
- **returns**: refund aggregate, return ledger
- **cash**: safe drop, reconciliation, movement
- **reporting**: read-models, operational metrics
- **sync**: conflict resolution, batching, cloud-hq
- **auth**: role-based access, PIN security
- **integrations**: printer, scanner, platform ports
- **prepared boundaries**: fb (F&B), service (Services)

## Rule
A context appearing in docs does not prove clean runtime ownership.
Always ask:
- who owns the data?
- who owns the invariant?
- does the critical flow still pass through legacy paths?
