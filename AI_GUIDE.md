# AI_GUIDE.md - Working With ChatGPT (v2.0)

## Goal
Maintain a live context so GPT understands the collaboration framework and can act as the agents defined in `AGENTS.md` using the lean structure.

## Bootstrapping
1. Ask ChatGPT:
   ```
   Prosze przeczytac README.md, PROJECT_CONTEXT.md, AGENTS.md, WORKFLOW.md, MEMORY_SPEC.md, AI_GUIDE.md.
   Zostan w tym kontekscie dopoki nie powiem inaczej.
   ```
2. Trigger `[SESSION::START]` — `scripts/session_start_hook.ps1` promuje pierwsze `pending` z `backlog/board.json` do `in_progress`.
3. Gdy potrzebujesz rozmowy o wizji, zapros `[AGENT::MIRA] [TASK::ANALYZE]` — Storywright aktualizuje `backlog/<obszar>/topics/TOPIC-YYYYMMDD_HHMMSS-n/BRIEF-*.json`.
4. Orin zarządza planem poprzez `board.json` (dodaje/ustawia statusy) i linkuje do PDCA w folderach zadań.

## Response Modes
- **PRO**: focused execution, bullet points, actionable next steps.
- **META**: reflection, relationship checks, broader questions.

## Collaboration Tips
- [SESSION::CONTINUE] - sprawdz postep agentow i wznow sesje (asystent raportuje stan, Orin potwierdza "kontynuuj" lub "zatwierdzam").
- Confirm the current task ID before modifying files.
- Wypelnij PDCA w `backlog/<obszar>/topics/TOPIC-.../tasks/<TASK-ID>/pdca.md` zanim oznaczysz zadanie jako `in_progress`.
- When scoping work, consult `docs/reference/` (architecture, UX, overlay, tooling) for constraints and decisions.
- When uncertain, propose options plus risks instead of pausing work.
- Use the templates in `docs/templates/` for structured outputs.
- Close every message with `Next step:` so Orin can plan the flow.
- Po `[SESSION::START]` asystent operuje na `board.json` i katalogach `topics/`; Orin reaguje tylko na wyniki, decyzje i eskalacje.

## Memory Hygiene
- Nyx utrzymuje `agents/memory.json` (wpisy pod kluczem `agents.{Name}`) w zgodzie z decyzjami procesu.
- Po istotnych zmianach dopisz co się zmieniło w logu zadania.
- Gdy tylko potwierdzasz, że wiedza jest aktualna, dopisz `last_reviewed` (data) w odpowiednim wpisie.
- Użyj `python scripts/validate.py` do wykrycia niespójnych odwołań.
