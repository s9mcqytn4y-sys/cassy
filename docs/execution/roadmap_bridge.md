# Cassy Roadmap Execution Bridge (PDF v1.1 -> Repo)

This document maps the strategic milestones from the PDF Roadmap to the actual repository state.

## Milestone Status Tracker

| ID | Milestone Name | Repo Evidence | Status |
|:---|:---|:---|:---|
| **M0** | Program Setup | `AGENTS.md`, `.agent/` structure | **DONE** |
| **M1** | Scope Lock V1 | `AGENTS.md` (Strategic Lock section) | **DONE** |
| **M2** | Architecture Control Plane | Multi-module Gradle, SQLDelight cleanup | **DONE** |
| **M3** | Desktop Store Bootstrap | `apps/desktop-pos` initialization | **IN PROGRESS** |
| **M4** | Business Day & Shift | `BusinessDayService.kt` in `:shared:kernel` | **IN PROGRESS** |
| **M5** | Catalog & Cart | `CatalogViewModel.kt` in `:shared` | **PENDING** |
| **M6** | Checkout & Receipt | `SalesService.kt` basic flow | **PENDING** |
| **M7** | Inventory Truth | `:shared:inventory` module created | **DONE** (Foundation) |
| **M8** | Governance & Reporting | - | **PENDING** |
| **M9** | Sync & Offline Safety | `OutboxRepository.kt` | **PENDING** |
| **M10**| Release & Hypercare | - | **PENDING** |

## Active Milestone: M3 & M4 Convergence
**Goal**: Menjadikan Desktop sebagai operational writer yang bisa membuka Business Day.

### Required Artifacts for M3/M4:
- [ ] Desktop UI for "Open Business Day"
- [ ] Role-based PIN access check
- [ ] Terminal-to-Shift binding logic
