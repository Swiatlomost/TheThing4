"""Append an entry to a task log.jsonl.

Usage:
  python scripts/log_add.py <TOPIC-ID> <TASK-ID> --who Lumen --why "..." --what "..." --next "..." [--tags tag1,tag2]
"""

from __future__ import annotations

import argparse
import json
from datetime import datetime
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


def resolve_task_dir(topic_id: str, task_id: str) -> Path:
    for p in ROOT.glob(f"backlog/*/topics/{topic_id}/tasks/{task_id}"):
        return p
    raise SystemExit(f"Task folder not found for {topic_id}/{task_id}")


def main() -> None:
    ap = argparse.ArgumentParser()
    ap.add_argument("topic_id")
    ap.add_argument("task_id")
    ap.add_argument("--who", required=True)
    ap.add_argument("--why", required=True)
    ap.add_argument("--what", required=True)
    ap.add_argument("--next", dest="next_", required=True)
    ap.add_argument("--tags", default="")
    args = ap.parse_args()

    task_dir = resolve_task_dir(args.topic_id, args.task_id)
    log_path = task_dir / "log.jsonl"
    entry = {
        "ts": datetime.now().strftime("%Y-%m-%dT%H:%M:%S"),
        "who": args.who,
        "why": args.why,
        "what": args.what,
        "next": args.next_,
        "tags": [t.strip() for t in args.tags.split(",") if t.strip()],
    }
    with log_path.open("a", encoding="utf-8") as f:
        f.write(json.dumps(entry, ensure_ascii=False) + "\n")
    print(f"Appended log entry to {log_path}")


if __name__ == "__main__":
    main()

