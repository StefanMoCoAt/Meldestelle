# 01 - Core Domain Entities

Dieses Dokument definiert die zentralen fachlichen Entitäten (Kern-Entitäten) des "Meldestelle"-Projekts. Diese Entitäten bilden das Fundament des Datenmodells und der gesamten Anwendungslogik. Sie sind das Ergebnis der Analyse der OEPS/ÖTO- und FEI-Regelwerke sowie der grundlegenden Anforderungen an das System.

## Die 6 Kern-Entitäten

1.  **Event**: Der organisatorische Rahmen.
2.  **Turnier**: Die administrative, regelbasierte Einheit.
3.  **Bewerb**: Die einzelne sportliche Prüfung.
4.  **Wertungsserie**: Der übergeordnete Cup oder die Meisterschaft.
5.  **Akteur**: Personen und Organisationen.
6.  **Pferd**: Die Pferde als eigenständige Entität.

---

### 1. Entität: `Event`

**Zweck:** Der übergeordnete organisatorische Container für eine Veranstaltung an einem bestimmten Ort und zu einer bestimmten Zeit. Ein Event kann ein oder mehrere Turniere umfassen.

**Beispiele:** "Apropos Pferd 2026", "Vereinsturnier Reitclub XY".

**Attribute:**
*   `Event-ID` (PK): Eindeutiger technischer Schlüssel.
*   `Name`: Offizieller Name des Events.
*   `Veranstaltungsort`: Adresse und Name der Anlage.
*   `Datum_Von`: Startdatum des Events.
*   `Datum_Bis`: Enddatum des Events.
*   `Veranstalter_ID` (FK): Verweis auf den `Akteur`, der das Event ausrichtet.
*   `Status`: Grober Zustand des Events (z.B. `In Planung`, `Laufend`, `Abgeschlossen`).

---

### 2. Entität: `Turnier`

**Zweck:** Definiert eine administrative Einheit innerhalb eines Events, die unter einem einheitlichen Regelwerk stattfindet. Hier werden Nennungen, Starter- und Ergebnislisten verwaltet.

**Beispiele:** "CSN-A im Rahmen der Apropos Pferd", "CSI2* im Rahmen der Apropos Pferd".

**Attribute:**
*   `Turnier-ID` (PK): Eindeutiger technischer Schlüssel.
*   `Event_ID` (FK): Verweis auf das übergeordnete `Event`.
*   `Regelwerk`: Entscheidende Weiche für die Anwendungslogik (Enum: `OETO`, `FEI`).
*   `Kategorie`: Offizielle Turnierkategorie (z.B. "CSN-A", "CSI2*", "CDI-W").
*   `Disziplinen`: Liste der angebotenen Sportarten (z.B. `Springen`, `Dressur`).
*   `Ausschreibung_Text`: Der vollständige Text der Ausschreibung.
*   `Nennungsschluss`: Datum und Uhrzeit.
*   `Status`: Detaillierter Zustand des Turniers (z.B. `Genehmigt`, `Nennschluss`, `Ergebnisse final`).

---

### 3. Entität: `Bewerb`

**Zweck:** Die einzelne sportliche Prüfung innerhalb eines Turniers. Ein Bewerb ist die kleinste Einheit, für die eine Nennung möglich ist und eine Ergebnisliste erstellt wird.

**Beispiele:** "Standardspringprüfung Kl. L", "Dressurprüfung Kl. M - Aufgabe M5".

**Attribute:**
*   `Bewerb-ID` (PK): Eindeutiger technischer Schlüssel.
*   `Turnier_ID` (FK): Verweis auf das zugehörige `Turnier`.
*   `Nummer`: Die offizielle Nummer des Bewerbs laut Ausschreibung (z.B. "05").
*   `Titel`: Der offizielle Titel des Bewerbs.
*   `Startgeld`: Das für diesen Bewerb zu entrichtende Startgeld.
*   `Startberechtigung_Text`: Textuelle Beschreibung der Teilnahmevoraussetzungen.
*   `Besondere_Bestimmungen`: Spezielle Regeln nur für diesen Bewerb.

---

### 4. Entität: `Wertungsserie`

**Zweck:** Definiert eine übergeordnete Wertung (Cup, Meisterschaft), die Ergebnisse aus spezifischen Bewerben über mehrere Turniere hinweg sammelt und nach einem eigenen Regelwerk auswertet.

**Beispiele:** "Casino Grand Prix 2026", "OÖ Landesmeisterschaft Dressur Allgemeine Klasse".

**Attribute:**
*   `Serie-ID` (PK): Eindeutiger technischer Schlüssel.
*   `Name`: Offizieller Name der Serie.
*   `Saison`: Das Jahr, in dem die Serie stattfindet.
*   `Reglement_Text`: Die spezifischen Regeln für die Wertung (Punktesystem, etc.).
*   `Teilnahmeberechtigung_Text`: Regeln, wer an der Serie teilnehmen darf.
*   `Qualifikationsbewerbe`: Eine Liste von Verweisen auf die `Bewerb-IDs`, deren Ergebnisse für diese Serie gewertet werden.

---

### 5. Entität: `Akteur`

**Zweck:** Zentrale, widerspruchsfreie Verwaltung aller beteiligten Personen und Organisationen, unabhängig von ihrer Rolle.

**Beispiele:** Ein Reiter, ein Pferdebesitzer, ein Züchter, ein Richter, ein Reitverein.

**Attribute:**
*   `Akteur-ID` (PK): Eindeutiger technischer Schlüssel.
*   `Typ`: `PERSON` oder `ORGANISATION`.
*   `Name`: Vollständiger Name der Person oder Organisation.
*   `Kontakt`: Adress- und Kontaktdaten.
*   `Rollen`: Eine Liste der Rollen, die dieser Akteur einnimmt (z.B. `REITER`, `PFERDEBESITZER`, `ZÜCHTER`, `FUNKTIONÄR`, `VERANSTALTER`).
*   `Identifikatoren`: Ein flexibler Speicher (z.B. JSON oder eigene Tabelle) für alle offiziellen Nummern:
  *   `OEPS-Lizenznummer`
  *   `FEI-ID`
  *   `Mitgliedsnummer_Zuchtverband`

---

### 6. Entität: `Pferd`

**Zweck:** Zentrale Verwaltung aller Pferde, egal ob im Sport oder in der Zucht, mit all ihren unterschiedlichen Identitäten.

**Beispiele:** Ein international erfolgreiches Sportpferd, eine Zuchtstute, ein junges Pferd in Ausbildung.

**Attribute:**
*   `Pferd-ID` (PK): Eindeutiger technischer Schlüssel.
*   `Name`: Offizieller Name des Pferdes.
*   `Abstammung_Vater_ID` (FK): Verweis auf ein anderes `Pferd` (Vater).
*   `Abstammung_Mutter_ID` (FK): Verweis auf ein anderes `Pferd` (Mutter).
*   `Besitzer_ID` (FK): Verweis auf den `Akteur`, dem das Pferd gehört.
*   `Identifikatoren`: Ein flexibler Speicher für alle offiziellen Nummern:
  *   `Lebensnummer` (aus der Zucht)
  *   `OEPS-Registrierungsnummer`
  *   `FEI-ID`
  *   `Chip-Nummer`
