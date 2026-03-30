. "$PSScriptRoot\env.ps1"
Write-Log "Starting GitHub Server..."

if (-not $env:GITHUB_TOKEN) {
    Write-Log "GITHUB_TOKEN is not set. GitHub MCP will fail to authenticate." "ERROR"
    exit 1
}

try {
    # Official GitHub MCP server
    $command = "npx"
    $args = @("-y", "@modelcontextprotocol/server-github")

    & $command $args
} catch {
    Write-Log $_.Exception.Message "ERROR"
    exit 1
}
