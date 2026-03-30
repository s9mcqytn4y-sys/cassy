# MCP Strategy for Cassy (v2 - Read-biased)

Strategi Model Context Protocol (MCP) di Cassy dirancang untuk memberikan Agent akses audit yang mendalam sambil menjaga keamanan repositori melalui pembatasan operasi mutatif Git.

## 1. Filesystem MCP
- **Scope**: Folder root `Cassy/`.
- **Tujuan**: Navigasi monorepo, pembacaan docs, penulisan kode, dan validasi struktur.
- **Batasan**: Dilarang menghapus folder sistem `.git` atau `.gradle`.

## 2. Git MCP (Audit-biased)
- **Scope**: Repository Cassy.
- **Tujuan**: Inspeksi sejarah, verifikasi status, dan pembuatan ringkasan perubahan (*diffing*).
- **Aturan**:
  - **Read-Only Focus**: Agent hanya menggunakan Git untuk `status`, `log`, `diff`, `show`, dan `blame`.
  - **No Mutation**: Agent DILARANG melakukan `git add`, `git commit`, `git push`, `git merge`, atau `git reset`.
  - **Reasoning**: Pembatasan ini diterapkan karena kebijakan keamanan IDE dan untuk memastikan developer manusia melakukan verifikasi manual sebelum commit.

## 3. SQLite MCP
- **Scope**: Database development (`local-dev.db`).
- **Tujuan**: Verifikasi skema, data integrity, dan debugging state aplikasi.
- **Keamanan**: Read-heavy. Operasi `write` hanya untuk penyiapan state testing lokal.

## Security Boundary
- Seluruh staging dan commit dilakukan secara MANUAL oleh user di terminal.
- Agent bertanggung jawab memberikan usulan pesan commit berdasarkan `git diff`.
