# Beta Burn-In Checklist

Updated: 2026-03-27

## Goal
- memastikan candidate beta desktop stabil untuk pilot single-outlet terbatas
- memaksa discipline operasional, bukan sekadar build hijau

## Burn-in loop minimum
- jalankan candidate yang sama selama 2-3 hari rehearsal internal
- ulangi flow:
  - bootstrap terminal baru
  - login operator
  - open business day
  - start shift
  - scan/add product
  - cash checkout
  - void sale cash final
  - export reporting
  - close shift
  - close business day
- ulangi install `baseline -> candidate` minimal sekali
- kumpulkan diagnostics bila ada anomaly

## Exit criteria
- tidak ada failure P0/P1 baru
- tidak ada data corruption lokal
- tidak ada misleading state pada reporting / void / shift close
- smoke source, distribution, installer, upgrade, dan perf probe lulus

## Notes
- breadth hardware tetap mengikuti `desktop_device_support_matrix.md`
- backend sync nyata tetap bukan hard dependency transaksi harian
