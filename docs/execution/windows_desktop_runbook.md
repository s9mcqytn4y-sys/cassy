# Windows Desktop Runbook

Dokumen ini hanya mencatat jalur yang benar-benar relevan untuk pilot Windows dan evidence yang bisa diverifikasi di repo.

## Prasyarat lokal

- Windows
- JDK 17
- Gradle wrapper repo
- koneksi internet saat packaging pertama jika Compose perlu mengunduh tooling WiX
- `JAVA_HOME` harus menunjuk ke JDK 17
- configuration cache default local adalah off; CI menyalakannya per command

## Development run

```powershell
.\gradlew :apps:desktop-pos:run
.\gradlew :apps:desktop-pos:smokeRun
.\gradlew :apps:desktop-pos:run --args="--smoke-run"
```

Expected smoke marker:
- `CASSY_SMOKE_OK scenario=basic ...` atau `CASSY_SMOKE_OK scenario=beta ...`

Yang harus terlihat dari flow foundation:
- bootstrap store/terminal bila context belum ada
- login operator dengan PIN
- open business day
- start shift dengan opening cash
- catalog dan cart baseline

## Verification matrix

Gunakan urutan ini:

```powershell
.\gradlew :apps:desktop-pos:smokeRun
.\gradlew :apps:desktop-pos:run --args="--smoke-run"
.\gradlew --version
.\gradlew clean
.\gradlew build
.\gradlew test
.\gradlew detekt
.\gradlew :apps:android-pos:lintDebug
.\gradlew :apps:desktop-pos:createDistributable :apps:desktop-pos:packageExe :apps:desktop-pos:packageMsi
.\tooling\scripts\Invoke-DesktopDistributionSmoke.ps1
.\tooling\scripts\Invoke-WindowsInstallerEvidence.ps1
.\tooling\scripts\Invoke-WindowsUpgradeEvidence.ps1
.\tooling\scripts\Invoke-DesktopPerformanceProbe.ps1
.\tooling\scripts\Collect-WindowsReleaseDiagnostics.ps1
```

Authority gate resmi release candidate sekarang dirangkum di `docs/execution/release_candidate_checklist.md`.

Catatan operasional:
- packaging dijalankan setelah `clean/build/test/lint` karena file lock Windows bisa membuat `clean` gagal bila artifact masih dipakai.
- jika `clean` gagal karena lock, jalankan `.\gradlew --stop` lalu ulangi `clean`.
- sebelum install/update candidate, backup state lokal dengan `.\tooling\scripts\Backup-CassyDesktopState.ps1`.

## Artifact packaging yang sudah terbukti lokal

- Source smoke task: `:apps:desktop-pos:smokeRun`
- Headless run-task smoke: `:apps:desktop-pos:run --args="--smoke-run"`
- Distribution smoke path: `tooling/scripts/Invoke-DesktopDistributionSmoke.ps1`
- Installer evidence path: `tooling/scripts/Invoke-WindowsInstallerEvidence.ps1`
- Diagnostics collector: `tooling/scripts/Collect-WindowsReleaseDiagnostics.ps1`
- State backup baseline: `tooling/scripts/Backup-CassyDesktopState.ps1`
- Upgrade evidence baseline: `tooling/scripts/Invoke-WindowsUpgradeEvidence.ps1`
- Perf probe baseline: `tooling/scripts/Invoke-DesktopPerformanceProbe.ps1`
- Distribution app folder: `apps/desktop-pos/build/compose/binaries/main/app/Cassy/`
- Task: `:apps:desktop-pos:createDistributable :apps:desktop-pos:packageExe :apps:desktop-pos:packageMsi`
- Format lokal terverifikasi: `EXE` dan `MSI`
- Artifact path EXE: `apps/desktop-pos/build/compose/binaries/main/exe/Cassy-<packageVersion>.exe`
- Artifact path MSI: `apps/desktop-pos/build/compose/binaries/main/msi/Cassy-<packageVersion>.msi`
- Embedded runtime evidence: `apps/desktop-pos/build/compose/binaries/main/app/Cassy/runtime/release`
- Installer evidence lokal terbaru selalu berada di `build/installer-evidence/<timestamp>/`.

## Hosted CI dan gap yang masih harus diakui

- Hosted `Mainline Evidence` adalah evidence lane aktif untuk artifact desktop, MSI, installer evidence, diagnostics, dan manifest.
- Workflow `Mainline Evidence` sekarang disiapkan untuk menjalankan `Invoke-WindowsInstallerEvidence.ps1` dan mengunggah `cassy-desktop-msi` + `cassy-installer-evidence`.
- Launcher GUI `Cassy.exe` dari app image tidak selalu memberi output exit code CLI yang stabil, sehingga smoke otomatis memakai marker file sebagai sumber kebenaran.
- Debian package pada Ubuntu hanya compatibility artifact; bukan release truth untuk pilot Windows.
- Diagnostics baseline sekarang mengandalkan `build/release-diagnostics/` + Gradle/Compose reports, bukan klaim adanya app log file khusus yang belum diimplementasikan.

## Recovery baseline

```powershell
.\tooling\scripts\Backup-CassyDesktopState.ps1
```

Recovery minimum yang jujur saat ini adalah restore folder `%USERPROFILE%\.cassy` dari arsip backup terakhir. Ini membantu rollback data lokal, tetapi bukan bukti rollback binary antar-versi.

## Diagnostics baseline

```powershell
.\tooling\scripts\Collect-WindowsReleaseDiagnostics.ps1
```

Minimum evidence setelah smoke/package failure:
- folder output `build/release-diagnostics/<timestamp>/`
- `build/reports/problems/problems-report.html` bila ada
- `apps/desktop-pos/build/compose/logs/` bila ada isi
- file DB `%USERPROFILE%\.cassy\*.db*` bila failure terkait state lokal

## Troubleshooting

- `clean` gagal di Windows:
  - hentikan daemon dengan `.\gradlew --stop`
  - tutup app atau installer yang masih memegang file di `build/`
- Artifact EXE/MSI tidak muncul:
  - pastikan task yang dipakai `:apps:desktop-pos:packageExe` atau `:apps:desktop-pos:packageMsi`
  - cek folder `apps/desktop-pos/build/compose/binaries/main/exe/` atau `.../msi/`
- Build lolos tetapi desktop app tidak ikut terkompilasi:
  - cek `apps/desktop-pos/build.gradle.kts` agar source set desktop diarahkan ke `src/jvmMain`
