# R2 Foundation Truth Lock

> Historical Note (2026-03-27)
> Snapshot ini merekam posisi R2 pada 2026-03-19. Untuk truth R4-R6 terbaru, pakai `docs/execution/roadmap_bridge.md`, `docs/execution/r4_windows_release_trust.md`, dan `docs/execution/r6_sync_ready_boundary_and_replay.md`.

Updated: 2026-03-19

## FACT
- R1 dire-verify ulang lewat command lokal nyata: `clean`, `build`, `test`, `detekt`, `lint`, `:apps:desktop-pos:smokeRun`, dan subset test R1 yang relevan.
- R1 core tetap lulus setelah Block 1 code changes.
- Pada saat dokumen ini ditulis, installer Windows install/uninstall masih **manual-soft-blocker**.
- `docs/execution/roadmap_bridge.md` sebelumnya stale karena masih menyebut `commonTest` untuk evidence sales/inventory; bridge sekarang harus mengacu ke `desktopTest` dan report yang benar.

## PASS / PARTIAL / FAIL LOCK

| Area | Status | Evidence |
|:--|:--|:--|
| checkout happy path | PASS | `SalesServiceTest`, `DesktopAppControllerTest` |
| failure path minimum | PASS | pending payment, crash replay, FK constraint |
| receipt reprint/readback | PASS | `SalesServiceTest`, `DesktopAppControllerTest` |
| inventory owner boundary | PASS | `InventoryServiceTest`, `SalesServiceTest` |
| audit/outbox intent | PASS | `SalesServiceTest`, `BusinessDayServiceTest`, `ShiftServiceTest` |
| migration minimum | PASS | `SalesPersistenceBootstrapTest` |
| desktop smoke | PASS | `:apps:desktop-pos:smokeRun` |
| hosted installer install/uninstall | PARTIAL (historical) | status 2026-03-19 sebelum scripted local evidence 2026-03-26 |

## INTERPRETATION
- Block 1 boleh lanjut karena R1 core tidak regress.
- Claim desktop readiness harus tetap dibatasi: cashier core + operational foundation, bukan full operational suite.
- Setelah Block 2, foundation operasional sudah mencakup cash control baseline, approval/reason durability, close shift review, dan close day fail-closed.

## RISK
- Jika docs bridge kembali stale, repo akan tampak lebih matang daripada evidence aktualnya.

## RECOMMENDATION
- Setiap block R2 harus memperbarui bridge, contract doc, dan `.agent` file inti di hari yang sama dengan code change.
- Evidence installer historical pada dokumen ini harus dibaca sebagai snapshot lama, bukan status runtime terbaru.
