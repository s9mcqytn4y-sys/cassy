# R2 Open Business Day And Shift

Updated: 2026-03-19

## Goal
Menutup slice dasar operasional: open business day, start shift, opening cash policy, readiness state, dan control tower desktop.

## FACT
- `BusinessDayService` sekarang mengevaluasi readiness open day dan mencatat `BUSINESS_DAY_OPENED` / `BUSINESS_DAY_CLOSED` ke audit + outbox event.
- `ShiftService` sekarang mengevaluasi readiness start shift, mencegah duplicate active shift, menerapkan `OpeningCashPolicy`, dan mencatat `SHIFT_OPENED` / `SHIFT_CLOSED`.
- `OperationalControlService` membangun snapshot dashboard berisi:
  - headline operasional
  - primary action
  - blocker kasir
  - status open day
  - status start shift
  - status void
- Desktop stage `OpenDay` dan `StartShift` kini menampilkan control tower card, blocker yang eksplisit, reason field, dan shortcut nominal opening cash.
- Desktop side panel kasir juga menampilkan control tower card agar state readiness tetap terlihat walau operator sudah masuk lane kasir.

## POLICY

| Rule | Current Truth |
|:--|:--|
| business day before shift | wajib |
| shift before sales unlock | wajib |
| duplicate active shift per terminal | ditolak |
| opening cash cashier without approval | sampai `Rp 500.000` |
| hard limit opening cash | `Rp 5.000.000` |
| opening cash di luar kebijakan | supervisor/owner + reason wajib |

## UX NOTES
- Shortcut nominal opening cash tersedia untuk mempercepat input awal.
- Dashboard selalu menunjukkan void sebagai `UNAVAILABLE` dengan alasan yang jujur, bukan tombol palsu.
- `nextActionLabel` tampil di top bar untuk membuat langkah berikutnya lebih jelas.

## TEST EVIDENCE
- `BusinessDayServiceTest`
- `ShiftServiceTest`
- `OperationalControlServiceTest`
- `DesktopAppControllerTest`

## RISK
- Approval reason masih berupa text field sederhana, belum structured reason catalog.
- Shortcut nominal masih fokus pada opening cash, belum ke seluruh flow pembayaran tunai R2.

## RECOMMENDATION
- Block berikutnya menambahkan `cash in/out`, approval replay, dan `close day` readiness dalam kontrak yang sama.
