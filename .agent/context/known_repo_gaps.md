# Cassy Known Repo Gaps

## Risks that must stay visible

- `:shared` masih menjadi legacy bridge dan belum sepenuhnya dievakuasi ke bounded context yang bersih.
- Access/PIN saat ini adalah local baseline untuk foundation, bukan security-hardening final.
- Business day dan shift guardrail sudah hidup, tetapi audit/reporting/receipt closure belum ikut tertutup.
- Inventory module sudah terhubung pada baseline service, tetapi belum terbukti pada flow checkout/finalize sale penuh.
- Windows packaging sudah terbukti lokal, tetapi hosted CI Windows packaging belum punya execution evidence.
- Installer smoke install/uninstall Windows belum tervalidasi end-to-end; yang terbukti baru artifact generation.
- Migration replay, sync visibility, dan release evidence penuh masih di depan milestone foundation ini.

## Gaps recently reduced

- false-ready desktop `NO-SOURCE` build sudah ditutup dengan source-set mapping yang nyata di `apps/desktop-pos`
- M3/M4 false completion di docs sudah diturunkan ke foundation status
- Java target drift ke bytecode > 17 pada shared desktop target sudah dijaga di build-logic
