# CI/CD & Build Pipeline [CURRENT]

Updated: 2026-03-27

Cassy menggunakan GitHub Actions untuk menjamin integritas kode, otomatisasi rilis, dan penyediaan bukti (*evidence*) rilis di seluruh modul Monorepo.

## 1. Core Workflows (Topology Truth)

| Workflow | Trigger | Tanggung Jawab |
| :--- | :--- | :--- |
| `ci.yml` (PR Gate) | Pull Request | **Fast Gate**: Validate wrapper, shared-fast, android-pos-fast, desktop-fast, lint (Detekt), dan summary job. |
| `mainline.yml` | Push to `main` | **Packaging Windows**: Membuat artifact EXE/MSI, upload artifact, dan manifest evidence. |
| `beta-release.yml`| Manual / Tag | Pembuatan artifact formal untuk pengujian lapangan. |
| `release-evidence.yml`| Manual | Mengumpulkan manifest rilis, checksum, dan log integrasi dari artifact hosted terbaru. |
| `nightly-integrity.yml`| Scheduled | Build penuh + verifikasi migrasi database jangka panjang. |

## 2. CI Policy & Hardening
- **Executable Wrapper**: Seluruh runner Ubuntu melakukan `chmod +x ./gradlew` sebelum eksekusi.
- **Headless Smoke**: Desktop smoke test dijalankan secara headless via `:apps:desktop-pos:smokeRun` atau `:apps:desktop-pos:run --args="--smoke-run"`.
- **Migration Verification**: Setiap file `.sqm` SQLDelight diverifikasi terhadap database kosong di CI.
- **Required Checks**: PR hanya dapat di-merge jika `pr-gate-summary` sukses. Packaging Windows tidak memblokir PR gate tetapi wajib sukses di Mainline.

## 3. Artifact & Distribution
- **Windows Primary**: Artifact utama adalah `MSI` dan `EXE` untuk pilot retail.
- **Debian/Linux**: Hanya sebagai compatibility artifact; bukan ukuran kesiapan rilis (*release truth*).
- **Versioning**: Mengikuti Semantic Versioning (SemVer) di `gradle.properties`.

## 4. Release Evidence Baseline
Setiap rilis formal menghasilkan bundle bukti di folder `build/release-diagnostics/` atau `build/installer-evidence/` yang berisi:
- Git Commit Hash & Build Log URL.
- SHA-256 Checksum untuk installer.
- Screenshot/Log dari `Invoke-WindowsInstallerEvidence.ps1`.

---
**Historical Note**: Per 2026-03-16, sistem dipisah antara *fast-check* untuk developer dan *heavy-packaging* untuk rilis guna mempercepat siklus feedback PR.
