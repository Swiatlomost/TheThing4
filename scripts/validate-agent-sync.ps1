param(
    [switch]$StopOnWarning
)

$projectRoot = Resolve-Path (Join-Path $PSScriptRoot "..")
$pythonExe = Get-Command python -ErrorAction SilentlyContinue
if (-not $pythonExe) {
    Write-Host "Python not found in PATH." -ForegroundColor Red
    exit 1
}

Write-Host "Agent Sync Validator" -ForegroundColor Cyan
Write-Host "Root: $projectRoot" -ForegroundColor DarkGray

$startInfo = New-Object System.Diagnostics.ProcessStartInfo
$startInfo.FileName = $pythonExe.Source
$startInfo.Arguments = '"' + (Join-Path $projectRoot 'scripts/validate-agent-sync.py') + '"'
$startInfo.WorkingDirectory = $projectRoot
$startInfo.RedirectStandardOutput = $true
$startInfo.RedirectStandardError = $true
$startInfo.UseShellExecute = $false

$process = New-Object System.Diagnostics.Process
$process.StartInfo = $startInfo
$process.Start() | Out-Null
$stdout = $process.StandardOutput.ReadToEnd()
$stderr = $process.StandardError.ReadToEnd()
$process.WaitForExit()

if ($stdout) { Write-Host $stdout.TrimEnd() }
if ($stderr) { Write-Host $stderr.TrimEnd() -ForegroundColor Yellow }

if ($StopOnWarning -and $process.ExitCode -ne 0) {
    Write-Host "Warnings detected. Stopping as requested." -ForegroundColor Yellow
    exit $process.ExitCode
}

exit $process.ExitCode
