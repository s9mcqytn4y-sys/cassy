# R4 Windows Release Contract

Updated: 2026-03-26

Dokumen ini mengunci foundation slice R4 yang benar-benar dibuktikan dari repo dan host Windows saat ini. Fokusnya pada toolchain/workspace truth, packaging task map, smoke path, installer evidence, dan batasan yang masih harus diakui.

## FACT
- R1, R2, dan R3 sudah dire-check ulang lewat command lokal nyata pada turn ini dan gate teknisnya hijau.
- Host turn ini adalah Windows 11 amd64.
- `JAVA_HOME` host saat verifikasi menunjuk ke `C:\Program Files\Java\jdk-17`.
- Desktop packaging saat ini dikonfigurasi di [apps/desktop-pos/build.gradle.kts](c:/Users/Acer/AndroidStudioProjects/Cassy/apps/desktop-pos/build.gradle.kts) dengan `TargetFormat.Exe` dan `TargetFormat.Msi`.
- Runtime image desktop sekarang secara eksplisit membawa modul `java.sql` agar launcher hasil install tidak gagal di startup.
- Task packaging desktop yang benar-benar ada di host ini: `createDistributable`, `packageDistributionForCurrentOS`, `packageExe`, `packageMsi`, `run`, `runDistributable`, `smokeRun`, dan task Compose desktop terkait.
- Source smoke yang benar-benar ada: `.\gradlew :apps:desktop-pos:smokeRun` dan `.\gradlew :apps:desktop-pos:run --args="--smoke-run"`.
- Distribution smoke yang benar-benar ada: `powershell -ExecutionPolicy Bypass -File tooling/scripts/Invoke-DesktopDistributionSmoke.ps1`.
- Installer evidence yang benar-benar ada: `powershell -ExecutionPolicy Bypass -File tooling/scripts/Invoke-WindowsInstallerEvidence.ps1`.
- CI foundation Windows yang aktif saat ini membangun distribusi Windows, menjalankan distribution smoke, menyiapkan installer evidence script, dan mengunggah artifact EXE, MSI, app image, installer evidence, dan diagnostics.

## INTERPRETATION
- Kontrak R4 sekarang sudah melampaui foundation packaging saja; install, repair, smoke, dan uninstall MSI sudah punya bukti lokal yang repeatable.
- Scope yang masih belum boleh di-overclaim adalah hosted rerun terbaru dan upgrade/rollback antar-versi.

## RISK
- Packaging desktop bersifat host-sensitive; claim sukses Windows tidak boleh diangkat dari host non-Windows.
- Hosted workflow yang sudah diperbarui belum punya run remote baru yang diverifikasi pada turn ini.
- Upgrade dan rollback antar-versi installer belum dibuktikan.

## RECOMMENDATION
- Pakai dokumen ini sebagai kontrak R4 saat ini.
- Gunakan [r4_jdk_workspace_truth.md](c:/Users/Acer/AndroidStudioProjects/Cassy/docs/execution/r4_jdk_workspace_truth.md) untuk truth JDK/workspace.
- Gunakan [r4_smoke_foundation.md](c:/Users/Acer/AndroidStudioProjects/Cassy/docs/execution/r4_smoke_foundation.md) untuk task map packaging dan smoke path.
