#!/bin/bash
#
# Pre-commit Hook für Meldestelle Guidelines Validierung
# Installation: ln -s ../../.junie/scripts/pre-commit-guidelines.sh .git/hooks/pre-commit
#

set -e

echo "🔍 Pre-commit Guidelines Validation..."
echo "======================================"

# Prüfe ob Guidelines-Änderungen vorliegen
GUIDELINES_CHANGED=$(git diff --cached --name-only | grep -E '\.junie/(guidelines|scripts)/' || true)

if [[ -z "$GUIDELINES_CHANGED" ]]; then
    echo "ℹ️  Keine Guidelines-Änderungen erkannt - überspringe Validierung"
    exit 0
fi

echo "📝 Geänderte Guidelines-Dateien:"
echo "$GUIDELINES_CHANGED" | sed 's/^/  - /'
echo ""

# Arbeitsverzeichnis sicherstellen
cd "$(git rev-parse --show-toplevel)"

# Temporäres Verzeichnis für Staging-Area-Dateien
TEMP_DIR=$(mktemp -d)
trap "rm -rf $TEMP_DIR" EXIT

# Staging-Area-Dateien extrahieren für Validierung
echo "🔄 Extrahiere Staging-Area-Dateien..."
echo "$GUIDELINES_CHANGED" | while read file; do
    if [[ -n "$file" ]]; then
        mkdir -p "$TEMP_DIR/$(dirname "$file")"
        git show ":$file" > "$TEMP_DIR/$file" 2>/dev/null || {
            # Neue Dateien (noch nicht im Repository)
            if [[ -f "$file" ]]; then
                cp "$file" "$TEMP_DIR/$file"
            fi
        }
    fi
done

# 1. YAML-Syntax Schnellprüfung
echo "📋 Prüfe YAML-Syntax..."
yaml_errors=0
find "$TEMP_DIR/.junie/guidelines" -name "*.md" -not -path "*/_archived/*" 2>/dev/null | while read file; do
    if [[ -f "$file" ]]; then
        # YAML-Header extrahieren
        yaml_content=$(sed -n '/^---$/,/^---$/p' "$file" | head -n -1 | tail -n +2)
        if [[ -n "$yaml_content" ]]; then
            echo "$yaml_content" | python3 -c "import yaml, sys; yaml.safe_load(sys.stdin)" 2>/dev/null || {
                echo "  ❌ YAML-Fehler in $(basename "$file")"
                yaml_errors=$((yaml_errors + 1))
            }
        fi
    fi
done

if [[ $yaml_errors -gt 0 ]]; then
    echo "❌ $yaml_errors YAML-Syntax-Fehler gefunden"
    exit 1
fi

# 2. Erforderliche Metadaten prüfen
echo "🏷️  Prüfe erforderliche Metadaten..."
metadata_errors=0
find "$TEMP_DIR/.junie/guidelines" -name "*.md" -not -path "*/_archived/*" -not -name "README.md" 2>/dev/null | while read file; do
    if [[ -f "$file" ]]; then
        filename=$(basename "$file")
        missing_fields=()

        if ! grep -q "guideline_type:" "$file"; then
            missing_fields+=("guideline_type")
        fi
        if ! grep -q "ai_context:" "$file"; then
            missing_fields+=("ai_context")
        fi
        if ! grep -q "last_updated:" "$file"; then
            missing_fields+=("last_updated")
        fi

        if [[ ${#missing_fields[@]} -gt 0 ]]; then
            echo "  ❌ $filename fehlt: ${missing_fields[*]}"
            metadata_errors=$((metadata_errors + 1))
        fi
    fi
done

if [[ $metadata_errors -gt 0 ]]; then
    echo "❌ $metadata_errors Metadaten-Fehler gefunden"
    exit 1
fi

# 3. JSON-Konfigurationsdateien validieren
echo "🔧 Prüfe JSON-Konfiguration..."
json_errors=0
find "$TEMP_DIR/.junie/guidelines/_meta" -name "*.json" 2>/dev/null | while read file; do
    if [[ -f "$file" ]]; then
        jq empty "$file" 2>/dev/null || {
            echo "  ❌ JSON-Syntax-Fehler in $(basename "$file")"
            json_errors=$((json_errors + 1))
        }
    fi
done

if [[ $json_errors -gt 0 ]]; then
    echo "❌ $json_errors JSON-Syntax-Fehler gefunden"
    exit 1
fi

# 4. Aktuelles Datum in last_updated prüfen
echo "📅 Prüfe Datum-Aktualität..."
current_date=$(date +%Y-%m-%d)
date_warnings=0
find "$TEMP_DIR/.junie/guidelines" -name "*.md" -not -path "*/_archived/*" 2>/dev/null | while read file; do
    if [[ -f "$file" ]]; then
        filename=$(basename "$file")
        if grep -q "last_updated:" "$file"; then
            file_date=$(grep "last_updated:" "$file" | cut -d'"' -f2 2>/dev/null || echo "")
            if [[ "$file_date" != "$current_date" && -n "$file_date" ]]; then
                echo "  ⚠️  $filename hat Datum $file_date (heute: $current_date)"
                date_warnings=$((date_warnings + 1))
            fi
        fi
    fi
done

if [[ $date_warnings -gt 0 ]]; then
    echo "⚠️  $date_warnings Guidelines haben veraltete Daten (wird toleriert)"
fi

# 5. Script-Berechtigungen prüfen (nur bei geänderten Scripts)
SCRIPT_CHANGES=$(echo "$GUIDELINES_CHANGED" | grep -E '\.junie/scripts/.*\.sh$' || true)
if [[ -n "$SCRIPT_CHANGES" ]]; then
    echo "⚙️  Prüfe Script-Berechtigungen..."
    script_errors=0
    echo "$SCRIPT_CHANGES" | while read script; do
        if [[ -f "$script" && ! -x "$script" ]]; then
            echo "  ❌ $script ist nicht ausführbar"
            script_errors=$((script_errors + 1))
        fi
    done

    if [[ $script_errors -gt 0 ]]; then
        echo "❌ $script_errors Script-Berechtigungsfehler"
        echo "💡 Lösung: chmod +x <script-name>"
        exit 1
    fi
fi

# 6. Schnelle Link-Validierung (nur bei verfügbarem validate-links.sh)
if [[ -x ".junie/scripts/validate-links.sh" ]]; then
    echo "🔗 Schnelle Link-Validierung..."
    if ! ./.junie/scripts/validate-links.sh --quick --staged 2>/dev/null; then
        echo "⚠️  Link-Validierung fehlgeschlagen (wird toleriert im Pre-commit)"
    fi
fi

# Erfolgreiche Validierung
echo ""
echo "✅ Pre-commit Guidelines Validation erfolgreich!"
echo "   - YAML-Syntax: OK"
echo "   - Metadaten: OK"
echo "   - JSON-Konfiguration: OK"
echo "   - Script-Berechtigungen: OK"
if [[ $date_warnings -gt 0 ]]; then
    echo "   - Datum-Warnings: $date_warnings (toleriert)"
fi
echo ""
echo "🚀 Commit kann fortgesetzt werden..."

exit 0
