"""Simple board.json CLI: add/move/ls/find.

Usage:
  python scripts/board.py ls [--topic TOPIC] [--owner NAME] [--status STATUS]
  python scripts/board.py move TASK_ID STATUS
  python scripts/board.py add TASK_ID TITLE TOPIC OWNER
"""

import argparse
import json
from pathlib import Path
from typing import Any, Dict, List

ROOT = Path(__file__).resolve().parents[1]
BOARD = ROOT / "backlog" / "board.json"


def load_board() -> Dict[str, Any]:
    if not BOARD.exists():
        raise SystemExit(f"board.json not found at {BOARD}")
    return json.loads(BOARD.read_text(encoding="utf-8"))


def save_board(data: Dict[str, Any]) -> None:
    BOARD.write_text(json.dumps(data, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")


def cmd_ls(args: argparse.Namespace) -> None:
    data = load_board()
    tasks = data.get("tasks", [])
    def match(t: Dict[str, Any]) -> bool:
        if args.topic and t.get("topic") != args.topic:
            return False
        if args.owner and t.get("owner", "").lower() != args.owner.lower():
            return False
        if args.status and t.get("status") != args.status:
            return False
        return True
    for t in tasks:
        if match(t):
            print(f"{t.get('id'):>20}  {t.get('status', '-'):<12}  {t.get('owner','-'):<10}  {t.get('title','')}")


def cmd_move(args: argparse.Namespace) -> None:
    data = load_board()
    updated = False
    for t in data.get("tasks", []):
        if t.get("id") == args.task_id:
            t["status"] = args.status
            updated = True
            break
    if not updated:
        raise SystemExit(f"Task not found: {args.task_id}")
    save_board(data)
    print(f"Moved {args.task_id} -> {args.status}")


def cmd_add(args: argparse.Namespace) -> None:
    data = load_board()
    if any(t.get("id") == args.task_id for t in data.get("tasks", [])):
        raise SystemExit(f"Task already exists: {args.task_id}")
    data.setdefault("tasks", []).append({
        "id": args.task_id,
        "title": args.title,
        "topic": args.topic,
        "status": "pending",
        "owner": args.owner
    })
    save_board(data)
    print(f"Added {args.task_id} [{args.owner}] in topic {args.topic}")


def main() -> None:
    ap = argparse.ArgumentParser()
    sub = ap.add_subparsers(dest="cmd", required=True)

    ap_ls = sub.add_parser("ls")
    ap_ls.add_argument("--topic")
    ap_ls.add_argument("--owner")
    ap_ls.add_argument("--status")
    ap_ls.set_defaults(func=cmd_ls)

    ap_move = sub.add_parser("move")
    ap_move.add_argument("task_id")
    ap_move.add_argument("status", choices=["pending", "in_progress", "done"])
    ap_move.set_defaults(func=cmd_move)

    ap_add = sub.add_parser("add")
    ap_add.add_argument("task_id")
    ap_add.add_argument("title")
    ap_add.add_argument("topic")
    ap_add.add_argument("owner")
    ap_add.set_defaults(func=cmd_add)

    args = ap.parse_args()
    args.func(args)


if __name__ == "__main__":
    main()
