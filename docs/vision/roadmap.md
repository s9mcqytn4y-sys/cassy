# Roadmap & Transition Plan [TARGET-STATE]

Dokumen ini memetakan rencana evolusi Cassy dari sistem POS lokal menjadi *retail operating core* yang komprehensif.

## Phase 1: Local-First Foundation (Current)
- **Fokus**: Stabilitas checkout di Windows Desktop.
- **Milestone**:
  - Implementasi penuh Ledger-based Inventory.
  - Stabilisasi `SyncReplayService` (Manual Retry).
  - Pemuatan native SQLite yang andal di Windows.
  - Basic Reporting (Shift & Daily Summary).

## Phase 2: Operational Maturity (Q3 2026)
- **Fokus**: Automasi dan skalabilitas outlet.
- **Milestone**:
  - **Automatic Sync**: Latar belakang sinkronisasi tanpa intervensi user.
  - **Conflict Management UI**: Resolusi perbedaan data lokal vs HQ secara visual.
  - **Device Management**: Remote locking dan update konfigurasi terminal dari HQ.
  - **Advanced Inventory**: Support Batch/Expiry (FIFO Layering).

## Phase 3: Android Parity & Mobility (Q4 2026)
- **Fokus**: Fitur lengkap di Android Lane.
- **Milestone**:
  - Integrasi Printer Bluetooth/Network yang lebih luas di Android.
  - Full Mobile Inventory (Stock Take via Camera Scanner).
  - Supervisor Approval via Android Mobile App.

## Phase 4: Expansion Ready (2027+)
- **Fokus**: Multi-outlet dan integrasi ekosistem.
- **Milestone**:
  - Inter-store stock transfer.
  - Konsolidasi laporan multi-toko di HQ.
  - Modul perluasan F&B dan Service (Prepared boundaries activated).

---
**Catatan**: Urutan prioritas dapat berubah berdasarkan realitas teknis dan kebutuhan operasional di lapangan.
