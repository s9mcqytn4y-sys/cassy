# R2 Cash Control And Approval

Updated: 2026-03-19

## FACT
- `shared:kernel` sekarang menyimpan `ReasonCode`, `ApprovalRequest`, dan `CashMovement` secara durable di `KernelDatabase`.
- Flow yang hidup:
  - `Cash In`
  - `Cash Out`
  - `Safe Drop`
- Semua flow kontrol kas mensyaratkan:
  - business day aktif
  - shift aktif pada terminal
  - reason code valid dari katalog reason di kernel
- Threshold policy saat ini:
  - `Cash In` kasir tanpa approval: sampai `Rp 500.000`
  - `Cash Out` kasir tanpa approval: sampai `Rp 500.000`
  - `Safe Drop` kasir tanpa approval: sampai `Rp 1.000.000`
  - hard limit nominal: `Rp 10.000.000`
- Di atas threshold, request approval disimpan dengan status `REQUESTED`, lalu supervisor/owner dapat `APPROVE` atau `DENY`.
- Approval denial tidak menulis `CashMovement`.
- Approval acceptance menulis `CashMovement` yang terhubung ke `approvalRequestId`.
- Audit/outbox intent dicatat untuk approval request dan cash movement recorded.

## INTERPRETATION
- Approval lane saat ini adalah light approval yang jujur:
  - operator aktif supervisor/owner menjadi approver
  - tidak ada re-auth PIN kedua khusus approval
  - durability request/decision sudah ada
- Reason capture kini bukan sekadar text field; ada katalog reason code dasar dan catatan bebas opsional.

## RISK
- Reason catalog masih baseline dan belum dikelola dari masterdata/admin UI.
- Approval masih berbasis session operator aktif; belum ada dual-control yang lebih keras.

## RECOMMENDATION
- Jika scope berikutnya membuka void execution, gunakan `ApprovalRequest` yang sama sebagai pola cross-cutting, bukan membuat lane approval baru di UI bridge.
