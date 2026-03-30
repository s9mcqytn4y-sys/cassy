. "$PSScriptRoot\env.ps1"
Write-Log "Starting PostgreSQL Server..."

if (-not $env:CASSY_PG_URL) {
    Write-Log "CASSY_PG_URL is not set. Postgres MCP cannot connect to HQ backend." "ERROR"
    exit 1
}

try {
    # Using official or common community postgres MCP server
    $command = "npx"
    $args = @("-y", "@modelcontextprotocol/server-postgres", $env:CASSY_PG_URL)

    # Hide the full URL in logs for safety
    $safeUrl = $env:CASSY_PG_URL -replace ":[^/@]+@", ":****@"
    Write-Log "Target URL: $safeUrl"

    & $command $args
} catch {
    Write-Log $_.Exception.Message "ERROR"
    exit 1
}
