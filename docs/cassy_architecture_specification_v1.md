# Cassy Architecture Specification v1

## Document Overview
Prescriptive target architecture untuk KMP client, local-first SQLite, Go/PostgreSQL HQ backend, dan bounded-context modularization.

## Purpose
Mengunci boundary, dependency direction, sync posture, transaction rules, security posture, dan module/runtime responsibility.

## Scope
Android POS, Android Mobile selektif, Desktop Backoffice, HQ API, PostgreSQL HQ DB, client module map, sync strategy, security, dan CI baseline.

## Key Decisions / Core Rules
Client bersifat local-first; shared KMP dipakai untuk domain/application/data; presentation tetap native; sync dimodelkan eksplisit dengan outbox + batch/item/conflict; satu terminal adalah operational writer phase 1.

## Detailed Content

### Normalized Source Body
Cassy
Architecture Specification
Prescriptive target architecture for KMP clients, local-first SQLite, and Go/PostgreSQL HQ
backend
Document type
Detailed architecture design specification
Status
Prescriptive baseline for implementation handoff
Primary scope
Phase 1 Retail POS with reusable kernel for F&B and
Service extensions
Authoring date
2026-03-08
Repository target
GitHub Cassy monorepo (recommended baseline)
This document is intentionally prescriptive. It translates the project artefacts, your final architecture decisions,
and the selected technology stack into explicit module boundaries, runtime responsibilities, transaction rules,
sync rules, security rules, and CI structure.

## 1. Executive Summary
The target architecture for Cassy is a local-first, retail-first store system built around a shared Kotlin Multiplatform
core, platform-specific presentation modules, SQLite 3 via SQLDelight on each client device, and a Go REST HQ
backend with PostgreSQL. The architecture deliberately keeps the checkout path operational when store
connectivity degrades, while preserving auditability, explicit sync state, and convergence into one shared
sales/cash/inventory/reporting kernel across Retail, F&B, and Service channels.
Decision
Final choice
Architectural consequence
Client source of truth
A. Local-first
All business mutations are committed
to the local database first; sync is a
separate durable concern.
Phase 1 scope
A. Retail-first
Sales, returns, cash, inventory basics,
reporting, and sync are mandatory;
F&B and Service stay as prepared
extension boundaries.
KMP modularization
C. Hybrid
Modules are grouped by bounded
context, then layered inside each
context.
Code sharing depth
B. Share domain + application + data
Presentation remains platform-
specific; shared modules own business
rules, repositories, and sync
orchestration.
Sync strategy
C. Outbox + batch/item/conflict
orchestration
Sync is modeled as a business-visible
subsystem, not an implicit background
queue.
Offline multi-device
A. Single writer per device/terminal
Each terminal owns its local DB and
write stream; no peer-to-peer sync is
allowed in phase 1.
Identity strategy
A. UUIDv7 + business numbers
separate
Primary keys are opaque; receipt /
invoice / business-day numbers stay in
business columns.
Auth / approval offline
B. Hybrid auth + local approval PIN
Login is online-first, but cached grants
and supervisor PIN approval support
controlled offline operation.
The architecture is not server-first cache. It is also not shared-UI KMP. Those two shortcuts would look simpler at
first, but they would break either the offline requirement or the native UX requirement.

Figure 1. Target runtime architecture
## 2. Evidence Base and Design Drivers
This architecture is grounded in uploaded project artefacts plus external technical standards/documentation. The
internal artefacts establish the business boundaries and consistency rules; the external references constrain the
technology decisions so they remain implementable with the chosen stack.
Ref
Source
Why it matters to architecture
SRC-01
UML-Modeling-Source-of-Truth.txt
Defines the mandatory SDLC order and
architecture rule to separate UI, App
Shell, Application, Domain, Data,
Database, and External Service with
explicit dependency direction.
SRC-02
store_pos_domain_model_detail_specifi
cations_v2.pdf
Defines the bounded contexts and
aggregate roots: Sales, Returns,
CashierShift, BusinessDay,
InventoryBalance, SyncBatch, and Shared
Kernel support objects.
SRC-03
Store_POS_ERD_Specification_v2.pdf
Defines cross-business persistence
integration, identity strategy,
business_day, pos_terminal,
inventory_balance, stock_ledger_entry,
and sync orchestration entities.
SRC-04
store_pos_activity_detail_specifications.
pdf
Defines offline operation boundaries,
auth fallback conditions, sync
reconciliation flow, and recovery
expectations.
SRC-05
store_pos_sequence_detail_specification
s.pdf
Defines the critical interaction order for
sync, auth, audit, and end-of-day,
including transaction, retry, and partial-
failure concerns.
EXT-01
Kotlin Multiplatform official docs
Confirms that business logic can be
shared while keeping the UI native and
platform entry points separate.
EXT-02
SQLDelight official docs
Constrains how foreign keys and
migrations are configured per driver and
how SQLite is operated through
generated types.
EXT-03
SQLite official docs
Constrains foreign key enforcement,
WAL usage, and partial index strategy for
local-first workloads.
EXT-04
RFC 9562 UUIDv7
Provides the standard basis for time-
ordered, opaque primary keys.
EXT-05
Kotlin / GitHub Actions docs
Informs CI layout for KMP and Gradle
workflows.
### 2.1 Design drivers
- 
Retail-first launch must be buildable without re-architecting later when F&B and Service are activated.
- 
Offline mode is controlled degradation, not a blanket bypass. Operations permitted offline must leave a durable
reconciliation trail.
- 
Sensitive operations must remain auditable even when the network is unavailable.
- 
The codebase must remain readable and scalable for a small engineering team: explicit module boundaries
beat clever abstractions.
- 
The same sales / cash / inventory / reporting kernel must serve Retail directly and also receive settlements
from F&B and Service flows.

### 2.2 Explicit assumptions
- 
Desktop is treated as a JVM desktop client for backoffice and reconciliation duties. Presentation remains
desktop-specific and is not shared with Android.
- 
The recommended repository baseline is a monorepo because client contracts, SQLDelight schema, backend
API contracts, and migration policies must evolve together.
- 
Phase 1 enables F&B and Service only at the architecture boundary level. Their dedicated presentation and
workflow modules may ship later without changing the shared kernel.
## 3. Architecture Principles
1. Business correctness wins over pretty layering. Cross-context shortcuts are rejected when they weaken
invariants or auditability.
2. Dependencies always point inward. Platform frameworks, SQL, transport DTOs, and UI state do not leak into
the domain layer.
3. Current state and historical explanation are separate concerns. Example: inventory_balance is current state,
stock_ledger_entry is explanation trail.
4. All retriable write paths require idempotency semantics. Local retries and sync retries must not duplicate
business effects.
5. Local DB writes for one business decision must be atomic at the device boundary. Audit records and outbox
records are part of the decision, not optional extras.
6. The architecture is local-first on the client and authority-convergent at HQ. Local devices own immediate
operational continuity; HQ owns cross-store consolidation and global coordination.
7. Presentation is native per platform. Shared code stops at business logic, orchestration, repositories, and sync.
Non-goals for phase 1: event sourcing, peer-to-peer terminal replication, shared presentation framework, multi-
writer store node, and server-roundtrip dependency in the checkout happy path.
Figure 2. Dependency rules and module boundaries

## 4. System Boundary and Deployment Topology
### 4.1 In-scope runtime nodes
Node
Primary actor(s)
Primary responsibility
Android POS / Tablet
Cashier
Checkout, receipt, shift operations, local
device integration, and foreground sync
visibility.
Android Mobile
Supervisor / inventory staff
Approvals, stock tasks, reconciliation
follow-up, and operational status views.
Desktop Backoffice
Store manager
Business-day close, reports, failed-sync
reconciliation, and administration.
Go REST HQ API
System-to-system / authenticated clients
Master data distribution, inbound sync
ingestion, conflict responses, cross-store
business services, and reporting
aggregation.
PostgreSQL HQ DB
Backend only
Authoritative server-side persistence for
consolidated state, reporting, and
operational coordination.
### 4.2 Platform capability matrix
Capability
Android POS
Android Mobile
Desktop Backoffice
Primary business scope
Sales / payment / receipt /
shift
Approvals / inventory assist /
supervisory actions
Reports / reconciliation /
administration
Local hardware integration
High
Medium
Low to medium
Must remain offline-capable
Yes
Partial
Partial
Single-writer store role
Yes
No
No
Uses shared KMP core
Yes
Yes
Yes
Only the POS terminal is treated as a phase-1 operational writer for checkout and shift-critical flows. Other clients
can mutate selected contexts, but they must not become an implicit second checkout terminal.
## 5. Client Architecture (KMP + Native UI)
### 5.1 Layer model
Layer
Must own
Must not own
Platform UI module
Screens, view state, input validation
close to UX, platform navigation, UI-only
formatting
Business invariants, SQLDelight queries,
sync persistence logic
App Shell
Session bootstrap, permission gates,
feature flags, connectivity status,
dependency composition, terminal
binding
Domain rules or repository SQL
Application layer
Use cases, facades, transaction
boundaries, command/query handlers,
orchestration across one or more
aggregates
Android APIs, Compose widgets, raw
SQL, transport DTOs in public surface
Domain layer
Aggregates, value objects, domain
policies, decision results, invariants,
status models
Framework annotations, database
models, API clients
Data layer
Repository implementations, SQLDelight
adapters, HTTP sync adapters,
DTO/entity mapping, cache policy
Screen state, navigation, direct business
approvals bypassing application services

### 5.2 Sharing policy
- 
Shared in KMP: domain models, application services, repository interfaces, repository implementations,
SQLDelight access, sync orchestration, auth/session cache policy, and integration ports.
- 
Not shared: screen models tied to a specific platform UX, navigation graph, platform lifecycle handling,
printer/EDC driver UI, and platform-specific permission prompts.
- 
Consequence: the project gets one shared business core without forcing desktop and Android into a lowest-
common-denominator presentation model.
### 5.3 Prescribed client module map
repo/
apps/
android-pos/
app-shell/
feature-sales-ui/
feature-shift-ui/
feature-receipt-ui/
feature-sync-ui/
device-adapters/
android-mobile/
app-shell/
feature-approval-ui/
feature-inventory-ui/
feature-sync-ui/
desktop-backoffice/
app-shell/
feature-reporting-ui/
feature-reconcile-ui/
feature-admin-ui/
shared/
platform-core/
kernel/{domain,application,data}
masterdata/{domain,application,data}
sales/{domain,application,data}
returns/{domain,application,data}
cash/{domain,application,data}
inventory/{domain,application,data}
reporting/{domain,application,data}
sync/{domain,application,data}
auth/{application,data}
integrations/{hqapi,payment,identity,printer}
Rule: each shared bounded context is split by layer inside the context. Do not create one giant shared:data
module and do not let one feature talk to another feature's SQL tables directly. Cross-context calls go through
application facades or explicit reference objects only.
### 5.4 Module dependency rules
- 
UI modules may depend on app-shell composition plus application contracts from relevant contexts.
- 
Application modules may depend on their own domain, the shared kernel, and explicit ports/facades from
other contexts when traceable.
- 
Domain modules may depend only on shared kernel domain types and pure Kotlin utilities.
- 
Data modules implement ports and are the only place allowed to map SQLDelight rows, network DTOs, or
platform integration payloads.
- 
No module may import another context's internal SQLDelight generated queries directly.

## 6. Bounded Contexts and Ownership
The bounded contexts below come directly from the domain and ERD artefacts. The point is not taxonomy for its
own sake; it is to stop checkout, cash, inventory, and sync rules from collapsing into one procedural blob.
Context
Phase
Owns
Notable invariants
Kernel
P1
Reason codes, approvals, audit,
idempotency metadata,
lightweight refs
Audit is append-only; approval
context must be explicit.
Master Data
P1
Store, terminal, employee,
product, price policy, snapshots
Master data changes arrive
through controlled refresh or
sync.
Sales
P1
Sale aggregate, sale lines,
payment, receipt, suspended
sale
Completed sale requires zero
outstanding amount and valid
payment state.
Returns
P1
Return transaction, return lines,
policy decision, refund linkage,
store credit
Return settlement must not
exceed policy or purchase
history.
Cash
P1
Business day, cashier session,
opening cash, movements,
reconciliation, shift report
One active cashier-terminal
shift at a time; close requires
reconciliation.
Inventory
P1-basic
Inventory balance, stock ledger,
receiving, adjustments, cycle
count, damage disposition
Every stock change must have a
source event and ledger entry.
Reporting
P1-basic
Operational report snapshots
and query facades
Reports derive from validated
events and state, not ad hoc
counters.
Sync
P1
Outbox, sync batch, sync item,
conflict, offline window, master
snapshot refresh
Sync failure cannot disappear
without explicit terminal state.
F&B
Prepared
Order session, table session,
kitchen ticket, routing
Financial settlement converges
into shared sales/cash kernel.
Service
Prepared
Service order, status log,
service task, part usage
Part usage must converge into
shared inventory ledger and
sales/invoice settlement.
Critical anti-pattern to reject: letting Sales own ad hoc stock updates or letting F&B / Service create parallel sales
and cash ledgers. All channels converge into the same kernel.
## 7. Local Data Architecture (SQLite 3 + SQLDelight)
### 7.1 Database role
Each writer terminal owns a local SQLite database as the immediate source of truth for permitted operations.
SQLDelight is the typed access layer over that database. The local database is not a cache of the server; it is the
operational store for the device boundary.
### 7.2 Mandatory local transaction bundles
- 
Sale completion bundle: sale_transaction + sale_item updates, payment, receipt, cash effect (if any), inventory
ledger effect (if any), audit log, and outbox_event must commit atomically from the client point of view.
- 
Return bundle: return_transaction + return_line + refund effect + stock ledger effect + approval/audit + outbox.
- 
Shift close bundle: reconciliation + shift status update + audit + outbox.
- 
Inventory adjustment bundle: adjustment header/line + balance mutation + stock_ledger_entry +
approval/audit + outbox.
### 7.3 Local schema policy
- 
Use the ERD v2 entity set as the target schema, including business_day, pos_terminal, inventory_balance,
stock_ledger_entry, sync_batch, sync_item, sync_conflict, and offline_operation_window.
- 
Enable foreign keys on every SQLDelight driver configuration, not just in DDL scripts.

- 
Use WAL mode on the operational database to improve local read/write concurrency, but treat the DB as a
single-host file, not a network-shared asset.
- 
Use partial indexes for active rows, tombstones, and idempotency keys where query patterns justify them.
- 
Use optimistic version columns on sync-participating entities that can legitimately conflict across local and
remote state.
### 7.4 SQLDelight-specific rules
- 
All schema changes must be delivered as SQLDelight migration files and verified in CI.
- 
Generated query interfaces are hidden behind repositories or data sources; UI modules never call generated
queries directly.
- 
Write transactions are coordinated inside application services so business invariants remain visible in one
place.
### 7.5 Identity and numbering policy
Primary keys use prefixed UUIDv7 and stay opaque. Business-readable numbers such as receipt_no, invoice_no,
business_day_no, and transfer_no stay in dedicated columns. Do not embed store code, date, or sequence
meaning into the PK. For phase 1, the client may generate business numbers locally within store-and-terminal
policy because the immutable PK remains the true referential identity.
Concern
Prescribed rule
PKs
<prefix>_<uuidv7>
Readable numbers
Separate business columns, never foreign keys
Terminal uniqueness
terminal_id participates in cashier session uniqueness and
operational numbering scope
Idempotency
Required on retriable commands and syncable mutations
Versions
Required on conflict-prone sync entities
## 8. Sync and Offline Architecture
Sync is modeled as its own bounded context because failure, retry, conflict resolution, and offline windows have
direct business impact. The architecture rejects the idea that outbox_event alone is enough.
### 8.1 Sync pipeline
1. Application service commits a local business transaction and appends one or more outbox_event records.
2. Sync worker groups pending work into sync_batch and sync_item records.
3. Outbound batches are pushed to HQ. Response metadata updates sync_item and batch status.
4. Inbound master-data or status updates are pulled and applied through application services, not by raw table
mutation scripts.
5. Partial failure creates explicit sync_conflict or failed statuses. Nothing is silently dropped.
6. Manager/supervisor reconciliation UI works against sync_batch/item/conflict state, not raw log scraping.
### 8.2 Offline policy by default
Flow
Offline default
Reason
Open shift / opening cash
Allowed
Store cannot wait for network to begin
operations.
Cash sale / standard checkout
Allowed
Core operational continuity requirement.
Receipt issue / reprint
Allowed
Customer-facing continuity requirement.
Supervisor approval by local PIN
Allowed
Needed for controlled exceptions when
identity service is unavailable.
Return / refund
Restricted
Allowed only when policy snapshot and
original-sale reference are available;
otherwise defer.
Business-day close
Conditionally allowed
May close locally with pending sync flags,
but only if critical readiness checks pass.
Inventory adjustment
Restricted
Allowed only for permitted reason codes

and thresholds.
### 8.3 Conflict resolution policy
- 
Business data does not use blanket last-write-wins.
- 
Master data snapshots are generally server-authoritative unless policy explicitly allows local override windows.
- 
Financially sensitive entities (sale, payment, cash reconciliation, business day close) require explicit
reconciliation if versions diverge.
- 
Inventory conflicts resolve at document/event level where possible, not by silently overwriting balance rows.
### 8.4 Single-writer rule
Phase 1 permits one operational checkout writer per device/terminal and does not support peer-to-peer terminal
replication. This is intentional. It keeps cash, numbering, and stock ledgers explainable. If future requirements
demand multi-writer offline within one store, the architecture must evolve toward a local hub/store node rather
than allowing uncontrolled direct device-to-device divergence.
## 9. Backend Architecture (Go REST API + PostgreSQL)
The backend is a central business API and sync authority, not just a thin BFF. It owns HQ-level validation,
consolidation, master data publication, reconciliation endpoints, and long-horizon reporting state.
### 9.1 Recommended backend package structure
backend/
cmd/cassy-api/
internal/
platform/{httpx,authn,authz,clock,idgen,tx,logging}
kernel/{approval,audit,reason}
masterdata/{domain,app,repo,http}
sales/{domain,app,repo,http}
returns/{domain,app,repo,http}
cash/{domain,app,repo,http}
inventory/{domain,app,repo,http}
reporting/{domain,app,repo,http}
sync/{domain,app,repo,http}
fb/{domain,app,repo,http}
service/{domain,app,repo,http}
db/
postgres/migrations/
queries/
### 9.2 API surface categories
API category
Examples
Notes
Authentication & session
/auth/login, /auth/refresh,
/auth/permissions
Online-first; client may cache constrained
grants for offline fallback.
Master data snapshot
/master-data/products,
/master-data/prices, /master-data/policies
Supports versioned pull and selective
refresh.
Sync ingestion
/sync/batches, /sync/reconcile
Primary write path for local-first client
mutations.
Operational commands
/business-days/close, /reports/xz,
/inventory/review
Used when a business action is better
validated centrally or after sync.
Reconciliation & admin
/sync/conflicts, /cash/reconcile,
/reports/operational
Backoffice and supervisor flows.

### 9.3 Postgres role
- 
PostgreSQL is the authoritative server store for consolidated multi-store state and analytics-ready operational
data.
- 
Server tables keep the same business identity rules as the client schema: opaque PKs, separate business
numbers, explicit versioning where needed.
- 
HQ write paths must be idempotent and able to recognize replayed client mutations by idempotency key and
aggregate identity.
### 9.4 Backend transaction policy
- 
Server applies each inbound client mutation in a transaction that validates business invariants, writes audit
state, and returns explicit acceptance / conflict / rejection semantics.
- 
Do not make the server call the client back synchronously to complete core financial operations.
- 
Derived reporting rows or denormalized summary tables are downstream concerns; they must not replace the
validated operational model as source of truth.
## 10. Security, Authorization, and Approval Architecture
Authentication is online-first. Authorization for sensitive flows is hybrid: the system may use cached grants and a
supervisor PIN path when the network or identity service is unavailable, but every approval must still produce
explicit local audit and approval state for later sync.
Concern
Prescribed architecture
Primary login
Online authentication against HQ identity / auth service.
Offline fallback
Signed or cached session profile with scoped TTL and role grants
appropriate for offline policy.
Sensitive approval
Supervisor PIN or approved local credential path tied to
approval_request, reason_code, actor, timestamp, and device
context.
Audit
Append-only local audit_log first, then sync / forward. Sensitive
action must not complete without durable local audit intent.
Permission model
Role-based baseline plus explicit permission checks in application
layer for void, manual discount, refund, cash variance, and stock
adjustment thresholds.
Do not bury approval logic inside UI conditionals. Approval is an application-level decision that must leave
domain-visible evidence.
## 11. Observability and Operational Control
- 
Every business mutation must carry actor context, store_id, terminal_id where relevant, and
correlation/idempotency metadata.
- 
Sync dashboards must read from sync_batch, sync_item, and sync_conflict rather than inferring state from logs
only.
- 
Backoffice must expose pending offline windows, failed batches, unresolved conflicts, and business-day
readiness blockers.
- 
Client logging may be sampled, but audit events for sensitive actions are not optional telemetry.
## 12. CI / CD Architecture (GitHub Actions)
The workflow design must reflect the module split and the chosen test scope (unit tests first). The goal is fast PR
feedback plus stronger main-branch guarantees without building a brittle mega-pipeline.
Workflow
Runner
Required jobs
ci-shared.yml
ubuntu-latest
Gradle setup, shared module compile, unit
tests for domain/application/data,

SQLDelight migration verification, static
analysis.
ci-android.yml
ubuntu-latest
Assemble Android targets, unit tests,
packaging checks for android-pos and
android-mobile.
ci-desktop.yml
ubuntu-latest
Build desktop-backoffice JVM app and run
desktop module unit tests.
ci-backend.yml
ubuntu-latest
go test ./..., build cassy-api, migration
checks, API contract/unit tests.
release.yml
tag or manual
Versioning, artifact packaging, changelog,
and optional deployment gates.
### 12.1 CI policy rules
- 
Use Gradle wrapper and a dedicated Gradle setup action in CI.
- 
Treat SQLDelight schema and migration verification as first-class build steps, not optional scripts.
- 
Run PR-fast checks on every pull request; reserve longer packaging or release jobs for main branch / tags.
- 
Keep backend and shared schema changes in the same pull request when they change sync contracts or
persistence semantics.
## 13. Recommended Monorepo Structure
Cassy/
apps/
android-pos/
android-mobile/
desktop-backoffice/
shared/
... bounded contexts and integrations ...
backend/
... Go API ...
docs/
architecture/
database/
implementation/
test/
tooling/
scripts/
ci/
.github/workflows/
Reason for monorepo recommendation: the same business decision often changes KMP use cases, SQLDelight
schema, sync contract, backend validation, and CI checks at once. Splitting these too early into separate
repositories usually creates version drift and weak review traceability.
## 14. Implementation Guardrails
Do this
Reject this
Expose one application facade per critical business flow.
Let UI compose raw repositories and SQL queries itself.
Keep domain models pure and persistence-agnostic.
Annotate domain types with transport or SQL framework concerns.
Write audit and outbox intent in the same local transaction as the
business decision.
Emit audit or sync records in best-effort fire-and-forget background
coroutines.
Keep one shared sales/cash/inventory/reporting kernel.
Create separate ledgers for Retail, F&B, and Service.
Treat sync as explicit stateful orchestration.
Hide sync failure behind retries with no visible state machine.
Model current state and ledger/history separately.
Use one mutable table as both balance and audit trail.

### 14.1 Critical invariants to encode early
- 
One active business day per store.
- 
One active cashier shift per cashier-terminal pair at a time.
- 
Sale completion requires valid settlement state and cannot be retroactively "fixed" by background sync only.
- 
Every stock-affecting operation produces stock_ledger_entry with a clear source.
- 
Unresolved sync conflicts remain visible until resolution_status is explicit.
## 15. Decision Log
ID
Decision
Status
D-01
Client uses local-first operational
persistence.
Accepted
D-02
Phase 1 is Retail-first; F&B and Service
are extension-ready only.
Accepted
D-03
KMP uses hybrid modularization by
context then layer.
Accepted
D-04
Shared code includes domain,
application, and data; presentation
remains platform-specific.
Accepted
D-05
Sync uses outbox + batch/item/conflict
model.
Accepted
D-06
One writer DB per operational
terminal/device; no peer-to-peer offline
sync.
Accepted
D-07
PKs use prefixed UUIDv7; readable
business numbers are separate.
Accepted
D-08
Auth is online-first with offline cached
grants and supervisor PIN approvals.
Accepted
D-09
Monorepo is the recommended
repository baseline.
Recommended
D-10
Desktop is assumed to be a JVM desktop
client for backoffice responsibilities.
Assumed
## 16. Appendix A - Architecture-to-Implementation Mapping
Architecture concern
Implementation baseline
Store terminal binding
pos_terminal table + terminal bootstrap in app-shell + terminal-
aware numbering scope
Checkout write path
sales application facade + SQLDelight transaction + outbox append
+ receipt adapter
Shift open/close
cash context application services + business_day checks +
reconciliation records
Inventory movement
inventory application service writes balance and ledger together
Sync visibility
sync context tables surfaced to POS/backoffice UI
Offline approval
approval_request + audit_log + local PIN validation adapter
HQ ingestion
Go sync service validates idempotency and aggregate version rules
before commit
## 17. Appendix B - External Technical References Used
- 
Kotlin Multiplatform official documentation on sharing business logic while keeping platform-specific entry
points and UI.
- 
SQLDelight official documentation for foreign key configuration and migration handling.
- 
SQLite official documentation for foreign key enforcement, partial indexes, and WAL mode.
- 
RFC 9562 for UUIDv7 as time-ordered identifiers.
- 
Kotlin / GitHub Actions official guidance for KMP CI and Gradle setup.

## 18. Closing Note
This document is intentionally not a generic clean-architecture memo. It is a target-state architecture for Cassy as
currently defined: retail-first, local-first, audit-heavy, sync-explicit, and prepared for F&B plus Service convergence
without duplicating the operational kernel. The next artefact after this document should be the implementation
specification: module contracts, interface signatures, transaction coordinator rules, migration plan, and test
mapping derived from these boundaries.


## Constraints / Policies
Tidak boleh jatuh ke server-first cache atau shared-UI penuh yang melanggar boundary device-heavy.

## Technical Notes
Dokumen ini preskriptif dan harus diprioritaskan terhadap snapshot repo saat ini.

## Dependencies / Related Documents
- `uml_modeling_source_of_truth.md`
- `store_pos_domain_model_detail_specifications_v2.md`
- `store_pos_erd_specification_v2.md`
- `cassy_module_project_structure_specification.md`
- `cassy_cicd_pipeline_strategy_v1.md`
- `cassy_event_contract_sync_specification_v1.md`
- `cassy_migration_script_specification.md`
- `cassy_auth_strategy_phase_1.md`

## Risks / Gaps / Ambiguities
- Tidak ditemukan gap fatal saat ekstraksi. Tetap review ulang bagian tabel/angka jika dokumen ini akan dijadikan baseline implementasi final.

## Reviewer Notes
- Struktur telah dinormalisasi ke Markdown yang konsisten dan siap dipakai untuk design review / engineering handoff.
- Istilah utama dipertahankan sedekat mungkin dengan dokumen sumber untuk menjaga traceability lintas artefak.
- Bila ada konflik antar dokumen, prioritaskan source of truth, artefak yang paling preskriptif, dan baseline yang paling implementable.

## Source Mapping
- Original source: `Cassy_Architecture_Specification_v1.pdf` (PDF, 13 pages)
- Output markdown: `cassy_architecture_specification_v1.md`
- Conversion approach: text extraction -> normalization -> structural cleanup -> standardized documentation wrapper.
- Note: beberapa tabel/list di PDF dapat mengalami wrapping antar baris; esensi dipertahankan, tetapi layout tabel asli tidak dipertahankan 1:1.
