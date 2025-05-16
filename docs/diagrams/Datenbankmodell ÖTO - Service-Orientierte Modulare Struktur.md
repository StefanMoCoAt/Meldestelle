# Datenbankmodell ÖTO für Meldestellen

**Stand:** 16. Mai 2025

## 1. Einleitung und Überblick

Dieses Datenbankmodell wurde entwickelt, um die Anforderungen der Österreichischen Turnierordnung (ÖTO) für den Einsatz in einer Meldestelle abzubilden. Das Ziel ist eine umfassende Datenstruktur, die sowohl die Verwaltung von reitsportlichen Veranstaltungen als auch die Integration von Stammdaten des Österreichischen Pferdesportverbands (OEPS) über die ZNS-Schnittstelle (Zentrales Nennservice) ermöglicht.

Der architektonische Ansatz ist modular und service-orientiert, um eine klare Trennung der Verantwortlichkeiten und eine gute Wartbarkeit zu gewährleisten. Das Modell ist so konzipiert, dass es als Grundlage für die Entwicklung verschiedener Software-Services dienen kann, die spezifische Aufgaben im Meldestellenkontext übernehmen.

Die Datenstrukturen für den ZNS-Datenaustausch basieren maßgeblich auf dem **OEPS Pflichtenheft 2021 Datentransfer OEPS – Meldestellen – OEPS, Version 2.4 vom 28.07.2021**.

## 2. Hochrangige Struktur (Service-Pakete)

Das Datenbankmodell ist in logische Service-Pakete unterteilt, die jeweils einen spezifischen Aufgabenbereich abdecken:

* **`Service_OeTO_Verwaltung`**:
    * **Verantwortlichkeit:** Zentralverwaltung von ÖTO-bezogenen Regeln, Definitionen und sportfachlichen Stammdaten. Dieser Service liefert die Grundlage für die Einhaltung von Regularien in anderen Services.
* **`Service_ZNS_Daten`**:
    * **Verantwortlichkeit:** Abbildung und Verwaltung der vom OEPS über die ZNS-Schnittstelle bereitgestellten Stammdaten (z.B. aus `LIZENZ01.dat`, `PFERDE01.dat`, `VEREIN01.dat`, `RICHT01.dat`). Dient als Basis für Personen-, Pferde- und Vereinsinformationen.
* **`Service_Veranstaltungsplanung`**:
    * **Verantwortlichkeit:** Planung, Strukturierung und Verwaltung von reitsportlichen Veranstaltungen, von übergeordneten Event-Rahmen bis hin zu detaillierten Prüfungen und deren spartenspezifischen Ausprägungen.
* **`Service_Nennungsabwicklung`**:
    * **Verantwortlichkeit:** Abwicklung des gesamten Nennungs- und Ergebnisprozesses, inklusive der Erfassung von Nennungen, der Startberechtigungsprüfung (konzeptionell) und der Erfassung und Aufbereitung von Ergebnissen.

Innerhalb der `Service_Veranstaltungsplanung` und `Service_Nennungsabwicklung` existieren zudem Unterpakete (`Sportfachliche_Details_Pruefung` bzw. `Sportfachliche_Details_Ergebnis`), die spartenspezifische Erweiterungen für Prüfungs- und Ergebnisdaten enthalten.

## 3. Detaillierte Beschreibung der Entitäten (Auswahl)

Im Folgenden werden die Kernentitäten innerhalb der jeweiligen Service-Pakete beschrieben.

### 3.1. Service_OeTO_Verwaltung

#### `OETORegelReferenz`
* **Zweck:** Speichert Verweise auf spezifische Paragraphen, Absätze oder Anhänge der Österreichischen Turnierordnung (ÖTO). Ermöglicht die Nachvollziehbarkeit von Datenmodellentscheidungen und Regelgrundlagen.
* **Wichtige Attribute:** `oeto_regel_referenz_id` (**PK**), `paragraph_nummer`, `kapitel_titel`, `oeto_version_datum`.

#### `QualifikationsTyp`
* **Zweck:** Definition verschiedener Qualifikationen für Personen (z.B. Richter, Parcoursbauer) mit Spartenzuordnung.
* **Wichtige Attribute:** `qual_typ_code` (**PK**, z.B. "DR-GP", "PB-S"), `bezeichnung`, `sparte`, `oeto_regel_ref_id` (**FK**).

#### `LizenzTyp_OEPS`
* **Zweck:** Definition der verschiedenen Lizenztypen gemäß OEPS-Systematik (z.B. R1, RS2, RD3N).
* **Wichtige Attribute:** `lizenz_typ_code` (**PK**), `bezeichnung`, `sparte`, `oeto_regel_ref_id` (**FK**).

#### `AltersklasseDefinition`
* **Zweck:** Definition von Altersklassen für Reiter und Pferde gemäß ÖTO und ZNS-Vorgaben.
* **Wichtige Attribute:** `altersklasse_code` (**PK**, z.B. "JG", "U18", "4J"), `bezeichnung`, `min_alter`, `max_alter`, `oeto_regel_ref_id` (**FK**).

#### `Sportfachliche_Stammdaten`
* **Zweck:** Zentrale Ablage für wiederverwendbare sportfachliche Definitionen, die nicht direkt Lizenz- oder Qualifikationstypen sind (z.B. Dressuraufgaben, Standard-Hindernistypen, Wertungsverfahren, Punktetabellen für RVK).
* **Wichtige Attribute:** `stammdatum_id` (**PK**), `typ` (zur Unterscheidung), `code`, `bezeichnung`, `sparte_zugehoerigkeit`.

### 3.2. Service_ZNS_Daten

Dieser Service bildet die Struktur der vom OEPS bereitgestellten `.dat`-Dateien ab.

#### `Verein_ZNS`
* **Zweck:** Speichert Vereinsinformationen gemäß `VEREIN01.dat`.
* **Wichtige Attribute:** `oeps_vereins_nr` (**PK**), `name`.

#### `Person_ZNS`
* **Zweck:** Zentrale Entität für Personen (Reiter, Richter, Parcoursbauer etc.), basierend auf `LIZENZ01.dat` und `RICHT01.dat`.
* **Wichtige Attribute:** `oeps_satz_nr_person` (**PK**), `familienname`, `vorname`, `geburtsdatum`, `geschlecht`, `nationalitaet_code`, `oeps_hauptverein_nr` (**FK** zu `Verein_ZNS`), `fei_id_person`, `ist_auf_sperrliste`.

#### `Person_hat_Lizenz_ZNS`
* **Zweck:** M:N-Beziehungstabelle, die abbildet, welche `Person_ZNS` welchen `LizenzTyp_OEPS` besitzt (basierend auf dem `LIZENZINFO`-Feld und den Hauptlizenzfeldern in `LIZENZ01.dat`).
* **Wichtige Attribute:** `oeps_satz_nr_person` (**PK, FK**), `lizenz_typ_code` (**PK, FK**), `bezahlt_im_jahr`.

#### `Person_hat_Qualifikation_ZNS`
* **Zweck:** M:N-Beziehungstabelle, die die Qualifikationen einer `Person_ZNS` (aus `RICHT01.dat`) mit den definierten `QualifikationsTypen` verknüpft.
* **Wichtige Attribute:** `oeps_satz_nr_person` (**PK, FK**), `qual_typ_code` (**PK, FK**).

#### `Pferd_ZNS`
* **Zweck:** Speichert Pferdeinformationen gemäß `PFERDE01.dat`.
* **Wichtige Attribute:** `oeps_satz_nr_pferd` (**PK**), `name`, `lebensnummer`, `geburtsjahr`, `geschlecht`, `farbe`, `abstammung_vater_name`, `oeps_verein_nr_pferd` (**FK** zu `Verein_ZNS`), `letzte_zahlung_pferdegebuehr_jahr`, `fei_pass_nr`.

### 3.3. Service_Veranstaltungsplanung

#### `VeranstaltungsRahmen`
* **Zweck:** Definiert die übergeordnete, konkrete Veranstaltung an einem Ort zu einer Zeit, die mehrere Turniere umfassen kann.
* **Wichtige Attribute:** `veranst_rahmen_id` (**PK**), `name`, `datum_von_gesamt`, `datum_bis_gesamt`, `hauptveranstalter_verein_nr` (**FK** zu `Verein_ZNS`).

#### `Turnier_OEPS`
* **Zweck:** Repräsentiert ein einzelnes, vom OEPS anerkanntes Turnier (Pferdesportliche Veranstaltung) innerhalb eines `VeranstaltungsRahmen`. Entspricht den Daten im A-Satz der OEPS-Dateien.
* **Wichtige Attribute:** `oeps_turnier_nr` (**PK**), `veranst_rahmen_id` (**FK**), `name_zusatz`, `datum_von_turnier`, `datum_bis_turnier`, `kategorie_text_turnier`, `turnierart_sparte`.

#### `Pruefung_OEPS` (Bewerb)
* **Zweck:** Definiert einen einzelnen Bewerb innerhalb eines `Turnier_OEPS`. Entspricht den Daten im B-Satz/BBEWERBE-Abschnitt.
* **Wichtige Attribute:** `pruefung_db_id` (**PK**), `oeps_turnier_nr` (**FK**), `oeps_bewerb_nr_display`, `name_text_pruefung`, `klasse_text`, `datum_pruefung`, `art_disziplin_haupt` (zur Steuerung spartenspezifischer Logik).

#### `Pruefung_Abteilung`
* **Zweck:** Definiert eine spezifische Abteilung innerhalb einer `Pruefung_OEPS`, da Ergebnisse und Nennungen oft pro Abteilung verwaltet werden (gemäß B-Satz im Pflichtenheft).
* **Wichtige Attribute:** `pruefung_abteilung_db_id` (**PK**), `pruefung_db_id` (**FK**), `oeps_abteilung_nr`, `bezeichnung_abteilung`, `anzahl_starter_abteilung_gemeldet`, `geldpreis_summe_abteilung`.

#### `Meisterschaft_Cup_Serie`
* **Zweck:** Abbildung von übergeordneten Wettbewerbsformaten wie Landesmeisterschaften, Cups oder Turnierserien, die sich über mehrere Turniere oder spezifische Prüfungen erstrecken können.
* **Wichtige Attribute:** `mcs_id` (**PK**), `name`, `typ`, `jahr`, `sparte`.

#### `MCS_Wertungspruefung`
* **Zweck:** M:N-Beziehungstabelle, die festlegt, welche `Pruefung_Abteilung` für eine `Meisterschaft_Cup_Serie` als Wertungsprüfung zählt.
* **Wichtige Attribute:** `mcs_id` (**PK, FK**), `pruefung_abteilung_db_id` (**PK, FK**), `faktor_fuer_wertung`.

#### Unterpaket `Sportfachliche_Details_Pruefung`
Enthält Entitäten zur Spezifizierung von Prüfungsdetails für einzelne Sparten:
* **`DressurPruefungSpezifika`**: Details wie Aufgabe, Platzgröße.
* **`SpringenPruefungSpezifika`**: Details wie Parcoursdesigner, Hindernisanzahl, Höhe, Wertungsverfahren.
* **`VielseitigkeitPruefungSpezifika`**: Details zu den Teilprüfungen Dressur, Gelände, Springen.
* **`ReitervierkampfPruefungSpezifika`**: Details zu den Teilprüfungen Dressur, Springen, Laufen, Schwimmen.
  Diese Entitäten sind 1:1 mit `Pruefung_OEPS` verknüpft und referenzieren ggf. `Sportfachliche_Stammdaten` aus dem `Service_OeTO_Verwaltung`.

### 3.4. Service_Nennungsabwicklung

#### `Nennung_OEPS`
* **Zweck:** Speichert eine Nennung eines Reiter-Pferd-Paares für eine spezifische `Pruefung_Abteilung`. Basiert auf dem KKARTEI-Satz der `n2-*.dat` Datei.
* **Wichtige Attribute:** `nennung_db_id` (**PK**), `pruefung_abteilung_db_id` (**FK**), `oeps_satz_nr_reiter` (**FK**), `oeps_satz_nr_pferd` (**FK**), `genutzte_lizenz_person_satz_nr` (**FK**), `genutzte_lizenz_typ_code` (**FK**), `nennungs_zeitpunkt`, `status_nennung`.

#### `Ergebnis_OEPS_Zeile`
* **Zweck:** Speichert die Ergebniszeile für eine Teilnahme, basierend auf dem D-Satz der `*.erg` Datei.
* **Wichtige Attribute:** `ergebnis_zeile_db_id` (**PK**), `nennung_db_id` (**FK** empfohlen), `pruefung_abteilung_db_id` (**FK**), `platz`, `punkte_wertnote_text_ergebnis`, `zeit_prozent_text_ergebnis`, `geldpreis_betrag_ergebnis`, `nation_code_fuer_ergebnis`.

#### Unterpaket `Sportfachliche_Details_Ergebnis`
Enthält Entitäten zur Spezifizierung von Ergebnisdetails für einzelne Sparten:
* **`DressurErgebnisSpezifika`**: Gesamtwertnote, Prozent; kann um Lektionsbewertungen erweitert werden.
* **`SpringenErgebnisSpezifika`**: Stilnote; kann um `SpringenUmlaufErgebnis` (Fehler/Zeit pro Umlauf/Stechen) erweitert werden.
* **`VielseitigkeitErgebnisSpezifika`**: Minuspunkte aus den einzelnen Teilprüfungen, Gesamtminuspunktzahl.
* **`ReitervierkampfErgebnisSpezifika`**: Punkte und Rohleistungen aus den vier Teilprüfungen, Gesamtpunktzahl.
  Diese Entitäten sind 1:1 mit `Ergebnis_OEPS_Zeile` verknüpft.

## 4. Veranstaltungshierarchie

Die Abwicklung von Pferdesportveranstaltungen folgt einer klaren Hierarchie, die im Modell abgebildet wird:

1.  **`VeranstaltungsRahmen`**: Die oberste Ebene, die eine komplette Veranstaltung an einem Ort und Zeitraum definiert (z.B. "Pfingstturnier Sudenhof").
2.  **`Turnier_OEPS`**: Einem `VeranstaltungsRahmen` können ein oder mehrere offizielle OEPS-Turniere zugeordnet sein (z.B. ein CDN-A und ein CSN-B am selben Wochenende unter einem `VeranstaltungsRahmen`). Jedes `Turnier_OEPS` hat eine eigene OEPS-Turniernummer.
3.  **`Pruefung_OEPS`**: Jedes `Turnier_OEPS` besteht aus mehreren Bewerben (Prüfungen), die im System als `Pruefung_OEPS` erfasst werden und eine OEPS-Bewerbsnummer tragen.
4.  **`Pruefung_Abteilung`**: Ein `Pruefung_OEPS` kann in eine oder mehrere Abteilungen unterteilt sein, für die separate Nennungen und Ergebnislisten geführt werden können.

Parallel dazu existiert die Entität **`Meisterschaft_Cup_Serie`**, die es erlaubt, Turniere oder spezifische Prüfungsabteilungen übergeordneten Wettbewerben (wie Landesmeisterschaften oder Cups) zuzuordnen. Die Zuordnung erfolgt über die Zwischentabelle `MCS_Wertungspruefung`.

## 5. Spartenspezifische Details

Für die vier Hauptsparten – Dressur, Springen, Vielseitigkeit und Reitervierkampf – sind exemplarisch spezifische Entitäten zur Detaillierung von Prüfungsanforderungen und Ergebnisstrukturen vorgesehen. Diese befinden sich in den Unterpaketen `Sportfachliche_Details_Pruefung` (unter `Service_Veranstaltungsplanung`) und `Sportfachliche_Details_Ergebnis` (unter `Service_Nennungsabwicklung`). Diese Entitäten sind stets mit einer Kern-Prüfung (`Pruefung_OEPS`) bzw. einer Kern-Ergebniszeile (`Ergebnis_OEPS_Zeile`) verknüpft und erweitern diese um disziplinspezifische Attribute.

## 6. Beziehungen

Alle Beziehungen zwischen den Entitäten, insbesondere die paketübergreifenden Verknüpfungen, sind im PlantUML-Diagramm am Ende des Skripts explizit definiert, um Klarheit und korrekte Verarbeitung sicherzustellen. (Das PlantUML-Diagramm selbst dient hier als visuelle Referenz für die Beziehungen).

## 7. Allgemeine Hinweise und Ausblick

* Dieses Datenbankmodell stellt einen umfassenden Entwurf dar, der als solide Grundlage für die Entwicklung einer Meldestellen-Software dient.
* Die Detailtiefe, insbesondere bei Attributen und komplexen Geschäftsregeln (z.B. exakte Logik der Startberechtigungsprüfung, Gebührenberechnung, Generierung von spezifischen Berichten), kann und muss in weiteren Schritten verfeinert werden.
* Die Normalisierung von Daten, die in den ZNS-Dateien als Textfelder oder kommaseparierte Listen vorliegen (z.B. Qualifikationen), wurde teilweise durch Zwischentabellen angedeutet und ist ein wichtiger Aspekt für die Datenbankintegrität.
* Die Pflege der ÖTO-Referenzen und der sportfachlichen Stammdaten ist für die Aktualität und Korrektheit des Systems entscheidend.
