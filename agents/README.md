# Agents Directory

Each agent keeps three core files:
- `task.json` - active and completed work, mirroring `agents/status.json`.
- `log.md` - running journal with decisions and timestamps.
- `memory.json` - evergreen heuristics updated at least once per day.

Optional artefacts (such as Scribe's `chronicle.md`) live alongside these files.

## Workflow
1. Orin rejestruje nowe zadanie w `agents/orin/task.json` oraz `agents/status.json`.
2. Powiazani agenci kopiują wpis do swojego `task.json` i rozpoczynają logowanie.
3. Przed ustawieniem statusu `in_progress` wypelnij PDCA w `log.md` (szablon: `docs/templates/pdca-template.md`).
4. Po zakonczeniu przenies wpisy do `completed_tasks`, wykonaj cooldown checklist i poproś Nyx o aktualizacje pamieci gdy kontekst sie zmienil.

Stosuj procedury rozwiazywania konfliktow z `AGENTS.md` gdy role maja rozbiezne oczekiwania.
