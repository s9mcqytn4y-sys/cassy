# Playbook: MCP Usage for Cassy

Gunakan playbook ini sebagai panduan interaksi antara Agent dan MCP server dengan kapabilitas penuh.

## 1. Filesystem Access
- Gunakan `list_files` untuk memahami struktur modul sebelum melakukan perubahan.
- Gunakan `read_file` pada `docs/` untuk memahami requirement bisnis sebelum menulis kode.
- Hindari membaca file binary atau folder `.gradle`.

## 2. Git Operations (Full Lifecycle)
Agent memiliki izin untuk melakukan manajemen repository secara mandiri:
- **Pre-flight**: Jalankan `git status` untuk memastikan working directory bersih.
- **Branching**: Buat branch baru untuk setiap fitur/fix: `feat/nama-fitur` atau `fix/issue-id`.
- **Changes**: Gunakan `git diff` untuk merangkum perubahan sebelum commit.
- **Commit**: Gunakan format: `feat(scope): message` atau `fix(scope): message`.
- **Sync**: Lakukan `git pull --rebase` sebelum push untuk menghindari conflict.
- **Remote**: Agent dapat melakukan `git push` ke branch remote.
- **Safety**: Untuk merge ke branch `main`/`master`, Agent harus memberikan ringkasan perubahan dan menunggu konfirmasi eksplisit atau menjalankan checklist rilis.

## 3. SQLite Interaction
- Gunakan `read_query` untuk memvalidasi data tanpa mengubahnya (Read-only by default).
- Gunakan `execute_query` untuk menyiapkan state testing lokal atau membersihkan data dummy.
- Selalu verifikasi skema tabel dengan `PRAGMA table_info(tableName)` sebelum melakukan query kompleks.

## 4. Error Handling
- Jika MCP return error "Permission Denied", laporkan kepada user.
- Jika ada merge conflict saat `git pull`, Agent harus melaporkan file mana yang bermasalah dan meminta bantuan resolusi jika logika bisnisnya ambigu.
- Laporkan jika ada anomali antara hasil query MCP dan ekspektasi di dokumen arsitektur.
