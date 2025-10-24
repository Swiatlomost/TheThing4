# WORKFLOW.md - Process (v2.0)

## Session Ritual
1. `[SESSION::START]` - run pre-task validation (status, logs, memories).
2. Mira (Storywright) destyluje rozmowe z wlascicielem wizji i przygotowuje brief dla Orina.
3. Orin loads memories, drafts the session file, and assigns agent tasks.
4. Echo & Vireal analyse and propose options.
5. Lumen & Nodus implement and integrate.
6. Kai reviews quality gates.
7. Scribe records facts and narrative.
8. Nyx updates memories and snapshots.

Before switching any task to `in_progress`, the owning agent fills a PDCA card (`docs/templates/pdca-template.md`) and records it in `log.md`.

## Pre-task Validation Checklist
- [ ] `agents/status.json` matches every `task.json`.
- [ ] No `done` items left inside `active_tasks`.
- [ ] Latest logs present for each agent (within 24h).
- [ ] Memories have `last_updated` within 24h or confirmation note.
- [ ] Automation scripts run without error.

If the checklist fails: **pause work**, fix discrepancies, log the incident, rerun validation.

## Communication Rules
- Finish replies with a **Next step** line.
- Decisions follow the pattern `Why -> What -> Next`.
- Provide evidence (`file:line`) when raising risks or blockers.
- Keep prompts short using tags `[TASK::...]`, `[AGENT::...]`, `[MODE::...]` when possible.

## Definition of Done
- Artefact produced and reviewed (Kai).
- Log entry recorded (Scribe).
- `task.json` and `agents/status.json` moved to `done`.
- Memory updated if the change affects future decisions.

## Cooldown Checklist
1. Delivery agent marks task `done` in `task.json` with summary.
2. Orin moves completed items in `agents/status.json` and archives in the session file.
3. Scribe writes cooldown note in log + chronicle.
4. Kai attaches final test results.
5. Nyx snapshots memory changes if any.

## Symptoms of Drift
- `agents/status.json` shows tasks missing from agent files.
- Logs lack timestamps around major changes.
- Memories reference outdated workflows.

When drift occurs, Orin leads a correction pass and documents the fix in `agents/orin/log.md`.
