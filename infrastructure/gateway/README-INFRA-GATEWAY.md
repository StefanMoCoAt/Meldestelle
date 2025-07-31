## Infrastructure/Gateway Module
Überblick
Das API-Gateway ist der zentrale und einzige öffentliche Einstiegspunkt für alle Anfragen von externen Clients (z.B. Web-Anwendung, Desktop-Anwendung, mobile Apps) an das Meldestelle-System. Es fungiert als "Pförtner" für die gesamte Microservice-Landschaft.

Kein externer Client sollte jemals direkt mit einem internen Microservice kommunizieren. Alle Anfragen laufen über das Gateway.

Architektur und Technologie
Das Gateway ist als eigenständiger Spring Boot Service implementiert und nutzt Spring Cloud Gateway als technologische Grundlage. Spring Cloud Gateway ist ein reaktives, nicht-blockierendes Framework, das sich nahtlos in das Spring-Ökosystem integriert.

Hauptverantwortlichkeiten
Das Gateway ist verantwortlich für die Handhabung aller Cross-Cutting Concerns (übergreifende Belange), die für mehrere oder alle Microservices gelten. Dies entlastet die Fach-Services von technischen Aufgaben.

Dynamisches Routing:
Das Gateway ist mit dem Consul Service Discovery integriert. Es fragt bei Consul an, welche Services unter welcher Adresse verfügbar sind, und leitet eingehende Anfragen dynamisch an die entsprechenden, gesunden Service-Instanzen weiter.
Beispiel: Eine Anfrage an /api/members/... wird automatisch an eine Instanz des members-service weitergeleitet.

## Sicherheit und Authentifizierung:
Das Gateway ist der Security Enforcement Point. Es bindet das :infrastructure:auth:auth-client-Modul ein, um jede eingehende Anfrage zu überprüfen:

Es validiert das im Authorization-Header mitgesendete JWT.

Anfragen ohne gültiges Token werden mit einem 401 Unauthorized-Fehler abgewiesen.

Nur validierte Anfragen mit einem gültigen Token werden an die internen Services weitergeleitet.

Rate Limiting:
Es schützt die Backend-Services vor Überlastung, indem es die Anzahl der Anfragen pro Client oder pro IP-Adresse begrenzt.

Monitoring und Tracing:
Durch die Einbindung des :infrastructure:monitoring:monitoring-client-Moduls generiert das Gateway Metriken über eingehenden Traffic und ist der Startpunkt für Distributed Traces. Jede Anfrage erhält eine eindeutige Trace-ID, die über alle folgenden Service-Aufrufe hinweg mitgeführt wird.

CORS-Management:
Verwaltet zentral die Cross-Origin Resource Sharing (CORS)-Richtlinien, um festzulegen, welche Web-Frontends auf die API zugreifen dürfen.

Zusammenspiel im System
Ein typischer Anfrage-Flow sieht wie folgt aus:

Ein Client (z.B. die Web-App) sendet eine Anfrage an https://api.meldestelle.at/members/123.

Das API-Gateway empfängt die Anfrage.

Gateway-Filter-Kette:
a. Der Security-Filter validiert das JWT.
b. Der Logging/Tracing-Filter startet einen neuen Trace.
c. Der Rate-Limiting-Filter prüft, ob das Limit überschritten ist.

Der Routing-Filter schaut in Consul nach, wo der members-service läuft (z.B. unter 172.18.0.5:8081).

Das Gateway leitet die Anfrage an die interne Service-Instanz weiter.

Die Antwort des members-service wird auf dem gleichen Weg zurück an den Client gesendet.

Diese Architektur schafft ein sicheres, robustes und wartbares System, indem sie die Komplexität der Infrastruktur vor den Fach-Services verbirgt.

Letzte Aktualisierung: 31. Juli 2025
