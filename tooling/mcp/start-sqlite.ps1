. "$PSScriptRoot\env.ps1"
Write-Log "Starting SQLite Server..."

# Ensure the DB path is valid and exists
if (-not (Test-Path $env:CASSY_SQLITE_PATH)) {
    Write-Log "Database file not found at $env:CASSY_SQLITE_PATH" "WARN"
    Write-Log "The server may start but queries will fail until the file is created." "WARN"
}

try {
    # Official SQLite MCP server
    $command = "npx"
    $args = @("-y", "@modelcontextprotocol/server-sqlite", "--db", $env:CASSY_SQLITE_PATH)

    Write-Log "Target DB: $env:CASSY_SQLITE_PATH"
    & $command $args
} catch {
    Write-Log $_.Exception.Message "ERROR"
    exit 1
}
