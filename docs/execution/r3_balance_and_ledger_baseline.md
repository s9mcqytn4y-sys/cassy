# R3 Balance And Ledger Baseline

Updated: 2026-03-19

## FACT
- `InventoryBalance`
  - `productId`
  - `quantity`
  - `rotationPolicy`
  - `lastLedgerEntryId`
  - `lastUpdatedAt`
- `StockLedgerEntry`
  - `productId`
  - `quantityDelta`
  - `mutationType`
  - `sourceType`
  - `sourceId`
  - `sourceLineId`
  - `reasonCode`
  - `reasonDetail`
  - `actorId`
  - `terminalId`
  - `status`
  - `createdAt`
- `InventoryDiscrepancyReview`
  - menyimpan evidence count vs book qty, variance, approval mode, resolution note, dan related ledger entry.
- `InventoryLayer`
  - dipakai untuk FIFO/FEFO baseline.

## FACT
- Positive mutation membuat layer baru.
- Negative mutation mengonsumsi layer aktif.
- Untuk item non-expiry, fallback rotation sekarang FIFO.
- Untuk mode FEFO, query ordering expiry sudah disiapkan.
- Negative legacy balance saat migrasi tidak di-overwrite diam-diam; ia masuk review/investigation path.

## ASSUMPTION
- `InventoryLayer` saat ini cukup sebagai fondasi traceability kuantitas, belum untuk breadth lot/serial penuh.

## INTERPRETATION
- `inventory_balance` menjawab “berapa stok sekarang”.
- `stock_ledger_entry` menjawab “kenapa stok berubah”.
- `InventoryDiscrepancyReview` menjawab “apa yang masih belum final / masih harus ditinjau”.

## RISK
- Balance nol tanpa row eksplisit masih bisa terjadi untuk produk yang baru dihitung tetapi belum punya mutasi final; desktop harus menampilkan ini apa adanya.
- FEFO penuh butuh metadata expiry/lot di phase berikutnya.

## RECOMMENDATION
- Pertahankan count sebagai evidence dulu, adjustment sebagai langkah terpisah.
- Jangan backfill mismatch dengan overwrite senyap; pakai discrepancy review.
