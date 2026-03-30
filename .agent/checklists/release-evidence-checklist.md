# Checklist: Release Evidence

Gunakan checklist ini sebelum menyetujui rilis ke Production/Beta.

- [ ] Gradle build (Assemble) sukses tanpa error.
- [ ] Versioning di `gradle.properties` sudah benar.
- [ ] CHANGELOG.md berisi semua perubahan utama.
- [ ] File biner (APK/MSI/EXE) sudah dipindahkan ke folder output yang benar.
- [ ] Bukti (Evidence) test rilis sudah dikumpulkan.
- [ ] Status database (Migrasi) sudah terverifikasi di local-dev.
- [ ] Git Tag sudah dibuat sesuai versi rilis.
- [ ] Backup database state (`Backup-CassyDesktopState.ps1`) sudah dilakukan.
