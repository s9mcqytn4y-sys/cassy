# R4 Windows Release Trust

Updated: 2026-03-26

Dokumen ini mengunci baseline R4 yang benar-benar hidup di repo dan host Windows saat ini. Untuk foundation slice yang lebih terstruktur, lihat juga:
- `docs/execution/r4_windows_release_contract.md`
- `docs/execution/r4_jdk_workspace_truth.md`
- `docs/execution/r4_smoke_foundation.md`

## Current truth

- [FACT] Host lokal turn ini adalah Windows 11 amd64.
- [FACT] `JAVA_HOME` host lokal menunjuk ke `C:\Program Files\Java\jdk-17`.
- [FACT] `gradle/gradle-daemon-jvm.properties` mengunci daemon ke Java 17.
- [FACT] `apps/desktop-pos` mendeklarasikan `TargetFormat.Exe` dan `TargetFormat.Msi`.
- [FACT] Task packaging desktop yang benar-benar ada di host ini adalah `createDistributable`, `packageDistributionForCurrentOS`, `packageExe`, `packageMsi`, `runDistributable`, `smokeRun`, dan task Compose desktop terkait.
- [FACT] App image lokal berada di `apps/desktop-pos/build/compose/binaries/main/app/Cassy/`.
- [FACT] Installer lokal yang dihasilkan turn ini berada di `apps/desktop-pos/build/compose/binaries/main/exe/Cassy-0.1.0.exe`.
- [FACT] MSI lokal yang dihasilkan turn ini berada di `apps/desktop-pos/build/compose/binaries/main/msi/Cassy-0.1.0.msi`.
- [FACT] Database runtime desktop saat ini hidup di `%USERPROFILE%\.cassy` sebagai `kernel.db`, `masterdata.db`, `sales.db`, dan `inventory.db`.
- [FACT] Repo punya baseline backup state di `tooling/scripts/Backup-CassyDesktopState.ps1`.
- [FACT] Repo punya baseline diagnostics collection di `tooling/scripts/Collect-WindowsReleaseDiagnostics.ps1`.
- [FACT] Repo punya automation installer evidence di `tooling/scripts/Invoke-WindowsInstallerEvidence.ps1`.

## Smoke and evidence baseline

- [FACT] Source smoke mengeluarkan marker stdout `CASSY_SMOKE_OK stage=<StageName>` saat sukses.
- [FACT] Distribution smoke script memaksa marker file `CASSY_SMOKE_MARKER`, memverifikasi hasilnya, lalu mencetak marker yang sama ke stdout.
- [FACT] Installer evidence script melakukan pre-clean install lama, install MSI, smoke installed launcher, repair, smoke ulang, dan uninstall.
- [FACT] Local installer evidence 2026-03-26 sukses di `build/installer-evidence/20260326-174744/`.
- [FACT] Workflow `Mainline Evidence` sekarang disiapkan untuk mengunggah artifact MSI, installer evidence, dan diagnostics.

## Recovery baseline

- [FACT] Recovery dasar yang benar-benar bisa dibuktikan dari repo saat ini adalah backup/restore state lokal `%USERPROFILE%\.cassy`.
- [FACT] Backup state bisa dibuat dengan:

```powershell
powershell -ExecutionPolicy Bypass -File tooling/scripts/Backup-CassyDesktopState.ps1
```

- [FACT] Restore minimum yang jujur saat ini:
  1. Tutup Cassy dan proses Java/installer yang masih membuka file DB.
  2. Backup folder `%USERPROFILE%\.cassy` yang current jika masih ada.
  3. Extract arsip backup ke `%USERPROFILE%\.cassy`.
  4. Jalankan `.\gradlew :apps:desktop-pos:smokeRun` atau distribution smoke untuk memastikan bootstrap tidak rusak.

- [RISK] Ini adalah rollback/recovery baseline untuk data lokal, bukan proof rollback binary antar-versi installer.
- [RISK] Repo belum membuktikan upgrade/rollback antar-versi package di mesin pilot.

## Diagnostics baseline

- [FACT] Jalur koleksi diagnostics minimum saat ini:

```powershell
powershell -ExecutionPolicy Bypass -File tooling/scripts/Collect-WindowsReleaseDiagnostics.ps1
```

- [FACT] Script diagnostics mengumpulkan:
  - ringkasan host Windows dan `JAVA_HOME`
  - output `gradlew.bat --version`
  - keberadaan artifact EXE, MSI, app image, `Cassy.cfg`, runtime release file
  - keberadaan compose logs dan Gradle problems report
  - daftar file `%USERPROFILE%\.cassy`
  - uninstall registry entry `Cassy*` bila ada

## Installer truth

- [FACT] Format installer repo saat ini adalah `EXE` dan `MSI`.
- [FACT] Evidence `msiexec` ada lewat `tooling/scripts/Invoke-WindowsInstallerEvidence.ps1`.
- [FACT] Checklist installer hidup di `docs/execution/windows_installer_smoke_checklist.md`, tetapi lane teknisnya sudah scripted.
- [UNKNOWN] Hosted Windows runner untuk workflow yang sudah diperbarui belum diverifikasi ulang pada turn ini.
- [RISK] Verdict R4 untuk hosted lane tetap perlu run remote baru agar tidak overclaim.

## Recommendation

- [RECOMMENDATION] Jalankan backup state sebelum install/update candidate lokal.
- [RECOMMENDATION] Lampirkan artifact `cassy-release-diagnostics` dan `cassy-installer-evidence` pada review release Windows.
- [RECOMMENDATION] Perlakukan upgrade/rollback antar-versi sebagai follow-up terpisah, bukan blocker untuk truth R4 saat ini.
> Historical Snapshot: dokumen ini tetap disimpan sebagai jejak evolusi. Authority aktif ada di `roadmap_bridge.md`, `windows_desktop_runbook.md`, dan checklist release aktif.
