---
applyTo: "shared/sync/**, backend/**/sync/**, **/*sync*.*"
---

# Sync Contract Instructions

- Preserve explicit `outbox_event`, `sync_batch`, `sync_item`, `sync_conflict`, `master_data_snapshot`, and `offline_operation_window` concepts.
- Timeout or unknown remote commit must end in visible terminal state.
- Do not infer sync status from logs.
- Financial divergence cannot use last-write-wins.
