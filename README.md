# Cassy

Desktop-first retail operating core untuk single outlet. Fokus V1 saat ini adalah foundation cashier yang jujur: access gate, business day, shift, catalog, cart, dan pricing baseline.

## Repo truth

- Primary release lane: desktop Windows
- Android: parity/business-semantics lane
- Desktop JDK policy: 17 only
- Local/IDE default: configuration cache off
- CI posture: configuration cache dipakai selektif per command, bukan dipaksa di semua lane
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
- `shared:inventory`: stock ownership baseline untuk ledger dan balance
- `shared`: legacy bridge yang sedang disusutkan

## Current foundation flow

1. bootstrap store dan terminal
2. login dengan PIN baseline
3. open business day
4. start shift dengan opening cash
5. browse/search catalog
6. mutate cart dengan pricing baseline

Checkout penuh, payment state final, dan receipt final masih di luar closure foundation ini.

## Operational ownership

- access/day/shift guardrail hidup di `shared:kernel`
- catalog/search/barcode contract hidup di `shared:masterdata`
- cart/pricing baseline hidup di `shared:sales`
- mutasi stok dari checkout baseline sekarang masuk lewat `shared:inventory:InventoryService`, bukan ditulis liar langsung dari sales

## CI topology truth

- `PR Gate`: fast verification untuk `pull_request`
- `Mainline Evidence`: packaging Windows/Linux dan artifact evidence untuk `push` ke `main`
- `Mainline Evidence`: packaging Windows dan artifact evidence untuk `push` ke `main`
- `Nightly Integrity`: build + migration/integrity subset terjadwal
- `Release Evidence`: manifest/manual evidence lane yang dipicu manual

## Verification quick start

Untuk evidence build/test/package yang dipakai repo saat ini:

```powershell
.\gradlew :apps:desktop-pos:smokeRun
.\gradlew :apps:desktop-pos:run --args="--smoke-run"
.\gradlew --version
.\gradlew clean
.\gradlew build
.\gradlew test
.\gradlew detekt
.\gradlew :apps:android-pos:lintDebug
.\tooling\scripts\Invoke-DesktopDistributionSmoke.ps1
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
- `docs/execution/ci_topology_truth.md`
- `docs/execution/windows_installer_smoke_checklist.md`
