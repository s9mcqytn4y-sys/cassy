# Cassy Known Repo Gaps (Updated 2026-03-20)

## Risks that must stay visible

- `:shared` masih menjadi legacy bridge dan belum sepenuhnya dievakuasi ke bounded context yang bersih.
- Access/PIN saat ini adalah local baseline untuk foundation, bukan security-hardening final.
- **M6 Checkout Gap**: Checkout saat ini baru mencatat transaksi lokal dan mutasi stok; finalisasi pembayaran eksternal dan pencetakan struk masih dalam pengembangan.
- **Hosted CI Validation**: Installer smoke test (install/uninstall) belum tervalidasi di GitHub Actions runner karena limitasi environment; validasi masih bergantung pada *Manual Evidence Pack*.
- **Sync/Reporting**: Migration replay, sync visibility, dan reporting penuh masih di luar cakupan foundation saat ini.

## Gaps recently reduced (Hardened)

- **M5 Hardening**: Barcode/SKU lookup sudah stabil dan terintegrasi dengan `SalesService`.
- **Basket Persistence**: Keranjang belanja kini tersimpan otomatis di database lokal (ActiveBasket), menjamin data tidak hilang saat aplikasi ditutup paksa.
- **Inventory Ownership**: Mutasi stok saat checkout kini wajib melalui `shared:inventory:InventoryService`, menghentikan penulisan stok langsung dari modul sales.
- **Kotlin 2.3.20**: Seluruh modul telah disinkronkan ke Kotlin 2.3.20 untuk stabilitas jangka panjang.
- **Test Coverage**: Regresi pada `SalesServiceTest` dan `AccessServiceTest` telah diperbaiki dan diverifikasi.
- **Windows Packaging**: Artifact EXE berhasil di-generate secara lokal dan diverifikasi melalui smoke run.
