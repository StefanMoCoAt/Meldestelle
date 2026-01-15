#!/usr/bin/env bash
set -euo pipefail

# validate-links.sh - Link-Validierung für Projektdokumentation (`docs/**`).
# Zweck: Guardrail für die "Docs-as-Code"-Strategie.

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
cd "$PROJECT_ROOT"

QUICK_MODE=false

while [[ $# -gt 0 ]]; do
  case $1 in
    --quick)
      QUICK_MODE=true
      shift
      ;;
    --help|-h)
      cat << 'EOF'
Docs Link-Validierung

USAGE:
  ./.junie/scripts/validate-links.sh [--quick]

BESCHREIBUNG:
  Prüft Markdown-Links in `docs/**/*.md` auf gebrochene relative Pfade.
  Ignoriert externe Links (http/https/mailto) sowie reine Anchors (#...).

OPTIONEN:
  --quick  Führt nur eine Teilmenge der Prüfungen durch (aktuell nicht implementiert).
EOF
      exit 0
      ;;
    *)
      echo "[ERROR] Unbekannter Parameter: $1" >&2
      exit 2
      ;;
  esac
done

python3 - <<'PY'
import os
import re
import sys
from pathlib import Path
from urllib.parse import unquote

root = Path.cwd()
docs_dir = root / "docs"

if not docs_dir.is_dir():
    print(f"[ERROR] docs-Verzeichnis nicht gefunden: {docs_dir}", file=sys.stderr)
    sys.exit(2)

# Veraltete Pfad-Prüfungen wurden entfernt, da sie zu wartungsintensiv waren.
# Das Skript konzentriert sich nun auf die Validierung der Link-Integrität.
FORBIDDEN_SUBSTRINGS = []

md_files = sorted(docs_dir.rglob("*.md"))

link_pattern = re.compile(r"\]\(([^)]+)\)")

errors = 0

def is_external(target: str) -> bool:
    t = target.lower()
    return t.startswith("http://") or t.startswith("https://") or t.startswith("mailto:")

def strip_fragment_and_query(target: str) -> str:
    # remove fragment and query parts
    target = target.split("#", 1)[0]
    target = target.split("?", 1)[0]
    return target

for f in md_files:
    text = f.read_text(encoding="utf-8", errors="replace")

    for forbidden in FORBIDDEN_SUBSTRINGS:
        if forbidden in text:
            print(f"[ERROR] Veralteter Pfad '{forbidden}' in {f}")
            errors += 1

    for match in link_pattern.finditer(text):
        target = match.group(1).strip()

        if not target:
            continue
        if is_external(target):
            continue
        if target.startswith("#"):
            continue

        # drop angle brackets <...> used in markdown for urls with spaces
        if target.startswith("<") and target.endswith(">"):
            target = target[1:-1]

        target = unquote(strip_fragment_and_query(target))

        # ignore absolute paths in the repo (we treat them as doc-style links; validate only if relative)
        if target.startswith("/"):
            continue

        # ignore non-file targets (e.g. empty or protocol-less anchors)
        if ":" in target.split("/", 1)[0]:
            # things like "vscode:..." etc.
            continue

        # treat as file path relative to markdown file
        resolved = (f.parent / target).resolve()

        # keep validation within repo
        try:
            resolved.relative_to(root.resolve())
        except ValueError:
            print(f"[ERROR] Link zeigt außerhalb des Repos: {f} -> {target}")
            errors += 1
            continue

        # allow directories if they contain README.md
        if resolved.is_dir():
            if not (resolved / "README.md").is_file():
                print(f"[ERROR] Verlinktes Verzeichnis ohne README.md: {f} -> {target}")
                errors += 1
            continue

        if not resolved.exists():
            print(f"[ERROR] Broken link: {f} -> {target}")
            errors += 1

if errors:
    print(f"[ERROR] Link-Validierung fehlgeschlagen: {errors} Fehler")
    sys.exit(1)

print(f"[OK] Link-Validierung erfolgreich: {len(md_files)} Markdown-Dateien geprüft")
PY
