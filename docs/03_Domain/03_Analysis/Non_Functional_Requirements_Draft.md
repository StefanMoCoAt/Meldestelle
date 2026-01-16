# Non-Functional Requirements (NFRs) - Phase 1

*   **Status:** Draft
*   **Fokus:** Offline-First Architektur, Robustheit, Datenintegrität

---

## 1. Offline-Fähigkeit & Resilienz (Availability)

Das System muss in einer Umgebung funktionieren, in der Netzwerkverbindungen unzuverlässig oder nicht vorhanden sind (Reitställe, ländliche Gebiete).

*   **NFR-01: Local-First Prinzip**
    *   Alle Kernfunktionen (Nennung, Startlistenerstellung, Ergebniserfassung, Drucken) müssen **zu 100% ohne Netzwerkverbindung** ausführbar sein.
    *   Die lokale Datenbank (SQLite/SQLDelight) ist die primäre Datenquelle für das UI.
    *   Der Server dient lediglich als Synchronisations-Hub und Backup, nicht als Laufzeit-Abhängigkeit.

*   **NFR-02: Synchronisation & Konfliktlösung**
    *   Sobald eine Verbindung besteht, müssen Daten im Hintergrund synchronisiert werden.
    *   **Konfliktstrategie:** Bei konkurrierenden Änderungen (z.B. zwei Richter ändern dasselbe Ergebnis) muss das System:
        1.  Technische Konflikte automatisch lösen (z.B. "Last Write Wins" basierend auf präzisen Zeitstempeln).
        2.  Fachliche Konflikte protokollieren und zur manuellen Klärung markieren.

*   **NFR-03: Wiederherstellung (Disaster Recovery)**
    *   Nach einem Absturz oder Stromausfall muss das System innerhalb von **< 30 Sekunden** wieder betriebsbereit sein.
    *   Kein Datenverlust von bereits bestätigten Eingaben (ACID-Transaktionen lokal).

---

## 2. Performance & Latenz (Usability)

Im Turnierbetrieb herrscht Zeitdruck. Wartezeiten summieren sich und führen zu Stress bei den Anwendern.

*   **NFR-04: Optimistic UI Updates**
    *   Benutzeraktionen (z.B. Speichern einer Note) müssen im UI **sofort (< 50ms)** bestätigt werden, ohne auf Netzwerk-Roundtrips oder Datenbank-Commits zu warten (Asynchrone Verarbeitung).

*   **NFR-05: Such-Performance**
    *   Die Suche nach Pferden (in > 50.000 Stammdaten) oder Reitern muss **< 200ms** dauern (Full-Text-Search Indexierung lokal).
    *   Dies gilt auch auf leistungsschwächerer Hardware (ältere Laptops, Tablets).

*   **NFR-06: Massendaten-Verarbeitung**
    *   Der Import der `zns.zip` (Stammdaten) darf den UI-Thread nicht blockieren und sollte **< 5 Minuten** dauern.

---

## 3. Datenintegrität & Audit (Compliance)

Ergebnisse entscheiden über Qualifikationen und Preisgelder. Manipulationen oder versehentliche Änderungen müssen nachvollziehbar sein.

*   **NFR-07: Audit Trail**
    *   Jede Änderung an einem Ergebnis (Score, Zeit, Platzierung) muss unveränderbar protokolliert werden.
    *   Inhalt: `Timestamp`, `User-ID`, `Old-Value`, `New-Value`, `Reason` (optional).
    *   Der Audit-Log muss mit synchronisiert werden.

*   **NFR-08: Validierungs-Hierarchie**
    *   Das System muss zwischen "Hard Constraints" (Datenbank-Integrität, z.B. Foreign Keys) und "Soft Constraints" (Fachliche Regeln, z.B. fehlende Startkarte) unterscheiden.
    *   Soft Constraints dürfen den Prozess nicht blockieren, müssen aber persistente Warnungen erzeugen ("Override"-Flag).

---

## 4. Sicherheit (Security)

*   **NFR-09: Lokale Datensicherheit**
    *   Da Laptops/Tablets gestohlen werden können: Sensible Daten (Personendaten, Adressen) sollten "At Rest" verschlüsselt sein (z.B. SQLCipher), sofern die Performance (NFR-05) nicht kritisch beeinträchtigt wird.
    *   Minimalanforderung: Keine Speicherung von Passwörtern im Klartext.

*   **NFR-10: Rollenbasierter Zugriff (RBAC)**
    *   Unterscheidung der Berechtigungen im UI:
        *   *Richter:* Darf nur Ergebnisse für zugewiesene Bewerbe eingeben.
        *   *Meldestelle:* Vollzugriff.
        *   *Zuschauer (Kiosk-Mode):* Nur Lesezugriff auf Starter-/Ergebnislisten.

---

## 5. Hardware & Umgebung

*   **NFR-11: Eingabe-Effizienz**
    *   Die Ergebniserfassung muss "Keyboard-First" bedienbar sein (Nummernblock-Optimierung). Maus/Touch ist für Massenerfassung zu langsam.

*   **NFR-12: Druck-Unterstützung**
    *   Unterstützung von lokalen Druckern (USB/Netzwerk) ohne komplexe Treiber-Installation, da Listen (Starterlisten, Ergebnisse) physisch ausgehängt werden müssen (Pflicht laut Reglement).
