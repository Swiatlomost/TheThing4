# Agents Directory (Lean)

- Pamięć zespołu jest utrzymywana centralnie w `agents/memory.json` (mapa `agents.{Name}` z treściami pamięci każdego agenta).
- Statusy i praca operacyjna są w `backlog/board.json` oraz `backlog/<obszar>/topics/...` (PDCA/log per zadanie).
- Kronika projektu: `reports/chronicle.md`.

## Memory
- Edytuj wpisy bezpośrednio w `agents/memory.json` (pole `last_updated` lub `last_reviewed`).
- Dla kompatybilności możesz wyeksportować/importować per‑agent pliki:
  - Export: `python scripts/memory_sync.py export`
  - Import: `python scripts/memory_sync.py import`
  - Status: `python scripts/memory_sync.py status`

## Start pracy
- Walidacja: `python scripts/validate.py`
- Indeks backlogu: `python scripts/update_backlog_index.py`
- Start sesji: `[SESSION::START]` (promocja z `backlog/board.json`)

Więcej zasad: `AGENTS.md`, `AI_GUIDE.md`, `WORKFLOW.md`.

