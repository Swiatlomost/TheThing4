param(
    [switch]$Global
)

Write-Host "Setting git hooksPath to .githooks" -ForegroundColor Cyan
if ($Global) {
  git config --global core.hooksPath .githooks
} else {
  git config core.hooksPath .githooks
}
Write-Host "Done. Verify by committing; hook will run validate+index." -ForegroundColor Green

