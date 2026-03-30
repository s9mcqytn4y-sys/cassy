# Playbook: Release Safety

Gunakan playbook ini untuk memastikan rilis (Desktop/Android) aman dan terdokumentasi dengan baik.

## Langkah 1: Validasi Versi
- Cek `gradle.properties` untuk `cassy.release.version` dan `cassy.package.version`.
- Pastikan versi di `CHANGELOG.md` sudah diupdate dan sesuai dengan tag git.

## Langkah 2: Audit Artifact (Desktop)
- Jalankan build desktop: `./gradlew :apps:desktop-pos:packageRelease`.
- Verifikasi keberadaan output di `apps/desktop-pos/build/compose/binaries`.
- Gunakan tool di `tooling/scripts/New-ReleaseArtifactEvidence.ps1` untuk merekam snapshot build.

## Langkah 3: Database Sanity Check
- Pastikan tidak ada migrasi `.sqm` yang "pending" (semua harus sudah ter-generate).
- Jalankan test migrasi jika tersedia untuk memastikan data existing tidak korup.

## Langkah 4: Evidence Collection
- Kumpulkan log build sukses.
- Ambil screenshot aplikasi (jika memungkinkan via automation).
- Jalankan smoke test sederhana pada binary yang dihasilkan.

## Output Release
Dokumen rilis harus mencakup:
1. Version hash (Git SHA).
2. Daftar fitur/perbaikan dari CHANGELOG.
3. Link ke artifact evidence.
4. Status verifikasi database.
