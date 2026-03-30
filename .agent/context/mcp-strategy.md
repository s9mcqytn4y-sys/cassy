# MCP Strategy for Cassy

Strategi Model Context Protocol (MCP) di Cassy dirancang untuk memberikan Agent akses yang terukur dan aman ke resource lokal dengan kapabilitas operasional penuh.

## 1. Filesystem MCP
- **Scope**: Folder root `Cassy/`.
- **Tujuan**: Navigasi monorepo, pembacaan docs, dan validasi struktur.
- **Batasan**: Dilarang menghapus folder sistem `.git` atau `.gradle`.

## 2. Git MCP (Full Lifecycle)
- **Scope**: Repository Cassy.
- **Tujuan**: Manajemen versi lengkap termasuk audit history, branching, committing, merging, dan remote synchronization (push/pull).
- **Aturan**:
  - Agent memiliki kapabilitas untuk melakukan semua aksi Git.
  - Setiap aksi `push` atau `merge` ke branch utama (`main`/`master`) harus didahului oleh `git diff` dan konfirmasi manual atau checklist release.
  - Gunakan `git status` secara rutin untuk memastikan integritas working directory.

## 3. SQLite MCP
- **Scope**: Database development (`dev.db`, `local-dev.db`).
- **Tujuan**: Verifikasi skema, data integrity, dan debugging state aplikasi.
- **Keamanan**: Read-heavy. Operasi `write` (update/delete) hanya untuk penyiapan state testing.

## 4. Custom Tooling MCP (Future)
- **Whitelisted Gradle Tasks**: Menjalankan build dan test.
- **Artifact Evidence**: Mengumpulkan hasil build secara otomatis.

## Security Boundary
- Akses kredensial (SSH key/Token) dikelola oleh environment host, bukan di dalam instruksi Agent.
- Agent bertindak sebagai operator Git menggunakan identitas yang terkonfigurasi di sistem.
