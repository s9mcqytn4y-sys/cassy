param(
    [Parameter(Mandatory=$false)]
    [string]$GitHubToken,

    [Parameter(Mandatory=$false)]
    [string]$PostgresUrl
)

Write-Host "--- Cassy Environment Manager ---" -ForegroundColor Cyan

if ($GitHubToken) {
    # Set User-level environment variable on Windows
    [System.Environment]::SetEnvironmentVariable("GITHUB_TOKEN", $GitHubToken, "User")
    Write-Host "GITHUB_TOKEN updated for current User." -ForegroundColor Green
}

if ($PostgresUrl) {
    [System.Environment]::SetEnvironmentVariable("CASSY_PG_URL", $PostgresUrl, "User")
    Write-Host "CASSY_PG_URL updated for current User." -ForegroundColor Green
}

Write-Host "Please RESTART Android Studio or any open Terminal for changes to take effect." -ForegroundColor Yellow
