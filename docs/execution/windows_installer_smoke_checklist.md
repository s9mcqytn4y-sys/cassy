# Windows Installer Smoke Checklist

Dokumen ini adalah checklist manual yang jujur untuk soft blocker installer Windows. Repo saat ini belum punya automation end-to-end untuk install/uninstall EXE installer.

## Input artifact

- EXE installer: `apps/desktop-pos/build/compose/binaries/main/exe/Cassy-0.1.0.exe`
- App distribution: `apps/desktop-pos/build/compose/binaries/main/app/Cassy/`
- Source smoke: `.\gradlew :apps:desktop-pos:run --args="--smoke-run"`
- Distribution smoke: `powershell -ExecutionPolicy Bypass -File tooling/scripts/Invoke-DesktopDistributionSmoke.ps1`
- State backup baseline: `powershell -ExecutionPolicy Bypass -File tooling/scripts/Backup-CassyDesktopState.ps1`
- Diagnostics baseline: `powershell -ExecutionPolicy Bypass -File tooling/scripts/Collect-WindowsReleaseDiagnostics.ps1`

## Truth posture

- [FACT] Artifact installer repo saat ini adalah `EXE`, bukan `MSI`.
- [FACT] Karena bukan `MSI`, jalur `msiexec` tidak berlaku untuk artifact yang dihasilkan turn ini.
- [FACT] Checklist ini tetap manual sampai install/uninstall benar-benar dijalankan di host Windows yang diizinkan.

## Manual checklist

1. Jalankan backup state lokal.
1. Jalankan installer EXE di mesin Windows pilot.
2. Verifikasi wizard install selesai tanpa error fatal.
3. Jalankan aplikasi hasil install.
4. Pastikan app membuka bootstrap/login flow, bukan crash di startup.
5. Lakukan bootstrap store/terminal/operator baseline.
6. Login dengan PIN supervisor valid.
7. Buka business day.
8. Start shift dengan opening cash valid.
9. Pastikan catalog tampil dan cart menerima item.
10. Tutup aplikasi lalu buka lagi untuk cek restore context baseline.
11. Coba PIN salah berulang sampai lockout muncul.
12. Jalankan uninstall.
13. Verifikasi shortcut/start menu/entry uninstall hilang sesuai perilaku installer.
14. Catat leftover file/folder jika ada.
15. Jalankan diagnostics collector bila ada failure atau perilaku aneh.

## Evidence log template

- Tanggal:
- Mesin/OS:
- Installer path:
- Backup path:
- Install result:
- Launch result:
- Bootstrap/login result:
- Day/shift result:
- Catalog/cart result:
- Restore result:
- Lockout result:
- Uninstall result:
- Leftover files:
- Diagnostics path:
- Keputusan:

## Status saat ini

- [FACT] Checklist ini sudah ada di repo.
- [UNKNOWN] Eksekusi manual checklist ini belum dilakukan dalam turn ini.
