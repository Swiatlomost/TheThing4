"""Edit PDCA.json for a topic or task.

Usage:
  python scripts/pdca_edit.py topic <TOPIC-ID> --add plan "punkt"
  python scripts/pdca_edit.py task  <TOPIC-ID> <TASK-ID> --add check "warunek"
"""

from __future__ import annotations

import argparse
import json
from datetime import date
from pathlib import Path
from typing import Tuple

ROOT = Path(__file__).resolve().parents[1]


def resolve_pdca_path(kind: str, topic_id: str, task_id: str | None) -> Path:
    base = None
    for p in ROOT.glob(f"backlog/*/topics/{topic_id}"):
        base = p
        break
    if base is None:
        raise SystemExit(f"Topic not found: {topic_id}")
    if kind == "topic":
        return base / "PDCA.json"
    return base / f"tasks/{task_id}/PDCA.json"


def main() -> None:
    ap = argparse.ArgumentParser()
    ap.add_argument("kind", choices=["topic", "task"])
    ap.add_argument("topic_id")
    ap.add_argument("task_id", nargs="?")
    ap.add_argument("--add", nargs=2, metavar=("SECTION", "TEXT"))
    args = ap.parse_args()

    path = resolve_pdca_path(args.kind, args.topic_id, args.task_id)
    data = json.loads(path.read_text(encoding="utf-8")) if path.exists() else {"plan": [], "do": [], "check": [], "act": [], "meta": {}}
    if args.add:
        section, text = args.add
        if section not in ("plan", "do", "check", "act"):
            raise SystemExit("SECTION must be one of: plan, do, check, act")
        data.setdefault(section, []).append(text)
        data.setdefault("meta", {})["updated_at"] = date.today().isoformat()
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(json.dumps(data, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
        print(f"Updated {path} -> {section} +1")
    else:
        print(json.dumps(data, indent=2, ensure_ascii=False))


if __name__ == "__main__":
    main()

