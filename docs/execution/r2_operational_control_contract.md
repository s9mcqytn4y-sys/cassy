# R2 Operational Control Contract

Updated: 2026-03-19

## Goal
Mengunci kontrak Block 1 untuk Operational Control desktop-first sebelum meluas ke cash movement, approval lane penuh, atau void execution.

## FACT
- `shared:kernel` sekarang memiliki kontrak readiness/dashboard operasional lewat `OperationalControlService`.
- `shared:kernel` tetap menjadi owner untuk `BusinessDayService`, `ShiftService`, capability gate, approval policy opening cash, audit, dan outbox event untuk keputusan kritis open/close day dan open/close shift.
- `apps:desktop-pos` hanya memantulkan keputusan operasional itu lewat `DesktopAppController`, `OpenDayStage`, `StartShiftStage`, dan panel control tower di lane kasir.
- `shared` legacy kehilangan screen/DI orphan yang sebelumnya menyimpan UI lama `BusinessDayScreen`, `CatalogScreen`, `CatalogViewModel`, `CatalogContract`, dan `CatalogModule`.
- Block 1 belum membuka eksekusi void; dashboard hanya menampilkan status `UNAVAILABLE` dengan alasan eksplisit.

## OWNER BOUNDARY

| Concern | Owner | Notes |
|:--|:--|:--|
| business day lifecycle | `shared:kernel` | open/close + audit/outbox decision |
| shift lifecycle | `shared:kernel` | start/end + duplicate shift prevention |
| opening cash policy | `shared:kernel` | threshold cashier vs supervisor approval |
| approval reason capture semantics | `shared:kernel` | enforced at policy boundary |
| control tower / dashboard presentation | `apps:desktop-pos` | renders kernel snapshot, no business rule ownership |
| checkout, receipt, finality | `shared:sales` | unchanged from R1 |
| stock mutation | `shared:inventory` | unchanged owner boundary |
| void execution resolver | NOT OPENED in Block 1 | status visible, execution not claimed |

## INTERPRETATION
- Block 1 selesai bila desktop dapat memandu operator secara jujur dari login ke open day ke start shift tanpa menyembunyikan blocker atau approval requirement.
- Approval yang hidup saat ini adalah **light approval**: opening cash di luar kebijakan hanya bisa dilanjutkan oleh supervisor/owner dengan alasan tercatat.

## RISK
- Approval belum memakai multi-session supervisor handoff atau PIN re-auth khusus.
- Void resolver belum dieksekusi; hanya readiness/truth state yang ditampilkan.
- Android parity untuk control tower belum dikerjakan di block ini.

## RECOMMENDATION
- Block berikutnya fokus pada `cash in/out`, `close day`, dan approval lane yang lebih eksplisit sebelum membuka void execution penuh.
