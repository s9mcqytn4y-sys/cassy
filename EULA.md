# Cassy Desktop EULA Baseline

Updated: 2026-03-27

## FACT
- Dokumen ini adalah baseline EULA internal untuk distribusi desktop Cassy V1.
- Posture produk saat ini adalah desktop-first, single-outlet, local-first retail operating core.
- Distribusi resmi yang didukung repo saat ini adalah Windows desktop melalui artifact `EXE` dan `MSI`.

## GRANT
- Lisensi penggunaan hanya diberikan untuk instalasi dan pengoperasian Cassy sesuai perjanjian tertulis terpisah atau distribusi resmi yang menyertakan dokumen ini.
- Hak penggunaan bersifat terbatas, non-exclusive, non-transferable, dan tidak mencakup redistribusi ulang.

## RESTRICTIONS
- Dilarang melakukan reverse engineering, decompilation, atau modifikasi binary distribusi tanpa izin tertulis.
- Dilarang menghapus notice hak cipta, notice third-party, atau metadata distribusi resmi.
- Dilarang menggunakan Cassy untuk beban operasional di luar scope yang didukung tanpa risk acceptance internal.

## DATA OWNERSHIP
- Data operasional outlet tetap dimiliki operator/pemilik outlet.
- Cassy V1 menyimpan truth utama secara lokal pada mesin operator dan tidak mengaktifkan sinkronisasi backend sebagai hard dependency transaksi harian.

## SUPPORT BOUNDARY
- Support resmi hanya berlaku untuk workflow yang dinyatakan didukung pada `docs/execution/desktop_device_support_matrix.md` dan runbook repo.
- Perangkat eksternal atau integrasi di luar matrix dukungan diperlakukan sebagai unsupported boundary sampai ada evidence lane baru.

## WARRANTY POSTURE
- Distribusi kandidat release diberikan dengan posture "best effort" sesuai quality gate repo.
- Tidak ada jaminan kesesuaian untuk scope ERP, multi-outlet orchestration penuh, CRM breadth, accounting penuh, atau hardware matrix luas.
