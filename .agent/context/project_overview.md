# Cassy Project Overview (Updated 2026-03-20)

## Product Shape & Business Context
Cassy adalah **Desktop-First Retail Operating Core** yang dirancang khusus untuk **Single-Outlet Retail**.

### Target Pelanggan (ICP)
- **Owner-Operator**: Pemilik toko yang juga mengoperasikan kasir atau mengawasi 1-3 staf kasir secara langsung.
- **Toko Retail Mandiri**: Warung modern, butik, toko hobi, atau outlet retail sejenis yang tidak memerlukan kompleksitas multi-outlet di V1.

### Nilai Jual Utama (Unique Selling Proposition)
- **Guided Operations**: Flow aplikasi yang membimbing user (Open Day -> Start Shift -> Sales -> Close Shift) untuk meminimalisir kesalahan operasional.
- **Local-First Resilience**: Kecepatan checkout maksimal dan ketahanan terhadap gangguan internet; sinkronisasi cloud bersifat asinkron/background.
- **Operational Integrity**: Guardrail yang ketat pada shift dan stok (inventory ownership) untuk mencegah kebocoran data bisnis.

## V1 Strategic Focus (PDF v1.1 - Hardened)
- **Primary Release Lane**: Desktop Windows (Installer .exe private).
- **Parity Lane**: Android POS (Semantic parity, bukan target release utama V1).
- **Core Scope**:
  - Master Data & Metadata Produk.
  - Basket Persistence (Survival on restart).
  - Business Day & Shift Guardrails.
  - Basic Inventory Ledger (Auto-mutation on checkout).
  - Sync Visibility (Offline-safe).

## Technical Posture
- Kotlin Multiplatform (KMP) untuk core logic.
- Compose Multiplatform untuk UI.
- SQLDelight (Local persistence).
- Koin (DI).

## Strategic Bridge
Status milestone saat ini (M2-M5 Done & Stable) diverifikasi melalui kode dan bukti manual. Lihat `docs/execution/roadmap_bridge.md` untuk rincian kepatuhan repo.
