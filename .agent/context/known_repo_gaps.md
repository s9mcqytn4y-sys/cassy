# Cassy Known Repo Gaps

## Risks that must stay visible

- `:shared` masih menjadi legacy bridge dan belum sepenuhnya dievakuasi ke bounded context yang bersih.
- Access/PIN saat ini adalah local baseline untuk foundation, bukan security-hardening final.
- Business day dan shift guardrail sudah hidup, tetapi audit/reporting/receipt closure belum ikut tertutup.
- Inventory module sekarang sudah menjadi owner mutasi stok untuk checkout baseline, tetapi belum terbukti pada flow checkout/reporting/closing penuh.
- Installer smoke install/uninstall Windows belum tervalidasi end-to-end; yang terbukti baru source smoke, distribution smoke, dan artifact generation.
- Migration replay, sync visibility, dan release evidence penuh masih di depan milestone foundation ini.

## Gaps recently reduced

- false-ready desktop `NO-SOURCE` build sudah ditutup dengan source-set mapping yang nyata di `apps/desktop-pos`
- M3/M4 false completion di docs sudah diturunkan ke foundation status
- Java target drift ke bytecode > 17 pada shared desktop target sudah dijaga di build-logic
- desktop run `Skiko` crash karena mixed Compose runtime sudah ditutup dengan dependency alignment dan smoke run JDK 17
- daemon criteria Java 21 sudah diturunkan kembali ke Java 17
- source smoke desktop sekarang bisa dijalankan lewat `:apps:desktop-pos:run --args="--smoke-run"` tanpa membuka window GUI
- Hosted `Mainline Evidence` run `23142319550` untuk commit `a27ddc7` sudah sukses dan mengunggah artifact `cassy-desktop-exe`, `cassy-desktop-app`, dan `cassy-mainline-evidence`
