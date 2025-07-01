# To-Do-Liste: Ausbaustufen für das ÖTO-Meldestellen-System

**Stand:** 02. Juli 2025

Dies ist eine Übersicht der nächsten logischen Schritte zur Vervollständigung des Systemdesigns, aufbauend auf dem bestehenden DDD-Modell.

---

## Bounded Context: `Abrechnung`

Dieser Context ist entscheidend für alle finanziellen Transaktionen und fehlt bisher komplett.

- [ ] **Datenmodell für Einnahmen entwerfen**
    - [ ] Entität `Rechnung` für Nenngelder, Stallgebühren etc. erstellen.
    - [ ] Entität `Zahlungseingang` zur Verfolgung von bezahlten Beträgen modellieren.
    - [ ] Prozess zur Verknüpfung von Zahlungen mit Nennungen definieren.

- [ ] **Datenmodell für Ausgaben (Preisgelder) entwerfen**
    - [ ] Entität `Preisgeldauszahlung` erstellen.
    - [ ] Prozess für die Berechnung und Zuordnung von Preisgeldern basierend auf der `Ergebnis`-Entität modellieren.
    - [ ] Statusverfolgung für Auszahlungen (z.B. "offen", "ausbezahlt") definieren.

- [ ] **Prozess für die Veranstalter-Abrechnung modellieren**
    - [ ] Logik zur Erstellung einer Endabrechnung (Einnahmen vs. Ausgaben) für den Veranstalter entwerfen.

---

## Funktionalität: Mannschaftswertungen

Die aktuelle Modellierung deckt nur Einzelnennungen ab.

- [ ] **Datenmodell für Mannschaften erstellen**
    - [ ] Entität `Mannschaft` definieren (Name, Verein, etc.).
    - [ ] Entität `Mannschaftsmitglied` als M:N-Beziehung zwischen `Mannschaft` und `Nennung` modellieren.
    - [ ] Prozess für die Mannschaftsnennung im `Nennungs_Context` entwerfen.

- [ ] **Modell für Mannschaftsergebnisse definieren**
    - [ ] Entität `Mannschaftsergebnis` im `Ergebnis_Context` erstellen.
    - [ ] Geschäftsregeln für die Berechnung von Mannschaftsergebnissen festlegen (z.B. Streichergebnisse).

---

## Bounded Context: `Identität & Zugriff`

Ein detailliertes Berechtigungssystem ist für den Betrieb unerlässlich.

- [ ] **Rollenkonzept definieren**
    - [ ] Rollen identifizieren (z.B. Meldestelle, Veranstalter, Richter, Zeitnehmer, OEPS-Admin).
    - [ ] Rechte pro Rolle granular festlegen (z.B. "darf Ergebnisse eintragen", "darf Turnier anlegen").

- [ ] **Datenmodell für Benutzer und Rechte erstellen**
    - [ ] Entität `Benutzer` für den Systemzugang definieren.
    - [ ] Entitäten für `Rollen` und `Berechtigungen` erstellen und mit `Benutzer` verknüpfen.

---

## Funktionalität: Detaillierte Zeitplanung (Zeiteinteilung)

Die Erstellung eines exakten Zeitplans ist ein komplexer Prozess.

- [ ] **Ressourcenmodell entwerfen**
    - [ ] Entitäten für Veranstaltungs-Ressourcen wie `Reitplatz` oder `Abreiteplatz` erstellen.
    - [ ] Modell zur Planung der Verfügbarkeit von Richtern und Funktionären entwickeln.

- [ ] **Planungslogik definieren**
    - [ ] Geschäftsregeln zur Berechnung von Prüfungsdauern (basierend auf Starterzahl) festlegen.
    - [ ] Logik für die Planung von Pausen, Umbauzeiten und Parallelnutzung von Ressourcen modellieren.

---

## Erweiterung: `Nennungs_Context` (Spezialfälle)

Das OEPS Pflichtenheft beschreibt Spezialfälle, die noch nicht vollständig im Modell abgebildet sind.

- [ ] **Prozess für Pferdetausch modellieren**
    - [ ] Methode `tauschePferd()` im `Nennung`-Aggregat entwerfen.
    - [ ] [cite_start]Logik zur Abbildung des **T-Satzes** für die Ergebnisdatei definieren. [cite: 209, 215]

- [ ] **Prozess für Nachnennungen modellieren**
    - [ ] Regeln für Nachnennungen (z.B. erhöhte Gebühren, Fristen) definieren.
    - [ ] [cite_start]Logik zur Abbildung des **N-Satzes** für die Ergebnisdatei entwerfen. [cite: 211, 215]

---

## Funktionalität: Berichtswesen & Dokumentation

Ein System muss diverse Ausgaben generieren können.

- [ ] **Design für Standard-Dokumente erstellen**
    - [ ] Layout und Datenanforderungen für druckfertige Startlisten definieren.
    - [ ] Layout und Datenanforderungen für offizielle Ergebnislisten definieren.
    - [ ] Design für weitere Dokumente wie Boxenschilder oder Richterzettel entwerfen.

- [ ] **Konzept für Berichte entwickeln**
    - [ ] Datenanforderungen für Finanzberichte für den Veranstalter spezifizieren.
    - [ ] Konzept für statistische Auswertungen (z.B. Teilnehmer pro Prüfung, erfolgreichste Pferde/Reiter) entwickeln.
