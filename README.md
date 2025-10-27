# Collaboration Starter Kit (Lean v3)

Start here: open `start.md` for a ready-to-use prompt and workflow cheat‑sheet for LLM sessions.

This branch contains only the multi-agent workflow, templates, and automation used by the team. Application code and historical project data were removed to give us a clean foundation.

## Quick Start (VS Code + ChatGPT)
1. Open the repository in VS Code.
2. In ChatGPT for VS Code run:
   ```
   Prosze przeczytac README.md, PROJECT_CONTEXT.md, AGENTS.md, WORKFLOW.md, MEMORY_SPEC.md, AI_GUIDE.md.
   Ustaw ten zestaw jako staly kontekst sesji.
   ```
3. Wywolaj `[SESSION::START]` — skrypt `scripts/session_start_hook.ps1` promuje pierwsze `pending` z `backlog/board.json` do `in_progress`.
4. Zbuduj kontekst LLM dla wybranego zadania:
   ```
   python scripts/context.py task MORPHO-007-undo-redo
   ```

## Repository Map (Lean)
- `backlog/board.json` - jedyne źródło prawdy o zadaniach (status, owner, topic, linki).
- `backlog/` - obszary funkcjonalne aplikacji, każdy z własnymi topicami i przewodnikami:
  - `backlog/okno-glowne/` — `LINKS.json`, `ARCHITECTURE.md`, `topics/`
  - `backlog/overlay/` — `LINKS.json`, `ARCHITECTURE.md`, `topics/`
  - `backlog/morfogeneza/` — `LINKS.json`, `ARCHITECTURE.md`, `topics/TOPIC-YYYYMMDD_HHMMSS-n/BRIEF-*.json`, `PDCA-*.md`, `tasks/`
- `backlog/backlog.json` — indeks obszarów i topiców (status + lista zadań). Aktualizacja: `python scripts/update_backlog_index.py`.
- `agents/<name>/memory.json` - długoterminowa pamięć agenta (bez task.json i logów).
- `reports/chronicle.md` - wspólna kronika Scribe.
- `docs/` - przewodniki i szablony (bez zmian w treści merytorycznej).
- `scripts/` - narzędzia (`board.py`, `context.py`, `validate.py`, `set_in_progress_board.py`).
- Git hooks: `.githooks/pre-commit` (uruchamia validate + update backlog); konfiguracja: `pwsh scripts/setup_hooks.ps1`

## How To Use It
- Przed startem: `python scripts/validate.py` (walidacja board + topics).
- PDCA: dla każdego zadania użyj `topics/<...>/tasks/<TASK-ID>/pdca.md` (link w `board.json` dodaje się automatycznie podczas pracy).
- Rozmowy o wizji: `[AGENT::MIRA]` aktualizuje `brief.json` w danym `topic/`.
- Logi: zapisuj krótkie wpisy per zadanie w `tasks/<TASK-ID>/log.md` (Why → Next).
- Status: zmieniaj przez `python scripts/board.py move <TASK-ID> in_progress|done`.
- Kronika: Scribe dopisuje do `reports/chronicle.md` po ważnych checkpointach.
- VS Code: Tasks dostępne pod nazwami „Validate Lean Structure”, „Update Backlog Index”, „Context: Build for Task”, „Board: Move -> in_progress/done”.

Contributions should focus on better procedures, tooling, or documentation that help the agents collaborate. Legacy files (`agents/status.json`, `agents/*/task.json`, `sessions/*`) pozostają do podglądu; nowy przepływ używa `board.json` i `topics/`.
