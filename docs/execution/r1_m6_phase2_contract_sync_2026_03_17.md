# R1 / M6 Phase 2 Contract Sync

Tanggal: 2026-03-17  
Status: Contract implemented dan diverifikasi lokal

## Before -> After
- Sebelum: `SalesService.checkout()` mengembalikan `Result<String>` dan payment state masih `String`.
- Sesudah: `SalesService.checkout()` mengembalikan `Result<SaleCompletionResult>` dengan `CompletedSaleReadback` dan `ReceiptPrintState`.
- Sebelum: `Payment.status` dan `ReceiptPaymentSnapshot.status` adalah string tipis tanpa detail minimum.
- Sesudah: payment memakai `PaymentState(status, detailCode, detailMessage)` dengan enum `PaymentStatus` dan `PaymentStatusDetailCode`.
- Sebelum: readback final memakai `FinalizedSale` dengan field receipt generik.
- Sesudah: readback final eksplisit sebagai `CompletedSaleReadback` dan source final tetap `ReceiptSnapshotDocument`.
- Sebelum: print preview direpresentasikan sebagai `String` langsung.
- Sesudah: print memakai `ReceiptPrintPayload` dengan `renderedContent + printState`, terpisah dari validity settlement.

## Contract aktual
- Payment status typed: `PENDING | SUCCESS | FAILED | CANCELLED`
- Pending/failed wajib punya `detailCode`
- Finalize result typed: `SaleCompletionResult`
- Final source of truth: `ReceiptSnapshotDocument`
- Final readback: `CompletedSaleReadback`
- Print contract terpisah: `ReceiptPrintPayload` + `ReceiptPrintState`

## Persisted change
- `SalePayment` sekarang menyimpan:
  - `status`
  - `statusReasonCode`
  - `statusDetailMessage`
- Migration SQLDelight: [2.sqm](/c:/Users/Acer/AndroidStudioProjects/Cassy/shared/sales/src/commonMain/sqldelight/id/azureenterprise/cassy/sales/db/2.sqm)

## Batas yang tetap dijaga
- Tidak ada ownership baru di `:shared`
- Desktop tetap lane utama
- Android hanya dijaga parity compile/workflow
- Print outcome tidak menentukan validitas settlement
