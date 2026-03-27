# Cassy Known Repo Gaps (Updated 2026-03-27)

## Risks that must stay visible

- `:shared` masih menjadi legacy bridge dan belum sepenuhnya dievakuasi ke bounded context yang bersih.
- Access/PIN saat ini adalah local baseline untuk foundation, bukan security-hardening final.
- **Sync/Replay Future Lane**: Transport backend nyata dan persistence conflict masih belum dibuka, tetapi ini bukan lagi blocker untuk definisi R6 local-boundary.
- **Android Checkout Lane**: Checkout finality dibuktikan di desktop-first lane; Android tetap parity/business-semantics lane dan belum menjadi owner UX finality.
- **Cross-Context Atomicity**: Finalisasi kini punya bundle durable + replay test sehingga operator tidak melihat sale final sebelum `inventory` dan intent `kernel` selesai. Namun ini tetap bukan satu transaksi ACID fisik lintas tiga database.
- **Void Execution**: Dashboard readiness untuk void sudah jujur, tetapi resolver execution lintas sales/cashflow/inventory/reporting belum dibuka.
- **Approval Lane Depth**: Approval operasional dan inventory kini durable, tetapi masih light approval berbasis operator supervisor/owner aktif + reason code.
- **Profiler Evidence**: Memory/performance belum punya profiler snapshot langsung; evidence saat ini masih static audit + runtime verification.

## Gaps recently reduced (Hardened)

- **M5 Hardening**: Barcode/SKU lookup sudah stabil dan terintegrasi dengan `SalesService`.
- **Basket Persistence**: Keranjang belanja kini tersimpan otomatis di database lokal (`ActiveBasket`), menjamin data tidak hilang saat aplikasi ditutup paksa.
- **Inventory Ownership**: Mutasi stok saat checkout kini wajib melalui `shared:inventory:InventoryService`, menghentikan penulisan stok langsung dari modul sales.
- **Kotlin 2.2.10**: Seluruh modul saat ini sinkron ke Kotlin 2.2.10 sesuai `libs.versions.toml`.
- **Windows Packaging**: Artifact EXE dan MSI berhasil di-generate secara lokal, diverifikasi via distribution smoke, lalu install/repair/uninstall MSI lulus scripted evidence.
- **R1 / M6 Finality Contracts**: `shared:sales` kini memakai `PaymentStatus`, `PaymentState`, `SaleCompletionResult`, `CompletedSaleReadback`, dan `ReceiptSnapshotDocument` sebagai kontrak finality typed.
- **R1 / M6 Persistence**: Snapshot struk final dipersist sebagai artefak final terstruktur dengan metadata template thermal, dibaca ulang dari sumber final yang sama, dan dimigrasikan lewat SQLDelight migration yang tervalidasi.
- **R1 / M6 Complete-Sale Facade**: Checkout kini lewat facade finalisasi dengan `PaymentGatewayPort`, outcome `COMPLETED | PENDING | REJECTED`, callback handling, dan replay guard untuk duplicate callback / retry.
- **Hardware Honesty**: Desktop tidak lagi menampilkan printer siap palsu; status printer/scanner/cash drawer kini lewat hardware port yang bisa di-fake di test tanpa device fisik.
- **R1 / M6 Finalization Bundle**: `shared:sales` menyimpan bundle finalisasi durable, lalu crash/replay tests membuktikan recovery setelah gagal di sela inventory dan kernel tanpa efek ganda.
- **R2 / Block 3 Final Gate**: Matrix verifikasi final sudah diulang pada 2026-03-26 dan gate teknis tetap hijau; blocker utama yang tersisa sekarang adalah void execution resolver.
- **R5 Visibility & Reporting Lite**: Daily summary, shift summary, sync status, dan operational issue readback kini hidup di desktop-first lane.
- **R5 Reporting Export**: Export bundle CSV/HTML untuk owner/supervisor kini hidup di desktop reporting dan lahir dari snapshot lokal yang sama.
- **R6 Boundary Hardening**: Outbox read path status-aware, failed requeue, replay worker minimal, dan prune processed sudah hidup.
- **Hosted Windows Evidence**: Mainline Evidence run `23622401164` sukses dengan artifact EXE, app bundle, MSI, installer evidence, diagnostics, dan manifest.
