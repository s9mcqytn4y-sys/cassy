# Cassy Documentation Hub

Selamat datang di pusat dokumentasi **Cassy**, sebuah *Desktop-First, Local-First Retail Operating Core*.

## 1. Vision & Strategy
- [Product Direction](vision/product_direction.md): Visi operasional dan filosofi sistem.
- [Current Implementation Reality](vision/current_state.md): Status nyata repo vs target arsitektur.
- [Roadmap](vision/roadmap.md): Rencana transisi dan target pengembangan ke depan.

## 2. Architecture & Design
- [Architecture Overview](architecture/overview.md): KMP, Bounded Contexts, dan Layering Rules.
- [Module Map](architecture/modules.md): Struktur Gradle dan aturan dependency.
- [Security & Auth](architecture/security_auth.md): Kebijakan approval lokal dan fallback online.

## 3. Data & Persistence
- [Persistence Policy](data/persistence_policy.md): SQLite, SQLDelight, UUIDv7, dan Migrasi.
- [Inventory Ledger Truth](data/inventory_ledger.md): Model data untuk akurasi stok.
- [Barcode Schema & Scanner Policy](data/barcode_schema.md): Skema barcode dan kebijakan scanner.
- [Sync Strategy](data/sync_strategy.md): Outbox pattern dan status replay.

## 4. Platform Lanes
- [Desktop POS (Windows)](platforms/desktop/runbook.md): Lane operasional utama (Primary).
- [Android POS](platforms/android/runbook.md): Business semantics parity lane.

## 5. User & Operations
- [Operator Quick Start](user/operator_quickstart.md): Panduan cepat penggunaan harian.
- [Daily Operations Guide](user/daily_operations_guide.md): Detail operasional dan prosedur pemulihan.

## 6. Engineering & Tooling
- [CI/CD & Build Pipeline](tooling/ci_cd.md): Workflow GitHub Actions.
- [Testing Strategy](tooling/testing.md): Unit test dan smoke test rules.

---
**Kebijakan Dokumentasi**: Cassy menggunakan pendekatan *Repo-First*. Segala sesuatu yang tertulis di sini yang belum ada di kode harus dianggap sebagai `[TARGET-STATE]`.
**Deprecation Note**: File di root `docs/` atau `docs/execution/` yang bertanda *DEPRECATED* sedang dalam proses pemindahan. Gunakan index ini sebagai panduan tunggal.
