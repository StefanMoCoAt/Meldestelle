# SSL/TLS Zertifikat-Setup für die Produktionsumgebung

Dieses Verzeichnis enthält SSL/TLS-Zertifikate und Schlüssel zur Absicherung der Meldestelle-Anwendung in der Produktionsumgebung.

## Verzeichnisstruktur

```
config/ssl/
├── postgres/          # PostgreSQL SSL-Zertifikate
├── redis/             # Redis TLS-Zertifikate
├── keycloak/          # Keycloak HTTPS-Zertifikate
├── prometheus/        # Prometheus HTTPS-Zertifikate
├── grafana/           # Grafana HTTPS-Zertifikate
├── nginx/             # Nginx SSL-Zertifikate
└── README.md          # Diese Datei
```

## Zertifikat-Anforderungen

### 1. PostgreSQL SSL-Zertifikate
Platzieren Sie die folgenden Dateien in `config/ssl/postgres/`:
- `server.crt` - Server-Zertifikat
- `server.key` - Privater Server-Schlüssel
- `ca.crt` - Certificate Authority-Zertifikat

### 2. Redis TLS-Zertifikate
Platzieren Sie die folgenden Dateien in `config/ssl/redis/`:
- `redis.crt` - Redis Server-Zertifikat
- `redis.key` - Privater Redis Server-Schlüssel
- `ca.crt` - Certificate Authority-Zertifikat
- `redis.dh` - Diffie-Hellman Parameter

### 3. Keycloak HTTPS-Zertifikate
Platzieren Sie die folgenden Dateien in `config/ssl/keycloak/`:
- `server.crt.pem` - Server-Zertifikat im PEM-Format
- `server.key.pem` - Privater Server-Schlüssel im PEM-Format

### 4. Prometheus HTTPS-Zertifikate
Platzieren Sie die folgenden Dateien in `config/ssl/prometheus/`:
- `prometheus.crt` - Prometheus Server-Zertifikat
- `prometheus.key` - Privater Prometheus Server-Schlüssel
- `web.yml` - Prometheus Web-Konfigurationsdatei

### 5. Grafana HTTPS-Zertifikate
Platzieren Sie die folgenden Dateien in `config/ssl/grafana/`:
- `server.crt` - Grafana Server-Zertifikat
- `server.key` - Privater Grafana Server-Schlüssel

### 6. Nginx SSL-Zertifikate
Platzieren Sie die folgenden Dateien in `config/ssl/nginx/`:
- `server.crt` - Haupt-SSL-Zertifikat
- `server.key` - Privater Haupt-SSL-Schlüssel
- `dhparam.pem` - Diffie-Hellman Parameter

## Generierung selbstsignierter Zertifikate (Entwicklung/Test)

⚠️ **Warnung**: Verwenden Sie selbstsignierte Zertifikate nur für Entwicklung und Tests. Nutzen Sie ordnungsgemäß von einer CA signierte Zertifikate in der Produktion.

### CA-Zertifikat generieren
```bash
# CA privaten Schlüssel erstellen
openssl genrsa -out ca.key 4096

# CA-Zertifikat erstellen
openssl req -new -x509 -days 365 -key ca.key -out ca.crt \
  -subj "/C=AT/ST=Vienna/L=Vienna/O=Meldestelle/OU=IT/CN=Meldestelle-CA"
```

### Server-Zertifikate generieren
```bash
# Für jeden Service privaten Schlüssel und Certificate Signing Request generieren
openssl genrsa -out server.key 2048
openssl req -new -key server.key -out server.csr \
  -subj "/C=AT/ST=Vienna/L=Vienna/O=Meldestelle/OU=IT/CN=ihre-domain.com"

# Zertifikat mit CA signieren
openssl x509 -req -days 365 -in server.csr -CA ca.crt -CAkey ca.key \
  -CAcreateserial -out server.crt

# Aufräumen
rm server.csr
```

### Diffie-Hellman Parameter generieren
```bash
openssl dhparam -out dhparam.pem 2048
```

## Produktions-Zertifikat Setup

### Option 1: Let's Encrypt (Empfohlen)
Verwenden Sie Certbot, um kostenlose SSL-Zertifikate zu erhalten:

```bash
# Certbot installieren
sudo apt-get install certbot

# Zertifikate erhalten
sudo certbot certonly --standalone -d ihre-domain.com -d www.ihre-domain.com

# Zertifikate in entsprechende Verzeichnisse kopieren
sudo cp /etc/letsencrypt/live/ihre-domain.com/fullchain.pem config/ssl/nginx/server.crt
sudo cp /etc/letsencrypt/live/ihre-domain.com/privkey.pem config/ssl/nginx/server.key
```

### Option 2: Kommerzielle CA
1. Certificate Signing Requests (CSRs) generieren
2. CSRs an Ihre Certificate Authority übermitteln
3. Signierte Zertifikate herunterladen
4. Zertifikate in entsprechende Verzeichnisse platzieren

### Option 3: Interne CA
Bei Verwendung einer internen Certificate Authority:
1. CSRs für jeden Service generieren
2. Zertifikate mit Ihrer internen CA signieren
3. CA-Zertifikat an alle Clients verteilen

## Dateiberechtigungen

Stellen Sie ordnungsgemäße Dateiberechtigungen für die Sicherheit sicher:

```bash
# Restriktive Berechtigungen für private Schlüssel setzen
chmod 600 config/ssl/*/server.key
chmod 600 config/ssl/*/redis.key
chmod 600 config/ssl/*/prometheus.key

# Lesbare Berechtigungen für Zertifikate setzen
chmod 644 config/ssl/*/server.crt
chmod 644 config/ssl/*/ca.crt

# Verzeichnisberechtigungen setzen
chmod 755 config/ssl/*/
```

## Docker Volume Mounts

Die Zertifikate werden als schreibgeschützte Volumes in die Docker-Container eingebunden:

```yaml
volumes:
  - ./config/ssl/nginx:/etc/ssl/nginx:ro
  - ./config/ssl/keycloak:/opt/keycloak/conf:ro
  # ... weitere Mounts
```

## Zertifikat-Erneuerung

### Automatisierte Erneuerung (Let's Encrypt)
Richten Sie einen Cron-Job für automatische Erneuerung ein:

```bash
# Zu Crontab hinzufügen
0 12 * * * /usr/bin/certbot renew --quiet --post-hook "docker-compose -f docker-compose.prod.yml restart nginx"
```

### Manuelle Erneuerung
1. Neue Zertifikate generieren
2. Alte Zertifikate in SSL-Verzeichnissen ersetzen
3. Betroffene Services neu starten:
   ```bash
   docker-compose -f docker-compose.prod.yml restart nginx keycloak grafana prometheus
   ```

## Sicherheits-Best-Practices

1. **Starke Verschlüsselung verwenden**: Mindestens 2048-Bit RSA-Schlüssel oder 256-Bit ECDSA-Schlüssel verwenden
2. **Regelmäßige Rotation**: Zertifikate regelmäßig rotieren (jährlich oder halbjährlich)
3. **Sichere Speicherung**: Private Schlüssel sicher speichern und Zugriff beschränken
4. **Ablauf überwachen**: Überwachung für Zertifikat-Ablauf einrichten
5. **HSTS verwenden**: HTTP Strict Transport Security aktivieren
6. **Perfect Forward Secrecy**: ECDHE-Cipher-Suites verwenden
7. **Certificate Transparency**: CT-Logs auf unbefugte Zertifikate überwachen

## Fehlerbehebung

### Häufige Probleme

1. **Berechtigung verweigert**
   ```bash
   # Dateiberechtigungen korrigieren
   sudo chown -R $USER:$USER config/ssl/
   chmod -R 755 config/ssl/
   chmod 600 config/ssl/*/server.key
   ```

2. **Zertifikat-Verifizierung fehlgeschlagen**
   ```bash
   # Zertifikat verifizieren
   openssl x509 -in config/ssl/nginx/server.crt -text -noout

   # Zertifikatskette prüfen
   openssl verify -CAfile config/ssl/nginx/ca.crt config/ssl/nginx/server.crt
   ```

3. **TLS-Handshake-Fehler**
   - Gültigkeitsdaten des Zertifikats prüfen
   - Verifizieren, dass Zertifikat zum Hostnamen passt
   - Ordnungsgemäße Cipher-Suite-Konfiguration sicherstellen

### SSL-Konfiguration testen

```bash
# SSL-Zertifikat testen
openssl s_client -connect ihre-domain.com:443 -servername ihre-domain.com

# Mit spezifischem Protokoll testen
openssl s_client -connect ihre-domain.com:443 -tls1_2

# Zertifikat-Ablauf prüfen
openssl x509 -in config/ssl/nginx/server.crt -noout -dates

# Zertifikat-Details anzeigen
openssl x509 -in config/ssl/nginx/server.crt -text -noout
```

## Monitoring und Wartung

### Zertifikat-Überwachung
Implementieren Sie Überwachung für:
- Zertifikat-Ablaufdaten
- Zertifikat-Gültigkeit
- SSL/TLS-Handshake-Erfolg
- Cipher-Suite-Verwendung

### Wartungsaufgaben
- Regelmäßige Überprüfung der Zertifikat-Gültigkeit
- Aktualisierung der Cipher-Suites
- Überwachung der Sicherheitsupdates
- Backup der Zertifikate und privaten Schlüssel

## Weitere Ressourcen

- [Mozilla SSL Configuration Generator](https://ssl-config.mozilla.org/)
- [SSL Labs Server Test](https://www.ssllabs.com/ssltest/)
- [Let's Encrypt Dokumentation](https://letsencrypt.org/docs/)
- [OpenSSL Dokumentation](https://www.openssl.org/docs/)

---

**Letzte Aktualisierung**: 25. Juli 2025

Für weitere Informationen zur Produktionsumgebung siehe [README-PRODUCTION.md](../../Tagebuch/README-PRODUCTION.md).
