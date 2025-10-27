"""Validate new lean structure: board.json, topics, task folders, references."""

import json
from pathlib import Path
from typing import Any, Dict, List

ROOT = Path(__file__).resolve().parents[1]
BOARD = ROOT / "backlog" / "board.json"


def ok(msg: str) -> None:
    print(f"[OK] {msg}")


def warn(msg: str) -> None:
    print(f"[WARN] {msg}")


def err(msg: str) -> None:
    print(f"[ERR] {msg}")


def load_board() -> Dict[str, Any]:
    try:
        return json.loads(BOARD.read_text(encoding="utf-8"))
    except Exception as exc:
        err(f"Cannot read board.json: {exc}")
        raise SystemExit(1)


def validate_topics(data: Dict[str, Any]) -> bool:
    ok_count = True
    topic_ids = set()
    for t in data.get("topics", []):
        tid = t.get("id")
        if not tid:
            err("Topic without id")
            ok_count = False
            continue
        if tid in topic_ids:
            err(f"Duplicate topic id: {tid}")
            ok_count = False
        topic_ids.add(tid)
        # resolve topic folder (backlog/*/topics/<id> preferred, then topics/<id>, then legacy with slug)
        base = None
        for p in ROOT.glob(f"backlog/*/topics/{tid}"):
            base = p
            break
        if base is None:
            cand = ROOT / f"topics/{tid}"
            if cand.exists():
                base = cand
        if base is None:
            slug = t.get("slug")
            if slug:
                cand = ROOT / f"topics/{tid}-{slug}"
                if cand.exists():
                    base = cand
        if base is None:
            warn(f"Topic folder missing for id: {tid}")
            continue
        # topic.json optional; brief required (BRIEF-*.json or brief.json)
        if not (base / "topic.json").exists():
            warn(f"Missing topic.json for topic {tid} (optional but recommended)")
        has_brief = any(base.glob("BRIEF-*.json")) or (base / "brief.json").exists()
        if not has_brief:
            warn(f"Missing brief file (BRIEF-*.json) for topic {tid}")
        # prefer JSON PDCA/test files
        if not (base / "PDCA.json").exists():
            warn(f"Topic {tid}: PDCA.json missing (md fallback allowed)")
        for req in ["test-plan.json", "event-checklist.json"]:
            if not (base / req).exists():
                warn(f"Topic {tid}: {req} missing (optional)")
    if ok_count:
        ok("Topics look sane")
    return ok_count


def validate_tasks(data: Dict[str, Any]) -> bool:
    ok_count = True
    seen = set()
    for t in data.get("tasks", []):
        tid = t.get("id")
        if not tid:
            err("Task without id")
            ok_count = False
            continue
        if tid in seen:
            err(f"Duplicate task id: {tid}")
            ok_count = False
        seen.add(tid)
        status = t.get("status")
        if status not in {"pending", "in_progress", "done"}:
            warn(f"Task {tid} has unexpected status: {status}")
        topic = t.get("topic")
        if topic and not any(x.get("id") == topic for x in data.get("topics", [])):
            warn(f"Task {tid} refers to unknown topic {topic}")
        # linked files
        links = t.get("links", {})
        for name, rel in links.items():
            p = ROOT / rel
            if not p.exists():
                warn(f"Task {tid} link missing: {name} -> {rel}")
        # prefer JSON PDCA/log for tasks
        topic_id = t.get("topic")
        if topic_id:
            # find base as in topics
            base = None
            for p in ROOT.glob(f"backlog/*/topics/{topic_id}"):
                base = p
                break
            if base is None:
                cand = ROOT / f"topics/{topic_id}"
                if cand.exists():
                    base = cand
            if base:
                if not (base / f"tasks/{tid}/PDCA.json").exists():
                    warn(f"Task {tid}: PDCA.json missing (md fallback allowed)")
                if not (base / f"tasks/{tid}/log.jsonl").exists():
                    warn(f"Task {tid}: log.jsonl missing (md fallback allowed)")
    if ok_count:
        ok("Tasks look sane")
    return ok_count


def main() -> None:
    print("# Lean Structure Validation\n")
    data = load_board()
    ok_topics = validate_topics(data)
    ok_tasks = validate_tasks(data)
    print("\n=== SUMMARY ===")
    if ok_topics and ok_tasks:
        print("[SUCCESS] Structure valid.")
    else:
        print("[ACTION] Resolve warnings/errors above.")


if __name__ == "__main__":
    main()
