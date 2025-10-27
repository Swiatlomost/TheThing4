#!/usr/bin/env python3
"""
Sync UI skin tokens from backlog to runtime resource.

Default source:
  backlog/ui/topics/TOPIC-20251027_131500-001/tokens/tokens.json

Destination:
  core/designsystem/src/main/res/raw/ui_tokens.json

Usage:
  python scripts/skin_tokens_sync.py [<src> [<dst>]]
"""
import json
import shutil
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
DEFAULT_SRC = ROOT / "backlog/ui/topics/TOPIC-20251027_131500-001/tokens/tokens.json"
DEFAULT_DST = ROOT / "core/designsystem/src/main/res/raw/ui_tokens.json"

def main():
    src = Path(sys.argv[1]) if len(sys.argv) > 1 else DEFAULT_SRC
    dst = Path(sys.argv[2]) if len(sys.argv) > 2 else DEFAULT_DST

    if not src.exists():
        print(f"[ERROR] Source not found: {src}")
        sys.exit(1)

    # Validate JSON loads
    with src.open("r", encoding="utf-8") as f:
        data = json.load(f)

    dst.parent.mkdir(parents=True, exist_ok=True)
    shutil.copyfile(src, dst)
    print(f"[OK] Synced tokens: {src} -> {dst}")
    print(f"[INFO] version={data.get('version')} palette.keys={list(data.get('palette', {}).keys())[:3]}…")

if __name__ == "__main__":
    main()

