. "$PSScriptRoot\env.ps1"
Write-Log "Starting Filesystem Server for $env:CASSY_REPO_ROOT"

Require-Path $env:CASSY_REPO_ROOT

try {
    # Official MCP server for filesystem
    $command = "npx"
    $args = @("-y", "@modelcontextprotocol/server-filesystem", $env:CASSY_REPO_ROOT)

    & $command $args
} catch {
    Write-Log $_.Exception.Message "ERROR"
    exit 1
}
