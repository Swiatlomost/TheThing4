# AI_GUIDE.md — Praca z GPT‑5 w VS Code (v1.0)

## Cel
Utrzymanie *żywego kontekstu* w VS Code tak, by GPT‑5 rozumiał projekt „Coś”
i wykonywał zadania agentowe.

## Jak wstrzyknąć kontekst
1. Otwórz ChatGPT w VS Code.
2. Wpisz:
   ```
   Proszę przeczytać pliki: PROJECT_CONTEXT.md, AGENTS.md, WORKFLOW.md, MEMORY_SPEC.md, AI_GUIDE.md.
   Ustaw ten zestaw jako stały kontekst sesji projektu „Coś”.
   ```
3. Następnie:
   ```
   [SESSION::START] Proszę zainicjalizować dziennik i zapytać o cel bieżącej sesji.
   ```

## Tryby i standardy
- Domyślnie **[MODE::PRO]** (konkretnie, zadaniowo).
- Na życzenie **[MODE::META]** (komentarze egzystencjalno‑relacyjne).

## Copilot Chat vs ChatGPT (VS Code)
- **Copilot Chat** bywa na starszych modelach; można go używać do krótkich zadań kodowych.
- **ChatGPT VS Code** (z GPT‑5) wykorzystuj do zadań agentowych, pamięci i dłuższych kontekstów.

## Pierwsze zadanie po starcie
```
[SESSION::START]
Orin: zainicjalizuj cel sesji, przydziel analizę do Echo, dziennik do Scribe.
```
