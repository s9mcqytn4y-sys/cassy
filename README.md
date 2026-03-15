# Cassy

Desktop-first retail operating core untuk single outlet. Fokus V1 saat ini adalah foundation cashier yang jujur: access gate, business day, shift, catalog, cart, dan pricing baseline.

## Repo truth

- Primary release lane: desktop Windows
- Android: parity/business-semantics lane
- Desktop JDK policy: 17 only
- Local/IDE default: configuration cache off
- CI default: configuration cache on per command
- Shared scope: domain, application, data
- Native scope: app shell, lifecycle, OS integration, printer/scanner/device-heavy concern

Status milestone aktif tidak boleh diambil dari UI semata. Gunakan:
- `docs/execution/roadmap_bridge.md`
- `docs/execution/windows_desktop_runbook.md`
- `.agent/plan.md`

## Module map

- `apps/desktop-pos`: desktop cashier shell
- `apps/android-pos`: Android parity lane
- `shared:kernel`: access, terminal binding, business day, shift
- `shared:masterdata`: catalog, search, barcode lookup
- `shared:sales`: cart dan pricing baseline
- `shared:inventory`: stock repository baseline
- `shared`: legacy bridge yang sedang disusutkan

## Current foundation flow

1. bootstrap store dan terminal
2. login dengan PIN baseline
3. open business day
4. start shift dengan opening cash
5. browse/search catalog
6. mutate cart dengan pricing baseline

Checkout penuh, payment state final, dan receipt final masih di luar closure foundation ini.

## Verification quick start

Untuk evidence build/test/package yang dipakai repo saat ini:

```powershell
.\gradlew :apps:desktop-pos:smokeRun
.\gradlew --version
.\gradlew clean
.\gradlew build
.\gradlew test
.\gradlew detekt
.\gradlew :apps:android-pos:lintDebug
.\gradlew :apps:desktop-pos:packageDistributionForCurrentOS
```

Catatan:
- Pastikan `JAVA_HOME` menunjuk ke JDK 17 sebelum run desktop.
- Jalankan packaging Windows terakhir karena file lock Windows bisa mengganggu `clean`.
- Jika Anda sedang melakukan sync/import IDE, biarkan `configuration-cache` tetap off secara default untuk local/dev path.
- Artifact EXE lokal saat ini ada di `apps/desktop-pos/build/compose/binaries/main/exe/`.
- App distribution folder untuk smoke runtime ada di `apps/desktop-pos/build/compose/binaries/main/app/Cassy/`.
- Smoke distribution terotomasi memakai `tooling/scripts/Invoke-DesktopDistributionSmoke.ps1` agar runtime image Windows bisa diverifikasi tanpa bergantung pada launcher GUI `Cassy.exe`.

## Docs entry

- `AGENTS.md`
- `CODEX.md`
- `README_INSTALLATION.md`
- `docs/execution/roadmap_bridge.md`
- `docs/execution/windows_desktop_runbook.md`
- `docs/execution/workspace_jdk_guide.md`
