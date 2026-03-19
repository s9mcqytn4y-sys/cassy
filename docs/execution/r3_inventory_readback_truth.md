# R3 Inventory Readback Truth

Updated: 2026-03-19

## FACT
- Desktop inventory dialog sekarang memisahkan tiga area truth:
  - current stock state
  - explanation trail
  - discrepancy / needs-approval queue
- `current state` dibaca dari `InventoryBalanceSnapshot`.
- `explanation trail` dibaca dari `StockLedgerEntry`.
- `discrepancy queue` dibaca dari `InventoryDiscrepancyReview`.
- `needs approval` dibaca dari `InventoryApprovalAction`.
- UI menampilkan `sourceType`, `sourceId`, `sourceLineId`, reason, status, dan approval limitation note.
- UI tidak menghitung ulang balance dari ledger; truth tetap datang dari inventory service/repository.

## ASSUMPTION
- Operator inventory utama tetap desktop operator dengan kebutuhan lookup cepat dan tabel padat.

## INTERPRETATION
- Flow readback sekarang lebih jujur untuk audit operasional: operator bisa melihat “stok sekarang”, “kenapa berubah”, dan “apa yang belum final”.

## RISK
- UI masih DIGITAL_ONLY. Tidak ada claim PDF export untuk inventory/readback.
- `input_images` tetap appendix I/O lokal; jika folder kosong, UI harus menyatakannya apa adanya.

## RECOMMENDATION
- Pertahankan severity language yang eksplisit: `Needs Approval`, `Investigasi`, `Defer`, `Ready`, `Warning`.
- Jangan campur queue approval dan explanation trail menjadi satu daftar kabur.
