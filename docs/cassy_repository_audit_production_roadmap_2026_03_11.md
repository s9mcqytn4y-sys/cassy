# Cassy Repository Audit Production Roadmap 2026 03 11

## Document Overview
Audit brutal terhadap snapshot repository terbaru dan roadmap produksi yang memisahkan kondisi repo saat ini dari target architecture.

## Purpose
Mengidentifikasi gap engineering paling mahal dan memberi roadmap refactor bertahap menuju repo production-ready.

## Scope
Deps Risk Exit Rollback Owner A. Architectur e & Modulariza tion Freeze coupling, finalize namespace bridge, define app- shell/modul e map. Pre-req semua stream. Rename blast radius, import churn. Root build stable, shell modules defined, AppContain er sunset rule documente d....

## Key Decisions / Core Rules
Repo saat ini tidak boleh dijadikan patokan final; focus diarahkan ke staged restructuring yang taat source-of-truth design artefacts.

## Detailed Content

### Normalized Source Body
CASSY
Repository Audit Brutal + Production-Ready
Roadmap
Update Basis: latest pushed repository snapshot + source artefacts (.md) observed on 11 March
2026
Strategic Override Applied: Desktop diprioritaskan sebagai retail operational E2E client,
bukan sekadar backoffice.
Dokumen ini memisahkan state repo saat ini dari target architecture. Repo yang berjalan
hari ini tidak diperlakukan sebagai kebenaran. Source of truth tetap directive terbaru + artefak
Cassy + docs, lalu repo diukur sebagai implementation snapshot yang harus dikoreksi.
Authoring mode: architecture review + engineering handoff + QA/release review

Executive Scope
- Audit ini berbasis snapshot repo GitHub Cassy yang diobservasi pada branch main, artefak
markdown hasil konversi source-of-truth, dan keputusan final pengguna untuk memprioritaskan
Desktop sebagai frontline retail client.
- Audit ini tidak menyamakan baseline arsitektur lama dengan kebutuhan baru. Dokumen source-of-
truth lama yang memposisikan Desktop sebagai backoffice diperlakukan sebagai baseline historis
yang telah ditimpa oleh arah bisnis terbaru.
- Live Gradle sync/build penuh tidak dieksekusi pada sesi ini karena working tree lokal tidak ter-
mount ke environment eksekusi. Karena itu bagian build-validation disajikan sebagai static
inference yang harus divalidasi ulang di CI/local engineering lane.
Source Priority Applied
- Directive eksplisit pengguna pada prompt ini.
- Artefak/source-of-truth Cassy (arsitektur, struktur modul, ERD, migrasi, sync, test, CI/CD,
traceability, UI/UX).
- Kondisi repo terbaru yang sudah di-push.
- Asumsi auditor, hanya bila evidence belum tersedia.
Document Map
## 1. Quick Feedback
## 2. The Critique
## 3. Current State vs Target State
## 4. Traceability Map
## 5. Layer-by-Layer Audit
## 6. Platform Audit - Android vs Desktop
## 7. Database / Migration / Query Audit
## 8. Legacy / Traditional Elimination List
## 9. Roadmap Production-Ready
## 10. Phase Plan
## 11. 30-60-90 Day Plan
## 12. Testing & Automation Blueprint
## 13. CI/CD & Release Blueprint
## 14. Immediate Backlog
## 15. Final Recommendation
## 16. Appendix - Evidence Base

Quick Feedback
Verdict singkat, biaya teknis paling mahal, dan keputusan yang tidak boleh ditunda.
Verdict. Cassy belum production-ready, belum sehat secara ownership architecture, dan belum
jujur terhadap target dual operational clients. Android masih transisional; Desktop masih jauh dari
frontline retail E2E; shared core masih bercampur antara split sungguhan dan sisa monolith lama.
5 masalah paling mahal
No
Masalah
Kenapa mahal
Pseudo-modularization
Android dan Desktop masih
bergantung pada giant :shared
sekaligus modul context baru.
Ini memberi ilusi progress,
tetapi boundary nyata masih
bocor.
AppContainer masih blob
migrasi
Ia merangkai monolithic
CassyDatabase,
Kernel/Cash/SalesDatabase,
repository lama, facade baru,
printer, permission, security,
seed data, F&B, dan service
flow dalam satu tempat.
Desktop belum viable sebagai
retail execution
Desktop masih sebatas shell
navigasi. Belum ada bukti flow
open shift, commit sale,
tender, receipt, audit, sync
visibility, atau packaging
readiness yang pantas untuk
frontline operation.
Inventory evacuation belum
benar-benar terjadi
Inventory module sudah ada,
tetapi runtime utama masih
memakai
InventoryRepositoryImpl(data
base) dan
LedgerRepositoryImpl(databa
se) dari monolith legacy.
Namespace, build topology,
dan CI target belum final
Repo masih campur
com.azure, com.cassy, dan
id.azureenterprise.cassy. Ini

akan menjadi technical debt
build, test, package ownership,
dan release traceability.
5 keputusan paling mendesak
- Freeze fitur baru yang menambah coupling sampai logic evacuation berjalan nyata.
- Finalisasi namespace melalui controlled bridge cutover, bukan rename kosmetik separuh jalan.
- Tetapkan Desktop sebagai writer operasional penuh per node/terminal sendiri, tanpa DB-sharing ke
Android dan tanpa shortcut client-server ke terminal lain.
- Turunkan AppContainer menjadi migration bridge terbatas di app-shell Android dan Desktop, lalu
siapkan sunset path ke composition root sehat.
- Jadikan inventory evacuation + sync hardening + migration safety sebagai prioritas sebelum
menambah scope channel/fitur baru.
Can it continue incrementally?
Ya, tetapi tidak secara naif. Bukan full rewrite, namun beberapa area butuh hard reset lokal: app
composition, inventory ownership, sync persistence model, namespace finalization, dan desktop
operational foundation.

The Critique
Pisahkan fakta, asumsi, risiko, deviasi, dan anti-pattern. Jangan biarkan repo saat ini membela dirinya sendiri.
Fakta
- Root Gradle saat ini meng-include :shared, context modules
(:shared:core, :shared:kernel, :shared:cash, :shared:sales, :shared:inventory, :shared:returns, :share
d:sync, :shared:reporting), serta :apps:android dan :apps:desktop. Backend belum menjadi bagian
dari active Gradle topology.
- Android app masih memakai namespace com.azure.cassy, shared monolith masih com.cassy.shared,
sedangkan modul baru seperti inventory/kernel/cash/sales sudah mulai memakai
id.azureenterprise.cassy.*.
- Android shell masih menarik top-level menu Kasir, Jasa, F&B, Kas, Produk, Stok, Riwayat,
Pengaturan. Ini berarti F&B dan Service masih hadir di shipping navigation, bukan hanya
prepared boundary.
- AppContainer masih menyatukan CassyDatabase (legacy monolith) dengan KernelDatabase,
CashDatabase, dan SalesDatabase pada driver yang sama, sekaligus menyusun
repository/facade/platform adapter dalam satu blob runtime.
- Desktop app terbaru masih berupa window + navigation rail + placeholder content. Itu belum bisa
disebut retail operational client.
- Inventory module memang sudah ada di Gradle, tetapi operasi stok yang aktif di Android masih
bertumpu pada repository/ledger legacy dari monolithic DB path.
- Kernel schema baru sudah mulai menambahkan reason_code lintas kategori, audit_log,
pos_terminal, business_day, dan approval_request. Cash queries baru juga sudah menambahkan
terminal_id dan business_day_id ke cashier_session.
Asumsi eksplisit
- Snapshot GitHub main yang diobservasi adalah representasi cukup dekat dengan repo lokal terbaru
yang Anda maksud.
- Artefak markdown tanggal 8-10 Maret 2026 adalah source-of-truth aktif, kecuali area yang ditimpa
langsung oleh directive baru pengguna.
- Tidak ada bukti pada sesi ini bahwa Desktop sudah punya implementasi flow retail penuh di branch
lain atau di working tree lokal yang belum di-push.
Risiko utama
- False modularization: tim merasa sudah split module, padahal dependency direction dan ownership
runtime belum bersih.
- Parity drift: Android dan Desktop tumbuh dengan rule bisnis berbeda karena shared core belum
dibatasi dengan keras.
- Release failure: desktop packaging terlihat ada, tetapi ternyata tidak melindungi workflow
operasional yang sebenarnya.

- Data correctness failure: inventory, cash, audit, outbox, dan approval bisa tetap berjalan dari legacy
path walau schema target-state mulai diperkenalkan.
- Migration failure: multi-database SQLDelight berjalan dalam mode transisi tanpa ownership chain
yang final dan tanpa bukti replay gate yang dieksekusi.
Deviasi terhadap source-of-truth
- Source-of-truth lama mengasumsikan Desktop sebagai backoffice/reconcile client. Directive baru
mengubah ini menjadi desktop retail operational E2E. Perubahan arah ini valid, tetapi mahal
secara app-shell, DB ownership, printer/barcode boundary, packaging, dan QA matrix.
- Module Structure Specification menuntut giant shared dipecah per bounded context dan service
locator dipensiunkan bertahap. Repo terbaru masih membawa :shared monolith dan
AppContainer blob.
- ERD v2 menuntut inventory_balance + stock_ledger_entry, business_day, pos_terminal,
sync_batch/item/conflict, dan return aggregate. Implementasi baru hanya menutup sebagian gap;
sisanya masih berada di legacy model.
- CI/CD spec menuntut reusable workflows, stable required checks, FK/migration verification, dan
mandatory Desktop lane. Belum ada bukti implementasi penuh terhadap target workflow topology
itu pada snapshot yang diobservasi.
Anti-pattern yang terdeteksi
- Giant :shared blob tetap hidup di samping bounded-context modules.
- AppContainer/service locator menjadi arsitektur terselubung, bukan sekadar bridge tipis.
- F&B/Service bocor ke retail shipping nav dan runtime, padahal harusnya prepared boundary.
- Legacy monolith database dan new bounded-context database hidup bersamaan tanpa ownership
final yang tegas.
- Desktop masih pseudo-support: ada app, tetapi bukan operational retail system yang dapat
dipilotkan secara jujur.

Current State vs Target State
Peta repo aktual, target-state, dan gap modul per bounded context.
Repository map - actual vs target
Area
Current State
Target State
Gap / Verdict
Root build
Minimal include
topology; backend
tidak aktif di Gradle
Monorepo aktif: apps
+ shared + backend +
tooling + workflows
Target topology belum
hidup penuh
Android
Satu app besar
apps/android
android-pos/app +
app-shell + platform-
device + feature/UI
split
Masih monolith app
Desktop
Compose shell
placeholder
desktop retail
operational client
dengan app-shell
sendiri
Jauh dari viable
Shared
shared monolith +
beberapa module
baru
shared/platform-core
+ per-context
domain/application/d
ata + shared compose
selectively
Split masih separuh
jalan
Database
Legacy CassyDatabase
+ beberapa DB context
baru
DB ownership per
context dengan
migration chain jelas
Transisional dan
riskan
Inventory
Masih legacy
inventory repo/ledger
runtime
inventory
domain/application/d
ata +
inventory_balance/sto
ck_ledger ownership
Evacuation belum
nyata
CI/CD
Root tasks lokal dan
target docs lebih maju
dari repo
PR fast gate, mainline,
nightly, release,
backend lane, desktop
lane
Dokumen lebih maju
dari implementasi
Bounded context gap map
Context
Docs Target
Repo Evidence
Gap Level
Action

Kernel
Active
New schema +
repos introduced
Medium
Continue
hardening, move
approvals/audit
fully here
Cash
Active
New
CashDatabase
and cashier/cash
session queries
introduced
Medium
Finish safe_drop,
reconciliation,
shift report
ownership
Sales
Active
New
SalesDatabase
introduced but
checkout
runtime still
transitional
High
Move
submit/void/recei
pt path off legacy
repository chain
Returns
Active
Target-state docs
strong,
implementation
evidence weak
High
Create real
return aggregate
before shipping
advanced refund
Inventory
Active
Module exists
but runtime still
legacy-led
Critical
Wave-4
evacuation must
be real, not
package move
Reporting
Active selective
Docs strong,
desktop/report
runtime weak
High
Build read-
model/report UI
intentionally
Sync
Active
Docs strong;
runtime
evidence mostly
outbox-era
Critical
Introduce
batch/item/confli
ct visibility and
persistence
F&B
Prepared
boundary
Still in Android
shipping
nav/runtime
Critical
Quarantine from
retail kernel
shipping path

Android vs Desktop retail execution gap
- Mandatory parity: login/access gate, business day/open shift, product lookup, cart, pricing/totals,
tender/payment state, commit sale, receipt preview/reprint, inventory impact trace, sync/outbox
visibility, close shift, close business day.
- Allowed controlled divergence: printer pairing UX, barcode capture UX, payment terminal SDK
wiring, packaging/distribution channel, device diagnostics, keyboard/touch ergonomics.
- Temporary platform-specific gap yang masih bisa ditoleransi: scanner lifecycle fidelity, Bluetooth
discovery, cash drawer integration, certain supervisor-only tools.
- Unacceptable inconsistency: berbeda pricing engine, berbeda tax/discount rule, sales mengubah
stok secara liar di satu platform saja, audit/outbox semantics berbeda, numbering/business-day
scope berbeda, ataupun reprint dibangun dari snapshot yang berbeda.

Traceability Map
Turunkan alur bisnis ke activity, sequence, domain, database, implementation, dan test gate.
Critical flow traceability matrix
Use Case
Act/Seq
Domain
DB
Android
Desktop
Test
Verdict
Open
Business
Day +
Start Shift
Yes
Business
Day/
CashierSh
ift
Partially
present
Partial
Missing
Weak
Not
productio
n-safe
Checkout
+
Payment
+ Finalize
Sale
Yes
Sale/
Payment/
Receipt
Partial
legacy/ne
w mix
Partial
Missing
Weak
Cannot
claim
dual-
client
readiness
Receipt/
Reprint
Yes
Receipt
snapshot
Legacy
path
exists
Partial
Missing
Weak
Needs
evidence-
safe final
snapshot
path
Cash
In/Out +
Safe Drop
Yes
CashMov
ement/
SafeDrop
Partial
Partial
Missing/
Selective
Weak
Docs
ahead of
repo
Inventory
Lookup +
Adjustme
nt
Yes
Inventory
Balance/
StockLedg
er
Legacy-
heavy
Partial
Missing
Weak
Current-
state vs
ledger
still
unsafe
Sync
Visibility
+ Conflict
Reconcile
Yes
SyncBatc
h/
SyncConfl
ict
Mostly
target-
state docs
Weak
Weak
Weak
Critical
missing
capability
Close
Shift /
Close Day
Yes
CashReco
nciliation/
Business
Day
Partial
Partial
Missing/
Selective
Weak
Desktop
parity not
achieved

Traceability conclusion
The repo is document-rich but implementation-thin. Traceability exists strongly from docs to
target-state, but weakly from runtime Android/Desktop code to final architectural intent. The risk
is not lack of vision; the risk is false completion.

Layer-by-Layer Audit
Status, temuan, risiko, refactor recommendation, dan prioritas per layer.
Layer
Status
Temuan
Refactor
Recommendation
Priority
UI
Amber
Android UI
already hosts too
many
operational and
non-retail
surfaces; Desktop
UI is still a
placeholder shell.
Define retail
feature map,
quarantine
F&B/Service,
build desktop
retail screens
intentionally.
P0
App Shell
Red
App shell
responsibility is
not isolated;
AppContainer
still carries too
much runtime
knowledge.
Create
AndroidShellMod
ule and
DesktopShellMod
ule as explicit
composition
roots.
P0
Application
Amber
Some new use
cases exist by
context, but
checkout/invento
ry still
orchestrate
through
transitional
facades.
Move critical
flows to explicit
application
services by
context.
P0
Domain
Amber
Kernel/cash/sales
domain
emergence is
visible, but
invariant
coverage is
incomplete.
Lock invariants
around
business_day,
cashier-terminal
uniqueness, sale
finality, stock
explanation trail.
P1
Data
Red
Legacy and new
repositories
coexist without
Inventory,
returns, sync,
receipt, and
P0

final ownership
boundary.
payment
persistence must
move under
target context
modules.
Database
Red
Monolithic and
bounded-context
DBs coexist over
same driver;
migration
verification not
proven live here.
Define one
operational DB
ownership plan
and replay
verification
policy.
P0
Integrations
Amber
Printer/
permissions exist
in Android, but
platform-device
separation is not
mature; Desktop
integration path
thin.
Split platform-
device modules
and ports per
app.
P1
Backend/HQ/
Sync
Red
Docs define
strong HQ
boundary;
Gradle repo
snapshot has no
active backend
module path.
Prepare
contracts now;
do not block
phase-1 checkout
on server.
P1
CI/CD
Red
Target pipeline is
well specified in
docs; repo
implementation
evidence is
behind.
Build PR gate,
shared-fast,
desktop-fast,
backend-fast,
migration/FK
gates.
P0
QA/QE/Test
Automation
Red
Docs strong,
implementation
gate weak.
Build
common/compon
ent/integration
first; device later.
P0

Platform Audit - Android vs Desktop
Bandingkan readiness nyata, bukan niat desain.
Area
Android
Desktop
Audit Verdict
Retail flow readiness
Partial
Low
Android has more
flow surface; Desktop
not ready
App shell maturity
Low/Partial
Low
Neither has healthy
final composition root
Dependency
composition
Blob/Transitional
Thin/Placeholder
Both need explicit
shell modules
Local DB ownership
Exists but mixed
legacy/new
Not evidenced enough
Desktop must get its
own file-based DB per
node
Printer/barcode
capability
Some Android-specific
path exists
Not evidenced
Desktop parity absent
Operator workflow
Prototype POS-like
Placeholder
navigation
Desktop not ready for
frontline use
Deployment model
Likely self-contained
app
Package shell only
Desktop rollout model
must be redesigned
Packaging/
distribution
Android assemble
exists
Compose desktop exe
baseline exists
Packaging exists ≠
operational readiness
Testability
Weak but salvageable
Very weak
Desktop functional
lane not alive yet
Release readiness
No
No
Parity not achieved
Parity conclusion
- Parity achieved: belum.
- Parity not achieved: retail execution foundation, local DB evidence, printer/barcode/device
integration, packaging hardening, and operational test coverage.
- Parity deferred: some hardware-specific integration details may legitimately diverge.
- Parity impossible in current architecture: full dual-platform frontline retail without first shrinking
AppContainer/blob dependency and completing inventory/sync migration would be reckless.

Database / Migration / Query Audit
SQLite local, SQLDelight, migration replay readiness, integrity hotspot, and sync compatibility.
What is improving
- Kernel schema now explicitly introduces reason_code with wider categories, audit_log,
pos_terminal, business_day, and approval_request.
- Cash queries now include terminal_id and business_day_id on cashier_session and cash_session,
showing movement toward the target-state session model.
- Bounded-context SQLDelight databases already exist for kernel, cash, sales, and inventory modules.
This means the repo has started the migration, not merely documented it.
What is still wrong
- Legacy CassyDatabase still anchors many critical repositories: product, sale, receipt, inventory,
ledger, payment, invoice, refund, report, device, service, and F&B.
- Inventory runtime still hangs off InventoryRepositoryImpl(database) and
LedgerRepositoryImpl(database), so the new inventory module is not yet the owner of stock truth.
- Cash still lacks explicit safe_drop and cash_reconciliation entities in the new DB path that match
target-state maturity.
- Return/refund still appears behind legacy refund/invoice paths rather than a first-class return
aggregate.
- Sync orchestration target tables (sync_batch, sync_item, sync_conflict, master_data_snapshot,
offline_operation_window) remain a source-of-truth demand more than a repo-proven runtime
path.
- Multi-database ownership over one driver is a valid migration tactic only if chain ownership,
migration replay order, and cutover checkpoints are explicit. Right now that discipline is not yet
evident enough.
Dangerous shortcuts to reject
- Do not let sales mutate stock directly without inventory application boundary.
- Do not use current-state table as explanation trail.
- Do not emit audit/outbox as fire-and-forget after core mutation commits.
- Do not let Desktop read/write Android terminal DB over local network share.
- Do not infer sync state from logs instead of sync tables and explicit status models.
Build / migration validation status
Status: not live-validated in this session. The repo was observed as a pushed snapshot, but the
working tree was not executed here. Therefore the document includes probable blocker analysis
rather than a dishonest claim of successful Gradle sync.
- Probable blocker 1: root quality gate is narrower than the target-state scope and does not prove
backend, desktop package, or all migration chains.

- Probable blocker 2: namespace drift can surface during KMP/refactor/test packaging and future
move operations.
- Probable blocker 3: multi-database migration verification will fail or become untrustworthy if
ownership and ordered migration policy are not aligned per bounded context.

Legacy / Traditional Elimination List
Apa yang harus dibunuh, kenapa salah, dan apakah boleh di-bridge sementara.
Temuan
Kenapa salah
Short-term
impact
Long-term
impact
Pengganti
Bridge?
Giant :shared
blob
Karena
mencampur
ownership
lintas domain
dan membuat
split modul
palsu.
Blast radius
rebuild,
review
ownership
kabur.
Dependency
hell dan
parity drift.
Bounded-
context split +
layer split.
Bridge
sementara:
ya, tapi time-
boxed.
AppContainer
blob
Karena
menjadi
service
locator,
composition
root, runtime
registry,
seed/bootstra
p, dan
integration
bag sekaligus.
Testing sulit,
wiring kabur.
Arsitektur
permanen
terselubung.
Platform shell
modules +
Koin/manual
DSL
migration.
Bridge
sementara:
ya, hanya di
app-shell.
F&B/Service
di retail nav
Karena
prepared
boundary
diperlakukan
seperti
shipping
scope.
Retail focus
buyar.
Kernel
terkontamina
si fitur dini.
Quarantine
ke feature
flag/boundary
modules.
Bridge
sementara:
tidak di nav
utama.
Legacy
inventory
repo as stock
owner
Karena
menahan
source-of-
truth stok di
model lama.
Adjustment/
ledger
ambiguity.
Data
correctness
failure.
Move to
inventory
domain/appli
cation/data +
ledger
ownership.
Bridge
sementara:
sangat
terbatas.
Desktop
placeholder
marketed as
Karena
package
bukan
Pilot failure.
False parity
and release
waste.
Build real
desktop retail
shell and flow
Bridge
sementara:
tidak untuk

client
product
readiness.
set.
pilot.

Roadmap Production-Ready
Stream A-J, dependency logic, risk, exit criteria, rollback concern, dan owner suggestion.
Stream
Scope
Deps
Risk
Exit
Rollback
Owner
A.
Architectur
e &
Modulariza
tion
Freeze
coupling,
finalize
namespace
bridge,
define app-
shell/modul
e map.
Pre-req
semua
stream.
Rename
blast
radius,
import
churn.
Root build
stable, shell
modules
defined,
AppContain
er sunset
rule
documente
d.
Forward-fix
only; no
half-
rename.
Architectur
e lead /
Staff
engineer
B. Shared
Core /
Bounded
Context
Split
Evacuate
shared
monolith
by kernel,
cash, sales,
inventory,
returns,
sync,
reporting.
A before
C/D/E/F.
Wrong
ownership
if rushed.
Legacy
repo usage
minimized
on critical
flows.
Keep
compatibili
ty facades
thin.
Core
platform
engineer
C. Android
Retail
Operational
Stabilize
Android
retail E2E:
open
day/shift,
cart,
payment,
receipt,
close
shift/day.
A+B
required.
Regression
in working
flow.
Android
can pilot as
truthful
single-node
retail
client.
Feature
flags for
cutover.
Android
lead

D. Desktop
Retail
Operational
Desktop-
first
buildout of
real retail
E2E, not
just
reconcile
shell.
A+B
required;
prioritize
after C
foundation
s but before
polish.
Operator
UX, printer,
packaging,
flow gap.
Desktop
can execute
mandatory
parity flows
on its own
DB.
Stage by
feature
flags and
pilot store
config.
Desktop/
KMP lead
E.
Inventory /
Ledger /
Audit
Move stock
truth to
inventory_
balance +
stock_ledge
r mindset,
keep audit
atomic.
A+B
required;
blocks
serious
pilot.
Data
mismatch/b
ackfill risk.
Sales no
longer
writes stock
ad hoc;
discrepanc
y queue
exists.
Freeze
cutover if
counts
mismatch.
Inventory
domain
owner
F.
Backend /
HQ / Sync
Boundary
Prepare
non-hard-
dependenc
y phase-1
boundary;
batch/item/
conflict
model.
B+E
influence.
Overbuildi
ng server
too early.
Client can
run offline
phase-1;
sync
contracts
ready for
phase-2.
Feature-
flag
worker/orc
hestration.
Backend
lead
G. Database
/ Migration
Dual-track
migration,
replay, FK,
integrity,
compatibili
ty bridges.
B+E+F
required.
Schema
drift, replay
failure.
Fresh
install +
upgrade
path
proven.
Hold
contract
enforceme
nt until
verification
passes.
Database
owner
H. QA / QE /
Automation
Common/
component/
integration
first;
desktop
functional
and
migration
replay
All streams.
Flaky suite
or shallow
coverage.
Critical
flows
protected
in PR gate.
Quarantine
flaky tests
fast.
QE lead

mandatory.
I. Release
Engineerin
g / DevOps
Build PR
fast gate,
mainline,
nightly,
release,
package
retention,
envs.
A+H
required.
False
green /
release
chaos.
Stable
required
checks +
repeatable
artifacts.
Promote by
environme
nts only.
Release
engineer
J. Docs /
ADR /
Traceabilit
y
Keep ADRs
and
migration
notes
synced
with
cutovers.
Across all
streams.
Docs lag
reality.
Design
review and
handoff
remain
trustworth
y.
Never
cutover
without
note.
Architect /
Tech writer

Phase Plan
Urutan implementasi yang realistis untuk tim kecil dan tidak merusak arsitektur.
Phase
Scope
Exit Criteria
Phase 0 - Stabilize &
Evidence Gathering
Namespace inventory, bridge
policy, baseline metrics, exact
module ownership, live gradle
sync in engineering
environment.
Freeze coupling-heavy feature
work. No new architectural
debt.
Phase 1 - Namespace + Build
Control Plane
Controlled rename bridge,
build logic normalization,
module naming discipline,
required check naming
contract.
Mixed namespace no longer
grows. Build graph stops
drifting.
Phase 2 - Shared Core Split
Evacuate kernel, cash, sales,
inventory contracts and
repositories from shared blob.
Critical flows stop depending
on shared monolith directly.
Phase 3 - Dual Retail
Execution Foundation
Real app-shell Android +
Desktop, shared business core
ports, terminal binding,
business day + shift
foundation.
Both platforms can start
day/shift with own local DB
and truthful boundaries.
Phase 4 - Cash/Sales
Hardening
Pricing, payment state, sale
finality, receipt snapshot,
void/approval, close shift
baseline.
No sale finality ambiguity.
Receipt/reprint path
trustworthy.
Phase 5 - Inventory / Ledger
Hardening
inventory_balance/
stock_ledger ownership,
adjustment, lookup, receiving,
discrepancy queue.
Sales no longer performs ad
hoc stock writes.
Phase 6 - Android + Desktop
E2E Operational Hardening
Mandatory parity flows on
both platforms, operator
ergonomics, packaging
hardening, smoke/UAT.
Desktop and Android both
pilotable, each on own node
DB.
Phase 7 - Sync / Reporting /
Approval / Return Hardening
Batch/item/conflict visibility,
backoffice reconcile,
approvals, returns aggregate,
Offline phase-1 intact; HQ/sync
phase-next ready.

reporting correctness.
Phase 8 - Release Hardening
Nightly, RC, artifact retention,
staging/prod env, rollback
manifest, packaging fidelity.
Repeatable promotion, not
artisanal release.
Phase 9 - Production
Readiness Review
Pilot evidence review,
operational checklist, blocker
closure, go/no-go.
Truthful launch decision.

30-60-90 Day Plan
Realistis untuk tim kecil, dengan desktop diprioritaskan namun tetap arsitektural.
Window
Must Lock
Must Stabilize
Outcome
30 hari
Namespace bridge,
AppContainer sunset
policy, shared
ownership map,
desktop operational
scope contract, live
Gradle evidence lane.
Kernel/cash/sales DB
path foundations and
business-day/shift
invariants.
No more structural
ambiguity.
60 hari
Desktop retail shell
real, Android shell
cleaned, F&B/Service
quarantined,
migration replay
harness, FK/integrity
gate.
Open shift, cart,
tender, receipt
preview/reprint, close
shift on both
platforms with
truthful divergence
notes.
Staging-grade dual
retail foundation.
90 hari
Inventory cutover,
sync visibility
baseline, release
pipeline, packaging
hardening, UAT
checklist, pilot
readiness review.
Android + Desktop
mandatory parity
flows stable; pilot
package and rollback
manifest available.
Pilot / staging /
production candidate
depending evidence.

Testing & Automation Blueprint
Jenis test, scope, gate, dan evidence yang wajib.
- Common/unit: invariants for pricing, role guard, approval, idempotency, business-day and shift
rules.
- Component/repository: atomic sale bundle, shift close bundle, inventory adjustment bundle,
audit+outbox commit, FK/integrity checks, migration replay.
- Application/use case: checkout facade replacement path, cash control orchestration, inventory
action path, return aggregate rules, sync state transitions.
- Integration: payment decline/pending, offline auth fallback, printer intent/result, sync partial
failure/conflict/retry, backend ingest replay.
- Android local/unit: navigation guards, UI state reduction, session restore, printer settings behavior,
permission edge logic.
- Android instrumented/device: scanner lifecycle, printed receipt flow, package install smoke,
hardware callback interruptions.
- Desktop functional/UI: reconcile screen, report/admin screen, retail checkout keyboard flow, close
shift/day flow, package install smoke.
- Cross-platform contract tests: same pricing/totals/tax/discount/void/ledger semantics on Android and
Desktop.
- DB migration replay: fresh install + upgrade path for local DB; backend chain separately when
backend becomes active.
- Audit/outbox atomicity tests: sensitive mutation must fail closed if durable audit intent cannot
persist.
- Sync partial failure/conflict/retry tests: no silent drop, explicit final state only.
- Printer/barcode integration strategy: adapter contract + selected real-device suite, not blanket UI-
only testing.
- Release smoke + UAT: open day, start shift, complete sale, reprint, stock adjust, close shift/day,
backlog review, rollback drill.

CI/CD & Release Blueprint
PR fast gate, mainline, nightly, release, and operational rollback.
Lane
Trigger
Required Pack
Failure Policy
PR Fast Gate
pull_request
shared-fast, android-
pos-fast, desktop-fast,
backend-fast,
migration/FK subset,
pr-gate-summary
Merge blocked
Mainline Packaging
push main
repeat fast gate +
package
Android/Desktop +
backend bundle
preview
No silent publish
Nightly Quality
schedule/manual
device suite,
migration replay,
sync-fault pack,
extended desktop
smoke
Flaky triage required
next day
Release / Promotion
tag/
workflow_dispatch
signed/package
candidates,
checksums, release
manifest, env
approvals
Manual promotion
only
Stable Required
Checks
branch protection
unique job names, no
trigger-level skip
causing Pending
Protection violation =
stop line
- Android internal distribution: unsigned/signed candidate per lane maturity; production promotion
only after RC evidence.
- Desktop packaging: Windows-first installer/package, checksum, manifest, smoke install, rollback
note.
- Schema/contract change policy: shared DB change, migration, sync contract, and relevant tests must
land in same PR.
- Observability: sync visibility, pending offline windows, unresolved conflicts, business-day blockers,
packaging manifest, release SHA traceability.
- Rollback readiness: release manifest must record commit SHA, schema version, migration level,
artifact checksum, and forward-fix/rollback note.

Immediate Backlog
Now / Next / Later / Not Yet dengan why, impact, effort, risk, dependency, blocker.
Bucket
Item
Why
Impact
Effort
Risk
Dep
Blocker
Now
Freeze
coupling-
heavy
features
Protect
architectu
re before
more
debt
lands
High
Low-Med
Low
None
Discipline
only
Now
Define
Desktop
retail
mandator
y parity
contract
Without
this,
desktop
priority is
slogan
only
High
Low
Low
User
decision
locked
None
Now
Controlle
d
namespac
e cutover
plan
Mixed
namespac
e will
poison
every
future
move
High
Med
Med
Phase 0
Import
churn
Now
Constrain
AppConta
iner to
bridge
surface
Stop
service
locator
normaliz
ation
High
Med
Med
Shell
module
plan
Refactor
effort
Next
Quaranti
ne
F&B/Servi
ce from
retail
shipping
nav
Restore
retail
kernel
focus
High
Low
Low
Product
direction
Potential
temporar
y UX
regressio
n
Next
Build
Desktop
operation
Desktop
priority
must
High
High
High
Shared
core ports
Desktop
flow still
missing

al shell +
local DB
path
become
real
Next
Inventory
ownershi
p map +
evacuatio
n plan
Most
dangerou
s hidden
debt
High
High
High
Schema
cutover
Backfill
complexit
y
Later
Returns
aggregate
hardenin
g
Needed
for full
policy-
safe retail
Med-High
High
Med
Sales/
inventory
foundatio
n
Business
rule
complexit
y
Later
Sync
batch/ite
m/conflict
runtime
Needed
before
HQ scale-
up
High
High
High
Kernel +
backend
contracts
Too early
if core
still
unstable
Not Yet
New
verticals /
expanded
F&B / rich
service
Will
pollute
kernel
before
retail
hardens
Very High
negative
if started
High
High
Retail
kernel
stable
first
Architect
ure debt

Final Recommendation
Tegas, langsung, dan bisa dieksekusi minggu ini.
- Minggu ini: freeze penambahan fitur yang memperbesar coupling; finalkan desktop parity contract;
buat controlled namespace cutover plan; definisikan AppContainer sunset policy; lakukan live
Gradle sync/check di environment engineering untuk memvalidasi static audit ini.
- Hentikan: menambah layar/flow baru di F&B, Service, atau shared blob tanpa ownership context
yang jelas; membela legacy hanya karena sudah jalan; memperlakukan desktop shell sebagai bukti
desktop readiness.
- Bridge sementara yang boleh: AppContainer di app-shell Android/Desktop; compatibility
repositories tipis; derived stock_qty sementara; feature flag untuk platform gaps; limited dual-read
saat migration.
- Yang tidak boleh dikompromikan: local-first source of truth per node, single-writer per
terminal/node, auditability, current-state vs explanation-trail separation, migration safety, sync
visibility, and release safety.
- Apakah dual-platform retail phase 1 realistis? Ya, tetapi hanya bila dipecah jujur: Desktop
diprioritaskan sebagai writer operational per node sendiri, Android tetap operational peer, dan
parity dibangun di mandatory flows lebih dulu. Bila tim mencoba full breadth parity sebelum
shared/blob evacuation, hasilnya hampir pasti gagal.
- Split scope paling aman: Desktop-first on mandatory retail flows; Android retains same mandatory
operational kernel; both defer low-level hardware parity where needed; F&B/Service stay
quarantined; HQ/sync stays boundary-prepared, not hard dependency for phase 1 offline
operation.
Stance final: hentikan penambahan fitur baru yang memperbesar coupling pada Android
maupun Desktop sampai logic evacuation dari shared blob ke bounded-context modules berjalan
nyata. Dual operational client tanpa ownership yang bersih hanya akan mempercepat
dependency hell, parity drift, dan release failure.

Appendix - Evidence Base
Artefak dan repo path yang dipakai untuk menyusun audit ini.
- README.md - project positioning, source-of-truth stance, local-first retail-first direction.
- DATA.md - artifact inventory and observation summary.
- docs/03_cassy_architecture_specification_v1.md - target architecture, local-first, bounded contexts,
single-writer, sync model.
- docs/04_cassy_cicd_pipeline_strategy_v1.md - target CI/CD lanes, required checks, release policy.
- docs/06_cassy_migration_script_specification.md - migration wave strategy and high-risk domain
sequencing.
- docs/07_cassy_test_automation_specification.md - test family map and mandatory platform gates.
- docs/10_store_pos_erd_specification_v2.md - target ERD, inventory/return/sync gaps vs legacy
schema.
- docs/13_cassy_e2e_store_operation_uiux_flow_scheme_v2.md - guided operations flow, readiness,
anti-skip gating.
- docs/15_cassy_module_project_structure_specification.md - target module map, Koin/composition
root direction, staged migration.
- docs/21_store_pos_test_specification.md and docs/23_traceability_matrix_store_pos.md - use case to
DB/test traceability.
- settings.gradle.kts, build.gradle.kts, gradle.properties, gradle/libs.versions.toml, build-logic/* -
actual build topology observed.
- apps/android/build.gradle.kts, AndroidManifest.xml, MainActivity.kt, ui/CassyApp.kt,
data/AppContainer.kt - Android runtime evidence.
- apps/desktop/build.gradle.kts, Main.kt, DesktopApp.kt - Desktop runtime evidence.
- shared/build.gradle.kts, shared/kernel/build.gradle.kts, shared/cash/build.gradle.kts,
shared/sales/build.gradle.kts, shared/inventory/build.gradle.kts - module split evidence.
- shared/kernel/.../KernelSchema.sq, KernelQueries.sq, shared/cash/.../CashQueries.sq - SQLDelight
transition evidence.
Freshness note
Repo and docs were observed from the latest pushed main snapshot accessible during this session.
Core source artefacts are dated 8-10 March 2026. This report explicitly marks where the repo lags
those artefacts or where the new business directive overrides older assumptions.


## Constraints / Policies
Dokumen ini adalah audit/recommendation terhadap repo snapshot, bukan source of truth bisnis/desain utama.

## Technical Notes
Gunakan sebagai input implementasi/refactor, bukan pengganti architecture/module structure spec.

## Dependencies / Related Documents
- `cassy_architecture_specification_v1.md`
- `cassy_module_project_structure_specification.md`
- `cassy_migration_script_specification.md`
- `cassy_test_automation_specification.md`
- `cassy_cicd_pipeline_strategy_v1.md`

## Risks / Gaps / Ambiguities
- Dokumen ini bersifat audit terhadap snapshot repository pada 2026-03-11; prioritaskan source-of-truth artefak desain jika terjadi konflik.

## Reviewer Notes
- Struktur telah dinormalisasi ke Markdown yang konsisten dan siap dipakai untuk design review / engineering handoff.
- Istilah utama dipertahankan sedekat mungkin dengan dokumen sumber untuk menjaga traceability lintas artefak.
- Bila ada konflik antar dokumen, prioritaskan source of truth, artefak yang paling preskriptif, dan baseline yang paling implementable.

## Source Mapping
- Original source: `Cassy_Repository_Audit_Production_Roadmap_2026-03-11.pdf` (PDF, 31 pages)
- Output markdown: `cassy_repository_audit_production_roadmap_2026_03_11.md`
- Conversion approach: text extraction -> normalization -> structural cleanup -> standardized documentation wrapper.
- Note: beberapa tabel/list di PDF dapat mengalami wrapping antar baris; esensi dipertahankan, tetapi layout tabel asli tidak dipertahankan 1:1.
