param(
    [string]$OutputDirectory = "build/release-diagnostics",
    [string]$DataRoot = ""
)

$ErrorActionPreference = "Stop"

$repoRoot = (Get-Location).Path
$resolvedDataRoot = if ($DataRoot) {
    $DataRoot
} elseif ($env:CASSY_DATA_DIR) {
    $env:CASSY_DATA_DIR
} else {
    "$HOME\.cassy"
}
$timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
$outputRoot = Join-Path $repoRoot $OutputDirectory
$targetRoot = Join-Path $outputRoot $timestamp
$null = New-Item -ItemType Directory -Path $targetRoot -Force

$summaryPath = Join-Path $targetRoot "diagnostics-summary.txt"
$gradleVersionPath = Join-Path $targetRoot "gradle-version.txt"
$uninstallPath = Join-Path $targetRoot "uninstall-registry.txt"

$artifactExe = Get-ChildItem -Path (Join-Path $repoRoot "apps/desktop-pos/build/compose/binaries/main/exe") -Filter "Cassy-*.exe" -File -ErrorAction SilentlyContinue |
    Sort-Object LastWriteTimeUtc -Descending |
    Select-Object -First 1
$artifactMsi = Get-ChildItem -Path (Join-Path $repoRoot "apps/desktop-pos/build/compose/binaries/main/msi") -Filter "Cassy-*.msi" -File -ErrorAction SilentlyContinue |
    Sort-Object LastWriteTimeUtc -Descending |
    Select-Object -First 1

$artifactCandidates = @(
    $(if ($artifactExe) { $artifactExe.FullName.Substring($repoRoot.Length + 1) }),
    $(if ($artifactMsi) { $artifactMsi.FullName.Substring($repoRoot.Length + 1) }),
    "apps/desktop-pos/build/compose/binaries/main/app/Cassy/Cassy.exe",
    "apps/desktop-pos/build/compose/binaries/main/app/Cassy/app/Cassy.cfg",
    "apps/desktop-pos/build/compose/binaries/main/app/Cassy/runtime/release",
    "apps/desktop-pos/build/reports",
    "apps/desktop-pos/build/compose/logs",
    "build/installer-evidence",
    "build/release-artifact-evidence",
    "build/reports/problems/problems-report.html"
) | Where-Object { $_ }

$summary = New-Object System.Collections.Generic.List[string]
$summary.Add("generated_at=$(Get-Date -Format o)")
$summary.Add("host=$env:COMPUTERNAME")
$summary.Add("user=$env:USERNAME")
$summary.Add("os=$([System.Environment]::OSVersion.VersionString)")
$summary.Add("java_home=$env:JAVA_HOME")
$summary.Add("data_root=$resolvedDataRoot")
$summary.Add("runtime_log_locations=dedicated app log file not implemented; use installer logs, build/reports/problems/problems-report.html, apps/desktop-pos/build/compose/logs, and release-diagnostics outputs")
$summary.Add("artifacts=")

foreach ($candidate in $artifactCandidates) {
    $fullPath = Join-Path $repoRoot $candidate
    if (Test-Path $fullPath) {
        $item = Get-Item $fullPath
        $size = if ($item.PSIsContainer) { "dir" } else { $item.Length }
        $summary.Add(" - exists path=$candidate size=$size modified=$($item.LastWriteTime.ToString('o'))")
    } else {
        $summary.Add(" - missing path=$candidate")
    }
}

$summary.Add("data_files=")
if (Test-Path $resolvedDataRoot) {
    Get-ChildItem $resolvedDataRoot -File | ForEach-Object {
        $summary.Add(" - $($_.Name) size=$($_.Length) modified=$($_.LastWriteTime.ToString('o'))")
    }
} else {
    $summary.Add(" - missing data root")
}

$summary.Add("program_files_candidates=")
@(
    "$env:ProgramFiles\Cassy",
    "$env:LOCALAPPDATA\Programs\Cassy"
) | ForEach-Object {
    if (Test-Path $_) {
        $item = Get-Item $_
        $summary.Add(" - exists path=$_ modified=$($item.LastWriteTime.ToString('o'))")
    } else {
        $summary.Add(" - missing path=$_")
    }
}

Set-Content -Path $summaryPath -Value $summary

cmd /c gradlew.bat --version *> $gradleVersionPath

$registryEntries = @(
    @(Get-ItemProperty 'HKCU:\Software\Microsoft\Windows\CurrentVersion\Uninstall\*' -ErrorAction SilentlyContinue)
    @(Get-ItemProperty 'HKLM:\Software\Microsoft\Windows\CurrentVersion\Uninstall\*' -ErrorAction SilentlyContinue)
) | Where-Object { $_.DisplayName -like 'Cassy*' } |
    Select-Object PSPath, DisplayName, DisplayVersion, InstallLocation, UninstallString, QuietUninstallString

if ($registryEntries) {
    $registryEntries | Format-List | Out-String | Set-Content -Path $uninstallPath
} else {
    Set-Content -Path $uninstallPath -Value "No Cassy uninstall entries found."
}

Write-Output "CASSY_RELEASE_DIAGNOSTICS_OK path=$targetRoot"
