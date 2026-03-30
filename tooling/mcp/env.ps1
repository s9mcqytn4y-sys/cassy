Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

# --- Configuration (Portable) ---
$script:REPO_ROOT = (Resolve-Path "$PSScriptRoot\..\..").Path
$script:SQLITE_PATH = Join-Path $script:REPO_ROOT "local-dev.db"
$script:ALLOWLIST_PATH = Join-Path $PSScriptRoot "commands\cassy-terminal-allowlist.json"

# Environment Variables (Overridable)
$env:CASSY_REPO_ROOT = $script:REPO_ROOT
$env:CASSY_SQLITE_PATH = $script:SQLITE_PATH

# --- Helper Functions (MCP Safe) ---

function Write-Log {
    param([string]$Message, [string]$Level = "INFO")
    # Redirect to Stderr so it doesn't break the MCP JSON-RPC on Stdout
    [Console]::Error.WriteLine("[CASSY-MCP] [$Level] $Message")
}

function Require-Path {
    param([string]$Path)
    if (-not (Test-Path $Path)) {
        Write-Log "Mandatory path missing: $Path" "ERROR"
        exit 1
    }
}

function Require-Command {
    param([string]$Cmd)
    if (-not (Get-Command $Cmd -ErrorAction SilentlyContinue)) {
        Write-Log "Missing dependency in PATH: $Cmd" "ERROR"
        exit 1
    }
}

Write-Log "Environment Loaded for $env:CASSY_REPO_ROOT"
