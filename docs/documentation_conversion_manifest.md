# Cassy Documentation Markdown Pack

## Document Overview
Index hasil konversi seluruh dokumen proyek ke Markdown yang dinormalisasi untuk design review, architecture review, engineering handoff, dan onboarding.

## Purpose
Memberi satu pintu masuk untuk seluruh artefak yang sudah dikonversi, beserta pemetaan hubungan antar dokumen dan catatan risiko penting.

## Scope
Seluruh 23 source document yang tersedia pada project aktif.

## Key Decisions / Core Rules
- Preserve meaning diutamakan di atas kosmetik format.
- Struktur Markdown dinormalisasi agar heading, section, dan penelusuran file lebih konsisten.
- Source of truth, artefak preskriptif, dan implementability menjadi prioritas saat ada konflik.

## Detailed Content

### Inventory
- `uml_modeling_source_of_truth.md` - Normative baseline untuk alur SDLC, prinsip modeling, aturan UML/PlantUML, dan guardrail traceability lintas artefak.
- `store_pos_use_case_detail_specifications.md` - Baseline use case tekstual untuk 39 use case Store/POS System, lengkap dengan tujuan bisnis, precondition, trigger, main flow, alternate flow, exception flow, dan business rules.
- `store_pos_activity_detail_specifications.md` - Spesifikasi operasional untuk 39 activity diagram yang menurunkan flow dari katalog use case.
- `store_pos_sequence_detail_specifications.md` - Spesifikasi interaksi lintas actor, UI, application/control, repository, database, dan external system untuk 39 use case.
- `store_pos_domain_model_detail_specifications_v2.md` - Baseline domain model yang memetakan bounded context, aggregate root, entity, value object, lifecycle, invariant, dan supporting object.
- `store_pos_erd_specification_v2.md` - Target-state ERD/database specification yang menutup gap schema lama dan menyelaraskan persistence model lintas domain bisnis.
- `cassy_architecture_specification_v1.md` - Prescriptive target architecture untuk KMP client, local-first SQLite, Go/PostgreSQL HQ backend, dan bounded-context modularization.
- `traceability_matrix_store_pos.md` - Matrix QA yang memetakan jejak end-to-end dari use case hingga domain, architecture, database, implementation baseline, dan test baseline.
- `store_pos_test_specification.md` - QA engineering baseline yang menurunkan strategi test, coverage, critical flow, evidence, dan data verification dari artefak desain.
- `cassy_module_project_structure_specification.md` - Implementation baseline untuk struktur monorepo, module boundary, hybrid shared UI, DI, naming, dan staged restructuring.
- `cassy_cicd_pipeline_strategy_v1.md` - Prescriptive baseline untuk GitHub Actions, quality gates, packaging, staged deployment, runner matrix, dan governance.
- `cassy_migration_script_specification.md` - Migration baseline untuk SQLite/SQLDelight local clients dan PostgreSQL HQ backend, dengan wave plan, verification, dan rollback guidance.
- `cassy_test_automation_specification.md` - Prescriptive automation baseline untuk shared core, Android POS, Desktop Backoffice, dan HQ Backend.
- `cassy_event_contract_sync_specification_v1.md` - Detailed sync event contract untuk outbox, batch, item, conflict, offline window, dan master snapshot.
- `cassy_barcode_schema_final_implementable_baseline.md` - Prescriptive baseline untuk barcode sebagai bagian resmi master data produk/SKU dan scanner flow local-first.
- `cassy_asset_resolver_scheme.md` - Baseline asset handling untuk image/file/evidence dengan mode hybrid local temp → background upload → cloud promotion.
- `cassy_branding_document.md` - Brand strategy dan copywriting foundation untuk positioning publik Cassy sebagai aplikasi operasional bisnis / retail operating system.
- `cassy_printing_mechanism_scheme.md` - Prescriptive printing baseline untuk thermal receipt POS, invoice A4/PDF, reprint, dan printer orchestration.
- `cassy_auth_strategy_phase_1.md` - Prescriptive auth/session baseline untuk shared POS device dengan personal identity, fast unlock, offline fallback, dan approval-heavy posture.
- `cassy_theming_ui_contract_phase_1.md` - Source of truth UI/theming v1.0 untuk branding, color system, typography, layout, responsiveness, motion, accessibility, dan native adaptation.
- `cassy_pos_phase_1_e2e_manual_setup_specification.md` - File bernama manual setup specification, tetapi isi yang diekstrak merupakan versi hardened/finalized dari Theming & Cross-Platform UI Contract Phase 1 (v1.1) yang mencakup setup, readiness, auth/session, printing, asset handling, dan acceptance criteria.
- `cassy_e2e_store_operation_uiux_flow_scheme_v2.md` - Updated integrated baseline untuk flow operasional harian, guided dashboard, setup gate, readiness state, approval, printing, offline/sync visibility, dan acceptance criteria.
- `cassy_repository_audit_production_roadmap_2026_03_11.md` - Audit brutal terhadap snapshot repository terbaru dan roadmap produksi yang memisahkan kondisi repo saat ini dari target architecture.

### Cross-Document Relationship Map
- SDLC backbone: `uml_modeling_source_of_truth.md` -> use case -> activity -> sequence -> domain -> architecture -> database -> implementation-oriented baselines -> test/automation.
- Core POS analysis set: use case, activity, sequence, domain model, ERD, traceability matrix, and test specification.
- Implementation set: architecture, module structure, migration, CI/CD, test automation, repository audit.
- Supporting capability set: sync event contract, auth strategy, printing, asset resolver, barcode baseline.
- Experience and brand set: branding, theming v1.0, hardened UI contract (stored in the manual-setup-named file), and E2E operation/UIUX flow.

### Notable Issues
- `cassy_migration_script_specification.md`: Dokumen menyatakan lampiran draft SQL perlu direkonsiliasi dengan implementation snapshot aktual sebelum dieksekusi.
- `cassy_pos_phase_1_e2e_manual_setup_specification.md`: Filename/content mismatch terdeteksi: nama file menyebut manual setup specification, tetapi isi yang terbaca adalah Theming & Cross-Platform UI Contract Phase 1 v1.1 / final hardened baseline.
- `cassy_repository_audit_production_roadmap_2026_03_11.md`: Dokumen ini bersifat audit terhadap snapshot repository pada 2026-03-11; prioritaskan source-of-truth artefak desain jika terjadi konflik.

## Constraints / Policies
- Markdown output tidak mengklaim mempertahankan layout visual PDF 1:1; fokusnya adalah structure, readability, dan implementability.
- Untuk bagian tabular yang padat, wrapping antar baris dapat berubah, tetapi esensi konten dipertahankan.

## Technical Notes
- Heading dan bullet dinormalisasi dengan gaya Markdown yang konsisten agar mudah dipakai di repository/docs tooling, selaras dengan CommonMark/GitHub Flavored Markdown practices.
- Beberapa file preskriptif lebih baru daripada baseline sebelumnya; ketika ada overlap, versi yang lebih preskriptif/finalized perlu diprioritaskan dalam review.

## Dependencies / Related Documents
- Semua file dalam paket ini saling terkait; lihat section Dependencies / Related Documents pada masing-masing file untuk detail per-dokumen.

## Risks / Gaps / Ambiguities
- `cassy_migration_script_specification.md`: Dokumen menyatakan lampiran draft SQL perlu direkonsiliasi dengan implementation snapshot aktual sebelum dieksekusi.
- `cassy_pos_phase_1_e2e_manual_setup_specification.md`: Filename/content mismatch terdeteksi: nama file menyebut manual setup specification, tetapi isi yang terbaca adalah Theming & Cross-Platform UI Contract Phase 1 v1.1 / final hardened baseline.
- `cassy_repository_audit_production_roadmap_2026_03_11.md`: Dokumen ini bersifat audit terhadap snapshot repository pada 2026-03-11; prioritaskan source-of-truth artefak desain jika terjadi konflik.

## Reviewer Notes
- Paket ini cocok sebagai baseline docs migration ke repository `docs/` atau knowledge base internal.
- Sebelum dipakai sebagai execution source yang mengubah code/schema, tetap lakukan review manusia pada dokumen migration, ERD, dan contract yang paling kritikal.

## Source Mapping
- Source count: 23 documents
- Conversion output folder: `cassy_markdown_docs/`
- Final package: `cassy_markdown_docs.zip`
