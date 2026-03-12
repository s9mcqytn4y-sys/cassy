# Cassy Theming and UI Contract Phase 1

## Document Overview
Source of truth UI/theming v1.0 untuk branding, color system, typography, layout, responsiveness, motion, accessibility, dan native adaptation.

## Purpose
Memberi baseline design system dan UI contract yang konsisten lintas Android POS, Desktop Backoffice, dan supervisor flow selektif.

## Scope
Brand direction, visual character, tokens, semantics, layout, component contract, dark mode, logo provisional, accessibility, dan governance.

## Key Decisions / Core Rules
Teal adalah primary color; gaya visual operational modern; no neumorphism; semantic consistency dengan adaptasi visual native.

## Detailed Content

### Normalized Source Body
CASSY
Theming & Cross-Platform UI Contract
Phase 1 · Retail POS / Backoffice / Supervisor Flows
Dokumen source of truth untuk branding, theming, layout, komponen, responsivitas, motion, dark mode, dan
aturan adaptasi native UI.
Versi
### 1.0 · 10 Maret 2026
Status: Ready for design review, engineering handoff, dan QA baseline.
Keputusan kunci yang dikunci: Teal tetap menjadi primary color; visual diarahkan ke operational modern; tanpa
neumorphism; semantic consistency, visual adaptation native.
Disusun dari artefak proyek Cassy yang relevan: branding foundation, E2E store operation flow, architecture baseline, module
structure, auth strategy, dan test baseline.

## 1. Ringkasan Eksekutif
Dokumen ini menetapkan kontrak desain yang mengikat lintas Android POS, Desktop Backoffice, dan surface
supervisor/mobile yang sangat selektif pada Phase 1. Fokus utamanya bukan membuat UI terlihat seragam
secara kaku, tetapi memastikan makna visual, aturan state, hierarchy, dan perilaku komponen tetap konsisten
walau dirender dengan idiom native tiap platform.
Keputusan final untuk Phase 1:
- Primary color tetap Teal; Sky/Azure hanya dipakai sebagai secondary accent dan support surfaces, bukan
warna inti brand.
- Gaya visual adalah operational modern: bersih, terstruktur, cepat dibaca, dan tenang saat dipakai pada
flow operasional yang padat.
- Neumorphism tidak dipakai. Affordance dibuat jelas melalui color, spacing, border, elevation ringan, dan
state semantics.
- Native adaptation diutamakan: token dan semantics konsisten, tetapi bentuk detail mengikuti platform.
- Dark mode harus balance: tetap jelas untuk flow kasir, approval, offline, sync, payment, dan error kritikal.
- Kontrak ini memasukkan foundation token, widget/komponen statis dan dinamis, responsive rules, motion,
iconography, logo provisional, dan governance.
Non-goal penting:
- Dokumen ini tidak memaksa pixel-perfect parity antar platform.
- Dokumen ini belum mengunci final logo artwork; yang dikunci adalah aturan penggunaan logo provisional.
- Dokumen ini bukan library implementation detail, tetapi cukup preskriptif untuk dijadikan baseline design
system dan handoff engineering.
## 2. Prinsip Desain yang Mengikat
No
Prinsip
Implikasi Praktis
Operational clarity first
Setiap surface harus
mempermudah operator membaca
status, readiness, dan next action
dengan cepat.
Hierarchy over decoration
Visual emphasis datang dari
struktur, contrast, dan spacing,
bukan efek dekoratif.
State must be visible
Offline, pending sync, approval
required, blocked, success, dan
failure tidak boleh samar.
Native where it matters
Gunakan idiom native untuk
kontrol dasar, navigasi, dan
gestures bila itu meningkatkan
learnability.
One meaning, one pattern
Makna visual untuk severity, CTA,
container, dan feedback tidak
boleh berubah antar modul.
Fast scanning under pressure
Dashboard, POS cart, payment,
dan shift flows harus bisa dipindai
dalam hitungan detik.
Consistency with controlled
flexibility
Shared tokens dan component
contracts wajib sama; visual detail
boleh beradaptasi per platform.
Accessibility is default
Contrast, focus visibility, target
size, motion reduction, dan text
scaling adalah baseline, bukan

afterthought.
Audit-friendly interaction
Flow kritikal harus memberikan
feedback yang jelas, dapat
ditelusuri, dan tidak
menyembunyikan exception.
## 3. Arah Brand & Karakter Visual
Brand Cassy tetap diposisikan sebagai aplikasi operasional bisnis dengan inti retail operating system yang local-
first, audit-heavy, dan sync-explicit. Karena itu karakter visual yang dipilih untuk Phase 1 adalah operational
modern: profesional, ramah, tidak ramai, dan tidak terlihat seperti aplikasi kasir generik yang playful.
Karakter yang harus terasa pada UI:
- Cepat: CTA utama, angka, harga, total, dan status penting harus dominan.
- Rapi: grid, alignment, divider, dan spacing harus disiplin.
- Terkendali: tidak ada state samar atau animasi yang membuat operator ragu.
- Modern: bersih, rounded secukupnya, elevation ringan, icon native-first, dan whitespace cukup.
- Ramah: bahasa mikrocopy ringkas dan tidak menghakimi.
## 4. Color System & Semantic States
Aturan inti: Teal adalah warna primary brand dan primary product accent. Azure/Sky blue hanya dipakai sebagai
support accent untuk info surfaces, analytics, atau ilustrasi ringan. Gray scale dipakai untuk struktur dan density
management.
Token
Hex
Penggunaan
Primary 700
#0B5350
Primary pressed / emphasis kuat /
app bar kontras
Primary 600
#0F7672
Primary default / CTA utama /
active state
Primary 500
#159A95
Hover / selected support
Primary 100
#D9EFED
Surface tint / chips / highlighted
neutral-positive
Secondary Sky 500
#67B8F7
Support accent / info-adjacent
surfaces
Neutral 900
#1F2937
Judul, teks utama, angka prioritas
tinggi
Neutral 600
#6B7280
Teks sekunder / metadata
Neutral 100
#F7FAFC
Background ringan / grouped
surfaces
Semantic state tokens wajib:
State
Hex
Makna
Komponen utama
Info
#0E74AF
Contextual information,
helper status, progress
info
Badge, banner, dialog,
icon, inline status,
timeline/event
Success
#16A34A
Final success, saved,
synced, approved
Badge, banner, dialog,
icon, inline status,
timeline/event
Warning
#D97706
Attention, threshold
breach, retry advisable
Badge, banner, dialog,
icon, inline status,
timeline/event
Danger
#DC2626
Failure, destructive
action, hard error
Badge, banner, dialog,
icon, inline status,
timeline/event

Blocked
#4B5563
Action unavailable
karena policy/readiness
Badge, banner, dialog,
icon, inline status,
timeline/event
Pending Sync
#7C3AED
Local commit berhasil
tetapi belum tersinkron
final
Badge, banner, dialog,
icon, inline status,
timeline/event
Rules:
- Jangan memakai primary teal untuk error state agar severity tidak ambigu.
- Gunakan tinted surface + icon + text label; jangan mengandalkan warna polos tanpa label.
- Pending Sync harus dibedakan jelas dari Success dan Warning.
- Blocked adalah state kebijakan/readiness, bukan error teknis.
## 5. Typography Hierarchy
Font recommendation Phase 1: Plus Jakarta Sans untuk display/headline marketing-level in-app title, dan Inter
untuk UI/body. Bila implementasi native membatasi, gunakan fallback sistem dengan metrik yang dekat dan
pertahankan hierarchy serta weight mapping.
Level
Style
Ukuran
Weight
Penggunaan
Display L
Plus Jakarta Sans
28/34
Bold
Screen title utama /
empty state title
Heading 1
Plus Jakarta Sans
22/28
Bold
Halaman utama /
section dominan
Heading 2
Inter
18/24
SemiBold
Section header /
panel title
Heading 3
Inter
16/22
SemiBold
Card title / form
subsection
Body M
Inter
14/20
Regular
Teks umum
Body S
Inter
12/18
Regular
Meta info /
supporting text
Label
Inter
12/16
Medium
Field label / chip /
tab label
Number XL
Inter
24/28
Bold
Total payment / KPI
penting
Type rules:
- Gunakan maksimal 3 level heading per screen agar scan tetap cepat.
- Hindari long paragraph pada layar operasional; pecah ke bullet, helper line, atau grouped sections.
- Angka kritikal seperti total, variance, pending count, dan item count mendapat treatment numerik yang
lebih kuat.
## 6. Layout Hierarchy, Grid, Spacing, Sizing
Foundation spacing memakai skala 4pt dengan token utama 4, 8, 12, 16, 20, 24, 32, 40, 48, 64. Radius
direkomendasikan 8 untuk controls, 12 untuk cards/panels, dan 16 untuk prominent containers. Elevation ringan,
bukan shadow tebal.
Device class
Viewport target
Grid
Density
Use case utama
Phone supervisor
360-430dp
4 col / fluid
Comfortable
Approval, monitor,
inventory assist
Tablet POS
768-1024dp
8 col
Compact-
comfortable
Checkout, cart,
payment, shift
Desktop backoffice
1280px+
12 col
Comfortable
Reporting,
reconcile, close
day, settings

Kiosk/large POS
1024px+ fixed
regions
8-12 col hybrid
Compact
High-throughput
cashier setup
Layout contract:
- Header > content > footer/utility regions harus jelas; jangan campur action bar dengan content tanpa
pemisah.
- Container utama wajib memiliki padding minimum 16 pada phone, 20 pada tablet, 24 pada desktop.
- Divider digunakan untuk memisahkan kelompok informasi; hindari border penuh pada semua sisi jika
spacing sudah cukup.
- Flex layout diutamakan untuk adaptive regions: cart summary, payment breakdown, status rail, quick
actions.
- Jangan membuat page terlalu penuh. Bila informasi padat, gunakan panel bertingkat atau progressive
disclosure.
## 7. Navigation Structure & Screen Anatomy
Entry point utama tetap Guided Operations Dashboard. Bukan home bebas. Setiap device class punya shell
berbeda, tetapi readiness, alerts, blockers, dan next recommended action harus tampil pada fold pertama.
Surface
Primary nav
Secondary nav
Catatan
Tablet POS
Contextual bottom nav
atau rail ringan
In-flow step / tabs
terbatas
Harus mendukung flow
transaksi cepat
Desktop backoffice
Sidebar + top utility bar
Tab/segmented / filter
bar
Cocok untuk data-heavy
tasks
Phone supervisor
Bottom nav minimal /
stacked list
Contextual actions
Jangan padat; fokus
approval/monitor
Screen anatomy minimum:
- Header: title, store/terminal context, connectivity/auth state, contextual action.
- Body: task-first composition, grouped panels, critical status above fold.
- Footer/utility: summary, sticky CTA, or device-specific utilities bila dibutuhkan.
- Critical flows memakai stepper ringan atau wizard cepat; operator tidak boleh kehilangan sense of
progress.
## 8. Widget & Component Contract
Komponen dibagi dua: foundation components dan operational components. Semua komponen harus punya
definisi visual, semantic states, size variants, density rule, dan motion behavior.
Foundation components
- Buttons: primary, secondary, tertiary, destructive, blocked, loading.
- Inputs: text field, number field, search, selector, date/time, PIN input.
- Containers: card, panel, sheet, dialog, drawer, modal confirmation.
- Data views: list item, data table, stat card, key-value block, timeline/event row.
- Feedback: toast, inline helper, snackbar, banner, skeleton, progress indicator.
- Navigation: app bar, tab, segmented control, sidebar item, bottom nav item.
Operational components
- Cart line item, order summary, payment method selector, cash keypad, total summary.
- Shift readiness card, opening cash card, close shift variance panel.
- Offline/pending sync banner, sync queue summary, retry sheet, conflict panel.

- Approval sheet, reason capture dialog, printer status card, receipt preview.
- Inventory quick adjust row, scan result state, barcode-not-found state.
Component rules yang wajib:
- CTA utama hanya satu per region agar decision path jelas.
- Blocked action tetap terlihat, tetapi dengan alasan yang bisa dipahami.
- Dialog kritikal wajib membedakan confirm, cancel, dan destructive secara visual maupun posisi.
- Data table desktop harus mendukung sticky header, zebra ringan opsional, dan numeric alignment kanan.
- List dan card tidak boleh punya visual weight yang sama jika prioritas informasinya berbeda.
## 9. Motion, Iconography, Ornament, Dark Mode
Motion bersifat fungsional. Tujuan utamanya memperjelas perpindahan state, progress, dan hasil aksi; bukan
dekorasi. Iconography mengikuti native/system icons semaksimal mungkin, lalu ditambah icon domain retail
yang benar-benar diperlukan.
Kategori
Durasi
Easing
Contoh
Micro feedback
120-160 ms
Standard out
Button press, chip select,
inline reveal
Panel transition
180-240 ms
Standard
Bottom sheet, side panel,
section expand
State transition
200-280 ms
Emphasized
Success to summary,
warning to resolve
Loading loop
Subtle / non-blocking
Linear
Sync progress, upload
queue
Dark mode rules:
- Gunakan neutral dark surfaces dengan primary teal yang sedikit diangkat saturasinya agar tetap terbaca.
- Jangan gunakan pure black pada surface utama; pilih dark gray berlapis agar depth tetap natural.
- Semantic colors diuji ulang terhadap contrast di dark mode; success/warning/danger tidak boleh saling
mirip.
- Cart total, pending sync, approval, dan blocked states harus tetap jelas pada kondisi low-light.
Ornamen & tren UI/UX yang dipilih:
- Accent surface ringan, rounded container moderat, border halus, dan shadow tipis.
- Use of spacious cards hanya bila membantu grouping; jangan jadikan semua elemen cardized.
- Ilustrasi dan empty state icon bersifat utilitarian, bukan decorative-heavy.
- Microcopy singkat, status chips, dan panel yang bisa dipindai cepat sesuai tren product UI saat ini.
## 10. Logo Application / Business (Provisional Contract)
Phase 1 belum mengunci artwork final logo. Namun penggunaan logo dan app mark tetap harus konsisten agar
semua surface terlihat satu keluarga.
Elemen
Aturan
Catatan
Wordmark
CASSY / Cassy sesuai konteks
produk
Gunakan bentuk sederhana,
tegas, tanpa efek gradient
berlebihan
App icon
Monogram C atau C-grid mark
berbasis geometri sederhana
Harus terbaca pada 24-64 px
Logo lockup
Mark + wordmark horizontal
Untuk splash, login,
documentation header
Clear space
Minimum setara tinggi huruf C
Tidak ditempel ke edge/container

Don't:
- Jangan pakai glow, bevel, glassmorphism berat, atau gradient agresif.
- Jangan membuat logo terlalu playful untuk konteks operasional.
- Jangan ubah warna logo seenaknya di atas semantic error/warning surfaces.
## 11. Accessibility, Responsiveness, dan QA Acceptance
- Minimum target sentuh 44x44 pt untuk mobile/tablet; region kritikal checkout boleh visualnya kecil tetapi
area sentuhnya tetap aman.
- Contrast teks dan komponen interaktif harus memenuhi baseline aksesibilitas; state tidak boleh dibedakan
oleh warna saja.
- Text scaling harus tetap memelihara hierarchy dan tidak memotong total, harga, atau badge kritikal.
- Reduced motion setting harus dihormati pada platform yang mendukung.
- Responsive acceptance wajib diuji pada tablet POS utama, desktop backoffice 1280+, dan phone
supervisor baseline.
Acceptance checklist handoff:
- Semua token warna, typography, radius, spacing, dan elevation tersedia sebagai design token semantic.
- Setiap komponen memiliki state matrix: default, hover/focus, pressed, disabled/blocked, loading,
success/error bila relevan.
- Semua critical flows mempunyai pattern yang konsisten untuk banner, dialog, inline status, dan CTA
placement.
- Dark mode, offline mode, pending sync, dan approval-required punya visual proof sebelum implementasi
final.
- Tidak ada screen yang mengandalkan dekorasi lebih kuat daripada hierarchy informasinya.
## 12. Governance & Implementation Notes
Kontrak ini harus diterjemahkan ke dalam design token lintas platform dan library komponen bertahap. Shared
UI diperbolehkan hanya untuk area yang aman; presentation detail yang device-heavy tetap native. Setiap
deviasi dari kontrak harus didokumentasikan dengan alasan platform, accessibility, atau kebutuhan operasional
yang sah.
Area
Decision
Owner
Catatan implementasi
Design tokens
Shared semantic tokens
Design + FE platform
Sumber kebenaran
tunggal
Native components
Adaptive implementation
Android/iOS/Desktop
engineers
Ikuti idiom platform
Operational patterns
Shared behavior contract
Product + Design + Eng
Tidak boleh drift per
modul
Future branding refresh
Allowed but versioned
Brand/PM
Tidak retroaktif tanpa
migration plan
Appendix A. Baseline Token Naming
- color.primary.default / strong / subtle
- color.surface.base / raised / sunken / inverse
- color.state.info | success | warning | danger | blocked | pendingSync
- type.display.l / heading.1 / body.m / label.s / number.xl
- space.4 / 8 / 12 / 16 / 20 / 24 / 32 / 40 / 48 / 64
- radius.8 / 12 / 16

- elevation.0 / 1 / 2 / 3
Appendix B. Final Design Decision Snapshot
Keputusan
Nilai final
Primary color
Teal tetap primary
Visual direction
Operational modern
Neumorphism
Tidak dipakai
Native adaptation
Semantic consistency, visual adaptation native
Typography
Plus Jakarta Sans + Inter
Density
Compact-comfortable hybrid
Layout strategy
Per device class
Navigation
Guided dashboard + contextual navigation
Dark mode
Balanced
Iconography
Native-first
Motion
Functional only
Logo
Provisional contract


## Constraints / Policies
Bukan pixel-perfect parity lintas platform; acceptance lebih fokus ke semantics dan hierarchy.

## Technical Notes
Versi 1.0 adalah baseline awal yang kemudian di-hardening oleh dokumen v1.1/final hardened baseline.

## Dependencies / Related Documents
- `cassy_branding_document.md`
- `cassy_architecture_specification_v1.md`
- `cassy_auth_strategy_phase_1.md`
- `cassy_e2e_store_operation_uiux_flow_scheme_v2.md`
- `cassy_pos_phase_1_e2e_manual_setup_specification.md`

## Risks / Gaps / Ambiguities
- Tidak ditemukan gap fatal saat ekstraksi. Tetap review ulang bagian tabel/angka jika dokumen ini akan dijadikan baseline implementasi final.

## Reviewer Notes
- Struktur telah dinormalisasi ke Markdown yang konsisten dan siap dipakai untuk design review / engineering handoff.
- Istilah utama dipertahankan sedekat mungkin dengan dokumen sumber untuk menjaga traceability lintas artefak.
- Bila ada konflik antar dokumen, prioritaskan source of truth, artefak yang paling preskriptif, dan baseline yang paling implementable.

## Source Mapping
- Original source: `cassy_theming_ui_contract_phase1.pdf` (PDF, 8 pages)
- Output markdown: `cassy_theming_ui_contract_phase_1.md`
- Conversion approach: text extraction -> normalization -> structural cleanup -> standardized documentation wrapper.
- Note: beberapa tabel/list di PDF dapat mengalami wrapping antar baris; esensi dipertahankan, tetapi layout tabel asli tidak dipertahankan 1:1.
