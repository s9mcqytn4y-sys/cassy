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
- R3 sudah menambah persistence durable untuk `InventoryApprovalAction`, migration inventory v3, dan FK/integrity proof untuk inventory truth.
- `.github` gate sekarang sinkron dengan inventory migration verify dan desktop smoke task yang nyata.
- R4 foundation sekarang punya dokumen truth terpisah untuk contract, JDK/workspace, dan smoke task map.
- Packaging desktop yang benar-benar dikonfigurasi saat ini adalah Windows `EXE`; repo belum mendeklarasikan `MSI`.
- Source smoke dan distribution smoke keduanya sudah punya jalur yang eksplisit dan tervalidasi ulang pada host Windows turn ini.
- README, AGENTS, CODEX, GEMINI, `.agent`, `.vscode`, dan tracked `.idea` subset telah disegarkan untuk posture desktop-first JDK 17 dan truth R3.

## ASSUMPTION
- V1 tetap single-outlet, terminal-bound, dan local-first.
- Reporting penuh, sync runtime penuh, dan expansion Android tetap di luar slice R2 ini.

## INTERPRETATION
- Repo truth saat ini konsisten dengan posture "guided operations + cashier core + inventory basic + operational guardrails".
- R3 inventory truth lite sekarang layak ditutup `DONE` bila matrix final tetap hijau.

## RISK
- Hosted CI remote completion tidak saya klaim dari verifikasi lokal.
- Release evidence Windows foundation sudah lebih eksplisit, tetapi install/uninstall end-user masih belum setara dengan smoke runtime/source yang sudah hijau.

## RECOMMENDATION
- Gunakan dokumen ini sebagai reality snapshot terbaru; dokumen block-level lama tetap dipertahankan sebagai jejak evolusi, bukan diganti total.
