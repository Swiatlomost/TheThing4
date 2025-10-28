# Backlog Vision (Lean Topics v1)

## Why
- Make backlog the single, reliable map of work across the project.
- Separate global, cross-cutting knowledge from per-conversation (topic) briefs and per-task execution.
- Keep artefacts lightweight, linked, and easy to validate/automate.

## Goals
- Single source of truth for status: `backlog/board.json`.
- Clear information layering:
  1) Global (project-wide guidance)  2) Topics (Mira briefs)  3) Tasks (execution files).
- Minimize duplication; prefer links over copying.
- First-class PDCA + Why/Next logs at topic/task level.
- Scriptable validation and index for fast navigation.

## Structure
- `backlog/`
  - `board.json` - statuses for all tasks (owner, topic, links); the only place to move tasks.
  - `backlog.json` - generated index of topics and tasks (counts, statuses, last updated).
  - `globals/` - cross-project references maintained by agents:
    - `ARCHITECTURE.md` (Vireal) - module map + ADR links.
    - `UX-GUIDE.md` (Echo) - guard rails, accessibility, global patterns.
    - `TEST-STRATEGY.md` (Kai) - levels, tools, checklists.
    - `CHANGELOG.md` (Scribe) - product-relevant checkpoints with file references.
    - `RISKS.md` (Echo/Orin) - risks, owners, mitigations.
    - `INTEGRATIONS.md` (Nodus) - external services/APIs, event contracts.
    - `REFERENCES.json` (Orin) - canonical paths (docs, ADR, topics).
  - `topics/`
    - `TOPIC-YYYYMMDD_HHMMSS-<slug>/`
      - `topic.json` - meta: WHY/WHAT, owners, status.
      - `brief.json` - Mira 4MAT summary + open questions.
      - `PDCA.json` (or `pdca.md`) - topic PDCA.
      - `test-plan.json` - Kai's acceptance at topic scope.
      - `links.json` - pointers to ADR/UX/docs.
      - `tasks/`
        - `<TASK-ID-<slug>>/`
          - `task.json` - description, acceptance, links.
          - `PDCA.json` - plan/do/check/act for the task.
          - `log.jsonl` - Why/What/Next entries.
          - `log.md` - short checkpoints (optional).

## Practices
- Status changes only via `backlog/board.json` (use `scripts/board.py move ...`).
- PDCA/logs live with the task; topic PDCA documents cross-task coordination.
- Global files updated when cross-cutting decisions land (new ADR, UX guard rail, testing policy, integration change).
- Scribe mirrors key milestones into `backlog/globals/CHANGELOG.md` with path references.

## Roles (who updates what)
- Orin: `board.json`, `REFERENCES.json`, session coordination.
- Mira: `brief.json`, seeds `topic.json`.
- Vireal: `ARCHITECTURE.md`, ADR links, guardrails.
- Lumen: task PDCA/logs; links consistency.
- Kai: `TEST-STRATEGY.md`, topic `test-plan.json`, acceptance notes.
- Echo: `UX-GUIDE.md`, `RISKS.md`.
- Nodus: `INTEGRATIONS.md`, CI/CD notes where relevant.
- Scribe: `CHANGELOG.md` (with links to logs/tasks).

## Automation
- `scripts/validate.py` - validates boardtopics/tasks linkage and required files.
- `scripts/update_backlog_index.py` - regenerates `backlog/backlog.json` (counts, freshness, owners).
- (Optional) `scripts/globals_sync.py` - sanity checks that globals reflect latest ADR/UX/test changes.

## How To Add Work
- New topic
  - Create `backlog/topics/TOPIC-YYYYMMDD_HHMMSS-<slug>/` with `topic.json`, `BRIEF-*.json`.
  - Add topic entry to `backlog/board.json.topics` with `id`, `title`.
  - (Kai) Add `test-plan.json` (optional) and `event-checklist.json` (optional).
  - Run `python scripts/update_backlog_index.py`.
- New task
  - Add task entry to `backlog/board.json.tasks` with `id`, `title`, `topic`, `owner`.
  - Run `python scripts/validate.py --scaffold` to create `tasks/<TASK-ID>/PDCA.json` + `log.jsonl`.
  - Move status via `python scripts/board.py move <TASK-ID> in_progress|done`.

## Mechanism Coverage (Fit)
- Cos Lifecycle (Seed  Bud  Mature  Spawned)
  - Owner: Lumen (impl), Vireal (arch), Kai (tests)
  - Docs: `docs/architecture/COS-v0.1-architecture.md`
  - Topics/Tasks: see `backlog/topics/TOPIC-20251024_195056-001`
- Overlay (floating service)
  - Owner: Lumen/Nodus, Kai (instrumentation)
  - Docs: `docs/reference/floating-overlay.md`
  - Topics/Tasks: see `backlog/topics/TOPIC-20251025_000554-001`
- Morfogeneza (form editor + channel)
  - Owner: Lumen (UI/VM), Vireal (ADR), Echo (UX), Kai (plan)
  - Docs: `docs/architecture/ADR-2025-10-25-morfogeneza.md`, `docs/ux/morphogeneza-ux-research.md`
  - Topics/Tasks: see `backlog/topics/TOPIC-20251025_120000-001`
- UI Skin (tokens + theme)
  - Owner: Lumen, Vireal; Kai (tests)
  - Docs: `core/designsystem/src/main/res/raw/ui_tokens.json`
  - Topics/Tasks: see `backlog/topics/TOPIC-20251027_131500-001`

## Migration Notes
- Existing area trees (e.g., `backlog/morfogeneza/...`) can be moved under `backlog/topics/TOPIC-...-morphogenesis/` without changing task content. Update `board.json.links` to new paths.
- Keep legacy archives in `archive/legacy/` for reference only.

## Success Criteria
- Anyone can answer: what's in progress, where are artefacts, what changed last.
- New topic onboarding  2 minutes (scaffold + link in board).
- Zero duplication of status; minimal drift caught by `validate.py`.
