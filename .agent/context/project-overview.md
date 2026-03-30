# Cassy Project Overview (Unified)

Cassy adalah **desktop-first retail operating core** untuk single outlet.

## Core Identity
- **Surface Utama**: Windows Desktop (JVM).
- **Parity Surface**: Android POS/Mobile.
- **Fokus**: Local-first operation, auditability, dan offline-ready checkout.

## Key Domains (Bounded Contexts)
- **Kernel**: Shared logic, auth profile, approval, audit, Outbox.
- **MasterData**: Products, Prices, Store, Terminal config.
- **Sales**: Checkout logic, Payments, Receipts, Returns.
- **Inventory**: Stock Ledger, Balances, Adjustments.
- **Cash**: Business Day, Shift Management, Reconciliation.
- **Sync**: Outbox, Batching, Conflict resolution.

## Technical Foundation
- **Language**: Kotlin Multiplatform (KMP).
- **Database**: SQLite 3 via SQLDelight (Typed, Migration-safe).
- **Backend**: Go + PostgreSQL (HQ-only).
- **Release Strategy**: Beta-3 stage, transisi ke production-ready.

## Implementation Principles
- **Guided Operations**: UI yang tenang dan jelas.
- **Explainable State**: Cash control, audit log, dan approval yang transparan.
- **Local-First**: Database lokal adalah source of truth; sync adalah queue eksplisit.
