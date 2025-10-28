# AI Collaboration Improvement Notes - 2025-10-26

> Rekonesans po dokumentach operacyjnych (`WORKFLOW.md`, `AI_GUIDE.md`, `PROJECT_CONTEXT.md`, `AGENTS.md`, `docs/reference/*`, logach agentow i aktualnym `backlog/board.json`; `agents/status.json` traktuj jako legacy) w celu uproszczenia wspolpracy z zespolem AI. Ponizsze punkty dotycza procesu - nie ingeruja w kod aplikacji.

## 1. Automatyzacja tablicy statusow
- **Obserwacja:** `agents/status.json` (legacy) wciaz bywa utrzymywany recznie; pojawialy sie duplikaty wpisow (np. powtorzone zadania Nodusa) i rozjazdy z poszczegolnymi `task.json`.
- **Propozycja:** rozszerzyc `scripts/validate-agent-sync.py`, aby:
  1. raportowal duplikaty i nieuzywane zadania,
  2. potrafil opcjonalnie wygenerowac nowa tablice na podstawie `agents/*/task.json`.
- **Efekt:** mniej recznych aktualizacji Orina, mniejsze ryzyko, ze agent (np. Echo) zostanie w `in_progress` mimo zamknietego `task.json`.

## 2. Slim PDCA" dla malych dzialan
- **Obserwacja:** zgodnie z `WORKFLOW.md` kazdy agent powinien linkowac pelny PDCA. W praktyce czesc wpisow (np. starsze logi Lumen/Kai) nie ma kart, bo dla drobnych zadan to zbyt ciezkie.
- **Propozycja:** dodac do `docs/templates/` krotka wersje PDCA (Plan/Check w dwoch zdaniach) i w `AI_GUIDE.md` doprecyzowac, kiedy mozna z niej skorzystac.
- **Efekt:** spojnosc logow bez nadmiarowej biurokracji, latwiejsze przechodzenie statusu `pending -> in_progress`.

## 3. Jedno Playbook.md" zamiast rozsianych instrukcji
- **Obserwacja:** instrukcje sa rozproszone po `AI_GUIDE.md`, `PROJECT_CONTEXT.md`, `WORKFLOW.md`, `docs/reference/session-timeline.md`. Wiele tresci sie powtarza (np. zasada Next step").
- **Propozycja:** utworzyc `docs/process/playbook.md`, w ktorym zebrane beda:
  - glowne rytualy (SESSION START, PDCA, cooldown),
  - mapowanie odpowiedzialnosci agentow,
  - skrot zasad komunikacji.
  Pozostale pliki moga linkowac do playbooka, a tam gdzie informacje sie dubluja - pozostawic tylko odnosniki.
- **Efekt:** nowa osoba (lub Ty po przerwie) szybciej odnajdzie aktualne reguly; mniejsza szansa na nieaktualne instrukcje.

## 4. Repozytorium briefow Storywrighta
- **Obserwacja:** `agents/storywright/briefs/` zawiera tylko historyczny plik `2025-10-24-cos-v0-1.json`; nowsze sesje sa w `sessions/`. W `WORKFLOW.md` zakladamy, ze brief trafia do katalogu Storywrighta.
- **Propozycja:** albo konsekwentnie odkladac wszystkie nowe briefy do `agents/storywright/briefs/SESSION-ID.md`, albo zaktualizowac workflow, ze jedynym zrodlem jest `sessions/...`. W obu przypadkach warto dodac skrypt sprawdzajacy, czy plik briefu istnieje dla kazdej aktywnej sesji.
- **Efekt:** brak starych instrukcji, latwiejsze odnajdywanie briefow przy restartach.

## 5. Template dla raportow ROI / meta-dokumentow
- **Obserwacja:** pojawia sie coraz wiecej meta-plikow (`docs/ai-first-development-roi*.md`, restart-briefy, teraz takze ten dokument). Brakuje szablonu mowiacego, jak je opisywac czy numerowac.
- **Propozycja:** przygotowac `docs/templates/meta-report-template.md` z sekcjami: zakres, obserwacje, rekomendacje, wplyw na workflow. Dodac krotka notke w `PROJECT_CONTEXT.md`, kiedy korzystac z template'u.
- **Efekt:** uporzadkowane meta-dokumenty i spojne naglowki, latwiejsze filtrowanie zmian niezwiazanych z kodem aplikacji.

## 6. Automatyczne Next step" / zakonczenia
- **Obserwacja:** `AI_GUIDE.md` wymaga konczenia odpowiedzi linia Next step", ale nie jest to egzekwowane (czesc logow i komunikatow - takze po stronie ludzi - konczy sie zwyklym opisem).
- **Propozycja:** dodac poreczny snippet (np. `docs/templates/reply-footer.md`) lub zaszyc to w aliasach komunikacyjnych. Mozna tez rozszerzyc walidacje przed commit/push o sprawdzanie, czy log wpisany przez Orina konczy sie sekcja Next".
- **Efekt:** rowna jakosc komunikacji, latwiejsze sledzenie co dalej".

## 7. Harmonogram rewizji pamieci (Nyx)
- **Obserwacja:** `MEMORY_SPEC.md` mowi o `last_reviewed`, ale brak harmonogramu przypominajacego Nyxowi o przegladzie. W `backlog/board.json` brak wpisu o zadaniu Nyx-a od czasu sprintu v0.1.
- **Propozycja:** dodac w `agents/status.md` sekcje Recurring Maintenance" z tabela (Agent  czestotliwosc). Dla Nyx: np. co 2 tygodnie automatyczne otwarcie zadania `[AGENT::NYX] [TASK::LOG] Memory review`.
- **Efekt:** pamiec nie zestarzeje sie w ciszy, a Ty nie musisz pamietac o manualnym przypomnieniu.

---

### Podsumowanie / Next steps
1. Zdecydowac, ktore propozycje wdrazamy w najblizszej iteracji (np. automatyzacja statusow + slim PDCA jako szybkie wygrane).
2. Jesli akceptujesz powyzsze, moge przygotowac taski dla odpowiednich agentow (Echo/Kai/Nyx/Scribe).
