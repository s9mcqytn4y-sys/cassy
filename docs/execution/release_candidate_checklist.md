# Release Candidate Checklist

Updated: 2026-03-27

Dokumen ini mendefinisikan arti `release candidate` untuk Cassy desktop V1.

## Gate otomatis wajib

```powershell
.\gradlew --version
.\gradlew clean
.\gradlew build
.\gradlew test
.\gradlew lint detekt
.\gradlew :shared:kernel:verifyCommonMainKernelDatabaseMigration :shared:inventory:verifyCommonMainInventoryDatabaseMigration :shared:masterdata:verifyCommonMainMasterDataDatabaseMigration :shared:sales:verifyCommonMainSalesDatabaseMigration
.\gradlew :apps:desktop-pos:createDistributable :apps:desktop-pos:packageExe :apps:desktop-pos:packageMsi :apps:desktop-pos:smokeRun
powershell -ExecutionPolicy Bypass -File tooling/scripts/Invoke-DesktopDistributionSmoke.ps1
powershell -ExecutionPolicy Bypass -File tooling/scripts/Invoke-WindowsInstallerEvidence.ps1
powershell -ExecutionPolicy Bypass -File tooling/scripts/Invoke-WindowsUpgradeEvidence.ps1
powershell -ExecutionPolicy Bypass -File tooling/scripts/Invoke-DesktopPerformanceProbe.ps1
powershell -ExecutionPolicy Bypass -File tooling/scripts/Collect-WindowsReleaseDiagnostics.ps1
```

## Gate manual wajib
- login operator dengan PIN valid dan invalid
- open business day
- start shift dengan opening cash
- sell item cash
- void completed cash sale dengan reason code
- lihat ringkasan harian dan shift summary
- export reporting bundle
- close shift / close day sesuai guardrail
- kirim diagnostics pack

## Release authority docs
- `README.md`
- `docs/execution/roadmap_bridge.md`
- `docs/execution/repo_reality_sync.md`
- `docs/execution/windows_desktop_runbook.md`
- `docs/execution/desktop_device_support_matrix.md`
- `docs/execution/beta_burn_in_checklist.md`
- `docs/execution/beta_release_readiness.md`
- `docs/execution/windows_code_signing_posture.md`
- `docs/user/operator_quickstart.md`
- `LICENSE`
- `EULA.md`
- `PRIVACY.md`
- `SECURITY.md`
- `THIRD_PARTY_NOTICES.md`

## Exit criteria
- tidak ada blocker P0/P1 aktif untuk desktop-first lane
- evidence lokal hijau
- evidence hosted latest tidak misleading
- docs authority sinkron dengan repo reality
- operator guide minimal siap dipakai pilot
- versioning beta dan changelog resmi siap
