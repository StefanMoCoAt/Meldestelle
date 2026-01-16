# Analyse der Legacy-Spezifikation (OEPS Pflichtenheft 2021 V2.4)

*   **Datum:** 2026-01-14
*   **Quelle:** `docs/03_Domain/02_Reference/Legacy_Specs/OETO-2026_Meldestelle_Pflichtenheft_V2.4_2021-07-28.md`
*   **Status:** Draft

## 1. Zusammenfassung

Das Pflichtenheft definiert das Datenaustauschformat zwischen Meldestellen-Software und dem OEPS (Österreichischer Pferdesportverband). Es ist die **maßgebliche technische Referenz** für nationale Turniere in Österreich. Die Einhaltung dieser Spezifikation ist zwingend erforderlich, um Ergebnisse an den Verband zu melden.

## 2. Kritische Datenfelder & Identifikatoren

Die Analyse zeigt, dass das aktuelle `Core_Model` (`Overview.md`) zu generisch ist. Für den operativen Betrieb fehlen essenzielle Identifikatoren.

### 2.1. Identifikation von Personen & Pferden
Das System verlässt sich nicht primär auf Namen, sondern auf **Satznummern**.

*   **Pferd:**
    *   `Satznummer` (10-stellig, numerisch): Der primäre Key im OEPS-System. Muss zwingend persistiert und exportiert werden.
    *   `Kopfnummer` (4-stellig, alphanumerisch):
        *   National: Die permanente Registrierungsnummer beim OEPS.
        *   International: Eine turnierspezifische Startnummer.
    *   `Lebensnummer` (9-stellig): Zuchtnummer. Achtung: Bei ausländischen Pferden oft generiert/fiktiv -> Nicht zur Suche geeignet!
    *   `FEI-Pass` vs. `FEI-ID`: Zwei getrennte Felder!

*   **Reiter:**
    *   `Satznummer` (6-stellig, numerisch): Der primäre Key.
    *   `Lizenz` (z.B. "RD1", "R1"): Bestimmt die Startberechtigung in Klassen.
    *   `Startkarte`: Flag, ob die Jahresgebühr bezahlt wurde. Ohne Startkarte keine Startberechtigung (außer Gastlizenzen).

### 2.2. Turnier & Bewerbsstruktur
Die Struktur ist starrer als im aktuellen Modell angenommen.

*   **Turniernummer:** 5-stellig.
*   **Bewerbe:**
    *   Haben eine 2-stellige Nummer (intern) UND eine 3-stellige Nummer (für Turniere > 99 Bewerbe).
    *   **Abteilungen:** Ein Bewerb kann in Abteilungen (Abt. 1, Abt. 2...) unterteilt sein. Dies ist keine rein organisatorische Trennung, sondern datentechnisch relevant (Feld `ABTEILUNG`).

## 3. Implizite Geschäftsregeln

Aus den Datenfeldern lassen sich harte Business Rules ableiten:

1.  **Startberechtigung (Sperrliste):** Es gibt ein Flag `SPERRLISTE`. Wenn gesetzt, muss das System warnen/blockieren. Grund oft: Offene Forderungen.
2.  **Nation-Logik (Gast vs. Inländer):**
    *   Ausländer mit österr. Lizenz + bezahlter Startkarte -> Startet für österr. Verein -> Nation im Ergebnis = "AUT".
    *   Ausländer ohne Mitgliedschaft -> Gastreiter -> Nation = Staatsbürgerschaft (z.B. "GER").
    *   *Konsequenz:* Die "Nation" eines Starts ist kontextabhängig und nicht rein statisch am Reiter hängend.
3.  **Pferde-Status:** Pferde, für die >3 Jahre keine Gebühr bezahlt wurde, gelten als "nicht registriert" -> Neuanlage erforderlich.

## 4. Lücken im aktuellen Modell (Gap Analysis)

| Entität | Fehlendes Attribut / Konzept | Dringlichkeit |
| :--- | :--- | :--- |
| **Pferd** | `Satznummer` (OEPS-ID) | **Hoch** (Sync unmöglich ohne dies) |
| **Pferd** | Unterscheidung `Kopfnummer` (Permanent) vs. `Startnummer` (Turnier) | Mittel |
| **Akteur** | `Satznummer` (OEPS-ID) | **Hoch** |
| **Akteur** | `Startkarte` (Status) | Hoch (Validierung) |
| **Bewerb** | `Abteilung` (Sub-Struktur) | Mittel |
| **Ergebnis** | `Ausschluss-Typ` (Disqualifikation, Aufgabe, Ausschluss) | Mittel |
| **Ergebnis** | `Geldpreis` (Formatierung, Währung ist implizit EUR) | Niedrig |

## 5. Empfehlung für das Datenmodell

Wir müssen die Entität `Akteur` in spezifische Rollen-Modelle ausdifferenzieren oder per Composition erweitern, da die Attribute für Reiter (Lizenz, Startkarte) für andere Akteure (Richter, Besitzer) irrelevant oder anders sind.

**Vorschlag:**
*   `Akteur` bleibt Basis (Name, Kontakt).
*   `ReiterProfile` (Value Object / 1:1 Relation): Enthält `Satznummer`, `Lizenz`, `Startkarte`, `Sperrvermerk`.
*   `Pferd` erhält `OEPS_Daten` (Value Object): `Satznummer`, `Kopfnummer`, `Lebensnummer`.

## 6. Offene Fragen an den PO

1.  Wie gehen wir mit **internationalen Turnieren** (FEI) um? Das Pflichtenheft deutet an, dass auch hier OEPS-Formate genutzt werden ("Version 2.4 für internationale Bewerbe"), aber die FEI hat eigene Formate. Welches ist führend?
2.  Soll das System den **Import** der `zns.zip` (Stammdaten) unterstützen? Das wäre essenziell für den Offline-Betrieb.
3.  Wie strikt soll die **Validierung** sein? Soll das System eine Nennung *verhindern*, wenn die Startkarte fehlt, oder nur *warnen*? (Realität: Oft wird vor Ort nachgezahlt).
