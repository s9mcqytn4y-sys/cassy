# Gemini Agent Guide for Cassy (v4 - Read-biased MCP)

Dokumen ini adalah panduan teknis terbaru untuk mengonfigurasi dan menggunakan **Gemini Agent** di Android Studio untuk proyek Cassy dengan fokus pada **keamanan dan audit**.

## 1. Konfigurasi MCP Servers
Buka `Settings` -> `Tools` -> `Gemini` -> `MCP Servers`. Tambahkan server berikut dengan mengganti `[PROJECT_ROOT]` menjadi `C:/Users/Acer/AndroidStudioProjects/Cassy`.

### A. Filesystem (Read-Write)
Memberikan kemampuan navigasi, pembacaan, dan penulisan file di seluruh repo.
- **Command**: `npx`
- **Args**: `@modelcontextprotocol/server-filesystem`, `C:/Users/Acer/AndroidStudioProjects/Cassy`
- **Gunakan untuk**: Manipulasi konten file secara langsung.

### B. Git (Audit & Inspection Only)
Memberikan kemampuan audit history dan verifikasi status.
- **Command**: `npx`
- **Args**: `@modelcontextprotocol/server-git`, `C:/Users/Acer/AndroidStudioProjects/Cassy`
- **PENTING**: Karena pembatasan keamanan IDE, Agent DILARANG melakukan `git add`, `git commit`, atau `git push`. Operasi mutatif Git harus dilakukan secara MANUAL oleh developer di terminal.

### C. SQLite (Operational Data)
Memberikan kemampuan verifikasi skema dan data integrity pada database lokal.
- **Command**: `npx`
- **Args**: `@modelcontextprotocol/server-sqlite`, `--db`, `C:/Users/Acer/AndroidStudioProjects/Cassy/local-dev.db`

## 2. Instruksi Inisialisasi
Setiap kali memulai sesi baru, berikan instruksi ini:
> "Gunakan instruksi dari `.agent/README.md` dan konteks di `.agent/context/` sebagai panduan kerja. Fokus pada audit-first. Perintah mutatif Git (commit/push) akan dilakukan secara manual oleh user."

## 3. Workflow Operasional
- **Audit Changes**: "Gunakan `git diff` untuk merangkum perubahan yang saya buat dan buatkan pesan commit yang sesuai."
- **Data Validation**: "Gunakan SQLite MCP untuk memverifikasi integritas data setelah mutasi."

## 4. Keamanan & Boundary
- Agent tidak melakukan commit otomatis.
- Selalu verifikasi `git status` sebelum mengakhiri sesi.
- Gunakan path absolut untuk stabilitas.
