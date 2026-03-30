# Gemini Agent Guide for Cassy (v3 - Full MCP)

Dokumen ini adalah panduan teknis terbaru untuk mengonfigurasi dan menggunakan **Gemini Agent** di Android Studio untuk proyek Cassy dengan kapabilitas **Full MCP**.

## 1. Konfigurasi MCP Servers
Buka `Settings` -> `Tools` -> `Gemini` -> `MCP Servers`. Tambahkan server berikut dengan mengganti `[PROJECT_ROOT]` menjadi `C:/Users/Acer/AndroidStudioProjects/Cassy`.

### A. Filesystem (Full Access)
Memberikan kemampuan navigasi, pembacaan, dan penulisan file di seluruh repo.
- **Command**: `npx`
- **Args**: `@modelcontextprotocol/server-filesystem`, `C:/Users/Acer/AndroidStudioProjects/Cassy`

### B. Git (Full Lifecycle)
Memberikan kemampuan audit history, branching, commit, merge, dan sync remote secara mandiri.
- **Command**: `npx`
- **Args**: `@modelcontextprotocol/server-git`, `C:/Users/Acer/AndroidStudioProjects/Cassy`

### C. SQLite (Operational Data)
Memberikan kemampuan verifikasi skema dan data integrity pada database lokal.
- **Command**: `npx`
- **Args**: `@modelcontextprotocol/server-sqlite`, `--db`, `C:/Users/Acer/AndroidStudioProjects/Cassy/local-dev.db`

## 2. Instruksi Inisialisasi
Setiap kali memulai sesi baru, berikan instruksi ini:
> "Gunakan instruksi dari `.agent/README.md` dan konteks di `.agent/context/` sebagai panduan kerja. Aktifkan kapabilitas Full MCP (Filesystem, Git, SQLite) sesuai `.agent/playbooks/mcp-usage-playbook.md`."

## 3. Workflow Operasional
- **Audit History**: "Gunakan Git MCP untuk merangkum 10 commit terakhir dan bandingkan dengan `docs/roadmap.md`."
- **Data Validation**: "Gunakan SQLite MCP untuk memverifikasi apakah `StockLedgerEntry` terbaru sinkron dengan `InventoryBalance`."
- **Automated Commit**: "Setelah melakukan perubahan kode, gunakan Git MCP untuk membuat branch `feat/...`, melakukan commit, dan memberikan ringkasan perubahan."

## 4. Keamanan & Boundary
- Agent beroperasi menggunakan identitas Git lokal (pastikan `git config user.name` dan `email` sudah benar).
- Operasi `git push` atau `merge` ke `main` memerlukan konfirmasi manual user.
- Gunakan path absolut untuk stabilitas pemanggilan tool MCP.
