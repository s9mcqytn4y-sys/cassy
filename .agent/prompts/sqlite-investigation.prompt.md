# Prompt: SQLite Investigation

"Gunakan MCP Sqlite untuk menganalisis tabel [NAMA_TABEL] di database Cassy.

Tujuan:
1. Validasi skema tabel terhadap dokumen `docs/store_pos_erd_specification_v2.md`.
2. Pastikan primary key menggunakan format UUIDv7 (cek 5 record contoh).
3. Verifikasi apakah ada index yang hilang untuk kolom yang sering di-query (seperti `idempotency_key` atau `sync_status`).
4. Berikan ringkasan temuan dan saran optimasi jika perlu."
