"""Builds a lightweight LLM context bundle for a given task or topic.

Usage:
  python scripts/context.py task TASK_ID
  python scripts/context.py topic TOPIC_ID
"""

import argparse
import json
from pathlib import Path
from typing import Any, Dict, List

ROOT = Path(__file__).resolve().parents[1]
BOARD = ROOT / "backlog" / "board.json"


def load_board() -> Dict[str, Any]:
    return json.loads(BOARD.read_text(encoding="utf-8"))


def file_exists(p: Path) -> bool:
    try:
        return p.exists()
    except Exception:
        return False


def collect_task_context(task_id: str) -> Dict[str, Any]:
    data = load_board()
    task = next((t for t in data.get("tasks", []) if t.get("id") == task_id), None)
    if not task:
        raise SystemExit(f"Task not found: {task_id}")

    topic = next((x for x in data.get("topics", []) if x.get("id") == task.get("topic")), None)
    # Resolve topic base folder: prefer backlog/*/topics/<ID>, then topics/<ID>, then legacy with slug
    topic_id = task.get('topic')
    base = None
    for p in ROOT.glob(f"backlog/*/topics/{topic_id}"):
        base = p
        break
    if base is None:
        cand = ROOT / f"topics/{topic_id}"
        if cand.exists():
            base = cand
    if base is None and topic is not None:
        topic_slug = topic.get("slug")
        if topic_slug:
            legacy = ROOT / f"topics/{topic_id}-{topic_slug}"
            if legacy.exists():
                base = legacy
    if base is None:
        raise SystemExit(f"Topic folder not found for {topic_id}")

    links = task.get("links", {})
    files: List[str] = []

    # Always include core files if present
    # Collect brief (support BRIEF-*.json or brief.json), optional topic.json and task files
    # brief
    brief_json = None
    for candidate in ["brief.json"] + [str(p.name) for p in (base.glob("BRIEF-*.json"))]:
        p = base / candidate
        if file_exists(p):
            brief_json = p
            break
    if brief_json:
        files.append(str(brief_json.relative_to(ROOT)))
    # topic.json (optional)
    tjson = base / "topic.json"
    if file_exists(tjson):
        files.append(str(tjson.relative_to(ROOT)))
    # task PDCA/log (prefer JSON/JSONL)
    for rel in [
        f"tasks/{task_id}/PDCA.json",
        f"tasks/{task_id}/log.jsonl",
        f"tasks/{task_id}/pdca.md",
        f"tasks/{task_id}/log.md"
    ]:
        p = base / rel
        if file_exists(p):
            files.append(str(p.relative_to(ROOT)))

    # Include linked references
    for k, v in links.items():
        p = ROOT / v
        if file_exists(p):
            files.append(str(p.relative_to(ROOT)))

    # Agent memories for owner + contributors
    agent_files: List[str] = []
    shared_mem = ROOT / "agents/memory.json"
    if file_exists(shared_mem):
        agent_files.append(str(shared_mem.relative_to(ROOT)))
    else:
        owner = (task.get("owner") or "").lower()
        if owner:
            mem = ROOT / f"agents/{owner}/memory.json"
            if file_exists(mem):
                agent_files.append(str(mem.relative_to(ROOT)))
        for c in task.get("contributors", []) or []:
            mem = ROOT / f"agents/{str(c).lower()}/memory.json"
            if file_exists(mem):
                agent_files.append(str(mem.relative_to(ROOT)))

    return {
        "task": task,
        "files": files,
        "agent_memories": agent_files,
    }


def collect_topic_context(topic_id: str) -> Dict[str, Any]:
    data = load_board()
    topic = next((x for x in data.get("topics", []) if x.get("id") == topic_id), None)
    if not topic:
        raise SystemExit(f"Topic not found: {topic_id}")
    # Resolve topic base folder: prefer backlog/*/topics/<ID>, then topics/<ID>, then legacy with slug
    base = None
    for p in ROOT.glob(f"backlog/*/topics/{topic_id}"):
        base = p
        break
    if base is None:
        cand = ROOT / f"topics/{topic_id}"
        if cand.exists():
            base = cand
    if base is None:
        legacy = ROOT / f"topics/{topic_id}-{topic.get('slug')}"
        if legacy.exists():
            base = legacy
    if base is None:
        raise SystemExit(f"Topic folder not found for {topic_id}")
    files: List[str] = []
    # brief + topic.json + PDCA/test-plan/checklist (optional, prefer JSON)
    tjson = base / "topic.json"
    if file_exists(tjson):
        files.append(str(tjson.relative_to(ROOT)))
    for p in base.glob("BRIEF-*.json"):
        if file_exists(p):
            files.append(str(p.relative_to(ROOT)))
    for rel in ["PDCA.json", "test-plan.json", "event-checklist.json", "ADR.json", "UX.json", "UI.json"]:
        p = base / rel
        if file_exists(p):
            files.append(str(p.relative_to(ROOT)))
    tasks = [t for t in data.get("tasks", []) if t.get("topic") == topic_id]
    return {"topic": topic, "files": files, "tasks": tasks}


def main() -> None:
    ap = argparse.ArgumentParser()
    sub = ap.add_subparsers(dest="cmd", required=True)
    ap_task = sub.add_parser("task")
    ap_task.add_argument("task_id")
    ap_topic = sub.add_parser("topic")
    ap_topic.add_argument("topic_id")
    args = ap.parse_args()

    if args.cmd == "task":
        bundle = collect_task_context(args.task_id)
    else:
        bundle = collect_topic_context(args.topic_id)
    print(json.dumps(bundle, indent=2, ensure_ascii=False))


if __name__ == "__main__":
    main()
