# Glossar der Domäne "Meldestelle"

Dieses Dokument definiert die **Ubiquitous Language** (allgegenwärtige Sprache) des Projekts. Alle Begriffe sind so zu verwenden, wie sie hier definiert sind – sowohl im Code als auch in der Kommunikation.

## A - E

*   **Abteilung:** Eine Unterteilung eines -> *Bewerbs*. Oft werden Bewerbe mit vielen Startern in mehrere Abteilungen geteilt (z.B. nach Lizenzklasse oder Rasse), die getrennt gewertet werden.
*   **Akteur:** Oberbegriff für alle Personen (Reiter, Richter, Besitzer) und Organisationen (Vereine), die im System interagieren.
*   **Ausschreibung:** Das offizielle Dokument, das alle Bedingungen eines -> *Turniers* festlegt.
*   **Bewerb:** Die einzelne sportliche Prüfung (z.B. "Springprüfung Kl. L"). Kleinste Einheit für Nennungen und Ergebnisse.
*   **Event:** Der organisatorische Rahmen (z.B. "Pferdefest 2026"), der ein oder mehrere -> *Turniere* beinhalten kann.

## F - J

*   **FEI-ID:** Eindeutige Identifikationsnummer der Internationalen Reiterlichen Vereinigung (FEI) für Reiter und Pferde.
*   **Gastreiter:** Ein Reiter mit ausländischer Staatsbürgerschaft, der nicht für einen österreichischen Verein startet.
*   **Kopfnummer:**
    *   *National (OEPS):* Die permanente, 4-stellige Registrierungsnummer eines Pferdes beim OEPS (z.B. "A123"). Wird oft am Zaumzeug getragen.
    *   *International/Turnier:* Eine temporäre Startnummer für das spezifische Turnier.

## K - O

*   **Lebensnummer:** Eine 9-stellige Nummer (bzw. 15-stellig international), die ein Pferd bei der Geburt vom Zuchtverband erhält. Dient der eindeutigen Identifizierung, ist aber im OEPS-Kontext bei ausländischen Pferden oft generiert und daher nicht zur Suche geeignet.
*   **Lizenz:** Die Qualifikationsstufe eines Reiters (z.B. "R1", "RD3"). Bestimmt, in welchen Klassen er startberechtigt ist.
*   **Nennung:** Die verbindliche Anmeldung eines Paares (Reiter & Pferd) zu einem -> *Bewerb*.
*   **OEPS:** Österreichischer Pferdesportverband.

## P - T

*   **Satznummer:**
    *   *Pferd:* 10-stellige, rein numerische ID (z.B. `0000123456`), die ein Pferd in der OEPS-Datenbank eindeutig identifiziert. **Primärer Schlüssel für den Datenaustausch.**
    *   *Reiter:* 6-stellige, rein numerische ID für Personen.
*   **Sperrliste:** Eine vom Verband geführte Liste von Personen oder Pferden, die aktuell nicht startberechtigt sind (meist wegen offener Zahlungen).
*   **Startkarte:** Der Nachweis, dass die Jahresgebühr für die Lizenz bezahlt wurde. Ohne aktive Startkarte ist (national) kein Start möglich.
*   **Turnier:** Die administrative Einheit (z.B. "CSN-A"), die einem spezifischen Regelwerk (ÖTO oder FEI) unterliegt.

## U - Z

*   **Wertungsserie:** Ein übergeordneter Wettbewerb (Cup, Meisterschaft), der Ergebnisse aus mehreren Bewerben/Turnieren aggregiert.
*   **ZNS:** Zentrales Nennsystem (bzw. die zugehörigen Datensätze wie `zns.zip`), über das Stammdaten und Nennungen ausgetauscht werden.
