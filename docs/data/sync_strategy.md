# Sync Strategy [CURRENT]

Cassy mengimplementasikan mekanisme sinkronisasi data yang **Explicit** dan **Durable** menggunakan Outbox Pattern.

## 1. Outbox Pattern
Setiap mutasi bisnis di database lokal wajib menuliskan satu atau lebih record ke tabel `OutboxEvent` dalam transaksi SQL yang sama.
- **Tujuan**: Menjamin atomisitas antara perubahan data lokal dan niat untuk sinkronisasi.
- **Payload**: Data dikirim dalam format JSON yang berisi aggregate state atau delta perubahan.

## 2. Sync Lifecycle
1.  **Capture**: Aplikasi menulis ke tabel `OutboxEvent`.
2.  **Queue**: Event berada dalam status `PENDING`.
3.  **Transport**: `SyncReplayService` membaca event tertua yang belum terproses dan mengirimkannya ke endpoint HQ.
4.  **Ack**: HQ memberikan respon sukses; status event diubah menjadi `PROCESSED`.
5.  **Fail**: Jika gagal, status menjadi `FAILED` dengan `lastErrorMessage`. Sistem akan melakukan retry berdasarkan kebijakan back-off.

## 3. Postur Sinkronisasi V1
Untuk rilis awal, Cassy menggunakan **Manual Replay/Retry**:
- Sinkronisasi tidak dilakukan secara real-time per detik untuk menjaga performa UI.
- Kasir dapat melihat status sinkronisasi di dashboard dan menekan tombol "Retry Sync" jika ada backlog.
- **Background Worker**: Digunakan untuk sinkronisasi otomatis saat aplikasi idle.

## 4. Penanganan Konflik [TARGET-STATE]
- **Otoritas**: HQ adalah sumber kebenaran akhir (*Final Authority*).
- **Conflict Strategy**: Jika versi data di lokal tertinggal jauh, HQ akan menolak mutasi dan mengirimkan status `CONFLICT`. Aplikasi wajib menampilkan UI rekonsiliasi bagi supervisor untuk memilih data yang benar.

## 5. Master Data Refresh
Data master (produk, harga, kebijakan) ditarik dari HQ melalui mekanisme **Snapshot Pull**:
- Aplikasi menyimpan `last_masterdata_version`.
- Setiap startup, aplikasi mengecek apakah ada versi master baru di HQ.
