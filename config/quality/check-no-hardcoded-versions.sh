#!/usr/bin/env bash
set -euo pipefail

# Guard: prevent hardcodierte Versionsangaben in Modul-Builddateien
# Erlaubt sind ausschließlich:
# - Zentrale Verwaltung in gradle/libs.versions.toml
# - Referenzen über libs.* Aliases oder ${libs.versions.*.get()} im :platform BOM
# - Ausnahmen müssen dokumentiert sein und mit // ALLOW_VERSION_JUSTIFIED kommentiert werden

ROOT_DIR="$(cd "$(dirname "$0")/../.." && pwd)"

echo "[PR-GUARD] Prüfe auf hartcodierte Versionen in build.gradle(.kts) Dateien..."

# 1) Finde Abhängigkeits-Notation mit expliziter Versionsnummer z.B. "group:artifact:1.2.3"
#    Erlaube explizit Einträge, die mit einem Kommentar  ALLOW_VERSION_JUSTIFIED  versehen sind
VIOLATIONS_A=$(grep -RIn \
  --include='build.gradle' --include='build.gradle.kts' \
  -E '"[^"\$]+:[0-9]+\.[0-9]+' \
  --exclude-dir='.git' \
  --exclude-dir='build' \
  --exclude-dir='.gradle' \
  --exclude='**/platform-bom/build.gradle.kts' \
  --exclude='**/platform-dependencies/build.gradle.kts' \
  "$ROOT_DIR" | grep -v 'ALLOW_VERSION_JUSTIFIED' || true)

# 2) Finde version = "1.2.3" in Gradle-Dateien (selten genutzt, aber absichern)
VIOLATIONS_B=$(grep -RIn \
  --include='build.gradle' --include='build.gradle.kts' \
  -E 'version\s*=\s*"[0-9]+\.[0-9]+' \
  --exclude-dir='.git' \
  --exclude-dir='build' \
  --exclude-dir='.gradle' \
  "$ROOT_DIR" | grep -v 'ALLOW_VERSION_JUSTIFIED' || true)

# 3) Ausnahmen: zentrale Dateien sind erlaubt
#    - gradle/libs.versions.toml (nicht in include)
#    - :platform BOM darf ${libs.versions.*.get()} verwenden (kein Match)

if [[ -n "$VIOLATIONS_A" || -n "$VIOLATIONS_B" ]]; then
  echo "[PR-GUARD] Verletzungen gefunden (hartcodierte Versionen):"
  [[ -n "$VIOLATIONS_A" ]] && echo "$VIOLATIONS_A"
  [[ -n "$VIOLATIONS_B" ]] && echo "$VIOLATIONS_B"
  echo "\n[HINWEIS] Bitte Versionen in gradle/libs.versions.toml pflegen und über libs.* / Platform-BOM referenzieren."
  echo "[AUSNAHME] Falls zwingend erforderlich, kommentiere die betroffene Zeile mit // ALLOW_VERSION_JUSTIFIED und dokumentiere die Ausnahme in docs/01_Architecture/README.md."
  exit 1
fi

echo "[PR-GUARD] OK – keine hartcodierten Versionen gefunden."
