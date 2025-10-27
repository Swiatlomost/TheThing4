"""Generate backlog/backlog.json from board.json and filesystem.

Groups topics by functional area (backlog/<area>/topics/<TOPIC-ID>) and lists tasks.
"""

import json
from datetime import datetime
import subprocess
from pathlib import Path
from typing import Any, Dict, List

ROOT = Path(__file__).resolve().parents[1]
BOARD = ROOT / "backlog" / "board.json"
BACKLOG = ROOT / "backlog"
OUT = BACKLOG / "backlog.json"


def load_board() -> Dict[str, Any]:
    return json.loads(BOARD.read_text(encoding="utf-8"))


def discover_topics() -> Dict[str, Dict[str, Any]]:
    topics: Dict[str, Dict[str, Any]] = {}
    for area_dir in BACKLOG.iterdir():
        if not area_dir.is_dir():
            continue
        topics_dir = area_dir / "topics"
        if not topics_dir.exists():
            continue
        for tdir in topics_dir.iterdir():
            if not tdir.is_dir():
                continue
            tid = tdir.name
            brief = next(tdir.glob("BRIEF-*.json"), None)
            pdca = next(tdir.glob("PDCA-*.md"), None)
            topics[tid] = {
                "area": area_dir.name,
                "brief": str(brief.relative_to(ROOT)) if brief else None,
                "pdca": str(pdca.relative_to(ROOT)) if pdca else None,
            }
    return topics


def compute_topic_status(tasks: List[Dict[str, Any]]) -> str:
    if any(t.get("status") == "in_progress" for t in tasks):
        return "active"
    if any(t.get("status") == "pending" and not t.get("deferred") for t in tasks):
        return "ready"
    return "backlog"


def main() -> None:
    board = load_board()
    topics_meta = discover_topics()

    areas: Dict[str, Dict[str, Any]] = {}

    # build topic -> tasks
    tasks_by_topic: Dict[str, List[Dict[str, Any]]] = {}
    for t in board.get("tasks", []):
        topic_id = t.get("topic")
        if not topic_id:
            continue
        tasks_by_topic.setdefault(topic_id, []).append(t)

    # materialize
    for topic in board.get("topics", []):
        tid = topic.get("id")
        title = topic.get("title")
        meta = topics_meta.get(tid, {})
        area = meta.get("area", "unknown")
        areas.setdefault(area, {"area": area, "topics": []})
        ttasks = tasks_by_topic.get(tid, [])
        # If no tasks in board for this topic, attempt to infer from git history (best effort)
        if not ttasks:
            inferred = []
            try:
                out = subprocess.run(
                    [
                        "git",
                        "log",
                        "--pretty=format:%h|%ad|%s",
                        "--date=format:%Y-%m-%d_%H%M%S",
                        "-n",
                        "200",
                    ],
                    cwd=ROOT,
                    capture_output=True,
                    text=True,
                    check=True,
                ).stdout.splitlines()
                for line in out:
                    parts = line.split("|", 2)
                    if len(parts) != 3:
                        continue
                    sha, when, subj = parts
                    low = subj.lower()
                    owner = None
                    if "overlay" in low:
                        target = "TOPIC-20251025_000554-001"
                        if tid != target:
                            continue
                        if "test" in low or "instrumentation" in low:
                            owner = "Kai"
                        elif "fix" in low or "feat" in low or "add" in low:
                            owner = "Lumen"
                        else:
                            owner = "Lumen"
                    elif ("cos v0.1" in low) or ("lifecycle" in low):
                        target = "TOPIC-20251024_195056-001"
                        if tid != target:
                            continue
                        if "adr" in low or "architecture" in low:
                            owner = "Vireal"
                        elif "test" in low or "plan" in low:
                            owner = "Kai"
                        elif "scaffold" in low or "feat" in low or "fix" in low:
                            owner = "Lumen"
                        else:
                            owner = "Lumen"
                    elif "morfogen" in low or "morpho" in low:
                        target = "TOPIC-20251025_120000-001"
                        if tid != target:
                            continue
                        if "adr" in low:
                            owner = "Vireal"
                        elif "test" in low or "plan" in low or "checklist" in low:
                            owner = "Kai"
                        else:
                            owner = "Lumen"
                    else:
                        continue
                    inferred.append({
                        "id": f"GIT-{sha}",
                        "title": subj,
                        "status": "done",
                        "owner": owner,
                    })
            except Exception:
                inferred = []
            # keep a few top inferred entries
            ttasks = inferred[:8]
        status = compute_topic_status(ttasks)
        areas[area]["topics"].append({
            "id": tid,
            "title": title,
            "status": status,
            "brief": meta.get("brief"),
            "pdca": meta.get("pdca"),
            "tasks": [
                {
                    "id": x.get("id"),
                    "title": x.get("title"),
                    "status": x.get("status"),
                    "owner": x.get("owner"),
                    "deferred": x.get("deferred"),
                    "tags": x.get("tags"),
                }
                for x in ttasks
            ],
        })

    out = {
        "generated_at": datetime.now().strftime("%Y-%m-%dT%H:%M:%S"),
        "areas": list(areas.values()),
    }
    OUT.write_text(json.dumps(out, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
    print(f"Updated {OUT}")


if __name__ == "__main__":
    main()
