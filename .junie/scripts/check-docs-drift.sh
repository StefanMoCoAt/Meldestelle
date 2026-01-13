#!/usr/bin/env bash
set -euo pipefail

# check-docs-drift.sh
# Zweck: sehr schlanke Drift-Checks gegen die neue Doku-Struktur.
# - Kein Guidelines-System mehr.
# - Single Source of Truth: `docs/`

err=0

has() { grep -q "$2" "$1" || { echo "[DRIFT] '$2' fehlt in $1"; err=1; }; }
miss() { grep -q "$2" "$1" && { echo "[DRIFT] Veralteter Begriff '$2' in $1"; err=1; }; }

# Harte Altlast-Pfade dürfen nicht mehr vorkommen
if git grep -n "docs/00_Domain/" -- docs >/dev/null 2>&1; then
  echo "[DRIFT] Veralteter Pfad 'docs/00_Domain/' in docs/* gefunden"
  err=1
fi
if git grep -n "docs/adr/" -- docs >/dev/null 2>&1; then
  echo "[DRIFT] Veralteter Pfad 'docs/adr/' in docs/* gefunden"
  err=1
fi
if git grep -n "docs/c4/" -- docs >/dev/null 2>&1; then
  echo "[DRIFT] Veralteter Pfad 'docs/c4/' in docs/* gefunden"
  err=1
fi
if git grep -n "docs/how-to/" -- docs >/dev/null 2>&1; then
  echo "[DRIFT] Veralteter Pfad 'docs/how-to/' in docs/* gefunden"
  err=1
fi
if git grep -n "docs/reference/" -- docs >/dev/null 2>&1; then
  echo "[DRIFT] Veralteter Pfad 'docs/reference/' in docs/* gefunden"
  err=1
fi

# Quelle der Wahrheit: Gateway-Technologie (sollte in Architektur/ADRs/C4 konsistent sein)
has docs/01_Architecture/ARCHITECTURE.md "Spring Cloud Gateway"
has docs/01_Architecture/adr/0007-api-gateway-pattern-de.md "Spring Cloud Gateway"
miss docs/01_Architecture/adr/0007-api-gateway-pattern-de.md "Ktor"
has docs/01_Architecture/c4/02-container-de.puml "Spring Cloud Gateway"
miss docs/01_Architecture/c4/02-container-de.puml "Ktor"

exit $err
