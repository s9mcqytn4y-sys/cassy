# Repo Reality Sync

Updated: 2026-03-19

## FACT
- Repo saat ini adalah desktop-first retail operating core untuk single outlet.
- `apps:desktop-pos` adalah lane operasional utama; Android tetap parity/business-semantics lane.
- Ownership concern operasional aktif berada di:
  - `shared:kernel` untuk business day, shift, approval, reason, audit, readiness, close review
  - `shared:sales` untuk finality transaksi dan read model summary yang dibutuhkan closing
  - `shared:inventory` untuk stock mutation truth
- `:shared` tetap ada sebagai legacy aggregator, tetapi concern operasional baru tidak ditambahkan ke sana.
- R2 sudah menambah persistence durable untuk `ReasonCode`, `ApprovalRequest`, `CashMovement`, dan `ShiftCloseReport`.
- Kernel migration untuk schema operasional sudah diverifikasi ulang lewat test desktop khusus migrasi.

## ASSUMPTION
- V1 tetap single-outlet, terminal-bound, dan local-first.
- Reporting penuh, sync runtime penuh, dan expansion Android tetap di luar slice R2 ini.

## INTERPRETATION
- Repo truth saat ini konsisten dengan posture "guided operations + cashier core + inventory basic + operational guardrails".
- Repo belum jujur disebut "full operational suite done" selama void execution dan release evidence installer belum ditutup.

## RISK
- Hosted CI remote completion tidak saya klaim dari verifikasi lokal.
- Release evidence Windows masih belum setara dengan runtime/source smoke yang sudah hijau.

## RECOMMENDATION
- Gunakan dokumen ini sebagai reality snapshot terbaru; dokumen block-level lama tetap dipertahankan sebagai jejak evolusi, bukan diganti total.
