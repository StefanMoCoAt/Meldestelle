#!/bin/bash

# create-guideline.sh - Automatisierte Guideline-Erstellung für Meldestelle Guidelines
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
TEMPLATES_DIR="$GUIDELINES_DIR/_templates"
META_DIR="$GUIDELINES_DIR/_meta"

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

# Hilfe-Funktion
show_help() {
    cat << 'EOF'
Meldestelle Guidelines Creator

USAGE:
    ./create-guideline.sh [TYPE] [NAME] [SCOPE] [OPTIONS]

ARGUMENTE:
    TYPE        Typ der Guideline (project-standard|process-guide|technology)
    NAME        Name der neuen Guideline (ohne .md Extension)
    SCOPE       Bereich/Scope der Guideline

OPTIONEN:
    --output-dir DIR    Alternatives Zielverzeichnis (Standard: entsprechend TYPE)
    --no-meta-update    Metadaten (versions.json) nicht automatisch aktualisieren
    --dry-run           Zeige nur was erstellt würde, ohne Dateien zu schreiben
    --help              Diese Hilfe anzeigen

BEISPIELE:
    ./create-guideline.sh project-standard security-standards security-practices
    ./create-guideline.sh process-guide deployment-process deployment-workflow
    ./create-guideline.sh technology kubernetes-guide kubernetes-orchestration

VERFÜGBARE TEMPLATES:
    project-standard-template.md    -> project-standards/
    process-guide-template.md       -> process-guides/
    technology-guideline-template.md -> technology-guides/

EOF
}

# Globale Variablen
TYPE=""
NAME=""
SCOPE=""
OUTPUT_DIR=""
NO_META_UPDATE=false
DRY_RUN=false

# Command-line Parameter parsen
parse_arguments() {
    if [[ $# -eq 0 ]] || [[ "$1" == "--help" ]] || [[ "$1" == "-h" ]]; then
        show_help
        exit 0
    fi

    # Erforderliche Argumente
    if [[ $# -lt 3 ]]; then
        log_error "Nicht genug Argumente. Benötigt: TYPE NAME SCOPE"
        echo "Nutze --help für Details"
        exit 1
    fi

    TYPE="$1"
    NAME="$2"
    SCOPE="$3"
    shift 3

    # Optionale Parameter
    while [[ $# -gt 0 ]]; do
        case $1 in
            --output-dir)
                OUTPUT_DIR="$2"
                shift 2
                ;;
            --no-meta-update)
                NO_META_UPDATE=true
                shift
                ;;
            --dry-run)
                DRY_RUN=true
                shift
                ;;
            *)
                log_error "Unbekannter Parameter: $1"
                echo "Nutze --help für verfügbare Optionen"
                exit 1
                ;;
        esac
    done
}

# Template-Pfad und Zielverzeichnis bestimmen
determine_paths() {
    local template_file=""
    local default_target_dir=""

    case "$TYPE" in
        "project-standard")
            template_file="$TEMPLATES_DIR/project-standard-template.md"
            default_target_dir="$GUIDELINES_DIR/project-standards"
            ;;
        "process-guide")
            template_file="$TEMPLATES_DIR/process-guide-template.md"
            default_target_dir="$GUIDELINES_DIR/process-guides"
            ;;
        "technology")
            template_file="$TEMPLATES_DIR/technology-guideline-template.md"
            default_target_dir="$GUIDELINES_DIR/technology-guides"
            ;;
        *)
            log_error "Unbekannter Guideline-Typ: $TYPE"
            echo "Verfügbare Typen: project-standard, process-guide, technology"
            exit 1
            ;;
    esac

    if [[ ! -f "$template_file" ]]; then
        log_error "Template nicht gefunden: $template_file"
        exit 1
    fi

    # Zielverzeichnis bestimmen
    if [[ -n "$OUTPUT_DIR" ]]; then
        TARGET_DIR="$OUTPUT_DIR"
    else
        TARGET_DIR="$default_target_dir"
    fi

    TARGET_FILE="$TARGET_DIR/$NAME.md"
    TEMPLATE_PATH="$template_file"
}

# Prüfe ob Ziel bereits existiert
check_target_exists() {
    if [[ -f "$TARGET_FILE" ]]; then
        log_error "Guideline existiert bereits: $TARGET_FILE"
        log_info "Lösche die existierende Datei oder wähle einen anderen Namen"
        exit 1
    fi

    # Stelle sicher, dass Zielverzeichnis existiert
    if [[ "$DRY_RUN" = false ]] && [[ ! -d "$TARGET_DIR" ]]; then
        mkdir -p "$TARGET_DIR"
        log_info "Zielverzeichnis erstellt: $TARGET_DIR"
    fi
}

# Template-Platzhalter ersetzen
process_template() {
    local current_date=$(date +%Y-%m-%d)
    local processed_content

    if [[ "$DRY_RUN" = true ]]; then
        log_info "DRY-RUN: Würde Template verarbeiten..."
        log_info "  Template: $TEMPLATE_PATH"
        log_info "  NAME: $NAME"
        log_info "  SCOPE: $SCOPE"
        log_info "  DATE: $current_date"
        log_info "  Ziel: $TARGET_FILE"
        return
    fi

    # Template lesen und Platzhalter ersetzen
    processed_content=$(cat "$TEMPLATE_PATH")
    processed_content="${processed_content//\{\{NAME\}\}/$NAME}"
    processed_content="${processed_content//\{\{SCOPE\}\}/$SCOPE}"
    processed_content="${processed_content//\{\{DATE\}\}/$current_date}"

    # Zieldatei erstellen
    echo "$processed_content" > "$TARGET_FILE"
    log_success "Neue Guideline erstellt: $TARGET_FILE"
}

# Metadaten aktualisieren
update_metadata() {
    if [[ "$NO_META_UPDATE" = true ]]; then
        log_info "Überspringe Metadaten-Update (--no-meta-update)"
        return
    fi

    if [[ "$DRY_RUN" = true ]]; then
        log_info "DRY-RUN: Würde Metadaten aktualisieren..."
        return
    fi

    local versions_file="$META_DIR/versions.json"
    if [[ ! -f "$versions_file" ]]; then
        log_warning "versions.json nicht gefunden: $versions_file"
        return
    fi

    # Relative Pfad für versions.json
    local relative_path="${TARGET_FILE#$GUIDELINES_DIR/}"
    local current_date=$(date +%Y-%m-%d)

    # Temporäre JSON-Update (einfache Implementierung)
    # In einer vollständigen Implementation würde man jq verwenden
    log_info "Metadaten-Update für $relative_path implementierung ausstehend"
    log_warning "Bitte aktualisieren Sie $versions_file manuell"
}

# Cross-Referenzen aktualisieren
update_cross_references() {
    if [[ "$DRY_RUN" = true ]]; then
        log_info "DRY-RUN: Würde Cross-Referenzen aktualisieren..."
        return
    fi

    local cross_refs_file="$META_DIR/cross-refs.json"
    if [[ ! -f "$cross_refs_file" ]]; then
        log_warning "cross-refs.json nicht gefunden: $cross_refs_file"
        return
    fi

    log_info "Cross-Referenz-Update implementierung ausstehend"
    log_warning "Bitte aktualisieren Sie $cross_refs_file manuell"
}

# Validierung der neuen Guideline
validate_new_guideline() {
    if [[ "$DRY_RUN" = true ]]; then
        log_info "DRY-RUN: Würde neue Guideline validieren..."
        return
    fi

    log_info "Validiere neue Guideline..."

    # Nutze das validate-links.sh Script falls verfügbar
    local validate_script=".junie/scripts/validate-links.sh"
    if [[ -x "$validate_script" ]]; then
        log_info "Führe Link-Validierung aus..."
        if "$validate_script" --quick; then
            log_success "Validierung erfolgreich!"
        else
            log_warning "Validierung ergab Warnungen - bitte prüfen Sie die Ausgabe"
        fi
    else
        log_warning "validate-links.sh nicht verfügbar - manuelle Validierung empfohlen"
    fi
}

# Hauptfunktion
main() {
    echo "🚀 Meldestelle Guidelines Creator"
    echo "=================================="

    parse_arguments "$@"
    determine_paths
    check_target_exists
    process_template
    update_metadata
    update_cross_references
    validate_new_guideline

    echo ""
    if [[ "$DRY_RUN" = true ]]; then
        log_info "DRY-RUN abgeschlossen - keine Dateien wurden geändert"
    else
        log_success "Guideline-Erstellung erfolgreich abgeschlossen!"
        echo ""
        echo "📋 Nächste Schritte:"
        echo "1. Bearbeiten Sie die neue Guideline: $TARGET_FILE"
        echo "2. Aktualisieren Sie die Metadaten manuell:"
        echo "   - $META_DIR/versions.json"
        echo "   - $META_DIR/cross-refs.json"
        echo "3. Führen Sie eine vollständige Validierung aus:"
        echo "   - .junie/scripts/validate-links.sh"
    fi
}

# Script ausführen
main "$@"
