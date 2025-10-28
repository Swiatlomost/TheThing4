# Collaboration Starter Kit (Lean v3)

Start tutaj: otwórz `start.md`, aby skorzystać z gotowego promptu i skrótu workflow dla sesji z LLM.

Ten branch zawiera wyłącznie wieloagentowy workflow, szablony i automatyzacje używane przez zespół. Kod aplikacji i historyczne dane projektu zostały usunięte, aby dać czystą bazę.

## Spis treści
- Szybki start
- Mapa repozytorium
- Jak z tego korzystać
- Szybkie linki

## Szybki start (VS Code + ChatGPT)
1. Otwórz repozytorium w VS Code.
2. W ChatGPT for VS Code uruchom:
   ```
   Proszę przeczytać README.md, PROJECT_CONTEXT.md, AGENTS.md, WORKFLOW.md, MEMORY_SPEC.md, AI_GUIDE.md.
   Ustaw ten zestaw jako stały kontekst sesji.
   ```
3. Wywołaj `[SESSION::START]` — skrypt `scripts/session_start_hook.ps1` promuje pierwsze `pending` z `backlog/board.json` do `in_progress`.
4. Zbuduj kontekst LLM dla wybranego zadania:
   ```
   python scripts/context.py task MORPHO-007-undo-redo
   ```

## Mapa repozytorium (Lean)
- `backlog/board.json` — jedyne źródło prawdy o zadaniach (status, owner, topic, linki).
- `backlog/` — nowy układ backlogu (patrz `BACKLOG_VISION.md`):
  - `backlog/globals/` — artefakty globalne (ARCHITECTURE, UX-GUIDE, TEST-STRATEGY, itp.).
  - `backlog/topics/TOPIC-YYYYMMDD_HHMMSS-<slug>/` — briefy MIRA + PDCA + tasks.
- `backlog/backlog.json` — indeks topiców i zadań (auto). Aktualizacja: `python scripts/update_backlog_index.py`.
- `agents/<name>/memory.json` — długoterminowa pamięć agenta (bez task.json i logów).
- `reports/chronicle.md` — wspólna kronika Scribe.
- `docs/` — przewodniki i szablony.
- `scripts/` — narzędzia (`board.py`, `context.py`, `validate.py`, `set_in_progress_board.py`).
- Git hooks: `.githooks/pre-commit` (uruchamia validate + update backlog); konfiguracja: `pwsh scripts/setup_hooks.ps1`

## Jak z tego korzystać
- Przed startem: `python scripts/validate.py` (walidacja board + topics).
- PDCA: dla każdego zadania użyj `backlog/topics/<TOPIC>/tasks/<TASK-ID>/pdca.md` (link w `board.json` dodaje się automatycznie podczas pracy).
- Rozmowy o wizji: `[AGENT::MIRA]` aktualizuje `brief.json` w danym `topic/`.
- Logi: zapisuj krótkie wpisy per zadanie w `tasks/<TASK-ID>/log.md` (Why -> Next).
- Status: zmieniaj przez `python scripts/board.py move <TASK-ID> in_progress|done`.
- Kronika: Scribe dopisuje do `reports/chronicle.md` po ważnych checkpointach.
- VS Code: Tasks dostępne pod nazwami „Validate Lean Structure”, „Update Backlog Index”, „Context: Build for Task”, „Board: Move -> in_progress/done”.

Kontrybucje powinny skupiać się na procedurach, narzędziach i dokumentacji ułatwiających współpracę agentów. Pliki legacy (`agents/status.json`, `agents/*/task.json`, `sessions/*`) pozostają do podglądu; nowy przepływ używa `board.json` i `backlog/topics/`.

## Szybkie linki
- BACKLOG_VISION: `BACKLOG_VISION.md`
- Start: `start.md`
- Board: `backlog/board.json`
- Index: `backlog/backlog.json`
- Topics: `backlog/topics/`
- Globals: `backlog/globals/`
