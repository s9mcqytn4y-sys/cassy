# Cassy Agent Control Plane (v2 - Read-biased Git)

Folder ini adalah **Source of Truth** untuk perilaku, konteks, dan instruksi khusus bagi AI Agent (Gemini, Claude, dsb.) yang bekerja di repository Cassy.

## Struktur Folder

- `context/`: Pengetahuan statis tentang arsitektur, domain, dan aturan main Cassy.
- `playbooks/`: Prosedur langkah-demi-langkah untuk tugas spesifik (Audit, Release, dsb.).
- `prompts/`: Template prompt yang sudah dioptimasi untuk use-case Cassy.
- `checklists/`: Daftar verifikasi sebelum commit atau release.

## Filosofi Agent di Cassy

1. **Evidence-based**: Agent tidak boleh berasumsi. Selalu verifikasi file atau status repo.
2. **Safety-first**: Dilarang melakukan destructive action tanpa konfirmasi manual.
3. **Local-first**: Prioritas pada stabilitas SQLite dan offline-readiness.
4. **Read-biased Git**: Agent fokus pada audit dan diffing. Staging/commit dilakukan secara manual oleh user.

## Cara Menggunakan

Saat memulai session baru dengan Agent, berikan instruksi:
> "Baca .agent/README.md dan gunakan konteks di dalam folder .agent/ sebagai panduan kerja Anda. Perhatikan bahwa kapabilitas Git MCP bersifat read-only untuk audit."
