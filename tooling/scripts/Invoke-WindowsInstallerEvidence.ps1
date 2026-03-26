param(
    [string]$MsiPath = "apps/desktop-pos/build/compose/binaries/main/msi/Cassy-0.1.0.msi",
    [string[]]$AppArgs = @("--smoke-run"),
    [string]$OutputDirectory = "build/installer-evidence"
)

$ErrorActionPreference = "Stop"

function Get-CassyUninstallEntry {
    return @(
        @(Get-ItemProperty 'HKCU:\Software\Microsoft\Windows\CurrentVersion\Uninstall\*' -ErrorAction SilentlyContinue)
        @(Get-ItemProperty 'HKLM:\Software\Microsoft\Windows\CurrentVersion\Uninstall\*' -ErrorAction SilentlyContinue)
    ) | Where-Object { $_.DisplayName -like 'Cassy*' } |
        Select-Object -First 1
}

function Resolve-ProductCode {
    param([object]$UninstallEntry)

    if ($UninstallEntry -and $UninstallEntry.PSChildName -match '^\{[A-F0-9-]+\}$') {
        return $UninstallEntry.PSChildName
    }

    if ($UninstallEntry -and $UninstallEntry.UninstallString -match '\{[A-F0-9-]+\}') {
        return $Matches[0]
    }

    throw "Unable to resolve Cassy MSI product code from uninstall entry."
}

function Invoke-MsiStep {
    param(
        [string]$StepName,
        [string[]]$Arguments,
        [string]$LogPath
    )

    $process = Start-Process -FilePath "msiexec.exe" -ArgumentList $Arguments -Wait -PassThru -NoNewWindow
    $exitCode = $process.ExitCode
    if ($exitCode -notin @(0, 3010)) {
        throw "$StepName failed with exit code $exitCode. Log: $LogPath"
    }
}

function Resolve-InstalledExe {
    param([object]$UninstallEntry)

    $candidates = @()
    if ($UninstallEntry -and $UninstallEntry.InstallLocation) {
        $candidates += (Join-Path $UninstallEntry.InstallLocation "Cassy.exe")
    }
    $candidates += "$env:LOCALAPPDATA\Programs\Cassy\Cassy.exe"
    $candidates += "$env:ProgramFiles\Cassy\Cassy.exe"

    foreach ($candidate in $candidates | Select-Object -Unique) {
        if (Test-Path $candidate) {
            return $candidate
        }
    }

    throw "Installed Cassy.exe not found from registry/candidate paths."
}

function Invoke-InstalledSmoke {
    param(
        [string]$ExePath,
        [string[]]$Arguments,
        [string]$MarkerPath,
        [string]$LogPath
    )

    if (Test-Path $MarkerPath) {
        Remove-Item $MarkerPath -Force
    }

    $previousMarker = $env:CASSY_SMOKE_MARKER
    $env:CASSY_SMOKE_MARKER = $MarkerPath
    try {
        & $ExePath @Arguments *> $LogPath
        $exitCode = $LASTEXITCODE
        $deadline = (Get-Date).AddSeconds(30)
        while (-not (Test-Path $MarkerPath) -and (Get-Date) -lt $deadline) {
            Start-Sleep -Milliseconds 250
        }
        if (-not (Test-Path $MarkerPath)) {
            if ($null -ne $exitCode -and "$exitCode" -ne "") {
                throw "Installed launcher smoke marker missing: $MarkerPath (exit code $exitCode)"
            }
            throw "Installed launcher smoke marker missing: $MarkerPath"
        }
        $marker = (Get-Content $MarkerPath -Raw).Trim()
        if ($marker -notlike "CASSY_SMOKE_OK*") {
            throw "Installed launcher smoke marker invalid: $marker"
        }
        if ($null -ne $exitCode -and "$exitCode" -ne "" -and $exitCode -ne 0) {
            throw "Installed launcher smoke reported success marker but exited with code $exitCode"
        }
        return $marker
    } finally {
        if ($null -eq $previousMarker) {
            Remove-Item Env:CASSY_SMOKE_MARKER -ErrorAction SilentlyContinue
        } else {
            $env:CASSY_SMOKE_MARKER = $previousMarker
        }
    }
}

$resolvedMsiPath = (Resolve-Path $MsiPath).Path
$repoRoot = (Get-Location).Path
$timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
$outputRoot = Join-Path $repoRoot $OutputDirectory
$targetRoot = Join-Path $outputRoot $timestamp
$null = New-Item -ItemType Directory -Path $targetRoot -Force

$installLog = Join-Path $targetRoot "msi-install.log"
$repairLog = Join-Path $targetRoot "msi-repair.log"
$uninstallLog = Join-Path $targetRoot "msi-uninstall.log"
$installSmokeLog = Join-Path $targetRoot "installed-smoke.log"
$repairSmokeLog = Join-Path $targetRoot "repair-smoke.log"
$installMarker = Join-Path $targetRoot "installed-smoke-marker.txt"
$repairMarker = Join-Path $targetRoot "repair-smoke-marker.txt"
$summaryPath = Join-Path $targetRoot "installer-evidence-summary.txt"
$precleanLog = Join-Path $targetRoot "msi-preclean.log"

$existingEntry = Get-CassyUninstallEntry
if ($existingEntry) {
    $productCode = Resolve-ProductCode -UninstallEntry $existingEntry
    Invoke-MsiStep -StepName "MSI preclean uninstall" -Arguments @("/x", $productCode, "/qn", "/norestart", "/L*V", $precleanLog) -LogPath $precleanLog

    $entryAfterPreclean = Get-CassyUninstallEntry
    if ($entryAfterPreclean) {
        throw "Cassy uninstall entry still exists after MSI preclean uninstall."
    }
}

Invoke-MsiStep -StepName "MSI install" -Arguments @("/i", $resolvedMsiPath, "/qn", "/norestart", "/L*V", $installLog) -LogPath $installLog

$installedEntry = Get-CassyUninstallEntry
if (-not $installedEntry) {
    throw "No Cassy uninstall entry found after MSI install."
}

$installedExe = Resolve-InstalledExe -UninstallEntry $installedEntry
$installMarkerValue = Invoke-InstalledSmoke -ExePath $installedExe -Arguments $AppArgs -MarkerPath $installMarker -LogPath $installSmokeLog

Invoke-MsiStep -StepName "MSI repair" -Arguments @("/fa", $resolvedMsiPath, "/qn", "/norestart", "/L*V", $repairLog) -LogPath $repairLog
$repairMarkerValue = Invoke-InstalledSmoke -ExePath $installedExe -Arguments $AppArgs -MarkerPath $repairMarker -LogPath $repairSmokeLog

Invoke-MsiStep -StepName "MSI uninstall" -Arguments @("/x", $resolvedMsiPath, "/qn", "/norestart", "/L*V", $uninstallLog) -LogPath $uninstallLog

$entryAfterUninstall = Get-CassyUninstallEntry
if ($entryAfterUninstall) {
    throw "Cassy uninstall entry still exists after MSI uninstall."
}

if (Test-Path $installedExe) {
    throw "Installed Cassy.exe still exists after MSI uninstall: $installedExe"
}

$summary = @(
    "generated_at=$(Get-Date -Format o)",
    "msi_path=$resolvedMsiPath",
    "installed_exe=$installedExe",
    "install_smoke=$installMarkerValue",
    "repair_smoke=$repairMarkerValue",
    "preclean_log=$precleanLog",
    "install_log=$installLog",
    "repair_log=$repairLog",
    "uninstall_log=$uninstallLog"
)
Set-Content -Path $summaryPath -Value $summary

Write-Output "CASSY_INSTALLER_EVIDENCE_OK path=$targetRoot"
