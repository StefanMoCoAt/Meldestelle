# TODO-Liste: Finale Bereinigung der Dokumentation

*   **Datum:** 2026-01-14
*   **Autor:** Documentation & Knowledge Curator
*   **Status:** Erledigt

## Kontext

Nach der großen Dokumentations-Restrukturierung sind beim Commit-Prozess Linting-Fehler und Link-Warnungen aufgetaucht. Diese Liste diente als Plan, um diese letzten Probleme in der Session vom 14.01.2026 zu beheben.

---

## Abarbeitung

Alle unten genannten Punkte wurden in der Session vom 14.01.2026 erfolgreich abgearbeitet.

### 1. "Broken Links" in Tech-Stack-Referenzen beheben

*   **Problem:** Die aus dem Web kopierten Dokumente (`Gradle_Kotlin_DSL_Primer.md`, `Kotlin_2-3-0_ReleaseNotes.md`) in `docs/02_Reference/Tech_Stack/` enthielten hunderte ungültige, relative Links.
*   **Lösung:** Die Dateien wurden auf ihren reinen Inhalts-Kern reduziert und von fehlerhaften Links befreit.

### 2. Ungültiges HTML in Legacy-Spezifikation korrigieren

*   **Problem:** Die Datei `docs/03_Domain/02_Reference/Legacy_Specs/OETO-2026_Meldestelle_Erweiterung-Schnittstelle_2014.md` enthielt ungültige XML/HTML-Tags.
*   **Lösung:** Der Inhalt wurde als Markdown-Code-Block vom Typ `xml` formatiert.

### 3. Fehlende `README.md`-Dateien im `docs`-Verzeichnis erstellen

*   **Problem:** Die Wegweiser-READMEs in den Modulen `backend`, `core` und `contracts` zeigten auf nicht-existente Zieldateien.
*   **Lösung:** Die entsprechenden Platzhalter-Dateien (`docs/05_Backend/README.md`, `docs/03_Domain/01_Core_Model/README.md`) wurden erstellt. Ein dabei neu entstandener fehlerhafter Link zum API-Gateway wurde ebenfalls korrigiert.

---

Das Projekt ist nun frei von den bekannten Dokumentations-Warnungen.
