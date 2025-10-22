# validate-agent-sync.ps1
# Skrypt walidacji synchronizacji plikow agentow
# Autor: Agent Orin (Coordinator)
# Data: 2025-10-22

param(
    [switch]$Fix = $false,
    [switch]$Verbose = $false
)

Write-Host "Agent Sync Validator v1.0" -ForegroundColor Cyan
Write-Host "Sprawdzanie spojnosci task.json, status.md i logow..." -ForegroundColor Gray
Write-Host ""

$ErrorCount = 0
$WarningCount = 0
$AgentList = @("orin", "echo", "vireal", "lumen", "kai", "scribe", "nyx", "nodus", "aurum")

function Write-Error-Message($message) {
    Write-Host "ERROR: $message" -ForegroundColor Red
    $script:ErrorCount++
}

function Write-Warning-Message($message) {
    Write-Host "WARNING: $message" -ForegroundColor Yellow
    $script:WarningCount++
}

function Write-Success-Message($message) {
    Write-Host "OK: $message" -ForegroundColor Green
}

function Write-Info-Message($message) {
    if ($Verbose) {
        Write-Host "INFO: $message" -ForegroundColor Blue
    }
}

# Sprawdź czy jesteśmy w głównym katalogu projektu
if (-not (Test-Path "agents" -PathType Container)) {
    Write-Error-Message "Skrypt musi być uruchomiony z głównego katalogu projektu (gdzie znajduje się folder 'agents')"
    exit 1
}

Write-Host "🔎 1. Sprawdzanie struktury plików agentów..." -ForegroundColor White

foreach ($agent in $AgentList) {
    $agentDir = "agents\$agent"
    if (-not (Test-Path $agentDir -PathType Container)) {
        Write-Warning-Message "Brak katalogu $agentDir"
        continue
    }
    
    $taskFile = "$agentDir\task.json"
    $logFile = "$agentDir\log.md"
    $memoryFile = "$agentDir\memory.json"
    
    if (-not (Test-Path $taskFile)) {
        Write-Error-Message "Brak pliku $taskFile"
    }
    if (-not (Test-Path $logFile)) {
        Write-Warning-Message "Brak pliku $logFile"
    }
    if (-not (Test-Path $memoryFile)) {
        Write-Warning-Message "Brak pliku $memoryFile"
    }
    
        Write-Info-Message "Agent ${agent}: struktura plikow OK"
}

Write-Host ""
Write-Host "🔎 2. Sprawdzanie spójności statusów zadań..." -ForegroundColor White

$AllDoneTasksInCurrent = @()

foreach ($agent in $AgentList) {
    $taskFile = "agents\$agent\task.json"
    if (-not (Test-Path $taskFile)) { continue }
    
    try {
        $taskData = Get-Content $taskFile -Raw | ConvertFrom-Json
        
        # Sprawdź czy są zadania "done" w current_tasks
        foreach ($task in $taskData.current_tasks) {
            if ($task.status -eq "done") {
                $AllDoneTasksInCurrent += @{
                    Agent = $agent
                    TaskId = $task.task_id
                    Title = $task.title
                }
                Write-Error-Message "Agent $agent ma zadanie 'done' w current_tasks: $($task.task_id)"
            }
        }
        
        Write-Info-Message "Agent ${agent}: spojnosc statusow OK"
    }
    catch {
        Write-Error-Message "Błąd parsowania $taskFile : $($_.Exception.Message)"
    }
}

Write-Host ""
Write-Host "🔎 3. Sprawdzanie agents/status.md..." -ForegroundColor White

if (Test-Path "agents\status.md") {
    try {
        $statusContent = Get-Content "agents\status.md" -Raw
        
        # Proste sprawdzenie czy status.md zawiera zadania marked as done w current_tasks
        foreach ($doneTask in $AllDoneTasksInCurrent) {
            if ($statusContent -match $doneTask.TaskId) {
                Write-Warning-Message "agents/status.md może zawierać zadanie $($doneTask.TaskId) które powinno być w completed_tasks"
            }
        }
        
        Write-Success-Message "agents/status.md istnieje i jest czytelny"
    }
    catch {
        Write-Error-Message "Błąd czytania agents/status.md: $($_.Exception.Message)"
    }
} else {
    Write-Error-Message "Brak pliku agents/status.md"
}

Write-Host ""
Write-Host "🔎 4. Sprawdzanie dashboard..." -ForegroundColor White

if (Test-Path "agents\dashboard.md") {
    Write-Success-Message "agents/dashboard.md istnieje"
} else {
    Write-Warning-Message "Brak pliku agents/dashboard.md"
}

Write-Host ""
Write-Host "🔎 5. Sprawdzanie infrastruktury..." -ForegroundColor White

# Sprawdź czy można uruchomić gradle test
if (Test-Path "gradlew.bat") {
    Write-Info-Message "gradlew.bat dostępny - można uruchomić testy"
    if ($Verbose) {
        Write-Host "  💡 Uruchom: .\gradlew.bat test" -ForegroundColor Gray
        Write-Host "  💡 Uruchom: .\gradlew.bat connectedDebugAndroidTest" -ForegroundColor Gray
    }
} else {
    Write-Warning-Message "Brak gradlew.bat - sprawdź setup Gradle"
}

# Sprawdź git status
try {
    $gitStatus = git status --porcelain 2>$null
    if ($LASTEXITCODE -eq 0) {
        if ($gitStatus) {
            Write-Info-Message "Git repo ma niezatwierdzone zmiany"
        } else {
            Write-Success-Message "Git repo jest czysty"
        }
    } else {
        Write-Info-Message "Brak git repo lub git niedostępny"
    }
} catch {
    Write-Info-Message "Nie można sprawdzić statusu git"
}

# Podsumowanie
Write-Host ""
Write-Host "📊 PODSUMOWANIE WALIDACJI" -ForegroundColor Cyan
Write-Host "========================" -ForegroundColor Cyan

if ($ErrorCount -eq 0 -and $WarningCount -eq 0) {
    Write-Host "🎉 Wszystkie sprawdzenia przeszły pomyślnie!" -ForegroundColor Green
    Write-Host "System agentów jest zsynchronizowany i gotowy do pracy." -ForegroundColor Green
    exit 0
} else {
    Write-Host "❌ Błędy: $ErrorCount" -ForegroundColor Red
    Write-Host "⚠️  Ostrzeżenia: $WarningCount" -ForegroundColor Yellow
    
    if ($AllDoneTasksInCurrent.Count -gt 0) {
        Write-Host ""
        Write-Host "🔧 ZALECANE DZIAŁANIA:" -ForegroundColor Yellow
        Write-Host "1. Uruchom procedurę cooldown z WORKFLOW.md" -ForegroundColor Gray
        Write-Host "2. Przenieś ukończone zadania do completed_tasks" -ForegroundColor Gray
        Write-Host "3. Zaktualizuj agents/status.md" -ForegroundColor Gray
        Write-Host "4. Uruchom ponownie: .\scripts\validate-agent-sync.ps1" -ForegroundColor Gray
    }
    
    if ($Fix) {
        Write-Host ""
        Write-Host "🛠️  Auto-fix nie jest jeszcze zaimplementowany" -ForegroundColor Yellow
        Write-Host "Planowane w przyszłej wersji skryptu" -ForegroundColor Gray
    }
    
    exit 1
}