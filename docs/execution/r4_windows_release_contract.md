# R4 Windows Release Contract

Updated: 2026-03-19

Dokumen ini mengunci foundation slice R4 yang benar-benar dibuktikan dari repo dan host Windows saat ini. Fokusnya hanya pada toolchain/workspace truth, packaging task map, smoke path, CI/docs baseline, dan batasan host Windows yang harus jujur.

## FACT
- R1, R2, dan R3 sudah dire-check ulang lewat command lokal nyata pada turn ini dan gate teknisnya hijau.
- Host turn ini adalah Windows 11 amd64.
- `JAVA_HOME` host saat verifikasi menunjuk ke `C:\Program Files\Java\jdk-17`.
- Desktop packaging saat ini dikonfigurasi di [apps/desktop-pos/build.gradle.kts](c:/Users/Acer/AndroidStudioProjects/Cassy/apps/desktop-pos/build.gradle.kts) dengan `TargetFormat.Exe`.
- Task packaging desktop yang benar-benar ada di host ini: `createDistributable`, `packageDistributionForCurrentOS`, `packageExe`, `run`, `runDistributable`, `smokeRun`, dan task Compose desktop terkait.
- Source smoke yang benar-benar ada: `.\gradlew :apps:desktop-pos:smokeRun` dan `.\gradlew :apps:desktop-pos:run --args="--smoke-run"`.
- Distribution smoke yang benar-benar ada: `powershell -ExecutionPolicy Bypass -File tooling/scripts/Invoke-DesktopDistributionSmoke.ps1`.
- CI foundation Windows yang aktif saat ini tetap minimal: `Mainline Evidence` membangun distribusi Windows, menjalankan distribution smoke, dan mengunggah artifact EXE, app image, dan diagnostics.

## INTERPRETATION
- BLOCK 1 R4 bisa ditutup `PASS` tanpa mengklaim installer install/uninstall end-to-end.
- Scope foundation yang jujur adalah memastikan jalur packaging dan smoke bisa diulang dengan JDK 17 dan host Windows, bukan meng-overclaim readiness release penuh.

## RISK
- Packaging desktop bersifat host-sensitive; claim sukses Windows tidak boleh diangkat dari host non-Windows.
- Artifact installer repo saat ini masih `EXE`, bukan `MSI`, sehingga lane `msiexec` belum berlaku.
- Install/uninstall end-to-end masih di luar foundation slice ini dan tetap harus diberi status terpisah.

## RECOMMENDATION
- Pakai dokumen ini sebagai kontrak BLOCK 1.
- Gunakan [r4_jdk_workspace_truth.md](c:/Users/Acer/AndroidStudioProjects/Cassy/docs/execution/r4_jdk_workspace_truth.md) untuk truth JDK/workspace.
- Gunakan [r4_smoke_foundation.md](c:/Users/Acer/AndroidStudioProjects/Cassy/docs/execution/r4_smoke_foundation.md) untuk task map packaging dan smoke path.
