# Scripts Overview

| Script | Purpose | Usage |
|--------|---------|-------|
| `validate-agent-sync.py` | Ensures task.json, status.json, logs, and memories stay aligned. | `python scripts/validate-agent-sync.py` |
| `validate-agent-sync.ps1` | PowerShell wrapper with optional stop-on-warning flag. | `pwsh scripts/validate-agent-sync.ps1 [-StopOnWarning]` |
| `set_in_progress.py` | Marks an Orin task (and linked agent tasks) as `in_progress`. | `python scripts/set_in_progress.py agents/orin/task.json ORIN-YYYYMMDD-XXX` |
| `session_start_hook.ps1` | Promotes the first pending Orin task to `in_progress` after `[SESSION::START]`. | `pwsh scripts/session_start_hook.ps1` |

Keep scripts idempotent and cross-platform. When adding new tooling, update this table and provide short inline help inside the script.
