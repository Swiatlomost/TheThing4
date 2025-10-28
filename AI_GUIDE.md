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
