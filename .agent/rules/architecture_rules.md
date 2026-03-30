# Architecture Rules

- Desktop Windows adalah frontline utama Cassy V1.
- Android hanya parity lane untuk business semantics, bukan driver UX utama.
- `shared:kernel` memegang access, business day, shift, approval, dan reporting shell.
- `shared:inventory` tetap owner stock truth dan ledger explanation trail.
- `shared:masterdata` memegang kategori, produk, SKU, dan barcode lookup posture.
- UI desktop wajib pasif terhadap domain logic; state/event/effect boundary harus jelas.
- Task kompleks harus hidup sebagai screen/workspace, bukan modal besar.
- Status sistem harus tampil human-readable; raw ID, unknown, dan istilah debug bukan copy final.
- Theme desktop harus terang, tegas, dan operasional; hindari dominasi near-black.
- Revisi shell global wajib dibagi per tahap dan diverifikasi sebelum lanjut ke workspace lain.
