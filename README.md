

## Docker Single Source of Truth (SSoT) – Lokale Nutzung & Troubleshooting

Dieser Abschnitt beschreibt den lokalen Workflow für die zentrale Docker-Versionsverwaltung (SSoT) sowie typische Fehlerbilder und deren Behebung.

### TL;DR – Zwei Betriebsmodi
- Kompatibilitätsmodus (Standard): build-args/*.env werden aus versions.toml generiert und verwendet
  - bash scripts/docker-versions-update.sh sync
  - bash scripts/generate-compose-files.sh all development
  - bash scripts/validate-docker-consistency.sh all
- Env-less Modus (Vereinfachung): Keine build-args/*.env nötig – docker-build.sh und Compose verwenden DOCKER_* direkt aus versions.toml
  - DOCKER_SSOT_MODE=envless bash scripts/docker-build.sh --versions
  - DOCKER_SSOT_MODE=envless bash scripts/generate-compose-files.sh all development
  - DOCKER_SSOT_MODE=envless bash scripts/validate-docker-consistency.sh all

Alternativ via Makefile:
- make docker-sync (nur Kompatibilitätsmodus relevant)
- make docker-compose-gen ENV=development
- make docker-validate

### Was ist die Single Source of Truth?
- docker/versions.toml enthält alle maßgeblichen Versionsangaben (Gradle, Java, Node, Nginx, Postgres, Redis, Prometheus, Grafana, Keycloak, App‑Version, Ports, Spring‑Profile).
- Env-less: docker/build-args/*.env sind optional; Variablen werden zur Laufzeit direkt aus versions.toml exportiert.
- docker-compose*.yml werden aus versions.toml generiert bzw. referenzieren ausschließlich die zentralen DOCKER_*‑Variablen.
- Dockerfiles deklarieren ARGs ohne Default‑Werte für zentralisierte Schlüssel (z. B. GRADLE_VERSION, JAVA_VERSION, NODE_VERSION, NGINX_VERSION, VERSION, SPRING_PROFILES_ACTIVE).

### Pre‑Commit Hook (optional)
Wenn installiert (make hooks-install), führt .git/hooks/pre-commit folgende Schritte aus:
- Modus „compat“ (Standard): Sync → Generate → Validate → Drift-Check
- Modus „envless“: Generate (env‑less) → Validate (env‑less) → Drift-Check

Um env‑less im Hook zu aktivieren:
- export DOCKER_SSOT_MODE=envless
- oder einmalig für den Commit: DOCKER_SSOT_MODE=envless git commit -m "..."

Bei Fehlern bricht der Commit ab mit:
[pre-commit][ERROR] SSoT validation failed. See details by running: DOCKER_SSOT_MODE=<mode> bash scripts/validate-docker-consistency.sh all

→ Führe den genannten Befehl aus, behebe die Meldungen und commite erneut.

### Häufige Fehlerursachen & Lösungen
1) Harte Image‑Tags in Compose‑Dateien
   - Beispiel: image: postgres:16-alpine oder image: redis:7-alpine
   - Lösung: Nur zentrale Variablen verwenden:
     - image: postgres:${DOCKER_POSTGRES_VERSION:-16-alpine}
     - image: redis:${DOCKER_REDIS_VERSION:-7-alpine}
     - Diese Werte kommen aus docker/versions.toml.

2) DOCKER_APP_VERSION in build‑args Env‑Dateien
   - Nicht erlaubt (wird zur Laufzeit aus VERSION gemappt).
   - Lösung: Entfernen; scripts/docker-versions-update.sh sync (compat) oder env‑less nutzen.

3) DOCKER_* Variablen in nicht‑globalen Env‑Dateien (nur compat)
   - Nur docker/build-args/global.env darf DOCKER_* enthalten.
   - Lösung: Entfernen; der Generator bereinigt dies nach dem Sync automatisch.

4) Default‑Werte für zentrale ARGs in Dockerfiles
   - Verboten: ARG JAVA_VERSION=21 (oder ähnlich)
   - Lösung: Nur ARG JAVA_VERSION ohne Default verwenden – Werte kommen über Build‑Args/Compose.

5) Veraltete Fallbacks in Compose
   - Beispiel: ${DOCKER_GRADLE_VERSION:-9.0.0} obwohl versions.toml 9.1.0 enthält.
   - Lösung: Compose‑Dateien neu generieren (scripts/generate-compose-files.sh all) und/oder Fallbacks aktualisieren.

### Nützliche Checks
- Keine harten Image‑Tags mehr:
  - grep -RInE 'image: (postgres:|redis:)' docker-compose*.yml* || true
- Überblick aktuelle Versionen (env‑less):
  - DOCKER_SSOT_MODE=envless bash scripts/docker-build.sh --versions

### Änderungen an Versionen vornehmen
- Empfehlung: Änderungen ausschließlich per Skript durchführen, z. B.:
  - bash scripts/docker-versions-update.sh update gradle 9.1.0
  - bash scripts/docker-versions-update.sh update node 22.21.0
  - bash scripts/docker-versions-update.sh update nginx 1.28.0-alpine
  - bash scripts/docker-versions-update.sh update postgres 16-alpine
  - bash scripts/docker-versions-update.sh update redis 7-alpine
- Danach immer generate + validate ausführen (env‑less) bzw. sync + generate + validate (compat).

### CI‑Schutz (GitHub Actions)
Die CI validiert die Docker‑SSoT in zwei Modi (Matrix):
- Job ssot-guard (compat):
  1) Sync der Env‑Dateien (versions.toml → docker/build-args/*.env)
  2) Generierung der Compose‑Dateien (development)
  3) Validierung (SSoT‑Policies)
  4) Drift‑Check per git diff (Zeitstempel/Kommentarmarker werden ignoriert)
- Job ssot-guard-envless (envless):
  1) Generierung der Compose‑Dateien (ohne Env‑Sync)
  2) Validierung mit DOCKER_SSOT_MODE=envless
  3) Drift‑Check per git diff

Reproduktion lokal:
- Compat:
  - bash scripts/docker-versions-update.sh sync
  - bash scripts/generate-compose-files.sh all development
  - bash scripts/validate-docker-consistency.sh all
  - git diff --name-only  # sollte leer sein (abgesehen von Zeitstempel‑Kommentaren)
- Env‑less:
  - DOCKER_SSOT_MODE=envless bash scripts/generate-compose-files.sh all development
  - DOCKER_SSOT_MODE=envless bash scripts/validate-docker-consistency.sh all
  - git diff --name-only  # sollte leer sein (abgesehen von Zeitstempel‑Kommentaren)

Hinweis: Env‑less ist die empfohlene lokale Nutzung; der compat‑Job bleibt für Rückwärtskompatibilität und Sicherheit bestehen.
