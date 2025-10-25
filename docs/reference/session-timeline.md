# Session Timeline Quick Reference (v1.0)

> One glance guide for planning and running a collaboration session. Each phase links the ritual, responsible agents, and required artefacts.

| Phase | Trigger | Primary owner(s) | Core actions | Artefacts & checkpoints |
|-------|---------|------------------|--------------|-------------------------|
| Prepare | `[SESSION::START]` requested or new topic emerges | Orin, echoing brief from Mira | Run pre-task validation, read latest memories, confirm scope | `scripts/validate-agent-sync.py`, PDCA snapshot drafted in `log.md`, session notes folder ready |
| Frame Vision | `[AGENT::MIRA] [TASK::ANALYZE]` (if vision update needed) | Mira, Orin | Conduct 4MAT conversation, summarise intent, capture open questions | `agents/storywright/briefs/SESSION-ID.md`, updated Orin task skeleton |
| Plan | Immediately after brief confirmed | Orin with Echo & Vireal support | Draft session plan, map tasks to agents, record Why/Next for each decision | `sessions/SESSION-ID.md`, `agents/status.json`, linked `task.json` entries |
| Build | Tasks marked `in_progress` | Lumen, Nodus, contributing agents | Execute implementation or research steps defined in PDCA | Working branches, code or doc edits, checkpoints logged in `log.md` |
| Review | Work ready for verification | Kai (quality), Echo/Vireal (scope), Orin (business) | Validate acceptance criteria, raise risks, capture decisions | Review notes in relevant `log.md`, comment threads, update PDCA “Check” outcomes |
| Log & Memory | After review decision | Scribe, Nyx, owning agents | Chronicle facts, sync task boards, update memories if work changes future decisions | `agents/scribe/log.md`, `agents/status.json`, affected `memory.json` entries with `last_updated` or `last_reviewed` |
| Cooldown | Session wraps or major deliverable shipped | Orin, Kai, Nyx, Scribe | Run cooldown checklist, archive results, plan follow-ups | `sessions/SESSION-ID.md` finalised, Kai test report attached, Nyx snapshot noted |

## PDCA Anchor

Every agent entering `in_progress` status starts from `docs/templates/pdca-template.md`. Link the PDCA note directly in your `log.md` entry so Orin and Kai can trace intent → action → outcome. References elsewhere in the repository now point back to this template to keep guidance single-sourced.

## Quick Reminders

- Keep `[AGENT::NAME]` tagging consistent in messages and artefacts so the coordinator can trace owners quickly.
- If humans push commits with `[USER]`, Echo and Orin acknowledge the change, update plan impacts, and loop in the relevant agent for follow-up.
- After each task or session wrap, the owning agent updates `last_updated` when content changed or adds a `last_reviewed` stamp (np. `2025-01-18 ORIN-20250118-001`).
- Escalations use `[CONFLICT]`: state problem, options, preferred resolution; expect Orin response within 15 minutes.
