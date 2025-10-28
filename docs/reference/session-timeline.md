# Session Timeline Quick Reference (Lean v3)

## Spis treści
- Phases table
- PDCA Anchor
- Quick Reminders

> One glance guide for planning and running a collaboration session with the lean structure.

| Phase | Trigger | Primary owner(s) | Core actions | Artefacts & checkpoints |
|-------|---------|------------------|--------------|-------------------------|
| Prepare | `[SESSION::START]` or new topic | Orin, brief from Mira | `python scripts/validate.py`, read latest memories, confirm scope | `board.json`, PDCA snapshot in task `pdca.md` |
| Frame Vision | `[AGENT::MIRA] [TASK::ANALYZE]` | Mira, Orin | 4MAT conversation, capture open questions | `backlog/topics/<TOPIC>/BRIEF-*.json` |
| Plan | After brief confirmed | Orin with Echo & Vireal | Map tasks to agents in `board.json`, record Why/Next in task logs | `board.json`, `backlog/topics/<TOPIC>/tasks/...` |
| Build | Tasks `in_progress` | Lumen, Nodus, others | Execute steps defined in PDCA | Code/docs, checkpoints in task `log.md` |
| Review | Ready for verification | Kai (quality), Echo/Vireal (scope) | Validate acceptance, raise risks, capture decisions | Notes in task `log.md`, update PDCA Check |
| Log & Memory | After review | Scribe, Nyx, owners | Chronicle facts, update memories if future-impact | `reports/chronicle.md`, `agents/<name>/memory.json` |
| Cooldown | Wrap | Orin, Kai, Nyx, Scribe | Archive results, plan follow-ups | `board.json` set to `done`, snapshot noted |

## PDCA Anchor

Every task entering `in_progress` starts from `backlog/topics/<TOPIC>/tasks/<TASK-ID>/pdca.md`. Reference it from the task `log.md` so Orin and Kai can trace intent  action  outcome.

## Quick Reminders

- Keep `[AGENT::NAME]` tagging consistent in messages and artefacts so the coordinator can trace owners quickly.
- If humans push commits with `[USER]`, Echo and Orin acknowledge the change, update plan impacts, and loop in the relevant agent for follow-up.
- After each task or wrap, the owner updates `last_updated` when content changed or adds a `last_reviewed` stamp.
- Escalations use `[CONFLICT]`: state problem, options, preferred resolution; expect Orin response within 15 minutes.
