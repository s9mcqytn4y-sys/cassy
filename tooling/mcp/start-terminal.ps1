. "$PSScriptRoot\env.ps1"
Write-Log "Starting Restricted Terminal Server..."

Require-Path $script:ALLOWLIST_PATH

try {
    # Using a common community pattern for terminal/command execution
    # TODO: Verify if @modelcontextprotocol/server-terminal is the final package name
    $command = "npx"
    $args = @("-y", "@modelcontextprotocol/server-terminal", "--allowlist", $script:ALLOWLIST_PATH)

    Write-Log "Allowlist loaded from: $script:ALLOWLIST_PATH"
    & $command $args
} catch {
    Write-Log $_.Exception.Message "ERROR"
    exit 1
}
