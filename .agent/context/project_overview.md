# Project Overview

## Identity
Cassy is a retail-first, local-first store operating system with an audit-heavy, sync-explicit posture.

## Phase-1 center of gravity
- Mandatory core: sales, returns, cash/shift/business day, inventory basics, reporting basics, and sync.
- Prepared boundaries: F&B and Service.
- Shared business core: Kotlin Multiplatform.
- Local operational store: SQLite via SQLDelight on clients.
- HQ convergence: backend API + PostgreSQL.

## Architectural posture
- Shared domain/application/data.
- Native app-shell and device-heavy integration.
- Sync is a business-visible subsystem.
- Approval and audit are first-class, not afterthoughts.

## Practical reading
Treat prescriptive design artefacts as source-of-truth.
Treat repository code as an implementation snapshot that may lag or drift.
