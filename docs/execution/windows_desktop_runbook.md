# Windows Desktop Runbook

Dokumen ini hanya mencatat jalur yang benar-benar relevan untuk pilot Windows dan evidence yang bisa diverifikasi di repo.

## Prasyarat lokal

- Windows
- JDK 17
- Gradle wrapper repo
- koneksi internet saat packaging pertama jika Compose perlu mengunduh tooling WiX

## Development run

```powershell
.\gradlew :apps:desktop-pos:run
```

Yang harus terlihat dari flow foundation:
- bootstrap store/terminal bila context belum ada
- login operator dengan PIN
- open business day
- start shift dengan opening cash
- catalog dan cart baseline

## Verification matrix

Gunakan urutan ini:

```powershell
.\gradlew --version
.\gradlew clean
.\gradlew build
.\gradlew test
.\gradlew detekt
.\gradlew :apps:android-pos:lintDebug
.\gradlew :apps:desktop-pos:packageDistributionForCurrentOS
```

Catatan operasional:
- `packageDistributionForCurrentOS` dijalankan terakhir karena file lock Windows dapat membuat `clean` gagal jika artifact masih dipakai.
- Jika `clean` gagal karena lock, jalankan `.\gradlew --stop` lalu ulangi `clean`.

## Artifact packaging yang sudah terbukti lokal

- Task: `:apps:desktop-pos:packageDistributionForCurrentOS`
- Format lokal terverifikasi: `EXE`
- Artifact path: `apps/desktop-pos/build/compose/binaries/main/exe/Cassy-0.1.0.exe`

## Gap yang masih harus diakui

- Packaging Windows sudah terbukti lokal, tetapi belum punya hosted CI execution evidence.
- Smoke install/uninstall installer Windows belum tervalidasi di repo ini.
- Debian package pada Ubuntu hanya compatibility artifact; bukan release truth untuk pilot Windows.

## Manual smoke checklist

1. Jalankan desktop app dari Gradle run.
2. Bootstrap store, terminal, cashier, supervisor.
3. Login supervisor.
4. Open business day.
5. Start shift dengan nominal opening cash valid.
6. Pastikan catalog tampil dan cart menerima item.
7. Logout/login ulang untuk cek restore context baseline.
8. Coba PIN salah berulang sampai lockout baseline muncul.

## Troubleshooting

- `clean` gagal di Windows:
  - hentikan daemon dengan `.\gradlew --stop`
  - tutup app atau installer yang masih memegang file di `build/`
- Artifact EXE tidak muncul:
  - pastikan task yang dipakai `packageDistributionForCurrentOS`
  - cek folder `apps/desktop-pos/build/compose/binaries/main/exe/`
- Build lolos tetapi desktop app tidak ikut terkompilasi:
  - cek `apps/desktop-pos/build.gradle.kts` agar source set desktop diarahkan ke `src/jvmMain`
