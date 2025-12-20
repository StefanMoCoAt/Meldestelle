# Docker-Troubleshooting und Best Practices

---

guideline_type: "technology"
scope: "docker-troubleshooting"
audience: ["developers", "devops", "ai-assistants"]
last_updated: "2025-09-15"
dependencies: ["docker-overview.md", "docker-architecture.md", "docker-development.md"]
related_files: ["docker-compose.yml", "scripts/validate-docker-consistency.sh", "scripts/docker-versions-update.sh"]
ai_context: "Fehlerbehebung häufiger Docker-Probleme, Debug-Kommandos und umfassende Best Practices"

---

## 🔧 Troubleshooting

### Häufige Probleme und Lösungen

#### 🚫 Port-Konflikte

```bash
# Überprüfe, welche Ports verwendet werden
netstat -tulpn | grep :8080
lsof -i :8080

# Stoppe konfligierende Services
docker-compose down
sudo systemctl stop apache2  # Falls Apache läuft
```

#### 🐌 Langsame Startup-Zeiten

```bash
# Überprüfe Container-Ressourcen
docker stats

# Health-Check Logs analysieren
docker-compose logs ping-service | grep health

# Java Startup optimieren
export JAVA_OPTS="$JAVA_OPTS -XX:TieredStopAtLevel=1 -noverify"
```

#### 💾 Disk-Space Probleme

```bash
# Docker-Cleanup
docker system prune -a --volumes
docker volume prune

# Log-Rotation für Container
docker-compose logs --tail=1000 > /dev/null  # Truncate logs
```

#### 🌐 Service Discovery Issues

```bash
# Consul Status prüfen
curl -s http://localhost:8500/v1/health/state/any | jq

# Service Registration überprüfen
curl -s http://localhost:8500/v1/catalog/services | jq

# DNS-Resolution testen
docker-compose exec api-gateway nslookup ping-service
```

### Debug-Kommandos

```bash
# Container introspection
docker-compose exec SERVICE_NAME sh
docker-compose exec postgres psql -U meldestelle -d meldestelle

# Live-Monitoring
docker-compose top
watch -n 1 'docker-compose ps'

# Memory und CPU-Usage
docker stats $(docker-compose ps -q)

# Detailed service logs
docker-compose logs -f --tail=50 SERVICE_NAME
```

## 🎯 AI-Assistenten: Troubleshooting-Schnellreferenz

### Häufige Befehle

| Problem               | Befehl                                            | Beschreibung          |
|-----------------------|---------------------------------------------------|-----------------------|
| Port belegt           | `netstat -tulpn \| grep :<port>`                  | Port-Nutzung prüfen   |
| Service startet nicht | `docker-compose logs <service>`                   | Service-Logs anzeigen |
| Container hängt       | `docker stats`                                    | Ressourcenverbrauch   |
| DNS-Probleme          | `docker-compose exec <service> nslookup <target>` | DNS-Resolution testen |
| Disk voll             | `docker system prune -a --volumes`                | Cleanup durchführen   |

### Debug-Workflows

#### Service startet nicht

1. `docker-compose ps` - Status prüfen
2. `docker-compose logs <service>` - Logs analysieren
3. `docker-compose exec <service> sh` - Container inspizieren
4. Health-Check-Endpoint testen

#### Performance-Probleme

1. `docker stats` - Ressourcenverbrauch
2. `docker-compose top` - Prozess-Übersicht
3. JVM-Parameter optimieren
4. Resource-Limits anpassen

#### Netzwerk-Probleme

1. `docker network ls` - Netzwerke auflisten
2. `docker-compose exec <service> ping <target>` - Connectivity testen
3. Consul Service-Discovery prüfen
4. DNS-Resolution validieren

## ✅ Best Practices

### 🔐 Security Best Practices

1. **Non-Root Users**: Alle Container laufen mit dedizierten Non-Root-Usern
2. **Minimal Base Images**: Alpine Linux für kleinste Angriffsfläche
3. **Secrets Management**: Externe Secret-Stores für Production
4. **Network Isolation**: Dedizierte Docker-Networks
5. **Regular Updates**: Automatische Security-Updates für Base Images

### ⚡ Performance Best Practices

1. **Multi-Stage Builds**: Minimale Runtime-Images
2. **Layer Caching**: Optimale COPY-Reihenfolge in Dockerfiles
3. **Resource Limits**: Definierte Memory und CPU-Limits
4. **Health Checks**: Proaktive Container-Health-Überwachung
5. **JVM Tuning**: Container-aware JVM-Settings

### 🧹 Wartung Best Practices

1. **Version Pinning**: Explizite Image-Versionen in Production
2. **Backup Strategies**: Automatische Volume-Backups
3. **Log Rotation**: Begrenzte Log-Datei-Größen
4. **Documentation**: Aktuelle README-Dateien pro Service
5. **Testing**: Automatisierte Container-Tests

### 🎯 Zentrale Verwaltung Best Practices

#### Single Source of Truth Prinzipien

```bash
# ✅ RICHTIG - Zentrale Version-Updates
./scripts/docker-versions-update.sh update java 22
./scripts/docker-versions-update.sh sync

# ❌ FALSCH - Manuelle Bearbeitung von Dockerfiles
vim dockerfiles/services/ping-service/Dockerfile  # Version hardcoden
```

> **🤖 AI-Assistant Hinweis:**
> Verwende immer das zentrale Versionssystem:
> - **Updates:** `./scripts/docker-versions-update.sh update <component> <version>`
> - **Validierung:** `./scripts/validate-docker-consistency.sh`
> - **Template-Updates:** `./scripts/generate-compose-files.sh`

#### Port-Verwaltung Richtlinien

1. **Immer zentrale Port-Registry verwenden**:
   ```toml
   # docker/versions.toml - Port-Definitionen
   [service-ports]
   new-service = 8089  # Nächster verfügbarer Port
   ```

2. **Port-Konflikte vor Deployment prüfen**:
   ```bash
   ./scripts/validate-docker-consistency.sh
   ```

3. **Port-Ranges einhalten**:
   - Infrastructure: 8081-8088
   - Services: 8082-8099
   - Monitoring: 9090-9099
   - Clients: 4000-4099

#### Environment-Overrides Standards

1. **Environment-spezifische Konfigurationen nutzen**:
   ```bash
   # Development
   export DOCKER_ENVIRONMENT=development

   # Production
   export DOCKER_ENVIRONMENT=production
   ```

2. **Konsistente Health-Check-Konfigurationen**:
   ```toml
   [environments.production]
   health-check-interval = "15s"
   health-check-timeout = "3s"
   health-check-retries = 3
   ```

#### Template-System Richtlinien

1. **Compose-Files aus Templates generieren**:
   ```bash
   # Automatische Generierung bevorzugen
   ./scripts/generate-compose-files.sh

   # Manuelle Bearbeitung nur bei spezifischen Anpassungen
   ```

2. **Service-Kategorien korrekt zuordnen**:
   - `services/`: Domain-Services (ping, members, horses)
   - `infrastructure/`: Platform-Services (gateway, auth, monitoring)
   - `clients/`: Frontend-Anwendungen (web-app, desktop-app)

#### Validierung und Konsistenz

1. **Regelmäßige Konsistenz-Prüfungen**:
   ```bash
   # Bei jedem Build
   ./scripts/validate-docker-consistency.sh

   # In CI/CD Pipeline integrieren
   ```

2. **Build-Args Konsistenz**:
   ```dockerfile
   # ✅ RICHTIG - Zentrale Referenz
   ARG GRADLE_VERSION
   ARG JAVA_VERSION

   # ❌ FALSCH - Hardcodierte Versionen
   ARG GRADLE_VERSION=9.2.1
   ```

#### IDE-Integration Best Practices

1. **JSON Schema für Validierung aktivieren**:
   ```json
   {
       "yaml.schemas": {
           "./docker/schemas/versions-schema.json": "docker/versions.toml"
       }
   }
   ```

2. **Automatisierte Tasks nutzen**:
   - Docker: Show Versions
   - Docker: Validate Consistency
   - Docker: Build All Services

### 🚀 Entwickler-Workflow Best Practices

#### Neuen Service hinzufügen

```bash
# 1. Port in versions.toml reservieren
echo "new-service = 8089" >> docker/versions.toml

# 2. Template-basierten Service erstellen
./scripts/generate-compose-files.sh

# 3. Dockerfile aus Template erstellen
cp dockerfiles/templates/spring-boot-service.Dockerfile \
   dockerfiles/services/new-service/Dockerfile

# 4. Build-Args und Environment synchronisieren
./scripts/docker-versions-update.sh sync

# 5. Konsistenz validieren
./scripts/validate-docker-consistency.sh
```

#### Version-Updates durchführen

```bash
# 1. Aktuelle Versionen prüfen
./scripts/docker-versions-update.sh show

# 2. Spezifische Version aktualisieren
./scripts/docker-versions-update.sh update java 22

# 3. Alle Build-Args synchronisieren
./scripts/docker-versions-update.sh sync

# 4. Services neu bauen
docker-compose build --no-cache

# 5. System-Tests durchführen
docker-compose up -d && make test
```

---

**Navigation:**
- [docker-overview](./docker-overview.md) - Grundlagen und Philosophie
- [docker-architecture](./docker-architecture.md) - Container-Services und Struktur
- [docker-development](./docker-development.md) - Entwicklungsworkflow
- [docker-production](./docker-production.md) - Production-Deployment
- [docker-monitoring](./docker-monitoring.md) - Observability
