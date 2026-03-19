# R3 Inventory Truth Contract

Updated: 2026-03-19

## Goal
Mengunci slice fondasi inventory truth Cassy V1 agar mutasi stok, readback, dan discrepancy tidak lagi kabur antar module.

## FACT
- Owner mutasi stok final ada di `shared:inventory`.
- `shared:sales` hanya mengirim intent finalization ke `InventoryService.recordSaleCompletion(...)`.
- `inventory_balance` dipakai sebagai current-state truth.
- `stock_ledger_entry` dipakai sebagai explanation trail kanonik.
- Mutasi stok final sekarang membawa `sourceType`, `sourceId`, dan `sourceLineId` bila relevan.
- Stock count sekarang menghasilkan `InventoryDiscrepancyReview`; count tidak auto-adjust.
- Manual adjustment sekarang butuh reason code kategori `INVENTORY_ADJUSTMENT`.
- Review discrepancy bisa di-resolve eksplisit atau ditandai `INVESTIGATION_REQUIRED`.
- Void/reversal inventory masih contract-only; ambiguous path diblok, bukan dieksekusi diam-diam.
- Desktop sekarang punya dialog inventory untuk current state, explanation trail, count, adjustment, dan review queue.

## ASSUMPTION
- Repo tetap single-outlet, desktop-first, local-first.
- Inventory layer sekarang cukup untuk FIFO baseline, belum untuk warehouse allocation breadth.
- Product image masih compatibility-first lewat `imageUrl`, dengan appendix local I/O lewat folder `input_images`.

## INTERPRETATION
- R3 Block 1 ini bukan receiving suite penuh, bukan return engine, dan bukan product master suite penuh.
- Fondasi ini cukup untuk menjaga agar sales bukan lagi owner stok tersembunyi, dan untuk membuat investigasi stok bisa dibaca jujur.

## RISK
- FEFO baru berupa policy hook dan query path; expiry/lot UI belum dibuka.
- Product/image boundary belum menjadi DAM penuh; `input_images` baru dipakai sebagai local image I/O appendix, bukan storage system baru.
- Return/refund fisik masih di luar scope executable block ini.

## RECOMMENDATION
- Pertahankan semua mutasi stok final lewat `InventoryService`.
- Jika phase berikutnya membuka receiving/return, gunakan `sourceType/sourceId/sourceLineId` yang sudah ada, jangan bikin jalur stok ad hoc baru.
- Jangan gabungkan `inventory_balance` dan `stock_ledger_entry` ke satu model convenience.
