# SETUP.md — Task startowy (opcjonalnie)

> Możesz dodać zadanie VS Code (tasks.json), które pomaga w szybkim otwarciu kontekstu.

Przykładowy `/.vscode/tasks.json`:
```json
{
  "version": "2.0.0",
  "tasks": [
    {
      "label": "Initialize Coś Context",
      "type": "shell",
      "command": "echo 'Wczytaj PROJECT_CONTEXT.md, AGENTS.md, WORKFLOW.md, MEMORY_SPEC.md, AI_GUIDE.md w ChatGPT i ustaw kontekst.'"
    }
  ]
}
```
