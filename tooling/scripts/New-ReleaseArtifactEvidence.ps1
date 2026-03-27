param(
    [string]$OutputDirectory = "build/release-artifact-evidence",
    [string]$ExePath = "",
    [string]$MsiPath = "",
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

$resolvedExePath = if ($ExePath) {
    $ExePath
} else {
    Get-ChildItem -Path (Join-Path $repoRoot "apps/desktop-pos/build/compose/binaries/main/exe") -Filter "Cassy-*.exe" -File -ErrorAction SilentlyContinue |
        Sort-Object LastWriteTimeUtc -Descending |
        Select-Object -First 1 |
        ForEach-Object { $_.FullName.Substring($repoRoot.Length + 1) }
}
$resolvedMsiPath = if ($MsiPath) {
    $MsiPath
} else {
    Get-ChildItem -Path (Join-Path $repoRoot "apps/desktop-pos/build/compose/binaries/main/msi") -Filter "Cassy-*.msi" -File -ErrorAction SilentlyContinue |
        Sort-Object LastWriteTimeUtc -Descending |
        Select-Object -First 1 |
        ForEach-Object { $_.FullName.Substring($repoRoot.Length + 1) }
}

$artifactPaths = @($resolvedExePath, $resolvedMsiPath) | Where-Object { $_ }
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
