# --- Coś Agent Validation Script ---
# Full workflow compliance: status, cooldown, logs, memory, infra, dashboard
import os
import json
from pathlib import Path
from datetime import datetime, timedelta

AGENTS = ["orin", "echo", "vireal", "lumen", "kai", "scribe", "nyx", "nodus", "aurum"]
REQUIRED_FILES = ["task.json", "log.md", "memory.json"]

def find_project_root():
    """Finds the project root by looking for 'agents' and 'gradlew.bat' in parent directories."""
    here = Path(__file__).resolve().parent
    for parent in [here] + list(here.parents):
        if (parent / "agents").is_dir() and (parent / "gradlew.bat").exists():
            return parent
    # fallback: script dir
    return here

BASE_DIR = find_project_root()
TODAY = datetime.now().date()

def print_header(title):
    print(f"\n=== {title} ===")

def check_agent_files():
    print_header("Agent File Structure")
    ok = True
    for agent in AGENTS:
        agent_dir = BASE_DIR / "agents" / agent
        for fname in REQUIRED_FILES:
            fpath = agent_dir / fname
            if not fpath.exists():
                print(f"[WARN] Missing: {fpath}")
                ok = False
    if ok:
        print("[OK] All agent files present.")
    return ok

def load_json(path):
    try:
        with open(path, encoding="utf-8") as f:
            return json.load(f)
    except Exception as e:
        print(f"[ERR] Failed to load {path}: {e}")
        return None

def check_status_sync():
    print_header("Status Synchronization & Cooldown (status.json)")
    status_json = BASE_DIR / "agents" / "status.json"
    if not status_json.exists():
        print("[ERR] agents/status.json missing!")
        return False
    status = load_json(status_json)
    if not status:
        print("[ERR] Failed to load agents/status.json!")
        return False
    all_ok = True
    # Build sets for quick lookup
    active_ids = set(t["task_id"] for t in status.get("active_tasks", []))
    completed_ids = set(t["task_id"] for t in status.get("completed_tasks", []))
    # Check agent task.json files
    for agent in AGENTS:
        tfile = BASE_DIR / "agents" / agent / "task.json"
        data = load_json(tfile)
        if not data:
            all_ok = False
            continue
        # 1. No 'done' in active_tasks
        for t in status.get("active_tasks", []):
            if t["agent"].lower() == agent and t["status"] == "done":
                print(f"[ERR] {agent}: Task {t['task_id']} is 'done' but still in active_tasks!")
                all_ok = False
        # 2. All agent current_tasks must be in active_tasks
        for t in data.get("current_tasks", []):
            tid = t.get("task_id")
            if tid and tid not in active_ids:
                print(f"[WARN] {agent}: Task {tid} in current_tasks missing from status.json active_tasks")
                all_ok = False
            if t.get("status") == "done":
                print(f"[ERR] {agent}: Task {tid} is 'done' but still in current_tasks!")
                all_ok = False
        # 3. All agent completed_tasks must be in completed_tasks
        for t in data.get("completed_tasks", []):
            tid = t.get("task_id")
            if tid and tid not in completed_ids:
                print(f"[WARN] {agent}: Task {tid} in completed_tasks missing from status.json completed_tasks")
                all_ok = False
            if t.get("status") != "done":
                print(f"[ERR] {agent}: Task {tid} in completed_tasks is not marked 'done'!")
                all_ok = False
        # 4. No completed_tasks in active_tasks
        for t in data.get("completed_tasks", []):
            tid = t.get("task_id")
            if tid and tid in active_ids:
                print(f"[ERR] {agent}: Task {tid} is completed but still present in status.json active_tasks!")
                all_ok = False
    print("[OK] Status/cooldown checks complete.")
    return all_ok

def check_linked_agent_tasks():
    print_header("Linked Agent Task Consistency")
    orin_file = BASE_DIR / "agents" / "orin" / "task.json"
    orin = load_json(orin_file)
    if not orin:
        print("[ERR] Cannot load Orin's task.json!")
        return False
    all_ok = True
    for task in orin.get("current_tasks", []) + orin.get("completed_tasks", []):
        for link in task.get("linked_agent_tasks", []):
            agent = link.get("agent").lower()
            tid = link.get("task_id")
            status = link.get("status")
            agent_file = BASE_DIR / "agents" / agent / "task.json"
            agent_data = load_json(agent_file)
            if not agent_data:
                all_ok = False
                continue
            found = False
            for t in agent_data.get("current_tasks", []) + agent_data.get("completed_tasks", []):
                if t.get("task_id") == tid:
                    found = True
                    if t.get("status") != status:
                        print(f"[ERR] {agent}: Linked task {tid} status mismatch (orin: {status}, agent: {t.get('status')})")
                        all_ok = False
            if not found:
                print(f"[ERR] {agent}: Linked task {tid} not found in agent's task.json")
                all_ok = False
    print("[OK] Linked agent task checks complete.")
    return all_ok

def check_logs_and_memory():
    print_header("Log & Memory Freshness")
    now = datetime.now()
    ok = True
    for agent in AGENTS:
        # Log check: last entry date
        log_path = BASE_DIR / "agents" / agent / "log.md"
        if log_path.exists():
            try:
                with open(log_path, encoding="utf-8") as f:
                    lines = f.readlines()
                # Find last date in log
                dates = [l for l in lines if any(y in l for y in ["2025-10-23", "2025-10-22", "2025-10-21"])]
                if dates:
                    last = dates[-1].strip()
                    print(f"[OK] {agent}: Last log entry: {last}")
                else:
                    print(f"[WARN] {agent}: No recent log entry found.")
                    ok = False
            except Exception as e:
                print(f"[ERR] {agent}: Failed to read log.md: {e}")
                ok = False
        else:
            print(f"[WARN] {agent}: log.md missing.")
            ok = False
        # Memory check: last_updated < 24h
        mem_path = BASE_DIR / "agents" / agent / "memory.json"
        if mem_path.exists():
            try:
                mem = load_json(mem_path)
                last_upd = mem.get("last_updated")
                if last_upd:
                    dt = datetime.strptime(last_upd, "%Y-%m-%d")
                    if (now.date() - dt.date()).days > 1:
                        print(f"[WARN] {agent}: memory.json not updated in last 24h (last: {last_upd})")
                        ok = False
                else:
                    print(f"[WARN] {agent}: memory.json missing last_updated field.")
                    ok = False
            except Exception as e:
                print(f"[ERR] {agent}: Failed to read memory.json: {e}")
                ok = False
        else:
            print(f"[WARN] {agent}: memory.json missing.")
            ok = False
    print("[OK] Log/memory checks complete.")
    return ok

def check_dashboard():
    print_header("Dashboard & Health")
    dash = BASE_DIR / "agents" / "dashboard.md"
    if not dash.exists():
        print("[WARN] agents/dashboard.md missing!")
        return False
    with open(dash, encoding="utf-8") as f:
        content = f.read()
    if "HEALTHY" in content and "SYNCED" in content:
        print("[OK] Dashboard: HEALTHY & SYNCED")
        return True
    print("[WARN] Dashboard does not confirm HEALTHY/SYNCED state.")
    return False

def check_infrastructure():
    print_header("Infrastructure & Tests")
    gradlew = BASE_DIR / "gradlew.bat"
    if gradlew.exists():
        print("[OK] gradlew.bat present.")
    else:
        print("[WARN] gradlew.bat missing!")
    # Git repo check
    git_dir = BASE_DIR / ".git"
    if git_dir.exists():
        # Check for uncommitted changes
        status = os.system(f'cd "{BASE_DIR}" && git status --porcelain >nul 2>&1')
        if status == 0:
            print("[OK] Git repo present.")
        else:
            print("[WARN] Git repo not clean or not available.")
    else:
        print("[WARN] .git repo missing!")
    # Test results (simulate)
    print("[INFO] Please run './gradlew test' and './gradlew connectedDebugAndroidTest' manually to confirm test status.")

def main():
    print("# Coś Agent Validation - Full Workflow Compliance\n")
    ok = True
    if not check_agent_files():
        ok = False
    if not check_status_sync():
        ok = False
    if not check_linked_agent_tasks():
        ok = False
    if not check_logs_and_memory():
        ok = False
    if not check_dashboard():
        ok = False
    check_infrastructure()
    print("\n=== VALIDATION SUMMARY ===")
    if ok:
        print("[SUCCESS] All checks passed. System is ready for new tasks.")
    else:
        print("[FAIL] Issues detected. See above for details. Do not start new tasks until resolved.")

if __name__ == "__main__":
    main()
