# PowerShell hook: uruchom po SESSION::START
$taskJson = "d:\TheThing4\TheThing4\agents\orin\task.json"
$orinTaskId = (
	(Get-Content $taskJson | ConvertFrom-Json).current_tasks |
	Where-Object { $_.status -eq "pending" } |
	Select-Object -First 1 -ExpandProperty task_id
)
if ($orinTaskId) {
	python d:\TheThing4\TheThing4\scripts\set_in_progress.py $taskJson $orinTaskId
} else {
	Write-Host "Brak aktywnego zadania Orina do uruchomienia."
}
