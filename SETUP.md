# SETUP.md - Task startowy (opcjonalnie)

> Mozesz dodac zadanie VS Code (tasks.json), ktore pomaga w szybkim otwarciu kontekstu.

Przykladowy `/.vscode/tasks.json`:
```json
{
  "version": "2.0.0",
  "tasks": [
    {
      "label": "Initialize Cos Context",
      "type": "shell",
      "command": "echo 'Wczytaj PROJECT_CONTEXT.md, AGENTS.md, WORKFLOW.md, MEMORY_SPEC.md, AI_GUIDE.md w ChatGPT i ustaw kontekst.'"
    }
  ]
}
```

## Przygotowanie srodowiska
Instrukcje produktowe (Android CLI, Gradle, overlay) znajdziesz w `docs/reference/tooling-setup.md`. Repozytorium bazowe skupia sie na procesie wspolpracy, dlatego konfiguracje platformowe traktujemy jako rozszerzenia dostepne na zadanie.
