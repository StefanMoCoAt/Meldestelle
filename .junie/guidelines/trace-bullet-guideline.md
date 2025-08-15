# Guideline: Zyklus "Tracer Bullet"

* **Zyklus-Start:** 15. August 2025
* **Status:** In Arbeit
* **Basis:** Diese Guideline erweitert die [Master-Guideline](./master-guideline.md)

## 1. Ziel des Zyklus

Das oberste und einzige Ziel dieses Entwicklungszyklus ist die **Validierung der gesamten technischen Architektur
End-to-End**. Wir wollen beweisen, dass eine Anfrage vom Client den gesamten technischen Stack (Gateway, Service
Discovery, Backend-Service) erfolgreich durchlaufen und eine Antwort zurückliefern kann.

Am Ende dieses Zyklus werden wir einen stabilen, qualitätsgesicherten und dokumentierten Unterbau haben, auf dem die
Entwicklung der fachlichen Features aufsetzen kann.

## 2. Umfang (Was gehört zu diesem Zyklus?)

Die folgenden Module und Aufgaben sind Teil dieses Zyklus:

* **Backend-Infrastruktur (`:core` & `:infrastructure`):**
    * Vollständige Überarbeitung, Optimierung und Testabdeckung aller Infrastruktur-Module (`cache`, `event-store`,
      `auth`, `messaging`, `monitoring`, `gateway`).
    * Implementierung einer robusten Logging- und Konfigurations-Infrastruktur.
* **Temporärer Test-Service (`:temp:ping-service`):**
    * Erstellung eines minimalen Spring-Boot-Service, der nur einen `GET /ping`-Endpunkt bereitstellt.
* **Frontend-Infrastruktur (`:client`):**
    * Aufbau einer sauberen, leeren Grundstruktur für die Kotlin Multiplatform App nach dem MVVM-Muster.
    * Implementierung einer minimalen UI mit einem "Ping"-Button und einem Anzeigefeld für die Antwort.

## 3. Spezifische Richtlinien für diesen Zyklus

* **Fokus auf Technik, nicht Fachlichkeit:** Jede Zeile Code, die in diesem Zyklus geschrieben wird, dient
  ausschließlich der Stabilisierung der technischen Infrastruktur. Es wird keine komplexe Geschäftslogik implementiert.
* **Qualitätsstandards gelten uneingeschränkt:** Auch für diesen technischen Zyklus gelten alle Regeln der
  Master-Guideline. Insbesondere:
    * **Tests sind Pflicht:** Jede neue oder geänderte Komponente muss durch Tests (insbesondere **Testcontainers** für
      Infrastruktur) abgesichert werden.
    * **Kein `println`:** Es wird ausschließlich der strukturierte Logger verwendet.
* **Dokumentation ist Teil der Aufgabe:** Jedes Modul, das wir überarbeiten, wird mit einer aktualisierten und präzisen
  `README.md`-Datei abgeschlossen.

## 4. Definition of Done (Wann sind wir fertig?)

Dieser Zyklus ist abgeschlossen, wenn **alle** der folgenden Kriterien erfüllt sind:

* [ ] Alle `:core` und `:infrastructure`-Module wurden überarbeitet, sind fehlerfrei testbar und ihre `README.md`
  -Dateien sind auf dem neuesten Stand.
* [ ] Der `:temp:ping-service` ist implementiert, getestet und lauffähig.
* [ ] Die `:client:web-app` ist mit einer sauberen MVVM-Struktur aufgesetzt und startet fehlerfrei.
* [ ] **Der End-to-End "Tracer Bullet"-Test ist erfolgreich:**
    * [ ] Alle Docker-Container (`docker-compose up`) starten.
    * [ ] Der `gateway`-Service startet.
    * [ ] Der `ping-service` startet und registriert sich erfolgreich bei Consul.
    * [ ] Die `web-app` startet.
    * [ ] Ein Klick auf den "Ping"-Button in der Web-App führt zu einer `GET`-Anfrage an das Gateway, wird korrekt an
      den `ping-service` weitergeleitet und die Antwort `"pong"` wird erfolgreich in der UI angezeigt.
* [ ] Der gesamte `clean build` des Projekts läuft ohne Fehler und **ohne Warnungen**.
* [ ] Die `master-guideline.md` und die `trace-bullet-guideline.md` sind finalisiert.

## 5. Lessons Learned (nach Abschluss)

- [ ] Was hat gut funktioniert?
- [ ] Was würden wir beim nächsten Zyklus anders machen?
- [ ] Welche Standards müssen in die Master-Guideline übernommen werden?
