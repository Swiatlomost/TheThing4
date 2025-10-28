# MEMORY_SPEC.md - Memory & Journals (Lean v3)

## Spis treści
- Memory Store
- Memory JSON Skeleton
- Hygiene
- Sample Task Log Snippet

## Memory Store
Prefer a single shared file `agents/memory.json` with per-agent entries under `agents` map. Example:

```
{
  "version": "1.0",
  "last_updated": "YYYY-MM-DD",
  "agents": {
    "Orin": { ... },
    "Echo": { ... }
  }
}
```

Legacy per-agent files `agents/<name>/memory.json` sa akceptowane (odczyt wsteczny) i beda wygaszane.

Global task status is maintained in `backlog/board.json`. Per-task logs and PDCA live under `backlog/topics/<TOPIC>/tasks/<TASK-ID>/`.

## Memory JSON Skeleton
```
{
  "agent": "Echo",
  "alias": "Analyst",
  "last_updated": "YYYY-MM-DD",
  "last_reviewed": "YYYY-MM-DD",
  "focus": "Obszar odpowiedzialnosci",
  "context": ["Najwazniejsze fakty do startu pracy"],
  "insights": ["Heurystyki pomagajace podejmowac decyzje"],
  "todos": ["Kolejny krok"],
  "risks": ["Co monitorowac"],
  "links": ["Przydatne sciezki"],
  "_template": { "...": "instrukcje utrzymania szablonu" }
}
```

## Hygiene
- Keep entries short; prefer bullet points to paragraphs.
- Aktualizuj `last_updated` po zmianach tresci. Gdy tylko potwierdzasz aktualnosc bez zmian, dopisz `last_reviewed` (data).
- Nyx coordinates snapshots before milestones and after major incidents.
- When workflow standards evolve, sync per-task logs/PDCA with `board.json` in the same session.
- Przed oznaczeniem zadania jako `in_progress` wypelnij PDCA w `backlog/topics/<TOPIC>/tasks/<TASK-ID>/pdca.md` (szczegoly: `docs/reference/session-timeline.md`).

## Sample Task Log Snippet
```
2025-10-26 Why: Potwierdzenie sanity SharedFlow po aktywacji formy. Next: Dodac Compose test i checkliste Kai.
```
