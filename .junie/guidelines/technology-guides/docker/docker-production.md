# Docker-Production Deployment

---
guideline_type: "technology"
scope: "docker-production"
audience: ["developers", "devops", "ai-assistants"]
last_updated: "2025-09-15"
dependencies: ["docker-overview.md", "docker-architecture.md"]
related_files: ["docker-compose.yml", "config/nginx/nginx.prod.conf", "config/ssl/*"]
ai_context: "Production-Deployment, Security-Hardening, SSL/TLS-Konfiguration und Ressourcenverwaltung"
---

## 🚀 Production-Deployment

### Security Hardening

Unsere Production-Konfiguration implementiert umfassende Sicherheitsmaßnahmen:

#### 🔒 SSL/TLS Everywhere

```bash
# TLS-Zertifikate vorbereiten
mkdir -p config/ssl/{postgres,redis,keycloak,grafana,prometheus,nginx}

# Let's Encrypt Zertifikate generieren
certbot certonly --dns-route53 -d api.meldestelle.at
certbot certonly --dns-route53 -d auth.meldestelle.at
certbot certonly --dns-route53 -d monitor.meldestelle.at
```

#### 🛡️ Environment Variables

> **🤖 AI-Assistant Hinweis:**
> Alle Passwörter werden in der Produktion mit starker Verschlüsselung generiert:
> - **PostgreSQL/Redis:** `openssl rand -base64 32`
> - **Keycloak:** Separate Admin-Credentials
> - **Monitoring:** Grafana/Prometheus Admin-Access

**Erforderliche Production-Variablen:**

```bash
# Datenschutz und Sicherheit
export POSTGRES_USER=meldestelle_prod
export POSTGRES_PASSWORD=$(openssl rand -base64 32)
export POSTGRES_DB=meldestelle_prod
export REDIS_PASSWORD=$(openssl rand -base64 32)

# Keycloak Admin
export KEYCLOAK_ADMIN=admin
export KEYCLOAK_ADMIN_PASSWORD=$(openssl rand -base64 32)
export KC_DB_PASSWORD=${POSTGRES_PASSWORD}
export KC_HOSTNAME=auth.meldestelle.at

# Monitoring
export GF_SECURITY_ADMIN_USER=admin
export GF_SECURITY_ADMIN_PASSWORD=$(openssl rand -base64 32)
export GRAFANA_HOSTNAME=monitor.meldestelle.at
export PROMETHEUS_HOSTNAME=metrics.meldestelle.at

# Kafka Security
export KAFKA_BROKER_ID=1
export KAFKA_ZOOKEEPER_CONNECT=zookeeper:2181
```

#### 🌐 Reverse Proxy Configuration

**nginx.prod.conf** Beispiel:

```nginx
upstream api_backend {
    server api-gateway:8080;
    keepalive 32;
}

upstream auth_backend {
    server keycloak:8443;
    keepalive 32;
}

upstream monitoring_backend {
    server grafana:3443;
    keepalive 32;
}

server {
    listen 443 ssl http2;
    server_name api.meldestelle.at;

    ssl_certificate /etc/ssl/nginx/api.meldestelle.at.crt;
    ssl_certificate_key /etc/ssl/nginx/api.meldestelle.at.key;

    # Security headers
    add_header Strict-Transport-Security "max-age=31536000; includeSubDomains" always;
    add_header X-Frame-Options DENY always;
    add_header X-Content-Type-Options nosniff always;
    add_header Referrer-Policy strict-origin-when-cross-origin always;

    location / {
        proxy_pass http://api_backend;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

### Resource Limits

Alle Production-Services haben definierte Resource-Limits:

```yaml
# Beispiel für Resource-Management
services:
  postgres:
    deploy:
      resources:
        limits:
          memory: 1G
          cpus: '0.5'
        reservations:
          memory: 512M
          cpus: '0.25'

  api-gateway:
    deploy:
      resources:
        limits:
          memory: 512M
          cpus: '0.5'
        reservations:
          memory: 256M
          cpus: '0.25'
```

## 🎯 AI-Assistenten: Production-Schnellreferenz

### Production-Domains
- **API:** api.meldestelle.at (HTTPS)
- **Auth:** auth.meldestelle.at (HTTPS)
- **Monitoring:** monitor.meldestelle.at (HTTPS)
- **Metrics:** metrics.meldestelle.at (HTTPS)

### Security-Checkliste
- [ ] SSL/TLS-Zertifikate installiert und gültig
- [ ] Alle Passwörter mit `openssl rand -base64 32` generiert
- [ ] Nginx Security Headers konfiguriert
- [ ] Resource Limits für alle Services definiert
- [ ] Firewall-Regeln nur für notwendige Ports
- [ ] Container laufen als non-root User

### Production-Befehle

| Aufgabe | Befehl | Beschreibung |
|---------|---------|--------------|
| Zertifikat erneuern | `certbot renew` | Let's Encrypt Zertifikate |
| SSL-Status prüfen | `openssl s_client -connect api.meldestelle.at:443` | SSL-Verbindung testen |
| Resource-Usage | `docker stats` | Container-Ressourcen |
| Security-Scan | `docker scan <image>` | Vulnerability Check |
| Log-Rotation | `docker system prune -f` | Alte Logs bereinigen |

### Environment-Variablen Validierung

```bash
# Production-Variablen prüfen
echo "Postgres User: $POSTGRES_USER"
echo "Keycloak Hostname: $KC_HOSTNAME"
echo "Grafana Hostname: $GRAFANA_HOSTNAME"

# Passwort-Stärke prüfen (sollte 32+ Zeichen haben)
echo ${#POSTGRES_PASSWORD}  # Sollte 44+ ausgeben
echo ${#KEYCLOAK_ADMIN_PASSWORD}  # Sollte 44+ ausgeben
```

### Deployment-Workflow

```bash
# 1. Environment-Variablen setzen
source .env.production

# 2. SSL-Zertifikate prüfen
certbot certificates

# 3. Services mit Production-Konfiguration starten
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d

# 4. Health-Checks durchführen
curl -f https://api.meldestelle.at/actuator/health
curl -f https://auth.meldestelle.at/health/ready
curl -f https://monitor.meldestelle.at/api/health

# 5. SSL-Konfiguration validieren
curl -I https://api.meldestelle.at | grep -i security
```

### Backup-Strategie

```bash
# Datenbank-Backup
docker-compose exec postgres pg_dump -U $POSTGRES_USER $POSTGRES_DB > backup_$(date +%Y%m%d).sql

# Konfigurationsdateien sichern
tar -czf config_backup_$(date +%Y%m%d).tar.gz config/

# Docker-Volumes sichern
docker run --rm -v postgres_data:/data -v $(pwd):/backup alpine tar czf /backup/postgres_backup_$(date +%Y%m%d).tar.gz /data
```

### Monitoring-Integration

```bash
# Prometheus-Targets prüfen
curl -s https://metrics.meldestelle.at/api/v1/targets | jq '.data.activeTargets[].health'

# Grafana-Dashboard Status
curl -s https://monitor.meldestelle.at/api/health | jq '.database'
```

---

**Navigation:**
- [Docker-Overview](./docker-overview.md) - Grundlagen und Philosophie
- [Docker-Architecture](./docker-architecture.md) - Container-Services und Struktur
- [Docker-Development](./docker-development.md) - Entwicklungsworkflow
- [Docker-Monitoring](./docker-monitoring.md) - Observability
- [Docker-Troubleshooting](./docker-troubleshooting.md) - Problemlösung
