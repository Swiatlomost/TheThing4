"""Validate new lean structure: board.json, topics, task folders, references.

Usage:
  python scripts/validate.py [--scaffold]

With --scaffold, missing PDCA/log files for tasks and missing topic
test stubs (test-plan.json, event-checklist.json) are created.
"""

import json
from pathlib import Path
import argparse
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
        # resolve topic folder (backlog/topics/<id> preferred, then legacy with slug)
        base = None
        cand = ROOT / f"backlog/topics/{tid}"
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
            cand = ROOT / f"backlog/topics/{topic_id}"
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


def scaffold_missing(data: Dict[str, Any]) -> None:
    # Topics: ensure test-plan.json and event-checklist.json exist
    for t in data.get("topics", []):
        tid = t.get("id")
        if not tid:
            continue
        base = ROOT / f"backlog/topics/{tid}"
        if not base.exists():
            continue
        for fname in ("test-plan.json", "event-checklist.json"):
            f = base / fname
            if not f.exists():
                if fname == "test-plan.json":
                    payload = {
                        "topic": tid,
                        "owner": "Kai",
                        "summary": "Scaffolded by validate --scaffold.",
                        "scenarios": [],
                    }
                else:
                    payload = {"topic": tid, "checks": []}
                f.write_text(__import__("json").dumps(payload, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
                ok(f"Scaffolded {f}")

    # Tasks: ensure PDCA.json and log.jsonl exist
    for task in data.get("tasks", []):
        tid = task.get("id")
        topic = task.get("topic")
        if not tid or not topic:
            continue
        base = ROOT / f"backlog/topics/{topic}/tasks/{tid}"
        try:
            base.mkdir(parents=True, exist_ok=True)
        except Exception:
            continue
        pdca = base / "PDCA.json"
        if not pdca.exists():
            payload = {
                "plan": [],
                "do": [],
                "check": [],
                "act": [],
                "links": {},
                "meta": {"updated_at": __import__("datetime").date.today().isoformat()},
            }
            pdca.write_text(__import__("json").dumps(payload, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
            ok(f"Scaffolded {pdca}")
        log = base / "log.jsonl"
        if not log.exists():
            entry = {
                "ts": __import__("datetime").datetime.now().isoformat(timespec="seconds"),
                "who": "Orin",
                "why": "Scaffolded by validate --scaffold",
                "what": "Create PDCA and log.jsonl",
                "next": "Fill PDCA before in_progress",
                "tags": ["migration", "pdca"],
            }
            log.write_text(__import__("json").dumps(entry, ensure_ascii=False) + "\n", encoding="utf-8")
            ok(f"Scaffolded {log}")


def main() -> None:
    ap = argparse.ArgumentParser()
    ap.add_argument("--scaffold", action="store_true", help="Create missing PDCA/log and topic test stubs")
    args = ap.parse_args()

    print("# Lean Structure Validation\n")
    data = load_board()
    if args.scaffold:
        scaffold_missing(data)
    ok_topics = validate_topics(data)
    ok_tasks = validate_tasks(data)
    print("\n=== SUMMARY ===")
    if ok_topics and ok_tasks:
        print("[SUCCESS] Structure valid.")
    else:
        print("[ACTION] Resolve warnings/errors above.")


if __name__ == "__main__":
    main()
