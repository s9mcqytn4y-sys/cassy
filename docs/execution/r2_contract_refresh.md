# R2 Contract Refresh

Updated: 2026-03-19

## FACT
- Kontrak operasional yang kini benar-benar hidup:
  - business day harus aktif sebelum shift dapat dibuka
  - shift aktif + opening cash valid wajib ada sebelum lane kasir terbuka
  - cash control mensyaratkan shift aktif + reason code valid
  - movement di atas threshold membuat approval request durable
  - close shift fail-closed saat ada pending transaction
  - close business day fail-closed saat masih ada open shift atau pending approval
  - dashboard memantulkan readiness truth dari service, bukan hitung lokal di UI
- Kontrak yang belum hidup penuh:
  - void execution resolver end-to-end
  - export formal close report
  - hard approval ceremony

## INTERPRETATION
- Kontrak R2 untuk operational control foundation dan hardening sudah cukup kuat untuk desktop-first single-outlet baseline.
- Kontrak R2 secara keseluruhan belum lengkap untuk label `DONE`.

## RISK
- Menyebut R2 selesai penuh akan menyesatkan pembaca dokumen terhadap scope void dan release evidence.

## RECOMMENDATION
- Gunakan refresh ini sebagai kontrak terkini.
- Dokumen block lama tetap dipertahankan karena masih berguna sebagai traceability evidence.
