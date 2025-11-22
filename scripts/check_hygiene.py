#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Hygiene checks for Lean workflow.
Run before each session or in pre-commit hook.

Checks:
1. Memory staleness (agents/memory.json)
2. Long in_progress tasks (>7 days)
3. Chronicle gap (recent done tasks missing from chronicle)

Exit codes:
- 0: All checks passed
- 1: Issues found (warnings, not blocking)
"""
import json
import sys
import io
from datetime import datetime, timedelta
from pathlib import Path

# Fix Windows encoding issues
if sys.platform == "win32":
    sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')
    sys.stderr = io.TextIOWrapper(sys.stderr.buffer, encoding='utf-8')


def check_memory_staleness(max_days=7):
    """Alert if agent memory > max_days old"""
    memory_path = Path("agents/memory.json")
    if not memory_path.exists():
        return [f"⚠️  agents/memory.json not found"]

    try:
        memory = json.loads(memory_path.read_text(encoding="utf-8"))
    except Exception as e:
        return [f"⚠️  Error reading memory.json: {e}"]

    today = datetime.now().date()
    issues = []

    for name, agent in memory.get("agents", {}).items():
        last_updated_str = agent.get("last_updated", "")
        if not last_updated_str:
            issues.append(f"⚠️  {name}: missing last_updated field")
            continue

        try:
            last_updated = datetime.strptime(last_updated_str, "%Y-%m-%d").date()
        except ValueError:
            issues.append(f"⚠️  {name}: invalid date format '{last_updated_str}'")
            continue

        age = (today - last_updated).days

        if age > max_days:
            issues.append(
                f"⚠️  {name}: memory stale ({age} days, guideline ≤{max_days} days)\n"
                f"    Action: Update context/insights/todos/risks for recent work"
            )

    return issues


def check_long_in_progress(max_days=7):
    """Alert if tasks in_progress > max_days"""
    board_path = Path("backlog/board.json")
    if not board_path.exists():
        return [f"⚠️  backlog/board.json not found"]

    try:
        board = json.loads(board_path.read_text(encoding="utf-8"))
    except Exception as e:
        return [f"⚠️  Error reading board.json: {e}"]

    today = datetime.now().date()
    issues = []

    for topic in board.get("topics", []):
        for task in topic.get("tasks", []):
            if task.get("status") != "in_progress":
                continue

            task_id = task.get("id", "UNKNOWN")
            topic_id = topic.get("id", "UNKNOWN")

            # Parse log.jsonl to find when it moved to in_progress
            task_log_path = Path(f"backlog/topics/{topic_id}/tasks/{task_id}/log.jsonl")
            if not task_log_path.exists():
                continue

            try:
                lines = task_log_path.read_text(encoding="utf-8").strip().split("\n")
                started_date = None

                for line in lines:
                    if not line.strip():
                        continue
                    try:
                        entry = json.loads(line)
                        # Look for in_progress indicator
                        what = entry.get("what", "").lower()
                        tags = entry.get("tags", [])

                        if "in_progress" in what or "in_progress" in tags:
                            ts_str = entry.get("ts", "")
                            if ts_str:
                                started_date = datetime.fromisoformat(ts_str).date()
                                # Keep first in_progress date
                                break
                    except (json.JSONDecodeError, ValueError):
                        continue

                if started_date:
                    age = (today - started_date).days

                    if age > max_days:
                        issues.append(
                            f"⚠️  {task_id}: in_progress {age} days (guideline ≤{max_days} days)\n"
                            f"    Action: Add checkpoint to PDCA, consider breaking down or architectural review"
                        )
            except Exception as e:
                continue

    return issues


def check_chronicle_staleness(max_days=14):
    """Alert if chronicle not updated after recent milestones"""
    chronicle_path = Path("reports/chronicle.md")
    board_path = Path("backlog/board.json")

    if not chronicle_path.exists():
        return [f"⚠️  reports/chronicle.md not found"]

    if not board_path.exists():
        return [f"⚠️  backlog/board.json not found"]

    try:
        chronicle = chronicle_path.read_text(encoding="utf-8")
    except Exception as e:
        return [f"⚠️  Error reading chronicle.md: {e}"]

    try:
        board = json.loads(board_path.read_text(encoding="utf-8"))
    except Exception as e:
        return [f"⚠️  Error reading board.json: {e}"]

    today = datetime.now().date()
    recent_done = []

    for topic in board.get("topics", []):
        for task in topic.get("tasks", []):
            if task.get("status") != "done":
                continue

            task_id = task.get("id", "UNKNOWN")
            topic_id = topic.get("id", "UNKNOWN")

            # Check if task ID in chronicle
            if task_id in chronicle:
                continue

            # Check when it was marked done
            task_log_path = Path(f"backlog/topics/{topic_id}/tasks/{task_id}/log.jsonl")
            if not task_log_path.exists():
                continue

            try:
                lines = task_log_path.read_text(encoding="utf-8").strip().split("\n")

                for line in reversed(lines):
                    if not line.strip():
                        continue
                    try:
                        entry = json.loads(line)
                        tags = entry.get("tags", [])

                        if "done" in tags or "done" in entry.get("what", "").lower():
                            ts_str = entry.get("ts", "")
                            if ts_str:
                                completed = datetime.fromisoformat(ts_str).date()
                                age = (today - completed).days

                                if age <= max_days:
                                    recent_done.append(f"{task_id} (completed {age} days ago)")
                                break
                    except (json.JSONDecodeError, ValueError):
                        continue
            except Exception:
                continue

    issues = []
    if recent_done:
        issues.append(
            f"⚠️  Chronicle missing recent milestones (last {max_days} days):\n"
            f"    " + "\n    ".join(recent_done) +
            f"\n    Action: Write chronicle entry (Scene, Plot Beats, Artefacts, Cliffhanger)"
        )

    return issues


def main():
    print("=" * 60)
    print("HYGIENE CHECKS (Lean Workflow)")
    print("=" * 60)
    print()

    all_issues = []

    print("Checking memory staleness...")
    memory_issues = check_memory_staleness(max_days=7)
    all_issues.extend(memory_issues)

    print("Checking long in_progress tasks...")
    long_issues = check_long_in_progress(max_days=7)
    all_issues.extend(long_issues)

    print("Checking chronicle gaps...")
    chronicle_issues = check_chronicle_staleness(max_days=14)
    all_issues.extend(chronicle_issues)

    print()
    print("=" * 60)

    if all_issues:
        print("HYGIENE ISSUES FOUND:")
        print()
        for issue in all_issues:
            print(issue)
            print()
        print("=" * 60)
        print()
        print("⚠️  Address hygiene issues before starting new work.")
        print("    These are guidelines, not hard blockers, but maintaining")
        print("    hygiene prevents technical debt accumulation.")
        print()
        return 1
    else:
        print("✅ ALL HYGIENE CHECKS PASSED")
        print()
        print("Memory: Fresh (≤7 days)")
        print("Tasks: No long in_progress (≤7 days)")
        print("Chronicle: Up to date (≤14 days)")
        print()
        return 0


if __name__ == "__main__":
    sys.exit(main())
