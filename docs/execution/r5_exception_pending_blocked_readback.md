# R5 Exception, Pending & Blocked Readback

## Readback Strategy
Operational exception dan pending state tidak boleh diam. Setiap non-finalized state harus menjelaskan apa yang terjadi, siapa yang terlibat, kapan mulai terjadi, dan kenapa operator harus peduli.

## Model Requirements
Model `OperationalIssue` adalah carrier utama untuk readback ini:
- **What**: `label` dan `description`
- **Who**: `actor`
- **When**: `timestamp`
- **Why**: `reasonCode` dan `status`

## Truthful Readback Scenarios

### 1. Pending Approvals
- **Status**: `REQUESTED`
- **Readback**: "Permintaan oleh [Requester] memerlukan tindakan supervisor."
- **Metadata**: requester dan waktu request.

### 2. Shift Variance
- **Status**: `COMPLETED_WITH_VARIANCE`
- **Readback**: "Ditemukan selisih kas sebesar Rp [X]."
- **Metadata**: operator dan waktu penutupan.

### 3. Sync Latency
- **Status**: `DELAYED` atau `STALLED`
- **Readback**: "Ada [N] data yang belum tersinkronisasi."
- **Metadata**: timestamp pending tertua.

### 4. Sync Failure
- **Status**: `ERROR`
- **Readback**: pesan terakhir dari `sync.last_error_message`.
- **Metadata**: last error, jumlah pending event, oldest pending, dan last success timestamp bila ada.

### 5. Hardware Failures
- **Status**: `OFFLINE` atau `UNAVAILABLE`
- **Readback**: pesan spesifik dari hardware port.

## UI Implementation
Desktop reporting summary, panel shift relevan, dan daftar issue adalah permukaan utama readback saat ini. User tidak perlu menebak kenapa state dianggap blocked atau pending.

## Export Readback Contract
- Export bundle desktop menyalin readback yang sama ke file CSV/HTML.
- `daily-summary.csv` adalah snapshot harian.
- `shift-summary.csv` adalah shift aktif atau shift terbaru yang relevan untuk business day.
- `operational-issues.csv` mempertahankan severity, type, status, actor, timestamp, reason code, dan source id.
- `README.html` adalah overview owner-friendly, bukan sumber truth baru.
