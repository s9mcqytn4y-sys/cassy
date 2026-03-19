# R3 Reality Sync

Updated: 2026-03-19

## FACT
- R3 Block 1 sudah menutup:
  - sale -> inventory boundary
  - balance vs ledger baseline
  - void-impact classification contract
  - FIFO baseline
  - discrepancy-first stock count
- R3 Block 2 menutup:
  - approval-aware inventory adjustment
  - discrepancy visibility / review queue baseline
  - inventory migration v3 baseline
  - FK / integrity verification
  - desktop readback separation yang lebih tegas
- Legacy `:shared` tidak menerima ownership inventory baru pada block ini.

## FACT
- Keterbatasan yang tetap jujur:
  - `LIGHT_PIN` only
  - `PDF_NOT_SHIPPED`
  - Windows installer install/uninstall evidence tetap manual-soft-blocker di luar core R3

## INTERPRETATION
- Inventory Truth Lite sekarang sudah lebih operasional dan explainable, tetapi bukan berarti receiving suite, return engine, atau auth breadth penuh sudah shipped.

## RISK
- Future phase yang membuka return/void fisik harus tetap memakai inventory-effect contract yang sudah ada; jangan shortcut.

## RECOMMENDATION
- Pakai dokumen ini sebagai bridge reality setelah rerun verification matrix, bukan sebagai pengganti evidence test/build.
