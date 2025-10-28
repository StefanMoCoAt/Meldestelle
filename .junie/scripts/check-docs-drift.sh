#!/usr/bin/env bash
set -euo pipefail

err=0

has() { grep -q "$2" "$1" || { echo "[DRIFT] '$2' fehlt in $1"; err=1; }; }
miss() { grep -q "$2" "$1" && { echo "[DRIFT] Veralteter Begriff '$2' in $1"; err=1; }; }

# Quelle der Wahrheit: Spring Cloud Gateway
has docs/overview/system-overview.md "Spring Cloud Gateway"
has docs/architecture/adr/0007-api-gateway-pattern-de.md "Spring Cloud Gateway"
miss docs/architecture/adr/0007-api-gateway-pattern-de.md "Ktor"

# C4: Container muss Technology korrekt führen
has docs/architecture/c4/02-container-de.puml "Spring Cloud Gateway"
miss docs/architecture/c4/02-container-de.puml "Ktor"

# Verbiete versehentlich verbliebene englische ADR/C4 ohne -de
if ls docs/architecture/adr/*.md 2>/dev/null | grep -E -v '-de\.md$' >/dev/null; then
  echo "[DRIFT] Englische ADR-Dateien ohne -de gefunden in docs/architecture/adr/"
  ls docs/architecture/adr/*.md | grep -E -v '-de\.md$' || true
  err=1
fi
if ls docs/architecture/c4/*.puml 2>/dev/null | grep -E -v '-de\.puml$' >/dev/null; then
  echo "[DRIFT] Englische C4-Dateien ohne -de gefunden in docs/architecture/c4/"
  ls docs/architecture/c4/*.puml | grep -E -v '-de\.puml$' || true
  err=1
fi

# ADR-Stubs: max. 40 Zeilen und YouTrack-Link Pflicht, wenn als Stub gekennzeichnet
for f in $(grep -RIl "^doc_type: adr-link" docs/architecture/adr 2>/dev/null || true); do
  lines=$(wc -l < "$f" | tr -d ' ')
  if [ "${lines}" -gt 40 ]; then
    echo "[DRIFT] ADR-Stub überschreitet 40 Zeilen: $f (${lines})"
    err=1
  fi
  if ! grep -Eiq "https?://[^ ]*youtrack" "$f"; then
    echo "[DRIFT] ADR-Stub ohne YouTrack-Link: $f"
    err=1
  fi
done

exit $err
