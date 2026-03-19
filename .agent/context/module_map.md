# Cassy Module Map

## Runtime-visible topology

- `apps/android-pos`: Android parity lane
- `apps/desktop-pos`: desktop cashier shell, control tower, dan primary release lane
- `shared`: legacy aggregator/DI bridge yang terus disusutkan
- `shared:kernel`: access, terminal binding, business day, shift, approval/readiness, audit kernel
- `shared:masterdata`: product catalog, lookup, barcode contract
- `shared:sales`: cart state, checkout finality, payment state, receipt snapshot, readback source
- `shared:inventory`: stock ownership baseline, ledger/balance writer, inventory application boundary

## Target-state direction

- native app shell per platform
- shared domain/application/data per bounded context
- access/day/shift/pricing guardrail hidup di shared layer
- `:shared` aggregator disusutkan bertahap, bukan dijadikan desain akhir

## Practical interpretation

- Desktop adalah release lane utama V1.
- Android mengikuti business semantics, bukan memimpin scope.
- Inventory tetap owner mutasi stok; sales tidak boleh menulis ledger/balance langsung.
- Finality transaksi R1/M6 hidup di `shared:sales`; `apps:desktop-pos` hanya menjadi execution lane utama.
- Control tower operasional R2 Block 1 hidup di `shared:kernel` dan dipresentasikan oleh desktop.
