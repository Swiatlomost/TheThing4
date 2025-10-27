#!/usr/bin/env python3
"""
Validate UI skin tokens JSON: required keys, color formats, and ranges.

Usage:
  python scripts/validate_tokens.py <path-to-tokens.json>
"""
import json
import re
import sys
from pathlib import Path

REQ_TOP = ["version", "palette", "glow", "cell", "typography", "animation", "progress"]
COLOR_HEX = re.compile(r"^#([A-Fa-f0-9]{8})$")

def fail(msg):
    print(f"[FAIL] {msg}")
    sys.exit(2)

def ok(msg):
    print(f"[OK] {msg}")

def main():
    if len(sys.argv) < 2:
        print("Usage: python scripts/validate_tokens.py <tokens.json>")
        sys.exit(1)
    path = Path(sys.argv[1])
    if not path.exists():
        fail(f"File not found: {path}")
    data = json.loads(path.read_text(encoding="utf-8"))

    for k in REQ_TOP:
        if k not in data:
            fail(f"Missing top-level key: {k}")
    ok("Top-level keys present")

    # Validate palette colors
    for name, val in data["palette"].items():
        if not isinstance(val, str) or not COLOR_HEX.match(val):
            fail(f"Palette color '{name}' must be #AARRGGBB: got {val}")
    ok("Palette colors valid (#AARRGGBB)")

    # Ranges
    def in01(x):
        return isinstance(x, (int, float)) and 0.0 <= float(x) <= 1.0
    glow = data["glow"]
    if not in01(glow.get("intensity", -1)):
        fail("glow.intensity must be in [0,1]")
    prog = data["progress"]
    if not in01(prog.get("glow-intensity", -1)):
        fail("progress.glow-intensity must be in [0,1]")
    ok("Intensity ranges valid [0,1]")

    # Durations
    anim = data["animation"]
    for k in ("birth-ms", "mature-ms"):
        if not isinstance(anim.get(k, None), int) or anim[k] <= 0:
            fail(f"animation.{k} must be positive integer ms")
    ok("Animation durations valid")

    print("[SUCCESS] Tokens validation passed")

if __name__ == "__main__":
    main()

