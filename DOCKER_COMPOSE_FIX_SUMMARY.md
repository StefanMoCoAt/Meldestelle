# Docker Compose Fix Summary - Meldestelle Project

## What was failing
Starting docker-compose.services.yml or docker-compose.clients.yml alone (while docker-compose.yml was already running) failed with errors like:
- service "ping-service" depends on undefined service "consul"
- service "web-app" depends on undefined service "api-gateway"

## Root cause
Docker Compose validates depends_on only against services defined in the same compose project (the files provided in the same command). Our services/clients files referenced infrastructure services (consul, postgres, redis, keycloak, api-gateway) that live in docker-compose.yml, so starting them standalone produced “depends on undefined service”.

## Fixes applied (minimal, safe)
1. Removed cross-file depends_on from these files:
   - docker-compose.services.yml → ping-service (removed depends_on on consul, postgres, redis)
   - docker-compose.clients.yml → web-app, desktop-app, auth-server, monitoring-server (removed depends_on on api-gateway, keycloak, postgres)
2. Kept existing healthchecks. The apps already handle startup ordering by retrying connections, and you are starting infra first, so this is safe.
3. Left networking as-is to continue sharing the same project-scoped bridge network when using the same project name.

## How to run now
Option A — Recommended project name (ensures all stacks share the same resources):
- Start infra:
  docker compose -p meldestelle -f docker-compose.yml up -d
- Start services (optional):
  docker compose -p meldestelle -f docker-compose.services.yml up -d
- Start clients (optional):
  docker compose -p meldestelle -f docker-compose.clients.yml up -d

Option B — Combined (unchanged and still works):
- Infra + Services:
  docker compose -f docker-compose.yml -f docker-compose.services.yml up -d
- Infra + Clients:
  docker compose -f docker-compose.yml -f docker-compose.clients.yml up -d
- Full stack:
  docker compose -f docker-compose.yml -f docker-compose.services.yml -f docker-compose.clients.yml up -d

Notes:
- Always start docker-compose.yml before the others when running separately.
- Using -p meldestelle ensures the same project-scoped network (meldestelle_meldestelle-network) is reused so containers can resolve each other (postgres, consul, api-gateway, etc.).
- If you prefer not to pass -p each time, you can export COMPOSE_PROJECT_NAME=meldestelle in your shell or define it in .env.

## Status
- Services and clients files can now be started standalone (with -p meldestelle) while the infra stack is already running.
- Combined modes continue to work.
