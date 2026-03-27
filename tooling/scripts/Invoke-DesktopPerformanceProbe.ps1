param(
    [string]$OutputDirectory = "build/perf-probe"
)

$ErrorActionPreference = "Stop"

$repoRoot = (Get-Location).Path
$timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
$targetRoot = Join-Path (Join-Path $repoRoot $OutputDirectory) $timestamp
New-Item -ItemType Directory -Path $targetRoot -Force | Out-Null
$summaryPath = Join-Path $targetRoot "perf-summary.txt"

$probe = Measure-Command {
    & .\gradlew.bat :shared:sales:desktopTest --tests "id.azureenterprise.cassy.sales.application.SalesOperationalProbeTest" --no-daemon
    if ($LASTEXITCODE -ne 0) {
        throw "Gradle performance probe gagal"
    }
}

$summary = @(
    "generated_at=$(Get-Date -Format o)",
    "probe_test=id.azureenterprise.cassy.sales.application.SalesOperationalProbeTest",
    "wall_clock_ms=$([int]$probe.TotalMilliseconds)"
)
Set-Content -Path $summaryPath -Value $summary

Write-Output "CASSY_PERF_PROBE_OK path=$targetRoot duration_ms=$([int]$probe.TotalMilliseconds)"
