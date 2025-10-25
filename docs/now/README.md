---
owner: project-maintainers
status: active
last_reviewed: 2025-10-15
review_cycle: 90d
summary: Anleitung zur Nutzung der Now-Page (Initiativen-One‑Pager) als zentraler Steuerungs- und Übersichtspunkt.
---

# Now-Page – Nutzung & Workflow

Die Now-Page ist ein schlanker One‑Pager für dein aktuelles Vorhaben. Sie beantwortet stets fünf Fragen: Was, Warum, Wie, Was ist zu tun, und Was ist als Nächstes dran.

## Struktur

- Aktive Seite: `docs/now/current.md`
- Vorlage: `docs/now/TEMPLATE.md`
- Archiv (optional): `docs/now/archive/` (einfach Dateien hinein verschieben)

## So verwendest du die Now-Page

1. Neue Initiative starten: Kopiere `TEMPLATE.md` nach `current.md` und fülle sie aus.
2. Kurz halten: 1 Seite, maximal 5–10 Tasks. Große Aufgaben in kleinere Schneiden.
3. Pflege-Ritual: Bei Änderung von Fokus/Status/Plan kurz aktualisieren, `last_reviewed` anpassen.
4. Abschluss: `status: done` setzen, 3 Bulletpoints „Lessons Learned“ ergänzen und Datei nach `now/archive/` verschieben.
5. Dauerhafte Entscheidungen: Als ADR in `docs/architecture/adr/` festhalten und aus der Now‑Page verlinken.

## Tipps

- Verlinke nur, was du beim Arbeiten wirklich brauchst (PRs, Issues, wichtige How‑Tos).
- Nutze die Now‑Page als Daily/Nächstes‑To‑Do‑Quelle statt vieler verstreuter Notizen.
- Optional in CI: Einen „Stale‑Check“ einführen, der warnt, wenn `current.md` länger als `review_cycle` nicht aktualisiert wurde.

## Navigation

- Die Startseite (docs/index.md) verlinkt direkt auf `now/current.md`, damit du jederzeit mit einem Klick am aktuellen Fokus bist.
