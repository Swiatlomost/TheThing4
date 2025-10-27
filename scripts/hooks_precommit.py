"""Pre-commit helper: validate lean structure and refresh backlog index.

Does not block commits (returns 0) but prints warnings if validation fails.
"""

from __future__ import annotations

import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


def run(cmd):
    return subprocess.run(cmd, cwd=ROOT, text=True)


def main() -> int:
    print("[hooks] validate.py ...")
    v = run([sys.executable, "scripts/validate.py"])  # ignore exit code
    print("[hooks] update_backlog_index.py ...")
    run([sys.executable, "scripts/update_backlog_index.py"]).check_returncode()
    print("[hooks] done")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

