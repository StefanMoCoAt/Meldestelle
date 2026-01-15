# Analyse und Strategie zur Wiederherstellung und Strukturverbesserung

**Datum:** 2026-01-02
**Autor:** Junie
**Kontext:** Dieser Bericht wurde nach einem großen Technologie-Upgrade (Kotlin 2.1.0+, Java 25, Spring Boot 3.5.x) erstellt, als die Infrastruktur und die Build-Prozesse instabil waren.

---

Es ist eine klassische Situation: Nach einem großen Technologie-Upgrade knirscht es oft an den Schnittstellen. Da der `ping-service` als technischer Blueprint dient, ist er der absolut richtige Startpunkt.

Hier ist der Schlachtplan, um Ordnung zu schaffen und die hexagonale Architektur sauber zu etablieren:

### 1. Wo beginnen? Bottom-Up vs. Top-Down
Da die Infrastruktur aktuell nicht stabil läuft, wird ein **"Core-First"** Ansatz empfohlen, gefolgt vom **Backend-Durchstich**.

*   **Zuerst: Core & Platform:** Ohne eine stabile Basis (`platform-bom`, `platform-dependencies`, `core-domain`) werden die anderen Module immer wieder Kompilierfehler werfen.
*   **Dann: Der technische vertikale Durchstich (`ping-service`):** Sobald die Plattform steht, wird der Weg repariert: `Infrastruktur (Docker) -> Ping-Service -> Gateway`.
*   **Zuletzt: Frontend:** Das Frontend (BFF-Gedanke) wird erst dann stabil, wenn die API-Contracts des Backends wieder verlässlich geliefert werden.

### 2. Ordnung schaffen: Der "Clean Desk" im Projekt
Bevor Code gefixt wird, muss die Build-Umgebung aufgeräumt werden:

1.  **Version Catalog Synchronität:** Die `libs.versions.toml` nutzt bereits Java 25 und Kotlin 2.1.0. Es muss geprüft werden, ob alle Gradle-Plugins (insbesondere das `compose-multiplatform` und `spring-boot` Plugin) mit Kotlin 2.1.0 kompatibel sind.
2.  **Modul-Konsolidierung (DDD):** Die "Modul-Explosion" sollte reduziert werden.
    *   **Vorschlag:** Statt 5 Module pro Domain (`api`, `common`, `domain`, `infrastructure`, `service`), auf maximal zwei reduzieren:
        *   `domain-api`: Nur DTOs und Interfaces (für KMP-Sharing mit dem Frontend).
        *   `domain-service`: Die gesamte Implementierung (Hexagonal strukturiert in Packages).

### 3. Hexagonale Architektur im `ping-service` umsetzen
Der `ping-service` ist aktuell noch sehr "Spring-lastig". Für eine echte hexagonale Vorlage sollte das Modul `ping-service` intern wie folgt umstrukturiert werden:

```text
at.mocode.ping.service
├── adapter
│   ├── in
│   │   └── web (PingController - Dein primärer Port-Adapter)
│   └── out
│       └── persistence (PingRepositoryAdapter - Sekundärer Port-Adapter)
├── application
│   ├── port
│   │   ├── in (PingUseCase - Das Interface für den Controller)
│   │   └── out (PingOutputPort - Interface für die Datenbank)
│   └── service (PingService - Hier liegt die Business Logik, OHNE Spring-Annotationen wo möglich)
└── domain
    └── model (PingEntity/Value Objects)
```

### 4. Konkrete Schritte zur Reparatur

**Schritt 1: Infrastruktur-Check (Docker)**
*   Check `docker-compose.yaml`: Laufen Postgres und Keycloak?
*   `ping-service` application.yaml: Die Datenbank-Verbindung (JPA) aktivieren.

**Schritt 2: Backend API-Gateway Fix**
*   Die Security-Konfiguration (`SecurityConfig.kt`) wegen Bibliotheks-Änderungen in Spring Security 7/Spring Boot 3.5 prüfen.

**Schritt 3: Frontend (BFF) Anpassung**
*   Der `PingApiClient` im Frontend sollte gegen das **Gateway** (BFF-Pattern) laufen, nicht direkt gegen den Service.

### Empfehlung zur Vorgehensweise (Prioritäten):
1.  **Gradle-Build stabilisieren:** Alle `:platform:*` und `:core:*` Module müssen mit `./gradlew build` fehlerfrei durchlaufen.
2.  **Ping-SCS fertigstellen:** Eine minimale Datenbank-Speicherung im `ping-service` implementieren.
3.  **Gateway-Security:** Sicherstellen, dass das JWT von Keycloak korrekt zum `ping-service` durchgereicht wird.
