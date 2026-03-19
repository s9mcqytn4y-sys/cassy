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
- `CASSY_SMOKE_OK stage=Bootstrap` atau stage non-fatal lain yang setara

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
.\gradlew :apps:desktop-pos:createDistributable
.\gradlew :apps:desktop-pos:packageDistributionForCurrentOS
.\tooling\scripts\Invoke-DesktopDistributionSmoke.ps1
.\tooling\scripts\Collect-WindowsReleaseDiagnostics.ps1
```

Catatan operasional:
- `packageDistributionForCurrentOS` dijalankan terakhir karena file lock Windows dapat membuat `clean` gagal jika artifact masih dipakai.
- Jika `clean` gagal karena lock, jalankan `.\gradlew --stop` lalu ulangi `clean`.
- Sebelum install/update candidate, backup state lokal dengan `.\tooling\scripts\Backup-CassyDesktopState.ps1`.

## Artifact packaging yang sudah terbukti lokal

- Source smoke task: `:apps:desktop-pos:smokeRun`
- Headless run-task smoke: `:apps:desktop-pos:run --args="--smoke-run"`
- Distribution smoke path: `tooling/scripts/Invoke-DesktopDistributionSmoke.ps1`
- Diagnostics collector: `tooling/scripts/Collect-WindowsReleaseDiagnostics.ps1`
- State backup baseline: `tooling/scripts/Backup-CassyDesktopState.ps1`
- Distribution app folder: `apps/desktop-pos/build/compose/binaries/main/app/Cassy/`
- Task: `:apps:desktop-pos:packageDistributionForCurrentOS`
- Format lokal terverifikasi: `EXE`
- Artifact path: `apps/desktop-pos/build/compose/binaries/main/exe/Cassy-0.1.0.exe`
- Embedded runtime evidence: `apps/desktop-pos/build/compose/binaries/main/app/Cassy/runtime/release`

## Hosted CI dan gap yang masih harus diakui

- Hosted `Mainline Evidence` run `23142319550` untuk commit `a27ddc7` sukses pada 2026-03-16 dan mengunggah artifact `cassy-desktop-exe`, `cassy-desktop-app`, serta `cassy-mainline-evidence`.
- Smoke installer install/uninstall Windows belum tervalidasi di repo ini; automation yang terbukti baru source smoke dan distribution runtime smoke.
- Launcher GUI `Cassy.exe` dari app image belum memberi output smoke CLI yang stabil di environment lokal ini, sehingga smoke otomatis menggunakan classpath distribusi dari `app/Cassy.cfg` dan fallback ke `JAVA_HOME` JDK 17 saat app image tidak menyertakan `java.exe`.
- Debian package pada Ubuntu hanya compatibility artifact; bukan release truth untuk pilot Windows.
- Diagnostics baseline sekarang mengandalkan `build/release-diagnostics/` + Gradle/Compose reports, bukan klaim adanya app log file khusus yang belum diimplementasikan.

## Manual smoke checklist

1. Jalankan desktop app dari Gradle run.
2. Bootstrap store, terminal, cashier, supervisor.
3. Login supervisor.
4. Open business day.
5. Start shift dengan nominal opening cash valid.
6. Pastikan catalog tampil dan cart menerima item.
7. Logout/login ulang untuk cek restore context baseline.
8. Coba PIN salah berulang sampai lockout baseline muncul.

Checklist installer detail dan log manual ada di `docs/execution/windows_installer_smoke_checklist.md`.

## Recovery baseline

```powershell
.\tooling\scripts\Backup-CassyDesktopState.ps1
```

Recovery minimum yang jujur saat ini adalah restore folder `%USERPROFILE%\.cassy` dari arsip backup terakhir. Ini membantu rollback data lokal, tetapi bukan bukti rollback installer antar-versi.

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
- Artifact EXE tidak muncul:
  - pastikan task yang dipakai `packageDistributionForCurrentOS`
  - cek folder `apps/desktop-pos/build/compose/binaries/main/exe/`
- Build lolos tetapi desktop app tidak ikut terkompilasi:
  - cek `apps/desktop-pos/build.gradle.kts` agar source set desktop diarahkan ke `src/jvmMain`
