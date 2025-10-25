---
owner: stefan
status: active
timeframe: 2025-10-15 → 2025-10-29
last_reviewed: 2025-10-15
review_cycle: 7d
summary: Git-Strategie (Trunk-based) + GitHub Actions CI/CD etablieren; Branchschutz, Releases per Tags; optional Images & Deploy zu Proxmox.
---

# Aktuelle Initiative: Git-Flow & GitHub Actions Strategy

## 1) Vision (Was?)

Ein schlanker, verlässlicher Dev-Flow: kurze Feature-Branches → PR → main, automatisierte CI, Releases per Tag; optional Build & Deploy.

- In Scope: Branchschutz `main`, CI aktualisieren, Release-Tags, (optional) Image-Build & Deploy-Workflow
- Out of Scope: Mehrstufige Environments/Canary, komplexe Monorepo-Pipelines

## 2) Why (Warum so?)

Weniger Overhead als GitFlow, klare Qualitätstore via CI, reproduzierbare Releases mit Tags. Ziel: schneller und sicherer liefern.

- Erfolg messbar an: CI grün auf PRs, geschützter `main`, erster Release-Tag `v0.1.0`, optional erfolgreicher Deploy-Run

## 3) How (Wie umsetzen?)

- Trunk-based: `main` geschützt; kurzlebige Branches `feature/*`, `fix/*`, `docs/*`, Squash-Merge only; PR-Titel nach Conventional Commits
- CI (CI.yml): Trigger auf PR/push zu `main`; Schritte: Gradle Build, Docs-Validation, optional Testreports-Artefakte; Concurrency aktiv
- Releases: zunächst manuell taggen (`vX.Y.Z`); später optional `release-please`
- Images: optional GHCR Build & Push bei Tags `v*` (Matrix für gateway, members, horses, events, masterdata, web)
- Deploy: optional via SSH zu Proxmox (`docker compose pull && up -d`); Secrets im Repo setzen

## 4) Plan (Was ist jetzt zu tun?)

- [ ] Branch umbenennen: `structur-umbau` → `feature/structur-umbau`; pushen und PR nach `main` eröffnen
- [ ] Branchschutz für `main` setzen: PR erforderlich, Required Checks (`CI`), „Squash & Merge only“, lineare History
- [ ] CI-Workflow anpassen: vorhandenes `.github/workflows/build.yml` auf PR/push nur `main`, Concurrency, Testreports-Upload
- [ ] (Optional) Deploy-Workflow anlegen: `.github/workflows/deploy.yml` (SSH); Secrets setzen: `PROD_SSH_HOST`, `PROD_SSH_USER`, `PROD_SSH_KEY`
- [ ] (Optional) Image-Build & Push bei Tags (`v*`) zu GHCR einführen
- [ ] (Optional) Release-Strategie entscheiden: manuelle Tags vs. `release-please`; ggf. Workflow hinzufügen
- [ ] Cloudflare/Nginx prüfen: DNS (Proxy ON), SSL/TLS „Full (strict)“, Origin-Zertifikat, HTTPS-Serverblöcke aktivieren
- [ ] Smoke-Tests: Health über Domains prüfen (`/actuator/health`, `/health`)

## 5) Status & Nächster Fokus

- Status: active
- Nächster Fokus (heute): Branch umbenennen → PR; Branchschutz setzen; CI-Workflow auf `main` fokussieren

## 6) Referenzen

- CI-Workflow aktuell: `.github/workflows/build.yml`
- Docs-Validator: `scripts/validation/validate-docs.sh`
- Nginx Beispiel: `docs/proxmox-nginx/meldestelle.conf`
- Überblick/Start: `docs/overview/system-overview.md`, `docs/how-to/start-local.md`
