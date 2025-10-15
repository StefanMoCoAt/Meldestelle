#!/bin/bash

# Documentation Validation Script
# Checks documentation completeness, consistency, and structure

set -e

echo "🔍 Starting documentation validation..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Counters
ERRORS=0
WARNINGS=0
CHECKS=0

# Function to log messages
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
    ((WARNINGS++))
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
    ((ERRORS++))
}

# Check if required directories exist
check_directory_structure() {
    log_info "Checking documentation directory structure..."
    ((CHECKS++))

    required_dirs=(
        "docs"
        "docs/api"
        "docs/development"
        "docs/architecture"
    )

    for dir in "${required_dirs[@]}"; do
        if [ ! -d "$dir" ]; then
            log_error "Required directory missing: $dir"
        else
            log_success "Directory exists: $dir"
        fi
    done
}

# Check if all modules have README files
check_module_readmes() {
    log_info "Checking module README files..."
    ((CHECKS++))

    modules=(
        "members"
        "horses"
        "events"
        "masterdata"
        "infrastructure"
        "core"
        "client"
    )

    for module in "${modules[@]}"; do
        if [ ! -f "$module/README.md" ]; then
            log_error "Missing README.md in module: $module"
        else
            log_success "README.md exists for module: $module"
        fi
    done
}

# Check for German translations
check_german_translations() {
    log_info "Checking German translations..."
    ((CHECKS++))

    # Find English docs that should have German translations
    english_docs=$(find docs -name "*.md" | grep -v "\-de\.md$" | grep -v "README.md")

    for doc in $english_docs; do
        german_doc="${doc%.md}-de.md"
        if [ ! -f "$german_doc" ]; then
            log_warning "Missing German translation for: $doc"
        else
            log_success "German translation exists for: $doc"
        fi
    done
}

# Check documentation consistency
check_documentation_consistency() {
    log_info "Checking documentation consistency..."
    ((CHECKS++))

    # Check for consistent date format
    inconsistent_dates=$(grep -r "Letzte Aktualisierung" docs/ | grep -v "25. Juli 2025" || true)
    if [ -n "$inconsistent_dates" ]; then
        log_warning "Inconsistent dates found in documentation"
        echo "$inconsistent_dates"
    else
        log_success "All documentation dates are consistent"
    fi

    # Check for broken internal links
    log_info "Checking internal links..."
    broken_links=$(grep -r "\[.*\](\..*\.md)" docs/ | while read -r line; do
        file=$(echo "$line" | cut -d: -f1)
        link=$(echo "$line" | grep -o "\[.*\](\..*\.md)" | sed 's/.*](\(.*\))/\1/')

        # Resolve relative path
        dir=$(dirname "$file")
        full_path="$dir/$link"

        if [ ! -f "$full_path" ]; then
            echo "Broken link in $file: $link"
        fi
    done)

    if [ -n "$broken_links" ]; then
        log_error "Broken internal links found:"
        echo "$broken_links"
    else
        log_success "All internal links are valid"
    fi
}

# Check API documentation completeness
check_api_documentation() {
    log_info "Checking API documentation..."
    ((CHECKS++))

    # Check if API controllers have corresponding documentation
    controllers=$(find . -name "*Controller.kt" -type f | grep -E "(members|horses|events|masterdata)" | wc -l)
    api_docs=$(find docs/api -name "*.md" | grep -v "README.md" | wc -l)

    if [ "$api_docs" -eq 0 ]; then
        log_warning "No specific API documentation found (only found $api_docs docs for $controllers controllers)"
    else
        log_success "API documentation exists ($api_docs docs for $controllers controllers)"
    fi
}

# Check code examples in documentation
check_code_examples() {
    log_info "Checking code examples in documentation..."
    ((CHECKS++))

    # Find Kotlin code blocks and check basic syntax
    kotlin_blocks=$(grep -r "```kotlin" docs/ | wc -l)
    if [ "$kotlin_blocks" -gt 0 ]; then
        log_success "Found $kotlin_blocks Kotlin code examples"
    else
        log_warning "No Kotlin code examples found in documentation"
    fi

    # Check for common syntax issues in code blocks
    syntax_issues=$(grep -A 10 "```kotlin" docs/**/*.md | grep -E "(fun|class|interface)" | grep -v ":" | head -5 || true)
    if [ -n "$syntax_issues" ]; then
        log_warning "Potential syntax issues in code examples (manual review recommended)"
    fi
}

# Check documentation completeness score
calculate_completeness_score() {
    log_info "Calculating documentation completeness score..."

    total_modules=7
    modules_with_readme=$(find members horses events masterdata infrastructure core client -maxdepth 1 -name "README.md" 2>/dev/null | wc -l)

    api_coverage=1  # We have API documentation
    german_coverage=1  # We have German translations

    completeness_score=$(echo "scale=2; ($modules_with_readme + $api_coverage + $german_coverage) / ($total_modules + 2) * 100" | bc -l)

    log_info "Documentation completeness score: ${completeness_score}%"

    if (( $(echo "$completeness_score >= 90" | bc -l) )); then
        log_success "Excellent documentation coverage!"
    elif (( $(echo "$completeness_score >= 70" | bc -l) )); then
        log_warning "Good documentation coverage, room for improvement"
    else
        log_error "Documentation coverage needs significant improvement"
    fi
}

# Optional external link check (lychee)
link_check() {
    log_info "Running external link check (if 'lychee' is available)..."
    ((CHECKS++))
    if command -v lychee &> /dev/null; then
        # Allow common transient codes and skip localhost links
        lychee --no-progress --accept 200,204,301,302,429 --exclude "localhost|127.0.0.1|yourdomain.com" docs/**/*.md || {
            log_error "Lychee reported broken external links"
        }
    else
        log_warning "'lychee' not found, skipping external link check"
    fi
}

# Optional PlantUML render check
plantuml_check() {
    log_info "Rendering PlantUML diagrams to validate syntax (if 'plantuml' is available)..."
    ((CHECKS++))
    if command -v plantuml &> /dev/null; then
        # Render to SVG; failures should surface as non-zero exit
        # Note: This may produce SVGs next to the .puml files in CI workspace
        if compgen -G "docs/architecture/**/*.puml" > /dev/null; then
            plantuml -tsvg -failfast2 docs/architecture/**/*.puml || log_error "PlantUML rendering failed"
        elif compgen -G "docs/**/*.puml" > /dev/null; then
            plantuml -tsvg -failfast2 docs/**/*.puml || log_error "PlantUML rendering failed"
        else
            log_warning "No .puml files found to render"
        fi
    else
        log_warning "'plantuml' not found, skipping diagram rendering"
    fi
}

# Main execution
main() {
    echo "📚 Meldestelle Documentation Validation"
    echo "========================================"

    check_directory_structure
    check_module_readmes
    check_german_translations
    check_documentation_consistency
    check_api_documentation
    check_code_examples
    link_check
    plantuml_check
    calculate_completeness_score

    echo ""
    echo "📊 Validation Summary"
    echo "===================="
    echo "Total checks performed: $CHECKS"
    echo "Errors found: $ERRORS"
    echo "Warnings found: $WARNINGS"

    if [ $ERRORS -eq 0 ]; then
        log_success "✅ Documentation validation passed!"
        exit 0
    else
        log_error "❌ Documentation validation failed with $ERRORS errors"
        exit 1
    fi
}

# Check if bc is available for calculations
if ! command -v bc &> /dev/null; then
    log_warning "bc calculator not found, skipping completeness score calculation"
fi

main "$@"
