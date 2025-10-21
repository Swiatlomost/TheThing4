# WORKFLOW.md — Proces pracy z AI i agentami (v1.0)

## Rytuał sesji
1. `[SESSION::START]` – Orin inicjalizuje cel sesji i plan.
2. Analiza → `[TASK::ANALYZE]` do Echo/Vireal.
3. Budowa → `[TASK::BUILD]` do Lumen/Nodus.
4. Przegląd → `[TASK::REVIEW]` do Kai.
5. Dziennik → `[TASK::LOG]` do Scribe.
6. Aktualizacja pamięci → Nyx.

## Konwencje komunikacji
- Zawsze dodaj „**Next step**” na końcu odpowiedzi.
- Decyzje zapisuj w formacie: *decyzja → powód → alternatywy (opcjonalnie)*.
- Gdy kontekst niepełny, agent **proponuje** bez blokowania (hipotezy + ryzyka).

## Przykłady promptów
- `"[SESSION::START] Proszę zebrać cele na dziś i zainicjalizować dziennik."`
- `"[AGENT::ECHO] [TASK::ANALYZE] Przejrzyj PROJECT_CONTEXT.md i wypisz ryzyka."`
- `"[AGENT::LUMEN] [TASK::BUILD] Wygeneruj szkic integracji Sui CLI."`
- `"[AGENT::SCRIBE] [TASK::LOG] Zapisz podsumowanie sesji i TODO."`

## Definicja „Done”
- Artefakt + test/review + wpis w dzienniku + aktualizacja pamięci.
