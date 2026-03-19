# R2 Owner Boundary Refresh

Updated: 2026-03-19

## FACT
- Concern R2 foundation yang dipindah/ditahan keluar dari `:shared` legacy:
  - dashboard readiness semantics
  - opening cash policy
  - light approval requirement
  - explicit blocker reason untuk open day / start shift / sales unlock
- Concern itu sekarang hidup di `shared:kernel`.
- `apps:desktop-pos` hanya menerima snapshot dan execution result.
- `shared:sales` dan `shared:inventory` tidak menerima ownership baru dari Block 1 ini.

## BOUNDARY TABLE

| Concern | Before | After |
|:--|:--|:--|
| open day gating | campuran controller + kernel basic service | `shared:kernel` |
| start shift policy | mostly `ShiftService` basic + UI parse | `shared:kernel` with policy/evaluation/execution split |
| approval requirement | tidak ada | `shared:kernel` |
| dashboard readiness | implicit di controller stage | `shared:kernel` snapshot + desktop render |
| orphan business day/catalog legacy UI | berada di `:shared` | dihapus |

## INTERPRETATION
- Extraction ini agresif tetapi tetap bounded karena tidak menyentuh ownership checkout, inventory, atau sync.

## RISK
- `shared` aggregator module masih ada sebagai compatibility layer untuk dependency graph, jadi repo belum sepenuhnya bebas dari legacy bridge.

## RECOMMENDATION
- Hindari menaruh approval/cash movement baru di `:shared`; lanjutkan langsung ke `shared:kernel` atau bounded context baru saat benar-benar perlu.
