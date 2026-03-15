# Cassy - Desktop-First Retail Operating Core

"Cepat di kasir. Rapi di operasional."

Cassy V1 adalah retail-first operational POS yang berfokus pada kecepatan transaksi lokal (local-first) dan integritas data operasional (ledger-based inventory).

## Strategic Posture (V1)
- **Primary Target**: Desktop POS (Primary release lane).
- **Secondary Target**: Android POS (Semantic parity lane).
- **Architecture**: Kotlin Multiplatform (KMP) for Domain/Application/Data.
- **Database**: SQLDelight (Bounded-context local SQLite).

## Project Structure
- `apps/desktop-pos`: Desktop retail client.
- `apps/android-pos`: Android retail client.
- `shared/kernel`: Core infra, Auth, Day/Shift management.
- `shared/inventory`: Stock ledger and balance truth.
- `shared/sales`: Basket, pricing, and checkout logic.
- `shared/masterdata`: Product and metadata.

## AI Agent Context
Proyek ini dioptimalkan untuk kolaborasi manusia dan AI. Seluruh aturan operasional dan roadmap tersimpan di:
1. `AGENTS.md` (Constitution & Entrypoint)
2. `.agent/` (Context compression layer)
3. `docs/execution/roadmap_bridge.md` (Active Milestone tracking)

## Getting Started
Lihat `README_INSTALLATION.md` untuk setup lingkungan pengembangan.
