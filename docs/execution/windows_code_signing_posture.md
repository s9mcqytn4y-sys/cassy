# Windows Code Signing Posture

Updated: 2026-03-27

## FACT
- Repo saat ini belum melakukan code signing binary Windows secara nyata.
- Posture yang dibangun pada fase ini adalah `signing-ready`, bukan `signed-and-published`.
- SmartScreen/reputation Windows untuk distribusi publik tetap akan lebih baik jika binary resmi sudah ditandatangani dan dipublikasikan secara konsisten.

## Signing-ready baseline
- versioning release dan package sudah eksplisit
- artifact EXE/MSI punya metadata release yang konsisten
- release checklist dan manifest mencatat status signing sebagai `NOT_CONFIGURED` sampai sertifikat resmi tersedia
- workflow beta/release dipersiapkan untuk menerima lane signing berikutnya tanpa mengubah arsitektur packaging

## What is still missing
- code signing certificate yang sah
- secret/credential signing yang aman
- step signing di workflow release
- evidence publish signed binary di channel distribusi resmi

## Recommendation
- jangan klaim SmartScreen maturity penuh sebelum lane signing benar-benar hidup
- setelah sertifikat tersedia, tambahkan signing pada workflow beta/release, bukan pada local dev flow
