#!/usr/bin/env python3
"""
Create a new backlog topic folder with minimal artefacts from templates.

Usage:
  python scripts/scaffold_topic.py --id TOPIC-YYYYMMDD_HHMMSS-XXX --title "Title" --area AREA [--owners Orin,Echo]
"""
import argparse
import json
from datetime import datetime
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
TPL = ROOT / "backlog" / "templates"
TOPICS = ROOT / "backlog" / "topics"


def write_json(path: Path, payload: dict) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(payload, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def load_tpl(name: str) -> dict:
    return json.loads((TPL / name).read_text(encoding="utf-8"))


def main() -> None:
    ap = argparse.ArgumentParser()
    ap.add_argument("--id", required=True)
    ap.add_argument("--title", required=True)
    ap.add_argument("--area", required=True)
    ap.add_argument("--owners", default="")
    args = ap.parse_args()

    topic_id = args.id
    title = args.title
    area = args.area
    owners = [x for x in args.owners.split(",") if x]

    base = TOPICS / topic_id
    if base.exists():
        raise SystemExit(f"Topic already exists: {base}")
    base.mkdir(parents=True)

    # topic.json
    tj = load_tpl("topic.json")
    tj.update({
        "id": topic_id,
        "title": title,
        "area": area,
        "owners": owners,
        "brief": f"backlog/topics/{topic_id}/brief.json",
        "pdca": f"backlog/topics/{topic_id}/PDCA.json",
    })
    write_json(base / "topic.json", tj)

    # brief.json (lightweight stub)
    brief = {
        "topic": topic_id,
        "title": title,
        "why": "",
        "what": "",
        "how": "",
        "what_if": "",
        "updated_at": datetime.now().strftime("%Y-%m-%d")
    }
    write_json(base / "brief.json", brief)

    # PDCA.json for the topic
    pdca = load_tpl("PDCA.json")
    pdca["meta"]["updated_at"] = datetime.now().strftime("%Y-%m-%d")
    write_json(base / "PDCA.json", pdca)

    # test-plan + event-checklist + links
    write_json(base / "test-plan.json", {"topic": topic_id, "owner": "Kai", "summary": "", "scenarios": []})
    write_json(base / "event-checklist.json", {"topic": topic_id, "checks": []})
    write_json(base / "links.json", json.loads((TPL / "links.json").read_text(encoding="utf-8")))

    # tasks dir
    (base / "tasks").mkdir(exist_ok=True)
    print(f"[OK] Scaffoled topic at {base}")


if __name__ == "__main__":
    main()

