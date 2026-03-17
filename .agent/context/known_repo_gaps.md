# Cassy Known Repo Gaps (Updated 2026-03-20)

## Risks that must stay visible

- `:shared` masih menjadi legacy bridge dan belum sepenuhnya dievakuasi ke bounded context yang bersih.
- Access/PIN saat ini adalah local baseline untuk foundation, bukan security-hardening final.
- **Hosted CI Validation**: Status hosted runner hanya boleh dianggap valid bila ada run remote yang benar-benar selesai; validasi lokal saja tidak cukup.
- **Sync/Reporting**: Migration replay, sync visibility, dan reporting penuh masih di luar cakupan foundation saat ini.
- **Android Checkout Lane**: Checkout finality dibuktikan di desktop-first lane; Android tetap parity/business-semantics lane dan belum menjadi owner UX finality.
- **Cross-Context Atomicity**: Finalisasi kini punya bundle durable + replay test sehingga operator tidak melihat sale final sebelum `inventory` dan intent `kernel` selesai. Namun ini tetap bukan satu transaksi ACID fisik lintas tiga database.

## Gaps recently reduced (Hardened)

- **M5 Hardening**: Barcode/SKU lookup sudah stabil dan terintegrasi dengan `SalesService`.
- **Basket Persistence**: Keranjang belanja kini tersimpan otomatis di database lokal (ActiveBasket), menjamin data tidak hilang saat aplikasi ditutup paksa.
- **Inventory Ownership**: Mutasi stok saat checkout kini wajib melalui `shared:inventory:InventoryService`, menghentikan penulisan stok langsung dari modul sales.
- **Kotlin 2.3.20**: Seluruh modul telah disinkronkan ke Kotlin 2.3.20 untuk stabilitas jangka panjang.
- **Test Coverage**: Regresi pada `SalesServiceTest` dan `AccessServiceTest` telah diperbaiki dan diverifikasi.
- **Windows Packaging**: Artifact EXE berhasil di-generate secara lokal dan diverifikasi melalui smoke run.
- **R1 / M6 Finality Contracts**: `shared:sales` kini memakai `PaymentStatus`, `PaymentState`, `SaleCompletionResult`, `CompletedSaleReadback`, dan `ReceiptSnapshotDocument` sebagai kontrak finality typed.
- **R1 / M6 Persistence**: Snapshot struk final kini dipersist sebagai artefak final terstruktur dengan metadata template thermal, dibaca ulang dari sumber final yang sama, dan dimigrasikan lewat SQLDelight migration yang tervalidasi.
- **R1 / M6 Complete-Sale Facade**: Checkout kini lewat facade finalisasi dengan `PaymentGatewayPort`, outcome `COMPLETED | PENDING | REJECTED`, callback handling, dan replay guard untuk duplicate callback / retry.
- **Hardware Honesty**: Desktop tidak lagi menampilkan printer siap palsu; status printer/scanner/cash drawer kini lewat hardware port yang bisa di-fake di test tanpa device fisik.
- **R1 / M6 Finalization Bundle**: `shared:sales` kini menyimpan bundle finalisasi durable, lalu crash/replay tests membuktikan recovery setelah gagal di sela inventory dan kernel tanpa efek ganda.
- **R1 / M6 Desktop Cashier Flow**: Desktop kini memakai quote tunai dari service, preview struk final dari snapshot persisted, status print yang terlihat, tombol batal draft, dan reprint dari source final yang sama.
