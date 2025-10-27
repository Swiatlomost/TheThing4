import json
import sys
from pathlib import Path
from typing import List

USAGE = "Usage: python set_in_progress.py <path_to_orin_task.json> <ORIN_TASK_ID>"


def set_in_progress(task_path: Path, orin_task_id: str) -> bool:
    data = json.loads(task_path.read_text(encoding="utf-8"))
    changed = False

    for task in data.get("current_tasks", []):
        if task.get("task_id") != orin_task_id:
            continue
        if task.get("status") != "in_progress":
            task["status"] = "in_progress"
            changed = True
        for link in task.get("linked_agent_tasks", []):
            if link.get("status") != "in_progress":
                link["status"] = "in_progress"
                changed = True
    if changed:
        task_path.write_text(json.dumps(data, indent=4) + "\n", encoding="utf-8")
    return changed


def main(argv: List[str]) -> None:
    if len(argv) != 3:
        print(USAGE)
        sys.exit(1)
    task_path = Path(argv[1])
    if not task_path.exists():
        print(f"Task file not found: {task_path}")
        sys.exit(1)
    task_id = argv[2]
    if set_in_progress(task_path, task_id):
        print(f"Updated {task_id} and linked agent tasks to in_progress.")
    else:
        print(f"No changes applied for {task_id}. Ensure the task exists and is pending.")


if __name__ == "__main__":
    main(sys.argv)
