# WORKFLOW.md - Process (Lean v3)

## Session Ritual
1. `[SESSION::START]` — uruchom `python scripts/validate.py`; następnie `pwsh scripts/session_start_hook.ps1` promuje pierwsze `pending` w `backlog/board.json` do `in_progress`.
2. Mira (Storywright) destyluje rozmowę i aktualizuje `topics/<TOPIC>/brief.json`.
3. Orin zarządza planem w `board.json` (dodaje zadania, ustawia statusy), linkuje PDCA per zadanie w `topics/<TOPIC>/tasks/<TASK-ID>/`.
4. Echo & Vireal analizują i proponują opcje (zapisy w PDCA/logu zadania).
5. Lumen & Nodus implementują i integrują (log per zadanie).
6. Kai weryfikuje jakość i testy (test-plan w katalogu zadania lub `topics/<TOPIC>/tests/`).
7. Scribe dopisuje rozdziały do `reports/chronicle.md` (fakty z logów zadań).
8. Nyx aktualizuje `agents/<name>/memory.json` po istotnych zmianach.

Before switching a task to `in_progress`, the owner fills PDCA in `topics/<...>/tasks/<TASK-ID>/pdca.md` and notes the intent in `log.md` (see PDCA Anchor in `docs/reference/session-timeline.md`).

## Pre-task Validation Checklist
- VS Code task: `Validate Lean Structure` (uruchamia `python scripts/validate.py`).
- [ ] `board.json` parsuje się poprawnie i nie ma duplikatów ID.
- [ ] Każde zadanie ma właściciela i status.
- [ ] Linki zadań wskazują na istniejące pliki (PDCA/log/brief/test-plan — gdzie dotyczy).
- [ ] Pamięci agentów mają świeży `last_updated` lub `last_reviewed` (<= 3 dni).
- [ ] Skrypty działają bez błędów.

If the checklist fails: pause work, fix discrepancies, log the incident (in the relevant task `log.md`), rerun validation.

## Communication Rules
- Finish replies with a **Next step** line.
- Decisions follow the pattern `Why -> What -> Next`.
- Provide evidence (`file:line`) when raising risks or blockers.
- Keep prompts short using tags `[TASK::...]`, `[AGENT::...]`, `[MODE::...]` when possible.

## Definition of Done
- Artefakt dostarczony i zreviewowany (Kai) — link w `board.json`.
- Dodatkowe wymagania produktowe (`docs/reference/`) stosowane tylko dla powiązanych zadań.
- Log per zadanie uzupełniony (Why → Next), kronika zaktualizowana.
- Status w `board.json` ustawiony na `done`.
- Pamięć zaktualizowana jeśli wpływa na przyszłe decyzje.

## Symptoms of Drift
- Brak spójności między `board.json` a folderami `topics/`.
- Logi per zadanie nie zawierają decyzji przy dużych zmianach.
- Pamięci agentów referują przestarzałe reguły.

When drift occurs, Orin leads a correction pass and documents the fix in the relevant task `log.md` and updates `board.json`.
