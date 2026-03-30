Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

. "$PSScriptRoot\env.ps1"

Write-Host "--- Cassy Dev Database Bootstrapper ---" -ForegroundColor Cyan

$dbPath = $env:CASSY_SQLITE_PATH
# Path to kernel schema
$sqlSchema = Join-Path $env:CASSY_REPO_ROOT "shared\kernel\src\commonMain\sqldelight\id\azureenterprise\cassy\kernel\db\KernelDatabase.sq"

if (Test-Path $dbPath) {
    Write-Host "Database already exists at $dbPath." -ForegroundColor Yellow
    $confirm = Read-Host "Overwrite? (y/N)"
    if ($confirm -ne "y") { Write-Host "Skipped."; exit 0 }
    Remove-Item $dbPath
}

Write-Host "Creating empty database at $dbPath..." -ForegroundColor Gray
New-Item -Path $dbPath -ItemType File | Out-Null

# Attempt to apply schema if sqlite3 exists
if (Get-Command "sqlite3" -ErrorAction SilentlyContinue) {
    Write-Host "sqlite3 found. Injecting schema from KernelDatabase.sq..." -ForegroundColor Gray
    # SQLDelight files contain both DDL and named queries.
    # For a quick bootstrap, we just pipe the whole file;
    # sqlite3 will ignore/error on named query syntax but should execute the CREATE TABLEs.
    Get-Content $sqlSchema | sqlite3 $dbPath
    Write-Host "Schema initialized (best-effort)." -ForegroundColor Green
} else {
    Write-Host "sqlite3 not found in PATH. Database created empty." -ForegroundColor Yellow
    Write-Host "Run a Gradle build/test to let SQLDelight populate the schema." -ForegroundColor Gray
}

Write-Host "Done." -ForegroundColor Green
