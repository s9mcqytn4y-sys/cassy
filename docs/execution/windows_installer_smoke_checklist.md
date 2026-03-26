# Windows Installer Smoke Checklist

Dokumen ini mencatat jalur installer Windows yang sekarang benar-benar hidup di repo. Fokusnya adalah MSI install, smoke, repair, dan uninstall yang bisa diulang secara scripted di host Windows.

## Input artifact

- EXE installer: `apps/desktop-pos/build/compose/binaries/main/exe/Cassy-0.1.0.exe`
- MSI installer: `apps/desktop-pos/build/compose/binaries/main/msi/Cassy-0.1.0.msi`
- App distribution: `apps/desktop-pos/build/compose/binaries/main/app/Cassy/`
- Distribution smoke: `powershell -ExecutionPolicy Bypass -File tooling/scripts/Invoke-DesktopDistributionSmoke.ps1`
- Installer evidence: `powershell -ExecutionPolicy Bypass -File tooling/scripts/Invoke-WindowsInstallerEvidence.ps1`
- State backup baseline: `powershell -ExecutionPolicy Bypass -File tooling/scripts/Backup-CassyDesktopState.ps1`
- Diagnostics baseline: `powershell -ExecutionPolicy Bypass -File tooling/scripts/Collect-WindowsReleaseDiagnostics.ps1`

## Truth posture

- [FACT] `apps/desktop-pos` saat ini menargetkan `TargetFormat.Exe` dan `TargetFormat.Msi`.
- [FACT] Script `Invoke-WindowsInstallerEvidence.ps1` melakukan pre-clean install lama, install MSI, smoke installed launcher, repair MSI, smoke ulang, lalu uninstall.
- [FACT] Runtime image installer sudah diverifikasi membawa `java.sql`, sehingga launcher hasil install tidak lagi gagal dengan `java/sql/DriverManager`.
- [FACT] Local evidence turn 2026-03-26 sukses di `build/installer-evidence/20260326-174744/`.
- [FACT] Script installer dibuat repeatable di workstation yang sebelumnya sudah pernah memasang Cassy.

## Automated checklist

1. Jalankan backup state lokal bila akan menguji di workstation yang menyimpan data nyata.
2. Bangun artifact desktop:

```powershell
.\gradlew :apps:desktop-pos:createDistributable :apps:desktop-pos:packageExe :apps:desktop-pos:packageMsi
```

3. Jalankan smoke distribusi:

```powershell
powershell -ExecutionPolicy Bypass -File tooling/scripts/Invoke-DesktopDistributionSmoke.ps1
```

4. Jalankan evidence installer:

```powershell
powershell -ExecutionPolicy Bypass -File tooling/scripts/Invoke-WindowsInstallerEvidence.ps1
```

5. Kumpulkan diagnostics:

```powershell
powershell -ExecutionPolicy Bypass -File tooling/scripts/Collect-WindowsReleaseDiagnostics.ps1
```

## Manual pilot follow-up

Automation di atas menutup lane installer teknis. Untuk pilot manusia nyata, hal yang masih layak dicek manual adalah:

1. shortcut/start menu tampil sesuai ekspektasi user Windows
2. first-run UX setelah install tidak membingungkan operator
3. leftover file/folder pasca-uninstall masih bisa diterima untuk lane local-first

## Status saat ini

- [FACT] Installer smoke tidak lagi manual-soft-blocker untuk lane lokal/repo.
- [ASSUMPTION] Hosted Windows runner juga bisa menjalankan script yang sama setelah workflow `Mainline Evidence` diperbarui.
- [RISK] Hosted evidence terbaru belum saya verifikasi karena run remote baru belum dijalankan pada turn ini.
