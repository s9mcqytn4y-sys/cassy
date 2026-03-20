param(
    [string]$OutputDirectory = "build/release-artifact-evidence",
    [string]$ExePath = "apps/desktop-pos/build/compose/binaries/main/exe/Cassy-0.1.0.exe",
    [string]$MsiPath = "apps/desktop-pos/build/compose/binaries/main/msi/Cassy-0.1.0.msi",
    [string]$AppRoot = "apps/desktop-pos/build/compose/binaries/main/app/Cassy"
)

$ErrorActionPreference = "Stop"

$repoRoot = (Get-Location).Path
$timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
$outputRoot = Join-Path $repoRoot $OutputDirectory
$targetRoot = Join-Path $outputRoot $timestamp
$null = New-Item -ItemType Directory -Path $targetRoot -Force

$manifestPath = Join-Path $targetRoot "release-manifest.txt"
$checksumsPath = Join-Path $targetRoot "checksums.sha256"
$commit = (git rev-parse HEAD).Trim()

$artifactPaths = @($ExePath, $MsiPath)
$manifest = New-Object System.Collections.Generic.List[string]
$checksums = New-Object System.Collections.Generic.List[string]

$manifest.Add("generated_at=$(Get-Date -Format o)")
$manifest.Add("commit=$commit")
$manifest.Add("java_home=$env:JAVA_HOME")
$manifest.Add("source_smoke=.\gradlew :apps:desktop-pos:smokeRun")
$manifest.Add("distribution_smoke=powershell -ExecutionPolicy Bypass -File tooling/scripts/Invoke-DesktopDistributionSmoke.ps1")
$manifest.Add("installer_evidence=powershell -ExecutionPolicy Bypass -File tooling/scripts/Invoke-WindowsInstallerEvidence.ps1")
$manifest.Add("diagnostics=powershell -ExecutionPolicy Bypass -File tooling/scripts/Collect-WindowsReleaseDiagnostics.ps1")
$manifest.Add("recovery=powershell -ExecutionPolicy Bypass -File tooling/scripts/Backup-CassyDesktopState.ps1")
$manifest.Add("runtime_log_locations=dedicated app log file not implemented; use installer logs, build/reports/problems/problems-report.html, apps/desktop-pos/build/compose/logs, and release-diagnostics outputs")
$manifest.Add("artifacts=")

foreach ($relativePath in $artifactPaths) {
    $fullPath = Join-Path $repoRoot $relativePath
    if (Test-Path $fullPath) {
        $item = Get-Item $fullPath
        $hash = (Get-FileHash $fullPath -Algorithm SHA256).Hash.ToLowerInvariant()
        $manifest.Add(" - path=$relativePath size=$($item.Length) sha256=$hash")
        $checksums.Add("$hash *$relativePath")
    } else {
        $manifest.Add(" - missing path=$relativePath")
    }
}

$appRootPath = Join-Path $repoRoot $AppRoot
if (Test-Path $appRootPath) {
    $manifest.Add(" - path=$AppRoot type=directory")
}

Set-Content -Path $manifestPath -Value $manifest
Set-Content -Path $checksumsPath -Value $checksums

Write-Output "CASSY_RELEASE_ARTIFACT_EVIDENCE_OK path=$targetRoot"
