# Cassy Roadmap Execution Bridge (PDF v1.1 -> Repo)

This document maps the strategic milestones from the PDF Roadmap to the actual repository state.

## Milestone Status Tracker

| ID | Milestone Name | Repo Evidence | Status |
|:---|:---|:---|:---|
| **M0** | Program Setup | `AGENTS.md`, `.agent/` structure | **DONE** |
| **M1** | Scope Lock V1 | `AGENTS.md` (Strategic Lock section) | **DONE** |
| **M2** | Architecture Control Plane | Multi-module Gradle, SQLDelight cleanup | **DONE** (Residual Debt in `:shared` aggregator) |
| **M3** | Desktop Store Bootstrap | `apps/desktop-pos` + `CatalogScreen` | **DONE** |
| **M4** | Business Day & Shift | `BusinessDayScreen.kt` + `BusinessDayService.kt` | **DONE** |
| **M5** | Catalog & Cart | `CatalogViewModel.kt` in `:shared` | **PARTIAL** |
| **M6** | Checkout & Receipt | `SalesService.kt` basic flow | **PENDING** |
| **M7** | Inventory Truth | `:shared:inventory` module created | **DONE** (Foundation only) |
| **M8** | Governance & Reporting | - | **PENDING** |
| **M9** | Sync & Offline Safety | `OutboxRepository.kt` | **PENDING** |
| **M10**| Release & Hypercare | - | **PENDING** |

## Active Milestone: M5 Catalog, Pricing & Cart
**Goal**: Memastikan produk bisa dicari, dimasukkan ke keranjang, dan harga dihitung dengan benar di shared domain.

### Required Artifacts for M5:
- [ ] Product Search & Barcode Lookup logic
- [ ] Cart State management in Shared Domain
- [ ] Pricing invariants (tax, discount baseline)

### Evidence for M4 (Closed):
- [x] Desktop UI for "Open Business Day" (`BusinessDayScreen.kt`)
- [x] State-driven navigation (Business Day -> Catalog)
- [x] Wiring to `BusinessDayService` in `:shared:kernel`
- [x] CI/CD Verification (`.github/workflows/ci.yml`)
