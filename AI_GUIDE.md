# AI_GUIDE.md - Working With ChatGPT (v2.0)

## Goal
Maintain a live context in VS Code so GPT-5 understands the collaboration framework and can act as the agents defined in `AGENTS.md`.

## Bootstrapping
1. Ask ChatGPT:
   ```
   Prosze przeczytac PROJECT_CONTEXT.md, AGENTS.md, WORKFLOW.md, MEMORY_SPEC.md, AI_GUIDE.md.
   Zostan w tym kontekscie dopoki nie powiem inaczej.
   ```
2. Trigger `[SESSION::START]` to begin the ritual.
3. Gdy potrzebujesz rozmowy o wizji, zapros `[AGENT::MIRA] [TASK::ANALYZE]` - Storywright zbierze narracje i przygotuje brief dla Orina.
4. Orin przygotowuje plik sesji, przydziela zadania i sygnalizuje agentom kolejne ruchy.

## Response Modes
- **PRO**: focused execution, bullet points, actionable next steps.
- **META**: reflection, relationship checks, broader questions.

## Collaboration Tips
- [SESSION::CONTINUE] - sprawdz postep agentow i wznow sesje (asystent raportuje stan, Orin potwierdza "kontynuuj" lub "zatwierdzam").
- Confirm the current task ID before modifying files.
- Wypelnij PDCA zanim oznaczysz zadanie jako `in_progress`, korzystajac z `docs/templates/pdca-template.md` i linkuj notatke w `log.md` (PDCA Anchor opisany w `docs/reference/session-timeline.md`).
- When scoping work, consult `docs/reference/` (architecture, UX, overlay, tooling) for constraints and decisions.
- When uncertain, propose options plus risks instead of pausing work.
- Use the templates in `docs/templates/` for structured outputs.
- Close every message with `Next step:` so Orin can plan the flow.
- Po `[SESSION::START]` asystent wysyla zlecenia agentom zgodnie z planem sesji; Orin reaguje tylko na wyniki, decyzje i eskalacje.

## Memory Hygiene
- Nyx ensures `memory.json` entries reflect the latest process decisions.
- After significant updates, mention which files changed so Orin can log them.
- Gdy tylko potwierdzasz, ze wiedza pozostaje aktualna, dopisz pole `last_reviewed` z data i identyfikatorem sesji zamiast sztucznie edytowac tresc.
- Use `scripts/validate-agent-sync.py` to detect stale metadata.
