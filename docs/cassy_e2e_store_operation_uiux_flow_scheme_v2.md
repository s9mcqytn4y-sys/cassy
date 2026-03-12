# Cassy E2E Store Operation UIUX Flow Scheme v2

## Document Overview
Updated integrated baseline untuk flow operasional harian, guided dashboard, setup gate, readiness state, approval, printing, offline/sync visibility, dan acceptance criteria.

## Purpose
Menyatukan flow bisnis nyata dengan UI contract, auth, printing, dan sync visibility ke satu baseline yang siap handoff.

## Scope
Retail POS Phase 1; Desktop Backoffice dan Android Mobile tetap selective sesuai boundary yang sudah disetujui Entry point UX Guided Operations Dashboard + setup gate + readiness-driven navigation Source basis E2E scheme lama + UI contract v1.1 + auth strategy + printing mecha...

## Key Decisions / Core Rules
Guided Operations Dashboard menjadi entry point; setup sebelum transaksi pertama adalah hard gate; offline adalah controlled degradation; print adalah business output yang traceable.

## Detailed Content

### Normalized Source Body
CASSY
E2E Store Operation + UI/UX Flow
Scheme
Phase 1 · Updated Integrated Baseline
Dokumen ini meng-update baseline flow operasional harian Cassy POS agar sinkron dengan
hardening terbaru pada manual setup, UI contract, auth/session, printing, offline/sync visibility,
dan acceptance criteria lintas platform.
Status
Updated baseline yang siap untuk design review
dan engineering handoff
Versi
### 2.0 · 10 Maret 2026
Scope utama
Retail POS Phase 1; Desktop Backoffice dan
Android Mobile tetap selective sesuai boundary
yang sudah disetujui
Entry point UX
Guided Operations Dashboard + setup gate +
readiness-driven navigation
Source basis
E2E scheme lama + UI contract v1.1 + auth
strategy + printing mechanism + event/sync
baseline + use case / sequence / test baseline
Inti perubahan: flow tidak lagi berhenti pada "buka toko -> login -> shift -> jualan" secara generik.
Baseline baru menambahkan setup gate sebelum transaksi pertama, severity language yang stabil,
pending sync visibility, printer/reprint state yang eksplisit, step-up approval yang auditable, dan
acceptance criteria UI yang bisa diuji.

## 1. Tujuan Update
- 
Menyatukan artefak E2E operasional, setup/manual onboarding, UI contract, auth strategy, printing,
dan sync visibility ke satu baseline yang konsisten.
- 
Mengoreksi bagian flow lama yang masih terlalu implisit pada setup awal, state severity, approval gate,
printer failure, dan pending sync.
- 
Menetapkan flow yang siap dipakai untuk design review, screen map, implementation handoff, dan
baseline QA tanpa ambiguity lintas Android POS, Desktop, dan surface supervisor selektif.
## 2. Hasil Observasi Artefak Sumber
- 
Flow lama sudah benar pada fondasi bisnis: POS harus terasa seperti guided operation, bukan menu
bebas; login, business day, shift, dan opening cash adalah hard gate sebelum checkout.
- 
Namun baseline lama belum cukup keras pada setup transaksi pertama, severity semantics,
persistence state untuk print/reprint failure, dan kontrak dashboard lintas platform.
- 
UI contract v1.1 menutup gap penting: setup & onboarding, readiness cards, pending sync state,
approval gate, printing/asset rule, aksesibilitas minimum, dan adaptive layout.
- 
Auth strategy memperjelas personal identity pada shared device, device enrollment, PIN unlock,
biometric optional, dan step-up approval yang harus meninggalkan audit/outbox state.
- 
Printing mechanism menegaskan preview wajib, reprint dari final snapshot, printer routing sederhana,
dan kegagalan print tidak boleh membatalkan sale yang sudah final.
- 
Event/sync baseline menegaskan offline adalah controlled degradation; mutasi lokal yang sah harus
terlihat sebagai pending sync dan dapat direconcile, bukan disembunyikan di log.
## 3. Delta Utama dari Baseline Sebelumnya
Area
Sebelumnya
Update final
Setup awal
Terminal bind disebut, tetapi
belum dimodelkan sebagai flow
setup lengkap.
Wajib ada setup gate: activation,
device registration, store info,
printer setup, product minimum,
receipt customization, readiness
final.
Dashboard
Masih kuat sebagai control
tower, tetapi kontrak section
belum cukup preskriptif.
Section wajib dikunci: Today
Status, Readiness Cards,
Primary Next Action, Task
Clusters, visible blockers.
Severity state
Ready / Warning / Blocked
sudah ada, tetapi belum sinkron
dengan UI contract.
Tambah semantic map stabil:
Info, Success, Warning, Danger,
Blocked, Pending Sync,
Approval Required, Offline
Restricted.
Auth/session
Login dan offline allowance
sudah ada, tetapi unlock cepat
dan step-up belum menyatu di
E2E.
Integrasi first login, daily unlock,
offline unlock, session lock,
supervisor PIN/biometric,
recovery boundary.
Printing
Receipt preview dan retry sudah
ada.
Tambah printer setup sebelum
go-live, print health card, reprint
dari final snapshot, explicit
unavailable state, print pending
visibility.
Acceptance
Masih dominan deskriptif.
Ditambah acceptance checklist,
anti-skip matrix, worst-case

journey, dan implementation
handoff rule.
## 4. Prinsip Desain yang Mengikat
- 
Operational clarity first: user harus langsung paham readiness toko, blocker, device health, dan next
action.
- 
Anti-skip flow: langkah berikut tidak boleh commit jika prerequisite bisnis belum valid.
- 
State must be visible: blocked, warning, danger, pending sync, approval required, dan offline restricted
tidak boleh samar.
- 
Native where it matters: semantics dan hierarchy sama, tetapi adaptation Android / iOS / Desktop
mengikuti idiom platform.
- 
Audit-friendly interaction: flow sensitif harus menghasilkan reason/evidence/audit state, bukan hanya
feedback visual.
- 
Setup is a gate: transaksi pertama tidak boleh dimulai sebelum setup minimum selesai.
- 
Print is business output: preview, print status, reprint, dan fallback harus eksplisit serta traceable.
- 
Offline is controlled degradation: flow aman boleh lanjut lokal; flow berisiko harus diblok atau di-
approval sesuai policy.
## 5. Integrated Flow Topology
Stage
Tujuan bisnis
Output UX
Gate utama
A. Setup awal toko
Menyiapkan
store/terminal/printer/da
ta minimum sebelum
transaksi pertama.
Setup wizard +
readiness checklist
Belum boleh jualan
sebelum setup
complete
B. Launch & access
Memastikan user sah,
device terbind,
capability valid.
Splash/bootstrap, login,
access decision
No login / terminal
mismatch / expired
grant = hard block
C. Business day
readiness
Mengaktifkan hari
operasional dan
menyelesaikan carry-
over issue.
Readiness review +
open business day
Business day wajib aktif
sebelum shift
D. Start shift
Membuka sesi
terminal/kasir dengan
opening cash yang sah.
3-step shift wizard +
approval branch
Tanpa shift aktif, sales
dilarang
E. Device & data
readiness
Memastikan
printer/scanner/data
snapshot/sync cukup
sehat.
Readiness cards +
diagnostics shortcuts
Data invalid = block;
printer/scanner
tergantung policy
F. Operasi harian
Sales, receipt, cash
control, inventory light
ops.
Task flow cepat +
preview + side flows
Approval/reason/policy
tetap aktif
G. Reconcile & close
Menutup shift dan
business day secara
terkendali.
Close shift wizard + end
of day readiness
Pending critical & open
shift = block
## 6. Updated E2E Flow
Phase 0A - Setup Sebelum Transaksi Pertama
- 
Welcome & introduction -> activation/license validation -> device registration/terminal binding.
- 
Store information wajib: nama toko, alamat, telepon. Logo dan footer receipt opsional.

- 
Printer setup: discovery, select, test print, fallback guidance. Failure tidak boleh disembunyikan di toast
singkat.
- 
Product setup minimum 1-5 produk; upload image/barcode opsional agar flow tetap cepat.
- 
Receipt customization hanya satu template phase 1; preview wajib, tanpa ilusi pilihan template
berlebihan.
- 
Operational readiness checklist menjadi gate terakhir sebelum CTA "Mulai Jualan".
Gate: Hard block sampai activation, binding, printer setup minimum, product minimum, dan
readiness final complete.
Phase 0B - Operational Preflight Sebelum Login
- 
Staf datang, menyalakan device, printer, scanner, cash drawer, dan mengecek kertas/printer/cash
drawer.
- 
Aplikasi harus menyediakan readiness context, bukan menganggap preflight ini tidak ada.
- 
Jika ada issue carry-over, issue tersebut harus muncul sebagai blocker/warning sejak bootstrap.
Gate: Tidak ada gate transaksi langsung, tetapi issue preflight harus visible di dashboard dan
readiness detail.
Phase 1 - Launch, Bootstrap, dan Access Decision
- 
Splash/bootstrap memuat app config, terminal binding, session state, local snapshot status, sync
health, dan device health.
- 
User login dengan password pada first login; daily return dapat memakai PIN atau biometric jika
enrollment valid.
- 
Jika terminal belum terbind, user diarahkan ke setup/bind terminal, bukan ke home kosong.
- 
Jika credential invalid atau offline grant expired, user tetap blocked dengan alasan operasional yang
eksplisit.
Gate: Tanpa login valid dan terminal binding aktif, tidak ada akses operasional.
Phase 2 - Guided Operations Dashboard
- 
Dashboard menjadi control tower, bukan grid menu bebas.
- 
Section wajib: Today Status, Readiness Cards, Primary Next Action, Task Clusters.
- 
Satu CTA utama paling dominan: Mulai Hari Bisnis, Mulai Shift, Lanjut Jualan, Selesaikan Blocker,
Rekonsiliasi, atau Tutup Hari.
- 
Aksi yang belum memenuhi prasyarat boleh terlihat read-only, tetapi tidak boleh commit operasi
kritikal.
Gate: Primary next action ditentukan policy; no free-jump ke critical commit flow.
Phase 3 - Open Business Day
- 
Dashboard mendeteksi apakah business day sudah aktif.
- 
Jika belum aktif, user dengan capability sesuai melihat readiness summary dan CTA "Mulai Hari
Bisnis".
- 
Sistem memvalidasi tanggal bisnis, store status, carry-over issue, minimal sync/master data readiness,
serta time drift bila relevan.
- 
Jika lolos, business day aktif dan flow lanjut otomatis ke Start Shift.
Gate: Tanpa business day aktif, shift dan checkout tidak boleh dimulai.
Phase 4 - Start Shift
- 
Wizard 3 step: Verify Access -> Opening Cash -> Shift Ready.
- 
Sistem mengecek capability start shift, conflict shift lama, dan policy terminal.

- 
Opening cash diinput dan divalidasi; jika out of policy, flow berpindah ke approval sheet dengan reason
wajib.
- 
Jika approval gagal atau supervisor tidak tersedia saat dibutuhkan, user tetap tertahan.
Gate: Tanpa shift aktif dan opening cash sah, Sales Home tetap locked.
Phase 5 - Device & Data Readiness
- 
Readiness cards memeriksa printer assigned/backup, scanner readiness, pricing/master snapshot
validity, online/offline mode, dan sync backlog health.
- 
Master data/pricing invalid = hard block transaksi.
- 
Printer abnormal tidak selalu memblok buka toko, tetapi memblok mode yang mewajibkan printed
receipt jika policy mengharuskannya.
- 
Scanner tidak siap boleh turun menjadi warning jika manual search masih valid.
Gate: Semua blocker wajib punya corrective CTA; warning hanya bisa diterima jika policy
mengizinkan.
Phase 6 - Sales & Checkout
- 
Flow cepat: Cart -> Payment -> Receipt Preview.
- 
Scan/cari produk, recalculation otomatis, optional member/voucher, review total, payment, receipt
preview, print/share, selesai.
- 
Preview wajib ringan dan print-ready; preview bukan editor transaksi.
- 
Payment pending/failed harus visible; suspend transaction tetap tersedia saat policy mengizinkan.
Gate: Tidak boleh ke payment jika cart/pricing invalid; tidak boleh finalisasi sale tanpa payment
state sah dan receipt snapshot terbentuk.
Phase 7 - Cash Control & Approval-Aware Side Flows
- 
Cash in/out, safe drop, dan cash audit ringan harus selalu terhubung ke shift aktif.
- 
Reason code wajib; evidence/attachment mengikuti risk policy.
- 
Jika nominal di atas limit atau variance melebihi tolerance, flow berubah menjadi approval gate.
- 
Approval sheet memakai supervisor PIN sebagai baseline; supervisor biometric opsional jika policy
mengizinkan.
Gate: Tanpa reason code dan approval yang sah, submit disabled/fail closed.
Phase 8 - Inventory Light Ops
- 
Quick stock lookup, receiving sederhana, adjustment terbatas, cycle count parsial.
- 
Inventory ops adalah side flow operasional; tidak boleh merusak fokus sales utama.
- 
Jika cache/master data tidak memadai atau adjustment high-risk, tampilkan block jelas dan action
korektif.
Gate: Flow high-risk dibatasi offline dan approval-aware.
Phase 9 - Print / Reprint / Document Access
- 
Receipt preview tampil segera dari local finalized snapshot; printer status berjalan paralel.
- 
Jika printer gagal: retry printer, switch printer, simpan/share sesuai policy, atau tandai print pending.
Sale tidak dibatalkan hanya karena print gagal.
- 
Reprint selalu membaca final snapshot yang tersedia lokal dan menampilkan badge REPRINT.
- 
Jika artifact final tidak tersedia, UI wajib jujur menampilkan unavailable; jangan rebuild diam-diam dari
partial data.
Gate: Reprint tanpa final snapshot tidak boleh dipaksa; preview wajib untuk semua output phase 1.

Phase 10 - Sync Visibility & Reconciliation Awareness
- 
Offline window, pending sync, failed sync, dan conflict harus terlihat di dashboard/sync detail, bukan
hanya di log.
- 
Mutasi lokal yang sah ditandai pending sync dan tetap bisa direconcile kemudian.
- 
User harus bisa review backlog, retry, resolve conflict, atau mark manual investigation sesuai peran.
Gate: Flow berisiko yang butuh snapshot/policy segar diblok bila dependency kritis tidak tersedia.
Phase 11 - Close Shift
- 
Wizard tegas: Check Pending -> Count Cash -> Reconcile -> Review Report -> Confirm Close.
- 
Jika ada pending transaction yang dilarang policy, wizard berhenti di step 1.
- 
Variance di atas tolerance memicu approval step; report print failure boleh fallback digital bila policy
mengizinkan.
Gate: Shift yang sudah closed tidak boleh dipakai transaksi lagi.
Phase 12 - End of Day / Tutup Hari
- 
Manager membuka readiness panel yang mengecek semua shift sudah closed, required sync
minimum valid, critical exception terselesaikan, dan data inti lengkap.
- 
Setiap blocker punya action jelas: review sync, resolve pending, buka shift yang masih aktif, atau mark
investigation jika policy mengizinkan.
- 
Close business day bukan tombol cepat; harus ada post-close summary.
Gate: Tidak boleh close business day jika masih ada open shift, missing critical data, atau sync
minimum belum terpenuhi.
## 7. Guided Operations Dashboard Contract
Section
Isi wajib
Today Status
business day, shift, online/offline, printer status,
sync health, pending approval, pending exception,
pending print failure, pending close blockers
Readiness Cards
Ready / Warning / Blocked untuk printer, scanner,
pricing snapshot, master snapshot, sync backlog,
dan dependency lain
Primary Next Action
satu CTA utama yang paling relevan secara policy;
paling dominan secara visual
Task Clusters
sales quick entry, cash control, stock lookup,
receiving ringan, reprint, approval queue, health
check, settings center
Diagnostics / Deep Links
readiness detail, sync detail, device diagnostics,
printer diagnostics, approval queue
## 8. Severity & State Language
State
Makna
Pola UI
Contoh
Ready
Siap lanjut
state card netral-positif
+ CTA lanjut
Printer ready; shift aktif
Warning
Masih bisa operasi,
tetapi ada risiko terukur
warning card +
acknowledgement
Sync backlog non-kritis;
scanner fallback ke
manual search
Blocked
Policy/readiness belum
memenuhi syarat
persistent blocker card
+ corrective CTA
Business day belum
aktif; pricing snapshot
invalid

Danger/Failed
Kegagalan teknis atau
aksi destruktif
error surface tegas +
retry/resolve
Payment failed; printer
cover open saat print
Pending Sync
Mutasi lokal berhasil
tetapi belum final
tersinkron
purple badge/banner +
retry/review
Approval offline queued;
sale tersimpan lokal
Needs Approval
Flow boleh lanjut hanya
setelah approval sah
sheet/dialog step-up
auth + reason wajib
Opening cash override;
return approval
Offline Restricted
Mode offline membatasi
flow ini
read-only/blocked state
+ alasan
Close day blocked
offline; return dibatasi
Unavailable
Artifact/dependency
tidak tersedia
disabled state + honest
explanation
Reprint unavailable
karena snapshot final
tidak ada
## 9. Anti-Skip Gate Matrix
Flow target
Tidak boleh lanjut jika
Catatan
Sales
belum login valid; business day
belum aktif; shift belum aktif;
opening cash belum tercatat;
pricing/master data invalid;
terminal blocked
Read-only context boleh terlihat,
commit tidak boleh.
Payment
cart invalid; pricing unresolved;
item tidak valid; requirement
member/voucher belum selesai
Payment pending/fail harus
visible dan retry-safe.
Finalize sale
payment state belum sah;
settlement belum sah; receipt
snapshot belum terbentuk
Print failure tidak membatalkan
sale final yang sudah sah.
Cash control
shift tidak aktif; reason code
kosong; limit over threshold
tanpa approval
Expected drawer balance harus
visible.
Close shift
ada pending transaction
terlarang policy; reconciliation
belum dilakukan; variance belum
approved; report belum
generated
Wizard berhenti di step blocker.
Close business day
masih ada shift terbuka; required
sync minimum belum valid;
critical blocker belum selesai
Tidak ada close parsial tanpa
state eksplisit.
## 10. Screen Map Updated
Launch & Access
- 
Splash / Bootstrap
- 
Terminal Bind / Device Setup
- 
Login
- 
PIN Setup
- 
Enable Biometric Prompt
- 
Access Denied / Offline Auth Decision
- 
Session Locked
Guided Operations
- 
Guided Operations Dashboard
- 
Readiness Detail

- 
Sync Status Detail
- 
Device Diagnostics
- 
Printer Diagnostics
Shift & Cash
- 
Open Business Day
- 
Start Shift Wizard
- 
Opening Cash Input
- 
Shift Conflict / Handover Approval
- 
Cash In/Out
- 
Safe Drop
- 
Cash Audit / Reconciliation
- 
Close Shift Wizard
Sales & Receipt
- 
Sales Home / Cart
- 
Product Search / Scan Result
- 
Member/Voucher
- 
Payment Method
- 
Payment Pending / Retry
- 
Receipt Preview
- 
Print / Share / Reprint
Inventory Light Ops
- 
Quick Stock Lookup
- 
Receiving Quick Flow
- 
Adjustment Request
- 
Cycle Count Partial
Closing & Control
- 
X/Z Report Preview
- 
End of Day Readiness
- 
Close Business Day
- 
Operational Summary
## 11. Cross-Platform UI Contract Ringkas
- 
Android POS / Tablet: adaptif dengan window size classes; checkout dan shift flow harus terasa
langsung dan cepat.
- 
Desktop Backoffice: sidebar + header utility + content pane; fokus keyboard, table density terkendali,
visible sync/reconcile state.
- 
iOS/surface supervisor selektif: gunakan sheet/picker/navigation chrome native, tanpa memaksa visual
Android 1:1.
- 
Yang harus identik: token meaning, severity language, field naming, critical CTA ordering, dan gate
semantics.
- 
Aksesibilitas minimum: contrast AA untuk teks normal, resize text sampai 200%, state tidak hanya
dibedakan dengan warna, dan target sentuh nyaman lintas platform.

## 12. Happy Path dan Worst-Case Journey Wajib
Happy Path Harian Ideal
1.
Preflight perangkat -> login / unlock -> open business day -> start shift -> opening cash -> readiness
valid -> sales berjalan.
2.
Bila perlu: cash movement / safe drop / reprint / quick stock lookup / receiving ringan.
3.
Stop new checkout -> reconcile cash -> review report -> close shift -> review readiness -> close
business day -> post-close summary.
Worst-Case yang Harus Tetap Terpandu
- 
Shift conflict right after login: dashboard langsung Blocked; guided next action = supervisor handling;
user tidak bisa loncat ke sales.
- 
Offline unlock denied: user blocked dengan copy eksplisit; diagnostics read-only boleh muncul jika
policy mengizinkan.
- 
Transaksi cash offline + printer gagal: sale tetap final lokal, receipt preview tetap tampil, print
retry/switch/share/pending visible.
- 
Approval attempted without valid local supervisor policy snapshot: hard stop, no bypass, user
diarahkan online/HQ-assisted route.
- 
Audit storage unavailable on sensitive flow: flow fail closed atau queued atomically sesuai policy; tidak
boleh silent success.
- 
Close shift gagal: wizard berhenti di pending/variance blocker; action korektif harus spesifik.
## 13. Acceptance Checklist untuk Review
- 
Guided Operations Dashboard benar-benar menjadi entry point utama, bukan sekadar home menu.
- 
Setup flow punya gate jelas hingga perangkat siap transaksi pertama.
- 
Login, business day, shift, pricing/master snapshot, dan terminal status diperlakukan sebagai anti-skip
gate nyata.
- 
Blocked, Warning, Danger, Success, Info, Pending Sync, Needs Approval, dan Offline Restricted
dibedakan secara stabil.
- 
State kritikal tidak hanya memakai toast singkat; ada persistent surface bila perlu.
- 
Receipt/print/reprint failure terlihat, dapat ditindaklanjuti, dan tidak merusak finality transaksi yang sah.
- 
Approval menyimpan reason/evidence; tidak ada bypass diam-diam.
- 
Sync backlog, conflict, dan offline window visible pada POS dan backoffice.
- 
Adaptive layout menjaga hierarchy yang sama walau pane berubah.
- 
Touch target, text scaling, dark mode, dan keyboard/focus pada desktop memenuhi baseline
acceptance.
## 14. Traceability Source Base
- 
E2E-STORE-OPERATION-+-UI-UX-FLOW-SCHEME---CASSY-POS-PHASE-1.txt
- 
cassy_pos_phase1_e2e_manual_setup_specification.pdf / Theming & Cross-Platform UI Contract v1.1
- 
CASSY-AUTH-STRATEGY---PHASE-1.txt
- 
PRINTING-MECHANISM-SCHEME---CASSY.txt
- 
Cassy_Event_Contract_Sync_Specification_v1.pdf
- 
store_pos_use_case_detail_specifications.pdf
- 
store_pos_sequence_detail_specifications.pdf
- 
store_pos_test_specification.pdf

- 
Cassy_Architecture_Specification_v1.pdf
Dokumen ini secara sengaja memprioritaskan correctness bisnis, traceability, dan
implementability. Jika ada konflik antara UI yang terlihat lebih "cantik" dengan gate
operasional yang benar, baseline ini memilih model bisnis yang benar.
## 15. External Validation Notes
- 
Adaptive layout mengikuti prinsip window size classes dan pane-based responsive layout agar
hierarchy tetap stabil pada compact, medium, dan expanded surfaces.
- 
Aksesibilitas minimum mengikuti contrast dan resize text baseline; state kritikal tidak boleh dibedakan
oleh warna saja.
- 
Reduced motion dan hit target perlu dihormati agar flow operasional tetap nyaman di bawah tekanan
kerja tinggi.
- 
Biometric tetap kontekstual: bukan ditanyakan saat first launch, melainkan setelah login online awal,
PIN setup selesai, dan enrollment aktif.


## Constraints / Policies
User tidak boleh diizinkan melewati prerequisite bisnis yang belum valid.

## Technical Notes
Dokumen ini cocok sebagai jembatan antara design system/UI contract dan operasional POS nyata.

## Dependencies / Related Documents
- `cassy_theming_ui_contract_phase_1.md`
- `cassy_pos_phase_1_e2e_manual_setup_specification.md`
- `cassy_auth_strategy_phase_1.md`
- `cassy_printing_mechanism_scheme.md`
- `cassy_event_contract_sync_specification_v1.md`
- `store_pos_test_specification.md`

## Risks / Gaps / Ambiguities
- Tidak ditemukan gap fatal saat ekstraksi. Tetap review ulang bagian tabel/angka jika dokumen ini akan dijadikan baseline implementasi final.

## Reviewer Notes
- Struktur telah dinormalisasi ke Markdown yang konsisten dan siap dipakai untuk design review / engineering handoff.
- Istilah utama dipertahankan sedekat mungkin dengan dokumen sumber untuk menjaga traceability lintas artefak.
- Bila ada konflik antar dokumen, prioritaskan source of truth, artefak yang paling preskriptif, dan baseline yang paling implementable.

## Source Mapping
- Original source: `cassy_e2e_store_operation_uiux_flow_scheme_v2.pdf` (PDF, 10 pages)
- Output markdown: `cassy_e2e_store_operation_uiux_flow_scheme_v2.md`
- Conversion approach: text extraction -> normalization -> structural cleanup -> standardized documentation wrapper.
- Note: beberapa tabel/list di PDF dapat mengalami wrapping antar baris; esensi dipertahankan, tetapi layout tabel asli tidak dipertahankan 1:1.
