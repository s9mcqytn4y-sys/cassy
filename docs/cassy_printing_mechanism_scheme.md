# Cassy Printing Mechanism Scheme

## Document Overview
Prescriptive printing baseline untuk thermal receipt POS, invoice A4/PDF, reprint, dan printer orchestration.

## Purpose
Menetapkan domain model, routing, printer capability, numbering, preview/print state, dan reprint policy.

## Scope
Receipt thermal 80mm/58mm, invoice A4/PDF, preview, print status, reprint, device adapter, resolver, numbering, dan audit minimal.

## Key Decisions / Core Rules
Printing adalah business output, bukan concern UI murni; printer routing diprioritaskan LAN/TCP-IP lalu USB lalu Bluetooth fallback terbatas; reprint harus membaca final snapshot.

## Detailed Content

### Normalized Source Body
Phase-1 scope:
- thermal receipt POS
- invoice A4 / PDF
- reprint document
- custom recipt untuk personalisasi bisnis
- future-ready hook untuk price label resolver, tetapi bukan hardware-first deliverable phase-1

==================================================
## 1. KEPUTUSAN FINAL DESAIN
==================================================

### 1.1 Prinsip utama
- Printing bukan concern UI murni.
- Printing adalah business output yang harus traceable ke transaksi/dokumen final.
- Preview, print execution, print status, reprint, dan print failure harus punya state yang eksplisit.
- Driver/koneksi printer tidak boleh bocor ke domain/application.
- Shared layer hanya mengelola contract, template rendering intent, resolver, status model, dan print job orchestration.
- Platform adapter Android/Desktop yang menangani discovery, pairing, socket/spooler, vendor SDK, dan lifecycle device.

### 1.2 Keputusan koneksi phase-1
Urutan prioritas:
## 1. LAN / TCP-IP
## 2. USB
## 3. Bluetooth Classic sebagai fallback terbatas

Alasan:
- Android + Desktop paling stabil jika printer jaringan atau USB.
- LAN memudahkan satu pola routing untuk POS Android dan desktop backoffice.
- USB bagus untuk single terminal fixed.
- Bluetooth jangan dijadikan baseline utama receipt checkout karena pairing, reconnect, power state, dan ownership printer lebih rapuh.

### 1.3 Keputusan vendor phase-1
Baseline compatibility target:
- Epson POS receipt printer class
- Star Micronics POS receipt printer class

Jangan baseline ke "generic Bluetooth thermal printer" sebagai kontrak resmi.
Support generic boleh ada di adapter experimental, tetapi bukan golden path.
Kalau hardware acak dijadikan baseline, nanti observability, status, dan troubleshooting jadi palsu.

### 1.4 Keputusan output document
- Receipt thermal: 80mm sebagai default; 58mm sebagai compact fallback.
- Invoice: A4 portrait + PDF export/share.
- Reprint:
- receipt thermal reprint dari dokumen receipt yang sudah tersimpan
- invoice PDF/A4 reprint dari dokumen final yang sudah tersimpan
- Preview wajib untuk semua output sesuai keputusan user.

### 1.5 Keputusan numbering
Gunakan business-readable number, bukan surrogate ID.

Format rekomendasi:
- ReceiptNumber:
RC-{StoreCode}-{BusinessDateYYYYMMDD}-{TerminalCode}-{Sequence}
- InvoiceNumber:
INV-{StoreCode}-{BusinessDateYYYYMMDD}-{Sequence}

Aturan:
- number dibuat lokal di terminal/store scope agar offline-safe
- uniqueness minimal per store + business day + document type + terminal scope
- jangan menunggu HQ untuk generate nomor dokumen operasional
- HQ hanya boleh menjadi authority rekonsiliasi/replication, bukan dependency hard untuk issuance print lokal

### 1.6 Keputusan source data print
- Receipt dan invoice dibangun dari local finalized transaction/document snapshot.
- Reprint membaca dokumen/artifact lokal yang sudah persisted.
- Label resolver future:
- local-first dari master data snapshot toko
- harga aktif toko
- barcode label preference
- template lokal/per-store
- HO/HQ bukan happy path dependency untuk print.
- Untuk free plan tanpa HO:
- semua print tetap jalan dari local store snapshot
- jika snapshot harga/master data belum siap, print label ditolak dengan error operasional yang jelas, bukan fallback ke data sembarang

==================================================
## 2. DOMAIN MODEL PRINT FINAL
==================================================

### 2.1 Aggregate / entity inti
A. PrintDocument
- documentId
- documentType = RECEIPT | INVOICE | LABEL | REPORT
- businessRefType = SALE | RETURN | REPORT | INVENTORY_LABEL
- businessRefId
- readableNumber
- templateId
- templateVersion
- payloadHash
- renderedAt
- renderedBy
- storeId
- terminalId
- businessDayId
- status = RENDERED | PREVIEWED | QUEUED | PRINTING | PRINTED | FAILED | CANCELLED

B. PrintJob
- printJobId
- documentId
- targetPrinterId nullable
- requestedBy
- requestedAt
- channel = THERMAL | PDF | SYSTEM_PRINTER | EXPORT_FILE
- copyCount
- state = QUEUED | DISPATCHING | WAITING_DEVICE | PRINTING | SUCCESS | FAILED | RETRY_WAIT | CANCELLED
- errorCode nullable
- errorMessage nullable
- correlationId
- idempotencyKey

C. PrinterDevice
- printerId
- printerKind = RECEIPT | INVOICE | LABEL | HYBRID
- connectionType = LAN | USB | BLUETOOTH
- vendor = EPSON | STAR | GENERIC_ESC_POS | SYSTEM_PDF
- displayName
- addressOrIdentifier
- capabilityProfileId
- isEnabled
- assignedScopes

D. PrinterCapabilityProfile
- supportsReceipt80mm
- supportsReceipt58mm
- supportsPdfDirect
- supportsQr
- supportsBarcodeSubset
- supportsCut
- supportsDrawerKick
- supportsStatusReadback
- supportsPreviewValidation
- maxColumns
- codepageProfile
- dpiProfile

E. PrinterStatusSnapshot
- printerId
- observedAt
- connectivity = CONNECTED | DISCONNECTED | UNKNOWN
- health = READY | BUSY | PAPER_NEAR_END | PAPER_OUT | COVER_OPEN | OVERHEAT | LOW_BATTERY | ERROR_UNKNOWN
- lastSuccessAt
- lastFailureAt
- pendingJobCount
- rawStatus optional

F. ReprintRecord
- reprintId
- documentId
- reasonCode
- requestedBy
- approvedBy nullable
- requestedAt
- copyCount
- result

### 2.2 Audit minimal
Setiap print/reprint/failure wajib menyimpan:
- actorId
- role
- storeId
- terminalId
- documentType
- businessRefType
- businessRefId
- readableNumber
- printerId
- printJobId
- action = PREVIEW | PRINT | REPRINT | EXPORT_PDF | CANCEL | RETRY
- result = SUCCESS | FAIL
- correlationId

==================================================
## 3. ARSITEKTUR DAN BOUNDARY
==================================================

### 3.1 Layering
UI Layer
- receipt preview screen
- invoice preview screen
- print dialog
- printer status panel
- print history / retry action
- reprint confirmation

Application Layer
- BuildPrintDocumentUseCase
- PreviewPrintDocumentUseCase
- SubmitPrintJobUseCase
- RetryPrintJobUseCase
- CancelPrintJobUseCase
- ReprintDocumentUseCase
- ResolvePrinterUseCase
- ObservePrinterStatusUseCase

Domain Layer
- PrintPolicy
- PrinterRoutePolicy
- ReprintPolicy
- CapabilityCompatibilityPolicy
- PreviewRequirementPolicy
- DocumentNumberPolicy

Data / Integration Layer
- PrintDocumentRepository
- PrintJobRepository
- PrinterRegistryRepository
- PrinterStatusDataSource
- ReceiptPrinterAdapter
- InvoicePrinterAdapter
- PdfRenderer
- PlatformSpoolerAdapter
- EscPosCommandEncoder
- VendorSdkAdapter

Platform Device Adapter
Android:
- LAN socket printer adapter
- USB printer adapter
- Bluetooth adapter fallback
- vendor SDK wrappers
- native pairing/discovery
Desktop:
- LAN socket printer adapter
- OS spooler / Windows printer adapter
- vendor SDK wrappers
- PDF export/open

### 3.2 Rule keras
- UI tidak boleh generate ESC/POS command.
- UI tidak boleh query printer device langsung tanpa status facade.
- Shared layer tidak boleh tahu detail pairing Android atau spooler Windows.
- Print document harus immutable per revision render.
- Reprint selalu membaca snapshot dokumen final, bukan rebuild liar dari data yang mungkin sudah berubah.

==================================================
## 4. PRINT RESOLVER FINAL
==================================================

### 4.1 Tujuan resolver
Menentukan:
- dokumen apa yang akan dirender
- template apa yang dipakai
- printer mana yang dipilih
- channel apa yang cocok
- apakah preview valid
- apakah device compatible
- fallback apa yang diizinkan

### 4.2 Resolver input
PrintRequest
- documentType
- businessRefType
- businessRefId
- requestedChannel
- targetPrinterHint nullable
- paperWidthHint nullable
- requiresPreview = true
- copyCount
- actorContext
- terminalContext
- storeContext

### 4.3 Resolver output
ResolvedPrintPlan
- printDocument
- previewModel
- selectedPrinter nullable
- selectedChannel
- rendererType
- commandProfile
- capabilityCheckResult
- fallbackOptions
- warnings

### 4.4 Routing policy
A. Receipt thermal
- resolve ke printer RECEIPT paling dekat / assigned ke terminal
- default channel THERMAL
- jika printer incompatible:
- fallback preview tetap tampil
- user bisa pilih printer lain
- atau export PDF kecil jika policy mengizinkan
- jangan auto-fallback ke device random

B. Invoice
- default ke PDF render preview
- print via desktop A4/system printer atau export PDF
- di Android POS, invoice lebih aman sebagai PDF/share daripada paksa direct print A4 kecuali memang ada printer A4 terkonfigurasi

C. Reprint
- baca document snapshot existing
- tampilkan badge REPRINT
- policy copy/original wording harus eksplisit
- reason code bisa diwajibkan untuk dokumen sensitif

### 4.5 Multi-printer routing recommendation
Rekomendasi phase-1:
- satu default receipt printer per terminal
- satu optional backup receipt printer per store zone
- invoice default ke PDF/system printer
- jangan dulu bikin dynamic printer marketplace di UI
- cukup:
- Assigned printer
- Backup printer
- Manual select jika failure

==================================================
## 5. TEMPLATE DAN FORMAT DOKUMEN
==================================================

### 5.1 Receipt thermal 80mm default
Struktur profesional:
- logo/store name
- alamat toko / kontak
- business day + timestamp
- receipt number
- cashier / terminal
- sale type
- item rows:
qty x short name
unit price
line total
- subtotal
- discount/promo summary
- tax summary
- grand total
- payment method breakdown
- change / outstanding
- footer policy:
return policy singkat
loyalty / promo message opsional
- QR / barcode receipt reference opsional

Aturan layout:
- receipt renderer harus deterministic
- long item names truncate / wrap konsisten
- angka rata kanan
- jangan campur formatting business dengan raw string concat liar

### 5.2 Receipt 58mm fallback
- lebih ringkas
- hide non-essential footer
- line wrapping lebih agresif
- QR hanya jika printer dan paper cukup
- jangan memakai template 80mm yang dipaksa menyusut

### 5.3 Invoice A4 / PDF
Struktur profesional:
- brand/store header
- invoice title
- invoice number
- issue date
- customer info opsional
- document reference ke sale/return
- item table:
no
item
qty
unit price
discount
tax
line total
- subtotal
- tax total
- net total
- payment summary
- notes / legal footer
- signature block opsional
- QR verification/reference opsional

Rekomendasi:
- invoice renderer berbasis document model yang sama dengan PDF/A4 preview
- jangan gunakan thermal formatter untuk invoice

### 5.4 Preview wajib
Karena keputusan user: semua output wajib preview.

Aturan UX:
- checkout selesai -> langsung buka receipt preview screen lightweight
- receipt preview harus render cepat dari local finalized transaction snapshot
- jangan blocking lama karena menunggu printer discovery
- printer status tampil paralel di preview
- tombol utama:
Print
Ganti Printer
Simpan PDF / Share jika policy mengizinkan
Cancel
- untuk receipt cepat:
preview harus lightweight dan "print-ready", bukan editor dokumen

Koreksi tegas:
- preview wajib boleh, tetapi jangan jadikan preview sebagai tempat edit business data.
- preview hanya untuk verifikasi visual dan pemilihan output.
- kalau preview merubah data transaksi, desainnya rusak.

==================================================
## 6. UX PRINT YANG MODERN DAN OPERASIONAL
==================================================

### 6.1 Checkout receipt flow
## 1. Payment success / transaction finalized
## 2. Build receipt snapshot
## 3. Buka receipt preview instan
## 4. Secara paralel:
- resolve assigned printer
- cek status printer
## 5. UI menampilkan:
- preview receipt
- nama printer aktif
- status printer
- warning jika incompatible / offline
## 6. User tekan Print
## 7. Job masuk queue + status berubah
## 8. UI menampilkan progress singkat
## 9. Hasil:
- success: badge "Printed"
- fail: state "Print failed" + retry + switch printer + save/share fallback

### 6.2 Status printer wajib terlihat
Status minimum yang wajib user tahu:
- Connected / Disconnected / Unknown
- Ready
- Busy
- Paper out
- Cover open
- Error unknown
- Last success time
- Pending queue count

Status opsional tergantung capability printer:
- Paper near end
- Low battery
- Overheat
- Drawer status

Rule penting:
- jangan bohong ke user.
- kalau printer tidak mendukung readback status, tampilkan "status terbatas / unknown", bukan fake READY.

### 6.3 Troubleshooting UX
Saat abnormal:
- tampilkan error yang action-oriented
- contoh:
- Printer tidak terhubung
- Kertas habis
- Cover printer terbuka
- Printer sibuk
- Printer tidak kompatibel dengan template/paper width
- Print timeout
- tombol yang relevan:
- Retry
- Pilih printer lain
- Lihat preview lagi
- Simpan PDF / bagikan
- Tandai print pending
- hindari:
- toast-only
- silent fail
- message teknis socket exception mentah
- forcing cashier restart app

### 6.4 Print pending policy
Receipt print failure tidak boleh membatalkan transaksi final yang sudah sah.
Yang benar:
- transaction tetap final bila payment dan sale final sah
- print status tercatat failed/pending
- user bisa retry/reprint
- audit failure tersimpan

### 6.5 Reprint UX
- cari dokumen via receipt/invoice lookup
- tampilkan preview snapshot dokumen existing
- beri badge:
## Reprint
- minta reason jika policy mengharuskan
- tampilkan histori print sebelumnya bila perlu
- jangan rebuild dari data sale yang sudah berubah

==================================================
## 7. HARDWARE INTEGRATION STRATEGY
==================================================

### 7.1 Adapter strategy
Gunakan capability-based adapter, bukan if-else vendor berantakan.

Contract:
PrinterAdapter
- discover()
- connect()
- disconnect()
- getStatus()
- validateCapability(printPlan)
- print(renderedPayload)
- cancel(jobId)
- selfTest() optional

Implementasi:
- LanEscPosPrinterAdapter
- UsbEscPosPrinterAdapter
- EpsonSdkPrinterAdapter
- StarSdkPrinterAdapter
- DesktopSystemPrinterAdapter
- PdfOnlyPrinterAdapter

### 7.2 Renderer split
- ThermalReceiptRenderer
- PdfInvoiceRenderer
- FutureLabelRenderer

Output thermal:
- intermediate command model
- lalu di-encode oleh ESC/POS encoder atau vendor command builder

Jangan:
- langsung bikin string raw ESC/POS di UI
- satu renderer untuk semua jenis output

### 7.3 Capability profile
Contoh:
- printer A: 80mm, cut, QR, status readback
- printer B: 58mm, no QR, no status readback
- system printer: A4 PDF only

Resolver wajib fail fast jika:
- template butuh 80mm tapi printer 58mm only tanpa fallback layout
- butuh cut/drawer tapi device tidak support dan policy mewajibkan
- invoice diarahkan ke thermal receipt printer

==================================================
## 8. LABEL PRICE RESOLVER (FUTURE-READY)
==================================================

Walau bukan phase-1 hardware deliverable utama, resolver label harus disiapkan sekarang agar nanti tidak redesign besar.

### 8.1 Source of truth
- local store master data snapshot
- active price per store
- product name
- preferred label barcode
- label attribute/template
- optional promo flag

### 8.2 Rule final
- gunakan isPrimaryLabelCode
- jika tidak ada, fallback ke isPrimaryScanCode
- jika tetap tidak ada, label job ditolak
- harga harus harga aktif toko
- jika data harga belum sinkron, jangan print label
- free plan tanpa HO tetap bisa print dari local snapshot toko
- HO hanya sumber sinkronisasi/master update, bukan dependency runtime wajib

### 8.3 Label preview
- wajib preview
- preview harus memvalidasi:
- barcode ada
- harga aktif ada
- template cocok dengan printer/profile
- ukuran label cocok

==================================================
## 9. OFFLINE POLICY FINAL
==================================================

### 9.1 Receipt
- boleh preview dan print dari local finalized transaction
- printer status lokal tetap bisa dibaca jika device support
- print job yang gagal tetap dicatat dan bisa diretry
- tidak bergantung pada HQ

### 9.2 Invoice
- preview PDF dari local snapshot
- print/export tergantung device/printer yang tersedia
- jika tidak ada printer A4, user tetap bisa simpan/share PDF

### 9.3 Reprint
- hanya untuk dokumen yang snapshot/final artifact-nya tersedia lokal
- jika artifact belum tersedia lokal, tampilkan state unavailable yang jujur
- jangan rebuild diam-diam dari partial data

==================================================
## 10. TROUBLESHOOTING DECISION TREE
==================================================

### 10.1 Sebelum print
- Apakah preview valid?
- Apakah printer dipilih?
- Apakah capability compatible?
- Apakah status printer READY atau minimal CONNECTED?
- Apakah paper width cocok?

### 10.2 Saat print gagal
Klasifikasi:
A. Connectivity
- disconnected
- timeout
- network unreachable
Tindakan:
- retry connect
- switch printer
- keep job pending

B. Device condition
- paper out
- cover open
- busy
- low battery
Tindakan:
- tampilkan instruksi operasional
- allow retry after resolved

C. Payload / compatibility
- unsupported width
- unsupported barcode/QR
- encoding issue
Tindakan:
- fallback template
- switch device
- log payload hash dan capability mismatch

D. Unknown error
Tindakan:
- mark failed
- simpan raw diagnostic
- user tetap punya preview + PDF fallback
- sarankan self-test printer / ganti printer

### 10.3 Self-test / diagnostics screen
Sediakan menu:
- printer list
- capability summary
- last seen status
- test print
- sample 58/80mm test
- reconnect
- forget device / rebind
- diagnostics log export

==================================================
## 11. TESTING STRATEGY TANPA HARDWARE
==================================================

### 11.1 Common / domain tests
- DocumentNumberPolicy_UniquePerStoreDayTerminalTest
- ReprintPolicy_RequiresExistingSnapshotTest
- CapabilityCompatibilityPolicy_RejectsWrongPaperWidthTest
- PrinterRoutePolicy_SelectsAssignedPrinterFirstTest
- PreviewRequirementPolicy_AllPhase1DocumentsRequirePreviewTest
- PrintFailurePolicy_DoesNotRollbackFinalizedSaleTest

### 11.2 Renderer snapshot / golden tests
- Receipt80mm_RenderSnapshotTest
- Receipt58mm_RenderSnapshotTest
- InvoiceA4_RenderSnapshotTest
- ReprintBadge_RenderSnapshotTest
- LongItemName_WrapDeterministicTest
- TaxDiscountPaymentSection_RenderConsistencyTest
- BarcodeQrSubset_RenderFallbackTest

### 11.3 Repository / component tests
- SavePrintDocument_StoresSnapshotAndTemplateVersionTest
- SubmitPrintJob_PersistsQueuedStateTest
- RetryPrintJob_PreservesIdempotencyTest
- ReprintDocument_WritesReprintRecordTest
- PrinterStatusSnapshot_StoresLatestStateTest

### 11.4 Adapter contract tests dengan fake printer
Buat FakePrinterAdapter:
- supports status readback configurable
- supports paper width configurable
- can simulate:
- success
- timeout
- disconnected
- paper out
- cover open
- busy
- garbled response
- assert:
- correct command sequence
- correct state transitions
- correct audit/logging
- retry behavior

### 11.5 Thermal command verification
- jangan tunggu hardware untuk verifikasi awal
- uji command encoder ke byte stream/golden hex/text expectation
- subset yang diuji:
- init/reset
- align
- text
- bold
- feed
- cut
- barcode/QR minimal
- ini cukup untuk menangkap 80% bug formatter tanpa printer fisik

### 11.6 UI / desktop functional / Android selective tests
- checkout success -> preview muncul
- printer disconnected -> status tampil jelas
- print fail -> retry/switch/save PDF tersedia
- reprint flow -> badge REPRINT dan reason gate bila perlu
- desktop invoice preview -> print/export path benar

### 11.7 Real hardware selective test pack
Jalankan belakangan, bukan blocker harian:
- 1 Epson class device
- 1 Star class device
- 1 LAN path
- 1 USB path
- 1 paper out / cover open / reconnect scenario
- 1 actual printed receipt barcode readability check

==================================================
## 12. IMPLEMENTATION SHAPE YANG DISARANKAN
==================================================

shared/
- sales/domain
- Receipt
- ReceiptReprintPolicy
- printing/domain
- PrintDocument
- PrintJob
- PrinterDevice
- PrinterStatusSnapshot
- CapabilityProfile
- PrintPolicy
- printing/application
- BuildPrintDocumentUseCase
- PreviewPrintDocumentUseCase
- SubmitPrintJobUseCase
- RetryPrintJobUseCase
- ResolvePrinterUseCase
- ObservePrinterStatusUseCase
- printing/data
- PrintDocumentRepositoryImpl
- PrintJobRepositoryImpl
- PrinterRegistryRepositoryImpl
- Renderer implementations
- adapter ports

apps/android-pos/
- feature-receipt-ui
- platform-device/printer
- epson
- star
- escpos-lan
- escpos-usb
- bluetooth-fallback
- feature-printer-diagnostics-ui

apps/desktop-backoffice/
- feature-receipt-ui
- feature-invoice-ui
- platform-device/printer
- system-spooler
- escpos-lan
- epson
- star

==================================================
## 13. KEPUTUSAN TEGAS YANG TIDAK BOLEH DILANGGAR
==================================================

1. Sale final yang sah tidak boleh dibatalkan hanya karena printer gagal.
2. Preview bukan editor business data.
3. Reprint harus berbasis snapshot dokumen final, bukan rebuild liar.
4. Status printer yang tidak diketahui harus tampil sebagai UNKNOWN, bukan READY palsu.
5. Jangan baseline pada printer generic murahan tanpa capability model.
6. Bluetooth bukan jalur utama receipt checkout.
7. HO bukan dependency runtime untuk print operasional toko.
8. Invoice dan receipt adalah business artifact dengan nomor readable.
9. UI tidak boleh tahu detail driver/protocol printer.
10. Testing awal harus bisa jalan tanpa hardware lewat fake adapter + snapshot/golden + command verification.

==================================================
## 14. REKOMENDASI FINAL PALING PRAKTIS
==================================================

Phase-1 shipping baseline:
- Receipt thermal 80mm + 58mm fallback
- Invoice PDF/A4
- Reprint receipt/invoice
- Preview wajib
- LAN/TCP + USB support dulu
- Epson + Star adapter resmi dulu
- Bluetooth fallback terbatas
- Printer diagnostics screen wajib
- Fake printer adapter + golden renderer tests wajib
- Real hardware selective pack menyusul

Itu baseline yang paling implementable, tidak bohong secara engineering, dan paling selaras dengan artefak Cassy.


## Constraints / Policies
UI tidak boleh generate ESC/POS langsung; failure print tidak boleh diam-diam menghapus jejak bisnis.

## Technical Notes
Harus disejajarkan dengan sales/receipt, theming/e2e, asset handling, dan hardware adapter platform.

## Dependencies / Related Documents
- `store_pos_sequence_detail_specifications.md`
- `cassy_architecture_specification_v1.md`
- `cassy_asset_resolver_scheme.md`
- `cassy_e2e_store_operation_uiux_flow_scheme_v2.md`
- `cassy_theming_ui_contract_phase_1.md`

## Risks / Gaps / Ambiguities
- Tidak ditemukan gap fatal saat ekstraksi. Tetap review ulang bagian tabel/angka jika dokumen ini akan dijadikan baseline implementasi final.

## Reviewer Notes
- Struktur telah dinormalisasi ke Markdown yang konsisten dan siap dipakai untuk design review / engineering handoff.
- Istilah utama dipertahankan sedekat mungkin dengan dokumen sumber untuk menjaga traceability lintas artefak.
- Bila ada konflik antar dokumen, prioritaskan source of truth, artefak yang paling preskriptif, dan baseline yang paling implementable.

## Source Mapping
- Original source: `PRINTING-MECHANISM-SCHEME-—-CASSY.txt` (TXT, 793 lines)
- Output markdown: `cassy_printing_mechanism_scheme.md`
- Conversion approach: text extraction -> normalization -> structural cleanup -> standardized documentation wrapper.
- Note: marker sitasi/annotation internal non-substantif dibersihkan agar hasil markdown lebih implementable.
