# MEMORY_SPEC.md - Memory & Journals (v2.0)

## Files per Agent
Every agent keeps three mandatory files inside `agents/<name>/`:

| File | Purpose |
|------|---------|
| `log.md` | Running notes (timestamped facts, decisions, next steps).
| `task.json` | Tasks owned by the agent with status `pending / in_progress / done` and parent Orin task.
| `memory.json` | Semi-stable heuristics, agreements, risks, and helpful links.

`agents/status.json` + `agents/status.md` provide the shared board for Orin.

## Memory JSON Skeleton
```
{
  "agent": "Echo",
  "alias": "Analyst",
  "last_updated": "YYYY-MM-DD",
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
- Update `last_updated` whenever content changes or you confirm it stays valid.
- Nyx coordinates snapshots before milestones and after major incidents.
- When workflow standards evolve, sync log entries, task files, and `agents/status.json` in the same session.
- Przed oznaczeniem zadania jako `in_progress` wypelnij PDCA w `log.md` (szablon: `docs/templates/pdca-template.md`).

## Sample Log Snippet (Scribe)
```
## 2025-10-24 - Fresh Start
Decyzje: Repo wyczyszczone z kodu aplikacji; utrzymano tylko proces agentowy.
TODO: [ ] ORIN-XXXX - przygotowac plan pierwszego zadania.
Next: Orin wyznacza cel i tworzy plik sessions/.
```
