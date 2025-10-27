# Scripts Overview

| Script | Purpose | Usage |
|--------|---------|-------|
| `board.py` | Manage `backlog/board.json` (ls/move/add). | `python scripts/board.py ls --topic TOPIC-20251025_120000-001` |
| `context.py` | Build context bundle for a task/topic. | `python scripts/context.py task MORPHO-007-undo-redo` |
| `validate.py` | Validate lean structure (`backlog/board.json` + topics). | `python scripts/validate.py` |
| `set_in_progress_board.py` | Promote first pending task (or given) to `in_progress`. | `python scripts/set_in_progress_board.py [TASK_ID]` |
| `update_backlog_index.py` | Generate `backlog/backlog.json` from `backlog/board.json` and filesystem. | `python scripts/update_backlog_index.py` |
| `archive_legacy.py` | Prepare safe cleanup (manifest + optional archive/remove). | `python scripts/archive_legacy.py [--archive] [--remove]` |
| `validate-agent-sync.py` | (Legacy) Old agent files validator. | `python scripts/validate-agent-sync.py` |
| `validate-agent-sync.ps1` | (Legacy) PowerShell wrapper for legacy validator. | `pwsh scripts/validate-agent-sync.ps1 [-StopOnWarning]` |
| `set_in_progress.py` | (Legacy) Orin/task.json helper. | `python scripts/set_in_progress.py agents/orin/task.json ORIN-YYYYMMDD-XXX` |
| `session_start_hook.ps1` | Promotes first pending task using board.json after `[SESSION::START]` (falls back to legacy if missing). | `pwsh scripts/session_start_hook.ps1` |

Keep scripts idempotent and cross-platform. When adding new tooling, update this table and provide short inline help inside the script.
