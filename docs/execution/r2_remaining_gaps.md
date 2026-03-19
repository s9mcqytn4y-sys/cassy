# R2 Remaining Gaps

Updated: 2026-03-19

## FACT
- Gap yang masih terbuka setelah verifikasi final:
  - void execution resolver lintas sales/cashflow/inventory/reporting
  - export formal/PDF untuk `ShiftCloseReport`
  - approval depth masih light approval
  - installer Windows install/uninstall evidence masih manual

## IMPACT
- Void belum bisa dioperasikan end-to-end walau dashboard readiness-nya jujur.
- Penutupan shift sudah punya artefak data final, tetapi belum punya dokumen export formal.
- Approval operasional sudah durable, tetapi belum mencapai kontrol dua langkah yang lebih keras.
- Release readiness untuk pengguna akhir Windows belum sekuat runtime/source verification lokal.

## RISK
- Jika R2 dipaksa diberi label `DONE` sekarang, repo akan over-claim pada area void dan release evidence.

## RECOMMENDATION
1. Buka resolver void secara bounded dengan owner tetap di `shared:sales`, `shared:inventory`, dan `shared:kernel`.
2. Tambahkan export/report presenter di atas `ShiftCloseReport`, bukan source data baru.
3. Jika dibutuhkan compliance operasional yang lebih keras, tambahkan re-auth approver tanpa memindahkan approval semantics ke UI.
4. Jalankan checklist installer manual saat release candidate berikutnya dibuat.
