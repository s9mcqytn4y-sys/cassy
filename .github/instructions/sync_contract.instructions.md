# Sync Contract Instructions

Use for sync/outbox/reconciliation work.

## Rules
- sync is explicit, visible, durable, conflict-aware
- do not treat sync as magical hidden retry
- preserve auditability and terminal-bound operational ownership
