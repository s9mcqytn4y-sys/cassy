# Cassy Asset Resolver Scheme

## Document Overview
Baseline asset handling untuk image/file/evidence dengan mode hybrid local temp → background upload → cloud promotion.

## Purpose
Menetapkan identity, revisioning, linking, orchestration, dan retention policy untuk asset lintas domain.

## Scope
Asset domain model, resolver contract, upload/download/replace/archive flow, audit evidence retention, dan cloud storage posture.

## Key Decisions / Core Rules
Jangan gunakan raw path/URL sebagai identity; logical asset dipisah dari physical revision; evidence sensitif tidak boleh hard delete secara destruktif.

## Detailed Content

### Normalized Source Body
Baseline: image + file + attachment audit
Mode: hybrid local temp -> background upload -> promote to cloud asset
Target UX: cepat, offline-safe, optimistic preview, thumbnail super cepat, low-memory Android

## Observasi Artefak Source Yang Mempengaruhi Keputusan
1. Cassy sudah jelas local-first: semua mutasi bisnis harus commit ke DB lokal dulu; sync adalah concern durable terpisah, bukan background queue implisit. Ini berarti asset flow tidak boleh didesain server-first. Asset write intent, metadata, audit, dan sync state harus bisa hidup lokal dulu.
2. Arsitektur Cassy memisahkan bounded context dan menolak shortcut yang mengaburkan auditability. Data layer yang boleh mengurus SQL/DTO/cache policy, bukan UI. Ini cocok untuk AssetResolver + AssetRepository + UploadWorker yang jelas boundary-nya.
3. ERD v2 sudah mengindikasikan shared-kernel entity file_asset dan bahwa file asset dipakai lintas domain. Jadi asset jangan ditempel sebagai raw path string liar di product/receipt/return tables. Harus lewat referential model.
4. Return/refund, approval, dan audit adalah flow sensitif dan harus meninggalkan evidence durable. Use case return mewajibkan transaksi return + audit log tersimpan; activity dan event contract juga menegaskan approval/audit evidence tidak boleh destruktif. Jadi keputusan "langsung dihapus" untuk audit attachment itu buruk kalau diterapkan rata.
5. Event contract tegas: audit/approval evidence bersifat append-only; koreksi harus follow-up evidence, bukan merge destruktif. Jadi replace asset harus soft replace + rollback pointer, bukan hard overwrite object.
6. Offline policy Cassy juga tegas: flow berisiko seperti return/refund boleh dibatasi jika cache/policy tidak memadai, tetapi mutasi offline yang sah harus tetap ditandai pending sync dan bisa direconcile. Maka asset state juga perlu pending upload / pending sync / conflicted.
7. Observability Cassy bukan log scraping. Sync batch/item/conflict harus visible. Untuk asset, ini berarti upload/download/replace juga harus punya sync visibility dan correlation/idempotency metadata.

## Quick Feedback
- Fondasi pilihan Anda sudah benar: hybrid local temp -> background upload -> promote.
- Yang perlu saya koreksi tegas: "langsung dihapus" tidak boleh berlaku global. Untuk product image boleh agresif. Untuk receipt attachment, return evidence, approval evidence, dan audit-related attachment harus retention by policy, minimal tombstone + purge job, bukan delete spontan.
- Saya sangat tidak menyarankan raw path string sebagai kontrak domain. Itu akan bikin local/cloud/cached/replaced asset cepat berantakan.

## The Critique
1. Jangan buat Asset hanya sebagai "url/path".
Itu desain murah di awal, mahal di maintenance. Anda butuh satu identity stabil yang survive local temp, upload retry, cloud promotion, replace, rollback, dan offline mode.

2. Jangan hard overwrite file object yang sama.
Ini akan tabrakan dengan cache invalidation, optimistic UI, rollback, auditability, dan CDN/browser/image cache. Untuk Cassy, yang benar adalah logical asset tetap, physical object version berganti.

3. Jangan samakan product image dengan audit evidence.
Keduanya beda total.
- Product image: boleh aggressive optimization, thumbnailing, replace cepat, purge lebih agresif.
- Audit/approval/return evidence: harus preserve chain of evidence, actor context, timestamp, reason, dan replace history.

4. Jangan biarkan UI langsung tahu local path, cloud URL, signed URL, cache file, dan variant logic.
UI hanya boleh tahu "AssetRef -> resolver -> result". Selain itu akan jadi spaghetti.

## Final Rekomendasi

A. REKOMENDASI CLOUD PALING MASUK AKAL
Pilihan utama: Cloudflare R2.

Alasan:
- biaya storage rendah,
- egress ke internet gratis,
- S3-compatible,
- cocok untuk object storage image/file,
- enak dipakai lewat signed URL atau backend proxy. Cloudflare menyebut R2 standard storage $0.015/GB-month dengan zero egress; docs pricing juga menyebut standard $0.015/GB-month, class A $4.50/million, class B $0.36/million.

Pembanding:
- Backblaze B2 terlihat murah untuk storage aktif, sekitar $6/TB/month, tetapi model egress dan workload pattern perlu dihitung hati-hati.
- S3 biasanya paling aman secara enterprise ecosystem, tapi untuk use case asset Cassy biasanya bukan yang paling murah.

Rekomendasi pragmatis:
- Phase 1: Cloudflare R2 + backend signed upload/download API.
- Jangan client upload langsung dengan kredensial provider mentah.
- Semua cloud access tetap lewat application backend contract agar policy, authz, audit, dan replace workflow tetap terkendali.

B. REKOMENDASI IDENTITAS DAN KONTRAK ASSET
Gunakan 2 lapis identitas:

## 1. logical_asset_id
Identitas domain-level yang stabil.
Ini yang direferensikan Product, ReceiptAttachment, ReturnEvidence, ApprovalEvidence.

## 2. asset_revision_id
Identitas versi fisik aktual.
Setiap edit/replace menghasilkan revision baru.

Jangan gunakan:
- raw local path sebagai identity
- raw cloud URL sebagai identity
- filename sebagai identity

Kontrak domain utama:
- AssetRef
- logicalAssetId
- usageType
- expectedMimeGroup
- preferredVariant
- AssetDescriptor
- logicalAssetId
- currentRevisionId
- state
- source
- variants
- metadata
- AssetRevision
- revisionId
- logicalAssetId
- storageBackend
- objectKey
- contentHash
- mimeType
- byteSize
- width/height jika image
- createdBy
- createdAt
- replacedByRevisionId nullable
- supersedesRevisionId nullable

C. DOMAIN MODEL YANG SAYA SARANKAN
## 1. Shared Kernel
- file_asset
- file_asset_revision
- file_asset_link
- file_asset_variant
- file_asset_job
- file_asset_access_log opsional

## 2. Linking table lintas domain
file_asset_link:
- link_id
- logical_asset_id
- owner_type = PRODUCT | RECEIPT | RETURN | APPROVAL | AUDIT
- owner_id
- role = PRIMARY_IMAGE | GALLERY_IMAGE | RECEIPT_PDF | RETURN_EVIDENCE | APPROVAL_EVIDENCE
- sort_order
- is_active
- attached_by
- attached_at

## 3. Variant table
file_asset_variant:
- variant_id
- revision_id
- variant_type = THUMB_128 | THUMB_256 | PREVIEW | ORIGINAL | PRINT
- object_key
- mime_type
- width
- height
- byte_size

## 4. Asset job table
file_asset_job:
- job_id
- logical_asset_id
- revision_id
- job_type = COMPRESS | TRANSCODE | THUMBNAIL | UPLOAD | DELETE_REMOTE | PURGE_LOCAL
- state = QUEUED | RUNNING | DONE | FAILED | RETRY_WAIT
- retry_count
- next_retry_at
- error_code
- error_message

D. STATE MACHINE ASSET
AssetDescriptor.state:
- LOCAL_DRAFT
- LOCAL_READY
- UPLOAD_QUEUED
- UPLOADING
- UPLOAD_FAILED_RETRYABLE
- UPLOADED_UNVERIFIED
- SYNC_PENDING
- ACTIVE
- REPLACED
- ARCHIVED
- PURGED
- CONFLICTED

Aturan:
- pilih file/gambar -> LOCAL_DRAFT
- crop/compress selesai -> LOCAL_READY
- commit metadata + link owner + audit -> UPLOAD_QUEUED
- worker upload -> UPLOADING
- cloud success + metadata promoted -> SYNC_PENDING atau ACTIVE
- replace -> revision baru ACTIVE, revision lama REPLACED
- rollback -> pointer currentRevisionId kembali ke revision lama
- purge policy -> ARCHIVED/PURGED sesuai domain

E. HANDLER YANG HARUS ADA
## 1. AssetResolver
Tugas:
- resolve AssetRef -> variant paling cocok
- pilih local cached file jika valid
- fallback ke signed URL/cloud
- pahami state LOCAL/QUEUED/FAILED/ACTIVE/REPLACED

## 2. AssetLoadHandler
Tugas:
- load thumbnail/list image
- load preview/detail image
- load original file kalau user eksplisit minta
- support placeholder + optimistic preview + retry state

## 3. AssetUploadHandler
Tugas:
- terima temp file hasil picker/camera/scanner
- normalize metadata
- compress/transcode bila image
- simpan draft lokal
- enqueue upload job
- promote revision saat upload sukses
- tulis audit/outbox bila domain mensyaratkan

## 4. AssetDownloadHandler
Tugas:
- resolve signed URL/backend stream
- download original bila user butuh open/share/export
- simpan cache sesuai policy
- checksum verify untuk file penting

## 5. AssetReplaceHandler
Tugas:
- buat revision baru
- current pointer pindah ke revision baru
- revision lama ditandai superseded
- invalidate variant cache
- publish domain event/audit
- support rollback

## 6. AssetArchiveHandler / PurgeHandler
Tugas:
- purge local temp
- purge stale cache
- archive or delete old remote objects sesuai policy domain
- jangan delete langsung untuk evidence sensitif

F. ALUR MODERN UX YANG SAYA REKOMENDASIKAN

## 1. Product / Catalog image
Flow:
- user pilih/take photo
- langsung tampil optimistic preview dari local temp
- user bisa crop
- sistem compress + buat thumbnail lokal
- save langsung terasa sukses secara UI
- background upload jalan
- kalau sukses, badge kecil "tersinkron"
- kalau gagal, badge "belum terkirim" + retry

UX rules:
- list/grid selalu pakai THUMB_128/256
- detail pakai PREVIEW
- ORIGINAL hanya saat zoom penuh
- shimmer/blur-up placeholder
- jangan block form submission hanya karena upload belum selesai, kecuali policy mewajibkan

## 2. Receipt / invoice attachment
Flow:
- attachment dibuat atau dipilih
- preview icon + filename + size
- jika PDF/image, preview inline
- original file di-download on demand
- signed URL jangan diekspos sebagai kontrak permanen

## 3. Return/refund/approval evidence
Flow:
- pilih/take photo wajib capture timestamp dan actor context
- evidence langsung attach ke entity target
- state "pending sync" harus terlihat
- replace evidence tidak menghapus chain; jadikan evidence baru atau revision baru tergantung policy
- supervisor/reconcile UI harus bisa melihat revision history minimal

G. KEBIJAKAN REPLACE / PATCH / EDIT
Rekomendasi final:
- product/catalog image: SOFT REPLACE + optional purge old revisions after retention singkat
- receipt/invoice attachment: SOFT REPLACE, retain short history
- return/refund evidence: APPEND-ONLY preferred; kalau "replace" di UI, implementasi sebenarnya add new revision/evidence + old one superseded, bukan overwrite senyap
- approval evidence: APPEND-ONLY
- audit evidence: APPEND-ONLY

Kenapa:
Artefak Cassy menolak merge destruktif untuk approval/audit evidence dan menuntut follow-up evidence saat ada koreksi.

Jadi keputusan user "langsung dihapus" saya ubah menjadi:
- boleh untuk temporary local files dan obsolete product image cache
- tidak boleh default untuk audit/approval/return evidence

H. PERFORMANCE IMAGE - AGAR APLIKASI TETAP LANCAR
1. Jangan load original image ke list.
Wajib pakai variant thumbnail.

## 2. Variant yang disarankan:
- THUMB_128 untuk list padat
- THUMB_256 untuk grid/catalog
- PREVIEW_1024 untuk detail
- ORIGINAL hanya untuk zoom/export

## 3. Decode policy Android:
- decode sesuai target size view, bukan full bitmap
- gunakan hardware bitmap bila cocok
- batasi memory cache
- disk cache untuk thumbnail harus agresif
- original cache terbatas dan LRU

## 4. Format:
- upload source image diterima: JPEG, PNG, WebP, HEIC jika platform support
- storage normalized:
- thumbnail/previews -> WebP atau AVIF bila backend pipeline siap
- fallback JPEG untuk compatibility
- jangan paksa AVIF kalau toolchain/device decode belum stabil lintas target

## 5. Low-memory strategy:
- list screen prefetch hanya thumbnail viewport + small lookahead
- detail screen baru request preview
- cancel in-flight load saat item keluar viewport
- jangan prefetch original

## 6. Hash + ETag:
- gunakan contentHash/revisionId untuk cache key
- jangan pakai logicalAssetId saja, nanti replace tidak kebaca

I. RESOLVER CONTRACT YANG BENAR
UI hanya pegang:
- AssetRef

Resolver output:
- ResolvedAsset
- displayUri/local file handle/signed url
- variantType
- source = MEMORY | DISK | LOCAL_DRAFT | CLOUD
- width/height
- mimeType
- isStale
- canRetry
- syncState

Resolution order:
## 1. current revision local processed variant
## 2. disk cached variant matching revision
## 3. local original and derive on device jika ringan
## 4. signed cloud url / backend proxy
## 5. placeholder/failure state

J. UPLOAD / DOWNLOAD PIPELINE
UPLOAD
## 1. Pick/capture file
## 2. sniff MIME + validate size
## 3. normalize name
## 4. if image: read bounds, rotate EXIF, crop, compress, generate thumb
## 5. create logical asset + revision + link owner
## 6. persist local metadata + local file path
## 7. enqueue upload job
## 8. worker gets signed upload target
## 9. upload object
## 10. verify checksum / size
## 11. promote revision ACTIVE
## 12. enqueue sync metadata if needed
## 13. invalidate resolver cache by revision key

## Download
## 1. request original/preview
## 2. resolver checks cache
## 3. if cloud needed, ask backend signed url
## 4. stream to temp or open via proxy
## 5. verify checksum for critical files
## 6. update access marker if needed

K. MAX SIZE / MIME POLICY YANG SAYA SARANKAN
Image:
- allowed: image/jpeg, image/png, image/webp, image/heic opsional
- capture target upload:
- product image: max 8 MB raw, compress target 300 KB-900 KB per preview depending dimension
- audit/return evidence: max 10 MB raw, preserve legibility over cosmetic compression
- dimensions:
- reject absurd images, misal > 8000 px unless admin/import flow

Files:
- allowed:
- application/pdf
- image/jpeg/png/webp
- text/csv bila memang dibutuhkan
- application/vnd.openxmlformats-officedocument.* bila business perlu
- general attachment max:
- normal operator flow: 20 MB
- backoffice/admin/manual upload: 50 MB
- block by default:
- apk, exe, bat, sh, js executable attachment, zip terenkripsi bila belum ada scanning policy

L. SECURITY / AUTHORIZATION YANG SAYA REKOMENDASIKAN
## 1. Upload permission
- Cashier:
- product image: no, kecuali policy store tertentu
- return evidence: yes
- receipt attachment: yes sesuai flow
- Supervisor:
- approval evidence: yes
- replace/delete evidence: yes by policy
- Manager/Admin:
- archive/purge/recover: yes

## 2. Download permission
- product image thumb/preview: broad
- original receipt/invoice/approval evidence: scoped by store/role
- signed URL short TTL
- jangan expose permanent public URL untuk evidence sensitif

## 3. Audit minimal setiap mutation
- actor_id
- role
- store_id
- terminal_id
- owner_type
- owner_id
- logical_asset_id
- revision_id
- action = ATTACH | REPLACE | ROLLBACK | ARCHIVE | PURGE | DOWNLOAD_ORIGINAL
- correlation_id
- idempotency_key bila retriable

Ini inline dengan aturan Cassy bahwa setiap mutasi harus membawa actor/store/terminal/correlation context dan operasi sensitif harus punya audit durable.

M. ARCHIVE POLICY FINAL
Saya ubah policy Anda menjadi ini:

## 1. TEMP local files
- auto purge 1-7 hari
- aman dihapus

## 2. Product/catalog image old revisions
- retain 1-3 old revisions selama 30 hari
- lalu purge remote + local cache
- rollback masih mungkin dalam window singkat

## 3. Receipt/invoice attachment
- retain minimal mengikuti kebutuhan operasional dan dispute window
- jangan langsung purge saat replace

## 4. Return/refund evidence
- retain lebih lama
- replace = new revision/evidence
- purge hanya via manager/admin policy

## 5. Approval/audit evidence
- append-only
- purge bukan operasi normal UI
- kalau compliance belum jelas, default retain

N. IMPLEMENTATION SHAPE YANG PALING MASUK AKAL UNTUK KMP/ANDROID/BACKEND
Shared KMP:
- asset/domain
- AssetRef
- AssetDescriptor
- AssetRevision
- AssetPolicy
- AssetState
- asset/application
- ResolveAssetUseCase
- AttachAssetUseCase
- ReplaceAssetUseCase
- RollbackAssetUseCase
- EnqueueUploadUseCase
- PurgeAssetUseCase
- asset/data
- AssetRepository
- AssetLocalDataSource
- AssetRemoteDataSource
- AssetVariantGenerator
- AssetUploadQueue

Android-specific:
- picker/camera/scanner adapter
- image cropper/compressor
- thumbnail decoder
- background worker integration

Backend:
- signed upload request endpoint
- signed download request endpoint
- asset finalize endpoint
- asset replace/rollback policy endpoint
- object lifecycle cleanup worker
- optional antivirus scan hook for file attachments

O. KEPUTUSAN 11 DAN 12 - REKOMENDASI SAYA

## 11. Security/scope recommendation:
- scope campuran, tapi semua asset wajib punya:
- store_id
- owner_type
- owner_id
- classification = PUBLIC_CATALOG | INTERNAL_OPS | SENSITIVE_EVIDENCE
- access control berbasis role + owner scope + store scope
- original sensitive asset selalu protected
- thumbnail untuk catalog bisa semi-public/internal depending architecture

## 12. Reference style recommendation:
- Domain/API/UI gunakan AssetRef berbasis logicalAssetId
- Data layer pegang objectKey, cache path, signed URL, checksum
- Jangan pakai raw path string atau raw URL sebagai kontrak lintas layer

## The Solution - Ringkas Final
- Cloud: Cloudflare R2
- Identity: logical_asset_id + asset_revision_id
- Replace: soft replace + rollback
- Evidence sensitif: append-only mindset
- Resolver: AssetRef -> variant-aware resolved asset
- Upload: local-first draft, background upload, promote on success
- Performance: thumbnail-first, preview-second, original on demand
- Cache key: revision-aware
- Archive: policy-based, bukan delete global
- UI: optimistic preview + pending sync visibility + retry

## Deep Question
Sebelum implementasi saya sarankan Anda putuskan satu hal ini dengan tegas:
Apakah "return/refund evidence" dan "approval evidence" diperlakukan sebagai revision dari asset yang sama, atau sebagai evidence baru yang ditautkan ke entity yang sama?

Kalau target Anda auditability kuat, jawaban terbaik biasanya:
- product image = revision
- return/approval evidence = new evidence record, bukan sekadar revision


## Constraints / Policies
Asset flow harus local-first, audit-heavy, dan sync-visible.

## Technical Notes
Cocok sebagai shared-kernel pattern untuk product image, receipt attachment, return evidence, approval evidence, dan audit artifact.

## Dependencies / Related Documents
- `cassy_architecture_specification_v1.md`
- `store_pos_erd_specification_v2.md`
- `cassy_event_contract_sync_specification_v1.md`
- `store_pos_use_case_detail_specifications.md`
- `cassy_printing_mechanism_scheme.md`

## Risks / Gaps / Ambiguities
- Tidak ditemukan gap fatal saat ekstraksi. Tetap review ulang bagian tabel/angka jika dokumen ini akan dijadikan baseline implementasi final.

## Reviewer Notes
- Struktur telah dinormalisasi ke Markdown yang konsisten dan siap dipakai untuk design review / engineering handoff.
- Istilah utama dipertahankan sedekat mungkin dengan dokumen sumber untuk menjaga traceability lintas artefak.
- Bila ada konflik antar dokumen, prioritaskan source of truth, artefak yang paling preskriptif, dan baseline yang paling implementable.

## Source Mapping
- Original source: `ASSET-RESOLVER-SCHEME-—-CASSY.txt` (TXT, 497 lines)
- Output markdown: `cassy_asset_resolver_scheme.md`
- Conversion approach: text extraction -> normalization -> structural cleanup -> standardized documentation wrapper.
- Note: marker sitasi/annotation internal non-substantif dibersihkan agar hasil markdown lebih implementable.
