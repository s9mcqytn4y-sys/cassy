. "$PSScriptRoot\env.ps1"
Write-Log "Starting Git Server for $env:CASSY_REPO_ROOT"

Require-Command "git"

try {
    # Using official Git MCP server
    $command = "npx"
    $args = @("-y", "@modelcontextprotocol/server-git", $env:CASSY_REPO_ROOT)

    Write-Log "Git version: $(git --version)"
    & $command $args
} catch {
    Write-Log $_.Exception.Message "ERROR"
    exit 1
}
