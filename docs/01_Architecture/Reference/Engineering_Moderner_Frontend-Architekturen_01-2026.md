---
type: Reference
status: ACTIVE
owner: Lead Architect
date: 2026-02-02
---

# Engineering Moderner Frontend-Architekturen: Kotlin 2.3.0, Compose Multiplatform 1.10.0 und Gradle 9.0 für Modulare Monolithen

Der architektonische Übergang zu modularen Monolithen bietet Unternehmen die Möglichkeit, die Komplexität von
Microservices zu reduzieren und gleichzeitig eine klare Trennung der Domänenlogik beizubehalten. In Kombination mit
Kotlin Multiplatform (KMP) für Single Page Applications (SPAs) lässt sich Geschäftslogik effizient über den gesamten
Stack teilen. Die Einführung von Kotlin 2.3.0, Compose Multiplatform 1.10.0 und Gradle 9.0 stellt dabei neue Best
Practices für Build-Performance und Deployment auf.

## 1. Gradle 9.x Optimierung in der CI/CD

Gradle 9.0 führt signifikante Änderungen ein, die speziell für große Multi-Modul-Projekte wie modulare Monolithen
optimiert sind.

- **Configuration Cache als Standard:** In Gradle 9.0 ist der Configuration Cache der bevorzugte Ausführungsmodus. Durch
  das Caching des Task-Graphen werden nachfolgende Builds erheblich beschleunigt, da die Konfigurationsphase
  übersprungen wird.
- **Kotlin DSL Script Compilation Avoidance:** Durch den Einsatz von ABI-Fingerprinting erkennt Gradle 9.0, ob
  Änderungen an `.kts`-Dateien die Build-Logik tatsächlich beeinflussen. Nicht-relevante Änderungen (wie Kommentare)
  führen nicht mehr zur Neukompilierung, was die Konfigurationszeit um bis zu 60 % reduzieren kann.
- **Parallel Configuration Store and Load:** Gradle 8.11 und 9.0 unterstützen das parallele Laden und Speichern von
  Cache-Einträgen, was die Konfigurationszeit in Projekten mit hunderten Modulen halbiert.
- **Speichermanagement:** Für speicherintensive Tasks wie die JS-Kompilierung in der CI wird eine explizite
  JVM-Konfiguration empfohlen (z. B. `org.gradle.jvmargs=-Xmx8g`), um Abstürze der Runner zu vermeiden.
- **Remote Build Caching:** Die Verwendung der `gradle-cache-action` in GitHub Actions ermöglicht es ephemeral Runnern,
  als Remote-Build-Cache-Proxy zu fungieren, wodurch Task-Outputs über verschiedene Jobs hinweg geteilt werden.

## 2. Dockerisierung von KMP Web Applications (JS IR)

Eine effiziente Containerisierung erfordert die Trennung von Build-Umgebung und produktivem Webserver.

- **Multi-Stage Build:** Verwenden Sie ein JDK-Image (z. B. eclipse-temurin:21) für die Kompilierung und ein schlankes
  Image (
  Nginx oder Caddy) für die Auslieferung der statischen Assets.
- **BuildKit Cache Mounts:** Nutzen Sie Cache-Mounts für das Gradle-Verzeichnis im Dockerfile (
  `RUN--mount=type=cache,target=/root/.gradle`), um Abhängigkeiten zwischen verschiedenen Docker-Builds lokal auf dem
  Host zu persistieren.
- **Layering:** Kopieren Sie zuerst nur den Gradle-Wrapper und den Version-Catalog (`libs.versions.toml`), um den
  Download der Abhängigkeiten in einem separaten Layer zu cachen.

## 3. Umgang mit Laufzeitkonfigurationen (Environment variables)

Da JS-Bundler (Vite, Webpack) Umgebungsvariablen zur Build-Zeit auflösen, ist eine Strategie für "Build Once, Deploy
Everywhere" erforderlich.

- **config.json Fetch-Pattern:** Dies ist die empfohlene Methode für KMP-SPAs.

  1. Die App wird ohne Umgebungswerte gebaut.
  2. Beim Container-Start generiert ein `docker-entrypoint.sh` Skript eine `config.json` aus den
     aktuellen System-Umgebungsvariablen.
  3. Die Kotlin-Anwendung führt in der `main()`-Funktion einen `fetch("/config.json")` aus, bevor die UI gerendert wird.

- **Typensicherheit:** Definieren Sie ein Kotlin-Interface für die Konfiguration, die mit der JSON-Struktur des
  Entrypoint-Skripts übereinstimmt, um Laufzeitfehler zu vermeiden.

## 4. Server-Konfiguration und Compose 1.10.0 Features

Die Wahl des Webservers beeinflusst das Routing und die Performance der Compose Multiplatform Anwendung.

- **SPA Routing:** Nginx muss so konfiguriert werden, dass alle unbekannten Pfade auf die `index.html` zurückfallen (
  `try_files $uri $uri/ /index.html`), damit das clientseitige Routing funktioniert.
- **Caddy Alternative:** Caddy bietet eine einfachere Syntax für SPA-Routing (`try_files {path} /index.html`) und
  unterstützt HTTP/3 sowie automatisches HTTPS out-of-the-box.
- **Web Cache API:** Compose Multiplatform 1.10.0 integriert die Web Cache API, um statische Ressourcen und Strings
  effizient zu speichern und die Verzögerungen durch die Standard-Validierung des Browsers zu umgehen.
- **Security Header:** Für zukünftige Kotlin/Wasm-Migrationen sollten bereits jetzt COOP (`same-origin`) und COEP (
  `require-corp`) Header gesetzt werden, um Cross-Origin-Isolation zu ermöglichen.

| Komponente	   | Empfehlung                          | 	Vorteil                                               |
|---------------|-------------------------------------|--------------------------------------------------------|
| Build-Tool    | 	Gradle 9.0 mit Configuration Cache | Extreme Verkürzung der Konfigurationsphase             |
| CI Caching    | 	Remote Cache Action (Proxy)        | Wiederverwendung von Task-Outputs auf frischen Runnern |
| Konfiguration | 	Runtime config.json Fetch          | Ein Docker-Image für alle Umgebungen (Dev/Prod)        |
| Webserver     | 	Caddy oder Nginx                   | Optimiertes SPA-Routing und Web Cache Support          |

## Fazit

Die Kombination aus Gradle 9.0 und Kotlin 2.3.0 ermöglicht hocheffiziente Build-Pipelines für modulare Monolithen. Durch
den Einsatz von Multi-Stage Docker-Builds und dem `config.json`-Fetch-Muster wird eine moderne, skalierbare
Deployment-Strategie umgesetzt, die die neuen Performance-Features von Compose Multiplatform 1.10.0 optimal nutzt.


