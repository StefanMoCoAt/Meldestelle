 # Start Local (Lokales Setup)

 Kurzanleitung, um das Projekt lokal in wenigen Minuten zu starten.

 ## Voraussetzungen
 - Docker und Docker Compose (v2)
 - Java 25 (JDK)
 - Git

 ## Schnellstart

 ```bash
 # 1) Repository klonen
 git clone https://github.com/StefanMoCoAt/meldestelle.git
 cd meldestelle

 # 2) Runtime-Environment vorbereiten (Single Source of Truth)
 #   Kopiere die Vorlage und passe sie bei Bedarf an.
 cp -n .env.template config/env/.env 2>/dev/null || true
 #   Optionale lokale Geheimnisse/Overrides (gitignored):
 #   echo "POSTGRES_PASSWORD=meinlokalespasswort" >> config/env/.env.local

 # 3) (Optional) Compose-Files generieren
 #    (nur falls du die Generator-Pipeline nutzt)
 # DOCKER_SSOT_MODE=envless bash scripts/generate-compose-files.sh all development

 # 4) Infrastruktur starten (Postgres, Redis, Kafka, Keycloak, Monitoring, Gateway)
 docker compose -f docker-compose.yaml up -d

 # 5) Backend-Service starten (Beispiel: Results Service)
 ./gradlew :backend:services:results:results-service:bootRun
 # oder – falls zentral gewollt und unterstützt:
 # ./gradlew bootRun
 ```

 Sobald die Infrastruktur läuft, erreichst du unter anderem:
 - Gateway: http://localhost:8081
 - Keycloak: http://localhost:8180
 - Grafana: http://localhost:3000
 - Prometheus: http://localhost:9090

 ## Tests ausführen
 ```bash
 ./gradlew test
 # Spezifisches Modul
 ./gradlew :backend:services:results:results-service:test
 ```

 ## Troubleshooting
 - Dienste starten nicht? Ports belegt oder Logs prüfen:
   ```bash
   docker ps
   docker logs <container-name>
   ```
 - Infrastruktur neu starten:
   ```bash
   docker compose -f docker-compose.yaml down -v
   docker compose -f docker-compose.yaml up -d
   ```
 - Environment-Variablen: in `config/env/.env` und optional `config/env/.env.local`.

 ## Weiterführende Hinweise
 - Architektur: `docs/01_Architecture/ARCHITECTURE.md`
 - ADRs: `docs/01_Architecture/adr/`
 - C4-Diagramme: `docs/01_Architecture/c4/`

 Stand: Dezember 2025
