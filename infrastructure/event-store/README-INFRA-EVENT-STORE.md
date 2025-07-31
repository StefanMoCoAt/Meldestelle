# Infrastructure/Event-Store Module

## Überblick

Das **Event-Store-Modul** ist eine kritische Komponente der Infrastruktur, die für die Persistenz und Veröffentlichung von Domänen-Events zuständig ist. Es bildet die technische Grundlage für **Event Sourcing** und eine allgemeine **event-getriebene Architektur**. Anstatt nur den aktuellen Zustand einer Entität zu speichern, speichert der Event Store die gesamte Kette von Ereignissen (Events), die zu diesem Zustand geführt haben.

## Architektur: Port-Adapter-Muster

Wie schon das Cache-Modul, folgt auch der Event Store streng dem **Port-Adapter-Muster**, um eine maximale Entkopplung von der konkreten Speichertechnologie zu erreichen.


infrastructure/event-store/
├── event-store-api/      # Der "Port": Definiert die Event-Store-Schnittstelle
└── redis-event-store/    # Der "Adapter": Implementiert die Schnittstelle mit Redis Streams


### `event-store-api`

Dieses Modul ist der **abstrakte "Port"** der Architektur. Es definiert den Vertrag, wie der Rest der Anwendung mit dem Event Store interagiert.

* **Zweck:** Definiert Interfaces wie `EventStore` (zum Speichern und Laden von Event-Streams) und `EventPublisher` (zum Veröffentlichen von Events an interessierte Listener). Es ist eng mit den `DomainEvent`-Definitionen aus dem `:core:core-domain`-Modul verknüpft.
* **Vorteil:** Die Fach-Services (z.B. `members-application`) sind vollständig von der Implementierung des Event Stores entkoppelt. Sie wissen nicht, ob die Events in Redis, Kafka oder einer relationalen Datenbank gespeichert werden.

### `redis-event-store`

Dieses Modul ist der **konkrete "Adapter"**, der die in `event-store-api` definierten Schnittstellen implementiert.

* **Zweck:** Stellt eine Implementierung des `EventStore` bereit, die **Redis Streams** als zugrunde liegenden Datenspeicher verwendet. Redis Streams sind eine leistungsstarke Datenstruktur, die sich ideal für die Implementierung eines append-only Logs eignet, wie es für einen Event Store benötigt wird.
* **Technologie:** Nutzt Spring Data Redis und den Lettuce-Client für die performante Kommunikation mit Redis. Die Domänen-Events werden vor der Speicherung mittels Jackson in ein JSON-Format serialisiert.
* **Vorteil:** Kapselt die gesamte Redis-spezifische Logik. Ein zukünftiger Wechsel zu einem anderen Event-Store-System (z.B. Apache Kafka) würde nur den Austausch dieses einen Moduls erfordern.

## Verwendung in anderen Modulen

Ein Anwendungs-Service, der Event Sourcing verwendet, interagiert wie folgt mit dem Modul:

1.  **Abhängigkeit deklarieren:** Das Service-Modul (z.B. `members-application`) fügt eine `implementation`-Abhängigkeit zu `:infrastructure:event-store:redis-event-store` in seiner `build.gradle.kts` hinzu.

2.  **Interface injizieren:** Im Service-Code wird nur das `EventStore`-Interface aus der `event-store-api` injiziert.

    ```kotlin
    // In einem Use Case oder Application Service
    @Service
    class MemberApplicationService(
        private val eventStore: EventStore, // Nur das Interface wird verwendet!
        private val eventPublisher: EventPublisher
    ) {
        fun registerNewMember(command: RegisterMemberCommand): Member {
            // 1. Geschäftslogik ausführen und ein oder mehrere Events erzeugen
            val memberRegisteredEvent = MemberRegisteredEvent(
                memberId = UUID.randomUUID(),
                name = command.name,
                // ...
            )

            // 2. Das Event im Event Store speichern
            eventStore.save(memberRegisteredEvent)

            // 3. Das Event veröffentlichen, damit andere Teile des Systems
            // (z.B. ein E-Mail-Service) darauf reagieren können.
            eventPublisher.publish(memberRegisteredEvent)

            // ...
        }
    }
    ```

Diese Architektur ermöglicht eine hochgradig entkoppelte, skalierbare und resiliente Systemlandschaft, die auf asynchroner Kommunikation basiert.

---
**Letzte Aktualisierung**: 31. Juli 2025
