# User Stories Draft - Phase 1 (Core Domain)

*   **Status:** Draft
*   **Fokus:** Nationale Turniere (OEPS), Offline-Betrieb, Basis-Verwaltung

---

## Epic 1: Stammdaten & Offline-Vorbereitung

### US-001: Import der Verbands-Stammdaten (ZNS)
**Als** Meldestellen-Leiter  
**möchte ich** die offizielle `zns.zip` Datei (Pferde, Reiter, Vereine, Richter) in das System importieren,  
**damit** ich auch ohne Internetverbindung Zugriff auf alle validen Lizenz- und Pferdedaten habe.

*   **Akzeptanzkriterien:**
    *   System akzeptiert `zns.zip` oder entpackte `.dat` Dateien (Codepage 850).
    *   Importiert `PFERDE01.dat` (inkl. Mapping der 10-stelligen Satznummer).
    *   Importiert `LIZENZ01.dat` (inkl. Startkarten-Status und Sperrvermerke).
    *   Der Import ist performant genug, um am Turniermorgen aktualisiert zu werden (< 5 Min).
    *   Fehlerhafte Datensätze werden protokolliert, brechen den Import aber nicht ab.

### US-002: Intelligente Akteur-Suche
**Als** Meldestellen-Mitarbeiter  
**möchte ich** Reiter und Pferde über eine fehlertolerante Suche finden (Name, Kopfnummer, Lizenznummer),  
**damit** ich Nennungen schnell erfassen kann, auch wenn der Reiter seine genaue Nummer nicht weiß.

*   **Akzeptanzkriterien:**
    *   Suche nach Pferdenamen (Teilübereinstimmung).
    *   Suche nach Kopfnummer (z.B. "A123").
    *   Anzeige von Warnhinweisen direkt im Suchergebnis (z.B. "Sperrliste", "Keine Startkarte").
    *   Unterscheidung bei Namensgleichheit durch Anzeige von Verein/Jahrgang/Abstammung.

---

## Epic 2: Nennung & Check-in

### US-003: Validierung der Startberechtigung (Startkarte)
**Als** Meldestellen-Leiter  
**möchte ich**, dass das System mich warnt, wenn ein Reiter für einen Bewerb nennt, aber keine aktive Startkarte (Jahresgebühr) hat,  
**damit** ich ihn zur Nachzahlung auffordern kann.

*   **Akzeptanzkriterien:**
    *   Prüfung des Flags `STARTKARTE` aus den Stammdaten.
    *   Prüfung der Lizenzklasse (z.B. darf "R1" nicht in Klasse S starten).
    *   **Wichtig:** Das System darf die Nennung *nicht* blockieren (Soft-Validation), sondern muss einen "Override" ermöglichen (z.B. "Zahlung vor Ort erfolgt").
    *   Visuelle Hervorhebung in der Starterliste (z.B. roter Status).

### US-004: Manuelle Nachnennung vor Ort
**Als** Meldestellen-Mitarbeiter  
**möchte ich** ein Pferd-Reiter-Paar kurzfristig zu einem Bewerb hinzufügen,  
**damit** Teilnehmer, die die Online-Nennfrist verpasst haben, gegen Gebühr noch starten können.

*   **Akzeptanzkriterien:**
    *   Auswahl von Bewerb, Reiter und Pferd.
    *   Automatische Berechnung der erhöhten Nenngebühr (Nachnenngebühr).
    *   Vergabe einer Startnummer (fortlaufend oder manuell).
    *   Eintrag in die `KKARTEI` (Nennliste) und `BBEWERBE` (Starterliste).

---

## Epic 3: Bewerbs-Abwicklung

### US-005: Verwaltung von Abteilungen
**Als** Meldestellen-Leiter  
**möchte ich** einen Bewerb mit vielen Startern in mehrere Abteilungen (z.B. R1-Reiter vs. R2-Reiter) unterteilen,  
**damit** ich getrennte Ergebnislisten und Platzierungen erstellen kann, wie es die ÖTO verlangt.

*   **Akzeptanzkriterien:**
    *   Ein Bewerb kann in n Abteilungen gesplittet werden.
    *   Starter können per Drag&Drop oder Regel (z.B. "Alle R1 in Abt. 1") zugewiesen werden.
    *   Jede Abteilung hat eine eigene Platzierung, aber sie teilen sich die gleichen Prüfungsparameter (Parcours).
    *   Export berücksichtigt das Feld `ABTEILUNG` im B-Satz.

### US-006: Ergebniserfassung & Platzierung
**Als** Richter oder Schreiber  
**möchte ich** Ergebnisse (Zeit, Fehler, Wertnote) für einen Starter eingeben,  
**damit** die Rangierung automatisch berechnet wird.

*   **Akzeptanzkriterien:**
    *   Eingabemaske optimiert für schnelle Nummernblock-Eingabe.
    *   Automatische Berechnung der Rangfolge basierend auf dem Regelwerk (Fehler/Zeit vs. Wertnote).
    *   Handling von Spezialfällen: Ausschluss (EL), Aufgabe (RET), Disqualifikation (DQ), Nicht angetreten (DNS).
    *   Sofortige Aktualisierung der "Live-Ergebnisse".

---

## Epic 4: Abschluss & Export

### US-007: OEPS-Konformer Ergebnis-Export
**Als** Meldestellen-Leiter  
**möchte ich** die Ergebnisse des Turniers in das definierte Format (ASCII, Codepage 850) exportieren,  
**damit** ich meiner Meldepflicht gegenüber dem Verband nachkommen kann.

*   **Akzeptanzkriterien:**
    *   Erstellung der `XXXXX.ERG` Datei.
    *   Strikte Einhaltung der Spaltenbreiten und Formate (siehe Legacy Spec Analyse).
    *   Validierung vor Export: Warnung bei fehlenden Satznummern oder ungültigen Codes.
    *   Korrekte Zuordnung der Nation (Gast vs. Inländer).

### US-008: Kassenabschluss & Abrechnung
**Als** Veranstalter  
**möchte ich** eine Liste aller offenen Posten (Nenngelder, Boxen, Nachnenngebühren) pro Reiter/Verein sehen,  
**damit** ich vor der Ausgabe der Pferdepässe kassieren kann.

*   **Akzeptanzkriterien:**
    *   Aggregierte Ansicht pro "Verantwortlicher Person" (Zahler).
    *   Auflistung aller Posten (Nennung, Startgeld, Gebühren).
    *   Verrechnung von gewonnenen Geldpreisen (Gutschrift).
    *   Druckfunktion für Rechnung/Quittung.
