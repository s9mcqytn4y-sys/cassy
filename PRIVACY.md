# Cassy Privacy Baseline

Updated: 2026-03-27

## FACT
- Cassy V1 memakai posture local-first.
- Data operasional utama tersimpan di storage lokal user Windows, umumnya di `%USERPROFILE%\\.cassy`.
- Repo ini tidak mengaktifkan telemetry cloud wajib, background sync wajib, atau outbound analytics sebagai syarat transaksi harian.
- Diagnostics dikumpulkan secara eksplisit melalui script/manual action, bukan dikirim otomatis.

## DATA CATEGORIES
- Data toko dan terminal
- Data operator dan role
- PIN hash dan salt operator lokal
- Data transaksi penjualan, pembayaran, cash movement, inventory, reporting, dan outbox lokal
- File diagnostics dan backup yang dijalankan operator/admin secara sadar

## WHAT CASSY DOES NOT CLAIM
- Tidak mengklaim end-to-end encrypted cloud sync.
- Tidak mengklaim anonymization otomatis untuk semua artifact diagnostics.
- Tidak mengklaim compliance framework eksternal tertentu yang belum dibuktikan di repo.

## OPERATOR RESPONSIBILITIES
- Lindungi akses Windows user account yang menyimpan data Cassy.
- Gunakan backup lokal sebelum install/update candidate.
- Perlakukan export reporting dan backup sebagai data sensitif outlet.
- Jangan membagikan folder diagnostics tanpa review, karena masih dapat memuat metadata sistem dan path lokal.

## SECURITY BASELINE
- PIN operator disimpan sebagai hash + salt, bukan plain text.
- Action sensitif seperti cash control, approval, dan void sale memiliki gate capability/role.
- Diagnostics dikumpulkan on-demand, bukan silent upload.

## RECOMMENDATION
- Untuk distribusi pilot/produksi, lampirkan dokumen ini bersama `EULA.md`, `SECURITY.md`, dan `docs/user/operator_quickstart.md`.
