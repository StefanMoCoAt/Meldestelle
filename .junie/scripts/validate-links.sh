#!/bin/bash

# validate-links.sh - Automatisierte Link-Validierung für Meldestelle Guidelines
# Version: 1.0.0
# Autor: Junie AI-Assistant
# Datum: 2025-09-15

set -euo pipefail

# Bestimme das Projekt-Root-Verzeichnis
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"

# Wechsle ins Projekt-Root für korrekte relative Pfade
cd "$PROJECT_ROOT"

GUIDELINES_DIR=".junie/guidelines"
META_DIR="$GUIDELINES_DIR/_meta"
CROSS_REFS_FILE="$META_DIR/cross-refs.json"
SCRIPTS_DIR=".junie/scripts"

# Farben für Output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Logging-Funktionen
log_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

log_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

log_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

log_error() {
    echo -e "${RED}❌ $1${NC}"
}

# Hauptvariablen
ERRORS=0
WARNINGS=0
QUICK_MODE=false

# Command-line Parameter
while [[ $# -gt 0 ]]; do
    case $1 in
        --quick)
            QUICK_MODE=true
            shift
            ;;
        --help|-h)
            cat << 'EOF'
Meldestelle Guidelines Link-Validierung

USAGE:
    ./validate-links.sh [OPTIONS]

OPTIONS:
    --quick     Schnelle Validierung (nur kritische Checks)
    --help      Diese Hilfe anzeigen

BESCHREIBUNG:
    Validiert alle Links und Cross-Referenzen in den Guidelines basierend auf
    der cross-refs.json Matrix. Prüft:

    - Cross-Referenzen zwischen Guidelines
    - Markdown-Links in Guidelines
    - YAML-Metadaten-Konsistenz
    - Template-Struktur-Konsistenz

EXIT-CODES:
    0 = Alle Validierungen erfolgreich
    1 = Fehler gefunden
    2 = Warnings gefunden (nur bei --strict)

EOF
            exit 0
            ;;
        *)
            log_error "Unbekannter Parameter: $1"
            echo "Nutze --help für Hilfe"
            exit 1
            ;;
    esac
done

echo "🔍 Meldestelle Guidelines Link-Validierung"
echo "=================================================="
echo "Datum: $(date '+%Y-%m-%d %H:%M:%S')"
echo "Modus: $([ "$QUICK_MODE" = true ] && echo "Quick" || echo "Vollständig")"
echo ""

# Prüfe ob erforderliche Dateien existieren
if [[ ! -f "$CROSS_REFS_FILE" ]]; then
    log_error "Cross-Referenz-Datei nicht gefunden: $CROSS_REFS_FILE"
    exit 1
fi

if [[ ! -d "$GUIDELINES_DIR" ]]; then
    log_error "Guidelines-Verzeichnis nicht gefunden: $GUIDELINES_DIR"
    exit 1
fi

# 1. Cross-Referenz-Validierung
validate_cross_references() {
    log_info "Validiere Cross-Referenzen aus cross-refs.json..."

    local temp_guidelines=$(mktemp)
    local temp_refs=$(mktemp)

    # Alle Guidelines aus cross-refs.json extrahieren
    jq -r '.cross_references | keys[]' "$CROSS_REFS_FILE" > "$temp_guidelines" 2>/dev/null || {
        log_error "Fehler beim Lesen der cross-refs.json"
        ((ERRORS++))
        return
    }

    while IFS= read -r guideline; do
        guideline_file="$GUIDELINES_DIR/$guideline"

        # Prüfe ob Guideline-Datei existiert
        if [[ ! -f "$guideline_file" ]]; then
            log_error "Guideline '$guideline' in cross-refs.json aber Datei fehlt: $guideline_file"
            ((ERRORS++))
            continue
        fi

        # Hole referenzierte Guidelines aus JSON
        jq -r ".cross_references[\"$guideline\"].references_to[]? // empty" "$CROSS_REFS_FILE" > "$temp_refs" 2>/dev/null

        while IFS= read -r ref; do
            # Skip leere Zeilen
            [[ -z "$ref" ]] && continue

            # Relativer Pfad zu absolut konvertieren
            if [[ "$ref" == /* ]]; then
                ref_file="$GUIDELINES_DIR$ref"
            elif [[ "$ref" == *"/" ]]; then
                # Directory-Referenz (z.B. technology-guides/docker/)
                ref_dir="$GUIDELINES_DIR/$ref"
                if [[ ! -d "$ref_dir" ]]; then
                    log_error "Referenziertes Verzeichnis '$ref' existiert nicht: $ref_dir"
                    ((ERRORS++))
                fi
                continue
            else
                # Alle Referenzen sind relativ zum Guidelines-Root-Verzeichnis
                ref_file="$GUIDELINES_DIR/$ref"
            fi

            # Normalisiere Pfad
            ref_file=$(realpath -m "$ref_file" 2>/dev/null || echo "$ref_file")

            # Prüfe ob referenzierte Datei existiert
            if [[ ! -f "$ref_file" ]]; then
                log_error "'$guideline' referenziert '$ref', aber Datei existiert nicht: $ref_file"
                ((ERRORS++))
                continue
            fi

            # Prüfe ob der Link tatsächlich im Markdown existiert (nur im vollständigen Modus)
            if [[ "$QUICK_MODE" = false ]]; then
                ref_basename=$(basename "$ref" .md)
                if ! grep -q "\[$ref_basename\]" "$guideline_file" && ! grep -q "($ref)" "$guideline_file"; then
                    log_warning "'$guideline' sollte '$ref' referenzieren, aber Link fehlt im Markdown"
                    ((WARNINGS++))
                fi
            fi

        done < "$temp_refs"

        log_success "'$guideline' - Cross-Referenzen validiert"

    done < "$temp_guidelines"

    rm -f "$temp_guidelines" "$temp_refs"
}

# 2. Markdown-Links Validierung
validate_markdown_links() {
    if [[ "$QUICK_MODE" = true ]]; then
        log_info "Überspringe Markdown-Link-Validierung (Quick-Modus)"
        return
    fi

    log_info "Validiere Markdown-Links in Guidelines..."

    find "$GUIDELINES_DIR" -name "*.md" -not -path "*/_archived/*" | while read file; do
        # Relative Links extrahieren ([Text](./path/file.md))
        grep -o "\[.*\](\./[^)]*\.md)" "$file" 2>/dev/null | sed 's/.*(\.\///' | sed 's/).*//' | while read link; do
            [[ -z "$link" ]] && continue

            # Absoluten Pfad konstruieren
            dir=$(dirname "$file")
            target_file="$dir/$link"
            target_file=$(realpath -m "$target_file" 2>/dev/null || echo "$target_file")

            if [[ ! -f "$target_file" ]]; then
                log_error "$(basename "$file") verlinkt auf '$link', aber Ziel existiert nicht: $target_file"
                ((ERRORS++))
            fi
        done

        # Relative Links mit ../ extrahieren
        grep -o "\[.*\](\.\./[^)]*\.md)" "$file" 2>/dev/null | sed 's/.*(\.\.\///' | sed 's/).*//' | while read link; do
            [[ -z "$link" ]] && continue

            dir=$(dirname "$file")
            target_file="$dir/../$link"
            target_file=$(realpath -m "$target_file" 2>/dev/null || echo "$target_file")

            if [[ ! -f "$target_file" ]]; then
                log_error "$(basename "$file") verlinkt auf '../$link', aber Ziel existiert nicht: $target_file"
                ((ERRORS++))
            fi
        done

        log_success "$(basename "$file") - Markdown-Links validiert"
    done
}

# 3. YAML-Metadaten Validierung
validate_yaml_metadata() {
    log_info "Validiere YAML-Metadaten-Konsistenz..."

    find "$GUIDELINES_DIR" -name "*.md" -not -path "*/_archived/*" -not -name "README.md" -not -name "master-guideline.md" | while read file; do
        # YAML-Header extrahieren (nur zwischen den ersten beiden --- Zeilen)
        yaml_content=$(awk '/^---$/{if(++count==2) exit} count==1 && !/^---$/{print}' "$file" 2>/dev/null || echo "")

        if [[ -z "$yaml_content" ]]; then
            log_warning "'$(basename "$file")' hat keinen YAML-Header"
            ((WARNINGS++))
            continue
        fi

        # YAML-Syntax prüfen (falls python verfügbar)
        if command -v python3 &> /dev/null; then
            echo "$yaml_content" | python3 -c "import yaml,sys; yaml.safe_load(sys.stdin)" 2>/dev/null || {
                log_error "'$(basename "$file")' hat ungültige YAML-Syntax im Header"
                ((ERRORS++))
                continue
            }
        fi

        # Erforderliche Felder prüfen
        required_fields=("guideline_type" "scope" "audience" "last_updated" "ai_context")
        for field in "${required_fields[@]}"; do
            if ! echo "$yaml_content" | grep -q "^$field:" 2>/dev/null; then
                log_error "'$(basename "$file")' fehlt erforderliches YAML-Feld: $field"
                ((ERRORS++))
            fi
        done

        # Dependencies validieren (nur im vollständigen Modus)
        if [[ "$QUICK_MODE" = false ]]; then
            deps=$(echo "$yaml_content" | grep "dependencies:" 2>/dev/null | sed 's/.*\[//' | sed 's/\].*//' | tr ',' '\n' | sed 's/[" ]//g' || echo "")
            for dep in $deps; do
                [[ -z "$dep" ]] && continue
                dep_file="$GUIDELINES_DIR/$dep"
                if [[ ! -f "$dep_file" ]]; then
                    log_error "'$(basename "$file")' dependency '$dep' existiert nicht"
                    ((ERRORS++))
                fi
            done
        fi

        log_success "$(basename "$file") - Metadaten validiert"
    done
}

# 4. Template-Struktur Validierung
validate_template_structure() {
    if [[ "$QUICK_MODE" = true ]]; then
        log_info "Überspringe Template-Validierung (Quick-Modus)"
        return
    fi

    log_info "Validiere Template-Struktur-Konsistenz..."

    local template_dir="$GUIDELINES_DIR/_templates"
    if [[ ! -d "$template_dir" ]]; then
        log_warning "Template-Verzeichnis nicht gefunden: $template_dir"
        ((WARNINGS++))
        return
    fi

    # Prüfe ob neue Guidelines Template-Struktur folgen
    find "$GUIDELINES_DIR" -name "*.md" -not -path "*/_archived/*" -not -path "*/_templates/*" -not -name "README.md" | while read file; do
        # Prüfe grundlegende Template-Struktur
        if ! grep -q "^guideline_type:" "$file" 2>/dev/null; then
            log_warning "'$(basename "$file")' folgt möglicherweise nicht der Template-Struktur (fehlt guideline_type)"
            ((WARNINGS++))
        fi

        if ! grep -q "^ai_context:" "$file" 2>/dev/null; then
            log_warning "'$(basename "$file")' folgt möglicherweise nicht der Template-Struktur (fehlt ai_context)"
            ((WARNINGS++))
        fi
    done

    log_success "Template-Struktur validiert"
}

# Hauptvalidierung ausführen
main() {
    validate_cross_references
    validate_markdown_links
    validate_yaml_metadata
    validate_template_structure

    echo ""
    echo "=================================================="
    echo "📊 Validierungs-Ergebnisse:"
    echo "   Fehler: $ERRORS"
    echo "   Warnungen: $WARNINGS"

    if [[ $ERRORS -eq 0 && $WARNINGS -eq 0 ]]; then
        log_success "Alle Validierungen erfolgreich! 🎉"
        exit 0
    elif [[ $ERRORS -eq 0 ]]; then
        log_warning "Validierung abgeschlossen mit $WARNINGS Warnungen"
        exit 0
    else
        log_error "Validierung fehlgeschlagen mit $ERRORS Fehlern und $WARNINGS Warnungen"
        exit 1
    fi
}

# Script ausführen
main "$@"
