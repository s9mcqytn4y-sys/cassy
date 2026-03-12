# Bounded Contexts

## Shared Kernel
Owns reason codes, approval decisions, audit log semantics, lightweight refs, and offline backlog support.
Forbidden shortcut: becoming a dumping ground for unrelated helpers or DTOs.

## Master Data
Owns store, terminal, employee, product, pricing policy, and snapshot refresh semantics.
Forbidden shortcut: mutating product/pricing state ad hoc from checkout UI.

## Sales
Owns sale aggregate, sale lines, payments, receipt, and suspended sale.
Invariant: completed sale requires valid payment state and zero outstanding amount.
Forbidden shortcut: direct stock mutation.

## Returns
Owns return transaction, return lines, policy decision, refund linkage, and store credit outcome.
Invariant: refund may not exceed policy and purchase history.
Forbidden shortcut: bypassing approval when policy demands it.

## Cash
Owns business day, cashier shift/session, opening cash, cash movement, safe drop, reconciliation, and shift report.
Invariant: one active cashier-terminal shift at a time; close requires reconciliation.
Forbidden shortcut: cash control outside shift context.

## Inventory
Owns inventory balance, stock ledger, receiving, transfer, adjustment, cycle count, damage disposition, and label jobs.
Invariant: every stock change must have a source event and ledger entry.
Forbidden shortcut: product.stock_qty style ad hoc truth.

## Reporting
Owns operational report snapshots and query facades.
Invariant: reports derive from validated state and events, not ad hoc counters.

## Sync
Owns outbox participation, batch/item/conflict model, offline windows, master snapshots, and reconciliation visibility.
Invariant: conflict and failure require explicit terminal state.
Forbidden shortcut: log-only sync observability.

## Auth
Owns terminal-scoped session rules, cached grants, local credential policy, and approval auth semantics.
Forbidden shortcut: role checks only in UI.

## Integrations
Owns HQ API, payment provider adapters, identity integration, printer integration, and external boundary translation.
Forbidden shortcut: leaking provider DTOs into domain logic.
