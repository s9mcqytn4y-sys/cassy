# R5 Reporting Export Contract

Updated: 2026-03-27

## FACT
- Export reporting berjalan di lane desktop melalui `DesktopReportingExporter`.
- Output default adalah bundle folder yang berisi:
  - `daily-summary.csv`
  - `shift-summary.csv`
  - `operational-issues.csv`
  - `README.html`
- Export selalu membaca `OperationsState.reportingSummary` dan `OperationsState.reportingShiftSummary` yang berasal dari `ReportingQueryFacade`.
- Naming folder export mengikuti pola `cassy-report-<timestamp>-<store>-<terminal>`.
- Root export default adalah `Documents/Cassy/exports` bila folder `Documents` tersedia; fallback ke home directory user bila tidak ada.

## ASSUMPTION
- Bundle export ini dipakai untuk review owner/supervisor dan release evidence operasional ringan.
- HTML overview adalah convenience output untuk dibaca manusia, bukan sumber truth baru.

## RULES
- Export tidak boleh melakukan query alternatif yang menghasilkan angka berbeda dari reporting dialog.
- Export tidak boleh menyembunyikan failed sync atau pending issue.
- Shift summary boleh kosong bila snapshot hari itu belum punya shift yang relevan; daily summary tetap wajib keluar.
- Format CSV dijaga stabil agar bisa dipakai audit/manual review tanpa transformasi tambahan.

## RECOMMENDATION
- Jika nanti dibutuhkan PDF, bangun dari snapshot bundle yang sama agar tidak memecah truth source.
