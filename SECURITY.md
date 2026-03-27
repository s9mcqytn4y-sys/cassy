# Cassy Security Baseline

Updated: 2026-03-27

## FACT
- Security posture Cassy V1 saat ini adalah local-first desktop security baseline, bukan enterprise zero-trust suite.
- JDK 17 adalah toolchain wajib untuk build dan packaging resmi.
- Role/capability gate aktif untuk approval operasional, cash flow, inventory, dan void sale.
- Repo tidak boleh memuat secret runtime atau credential produksi.

## REPORTING
- Vulnerability atau kelemahan desain harus dilaporkan secara privat kepada maintainer repo, bukan lewat issue publik yang membocorkan detail eksploitasi.

## BASELINE CONTROLS
- PIN operator di-hash dan di-salt.
- Installer Windows memakai lane `EXE`/`MSI` yang bisa di-diagnose dan di-uninstall secara scripted.
- Diagnostics dikumpulkan secara sadar melalui `tooling/scripts/Collect-WindowsReleaseDiagnostics.ps1`.
- Backup state lokal tersedia melalui `tooling/scripts/Backup-CassyDesktopState.ps1`.

## KNOWN LIMITS
- Access/PIN saat ini adalah baseline operasional, bukan MFA atau hardware-backed auth.
- Tidak ada attestation binary, code signing, atau secure auto-update di repo saat ini.
- Conflict sync durable dan backend transport nyata masih di luar scope aktif desktop-first V1.
- Profiler-backed performance/security evidence belum otomatis berjalan di CI.

## OPERATOR / ADMIN GUIDANCE
- Jalankan Cassy pada akun Windows yang terlindungi.
- Batasi akses fisik ke mesin kasir.
- Simpan backup dan export reporting di lokasi yang dikendalikan outlet.
- Review artifact diagnostics sebelum dibagikan keluar organisasi.
