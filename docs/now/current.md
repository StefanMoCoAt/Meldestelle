---
owner: stefan
status: active
timeframe: 2025-10-15 → 2025-10-29
last_reviewed: 2025-10-15
review_cycle: 7d
summary: Minimal‑Doku + Now‑Page etablieren, um Übersicht zurückzugewinnen.
---

# Aktuelle Initiative: Doku verschlanken & Now‑Page einführen

## 1) Vision (Was?)
Eine verlässliche, minimale Doku (≤5 Seiten) + ein stets aktueller One‑Pager für das laufende Vorhaben.
- In Scope: Start/Overview/API/Prod-HowTo/Now-Page
- Out of Scope: Vollständige Übersetzungen, alte Berichte/Prosa

## 2) Why (Warum so?)
Zu viele, verstreute Dokumente erzeugen Drift und Entscheidungsunsicherheit. Ziel: Orientierung in <2 Min. wiederfinden.
- Erfolg messbar an: 1 Einstiegspunkt, 0 Broken Links, Validierung grün
- Dauerhaft relevante Entscheidungen künftig als ADR, aus Now‑Page verlinkt

## 3) How (Wie umsetzen?)
- Behalten: `docs/index.md`, `overview/system-overview.md`, `how-to/*`, `api/README.md`, `now/current.md`
- Entfernen/Archivieren: `Tagebuch/`, alte Indizes
- CI beibehalten (Link‑Check optional), später Stale‑Check für Now‑Page ergänzen

## 4) Plan (Was ist jetzt zu tun?)
- [ ] Index minimalisieren und nur auf Kernseiten verlinken
- [ ] System Overview anlegen und Ports/Health bündeln
- [ ] Now‑Page Template + current.md erstellen
- [ ] Alte Indizes und Tagebuch entfernen
- [ ] Validierung laufen lassen und etwaige Links reparieren
- [ ] Nächste Initiative vorbereiten: Git‑Flow & GitHub Actions Strategy

## 5) Status & Nächster Fokus
- Status: active
- Nächster Fokus: Validierung ausführen und offene Link‑Themen bereinigen; danach Git‑Flow/GitHub‑Actions planen

## 6) Referenzen
- Start lokal: `docs/how-to/start-local.md`
- Übersicht: `docs/overview/system-overview.md`
- Produktion/Nginx: `docs/how-to/deploy-proxmox-nginx.md`
- API: `docs/api/README.md`
