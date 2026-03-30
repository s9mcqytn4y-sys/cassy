. "$PSScriptRoot\env.ps1"
Write-Log "Starting ADB Server..."

try {
    # Check for adb command
    $adb = Get-Command "adb" -ErrorAction SilentlyContinue
    if (-not $adb) {
        Write-Log "adb not found in PATH. Ensure Android SDK is installed." "ERROR"
        exit 1
    }

    # Using a common community ADB MCP server
    $command = "npx"
    $args = @("-y", "@modelcontextprotocol/server-adb")

    & $command $args
} catch {
    Write-Log $_.Exception.Message "ERROR"
    exit 1
}
