# R4 JDK Workspace Truth

Updated: 2026-03-19

Dokumen ini mengunci truth JDK/toolchain/workspace untuk foundation slice R4.

## FACT
- `JAVA_HOME` host verifikasi: `C:\Program Files\Java\jdk-17`
- `.\gradlew.bat --version` pada turn ini menunjukkan:
  - Gradle `9.3.1`
  - Launcher JVM `17.0.12`
  - Daemon JVM `Compatible with Java 17`
  - OS `Windows 11 10.0 amd64`
- [gradle/gradle-daemon-jvm.properties](c:/Users/Acer/AndroidStudioProjects/Cassy/gradle/gradle-daemon-jvm.properties) mengunci `toolchainVersion=17`.
- [gradle.properties](c:/Users/Acer/AndroidStudioProjects/Cassy/gradle.properties) menjaga `org.gradle.configuration-cache=false` untuk local/dev lane.
- [apps/desktop-pos/build.gradle.kts](c:/Users/Acer/AndroidStudioProjects/Cassy/apps/desktop-pos/build.gradle.kts) memaksa:
  - `javaToolchains.launcherFor(JavaLanguageVersion.of(17))`
  - `JavaExec` memakai launcher JDK 17
  - `KotlinJvmCompile` memakai target `JVM_17`
- [gradle/libs.versions.toml](c:/Users/Acer/AndroidStudioProjects/Cassy/gradle/libs.versions.toml) mengunci Kotlin plugin repo ke `2.3.20`.

## ASSUMPTION
- IDE/terminal lokal yang dipakai operator release lane mengikuti `JAVA_HOME` dan Gradle daemon criteria yang sama.

## INTERPRETATION
- Truth workspace saat ini konsisten: desktop lane memang dikunci ke JDK 17 dari tiga sisi sekaligus, yaitu host env, daemon criteria, dan module build config.

## RISK
- Java extension di editor masih bisa hidup dengan runtime lain untuk tooling editor, tetapi itu bukan launcher Gradle repo ini.
- Mengaktifkan configuration cache secara global pada local lane masih belum menjadi truth repo.

## RECOMMENDATION
- Jika smoke/build aneh setelah ganti JDK, jalankan `.\gradlew --stop` lalu ulangi verifikasi.
- Jangan mempromosikan hasil packaging desktop bila `--version` tidak lagi menunjukkan launcher/daemon Java 17.
