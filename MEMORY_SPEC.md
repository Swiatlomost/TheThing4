# MEMORY_SPEC.md - Pamiec i dzienniki (v1.1)

## Struktura per agent
Kazdy agent posiada katalog `agents/<name>/` zawierajacy:

| Plik | Przeznaczenie |
| ---- | ------------- |
| `log.md` | Biezacy wpis (sekcja **Active Task**) oraz szablon archiwum. Logujemy start/stop, fakty i decyzje. |
| `task.json` | Lista zadan z identyfikatorami (`AGENT-YYYYMMDD-XXX`), powiazaniem z zadaniem Orina oraz statusem `pending / in_progress / done`. |
| `memory.json` | Opis sposobu wspolpracy: fokus, ustalenia globalne, heurystyki agenta, ryzyka do monitorowania. |

Tablica `agents/status.md` agreguje najwazniejsze pola z `task.json`, by Orin widzial stan calego zespolu.

## Minimalny schemat `memory.json`
```json
{
  "agent": "Echo",
  "last_updated": "YYYY-MM-DD",
  "focus": "Obszar odpowiedzialnosci",
  "context": ["Najwazniejsze ustalenia wspolpracy"],
  "insights": ["Wnioski / heurystyki"],
  "todos": ["Nastepne dzialania"],
  "risks": ["Ryzyka do monitorowania"],
  "links": ["agents/status.md"],
  "_template": { "...": "instrukcje utrzymania szablonu" }
}
```

## Zasady pracy z pamiecia
- Zwiezlosc ponad objetosc - krotkie frazy zamiast dlugich akapitow.
- Aktualizujemy `memory.json` po istotnej decyzji lub zmianie procesu.
- Nyx dba o konsolidacje i snapshoty, szczegolnie przed wiekszymi milestone'ami.
- Jesli zmieniamy standard, synchronizujemy logi, `task.json` oraz tablice statusow.

## Przyklad wpisu Scribe
```
## 2025-10-21 - Sesja #01
Cel: inicjalizacja kontekstu w VS Code.
Decyzje: tryb PRO jako domyslny, META na zyczenie; zadanie ORIN-20251021-001 rozbite na Echo/Nodus/Scribe.
TODO: [ ] NODUS-20251021-001 - checklisty build APK (due 2025-10-22)
Next: Orin zbiera wyniki i planuje testy.
```
