# Playbook: Repository Audit

Gunakan playbook ini untuk melakukan audit berkala terhadap kesehatan repository Cassy.

## Langkah 1: Struktur Modul
- Verifikasi apakah ada modul baru yang tidak mengikuti konvensi `shared/{context}`.
- Cek apakah `settings.gradle.kts` sudah mendaftarkan semua modul yang ada di filesystem.

## Langkah 2: Audit SQLDelight
- Cari file `.sq` yang tidak memiliki unit test terkait.
- Pastikan semua file migrasi `.sqm` memiliki penomoran yang berurutan.

## Langkah 3: Dependency Check
- Jalankan `./gradlew :shared:kernel:dependencies` (via Gradle MCP jika tersedia).
- Cari dependensi yang "bocor" dari platform-specific ke `commonMain`.

## Langkah 4: Dokumentasi
- Bandingkan kode di `shared/kernel` dengan `docs/cassy_architecture_specification_v1.md`.
- Laporkan jika ada Business Logic yang diimplementasikan di layer UI.

## Output Audit
Hasil audit harus mencakup:
1. Daftar ketidaksesuaian (Mismatch).
2. Risiko teknis yang ditemukan.
3. Rekomendasi perbaikan jangka pendek.
