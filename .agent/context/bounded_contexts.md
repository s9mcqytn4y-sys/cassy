# Cassy Bounded Contexts

## Active / visible now
- **kernel**: infrastructure, outbox, audit, day/shift, readiness, approval policy, reason capture
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

## R2 Note
- Untuk Block 1, approval/readiness/opening cash policy tetap ditahan di `kernel`.
- Jangan memindahkan concern ini kembali ke `:shared` legacy bridge.

## R3 Note
- `inventory` adalah owner tunggal untuk `inventory_balance`, `stock_ledger_entry`, discrepancy review, dan approval-aware stock action payload.
- `sales` hanya meminta efek stok lewat boundary inventory.
