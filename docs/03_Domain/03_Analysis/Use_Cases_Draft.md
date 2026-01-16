# Use Cases Draft - Phase 1 (Core Domain)

*   **Status:** Draft
*   **Fokus:** High-Level Prozessflüsse und Systemgrenzen

---

## Cluster 1: Turnier-Initialisierung & Datenbasis

### UC-01: Turnier-Stammdaten importieren
*   **Akteur:** Meldestellen-Leiter
*   **Auslöser:** Vorbereitung eines neuen Turniers oder Update am Turniermorgen.
*   **Vorbedingung:** `zns.zip` (oder äquivalente OEPS-Daten) liegt vor.
*   **Ablauf:**
    1.  System liest die Datensätze für Pferde, Reiter, Vereine und Funktionäre.
    2.  System aktualisiert die lokale Datenbank (Insert/Update).
    3.  System markiert Datensätze mit Sperrvermerken oder fehlenden Lizenzen.
*   **Nachbedingung:** Die lokale Datenbank ist die "Single Source of Truth" für Validierungen.

### UC-02: Turnier-Konfiguration anlegen
*   **Akteur:** Meldestellen-Leiter
*   **Auslöser:** Erstellung eines neuen Events.
*   **Vorbedingung:** Ausschreibung liegt vor.
*   **Ablauf:**
    1.  Akteur definiert Stammdaten (Ort, Datum, Veranstalter).
    2.  Akteur legt Bewerbe an (Nummer, Klasse, Richtverfahren).
    3.  Akteur definiert Gebühren (Nenngeld, Startgeld, Boxenpreise).
*   **Nachbedingung:** Das Turniergerüst steht bereit für Nennungen.

---

## Cluster 2: Nennungs-Management (Pre-Competition)

### UC-03: Nennung erfassen & validieren
*   **Akteur:** Meldestellen-Mitarbeiter
*   **Auslöser:** Import von Online-Nennungen oder manuelle Eingabe.
*   **Ablauf:**
    1.  System prüft Existenz von Reiter und Pferd (via Satznummer).
    2.  **Validierung:**
        *   Ist die Startkarte bezahlt?
        *   Ist die Lizenz ausreichend für die Klasse?
        *   Liegt eine Sperre vor?
        *   Ist das Pferd geimpft/registriert?
    3.  Bei Validierungsfehler: System zeigt Warnung, erlaubt aber "Override" durch Akteur (z.B. "Zahlung erfolgt").
    4.  System verknüpft Paar mit Bewerb.
*   **Nachbedingung:** Das Paar ist auf der "Nennliste" (noch nicht Starterliste).

### UC-04: Pferd/Reiter tauschen
*   **Akteur:** Meldestellen-Mitarbeiter
*   **Auslöser:** Reiter fällt aus oder Pferd ist lahm.
*   **Ablauf:**
    1.  Akteur wählt bestehende Nennung.
    2.  Akteur tauscht Reiter ODER Pferd aus.
    3.  System führt Validierung (UC-03) für die neue Kombination durch.
    4.  System protokolliert den Tausch (relevant für T-Satz im Export).
*   **Nachbedingung:** Nennung ist aktualisiert, Historie ist gewahrt.

---

## Cluster 3: Durchführung & Sport (Competition)

### UC-05: Startliste erstellen
*   **Akteur:** Meldestellen-Leiter
*   **Auslöser:** Nennschluss für einen Bewerb ist erreicht.
*   **Ablauf:**
    1.  Akteur definiert Startreihenfolge (z.B. "Alphabetisch", "Gelost", "Nach Lizenz").
    2.  System generiert die Reihenfolge.
    3.  System weist Kopfnummern zu (falls noch nicht geschehen).
    4.  System teilt bei Bedarf in Abteilungen (siehe US-005).
*   **Nachbedingung:** Die Startliste ist fixiert und kann gedruckt/publiziert werden.

### UC-06: Ergebnis erfassen
*   **Akteur:** Richter / Schreiber / Zeitnehmung
*   **Auslöser:** Ein Ritt ist beendet.
*   **Ablauf:**
    1.  Akteur wählt Starter.
    2.  Akteur gibt Rohdaten ein (Zeit, Fehlerpunkte, Wertnote).
    3.  System berechnet sofort den Score und den vorläufigen Rang.
    4.  System prüft auf Spezialfälle (Ausschluss, Aufgabe).
*   **Nachbedingung:** Ergebnis ist gespeichert, Live-Ranking ist aktualisiert.

### UC-07: Bewerb abschließen
*   **Akteur:** Meldestellen-Leiter / Hauptrichter
*   **Auslöser:** Letzter Reiter ist fertig, Einspruchsfrist abgelaufen.
*   **Ablauf:**
    1.  System finalisiert die Rangierung (inkl. Ex-Aequo Regeln).
    2.  System berechnet Geldpreise gemäß Ausschreibung und Teilnehmerzahl.
    3.  System sperrt den Bewerb für Änderungen.
*   **Nachbedingung:** Ergebnisse sind "amtlich", Geldpreise sind den Konten gutgeschrieben.

---

## Cluster 4: Abschluss & Finanzen

### UC-08: Konto abrechnen (Kassieren)
*   **Akteur:** Kassen-Mitarbeiter
*   **Auslöser:** Teilnehmer will abreisen/bezahlen.
*   **Ablauf:**
    1.  System aggregiert alle Kosten (Nenngelder, Boxen, Gebühren) pro "Verantwortlicher Person".
    2.  System zieht gewonnene Geldpreise ab.
    3.  System erstellt Saldo.
    4.  Akteur verbucht Zahlungseingang.
*   **Nachbedingung:** Konto ist ausgeglichen, "Horse Pass" kann ausgegeben werden.

### UC-09: OEPS-Export durchführen
*   **Akteur:** Meldestellen-Leiter
*   **Auslöser:** Turnierende.
*   **Ablauf:**
    1.  System prüft Datenintegrität (Alle Pflichtfelder für Export vorhanden?).
    2.  System generiert `XXXXX.ERG` Datei gemäß Spezifikation V2.4.
    3.  System erstellt Protokoll über eventuelle Warnungen/Abweichungen.
*   **Nachbedingung:** Export-Datei liegt bereit zur Übermittlung.
