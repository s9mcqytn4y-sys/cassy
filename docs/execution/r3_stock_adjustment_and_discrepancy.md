# R3 Stock Adjustment And Discrepancy

Updated: 2026-03-19

## FACT
- Stock adjustment final tetap di-owner oleh `shared:inventory`.
- Adjustment sekarang wajib punya reason code kategori `INVENTORY_ADJUSTMENT`.
- Adjustment high-risk tidak langsung menulis stok bila operator aktif belum punya capability approve.
- Payload approval-aware untuk inventory sekarang durable di `InventoryApprovalAction`.
- `InventoryApprovalAction` disambungkan ke `ApprovalRequest` kernel lewat `approvalRequestId` sebagai cross-db link, bukan FK lintas database.
- `InventoryDiscrepancyReview` tetap menyimpan evidence count vs book qty dan tidak auto-adjust.
- Resolusi discrepancy besar/high-risk sekarang bisa masuk approval queue lebih dulu.
- Penolakan approval tidak menulis `inventory_balance` maupun `stock_ledger_entry`.

## ASSUMPTION
- Scope block ini tetap desktop-first single outlet.
- Approval yang shipped saat ini tetap `LIGHT_PIN`.

## INTERPRETATION
- Adjustment kecil bisa final langsung oleh operator yang memang punya capability.
- Adjustment/discrepancy yang butuh approval sekarang terlihat sebagai queue eksplisit, bukan keputusan sunyi di service atau UI.

## RISK
- `SECOND_PIN` dan `DUAL_AUTH` belum diimplementasikan; keduanya hanya future hook.
- Inventory approval queue belum dibawa ke dashboard operasional R2; saat ini baseline-nya ada di inventory flow desktop.

## RECOMMENDATION
- Pertahankan semua adjustment final lewat `InventoryService`.
- Jangan bypass queue approval inventory dengan write langsung ke tabel balance/ledger.
