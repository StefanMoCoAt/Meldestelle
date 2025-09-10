# Guideline: Zyklus "Tracer Bullet"

* **Zyklus-Start:** 15. August 2025
* **Status:** In Arbeit
* **Basis:** Diese Guideline erweitert die [Master-Guideline](./master-guideline.md)
* **Frontend-Standard:** Alle Web-Frontend-Entwicklung erfolgt gemäß der [`web-app-guideline.md`](./web-app-guideline.md), die ab sofort der verbindliche Standard ist.

## 1. Ziel des Zyklus

Das oberste und einzige Ziel dieses Entwicklungszyklus ist die **Validierung der gesamten technischen Architektur
End-to-End**. Wir wollen beweisen, dass eine Anfrage vom Client den gesamten technischen Stack (Gateway, Service
Discovery, Backend-Service) erfolgreich durchlaufen und eine Antwort zurückliefern kann.

Am Ende dieses Zyklus werden wir einen stabilen, qualitätsgesicherten und dokumentierten Unterbau haben, auf dem die
Entwicklung der fachlichen Features aufsetzen kann.

## 2. Umfang (Was gehört zu diesem Zyklus?)

Die folgenden Module und Aufgaben sind Teil dieses Zyklus:

### 2.1. Backend-Infrastruktur (`:core` & `:infrastructure`):

* Vollständige Überarbeitung, Optimierung und Testabdeckung aller Infrastruktur-Module (`cache`, `event-store`,
  `auth`, `messaging`, `monitoring`, `gateway`).
* Implementierung einer robusten Logging- und Konfigurations-Infrastruktur.

### 2.2. Temporärer Test-Service (`:temp:ping-service`):

* Erstellung eines minimalen Spring-Boot-Service, der nur einen `GET /ping`-Endpunkt bereitstellt.

* **Frontend-Infrastruktur (`:client`):**
    * Aufbau einer sauberen, leeren Grundstruktur für die Kotlin Multiplatform App nach dem MVVM-Muster.
    * Implementierung einer minimalen UI mit einem "Ping"-Button und einem Anzeigefeld für die Antwort.

### 2.3. Frontend-Infrastruktur (:client)

* **Aufgabe:** Aufbau einer sauberen Grundstruktur für die Kotlin Multiplatform App nach dem **MVVM-Muster** und Implementierung der **"Ping"**-Funktionalität. Die Umsetzung erfolgt mit **Compose for Web** gemäß der [`web-app-guideline.md`](./web-app-guideline.md).
* **Status:** 🔳 In Arbeit.
* **Spezifische Anforderungen & Test-Szenarien:**
    * **UI-Komponenten:** Die UI muss einen Button ("Ping Backend") und ein Textfeld zur Statusanzeige enthalten, umgesetzt als `@Composable`-Funktionen.
        * **Zustands-Management:** Die UI muss vier Zustände klar und visuell unterscheidbar abbilden:
            1. **Initialzustand:** Neutrale Nachricht ("Klicke auf den Button …"), Button aktiv.
            2. **Ladezustand:** Lade-Nachricht ("Pinge Backend …"), Button deaktiviert.
            3. **Erfolgszustand:** Positive Antwort ("Antwort vom Backend: pong"), Button aktiv.
            4. **Fehlerzustand:** Klare Fehlermeldung ("Fehler: ..."), Button aktiv.
        * **Architektur:** Der API-Aufruf muss nach dem **MVVM-Muster** gekapselt sein, wobei die UI (`jsMain`) das ViewModel aus `commonMain` konsumiert.

## 3. Spezifische Richtlinien für diesen Zyklus

* **Fokus auf Technik, nicht Fachlichkeit:** Jede Zeile Code, die in diesem Zyklus geschrieben wird, dient
  ausschließlich der Stabilisierung der technischen Infrastruktur. Es wird keine komplexe Geschäftslogik implementiert.
* **Qualitätsstandards gelten uneingeschränkt:** Auch für diesen technischen Zyklus gelten alle Regeln der
  Master-Guideline. Insbesondere:
    * **Minimale, aber essentielle Tests:** Für den "Tracer-Bullet"-Zyklus sind nur die **absolut notwendigen Tests** erforderlich, die beweisen, dass die Kernfunktionalität gegeben ist. Komplexere Testsuites sind für die Architektur-Validierung nicht notwendig.
        * **Beispiel Monitoring:** Nur ein "Smoke-Test" für den monitoring-server (startet er überhaupt?) ist essentiell für den E2E-Test.
    * **Kein `println`:** Es wird ausschließlich der strukturierte Logger verwendet.
* **Dokumentation ist Teil der Aufgabe:** Jedes Modul, das wir überarbeiten, wird mit einer aktualisierten und präzisen
  `README.md`-Datei abgeschlossen.

## 4. Definition of Done (Wann sind wir fertig?)

Dieser Zyklus ist abgeschlossen, wenn **alle** der folgenden Kriterien erfüllt sind:

* [x] Alle `:core` und `:infrastructure`-Module wurden überarbeitet, sind fehlerfrei testbar und ihre `README.md`
  -Dateien sind auf dem neuesten Stand.
* [x] Der `:temp:ping-service` ist implementiert, getestet und lauffähig.
* [ ] Die `:client:web-app` ist mit einer sauberen MVVM-Struktur aufgesetzt, startet fehlerfrei und implementiert den Ping-Test mit **Compose for Web**.
* [ ] **Der End-to-End "Tracer Bullet"-Test ist erfolgreich:**
    * [ ] Alle Docker-Container (`docker-compose up`) starten fehlerfrei.
    * [ ] Der `gateway`-Service startet.
    * [ ] Der `ping-service` startet und registriert sich erfolgreich bei Consul.
    * [ ] Die `web-app` startet.
    * [ ] Ein Klick auf den "Ping"-Button in der Web-App führt zu einer `GET`-Anfrage an das Gateway, wird korrekt an
      den `ping-service` weitergeleitet und die Antwort `"pong"` wird erfolgreich in der UI angezeigt.
* [ ] Der gesamte `clean build` des Projekts läuft ohne Fehler und **ohne Warnungen**. *(Status: Build läuft durch, aber mit 5 Testfehlern und mehreren Kotlin-Warnungen)*
* [ ] Die `master-guideline.md` und die `trace-bullet-guideline.md` sind finalisiert.

---

## Status-Update (Stand: 16. August 2025, 10:54 Uhr)

### ✅ **Bereits erledigt:**
1. **Strukturelle Komponenten sind implementiert:**
   - Alle `:core` Module (core-domain, core-utils) mit README-CORE.md
   - Alle `:infrastructure` Module (auth, cache, event-store, gateway, messaging, monitoring) mit README-INFRASTRUCTURE.md
   - `:temp:ping-service` mit README_TEMP.md
   - `:client` Module (common-ui, desktop-app, web-app) mit ClientModuleDocumentation.md

### ❌ **Noch offen:**
1. **End-to-End "Tracer Bullet"-Test:** Nicht durchführbar, da docker-compose nicht installiert
2. **Clean Build ohne Warnungen:**
   - 5 Testfehler (4 in auth-client, 1 in redis-event-store)
   - Multiple Kotlin-Warnungen und Deprecation-Warnings
3. **Funktionale Validierung:** Ohne Docker-Umgebung nicht testbar
4. **Guideline-Finalisierung:** Diese Überprüfung abgeschlossen, aber master-guideline.md Status unbekannt

### 🔧 **Nächste Schritte:**
1. Testfehler in auth-client (Performance- und Security-Tests) beheben
2. Testfehler in redis-event-store beheben
3. Kotlin-Warnungen und Deprecation-Warnings eliminieren
4. Docker-Umgebung einrichten und End-to-End-Test durchführen
5. Master-Guideline finalisieren

---

## 5. Lessons Learned (nach Abschluss)

- [ ] Was hat gut funktioniert?
- [ ] Was würden wir beim nächsten Zyklus anders machen?
- [ ] Welche Standards müssen in die Master-Guideline übernommen werden?
