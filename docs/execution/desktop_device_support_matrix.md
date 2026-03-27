# Desktop Device Support Matrix

Updated: 2026-03-27

Dokumen ini adalah authority untuk batas dukungan device dan permission Cassy desktop V1.

| Capability | Status | Evidence | Notes |
|:--|:--|:--|:--|
| Keyboard-first cashier flow | SUPPORTED | desktop UI, smoke run, shortcut rail | Jalur utama kasir. Shortcut aktif untuk sync, void, reporting, stock, cash, shift, dan day flow. |
| Barcode scanner sebagai keyboard wedge | SUPPORTED | product lookup + desktop flow | Cassy memperlakukan scanner wedge sebagai input keyboard. Tidak ada driver khusus yang diwajibkan repo. |
| Receipt printer native integration | NOT SHIPPED | hardware port honesty | UI tidak boleh mengklaim printer siap bila adapter/device belum diintegrasikan. |
| Cash drawer direct integration | NOT SHIPPED | hardware port honesty | Cassy hanya menjaga visibility/status boundary, bukan dukungan drawer matrix luas. |
| Camera barcode scanning desktop | NOT SUPPORTED | repo reality | Scope desktop V1 tidak membuka camera workflow. |
| USB device custom protocol | NOT SUPPORTED | repo reality | Di luar lane aktif sampai ada port native + evidence. |
| Bluetooth/BLE device | NOT SUPPORTED | repo reality | Belum ada capability/adapter resmi. |
| Windows per-user install | SUPPORTED | packaging + installer evidence | Installer MSI memakai `perUserInstall = true`. |
| Windows uninstall cleanliness | SUPPORTED | installer evidence script | Uninstall entry dan installed exe diverifikasi hilang setelah uninstall. |
| Local data backup/restore | SUPPORTED | backup script + runbook | Baseline backup ada; restore binary antar versi bukan bagian proof saat ini. |

## Permission posture
- Cassy desktop tidak bergantung pada permission runtime ala mobile.
- Dukungan perangkat eksternal yang butuh driver/vendor utility tetap menjadi tanggung jawab lane integrasi native berikutnya.
- Jika capability belum supported di matrix ini, UI dan docs harus jujur menyebut boundary tersebut.
