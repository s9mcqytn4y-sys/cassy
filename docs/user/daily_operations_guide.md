# Daily Operations Guide

Updated: 2026-03-27

## Start of day
- verifikasi terminal yang dipakai memang terminal outlet yang benar
- buka business day hanya sekali per hari operasional
- mulai shift dengan opening cash yang jujur

## During sales
- utamakan scanner keyboard-wedge atau input keyboard
- gunakan reason code yang benar untuk cash movement dan void
- jangan menganggap printer/cash drawer siap jika UI belum menunjukkan adapter/device yang benar-benar hidup

## Void policy V1
- void sale desktop V1 hanya mengeksekusi sale `CASH` yang sudah final
- `CARD` dan `QRIS` tetap butuh reversal/refund eksternal
- dampak stok tidak dibalik otomatis; follow-up fisik/inventory harus dicatat pada note void

## Reporting & export
- gunakan dialog `Ringkasan` untuk membaca kondisi hari ini
- export bundle berisi `daily-summary.csv`, `shift-summary.csv`, `operational-issues.csv`, dan `README.html`
- perlakukan hasil export sebagai data operasional sensitif outlet

## End of shift / end of day
- review pending issue, sync failure, dan blocker operasional
- pastikan cash movement dan void yang terjadi dapat dijelaskan
- tutup shift lalu business day sesuai jalur guided operation

## Recovery basics
- sebelum install/update candidate, jalankan backup state lokal
- jika smoke atau install gagal, kumpulkan diagnostics terlebih dahulu
- restore data lokal hanya dari backup yang dipercaya
