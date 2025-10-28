# Agents Directory (Lean)

- Pamiec zespolu jest utrzymywana centralnie w `agents/memory.json` (mapa `agents.{Name}` z tresciami pamieci kazdego agenta).
- Statusy i praca operacyjna sa w `backlog/board.json` oraz `backlog/topics/...` (PDCA/log per zadanie).
- Kronika projektu: `reports/chronicle.md`.

## Memory
- Edytuj wpisy bezposrednio w `agents/memory.json` (pole `last_updated` lub `last_reviewed`).
- Dla kompatybilnosci mozesz wyeksportowac/importowac per-agent pliki:
  - Export: `python scripts/memory_sync.py export`
  - Import: `python scripts/memory_sync.py import`
  - Status: `python scripts/memory_sync.py status`

## Start pracy
- Walidacja: `python scripts/validate.py`
- Indeks backlogu: `python scripts/update_backlog_index.py`
- Start sesji: `[SESSION::START]` (promocja z `backlog/board.json`)

Wiecej zasad: `AGENTS.md`, `AI_GUIDE.md`, `WORKFLOW.md`.
