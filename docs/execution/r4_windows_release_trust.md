# R4 Windows Release Trust

Updated: 2026-03-19

Dokumen ini mengunci baseline R4 yang benar-benar hidup di repo dan host Windows saat ini. Fokusnya hanya JDK/workspace truth, smoke distribusi, recovery dasar, diagnostics, dan gap installer yang masih harus jujur.

## Current truth

- [FACT] Host lokal turn ini adalah Windows 11 amd64.
- [FACT] `JAVA_HOME` host lokal menunjuk ke `C:\Program Files\Java\jdk-17`.
- [FACT] `gradle/gradle-daemon-jvm.properties` mengunci daemon ke Java 17.
- [FACT] `apps/desktop-pos` hanya mendeklarasikan `TargetFormat.Exe`, bukan `Msi`.
- [FACT] Task packaging desktop yang benar-benar ada di host ini adalah `createDistributable`, `packageDistributionForCurrentOS`, `packageExe`, `runDistributable`, `smokeRun`, dan task Compose desktop terkait.
- [FACT] App image lokal berada di `apps/desktop-pos/build/compose/binaries/main/app/Cassy/`.
- [FACT] Installer lokal yang dihasilkan turn ini berada di `apps/desktop-pos/build/compose/binaries/main/exe/Cassy-0.1.0.exe`.
- [FACT] Database runtime desktop saat ini hidup di `%USERPROFILE%\.cassy` sebagai `kernel.db`, `masterdata.db`, `sales.db`, dan `inventory.db`.
- [FACT] Repo sekarang punya baseline backup state di `tooling/scripts/Backup-CassyDesktopState.ps1`.
- [FACT] Repo sekarang punya baseline diagnostics collection di `tooling/scripts/Collect-WindowsReleaseDiagnostics.ps1`.

## Smoke and evidence baseline

- [FACT] Source smoke sekarang mengeluarkan marker stdout `CASSY_SMOKE_OK stage=<StageName>` saat sukses.
- [FACT] Distribution smoke script sekarang memaksa marker file `CASSY_SMOKE_MARKER`, memverifikasi hasilnya, lalu mencetak marker yang sama ke stdout.
- [FACT] Workflow `Mainline Evidence` sekarang mengumpulkan artifact diagnostics `cassy-release-diagnostics` pada lane Windows.
- [INTERPRETATION] Ini membuat smoke evidence lebih eksplisit dan lebih mudah dibedakan dari sekadar exit code hijau.

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

- [RISK] Ini adalah rollback/recovery baseline untuk data lokal, bukan proof rollback binary installer end-to-end.
- [RISK] Repo belum membuktikan rollback antar-versi installer terpasang di mesin pilot.

## Diagnostics baseline

- [FACT] Jalur koleksi diagnostics minimum saat ini:

```powershell
powershell -ExecutionPolicy Bypass -File tooling/scripts/Collect-WindowsReleaseDiagnostics.ps1
```

- [FACT] Script diagnostics mengumpulkan:
  - ringkasan host Windows dan `JAVA_HOME`
  - output `gradlew.bat --version`
  - keberadaan artifact EXE, app image, `Cassy.cfg`, runtime release file
  - keberadaan compose logs dan Gradle problems report
  - daftar file `%USERPROFILE%\.cassy`
  - uninstall registry entry `Cassy*` bila ada

- [FACT] Minimum evidence setelah failure:
  - folder hasil script diagnostics
  - `build/reports/problems/problems-report.html` bila ada
  - `apps/desktop-pos/build/compose/logs/` bila terisi
  - snapshot `%USERPROFILE%\.cassy` sebelum recovery jika data perlu dianalisis

## Installer truth

- [FACT] Format installer repo saat ini adalah `EXE`, bukan `MSI`.
- [FACT] Tidak ada evidence `msiexec` pada turn ini karena artifact yang dihasilkan bukan `MSI`.
- [FACT] Install/uninstall manual checklist masih hidup di `docs/execution/windows_installer_smoke_checklist.md`.
- [UNKNOWN] Install/uninstall EXE end-to-end belum terbukti dalam turn ini.
- [RISK] Tanpa install/uninstall proof nyata, verdict R4 tidak boleh dinaikkan ke `DONE`.

## Recommendation

- [RECOMMENDATION] Jalankan backup state sebelum install/update candidate lokal.
- [RECOMMENDATION] Lampirkan artifact `cassy-release-diagnostics` pada review release Windows.
- [RECOMMENDATION] Tutup R4.3 hanya setelah installer EXE benar-benar di-install, di-launch, lalu di-uninstall pada mesin Windows pilot dengan evidence log terisi.
