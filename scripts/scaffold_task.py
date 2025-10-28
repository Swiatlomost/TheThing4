#!/usr/bin/env python3
"""
Append a task to board.json and scaffold PDCA/log under the topic folder.

Usage:
  python scripts/scaffold_task.py TASK-ID --topic TOPIC-... --title "..." --owner Lumen [--status pending]
        [--contributors Echo,Kai] [--tags ui,glow] [--deferred]
"""
import argparse
import json
from datetime import datetime
from pathlib import Path
from typing import Any, Dict

ROOT = Path(__file__).resolve().parents[1]
BOARD = ROOT / "backlog" / "board.json"
TOPICS = ROOT / "backlog" / "topics"


def load_board() -> Dict[str, Any]:
    return json.loads(BOARD.read_text(encoding="utf-8"))


def save_board(obj: Dict[str, Any]) -> None:
    BOARD.write_text(json.dumps(obj, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def write_json(path: Path, payload: dict) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(payload, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def main() -> None:
    ap = argparse.ArgumentParser()
    ap.add_argument("task_id")
    ap.add_argument("--topic", required=True)
    ap.add_argument("--title", required=True)
    ap.add_argument("--owner", required=True)
    ap.add_argument("--status", default="pending", choices=["pending", "in_progress", "done"])
    ap.add_argument("--contributors", default="")
    ap.add_argument("--tags", default="")
    ap.add_argument("--deferred", action="store_true")
    args = ap.parse_args()

    task_id = args.task_id
    topic_id = args.topic
    board = load_board()

    # Append to board if not present
    existing = next((t for t in board.get("tasks", []) if t.get("id") == task_id), None)
    if existing:
        raise SystemExit(f"Task already exists in board.json: {task_id}")
    task_entry = {
        "id": task_id,
        "title": args.title,
        "topic": topic_id,
        "status": args.status,
        "owner": args.owner,
    }
    if args.contributors:
        task_entry["contributors"] = [x for x in args.contributors.split(",") if x]
    if args.tags:
        task_entry["tags"] = [x for x in args.tags.split(",") if x]
    if args.deferred:
        task_entry["deferred"] = True
    board.setdefault("tasks", []).append(task_entry)
    save_board(board)

    # Scaffold PDCA/log
    base = TOPICS / topic_id / "tasks" / task_id
    base.mkdir(parents=True, exist_ok=True)
    today = datetime.now().strftime("%Y-%m-%d")
    now = datetime.now().isoformat(timespec="seconds")
    pdca = {
        "plan": [],
        "do": [],
        "check": [],
        "act": [],
        "links": {},
        "meta": {"updated_at": today},
    }
    write_json(base / "PDCA.json", pdca)
    entry = {
        "ts": now,
        "who": args.owner,
        "why": "Scaffold task",
        "what": args.title,
        "next": "Fill PDCA and start work",
        "tags": ["scaffold"]
    }
    (base / "log.jsonl").write_text(json.dumps(entry, ensure_ascii=False) + "\n", encoding="utf-8")
    print(f"[OK] Added task {task_id} to board and scaffolded at {base}")


if __name__ == "__main__":
    main()

