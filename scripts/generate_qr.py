#!/usr/bin/env python
"""
Generate a QR code (PNG + ASCII preview) for the internal test link.

Usage:
    python scripts/generate_qr.py [output_path]

If output_path is omitted, `qr-internal-test.png` will be created
under the current working directory.
"""

from __future__ import annotations

import sys
from pathlib import Path

try:
    import qrcode
except ImportError:
    sys.stderr.write(
        "Python package 'qrcode' is required. "
        "Install it via 'pip install qrcode[pil]' and rerun.\n"
    )
    sys.exit(1)


LINK = "https://play.google.com/apps/internaltest/4701145912598647155"
DEFAULT_FILENAME = "qr-internal-test.png"


def main() -> None:
    out_path = Path(sys.argv[1]) if len(sys.argv) > 1 else Path(DEFAULT_FILENAME)
    qr = qrcode.QRCode(border=4, box_size=10)
    qr.add_data(LINK)
    qr.make(fit=True)
    img = qr.make_image(fill_color="black", back_color="white")
    img.save(out_path)
    sys.stdout.write(f"Saved QR code to {out_path.resolve()}\n")

    # Optional ASCII preview (quick sanity check).
    matrix = qr.get_matrix()
    sys.stdout.write("\nASCII preview:\n")
    for row in matrix:
        line = "".join("##" if cell else "  " for cell in row)
        sys.stdout.write(line + "\n")


if __name__ == "__main__":
    main()
