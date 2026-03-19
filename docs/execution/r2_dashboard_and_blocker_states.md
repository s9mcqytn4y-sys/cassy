# R2 Dashboard And Blocker States

Updated: 2026-03-19

## FACT
- Guided Operations Dashboard tetap menjadi control tower di desktop.
- Dashboard sekarang memantulkan status untuk:
  - open business day
  - start shift
  - cash in
  - cash out
  - safe drop
  - close shift
  - close business day
  - void sale
- Dashboard menampilkan `pendingApprovalCount` dari approval operasional aktif.
- `Void Sale` tetap ditampilkan sebagai `UNAVAILABLE` dengan alasan eksplisit. Resolver execution lintas sales/cashflow/inventory/reporting belum dibuka.
- Lane kasir tidak menghitung truth operasional secara lokal:
  - readiness berasal dari `OperationalControlService`
  - close shift review berasal dari `ShiftClosingService`
  - cash control policy berasal dari `CashControlService`

## STATUS SEMANTICS

| Status | Arti operasional saat ini |
|:--|:--|
| `READY` | aksi boleh dilakukan |
| `BLOCKED` | aksi tidak boleh dilakukan; ada blocker yang harus diselesaikan |
| `REQUIRES_APPROVAL` | operator sekarang tidak boleh commit; supervisor/owner harus memutuskan |
| `COMPLETED` | state target sudah tercapai |
| `UNAVAILABLE` | flow belum dibuka pada block ini |

## CTA TRUTH
- Blocker close shift karena pending transaction: CTA mengarah ke review pending.
- Blocker close day karena open shift: CTA mengarah ke tutup shift aktif.
- Blocker close day karena approval request tersisa: CTA mengarah ke review approval.
- Blocker cash control karena threshold: CTA mengarah ke review approval supervisor.

## RISK
- Dashboard belum punya lane task clustering yang lebih visual per role; saat ini masih decision-list desktop-first.

## RECOMMENDATION
- Pertahankan dashboard sebagai source-of-truth operasional; jangan memindahkan policy branching ke composable atau dialog state.
