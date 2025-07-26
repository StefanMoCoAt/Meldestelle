# Meldestelle_Pro: Strategische Roadmap & finales Zieldomänen-Modell

**Datum:** 26. Juli 2025
**Autor:** Programmier-Meister (in Abstimmung mit Stefan-Mo)

Dieses Dokument fasst die strategische Entwicklungs-Roadmap und das finale, detaillierte Domänen-Modell für das Projekt "Meldestelle_Pro" zusammen. Es dient als zentrale Blaupause für die gesamte weitere Entwicklung.

---

## Teil 1: Strategische Entwicklungs-Roadmap

Wir verfolgen einen agilen, iterativen Ansatz. Jeder Zyklus liefert ein funktionierendes, in der Praxis testbares Produkt ("Minimum Viable Product" - MVP), das wir basierend auf Feedback schrittweise erweitern.

### Zyklus 1: MVP für C/C-Neu Turniere (Dressur & Springen)

* **Ziel:** Ein voll funktionsfähiges End-to-End-System für den am weitesten verbreiteten Turniertyp, um schnelles Feedback aus "Feld-Versuchen" zu erhalten.
* **Kern-Features pro Domäne:**
    * **OeTO-Verwaltung (Masterdata):** Implementierung der Regeln für Klassen E-LM, einfache Lizenztypen (Reiterpass, R1) und Turnierkategorien C/C-Neu.
    * **ZNS-Import (ACL):** Implementierung des Imports der `.dat`-Dateien (`LIZENZ01`, `PFERDE01`, `VEREIN01`) zur Befüllung der Stammdaten.
    * **Sportler, Pferde & Lizenzen:** Grundlegende Verwaltung der importierten `DomPerson`, `DomPferd` und `DomLizenz` Entitäten.
    * **Veranstaltungsplanung (Events):** Der "Event-Setup-Wizard" wird auf die Erstellung von C/C-Neu Turnieren mit dem dreistufigen Modell (`Dach-Veranstaltung` -> `Turnier-Mandat` -> `Bewerb`) beschränkt.
    * **Nennungsabwicklung:** Implementierung der Online-Nennung, Validierung für die in C-Turnieren relevanten Lizenzen und Erstellung von `Startlisten`.
    * **Abrechnung & Finanzen:** Verbuchung von `Nenngeld` und `Startgeld`. Preisgelder und komplexe Gebühren sind vorerst ausgenommen.
    * **Ergebnisdienst:** Manuelle Eingabe für "gemeinsames Richten" (Dressur) und "Standardspringen" (Springen). Finaler Export der Ergebnisse im dualen Format (`.erg` und `.erg.xml`).

### Zyklus 2: Erweiterung für B/A-Turniere & Professionalisierung

* **Ziel:** Abbildung der komplexeren Regeln höherer Turnierkategorien und Automatisierung von Prozessen.
* **Kern-Features pro Domäne:**
    * **Masterdata:** Erweiterung um Regeln für Klassen M und S, komplexere Lizenztypen (R2, R3, etc.) und deren Höherreihungs-Logik.
    * **Springreiten-Bewertung:** Anbindung externer Zeitmessgeräte über die "Hardware-Adapter-Schicht".
    * **Dressur-Bewertung:** Implementierung des "getrennten Richtens" und der Erfassung von Einzelnoten pro Richter.
    * **Abrechnung & Finanzen:** Implementierung der korrekten Preisgeldberechnung und -aufteilung gemäß ÖTO.
    * **Client-App:** Entwicklung des "Live-Turnier-Cockpits" für die Meldestelle.

### Zyklus 3 & darüber hinaus: Ökosystem & Wachstum

* **Ziel:** Das System um strategische Module erweitern, die über die reine Turnierabwicklung hinausgehen.
* **Kern-Features pro Domäne:**
    * **Serien-Verwaltung:** Implementierung des `championship-service` zur Verwaltung von Cups und Meisterschaften, die sich über mehrere Turniere erstrecken.
    * **Parcours-Design-Modul:** Entwicklung des visuellen Editors als "Freemium"-Tool, um Parcours-Bauer als neue Nutzergruppe zu gewinnen.
    * **Erweiterung der Sparten:** Schrittweise Implementierung der spezifischen Logiken für weitere Disziplinen wie Vielseitigkeit, Fahren etc.

---

## Teil 2: Detailliertes Zieldomänen-Modell (DDD Context Map)

Dieses Modell ist die finale Blaupause unserer Architektur. Es integriert die Struktur Ihrer `.puml`-Diagramme in unsere service-orientierte Landschaft.

### BC1: OeTO-Verwaltung (Masterdata-Service)
* **Kern-Verantwortung:** Definiert alle Regelwerke, Typen und Klassifikationen.
* **Schlüssel-Konzepte:** `LizenzTypGlobal`, `QualifikationsTyp`, `BewerbsKategorieOetoDefinition`, `Sportfachliche_Stammdaten` (z.B. Dressuraufgaben, Richtverfahren).

### BC2: ZNS-Import (Anti-Corruption-Layer)
* **Kern-Verantwortung:** Isoliert das System von den OEPS-Rohdatenformaten. Liest `.dat`-Dateien und publiziert saubere Domänen-Events.
* **Schlüssel-Konzepte:** `ZNS_Daten_Uebersetzer`, `ZNS_LIZENZ01_dat_Satz`, etc..

### BC3: Sportler, Pferde & Vereine (z.B. `members-` & `horses-service`)
* **Kern-Verantwortung:** Hält die Stammdaten der Akteure.
* **Schlüssel-Aggregate:** `DomPerson`, `DomPferd`, `DomVerein`.

### BC4: Lizenzen & Qualifikationen (`licensing-service`)
* **Kern-Verantwortung:** Verwaltet die Berechtigungen von Personen.
* **Schlüssel-Aggregate:** `Lizenznehmer` (referenziert `DomPerson`). Enthält Listen von `DomLizenz` und `DomQualifikation`.

### BC5: Veranstaltungsplanung (`events-service`)
* **Kern-Verantwortung:** Modelliert die komplette Struktur von Veranstaltungen.
* **Schlüssel-Aggregate & Entitäten:** `DachVeranstaltung` -> `TurnierMandat` -> `Bewerb` -> `Abteilung`. Nutzt spartenspezifische Detail-Entitäten wie `SpringBewerbDetails`.

### BC6: Mandanten- & Lizenz-Verwaltung (`tenancy-service`)
* **Kern-Verantwortung:** Steuert das Geschäftsmodell und den Software-Zugriff.
* **Schlüssel-Aggregate:** `Mandant` (referenziert `DomVerein`), `Lizenz`.

### BC7: Nennungsabwicklung (`nennungs-service`)
* **Kern-Verantwortung:** Orchestriert den Nennprozess.
* **Schlüssel-Aggregate:** `Nennung`, `Startliste`.
* **Wichtiges Entwurfs-Muster:** **Daten-Snapshots**. Die `Nennung` speichert beim Erstellen eine Kopie der relevanten Daten (z.B. in `ReiterReferenzVO`, `PferdeReferenzVO`), um historische Korrektheit zu garantieren.

### BC8: Abrechnung & Finanzen (`billing-service`)
* **Kern-Verantwortung:** Garantiert eine strikt getrennte Kassenführung und zentrale Gebührenberechnung.
* **Schlüssel-Aggregate & Entitäten:** `TurnierKonto` (pro `TurnierMandat`), `Gebuehrenposten`.

### BC9: Ergebnisdienst (`result-service`)
* **Kern-Verantwortung:** Erfasst, berechnet und exportiert Ergebnisse.
* **Schlüssel-Aggregate:** `Bewerbsergebnis` (pro `Abteilung`).
* **Wichtiges Entwurfs-Muster:** **Polymorphe `LeistungVO`**. Nutzt spezifische Value Objects (`DressurLeistungVO`, `SpringenLeistungVO`) zur Abbildung der unterschiedlichen Ergebnisstrukturen pro Sparte.

### BC10: Serien-Verwaltung (`championship-service`)
* **Kern-Verantwortung:** Verwaltet übergreifende Cups und Meisterschaften.
* **Schlüssel-Aggregate:** `Serie`, die auf `Bewerbe` aus der `Events-Domäne` verweist.
