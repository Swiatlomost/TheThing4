# Agents Directory

Each agent keeps three core files:
- `task.json` - active and completed work, mirroring `agents/status.json`.
- `log.md` - running journal with decisions and timestamps.
- `memory.json` - evergreen heuristics updated at least once per day.

Optional artefacts (such as Scribe's `chronicle.md`) live alongside these files.

## Workflow
1. Orin registers new work in `agents/orin/task.json` and `agents/status.json`.
2. Linked agents copy the task entry into their `task.json` and start logging.
2a. Przed oznaczeniem zadania jako `in_progress` wypelnij PDCA w `log.md` (szablon: `docs/templates/pdca-template.md`).
3. When done, move entries to `completed_tasks`, run the cooldown checklist, and ask Nyx to update memories if context changed.

Follow the conflict resolution steps defined in `AGENTS.md` whenever roles disagree.
