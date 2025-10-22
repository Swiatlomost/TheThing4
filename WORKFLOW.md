# WORKFLOW.md - Proces pracy z agentami (v1.1)

## Rytual sesji
1. `[SESSION::START]` - Orin zbiera cel, nadaje identyfikator zadania (`ORIN-YYYYMMDD-XXX`) i deleguje pod-zadania.
2. Analiza -> `[TASK::ANALYZE]` do Echo / Vireal.
3. Budowa -> `[TASK::BUILD]` do Lumen / Nodus.
4. Przeglad -> `[TASK::REVIEW]` do Kai.
5. Dziennik -> `[TASK::LOG]` do Scribe.
6. Pamiec / snapshoty -> Nyx.

## Konwencje komunikacji
- Odpowiedzi konczymy sekcja **Next step**.
- Decyzje zapisujemy jako: *Decyzja -> Powod -> Nastepny krok (opcjonalnie alternatywy)*.
- Gdy kontekst jest niepelny, agent proponuje rozwiazanie wraz z ryzykami - nie blokuje.
- Kazdy agent utrzymuje w katalogu `agents/<name>/` pliki:
  - `log.md` - biezacy wpis + szablon archiwum,
  - `task.json` - lista zadan (status, identyfikatory, powiazania),
  - `memory.json` - sposob wspolpracy, kontekst i heurystyki.
- `agents/status.md` jest tablica kontrolna: agent, rola, task_id, status, timestamp.
- Scribe prowadzi faktograficzny log w `agents/scribe/log.md`, a prozowa kronika (humor/epika/Grzesiuk) trafia do `agents/scribe/chronicle.md`.

## Przyklady promptow
- `"[SESSION::START] Prosze zebrac cele na dzis i zainicjalizowac dziennik."`
- `"[AGENT::ECHO] [TASK::ANALYZE] Przejrzyj PROJECT_CONTEXT.md i wypisz ryzyka."`
- `"[AGENT::LUMEN] [TASK::BUILD] Przygotuj szkic widoku Device Info."`
- `"[AGENT::SCRIBE] [TASK::LOG] Zanotuj podsumowanie sesji i TODO."`

## Definicja "Done"
- Artefakt + test/review (Kai) + wpis w dzienniku (Scribe) + aktualizacja `task.json` i `memory.json`.

## Cooldown po zadaniu
1. Orin zamyka zadanie w `agents/orin/log.md` / `task.json` i aktualizuje `agents/status.md`.
2. Scribe dopisuje podsumowanie (log + kronika) oraz oznacza swoje zadanie jako DONE.
3. Orin ze Scribe przechodza checkliste zamkniecia: task.json -> log.md (status/notes) -> `agents/status.md`.
4. Nyx, gdy zaszla istotna zmiana stanu, aktualizuje `memory.json` i snapshot.
5. Opcjonalnie archiwizujemy starsze logi do `log-archive/`, aby krotki kontekst byl swiezy.

