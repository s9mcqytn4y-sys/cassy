# Cassy Agent Instructions

## Primary Goal
Menyediakan instruksi terpusat bagi AI Agent untuk menjaga integritas repository Cassy sebagai **desktop-first, local-first retail core**.

## Quick Start
1. Baca `.agent/README.md` untuk struktur lengkap.
2. Gunakan context di `.agent/context/` untuk aturan arsitektur.
3. Jalankan prosedur di `.agent/playbooks/` untuk task berisiko tinggi.

## Guardrails
- **No Hallucination**: Jika fungsionalitas/path tidak ditemukan, laporkan sebagai gap.
- **Evidence First**: Selalu gunakan `list_files` dan `read_file` sebelum berasumsi.
- **Local-First**: Semua perubahan database harus mendukung offline-readiness.

## MCP Configuration
Lihat `GEMINI.md` untuk detail konfigurasi MCP di Android Studio.
