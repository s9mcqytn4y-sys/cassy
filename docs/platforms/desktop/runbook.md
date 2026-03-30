# Desktop POS Runbook (Windows) [PRIMARY LANE]

Updated: 2026-03-27

Dokumen ini adalah panduan operasional dan teknis untuk menjalankan, mengembangkan, memaketkan, dan memverifikasi aplikasi **Cassy Desktop POS** di lingkungan Windows.

## 1. Lingkungan Pengembangan
- **OS**: Windows 10/11 (Direkomendasikan).
- **JDK**: Amazon Corretto 17.
- **Gradle**: Gunakan Gradle Wrapper (`./gradlew`) yang ada di root repo.
- **JAVA_HOME**: Harus menunjuk ke instalasi JDK 17.

## 2. Menjalankan Aplikasi (Development)
Gunakan Gradle task berikut untuk menjalankan aplikasi dalam mode pengembangan:
```powershell
.\gradlew :apps:desktop-pos:run
```
Untuk menjalankan **Smoke Test** lokal (verifikasi flow dasar otomatis):
```powershell
.\gradlew :apps:desktop-pos:smokeRun
```
Atau melalui script sandbox:
```powershell
powershell -ExecutionPolicy Bypass -File tooling/scripts/Invoke-DesktopSandbox.ps1 -SmokeRun -TruncateData
```

## 3. Packaging & Distribution (Windows MSI/EXE)
Cassy Desktop mendukung pembuatan installer native Windows secara mandiri.

### Membuat Installer (MSI/EXE)
```powershell
.\gradlew :apps:desktop-pos:createDistributable :apps:desktop-pos:packageExe :apps:desktop-pos:packageMsi
```
### Lokasi Artifact Hasil Build
- **MSI**: `apps/desktop-pos/build/compose/binaries/main/msi/Cassy-<version>.msi`
- **EXE**: `apps/desktop-pos/build/compose/binaries/main/exe/Cassy-<version>.exe`
- **App Image**: `apps/desktop-pos/build/compose/binaries/main/app/Cassy/`

## 4. Integrasi Hardware & Persistence
- **Printer**: Menggunakan Windows Spooler atau direct ESC/POS.
- **Scanner**: Diasumsikan sebagai Keyboard Emulator (HID).
- **SQLite Native**: Runtime SQLite dimuat secara dinamis dari `:tooling:sqlite-worker-init`.
- **Database Location**: `%USERPROFILE%\.cassy\*.db`

## 5. Verification & Diagnostics
Setiap rilis harus melewati tahap verifikasi bukti (*evidence*):
- **Installer Evidence**: `.\tooling\scripts\Invoke-WindowsInstallerEvidence.ps1`
- **Collect Diagnostics**: `.\tooling\scripts\Collect-WindowsReleaseDiagnostics.ps1`
- **State Backup**: `.\tooling\scripts\Backup-CassyDesktopState.ps1`

Hasil verifikasi (logs/reports) akan tersimpan di folder `build/release-diagnostics/` atau `build/installer-evidence/`.

## 6. Penanganan Masalah (Troubleshooting)
- **File Lock pada Clean**: Jika `.\gradlew clean` gagal, jalankan `.\gradlew --stop` untuk mematikan daemon yang menahan file.
- **Runtime Error**: Periksa apakah `%USERPROFILE%\.cassy` memiliki file database yang korup. Lakukan backup lalu reset jika perlu.
- **Hosted CI**: Gunakan workflow **Mainline Evidence** di GitHub Actions untuk memverifikasi artifact secara formal.

---
**Catatan**: Desktop lane adalah prioritas utama untuk seluruh fitur retail execution V1.
