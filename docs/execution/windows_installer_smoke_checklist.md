# Windows Installer Smoke Checklist

Dokumen ini mencatat jalur installer Windows yang sekarang benar-benar hidup di repo. Fokusnya adalah MSI install, smoke, repair, dan uninstall yang bisa diulang secara scripted di host Windows.

## Input artifact

- EXE installer: `apps/desktop-pos/build/compose/binaries/main/exe/Cassy-<packageVersion>.exe`
- MSI installer: `apps/desktop-pos/build/compose/binaries/main/msi/Cassy-<packageVersion>.msi`
- App distribution: `apps/desktop-pos/build/compose/binaries/main/app/Cassy/`
- Distribution smoke: `powershell -ExecutionPolicy Bypass -File tooling/scripts/Invoke-DesktopDistributionSmoke.ps1`
- Installer evidence: `powershell -ExecutionPolicy Bypass -File tooling/scripts/Invoke-WindowsInstallerEvidence.ps1`
- Upgrade evidence: `powershell -ExecutionPolicy Bypass -File tooling/scripts/Invoke-WindowsUpgradeEvidence.ps1`
- State backup baseline: `powershell -ExecutionPolicy Bypass -File tooling/scripts/Backup-CassyDesktopState.ps1`
- Diagnostics baseline: `powershell -ExecutionPolicy Bypass -File tooling/scripts/Collect-WindowsReleaseDiagnostics.ps1`

## Truth posture

- [FACT] `apps/desktop-pos` saat ini menargetkan `TargetFormat.Exe` dan `TargetFormat.Msi`.
- [FACT] Script `Invoke-WindowsInstallerEvidence.ps1` melakukan pre-clean install lama, install MSI, smoke installed launcher, repair MSI, smoke ulang, lalu uninstall.
- [FACT] Runtime image installer sudah diverifikasi membawa `java.sql`, sehingga launcher hasil install tidak lagi gagal dengan `java/sql/DriverManager`.
- [FACT] Local evidence terbaru selalu ditulis ke `build/installer-evidence/<timestamp>/`.
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
- [FACT] Hosted `Mainline Evidence` sudah menjadi lane evidence aktif untuk artifact installer dan diagnostics.
- [FACT] Upgrade antar-versi installer sekarang punya lane evidence terpisah lewat `Invoke-WindowsUpgradeEvidence.ps1`.
- [RISK] Hosted beta-tag evidence untuk upgrade belum ada sampai commit/tag terbaru dipush dan workflow release dijalankan.
