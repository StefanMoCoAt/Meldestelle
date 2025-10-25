---
owner: project-owner
status: active
last_reviewed: 2025-10-15
review_cycle: 90d
summary: Gesamtüberblick – Was ist vorhanden, wie funktioniert es, wie starte/deploye ich es.
---

# Meldestelle – System Overview (Kurz & vollständig)

## Was ist vorhanden (Bausteine)

- Clients
  - Web App (Port 4000)
  - optional Desktop/noVNC (Port 6080)
- Gateway & Services
  - API Gateway (Spring Cloud Gateway, Port 8081)
  - Microservices: Members (8083), Horses (8084), Events (8085), Masterdata (8086), Ping (8082)
- Infrastruktur
  - Postgres (5432), Redis (6379), Keycloak (8180), Consul (8500)
- Reverse Proxy (Produktion)
  - Nginx auf Proxmox-Host
  - vHosts: `meldestelle.yourdomain.com` (Web), `api.meldestelle.yourdomain.com` (API), `vnc.meldestelle.yourdomain.com` (VNC)

## Wie funktioniert es (Ablauf & Verantwortungen)

- Einstieg nur über das API-Gateway (Security, CORS, Rate-Limits, Observability, Routing)
- Authentifizierung via Keycloak (OIDC/JWT)
  - Web holt Token bei Keycloak → sendet Requests mit `Authorization: Bearer <JWT>`
  - Gateway validiert JWT (JWKs), injiziert Kontext, routet an Services
- Service Discovery über Consul (Gateway ↔ Services)
- Persistenz: Services schreiben/lesen in Postgres; Redis optional für Cache
- Produktion: Öffentliche Zugriffe laufen über Nginx-vHosts → Gateway/Web/noVNC in Docker

## Starten & Stoppen (lokal)

- Komplettes System: `make full-up`
- Nur Infrastruktur: `make infrastructure-up`
- Nur Backend (inkl. Gateway): `make services-up`
- Nur Clients (inkl. Web): `make clients-up`
- Stoppen: `make full-down` (bzw. `*-down`)
- Logs: `make full-logs` (bzw. `services-logs`, `infrastructure-logs`)

## Health, URLs & Ports

- Web: `http://localhost:4000` → Health: `/health`
- Gateway: `http://localhost:8081` → Health: `/actuator/health`
- Services (dev): Ping 8082, Members 8083, Horses 8084, Events 8085, Masterdata 8086
- Keycloak: `http://localhost:8180`
- Consul UI: `http://localhost:8500`
- Postgres: `localhost:5432`
- Redis: `localhost:6379`
- noVNC: `http://localhost:6080`

## Auth-Flow (kurz)

1. Web ruft geschützte Seite → Redirect zu Keycloak `/authorize`
2. Login → Code → Token-Tausch (ID/Access Token)
3. Web ruft Gateway mit `Bearer <JWT>` auf → Gateway prüft Token → leitet an Service

## Produktion (Proxmox/Nginx)

- Datei: `docs/proxmox-nginx/meldestelle.conf`
- vHosts:
  - `meldestelle.yourdomain.com` → Web (`localhost:4000`)
  - `api.meldestelle.yourdomain.com` → Gateway (`localhost:8081`)
  - `vnc.meldestelle.yourdomain.com` → noVNC (`localhost:6080`)
- Health-Checks:
  - `curl -i http://api.meldestelle.yourdomain.com/actuator/health`
  - `curl -i http://meldestelle.yourdomain.com/health`

## Konfiguration auf einen Blick (Defaults)

- Postgres: `POSTGRES_USER=meldestelle`, `POSTGRES_PASSWORD=meldestelle`, `POSTGRES_DB=meldestelle`
- Keycloak Admin: `KC_BOOTSTRAP_ADMIN_USERNAME=admin`, `KC_BOOTSTRAP_ADMIN_PASSWORD=admin`
- Gateway: Port 8081, Profil `dev` (per `SPRING_PROFILES_ACTIVE`)

## Troubleshooting (Top 5)

- 401/403 am Gateway: Token fehlt/abgelaufen? Keycloak auf `http://localhost:8180` erreichbar?
- 502/Bad Gateway: Zielservice down? Logs prüfen (`make services-logs`).
- CORS im Browser: API über `api.meldestelle.*` bzw. `localhost:8081` aufrufen.
- Consul leer: Services nicht registriert → Services neu starten.
- Port-Konflikt: Belegte Ports mit `lsof -i :<port>` prüfen, Prozesse beenden.

## Diagramme (PlantUML)

Siehe `docs/architecture/c4/` – Context & Container sowie Login‑Sequenz. In CI zu SVG rendern.
