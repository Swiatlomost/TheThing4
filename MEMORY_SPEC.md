# MEMORY_SPEC.md — Pamięć i dzienniki (v1.0)

## Struktura
- `memory/<agent>.json` — krótkie fakty, ostatnie decyzje, TODO.
- `agents/<agent>/log.md` — narracyjny dziennik działań i wniosków.

## Schemat pamięci JSON (minimalny)
```json
{
  "agent": "Echo",
  "last_updated": "YYYY-MM-DD",
  "context": ["krótki opis kontekstu"],
  "insights": ["wniosek 1", "wniosek 2"],
  "todos": ["kolejny krok"],
  "risks": ["ryzyko 1"]
}
```

## Zasady
- Zwięzłość: krótkie frazy zamiast pełnych akapitów.
- Aktualizacja po każdej istotnej decyzji lub zmianie kierunku.
- Snapshot co większy milestone (Nyx konsoliduje pliki).

## Przykład wpisu w dzienniku (Scribe)
```
## 2025-10-21 — Sesja #01
Cel: inicjalizacja kontekstu w VS Code.
Decyzje: ustawiamy tryb PRO jako domyślny, META na żądanie.
TODO: przygotować szkic integracji Sui CLI.
Next: Echo analizuje ryzyka.
```
