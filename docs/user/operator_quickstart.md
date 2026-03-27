# Operator Quick Start

Updated: 2026-03-27

## Sebelum mulai
- pastikan Cassy ter-install dari artifact resmi
- pastikan JDK tidak diperlukan oleh operator; JDK hanya untuk build/developer
- siapkan akun operator dan PIN
- siapkan backup lokal sebelum update candidate

## Alur harian minimum
1. Buka Cassy dan login dengan PIN operator.
2. Buka business day jika hari kerja belum aktif.
3. Start shift dan masukkan opening cash.
4. Scan barcode atau cari produk, lalu tambahkan ke cart.
5. Finalisasi pembayaran.
6. Jika ada salah input sale cash final, pakai jalur `Void (F7)` dengan reason code yang tepat.
7. Jika produk belum siap scan/lookup, buka `Inventori > Master Data` untuk atur kategori, SKU, barcode, dan image ref produk.
8. Review `Ringkasan (F8)` untuk daily summary, shift summary, sync status, dan issue operasional.
9. Export reporting bundle bila owner/supervisor membutuhkan snapshot hari ini.
10. Tutup shift dan business day sesuai guardrail saat operasional selesai.

## Shortcut utama
- `F1` atau `F5`: sync/reload visibility
- `F7`: void sale cash yang sudah final
- `F8`: ringkasan/reporting
- `F9`: inventori / stock truth
- `F10`: kas
- `F11`: close shift
- `F12`: diagnostics
- `Ctrl+K`: command palette
- `Ctrl+/`: bantuan shortcut
- `Ctrl+Shift+S`: sync center
- `Ctrl+Shift+C`: cash control
- `Ctrl+Shift+I`: inventori

## Approval cepat
- Jika kasir butuh menyetujui approval sensitif, Cassy akan membuka mini-flow step-up auth.
- Supervisor/owner cukup pilih nama operator approver, masukkan PIN lokal, lalu lanjutkan approve/tolak tanpa mengambil alih sesi kasir.

## Jika terjadi masalah
- jangan hapus database lokal secara manual
- kumpulkan diagnostics dengan `tooling/scripts/Collect-WindowsReleaseDiagnostics.ps1`
- backup data lokal dengan `tooling/scripts/Backup-CassyDesktopState.ps1`
- laporkan issue dengan menyertakan langkah reproduksi dan jam kejadian
