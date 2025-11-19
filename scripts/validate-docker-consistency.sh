#!/bin/bash
# ===================================================================
# Docker Konsistenz-Prüfer
# Validiert Dockerfiles und docker-compose-Dateien gegen docker/versions.toml
# ===================================================================

# Strikte Fehlerbehandlung: Abbruch bei Fehler, bei unset Variablen, und wenn ein Befehl in einer Pipe fehlschlägt.
set -euo pipefail

# Skript-Verzeichnis und Projekt-Wurzel
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
DOCKER_DIR="$PROJECT_ROOT/docker"
VERSIONS_TOML="$DOCKER_DIR/versions.toml"
DOCKERFILES_DIR="$PROJECT_ROOT/dockerfiles"

# Farben für die Ausgabe
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # Keine Farbe

# Globale Zähler (werden innerhalb der Funktionen aktualisiert)
_ERRORS=0
_WARNINGS=0
_CHECKS_PASSED=0

# --- Hilfsfunktionen für Ausgabe und Zählung ---

# Funktion zur Ausgabe von farbigem Text und zur Aktualisierung eines globalen Zählers
print_and_count() {
    local color=$1
    local type=$2
    local message=$3
    local counter_name=$4 # Name der globalen Zählervariable

    echo -e "${color}[${type}]${NC} $message"
    if [[ -n "$counter_name" ]]; then
        # Nameref für robuste Zählerinkrementierung verwenden (Globale Variable)
        declare -g -i "$counter_name"
        eval "$counter_name=\$(( $counter_name + 1 ))"
    fi
}

print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    print_and_count "$GREEN" "ERFOLG" "$1" "_CHECKS_PASSED"
}

print_warning() {
    print_and_count "$YELLOW" "WARNUNG" "$1" "_WARNINGS"
}

print_error() {
    print_and_count "$RED" "FEHLER" "$1" "_ERRORS"
}

# --- TOML Parsing Funktionen ---

# Funktion zum Extrahieren einer Version aus der [versions] Sektion der TOML-Datei
get_version() {
    local key=$1
    # grep zur schnellen Lokalisierung des Abschnitts, dann awk zum Finden des Schlüssels
    grep -A 20 -E '^\[versions\]' "$VERSIONS_TOML" | \
        awk -v k="$key" '
            /\[/ { exit } # Beim nächsten Abschnitt stoppen
            $1 == k && $2 == "=" { v=$3; gsub(/"/,"",v); print v; exit }
        ' || true
}

# Funktion zum Abrufen aller gültigen ARG-Namen aus der TOML-Datei
get_valid_args() {
    # 1. Versions-Schlüssel aus [versions] extrahieren
    awk '/^\[versions\]/,/^\[/ {if (/^[a-zA-Z].*= /) print $1}' "$VERSIONS_TOML" | grep -v "^\[" || true
    # 2. Build-Argumente aus [build-args] extrahieren (Tokens innerhalb von Anführungszeichen)
    awk '/^\[build-args\]/,/^\[/ {
        if ($0 ~ /^[[:space:]]*args[[:space:]]*=/) {
            line = $0
            # Array-Begrenzer entfernen: [ und ]
            gsub(/[\[\]]/, "", line)
            # Durch die zitierten Tokens iterieren
            while (match(line, /"[A-Za-z0-9_]+"/)) {
                token = substr(line, RSTART+1, RLENGTH-2)
                print token
                line = substr(line, RSTART+RLENGTH)
            }
        }
    }' "$VERSIONS_TOML" || true
    # 3. Service-Ports extrahieren
    awk '/^\[service-ports\]/,/^\[/ {if (/^[a-zA-Z].*= /) print $1}' "$VERSIONS_TOML" | grep -v "^\[" || true
}

# Funktion zum Abrufen der Environment-Variablen-Mappings aus der TOML-Datei
get_env_mappings() {
    awk '/^\[environment-mapping\]/,/^\[/ {
        if (/^[a-zA-Z].*= /) {
            key = $1
            value = $3
            gsub(/"/, "", value)
            print key ":" value
        }
    }' "$VERSIONS_TOML" || true
}

# Port-Wert aus [service-ports] in versions.toml abrufen
get_toml_port() {
    local service_key=$1
    grep -A 20 -E '^\[service-ports\]' "$VERSIONS_TOML" | \
        awk -v key="$service_key" '
            /\[/ { exit }
            $1 == key { print $3; exit }
        ' || true
}

# --- Validierungsfunktionen ---

# Funktion zur Validierung der Dockerfile ARGs
validate_dockerfile_args() {
    local dockerfile=$1
    local relative_path=${dockerfile#"$PROJECT_ROOT"/}

    print_info "Validiere Dockerfile: $relative_path"

    if [[ ! -f "$dockerfile" ]]; then
        print_error "Dockerfile nicht gefunden: $relative_path"
        return
    fi

    local dockerfile_args
    # Alle ARG-Deklarationen erfassen (nur Name)
    dockerfile_args=$(grep "^ARG " "$dockerfile" 2>/dev/null | sed 's/^ARG //' | sed 's/=.*//' | sort -u || true)

    local valid_args
    valid_args=$(get_valid_args | sort -u)

    local has_centralized_args=false

    # Jedes ARG im Dockerfile prüfen
    while IFS= read -r arg; do
        [[ -z "$arg" ]] && continue

        # Prüfen, ob ARG in versions.toml definiert oder ein Standard Docker ARG ist
        case "$arg" in
            # Standard Docker Build-Argumente
            BUILDPLATFORM|TARGETPLATFORM|BUILDOS|TARGETOS|BUILDARCH|TARGETARCH)
                print_success "  ✓ Standard Docker ARG: $arg"
                has_centralized_args=true
                ;;
            # Anwendungs-spezifische Argumente, die zentralisiert sein sollten
            GRADLE_VERSION|JAVA_VERSION|NODE_VERSION|NGINX_VERSION|VERSION|SPRING_PROFILES_ACTIVE|SERVICE_PATH|SERVICE_NAME|SERVICE_PORT|CLIENT_PATH|CLIENT_MODULE|CLIENT_NAME)
                if echo "$valid_args" | grep -q "^$arg$"; then
                    print_success "  ✓ Zentralisiertes ARG: $arg"
                    has_centralized_args=true
                else
                    print_warning "  ⚠ ARG $arg sollte in versions.toml definiert werden"
                fi
                ;;
            # Laufzeit-Konfigurationsargumente (lokal akzeptabel)
            APP_USER|APP_GROUP|APP_UID|APP_GID)
                print_success "  ✓ Laufzeit-Konfigurations ARG: $arg"
                ;;
            *)
                # Prüfen, ob es ein Versions-bezogenes ARG ist, das zentralisiert werden sollte
                if [[ "$arg" =~ _(VERSION|PORT)$ ]] || [[ "$arg" =~ ^(DOCKER_|SERVICE_|CLIENT_) ]]; then
                    print_warning "  ⚠ ARG $arg sollte möglicherweise in versions.toml zentralisiert werden"
                else
                    print_success "  ✓ Benutzerdefiniertes ARG: $arg"
                fi
                ;;
        esac
    done <<< "$dockerfile_args"

    if [[ "$has_centralized_args" == true ]]; then
        print_success "  ✓ Dockerfile verwendet zentralisiertes Versionsmanagement"
    else
        print_warning "  ⚠ Dockerfile sollte zentralisierte ARGs aus versions.toml verwenden"
    fi

    # Prüfen auf Standardzuweisungen bei zentralisierten ARGs (verboten)
    local centralized_args_regex='^(GRADLE_VERSION|JAVA_VERSION|NODE_VERSION|NGINX_VERSION|VERSION|SPRING_PROFILES_ACTIVE)='
    local defaulted_args
    defaulted_args=$(grep -nE "^ARG ${centralized_args_regex}" "$dockerfile" || true)

    if [[ -n "$defaulted_args" ]]; then
        print_error "  ❌ Zentralisierte ARGs dürfen keine Standardwerte in Dockerfiles haben:"
        # Prozess-Substitution verwenden, um den Verlust von $ERRORS-Updates zu vermeiden
        while IFS= read -r line; do
            print_error "    $relative_path:$line"
        done < <(echo "$defaulted_args")
    else
        print_success "  ✓ Keine Standardwerte für zentralisierte ARGs gesetzt"
    fi

    # Prüfen auf festcodierte Versionen in ARG-Standardwerten
    local hardcoded_versions
    hardcoded_versions=$(grep -nE "^ARG [A-Z0-9_]+=.*(alpine|[0-9]+\.[0-9]+)" "$dockerfile" | grep -v "APP_" || true)

    if [[ -n "$hardcoded_versions" ]]; then
        print_error "  ❌ Festcodierte Versionen in ARG-Standardwerten gefunden (sollten versions.toml verwenden):"
        # Prozess-Substitution verwenden, um den Verlust von $ERRORS-Updates zu vermeiden
        while IFS= read -r line; do
            print_error "    $relative_path:$line"
        done < <(echo "$hardcoded_versions")
    else
        print_success "  ✓ Keine festcodierten Versionsliterale in ARG-Standardwerten"
    fi
}

# Funktion zur Validierung von docker-compose Versionsreferenzen
validate_compose_versions() {
    local compose_file=$1
    local relative_path=${compose_file#"$PROJECT_ROOT"/}

    print_info "Validiere Docker Compose Datei: $relative_path"

    if [[ ! -f "$compose_file" ]]; then
        print_error "Compose-Datei nicht gefunden: $relative_path"
        return
    fi

    local env_mappings
    env_mappings=$(get_env_mappings)

    # 0) Fehler bei leeren ARG-Werten für kritische Build-Argumente
    local blank_args
    blank_args=$(grep -nE '^[[:space:]]*(GRADLE_VERSION|JAVA_VERSION|NODE_VERSION|NGINX_VERSION|VERSION|SPRING_PROFILES_ACTIVE):[[:space:]]*$' "$compose_file" || true)
    if [[ -n "$blank_args" ]]; then
        print_error "  ❌ Leere Build-Argumente erkannt (müssen auf zentralisierte DOCKER_* Variablen verweisen):"
        while IFS= read -r line; do
            print_error "    $relative_path:$line"
        done < <(echo "$blank_args")
    else
        print_success "  ✓ Keine leeren kritischen Build-Argumente in der Compose-Datei"
    fi

    # Sicherstellen, dass kritische Build-Argumente auf zentralisierte DOCKER_* Variablen verweisen
    local critical_vars=(GRADLE_VERSION JAVA_VERSION NODE_VERSION NGINX_VERSION VERSION SPRING_PROFILES_ACTIVE)
    for v in "${critical_vars[@]}"; do
        # Verwenden von awk, um Mapping-Einträge innerhalb von build->args zuverlässig zu finden
        local mapping_lines
        mapping_lines=$(awk -v var="$v" '
            { line[NR] = $0 }
            END {
                for (i = 1; i <= NR; i++) {
                    if (line[i] ~ "^[[:space:]]*" var ":[[:space:]]*.+$") {
                        found = 0
                        # Bis zu 12 Zeilen zurückblicken, um nach "args:" zu suchen
                        for (j = i - 1; j >= 1 && j >= i - 12; j--) {
                            if (line[j] ~ /^[[:space:]]*args:[[:space:]]*$/) { found = 1; break }
                            # Suche stoppen, wenn eine übergeordnete Sektion erreicht wird
                            if (line[j] ~ /^[[:space:]]*(environment|services|volumes|secrets|networks):/ ) { break }
                        }
                        if (found) { printf("%d:%s\n", i, line[i]) }
                    }
                }
            }' "$compose_file" || true)

        if [[ -n "$mapping_lines" ]]; then
            while IFS= read -r line; do
                local ln
                local content
                ln=$(echo "$line" | cut -d: -f1)
                content=$(echo "$line" | cut -d: -f2- | sed 's/^[[:space:]]*//')
                # Prüfen, ob der Wert auf ${DOCKER_*} verweist
                if ! echo "$content" | grep -q "\${DOCKER_"; then
                    print_error "  ❌ $v sollte in Build-Args-Mappings auf zentralisierte DOCKER_* Variable verweisen (gefunden: $content)"
                    print_error "    $relative_path:$ln"
                fi
            done < <(echo "$mapping_lines")
        fi
    done

    # 2a) Validierung von Standard-Fallbacks in ${DOCKER_*:-fallback} gegen SSoT-Werte
    declare -A env_to_version_key
    while IFS=':' read -r toml_key env_var; do
        [[ -z "$toml_key" || -z "$env_var" ]] && continue
        # Mapping der TOML-Schlüsselnamen zu Environment-Variablennamen
        env_to_version_key[$env_var]=$(echo "$toml_key" | tr -d '\r')
    done <<< "$env_mappings"

    # Vorkommen mit expliziten Standard-Fallbacks finden
    local fallback_lines
    fallback_lines=$(grep -nE "\${DOCKER_[A-Z0-9_]+:-[^}]+" "$compose_file" || true)

    if [[ -n "$fallback_lines" ]]; then
        while IFS= read -r ln; do
            [[ -z "$ln" ]] && continue
            local num
            local text
            num=$(echo "$ln" | cut -d: -f1)
            text=$(echo "$ln" | cut -d: -f2-)

            # Variablennamen und Fallback-Wert extrahieren
            local var
            local fallback
            var=$(echo "$text" | sed -nE "s/.*\$([A-Z0-9_]+):-\([^}][^}]*\).*/\1/p")
            fallback=$(echo "$text" | sed -nE "s/.*\$[A-Z0-9_]+:-([^}][^}]*).*/\1/p")

            if [[ -z "$var" || -z "$fallback" ]]; then
                continue
            fi

            local key
            key=${env_to_version_key[$var]}
            if [[ -z "$key" ]]; then
                print_warning "  ⚠ Variable $var wird mit Fallback verwendet, ist aber nicht in [environment-mapping] gemappt. Fallback-Prüfung übersprungen."
                continue
            fi

            local expected
            expected=$(get_version "$key")
            if [[ -z "$expected" ]]; then
                print_warning "  ⚠ Kein SSoT-Wert für $var (Schlüssel: $key) in versions.toml zum Vergleich des Fallbacks gefunden"
                continue
            fi

            if [[ "$fallback" != "$expected" ]]; then
                print_error "  ❌ Veralteter Standard-Fallback für $var in ${relative_path}:${num} — gefunden '$fallback', erwartet '$expected' aus versions.toml ($key)"
            else
                print_success "  ✓ Fallback für $var stimmt mit SSoT überein ($expected)"
            fi
        done < <(echo "$fallback_lines")
    fi

    # Prüfen auf Versionsreferenzen in der Compose-Datei
    local version_refs
    version_refs=$(grep -o "\${DOCKER_[^}]*}" "$compose_file" | sort -u || true)

    if [[ -z "$version_refs" ]]; then
        print_warning "  ⚠ Keine zentralisierten Versionsreferenzen gefunden"
    else
        while IFS= read -r ref; do
            [[ -z "$ref" ]] && continue

            local var_name
            var_name=${ref#\$\{}
            var_name=${var_name%\}}
            var_name=${var_name%%:-*} # Standard-Fallback (:-value) entfernen

            local mapping_found=false
            while IFS=':' read -r toml_key env_var; do
                if [[ "$env_var" == "$var_name" ]]; then
                    mapping_found=true
                    local toml_version
                    toml_version=$(get_version "$toml_key")
                    if [[ -n "$toml_version" ]]; then
                        print_success "  ✓ Versionsreferenz $ref mappt zu $toml_key = $toml_version"
                    else
                        print_error "  ❌ TOML-Schlüssel $toml_key hat keinen Wert"
                    fi
                    break
                fi
            done <<< "$env_mappings"

            if [[ "$mapping_found" == false ]]; then
                print_warning "  ⚠ Versionsreferenz $ref hat kein Mapping in der [environment-mapping] Sektion"
            fi
        done <<< "$version_refs"
    fi

    # Prüfen auf festcodierte Image-Versionen
    local hardcoded_images
    hardcoded_images=$(grep -E "image:.*:[0-9]" "$compose_file" | grep -v "\${" || true)
    if [[ -n "$hardcoded_images" ]]; then
        print_error "  ❌ Festcodierte Image-Versionen gefunden:"
        while IFS= read -r line; do
            print_error "    $line"
        done < <(echo "$hardcoded_images")
    else
        print_success "  ✓ Keine festcodierten Image-Versionen gefunden"
    fi
}

# Funktion zur Validierung der Port-Konsistenz
# Diese Funktion benötigt keine Argumente.
# shellcheck disable=SC2120
validate_port_consistency() {

    local toml_ports
    local overall_success
    local compose_files
    local full_path
    local compose_ports_raw
    local found_port

    print_info "Validiere Port-Konsistenz..."

    # toml_ports werden durch awk und VERSIONS_TOML gefüllt
    toml_ports=$(awk "/^\[service-ports\]/,/^\[/ {
        if (/^[a-zA-Z].*= [0-9]/) {
            service = $1
            port = $3
            print service ":" port
        }
    }" "$VERSIONS_TOML" || true)

    compose_files=("docker-compose.yml" "docker-compose.services.yml" "docker-compose.clients.yml")
    overall_success=true

    for compose_file in "${compose_files[@]}"; do
        full_path="$PROJECT_ROOT/$compose_file"
        if [[ -f "$full_path" ]]; then
            # Port-Mappings aus Compose-Datei extrahieren (z.B. "8080:8080" oder "80:8080")
            compose_ports_raw=$(grep -E 'ports:[[:space:]]*$|\s*-\s*("?[0-9]+:[0-9]+"?)' "$full_path" 2>/dev/null | grep -v "ports:$" | sed -E 's/.*-\s*"?([^"]*)"?/\1/' || true)

            while IFS=':' read -r service expected_port; do
                [[ -z "$service" ]] && continue

                # Prüfen, ob der Dienstname in der Compose-Datei existiert
                if grep -qE "^[[:space:]]*${service}:" "$full_path"; then
                    found_port=false

                    # Prüfen, ob ein Port-Eintrag mit dem erwarteten internen Port übereinstimmt
                    if echo "$compose_ports_raw" | grep -q ":${expected_port}$"; then
                        found_port=true
                    fi

                    if [[ "$found_port" == true ]]; then
                        print_success "  ✓ Port-Konsistenz für $service: $expected_port"
                    else
                        print_warning "  ⚠ Port-Fehlübereinstimmung/Fehlendes Mapping für $service (erwarteter interner Port: $expected_port)"
                        overall_success=false
                    fi
                fi
            done <<< "$toml_ports"
        fi
    done

  if [[ "$overall_success" == true ]]; then
    # Nur eine allgemeine Erfolgsmeldung hinzufügen, wenn keine detaillierten
    # Warnungen aufgetreten sind, die overall_success auf false gesetzt hätten.
    # Beachten Sie, dass print_success/print_warning die globalen Zähler aktualisieren.
    print_success "  ✓ Gesamte Port-Konsistenzprüfung abgeschlossen."
  else
    # Wenn overall_success auf false gesetzt wurde, wurden bereits detaillierte Warnungen ausgegeben.
    # Hier ist keine weitere Fehlermeldung nötig.
    :
  fi
}

# Funktion zur Validierung von Build-Args Environment-Dateien
validate_build_args_files() {

  local build_args_files
  local full_path
  local invalid_lines
  local bare_docker
  local docker_keys_count

  # Überspringen, wenn der Env-less-Modus aktiv ist
  if [[ "${DOCKER_SSOT_MODE:-compat}" == "envless" ]]; then
    print_info "Env-less Modus aktiv → Überspringe build-args/*.env Validierung"
    print_success "  ✓ Übersprungen: build-args Env-Dateien sind im Env-less Modus nicht erforderlich"
    return
  fi

  print_info "Validiere Build-Args Environment-Dateien..."

  build_args_files=("global.env" "services.env" "infrastructure.env" "clients.env")

  for env_file in "${build_args_files[@]}"; do
    full_path="$DOCKER_DIR/build-args/$env_file"

    if [[ ! -f "$full_path" ]]; then
      print_error "  ❌ Build-Args-Datei fehlt: $env_file"
      continue
    fi

    print_success "  ✓ Build-Args-Datei existiert: $env_file"

    if [[ -s "$full_path" ]]; then
      print_success "  ✓ Build-Args-Datei ist nicht leer: $env_file"
    else
      print_warning "  ⚠ Build-Args-Datei ist leer: $env_file"
    fi

    # 1) Sicherstellen, dass nur gültige Zeilen vorhanden sind: Kommentare, Leerzeilen oder key=value
    invalid_lines=$(grep -n -vE "^(#|\s*$|[A-Za-z_][A-Za-z0-9_]*=)" "$full_path" || true)
    if [[ -n "$invalid_lines" ]]; then
      print_error "  ❌ Ungültige Zeilen (muss key=value oder Kommentar sein):"
      while IFS= read -r line; do
        print_error "    $env_file:$line"
      done < <(echo "$invalid_lines")
    else
      print_success "  ✓ Format OK (nur key=value/Kommentare) in $env_file"
    fi

    # 2) Keine bloßen Platzhalter wie `DOCKER_XYZ` ohne Wert
    bare_docker=$(grep -nE "^DOCKER_[A-Z0-9_]+$" "$full_path" || true)
    if [[ -n "$bare_docker" ]]; then
      print_error "  ❌ Bloße DOCKER_* Platzhalter ohne Werte gefunden:"
      while IFS= read -r line; do
        print_error "    $env_file:$line"
      done < <(echo "$bare_docker")
    else
      print_success "  ✓ Keine bloßen DOCKER_* Platzhalter in $env_file"
    fi

    # 3) Richtlinie: Nur global.env darf DOCKER_* Schlüssel enthalten
    docker_keys_count=$(grep -c -E "^DOCKER_[A-Z0-9_]+" "$full_path" || echo "0")
    if [[ "$env_file" == "global.env" ]]; then
      if [[ "$docker_keys_count" -gt 0 ]]; then
        print_success "  ✓ DOCKER_* Variablen sind nur in global.env vorhanden ($docker_keys_count gefunden)"
      else
        print_warning "  ⚠ Einige DOCKER_* Variablen erwartet in global.env (prometheus/grafana/keycloak, etc.)"
      fi
      # Erforderliche Schlüssel in global.env
      for key in GRADLE_VERSION JAVA_VERSION VERSION; do
        if grep -q "^$key=" "$full_path"; then
          print_success "  ✓ $key ist in global.env vorhanden"
        else
          print_error "  ❌ $key fehlt in global.env"
        fi
      done
    else
      if [[ "$docker_keys_count" -gt 0 ]]; then
        print_error "  ❌ DOCKER_* Variablen dürfen nicht in $env_file vorhanden sein (in global.env zentralisieren)"
      else
        print_success "  ✓ Keine zentralisierten DOCKER_* Variablen in $env_file (wie erwartet)"
      fi
    fi

    # 4) DOCKER_APP_VERSION in Build-Args Env-Dateien verbieten
    if grep -q '^DOCKER_APP_VERSION=' "$full_path"; then
        print_error "  ❌ DOCKER_APP_VERSION sollte nicht in Build-Args-Dateien definiert werden (wird zur Laufzeit von VERSION gemappt)"
    fi
  done
}

# Validierung der Wertgleichheit zwischen versions.toml und Build-Args Env-Dateien
validate_env_value_equality() {

  local has_diff

  # Überspringen, wenn der Env-less-Modus aktiv ist
  if [[ "${DOCKER_SSOT_MODE:-compat}" == "envless" ]]; then
    print_info "Env-less Modus aktiv → Überspringe TOML↔Env-Wert-Gleichheitsprüfung"
    print_success "  ✓ Übersprungen: Werte stammen zur Laufzeit direkt aus versions.toml"
    return
  fi

  print_info "Validiere Wertgleichheit zwischen versions.toml und Build-Args Envs..."

  has_diff=false

  # Interne Hilfsfunktion zum Vergleichen eines TOML-Schlüssels mit einem Env-Schlüssel in einer bestimmten Datei
  _check_env_pair() {

    local env_file=$1
    local env_key=$2
    local toml_key=$3
    local path="$DOCKER_DIR/build-args/$env_file"
    local expected actual port_lookup

    if [[ ! -f "$path" ]]; then
      print_error "  ❌ Fehlende Env-Datei: $env_file"
      has_diff=true
      return
    fi

    # Erwarteter Wert aus TOML (zuerst [versions] versuchen)
    expected=$(get_version "$toml_key")
    # Fallback: [service-ports] versuchen, falls nicht in [versions] gefunden
    if [[ -z "$expected" ]]; then
      port_lookup=$(get_toml_port "$toml_key")
      if [[ -n "$port_lookup" ]]; then
          expected="$port_lookup"
      fi
    fi

    # Aktueller Wert aus Env-Datei
    actual=$(grep -E "^${env_key}=" "$path" | head -1 | sed "s/^[^=]*=//")

    if [[ -z "$expected" ]]; then
      print_warning "  ⚠ TOML-Schlüssel '$toml_key' lieferte keinen Wert (versions.toml prüfen)"
      return
    fi
    if [[ -z "$actual" ]]; then
      print_error "  ❌ $env_file fehlt $env_key (erwartet $expected)"
      has_diff=true
      return
    fi

    # Prüfen auf Nichtübereinstimmung
    if [[ "$expected" != "$actual" ]]; then
      # Versuch zur Normalisierung für den Vergleich (Anführungszeichen entfernen, falls vorhanden)
      local clean_expected
      clean_expected=$(echo "$expected" | sed 's/^"//;s/"$//')
      local clean_actual
      clean_actual=$(echo "$actual" | sed 's/^"//;s/"$//')

      if [[ "$clean_expected" != "$clean_actual" ]]; then
       print_error "  ❌ Nichtübereinstimmung in $env_file: $env_key=$actual != $toml_key=$expected"
       has_diff=true
      else
       # Nur ein Anführungszeichen-Unterschied (z.B. actual="1.0" expected=1.0)
       print_warning "  ⚠ Anführungszeichen-Unterschied in $env_file: $env_key=$actual vs $toml_key=$expected (Wert stimmt überein)"
      fi
    else
      print_success "  ✓ $env_file: $env_key stimmt mit $toml_key überein ($expected)"
    fi
  }

  # Global.env Mappings
  _check_env_pair "global.env" "GRADLE_VERSION" "gradle"
  _check_env_pair "global.env" "JAVA_VERSION" "java"
  _check_env_pair "global.env" "VERSION" "app-version"
  _check_env_pair "global.env" "PROMETHEUS_IMAGE_TAG" "prometheus"
  _check_env_pair "global.env" "GRAFANA_IMAGE_TAG" "grafana"
  _check_env_pair "global.env" "KEYCLOAK_IMAGE_TAG" "keycloak"

  # Clients.env Mappings
  _check_env_pair "clients.env" "NODE_VERSION" "node"
  _check_env_pair "clients.env" "NGINX_VERSION" "nginx"

  if [[ "$has_diff" == false ]]; then
    print_success "Umwelt-Dateien sind vollständig mit versions.toml synchronisiert"
  fi
}

# Nach frei schwebenden Versions-Strings außerhalb kontrollierter Dateien scannen
scan_free_floating_versions() {

  local version_values
  local found_any=false
  local search_paths
  local hits=""
  local generic_hits

  print_info "Scanne nach frei schwebenden Versions-Literalen außerhalb von SSoT-verwalteten Dateien..."

  # Versionswerte aus [versions] sammeln (muss einen Punkt oder Bindestrich enthalten, um ein Versions-String zu sein)
  version_values=$(awk '
    /^\[versions\]/ { in_section=1; next }
    /^\[/ { if (in_section) exit; in_section=0 }
    in_section && $2 == "=" { v=$3; gsub(/"/,"",v); if (v ~ /[\.-]/) print v }
  ' "$VERSIONS_TOML" || true)

  # Alle Dateien finden, die NICHT auf der Ausschlussliste stehen (Endet mit Null-Byte)
  search_paths=$(
    find "$PROJECT_ROOT" -type f \
      -not -path "*/.git/*" \
      -not -path "*/build/*" \
      -not -path "*/.gradle/*" \
      -not -path "*/node_modules/*" \
      -not -path "*/dist/*" \
      -not -path "*/out/*" \
      -not -path "*/target/*" \
      -not -path "$PROJECT_ROOT/README.md" \
      -not -path "$PROJECT_ROOT/docker/versions.toml" \
      -not -path "$PROJECT_ROOT/docker/build-args/*" \
      -not -name "docker-compose*.yml" \
      -not -name "docker-compose*.yml.optimized" \
      -not -path "$PROJECT_ROOT/scripts/*" \
      -not -name "*.sh" \
      -print0
  )

  # 1. Nach exakten, kontrollierten Versions-Strings suchen
  while IFS= read -r val; do
    [[ -z "$val" ]] && continue
    # Portable Suche nach exaktem Wert
    while IFS= read -r -d '' f; do
      grep -nF -- "$val" "$f" 2>/dev/null || true
    done < <(echo "$search_paths" | tr '\0' '\n') | tr '\n' '\0' | while IFS= read -r -d '' line; do
      hits+="${line}\n"
    done

    if [[ -n "$hits" ]]; then
      found_any=true
      print_warning "  ⚠ Vorkommen des Versions-Literals '$val' außerhalb kontrollierter Dateien entdeckt:"
      while IFS= read -r line; do
        print_warning "    $line"
      done < <(echo -e "$hits" | sed 's/\n$//')
    fi
  done <<< "$version_values"

  # 2. Generischer Muster-Scan nach verdächtigen Literalen
  # Suche nach gängigen Versions-Mustern (X.Y.Z, X.Y)
  generic_hits=$(\
    echo "$search_paths" | tr '\0' '\n' | xargs -r grep -nE -- "(^|[^0-9])([0-9]+\.[0-9]+\.[0-9]+([a-zA-Z0-9._-]+)?|([0-9]+\.[0-9]+))" 2>/dev/null \
    | head -n 200 || true) # Ausgabe auf 200 Zeilen begrenzen

  if [[ -n "$generic_hits" ]]; then
    found_any=true
    print_warning "  ⚠ Generische Versions-ähnliche Strings gefunden (auf potenziellen Drift überprüfen):"
    while IFS= read -r line; do
        print_warning "    $line"
    done < <(echo "$generic_hits")
  fi

  if [[ "$found_any" == false ]]; then
    print_success "  ✓ Keine frei schwebenden Versions-Literale entdeckt"
  fi
}

# Funktion zur Anzeige der Validierungszusammenfassung
show_summary() {
  echo ""
  echo "==============================================="
  echo "Zusammenfassung der Docker Konsistenz-Validierung"
  echo "==============================================="
  echo -e "${GREEN}Prüfungen bestanden: $_CHECKS_PASSED${NC}"
  echo -e "${YELLOW}Warnungen: $_WARNINGS${NC}"
  echo -e "${RED}Fehler: $_ERRORS${NC}"
  echo "==============================================="

  if [[ $_ERRORS -eq 0 ]]; then
    if [[ $_WARNINGS -eq 0 ]]; then
      print_info "Alle Konsistenzprüfungen bestanden! ✨"
      return 0
    else
      print_warning "Validierung mit Warnungen abgeschlossen. Bitte beheben Sie diese für optimale Konsistenz."
      return 0
    fi
  else
    print_error "Validierung fehlgeschlagen mit $_ERRORS Fehlern. Bitte beheben Sie diese, bevor Sie fortfahren."
    return 1
  fi
}

# Funktion zur Anzeige der Hilfe
show_help() {
  echo "Docker Konsistenz-Prüfer"
  echo ""
  echo "Verwendung: $0 [BEFEHL]"
  echo ""
  echo "Befehle:"
  echo "  all              Führt alle Validierungsprüfungen aus (Standard)"
  echo "  dockerfiles      Validiert Dockerfile ARG-Deklarationen"
  echo "  compose          Validiert docker-compose Versionsreferenzen"
  echo "  ports            Validiert Port-Konsistenz"
  echo "  build-args       Validiert build-args Environment-Dateien und Wertgleichheit"
  echo "  scan-drift       Scannt das Repo nach frei schwebenden Versions-Literalen"
  echo ""
  echo "Beispiele:"
  echo "  $0               # Alle Validierungen ausführen"
  echo "  $0 dockerfiles   # Nur Dockerfiles validieren"
}

# Hauptausführung
main() {
  local command=${1:-all}

  # Prüfen, ob versions.toml existiert
  if [[ ! -f "$VERSIONS_TOML" ]]; then
    print_error "Versions-Datei nicht gefunden: $VERSIONS_TOML"
    show_summary
    exit 1
  fi

  print_info "Docker Konsistenz-Validierung - Startet..."
  print_info "Versions-Datei: $VERSIONS_TOML"
  echo ""

  case $command in
    "all")
      # Dockerfiles validieren
      find "$DOCKERFILES_DIR" -name "Dockerfile" -type f -print0 | while IFS= read -r -d '' dockerfile; do
        validate_dockerfile_args "$dockerfile"
        echo ""
      done

      # docker-compose-Dateien validieren
      find "$PROJECT_ROOT" -maxdepth 1 -name "docker-compose*.yml*" -type f -print0 | while IFS= read -r -d '' compose_file; do
        validate_compose_versions "$compose_file"
        echo ""
      done

        # Port-Konsistenz validieren
        validate_port_consistency
        echo ""

        # Build-Args-Dateien validieren
        validate_build_args_files
        echo ""

        # Wertgleichheit zwischen TOML und Env-Dateien validieren
        validate_env_value_equality
        echo ""

        # Repository nach frei schwebenden Versions-Literalen scannen
        scan_free_floating_versions
        ;;
      "dockerfiles")
        find "$DOCKERFILES_DIR" -name "Dockerfile" -type f -print0 | while IFS= read -r -d '' dockerfile; do
          validate_dockerfile_args "$dockerfile"
          echo ""
        done
        ;;
      "compose")
        find "$PROJECT_ROOT" -maxdepth 1 -name "docker-compose*.yml*" -type f -print0 | while IFS= read -r -d '' compose_file; do
          validate_compose_versions "$compose_file"
          echo ""
        done
        ;;
      "ports")
        validate_port_consistency
        ;;
      "build-args")
        validate_build_args_files
        validate_env_value_equality
        ;;
      "scan-drift")
        scan_free_floating_versions
        ;;
      "-h"|"--help"|"help")
        show_help
        exit 0
        ;;
    *)
      print_error "Unbekannter Befehl: $command"
      show_help
      exit 1
      ;;
  esac

  show_summary
}

# Hauptfunktion mit allen Argumenten ausführen
main "$@"
