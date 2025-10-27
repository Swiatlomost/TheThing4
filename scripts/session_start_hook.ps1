# Run this script after `[SESSION::START]` to promote the first pending Orin task.

$projectRoot = Resolve-Path (Join-Path $PSScriptRoot "..")
$boardJson = Join-Path $projectRoot "board.json"

if (Test-Path $boardJson) {
    $pythonExe = Get-Command python -ErrorAction SilentlyContinue
    if (-not $pythonExe) {
        Write-Host "Python not found in PATH." -ForegroundColor Red
        exit 1
    }
    python (Join-Path $projectRoot "scripts/set_in_progress_board.py")
    exit $LASTEXITCODE
}

# Legacy fallback (pre-lean structure)
$taskJson = Join-Path $projectRoot "agents/orin/task.json"
if (Test-Path $taskJson) {
    $orinTasks = Get-Content $taskJson | ConvertFrom-Json
    $pending = $orinTasks.current_tasks | Where-Object { $_.status -eq "pending" } | Select-Object -First 1
    if ($pending) {
        $pythonExe = Get-Command python -ErrorAction SilentlyContinue
        if (-not $pythonExe) {
            Write-Host "Python not found in PATH. Please run scripts/set_in_progress.py manually." -ForegroundColor Yellow
            exit 1
        }
        python (Join-Path $projectRoot "scripts/set_in_progress.py") $taskJson $($pending.task_id)
    } else {
        Write-Host "No pending Orin tasks to promote (legacy)." -ForegroundColor Gray
    }
} else {
    Write-Host "Neither board.json nor legacy agents/orin/task.json found." -ForegroundColor Yellow
}
