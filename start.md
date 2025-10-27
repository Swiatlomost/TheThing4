# Start Prompt for LLM – Backlog‑First Workflow

Goal: load a compact, actionable context so you can work effectively with this repository using our lean, backlog‑first process.

## Load Context (once per session)
- Read: `README.md`, `PROJECT_CONTEXT.md`, `AGENTS.md`, `WORKFLOW.md`, `AI_GUIDE.md`, `MEMORY_SPEC.md`.
- Prefer backlog files as the source of truth:
  - Board: `backlog/board.json`
  - Areas and topics: `backlog/<obszar>/topics/TOPIC-.../`
  - Topic meta: `topic.json`, `BRIEF-*.json`, `PDCA.json`, `test-plan.json`, `event-checklist.json`
  - Task meta: `tasks/<TASK-ID>/{task.json, PDCA.json, log.jsonl}`
- Central team memory: `agents/memory.json` (map `agents.{Name}`)
- Chronicle: `reports/chronicle.md`

## Roles, Tags, Modes
- Address agents via tags: `[AGENT::NAME]` (Orin, Echo, Vireal, Lumen, Kai, Scribe, Nyx, Nodus, Aurum, Mira)
- Intent tags: `[TASK::ANALYZE]`, `[TASK::BUILD]`, `[TASK::REVIEW]`, `[TASK::LOG]`
- Reply modes: `[MODE::PRO]` (concise, actionable), `[MODE::META]` (reflective)

## Session Ritual (LLM)
1. Validate structure: `python scripts/validate.py` (report only)
2. Update index: `python scripts/update_backlog_index.py`
3. If instructed to start work: promote first `pending` (non‑deferred) with `[SESSION::START]` (hook calls `scripts/session_start_hook.ps1`). By default we keep backlog idle.

## Working on a Topic / Task
- Discover topics: open `backlog/backlog.json` or `backlog/board.json`
- Build context bundle (read‑only snapshot for you / tools):
  - Topic: `python scripts/context.py topic <TOPIC-ID>`
  - Task: `python scripts/context.py task <TASK-ID>`
- For a task you own:
  - PDCA: edit `backlog/<obszar>/topics/<TOPIC-ID>/tasks/<TASK-ID>/PDCA.json`
  - Log entries: append to `log.jsonl` via
    - `python scripts/log_add.py <TOPIC-ID> <TASK-ID> --who <Agent> --why "..." --what "..." --next "..." --tags tag1,tag2`
  - Status transitions (by Orin or on request):
    - `python scripts/board.py move <TASK-ID> in_progress|done`

## JSON Conventions
- `PDCA.json` keys: `plan`, `do`, `check`, `act`, `links`, `meta.updated_at`
- `log.jsonl` (one JSON per line): `{ "ts": "YYYY-MM-DDTHH:MM:SS", "who": "Kai", "why": "...", "what": "...", "next": "...", "tags": ["checkpoint"] }`
- Topic support files: `BRIEF-*.json`, `test-plan.json`, `event-checklist.json`, and local redirect stubs `ADR.json`, `UX.json`, `UI.json` (point to canonical docs under `docs/` or code paths).

## Guard Rails
- Single source of truth for work: `backlog/board.json`. Do not use legacy `agents/status.json` or per‑agent `task.json`.
- Keep “Why → What → Next” visible in logs and PDCA.
- Favor minimal, verifiable increments; avoid broad refactors unless asked.
- Archival copies live under `archive/legacy/` (read‑only for context).

## Useful Commands (cheat‑sheet)
- Validate: `python scripts/validate.py`
- Index: `python scripts/update_backlog_index.py`
- Build context: `python scripts/context.py task <TASK-ID>`
- Board list/move/add: `python scripts/board.py ls|move|add ...`
- Append log: `python scripts/log_add.py <TOPIC-ID> <TASK-ID> --who ... --why ... --what ... --next ...`
- Edit PDCA: `python scripts/pdca_edit.py topic|task <TOPIC-ID> [<TASK-ID>] --add plan|do|check|act "text"`
- Hooks setup (git): `pwsh scripts/setup_hooks.ps1`

## Typical Flows
- Refinement with Mira
  - `[AGENT::MIRA] [TASK::ANALYZE]` gather WHY/WHAT/HOW/WHAT_IF → create/update `BRIEF-*.json`
  - Draft `topic.json` + `PDCA.json`; add `UX.json`/`ADR.json` stubs
  - Orin adds tasks into `backlog/board.json` (tag `refinement`)
- Implementation with Lumen
  - Build task context → fill `PDCA.json` → append `log.jsonl` for each checkpoint
  - Request move to `in_progress` / `done` via board script
- QA with Kai / CI with Nodus
  - Maintain `test-plan.json` / `event-checklist.json`; log sanity evidence in `log.jsonl`
- Documentation with Scribe
  - Add chapter to `reports/chronicle.md` citing files with path references

## When to Ask
- Clarify ambiguous acceptance criteria before coding.
- Escalate via `[CONFLICT]` with options and a preferred recommendation.

## Commit Messages (LLM style)
- Prefix with the smallest scope (topic/task), summarize intent, list key files touched. Example:
  - `feat(morpho/MORPHO-008): autosort heuristics + tests; update PDCA/log`

## Quick Pointers (clickable)
- Board: `backlog/board.json`
- Index: `backlog/backlog.json`
- Example topic: `backlog/morfogeneza/topics/TOPIC-20251025_120000-001/topic.json`
- Example task: `backlog/morfogeneza/topics/TOPIC-20251025_120000-001/tasks/MORPHO-008-autosort/PDCA.json`
- Memory: `agents/memory.json`
- Chronicle: `reports/chronicle.md`

If you need me to scaffold a new topic or tasks, reply with area name, short WHY/WHAT, and owners; I will create the JSON files, link them in the board, and build a context bundle.

