# R3 Inventory Owner Boundary

Updated: 2026-03-19

## FACT
- `shared:inventory`
  - owner untuk balance, ledger, discrepancy review, layer rotation, count/adjustment semantics, dan void-impact classification contract.
- `shared:sales`
  - boleh meminta efek inventory lewat `InventoryService`.
  - tidak boleh menulis ledger/balance langsung.
- `shared:kernel`
  - owner untuk access, reason code, approval semantics lintas domain, audit, dan outbox intent.
- `apps:desktop-pos`
  - owner presentasi flow inventory desktop.
  - tidak menghitung truth stok secara lokal.
- `shared:masterdata`
  - tetap owner product identity dasar (`product_id`, `sku`, barcode lookup posture, `imageUrl` compatibility).

## FACT
- Tidak ada ownership inventory baru yang ditambahkan ke legacy `shared/src`.
- Legacy `:shared` tidak dipakai sebagai tempat mutasi stok baru.

## INTERPRETATION
- Boundary sale -> inventory sekarang lebih jujur: sales finalize, inventory mutate.
- Temporary bridge yang masih tersisa hanya compatibility data/UI yang tidak mengambil ownership stok.

## RISK
- Product management masih appendix boundary, belum menjadi suite CRUD/masterdata penuh.
- Approval mode future-safe (`LIGHT_PIN | SECOND_PIN | DUAL_AUTH`) belum menjadi engine inventory approval penuh; yang shipped tetap `LIGHT_PIN`.

## RECOMMENDATION
- Jika ada fitur baru yang menyentuh stok, letakkan application policy di `shared:inventory`, bukan di desktop controller atau `shared:sales`.
- Jika ada kebutuhan cross-domain, minta reason/audit/approval ke `shared:kernel`, bukan duplikasi lokal.
