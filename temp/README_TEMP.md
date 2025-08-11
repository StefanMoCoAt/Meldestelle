# Temp / Ping-Service

## ⚠️ Wichtiger Hinweis

Dieses Modul (`:temp:ping-service`) ist ein **temporärer Service** ausschließlich für Testzwecke. Seine einzige Aufgabe ist die Validierung der technischen Infrastruktur im Rahmen des **"Tracer Bullet"-Szenarios**.

Nachdem der End-to-End-Test erfolgreich war, sollte dieses Modul in der `settings.gradle.kts` wieder deaktiviert oder vollständig entfernt werden.

## 1. Überblick

Der `ping-service` ist ein minimaler Spring Boot Microservice, der beweisen soll, dass die grundlegende Service-Architektur funktioniert. Dies beinhaltet:
* Korrekte Konfiguration und Start einer Spring Boot Anwendung.
* Bereitstellung eines einfachen REST-Endpunkts.
* Einbindung in die Gradle-Build-Logik.
* Integration in das Test-Framework.

## 2. Funktionalität

Der Service stellt einen einzigen HTTP-Endpunkt zur Verfügung:

* **`GET /ping`**
    * **Antwort:** Gibt ein einfaches JSON-Objekt zurück, das den erfolgreichen Aufruf bestätigt.
    * **Beispiel-Antwort-Body:**
        ```json
        {
          "status": "pong"
        }
        ```

## 3. Konfiguration

Die Konfiguration des Services erfolgt über die `application.yml`-Datei.

* **`spring.application.name`**: `ping-service`
* **`server.port`**: `8082`

## 4. Wie man den Service startet

Um den Service lokal zu starten, führen Sie den folgenden Gradle-Befehl aus:

```bash
./gradlew :temp:ping-service:bootRun
```

## 5. Wie man den Service testet

Nach dem Start können Sie die Funktionalität mit einem einfachen curl-Befehl überprüfen:

```bash
curl http://localhost:8082/ping
```
