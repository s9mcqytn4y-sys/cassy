# R2 Close Shift And Close Day

Updated: 2026-03-19

## FACT
- `shared:kernel` sekarang memiliki `ShiftClosingService` dan `ShiftCloseReport` durable.
- Review close shift menghitung:
  - opening cash
  - total cash sales per shift dari `shared:sales`
  - total `cash in`
  - total `cash out`
  - total `safe drop`
  - expected cash
  - actual cash
  - variance
  - pending transaction count
- Close shift diblokir jika masih ada transaksi `PENDING` pada shift yang sama.
- Variance policy saat ini:
  - tolerance baseline: `Rp 20.000`
  - perlu approval di atas: `Rp 100.000`
  - hard stop investigasi: `Rp 500.000`
- Jika variance di atas approval threshold:
  - request approval durable dibuat
  - supervisor/owner dapat approve atau deny
  - report penutupan tetap menjadi artefak final saat close shift sukses
- Close business day kini memakai readiness review:
  - gagal jika masih ada open shift
  - gagal jika masih ada approval operasional `REQUESTED`

## ASSUMPTION
- Baseline produk saat ini tetap single-outlet desktop-first; close day dibuktikan terhadap open shift yang terdaftar di business day aktif.

## INTERPRETATION
- Repo sekarang punya prosedur close shift/day yang fail-closed, bukan tombol penutupan longgar.
- Reconciliation report sudah cukup untuk baseline operasional outlet tunggal, walau belum menjadi reporting suite penuh.

## RISK
- Reporting penutupan masih berupa baseline data report, belum PDF/export formal.
- Approval close shift masih memakai operator aktif sebagai approver, belum two-person ceremony.

## RECOMMENDATION
- Jika nanti dibutuhkan export resmi, jadikan `ShiftCloseReport` sebagai source-of-truth, bukan render ulang dari UI state.
