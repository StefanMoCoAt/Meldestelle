---
owner: project-maintainers
status: active
review_cycle: 90d
last_reviewed: 2025-10-15
summary: Kürzeste Anleitung, um das komplette System lokal zu starten und zu prüfen, ob alles läuft.
bc: infrastructure
doc_type: how-to
---

# How-To: Lokale Umgebung starten (Quickstart)

Diese Kurz-Anleitung bringt deine lokale Umgebung in wenigen Minuten zum Laufen.

## Starten

- Komplettes System (Infra + Services + Clients)

```bash
make full-up
```

- Nur Backend (Infra + Gateway + Microservices)

```bash
make services-up
```

- Nur Clients (Infra + Web-App)

```bash
make clients-up
```

Logs ansehen (z. B. Backend):

```bash
make services-logs
```

## Wichtige URLs

- Web App: <http://localhost:4000>
- API Gateway: <http://localhost:8081> (Health: /actuator/health)
- Keycloak (Auth): <http://localhost:8180>
- Consul (Service Discovery): <http://localhost:8500>

Weitere Ports findest du unter: [reference/ports-and-urls.md](../reference/ports-and-urls.md)

## Health-Checks

```bash
# Gateway
curl -i http://localhost:8081/actuator/health

# Web-App (falls vorhanden)
curl -i http://localhost:4000/health || true
```

## Auth (Keycloak)

- Admin-Login (default): <http://localhost:8180>
  - Username: KC_BOOTSTRAP_ADMIN_USERNAME (default: admin)
  - Password: KC_BOOTSTRAP_ADMIN_PASSWORD (default: admin)
- Beim ersten Start wird der Realm aus docker/services/keycloak/meldestelle-realm.json importiert.

## Häufige Probleme

- Dienste sind nicht erreichbar → Container laufen? `make full-logs` bzw. `make services-logs` prüfen.
- 401/403 beim API-Aufruf → Prüfen, ob ein gültiges Bearer-Token gesendet wird und Keycloak erreichbar ist.
- CORS im Browser → API über das Gateway (<http://localhost:8081>) aufrufen und nicht direkt die Services (8082–8086).
- Port-Kollisionen → Belegte Ports mit `lsof -i :PORT` prüfen oder Ports anpassen.

## Stoppen

```bash
 make full-down
 # oder spezifisch: 
 make services-down 
 make clients-down 
 make infrastructure-down
```
