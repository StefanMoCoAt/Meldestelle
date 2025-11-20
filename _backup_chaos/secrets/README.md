# Docker Secrets (Development vs. Production)

In der lokalen Entwicklung werden keine Docker-Secrets erzwungen.

- Verwende für sensible Werte stattdessen die Datei `config/env/.env.local` (ist gitignored).
- Die Dateien in diesem Ordner sind lediglich Platzhalter und enthalten KEINE echten Geheimnisse.
- Für ein Deployment in Produktion kannst du diese Dateien mit echten Werten befüllen oder einen sicheren Secret-Store (Docker/K8s) verwenden.

Hinweise:
- Postgres-User/Passwort haben in der lokalen Entwicklung Standard/Fallback-Werte via `docker-compose.yml` (Environment mit Defaults).
- Die optimierten Compose-Dateien (`*.optimized`) können weiterhin Docker-Secrets verwenden – diese sind für Prod gedacht.

Schnellstart lokal (ohne Secrets):
- Passe `config/env/.env` und optional `config/env/.env.local` an
- Starte mit: `docker compose -f docker-compose.yml -f docker-compose.services.yml up`
