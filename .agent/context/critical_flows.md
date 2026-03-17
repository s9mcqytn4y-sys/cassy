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

## Mandatory semantics

- user tidak boleh masuk catalog/cart tanpa access context valid
- cart tidak boleh berjalan tanpa business day dan shift aktif
- PIN validation, lockout baseline, dan capability gate harus hidup di shared boundary
- pricing, subtotal, tax, dan discount baseline harus konsisten lintas platform
- validitas settlement tidak boleh bergantung pada printer side effect
- history/readback/reprint harus mengambil dari snapshot struk final yang persisted
- mutasi stok checkout hanya boleh terjadi lewat `shared:inventory`
- UI hanya memantulkan state; invariant tidak boleh tinggal di screen

## Cross-platform stance

### Mandatory parity
- access capability semantics
- business day / shift transition semantics
- product lookup
- cart semantics
- pricing / totals baseline
- checkout finality contract
- receipt snapshot readback contract

### Allowed divergence
- keyboard vs touch ergonomics
- printer / scanner / permission / spooler UX
- platform packaging dan distribution
