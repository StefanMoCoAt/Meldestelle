# Zentrale Konfigurationsverwaltung - Single Source of Truth

> **Version:** 4.0.0
> **Datum:** 15. September 2025
> **Status:** ✅ Produktiv - Eliminiert 38+ Port-Redundanzen und 72+ Spring-Profile-Duplikate

## 🎯 Überblick

Das **zentrale Konfigurationssystem** eliminiert Redundanzen über das gesamte Meldestelle-Projekt und stellt sicher, dass alle Konfigurationswerte aus einer **einzigen Quelle der Wahrheit** stammen.

### Vor der Zentralisierung (Problem)

```
Port 8082 war in 38+ Dateien dupliziert:
├── gradle.properties
├── docker-compose.services.yml
├── dockerfiles/services/ping-service/Dockerfile
├── scripts/test/integration-test.sh
├── config/monitoring/prometheus.dev.yml
└── ... 33 weitere Dateien!
```

### Nach der Zentralisierung (Lösung)

```
Port 8082 einmalig in config/central.toml definiert:
├── config/central.toml              [SINGLE SOURCE OF TRUTH]
└── scripts/config-sync.sh sync      [Automatische Synchronisation]
    └── 38+ Dateien automatisch aktualisiert ✓
```

## 📁 Verzeichnisstruktur

```
config/
├── central.toml              # 🎯 MASTER-Konfigurationsdatei
├── README.md                 # 📖 Diese Dokumentation
├── .env.template            # 🔧 Environment-Variables Template (Legacy)
└── monitoring/              # 📊 Monitoring-Konfigurationen
    ├── prometheus.yml
    ├── prometheus.dev.yml
    └── grafana/
```

## 🛠️ Verwendung

### Schnellstart

```bash
# 1. Aktuelle Konfiguration anzeigen
./scripts/config-sync.sh status

# 2. Alle Konfigurationen synchronisieren
./scripts/config-sync.sh sync

# 3. Konfiguration validieren
./scripts/config-sync.sh validate
```

### Port ändern (Beispiel)

```bash
# 1. central.toml bearbeiten
vim config/central.toml

[ports]
ping-service = 8092  # Geändert von 8082

# 2. Alle abhängigen Dateien aktualisieren
./scripts/config-sync.sh sync

# ✅ Ergebnis: 38+ Dateien automatisch synchronisiert!
```

### Spring Profile ändern

```bash
# 1. central.toml bearbeiten
[spring-profiles.defaults]
services = "production"  # Geändert von "docker"

# 2. Synchronisieren
./scripts/config-sync.sh sync

# ✅ Ergebnis: 72+ Profile-Referenzen automatisch aktualisiert!
```

## 📋 Konfigurationsbereiche

### 1. **Ports** - Eliminiert 38+ Redundanzen

```toml
[ports]
# Infrastructure Services
api-gateway = 8081
auth-server = 8087
monitoring-server = 8088

# Application Services
ping-service = 8082
members-service = 8083
horses-service = 8084
events-service = 8085
masterdata-service = 8086

# External Infrastructure
postgres = 5432
redis = 6379
consul = 8500
prometheus = 9090
grafana = 3000
```

**Synchronisiert folgende Dateien:**

- `gradle.properties` - Service-Port-Eigenschaften
- `docker-compose*.yml` - Port-Mappings und Environment-Variablen
- `dockerfiles/*/Dockerfile` - EXPOSE-Statements
- `scripts/test/*.sh` - Test-Endpunkt-URLs
- `config/monitoring/*.yml` - Prometheus-Targets
- Und 25+ weitere Dateien!

### 2. **Spring Profiles** - Eliminiert 72+ Duplikate

```toml
[spring-profiles]
default = "default"
development = "dev"
docker = "docker"
production = "prod"
test = "test"

[spring-profiles.defaults]
infrastructure = "default"    # Infrastructure Services
services = "docker"          # Application Services
clients = "dev"             # Client Applications
```

**Synchronisiert folgende Dateien:**

- Alle `dockerfiles/*/Dockerfile` - `SPRING_PROFILES_ACTIVE` Build-Args
- `docker-compose*.yml` - Spring-Profile Environment-Variablen
- `docker/build-args/*.env` - Build-Argument-Dateien
- Und 60+ weitere Referenzen!

### 3. **Service Discovery** - Standardisiert URLs

```toml
[services.ping-service]
name = "ping-service"
port = 8082
internal-host = "ping-service"
external-host = "localhost"
internal-url = "http://ping-service:8082"
external-url = "http://localhost:8082"
health-endpoint = "/actuator/health/readiness"
metrics-endpoint = "/actuator/prometheus"
info-endpoint = "/actuator/info"
```

## 🚀 Scripts und Automatisierung

### `scripts/config-sync.sh` - Haupttool

```bash
# Alle Konfigurationen synchronisieren
./scripts/config-sync.sh sync

# Nur bestimmte Bereiche synchronisieren
./scripts/config-sync.sh gradle       # gradle.properties
./scripts/config-sync.sh compose      # Docker Compose files
./scripts/config-sync.sh env          # Environment files
./scripts/config-sync.sh docker-args  # Docker build arguments
./scripts/config-sync.sh monitoring   # Prometheus/Grafana config
./scripts/config-sync.sh tests        # Test scripts

# Status und Validierung
./scripts/config-sync.sh status       # Aktuelle Konfiguration anzeigen
./scripts/config-sync.sh validate     # TOML-Syntax validieren

# Hilfe
./scripts/config-sync.sh --help
```

## 🎯 Best Practices

### ✅ DO (Empfohlen)

```bash
# Vor Änderungen Status prüfen
./scripts/config-sync.sh status

# Nach Änderungen validieren
./scripts/config-sync.sh validate

# Regelmäßig synchronisieren
./scripts/config-sync.sh sync

# Backups vor wichtigen Änderungen
cp config/central.toml config/central.toml.backup
```

### ❌ DON'T (Vermeiden)

```bash
# ❌ Niemals direkte Datei-Bearbeitung
vim docker-compose.yml              # Änderungen gehen verloren!
vim gradle.properties              # Wird überschrieben!

# ✅ Stattdessen zentrale Konfiguration verwenden
vim config/central.toml
./scripts/config-sync.sh sync
```

## 🔍 Debugging und Troubleshooting

### Häufige Probleme

#### Problem: Synchronisation schlägt fehl

```bash
# Lösung: Validierung prüfen
./scripts/config-sync.sh validate

# TOML-Syntax-Fehler beheben
vim config/central.toml
```

#### Problem: Inkonsistente Konfiguration

```bash
# Lösung: Status prüfen und re-synchronisieren
./scripts/config-sync.sh status
./scripts/config-sync.sh sync
```

#### Problem: Backup wiederherstellen

```bash
# Backups anzeigen
ls -la *.bak.*

# Wiederherstellen
cp gradle.properties.bak.20250915_103927 gradle.properties
```

### Validierung

```bash
# Umfassende Validierung
./scripts/config-sync.sh validate

# Prüft:
# ✓ TOML-Syntax
# ✓ Duplicate Sections
# ✓ Port-Konflikte
# ✓ Ungültige Werte
```

## 🚀 Migration und Integration

Die zentrale Konfigurationsverwaltung ist **rückwärtskompatibel** und kann schrittweise eingeführt werden:

1. **config/central.toml** erstellen ✅
2. **scripts/config-sync.sh** ausführen ✅
3. **Backups prüfen** und validieren ✅
4. **Entwickler-Workflow** anpassen ✅

**🎉 Mit der zentralen Konfigurationsverwaltung haben Sie einen wartungsfreundlichen, skalierbaren und fehlerresistenten Ansatz für die Verwaltung aller Konfigurationswerte in Ihrem Meldestelle-Projekt!**
