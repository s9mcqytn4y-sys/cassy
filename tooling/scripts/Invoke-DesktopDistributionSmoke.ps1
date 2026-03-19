param(
    [string]$DistributionRoot = "apps/desktop-pos/build/compose/binaries/main/app/Cassy",
    [string[]]$AppArgs = @("--smoke-run")
)

$ErrorActionPreference = "Stop"

$root = (Resolve-Path $DistributionRoot).Path
$cfgPath = Join-Path $root "app/Cassy.cfg"
$javaPath = Join-Path $root "runtime/bin/java.exe"
$markerPath = Join-Path ([IO.Path]::GetTempPath()) "cassy-distribution-smoke-marker.txt"

if (-not (Test-Path $cfgPath)) {
    throw "Missing distribution config: $cfgPath"
}

if (-not (Test-Path $javaPath)) {
    if (-not $env:JAVA_HOME) {
        throw "Distribution runtime has no java launcher and JAVA_HOME is not set"
    }
    $javaPath = Join-Path $env:JAVA_HOME "bin/java.exe"
}

if (-not (Test-Path $javaPath)) {
    throw "Missing Java launcher for distribution smoke: $javaPath"
}

$cfgLines = Get-Content $cfgPath
$mainClass = ($cfgLines | Where-Object { $_ -like "app.mainclass=*" } | Select-Object -First 1)
if (-not $mainClass) {
    throw "Missing app.mainclass entry in $cfgPath"
}
$mainClass = $mainClass.Substring("app.mainclass=".Length)

$classpathEntries = $cfgLines |
    Where-Object { $_ -like "app.classpath=*" } |
    ForEach-Object { $_.Substring("app.classpath=".Length).Replace('$APPDIR', (Join-Path $root "app")) }

if (-not $classpathEntries) {
    throw "Missing app.classpath entries in $cfgPath"
}

$javaOptions = $cfgLines |
    Where-Object { $_ -like "java-options=*" } |
    ForEach-Object { $_.Substring("java-options=".Length).Replace('$APPDIR', (Join-Path $root "app")) }

$classpath = [string]::Join([IO.Path]::PathSeparator, $classpathEntries)
$arguments = @()
$arguments += $javaOptions
$arguments += "-cp"
$arguments += $classpath
$arguments += $mainClass
$arguments += $AppArgs

Write-Output "Using Java launcher: $javaPath"

if (Test-Path $markerPath) {
    Remove-Item $markerPath -Force
}

$previousMarker = $env:CASSY_SMOKE_MARKER
$env:CASSY_SMOKE_MARKER = $markerPath

try {
    & $javaPath @arguments
    $exitCode = $LASTEXITCODE
    if ($exitCode -ne 0) {
        throw "Desktop distribution smoke failed with exit code $exitCode"
    }

    if (-not (Test-Path $markerPath)) {
        throw "Desktop distribution smoke did not create marker: $markerPath"
    }

    $marker = (Get-Content $markerPath -Raw).Trim()
    if ($marker -notlike "CASSY_SMOKE_OK*") {
        throw "Desktop distribution smoke marker is not OK: $marker"
    }

    Write-Output "Distribution marker verified: $marker"
} finally {
    if ($null -eq $previousMarker) {
        Remove-Item Env:CASSY_SMOKE_MARKER -ErrorAction SilentlyContinue
    } else {
        $env:CASSY_SMOKE_MARKER = $previousMarker
    }
}
