# R3 Approval Limitations And Future Hooks

> Historical Note (2026-03-27)
> Catatan ini tetap valid untuk approval breadth, tetapi referensi installer manual adalah konteks historis sebelum R4 local evidence ditutup.

Updated: 2026-03-19

## FACT
- Approval mode shape yang ada di repo:
  - `LIGHT_PIN`
  - `SECOND_PIN`
  - `DUAL_AUTH`
- Yang benar-benar shipped saat Block ini hanya `LIGHT_PIN`.
- `SECOND_PIN` dan `DUAL_AUTH` tetap `NOT_SHIPPED` / future hook.
- Inventory adjustment dan discrepancy resolution high-risk sekarang bisa meminta approval secara durable.
- UI desktop menampilkan limitation note ini secara eksplisit.

## ASSUMPTION
- Local supervisor PIN tetap cukup untuk baseline single-outlet V1 saat ini.

## INTERPRETATION
- Repo sekarang future-safe tanpa memalsukan breadth approval yang belum ada.

## RISK
- Jika kebutuhan compliance naik, `LIGHT_PIN` saja mungkin tidak cukup.
- Approval queue inventory masih lane-local dan belum menjadi orchestration multi-terminal.

## RECOMMENDATION
- Jika phase berikutnya membuka `SECOND_PIN` atau `DUAL_AUTH`, jangan ganti makna `LIGHT_PIN`; tambah lane baru secara eksplisit.
- Tetap dokumentasikan `PDF_NOT_SHIPPED`; referensi installer manual di dokumen ini adalah konteks historis, bukan gap aktif terbaru.
