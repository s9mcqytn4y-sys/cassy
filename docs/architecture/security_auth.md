# Security & Auth Strategy [CURRENT]

Cassy mengadopsi postur keamanan yang ketat namun praktis untuk lingkungan retail, dengan fokus pada personal accountability dan offline readiness.

## 1. Prinsip Utama
- **Personal Identity**: Setiap staf memiliki akun pribadi. Berbagi akun kasir dilarang.
- **Online-First, Offline-Ready**: Login awal harus online untuk pendaftaran device, namun unlock harian didukung secara offline melalui cached credentials.
- **Terminal Binding**: Aplikasi hanya dapat beroperasi jika device sudah terikat (*enrolled*) ke terminal tertentu di toko.

## 2. Model Otentikasi
- **Faktor Utama**: Password akun (untuk login awal/reset).
- **Faktor Harian**: 6-digit PIN (untuk unlock cepat di POS).
- **Faktor Opsional**: Biometrik (Fingerprint/Face) sebagai jembatan pembuka PIN/Secret lokal.

## 3. Approval & Hak Akses
Operasi sensitif membutuhkan otorisasi tambahan:
- **Supervisor PIN**: Diperlukan untuk Void Sale, Refund, Adjustment stok di atas threshold, dan Closing Day dengan varians tinggi.
- **Reason Codes**: Setiap tindakan supervisor wajib menyertakan kode alasan yang akan dicatat di audit log.
- **Audit Trail**: Setiap keputusan approval menghasilkan record `ApprovalAuthDecision` yang akan disinkronkan ke HQ.

## 4. Keamanan Data Lokal
- **Encryption at Rest**: Data kredensial lokal dan enrollment secret disimpan dalam storage terenkripsi platform (Android Keystore / Windows Data Protection).
- **UUIDv7**: Digunakan untuk menjamin keunikan ID transaksi tanpa membocorkan urutan atau informasi sensitif.
- **Append-Only Audit**: Audit log lokal tidak dapat dimodifikasi oleh user dan akan terus dikirim ke HQ sebagai bukti integritas operasional.

## 5. Kebijakan Brute Force
- **Throttling**: Penundaan waktu setelah 5 kali salah PIN.
- **Hard Lock**: Device terkunci setelah 10 kali salah PIN berturut-turut, memerlukan re-auth menggunakan password akun secara online.
