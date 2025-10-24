# Ops Checklist

> Maintained by Nodus. Adjust items per environment.

## Environment
- [ ] Python 3.10+ available (`python --version`)
- [ ] Git configured with `user.name` and `user.email`
- [ ] Required secrets stored outside the repo

## Automation Scripts
- [ ] `scripts/validate-agent-sync.py` returns SUCCESS
- [ ] `scripts/set_in_progress.py` updates sample data without errors
- [ ] PowerShell modules required for hooks installed

## Tooling
- [ ] VS Code extensions for Git and Python
- [ ] ChatGPT VS Code extension authenticated
- [ ] Local tasks in `.vscode/tasks.json` aligned with current workflow

## Incident Response
- [ ] Document escalation channels in `WORKFLOW.md`
- [ ] Keep rollback notes for each external integration
