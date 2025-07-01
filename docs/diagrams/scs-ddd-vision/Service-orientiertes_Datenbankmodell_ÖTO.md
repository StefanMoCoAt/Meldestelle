# Dokumentation: Service-orientiertes Datenbankmodell ÖTO

**Stand:** 02. Juli 2025

## 1. Einleitung und Überblick

Dieses Dokument beschreibt die Architektur und das Datenmodell für eine moderne, service-orientierte Meldestellen-Software. Der Entwurf basiert auf den Prinzipien des **Domain-Driven Design (DDD)** und einer Architektur von **Self-Contained Systems (SCS)**.

Das Ziel ist ein robustes, wartbares und erweiterbares System, das die komplexen Anforderungen der Österreichischen Turnierordnung (ÖTO) abbildet und die Datenflüsse des OEPS (Zentrales Nennservice, ZNS) sauber integriert. Die Datenstrukturen basieren maßgeblich auf dem **OEPS Pflichtenheft 2021 (Version 2.4 vom 28.07.2021)**.

## 2. Systemarchitektur: Bounded Contexts

Das System ist in fachliche, voneinander abgegrenzte Domänen, sogenannte **Bounded Contexts**, aufgeteilt. Jeder Context hat eine klare Verantwortung und ein eigenes, darauf optimiertes Datenmodell. Die Kommunikation zwischen den Contexts erfolgt über klar definierte Schnittstellen (APIs) und asynchrone Domänen-Events.

Die Kern-Contexte sind:

* **`ZNS_Import_ACL`**: Ein "Anti-Corruption Layer", der als Pufferzone und Übersetzer für die externen ZNS-Daten dient.
* **`Personen_und_Vereine_Context`**: Die Quelle der Wahrheit für alle Stammdaten von Personen und Vereinen.
* **`Lizenzen_und_Qualifikationen_Context`**: Verwaltet die Startberechtigungen (Lizenzen, Qualifikationen) von Personen.
* **`Veranstaltungs_Context`**: Dient der Planung und Definition von Veranstaltungen, Turnieren und Bewerben.
* **`Nennungs_Context`**: Wickelt den gesamten Nennungsprozess bis zur Erstellung der Startliste ab.
* **`Ergebnis_Context`**: Ist für die Erfassung, Berechnung und Veröffentlichung der Ergebnisse zuständig.

*(In einer vollständigen Dokumentation würde hier eine visuelle Context Map die Beziehungen zwischen diesen Services darstellen.)*

---

## 3. Detaillierte Modelle der Bounded Contexts

### 3.1. ZNS-Import als Anti-Corruption Layer (ACL)
* **Verantwortung:** Schutz des internen Domänenmodells vor den Details der externen ZNS-Schnittstelle. Dieser Context liest die `.dat`-Dateien ein, validiert und übersetzt sie in eine saubere, strukturierte Form für die anderen Services.
* **Aggregate Roots:** Dieser Context hat keine eigenen Aggregate, da er primär ein prozeduraler Übersetzer ist.
* **Kernentitäten:**
    * [cite_start]**`ZNS_*_dat_Satz`**: Eine Reihe von Entitäten (`ZNS_LIZENZ01_dat_Satz`, `ZNS_PFERDE01_dat_Satz` etc.), die exakte Abbilder der Zeilen aus den jeweiligen OEPS-Dateien sind[cite: 20]. Sie speichern die Rohdaten.
    * **`ZNS_Daten_Uebersetzer`**: Eine konzeptionelle Komponente, die die Logik zur Transformation der Rohdaten in Domänen-Events oder Objekte für die internen Kontexte kapselt.

### 3.2. Personen_und_Vereine_Context
* **Verantwortung:** Die zentrale Verwaltung der Stammdaten aller Akteure (Personen, Vereine).
* **Aggregate Roots:** `Person`, `Verein`.
* **Kernentitäten:**
    * **`Person`**: Bündelt alle persönlichen Daten. [cite_start]Der Primärschlüssel `oepsSatzNrPerson` ist die 6-stellige Satznummer aus den ZNS-Dateien[cite: 149]. Enthält Value Objects wie `PersonenNameVO` und `AdresseVO` sowie eine Liste von `Mitgliedschaft`-Entitäten.
    * **`Verein`**: Verwaltet Vereinsstammdaten. [cite_start]Der Primärschlüssel `oepsVereinsNr` ist die 4-stellige Nummer aus `VEREIN01.DAT`[cite: 189].
    * **`Mitgliedschaft`**: Eine Entität innerhalb des `Person`-Aggregates, die die Beziehung einer Person zu einem `Verein` beschreibt.

### 3.3. Lizenzen_und_Qualifikationen_Context
* **Verantwortung:** Verwaltung der Startberechtigungen und offiziellen Qualifikationen einer Person.
* **Aggregate Roots:** `Lizenznehmer`.
* **Kernentitäten:**
    * **`Lizenznehmer`**: Repräsentiert eine Person im Kontext von Berechtigungen. Wird durch die `oepsSatzNrPerson` identifiziert. Bündelt alle `Lizenz`- und `Qualifikation`-Objekte einer Person und stellt deren Konsistenz sicher.
    * **`Lizenz`**: Eine konkrete Lizenzinstanz einer Person, die sich auf einen `LizenzTyp_OEPS` (definiert im OeTO-Verwaltungs-Context) bezieht. [cite_start]Daten wie `bezahltImJahr` werden aus dem Feld `LIZENZINFO` der `LIZENZ01.dat` abgeleitet[cite: 181].
    * **`Qualifikation`**: Eine konkrete Qualifikation (z.B. als Richter), die sich auf einen `QualifikationsTyp` bezieht. [cite_start]Die Daten stammen aus der Interpretation des `QUALIFIKATIONEN`-Feldes der `RICHT01.dat`[cite: 166].

### 3.4. Veranstaltungs_Context
* **Verantwortung:** Detaillierte Planung und Definition aller reitsportlichen Veranstaltungen.
* **Aggregate Roots:** `VeranstaltungsRahmen`, `Turnier_OEPS`, `Meisterschaft_Cup_Serie`.
* **Veranstaltungshierarchie:**
    1.  **`VeranstaltungsRahmen`**: Die oberste Ebene, eine konkrete Veranstaltung an Ort und Zeit (z.B. "Reitertage 2025").
    2.  [cite_start]**`Turnier_OEPS`**: Ein offizielles OEPS-Turnier innerhalb des Rahmens, identifiziert durch die 5-stellige `oepsTurnierNr` aus dem A-Satz[cite: 144, 193].
    3.  [cite_start]**`Pruefung_OEPS` (Bewerb)**: Ein Bewerb innerhalb eines Turniers, identifiziert durch die 3-stellige `oepsBewerbNr` aus dem B-Satz[cite: 100, 151].
    4.  **`Pruefung_Abteilung`**: Eine Unterteilung einer Prüfung, für die separat genannt und gewertet werden kann. [cite_start]Gemäß Pflichtenheft ist für jede Abteilung ein eigener B-Satz zu stellen[cite: 197].
* **Weitere Entitäten:**
    * **`Meisterschaft_Cup_Serie`**: Ein Aggregat zur Verwaltung von übergreifenden Wettbewerben.
    * **`MCS_Wertungspruefung`**: Verknüpft Meisterschaften/Cups mit den spezifischen Prüfungsabteilungen, die als Wertungsprüfungen zählen.
    * **`PruefungsAnforderungen`**: Definiert die Startberechtigungen (Lizenzen, Altersklassen) für eine Prüfung oder Abteilung.
    * **Spartenspezifische Erweiterungen (`...Spezifika`)**: Erweitern `Pruefung_OEPS` um disziplinspezifische Details.

### 3.5. Nennungs_Context
* **Verantwortung:** Abwicklung des gesamten Nennungsprozesses von der Abgabe bis zur finalen Startliste.
* **Aggregate Roots:** `Nennung`, `Startliste`.
* **Kernentitäten:**
    * **`Nennung`**: Eine unteilbare Transaktion, die ein Reiter-Pferd-Paar für eine `Pruefung_Abteilung` anmeldet. Verwendet Value Objects als **Snapshots**, um die Daten zum Zeitpunkt der Nennung historisch korrekt zu speichern. [cite_start]Attribute wie `ersatzreiter`, `accontoBetrag` oder `istStallReserviert` sind direkt auf den `KKARTEI`-Satz des Pflichtenhefts zurückzuführen[cite: 160].
    * **`Startliste`**: Ein eigenständiges Aggregat, das die offizielle, geordnete Startreihenfolge für eine `Pruefung_Abteilung` verwaltet. Es stellt die Konsistenz der Startnummern sicher.
    * **`Starter`**: Eine Entität innerhalb des `Startliste`-Aggregates; repräsentiert eine Zeile auf der Startliste mit `startnummer`, optionaler `startzeit` und einem Snapshot der Nennungsdaten.

### 3.6. Ergebnis_Context
* **Verantwortung:** Erfassung, Berechnung, Platzierung und Veröffentlichung der Ergebnisse.
* **Aggregate Roots:** `Bewerbsergebnis`.
* **Kernentitäten:**
    * **`Bewerbsergebnis`**: Bündelt alle `Einzelergebnis`-Objekte einer `Pruefung_Abteilung`. Seine Hauptaufgabe ist die Berechnung der finalen, konsistenten `Rangliste`. [cite_start]Es speichert auch die Referenzen auf die eingesetzten Richter und Funktionäre gemäß C-Satz[cite: 201].
    * **`Einzelergebnis`**: Repräsentiert das Ergebnis eines einzelnen Starters. [cite_start]Die Attribute (`platz`, `ausschluss_disq_code`, `geldpreis` etc.) sind direkt auf den **D-Satz** des Pflichtenhefts abgebildet[cite: 210].
    * **`LeistungVO` (polymorph)**: Ein flexibles Value Object, das die je nach Sparte unterschiedlichen Leistungsdaten (z.B. Wertnote in der Dressur, Fehler/Zeit im Springen) kapselt. [cite_start]Die Daten stammen aus den Feldern `PUNKTE/WERTNOTE` und `ZEIT/PROZENT` im D-Satz[cite: 210].

---

## 4. Prozess- und Interaktions-Beispiele

Die Stärke dieses Designs liegt im Zusammenspiel der entkoppelten Services. Wir haben zwei zentrale Prozesse modelliert:

* **Nennung wird abgegeben:**
    * Ein **Command** `NennungAbgeben` wird an den `Nennungs_Context` gesendet.
    * Dieser validiert die Anfrage durch synchrone **Queries** an den `Veranstaltungs_Context` und den `Lizenzen_und_Qualifikationen_Context`.
    * Nach erfolgreicher interner Speicherung wird ein asynchrones **Event** `NennungWurdeEingereicht` veröffentlicht, auf das z.B. ein Benachrichtigungs-Service reagieren kann.

* **Startliste wird erstellt:**
    * Ein **Command** `ErstelleStartliste` von der **Meldestelle** an den `Nennungs_Context` stößt den Prozess an.
    * Der Context sammelt alle akzeptierten Nennungen, wendet die Sortier-/Loslogik an und erstellt das `Startliste`-Aggregat.
    * Ein asynchrones **Event** `StartlisteWurdeFinalisiert` wird veröffentlicht.
    * Der `Ergebnis_Context` abonniert dieses Event und bereitet sich proaktiv auf die Ergebniserfassung vor, indem er für jeden Starter einen leeren `Einzelergebnis`-Datensatz anlegt.

## 5. Schlussbemerkung und Ausblick

Dieses Dokument fasst ein umfassendes, service-orientiertes Domänenmodell zusammen, das als Blaupause für die Entwicklung einer modernen, robusten und erweiterbaren Meldestellen-Software dient.

Die Architektur mit klar definierten Bounded Contexts, Aggregates und der ereignisgesteuerten Kommunikation ermöglicht es, die hohe fachliche Komplexität des Turniersports beherrschbar zu machen.

Die nächsten Schritte in einem realen Projekt wären:
* Die weitere Detaillierung der Attribute und Methoden in jedem Context.
* Die genaue Definition der APIs (Commands, Queries) und Event-Strukturen.
* Die Modellierung weiterer unterstützender Kontexte (z.B. für die Bezahlungsabwicklung oder das Berichtswesen).
