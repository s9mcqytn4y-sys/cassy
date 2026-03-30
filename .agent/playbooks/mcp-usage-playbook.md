# Playbook: MCP Usage for Cassy (Audit-biased)

Gunakan playbook ini sebagai panduan interaksi antara Agent dan MCP server dengan fokus pada inspeksi dan keamanan.

## 1. Filesystem Access
- Gunakan `list_files` untuk memahami struktur modul sebelum melakukan perubahan.
- Gunakan `read_file` pada `docs/` untuk memahami requirement bisnis.
- **Write Permission**: Agent diizinkan melakukan `write_file` atau `replace_text` untuk modifikasi kode/docs.

## 2. Git Operations (Read-Only Audit)
Agent dilarang melakukan mutasi Git. Gunakan Git hanya untuk konteks:
- **Status**: Jalankan `git status` untuk melihat file yang berubah.
- **Diffing**: Gunakan `git diff` untuk merangkum perubahan yang telah Anda buat melalui Filesystem MCP.
- **History**: Gunakan `git log` untuk memahami konteks perubahan sebelumnya.
- **Commit Message**: Buatkan usulan pesan commit yang berkualitas berdasarkan perubahan Anda, lalu minta user untuk melakukan commit secara manual.

## 3. SQLite Interaction
- Gunakan `read_query` untuk memvalidasi data (Read-only by default).
- Operasi `write` hanya diperbolehkan untuk manipulasi data testing lokal.

## 4. Workflow Kerja
1. Agent memodifikasi file via Filesystem MCP.
2. Agent menjalankan `git diff` untuk memverifikasi perubahan.
3. Agent melaporkan hasil kerja dan memberikan perintah terminal untuk dilakukan user (misal: `git add . && git commit -m "..."`).
