param(
    [string]$BaselineReleaseVersion = "0.1.0",
    [string]$BaselinePackageVersion = "0.1.0",
    [string]$CandidateReleaseVersion = "",
    [string]$CandidatePackageVersion = "",
    [string]$OutputDirectory = "build/upgrade-evidence"
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

    Write-Output "STEP $StepName"
    Write-Output "LOG  $LogPath"
    $process = Start-Process -FilePath "msiexec.exe" -ArgumentList $Arguments -Wait -PassThru -NoNewWindow
    $exitCode = $process.ExitCode
    if ($exitCode -notin @(0, 3010)) {
        throw "$StepName failed with exit code $exitCode. Log: $LogPath"
    }
    Write-Output "OK   $StepName exit=$exitCode"
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
        [string]$MarkerPath,
        [string]$LogPath,
        [string]$DataRoot
    )

    if (Test-Path $MarkerPath) {
        Remove-Item $MarkerPath -Force
    }

    $previousMarker = $env:CASSY_SMOKE_MARKER
    $previousDataRoot = $env:CASSY_DATA_DIR
    $previousScenario = $env:CASSY_SMOKE_SCENARIO
    $env:CASSY_SMOKE_MARKER = $MarkerPath
    $env:CASSY_DATA_DIR = $DataRoot
    $env:CASSY_SMOKE_SCENARIO = "beta"
    try {
        New-Item -ItemType Directory -Path $DataRoot -Force | Out-Null
        & $ExePath --smoke-run *> $LogPath
        $exitCode = $LASTEXITCODE
        if ($exitCode -ne 0) {
            throw "Installed smoke gagal dengan exit code $exitCode"
        }
        $markerReady = $false
        foreach ($attempt in 1..120) {
            if ((Test-Path $MarkerPath) -and ((Get-Item $MarkerPath).Length -gt 0)) {
                $markerReady = $true
                break
            }
            Start-Sleep -Milliseconds 500
        }
        if (-not $markerReady) {
            throw "Installed smoke marker belum tersedia: $MarkerPath"
        }
        $marker = (Get-Content $MarkerPath -Raw).Trim()
        if ($marker -notlike "CASSY_SMOKE_OK*") {
            throw "Installed smoke marker invalid: $marker"
        }
        return $marker
    } finally {
        if ($null -eq $previousMarker) {
            Remove-Item Env:CASSY_SMOKE_MARKER -ErrorAction SilentlyContinue
        } else {
            $env:CASSY_SMOKE_MARKER = $previousMarker
        }
        if ($null -eq $previousDataRoot) {
            Remove-Item Env:CASSY_DATA_DIR -ErrorAction SilentlyContinue
        } else {
            $env:CASSY_DATA_DIR = $previousDataRoot
        }
        if ($null -eq $previousScenario) {
            Remove-Item Env:CASSY_SMOKE_SCENARIO -ErrorAction SilentlyContinue
        } else {
            $env:CASSY_SMOKE_SCENARIO = $previousScenario
        }
    }
}

function Get-GradlePropertyValue {
    param([string]$Name)

    $match = Get-Content "gradle.properties" |
        Where-Object { $_.StartsWith("$Name=") } |
        Select-Object -First 1
    if (-not $match) {
        throw "Gradle property tidak ditemukan: $Name"
    }
    return $match.Substring($Name.Length + 1).Trim()
}

function Invoke-GradlePackaging {
    param(
        [string]$ReleaseVersion,
        [string]$PackageVersion
    )

    & .\gradlew.bat :apps:desktop-pos:packageMsi "-Pcassy.release.version=$ReleaseVersion" "-Pcassy.package.version=$PackageVersion" --no-daemon
    if ($LASTEXITCODE -ne 0) {
        throw "Packaging MSI gagal untuk release=$ReleaseVersion package=$PackageVersion"
    }
}

$repoRoot = (Get-Location).Path
$timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
$targetRoot = Join-Path (Join-Path $repoRoot $OutputDirectory) $timestamp

try {
    if (-not $CandidateReleaseVersion) {
        $CandidateReleaseVersion = Get-GradlePropertyValue -Name "cassy.release.version"
    }
    if (-not $CandidatePackageVersion) {
        $CandidatePackageVersion = Get-GradlePropertyValue -Name "cassy.package.version"
    }

    New-Item -ItemType Directory -Path $targetRoot -Force | Out-Null

    $baselineCopy = Join-Path $targetRoot "Cassy-$BaselinePackageVersion.msi"
    $candidateCopy = Join-Path $targetRoot "Cassy-$CandidatePackageVersion.msi"
    $summaryPath = Join-Path $targetRoot "upgrade-evidence-summary.txt"
    $dataRoot = Join-Path $targetRoot "upgrade-data"
    $baselineInstallLog = Join-Path $targetRoot "baseline-install.log"
    $candidateInstallLog = Join-Path $targetRoot "candidate-upgrade.log"
    $uninstallLog = Join-Path $targetRoot "candidate-uninstall.log"
    $baselineSmokeLog = Join-Path $targetRoot "baseline-smoke.log"
    $candidateSmokeLog = Join-Path $targetRoot "candidate-smoke.log"
    $baselineMarker = Join-Path $targetRoot "baseline-smoke-marker.txt"
    $candidateMarker = Join-Path $targetRoot "candidate-smoke-marker.txt"

    Invoke-GradlePackaging -ReleaseVersion $BaselineReleaseVersion -PackageVersion $BaselinePackageVersion
    Copy-Item "apps/desktop-pos/build/compose/binaries/main/msi/Cassy-$BaselinePackageVersion.msi" $baselineCopy -Force
    Write-Output "Prepared baseline MSI: $baselineCopy"

    Invoke-GradlePackaging -ReleaseVersion $CandidateReleaseVersion -PackageVersion $CandidatePackageVersion
    Copy-Item "apps/desktop-pos/build/compose/binaries/main/msi/Cassy-$CandidatePackageVersion.msi" $candidateCopy -Force
    Write-Output "Prepared candidate MSI: $candidateCopy"

    $existingEntry = Get-CassyUninstallEntry
    if ($existingEntry) {
        $productCode = Resolve-ProductCode -UninstallEntry $existingEntry
        Invoke-MsiStep -StepName "MSI preclean uninstall" -Arguments @("/x", $productCode, "/qn", "/norestart", "/L*V", (Join-Path $targetRoot "preclean.log")) -LogPath (Join-Path $targetRoot "preclean.log")
    }

    Invoke-MsiStep -StepName "Baseline MSI install" -Arguments @("/i", $baselineCopy, "/qn", "/norestart", "/L*V", $baselineInstallLog) -LogPath $baselineInstallLog
    $baselineEntry = Get-CassyUninstallEntry
    if (-not $baselineEntry) {
        throw "Tidak ada uninstall entry setelah baseline install."
    }
    $installedExe = Resolve-InstalledExe -UninstallEntry $baselineEntry
    $baselineMarkerValue = Invoke-InstalledSmoke -ExePath $installedExe -MarkerPath $baselineMarker -LogPath $baselineSmokeLog -DataRoot $dataRoot
    Write-Output "Baseline smoke marker: $baselineMarkerValue"

    Invoke-MsiStep -StepName "Candidate MSI upgrade" -Arguments @("/i", $candidateCopy, "/qn", "/norestart", "/L*V", $candidateInstallLog) -LogPath $candidateInstallLog
    $candidateEntry = Get-CassyUninstallEntry
    if (-not $candidateEntry) {
        throw "Tidak ada uninstall entry setelah candidate upgrade."
    }
    $candidateExe = Resolve-InstalledExe -UninstallEntry $candidateEntry
    $candidateMarkerValue = Invoke-InstalledSmoke -ExePath $candidateExe -MarkerPath $candidateMarker -LogPath $candidateSmokeLog -DataRoot $dataRoot
    Write-Output "Candidate smoke marker: $candidateMarkerValue"

    $candidateProductCode = Resolve-ProductCode -UninstallEntry $candidateEntry
    Invoke-MsiStep -StepName "Candidate MSI uninstall" -Arguments @("/x", $candidateProductCode, "/qn", "/norestart", "/L*V", $uninstallLog) -LogPath $uninstallLog

    $summary = @(
        "generated_at=$(Get-Date -Format o)",
        "baseline_release_version=$BaselineReleaseVersion",
        "baseline_package_version=$BaselinePackageVersion",
        "candidate_release_version=$CandidateReleaseVersion",
        "candidate_package_version=$CandidatePackageVersion",
        "baseline_msi=$baselineCopy",
        "candidate_msi=$candidateCopy",
        "baseline_smoke=$baselineMarkerValue",
        "candidate_smoke=$candidateMarkerValue",
        "upgrade_data_root=$dataRoot",
        "status=baseline-install-upgrade-candidate-smoke-uninstall"
    )
    Set-Content -Path $summaryPath -Value $summary

    Write-Output "CASSY_UPGRADE_EVIDENCE_OK path=$targetRoot"
    exit 0
} catch {
    if (Test-Path $targetRoot) {
        $errorPath = Join-Path $targetRoot "upgrade-error.txt"
        Set-Content -Path $errorPath -Value ($_ | Out-String)
    }
    Write-Error ($_ | Out-String)
    exit 1
}
