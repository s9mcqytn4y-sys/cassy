param(
    [switch]$ResetDemo,
    [switch]$SmokeRun,
    [switch]$TruncateData,
    [string]$DataRoot
)

$ErrorActionPreference = "Stop"

$repoRoot = Split-Path (Split-Path $PSScriptRoot -Parent) -Parent
$gradleWrapper = Join-Path $repoRoot "gradlew"
$sandboxRoot = Join-Path $repoRoot ".sandbox"

if ([string]::IsNullOrWhiteSpace($DataRoot)) {
    $DataRoot = Join-Path $sandboxRoot "desktop-dev"
}

$sandboxRootFull = [System.IO.Path]::GetFullPath($sandboxRoot)
$dataRootFull = [System.IO.Path]::GetFullPath($DataRoot)

if (-not $dataRootFull.StartsWith($sandboxRootFull, [System.StringComparison]::OrdinalIgnoreCase)) {
    throw "DataRoot harus berada di bawah folder sandbox repo: $sandboxRootFull"
}

New-Item -ItemType Directory -Path $sandboxRoot -Force | Out-Null
New-Item -ItemType Directory -Path $DataRoot -Force | Out-Null

if ($TruncateData -and (Test-Path -LiteralPath $DataRoot)) {
    Get-ChildItem -LiteralPath $DataRoot -Force | Remove-Item -Recurse -Force
}

$previousDataRoot = $env:CASSY_DATA_DIR
$previousResetFlag = $env:CASSY_DEV_RESET_ENABLED

try {
    $env:CASSY_DATA_DIR = $DataRoot
    if ($ResetDemo) {
        $env:CASSY_DEV_RESET_ENABLED = "true"
    }

    $args = @(":apps:desktop-pos:run", "--no-configuration-cache", "--console=plain")
    if ($ResetDemo) {
        $args += "--args=--dev-reset-demo"
    } elseif ($SmokeRun) {
        $args += "--args=--smoke-run"
    }

    Write-Host "CASSY_SANDBOX_RUN dataRoot=$DataRoot resetDemo=$ResetDemo smokeRun=$SmokeRun truncateData=$TruncateData"
    & $gradleWrapper @args
    exit $LASTEXITCODE
} finally {
    if ($null -eq $previousDataRoot) {
        Remove-Item Env:CASSY_DATA_DIR -ErrorAction SilentlyContinue
    } else {
        $env:CASSY_DATA_DIR = $previousDataRoot
    }

    if ($null -eq $previousResetFlag) {
        Remove-Item Env:CASSY_DEV_RESET_ENABLED -ErrorAction SilentlyContinue
    } else {
        $env:CASSY_DEV_RESET_ENABLED = $previousResetFlag
    }
}
