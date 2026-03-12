# UML Modeling Source of Truth

## Document Overview
Normative baseline untuk alur SDLC, prinsip modeling, aturan UML/PlantUML, dan guardrail traceability lintas artefak.

## Purpose
Project ini digunakan untuk menghasilkan artefak analisis dan desain software yang: - mengikuti alur SDLC yang konsisten - menggunakan UML/PlantUML yang rapi dan mudah dipelihara - siap direview oleh stakeholder bisnis, architect, engineer, dan QA - mudah dipindahkan atau dire...

## Scope
4. Output .puml / model utama

## Key Decisions / Core Rules
Wajib mengikuti urutan Use Case → Activity → Sequence → Domain Model → Architecture → Database → Implementation → Test; bisnis nyata lebih penting daripada diagram kosmetik; traceability harus utuh lintas artefak.

## Detailed Content

### Normalized Source Body
## Tujuan
Project ini digunakan untuk menghasilkan artefak analisis dan desain software yang:
- mengikuti alur SDLC yang konsisten
- menggunakan UML/PlantUML yang rapi dan mudah dipelihara
- siap direview oleh stakeholder bisnis, architect, engineer, dan QA
- mudah dipindahkan atau direplikasi di Sparx Enterprise Architect
- mencerminkan flow bisnis nyata, bukan diagram generik

## Alur SDLC yang wajib diikuti
Semua artefak harus mengikuti urutan ini:

Use Case
-> Activity Diagram
-> Sequence Diagram
-> Domain Model
-> Architecture
-> Database
-> Implementation
-> Test

Aturan traceability:
- Use Case mendefinisikan scope, actor, dan tujuan bisnis
- Activity Diagram menurunkan flow proses dari use case
- Sequence Diagram menurunkan interaksi sistem dari flow
- Domain Model menurunkan entity dan relasi bisnis
- Architecture menurunkan struktur layer, boundary, dan dependency
- Database menurunkan model persistence dari domain
- Implementation menurunkan module, service, repository, UI, dan contract teknis
- Test menurunkan skenario verifikasi dari seluruh artefak sebelumnya

## Prinsip utama modeling
## 1. Selalu mulai dari bisnis nyata
- gunakan actor nyata
- gunakan tujuan bisnis nyata
- gunakan exception flow nyata
- gunakan boundary sistem yang jelas

## 2. Prioritaskan model yang benar secara bisnis
- jangan mengorbankan correctness demi diagram yang "cantik"
- jangan menulis use case seperti label tombol UI
- jangan membuat model terlalu abstrak hingga tidak berguna untuk engineering

## 3. Jaga konsistensi istilah
- actor naming konsisten
- use case naming konsisten
- entity naming konsisten
- layer naming konsisten
- database naming harus konsisten dengan domain, kecuali ada alasan teknis jelas

## 4. Hindari over-modeling
- cukup detail untuk design review dan implementation handoff
- jangan penuh noise
- jika diagram terlalu besar, pecah menjadi beberapa diagram fokus

## 5. Semua diagram harus bisa dipakai untuk:
- design review
- implementation handoff
- QA scenario derivation
- technical discussion
- architecture validation

## Aturan per artefak

### 1. Use Case Diagram
Gunakan untuk:
- mendefinisikan scope fungsional
- menunjukkan actor eksternal
- menunjukkan system boundary
- menunjukkan tujuan utama actor terhadap sistem

Aturan:
- actor berada di luar boundary
- use case berada di dalam boundary
- nama use case berbasis tujuan bisnis
- gunakan include hanya jika ada perilaku wajib yang direuse
- gunakan extend hanya jika benar-benar perilaku opsional/conditional
- jangan memasukkan detail teknis internal sistem

Best practice:
- gunakan grouping/package jika sistem besar
- pisahkan front office, back office, admin, inventory, approval, reporting, device integration, dll bila relevan
- gunakan orientasi yang memudahkan pembacaan

### 2. Use Case Specification
Setiap use case penting harus bisa diturunkan ke specification minimal:
- use case name
- goal
- primary actor
- precondition
- trigger
- main flow
- alternate flow
- exception flow
- postcondition
- business rules terkait

### 3. Activity Diagram
Gunakan untuk:
- memodelkan alur proses
- decision path
- alternate path
- exception path
- handoff antar actor atau sistem

Aturan:
- wajib punya start dan end/stop yang jelas
- gunakan decision yang eksplisit
- gunakan swimlane/partition bila tanggung jawab berbeda perlu dipisah
- jangan menjadikan activity diagram sebagai pseudo-sequence diagram
- fokus pada flow proses, bukan detail tampilan UI

Best practice:
- gunakan syntax PlantUML baru untuk Activity Diagram
- sederhanakan cabang yang berlebihan
- modelkan happy path dan failure path penting

### 4. Sequence Diagram
Gunakan untuk:
- menggambarkan interaksi antar actor, UI, application layer, domain/service, repository, database, dan external system
- memvalidasi urutan eksekusi
- memvalidasi boundary layer

Aturan:
- lifeline harus jelas
- jangan melompati layer arsitektur tanpa alasan
- gunakan alt, opt, loop, dan error path jika relevan
- tampilkan siapa memanggil siapa, dalam urutan yang realistis
- cocokkan dengan arsitektur target, bukan sequence teoritis

Best practice:
- actor -> UI -> facade/use case -> domain/service -> repository -> database/external system
- hindari mencampur semua concern dalam satu diagram besar
- buat sequence per use case utama atau per flow kritikal

### 5. Domain Model / Class Diagram
Gunakan untuk:
- memetakan entity bisnis
- value object
- aggregate
- relasi bisnis
- lifecycle penting

Aturan:
- prioritaskan bisnis, bukan UI
- bedakan domain entity vs DTO vs persistence model bila perlu
- tampilkan relasi hanya jika membantu kejelasan
- gunakan multiplicity bila relevan dan benar

Best practice:
- domain model harus bisa menjelaskan kenapa tabel/relasi database ada
- domain model harus selaras dengan use case dan sequence
- state penting bisa diturunkan ke state diagram jika perlu

### 6. Architecture Diagram
Gunakan untuk:
- menunjukkan layer
- boundary
- dependency rule
- integration point
- external system
- flow tanggung jawab besar

Aturan:
- pisahkan UI / App Shell / Application / Domain / Data / Database / External Service
- jangan mencampur sequence detail di diagram arsitektur
- gunakan boundary yang jelas
- tampilkan arah dependency, bukan sekadar daftar kotak

Best practice:
- architecture diagram harus bisa dipakai untuk codebase structuring
- ideal untuk menjelaskan facade/use case boundary dan sensitive flow boundary

### 7. Database / ERD / Data Model
Gunakan untuk:
- menunjukkan tabel/entitas persistence
- primary key
- foreign key
- constraint penting
- audit/history relation
- persistence structure untuk implementation

Aturan:
- turunkan dari domain model dan business rule
- jangan membuat tabel yang bertentangan dengan domain tanpa alasan kuat
- tampilkan relasi penting
- tampilkan audit/history bila sistem membutuhkannya

Best practice:
- jika ada status, audit log, movement ledger, snapshot, history, tampilkan hubungannya
- jelaskan aturan integritas penting

### 8. Test Mapping
Setiap flow penting harus punya turunan test:
- happy path
- alternate path
- failure path
- validation rule test
- guard/authorization test
- persistence integrity test bila relevan

Traceability minimal:
Use Case -> Activity -> Sequence -> Domain/Database -> Test

## Standar PlantUML (.puml)
Semua output PlantUML harus:
- menggunakan @startuml dan @enduml
- memiliki title yang jelas
- menggunakan indentasi rapi
- menggunakan alias yang konsisten
- menggunakan grouping yang jelas: package, rectangle, frame, box, partition, dll
- mudah dibaca di raw text
- mudah dipisah per file bila besar

Aturan format:
- nama file harus deskriptif
- satu file untuk satu konteks diagram yang fokus
- jangan membuat satu diagram raksasa untuk semua hal
- hindari trik visual yang sulit dipelihara
- hindari syntax eksperimental kecuali benar-benar diperlukan

Contoh penamaan file:
- retail_checkout_use_case.puml
- retail_checkout_activity.puml
- retail_checkout_sequence.puml
- retail_domain_model.puml
- retail_architecture_context.puml
- retail_inventory_erd.puml

## Standar kompatibilitas Enterprise Architect
Semua model harus tetap portabel ke Sparx Enterprise Architect.

Aturan:
- prioritaskan semantik UML standar
- gunakan actor, use case, system boundary, activity, decision, partition, lifeline, class/entity, component, package, dan relasi standar
- jangan terlalu bergantung pada gaya PlantUML yang hanya bagus di render tapi sulit direplikasi di EA
- gunakan naming stabil agar mudah dipetakan ulang ke elemen UML EA
- prioritaskan model yang bisa dibangun ulang di EA secara manual atau melalui pipeline impor yang wajar

## Struktur output yang diharapkan dari assistant
Saat diminta membuat artefak, hasil sebaiknya mengikuti pola ini:

## 1. Tujuan diagram / artefak
## 2. Asumsi penting
## 3. Boundary dan scope
## 4. Output .puml / model utama
## 5. Catatan best practice singkat
## 6. Jika relevan, turunan berikutnya dalam SDLC

## Gaya kerja yang diharapkan
- kritis terhadap requirement yang ambigu
- tidak menjadi yes-man
- berani mengoreksi model yang tidak realistis
- berani menyederhanakan diagram yang terlalu ramai
- selalu mengutamakan flow bisnis nyata
- selalu memikirkan implementability
- selalu menjaga traceability antar artefak

## Definition of Done untuk artefak modeling
Sebuah artefak dianggap baik jika:
- boundary jelas
- istilah konsisten
- flow bisnis masuk akal
- tidak over-modeled
- siap direview
- siap diturunkan ke implementasi
- dapat diturunkan ke test
- kompatibel secara konsep dengan Enterprise Architect
- output .puml rapi dan terstruktur

## Catatan referensi praktik
Project ini mengikuti prinsip umum dari:
- UML subject/system boundary untuk use case
- praktik PlantUML resmi untuk use case dan activity diagram
- preferensi syntax baru PlantUML untuk activity diagram
- interoperabilitas UML/XMI dan pendekatan model standar yang sesuai dengan Sparx Enterprise Architect

## Prioritas kualitas
Urutan prioritas saat ada trade-off:
## 1. correctness bisnis
## 2. clarity boundary
## 3. traceability
## 4. implementability
## 5. portability ke EA
## 6. visual neatness


## Constraints / Policies
Hindari over-modeling, jaga konsistensi istilah, prioritaskan boundary sistem dan correctness bisnis.

## Technical Notes
Dokumen ini bersifat normatif dan harus diprioritaskan ketika ada konflik terminologi atau urutan artefak.

## Dependencies / Related Documents
- `store_pos_use_case_detail_specifications.md`
- `store_pos_activity_detail_specifications.md`
- `store_pos_sequence_detail_specifications.md`
- `store_pos_domain_model_detail_specifications_v2.md`
- `cassy_architecture_specification_v1.md`
- `store_pos_erd_specification_v2.md`
- `store_pos_test_specification.md`
- `traceability_matrix_store_pos.md`

## Risks / Gaps / Ambiguities
- Tidak ditemukan gap fatal saat ekstraksi. Tetap review ulang bagian tabel/angka jika dokumen ini akan dijadikan baseline implementasi final.

## Reviewer Notes
- Struktur telah dinormalisasi ke Markdown yang konsisten dan siap dipakai untuk design review / engineering handoff.
- Istilah utama dipertahankan sedekat mungkin dengan dokumen sumber untuk menjaga traceability lintas artefak.
- Bila ada konflik antar dokumen, prioritaskan source of truth, artefak yang paling preskriptif, dan baseline yang paling implementable.

## Source Mapping
- Original source: `UML-Modeling-Source-of-Truth.txt` (TXT, 285 lines)
- Output markdown: `uml_modeling_source_of_truth.md`
- Conversion approach: text extraction -> normalization -> structural cleanup -> standardized documentation wrapper.
- Note: marker sitasi/annotation internal non-substantif dibersihkan agar hasil markdown lebih implementable.
