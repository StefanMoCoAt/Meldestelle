# Infrastructure/Messaging Module

## Überblick

Das **Messaging-Modul** stellt die Infrastruktur für die asynchrone, reaktive Kommunikation zwischen den Microservices bereit. Es nutzt **Apache Kafka** als hochperformanten, verteilten Message-Broker und ist entscheidend für die Entkopplung von Services und die Implementierung einer skalierbaren, ereignisgesteuerten Architektur.

## Architektur

Das Modul ist in zwei spezialisierte Komponenten aufgeteilt, um Konfiguration von der Client-Logik zu trennen:


infrastructure/messaging/
├── messaging-config/   # Stellt die zentrale Kafka-Konfiguration bereit
└── messaging-client/   # Stellt wiederverwendbare, reaktive Clients bereit


### `messaging-config`

Dieses Modul zentralisiert die grundlegende Kafka-Konfiguration für das gesamte Projekt.

* **Zweck:** Definiert Spring-Beans für die `ProducerFactory` (Basis für Producer) und eine `Map` mit Standard-Konfigurationen für Consumer (z.B. `bootstrap-servers`, `group-id`, Serializer).
* **Vorteil:** Stellt Konsistenz sicher und vereinfacht die Einrichtung neuer Producer oder Consumer in den Services.

### `messaging-client`

Dieses Modul baut auf der Konfiguration auf und stellt wiederverwendbare High-Level-Komponenten für die Interaktion mit Kafka bereit.

* **Zweck:**
    * **`KafkaEventPublisher`**: Ein reaktiver, nicht-blockierender Service zum Senden von Nachrichten. Er nutzt den `ReactiveKafkaProducerTemplate` von Spring.
    * **`KafkaEventConsumer`**: Ein reaktiver Service zum Empfangen von Nachrichten. Er kapselt die Komplexität von `reactor-kafka` und gibt einen kontinuierlichen `Flux`-Stream von Events zurück.
* **Vorteil:** Kapselt die Komplexität der reaktiven Kafka-API. Ein Fach-Service muss nur noch reaktive Streams (`Mono`, `Flux`) handhaben, ohne sich um die Details der Kafka-Interaktion zu kümmern.

## Verwendung

Ein Microservice, der Nachrichten senden oder empfangen möchte, deklariert eine Abhängigkeit zu `:infrastructure:messaging:messaging-client` und injiziert die entsprechenden Interfaces.

**Beispiel für das Senden einer Nachricht (nicht-blockierend):**
```kotlin
@Service
class EventNotificationService(
    private val eventPublisher: EventPublisher
) {
    fun notifyNewEvent(eventDetails: EventDetails) {
        val topic = "new-events-topic"
        eventPublisher.publishEvent(topic, eventDetails.id, eventDetails)
            .subscribe(
                null, // onComplete: Nichts zu tun
                { error -> logger.error("Failed to send message to topic '{}'", topic, error) }
            )
        // Die Methode kehrt sofort zurück, ohne auf die Bestätigung von Kafka zu warten.
    }
}
```

**Beispiel für das Empfangen von Nachrichten (reaktiv):**
```kotlin
@Component
class EventListener(
    private val eventConsumer: EventConsumer
) {
    @PostConstruct
    fun listenForEvents() {
        val topic = "new-events-topic"
        eventConsumer.receiveEvents<EventDetails>(topic)
            .subscribe { event ->
                logger.info("Received new event with ID: {}", event.id)
                // Geschäftslogik zur Verarbeitung des Events...
            }
    }
}
```

## Testing-Strategie

Die Zuverlässigkeit des Moduls wird durch einen umfassenden Integrationstest sichergestellt, der auf dem "Goldstandard"-Prinzip beruht:

* **Testcontainers: Der KafkaIntegrationTest startet einen echten Apachen Kafka Docker-Container, um die Funktionalität unter realen Bedingungen zu validieren.*

* **Reaktives Testen: Der Test nutzt Project Reactor's StepVerifier, um die reaktiven Streams (Mono, Flux) deterministisch und ohne unzuverlässige Thread.sleep-Aufrufe zu überprüfen.*

* **Lifecycle Management: Der Test-Lebenszyklus wird sauber über @BeforeEach und @AfterEach verwaltet, um sicherzustellen, dass alle Ressourcen (insbesondere Producer-Threads) nach jedem Test korrekt freigegeben werden.*

---

**Letzte Aktualisierung**: 9. August 2025
