. "$PSScriptRoot\env.ps1"
Write-Log "Starting Playwright Server..."

try {
    # Playwright is optional and used for E2E testing
    $command = "npx"
    $args = @("-y", "@modelcontextprotocol/server-playwright")

    Write-Log "Note: Ensure browsers are installed via 'npx playwright install'." "WARN"
    & $command $args
} catch {
    Write-Log $_.Exception.Message "ERROR"
    exit 1
}
