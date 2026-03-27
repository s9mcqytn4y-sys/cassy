# Beta Release Readiness

Updated: 2026-03-27

## FACT
- Target posture saat ini adalah `Private Beta / Controlled Beta`.
- Scope beta tetap desktop-first, Windows only, single-outlet retail operating core.
- Android tetap parity/business-semantics lane dan bukan owner UX final.
- Gate lokal build/test/lint/migration/package/smoke sudah hijau pada verifikasi terbaru 27 Maret 2026.
- Installer upgrade evidence `baseline -> candidate -> uninstall` sudah hijau secara lokal.
- Perf/resource probe ringan sudah dijalankan secara lokal.

## Required evidence
- release candidate checklist hijau
- burn-in checklist hijau
- support matrix device/peripheral jelas
- legal/privacy/security baseline siap
- changelog dan versioning resmi siap
- installer upgrade evidence tersedia

## Not safe to claim yet
- public GA
- hardware breadth luas
- cloud sync penuh
- code signing reputation penuh
- enterprise compliance breadth

## RECOMMENDATION
- Gunakan status ini sebagai dasar `Private Beta`, bukan `Public Beta`.
- Setelah commit/tag dipush, verifikasi hosted workflow/tag terbaru agar authority lokal dan remote kembali sinkron.
