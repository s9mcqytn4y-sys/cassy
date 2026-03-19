# Cassy Module Project Structure Specification

## Document Overview
Implementation baseline untuk struktur monorepo, module boundary, hybrid shared UI, DI, naming, dan staged restructuring.

## Purpose
Menerjemahkan architecture target ke struktur repository dan Gradle module yang implementable.

## Scope
Phase 1 Retail POS; F&B; dan Service hanya sebagai prepared boundary Keputusan Kunci Hybrid shared UI, bounded-context modules, Koin DI, monorepo, selective macOS CI untuk iOS Namespace Root id.azureenterprise.cassy Tanggal

## Key Decisions / Core Rules
Hybrid shared UI diterima selektif; app-shell/platform adapter tetap native; Koin dipakai sebagai DI; repo saat ini diperlakukan sebagai snapshot transisional, bukan source of truth.

## Detailed Content

### Normalized Source Body
CASSY
Module Project Structure
Specification
Baseline struktur modul implementasi untuk target-state Phase 1 Retail POS,
dengan pendekatan hybrid shared UI, bounded context modularization, dan
monorepo yang siap diimplementasikan.
Jenis Dokumen
Implementation / Engineering Model Artifact
Scope Utama
Phase 1 Retail POS; F&B; dan Service hanya sebagai prepared
boundary
Keputusan Kunci
Hybrid shared UI, bounded-context modules, Koin DI, monorepo,
selective macOS CI untuk iOS
Namespace Root
id.azureenterprise.cassy
Tanggal
8 Maret 2026
Posisi dokumen ini
Dokumen ini adalah turunan sesudah artefak Architecture dan sebelum baseline detail
implementation/test diperluas lebih jauh.
Repo GitHub saat ini diperlakukan sebagai implementation snapshot, bukan source of
truth desain. Source of truth tetap artefak project yang telah diunggah.
Dokumen ini secara sengaja mengoreksi baseline lama pada satu area: strategi UI
berubah dari native-only menjadi hybrid shared UI yang tetap menjaga app-shell dan
platform adapter tetap native.
Disusun untuk design review, engineering handoff, repository restructuring, dan baseline migration plan.

## 1. Ringkasan Eksekutif
Dokumen ini menetapkan struktur modul target untuk Cassy dengan orientasi engineering
yang implementable, bukan sekadar struktur folder yang terlihat rapi. Prinsip utamanya
adalah retail-first, local-first, audit-heavy, dan sync-explicit. Konsekuensinya, struktur modul
harus menjaga transaksi inti checkout tetap stabil saat offline, menjaga traceability
audit/outbox, dan mencegah feature berkembang menjadi procedural blob.
Keputusan akhir pengguna yang mengikat dokumen ini adalah: hybrid shared UI,
bounded-context module naming, phase-1 realistic target state, F&B;/Service sebagai
prepared boundary, Koin sebagai DI, dan CI iOS selektif di macOS runner walau workstation
utama menggunakan Windows.
Kesimpulan desain
Struktur target bukan sekadar memecah `apps/android` menjadi banyak modul. Yang
benar adalah: shared business core dipecah per bounded context, shared Compose UI
dibatasi pada layar yang memang cocok dibagi lintas platform, sementara app-shell,
hardware integration, lifecycle, permission, printer, scanner, payment, dan bootstrap
tetap native.
Dengan pendekatan ini, Android dan iOS tetap berbagi business logic dan sebagian UI,
tetapi tidak jatuh ke jebakan shared UI penuh yang berisiko mengikat semua concern
device-specific ke layer yang salah.
Aspek
Keputusan
Makna untuk struktur project
Scope produk
Phase 1 Retail POS
Sales, return, cash, inventory basics, reporting, sync
wajib aktif; F&B; dan Service hanya disiapkan
boundary-nya.
Organisasi modul
Bounded context ->
layer
Shared code dipecah per context, lalu dipecah lagi
menjadi domain/application/data.
Strategi UI
Hybrid shared UI
UI tertentu dibagi lewat Compose Multiplatform; shell
dan adapter platform tetap native.
DI
Koin
Composition root jelas per app-shell; service locator
lama dipensiunkan bertahap.
Repository
baseline
Monorepo
Shared core, app, backend, docs, tooling, dan workflow
hidup di repositori yang sama agar traceability tidak
pecah.
## 2. Basis Artefak dan Koreksi terhadap Baseline
Sebelumnya
Source of truth dokumen ini tetap mengikuti urutan SDLC project: Use Case -> Activity ->
Sequence -> Domain Model -> Architecture -> Database -> Implementation -> Test. Artinya
struktur modul harus diturunkan dari artefak bisnis dan arsitektur, bukan dari kebetulan
struktur repo yang ada saat ini.
Artefak arsitektur yang sudah dibuat sebelumnya menetapkan beberapa keputusan yang
tetap dipertahankan: local-first client, single operational writer per terminal/device, outbox +
batch/item/conflict sync model, monorepo recommendation, dan bounded context ownership
untuk kernel, masterdata, sales, returns, cash, inventory, reporting, sync, serta boundary

F&B; dan Service.
Koreksi yang dilakukan dokumen ini
Baseline architecture lama menolak shared UI. Dokumen ini merevisi area tersebut
menjadi hybrid shared UI karena pengguna secara eksplisit memilih Compose
Multiplatform lintas Android dan iOS.
Revisi ini bukan izin untuk memindahkan printer, scanner, Bluetooth, payment terminal,
permission, atau lifecycle handling ke shared layer. Shared UI hanya boleh mengambil
concern presentasi yang cukup stabil dan tidak device-heavy.
Dengan kata lain: yang berubah adalah kebijakan berapa jauh UI dibagi, bukan aturan
dependency direction, transaction ownership, auditability, atau offline correctness.
Implikasinya, dokumen ini harus diperlakukan sebagai implementation baseline terbaru untuk
struktur modul, walau pada topik lain masih tetap selaras dengan architecture specification
yang sudah disusun sebelumnya.
## 3. Observasi Repo Saat Ini dan Diagnosis
Engineering
Berdasarkan observasi terhadap repo `cassy` saat ini, codebase masih berada di bentuk
transisional: ada `apps/android`, `apps/ios`, dan satu modul `shared` yang menampung
terlalu banyak concern. Struktur ini cukup baik untuk validasi awal, tetapi belum sehat untuk
scale-up engineering.
Area
Kondisi repo saat ini
Masalah engineering
Android app
Masih berupa satu aplikasi utama
di `apps/android` dengan
`src/main`, `src/test`, dan
`src/androidTest`.
Feature boundary belum eksplisit; risiko
coupling tinggi; build cache dan review
ownership sulit dipisah.
Shared
module
`shared` masih memegang
business/data lintas domain dalam
satu payung besar.
Bounded context belum tercermin jelas pada
Gradle module; perubahan kecil berpotensi
memicu rebuild besar dan membuat
dependency boundary kabur.
iOS
`apps/ios` masih diposisikan
sebagai host app yang bergantung
pada framework hasil build shared
secara terpisah.
iOS belum menjadi citizen kelas satu di
topologi build utama; onboarding dan CI
mudah timpang.
Gradle
topology
Akar build masih minimal dan
belum mencerminkan
feature/module map target.
Belum ada convention plugin yang
menegakkan standar modul baru; risiko
duplikasi config tinggi.
Naming
Namespace dan package naming
belum seragam.
Inkonistensi naming akan menjadi technical
debt saat modul bertambah banyak.
Kritik pentingnya: repo sekarang tidak salah, tetapi tidak boleh dijadikan patokan final. Ia
baru snapshot implementasi awal. Kalau struktur sekarang dipertahankan lalu hanya
ditempeli beberapa modul tambahan, hasilnya justru lebih buruk: monolith lama tetap hidup,
tetapi kini dibungkus pseudo-modular repo yang susah dipahami.
Keputusan refactoring

Jangan lakukan rename kosmetik. Lakukan staged restructuring yang memindahkan
concern secara sadar: app-shell, platform adapter, shared UI, shared business core,
backend, dan tooling build logic.
`AppContainer` model lama sebaiknya tidak dipertahankan sebagai pusat dependency
jangka panjang. Ia hanya boleh menjadi jembatan migrasi menuju composition root
berbasis Koin.
## 4. Prinsip Struktur Project Target
G Bisnis lebih dulu, folder belakangan. Modul dibentuk dari bounded context dan flow bisnis,
bukan dari layar atau library semata.
G UI adalah layer, bukan tempat rule bisnis. Shared UI Compose boleh ada, tetapi ia tetap UI
layer yang hanya berbicara ke application contracts / presenter / viewmodel resmi.
G Native shell tetap wajib. Android, iOS, dan desktop masing-masing memiliki app-shell
sendiri untuk bootstrap, lifecycle, permissions, navigation host, terminal binding, dan
platform integration.
G F&B; dan Service tidak diaktifkan prematur. Module placeholder boleh disiapkan, tetapi
implementasi rinci ditunda sampai retail kernel stabil.
G Convention over repetition. Build logic bersama diletakkan pada included build
`tooling/build-logic` agar standar compiler, lint, testing, dan packaging tidak di-copy paste
antar modul.
G Phase-1 realistic target. Struktur harus bisa dipakai bertahap tanpa mewajibkan semua
app/platform lahir sekaligus.
Secara konseptual, struktur target membagi repo menjadi lima lapisan organisasi besar:
apps, shared, backend, docs, dan tooling. Di dalam `shared`, pemisahan utama tetap
mengikuti bounded context; di dalam `apps`, pemisahan mengikuti app-shell dan platform
adapter; sedangkan shared UI diletakkan sebagai layer presentasi yang terpisah dari
domain/application/data.
## 5. Struktur Monorepo Target
Berikut struktur target yang direkomendasikan. Ini bukan tree dekoratif; setiap cabang punya
alasan ownership yang jelas. Struktur ini memisahkan shared business core, shared UI,
app-shell native, dan platform device adapter agar dependency tetap terbaca.
Bagian A - apps dan shared

Cassy/
├── apps/
│ ├── android-pos/
│ │ ├── app/
│ │ ├── app-shell/
│ │ └── platform-device/
│ │ ├── permissions/
│ │ ├── printer/
│ │ ├── scanner/
│ │ ├── payment/
│ │ └── share/
│ ├── ios-pos/
│ │ ├── iosApp/
│ │ ├── app-shell/
│ │ └── platform-device/
│ │ ├── keychain/
│ │ ├── printer/
│ │ ├── share/
│ │ └── lifecycle/
│ ├── android-mobile/
│ │ ├── app/
│ │ ├── app-shell/
│ │ └── platform-device/permissions/
│ └── desktop-backoffice/
│ ├── app/
│ └── app-shell/
├── shared/
│ ├── platform-core/
│ │ ├── core-common/
│ │ ├── core-coroutines/
│ │ ├── core-datetime/
│ │ ├── core-logging/
│ │ ├── core-result/
│ │ ├── core-di/
│ │ └── core-test/
│ ├── compose/
│ │ ├── design-system/
│ │ ├── ui-core/
│ │ ├── navigation-contract/
│ │ ├── feature-sales-ui/
│ │ ├── feature-shift-ui/
│ │ ├── feature-receipt-ui/
│ │ ├── feature-sync-ui/
│ │ ├── feature-approval-ui/
│ │ ├── feature-inventory-ui/
│ │ ├── feature-reporting-ui/
│ │ ├── feature-reconcile-ui/
│ │ └── feature-admin-ui/
│ ├── kernel/{domain,application,data}
│ ├── masterdata/{domain,application,data}
│ ├── sales/{domain,application,data}
│ ├── returns/{domain,application,data}
│ ├── cash/{domain,application,data}
│ ├── inventory/{domain,application,data}
│ ├── reporting/{domain,application,data}
│ ├── sync/{domain,application,data}
│ ├── auth/{application,data}
│ ├── integrations/
│ │ ├── hqapi/
│ │ ├── payment/
│ │ ├── identity/
│ │ ├── printer/
│ │ └── scanner/
│ ├── fb/{domain,application,data} # prepared boundary
│ └── service/{domain,application,data} # prepared boundary
Bagian B - backend, docs, tooling, workflow

├── backend/
│ ├── cmd/cassy-api/
│ ├── internal/
│ │ ├── platform/{httpx,authn,authz,clock,idgen,tx,logging}
│ │ ├── kernel/{approval,audit,reason}
│ │ ├── masterdata/{domain,app,repo,http}
│ │ ├── sales/{domain,app,repo,http}
│ │ ├── returns/{domain,app,repo,http}
│ │ ├── cash/{domain,app,repo,http}
│ │ ├── inventory/{domain,app,repo,http}
│ │ ├── reporting/{domain,app,repo,http}
│ │ ├── sync/{domain,app,repo,http}
│ │ ├── fb/{domain,app,repo,http}
│ │ └── service/{domain,app,repo,http}
│ └── db/postgres/{migrations,queries}
├── docs/
│ ├── architecture/
│ ├── database/
│ ├── implementation/
│ └── test/
├── tooling/
│ ├── build-logic/
│ ├── scripts/
│ └── ci/
└── .github/workflows/
Catatan penting: `fb` dan `service` tetap hadir sebagai folder/module boundary, tetapi
statusnya prepared. Jangan isi banyak code hanya agar tree terlihat lengkap. Boundary ada
untuk menjaga arah evolusi, bukan untuk menciptakan dead code.
## 6. Detail Tanggung Jawab Modul
Bagian ini menjelaskan siapa seharusnya memiliki apa. Ini penting agar setelah repo dipecah,
tim tidak memindahkan concern ke tempat yang salah.
Kelompok modul
Harus memiliki
Tidak boleh memiliki
apps/*/app-shell
Bootstrap aplikasi, composition root,
feature flag, session bootstrap,
terminal binding, connectivity
observer, route host.
Rule bisnis, query SQL langsung,
DTO transport, perhitungan domain.
apps/*/platform-
device/*
Permission prompt, printer bridge,
scanner lifecycle, payment terminal
SDK, keychain/keystore, file share, OS
callback.
Use case bisnis lintas context, state
sync, domain invariant.
shared/compose/
*
Composable screen, UI state contract,
presenter/viewmodel binding, shared
design system, navigation contract
lintas platform.
Repository SQL, HTTP client
langsung, approval/policy/business
rule inti.
shared/*/domain
Aggregate, entity, value object,
invariant, domain policy, state
machine bisnis.
Android API, Compose widget, raw
SQL, SDK vendor.
shared/*/applicat
ion
Use case, facade, transaction
boundary, orchestration lintas
aggregate, permission/approval
business flow.
UI toolkit, platform lifecycle,
SQLDelight query interface langsung
terekspos ke UI.
shared/*/data
Repository impl, SQLDelight adapter,
DTO mapping, integration adapter,
cache policy, outbox persistence.
Compose state, navigation, business
branching yang seharusnya berada
di application/domain.
Untuk hybrid shared UI, shared Compose modules diperlakukan sebagai UI layer lintas
platform, bukan sebagai pengganti app-shell. Ini adalah batas yang wajib dijaga. Begitu

printer pairing, camera lifecycle, Bluetooth discovery, atau payment terminal callback mulai
bocor ke shared UI, desain langsung salah arah.
## 7. Bounded Context dan Modul Fitur yang Diaktifkan
Pengguna memilih penamaan berbasis bounded context / flow, bukan label departemen
kasar. Itu keputusan yang benar karena lebih traceable ke artefak domain, architecture,
database, dan test.
Context /
flow
Modul shared aktif
Modul UI phase-1
Status
Kernel
kernel/{domain,application,dat
a}
-
Aktif
Master Data
masterdata/{domain,applicati
on,data}
feature-sales-ui,
feature-inventory-ui
Aktif
Sales
sales/{domain,application,data
}
feature-sales-ui,
feature-receipt-ui
Aktif
Returns
returns/{domain,application,d
ata}
feature-sales-ui / approval-ui
(jalur return)
Aktif
Cash
cash/{domain,application,data
}
feature-shift-ui,
feature-reconcile-ui
Aktif
Inventory
inventory/{domain,application,
data}
feature-inventory-ui
Aktif
Reporting
reporting/{domain,application,
data}
feature-reporting-ui,
feature-admin-ui
Aktif selektif
Sync
sync/{domain,application,data
}
feature-sync-ui
Aktif
F&B;
fb/{domain,application,data}
belum ada UI shipping
Prepared
boundary
Service
service/{domain,application,d
ata}
belum ada UI shipping
Prepared
boundary
Konsekuensinya, istilah seperti Kasir, Produk, Riwayat, atau Pengaturan tetap boleh muncul
sebagai label menu pada UI, tetapi bukan menjadi skema utama naming Gradle module
shared core. Module core harus tetap mengikuti ownership domain: sales, returns, cash,
inventory, reporting, sync, dan seterusnya.
## 8. Konvensi Nama Modul, Package, dan Gradle Build
Logic
Namespace final yang dipakai adalah `id.azureenterprise.cassy`. Keputusan ini harus
ditegakkan konsisten dan menjadi dasar rename terhadap package lama yang masih
bercampur.

# Root namespace
id.azureenterprise.cassy
# App namespaces
id.azureenterprise.cassy.android.pos
id.azureenterprise.cassy.android.mobile
id.azureenterprise.cassy.ios.pos
id.azureenterprise.cassy.desktop.backoffice
# Shared packages
id.azureenterprise.cassy.shared.kernel.domain
id.azureenterprise.cassy.shared.sales.application
id.azureenterprise.cassy.shared.inventory.data
id.azureenterprise.cassy.shared.compose.feature.sales
id.azureenterprise.cassy.shared.integrations.payment
# Backend packages
id.azureenterprise.cassy.backend.sales.app
id.azureenterprise.cassy.backend.sync.repo
Aturan praktisnya:
G Gunakan nama Gradle module berbasis tanggung jawab (`feature-sales-ui`,
`sales:application`, `platform-device:printer`), bukan nama layar atau nama tim.
G Gunakan package namespace yang stabil dan eksplisit; jangan campur `com.azure.*`,
`com.cassy.*`, dan namespace baru dalam repo yang sama lebih lama dari masa migrasi.
G Untuk build logic, gunakan included build `tooling/build-logic` berisi convention plugins. Ini
lebih scalable daripada mengandalkan konfigurasi root atau `subprojects {}` yang makin
lama makin opaque.
G Convention plugin minimal yang disarankan: `cassy.kotlin.library`,
`cassy.android.application`, `cassy.kmp.shared`, `cassy.compose.ui`,
`cassy.code.quality`, `cassy.testing`.
Keputusan non-populer tapi sehat
Jangan langsung memakai Koin annotations/KSP di awal migrasi. Mulai dari DSL manual
dulu agar split module dan dependency graph stabil terlebih dahulu.
Annotations boleh dipertimbangkan kemudian, tetapi menambah KSP di awal restruktur
repo justru memperbesar noise build dan debugging.
## 9. Kebijakan Hybrid Shared UI dan Dependency
Injection
Hybrid shared UI berarti Compose Multiplatform dipakai secara selektif untuk layar yang
value-nya memang tinggi bila dibagi lintas Android dan iOS. Bukan semua layar harus dibagi.
Jenis layar / concern
Kebijakan
Sales cart, receipt preview, sync
status, inventory list, operational
report viewer
Layak dibagi di `shared/compose/*` bila interaksi
utamanya business-flow dan state management.
Permission prompt, printer pairing,
barcode/camera lifecycle,
payment terminal callback,
Bluetooth discovery
Tetap native dan berada di `apps/*/platform-device/*`.
Shared layer hanya menerima port/interface yang bersih.

Jenis layar / concern
Kebijakan
App startup, deep link, session
restore, terminal binding,
crash/reporting bootstrap
Tetap di `app-shell` masing-masing platform.
Untuk DI, Koin dipilih karena cocok untuk KMP dan Compose Multiplatform, tetapi cara
pakainya harus tetap disiplin: platform module mendaftarkan adapter native, shared modules
mendaftarkan domain/application/data, dan app-shell menjadi composition root tunggal per
aplikasi.
// commonMain
implementation("io.insert-koin:koin-core")
implementation("io.insert-koin:koin-core-viewmodel")
implementation("io.insert-koin:koin-compose")
implementation("io.insert-koin:koin-compose-viewmodel")
// androidMain / android app-shell
implementation("io.insert-koin:koin-android")
implementation("io.insert-koin:koin-androidx-compose")
// composition root example
val sharedModules = listOf(
kernelModule,
salesModule,
returnsModule,
cashModule,
inventoryModule,
reportingModule,
syncModule,
composeUiModule,
)
fun startCassyKoin(platformModules: List<Module>) = startKoin {
modules(sharedModules + platformModules)
}
Larangan penting: jangan membuat shared Compose screen mengambil repository langsung
hanya karena Koin memudahkan injeksi. Koin adalah alat wiring, bukan alasan untuk merusak
boundary.
## 10. CI / CD dan Strategi iOS untuk Workstation
Windows
Karena workstation utama menggunakan Windows, strategi yang paling realistis adalah:
Android/shared/backend dibangun cepat di Ubuntu, sedangkan iOS dibangun selektif di
macOS hosted runner. Ini menghindari biaya macOS yang tidak perlu pada setiap perubahan
kecil, tetapi tetap menjaga integritas iOS path.
# Proposed workflows
.github/workflows/
├── ci-shared.yml
├── ci-android-pos.yml
├── ci-android-mobile.yml # selective / later wave
├── ci-ios-pos.yml # macOS runner, path-filtered
├── ci-desktop-backoffice.yml # selective / later wave
├── ci-backend.yml
└── release.yml
Workflow
Runner
Trigger utama
Catatan
ci-shared.yml
ubuntu-late
st
Perubahan di `shared/**`,
`tooling/build-logic/**`,
`gradle/**`
Compile shared, common
tests, SQLDelight migration
verification, lint/statik.

Workflow
Runner
Trigger utama
Catatan
ci-android-pos.yml
ubuntu-late
st
Perubahan di
`apps/android-pos/**`,
`shared/**`
Assemble, unit test,
packaging checks.
ci-ios-pos.yml
macos-lates
t
Perubahan di
`apps/ios-pos/**`,
`shared/compose/**`,
`shared/**` yang
memengaruhi iOS
Build host iOS dan smoke
test integration path.
ci-backend.yml
ubuntu-late
st
Perubahan di `backend/**`
atau kontrak sync/schema
terkait
go test, build, migration
checks, contract validation.
release.yml
tag/manual
main/tag/release
Versioning, artifact
packaging, optional
deployment gate.
Tambahan penting: `ci-ios-pos.yml` tidak harus jalan untuk semua PR. Gunakan path filter
dan pertimbangkan dua level gate: smoke build pada PR yang relevan, dan full
packaging/signing hanya pada main/release.
Untuk local developer experience, shared UI tetap bisa dikembangkan mayoritas dari
Windows/Android Studio. Tetapi build/run iOS lokal tetap membutuhkan macOS dan Xcode,
sehingga validasi akhir iOS harus bergantung pada CI atau mesin Mac khusus tim.
## 11. Rencana Migrasi Bertahap
Pengguna memilih pendekatan staged migration, dan itu keputusan yang tepat. Big-bang
restructure pada codebase seperti ini terlalu berisiko: terlalu banyak rename, terlalu banyak
moving parts, dan sulit memisahkan bug struktural dari bug perilaku bisnis.
Wave
Fokus
Exit criteria
Wave 0 -
Stabilize
Bekukan naming liar, rapikan root build,
inventaris paket lama, dan tetapkan
namespace final.
Belum ada split besar; fokus
menurunkan risiko migrasi.
Wave 1 -
Foundation
Buat `tooling/build-logic`, rename root
app menjadi `android-pos`, bentuk
`app-shell`, dan pisahkan
`platform-device` Android.
Build logic dan shell boundary lahir
lebih dulu.
Wave 2 -
Shared core
split
Pecah `shared` menjadi `platform-core`,
`kernel`, `masterdata`, `sales`,
`returns`, `cash`, `inventory`,
`reporting`, `sync`, `auth`,
`integrations`.
Business core jadi traceable per
context.
Wave 3 -
Shared UI
rollout
Tambahkan
`shared/compose/design-system`,
`ui-core`, lalu migrasikan layar yang
low-risk/high-reuse ke Compose shared
UI.
Mulai dari `feature-sync-ui`,
`feature-reporting-ui`, lalu
`feature-sales-ui` terbatas.
Wave 4 -
iOS/secondary
app
Bentuk `ios-pos` host app baru, lalu
aktifkan path CI macOS selektif. Setelah
itu baru `android-mobile` dan
`desktop-backoffice` dipisah bertahap.
Jangan buka terlalu banyak front
sekaligus.

Wave
Fokus
Exit criteria
Wave 5 -
Backend
alignment
Selaraskan kontrak sync, migration, dan
workflow backend agar repo benar-benar
monorepo yang hidup, bukan hanya satu
folder besar.
Backend ikut jadi first-class
citizen.
Urutan rollout shared UI juga harus disiplin. Modul yang sebaiknya dibagi paling awal adalah
yang paling sedikit device-specific concern-nya: design-system, sync dashboard, report
viewer, dan sebagian inventory list/detail. Checkout/payment boleh menyusul setelah shell,
printer, scanner, dan settlement adapter sudah bersih.
Rule of migration
Jangan memecah modul kalau boundary bisnisnya belum jelas.
Jangan memindahkan UI ke shared layer sebelum port/platform adapter untuk printer,
scanner, payment, dan permission benar-benar bersih.
Jangan mengaktifkan F&B;/Service sebagai feature shipping sebelum retail kernel stabil
dan observable.
## 12. Guardrails Implementasi
G UI module tidak boleh mengakses SQLDelight query interfaces secara langsung.
G Perubahan bisnis yang retriable harus tetap menyimpan audit/outbox intent dalam
transaksi lokal yang sama.
G Shared Kernel tidak boleh berubah menjadi dumping ground untuk utilitas acak, DTO, atau
helper teknis.
G `InventoryBalance` adalah current state, `StockLedgerEntry` adalah explanation trail;
keduanya tidak boleh dicampur.
G F&B; dan Service harus tetap converge ke sales/cash/inventory kernel yang sama; jangan
pernah buat ledger paralel per channel.
G Path iOS tidak boleh lagi bergantung pada copy manual framework yang rapuh sebagai
workflow utama tim.
Guardrails ini penting karena structure yang bagus di repo bisa rusak sangat cepat kalau
aturan ownership dilanggar oleh convenience coding. Modul yang banyak tanpa disiplin
dependency justru lebih berbahaya daripada monolith jujur.
## 13. Appendix - Mapping Repo Saat Ini ke Target
State
Area sekarang
Target
Arah perubahan
`apps/android`
`apps/android-pos/app`,
`apps/android-pos/app-shell`, `app
s/android-pos/platform-device/*`
Pisahkan concern bootstrap dari
adapter device dan siapkan Android
POS sebagai app utama phase-1.
`apps/ios`
`apps/ios-pos/iosApp`,
`apps/ios-pos/app-shell`,
`apps/ios-pos/platform-device/*`
Naikkan iOS menjadi host app yang
jelas dan integrasikan ke pipeline build
yang resmi.

Area sekarang
Target
Arah perubahan
`shared`
`shared/platform-core`,
`shared/compose/*`, `shared//{do
main,application,data}`
Pecah shared berdasarkan bounded
context dan layer; jangan sisakan satu
modul shared raksasa.
root Gradle
config
`tooling/build-logic` + convention
plugins
Hapus duplikasi build config dan
enforce standar modul secara
konsisten.
`AppContainer`
style wiring
Koin composition root per app-shell
Transisi dari service locator ke DI yang
traceable dan testable.
Bila migrasi dilakukan dengan benar, hasil akhirnya bukan hanya repo yang lebih modular,
tetapi juga alur engineering yang lebih jelas: siapa pemilik domain, siapa pemilik UI, siapa
pemilik adapter, di mana rule bisnis hidup, dan workflow CI mana yang melindungi boundary
tersebut.
## 14. Referensi yang Digunakan
Artefak internal project: UML-Modeling-Source-of-Truth, Cassy Architecture Specification,
Domain Model Detail Specifications, ERD Specification, Test Specification, Traceability Matrix,
dan observasi repo `cassy` saat ini.
Referensi eksternal resmi yang mendasari revisi implementasi ini:
G Kotlin Multiplatform quickstart dan Compose Multiplatform app creation untuk opsi shared
UI, struktur `composeApp` + `iosApp`, dan kebutuhan macOS/Xcode pada iOS build.
G Kotlin Multiplatform direct integration untuk pola integrasi framework ke Xcode host app.
G GitHub Actions hosted runners reference untuk penggunaan runner macOS pada pipeline
iOS.
G Koin official docs untuk Compose Multiplatform packages, multiplatform ViewModel DSL,
dan injection di Compose.
G Gradle official docs untuk convention plugins dan included build `build-logic` sebagai
fondasi build logic yang scalable.
Penutup. Dokumen ini sengaja tajam: ia tidak menganggap struktur repo saat ini sebagai
sesuatu yang harus dipertahankan. Yang dipertahankan adalah correctness bisnis,
traceability artefak, dan arah engineering yang masih waras untuk tim kecil. Struktur target
di sini harus dibaca sebagai baseline kerja yang boleh dievolusikan, tetapi tidak boleh
diencerkan menjadi modularization kosmetik.


## Constraints / Policies
Jangan melakukan rename kosmetik; refactor harus staged dan boundary bisnis harus eksplisit.

## Technical Notes
Dokumen ini mengoreksi baseline lama pada area strategi UI.

## Dependencies / Related Documents
- `cassy_architecture_specification_v1.md`
- `store_pos_domain_model_detail_specifications_v2.md`
- `store_pos_erd_specification_v2.md`
- `cassy_cicd_pipeline_strategy_v1.md`
- `cassy_test_automation_specification.md`
- `cassy_repository_audit_production_roadmap_2026_03_11.md`

## Risks / Gaps / Ambiguities
- Tidak ditemukan gap fatal saat ekstraksi. Tetap review ulang bagian tabel/angka jika dokumen ini akan dijadikan baseline implementasi final.

## Reviewer Notes
- Struktur telah dinormalisasi ke Markdown yang konsisten dan siap dipakai untuk design review / engineering handoff.
- Istilah utama dipertahankan sedekat mungkin dengan dokumen sumber untuk menjaga traceability lintas artefak.
- Bila ada konflik antar dokumen, prioritaskan source of truth, artefak yang paling preskriptif, dan baseline yang paling implementable.
- Untuk repo reality 2026-03-19, lihat `docs/execution/r3_kmp_architecture_layering_audit.md` dan `docs/execution/roadmap_bridge.md`.

## Source Mapping
- Original source: `cassy_module_project_structure_specification.pdf` (PDF, 12 pages)
- Output markdown: `cassy_module_project_structure_specification.md`
- Conversion approach: text extraction -> normalization -> structural cleanup -> standardized documentation wrapper.
- Note: beberapa tabel/list di PDF dapat mengalami wrapping antar baris; esensi dipertahankan, tetapi layout tabel asli tidak dipertahankan 1:1.
