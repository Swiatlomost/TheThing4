Reusable templates for backlog artefacts.

- topic.json
- task.json
- PDCA.json
- test-plan.json
- event-checklist.json
- links.json
- log.jsonl.example

Use scripts:
- python scripts/scaffold_topic.py --id TOPIC-YYYYMMDD_HHMMSS-XXX --title "..." --area AREA --owners Orin,Echo
- python scripts/scaffold_task.py TASK-ID --topic TOPIC-... --title "..." --owner Lumen [--status pending]

