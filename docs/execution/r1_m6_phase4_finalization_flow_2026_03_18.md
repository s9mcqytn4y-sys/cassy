# R1 / M6 Phase 4 Finalization Flow

Dokumen ini merangkum perubahan PHASE 4 yang benar-benar hidup di repo.

## Scope yang ditutup

- facade `completeSale` sebagai jalur finalisasi utama
- `PaymentGatewayPort` + stub baseline
- callback handling untuk provider result
- replay guard untuk retry dan duplicate callback
- receipt snapshot dibangun dari transaksi persisted, bukan hanya basket in-memory
- hardware status desktop dibuat jujur melalui hardware port yang bisa di-fake saat test

## Perilaku final

### Complete-sale outcome

- `COMPLETED`: payment final `SUCCESS`, receipt snapshot dibuat, history/readback/reprint siap
- `PENDING`: payment belum final, sale tidak completed, tidak ada receipt final
- `REJECTED`: payment gagal/declined/cancelled, sale tidak completed

### Guardrail utama

- sale tidak boleh completed tanpa `PaymentState.SUCCESS`
- receipt final tidak dibuat untuk payment non-final
- duplicate callback tidak boleh menggandakan inventory, receipt, audit, atau outbox intent
- retry pada pending sale harus reuse pending session yang sama
- inventory mutation tetap lewat `shared:inventory`
- print/drawer/scanner tetap post-finalization concern, bukan bagian validity settlement

## Persisted source of truth

- `SaleItem` sekarang menyimpan `productName`
- snapshot receipt final dibangun dari sale + payment + sale items yang persisted
- `ReceiptSnapshot` tetap sumber final untuk readback/history/reprint

## Hardware integration stance tanpa device fisik

- desktop memakai `CashierHardwarePort`
- default runtime: no-op / honest status `UNKNOWN`, bukan fake `READY`
- test memakai fake hardware port untuk mensimulasikan warning drawer/printer/scanner
- checkout tetap final meskipun post-finalization hardware memberi warning

## Hal yang sengaja tidak dibohongi

- idempotency sudah diperkeras pada boundary sales/inventory/kernel
- tetapi atomicity lintas context belum dibuktikan sebagai satu transaksi ACID penuh
- karena itu claim yang benar adalah retry-safe/idempotent-aware, bukan distributed transaction proven
