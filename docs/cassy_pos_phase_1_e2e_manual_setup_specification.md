# Cassy POS Phase 1 E2E Manual Setup Specification

## Document Overview
File bernama manual setup specification, tetapi isi yang diekstrak merupakan versi hardened/finalized dari Theming & Cross-Platform UI Contract Phase 1 (v1.1) yang mencakup setup, readiness, auth/session, printing, asset handling, dan acceptance criteria.

## Purpose
Menjadi source of truth theming dan UI lintas platform yang siap design review, engineering handoff, QA baseline, dan penurunan design tokens. Dokumen ini menghardening versi 1.0 dengan implikasi setup/manual onboarding, readiness gating, auth/session, printing, asset handling...

## Scope
Hardening UI contract, setup/onboarding, readiness & gating, auth/session, printing & assets, accessibility, adaptive layout, acceptance baseline.

## Key Decisions / Core Rules
Operational clarity first; setup adalah hard gate; readiness, blocked, pending sync, approval required, dan offline restricted harus terlihat jelas; kontrak UI harus audit-friendly.

## Detailed Content

### Normalized Source Body
CASSY
Theming & Cross-Platform UI Contract
Phase 1 · Final Hardened Baseline · Retail POS / Backoffice / Supervisor
Versi 1.1 · 10 Maret 2026
Tujuan dokumen
Menjadi source of truth theming dan UI lintas platform yang siap design review, engineering handoff, QA baseline, dan
penurunan design tokens. Dokumen ini menghardening versi 1.0 dengan implikasi setup/manual onboarding, readiness
gating, auth/session, printing, asset handling, sync visibility, dan acceptance criteria.
Keputusan inti
Baseline final
Warna utama
Teal tetap primary; Sky/Azure hanya secondary support
Gaya visual
Operational modern; bersih, cepat dibaca, tidak dekoratif
Arah platform
Semantic consistency, visual adaptation native
Density
Compact-comfortable hybrid sesuai konteks layar
Navigasi
Guided Operations Dashboard + contextual navigation
Dark mode
Balanced dark mode; state kritikal tetap jelas
Motion
Functional motion only; hormati reduced motion
Komponen
Foundation + core operational components + state-heavy
patterns
Status: finalized untuk Phase 1 dan diposisikan sebagai baseline preskriptif, bukan sekadar style guide visual.
## 1. Perubahan dari Versi 1.0
Versi 1.1 ini menutup gap yang sebelumnya masih terlalu implisit. Hardening difokuskan pada integrasi
dengan flow operasional nyata dan constraint arsitektur proyek.
Area
Perubahan hardening
Dampak handoff
Setup & onboarding
Menambahkan kontrak layout dan
komponen untuk setup gate, activation,
device registration, printer setup, product
setup, receipt customization, dan
operational readiness checklist.
UI awal tidak lagi ditafsirkan bebas oleh
tiap platform.
Readiness & gating
Menegaskan state Ready, Warning,
Blocked, Pending Sync, Approval
Required, dan Offline Restricted beserta
penggunaan visual dan CTA.
Dashboard dan preflight operasional jadi
konsisten.
Auth & session
Menautkan lock screen, unlock cepat,
step-up approval, terminal binding, dan
offline auth ke aturan visual serta
Flow sensitif tidak lagi hanya "diwarnai",
tetapi digate jelas.

hierarchy aksi.
Printing & assets
Menambahkan aturan khusus untuk
receipt preview, print failure, reprint,
logo/provisional asset, dan evidential
asset.
Engineering punya batas implementasi
yang lebih aman.
Accessibility & adaptive layout
Mengunci minimum contrast, hit target,
responsive panes, dan reduced motion
sebagai acceptance baseline.
Design QA lebih objektif dan lintas OS
lebih stabil.
QA governance
Menambahkan acceptance checklist dan
artifact trace section.
Dokumen siap dipakai sebagai control
point review.
## 2. Evidence Base & Design Stance
Dokumen ini mengikuti source of truth proyek: flow bisnis nyata lebih penting daripada preferensi visual. UI
tidak boleh mengaburkan gate operasional, auditability, local-first behavior, atau single-writer terminal.
Artefak
Kontribusi ke kontrak UI
Branding foundation
Menetapkan Cassy sebagai aplikasi operasional bisnis yang
retail-first, local-first, audit-heavy, dan modern.
E2E store operation flow
Menetapkan Guided Operations Dashboard, strict gate antar
flow, dan visibility exception.
Manual setup flow
Menetapkan setup gate sebelum transaksi pertama: activation,
device binding, store info, printer, produk awal, receipt
customization, readiness checklist.
Architecture
Menegaskan local-first, presentation native per platform, shared
core untuk business logic, dan sync visible sebagai subsistem
bisnis.
Auth strategy
Menetapkan personal identity di shared device, online-first with
offline fallback, step-up approval, dan terminal-scoped session.
Printing / Asset / Sync
Menuntut print state eksplisit, evidence non-destruktif untuk
asset sensitif, serta offline/sync state yang terlihat.
Traceability + Test baseline
Memaksa kontrak UI tetap testable: bukan hanya tampilan,
tetapi state dan edge case.
Design stance yang mengikat
Jangan memaksa keseragaman visual lintas OS sampai mengorbankan learnability native. Yang harus sama adalah
semantics, hierarchy, token meaning, component contract, severity language, dan acceptance criteria.
## 3. Prinsip Desain yang Mengikat
Prinsip
Implikasi preskriptif
Operational clarity first
Informasi readiness, blocker, total, status perangkat, dan next
action harus terbaca dalam hitungan detik.
Hierarchy over decoration
Penekanan visual datang dari layout, contrast, spacing, size,
dan semantic color; bukan efek dekoratif.

State must be visible
Offline, pending sync, approval required, blocked, success,
failure, dan warning tidak boleh samar.
Native where it matters
Kontrol dasar, navigation idiom, pointer/touch behavior,
keyboard, dan context menu mengikuti platform bila itu lebih
usable.
One meaning, one pattern
Makna visual untuk CTA, severity, and container state tidak
boleh berubah-ubah antar modul.
Fast scanning under pressure
Kasir, supervisor, dan backoffice harus bisa memindai angka,
status, dan blocker dengan cepat.
Accessibility is default
Kontras, focus visibility, hit target, text scaling, dan reduced
motion adalah baseline, bukan fitur tambahan.
Audit-friendly interaction
Flow kritikal harus meninggalkan feedback dan evidence yang
jelas, bukan silent failure.
Controlled flexibility
Token dan contract sama; per-platform detail boleh adaptif
selama makna tidak berubah.
## 4. Brand Direction & Visual Character
Karakter visual Cassy untuk Phase 1 adalah operational modern: profesional, ramah, tidak ramai, dan terasa
terkendali saat dipakai pada flow yang padat.
UI harus terasa cepat pada angka, harga, total, printer state, sync state, approval, dan readiness; bukan
playful atau ornamental.
Rounded corner dipakai secukupnya, elevation ringan, divider halus, whitespace disiplin, dan ikon native-first.
Tidak ada neumorphism.
## 5. Color System & Semantic States
Token
Hex
Penggunaan
Primary 700
#0B5350
Pressed state, app bar kontras, emphasis
kuat
Primary 600
#0F7672
CTA utama, active state, highlight utama
Primary 500
#159A95
Hover/selected support, chart support
terbatas
Primary 100
#D9EFED
Tinted surface, chip ringan, positive-
neutral highlight
Secondary Sky 500
#67B8F7
Info-adjacent surfaces, analytics accent,
ilustrasi support
Neutral 900
#1F2937
Judul, angka prioritas tinggi, teks utama
Neutral 600
#6B7280
Metadata, helper text, caption
Neutral 100
#F7FAFC
Background grouped surfaces dan
canvas ringan
State
Hex
Makna operasional
Info
#0E74AF
Informasi kontekstual, progress info,

health detail
Success
#16A34A
Saved, synced, approved, complete
Warning
#D97706
Threshold breach, retry advisable,
degraded but still operable
Danger
#DC2626
Failure, destructive action, hard error
Blocked
#4B5563
Action unavailable karena
policy/readiness belum terpenuhi
Pending Sync
#7C3AED
Local commit berhasil tetapi belum final
tersinkron
Aturan keras: jangan memakai teal primary untuk error; semua status harus memakai color + icon + label +
copy singkat. Pending Sync harus dibedakan jelas dari Success dan Warning.
## 6. Typography Hierarchy & Content Rhythm
Level
Ukuran / berat
Penggunaan
Display / screen title
28-32 semibold
Judul halaman utama, dashboard
headline, setup hero
Section title
22-24 semibold
Group heading utama per screen atau per
card cluster
Subsection title
18-20 semibold
Bagian dalam form, dialog besar, wizard
step heading
Body strong
15-16 medium
Label penting, card title, summary row
Body default
14-15 regular
Isi utama, input, list row, helper utama
Meta / caption
12-13 regular
Timestamp, state detail sekunder, badge
support
Numeric emphasis
20-28 semibold
Harga, total, varians, KPI, readiness
counters
Pasangan font baseline: Plus Jakarta Sans untuk display/headline dan Inter untuk UI/body. Bila platform
memaksa atau pipeline font tidak seragam, fallback ke system sans yang paling dekat secara metrik.
Body copy tidak boleh terlalu kecil pada surface operasional. Prioritaskan 14-15 untuk body default, bukan
12 sebagai baseline kasir.
Angka finansial dan status kritikal harus memakai tabular feel atau setidaknya konsisten secara spacing agar
mudah dipindai.
## 7. Spacing, Sizing, Grid, dan Layout Hierarchy
Sistem spacing memakai basis 4 pt, dengan ritme utama 4 / 8 / 12 / 16 / 20 / 24 / 32 / 40 / 48. Komponen
operasional tidak boleh menempel rapat; breathing room lebih penting daripada memaksa banyak informasi
dalam satu kartu.
Area
Aturan baseline
Compact mobile/POS
Gunakan container padding 16; antar section 16-24; antar form

row 12; target kepadatan tinggi tetapi tetap bisa dipindai.
Tablet POS
Gunakan content max width per pane; split pane diperbolehkan;
cart dan product pane tidak boleh full-bleed.
Desktop backoffice
Gunakan content containment; max width per content region;
hindari tabel dan form full-width tanpa alasan.
Card/container
Padding internal minimal 12 untuk padat, 16 untuk default, 20-
24 untuk summary surfaces.
Dividers
Gunakan divider halus antar section hanya bila membantu scan;
jangan membuat grid tebal seperti spreadsheet.
Responsive rule utama
Gunakan containment dan pane thinking. Konten boleh reflow, expand, collapse, atau pindah ke pane/overlay berbeda,
tetapi hierarchy dan semantic grouping harus tetap sama.
## 8. Adaptive Cross-Platform Rules
Platform
Aturan adaptasi
Android POS / Tablet
Gunakan idiom Material 3 / native Android untuk app bar, bottom
sheet, snackbar, segmented control, dan adaptive layout
berbasis window size. Checkout dan shift flow harus terasa
langsung dan cepat.
iOS / supervisor selective
Gunakan idiom iOS untuk navigation chrome, sheet, picker,
swipe action, dan target touch minimal; jangan memaksa visual
Android 1:1.
Desktop Backoffice
Gunakan sidebar + header utility + content pane. Prioritaskan
keyboard, table density terkendali, visible sync/reconcile state,
dan resizable content area.
Adaptive decisions dilakukan pada level layout shell dan pane composition, bukan dengan menggandakan
semantic pattern.
Tidak ada pixel-perfect parity. Yang harus identik adalah token meaning, severity language, field naming, and
critical CTA ordering.
## 9. Navigation, Header, Container, Footer, dan Divider Contract
Elemen
Kontrak preskriptif
Entry point utama
Guided Operations Dashboard. Bukan home bebas. User masuk
ke control tower operasional dengan readiness, blockers, dan
next action.
Header
Menampilkan context aktif: store, terminal, business day, shift,
auth/session, online/offline, dan utility action seperlunya.
Primary navigation
POS/tablet memakai contextual bottom/rail jika perlu; desktop
memakai sidebar + top utility. Critical flow memakai wizard atau
full-screen task flow.

Container
Container utama wajib punya peran jelas: summary, action
cluster, state card, data list, form group, audit evidence, atau
task pane.
Footer
Footer hanya dipakai untuk persistent summary/action yang
benar-benar bernilai: total, payment CTA, wizard progress, atau
desktop status rail.
Divider
Divider dipakai untuk memperjelas boundary. Jangan
memakainya sebagai dekorasi atau untuk mengganti spacing
yang buruk.
## 10. Guided Operations Dashboard Contract
Dashboard adalah control tower harian. Ia harus memusatkan business day, shift, printer/device health, sync
health, pending approval, pending exception, dan close blockers.
Section
Isi wajib
Today status
business day status, shift status, online/offline, printer status,
sync health, pending approval, pending exception, pending print
failure, pending close blockers
Readiness cards
Ready / Warning / Blocked untuk device, pricing snapshot,
scanner, printer, sync backlog, dan critical dependency lain
Primary next action
satu CTA utama yang paling relevan sesuai policy: Mulai Hari
Bisnis, Mulai Shift, Lanjut Jualan, Selesaikan Blocker,
Rekonsiliasi, atau Tutup Hari
Task clusters
sales quick entry, cash control, stock lookup, receiving ringan,
reprint, approval queue, health check, settings center
Aksi yang belum memenuhi prasyarat boleh terlihat sebagai read-only, tetapi tidak boleh commit operasi
kritikal.
## 11. Setup & Onboarding Contract (Hardening Baru)
Setup adalah flow bergate. Transaksi tidak boleh dimulai sebelum aktivasi, device binding, store info, printer
setup, product setup minimum, receipt customization minimum, dan readiness checklist selesai.
Tahap
Tujuan UI
Aturan visual
Welcome & introduction
Menjelaskan nilai produk tanpa bertele-
tele; CTA tunggal "Mulai Setup Toko".
Hero sederhana, benefit card ringkas,
tidak ada navigasi yang mengalihkan.
Activation & validation
Memasukkan activation/license code dan
menampilkan status validasi.
Field fokus tunggal, helper/error jelas,
state pending/failed/success terlihat.
Device registration
Menjelaskan device enrollment dan
binding secara ringkas.
Tampilkan device summary, bukan istilah
teknis yang berlebihan.
Store information
Form inti nama, alamat, telepon, logo
opsional, footer receipt opsional.
Gunakan progressive disclosure; file
asset opsional tidak boleh menghambat
field wajib.
Printer setup
Discovery, select, test print, fallback
guidance.
Health card dan test result harus terlihat
jelas; jangan sembunyikan failure di toast
singkat.

Product setup
Minimal 1-5 produk dengan field wajib
sederhana.
Form cepat; upload image/barcode
opsional tidak boleh memecah flow.
Receipt customization
Logo, footer, preview.
Hanya satu template phase 1; jangan beri
ilusi banyak pilihan.
Operational readiness
Checklist device aktif, info lengkap, printer
siap, produk tersedia.
Checklist bersifat gate final sebelum
"Mulai Jualan".
## 12. Readiness, Gate, dan Severity Language
Jenis gate
Makna
Pola UI
Hard block
Flow tidak boleh lanjut sama sekali.
Banner/inline block + CTA corrective +
state card blocked.
Read-only access
User boleh lihat konteks tapi tidak boleh
commit aksi.
Control disabled + reason text + optional
deep link ke prerequisite.
Approval gate
Flow boleh lanjut setelah
approval/reason/evidence sah.
Sheet/dialog step-up auth + reason field
wajib + explicit outcome.
Warning allowed
Flow masih boleh lanjut jika policy
mengizinkan.
Warning card + acknowledgment +
contextual CTA.
Pending sync
Commit lokal berhasil, finalisasi server
belum selesai.
Purple state badge/banner, action untuk
review atau retry bila relevan.
Blocked tidak sama dengan Danger. Blocked berarti policy/readiness belum memenuhi syarat; Danger berarti
error/failure/destructive state.
## 13. Core Component Contract
Komponen
Aturan inti phase 1
Buttons / CTA
Primary hanya satu per surface. Secondary/tertiary dipakai
untuk defer, detail, atau safe alternative. Destructive harus
memakai danger semantics.
Text field / input
Label selalu terlihat; error inline; helper jelas; jangan hanya
placeholder.
Cards
Gunakan untuk summary, status, actionable readiness, KPI, dan
grouped task. Jangan jadikan semua hal card.
Lists / rows
Support leading state/icon + title + meta + trailing action/status.
Row operasional harus mudah dipindai.
Tables
Desktop/backoffice boleh lebih padat; tetap jaga zebra ringan,
header sticky bila perlu, dan column semantics yang jelas.
Dialogs / sheets
Dipakai untuk confirmation, approval, resolve conflict, printer
action, payment follow-up. Bukan untuk form panjang.
Badges / chips
Dipakai untuk severity, sync state, device health, approval state,
bukan sebagai dekorasi kategori semata.
Toast / snackbar
Hanya untuk feedback singkat non-kritis. Error kritikal wajib
punya inline or persistent state.
Skeleton / loading
Hanya untuk loading transien. Setup gate, sync failure, atau auth

issue tidak boleh disamarkan sebagai loading berkepanjangan.
## 14. Domain-Specific UI Patterns yang Wajib
Pattern
Kontrak
POS cart
Header ringkas, line item mudah edit, subtotal/tax/discount jelas,
total dominan, payment CTA persistent.
Payment sheet
Metode pembayaran, amount due, status authorization, pending
investigation, retry/retry-safe action, dan fallback cash visible.
Printer health
Assigned printer, last success/failure, paper state, fallback print
route, dan test print action.
Sync visibility
Pending batch/item/conflict visible; jangan hanya icon kecil.
Desktop dan POS harus bisa membaca status akhir yang
operasional.
Approval sheet
Supervisor identity, method (PIN/biometric bila policy), reason
code, outcome, dan audit hint.
Exception reason capture
Reason code wajib saat policy mensyaratkan; free text opsional
sesuai risk.
Audit/evidence asset
Attachment sensitif bersifat append-only atau soft replace; UI
tidak boleh memberi ilusi overwrite destruktif.
## 15. Printing, Receipt, dan Asset Rules
Receipt preview, print execution, print failure, dan reprint adalah state bisnis yang terlihat. Jangan treat
printing sebagai efek samping tersembunyi.
Phase 1 hanya memiliki satu template receipt. UI tidak boleh membuka choice architecture palsu seolah ada
banyak template.
Printer abnormal tidak selalu memblok buka toko, tetapi harus memblok mode receipt-printed-required bila
policy mewajibkan.
Logo/store asset untuk branding receipt boleh bersifat soft replace. Evidence untuk approval, return, atau
audit harus append-only atau minimal soft replace dengan history. Tidak boleh hard overwrite senyap.
## 16. Motion & Animation Contract
Kategori motion
Baseline
Allowed
state transition singkat, progress feedback, expand/collapse
informatif, highlight success/failure, pane transition seperlunya
Avoid
parallax dekoratif, bounce berlebihan, spinning terus-menerus,
blur transisi berat, motion yang tidak menambah pemahaman
Reduced motion
Bila sistem meminta reduced motion, ganti transisi besar dengan
fade/highlight sederhana, kurangi auto-motion, dan hentikan
motion berulang yang tidak penting
Motion harus menjelaskan perubahan status atau konteks, bukan memamerkan gaya visual.

## 17. Dark Mode Contract
Dark mode harus balance, bukan inverse ekstrem. Surface kritikal harus tetap jelas, angka finansial tidak
boleh kehilangan kontras, dan semantic states tetap mudah dibedakan.
Primary teal tetap dipakai, tetapi saturasi dan luminance harus dikontrol agar tidak menyilaukan di latar gelap.
Pending Sync, Warning, dan Danger tidak boleh menyatu menjadi kumpulan warna neon yang sama kuat.
Gunakan tinted surface + icon + label.
## 18. Accessibility & Minimum Acceptance Baseline
Aturan
Baseline minimum
Kontras teks
Target minimum setara WCAG AA: 4.5:1 untuk teks normal dan
3:1 untuk teks besar.
Resize / scaling
Text dan layout harus tetap usable saat pembesaran sampai
200% pada konteks yang relevan; hindari clipped text.
Hit target
iOS minimal 44×44 pt; Android mengikuti ukuran sentuh yang
nyaman dan tidak rapat.
Differentiate without color
Severity/state kritikal wajib memakai label dan/atau icon; warna
saja tidak cukup.
Keyboard/focus desktop
Backoffice harus punya focus visibility jelas dan urutan tab yang
masuk akal.
Catatan implementasi
Kontrak aksesibilitas ini adalah baseline acceptance, bukan best-effort. Bila token warna yang dipilih gagal memenuhi
kontras pada surface tertentu, surface atau tint wajib diubah; jangan memaksa brand color di tempat yang salah.
## 19. Implementation Handoff Rules
Layer
Yang harus dikunci
Design token
color roles, typography roles, spacing scale, corner radius,
elevation levels, icon size, motion duration, semantic state roles
Component matrix
variant, slot, state, size, density, disabled/read-only/blocked
behavior, loading skeleton, error state, platform notes
Screen anatomy
dashboard, setup wizard, POS cart, payment, printer setup,
health check, approval sheet, sync reconcile summary
Do / do not
contoh penggunaan benar dan salah untuk state-heavy
surfaces, CTA ordering, divider usage, dan warning vs blocked
QA checklist
contrast, touch target, dark mode, offline state, pending sync,
printer failure, approval evidence, text scaling
Shared UI boleh dipakai hanya untuk concern presentasi yang stabil. Hardware integration, lifecycle,
permission, scanner, printer, payment terminal callback, dan biometrik tetap native/app shell.

## 20. Governance, Versioning, dan Out of Scope
Area
Keputusan
Owner dokumen
Product + Design System + Engineering lead wajib menyetujui
perubahan token/semantics.
Versioning
Perubahan minor untuk token non-breaking; perubahan mayor
bila semantic mapping, navigation contract, atau readiness
language berubah.
Out of scope phase 1
final logo artwork, motion branding ekspresif, multi-theme brand
suite, full F&B/service screen library, dan shared UI penuh lintas
semua concern.
Change control
Tidak boleh mengubah meaning severity atau gate language di
satu modul tanpa review lintas modul.
Appendix A. Acceptance Checklist untuk Review
- 
Teal tetap primary; sky/azure tidak mengambil alih semantic utama.
- 
Dashboard menjadi entry point utama dan menampilkan readiness yang operasional.
- 
Setup flow punya gate jelas sampai perangkat siap transaksi pertama.
- 
Blocked, Warning, Danger, Success, Info, dan Pending Sync dibedakan dengan pattern yang stabil.
- 
State kritikal tidak hanya memakai toast singkat; ada persistent surface bila perlu.
- 
Dark mode masih menjaga readability dan semantic distinction.
- 
Touch target dan text scaling lolos baseline aksesibilitas.
- 
Receipt/print/reprint failure terlihat dan dapat ditindaklanjuti.
- 
Approval flow menyimpan reason/evidence; UI tidak memberi bypass diam-diam.
- 
Desktop backoffice mendukung keyboard/focus dan reconcile visibility.
Appendix B. Referensi Normatif Eksternal
- 
W3C WCAG 2.2 untuk contrast minimum, resize text, dan prinsip aksesibilitas dasar.
- 
Android Developers / Material adaptive layout guidance untuk window size classes, containment, dan
pane-based responsive layout.
- 
Apple Human Interface Guidelines untuk aksesibilitas, hit target minimum 44×44 pt, dan reduced motion
behavior.


## Constraints / Policies
Karena ada mismatch nama file vs isi dokumen, gunakan isi dokumen sebagai sumber kebenaran konten dan simpan mismatch sebagai reviewer note.

## Technical Notes
Secara substantif dokumen ini tampak menjadi versi 1.1/final hardened baseline dari UI contract, bukan manual setup spec yang berdiri sendiri.

## Dependencies / Related Documents
- `cassy_theming_ui_contract_phase_1.md`
- `cassy_branding_document.md`
- `cassy_auth_strategy_phase_1.md`
- `cassy_printing_mechanism_scheme.md`
- `cassy_asset_resolver_scheme.md`
- `cassy_e2e_store_operation_uiux_flow_scheme_v2.md`

## Risks / Gaps / Ambiguities
- Filename/content mismatch terdeteksi: nama file menyebut manual setup specification, tetapi isi yang terbaca adalah Theming & Cross-Platform UI Contract Phase 1 v1.1 / final hardened baseline.

## Reviewer Notes
- Struktur telah dinormalisasi ke Markdown yang konsisten dan siap dipakai untuk design review / engineering handoff.
- Istilah utama dipertahankan sedekat mungkin dengan dokumen sumber untuk menjaga traceability lintas artefak.
- Bila ada konflik antar dokumen, prioritaskan source of truth, artefak yang paling preskriptif, dan baseline yang paling implementable.

## Source Mapping
- Original source: `cassy_pos_phase1_e2e_manual_setup_specification.pdf` (PDF, 10 pages)
- Output markdown: `cassy_pos_phase_1_e2e_manual_setup_specification.md`
- Conversion approach: text extraction -> normalization -> structural cleanup -> standardized documentation wrapper.
- Note: beberapa tabel/list di PDF dapat mengalami wrapping antar baris; esensi dipertahankan, tetapi layout tabel asli tidak dipertahankan 1:1.
