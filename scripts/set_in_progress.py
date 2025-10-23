import json
import sys
from pathlib import Path

def set_in_progress(task_json_path, orin_task_id):
    with open(task_json_path, 'r', encoding='utf-8') as f:
        data = json.load(f)

    changed = False
    for task in data.get('current_tasks', []):
        if task['task_id'] == orin_task_id:
            if task['status'] != 'in_progress':
                task['status'] = 'in_progress'
                changed = True
            for agent_task in task.get('linked_agent_tasks', []):
                if agent_task['status'] != 'in_progress':
                    agent_task['status'] = 'in_progress'
                    changed = True
    if changed:
        with open(task_json_path, 'w', encoding='utf-8') as f:
            json.dump(data, f, indent=4, ensure_ascii=False)
        print(f"Statusy zaktualizowane na 'in_progress' dla {orin_task_id}")
    else:
        print(f"Brak zmian. Statusy już były 'in_progress' dla {orin_task_id}")

if __name__ == "__main__":
    if len(sys.argv) != 3:
        print("Użycie: python set_in_progress.py <ścieżka_do_task.json> <ORIN_TASK_ID>")
        sys.exit(1)
    set_in_progress(sys.argv[1], sys.argv[2])
