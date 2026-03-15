# Workspace and JDK Guide

Dokumen ini mengunci jalur local development agar tidak diam-diam lari ke Java 21 atau configuration cache yang merusak sync/import.

## Local development truth

- Desktop lane: JDK 17 only
- `JAVA_HOME` wajib menunjuk ke JDK 17
- `gradle/gradle-daemon-jvm.properties` saat ini mengunci daemon ke Java 17
- `org.gradle.configuration-cache=false` adalah default local/dev
- CI mengaktifkan configuration cache secara eksplisit per command

## VS Code

- Gunakan task workspace di `.vscode/tasks.json`
- `Cassy Desktop Smoke Run` untuk cek runtime cepat
- `Cassy Core Verify` untuk build/test/lint utama
- `Cassy Windows Package` untuk createDistributable + EXE packaging

## Android Studio / IntelliJ

- Pastikan Gradle JVM project menunjuk ke JDK 17
- Jika IDE terasa tersangkut setelah ganti JDK atau habis packaging, jalankan `.\gradlew --stop`
- Jangan memaksa configuration cache global on untuk import path sampai seluruh sync ergonomics terbukti stabil

## Troubleshooting

- `desktop-pos:run` crash `UnsatisfiedLinkError`:
  - cek `.\gradlew --version` dan pastikan daemon criteria = Java 17
  - jalankan `.\gradlew :apps:desktop-pos:dependencyInsight --dependency skiko --configuration runtimeClasspath`
  - pastikan `skiko` Java dan runtime berada pada versi yang sama
- Sync/import terasa aneh:
  - stop daemon
  - refresh Gradle project
  - ulangi dengan configuration cache tetap off pada local/IDE lane
