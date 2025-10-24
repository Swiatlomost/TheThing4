# Daily Ritual

> Use this checklist at the start of every working block.

## Opening
0. If a new vision is brewing, talk to `[AGENT::MIRA]` so she can draft a brief for Orin.
1. Run `python scripts/validate-agent-sync.py`.
2. Review `agents/status.json` for outdated entries.
3. Skim `agents/*/memory.json` for stale dates.
4. Confirm that `sessions/` contains the planned work item.

## During The Day
- Keep `log.md` entries short, date-stamped, and action oriented.
- Capture PDCA for every task before you start work (see `docs/templates/pdca-template.md`).
- Update `task.json` immediately after changing status.
- Ping Orin when a blocker lasts longer than 30 minutes.
- Kai reminds the team about missing tests or evidence.

## Closing
1. Move finished work to `completed_tasks` and set status `done`.
2. Ask Scribe to capture the cooldown narrative.
3. Nyx snapshots any major memory changes.
4. Orin syncs `agents/status.json` and writes Why/Next.
