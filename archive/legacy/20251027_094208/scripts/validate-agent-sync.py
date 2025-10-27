"""Agent workspace validation.
Checks consistency between task files, status board, logs, and memories.
"""

import json
import subprocess
from datetime import datetime
from pathlib import Path
from typing import Dict, Optional

AGENTS = ["orin", "echo", "vireal", "lumen", "kai", "scribe", "nyx", "nodus", "aurum", "storywright"]
REQUIRED_FILES = ["task.json", "log.md", "memory.json"]
DATE_FMT = "%Y-%m-%d"
MAX_MEMORY_AGE_DAYS = 3


def find_project_root() -> Path:
    root = Path(__file__).resolve().parent
    for parent in [root] + list(root.parents):
        if (parent / "agents").is_dir() and (parent / "docs" / "templates").is_dir():
            return parent
    return root


ROOT = find_project_root()
TODAY = datetime.now().date()


def print_header(title: str) -> None:
    print(f"\n=== {title} ===")


def load_json(path: Path) -> Optional[Dict]:
    try:
        with path.open(encoding="utf-8") as handle:
            return json.load(handle)
    except Exception as exc:  # pragma: no cover
        print(f"[ERR] Could not read {path}: {exc}")
        return None


def check_agent_files() -> bool:
    print_header("Agent file structure")
    ok = True
    for agent in AGENTS:
        agent_dir = ROOT / "agents" / agent
        for filename in REQUIRED_FILES:
            if not (agent_dir / filename).exists():
                print(f"[WARN] Missing {agent}/{filename}")
                ok = False
    if ok:
        print("[OK] All agent files present.")
    return ok


def check_status_alignment() -> bool:
    print_header("Task and status alignment")
    status_path = ROOT / "agents" / "status.json"
    status = load_json(status_path)
    if not status:
        print("[ERR] agents/status.json is missing or invalid.")
        return False

    active_ids = {item.get("task_id") for item in status.get("active_tasks", [])}
    completed_ids = {item.get("task_id") for item in status.get("completed_tasks", [])}

    all_ok = True

    for agent in AGENTS:
        data = load_json(ROOT / "agents" / agent / "task.json")
        if not data:
            all_ok = False
            continue
        for task in data.get("current_tasks", []):
            tid = task.get("task_id")
            if not tid:
                print(f"[WARN] {agent}: current_tasks entry without task_id")
                all_ok = False
                continue
            if task.get("status") == "done":
                print(f"[ERR] {agent}: task {tid} marked done but still in current_tasks")
                all_ok = False
            if tid not in active_ids:
                print(f"[WARN] {agent}: task {tid} missing from agents/status.json active_tasks")
                all_ok = False
        for task in data.get("completed_tasks", []):
            tid = task.get("task_id")
            if not tid:
                print(f"[WARN] {agent}: completed_tasks entry without task_id")
                all_ok = False
                continue
            if task.get("status") != "done":
                print(f"[ERR] {agent}: completed task {tid} not marked as done")
                all_ok = False
            if tid not in completed_ids:
                print(f"[WARN] {agent}: task {tid} missing from agents/status.json completed_tasks")
                all_ok = False
    if all_ok:
        print("[OK] Status board matches agent task files.")
    return all_ok


def check_orin_links() -> bool:
    print_header("Linked tasks from Orin")
    orin_tasks = load_json(ROOT / "agents" / "orin" / "task.json")
    if not orin_tasks:
        return False

    all_ok = True
    combined = orin_tasks.get("current_tasks", []) + orin_tasks.get("completed_tasks", [])
    for task in combined:
        for link in task.get("linked_agent_tasks", []):
            agent = link.get("agent", "").lower()
            tid = link.get("task_id")
            status = link.get("status")
            if not agent or not tid:
                print(f"[WARN] Orin linked task missing agent or task_id: {link}")
                all_ok = False
                continue
            agent_tasks = load_json(ROOT / "agents" / agent / "task.json")
            if not agent_tasks:
                all_ok = False
                continue
            match = next(
                (t for t in agent_tasks.get("current_tasks", []) + agent_tasks.get("completed_tasks", []) if t.get("task_id") == tid),
                None,
            )
            if not match:
                print(f"[ERR] {agent}: task {tid} referenced by Orin but not found in agent file")
                all_ok = False
            elif match.get("status") != status:
                print(f"[ERR] {agent}: status mismatch for {tid} (orin={status}, agent={match.get('status')})")
                all_ok = False
    if all_ok:
        print("[OK] Orin linked tasks look consistent.")
    return all_ok


def latest_date_from_log(log_path: Path) -> Optional[datetime.date]:
    if not log_path.exists():
        return None
    for line in reversed(log_path.read_text(encoding="utf-8").splitlines()):
        for part in line.split():
            try:
                return datetime.strptime(part, DATE_FMT).date()
            except ValueError:
                continue
    return None


def check_logs_and_memories() -> bool:
    print_header("Log and memory freshness")
    ok = True
    for agent in AGENTS:
        log_path = ROOT / "agents" / agent / "log.md"
        last_log = latest_date_from_log(log_path)
        if last_log is None:
            print(f"[INFO] {agent}: no dated log entry yet.")
        elif (TODAY - last_log).days > 1:
            print(f"[WARN] {agent}: log last touched on {last_log}")
            ok = False

        mem_path = ROOT / "agents" / agent / "memory.json"
        mem = load_json(mem_path)
        if not mem:
            ok = False
            continue
        last_updated = mem.get("last_updated")
        last_reviewed = mem.get("last_reviewed")

        def parse_memory_date(raw: Optional[str]) -> Optional[datetime.date]:
            if not raw:
                return None
            token = raw.strip().split()[0]
            try:
                return datetime.strptime(token, DATE_FMT).date()
            except ValueError:
                return None

        updated_dt = parse_memory_date(last_updated)
        reviewed_dt = parse_memory_date(last_reviewed)

        reference_dt = max((dt for dt in [updated_dt, reviewed_dt] if dt), default=None)
        if reference_dt is None:
            print(f"[WARN] {agent}: memory.json missing usable last_updated/last_reviewed stamp")
            ok = False
        else:
            age = (TODAY - reference_dt).days
            if age > MAX_MEMORY_AGE_DAYS:
                stamp = last_reviewed or last_updated
                print(f"[WARN] {agent}: memory last confirmed {stamp} ({age} days ago)")
                ok = False
        if last_reviewed and reviewed_dt is None:
            print(f"[ERR] {agent}: last_reviewed has invalid format ({last_reviewed})")
            ok = False
        if last_updated and updated_dt is None:
            print(f"[ERR] {agent}: last_updated has invalid format ({last_updated})")
            ok = False
    if ok:
        print("[OK] Logs and memories look fresh enough.")
    return ok


def check_dashboard() -> bool:
    print_header("Dashboard sanity")
    dash = ROOT / "agents" / "dashboard.md"
    if dash.exists():
        print("[OK] agents/dashboard.md present.")
        return True
    print("[WARN] agents/dashboard.md missing.")
    return False


def check_git_status() -> bool:
    print_header("Git status")
    try:
        result = subprocess.run(["git", "status", "--short"], cwd=ROOT, capture_output=True, text=True, check=True)
    except Exception as exc:  # pragma: no cover
        print(f"[WARN] Could not read git status: {exc}")
        return False
    if result.stdout.strip():
        print(result.stdout.strip())
        print("[WARN] Working tree not clean.")
        return False
    print("[OK] Working tree clean.")
    return True


def main() -> bool:
    print("# Agent System Validation\n")
    ok = True
    ok &= check_agent_files()
    ok &= check_status_alignment()
    ok &= check_orin_links()
    ok &= check_logs_and_memories()
    ok &= check_dashboard()
    ok &= check_git_status()

    print("\n=== SUMMARY ===")
    if ok:
        print("[SUCCESS] All checks passed. System ready for new collaboration tasks.")
    else:
        print("[ACTION] Resolve warnings above before starting new work.")
    return ok


if __name__ == "__main__":
    raise SystemExit(0 if main() else 1)
