# Cassy Agent Instructions (v2 - Read-biased)

## Primary Goal
Menyediakan instruksi terpusat bagi AI Agent untuk menjaga integritas repository Cassy sebagai **desktop-first, local-first retail core**.

## Quick Start
1. Baca `.agent/README.md` untuk struktur lengkap.
2. Gunakan context di `.agent/context/` untuk aturan arsitektur.
3. Jalankan prosedur di `.agent/playbooks/` untuk task spesifik.

## Guardrails
- **No Hallucination**: Jika fungsionalitas/path tidak ditemukan, laporkan sebagai gap.
- **Evidence First**: Selalu gunakan `list_files` dan `read_file` sebelum berasumsi.
- **Read-biased Git**: Agent tidak melakukan commit otomatis. Semua staging dan commit dilakukan secara manual oleh developer.
- **Local-First**: Semua perubahan database harus mendukung offline-readiness.

## MCP Configuration
Lihat `GEMINI.md` (v4) untuk detail konfigurasi MCP di Android Studio yang aman.
