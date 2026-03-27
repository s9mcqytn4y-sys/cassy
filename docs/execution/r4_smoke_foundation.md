# R4 Smoke Foundation

Updated: 2026-03-26

Dokumen ini mencatat task map packaging dan smoke path desktop yang benar-benar ada dan dieksekusi pada turn ini.

## Packaging Task Map

## FACT
- Dari `.\gradlew :apps:desktop-pos:tasks --all`, task Compose desktop yang relevan adalah:
  - `smokeRun`
  - `run`
  - `runDistributable`
  - `createDistributable`
  - `packageDistributionForCurrentOS`
  - `packageExe`
  - `packageMsi`
  - `createRuntimeImage`
  - `checkRuntime`
- [apps/desktop-pos/build.gradle.kts](c:/Users/Acer/AndroidStudioProjects/Cassy/apps/desktop-pos/build.gradle.kts) menargetkan `TargetFormat.Exe` dan `TargetFormat.Msi`.
- Artifact hasil packaging lokal turn ini:
  - app image: `apps/desktop-pos/build/compose/binaries/main/app/Cassy/`
  - installer EXE: `apps/desktop-pos/build/compose/binaries/main/exe/Cassy-0.1.0.exe`
  - installer MSI: `apps/desktop-pos/build/compose/binaries/main/msi/Cassy-0.1.0.msi`

## Smoke Paths

- Source smoke task:

```powershell
.\gradlew :apps:desktop-pos:smokeRun
```

- Headless run-task smoke:

```powershell
.\gradlew :apps:desktop-pos:run --args="--smoke-run"
```

- Distribution smoke:

```powershell
powershell -ExecutionPolicy Bypass -File tooling/scripts/Invoke-DesktopDistributionSmoke.ps1
```

- Installer evidence:

```powershell
powershell -ExecutionPolicy Bypass -File tooling/scripts/Invoke-WindowsInstallerEvidence.ps1
```

## Smoke Evidence From This Turn

- [FACT] `:apps:desktop-pos:smokeRun` sukses dan mencetak `CASSY_SMOKE_OK stage=Login`
- [FACT] `:apps:desktop-pos:createDistributable :apps:desktop-pos:packageExe :apps:desktop-pos:packageMsi` sukses
- [FACT] distribution smoke sukses dan memverifikasi marker `CASSY_SMOKE_OK stage=Login`
- [FACT] installer evidence sukses dan mencetak `CASSY_INSTALLER_EVIDENCE_OK path=...`

## Windows Host Constraints

- [FACT] Packaging EXE/MSI yang dibuktikan pada turn ini bergantung pada host Windows.
- [FACT] WiX tooling tetap host-sensitive dan bisa perlu diunduh pada packaging pertama.
- [FACT] `clean` bisa gagal bila artifact EXE/MSI masih di-lock proses installer/app; ini adalah constraint Windows operasional, bukan bug kontrak R4 foundation.
- [FACT] Distribution smoke memakai classpath distribusi dari `app/Cassy.cfg` dan fallback ke `JAVA_HOME` JDK 17 bila launcher runtime tidak tersedia.
- [FACT] Installer smoke memakai marker file karena launcher GUI Windows tidak selalu memberi exit code CLI yang stabil.

## RECOMMENDATION
- Jalankan lane smoke/package secara serial, bukan paralel, untuk menghindari kontensi jar/build artifact pada host lokal.
- Pertahankan urutan verifikasi: smoke source -> `--version` -> clean/build/test/lint -> package -> distribution smoke -> installer evidence.
> Historical Snapshot: dokumen ini adalah catatan fase lama. Untuk truth aktif, gunakan `roadmap_bridge.md`, `repo_reality_sync.md`, dan checklist aktif.
