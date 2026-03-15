# Cassy Module Map

## Runtime-visible topology

- `apps/android-pos`: Android parity lane
- `apps/desktop-pos`: desktop cashier shell dan primary release lane
- `shared`: legacy UI/DI bridge yang masih perlu disusutkan
- `shared:kernel`: access, terminal binding, business day, shift, audit kernel
- `shared:masterdata`: product catalog, lookup, barcode contract
- `shared:sales`: cart state dan pricing baseline
- `shared:inventory`: stock repository baseline

## Target-state direction

- native app shell per platform
- shared domain/application/data per bounded context
- access/day/shift/pricing guardrail hidup di shared layer
- `:shared` aggregator disusutkan bertahap, bukan dijadikan desain akhir

## Practical interpretation

- Desktop adalah release lane utama V1.
- Android mengikuti business semantics, bukan memimpin scope.
- Inventory belum boleh dipromosikan sebagai checkout truth penuh sampai flow finalize sale terbukti.
