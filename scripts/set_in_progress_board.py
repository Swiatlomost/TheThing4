"""Promote a task on the new board.json to in_progress.

Usage:
  python scripts/set_in_progress_board.py [TASK_ID]

If TASK_ID is omitted, the first pending task is promoted.
"""

import json
import sys
from pathlib import Path
from typing import Any, Dict

ROOT = Path(__file__).resolve().parents[1]
BOARD = ROOT / "backlog" / "board.json"


def load_board() -> Dict[str, Any]:
    return json.loads(BOARD.read_text(encoding="utf-8"))


def save_board(data: Dict[str, Any]) -> None:
    BOARD.write_text(json.dumps(data, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")


def main(argv):
    data = load_board()
    target = argv[1] if len(argv) > 1 else None
    tasks = data.get("tasks", [])
    if target:
        task = next((t for t in tasks if t.get("id") == target), None)
        if not task:
            print(f"Task not found: {target}")
            sys.exit(1)
    else:
        # Pick first pending task that is not explicitly deferred/backlog/someday
        def is_candidate(t):
            if t.get("status") != "pending":
                return False
            if t.get("deferred"):
                return False
            tags = {x.lower() for x in (t.get("tags") or [])}
            if any(tag in tags for tag in ("backlog", "someday", "later")):
                return False
            return True
        task = next((t for t in tasks if is_candidate(t)), None)
        if not task:
            print("No pending non-deferred tasks to promote.")
            sys.exit(0)
    if task.get("status") != "in_progress":
        task["status"] = "in_progress"
        save_board(data)
        print(f"Updated {task.get('id')} to in_progress.")
    else:
        print(f"No change: {task.get('id')} is already in_progress.")


if __name__ == "__main__":
    main(sys.argv)
