# Runbook: Lokale Entwicklungsumgebung

Dieses Dokument beschreibt, wie die Docker-basierte lokale Entwicklungsumgebung für das Projekt "Meldestelle" verwendet wird.

## 1. Voraussetzungen

- Docker und Docker Compose müssen installiert sein.
- Eine `.env`-Datei muss im Projekt-Root vorhanden sein. Eine Vorlage befindet sich in `.env.example`.

## 2. Starten der Umgebung

Die Docker-Compose-Konfiguration verwendet [Profile](https://docs.docker.com/compose/profiles/), um das Starten von verschiedenen Teilen des Systems zu ermöglichen.

### 2.1. Vollständiger Start (All-in-One)

Dieser Befehl startet alle definierten Services, einschließlich der Kerninfrastruktur, Backend-Services, Frontend und Monitoring-Tools.

```bash
docker-compose --profile all up --build -d
```

- `--build`: Baut die Images vor dem Start neu, falls Änderungen in den Dockerfiles oder im Quellcode vorliegen.
- `-d`: Startet die Container im "detached" Modus (im Hintergrund).

### 2.2. Nur Backend-Entwicklung

Wenn Sie nur an den Backend-Services arbeiten, können Sie die GUI- und Monitoring-Komponenten weglassen, um Ressourcen zu sparen.

```bash
docker-compose --profile backend --profile infra up --build -d
```

Dieser Befehl startet:
- Die Kerninfrastruktur (`infra`): `postgres`, `redis`, `keycloak`, `consul`.
- Alle Backend-Services (`backend`): `api-gateway`, `ping-service`, etc.

### 2.3. Nur Frontend-Entwicklung

Wenn Sie primär am Frontend arbeiten, benötigen Sie nur das API-Gateway und die Kerninfrastruktur.

```bash
docker-compose --profile gui --profile infra up --build -d
```

Dieser Befehl startet:
- Die Kerninfrastruktur (`infra`).
- Das `api-gateway`.
- Die `web-app` (Nginx-Server für das Frontend).

## 3. Stoppen der Umgebung

Um alle laufenden Container des Projekts zu stoppen:

```bash
docker-compose down
```

Um die Container zu stoppen und auch die benannten Volumes (Datenbankinhalte, etc.) zu löschen:

```bash
docker-compose down -v
```

## 4. Debugging eines Services

Alle Backend-Services sind so konfiguriert, dass sie im Debug-Modus gestartet werden können, wenn die Umgebungsvariable `DEBUG=true` gesetzt ist.

**Beispiel: `ping-service` debuggen**

1.  **Setzen der Umgebungsvariable:**
    Öffnen Sie die `docker-compose.yaml` und finden Sie den `ping-service`. Fügen Sie unter `environment` die folgende Zeile hinzu oder passen Sie sie an:
    ```yaml
    environment:
      # ... andere Variablen
      DEBUG: "true"
    ```
    *Hinweis: In der `docker-compose.yaml` ist dies bereits über `${PING_DEBUG:-true}` vorbereitet. Sie können also auch Ihre `.env`-Datei anpassen.*

2.  **Port-Mapping:**
    Der `ping-service` mappt den Debug-Port `5006` auf den Host.

3.  **Starten und Verbinden:**
    Starten Sie die Umgebung neu (`docker-compose up -d --build ping-service`). Sie können nun einen Remote-Debugger Ihrer IDE mit dem Host `localhost` und dem Port `5006` verbinden.

## 5. Nützliche Endpunkte und Tools

Wenn die Umgebung mit dem `all`-Profil läuft, sind die folgenden Tools verfügbar:

- **Keycloak Admin Console:** [http://localhost:8180](http://localhost:8180)
  - Login: `kc-admin` / `kc-password` (Standard, siehe `.env`)
- **pgAdmin (Datenbank-Tool):** [http://localhost:8888](http://localhost:8888)
  - Login: `meldestelle@mo-code.at` / `pgadmin` (Standard, siehe `.env`)
- **Grafana (Monitoring):** [http://localhost:3000](http://localhost:3000)
  - Login: `gf-admin` / `gf-password` (Standard, siehe `.env`)
- **Consul UI (Service Discovery):** [http://localhost:8500](http://localhost:8500)
- **API Gateway:** [http://localhost:8081](http://localhost:8081)
- **Web App:** [http://localhost:4000](http://localhost:4000)
- **Mailpit (E-Mail Testing):** [http://localhost:8025](http://localhost:8025)
  - Fängt alle E-Mails ab, die von Keycloak oder den Services gesendet werden.

## 6. Fortgeschrittene Anwendungsfälle: Override-Dateien

Im Projekt existieren leere Dateien wie `docker-compose.frontend.yaml` und `docker-compose.services.yaml`. Diese sind absichtlich leer und sollen **nicht** versioniert werden. Sie dienen als persönliche Override-Dateien für lokale Entwicklungsszenarien.

**Zweck:**
Anstatt die zentrale `docker-compose.yaml` zu verändern, können Sie lokale Anpassungen in diesen Dateien vornehmen.

**Beispiel: Frontend-Entwicklung mit Hot-Reload**

Um Hot-Reload für die Nginx-Konfiguration zu aktivieren, können Sie die `docker-compose.frontend.yaml` mit folgendem Inhalt füllen:

```yaml
# docker-compose.frontend.yaml
services:
  web-app:
    volumes:
      - ./config/docker/nginx/web-app/nginx.conf:/etc/nginx/nginx.conf:ro
```

Starten Sie die Umgebung dann mit beiden Dateien:

```bash
docker-compose -f docker-compose.yaml -f docker-compose.frontend.yaml up
```

Docker Compose wird die Konfigurationen zusammenführen, wobei die Definitionen in der `docker-compose.frontend.yaml` Vorrang haben.
