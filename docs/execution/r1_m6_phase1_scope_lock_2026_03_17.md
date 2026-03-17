# R1 / M6 Phase 1 Scope Lock

Tanggal: 2026-03-17  
Status: Scope lock untuk eksekusi berikutnya, bukan klaim feature DONE

## Sumber bukti
- PDF roadmap `c:\Users\Acer\Downloads\cassy_roadmap_contract_final_2026_03_16.pdf`
- [settings.gradle.kts](/c:/Users/Acer/AndroidStudioProjects/Cassy/settings.gradle.kts)
- [docs/execution/roadmap_bridge.md](/c:/Users/Acer/AndroidStudioProjects/Cassy/docs/execution/roadmap_bridge.md)
- [.agent/context/module_map.md](/c:/Users/Acer/AndroidStudioProjects/Cassy/.agent/context/module_map.md)
- [.agent/context/known_repo_gaps.md](/c:/Users/Acer/AndroidStudioProjects/Cassy/.agent/context/known_repo_gaps.md)
- [SalesService.kt](/c:/Users/Acer/AndroidStudioProjects/Cassy/shared/sales/src/commonMain/kotlin/id/azureenterprise/cassy/sales/application/SalesService.kt)
- [SalesRepository.kt](/c:/Users/Acer/AndroidStudioProjects/Cassy/shared/sales/src/commonMain/kotlin/id/azureenterprise/cassy/sales/data/SalesRepository.kt)
- [SalesDatabase.sq](/c:/Users/Acer/AndroidStudioProjects/Cassy/shared/sales/src/commonMain/sqldelight/id/azureenterprise/cassy/sales/db/SalesDatabase.sq)
- [InventoryService.kt](/c:/Users/Acer/AndroidStudioProjects/Cassy/shared/inventory/src/commonMain/kotlin/id/azureenterprise/cassy/inventory/application/InventoryService.kt)
- [KernelRepository.kt](/c:/Users/Acer/AndroidStudioProjects/Cassy/shared/kernel/src/commonMain/kotlin/id/azureenterprise/cassy/kernel/data/KernelRepository.kt)
- [KernelDatabase.sq](/c:/Users/Acer/AndroidStudioProjects/Cassy/shared/kernel/src/commonMain/sqldelight/id/azureenterprise/cassy/kernel/db/KernelDatabase.sq)
- [DesktopAppController.kt](/c:/Users/Acer/AndroidStudioProjects/Cassy/apps/desktop-pos/src/jvmMain/kotlin/id/azureenterprise/cassy/desktop/DesktopAppController.kt)
- [CassyCatalogComponents.kt](/c:/Users/Acer/AndroidStudioProjects/Cassy/apps/desktop-pos/src/jvmMain/kotlin/id/azureenterprise/cassy/desktop/CassyCatalogComponents.kt)
- [Main.kt](/c:/Users/Acer/AndroidStudioProjects/Cassy/apps/desktop-pos/src/jvmMain/kotlin/id/azureenterprise/cassy/desktop/Main.kt)
- [shared/build.gradle.kts](/c:/Users/Acer/AndroidStudioProjects/Cassy/shared/build.gradle.kts)
- [apps/android-pos/build.gradle.kts](/c:/Users/Acer/AndroidStudioProjects/Cassy/apps/android-pos/build.gradle.kts)
- [shared/src/commonMain/kotlin/id/azureenterprise/cassy/di/Koin.kt](/c:/Users/Acer/AndroidStudioProjects/Cassy/shared/src/commonMain/kotlin/id/azureenterprise/cassy/di/Koin.kt)

## Kontrak scope R1 yang dikunci
Mengacu ke PDF roadmap, R1 = checkout, payment validity, receipt snapshot, dan readback/history. Untuk repo ini, lane eksekusi utama dikunci ke desktop cashier flow lebih dulu; Android tetap parity/business semantics lane.

### In scope
- Validasi payment state sebelum sale ditandai final.
- Finalisasi sale yang konsisten dengan shift dan terminal aktif.
- Receipt snapshot final sebagai artifact persisted.
- Readback/history dari source final yang sama dengan finalize.
- Print/reprint dari final snapshot yang persisted.
- Audit/outbox intent pada saat finalisasi sale.
- Mutasi inventory hanya pada jalur checkout yang sah dan seperlunya.
- Desktop cashier flow sebagai execution lane utama R1.
- Schema dan migration yang langsung terkait `Sale`, `SalePayment`, `ReceiptSnapshot`, audit/outbox intent, dan query history/readback.

### Out of scope
- Reporting, owner summary, atau read model operasional lebih luas.
- Full sync runtime, replay runtime, conflict resolution, atau UI sync detail.
- Return/refund, approval engine lebih luas, dan cash control di luar kebutuhan finality checkout.
- Multi-outlet, HQ dependency, atau hardware ecosystem printing penuh.
- Android feature expansion di luar parity compile/semantic baseline.
- Refactor besar untuk evakuasi total `:shared`.
- Ownership baru di `:shared`.

## Owner boundary minimum

| Area | Owner minimum untuk R1 | Bukti repo | Bukan owner untuk fase ini |
|---|---|---|---|
| `shared:kernel` | access context, business day, shift, audit, outbox | `KernelRepository` expose `getTerminalBinding`, `getActiveAccessSession`, `getActiveBusinessDay`, `getActiveShift`, `insertAudit`, `insertEvent`; schema `OutboxEvent`, `AuditLog`, `BusinessDay`, `Shift` ada di `KernelDatabase.sq` | tidak owns basket, sale item, payment state, receipt snapshot |
| `shared:sales` | cart, pricing baseline, checkout orchestration, sale/payment persistence, receipt snapshot final, readback/history | `SalesService.checkout`, `getFinalizedSale`, `getSaleHistory`, `getReceiptForPrint`; `SalesRepository.saveSale`, `recordPayment`, `finalizeSale`, `getCompletedSales`; schema `Sale`, `SalePayment`, `ReceiptSnapshot`, `ActiveBasket` | tidak owns ledger stok, tidak owns driver printer, tidak owns auth/day/shift truth |
| `shared:inventory` | stock ledger mutation akibat sale final | `InventoryService.recordSaleCompletion` menulis `InventoryTransaction` bertipe `SALE` | tidak menentukan validitas payment/final sale |
| `apps:desktop-pos` | guided cashier flow, payment trigger UI, receipt preview/reprint entry point, honest failure state | `DesktopAppController.checkoutCash`, `reprintLastReceipt`, `refreshStage`; `CassyCartPanel` memunculkan `Bayar Tunai`, `Print Ulang Struk`, dan riwayat final | tidak owns business invariant checkout/finality |
| persistence / migration | schema/query/migration per bounded context yang langsung menopang finality | `SalesDatabase.sq`, `KernelDatabase.sq`, plus `1.sqm` di `shared/sales`, `shared/kernel`, `shared/inventory` | tidak boleh menaruh truth baru ke `shared/src/commonMain/sqldelight` legacy |

## Freeze area legacy `:shared`
- `:shared` root tetap legacy bridge, bukan owner baru. Bukti: `.agent/context/module_map.md` dan `.agent/context/known_repo_gaps.md` sama-sama menyebut `:shared` sebagai legacy bridge.
- `shared/build.gradle.kts` masih menjadi aggregator `api(project(":shared:kernel"))`, `api(project(":shared:masterdata"))`, `api(project(":shared:sales"))`, `api(project(":shared:inventory"))`.
- `shared/src/commonMain/kotlin/id/azureenterprise/cassy/di/Koin.kt` masih menggabungkan module dan presentation legacy, termasuk `catalogModule`.
- `shared/src/commonMain/kotlin/id/azureenterprise/cassy/ui/*` masih berisi UI/viewmodel legacy (`CatalogScreen`, `CatalogViewModel`, `BusinessDayScreen`).
- `apps/android-pos/build.gradle.kts` masih bergantung ke `project(":shared")`.

### Implikasi freeze
- Jangan menambah invariant finality baru ke `shared/src/commonMain/kotlin/id/azureenterprise/cassy/ui/*`.
- Jangan menambah schema/truth baru ke `shared/src/commonMain/sqldelight`.
- Jangan memindahkan owner finality ke `:shared`; kalau butuh bridge, tetap tipis dan sementara.
- Pembersihan struktur yang aman untuk fase berikutnya cukup berupa penyusutan referensi legacy yang langsung mengganggu R1, bukan evakuasi besar seluruh module.

## Target file / class / schema untuk Phase 2

### Wajib disentuh atau diverifikasi ketat
- [SalesService.kt](/c:/Users/Acer/AndroidStudioProjects/Cassy/shared/sales/src/commonMain/kotlin/id/azureenterprise/cassy/sales/application/SalesService.kt)
- [SalesRepository.kt](/c:/Users/Acer/AndroidStudioProjects/Cassy/shared/sales/src/commonMain/kotlin/id/azureenterprise/cassy/sales/data/SalesRepository.kt)
- [SalesDatabase.sq](/c:/Users/Acer/AndroidStudioProjects/Cassy/shared/sales/src/commonMain/sqldelight/id/azureenterprise/cassy/sales/db/SalesDatabase.sq)
- [Models.kt](/c:/Users/Acer/AndroidStudioProjects/Cassy/shared/sales/src/commonMain/kotlin/id/azureenterprise/cassy/sales/domain/Models.kt)
- [SalesKernelPort.kt](/c:/Users/Acer/AndroidStudioProjects/Cassy/shared/sales/src/commonMain/kotlin/id/azureenterprise/cassy/sales/application/SalesKernelPort.kt)
- [InventoryService.kt](/c:/Users/Acer/AndroidStudioProjects/Cassy/shared/inventory/src/commonMain/kotlin/id/azureenterprise/cassy/inventory/application/InventoryService.kt)
- [KernelRepository.kt](/c:/Users/Acer/AndroidStudioProjects/Cassy/shared/kernel/src/commonMain/kotlin/id/azureenterprise/cassy/kernel/data/KernelRepository.kt)
- [KernelDatabase.sq](/c:/Users/Acer/AndroidStudioProjects/Cassy/shared/kernel/src/commonMain/sqldelight/id/azureenterprise/cassy/kernel/db/KernelDatabase.sq)
- [DesktopAppController.kt](/c:/Users/Acer/AndroidStudioProjects/Cassy/apps/desktop-pos/src/jvmMain/kotlin/id/azureenterprise/cassy/desktop/DesktopAppController.kt)
- [CassyCatalogComponents.kt](/c:/Users/Acer/AndroidStudioProjects/Cassy/apps/desktop-pos/src/jvmMain/kotlin/id/azureenterprise/cassy/desktop/CassyCatalogComponents.kt)
- [Main.kt](/c:/Users/Acer/AndroidStudioProjects/Cassy/apps/desktop-pos/src/jvmMain/kotlin/id/azureenterprise/cassy/desktop/Main.kt)

### Harus dibekukan atau dibatasi
- [shared/src/commonMain/kotlin/id/azureenterprise/cassy/di/Koin.kt](/c:/Users/Acer/AndroidStudioProjects/Cassy/shared/src/commonMain/kotlin/id/azureenterprise/cassy/di/Koin.kt)
- [shared/src/commonMain/kotlin/id/azureenterprise/cassy/ui/CatalogViewModel.kt](/c:/Users/Acer/AndroidStudioProjects/Cassy/shared/src/commonMain/kotlin/id/azureenterprise/cassy/ui/CatalogViewModel.kt)
- [shared/src/commonMain/kotlin/id/azureenterprise/cassy/ui/CatalogScreen.kt](/c:/Users/Acer/AndroidStudioProjects/Cassy/shared/src/commonMain/kotlin/id/azureenterprise/cassy/ui/CatalogScreen.kt)
- [apps/android-pos/build.gradle.kts](/c:/Users/Acer/AndroidStudioProjects/Cassy/apps/android-pos/build.gradle.kts)
- [shared/build.gradle.kts](/c:/Users/Acer/AndroidStudioProjects/Cassy/shared/build.gradle.kts)

## Blocker jujur sebelum lanjut Phase 2
- State finality di repo harus dievaluasi terhadap working tree saat ini, bukan hanya HEAD, karena ada perubahan lokal yang belum commit pada desktop/sales. Ini mempengaruhi baseline apa yang dianggap “current repo”.
- Dependensi Android ke `:shared` root masih ada. Selama dependency ini hidup, cleanup legacy hanya bisa partial dan harus hati-hati agar parity lane tidak ikut rusak.
- Printing runtime masih belum menjadi bounded context sendiri. Untuk R1, aman hanya mengunci preview/reprint dari final snapshot; jangan memaksakan printer orchestration penuh.
- Migration/schema change harus tetap satu bundle dengan test karena guardrail repo menolak truth baru di legacy path dan SQLDelight migration ada per context.
- Verification task Gradle besar harus dijalankan berurutan di Windows. Percobaan paralel setelah `clean` sempat menghasilkan false failure pada `:apps:android-pos:lintDebug` dan `:apps:desktop-pos:test`.

## Keputusan eksekusi Phase 2
- Fokus perubahan tetap di `shared:sales`, `shared:inventory`, `shared:kernel`, dan `apps:desktop-pos`.
- `:shared` hanya boleh disentuh untuk mengurangi coupling langsung yang menghambat R1, bukan untuk menambah behavior finality.
- Android cukup dijaga compile/parity, bukan lane implementasi utama.

## Verdict Phase 1
- R1 scope explicit: PASS
- Out-of-scope explicit: PASS
- Owner boundary explicit: PASS
- Legacy bridge freeze explicit: PASS
- Target files/classes/schema identified: PASS
- Blocker list honest dan evidence-based: PASS
