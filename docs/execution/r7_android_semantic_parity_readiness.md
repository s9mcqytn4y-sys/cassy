# R7 Android Semantic Parity Readiness

Updated: 2026-03-27

## FACT
- Android tetap diposisikan sebagai semantic parity lane, bukan frontline delivery lane.
- Verification matrix turn ini sudah melewati `build`, `test`, `lint`, dan artifact Android yang relevan sebagai bagian dari `.\gradlew build`.
- `apps/android-pos` tetap memakai parity posture dengan `proguard-rules.pro` minimal dan tidak menggeser ownership domain keluar dari shared core.
- Shared core yang dipakai Android sekarang sudah lebih stabil untuk parity karena R4-R6 hardening tidak menambah concern baru ke legacy `:shared`.

## ASSUMPTION
- Target R7 berikutnya adalah menjaga semantic parity untuk access, operational control, cashier semantics, reporting readback, dan sync visibility, bukan mengejar UI parity 1:1 dengan desktop.

## RISK
- Android belum menjadi owner UX finality atau owner release lane.
- Belum ada matrix parity test khusus Android yang memverifikasi reporting readback dan replay visibility dari surface Android.

## RECOMMENDATION
- Langkah R7 yang paling bernilai sesudah hosted rerun adalah menambah parity checks Android untuk:
  - operational summary/readback
  - sync visibility/error surface
  - guided operations semantics
- Jangan memindahkan concern packaging, device-heavy integration, atau Windows release concern ke Android lane.
