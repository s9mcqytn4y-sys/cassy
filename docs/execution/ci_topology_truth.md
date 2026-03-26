# CI Topology Truth

> Historical Note (2026-03-27)
> Dokumen ini adalah snapshot topology sebelum lane installer Windows menjadi scripted evidence.
> Authority terbaru untuk release truth: `docs/execution/r4_windows_release_trust.md`, `docs/execution/windows_desktop_runbook.md`, `docs/execution/roadmap_bridge.md`.

Dokumen ini mencatat topology workflow yang benar-benar dimaksud repo setelah hardening 2026-03-16.

## Lane aktif

- `PR Gate` di `.github/workflows/ci.yml`
  - trigger: `pull_request`, `workflow_dispatch`
  - tujuan: fast gate yang stabil untuk wrapper, shared-fast, android-fast, desktop-fast, lalu summary job
- `Mainline Evidence` di `.github/workflows/mainline.yml`
  - trigger: `push` ke `main/master`, `workflow_dispatch`
  - tujuan: packaging Windows, upload artifact, dan manifest evidence
- `Nightly Integrity` di `.github/workflows/nightly-integrity.yml`
  - trigger: schedule harian dan manual
  - tujuan: build penuh + subset migration verification yang jujur
- `Release Evidence` di `.github/workflows/release-evidence.yml`
  - trigger: manual
  - tujuan: manifest release evidence untuk artifact hosted terbaru

## Failure history yang masih relevan

- Hosted `PR Gate #15` pada 2026-03-16 gagal di workflow lama.
- Evidence yang terlihat saat audit:
  - Linux jobs gagal `exit code 126`
  - Windows package evidence gagal `exit code 1`
- Repo hardening yang diterapkan setelah itu:
  - `chmod +x ./gradlew` untuk runner Ubuntu
  - source smoke desktop dibuat headless dan bisa dijalankan lewat `:apps:desktop-pos:smokeRun` maupun `:apps:desktop-pos:run --args="--smoke-run"`
  - topology dipisah agar required checks tidak macet oleh packaging/release lane yang tidak relevan untuk PR
  - mainline dipersempit ke evidence Windows; Debian compatibility tidak lagi menjadi blocker jalur pilot Windows

## Required-check posture yang jujur

- Required check ideal untuk PR:
  - `validate-wrapper`
  - `shared-fast`
  - `android-pos-fast`
  - `desktop-fast`
  - `pr-gate-summary`
- Packaging Windows tidak dijadikan substitusi PR gate.
- Debian/Linux package tidak boleh dipakai sebagai bukti readiness Windows.

## Gap yang masih tersisa

- Hosted `Mainline Evidence` run `23142319550` untuk commit `a27ddc7` sudah sukses dan menghasilkan artifact Windows.
- Hosted rerun terbaru untuk workflow Windows tetap perlu dibuktikan; lane installer lokal sendiri sudah scripted dan tidak lagi manual-soft-blocker.
