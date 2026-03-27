# Cassy Known Repo Gaps (Updated 2026-03-27)

## Risks that must stay visible

- `:shared` masih menjadi legacy bridge dan belum sepenuhnya dievakuasi ke bounded context yang bersih.
- Access/PIN saat ini adalah local baseline untuk foundation, bukan security-hardening final.
- **Sync/Replay Future Lane**: Transport backend nyata dan persistence conflict masih belum dibuka, tetapi ini bukan lagi blocker untuk definisi R6 local-boundary.
- **Android Checkout Lane**: Checkout finality dibuktikan di desktop-first lane; Android tetap parity/business-semantics lane dan belum menjadi owner UX finality.
- **Cross-Context Atomicity**: Finalisasi kini punya bundle durable + replay test sehingga operator tidak melihat sale final sebelum `inventory` dan intent `kernel` selesai. Namun ini tetap bukan satu transaksi ACID fisik lintas tiga database.
- **Installer Upgrade Path**: Evidence lokal `baseline -> candidate -> uninstall` sudah hijau. Yang belum ada adalah evidence hosted beta-tag terbaru dan rollback binary lintas versi.
- **Approval Lane Depth**: Approval operasional dan inventory kini durable, tetapi masih light approval berbasis operator supervisor/owner aktif + reason code.
- **Profiler Evidence**: Memory/performance kini sudah punya probe ringan yang dieksekusi lokal, tetapi belum menjadi profiler snapshot penuh.

## Gaps recently reduced (Hardened)

- **M5 Hardening**: Barcode/SKU lookup sudah stabil dan terintegrasi dengan `SalesService`.
- **Basket Persistence**: Keranjang belanja kini tersimpan otomatis di database lokal (`ActiveBasket`), menjamin data tidak hilang saat aplikasi ditutup paksa.
- **Inventory Ownership**: Mutasi stok saat checkout kini wajib melalui `shared:inventory:InventoryService`, menghentikan penulisan stok langsung dari modul sales.
- **Kotlin 2.2.10**: Seluruh modul saat ini sinkron ke Kotlin 2.2.10 sesuai `libs.versions.toml`.
- **Windows Packaging**: Artifact EXE dan MSI berhasil di-generate secara lokal, diverifikasi via distribution smoke, lalu install/repair/uninstall MSI lulus scripted evidence.
- **Windows Upgrade Evidence**: Upgrade `0.1.0 -> 0.2.0-beta.1` sekarang punya scripted evidence lokal dengan smoke sebelum dan sesudah upgrade, lalu uninstall candidate.
- **Performance Probe**: Probe ringan cashier-critical path sekarang hidup di `tooling/scripts/Invoke-DesktopPerformanceProbe.ps1` dan sudah dieksekusi lokal.
- **R1 / M6 Finality Contracts**: `shared:sales` kini memakai `PaymentStatus`, `PaymentState`, `SaleCompletionResult`, `CompletedSaleReadback`, dan `ReceiptSnapshotDocument` sebagai kontrak finality typed.
- **R1 / M6 Persistence**: Snapshot struk final dipersist sebagai artefak final terstruktur dengan metadata template thermal, dibaca ulang dari sumber final yang sama, dan dimigrasikan lewat SQLDelight migration yang tervalidasi.
- **R1 / M6 Complete-Sale Facade**: Checkout kini lewat facade finalisasi dengan `PaymentGatewayPort`, outcome `COMPLETED | PENDING | REJECTED`, callback handling, dan replay guard untuk duplicate callback / retry.
- **Hardware Honesty**: Desktop tidak lagi menampilkan printer siap palsu; status printer/scanner/cash drawer kini lewat hardware port yang bisa di-fake di test tanpa device fisik.
- **R1 / M6 Finalization Bundle**: `shared:sales` menyimpan bundle finalisasi durable, lalu crash/replay tests membuktikan recovery setelah gagal di sela inventory dan kernel tanpa efek ganda.
- **Beta Release Discipline**: Changelog, release config, checklist burn-in, code-signing posture, dan workflow beta release sekarang sudah masuk repo.
- **R5 Visibility & Reporting Lite**: Daily summary, shift summary, sync status, dan operational issue readback kini hidup di desktop-first lane.
- **R5 Reporting Export**: Export bundle CSV/HTML untuk owner/supervisor kini hidup di desktop reporting dan lahir dari snapshot lokal yang sama.
- **R6 Boundary Hardening**: Outbox read path status-aware, failed requeue, replay worker minimal, dan prune processed sudah hidup.
- **Void Execution**: Jalur void sale cash final kini hidup lintas sales/cashflow/reporting, memakai reason code, audit/event, dan visibility reporting yang eksplisit.
- **Hosted Windows Evidence**: Hosted `Mainline Evidence` sudah pernah sukses dengan artifact EXE, app bundle, MSI, installer evidence, diagnostics, dan manifest. Authority aktif tetap mengikuti run terbaru yang benar-benar diverifikasi.
