# Struktur einer Turnier-Ausschreibung gemäß ÖTO

Diese Dokumentation beschreibt die notwendigen Felder und Sektionen einer offiziellen Ausschreibung für den österreichischen Pferdesport (OEPS).

## 1. Allgemeine Turnierdaten (Header)
Dies entspricht im Wesentlichen dem **A-Satz** der ZNS-Schnittstelle.

* **Turniernummer:** Eindeutige vom OEPS vergebene Kennung (z.B. 25123).
* **Veranstalter:** Name des durchführenden Vereins und dessen Vereinsnummer.
* **Austragungsort:** Genaue Adresse der Reitanlage.
* **Datum:** Von-bis Datum des Turniers.
* **Turnierkategorie:** Einstufung (z.B. CSN-B, CDN-A, CCN-C).
* **Nennschluss:** Datum, bis zu dem reguläre Nennungen möglich sind.

## 2. Besondere Bestimmungen (Rechtlicher Rahmen)
* **Regelwerk:** Hinweis auf die gültige ÖTO (Österreichische Turnierordnung) und ggf. FEI-Regeln.
* **Haftungsausschluss:** Verweis auf die Haftungsbestimmungen der ÖTO.
* **Teilnahmeberechtigung:** Allgemeine Einschränkungen (z.B. nur für Mitglieder bestimmter Landesverbände oder geladene Gäste).

## 3. Funktionäre (Offizielle)
Wichtig für die Zuweisung im **C-Satz**.

* **Turnierleiter:** Verantwortliche Person des Veranstalters.
* **Richterkollegium:** Liste der Richter inkl. deren Qualifikationen (z.B. "D-GP", "S").
* **Technischer Delegierter (TD):** (Vor allem bei Vielseitigkeit).
* **Parcourschef:** Verantwortlich für das Design der Hindernisse.
* **Turniertierarzt & Schmied:** Notwendige medizinische Versorgung.

## 4. Beschaffenheit der Anlage
Informationen, die Einfluss auf das **DressurPruefungSpezifika** oder **SpringenPruefungSpezifika** haben.

* **Austragungsplatz:** Maße (z.B. 20x60m), Bodenbelag (Sand, Gras).
* **Vorbereitungsplatz:** (Abreiteplatz) Maße und Bodenbelag.

## 5. Nennungen & Gebühren
Grundlage für den **Nennungs_Context**.

* **Nennweg:** Hinweis auf das ZNS (Zentrales Nennsystem).
* **Nenngebühr:** Grundgebühr pro Pferd/Reiter-Paar.
* **Startgebühr:** Gebühr pro einzelner Prüfung.
* **Boxen/Einstreu:** Kosten für fixe oder mobile Boxen, inkl. Erst-Einstreu.
* **Zusatzgebühren:** Stromanschluss, Camping, Nachnenngebühren.

## 6. Prüfungs-Programm (Bewerbe)
Dies ist das Herzstück und bildet den **B-Satz** ab. Jede Prüfung muss folgende Details aufweisen:

### Pflichtfelder pro Prüfung:
| Feld | Beschreibung | Beispiel |
| :--- | :--- | :--- |
| **Bewerbsnummer** | Fortlaufende Nummer | 01, 02, ... |
| **Bezeichnung** | Name der Prüfung | Standardspringprüfung |
| **Klasse** | Schwierigkeitsgrad | E, A, L, LM, M, S |
| **Abteilungen** | Unterteilung nach Lizenzen | Abt. 1: R1 / Abt. 2: R2 u. höher |
| **Aufgabe** | (Nur Dressur) Spezifische ÖTO-Aufgabe | A2, L1, FEI Grand Prix |
| **Anforderungen** | Erforderliche Lizenzen/Alter | R1 oder höher |
| **Richtverfahren** | Verweis auf ÖTO-Paragraphen | § 218, § 204 |
| **Dotierung** | Preisgeld-Aufstellung | EUR 500,- (150/100/80/...) |

## 7. Stallungen & Unterbringung
* Anreise- und Abreisezeiten.
* Verfügbarkeit von Futter und Einstreu.
* Veterinäramtliche Bestimmungen (Impfschutz-Kontrolle gemäß ÖTO).

## 8. Vorläufige Zeiteinteilung
* Grober Ablaufplan (welcher Tag, welche Prüfungen).
* Hinweis auf die endgültige Zeiteinteilung (meist am Vorabend im Nennungs-System).
