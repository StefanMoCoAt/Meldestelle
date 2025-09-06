# Meldestelle - Produktionsumgebung Setup

## Übersicht

Dieses Dokument beschreibt die Einrichtung und den Betrieb der Meldestelle-Anwendung in einer Produktionsumgebung mit Docker Compose. Die Produktionskonfiguration bietet erweiterte Sicherheitsfeatures, TLS-Verschlüsselung und optimierte Performance-Einstellungen.

## 🔒 Sicherheitsfeatures

### Implementierte Sicherheitsmaßnahmen

1. **Starke Authentifizierung**
   - Redis mit Passwort-Authentifizierung
   - PostgreSQL mit SCRAM-SHA-256 Authentifizierung
   - Kafka mit SASL/SSL Sicherheit
   - Zookeeper mit SASL Authentifizierung

2. **TLS/SSL Verschlüsselung**
   - HTTPS-only für alle Web-Services
   - TLS-Unterstützung für Redis (konfigurierbar)
   - SSL für PostgreSQL
   - SSL/TLS für Kafka Inter-Broker Kommunikation

3. **Netzwerksicherheit**
   - Interne Service-Kommunikation ohne Host-Port-Exposition
   - Nginx Reverse Proxy als einziger öffentlicher Zugang
   - Isoliertes Docker-Netzwerk mit definiertem Subnetz

4. **Container-Sicherheit**
   - Non-root User für alle Services
   - Resource-Limits für alle Container
   - Read-only Mounts für Konfigurationsdateien
   - Restart-Policies für Hochverfügbarkeit

## 📋 Voraussetzungen

### System-Anforderungen

- **Betriebssystem**: Linux (Ubuntu 20.04+ empfohlen)
- **Docker**: Version 20.10+
- **Docker Compose**: Version 2.0+
- **RAM**: Mindestens 8GB (16GB empfohlen)
- **CPU**: Mindestens 4 Cores
- **Speicher**: Mindestens 50GB freier Speicherplatz

### Netzwerk-Anforderungen

- **Ports**: 80, 443 (HTTP/HTTPS)
- **Domain**: Gültige Domain-Namen für SSL-Zertifikate
- **DNS**: Korrekte DNS-Konfiguration für alle Subdomains

## 🚀 Installation und Setup

### 1. Repository klonen

```bash
git clone <repository-url>
cd Meldestelle
```

### 2. Produktionsumgebung konfigurieren

```bash
# Kopieren Sie die Produktions-Umgebungsvariablen aus dem config Verzeichnis
cp config/.env.prod .env.prod

# Bearbeiten Sie die Produktionskonfiguration
nano .env.prod

# Oder verwenden Sie einen Symlink für direkte Nutzung
ln -sf config/.env.prod .env
```

### 3. SSL-Zertifikate einrichten

Siehe [SSL-Zertifikat Setup Anleitung](config/ssl/README-de.md) für detaillierte Anweisungen.

#### Schnellstart mit Let's Encrypt

```bash
# Installieren Sie Certbot
sudo apt-get update
sudo apt-get install certbot

# Generieren Sie Zertifikate
sudo certbot certonly --standalone \
  -d yourdomain.com \
  -d api.yourdomain.com \
  -d auth.yourdomain.com \
  -d monitoring.yourdomain.com

# Kopieren Sie Zertifikate
sudo cp /etc/letsencrypt/live/yourdomain.com/fullchain.pem config/ssl/nginx/server.crt
sudo cp /etc/letsencrypt/live/yourdomain.com/privkey.pem config/ssl/nginx/server.key

# Generieren Sie Diffie-Hellman Parameter
openssl dhparam -out config/ssl/nginx/dhparam.pem 2048
```

### 4. Konfigurationsdateien anpassen

#### Passwörter generieren

```bash
# Starke Passwörter generieren
openssl rand -base64 32  # Für Datenbank-Passwörter
openssl rand -base64 64  # Für JWT-Secret
openssl rand -base64 32  # Für Redis-Passwort
```

#### Wichtige Konfigurationen in .env.prod

```bash
# Datenbank (ÄNDERN SIE DIESE WERTE!)
POSTGRES_PASSWORD=IHR_STARKES_DB_PASSWORT
DB_PASSWORD=IHR_STARKES_DB_PASSWORT

# Redis (ÄNDERN SIE DIESE WERTE!)
REDIS_PASSWORD=IHR_STARKES_REDIS_PASSWORT

# JWT (ÄNDERN SIE DIESE WERTE!)
JWT_SECRET=IHR_STARKER_JWT_SECRET_MINDESTENS_256_BITS

# Keycloak (ÄNDERN SIE DIESE WERTE!)
KEYCLOAK_ADMIN=ihr_admin_username
KEYCLOAK_ADMIN_PASSWORD=IHR_STARKES_ADMIN_PASSWORT

# Domains (ÄNDERN SIE DIESE WERTE!)
KC_HOSTNAME=auth.ihredomain.com
GRAFANA_HOSTNAME=monitoring.ihredomain.com
PROMETHEUS_HOSTNAME=metrics.ihredomain.com
```

### 5. Services starten

```bash
# Produktionsumgebung starten
docker-compose -f docker-compose.prod.yml --env-file .env.prod up -d

# Status überprüfen
docker-compose -f docker-compose.prod.yml ps

# Logs überwachen
docker-compose -f docker-compose.prod.yml logs -f
```

## 🔧 Konfiguration

### Service-Übersicht

| Service | Interner Port | Externer Zugang | Beschreibung |
|---------|---------------|-----------------|--------------|
| nginx | 80, 443 | ✅ | Reverse Proxy, SSL-Terminierung |
| postgres | 5432 | ❌ | Datenbank (nur intern) |
| redis | 6379 | ❌ | Cache & Event Store (nur intern) |
| keycloak | 8443 | ❌ | Authentifizierung (über nginx) |
| kafka | 9092, 9093 | ❌ | Messaging (nur intern) |
| zookeeper | 2181 | ❌ | Kafka Koordination (nur intern) |
| prometheus | 9090 | ❌ | Metriken (über nginx) |
| grafana | 3000 | ❌ | Monitoring Dashboard (über nginx) |
| zipkin | 9411 | ❌ | Distributed Tracing (nur intern) |

### Nginx Reverse Proxy Konfiguration

Erstellen Sie Service-spezifische Konfigurationen in `config/nginx/conf.d/`:

#### Keycloak (auth.ihredomain.com)
```nginx
server {
    listen 443 ssl http2;
    server_name auth.ihredomain.com;

    ssl_certificate /etc/ssl/nginx/server.crt;
    ssl_private_key /etc/ssl/nginx/server.key;

    location / {
        proxy_pass https://keycloak:8443;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

#### Grafana (monitoring.ihredomain.com)
```nginx
server {
    listen 443 ssl http2;
    server_name monitoring.ihredomain.com;

    ssl_certificate /etc/ssl/nginx/server.crt;
    ssl_private_key /etc/ssl/nginx/server.key;

    location / {
        proxy_pass https://grafana:3000;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

## 🔍 Monitoring und Logging

### Prometheus Metriken

Zugang über: `https://metrics.ihredomain.com`

Überwachte Services:
- Anwendungsmetriken
- PostgreSQL Metriken
- Redis Metriken
- Kafka Metriken
- System-Metriken (Node Exporter)
- Container-Metriken (cAdvisor)

### Grafana Dashboards

Zugang über: `https://monitoring.ihredomain.com`

Standard-Dashboards für:
- Anwendungs-Performance
- Datenbank-Performance
- Redis-Performance
- Kafka-Metriken
- System-Übersicht

### Log-Management

```bash
# Service-Logs anzeigen
docker-compose -f docker-compose.prod.yml logs [service-name]

# Logs in Echtzeit verfolgen
docker-compose -f docker-compose.prod.yml logs -f [service-name]

# Log-Rotation konfigurieren
# Fügen Sie zu /etc/docker/daemon.json hinzu:
{
  "log-driver": "json-file",
  "log-opts": {
    "max-size": "10m",
    "max-file": "3"
  }
}
```

## 🛡️ Sicherheits-Checkliste

### Vor der Produktionsfreigabe

- [ ] Alle Standard-Passwörter geändert
- [ ] SSL-Zertifikate von vertrauenswürdiger CA installiert
- [ ] Firewall konfiguriert (nur Ports 80, 443 öffentlich)
- [ ] Backup-Strategie implementiert
- [ ] Monitoring und Alerting konfiguriert
- [ ] Log-Rotation eingerichtet
- [ ] Security-Updates installiert
- [ ] Penetration-Test durchgeführt

### Regelmäßige Sicherheitsaufgaben

- [ ] Passwörter alle 90 Tage rotieren
- [ ] SSL-Zertifikate vor Ablauf erneuern
- [ ] Security-Updates monatlich installieren
- [ ] Backup-Wiederherstellung testen
- [ ] Access-Logs regelmäßig überprüfen
- [ ] Vulnerability-Scans durchführen

## 💾 Backup und Wiederherstellung

### Automatische Backups

```bash
# Datenbank-Backup Script erstellen
cat > backup-db.sh << 'EOF'
#!/bin/bash
DATE=$(date +%Y%m%d_%H%M%S)
docker-compose -f docker-compose.prod.yml exec -T postgres \
  pg_dump -U meldestelle_prod meldestelle_prod | \
  gzip > backups/db_backup_$DATE.sql.gz
find backups/ -name "db_backup_*.sql.gz" -mtime +30 -delete
EOF

chmod +x backup-db.sh

# Cron-Job für tägliche Backups
echo "0 2 * * * /path/to/backup-db.sh" | crontab -
```

### Redis Backup

```bash
# Redis-Daten sichern
docker-compose -f docker-compose.prod.yml exec redis \
  redis-cli --rdb /data/backup.rdb

# Backup kopieren
docker cp $(docker-compose -f docker-compose.prod.yml ps -q redis):/data/backup.rdb \
  backups/redis_backup_$(date +%Y%m%d_%H%M%S).rdb
```

### Wiederherstellung

```bash
# Datenbank wiederherstellen
gunzip -c backups/db_backup_YYYYMMDD_HHMMSS.sql.gz | \
docker-compose -f docker-compose.prod.yml exec -T postgres \
  psql -U meldestelle_prod -d meldestelle_prod

# Redis wiederherstellen
docker-compose -f docker-compose.prod.yml stop redis
docker cp backups/redis_backup_YYYYMMDD_HHMMSS.rdb \
  $(docker-compose -f docker-compose.prod.yml ps -q redis):/data/dump.rdb
docker-compose -f docker-compose.prod.yml start redis
```

## 🔄 Updates und Wartung

### Rolling Updates

```bash
# Service einzeln aktualisieren
docker-compose -f docker-compose.prod.yml pull [service-name]
docker-compose -f docker-compose.prod.yml up -d --no-deps [service-name]

# Alle Services aktualisieren
docker-compose -f docker-compose.prod.yml pull
docker-compose -f docker-compose.prod.yml up -d
```

### Wartungsmodus

```bash
# Wartungsseite aktivieren
docker-compose -f docker-compose.prod.yml stop nginx
# Wartungs-Nginx Container starten (mit Wartungsseite)

# Nach Wartung: Normalen Betrieb wiederherstellen
docker-compose -f docker-compose.prod.yml start nginx
```

## 🚨 Troubleshooting

### Häufige Probleme

#### 1. SSL-Zertifikat Fehler
```bash
# Zertifikat überprüfen
openssl x509 -in config/ssl/nginx/server.crt -text -noout

# Zertifikat-Gültigkeit prüfen
openssl x509 -in config/ssl/nginx/server.crt -noout -dates
```

#### 2. Service startet nicht
```bash
# Logs überprüfen
docker-compose -f docker-compose.prod.yml logs [service-name]

# Container-Status prüfen
docker-compose -f docker-compose.prod.yml ps

# Health-Check Status
docker inspect $(docker-compose -f docker-compose.prod.yml ps -q [service-name])
```

#### 3. Datenbankverbindung fehlgeschlagen
```bash
# Datenbank-Logs prüfen
docker-compose -f docker-compose.prod.yml logs postgres

# Verbindung testen
docker-compose -f docker-compose.prod.yml exec postgres \
  psql -U meldestelle_prod -d meldestelle_prod -c "SELECT 1;"
```

#### 4. Redis-Verbindung fehlgeschlagen
```bash
# Redis-Logs prüfen
docker-compose -f docker-compose.prod.yml logs redis

# Redis-Verbindung testen
docker-compose -f docker-compose.prod.yml exec redis \
  redis-cli -a $REDIS_PASSWORD ping
```

#### 5. Container startet nicht (Out of Memory)
```bash
# Container-Ressourcenverbrauch prüfen
docker stats --no-stream

# Speicher-Limits überprüfen
docker inspect $(docker-compose -f docker-compose.prod.yml ps -q [service-name]) | grep -i memory

# System-Speicher prüfen
free -h
df -h

# Container mit mehr Speicher neu starten
docker-compose -f docker-compose.prod.yml up -d --force-recreate [service-name]
```

#### 6. Netzwerk-Verbindungsprobleme
```bash
# Docker-Netzwerk prüfen
docker network ls
docker network inspect meldestelle-network

# Service-zu-Service Verbindung testen
docker-compose -f docker-compose.prod.yml exec [service1] \
  ping [service2]

# Port-Erreichbarkeit testen
docker-compose -f docker-compose.prod.yml exec [service] \
  nc -zv [target-service] [port]

# DNS-Auflösung testen
docker-compose -f docker-compose.prod.yml exec [service] \
  nslookup [target-service]
```

#### 7. Volume-Mount Probleme
```bash
# Volume-Status prüfen
docker volume ls
docker volume inspect [volume-name]

# Berechtigungen prüfen
docker-compose -f docker-compose.prod.yml exec [service] \
  ls -la /path/to/mounted/directory

# Volume-Speicherplatz prüfen
docker system df
docker system df -v
```

#### 8. Docker-Compose Konfigurationsfehler
```bash
# Konfiguration validieren
docker-compose -f docker-compose.prod.yml config

# Syntax-Fehler finden
docker-compose -f docker-compose.prod.yml config --quiet

# Umgebungsvariablen-Substitution prüfen
docker-compose -f docker-compose.prod.yml config --resolve-image-digests
```

### Performance-Optimierung

#### Ressourcen-Monitoring
```bash
# Container-Ressourcenverbrauch
docker stats

# Detaillierte Container-Informationen
docker-compose -f docker-compose.prod.yml top
```

#### Datenbank-Optimierung
```bash
# PostgreSQL-Performance analysieren
docker-compose -f docker-compose.prod.yml exec postgres \
  psql -U meldestelle_prod -d meldestelle_prod \
  -c "SELECT * FROM pg_stat_activity;"
```

## 📞 Support und Kontakt

### Notfall-Kontakte
- **System-Administrator**: [Kontaktinformationen]
- **Entwicklungsteam**: [Kontaktinformationen]
- **Security-Team**: [Kontaktinformationen]

### Dokumentation
- [API-Dokumentation](docs/api/)
- [Architektur-Dokumentation](docs/architecture/)
- [Entwickler-Dokumentation](docs/development/)

### Monitoring-Dashboards
- **Grafana**: https://monitoring.ihredomain.com
- **Prometheus**: https://metrics.ihredomain.com
- **Keycloak Admin**: https://auth.ihredomain.com/admin

---

**⚠️ Wichtiger Hinweis**: Diese Produktionskonfiguration enthält sensible Sicherheitseinstellungen. Stellen Sie sicher, dass alle Passwörter und Geheimnisse sicher verwaltet und regelmäßig rotiert werden.
