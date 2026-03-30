# Checklist: Migration Safety

Gunakan checklist ini sebelum menyentuh file `.sqm` SQLDelight.

- [ ] File `.sqm` baru ditambahkan ke folder migrasi yang tepat.
- [ ] Versi migrasi baru adalah `n + 1` dari versi tertinggi saat ini.
- [ ] Tidak ada penggunaan `DROP TABLE` tanpa bukti migrasi data.
- [ ] Kolom baru (jika ada) didefinisikan sebagai `NULLABLE` atau memiliki `DEFAULT VALUE`.
- [ ] `generateSqlDelightInterface` sukses tanpa breaking changes di kode Kotlin.
- [ ] Foreign keys sudah diaktifkan di level driver SQLDelight.
- [ ] Audit log: migrasi baru sudah dicatat di repository audit.
