# R3 Void Impact Contract

Updated: 2026-03-19

## Goal
Mencegah jalur `void` atau `reversal` yang samar merusak inventory truth sebelum engine return/refund penuh benar-benar dibuka.

## FACT
- Contract klasifikasi inventory effect yang shipped:
  - `PRE_SETTLEMENT_VOID_NO_STOCK_EFFECT`
  - `POST_SETTLEMENT_REVERSAL_CANDIDATE`
  - `RETURN_REQUIRED`
  - `MANUAL_INVESTIGATION_REQUIRED`
- Semua klasifikasi di Block 1 ini masih `blocksInventoryMutation = true`.
- `PRE_SETTLEMENT_VOID_NO_STOCK_EFFECT`
  - void sebelum settlement tidak boleh memutasi stok final.
- `POST_SETTLEMENT_REVERSAL_CANDIDATE`
  - reversal pasca-settlement perlu contract/fase terpisah, belum dieksekusi otomatis.
- `RETURN_REQUIRED`
  - stok kembali hanya boleh lewat flow return terpisah bila barang sudah keluar.
- `MANUAL_INVESTIGATION_REQUIRED`
  - jika dampak inventory tidak bisa dibuktikan aman, status resmi adalah investigasi manual.

## FACT
- Inventory mutation final sekarang menuntut source semantics eksplisit.
- Tidak ada jalur vague `void` yang langsung menulis balance/ledger di R3 Block 1.

## ASSUMPTION
- Future phase R5/R6/R7/R8 akan memakai contract ini sebagai map awal untuk return/refund/reversal yang lebih lengkap.

## RISK
- Karena full void engine memang belum shipped, operator masih membutuhkan CTA investigasi/manual follow-up untuk kasus pasca-settlement.
- Approval limitation tetap `LIGHT_PIN`; `SECOND_PIN` dan `DUAL_AUTH` belum boleh diklaim.

## RECOMMENDATION
- Pertahankan blocker keras untuk void inventory effect yang ambiguous.
- Saat future phase membuka reversal/return, wajib tetap membawa `sourceType`, `sourceId`, dan reason eksplisit.
