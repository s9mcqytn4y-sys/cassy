# Cassy Barcode Schema Final Implementable Baseline

## Document Overview
Prescriptive baseline untuk barcode sebagai bagian resmi master data produk/SKU dan scanner flow local-first.

## Purpose
Menetapkan model domain, database, lookup policy, collision handling, parser boundary, dan sync baseline untuk barcode.

## Scope
Product/ProductBarcode, scanner flow checkout/inventory, label printing dependency, collision policy, offline lookup, database/ERD baseline, dan testing.

## Key Decisions / Core Rules
Satu product dapat memiliki banyak barcode; lookup wajib ke local DB/cached master data; collision adalah data defect yang harus fail loudly.

## Detailed Content

### Normalized Source Body
Status
- Finalized berdasarkan kontrak yang Anda pilih:
- Q1-B, Q2-B, Q3-C, Q4-B, Q5-B, Q6-B, Q7-B, Q8-C, Q9-B, Q10-B, Q11-B, Q12-B, Q13-B, Q14-B, Q15-C
- Baseline ini diturunkan dari artefak Cassy secara traceable dari Use Case -> Activity -> Sequence -> Domain -> Architecture -> Database -> Test.

========================================
## 1. TUJUAN DAN BATAS SCOPE
========================================

Tujuan:
- Menjadikan barcode sebagai bagian resmi dari master data produk/SKU.
- Mendukung scan end-to-end dengan prinsip local-first.
- Mendukung barcode supplier dan barcode internal toko.
- Menjaga flow kasir tetap cepat, deterministik, dan aman saat offline.
- Menolak ambiguity/collision secara tegas.
- Menyediakan dasar implementasi untuk ERD, SQLDelight, use case, UI state, test, dan sync.

Boundary bisnis:
- Barcode dipakai untuk:
1) lookup produk saat checkout/inventory,
2) label printing,
3) operasional master data.
- Barcode bukan pengganti product identity utama.
- SKU dan barcode adalah dua key yang berbeda.
- Lookup wajib berjalan ke local DB/cached master data, bukan langsung ke server. UC-08 memang mensyaratkan scan/keyword SKU/nama, fallback manual saat scan gagal, dan exception saat produk tidak ditemukan atau master data belum tersedia.
- Arsitektur Cassy menegaskan local SQLite adalah immediate operational source of truth di device boundary.

========================================
## 2. KEPUTUSAN FINAL DESAIN
========================================

### 2.1 Model relasi
- Final: satu Product dapat memiliki banyak ProductBarcode.
- Jangan simpan hanya satu kolom `barcode` di tabel product.
- Alasannya:
- support barcode supplier,
- support barcode internal,
- support label printing,
- siap untuk barcode timbang di fase berikutnya,
- menghindari dead-end migrasi.

### 2.2 Posisi SKU
- SKU = identifier bisnis internal.
- Barcode = identifier scan/operasional.
- Keduanya sama-sama searchable.
- Artefak UC-08 eksplisit mensyaratkan pencarian minimal berdasarkan SKU, barcode, dan nama pendek.

### 2.3 Barcode internal toko
- Wajib didukung.
- Bentuknya salah satu `barcode_type`.
- Dipakai untuk item tanpa barcode supplier, item relabel, repack, atau label rak/harga.
- Label printing memang mengambil harga, nama, barcode, dan atribut label dari sistem.

### 2.4 Barcode timbang / variable barcode
- Belum menjadi shipping feature penuh phase-1.
- Tetapi boundary parser harus disiapkan sekarang.
- Implementasi sekarang: fixed barcode penuh + parser contract untuk future variable barcode.

### 2.5 Collision policy
- Collision bukan warning; ini data defect.
- Jika satu barcode memetakan ke lebih dari satu produk aktif yang valid, sistem harus fail loudly dan memblok scan result.
- Tidak boleh auto-pilih yang paling baru.
- Tidak boleh tampilkan pilihan produk di flow kasir normal.
- Ambiguity hanya boleh diselesaikan via master data correction.

### 2.6 Offline policy
- Lookup barcode harus membaca local master data cache.
- Jika master data belum tersedia atau belum sinkron, sistem tampilkan state error yang bisa ditindaklanjuti.
- Ini konsisten dengan local-first architecture dan traceability matrix untuk UC-08.

========================================
## 3. DOMAIN MODEL FINAL YANG BERSIH
========================================

### 3.1 Aggregate dan ownership
Bounded context:
- Master Data: Product, ProductBarcode, ProductSearchAlias, ProductLabelPreference
- Sales: ScanProductForSaleUseCase memakai master data lookup
- Inventory: ScanProductForInventoryUseCase memakai master data lookup
- Kernel: ReasonCode, AuditLog, idempotency metadata bila diperlukan
- Sync: snapshot refresh untuk product/barcode master data

### 3.2 Domain objects
A. Product (Aggregate Root)
- id: ProductId
- sku: Sku
- shortName: String?
- fullName: String
- sellPrice: Money
- status: ProductStatus
- sellable: Boolean
- trackStock: Boolean
- labelPreference: ProductLabelPreference?
- version: Long
- createdAt
- updatedAt
- deletedAt?

B. ProductBarcode (Entity di bawah Product / atau supporting entity master data dengan relasi kuat)
- id: ProductBarcodeId
- productId: ProductId
- barcodeValue: BarcodeValue
- barcodeType: BarcodeType
- status: BarcodeStatus
- source: BarcodeSource
- isPrimaryScanCode: Boolean
- isPrimaryLabelCode: Boolean
- storeScope: StoreRef?          // null untuk global/supplier, isi untuk barcode internal lokal
- generatedBySystem: Boolean
- generatedAt?
- activatedAt
- deactivatedAt?
- note?
- version: Long

C. BarcodeValue (Value Object)
- raw: String
- normalized: String
Aturan:
- hanya digit untuk barcode scanner klasik phase-1
- trim whitespace
- hilangkan separator non-digit jika scanner/device mengirim karakter tambahan
- panjang tervalidasi oleh barcode policy
- equality berbasis `normalized`

D. BarcodeType (Enum)
- EAN13
- UPCA
- INTERNAL_FIXED
- VARIABLE_WEIGHT_PREPARED
- UNKNOWN_EXTERNAL

E. BarcodeSource (Enum)
- SUPPLIER
- STORE_INTERNAL
- IMPORTED
- MIGRATED

F. BarcodeStatus (Enum)
- ACTIVE
- INACTIVE
- BLOCKED
- COLLIDED

G. ProductLabelPreference (Value Object)
- preferredLabelBarcodeId
- preferredTemplateCode?
- printableNameOverride?
- printablePriceMode?

H. ProductSearchAlias (opsional tapi disarankan)
- id
- productId
- aliasType: SKU | SHORT_NAME | ALT_KEYWORD
- aliasValueNormalized
- active

### 3.3 Invariant domain
- Product harus boleh punya 0..n barcode.
- Maksimal satu `isPrimaryScanCode = true` per scope produk + store scope aktif.
- Maksimal satu `isPrimaryLabelCode = true` per scope produk + store scope aktif.
- Barcode ACTIVE tidak boleh ambigu pada namespace yang sama.
- SKU tidak boleh dianggap barcode.
- ProductBarcode tidak boleh orphan; harus merefer ke Product yang aktif/terdefinisi.
- Barcode COLLIDED tidak boleh dipakai pada flow scan sampai diperbaiki.

========================================
## 4. DATABASE / ERD IMPLEMENTABLE BASELINE
========================================

### 4.1 Tabel inti

TABLE product (
id TEXT PRIMARY KEY,                       -- prefixed UUIDv7, mis. prd_...
sku TEXT NOT NULL,
short_name TEXT,
full_name TEXT NOT NULL,
sell_price_amount INTEGER NOT NULL,
sell_price_currency TEXT NOT NULL,
status TEXT NOT NULL,                     -- ACTIVE / INACTIVE / ARCHIVED
sellable INTEGER NOT NULL DEFAULT 1,
track_stock INTEGER NOT NULL DEFAULT 1,
version INTEGER NOT NULL DEFAULT 1,
created_at TEXT NOT NULL,
updated_at TEXT NOT NULL,
deleted_at TEXT
);

TABLE product_barcode (
id TEXT PRIMARY KEY,                      -- pbc_...
product_id TEXT NOT NULL,
barcode_value_raw TEXT NOT NULL,
barcode_value_normalized TEXT NOT NULL,
barcode_type TEXT NOT NULL,               -- EAN13 / UPCA / INTERNAL_FIXED / VARIABLE_WEIGHT_PREPARED / UNKNOWN_EXTERNAL
barcode_source TEXT NOT NULL,             -- SUPPLIER / STORE_INTERNAL / IMPORTED / MIGRATED
barcode_status TEXT NOT NULL,             -- ACTIVE / INACTIVE / BLOCKED / COLLIDED
store_id TEXT,                            -- null = global/supplier; non-null = internal barcode per store/tenant
is_primary_scan_code INTEGER NOT NULL DEFAULT 0,
is_primary_label_code INTEGER NOT NULL DEFAULT 0,
generated_by_system INTEGER NOT NULL DEFAULT 0,
generated_at TEXT,
activated_at TEXT NOT NULL,
deactivated_at TEXT,
note TEXT,
version INTEGER NOT NULL DEFAULT 1,
created_at TEXT NOT NULL,
updated_at TEXT NOT NULL,
deleted_at TEXT,
FOREIGN KEY(product_id) REFERENCES product(id)
);

TABLE product_search_alias (
id TEXT PRIMARY KEY,
product_id TEXT NOT NULL,
alias_type TEXT NOT NULL,                 -- SKU / SHORT_NAME / ALT_KEYWORD
alias_value_normalized TEXT NOT NULL,
active INTEGER NOT NULL DEFAULT 1,
created_at TEXT NOT NULL,
updated_at TEXT NOT NULL,
FOREIGN KEY(product_id) REFERENCES product(id)
);

TABLE barcode_scan_log (
id TEXT PRIMARY KEY,
scanned_value_raw TEXT NOT NULL,
scanned_value_normalized TEXT NOT NULL,
scan_context TEXT NOT NULL,               -- SALES / INVENTORY / LABEL / ADMIN
result_status TEXT NOT NULL,              -- FOUND / NOT_FOUND / COLLISION / INVALID / MASTERDATA_UNAVAILABLE
matched_product_id TEXT,
matched_barcode_id TEXT,
actor_id TEXT,
terminal_id TEXT,
business_day_id TEXT,
scanned_at TEXT NOT NULL,
detail_json TEXT
);

### 4.2 Constraint yang wajib
A. Uniqueness hybrid
- Untuk barcode supplier/global:
UNIQUE(barcode_value_normalized)
WHERE deleted_at IS NULL
AND barcode_status = 'ACTIVE'
AND store_id IS NULL

- Untuk barcode internal/store-scoped:
UNIQUE(store_id, barcode_value_normalized)
WHERE deleted_at IS NULL
AND barcode_status = 'ACTIVE'
AND store_id IS NOT NULL

B. Satu primary scan per scope
- UNIQUE(product_id, store_id, is_primary_scan_code)
dengan filter is_primary_scan_code = 1 dan status aktif

C. Satu primary label per scope
- UNIQUE(product_id, store_id, is_primary_label_code)
dengan filter is_primary_label_code = 1 dan status aktif

D. Search index
- INDEX product_barcode(barcode_value_normalized, barcode_status, store_id)
- INDEX product(sku)
- INDEX product(short_name)
- INDEX product_search_alias(alias_value_normalized, active)

### 4.3 Catatan desain
- Ini sengaja memisahkan current master product dari barcode mapping.
- Cocok dengan prinsip ERD v2 yang memisahkan current state dan explanation trail, dan mendorong model persistence implementable, bukan tabel serbaguna.
- Query generated SQLDelight harus tetap disembunyikan di repository/data source; UI tidak boleh akses query mentah.

========================================
## 5. FLOW SCANNER END-TO-END
========================================

### 5.1 Flow utama
Flow final:

Scanner/Keyboard Input
-> UI Scan Handler
-> Normalize Barcode Input
-> ProductLookupUseCase.findByScanInput()
-> MasterDataRepository.findProductByBarcodeOrSearchKey()
-> Local SQLite / SQLDelight
-> LookupResult
-> UI Response
-> downstream (cart / inventory / label)

### 5.2 Sequence implementable
1. User scan barcode atau input keyword.
2. UI kirim input ke `ProductLookupUseCase`.
## 3. Use case tentukan mode:
- jika input tampak barcode -> lookup barcode
- jika scan gagal / tidak terbaca -> fallback manual search
## 4. Repository query local DB:
- exact match ke `product_barcode.barcode_value_normalized`
- jika bukan barcode, cari SKU / short name / alias
## 5. Hasil:
- FOUND_SINGLE -> return ProductLookupSuccess
- NOT_FOUND -> return ProductLookupNotFound
- COLLISION -> return ProductLookupCollision
- MASTERDATA_UNAVAILABLE -> return ProductLookupUnavailable
- INVALID_INPUT -> return ProductLookupInvalid
6. UI render hasil.
## 7. Bila konteks SALES dan hasil FOUND_SINGLE:
- item masuk ke cart flow
- pricing recalculation tetap lewat pricing engine resmi
8. Log scan result ke `barcode_scan_log` secara ringan jika policy audit/observability mengaktifkannya.

Ini konsisten dengan sequence UC-08:
- UI -> Product Lookup Service
- Product Lookup Service -> Catalog Repository
- Catalog Repository -> Store DB query by barcode / SKU / short name
- hasil -> UI
- fallback manual bila scan gagal.
Juga konsisten dengan activity UC-08 yang memodelkan scan/keyword, search, tampilkan hasil, pilih item, fallback manual, dan exception not found/master data unavailable.

### 5.3 Contract application layer
sealed interface ProductLookupResult
- FoundSingle(
productId,
productSnapshot,
matchedBy,              // BARCODE / SKU / SHORT_NAME / ALIAS
matchedBarcodeId?,
matchedBarcodeType?
)
- NotFound(
normalizedInput,
reason                 // BARCODE_NOT_REGISTERED / PRODUCT_NOT_AVAILABLE
)
- Collision(
normalizedInput,
conflictType,          // GLOBAL_COLLISION / STORE_COLLISION / DATA_CORRUPTION
collisionRefsCount
)
- Unavailable(
normalizedInput,
reason                 // MASTERDATA_NOT_READY / LOCAL_DB_ERROR
)
- InvalidInput(
rawInput,
reason                 // EMPTY / ILLEGAL_CHARACTER / UNSUPPORTED_FORMAT
)

### 5.4 Contract repository
interface ProductLookupRepository {
suspend fun findByScanInput(
rawInput: String,
storeId: String?,
mode: LookupMode
): ProductLookupResult
}

LookupMode:
- SALES
- INVENTORY
- LABEL_PRINT
- ADMIN_MASTERDATA

========================================
## 6. BARCODE GENERATOR - MODEL FINAL
========================================

### 6.1 Scope generator
- Generator hanya untuk `STORE_INTERNAL`.
- Tidak dipakai untuk overwrite barcode supplier.
- Tidak dipakai otomatis saat transaksi.
- Dipakai saat:
- create product tanpa barcode supplier,
- tambah barcode internal,
- cetak label internal.

### 6.2 Format rekomendasi
Gunakan internal fixed barcode dengan format numeric scanner-friendly dan check digit.

Format final:
- Prefix internal: `29`
- Namespace store: 3 digit
- Product serial: 7 digit
- Check digit: 1 digit
- Total: 13 digit

Pattern:
29 SSS PPPPPPP C

Contoh:
29 101 0002457 8

Arti:
- 29 = namespace internal Cassy store barcode
- 101 = store namespace
- 0002457 = running serial produk/barcode internal
- 8 = check digit

Kenapa ini dipilih:
- tetap kompatibel dengan scanner barcode klasik
- mudah dicetak di label
- mudah dibedakan dari barcode supplier
- store namespace mengurangi collision
- ada check digit untuk mendeteksi salah input/scan noise

### 6.3 Aturan generator
Input:
- storeId / storeCode
- productId
- generatorSequence

Output:
- generatedBarcodeNormalized

Rules:
1. Hanya digit.
2. Panjang 13 digit.
3. Prefix internal tetap.
4. Store namespace deterministik.
5. Serial memakai sequence terkontrol, bukan random.
6. Check digit dihitung.
7. Setelah generate, wajib lewat collision check ke DB.
## 8. Jika bentrok:
- retry dengan serial berikutnya
- maksimum N retry
- jika tetap bentrok -> error operasional

### 6.4 Generator contract
interface InternalBarcodeGenerator {
suspend fun generate(
storeId: String,
productId: String
): GeneratedBarcode
}

data class GeneratedBarcode(
val normalizedValue: String,
val type: BarcodeType = INTERNAL_FIXED,
val source: BarcodeSource = STORE_INTERNAL,
val generatedAt: Instant
)

### 6.5 Anti-pattern yang dilarang
- random 13 digit tanpa namespace
- barcode = UUID string
- barcode = hash product name
- generate barcode otomatis saat scan gagal di kasir
- generate barcode tanpa simpan relasi ProductBarcode resmi

========================================
## 7. HANDLER EDGE CASE
========================================

### 7.1 Scan gagal terbaca
Kasus:
- scanner kirim input corrupt / partial / empty

Handling:
- classify as `InvalidInput` atau `ScanReadFailure`
- UI tampilkan:
- "Barcode tidak terbaca"
- aksi: scan ulang / cari manual
- jangan lanjut ke lookup exact jika input jelas corrupt

### 7.2 Produk tidak ditemukan
Kasus:
- barcode valid format, tapi tidak ada di master data lokal

Handling:
- `NotFound(BARCODE_NOT_REGISTERED)`
- log ringan ke `barcode_scan_log`
- UI:
- tampilkan state error jelas
- tombol: Cari manual
- tombol: Scan ulang
- opsional: Laporkan ke admin/master data

### 7.3 Master data belum tersedia
Kasus:
- local snapshot produk/barcode kosong/rusak/terlambat

Handling:
- `Unavailable(MASTERDATA_NOT_READY)`
- UI jangan tampilkan not found palsu
- bedakan antara "barcode tidak terdaftar" vs "data produk belum siap"

### 7.4 Produk inactive / tidak sellable
Kasus:
- barcode ketemu, tapi produk archived/non-sellable

Handling:
- hasil lookup bisa ketemu, tapi use case SALES menolak sebagai item jual
- UI: "Produk ditemukan tetapi tidak dapat dijual"
- untuk INVENTORY/ADMIN mode masih bisa ditampilkan jika policy mengizinkan

### 7.5 Duplicate active barcode
Kasus:
- satu barcode aktif mengarah ke >1 product

Handling:
- `Collision`
- block flow
- audit/log anomaly
- jangan pilih salah satu
- butuh data correction

### 7.6 Barcode internal generator bentrok
Kasus:
- generated code sudah dipakai

Handling:
- retry next serial
- setelah max retry -> fail hard
- operator/admin diberi error operasional, bukan silent fallback

### 7.7 Future variable barcode detected
Kasus:
- prefix cocok ke pattern timbang masa depan

Handling phase-1:
- return `InvalidInput(UNSUPPORTED_FORMAT)` atau `NotSupportedYet`
- jangan parse setengah-setengah
- siapkan parser interface terpisah untuk future rollout

========================================
## 8. BARCODE COLLISION POLICY
========================================

Definisi collision:
- Dua atau lebih ProductBarcode ACTIVE dalam namespace yang seharusnya unik memakai `barcode_value_normalized` yang sama.

Jenis:
A. Global collision
- barcode supplier/global bentrok lintas product

B. Store collision
- barcode internal pada store yang sama bentrok

C. Cross-scope legal coexistence
- barcode internal store A sama dengan barcode internal store B
- ini legal jika uniqueness memang store-scoped

Policy final:
1. Collision dicegah di DB constraint.
## 2. Jika lolos karena data legacy/migrasi/import:
- tandai row sebagai `COLLIDED` atau blok aktivasi
- scan result harus `Collision`
3. Tidak boleh ada automatic winner selection.
4. Collision wajib diperbaiki di master data, bukan di flow kasir.
## 5. Jika barcode supplier bentrok:
- treat as critical master data error

========================================
## 9. OFFLINE SCAN POLICY
========================================

### 9.1 Prinsip
- Lookup barcode wajib local-first.
- Query ke HQ bukan bagian dari happy path scan.
- Sequence/traceability sudah mengunci product lookup ke katalog lokal/cached.
- Architecture juga mengunci local DB sebagai operational store, bukan cache pasif.

### 9.2 Source of truth scan
- Local SQLite / SQLDelight master data cache:
- product
- product_barcode
- product_search_alias
- price policy/snapshot yang relevan

### 9.3 Mode offline
Allowed:
- scan barcode ke product yang sudah ada di local snapshot
- fallback manual search by SKU/nama
- add to cart / inventory flow sejauh policy mengizinkan

Blocked:
- resolve barcode ke HQ live
- create product baru dari flow scan
- generate barcode otomatis di flow scan kasir

### 9.4 Failure state offline
- MASTERDATA_NOT_READY
- STALE_MASTERDATA_WARNING (opsional)
- LOCAL_DB_ERROR

========================================
## 10. FEEDBACK UI - BARCODE TIDAK DITEMUKAN
========================================

### 10.1 State UI final
sealed interface BarcodeScanUiState
- Idle
- LookingUp(input)
- Found(productSummary)
- NotFound(
input,
title,
message,
primaryAction,
secondaryAction
)
- Collision(
input,
title,
message
)
- Invalid(
input,
title,
message
)
- Unavailable(
input,
title,
message
)

### 10.2 UX rule
Untuk `NotFound`, UI minimum harus:
- tampilkan pesan jelas
- bedakan dari scanner error
- sediakan aksi lanjut

Rekomendasi copy:
Title:
- "Barcode tidak ditemukan"

Message:
- "Produk dengan barcode ini belum terdaftar pada data toko."

Primary action:
- "Cari manual"

Secondary action:
- "Scan ulang"

Optional tertiary:
- "Tutup"

### 10.3 UX rule untuk collision
Title:
- "Barcode bermasalah"

Message:
- "Barcode ini terdeteksi ganda pada master data dan tidak bisa dipakai saat ini."

Action:
- "Tutup"
- opsional admin-only: "Laporkan masalah"

### 10.4 UX rule untuk master data unavailable
Title:
- "Data produk belum siap"

Message:
- "Master data lokal belum tersedia atau belum sinkron. Coba lagi setelah sinkronisasi."

### 10.5 UX anti-pattern
- jangan silent fail
- jangan toast-only untuk not found
- jangan auto-buka create product flow dari kasir
- jangan tampilkan hasil acak saat collision

========================================
## 11. LABEL PRINTING RULE
========================================

Karena use case label printing mengambil harga, nama, barcode, dan atribut label, maka rule finalnya:

1. Product boleh punya banyak barcode.
2. Label printing harus memilih `isPrimaryLabelCode = true`.
## 3. Jika tidak ada:
- fallback ke `isPrimaryScanCode = true`
## 4. Jika tetap tidak ada:
- label job ditolak dengan error master data
5. Label harus memakai harga aktif toko yang sinkron. Ini juga ditegaskan di activity cetak label.

========================================
## 12. SQLDELIGHT QUERY BASELINE
========================================

### 12.1 Lookup by barcode
SELECT
p.id,
p.sku,
p.short_name,
p.full_name,
p.sell_price_amount,
p.sell_price_currency,
pb.id AS barcode_id,
pb.barcode_type
FROM product_barcode pb
JOIN product p ON p.id = pb.product_id
WHERE pb.barcode_value_normalized = :normalizedInput
AND pb.barcode_status = 'ACTIVE'
AND pb.deleted_at IS NULL
AND p.deleted_at IS NULL
AND p.status = 'ACTIVE'
AND (
(pb.store_id IS NULL)
OR (pb.store_id = :storeId)
)
## Order By
CASE WHEN pb.store_id = :storeId THEN 0 ELSE 1 END,
pb.is_primary_scan_code DESC,
pb.created_at DESC;

### 12.2 Search fallback by sku / short name / alias
SELECT ...
WHERE
p.sku = :normalizedInput
OR LOWER(p.short_name) LIKE :keyword
## Or Exists (
## Select 1
FROM product_search_alias a
WHERE a.product_id = p.id
AND a.active = 1
AND a.alias_value_normalized LIKE :keyword
)

### 12.3 Collision detection
SELECT COUNT(*)
FROM product_barcode pb
WHERE pb.barcode_value_normalized = :normalizedInput
AND pb.barcode_status = 'ACTIVE'
AND pb.deleted_at IS NULL
AND (
(pb.store_id IS NULL)
OR (pb.store_id = :storeId)
);

Interpretasi:
- 0 = not found
- 1 = found single
- >1 = collision

========================================
## 13. IMPLEMENTATION CONTRACT PER LAYER
========================================

UI layer
- menerima scan input
- memanggil use case
- render state
- tidak melakukan query DB langsung
- tidak melakukan pricing sendiri

Application layer
- normalize input
- klasifikasi mode lookup
- orchestration repository
- mapping result ke UI contract
- logging ringan/observability bila perlu

Domain layer
- BarcodeValue validation
- Barcode uniqueness semantics
- ProductBarcode invariants
- generator policy
- collision policy

Data layer
- SQLDelight queries
- row -> domain mapping
- namespace uniqueness policy
- local DB read/write
- migration handling

Ini konsisten dengan aturan arsitektur bahwa cross-context access harus via application contracts / explicit refs, dan data layer adalah satu-satunya tempat mapping SQLDelight rows.

========================================
## 14. TEST BASELINE WAJIB
========================================

### 14.1 Common/domain tests
- BarcodeValue_NormalizationTest
- ProductBarcode_PrimaryScanUniquenessTest
- ProductBarcode_PrimaryLabelUniquenessTest
- InternalBarcodeGenerator_CheckDigitTest
- InternalBarcodeGenerator_RetryOnCollisionTest
- BarcodeCollisionPolicy_FailLoudTest
- ProductLookupMode_ClassifyInputTest

### 14.2 Repository/component tests
- FindProductByBarcode_FoundSingleTest
- FindProductByBarcode_NotFoundTest
- FindProductByBarcode_CollisionTest
- FindProductBySkuFallbackTest
- FindProductByShortNameFallbackTest
- Lookup_StoreScopedInternalBarcodeWinsOverGlobalTest
- Lookup_InactiveBarcodeIgnoredTest
- Lookup_InactiveProductRejectedForSalesTest
- Lookup_MasterDataUnavailableTest
- ProductBarcode_MigrationReplayTest
- FK_Verification_ProductBarcodeRequiresProductTest

### 14.3 Integration / platform adapter tests
- ScannerInputNormalizationTest
- BarcodeScanUi_NotFoundActionTest
- BarcodeScanUi_CollisionMessageTest
- LabelPrinting_UsesPrimaryLabelBarcodeTest

### 14.4 Device/selective tests
- scanner input normalization
- scanner lifecycle interruption
- printed label / receipt barcode rendering subset

Ini selaras dengan baseline automation yang memang mewajibkan scanner/barcode normalization, SQLDelight migration replay, FK verification, dan selective device fidelity saja.

========================================
## 15. ROLL-OUT PLAN IMPLEMENTABLE
========================================

Step 1
- Tambah tabel `product_barcode`
- Tambah tabel `product_search_alias`
- Tambah query lookup by barcode / SKU / short name
- Tambah migration SQLDelight

Step 2
- Migrasi data existing:
- jika product lama punya kolom barcode tunggal, pindahkan ke `product_barcode`
- tandai sebagai `barcode_source = MIGRATED`
- default `is_primary_scan_code = 1`
- default `is_primary_label_code = 1`

Step 3
- Implement `ProductLookupUseCase`
- Implement `InternalBarcodeGenerator`
- Implement `BarcodeNormalizer`

Step 4
- Adapt UI POS / Inventory:
- found
- not found
- collision
- unavailable

Step 5
- Adapt label printing ke `preferred label barcode`

Step 6
- Tambahkan test + CI migration verification
- Semua perubahan SQLDelight wajib hadir dengan migration file dan diverifikasi di CI. Ini sudah menjadi aturan arsitektur dan automation baseline Cassy.

========================================
## 16. KEPUTUSAN TEGAS YANG TIDAK BOLEH DILANGGAR
========================================

1. SKU bukan barcode.
2. Barcode lookup wajib ke local DB.
3. Product tidak boleh hanya punya satu field barcode tunggal sebagai model final.
4. Collision harus block flow, bukan dipilihkan otomatis.
5. Not found harus punya UI feedback eksplisit + fallback manual search.
6. Internal barcode harus namespace-based + check digit + retry-on-collision.
7. Label printing harus memilih barcode aktif yang eksplisit.
8. UI tidak boleh query SQLDelight langsung.
9. Semua perubahan barcode persistence harus ikut migration + test + CI verification pada PR yang sama.
10. Future variable barcode disiapkan di boundary, tetapi belum diaktifkan penuh phase-1.

## Ringkasan Final
- Final model: Product + ProductBarcode + ProductSearchAlias
- Final scan path: scanner input -> normalize -> ProductLookupUseCase -> local DB -> result -> UI
- Final offline rule: local-first, no HQ lookup in happy path
- Final collision rule: fail loud
- Final UI rule: explicit not found / collision / unavailable states
- Final generator rule: internal numeric prefix-based 13-digit barcode dengan store namespace + check digit
- Final implementation stance: production-grade, migration-ready, SQLDelight-friendly, dan traceable ke artefak Cassy


## Constraints / Policies
Barcode bukan pengganti product identity utama; variable barcode future-ready tetapi belum shipping feature penuh phase 1.

## Technical Notes
Dokumen ini bersifat implementable baseline dan perlu disejajarkan dengan master data, printing, ERD, serta UC-08.

## Dependencies / Related Documents
- `store_pos_use_case_detail_specifications.md`
- `store_pos_sequence_detail_specifications.md`
- `store_pos_erd_specification_v2.md`
- `cassy_architecture_specification_v1.md`
- `cassy_printing_mechanism_scheme.md`
- `store_pos_test_specification.md`

## Risks / Gaps / Ambiguities
- Tidak ditemukan gap fatal saat ekstraksi. Tetap review ulang bagian tabel/angka jika dokumen ini akan dijadikan baseline implementasi final.

## Reviewer Notes
- Struktur telah dinormalisasi ke Markdown yang konsisten dan siap dipakai untuk design review / engineering handoff.
- Istilah utama dipertahankan sedekat mungkin dengan dokumen sumber untuk menjaga traceability lintas artefak.
- Bila ada konflik antar dokumen, prioritaskan source of truth, artefak yang paling preskriptif, dan baseline yang paling implementable.

## Source Mapping
- Original source: `SKEMA-BARCODE-CASSY-—-FINAL-IMPLEMENTABLE-BASELINE.txt` (TXT, 864 lines)
- Output markdown: `cassy_barcode_schema_final_implementable_baseline.md`
- Conversion approach: text extraction -> normalization -> structural cleanup -> standardized documentation wrapper.
- Note: marker sitasi/annotation internal non-substantif dibersihkan agar hasil markdown lebih implementable.
