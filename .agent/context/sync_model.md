# Sync Model

## Principle
Sync is not hidden infrastructure. It is a visible bounded context with business consequences.

## Core records
- `outbox_event`
- `sync_batch`
- `sync_item`
- `sync_conflict`
- `master_data_snapshot`
- `offline_operation_window`

## Rules
- Business decision + audit intent + outbox must commit atomically.
- Batch/item creation is separate from the source transaction, but durable.
- Unknown remote result must end in visible retryable or investigation state.
- Conflict resolution must persist actor/accountability.
- Financial divergence cannot use silent last-write-wins.

## UI semantics
Users must see offline window, pending sync, failed sync, and conflict from dashboard/detail surfaces.
