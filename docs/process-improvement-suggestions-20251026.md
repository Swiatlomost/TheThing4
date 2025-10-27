# AI Collaboration Improvement Notes – 2025-10-26

> Rekonesans po dokumentach operacyjnych (`WORKFLOW.md`, `AI_GUIDE.md`, `PROJECT_CONTEXT.md`, `AGENTS.md`, `docs/reference/*`, logach agentów i aktualnym `agents/status.json`) w celu uproszczenia współpracy z zespołem AI. Poniższe punkty dotyczą procesu – nie ingerują w kod aplikacji.

## 1. Automatyzacja tablicy statusów
- **Obserwacja:** `agents/status.json` wciąż bywa utrzymywany ręcznie; pojawiały się duplikaty wpisów (np. powtórzone zadania Nodusa) i rozjazdy z poszczególnymi `task.json`.
- **Propozycja:** rozszerzyć `scripts/validate-agent-sync.py`, aby:
  1. raportował duplikaty i nieużywane zadania,
  2. potrafił opcjonalnie wygenerować nową tablicę na podstawie `agents/*/task.json`.
- **Efekt:** mniej ręcznych aktualizacji Orina, mniejsze ryzyko, że agent (np. Echo) zostanie w `in_progress` mimo zamkniętego `task.json`.

## 2. „Slim PDCA” dla małych działań
- **Obserwacja:** zgodnie z `WORKFLOW.md` każdy agent powinien linkować pełny PDCA. W praktyce część wpisów (np. starsze logi Lumen/Kai) nie ma kart, bo dla drobnych zadań to zbyt ciężkie.
- **Propozycja:** dodać do `docs/templates/` krótką wersję PDCA (Plan/Check w dwóch zdaniach) i w `AI_GUIDE.md` doprecyzować, kiedy można z niej skorzystać.
- **Efekt:** spójność logów bez nadmiarowej biurokracji, łatwiejsze przechodzenie statusu `pending -> in_progress`.

## 3. Jedno „Playbook.md” zamiast rozsianych instrukcji
- **Obserwacja:** instrukcje są rozproszone po `AI_GUIDE.md`, `PROJECT_CONTEXT.md`, `WORKFLOW.md`, `docs/reference/session-timeline.md`. Wiele treści się powtarza (np. zasada „Next step”).
- **Propozycja:** utworzyć `docs/process/playbook.md`, w którym zebrane będą:
  - główne rytuały (SESSION START, PDCA, cooldown),
  - mapowanie odpowiedzialności agentów,
  - skrót zasad komunikacji.
  Pozostałe pliki mogą linkować do playbooka, a tam gdzie informacje się dublują – pozostawić tylko odnośniki.
- **Efekt:** nowa osoba (lub Ty po przerwie) szybciej odnajdzie aktualne reguły; mniejsza szansa na nieaktualne instrukcje.

## 4. Repozytorium briefów Storywrighta
- **Obserwacja:** `agents/storywright/briefs/` zawiera tylko historyczny plik `2025-10-24-cos-v0-1.json`; nowsze sesje są w `sessions/`. W `WORKFLOW.md` zakładamy, że brief trafia do katalogu Storywrighta.
- **Propozycja:** albo konsekwentnie odkładać wszystkie nowe briefy do `agents/storywright/briefs/SESSION-ID.md`, albo zaktualizować workflow, że jedynym źródłem jest `sessions/…`. W obu przypadkach warto dodać skrypt sprawdzający, czy plik briefu istnieje dla każdej aktywnej sesji.
- **Efekt:** brak starych instrukcji, łatwiejsze odnajdywanie briefów przy restartach.

## 5. Template dla raportów ROI / meta-dokumentów
- **Obserwacja:** pojawia się coraz więcej meta-plików (`docs/ai-first-development-roi*.md`, restart-briefy, teraz także ten dokument). Brakuje szablonu mówiącego, jak je opisywać czy numerować.
- **Propozycja:** przygotować `docs/templates/meta-report-template.md` z sekcjami: zakres, obserwacje, rekomendacje, wpływ na workflow. Dodać krótką notkę w `PROJECT_CONTEXT.md`, kiedy korzystać z template’u.
- **Efekt:** uporządkowane meta-dokumenty i spójne nagłówki, łatwiejsze filtrowanie zmian niezwiązanych z kodem aplikacji.

## 6. Automatyczne „Next step” / zakończenia
- **Obserwacja:** `AI_GUIDE.md` wymaga kończenia odpowiedzi linią „Next step”, ale nie jest to egzekwowane (część logów i komunikatów – także po stronie ludzi – kończy się zwykłym opisem).
- **Propozycja:** dodać poręczny snippet (np. `docs/templates/reply-footer.md`) lub zaszyć to w aliasach komunikacyjnych. Można też rozszerzyć walidację przed commit/push o sprawdzanie, czy log wpisany przez Orina kończy się sekcją „Next”.
- **Efekt:** równa jakość komunikacji, łatwiejsze śledzenie „co dalej”.

## 7. Harmonogram rewizji pamięci (Nyx)
- **Obserwacja:** `MEMORY_SPEC.md` mówi o `last_reviewed`, ale brak harmonogramu przypominającego Nyxowi o przeglądzie. W `agents/status.json` brak wpisu o zadaniu Nyx-a od czasu sprintu v0.1.
- **Propozycja:** dodać w `agents/status.md` sekcję „Recurring Maintenance” z tabelą (Agent → częstotliwość). Dla Nyx: np. co 2 tygodnie automatyczne otwarcie zadania `[AGENT::NYX] [TASK::LOG] Memory review`.
- **Efekt:** pamięć nie zestarzeje się w ciszy, a Ty nie musisz pamiętać o manualnym przypomnieniu.

---

### Podsumowanie / Next steps
1. Zdecydować, które propozycje wdrażamy w najbliższej iteracji (np. automatyzacja statusów + slim PDCA jako szybkie wygrane).
2. Jeśli akceptujesz powyższe, mogę przygotować taski dla odpowiednich agentów (Echo/Kai/Nyx/Scribe).
