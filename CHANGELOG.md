# Changelog

Semua perubahan penting di Cassy dicatat di file ini.

Format changelog ini mengikuti prinsip Keep a Changelog dan versioning pre-release berbasis SemVer.

## [0.2.0-beta.3] - 2026-03-27

### Added
- Sub-route operasional desktop: `Cash Control`, `Void Sale`, `Close Shift`, `Close Day`, `Sync Center`, dan `Diagnostics`.
- Layar `Master Data` di lane inventori untuk pengelolaan kategori, SKU, barcode, dan image reference produk.
- Step-up auth mini-flow supervisor/owner tanpa mengganti sesi kasir aktif.
- Footer desktop ringan yang merangkum versi, store, terminal, dan bantuan shortcut.

### Changed
- `.gitignore` sekarang mengabaikan `.agent/**`, `AGENTS.md`, `GEMINI.md`, `.aiexclude`, dan file AI lokal sejenis agar tidak ikut ter-push.
- Shortcut F-key laptop/desktop dipetakan ulang ke workspace/sub-route yang lebih jelas: `F7` void, `F8` laporan, `F9` inventori, `F10` kas, `F11` close shift, `F12` diagnostics.
- Master data seed diperluas agar demo produk/kategori lebih realistis untuk retail single outlet.
- Topbar title kini mengikuti sub-route aktif agar operator selalu tahu konteks kerja saat ini.

### Fixed
- Flow approval desktop yang sebelumnya mengandalkan sesi approver aktif penuh sekarang bisa memakai verifikasi PIN step-up secara eksplisit.
- Filter kategori dan pencarian master data sekarang memicu refresh state yang konsisten.

## [0.2.0-beta.2] - 2026-03-27

### Added
- App shell desktop baru dengan workspace `Dashboard`, `Kasir`, `Riwayat`, `Inventori`, `Operasional`, `Laporan`, dan `Sistem`.
- Command palette `Ctrl+K`, shortcut help `Ctrl+/`, bottom status strip, dan alias shortcut Ctrl-based untuk laptop.
- Jalur reset database development eksplisit via `--dev-reset-demo` yang hanya aktif jika `cassy.dev.reset.enabled=true`.
- Formatter presentasi untuk label shift/hari/approval/sale agar raw ID tidak tampil vulgar di UI utama.

### Changed
- Entry point pasca-login menjadi guided operations dashboard, bukan langsung cart campuran.
- Checkout desktop dipecah menjadi lookup pane, cart pane, dan checkout/receipt pane.
- Task kompleks inventori, reporting, dan operasional dipindah dari modal besar ke dedicated workspace.
- Seeder masterdata demo diubah ke katalog retail yang lebih realistis dan lebih cocok untuk smoke UX.
- Bootstrap migration desktop untuk `shared:masterdata` sekarang memakai `user_version` + migration path yang eksplisit.

### Fixed
- Logo rail desktop dirapikan agar proporsi lebih stabil di laptop/desktop.
- Regression test masterdata akibat perubahan seed katalog demo.

## [0.2.0-beta.1] - 2026-03-27

### Added
- Jalur `void sale` cash final yang nyata di desktop-first lane.
- Export reporting operasional CSV/HTML yang lebih formal untuk owner/supervisor.
- Baseline legal/privacy/security: `LICENSE`, `EULA.md`, `PRIVACY.md`, `SECURITY.md`, `THIRD_PARTY_NOTICES.md`.
- Support matrix desktop, operator quick start, dan daily operations guide.
- Release candidate checklist, beta burn-in checklist, dan workflow beta release.
- Script upgrade evidence Windows untuk install `baseline -> candidate`.
- Perf/resource probe ringan untuk cashier-critical path.

### Changed
- README, docs execution, dan `.agent/*` disinkronkan ke repo reality terbaru.
- Windows packaging memakai versioning beta resmi dengan package version dan release version terpisah.
- Smoke desktop diperluas ke skenario operasional beta dengan isolated data root.
- Workflow CI/release diperketat dengan authority docs dan migration verify yang lebih lengkap.

### Fixed
- Migrasi `shared:sales` v6 diselaraskan dengan persistence `SaleVoid`.
- Drift status milestone dan versioning artifact yang masih hardcoded `0.1.0`.
- Detekt/test regressions yang muncul selama hardening R5/R6.
