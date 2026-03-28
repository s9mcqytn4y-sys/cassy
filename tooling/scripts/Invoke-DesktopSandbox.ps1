param(
    [switch]$ResetDemo,
    [switch]$SmokeRun,
    [string]$DataRoot
)

$ErrorActionPreference = "Stop"

$repoRoot = Split-Path (Split-Path $PSScriptRoot -Parent) -Parent
$gradleWrapper = Join-Path $repoRoot "gradlew"

if ([string]::IsNullOrWhiteSpace($DataRoot)) {
    $DataRoot = Join-Path $repoRoot ".sandbox\desktop-dev"
}

New-Item -ItemType Directory -Path $DataRoot -Force | Out-Null

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
