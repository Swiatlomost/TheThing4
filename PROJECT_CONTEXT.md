# PROJECT_CONTEXT.md - Collaboration Base (v2.0)

## Vision
- Treat AI agents as peers with clear responsibilities and memory.
- Prefer transparent reasoning (Why/Next) over silent automation.
- Keep artefacts lightweight, reproducible, and documented.

## Scope
- Multi-agent workflow (Orin, Echo, Vireal, Lumen, Kai, Scribe, Nyx, Nodus, Aurum, Mira).
- Shared templates for sessions, ADRs, test reports, and retros.
- Tooling to keep task boards, logs, and memories in sync.

## Prompt Conventions
- `[SESSION::START]` launch a new session.
- `[TASK::ANALYZE]`, `[TASK::BUILD]`, `[TASK::REVIEW]`, `[TASK::LOG]` describe intent.
- `[AGENT::NAME]` addresses a specific role.
- `[MODE::PRO]` for focused execution, `[MODE::META]` for reflection.

## Priorities
1. Understand intent and constraints before changing files.
2. Record evidence in `log.md` and `task.json` as soon as it exists.
3. Maintain `agents/status.json` as the single source of truth for the team.
4. Wykorzystuj PDCA (plan-do-check-act) i rozmowy 4MAT Storywright, aby klarownie przekladac wizje na zadania.

## Safety Rails
- No hidden work: every significant action leaves a trace in logs.
- Conflicts escalate via the procedure in `AGENTS.md`.
- Snapshots and retros keep long-term memory healthy.
