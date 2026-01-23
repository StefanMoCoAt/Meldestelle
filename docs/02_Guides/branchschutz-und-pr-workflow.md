---
type: Guide
status: ACTIVE
owner: DevOps Engineer
tags: [git, workflow, pr, branching]
---

# Branchschutz & Pull-Request Workflow

Diese Anleitung beschreibt einen einfachen, robusten Flow für `main` mit kurzen Feature-Branches und klaren
Qualitätschecks.

## 1) Branch-Naming

- Feature: `feature/<kurz-beschreibung>`
- Bugfix: `fix/<kurz-beschreibung>`
- Docs: `docs/<kurz-beschreibung>`

Optional: Issue-Key voranstellen, z. B. `feature/MP-7-doku-konsolidieren`.

## 2) Pull Request (PR)

- PR-Titel nach Conventional Commits (kurz): `docs(api): Front‑Matter vereinheitlicht (MP-7)`
- Beschreibung kurz mit Bulletpoints; DoD-Checkliste abhaken (Template vorhanden)
- CI muss grün sein (Backend + Docs)

## 3) Branchschutz (GitHub Einstellungen → Branches → main)

- Require a pull request before merging
- Require status checks to pass before merging
  - aktivieren: `CI Docs`, `CI` (Backend falls vorhanden)
- Require linear history
- Require approvals: mindestens 1 (bei Solo-Projekt optional, aber empfohlen)
- Allow squash merging only
- Disallow force pushes, Disallow deletions

## 4) Commits & YouTrack

- Commit-Message enthält Issue-Key (z. B. `MP-7`) → erleichtert Nachverfolgung
- In Doku-Front‑Matter `yt_epic`/`yt_issues` pflegen
- Optional: GitHub Secrets `YT_URL`, `YT_TOKEN` setzen → CI validiert verlinkte Issues, und `youtrack-sync.yml`
  kommentiert beim Merge automatisch ins Issue

## 5) Definition of Done (Auszug)

- Doku aktuell (README/ADR/C4/API)
- Front‑Matter valide (`modul`, `status`, `summary`, optional `last_reviewed`, `review_cycle`, `yt_*`)
- Links funktionieren (CI link-check grün)
- Tests grün

## 6) Lokale Tipps

- Vor dem Push: `markdownlint`, `vale` lokal laufen lassen (optional via pre-commit hooks)
- kleine, häufige PRs statt großer Monster-PRs
