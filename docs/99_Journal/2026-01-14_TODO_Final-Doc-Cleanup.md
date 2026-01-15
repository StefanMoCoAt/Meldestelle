# TODO-Liste: Finale Bereinigung der Dokumentation

*   **Datum:** 2026-01-14
*   **Autor:** Documentation & Knowledge Curator
*   **Status:** Offen

## Kontext

Nach der großen Dokumentations-Restrukturierung sind beim Commit-Prozess Linting-Fehler und Link-Warnungen aufgetaucht. Diese Liste dient als Plan, um diese letzten Probleme in der nächsten Session zu beheben.

---

## Offene Punkte

### 1. "Broken Links" in Tech-Stack-Referenzen beheben

*   **Problem:** Die aus dem Web kopierten Dokumente (`Gradle_Kotlin_DSL_Primer.md`, `Kotlin_2-3-0_ReleaseNotes.md`) in `docs/02_Reference/Tech_Stack/` enthalten hunderte ungültige, relative Links, die von den Original-Webseiten stammen.
*   **Aktion:**
    *   Öffne beide Dateien.
    *   Entferne die kompletten Navigations-Sidebars und alle anderen internen Links, die Fehler verursachen.
    *   Reduziere die Dateien auf ihren reinen Inhalts-Kern. Der Link zur Original-Quelle am Anfang jeder Datei ist ausreichend.

### 2. Ungültiges HTML in Legacy-Spezifikation korrigieren

*   **Problem:** Die Datei `docs/03_Domain/02_Reference/Legacy_Specs/OETO-2026_Meldestelle_Erweiterung-Schnittstelle_2014.md` enthält ungültige XML/HTML-Tags.
*   **Aktion:**
    *   Öffne die Datei.
    *   Formatiere den Inhalt als Markdown-Code-Block mit dem Typ `xml`, um die Struktur zu erhalten, ohne dass der Parser Fehler meldet.

### 3. Fehlende `README.md`-Dateien im `docs`-Verzeichnis erstellen

*   **Problem:** Die Wegweiser-READMEs in den Modulen `backend`, `core` und `contracts` zeigen auf nicht-existente Zieldateien.
*   **Aktion:** Erstelle die folgenden Platzhalter-Dateien, um die Links gültig zu machen:
    *   `docs/05_Backend/README.md` (mit einem kurzen Platzhaltertext)
    *   `docs/03_Domain/01_Core_Model/README.md` (mit einem kurzen Platzhaltertext)

---

Nach Abarbeitung dieser Liste sollte das Projekt frei von Dokumentations-Warnungen sein.
