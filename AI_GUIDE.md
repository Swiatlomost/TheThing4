# AI_GUIDE.md - Praca z GPT-5 w VS Code (v1.1)

## Cel
Utrzymanie *zywego kontekstu* w VS Code tak, by GPT-5 rozumial projekt "Cos" i wykonywal zadania agentowe w oparciu o strukture `log/task/memory` kazdego agenta.

## Jak wstrzyknac kontekst
1. Otworz ChatGPT w VS Code.
2. Polec modelowi:
   ```
   Prosze przeczytac pliki: PROJECT_CONTEXT.md, AGENTS.md, WORKFLOW.md, MEMORY_SPEC.md, AI_GUIDE.md.
   Ustaw ten zestaw jako staly kontekst sesji projektu "Cos".
   ```
3. Rozpocznij sesje poleceniem:
   ```
   [SESSION::START] Prosze zainicjalizowac dziennik i zapytac o cel biezacej sesji.
   ```
4. Orin nadaje identyfikator zadaniu glownemu i deleguje pod-zadania (Echo, Nodus, Scribe itd.).

## Tryby i standardy
- Domyslnie **[MODE::PRO]** (konkretnie, zadaniowo).
- Na zyczenie **[MODE::META]** (komentarze meta / relacyjne).
- Kazda odpowiedz konczy sie sekcja **Next step**.

## Copilot Chat vs ChatGPT (VS Code)
- **Copilot Chat** (starsze modele) - krotkie zadania kodowe, szybkie snippety.
- **ChatGPT VS Code** (GPT-5) - praca agentowa, pamiec, dluzsze konteksty.

## Pierwsze zadanie po starcie
```
[SESSION::START]
Orin: zainicjalizuj cel sesji, przydziel analize Echo, checklisty Nodus i log do Scribe.
```

## Utrzymanie stanu
- Po kazdej delegacji aktualizujemy `agents/status.md` oraz `task.json` danego agenta.
- Nyx dopilnowuje, by zmiany w pamieciach byly spojne z dokumentami `.md`.
