# Checklist: Development Loop

Gunakan checklist ini sebelum melakukan commit pada task rutin.

- [ ] Kode baru berada di module yang tepat (Shared Kernel vs Platform App).
- [ ] Primary key baru menggunakan UUIDv7.
- [ ] Mutasi database menyertakan record di tabel Outbox.
- [ ] Unit Test telah dijalankan dan lulus 100%.
- [ ] Tidak ada dependensi platform yang bocor ke `commonMain`.
- [ ] Migrasi database (jika ada) telah dibuat file `.sqm` nya.
- [ ] Dokumentasi `docs/` telah diupdate jika ada perubahan arsitektur.
