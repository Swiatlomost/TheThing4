"""Memory sync utility for agents.

Consolidates agent memories into a single `agents/memory.json` and can export/import
per‑agent files for compatibility with external tools.

Usage:
  python scripts/memory_sync.py status
  python scripts/memory_sync.py export   # agents/memory.json -> agents/<name>/memory.json
  python scripts/memory_sync.py import   # agents/<name>/memory.json -> agents/memory.json
"""

from __future__ import annotations

import argparse
import json
from datetime import datetime
from pathlib import Path
from typing import Dict, Any

ROOT = Path(__file__).resolve().parents[1]
SHARED = ROOT / "agents" / "memory.json"


def load_shared() -> Dict[str, Any]:
    if not SHARED.exists():
        return {"version": "1.0", "last_updated": datetime.now().date().isoformat(), "agents": {}}
    return json.loads(SHARED.read_text(encoding="utf-8"))


def save_shared(data: Dict[str, Any]) -> None:
    data["last_updated"] = datetime.now().date().isoformat()
    SHARED.write_text(json.dumps(data, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")


def export_per_agent() -> None:
    data = load_shared()
    agents = data.get("agents", {}) or {}
    count = 0
    for name, payload in agents.items():
        dirname = ROOT / "agents" / name.lower()
        dirname.mkdir(parents=True, exist_ok=True)
        out = dirname / "memory.json"
        out.write_text(json.dumps(payload, indent=4, ensure_ascii=False) + "\n", encoding="utf-8")
        count += 1
    print(f"Exported {count} agent memories -> agents/<name>/memory.json")


def import_per_agent() -> None:
    data = load_shared()
    store = data.setdefault("agents", {})
    imported = 0
    for path in (ROOT / "agents").glob("*/memory.json"):
        try:
            payload = json.loads(path.read_text(encoding="utf-8"))
        except Exception as exc:
            print(f"[WARN] Skip {path}: {exc}")
            continue
        # Determine agent name
        agent_name = payload.get("agent") or path.parent.name.title()
        store[agent_name] = payload
        imported += 1
    save_shared(data)
    print(f"Imported {imported} agent files into {SHARED}")


def status() -> None:
    data = load_shared()
    agents = data.get("agents", {}) or {}
    print(f"Shared: {SHARED} -> {len(agents)} entries")
    missing = []
    present = 0
    for name in sorted(agents.keys()):
        p = ROOT / "agents" / name.lower() / "memory.json"
        if p.exists():
            present += 1
        else:
            missing.append(name)
    print(f"Per-agent files present: {present}, missing: {len(missing)}")
    if missing:
        print("Missing for:", ", ".join(missing))


def main() -> None:
    ap = argparse.ArgumentParser()
    ap.add_argument("cmd", choices=["status", "export", "import"])
    args = ap.parse_args()
    if args.cmd == "status":
        status()
    elif args.cmd == "export":
        export_per_agent()
    else:
        import_per_agent()


if __name__ == "__main__":
    main()

