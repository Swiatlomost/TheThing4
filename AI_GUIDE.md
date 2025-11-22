# AI_GUIDE.md - Praca z ChatGPT (v2.0)

## Spis treści
- Cel
- Bootstrapping
- Tryby odpowiedzi
- Wskazówki współpracy
- Higiena pamięci

## Cel
Utrzymuj żywy kontekst, aby GPT rozumiało ramy współpracy i mogło działać jako agenci zdefiniowani w `AGENTS.md`, zgodnie z lean‑strukturą.

## Bootstrapping
1. Poproś ChatGPT:
   ```
   Proszę przeczytać README.md, PROJECT_CONTEXT.md, AGENTS.md, WORKFLOW.md, MEMORY_SPEC.md, AI_GUIDE.md.
   Zostań w tym kontekście dopóki nie powiem inaczej.
   ```
2. Wywołaj `[SESSION::START]` — `scripts/session_start_hook.ps1` promuje pierwsze `pending` z `backlog/board.json` do `in_progress`.
3. Gdy potrzebujesz rozmowy o wizji, zaproś `[AGENT::MIRA] [TASK::ANALYZE]` — Storywright aktualizuje `backlog/topics/TOPIC-YYYYMMDD_HHMMSS-<slug>/BRIEF-*.json`.
4. Orin zarządza planem poprzez `board.json` (dodaje/ustawia statusy) i linkuje do PDCA w folderach zadań.

## Tryby odpowiedzi
- **PRO**: wykonanie, listy punktowane, konkretne następne kroki.
- **META**: refleksja, sprawdzenie relacji, szersze pytania.

## Wskazówki współpracy
- `[SESSION::CONTINUE]` — sprawdź postęp agentów i wznow sesję (asystent raportuje stan, Orin potwierdza „kontynuuj” lub „zatwierdzam”).
- Potwierdź bieżący ID zadania przed zmianą plików.
- Wypełnij PDCA w `backlog/topics/TOPIC-.../tasks/<TASK-ID>/pdca.md` zanim ustawisz zadanie na `in_progress`.
- Przy kadrowaniu pracy zajrzyj do `docs/reference/` (architektura, UX, overlay, tooling).
- W razie wątpliwości — zaproponuj warianty z ryzykami zamiast pauzy.
- Korzystaj z szablonów w `docs/templates/` dla ustrukturyzowanych wyników.
- Kończ wiadomości sekcją `Next step:` aby Orin mógł planować przepływ.
- Po `[SESSION::START]` asystent operuje na `board.json` i `backlog/topics/`; Orin reaguje na wyniki, decyzje i eskalacje.

## Higiena pamięci
- Nyx utrzymuje `agents/memory.json` (wpisy pod kluczem `agents.{Name}`) w zgodzie z decyzjami procesu.
- Po istotnych zmianach dopisz co się zmieniło w logu zadania.
- Gdy potwierdzasz, że wiedza jest aktualna, dopisz `last_reviewed` (data) w odpowiednim wpisie.
- Użyj `python scripts/validate.py`, aby wykryć niespójne odwołania.

## Hygiene-First Development (NEW - 2025-11-22)

### Pre-Session Checklist (Mandatory)

Before starting ANY work, run:
```bash
python scripts/check_hygiene.py
```

If check fails, **address hygiene issues BEFORE new work**:

1. **Memory Staleness (>7 days)**:
   - Read recent PDCA.json + log.jsonl for stale agents
   - Update their context/insights/todos/risks
   - Update last_updated to today (YYYY-MM-DD)

2. **Long In-Progress Tasks (>7 days)**:
   - Review with Orin: break down or checkpoint?
   - Update PDCA `do` section with interim progress
   - If blocked >3 attempts → escalate to architectural review (Vireal)

3. **Chronicle Gap (>14 days)**:
   - Identify recent done tasks not in chronicle
   - Write milestone entry (Scene, Plot Beats, Artefacts, Cliffhanger)
   - Or escalate to Scribe

### Embedded Behavioral Rules

1. **After completing a task → Update memory + chronicle IMMEDIATELY**
   - Don't batch updates
   - "Done" means: code + tests + evidence + PDCA + memory + chronicle

2. **If debugging >3 attempts → Escalate to architecture review**
   - Tag `[ARCH::REVIEW]` in log.jsonl
   - Notify Vireal + Echo (add to their memory todos)
   - STOP trial-and-error debugging

3. **Every 3-5 days in_progress → Checkpoint**
   - Update PDCA `do` section
   - Add log.jsonl entry with interim progress
   - If blocked, escalate to Orin

4. **Before starting new work → Check hygiene**
   - Run check_hygiene.py
   - Fix issues first
   - Prevents debt accumulation

### Example: Hygiene-Driven Session

```bash
# Session start
python scripts/check_hygiene.py

# Output:
⚠️ Nyx: memory stale (12 days, guideline ≤7 days)
⚠️ POI-213: in_progress 6 days (approaching limit)

# AI Response:
"Before continuing work, I see 2 hygiene issues:
1. Nyx memory 12 days stale - updating now with POI-210..213 context
2. POI-213 approaching 7-day limit - adding checkpoint to PDCA

After addressing hygiene, I'll continue with POI-213 nonce debugging."
```

### Definition of Done (Updated)

Task is "done" when ALL completed:
- [ ] Code/artifact delivered
- [ ] Tests passing
- [ ] Evidence collected (logcat, tests, benchmarks)
- [ ] PDCA.json: `plan`, `do`, `check` filled
- [ ] log.jsonl: final entry with `done` tag
- [ ] **agents/memory.json updated for involved agents**
- [ ] **Chronicle entry added**
- [ ] Status moved to `done` in board.json

**If ANY checkbox unchecked → NOT done.**
