"""Prepare a no-loss cleanup: build a manifest and optionally archive legacy files.

Usage:
  python scripts/archive_legacy.py           # dry run, writes archive/cleanup-manifest.json
  python scripts/archive_legacy.py --archive # copy files into archive/legacy/<ts>/
  python scripts/archive_legacy.py --archive --remove  # copy then delete originals
"""

from __future__ import annotations

import argparse
import hashlib
import json
import shutil
from datetime import datetime
from pathlib import Path
from typing import Iterable, List, Dict, Any

ROOT = Path(__file__).resolve().parents[1]
ARCHIVE_DIR = ROOT / "archive" / "legacy"
MANIFEST = ROOT / "archive" / "cleanup-manifest.json"


CANDIDATE_GLOBS = [
    "agents/status.json",
    "agents/status.md",
    "agents/dashboard.md",
    "agents/*/task.json",
    "agents/*/log.md",
    "agents/storywright/briefs/**/*",
    "sessions/**/*",
    "topics/**/*",
    "board.json",
    "scripts/validate-agent-sync.py",
    "scripts/validate-agent-sync.ps1",
    "scripts/set_in_progress.py",
]


def sha256_of(path: Path) -> str:
    h = hashlib.sha256()
    with path.open("rb") as f:
        for chunk in iter(lambda: f.read(1024 * 1024), b""):
            h.update(chunk)
    return h.hexdigest()


def iter_candidates() -> Iterable[Path]:
    for pattern in CANDIDATE_GLOBS:
        for p in ROOT.glob(pattern):
            if p.is_file():
                yield p


def build_manifest(paths: Iterable[Path]) -> Dict[str, Any]:
    items: List[Dict[str, Any]] = []
    for p in paths:
        try:
            stat = p.stat()
        except FileNotFoundError:
            continue
        items.append(
            {
                "path": str(p.relative_to(ROOT)),
                "size": stat.st_size,
                "mtime": int(stat.st_mtime),
                "sha256": sha256_of(p),
            }
        )
    out = {
        "generated_at": datetime.now().strftime("%Y-%m-%dT%H:%M:%S"),
        "root": str(ROOT),
        "items": items,
    }
    return out


def write_manifest(data: Dict[str, Any]) -> None:
    MANIFEST.parent.mkdir(parents=True, exist_ok=True)
    MANIFEST.write_text(json.dumps(data, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")


def copy_to_archive(manifest: Dict[str, Any]) -> Path:
    ts = datetime.now().strftime("%Y%m%d_%H%M%S")
    dest_root = ARCHIVE_DIR / ts
    for item in manifest.get("items", []):
        rel = Path(item["path"])  # relative to ROOT
        src = ROOT / rel
        dst = dest_root / rel
        dst.parent.mkdir(parents=True, exist_ok=True)
        if src.exists():
            shutil.copy2(src, dst)
    (dest_root / "MANIFEST.json").write_text(json.dumps(manifest, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
    return dest_root


def remove_originals(manifest: Dict[str, Any]) -> None:
    for item in manifest.get("items", []):
        rel = Path(item["path"])  # relative to ROOT
        src = ROOT / rel
        try:
            src.unlink()
        except FileNotFoundError:
            continue


def main() -> None:
    ap = argparse.ArgumentParser()
    ap.add_argument("--archive", action="store_true", help="Copy files into archive/legacy/<ts>/")
    ap.add_argument("--remove", action="store_true", help="Remove originals after archiving (requires --archive)")
    args = ap.parse_args()

    candidates = list(iter_candidates())
    manifest = build_manifest(candidates)
    write_manifest(manifest)
    print(f"Wrote manifest: {MANIFEST}")

    if args.archive:
        dest = copy_to_archive(manifest)
        print(f"Archived {len(manifest['items'])} files to {dest}")
        if args.remove:
            remove_originals(manifest)
            print("Original files removed.")


if __name__ == "__main__":
    main()

