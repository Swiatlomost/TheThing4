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
4. **Orin Session Procedure** (OBOWIĄZKOWA dla nowych zadań):
   ```
   Orin: Zainicjuj sesję i załaduj pamięć z poprzednich prac. 
   Opisz szczegółowo wymagania oraz kryteria sukcesu dla zadania ORIN-YYYYMMDD-XXX, 
   następnie przypisz poszczególne zadania dla agentów.
   ```
5. Orin tworzy plik `sessions/ORIN-YYYYMMDD-XXX-{nazwa}.md` z kompletną specyfikacją i deleguje pod-zadania (Echo, Vireal, Lumen, Kai, Scribe, Nyx).

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
Orin: zainicjalizuj cel sesji, załaduj pamięć poprzednich prac, 
utwórz plik sessions/ z pełną specyfikacją i przydziel delegacje wszystkim agentom.
```

## Standard pliku sesji
**Każde zadanie ORIN-YYYYMMDD-XXX musi mieć:**
- Plik `sessions/ORIN-YYYYMMDD-XXX-{nazwa}.md`
- Szczegółowe wymagania funkcjonalne
- Kryteria sukcesu (funkcjonalne/techniczne/jakościowe)
- Precyzyjne delegacje dla każdego agenta
- Status realizacji z checkpointami
- Ryzyka i mitygacje

## Utrzymanie stanu
- Po kazdej delegacji aktualizujemy `agents/status.md` oraz `task.json` danego agenta.
- Nyx dopilnowuje, by zmiany w pamieciach byly spojne z dokumentami `.md`.
