# Third-Party Notices Baseline

Updated: 2026-03-27

## FACT
- Daftar ini adalah baseline notice untuk dependency langsung yang tampak dari `gradle/libs.versions.toml`.
- Daftar ini belum dimaksudkan sebagai SBOM transitive lengkap.
- Lisensi final dan notice lengkap tetap mengikuti metadata upstream masing-masing komponen.

## Direct dependency baseline
- Kotlin / Kotlin Gradle Plugin / Kotlinx Coroutines / Kotlinx Serialization / Kotlinx Datetime
  - Upstream: JetBrains / Kotlin Foundation
- Compose Multiplatform
  - Upstream: JetBrains
- AndroidX libraries
  - Upstream: Google / Android Open Source Project
- Koin
  - Upstream: InsertKoin.io
- SQLDelight
  - Upstream: Cash App
- Detekt
  - Upstream: Detekt contributors
- Apache Commons CSV
  - Upstream: Apache Software Foundation
- JUnit 4
  - Upstream: JUnit contributors

## RECOMMENDATION
- Sebelum distribusi publik yang lebih luas, hasilkan inventory third-party yang lebih formal dari dependency graph final release.
- Jangan menghapus notice upstream yang ikut terbawa oleh artifact atau source dependency.
