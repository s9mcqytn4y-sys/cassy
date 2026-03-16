# Cassy Roadmap Status Sync

Tanggal: 2026-03-16
Posture: Desktop-first retail operating core
Primary pilot OS: Windows

## Executive reality

- M0: DONE
- M1: DONE
- M2: PARTIAL / FOUNDATION
- M3: PARTIAL / FOUNDATION
- M4: PARTIAL / FOUNDATION
- thin M5: PARTIAL / FOUNDATION
- Highest fully stable milestone: M1
- Highest operationally advanced milestone: thin M5

## False readiness removed

1. Desktop source set pernah false-ready saat source utama berada di `src/jvmMain` tetapi lane build belum benar.
2. M3 dan M4 sempat dianggap selesai hanya karena UI tampil, bukan karena access/day/shift guardrail benar-benar hidup.
3. Debian package pernah terlihat seperti readiness desktop, padahal pilot utama adalah Windows.
4. Desktop run sempat bocor ke Java 21 dan crash di Skiko; lane ini sekarang dipaksa ke JDK 17 only.
5. Hosted `PR Gate #15` pada 2026-03-16 gagal di lane lama; repo sekarang sudah dipecah ke lane `PR Gate`, `Mainline Evidence`, `Nightly Integrity`, dan `Release Evidence`, tetapi hosted rerun baru masih perlu dibuktikan.
6. Hosted `Mainline Evidence` untuk commit `3522efd` juga gagal pada step `Smoke Run Desktop Source`; lane hosted Windows belum bisa disebut stabil.

## Current evidence

- `.\gradlew --version` menunjukkan launcher JVM 17.0.12 dan daemon criteria Java 17.
- `.\gradlew clean` sukses.
- `.\gradlew build` sukses.
- `.\gradlew test` sukses.
- `.\gradlew detekt` sukses.
- `.\gradlew :apps:android-pos:lintDebug` sukses.
- `.\gradlew :apps:desktop-pos:smokeRun` sukses dan mencetak `CASSY_SMOKE_OK stage=Bootstrap`.
- `.\gradlew :apps:desktop-pos:run --args="--smoke-run"` sukses dan mencetak `CASSY_SMOKE_OK stage=Bootstrap`.
- `.\gradlew :apps:desktop-pos:createDistributable` sukses.
- `.\gradlew :apps:desktop-pos:packageDistributionForCurrentOS` sukses dan menghasilkan `apps/desktop-pos/build/compose/binaries/main/exe/Cassy-0.1.0.exe`.
- `powershell -ExecutionPolicy Bypass -File tooling/scripts/Invoke-DesktopDistributionSmoke.ps1` sukses dan memverifikasi distribution runtime.
- SQLDelight migration verification Windows sukses setelah worker migration diberi initializer sqlite khusus.
- Mutasi stok dari checkout baseline sekarang melewati `shared:inventory:InventoryService`, bukan langsung dari `SalesService` ke repository stock.

## Remaining gaps

- Hosted Windows CI execution evidence baru belum stabil; failure terbaru ada pada `Mainline Evidence` commit `3522efd`.
- Installer smoke install/uninstall Windows belum tervalidasi end-to-end.
- `:shared` masih menjadi legacy bridge yang harus terus disusutkan.
- Checkout final, payment final, receipt final, reporting dasar, sync visibility, dan migration replay belum bisa diklaim done.

## Release truth

- Windows local package evidence: ada
- Windows hosted CI evidence: pending verification
- Windows installer install/uninstall smoke: manual pending
- Debian package: compatibility only, bukan release truth pilot Windows

## Regeneration

- Source markdown ini: `docs/execution/roadmap_pdf_source_2026_03_16.md`
- Output PDF repo-local: `output/pdf/cassy_roadmap_status_sync_2026_03_16.pdf`
- Generator: `tooling/scripts/generate_roadmap_status_pdf.py`
