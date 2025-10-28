# WORKFLOW.md - Process (Lean v3)

Note: zobacz `BACKLOG_VISION.md` dla struktury backlogu (globals → topics → tasks). Wszystkie foldery topiców znajdują się w `backlog/topics/`.

## Session Ritual
1. `[SESSION::START]` — uruchom `python scripts/validate.py`; następnie `pwsh scripts/session_start_hook.ps1` promuje pierwsze `pending` w `backlog/board.json` do `in_progress`.
2. Mira (Storywright) destyluje rozmowę i aktualizuje `backlog/topics/<TOPIC>/brief.json`.
3. Orin zarządza planem w `board.json` (dodaje zadania, ustawia statusy), linkuje PDCA per zadanie w `backlog/topics/<TOPIC>/tasks/<TASK-ID>/`.
4. Echo & Vireal analizują i proponują opcje (zapisy w PDCA/logu zadania).
5. Lumen & Nodus implementują i integrują (log per zadanie).
6. Kai weryfikuje jakość i testy (test-plan w katalogu zadania lub `backlog/topics/<TOPIC>/tests/`).
7. Scribe dopisuje rozdziały do `reports/chronicle.md` (fakty z logów zadań).
8. Nyx aktualizuje `agents/<name>/memory.json` po istotnych zmianach.

Przed ustawieniem zadania na `in_progress`, właściciel uzupełnia PDCA w `backlog/topics/<TOPIC>/tasks/<TASK-ID>/pdca.md` i zapisuje intencję w `log.md` (zob. PDCA Anchor w `docs/reference/session-timeline.md`).

## Pre-task Validation Checklist
- VS Code task: `Validate Lean Structure` (uruchamia `python scripts/validate.py`).
- [ ] `board.json` parsuje się poprawnie i nie ma duplikatów ID.
- [ ] Każde zadanie ma właściciela i status.
- [ ] Linki zadań wskazują na istniejące pliki (PDCA/log/brief/test-plan — gdzie dotyczy).
- [ ] Pamięci agentów mają świeży `last_updated` lub `last_reviewed` (≤ 3 dni).
- [ ] Skrypty działają bez błędów.

Jeśli checklista nie przejdzie: przerwij pracę, popraw rozbieżności, zanotuj incydent (w `log.md` danego zadania), uruchom walidację ponownie.

## Communication Rules
- Kończ odpowiedzi sekcją **Next step**.
- Decyzje zapisuj wg schematu `Why -> What -> Next`.
- Dodawaj dowody (`plik:linia`) przy ryzykach/blokadach.
- Używaj tagów `[TASK::...]`, `[AGENT::...]`, `[MODE::...]` dla skróconych promptów.

## Definition of Done
- Artefakt dostarczony i zrecenzowany (Kai) — link w `board.json`.
- Dodatkowe wymagania produktowe (`docs/reference/`) stosowane tylko dla powiązanych zadań.
- Log per zadanie uzupełniony (Why -> Next), kronika zaktualizowana.
- Status w `board.json` ustawiony na `done`.
- Pamięć zaktualizowana, jeśli wpływa na przyszłe decyzje.

## Symptoms of Drift
- Brak spljnoci midzy `board.json` a folderami `backlog/topics/`.
- Logi per zadanie nie zawieraj... decyzji przy duych zmianach.
- Pamici agentlw referuj... przestarzae reguy.

When drift occurs, Orin leads a correction pass and documents the fix in the relevant task `log.md` and updates `board.json`.



# WORKFLOW.md - Process (Lean v3)

Note: zobacz `BACKLOG_VISION.md` dla struktury backlogu (globals → topics → tasks). Wszystkie foldery topiców znajdują się w `backlog/topics/`.

## Spis treści
- Session Ritual
- Pre-task Validation Checklist
- Communication Rules
- Definition of Done
- Symptoms of Drift

## Session Ritual
1. `[SESSION::START]` — uruchom `python scripts/validate.py`; następnie `pwsh scripts/session_start_hook.ps1` promuje pierwsze `pending` w `backlog/board.json` do `in_progress`.
2. Mira (Storywright) destyluje rozmowę i aktualizuje `backlog/topics/<TOPIC>/brief.json`.
3. Orin zarządza planem w `board.json` (dodaje zadania, ustawia statusy), linkuje PDCA per zadanie w `backlog/topics/<TOPIC>/tasks/<TASK-ID>/`.
4. Echo & Vireal analizują i proponują opcje (zapisy w PDCA/logu zadania).
5. Lumen & Nodus implementują i integrują (log per zadanie).
6. Kai weryfikuje jakość i testy (test-plan w katalogu zadania lub `backlog/topics/<TOPIC>/tests/`).
7. Scribe dopisuje rozdziały do `reports/chronicle.md` (fakty z logów zadań).
8. Nyx aktualizuje `agents/<name>/memory.json` po istotnych zmianach.

Przed ustawieniem zadania na `in_progress`, właściciel uzupełnia PDCA w `backlog/topics/<TOPIC>/tasks/<TASK-ID>/pdca.md` i zapisuje intencję w `log.md` (zob. PDCA Anchor w `docs/reference/session-timeline.md`).

## Pre-task Validation Checklist
- VS Code task: `Validate Lean Structure` (uruchamia `python scripts/validate.py`).
- [ ] `board.json` parsuje się poprawnie i nie ma duplikatów ID.
- [ ] Każde zadanie ma właściciela i status.
- [ ] Linki zadań wskazują na istniejące pliki (PDCA/log/brief/test-plan — gdzie dotyczy).
- [ ] Pamięci agentów mają świeży `last_updated` lub `last_reviewed` (≤ 3 dni).
- [ ] Skrypty działają bez błędów.

Jeśli checklista nie przejdzie: przerwij pracę, popraw rozbieżności, zanotuj incydent (w `log.md` danego zadania), uruchom walidację ponownie.

## Communication Rules
- Kończ odpowiedzi sekcją **Next step**.
- Decyzje zapisuj wg schematu `Why -> What -> Next`.
- Dodawaj dowody (`plik:linia`) przy ryzykach/blokadach.
- Używaj tagów `[TASK::...]`, `[AGENT::...]`, `[MODE::...]` dla skróconych promptów.

## Definition of Done
- Artefakt dostarczony i zrecenzowany (Kai) — link w `board.json`.
- Dodatkowe wymagania produktowe (`docs/reference/`) stosowane tylko dla powiązanych zadań.
- Log per zadanie uzupełniony (Why -> Next), kronika zaktualizowana.
- Status w `board.json` ustawiony na `done`.
- Pamięć zaktualizowana, jeśli wpływa na przyszłe decyzje.

## Symptoms of Drift
- Brak spójności między `board.json` a folderami `backlog/topics/`.
- Logi per zadanie nie zawierają decyzji przy dużych zmianach.
- Pamięci agentów referują przestarzałe reguły.

Gdy pojawi się drift, Orin prowadzi korektę i dokumentuje poprawkę w `log.md` zadania oraz aktualizuje `board.json`.
