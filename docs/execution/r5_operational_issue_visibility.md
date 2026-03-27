# R5 Operational Issue Visibility

## Overview
Operational Issue Visibility memastikan setiap state yang mencegah atau menunda operasi toko tetap terlihat eksplisit untuk operator dan supervisor.

## Implementation Truth
1. `ReportingQueryFacade` mengagregasi data dari `KernelRepository`, `OutboxRepository`, `OperationalSalesPort`, dan `OperationalHardwarePort`.
2. Setiap issue dikategorikan dengan `OperationalIssueType`.
3. Model `OperationalIssue` dipakai konsisten untuk summary dan detail list.
4. Readback memuat `actor`, `timestamp`, `reasonCode`, dan `status` bila data memang tersedia.
5. Desktop reporting sekarang punya export bundle operasional (`daily-summary.csv`, `shift-summary.csv`, `operational-issues.csv`, `README.html`) sebagai evidence owner/supervisor yang tetap lahir dari snapshot lokal yang sama.

## Visibility Rules
- `sync.last_error_message` membuat status menjadi `ERROR` sampai ada `recordSyncSuccess()` yang membersihkan error terakhir.
- Latency > 1 hour adalah `STALLED`.
- Latency > 5 minutes adalah `DELAYED`.
- Approval `REQUESTED` menjadi `PENDING_APPROVAL`.
- Transaksi belum selesai saat close shift menjadi blocker `CRITICAL`.
- Shift yang masih open saat close day menjadi blocker `CRITICAL`.
- Hardware yang putus atau gagal dilaporkan sebagai `UNAVAILABLE`.
- Export tidak boleh menghitung ulang data dari source lain. Bundle hanya memotret state lokal yang sudah dibaca `ReportingQueryFacade`.
