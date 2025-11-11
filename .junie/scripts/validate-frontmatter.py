"""
Validiert YAML-Frontmatter in Markdown-Dateien unterhalb von docs/ gegen ein JSON-Schema.

- Erwartet das Schema in docs/.frontmatter.schema.json
- Meldet fehlendes oder ungültiges Frontmatter pro Datei
- Exit-Code 0 bei Erfolg, 1 wenn mindestens ein Fehler auftritt
"""

from __future__ import annotations

import json
import re
import sys
from pathlib import Path
from typing import Any

import yaml

try:
    import jsonschema
except ImportError:  # pragma: no cover
    # GitHub Actions step installiert dies vor dem Lauf; freundlichere Fehlermeldung
    raise SystemExit("[FM] jsonschema nicht installiert. Bitte ausführen: pip install jsonschema pyyaml")


SCHEMA_PATH = Path("docs/.frontmatter.schema.json")
# Erlaubt LF und CRLF, verlangt Frontmatter am Datei-Anfang
FM_REGEX = re.compile(r"\A---\r?\n(.*?)\r?\n---(?:\r?\n|$)", re.S)


def load_schema(path: Path) -> dict[str, Any]:
    try:
        with path.open(encoding="utf-8") as f:
            return json.load(f)
    except FileNotFoundError:
        print(f"[FM] Schema-Datei fehlt: {path.as_posix()}")
        sys.exit(1)
    except json.JSONDecodeError as e:  # pragma: no cover
        print(f"[FM] Ungültiges JSON-Schema in {path.as_posix()}: {e}")
        sys.exit(1)


def extract_frontmatter(text: str) -> str | None:
    m = FM_REGEX.search(text)
    return m.group(1) if m else None


def validate_file(path: Path, schema: dict[str, Any]) -> bool:
    content = path.read_text(encoding="utf-8")
    fm_text = extract_frontmatter(content)
    if fm_text is None:
        print(f"[FM] fehlt: {path.as_posix()}")
        return False
    try:
        fm = yaml.safe_load(fm_text) or {}
        jsonschema.validate(fm, schema)
        return True
    except Exception as e:  # jsonschema.ValidationError u.a.
        print(f"[FM] ungültig in {path.as_posix()}: {e}")
        return False


def main() -> int:
    schema = load_schema(SCHEMA_PATH)

    had_error = False
    for path in Path("docs").rglob("*.md"):
        # ADRs und ggf. generierte Inhalte vorerst ausnehmen (separater Rollout für FM)
        if path.as_posix().startswith("docs/architecture/adr/"):
            continue
        if not validate_file(path, schema):
            had_error = True

    return 1 if had_error else 0


if __name__ == "__main__":
    sys.exit(main())
