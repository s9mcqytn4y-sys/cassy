param(
    [string]$DataRoot = "$HOME\.cassy",
    [string]$OutputDirectory = "build/release-recovery"
)

$ErrorActionPreference = "Stop"

if (-not (Test-Path $DataRoot)) {
    throw "Missing Cassy data root: $DataRoot"
}

$outputRoot = Join-Path (Get-Location) $OutputDirectory
$null = New-Item -ItemType Directory -Path $outputRoot -Force

$timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
$stagingRoot = Join-Path $outputRoot "cassy-state-$timestamp"
$zipPath = Join-Path $outputRoot "cassy-state-$timestamp.zip"
$null = New-Item -ItemType Directory -Path $stagingRoot -Force

$patterns = @("*.db", "*.db-wal", "*.db-shm")
$files = Get-ChildItem $DataRoot -File | Where-Object {
    $name = $_.Name
    $patterns | Where-Object { $name -like $_ }
}

if (-not $files) {
    throw "No Cassy database files found in $DataRoot"
}

foreach ($file in $files) {
    Copy-Item $file.FullName -Destination (Join-Path $stagingRoot $file.Name) -Force
}

$manifestPath = Join-Path $stagingRoot "backup-manifest.txt"
$manifestLines = @(
    "generated_at=$(Get-Date -Format o)",
    "source_data_root=$DataRoot",
    "host=$env:COMPUTERNAME",
    "user=$env:USERNAME",
    "files="
)
$manifestLines += $files | ForEach-Object { " - $($_.Name) size=$($_.Length) modified=$($_.LastWriteTime.ToString('o'))" }
Set-Content -Path $manifestPath -Value $manifestLines

if (Test-Path $zipPath) {
    Remove-Item $zipPath -Force
}
Compress-Archive -Path (Join-Path $stagingRoot '*') -DestinationPath $zipPath -Force
Remove-Item $stagingRoot -Recurse -Force

Write-Output "CASSY_STATE_BACKUP_OK path=$zipPath"
