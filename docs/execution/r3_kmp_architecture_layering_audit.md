# R3 KMP Architecture Layering Audit

Updated: 2026-03-19

## Goal
Mengunci audit layering KMP setelah R3 agar owner boundary, data access boundary, dan app-shell boundary tidak drift.

## FACT
- `shared:kernel` meng-owner access, business day, shift, approval cross-cutting, reason code, audit, dan outbox intent.
- `shared:masterdata` meng-owner catalog/product lookup.
- `shared:sales` meng-owner basket, checkout finality, payment/readback, dan hanya meminta efek stok ke `shared:inventory`.
- `shared:inventory` meng-owner `inventory_balance`, `stock_ledger_entry`, discrepancy review, inventory layer, dan approval-aware inventory action payload.
- `apps:desktop-pos` hanya memakai service/domain state; tidak memanggil generated SQLDelight queries langsung.
- Pencarian `DatabaseQueries` di `apps/**` tidak menemukan call generated query di app-shell.
- Legacy `:shared` tidak menerima owner inventory baru.

## ASSUMPTION
- Android tetap lane parity semantics, bukan release lane utama.

## INTERPRETATION
- Layering KMP saat ini cukup bersih untuk desktop-first retail core:
  - domain/application tetap di shared contexts
  - persistence tetap di data modules
  - app-shell hanya presentation/orchestration

## RISK
- `:shared` aggregator masih ada sebagai legacy bridge, jadi drift tetap harus diawasi.
- Approval request inventory masih cross-db link string-based karena kernel dan inventory tidak berbagi database.

## RECOMMENDATION
- Pertahankan aturan: UI tidak memanggil query interface langsung, sales tidak menulis stok langsung, dan inventory tetap owner mutasi stok final.
