# Meldestelle - Zentrale Konfigurationsverwaltung

## Übersicht

Dieses Verzeichnis enthält die **SINGLE SOURCE OF TRUTH** für alle Umgebungsvariablen und Konfigurationsdateien im Meldestelle-Projekt. Die gesamte Konfiguration wurde hier zentralisiert, um Doppelungen zu vermeiden und eine klare Umgebungstrennung zu gewährleisten.

## Struktur

```
config/
├── .env.template       # Vorlage mit allen verfügbaren Variablen
├── .env.dev           # Entwicklungsumgebung
├── .env.prod          # Produktionsumgebung
├── .env.staging       # Staging-Umgebung
├── .env.test          # Testumgebung
├── application.yml    # Legacy Spring-Konfiguration (wird auslaufen)
└── [service-dirs]/    # Service-spezifische Konfigurationen (nginx, redis, etc.)
```

## Umgebungsdateien

### `.env.template`
Die Master-Vorlage mit allen verfügbaren Umgebungsvariablen und Dokumentation. Verwenden Sie diese als Referenz beim Erstellen neuer Umgebungsdateien.

### `.env.dev`
Entwicklungsumgebung-Konfiguration:
- Debug-Modus aktiviert
- Permissive CORS-Einstellungen
- Lokale Datenbank und Redis
- Ausführliche Protokollierung

### `.env.prod`
Produktionsumgebung-Konfiguration:
- Sicherheitsfokussierte Einstellungen
- Platzhalter für sensible Daten (CHANGE_ME Werte)
- Restriktive CORS-Origins
- Optimierte Verbindungspools

### `.env.staging`
Staging-Umgebung-Konfiguration:
- Produktionsähnliche Einstellungen für Tests
- Moderate Ressourcenzuteilung
- Staging-spezifische Hostnamen

### `.env.test`
Testumgebung-Konfiguration:
- Optimiert für automatisierte Tests
- Alternative Ports zur Konfliktvermeidung
- Minimaler Ressourcenverbrauch
- Service Discovery deaktiviert

## Verwendung

### 1. Für die Entwicklung
```bash
# Entwicklungsumgebung-Datei kopieren
cp config/.env.dev .env

# Oder einen Symlink erstellen
ln -sf config/.env.dev .env
```

### 2. Für die Produktion
```bash
# Produktions-Vorlage kopieren und anpassen
cp config/.env.prod .env.prod

# Alle CHANGE_ME Werte mit sicheren Zugangsdaten bearbeiten
vim .env.prod

# Produktions-Datei verwenden
ln -sf .env.prod .env
```

### 3. Für Tests
```bash
# Testumgebung verwenden
ln -sf config/.env.test .env
```

## Struktur der Umgebungsvariablen

Die Konfiguration ist in 12 logische Abschnitte unterteilt:

1. **Anwendungskonfiguration** - Grundlegende App-Einstellungen
2. **Port-Verwaltung** - Alle Service-Ports an einem Ort
3. **Datenbank-Konfiguration** - PostgreSQL-Einstellungen
4. **Redis-Konfiguration** - Cache und Event Store
5. **Sicherheitskonfiguration** - JWT, API-Schlüssel
6. **Keycloak-Konfiguration** - Authentifizierungsserver
7. **Service Discovery** - Consul-Einstellungen
8. **Messaging** - Kafka-Konfiguration
9. **Überwachung** - Grafana, Prometheus
10. **Protokollierungskonfiguration** - Log-Level und Formate
11. **CORS und Rate Limiting** - Web-Sicherheit
12. **Spring Profile und Gateway** - Framework-Einstellungen

## Sicherheitsrichtlinien

### Entwicklung
- Standard-Passwörter für lokale Entwicklung verwenden
- Debug-Modus aktiviert lassen
- Permissive CORS-Einstellungen verwenden

### Produktion
- **NIEMALS** Produktions-`.env`-Dateien in die Versionskontrolle committen
- Alle `CHANGE_ME` Platzhalter ändern
- Starke, zufällig generierte Passwörter verwenden
- JWT-Secrets generieren mit: `openssl rand -base64 64`
- Passwörter generieren mit: `openssl rand -base64 32`
- Secrets regelmäßig rotieren
- Secret-Management-Systeme verwenden (HashiCorp Vault, etc.)

## Migration von der alten Struktur

Die alten Konfigurationsdateien wurden konsolidiert:

### Entfernte Dateien
- `/project-root/.env` → `config/.env.dev`
- `/project-root/.env.template` → `config/.env.template`
- `/project-root/.env.prod.example` → `config/.env.prod`
- `config/application*.properties` - Entfernt und durch .env-Dateien ersetzt

### Legacy-Dateien (werden auslaufen)
- `config/application.yml` - Wird durch .env-Dateien ersetzt

## Referenz der Umgebungsvariablen

### Wichtige Variablen nach Umgebung

| Variable | Dev | Staging | Prod | Test |
|----------|-----|---------|------|------|
| `DEBUG_MODE` | true | false | false | true |
| `LOGGING_LEVEL` | DEBUG | INFO | INFO | DEBUG |
| `CORS_ALLOWED_ORIGINS` | * | staging domains | prod domains | * |
| `DB_AUTO_MIGRATE` | true | true | false | true |
| `CONSUL_ENABLED` | true | true | true | false |

### Port-Zuteilung

| Service | Port |
|---------|------|
| Gateway | 8081 |
| Gateway Admin | 8080 |
| Ping Service | 8082 |
| Members Service | 8083 |
| Horses Service | 8084 |
| Events Service | 8085 |
| Masterdata Service | 8086 |
| Auth Service | 8087 |

**Testumgebung:** Alle Ports +1000 (z.B. Gateway: 9081)

## Best Practices

1. **Immer die Vorlage verwenden** als Ausgangspunkt für neue Umgebungen
2. **Benutzerdefinierte Variablen dokumentieren** in Kommentaren
3. **Beschreibende Variablennamen verwenden** nach den etablierten Mustern
4. **Verwandte Variablen gruppieren** in logischen Abschnitten
5. **Konfiguration validieren** vor der Bereitstellung
6. **Konfigurationsabweichungen überwachen** zwischen Umgebungen

## Fehlerbehebung

### Häufige Probleme

1. **Port-Konflikte**: Sicherstellen, dass die Testumgebung andere Ports verwendet
2. **Fehlende Variablen**: Gegen `.env.template` prüfen
3. **Zugriff verweigert**: Dateiberechtigungen für `.env`-Dateien überprüfen
4. **Datenbankverbindung fehlgeschlagen**: DB-Zugangsdaten und Hostname prüfen

### Validierungsskript

```bash
# TODO: Validierungsskript erstellen
./scripts/validate-config.sh config/.env.prod
```

## Zukünftige Verbesserungen

- [ ] Konfigurationsvalidierungsskripte
- [ ] Automatische Secret-Generierung
- [ ] Umgebungsspezifische docker-compose-Dateien
- [ ] Erkennung von Konfigurationsabweichungen
- [ ] Integration von Secret-Management
