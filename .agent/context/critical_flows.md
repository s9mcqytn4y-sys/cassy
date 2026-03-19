# Cassy Critical Flows

## Priority foundation

1. access gate
2. terminal/store bootstrap
3. open business day
4. start shift
5. catalog search / barcode fallback
6. cart mutation
7. pricing baseline
8. checkout finalization
9. receipt readback / reprint from persisted snapshot
10. cash in / cash out / safe drop
11. close shift reconciliation
12. close business day readiness review
13. inventory readback / count / discrepancy review / stock adjustment

## Mandatory semantics

- user tidak boleh masuk catalog/cart tanpa access context valid
- cart tidak boleh berjalan tanpa business day dan shift aktif
- dashboard harus menunjukkan blocker/readiness secara eksplisit sebelum operator masuk ke lane kasir
- opening cash di luar kebijakan harus ditahan oleh approval requirement yang jujur
- cash movement operasional harus terikat shift aktif, reason code valid, dan approval threshold yang durable
- close shift tidak boleh lolos bila masih ada transaksi pending
- close day harus fail-closed bila masih ada shift aktif atau approval operasional yang belum selesai
- PIN validation, lockout baseline, dan capability gate harus hidup di shared boundary
- pricing, subtotal, tax, dan discount baseline harus konsisten lintas platform
- validitas settlement tidak boleh bergantung pada printer side effect
- history/readback/reprint harus mengambil dari snapshot struk final yang persisted
- mutasi stok checkout hanya boleh terjadi lewat `shared:inventory`
- stock adjustment final harus reason-based dan approval-aware bila policy meminta
- count harus menghasilkan discrepancy dulu; tidak boleh auto-adjust silently
- UI hanya memantulkan state; invariant tidak boleh tinggal di screen

## Cross-platform stance

### Mandatory parity
- access capability semantics
- business day / shift transition semantics
- cash control / approval / close shift semantics
- product lookup
- cart semantics
- pricing / totals baseline
- checkout finality contract
- receipt snapshot readback contract

### Allowed divergence
- keyboard vs touch ergonomics
- printer / scanner / permission / spooler UX
- platform packaging dan distribution
